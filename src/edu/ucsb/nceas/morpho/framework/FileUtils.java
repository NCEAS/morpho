/**
 *  '$RCSfile: FileUtils.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2001-06-11 02:13:38 $'
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

package edu.ucsb.nceas.morpho.framework;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This class provides convenience methods for manipulating filesystem
 * objects, such as copying from one file to another.
 */
public class FileUtils
{
  /**
   * Copy one file to another, given their file names as inputs
   * 
   * @param src the source file for the copy
   * @param dest the destination file for the copy
   */
  public static void copy(String src, String dest) throws IOException
  {
    File inputFile = new File(src);
    File outputFile = new File(dest);
    
    FileReader in = new FileReader(inputFile);
    FileWriter out = new FileWriter(outputFile);
    int c;
    
    while ((c = in.read()) != -1) {
      out.write(c);
    }
    in.close();
    out.close();
  }
}
