/**
 *  '$RCSfile: MultipartForm.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2002-07-12 17:21:50 $'
 * '$Revision: 1.3 $'
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 *  Portions of this file were derived from the HTTPClient package
 *  Copyright (C) 1996-2001 Ronald Tschalär
 *
 *  The HTTPClient's home page is located at:
 *
 *  http://www.innovation.ch/java/HTTPClient/ 
 *
 */

package edu.ucsb.nceas.morpho.framework;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.BitSet;

import HTTPClient.NVPair;
import HTTPClient.FilenameMangler;

/**
 * This class represents name/value pairs and files in a byte array
 * using the multipart/form-data encoding. After creating an instance,
 * call "getLength()" to determine the content length of the encoded
 * form, and call "writeEncodedMultipartForm()" to write the form
 * to a stream. This is useful for sending large files in a stream
 * using HTTP POST (of course, you have to use HTTPClient to replace
 * the default http protocol handler in order to avoid buffering the 
 * stream).
 * <BR>Example:
 * <PRE>
 *     NVPair[] opts = { new NVPair("option", "doit") };
 *     NVPair[] file = { new NVPair("comment", "comment.txt") };
 *     NVPair[] hdrs = new NVPair[1];
 *     MultipartForm myform = new MultipartForm(opts, file);
 *     long length = myform.getLength();
 *     URL url = new URL("http://foo.bar.com");
 *     URLConnection con = url.openConnection();
 *     ((HttpURLConnection)con).setRequestMethod("POST");
 *     ((HttpURLConnection)con).setRequestProperty("Content-Length",
 *              new Long(length).toString());
 *     con.setDoInput(true);
 *     con.setDoOutput(true);
 *     con.setUseCaches(false);
 *     OutputStream out = con.getOutputStream();
 *     myform.writeEncodedMultipartForm(out);
 * </PRE>
 * The data written to <VAR>out</VAR> will look something like 
 * the following:
 * <PRE>
 * -----------------------------114975832116442893661388290519
 * Content-Disposition: form-data; name="option"
 *                                                         &nbsp;
 * doit
 * -----------------------------114975832116442893661388290519
 * Content-Disposition: form-data; name="comment"; filename="comment.txt"
 * Content-Type: text/plain
 *                                                         &nbsp;
 * Gnus and Gnats are not Gnomes.
 * -----------------------------114975832116442893661388290519--
 * </PRE>
 * where the "Gnus and Gnats ..." is the contents of the file
 * <VAR>comment.txt</VAR> in the current directory.
 *
 * <P>If no elements are found in the parameters then a zero-length
 * no data is written to out and the content-type is set to
 * <var>application/octet-string</var> (because a multipart must
 * always have at least one part.
 *
 * <P>For files an attempt is made to discover the content-type, and if
 * found a Content-Type header will be added to that part. The content type
 * is retrieved using java.net.URLConnection.guessContentTypeFromName() -
 * see java.net.URLConnection.setFileNameMap() for how to modify that map.
 * Note that under JDK 1.1 by default the map seems to be empty. If you
 * experience troubles getting the server to accept the data then make
 * sure the fileNameMap is returning a content-type for each file (this
 * may mean you'll have to set your own).
 *
 */
public class MultipartForm
{
    private static BitSet  BoundChar;
    private final static String ContDisp = 
                                "\r\nContent-Disposition: form-data; name=\"";
    private final static String FileName = "\"; filename=\"";
    private final static String ContType = "\r\nContent-Type: ";
    private final static String Boundary = 
       "\r\n----------ieoau._._+2_8_8.3-dskdfJwSJKl234324jfLdsjfdAuaoei-----";
    private static NVPair[] dummy = new NVPair[0];

    // Class Initializer
    static
    {
	// rfc-2046 & rfc-2045: (bcharsnospace & token)
	// used for multipart codings
	BoundChar = new BitSet(256);
	for (int ch='0'; ch <= '9'; ch++)  BoundChar.set(ch);
	for (int ch='A'; ch <= 'Z'; ch++)  BoundChar.set(ch);
	for (int ch='a'; ch <= 'z'; ch++)  BoundChar.set(ch);
	BoundChar.set('+');
	BoundChar.set('_');
	BoundChar.set('-');
	BoundChar.set('.');
    }

    // Members
    private byte[] boundary, cont_disp, cont_type, filename;
    private long encodedLength;
    private NVPair opts[];
    private NVPair files[];
    private FilenameMangler mangler;
    private NVPair content_header = null;

    // Constructors

    /**
     * Create a new form with form data and files using the multipart/form-data 
     * encoding and use the given FilenameMangler to alter filenames 
     * before encoding.
     *
     * @param     opts        the simple form-data to encode (may be null);
     *                        for each NVPair the name refers to the 'name'
     *                        attribute to be used in the header of the part,
     *                        and the value is contents of the part.
     *                        null elements in the array are ingored.
     * @param     files       the files to encode (may be null); for each
     *                        NVPair the name refers to the 'name' attribute
     *                        to be used in the header of the part, and the
     *                        value is the actual filename (the file will be
     *                        read and it's contents put in the body of
     *                        that part). null elements in the array
     *                        are ingored.
     * @param     mangler     the filename mangler, or null if no mangling is
     *                        to be done. This allows you to change the name
     *                        used in the <var>filename</var> attribute of the
     *                        Content-Disposition header. Note: the mangler
     *                        will be invoked twice for each filename.
     * @exception IOException If any file operation fails.
     * @see #getLength()
     * @see #writeEncodedMultipartForm(OutputStream out, int bufferSize)
     */
    public MultipartForm(NVPair opts[], NVPair files[],
                         FilenameMangler mangler) throws IOException
    {
      boundary  = Boundary.getBytes("8859_1");
      cont_disp = ContDisp.getBytes("8859_1");
      cont_type = ContType.getBytes("8859_1");
      filename  = FileName.getBytes("8859_1");
      this.opts = opts;
      this.files = files;
      this.mangler = mangler;
      this.encodedLength = calculateLength();
    }

    /**
     * Create a new form with form data and files using the multipart/form-data 
     * encoding. 
     *
     * @param     opts        the simple form-data to encode (may be null);
     *                        for each NVPair the name refers to the 'name'
     *                        attribute to be used in the header of the part,
     *                        and the value is contents of the part.
     * @param     files       the files to encode (may be null); for each
     *                        NVPair the name refers to the 'name' attribute
     *                        to be used in the header of the part, and the
     *                        value is the actual filename (the file will be
     *                        read and it's contents put in the body of that
     *                        part).
     * @return                an encoded byte array containing all the opts
     *			      and files.
     * @exception IOException If any file operation fails.
     * @see #getLength()
     * @see #writeEncodedMultipartForm(OutputStream out, int bufferSize)
     */
    public MultipartForm(NVPair opts[], NVPair files[]) throws IOException
    {
      this(opts, files, null);
    }

    /**
     * Calculate the length in bytes of this form after encoding.  
     *
     * @exception IOException If any file operation fails.
     * @return the length of the encoded form in bytes
     */
    private long calculateLength() throws IOException
    {
	int len = 0,
	    hdr_len = boundary.length + cont_disp.length+1 + 2 +  2;
	    //        \r\n --  bnd      \r\n C-D: ..; n=".." \r\n \r\n

	if (opts == null)   opts  = dummy;
	if (files == null)  files = dummy;


	// Calculate the length of the parameters
	for (int idx=0; idx<opts.length; idx++)
	{
	    if (opts[idx] == null)  continue;

	    len += hdr_len + opts[idx].getName().length() +
		   opts[idx].getValue().length();
	}

	// Calculate the length of the files
	for (int idx=0; idx<files.length; idx++)
	{
	    if (files[idx] == null)  continue;

	    File file = new File(files[idx].getValue());
	    String fname = file.getName();
	    if (mangler != null)
		fname = mangler.mangleFilename(fname, files[idx].getName());
	    if (fname != null)
	    {
		len += hdr_len + files[idx].getName().length() + filename.length;
		len += fname.length() + file.length();

		String ct = CT.getContentType(file.getName());
		if (ct != null)
		    len += cont_type.length + ct.length();
	    }
	}

	if (len == 0) {
	    content_header = new NVPair("Content-Type", "application/octet-stream");
	    return len;
	} else {
	    content_header = new NVPair("Content-Type",
			       "multipart/form-data; boundary=" +
			       new String(boundary, 4, boundary.length-4, "8859_1"));
        }

	len -= 2;			// first CR LF is not written
	len += boundary.length + 2 + 2;	// \r\n -- bnd -- \r\n
        return len;
    }

    /**
     * Write the multipart/form-data encoded version of the form to the provided
     * output stream, using a default bufferSize of 4096 bytes for reading in the
     * files. Note that the output stream is closed after writing.
     * 
     * @param out the OutputStream to which the encoded form should be written
     * @exception IOException If any file operation fails.
     */
    public void writeEncodedMultipartForm(OutputStream out) throws IOException
    {
      writeEncodedMultipartForm(out, 4096);
    }

    /**
     * Write the multipart/form-data encoded version of the form to the provided
     * output stream, using the provided bufferSize for reading in the files. 
     * Note that the output stream is closed after writing.
     * 
     * @param out the OutputStream to which the encoded form should be written
     * @param bufferSize the size in bytes of the buffer used to read data files
     * @exception IOException If any file operation fails.
     */
    public void writeEncodedMultipartForm(OutputStream out, int bufferSize) 
                                      throws IOException
    {
        // Record the number of bytes written for error checking
	int pos = 0;

	NewBound: for (int new_c=0x30303030; new_c!=0x7A7A7A7A; new_c++)
	{
	    pos = 0;

	    // modify boundary in hopes that it will be unique
	    while (!BoundChar.get(new_c     & 0xff)) new_c += 0x00000001;
	    while (!BoundChar.get(new_c>>8  & 0xff)) new_c += 0x00000100;
	    while (!BoundChar.get(new_c>>16 & 0xff)) new_c += 0x00010000;
	    while (!BoundChar.get(new_c>>24 & 0xff)) new_c += 0x01000000;
/*
	    boundary[40] = (byte) (new_c     & 0xff);
	    boundary[42] = (byte) (new_c>>8  & 0xff);
	    boundary[44] = (byte) (new_c>>16 & 0xff);
	    boundary[46] = (byte) (new_c>>24 & 0xff);
*/
	    int off = 2;

	    for (int idx=0; idx<opts.length; idx++)
	    {
		if (opts[idx] == null)  continue;

                out.write(boundary, off, boundary.length-off);
		pos += boundary.length - off;
		off  = 0;
		int  start = pos;

                out.write(cont_disp, 0, cont_disp.length);
		pos += cont_disp.length;

		int nlen = opts[idx].getName().length();
                out.write(opts[idx].getName().getBytes("8859_1"), 0, nlen);
		pos += nlen;

                out.write((byte) '"');
                pos++;
                out.write((byte) '\r');
                pos++;
                out.write((byte) '\n');
                pos++;
                out.write((byte) '\r');
                pos++;
                out.write((byte) '\n');
                pos++;

                out.flush();

		int vlen = opts[idx].getValue().length();
                out.write(opts[idx].getValue().getBytes("8859_1"), 0, vlen);
                out.flush();
		pos += vlen;

/*
                // Not sure why this is here -- seems unnecessary to me -- mbj
		if ((pos-start) >= boundary.length  &&
		    Util.findStr(boundary, bnd_cmp, res, start, pos) != -1)
		    continue NewBound;
*/
	    }

	    for (int idx=0; idx<files.length; idx++)
	    {
		if (files[idx] == null)  continue;

		File file = new File(files[idx].getValue());
		String fname = file.getName();
		if (mangler != null)
		    fname = mangler.mangleFilename(fname, files[idx].getName());
		if (fname == null)  continue;

                out.write(boundary, off, boundary.length-off);
		pos += boundary.length - off;
		off  = 0;
		int start = pos;

                out.write(cont_disp, 0, cont_disp.length);
		pos += cont_disp.length;

		int nlen = files[idx].getName().length();
                out.write(files[idx].getName().getBytes("8859_1"), 0, nlen);
		pos += nlen;

                out.write(filename, 0, filename.length);
		pos += filename.length;

		nlen = fname.length();
                out.write(fname.getBytes("8859_1"), 0, nlen);
		pos += nlen;

                out.write((byte) '"');
                pos++;

		String ct = CT.getContentType(file.getName());
		if (ct != null)
		{
                    out.write(cont_type, 0, cont_type.length);
		    pos += cont_type.length;
                    out.write(ct.getBytes("8859_1"), 0, ct.length());
		    pos += ct.length();
		}

                out.write((byte) '\r');
                pos++;
                out.write((byte) '\n');
                pos++;
                out.write((byte) '\r');
                pos++;
                out.write((byte) '\n');
                pos++;

                out.flush();

                FileInputStream fs = new FileInputStream(file);
                byte[] buf = new byte[bufferSize];
                int cnt = 0;
                while (cnt!=-1) {
                    cnt = fs.read(buf);
                    if (cnt!=-1) {
                        out.write(buf, 0, cnt);
                        out.flush();
		                    pos += cnt;
                    }
                }
                fs.close();    // added by DFH after e-mail from MJones

/*
                // Not sure why this is here -- seems unnecessary to me -- mbj
		if ((pos-start) >= boundary.length  &&
		    Util.findStr(boundary, bnd_cmp, res, start, pos) != -1)
		    continue NewBound;
*/
	    }

	    break NewBound;
	}

        out.write(boundary, 0, boundary.length);
	pos += boundary.length;
        out.write((byte) '-');
        pos++;
        out.write((byte) '-');
        pos++;
        out.write((byte) '\r');
        pos++;
        out.write((byte) '\n');
        pos++;

        out.flush();
        out.close();

	if (pos != getLength())
	    throw new Error("Calculated "+getLength()+
                            " bytes but wrote "+ pos+" bytes!");

	/* the boundary parameter should be quoted (rfc-2046, section 5.1.1)
	 * but too many script authors are not capable of reading specs...
	 * So, I give up and don't quote it.
	 */
	content_header = new NVPair("Content-Type",
			       "multipart/form-data; boundary=" +
			       new String(boundary, 4, boundary.length-4, "8859_1"));
    }

    /**
     * Get the length in bytes of this form after encoding.  
     * This method can be used to set "Content-Length" headers and in 
     * other situations where the length must be known before the data is 
     * read from disk.
     *
     * @return the length of the encoded form in bytes
     */
    public long getLength()
    {
      return encodedLength;
    }

    /**
     * Get the content header after encoding.
     * This returns a String that contains the
     * value = "multipart/form-data; boundary=..." The exception to 
     * this is that if no opts or files are given the type is set to
     * "application/octet-stream" instead.
     *
     * @return the content header String
     */
    public String getContentType()
    {
      return content_header.getValue();
    }

    private static class CT extends URLConnection
    {
	protected static final String getContentType(String fname)
	{
	    return guessContentTypeFromName(fname);
	}

	private CT() { super(null); }
	public void connect() { }
    }
}
