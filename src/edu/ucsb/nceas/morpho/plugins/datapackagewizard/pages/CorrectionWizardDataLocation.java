/**
 *  '$RCSfile: CorrectionWizardDataLocation.java,v $'
 *    Purpose: A widget that displays data of multiple columns from multiple tables
 *						 in a columnar fashion and allows the user to select multiple columns
 * 						 using checkboxes
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Release: @release@
 *
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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JRadioButton;

import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.DataLocation.ThirdChoiceWidget;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.Util;
import edu.ucsb.nceas.utilities.OrderedMap;


/**
 * This class represents a page to modify data location xpath.
 * It will modify the both physical/objectName and physical/distribution/online or physical/distribution/offline.
 * This page only use the part of DataLocation GUI, e.g. the third panel when user choose describe/manually.
 * It has three events:
 * DESCRIBE_MAN_NODATA  ("data not available" - description only)
 * has entities, *no* distribution elements. So this page only has objectName field.
 * class parameter "distribution" will be WizardSettings.OFFLINE 
 *
 * DESCRIBE_MAN_ONLINE
 * has entities, distribution - online-url. Page has both ojbectName and distribution url.
 * class parameter "distribution" will be WizardSettings.ONLINE
 *
 * DESCRIBE_MAN_OFFLINE
 * has entities, distribution - offline. Page has both ojbectName and distribution/offline
 * class parameter "distribution" will be WizardSettings.OFFLINE
 * @author tao
 *
 */
public class CorrectionWizardDataLocation extends DataLocation
{
	private final String pageID       = DataPackageWizardInterface.CORRECTION_DATA_LOCATION;
	private static final String OBJECTNAMEPATH = "/objectName";
	private static final String ONLINEPATH = "/url";
	private static final String OFFLINEMDEIDUMNAMEPATH = "/mediumName";
	private static final String OFFLINEPATH = "/offline";
	private static final String FULLONLINEPATH = "/eml:eml/dataset/dataTable/physical/distribution/online/url";
	private static final String FULLOFFLINEPATH = "/eml:eml/dataset/dataTable/physical/distribution/offline/mediumName";
	private static final String FULLOFFLINEPATH2 = "/eml:eml/dataset/dataTable/physical/distribution/offline";
	private OrderedMap storedMap = new OrderedMap();
	private String rootPath = null;
	
    public CorrectionWizardDataLocation()
    {
    	setLastEvent(DESCRIBE_MAN_NODATA);
    	//setNextPageID(DataPackageWizardInterface.DATA_FORMAT);
    	init();
    }
    
    /*
     * init the panel for correction wizard. 
     * This panel only has the thrid question panel. The other is are blank
     */
    private void init()
    {
  	  this.setLayout(new BorderLayout());

  	    ActionListener q2Listener_import = null;
  	    ActionListener q2Listener_describe = null;

  	    Box topBox = Box.createVerticalBox();

  	    JLabel desc = WidgetFactory.makeHTMLLabel(
  	       "<p><b>Describe the data location and/or file name of  "
  	      +"your data package.</b><br></br>Note that you cannot import a new data table here. <p> ", 7);

  	    topBox.add(desc);

  	    onlinePanel  = getOnlinePanel();
  	    offlinePanel = getOfflinePanel();
  	    nodataPanel = getNoDataPanel();
  	    q3Widget = new ThirdChoiceWidget();
  	    mainRadioPanel = q3Widget;

  	    topBox.add(mainRadioPanel);

  	    topBox.add(WidgetFactory.makeDefaultSpacer());


  	    this.add(topBox, BorderLayout.NORTH);    
  	  
  	    blankPanel  = WidgetFactory.makeVerticalPanel(7);
  	    //currentSecondChoicePanel = blankPanel;
    }
    
    
    /**
     *  The action to be executed when the page is displayed. May be empty
     */
    public void onLoadAction() 
    {

      initializeGUI(storedMap, rootPath);
      
    }
    
    /**
     *  The action to be executed when the "Next" button (pages 1 to last-but-one)
     *  or "Finish" button(last page) is pressed. May be empty, but if so, must
     *  return true
     *
     *  @return boolean true if wizard should advance, false if not
     *          (e.g. if a required field hasn't been filled in)
     */
    public boolean onAdvanceAction() {

        return onAdvance(false);
    }
    
    /**
     *  gets the unique ID for this wizard page
     *
     *  @return   the unique ID String for this wizard page
     */
    public String getPageID() 
    { 
    	return pageID; 
    }
    
    /**
     *  gets the OrderedMap object that contains all the key/value paired
     *  settings for this particular wizard page
     *
     *  @return   data the OrderedMap object that contains all the
     *            key/value paired settings for this particular wizard page   
     */           
    public OrderedMap getPageData()
    {
    	OrderedMap map = getPageData("/eml:eml/dataset/dataTable/physical");
    	
        //Log.debug(5, "the map from CorrectionWizardDataLocation is "+map);
    	return map;
    }
    
    /**
     * gets the Map object that contains all the key/value paired settings for
     * this particular wizard page
     *
     * @param rootXPath the root xpath to prepend to all the xpaths returned by
     *   this method
     * @return data the Map object that contains all the key/value paired
     *   settings for this particular wizard page
     */
    public OrderedMap getPageData(String rootXPath) {

        returnMap.clear();
        switch (distribution) {

          case WizardSettings.ONLINE:
        	  // get object name from filenamefield.
        	  if(!Util.isBlank(fileNameFieldOnline.getText().trim()))
        	  {
        		  returnMap.put(rootXPath+OBJECTNAMEPATH, fileNameFieldOnline.getText().trim());
        	  }
              else
              {
            	  returnMap.put(rootXPath+OBJECTNAMEPATH, WizardSettings.UNAVAILABLE);
              }
	          if(!Util.isBlank(urlFieldOnline.getText().trim())) 
	          {
	            	//  it's an online URL
	            	returnMap.put(rootXPath+ONLINEPATH, urlFieldOnline.getText().trim());
	          } 
	          else 
	          {
	              
	              returnMap.put(rootXPath+ONLINEPATH, WizardSettings.UNAVAILABLE);
	          }
              break;
            //////

          case WizardSettings.OFFLINE:
        	if(getLastEvent() == DESCRIBE_MAN_OFFLINE)
        	{
        		if(!Util.isBlank(objNameField.getText().trim()))
            	{
                    returnMap.put(rootXPath+OBJECTNAMEPATH, objNameField.getText().trim());
            	}
            	else
            	{
            		returnMap.put(rootXPath+OBJECTNAMEPATH, WizardSettings.UNAVAILABLE);
            	}
            	
        		  String offlinePath = rootXPath;
        		  if(this.containsXpathWithEmptyValue(FULLOFFLINEPATH2))
        		  {
        		    offlinePath = offlinePath+OFFLINEPATH+OFFLINEMDEIDUMNAMEPATH;
        		  }
        		  else
        		  {
        		    offlinePath = offlinePath+OFFLINEMDEIDUMNAMEPATH;
        		  }
            	if(!Util.isBlank(medNameField.getText().trim()))
            	{
                    returnMap.put(offlinePath, medNameField.getText().trim());
            	}
            	else
            	{
            		returnMap.put(offlinePath, WizardSettings.UNAVAILABLE);
            	}
        	}    	
            break;
            //////

          case WizardSettings.NODATA:
        	  //if no data, then miss out the distribution elements altogether. But we need
        	  // object name  	  
        	  if (!Util.isBlank( fileNameFieldNoData.getText().trim()))
              {
                 returnMap.put(rootXPath+OBJECTNAMEPATH,  fileNameFieldNoData.getText().trim());
              }
              else
              {
            	  returnMap.put(rootXPath+OBJECTNAMEPATH, WizardSettings.UNAVAILABLE);
              }
        	
        	 
          
        }
        Log.debug(5, "map is "+returnMap.toString());
        return returnMap;
      }

      
    /**
     * Set ordered map to the page
     */
    public boolean setPageData(OrderedMap map, String xPathRoot) 
    { 
    	 Log.debug(45,
    		        "CorrectionWizardDataLocation.setPageData() called with xPathRoot = " + xPathRoot
    		        + "\n Map = \n" + map);
    	rootPath = xPathRoot;
    	if(map != null)
    	{
    	  storedMap.clear();
    	  Iterator iterator = map.keySet().iterator();
    	  while(iterator.hasNext())
    	  {
    	    String key = (String)iterator.next();
    	    String value = (String)map.get(key);
    	    storedMap.put(key, value);
    	  }
    	}
    	map.remove(xPathRoot+OBJECTNAMEPATH);
    	map.remove(xPathRoot+ONLINEPATH);
    	map.remove(xPathRoot+OFFLINEMDEIDUMNAMEPATH); 
    	map.remove(xPathRoot+OFFLINEPATH);
    	boolean canHandleAllData = map.isEmpty();
    	if (!canHandleAllData) 
    	{  
		      Log.debug(20, "CorrectionWizardDataLocation.setPageData returning FALSE! Map still contains:"+ map);
    	}
    	return canHandleAllData;
    }
    
 
    
    /*
     * Find out the distribution or OFFLINE even type base on the data in the 
     * from the xpathWithEmptyValueSet
     */
    private short findDistributionType() 
    {

        //// Online type
        if(this.containsXpathWithEmptyValue(FULLONLINEPATH)) 
        {
          setLastEvent(DESCRIBE_MAN_ONLINE);
          q3Widget.click(1);
          return WizardSettings.ONLINE;
        }
        else
        {
          if(this.containsXpathWithEmptyValue(FULLOFFLINEPATH) || this.containsXpathWithEmptyValue(FULLOFFLINEPATH2)) 
          {
             //// Offline type
            setLastEvent(DESCRIBE_MAN_OFFLINE);
            q3Widget.click(2);
            return WizardSettings.OFFLINE;
          }
          else
          {
            
            //have no distribution element.
            //Actually, this is not right, it can have be inline data. 
            //But we assume this case is no distribution. This will be sorted out in setPageData to check the if the map eventually is empty.
            setLastEvent(DESCRIBE_MAN_NODATA);
            q3Widget.click(0);
            return WizardSettings.NODATA;
          }
        }
        
      }
    
    /*
     * Set ordered map to the page
     */
    private boolean initializeGUI(OrderedMap map, String xPathRoot) 
    { 
       Log.debug(45,
                "CorrectionWizardDataLocation.setPageData() called with xPathRoot = " + xPathRoot
                + "\n Map = \n" + map);
      short type = findDistributionType();
      String value = null;
      switch(type)
      {
          //// Online data case
          case WizardSettings.ONLINE:
                  value = (String)map.get(xPathRoot+OBJECTNAMEPATH);
                  //Log.debug(45, "value for online ojbect name "+value);
                  if (value != null) 
                  {
                     fileNameFieldOnline.setText(value);
                     map.remove(xPathRoot+ OBJECTNAMEPATH);
                  }
                  value = (String)map.get(xPathRoot+ONLINEPATH);
                  //Log.debug(45, "value for online url is "+value);
                  if(value != null)
                  {
                    //Log.debug(45, "value for online url is (after if stamente "+value);
                    urlFieldOnline.setText(value);
                    map.remove(xPathRoot+ONLINEPATH);
                    //Log.debug(45, "value for online url is (after reming from map) "+value);
                  }
                  distribution= WizardSettings.ONLINE;              
                  break;
          //OFFline, but has distribution/offline path
          case WizardSettings.OFFLINE:
            if(getLastEvent() == DESCRIBE_MAN_OFFLINE)
              {
              value = (String)map.get(xPathRoot+OBJECTNAMEPATH);
                if(value != null)
                  {
                        objNameField.setText(value);
                        map.remove(xPathRoot+OBJECTNAMEPATH);
                  }
                
                  value = (String)map.get(xPathRoot+OFFLINEMDEIDUMNAMEPATH);
                  if(value != null)
                  {
                        medNameField.setText(value);
                        map.remove(xPathRoot+OFFLINEMDEIDUMNAMEPATH);
                  }            
              }     
            break;
          //No distribution path, only has objectName path.
          case WizardSettings.NODATA:
              value = (String)map.get(xPathRoot+OBJECTNAMEPATH);
              if (value != null)
                  {
                     fileNameFieldNoData.setText(value);
                     map.remove(xPathRoot+OBJECTNAMEPATH);
                  }
              break;
                 
      }
      q3Widget.disableAllRadioButtons(); //doesn't allow user to modify the distribution type
       boolean canHandleAllData = map.isEmpty();
     if (!canHandleAllData) 
     {
          Log.debug(20, "CorrectionWizardDataLocation.setPageData returning FALSE! Map still contains:"+ map);
     }
     return canHandleAllData;
    }
}
