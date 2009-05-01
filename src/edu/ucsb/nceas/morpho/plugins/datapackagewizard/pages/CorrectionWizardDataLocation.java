package edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
	private static final String OBJECTNAMEPATH = "/objectName";
	private static final String ONLINEPATH = "/distribution/online/url";
	private static final String OFFLINEMDEIDUMNAMEPATH = "/distribution/offline/mediumName";
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
  	       "<p><b>Describe and optionally include a data "
  	      +"table in your data package.</b> You may create a table from "
  	      +"scratch and populate it using Morpho's spreadsheet-style data editor, "
  	      +"or you can import certain types of existing data files and use the "
  	      +"wizard to automatically extract much of the documentation from the data "
  	      +"file itself. If you "
  	      +"choose the second option, you will be prompted to review the "
  	      +"information that "
  	      +"is extracted and provide any required fields that can not be generated "
  	      +"automatically.<br></br></p>"
  	      +"<p>You can also choose to manually enter all of the required fields "
  	      +"(rather than using the metadata extractor), which is useful for "
  	      +"proprietary file types like Excel, or other "
  	      +"file types that are not yet supported.</p>", 7);
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
            	
            	if(!Util.isBlank(medNameField.getText().trim()))
            	{
                    returnMap.put(rootXPath+OFFLINEMDEIDUMNAMEPATH, medNameField.getText().trim());
            	}
            	else
            	{
            		returnMap.put(rootXPath+OFFLINEMDEIDUMNAMEPATH, WizardSettings.UNAVAILABLE);
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
        return returnMap;
      }

      
}
