/**
 *  '$RCSfile: KeywordsPage.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2008-11-26 04:31:56 $'
 * '$Revision: 1.15 $'
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


import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.framework.ModalDialog;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.CustomList;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.UISettings;
import edu.ucsb.nceas.morpho.util.Util;
import edu.ucsb.nceas.utilities.OrderedMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageLibrary;
import edu.ucsb.nceas.morpho.plugins.vocabulary.AbstractUIVocabularyPage;
import edu.ucsb.nceas.morpho.plugins.vocabulary.VocabularyPlugin;

public class KeywordsPage extends AbstractUIPage {

  private final String pageID     = DataPackageWizardInterface.KEYWORDS_PAGE;
  private final String nextPageID = "";
  private final String pageNumber = "";
  private final String title      = "Keywords Page";
  private final String subtitle   = "";

  private final String EMPTY_STRING = "";
  private JComboBox thesaurusField;
  private JLabel thesaurusLabel;
  private JLabel kwLabel;
  private CustomList kwList;
  private JPanel middlePanel;
  private JPanel radioPanel;
  
  private String[] thesaurii = new String[] {EMPTY_STRING};

  private final String[] buttonsText = new String[] {
    "These keywords are not chosen from a predefined list",
    "These keywords are chosen from a predefined list:"
  };

  private     String xPathRoot  = "/eml:eml/dataset/keywordSet[1]";
  private final String KEYWORD_REL_XPATH = "keyword[";
  private final String THESAURUS_REL_XPATH = "keywordThesaurus[1]";

  ////
  private ActionListener listener = new ActionListener() {

    public void actionPerformed(ActionEvent e) {
      Log.debug(45, "got radiobutton command: "+e.getActionCommand());

      onLoadAction();

      if (e.getActionCommand().equals(buttonsText[0])) {
        thesaurusField.setVisible(false);
        thesaurusLabel.setVisible(false);
        thesaurusField.setSelectedItem(EMPTY_STRING);
      } else if (e.getActionCommand().equals(buttonsText[1])) {
        thesaurusField.setVisible(true);
        thesaurusLabel.setVisible(true);
      }
    }
  };

  public KeywordsPage() {
	initThesaurii();
    init();
  }
  
  private void initThesaurii() {
	  try {
		Vector vocab = VocabularyPlugin.getInstance().getAvailableVocabularies();
		vocab.add(0, EMPTY_STRING);
		thesaurii = (String[]) vocab.toArray(new String[0]);
	  }
	  catch (Exception e) {
		// ignore this error, likely we have no vocabularies yet
		Log.debug(10, "Could not initialize the available vocabularies");  
	}
  }

  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {

    middlePanel = new JPanel();
    this.setLayout( new BorderLayout());

    middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));
    middlePanel.add(WidgetFactory.makeDefaultSpacer());

    JLabel desc = WidgetFactory.makeHTMLLabel(
                      "<font size=\"4\"><b>Define Keyword Set:</b></font>", 1);
    middlePanel.add(desc);

    ////
    radioPanel = WidgetFactory.makeRadioPanel(buttonsText, 0, listener);
    middlePanel.add(WidgetFactory.makeDefaultSpacer());
    middlePanel.add(radioPanel);

    JPanel thesaurusPanel = WidgetFactory.makePanel(1);
    thesaurusLabel = WidgetFactory.makeLabel("Thesaurus name:", false);
    thesaurusPanel.add(thesaurusLabel);
    thesaurusLabel.setVisible(false);
    thesaurusField = WidgetFactory.makePickList(thesaurii, true, 0, null);
    thesaurusField.setVisible(false);
    thesaurusPanel.add(thesaurusField);
    thesaurusPanel.setBorder(new javax.swing.border.EmptyBorder(0,0,0,
        WizardSettings.PADDING));
    middlePanel.add(WidgetFactory.makeHalfSpacer());
    middlePanel.add(thesaurusPanel);
    
    middlePanel.add(WidgetFactory.makeDefaultSpacer());

    ////
    JPanel kwPanel = WidgetFactory.makePanel(16);
    kwLabel = WidgetFactory.makeLabel("Keywords:", true);
    kwPanel.add(kwLabel);

    kwList = WidgetFactory.makeList(new String[]{ "Keyword" },
                                    new Object[]{ new JTextField()},
                                    8, true, false, false, true, true, true );
    
    Action thesaurusAction = 
    	new AbstractAction() {
	      public void actionPerformed(ActionEvent e) {
	        Log.debug(45, "\nKeywords: CustomEditAction called");
	        // show lookup for known thesaurii
	        if (
	        		((String) thesaurusField.getSelectedItem()).equals(EMPTY_STRING)
	        		||
	        		!VocabularyPlugin.getInstance().getAvailableVocabularies().contains(thesaurusField.getSelectedItem())
	        ) {	
	        	List row = new ArrayList();
	        	row.add("");
	        	kwList.addRow(row);
	        	kwList.setEditable(true);
	        	return;
	        }
	        // configured vocabularies as given in the Configuration
	        else {	
	        	showLookupDialog((String) thesaurusField.getSelectedItem());
	        	kwList.setEditable(false);
	        }
	        //lock the selection box
	        thesaurusField.setEnabled(false);
	      }
	};
	Action thesaurusDeleteAction = 
    	new AbstractAction() {
	      public void actionPerformed(ActionEvent e) {
	    	  int row = kwList.getSelectedRowIndex();
	    	  kwList.removeRow(row);
	    	  if (kwList.getRowCount() == 0) {
	  	        thesaurusField.setEnabled(true);
	    	  }
	      }
		
	};
	kwList.setCustomAddAction(thesaurusAction);
    kwList.setCustomDeleteAction(thesaurusDeleteAction);
    kwPanel.add(kwList);
    middlePanel.add(kwPanel);

    middlePanel.setBorder(new javax.swing.border.EmptyBorder(0,4*WizardSettings.PADDING,
        7*WizardSettings.PADDING,8*WizardSettings.PADDING));

    this.add(middlePanel, BorderLayout.CENTER);
  }

  private void showLookupDialog(String vocab) {

	    AbstractUIVocabularyPage page = 
	    	VocabularyPlugin.getInstance().getVocabularyPage(vocab);
	    
	    ModalDialog wpd = new ModalDialog(
	    		page,
	    		UIController.getInstance().getCurrentActiveWindow(),
	    		UISettings.POPUPDIALOG_WIDTH,
	    		UISettings.POPUPDIALOG_HEIGHT, false);
	    
	    //deactivate the default "ok" action
	    wpd.getRootPane().setDefaultButton(null);
	    wpd.resetBounds();
	    wpd.setVisible(true);

	    if (wpd.USER_RESPONSE == ModalDialog.OK_OPTION) {

			List terms = page.getSelectedTerms();
	    	for (int i=0; i<terms.size(); i++) {
				List newRow = new ArrayList();
	    		String term = (String) terms.get(i);
	    		newRow.add(term);
	    		kwList.addRow(newRow);
	    	}
	    }
  }

  /**
   *  The action to be executed when the "OK" button is pressed. If no onAdvance
   *  processing is required, implementation must return boolean true.
   *
   *  @return boolean true if dialog should close and return to wizard, false
   *          if not (e.g. if a required field hasn't been filled in)
   */
  public boolean onAdvanceAction() {

    Map listNVP = getKWListAsNVP(EMPTY_STRING);
    if (listNVP==null || listNVP.size() < 1) {

      WidgetFactory.hiliteComponent(kwLabel);
      return false;
    }
    return true;
  }


  /**
   *  @return a List contaiing 2 String elements - one for each column of the
   *  2-col list in which this surrogate is displayed
   *
   */
  private final StringBuffer surrogateBuff = new StringBuffer();
  //
  public List getSurrogate() {

    WidgetFactory.unhiliteComponent(kwLabel);

    List surrogate = new ArrayList();

    //thesaurus (first column) surrogate:
    surrogateBuff.delete(0, surrogateBuff.length());
    List rowLists = kwList.getListOfRowLists();
    boolean firstKW = true;
    String  nextKW  = null;

    for (Iterator it = rowLists.iterator(); it.hasNext(); ) {

      // CHECK FOR AND ELIMINATE EMPTY ROWS...
      Object nextRowObj = it.next();
      if (nextRowObj==null) continue;

      List nextRow = (List)nextRowObj;
      if (nextRow.size() < 1) continue;

      if (nextRow.get(0)==null) continue;
      nextKW = ((String)(nextRow.get(0))).trim();

      if (nextKW.equals(EMPTY_STRING)) continue;

      if (firstKW) firstKW = false;
      else surrogateBuff.append(", ");

      surrogateBuff.append(nextKW);
    }

    surrogate.add(surrogateBuff.toString());


    //keywords (second column) surrogate:
    String thesaurus   = ((String) thesaurusField.getSelectedItem()).trim();
    if (thesaurus==null) thesaurus = EMPTY_STRING;
    surrogate.add(thesaurus);

    return surrogate;
  }


  /**
   *  gets the Map object that contains all the key/value paired
   *
   *  @param    xPathRoot the string xpath to which this dialog's xpaths will be
   *            appended when making name/value pairs.  For example, in the
   *            xpath: /eml:eml/dataset/keywordSet[2]/keywordThesaurus, the
   *            root would be /eml:eml/dataset/keywordSet[2]
   *            NOTE - MUST NOT END WITH A SLASH, BUT MAY END WITH AN INDEX IN
   *            SQUARE BRACKETS []
   *
   *  @return   data the Map object that contains all the
   *            key/value paired settings for this particular wizard page
   */
  private OrderedMap returnMap = new OrderedMap();
  //
  public OrderedMap getPageData() {
    return getPageData(xPathRoot);
  }

  public OrderedMap getPageData(String xPathRoot) {

    returnMap.clear();

    returnMap.putAll(getKWListAsNVP(xPathRoot));

    String thesaurus = ((String) thesaurusField.getSelectedItem()).trim();
    //if (thesaurus!=null && !thesaurus.equals(EMPTY_STRING)) {
    if (!Util.isBlank(thesaurus)) {
      returnMap.put(xPathRoot + "/keywordThesaurus", thesaurus);
    }

    return returnMap;
  }


  private final OrderedMap listResultsMap    = new OrderedMap();
  private final StringBuffer listResultsBuff = new StringBuffer();
  //
  private OrderedMap getKWListAsNVP(String xPathRoot) {

    listResultsMap.clear();

    // CHECK FOR AND ELIMINATE EMPTY ROWS...
    kwList.deleteEmptyRows( CustomList.OR,
                            new short[] {  CustomList.EMPTY_STRING_TRIM  } );

    int rowNumber       = -1;
    int predicateIndex  = 0;
    List rowLists       = kwList.getListOfRowLists();
    String nextKWType   = null;


    for (Iterator it = rowLists.iterator(); it.hasNext(); ) {

      rowNumber++;
      // CHECK FOR AND ELIMINATE EMPTY ROWS...
      Object nextRowObj = it.next();
      if (nextRowObj==null) continue;

      List nextRow = (List)nextRowObj;
      if (nextRow.size() < 1) continue;

      listResultsBuff.delete(0,listResultsBuff.length());
      listResultsBuff.append(xPathRoot);
      listResultsBuff.append("/keyword[");
      listResultsBuff.append(++predicateIndex);
      listResultsBuff.append("]");
      listResultsMap.put(listResultsBuff.toString(),
                          ((String)(nextRow.get(0))).trim());

      if (nextRow.get(1)==null) continue;
      nextKWType = ((String)(nextRow.get(1))).trim();

      if (nextKWType.equals(EMPTY_STRING)) continue;

      listResultsBuff.delete(0,listResultsBuff.length());
      listResultsBuff.append(xPathRoot);
      listResultsBuff.append("/keyword[");
      listResultsBuff.append(predicateIndex);
      listResultsBuff.append("]/@keywordType");
      listResultsMap.put(listResultsBuff.toString(), nextKWType);
    }

    return listResultsMap;
  }

  /**
   *  The action to be executed when the "Prev" button is pressed. May be empty
   *  Here, it does nothing because this is just a Panel and not the outer container
   */

  public void onRewindAction() {
  }

  /**
   *  The action to be executed when the page is loaded
   *  Here, it does nothing because this is just a Panel and not the outer container
   */

  public void onLoadAction() {
  }

  /**
   *  gets the unique ID for this wizard page
   *
   *  @return   the unique ID String for this wizard page
   */
  public String getPageID() { return this.pageID;}

  /**
   *  gets the title for this wizard page
   *
   *  @return   the String title for this wizard page
   */
  public String getTitle() { return title; }

  /**
   *  gets the subtitle for this wizard page
   *
   *  @return   the String subtitle for this wizard page
   */
  public String getSubtitle() { return subtitle; }

  /**
   *  Returns the ID of the page that the user will see next, after the "Next"
   *  button is pressed. If this is the last page, return value must be null
   *
   *  @return the String ID of the page that the user will see next, or null if
   *  this is te last page
   */
  public String getNextPageID() { return this.nextPageID; }

  /**
     *  Returns the serial number of the page
     *
     *  @return the serial number of the page
     */
  public String getPageNumber() { return pageNumber; }

  // resets all fields to blank
  private void resetBlankData() {
    radioPanel.removeAll();
    radioPanel.add((JPanel)WidgetFactory.makeRadioPanel(buttonsText, 0, listener));
    thesaurusField.setSelectedItem(EMPTY_STRING);
    kwList.removeAllRows();

  }
  public boolean setPageData(OrderedMap map, String _xPathRoot) {

    if (_xPathRoot != null && _xPathRoot.trim().length() > 0) {
      this.xPathRoot = _xPathRoot;
    }

    if (map == null || map.isEmpty()) {
      resetBlankData();
      return true;
    }

    List toDeleteList = new ArrayList();
    Iterator keyIt = map.keySet().iterator();
    Object nextXPathObj = null;
    String nextXPath = null;
    Object nextValObj = null;
    String nextVal = null;

    List keywordList = new ArrayList();

    while (keyIt.hasNext()) {

      nextXPathObj = keyIt.next();
      if (nextXPathObj == null) {
        continue;
      }
      nextXPath = (String) nextXPathObj;

      nextValObj = map.get(nextXPathObj);
      nextVal = (nextValObj == null) ? "" : ( (String) nextValObj).trim();

      Log.debug(45, "Keyword:  nextXPath = " + nextXPath
          + "\n nextVal   = " + nextVal);

      if (nextXPath.indexOf(KEYWORD_REL_XPATH) > -1) {
        Log.debug(45, ">>>>>>>>>> adding to kwList: nextXPathObj="
            + nextXPathObj + "; nextValObj=" + nextValObj);
        List newRow = new ArrayList();
        newRow.add(nextVal);
        kwList.addRow(newRow);
        toDeleteList.add(nextXPathObj);
      } else if (nextXPath.indexOf(THESAURUS_REL_XPATH) > -1) {

        Log.debug(45, ">>>>>>>>>> adding to thesaurus: nextXPathObj="
            + nextXPathObj + "; nextValObj=" + nextValObj);

        radioPanel.removeAll();
        radioPanel.add((JPanel)WidgetFactory.makeRadioPanel(buttonsText, 1,
            listener));
        thesaurusField.setVisible(true);
        thesaurusField.setEnabled(true);
        if (kwList.getRowCount() != 0) {
        	//don't allow changes if there are terms already
        	thesaurusField.setEnabled(false);
        	kwList.setEditable(false);
        }
        thesaurusField.setSelectedItem(nextVal);
        toDeleteList.add(nextXPathObj);
      }

    }

    //remove entries we have used from map:
    Iterator dlIt = toDeleteList.iterator();
    while (dlIt.hasNext()) {
      map.remove(dlIt.next());

      //if anything left in map, then it included stuff we can't handle...
    }
    boolean returnVal = map.isEmpty();

    if (!returnVal) {

      Log.debug(20, "Keyword.setPageData returning FALSE! Map still contains:"
          + map);
    }
    return (returnVal);
  }
}
