/**
 *  '$RCSfile: Util.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2008-11-11 23:51:05 $'
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
package edu.ucsb.nceas.morpho.util;

/**
 * This class presents utility methods which will be repeatly used in morpho code.
 * @author tao
 *
 */
public class Util 
{
		/**
		 * Determine if the specified string is blank or not. If string is null, it will be false.
		 * @param input the specified string
		 * @return true or false if the the string is blank.
		 */
		public static  boolean isBlank(String input)
		{
			boolean isBlank = true;
			if (input != null)
			{
				if (!input.trim().equals(""))
				{
					isBlank = false;
				}
			}
			return isBlank;
		}
		
		/**
		 * Determine if the specified string is number or not. If String is null, it will return false.
		 * @param input the specified string
		 * @return true if it is number. otherwise false.
		 */
		public static boolean isNumber (String input)
		{
			boolean isNumber = true;
			if (input != null)
			{
				try
				{
					Double number = new Double(input);
				}
				catch(NumberFormatException e)
				{
					isNumber = false;
				}
			}
			else
			{
				isNumber = false;
			}
			return isNumber;
		}
}
