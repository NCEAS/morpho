/**
 *  '$RCSfile: IOUtil.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-08-21 03:21:59 $'
 * '$Revision: 1.1 $'
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
 */

package edu.ucsb.nceas.morpho.util;

import java.io.Reader;
import java.io.IOException;

import edu.ucsb.nceas.morpho.util.Log;

/**
 *  General static utilities for IO operations
 */
public class IOUtil                                         
{
    /**
     *  constructor
     */
    private IOUtil() {}

    /**
     *  reads character data from the <code>Reader</code> provided, using a 
     *  buffered read. Returns data as a <code>StringBufer</code>
     *
     *  @param  reader          <code>Reader</code> object to be read
     *
     *  @return                 <code>StringBuffer</code> containing characters 
     *                          read from the <code>Reader</code>
     *
     *  @throws DocumentNotFoundException if id does not point to a document, or
     *          if requested document exists but cannot be accessed.
     */
    public static StringBuffer getAsStringBuffer(Reader reader) 
                                                            throws IOException
    {
        if (reader==null) return null;
        
        StringBuffer sb = new StringBuffer();
        try {
            char[] buff = new char[4096];
            int numCharsRead;

            while ((numCharsRead = reader.read( buff, 0, buff.length ))!=-1) {
                sb.append(buff, 0, numCharsRead);
            }
        } catch (IOException ioe) {
            Log.debug(12, "IOUtil.getAsStringBuffer(): Error reading Reader: "
                                                            +ioe.getMessage());
            throw ioe;
        } finally {
            try { 
                if (reader!=null) reader.close();
            } catch (IOException ce) {  
                Log.debug(12, "IOUtil.getAsStringBuffer(): closing Reader: "
                                                             +ce.getMessage());
            }
        }
        return sb;
    }
    
    
}


