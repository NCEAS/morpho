/**
 *  '$RCSfile: EMLVersionTransformationException.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-04-23 23:59:28 $'
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
package edu.ucsb.nceas.morpho.datapackage;

/**
 * This class represents an Exception during the transformation from
 * old version eml to new verison eml
 * @author tao
 *
 */
public class EMLVersionTransformationException extends Exception
{
    private String newEMLOutput = null;
    
    /**
     * The exception has two parts. One is the error message, the other
     * is the output of new eml document. Note the output may be invalid.
     * @param errorMessage
     * @param newEMLOutput
     */
    public EMLVersionTransformationException(String errorMessage, String newEMLOutput)
    {
    	super(errorMessage);
    	this.newEMLOutput = newEMLOutput;
    }
    
    /**
     * Gets the output of new eml version document.
     * @return
     */
    public String getNewEMLOutput()
    {
    	return this.newEMLOutput;
    }
    
}
