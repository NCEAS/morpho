/**
 *  '$RCSfile: ColumnData.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2002-12-04 18:04:40 $'
 * '$Revision: 1.4 $'
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

import java.util.*;

/**
 *  class to store all the metadate about the data in a column of the table
 *  Previously, this was an inner private class defined as part of the 
 *  TextImportWizard. It was made a 'stand-alone' class for use in sharing
 * data between the TextImportWizard and the ColumnMetadataEditPanel class
 */
public class ColumnData
	{
	    public int colNumber;
	    public String colTitle = "";
	    public String colName = "";
	    public String colDefinition = "";
	    public String colType = "";
	    public String colUnits = "";
	    public int colNumUniqueItems;
      public String colMissingValue;
      public String colPrecision;
      public String colTextDefinition = "";
      public String colTextPattern = "";
      public String colTextSource = "";
	    public double colMin = 0.0;
	    public double colMax = 0.0;
	    public double colAverage = 0.0;
	    public Vector colUniqueItemsList;
	    public Vector colUniqueItemsDefs;
	    public boolean useEnumerationList = false;
      public boolean textChoice = true;
      public boolean numChoice = false;
      public boolean enumChoice = false;
      
      public Vector enumCodeVector = new Vector();
      public Vector enumDefinitionVector = new Vector();
      public Vector enumSourceVector = new Vector();
      
      
      
	    ColumnData(int colnum) {
	        this.colNumber = colnum;    
	    }
	    
	    boolean hasInfo() {
	        boolean res = true;
	        if (colTitle.length()==0) res=false;
	        if (colName.length()==0) res=false;
	        if (colDefinition.length()==0) res=false;
	        if (colType.length()==0) res=false;
	   //     if (colUnits.length()==0) res=false;
	        return res;
	    }
	}
