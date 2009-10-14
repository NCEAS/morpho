/**
 *  '$RCSfile: CitationPage.java,v $'
 *    Purpose: A class that handles display of Citation Information
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-04-24 23:06:55 $'
 * '$Revision: 1.29 $'
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
package edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages;

import java.io.File;

import javax.swing.JTable;


/**
 * This represents a text data file which will be imported. Data will be stored in a JTable. 
 * Some medata will be stored too.
 * @author tao
 *
 */
public class ImportedTextFile 
{
	/**
	   * a global reference to the table used to display the data that is
	   * being referenced by this text import process
	   */
	  private JTable table;

	  /**
	   * 	a global reference to the table used to display the lines read from the file
	   *	this is the table that is displayed on the first screen of the TIW
	   */
	  private JTable linesTable = null;

	  /**
	   * actual number of lines in fdata file
	   */
	  private int nlines_actual;

	  /**
	   * number of parsed lines in file
	   */
	  private int nlines;

	  /**
	   * max number of lines to be parsed in file
	   */
	  private int nlines_max = 5000;

	  /**
	   * array of line strings
	   */
	  private String[] lines;

	  private File dataFile;

	  private String shortFilename;

	public JTable getTable() 
	{
		return table;
	}

	public void setTable(JTable table) 
	{
		this.table = table;
	}

	public JTable getLinesTable() {
		return linesTable;
	}

	public void setLinesTable(JTable linesTable) 
	{
		this.linesTable = linesTable;
	}

	public int getNlines_actual() 
	{
		return nlines_actual;
	}

	public void setNlines_actual(int nlines_actual) 
	{
		this.nlines_actual = nlines_actual;
	}

	public int getNlines() 
	{
		return nlines;
	}

	public void setNlines(int nlines) 
	{
		this.nlines = nlines;
	}

	public String[] getLines() 
	{
		return lines;
	}

	public void setLines(String[] lines) 
	{
		this.lines = lines;
	}

	public File getDataFile() {
		return dataFile;
	}

	public void setDataFile(File dataFile) 
	{
		this.dataFile = dataFile;
	}

	public String getShortFilename() 
	{
		return shortFilename;
	}

	public void setShortFilename(String shortFilename) 
	{
		this.shortFilename = shortFilename;
	}


}
