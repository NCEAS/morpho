/**
 *  '$RCSfile: XMLUtil.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2008-07-08 00:42:46 $'
 * '$Revision: 1.10 $'
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

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLUtil
{
	
    /** Normalizes the given string. */
    public static String normalize(Object ss) {
        String s = "";
        s = (String)ss;
        StringBuffer str = new StringBuffer();

        int len = (s != null) ? s.length() : 0;
        for (int i = 0; i < len; i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '<': {
                    str.append("&lt;");
                    break;
                }
                case '>': {
                    str.append("&gt;");
                    break;
                }
                case '&': {
                    str.append("&amp;");
                    break;
                }
                case '"': {
                    str.append("&quot;");
                    break;
                }
/*  handled in default
                case '\r':
		            case '\t':
                case '\n': {
                    if (true) {
                        str.append("&#");
                        str.append(Integer.toString(ch));
                        str.append(';');
                        break;
                    } else {
                    // else, default append char
                    str.append(" ");
			              break;
                    }
               }
*/                 default: {
                    if ((ch<128)&&(ch>31)) {
                      str.append(ch);
                    }
                    else if (ch<32) {
                      if (ch== 10) {
                        str.append(ch);
                      }
                      if (ch==13) {
                        str.append(ch);
                      }
                      if (ch==9) {
                        str.append(ch);
                      }
                      // otherwise skip
                    }
                    else {
                    	// do not do escape these now that we are using UTF-8
                    	str.append(ch);
                    	// commenting this out in favor of UTF-8 encoding, BRL 11/12/2010
                    	// see http://bugzilla.ecoinformatics.org/show_bug.cgi?id=5238
                        //str.append("&#");
                        //str.append(Integer.toString(ch));
                        //str.append(';');
                    }
                }
            }
        }
        String temp = str.toString();
        temp = temp.trim();
        if (temp.length()<1) temp = " ";
        return temp;

    } // normalize(String):String



    public static String getDOMTreeAsString(Node node) {
      if (node==null) return null;
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      Writer writer = new BufferedWriter(new OutputStreamWriter(baos, Charset.forName("UTF-8")));

      try {
        print(node, writer);
      } catch (Exception e) {
        String msg = "getDOMTreeAsString() - unexpected Exception: "+e+"\n";
        //printWriter.write(msg);
        e.printStackTrace();
      } finally {
        try {
          writer.flush();
          baos.flush();
          baos.close();
          writer.close();
        } catch (IOException ioe) {}
      }
      String retString = null;
      try {
		retString = baos.toString(Charset.forName("UTF-8").name());
	} catch (UnsupportedEncodingException e) {
		e.printStackTrace();
	}
      return retString;
  }


  /**
   * This method can 'print' any DOM subtree. Specifically it is
   * set (by means of 'out') to write the in-memory DOM to the
   * same XML file that was originally read. Action thus saves
   * a new version of the XML doc
   *
   * @param node node usually set to the 'doc' node for complete XML file
   * re-write
   */
  public static void print(Node node, Writer out) throws IOException
  {

    // is there anything to do?
    if (node == null)
    {
      return;
    }

    int type = node.getNodeType();
    switch (type)
    {
      // print document
    case Node.DOCUMENT_NODE:
    {

      out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      print(((Document) node).getDocumentElement(), out);
      out.flush();
      break;
    }

      // print element with attributes
    case Node.ELEMENT_NODE:
    {
      out.write('<');
      out.write(node.getNodeName());
      Attr attrs[] = sortAttributes(node.getAttributes());
      for (int i = 0; i < attrs.length; i++)
      {
        Attr attr = attrs[i];
        out.write(' ');
        out.write(attr.getNodeName());
        out.write("=\"");
        out.write(XMLUtil.normalize(attr.getNodeValue()));
        out.write('"');
      }
      out.write('>');
      NodeList children = node.getChildNodes();
      if (children != null)
      {
        int len = children.getLength();
        for (int i = 0; i < len; i++)
        {
          print(children.item(i), out);
        }
      }
      break;
    }

      // handle entity reference nodes
    case Node.ENTITY_REFERENCE_NODE:
    {
      out.write('&');
      out.write(node.getNodeName());
      out.write(';');

      break;
    }

      // print cdata sections
    case Node.CDATA_SECTION_NODE:
    {
      out.write("<![CDATA[");
      out.write(node.getNodeValue());
      out.write("]]>");

      break;
    }

      // print text
    case Node.TEXT_NODE:
    {
      out.write(XMLUtil.normalize(node.getNodeValue()));
      break;
    }

      // print processing instruction
    case Node.PROCESSING_INSTRUCTION_NODE:
    {
      out.write("<?");
      out.write(node.getNodeName());
      String data = node.getNodeValue();
      if (data != null && data.length() > 0)
      {
        out.write(' ');
        out.write(data);
      }
      out.write("?>");
      break;
    }
    }

    if (type == Node.ELEMENT_NODE)
    {
      out.write("</");
      out.write(node.getNodeName());
      out.write(">\n");
    }

    out.flush();

  } // print(Node)


  /** Returns a sorted list of attributes. */
  protected static Attr[] sortAttributes(NamedNodeMap attrs)
  {

    int len = (attrs != null) ? attrs.getLength() : 0;
    Attr array[] = new Attr[len];
    for (int i = 0; i < len; i++)
    {
      array[i] = (Attr) attrs.item(i);
    }
    for (int i = 0; i < len - 1; i++)
    {
      String name = array[i].getNodeName();
      int index = i;
      for (int j = i + 1; j < len; j++)
      {
        String curName = array[j].getNodeName();
        if (curName.compareTo(name) < 0)
        {
          name = curName;
          index = j;
        }
      }
      if (index != i)
      {
        Attr temp = array[i];
        array[i] = array[index];
        array[index] = temp;
      }
    }

    return (array);

  } // sortAttributes(NamedNodeMap):Attr[]


  /**
   * Returns attribute value given a node's attributes and attribute name.
   * Returns null if attribute is not found.
   * @param attributes NamedNodeMap
   * @param attributeName String
   * @return String
   */
  public static String getAttributeValue(NamedNodeMap attributes,
                                         String attributeName) {
      if (attributes == null)  return null;
      Node n = attributes.getNamedItem(attributeName);
      if (n == null)  return null;
      return n.getNodeValue();
  } // getAttributeValue(NamedNodeMap,String):String



}
