package edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;

import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.HyperlinkButton;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.OrderedMap;

public class CorrectionSummary extends AbstractUIPage {

	  public final String pageID     = DataPackageWizardInterface.CORRECTION_SUMMARY;
	  
	  public final String pageNumber = "15";
	  public final String CORRECTION_WIZ_SUMMARY_TITLE = "Correction Wizard";
	  //public final String ENTITY_WIZ_SUMMARY_TITLE  = "New Data Table Wizard";
	  public final String SUBTITLE                  = "Summary";

	  private JLabel desc1;
	  private JLabel desc2;
	  private JLabel desc4;
	  private WizardContainerFrame mainWizFrame;
	  private JComponent showMeButton;
	  private String message = "";
	  private static final String FINISH = "This wizard has corrected all invalid fields.";
	  private static final String NEEDTREEEDITOR = "This wizard has corrected some invalid fields. But some fileds still need tree editors to fix.<<br></br>"
		                                                                +"<b>Note:<br></br>One tree editor page will show one field with white space value and you should fill some value</b>";
	                                                                     

	  public CorrectionSummary(WizardContainerFrame mainWizFrame, boolean needTreeEditor) {
		 nextPageID = null;
	    this.mainWizFrame = mainWizFrame;
	    if(needTreeEditor)
	    {
	    	message = NEEDTREEEDITOR;
	    }
	    else
	    {
	    	message = FINISH;
	    }
	    init();
	  }


	  /**
	   * initialize method does frame-specific design - i.e. adding the widgets that
	   * are displayed only in this frame (doesn't include prev/next buttons etc)
	   */
	  private void init() {

	    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

	    this.add(WidgetFactory.makeDefaultSpacer());
	    this.add(WidgetFactory.makeDefaultSpacer());
	    this.add(WidgetFactory.makeDefaultSpacer());

	    desc1 = WidgetFactory.makeHTMLLabel("", 2);
	    this.add(desc1);

	    desc2 = WidgetFactory.makeHTMLLabel(
	        "<br></br><p><b>"+WizardSettings.getSummaryText()+"</b></p><br></br>", 3);

	    if(desc2!=null && WizardSettings.getSummaryText()!=null)
	      this.add(desc2);

	    JLabel desc3 = WidgetFactory.makeHTMLLabel(getSecondParagraph(), 2);
	    this.add(desc3);

	    desc4 = WidgetFactory.makeHTMLLabel("", 2);

	    this.add(desc4);
	    this.add(getShowMeButton());

	    desc1.setAlignmentX(-1f);
	    desc2.setAlignmentX(-1f);
	    desc3.setAlignmentX(-1f);
	    desc4.setAlignmentX(-1f);

	    this.add(Box.createVerticalGlue());
	  }

	  private String getSecondParagraph(){
	 
	    return "";
	  }


	  private String getLastParagraph() {

	 
	    return "";
	  }

	  private String getProductName() {

	    return "";
	  }

	  /**
	   *  The action to be executed when the page is displayed. May be empty
	   */
	  public void onLoadAction() {

	    desc1.setText(
	      WizardSettings.HTML_TABLE_LABEL_OPENING
	      +"<p>"+message+"</p>"
	       +WizardSettings.HTML_TABLE_LABEL_CLOSING);

	    /*desc2.setText( WizardSettings.HTML_TABLE_LABEL_OPENING
	                  +"<p><b>"+WizardSettings.getSummaryText()
	                  +this.getDataLocation()
	                  +"</b></p><br></br>"
	                  +WizardSettings.HTML_TABLE_LABEL_CLOSING);

	    desc4.setText( WizardSettings.HTML_TABLE_LABEL_OPENING
	                  +getLastParagraph()+WizardSettings.HTML_TABLE_LABEL_CLOSING);*/

	    updateShowMeButton();

	    String ID = mainWizFrame.getFirstPageID();

	    if (ID==null) return;
	    if (ID.equals(DataPackageWizardInterface.DATA_LOCATION)) {
	      mainWizFrame.setButtonsStatus(false, false, true);
	    }

	  }

	  private JComponent getShowMeButton() {

	    if (showMeButton==null) {

	      ServiceProvider provider = null;
	      ServiceController services = ServiceController.getInstance();

	      DataPackageInterface dataPackagePlugin = null;
	      try {
	        provider = services.getServiceProvider(DataPackageInterface.class);
	        dataPackagePlugin = (DataPackageInterface)provider;
	      } catch (ServiceNotHandledException snhe) {
	        Log.debug(6, snhe.getMessage());
	      }

	      final DataPackageInterface finalDataPackagePlugin = dataPackagePlugin;

	      GUIAction newDataTableAction
	        = new GUIAction("or click here to finish this wizard and add a new " +
	                        "data table now.",
	                        null,
	                        new Command() {

	        public void execute(ActionEvent ae) {

	          mainWizFrame.finishAction();

	          try {
	            finalDataPackagePlugin.getCommandObject(
	                DataPackageInterface.NEW_DATA_TABLE_COMMAND).execute(ae);

	          } catch (ClassNotFoundException cnfe) {

	            Log.debug(5, "Sorry - unable to start the table creator "
	                      + "automatically - please select the "
	                      + "\"Create/Import New Data Table...\" option from the \"Data\" menu "
	                      + "by hand ");
	            cnfe.printStackTrace();
	          }
	        }
	      });

	      showMeButton = new HyperlinkButton(newDataTableAction);
	      final Dimension DIM = new Dimension(400,20);
	      showMeButton.setMinimumSize(DIM);
	      showMeButton.setPreferredSize(DIM);
	      showMeButton.setMaximumSize(DIM);
	    }
	    updateShowMeButton();
	    return showMeButton;
	  }


	  private void updateShowMeButton() {

	    String ID = mainWizFrame.getFirstPageID();

	    if (ID == null) {

	      showMeButton.setEnabled(false);
	      showMeButton.setVisible(false);

	    } else if (ID.equals(DataPackageWizardInterface.INTRODUCTION)
	                || ID.equals(DataPackageWizardInterface.DATA_LOCATION)) {

	      showMeButton.setEnabled(true);
	      showMeButton.setVisible(true);

	    } else {

	      showMeButton.setEnabled(false);
	      showMeButton.setVisible(false);
	    }
	  }


	  private String getDataLocation() {

	    String summaryText = WizardSettings.getSummaryText();
	    if (summaryText!=null
	            && (   summaryText.equals(WizardSettings.SUMMARY_TEXT_ONLINE)
	                || summaryText.equals(WizardSettings.SUMMARY_TEXT_INLINE)) ) {

	      String loc = WizardSettings.getDataLocation();
	      if (loc!=null) return "<span style=\"text-decoration: underline;\">"+loc
	                                                                    +"</span>";
	    }
	    return "";
	  }


	  /**
	   *  The action to be executed when the "Prev" button is pressed. May be empty
	   *
	   */
	  public void onRewindAction() {

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
	    return true;
	  }


	  /**
	   *  gets the Map object that contains all the key/value paired
	   *  settings for this particular wizard page
	   *
	   *  @return   data the Map object that contains all the
	   *            key/value paired settings for this particular wizard page
	   */
	  public OrderedMap getPageData() {

	    return null;
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

	    return null;
	  }


	  /**
	   *  gets the unique ID for this wizard page
	   *
	   *  @return   the unique ID String for this wizard page
	   */
	  public String getPageID() { return pageID; }

	  /**
	   *  gets the title for this wizard page
	   *


	  /**
	   *  gets the title for this wizard page
	   *
	   *  @return   the String title for this wizard page
	   */
	  public String getTitle() {
	    return CORRECTION_WIZ_SUMMARY_TITLE;
	  }


	  /**
	   *  gets the subtitle for this wizard page
	   *
	   *  @return   the String subtitle for this wizard page
	   */
	  public String getSubtitle() {

	    return SUBTITLE;
	  }

	  /**
	   *  Returns the ID of the page that the user will see next, after the "Next"
	   *  button is pressed. If this is the last page, return value must be null
	   *
	   *  @return the String ID of the page that the user will see next, or null if
	   *  this is te last page
	   */
	  public String getNextPageID() { return nextPageID; }

	  /**
	     *  Returns the serial number of the page
	     *
	     *  @return the serial number of the page
	     */
	  public String getPageNumber() { return pageNumber; }

	    public boolean setPageData(OrderedMap data, String xPathRoot) { return false; }
	}

