/**
 *  '$RCSfile: FileUtils.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2001-06-11 23:32:26 $'
 * '$Revision: 1.2 $'
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;

/**
 * This class provides convenience methods for manipulating filesystem
 * objects, such as copying from one file to another.
 */
public class FileUtils
{

  public static final String DELIMITER = "@";

  /**
   * Copy one file to another, given Files as inputs
   * 
   * @param inputFile the source file for the copy
   * @param outputFile the destination file for the copy
   */
  public static void copy(File inputFile, File outputFile) throws IOException
  {
    FileReader in = new FileReader(inputFile);
    FileWriter out = new FileWriter(outputFile);
    int len;
    char[] buffer =  new char[512];
    while ((len = in.read(buffer)) != -1) {
      out.write(buffer, 0, len);
    }
    in.close();
    out.close();
  }

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
    copy(inputFile, outputFile);  
  }

  /**
   * Copy one file to another, given Files as inputs, and
   * substitute special tokens with their corresponding values
   * 
   * @param inputFile the source file for the copy
   * @param outputFile the destination file for the copy
   * @param tokens a hash of tokens to be substituted in the dest file
   */
  public static void copy(File inputFile, File outputFile, Hashtable tokens)
         throws IOException
  {
    BufferedReader in = new BufferedReader(new FileReader(inputFile));
    BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));

    int length;
    String newline = null;
    String line = in.readLine();
    while (line != null) {
      if (line.length() != 0) {
        newline = replace(line, tokens);
        out.write(newline);
      } 
      out.newLine();
      line = in.readLine();
    }

    out.close();
    in.close();
  }

  /**
   * Copy one file to another, given their file names as inputs, and
   * substitute special tokens with their corresponding values
   * 
   * @param src the source file for the copy
   * @param dest the destination file for the copy
   * @param tokens a hash of tokens to be substituted in the dest file
   */
  public static void copy(String src, String dest, Hashtable tokens) 
         throws IOException
  {
    File inputFile = new File(src);
    File outputFile = new File(dest);
    copy(inputFile, outputFile, tokens);
  }

  /**
   * Replace tokens with their values in a line of text
   *
   * @param line the string on which we perform replacement
   * @param tokenList a hash of name-value pairs to be replaced
   * @returns line with the tokens replaced with their values
   */
  private static String replace(String line, Hashtable tokenList) {
    int tokenBegPos = line.indexOf(DELIMITER);

    if (tokenBegPos > -1) {
      try {
        String token = null;
        String value = null;

        StringBuffer newLine = new StringBuffer();

        int pos = 0;
        while ((tokenBegPos = line.indexOf(DELIMITER, pos)) > -1) {
          int tokenEndPos = line.indexOf(DELIMITER,
                            tokenBegPos + DELIMITER.length() + 1);
          if (tokenEndPos == -1) {
            break;
          }
          token = line.substring(tokenBegPos + DELIMITER.length(), 
                                 tokenEndPos);
          newLine.append(line.substring(pos, tokenBegPos));
          if (tokenList.containsKey(token)) {
            value = (String)tokenList.get(token);
            newLine.append(value);
            pos = tokenBegPos + DELIMITER.length() + token.length() + 
                DELIMITER.length();
          } else {
            newLine.append(DELIMITER);
            pos = tokenBegPos + DELIMITER.length();
          }
        }

        newLine.append(line.substring(pos));
        return newLine.toString();
      } catch (StringIndexOutOfBoundsException sioobe) {
        return line;
      }
    } else {
      return line;
    }
  }
}
