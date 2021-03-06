/**
 *  '$RCSfile: AbstractUIPage.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-04-19 23:07:49 $'
 * '$Revision: 1.12 $'
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

import java.util.HashSet;
import java.util.Vector;

import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.XPathUIPageMapping;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;

import javax.swing.JPanel;

import org.w3c.dom.Node;




/**
 *  Class       AbstractUIPage
 *
 */
public abstract class AbstractUIPage extends JPanel {
	
	protected String nextPageID = null;
	
	
	private XPathUIPageMapping xpathUIPageMapping= null;
	
	private String temporaryPageNumber = "-1";
	
	//In eml 201, it can have some empty string as element value.
	//However, eml210 doesn't allow that. We need a correction wizard to allow user
	//to replace those empty strings.
	//It will store the xpath which contains empty string value for
	//correction wizard during transform eml 201 to 210.
	private HashSet<String> emptyValuePathSet = new HashSet<String>();

  /**
   *  gets the unique ID for this UI page
   *
   *  @return   the unique ID String for this UI page
   */
  public abstract String getPageID();


  /**
   *  gets the title for this UI page
   *
   *  @return   the String title for this UI page
   */
  public abstract String getTitle();


  /**
   *  gets the subtitle for this UI page
   *
   *  @return   the String subtitle for this UI page
   */
  public abstract String getSubtitle();


  /**
   *  Returns the ID of the page that the user will see next, after the "Next"
   *  button is pressed. If this is the last page, return value must be null
   *
   *  @return the String ID of the page that the user will see next, or null if
   *  this is te last page
   */
  public abstract String getNextPageID();


  /**
   *  Returns the serial number of the page
   *
   *  @return the serial number of the page
   */
  public abstract String getPageNumber();


  /**
   *  The action to be executed when the page is displayed. May be empty
   */
  public abstract void onLoadAction();


  /**
   *  The action to be executed when the "Prev" button is pressed. May be empty
   *
   */
  public abstract void onRewindAction();


  /**
   *  The action to be executed when the "Next" button (pages 1 to last-but-one)
   *  or "Finish" button(last page) is pressed. May be empty
   *
   *  @return boolean true if wizard should advance, false if not
   *          (e.g. if a required field hasn't been filled in)
   */
  public abstract boolean onAdvanceAction();


  /**
   * Pages should override this method when action should be taken
   * when a modal dialog is canceled
   */
  public void cancelAction() {
	  
  }
  
  /**
   *  gets the Map object that contains all the key/value paired
   *  settings for this particular UI page
   *
   *  @return   data the Map object that contains all the
   *            key/value paired settings for this particular UI page
   */
  public abstract OrderedMap getPageData();


  /**
   * gets the Map object that contains all the key/value paired settings for
   * this particular UI page
   *
   * @param rootXPath the root xpath to prepend to all the xpaths returned by
   *   this method
   * @return data the Map object that contains all the key/value paired
   *   settings for this particular UI page
   */
  public abstract OrderedMap getPageData(String rootXPath);


  /**
   * sets the fields in the UI page using the Map object that contains all
   * the key/value paired
   *
   * @param data the Map object that contains all the key/value paired settings
   *   for this particular UI page
   * @param rootXPath the String that represents the "root" of the XPath to the
   *   content of this widget, INCLUDING PREDICATES. example - if this is a
   *   "Party" widget, being used for the second "Creator" entry in a list,
   *   then xPathRoot = "/eml:eml/dataset[1]/creator[2]
   * @return boolean true if this page can handle all the data passed in the
   * OrderedMap, false if not. <em>NOTE that the setPageData() method should
   * still complete its work and fill out all the UI values, even if it is
   * returning false</em>
   */
  public abstract boolean setPageData(OrderedMap data, String rootXPath);
  
  /**
   * The default implementation returns null so that subclasses are 
   * not required to provide their own implementation.
   * @return the ModalDialog that contains this page
   */
  public ModalDialog getModalDialog() {
	  return null;
  }
  
  /**
   * Can be used to allow the page to control the dialog
   * Default implementation does nothing
   * @param md the ModalDialog (if any) that contains this page
   */
  public void setModalDialog(ModalDialog md) {}
  
  /**
   *  Set the ID of the page that the user will see next, after the "Next"
   *  button is pressed. It can overwrite the default one
   *
   *  @para nexPageID the String ID of the page that the user will see next
   */
  public void setNextPageID(String nextPageID)
  {
	  this.nextPageID = nextPageID;
  }
  
  
  /**
   * Sets a XPathUIPageMapping to this page
   * @param mapping
   */
  public void setXPathUIPageMapping(XPathUIPageMapping mapping)
  {
	  this.xpathUIPageMapping= mapping;
  }
  
  /**
   * Gets the XPathUIPageMapping of this page
   * @return
   */
  public XPathUIPageMapping getXPathUIPageMapping()
  {
	 return this.xpathUIPageMapping;  
  }
  
  /**
   * Action will be executed when "Save for Later" button is clicked
   * @return true if the action can continue
   */
  public boolean onSaveForLaterAction()
  {
    boolean success = true;
    return success;
  }
  
  /**
   * Gets a temporary page number assigned to a page.
   * @return
   */
  public String getTemporaryPageNumber()
  {
    return this.temporaryPageNumber;
  }
  
  /**
   * Sets a temporary page number to a page
   * @param temporaryPageNumber
   */
  public void setTemporaryPageNumber(String temporaryPageNumber)
  {
    this.temporaryPageNumber = temporaryPageNumber;
  }
  
  
  /**
   * Adds the specified path to the set containing xpath with empty value.
   * @param path the xpath to be added. This method will remove the predicates of path.
   */
  public void addXPathWithEmptyValue(String path)
  {
    if(path != null || !path.trim().equals(""))
    {
      path =XMLUtilities.removeAllPredicates(path);
      //Log.debug(5, "adding path "+path);
      emptyValuePathSet.add(path);
    }   
  }
  
  /**
   * Returns true if this set contains the specified path.
   * @param path the path whose presence is to be tested
   * @return true if the set contains it.
   */
  public boolean containsXpathWithEmptyValue(String path)
  {
    //Log.debug(5, "the size of set is "+emptyValuePathSet.size());
    return emptyValuePathSet.contains(path);
  }
}
