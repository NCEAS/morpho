/**
 *  '$RCSfile: PrinterInterface.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: sambasiv $'
 *     '$Date: 2004-04-05 21:58:20 $'
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

package edu.ucsb.nceas.morpho.plugins;

/**
 *  This Interface enables calling classes to display and be able to print a html
 *  or plain text string. Implementing classes need to display the provided string
 *  in an appropriate display panel and provide an option of printing that.
 */

public interface PrinterInterface
{
	
		/** 
		constant to describe the default content type of the string to be displayed 
		*/
		
		public static final String DEFAULT_CONTENT_TYPE = "text/html";
		
		
    /**
     *  method to display a user-provided String and provide an option to print it.
     *  The display string could be a html string or a plain text. The content type 
		 *  of the display string is also provided by the caller.
     *
     *  @param displayString  the string to be displayed (html or plain text string) 
     *
		 *  @param contentType  the contentType of the display string 
		 *												("text/html" or "text/plain") 
     *
     */
		 
    public void display (String displayString, String contentType);
		
		/**
     *  method to just print a user-provided String. The print string could be a html 
		 *	string or a plain text. The content type of the print string is also provided 
		 *	by the caller.
     *
     *  @param displayString  the string to be printed (html or plain text string) 
     *
		 *  @param contentType  the contentType of the print string 
		 *												("text/html" or "text/plain") 
     *
     */
		 
    public void print (String displayString, String contentType);
		
		
}

