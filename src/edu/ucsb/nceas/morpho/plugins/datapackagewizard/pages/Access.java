/**
 *  '$RCSfile: Access.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Saurabh Garg
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-05-19 22:49:27 $'
 * '$Revision: 1.45 $'
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;

import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.framework.ModalDialog;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.CustomList;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageLibrary;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.UISettings;
import edu.ucsb.nceas.utilities.OrderedMap;
import javax.swing.JRadioButton;

public class Access
    extends AbstractUIPage {

  public final String pageID = DataPackageWizardInterface.ACCESS;
  public final String pageNumber = "14";

  /////////////////////////////////////////////////////////

  public final String title = "Access Information";
  public final String subtitle = " ";

  private JPanel radioPanel;
  private String xPathRoot = "/eml:eml/access/";
  
  protected boolean isEntity = false;

  private boolean publicReadAccess = true;
  private boolean inherit = false;
  private String[] buttonsText = new String[] {
      "Yes, give read-only access to public.",
      "No."
  };

  private final String ALLOW_REL_XPATH = "allow[";
  private final String DENY_REL_XPATH = "deny[";
  private final String AUTHSYSTEM_REL_XPATH = "@authSystem";
  private final String ORDER_REL_XPATH = "@order";

  private final String[] colNames = {
      "Name", "Organization", "Email/Description", "Permissions"};
  private final Object[] editors = null;
  private CustomList accessList;

  private String AUTHSYSTEM_VALUE = "knb";
  private String ORDER_VALUE = "allowFirst";

  public static DefaultMutableTreeNode accessTreeNode = null;
  public static String accessTreeMetacatServerName = null;

  public Access(boolean isEntity) {
	this.isEntity = isEntity;
	nextPageID = DataPackageWizardInterface.SUMMARY;
    init();
  }

  /**
   * initialize access does frame-specific design - i.e. adding the widgets that
   are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  protected void init() {

    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    Box vBox = Box.createVerticalBox();
    vBox.add(WidgetFactory.makeDefaultSpacer());

    JLabel desc = null; 
    if (isEntity) {	
    	buttonsText = 
    		new String[] {
    		      "Yes, give read-only access to public.",
    		      "No.",
    		      "Same as Metadata."
    		  };
    	desc =
    		WidgetFactory.makeHTMLLabel(
	        "<p><b>Would you like the allow the public to read your data entity?"
	        + "</b></p>", 3);
    }
    else {
    	desc =
    		WidgetFactory.makeHTMLLabel(
	        "<p><b>Would you like the allow the public to read your data package?"
	        + "</b></p>", 3);
    }
    vBox.add(desc);

    ActionListener listener = new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        Log.debug(45, "got radiobutton command: " + e.getActionCommand());
        if (e.getActionCommand().equals(buttonsText[0])) {
          publicReadAccess = true;
          inherit = false;
        }
        if (e.getActionCommand().equals(buttonsText[1])) {
          publicReadAccess = false;
          inherit = false;
        }
        if (isEntity) {
	        if (e.getActionCommand().equals(buttonsText[2])) {
	            inherit = true;
            }
        }
      }
    };

    radioPanel = WidgetFactory.makeRadioPanel(buttonsText, 0, listener);
    radioPanel.setBorder(new javax.swing.border.EmptyBorder(0, WizardSettings.
        PADDING, 0, 0));

    vBox.add(radioPanel);
    vBox.add(WidgetFactory.makeDefaultSpacer());

    JLabel desc1 = null;
	if (isEntity) {	
	    desc1 = WidgetFactory.makeHTMLLabel(
		        "<p><b>Would you like to give special access rights to other people?"
		        + "</b> You can specify access for other members of your team or any "
		        + "other person. "
		        + "Use the table below to add, edit and "
		        + "delete access rights to your data entity.</p>", 3);
	}
	else {
		desc1 = WidgetFactory.makeHTMLLabel(
		        "<p><b>Would you like to give special access rights to other people?"
		        + "</b> You can specify access for other members of your team or any "
		        + "other person. "
		        + "Use the table below to add, edit and "
		        + "delete access rights to your data package.</p>", 3);
	}
    vBox.add(desc1);

    accessList = WidgetFactory.makeList(colNames, editors, 4,
        true, true, false, true, true, true);
    accessList.setBorder(new EmptyBorder(0, WizardSettings.PADDING,
        WizardSettings.PADDING,
        2 * WizardSettings.PADDING));

    vBox.add(accessList);
    vBox.add(WidgetFactory.makeDefaultSpacer());

    this.add(vBox);

    initActions();
  }

  /**
   *  Custom actions to be initialized for list buttons
   */
  private void initActions() {

    accessList.setCustomAddAction(

        new AbstractAction() {

      public void actionPerformed(ActionEvent e) {

        Log.debug(45, "\nAccess: CustomAddAction called");
        showNewAccessDialog();
      }
    });

    accessList.setCustomEditAction(
        new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        Log.debug(45, "\nAccess: CustomEditAction called");
        showEditAccessDialog();
      }
    });
  }

  protected void showNewAccessDialog() {

    AccessPage accessPage = (AccessPage) WizardPageLibrary.getPage(
        DataPackageWizardInterface.ACCESS_PAGE);
    ModalDialog wpd = new ModalDialog(accessPage,
        WizardContainerFrame.getDialogParent(),
        UISettings.POPUPDIALOG_WIDTH,
        UISettings.POPUPDIALOG_HEIGHT, false);
    wpd.setVisible(true);

    if (wpd.USER_RESPONSE == ModalDialog.OK_OPTION) {

      List newRows = accessPage.getSurrogate();
      Iterator itRow = newRows.iterator();
      while (itRow.hasNext()) {
        List newRow = (ArrayList) itRow.next();
        newRow.add(accessPage);
        accessList.addRow(newRow);
      }
    }
  }

  protected void showEditAccessDialog() {

    List selRowList = accessList.getSelectedRowList();

    if (selRowList == null || selRowList.size() < 5) {
      Log.debug(45, selRowList.size() + "");
      return;
    }

    Object dialogObj = selRowList.get(4);

    if (dialogObj == null || ! (dialogObj instanceof AccessPage)) {
      return;
    }
    AccessPage editAccessPage = (AccessPage) dialogObj;

    ModalDialog wpd = new ModalDialog(editAccessPage,
        WizardContainerFrame.getDialogParent(),
        UISettings.POPUPDIALOG_WIDTH,
        UISettings.POPUPDIALOG_HEIGHT, false);
    wpd.resetBounds();
    wpd.setVisible(true);

    List delRow = new ArrayList();
    List rowLists = accessList.getListOfRowLists();
    Iterator it = rowLists.iterator();
    int count = 0;
    while (it.hasNext()) {
      List row = (List) it.next();
      if (row.get(4) == dialogObj) {
        delRow.add(new Integer(count));
      }
      count++;
    }

    if (wpd.USER_RESPONSE == ModalDialog.OK_OPTION) {
      int size = delRow.size();
      for (int j = size; j > 0; j--) {
        Integer i = (Integer) delRow.get(j - 1);
        accessList.removeRow(i.intValue());
      }

      List newRows = editAccessPage.getSurrogate();
      Iterator itRow = newRows.iterator();
      while (itRow.hasNext()) {
        List newRow = (ArrayList) itRow.next();
        newRow.add(editAccessPage);
        accessList.addRow(newRow);
      }
    }
  }

  /**
   *  The action to be executed when the page is displayed. May be empty
   */
  public void onLoadAction() {
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
   *  gets the Map object that contains all the access/value paired
   *  settings for this particular wizard page
   *
   *  @return   data the Map object that contains all the
   *            access/value paired settings for this particular wizard page
   */
  private OrderedMap returnMap = new OrderedMap();

  public OrderedMap getPageData() {
    return getPageData(xPathRoot);
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

    int allowIndex = 1;
    int denyIndex = 1;
    Object nextRowObj = null;
    List nextRowList = null;
    Object nextUserObject = null;
    OrderedMap nextNVPMap = null;
    AccessPage nextAccessPage = null;
    boolean addedAuthSystem = false;

    if (isEntity && inherit) {
    	//we are removing everything
    	return null;
    }
    
    List rowLists = accessList.getListOfRowLists();
    
    if (publicReadAccess) {
      returnMap.put(rootXPath + AUTHSYSTEM_REL_XPATH, AUTHSYSTEM_VALUE);
      returnMap.put(rootXPath + ORDER_REL_XPATH, ORDER_VALUE);
      returnMap.put(rootXPath + "allow[" + (allowIndex) + "]/principal",
          "public");
      returnMap.put(rootXPath + "allow[" + (allowIndex++) + "]/permission",
          "read");
       addedAuthSystem = true;
    }
    else if(rowLists == null || rowLists.isEmpty())
    {
    	// for non-public readable and there is no another rules,
    	// we add a specific deny rule for public
    	returnMap.put(rootXPath + AUTHSYSTEM_REL_XPATH, AUTHSYSTEM_VALUE);
        returnMap.put(rootXPath + ORDER_REL_XPATH, ORDER_VALUE);
        returnMap.put(rootXPath + "deny[" + (denyIndex) + "]/principal",
            "public");
        returnMap.put(rootXPath + "deny[" + (denyIndex++) + "]/permission",
            "read");
        return returnMap;
    }
  
    //now we should go through the list of other rules specifed by user.
    
    //First, to check code above added  AUTHSYSTEM and ORDER  or not.
    // if not, add them
    if(!addedAuthSystem)
    {
      // need to add AUTHSYSTEM and ORDER as these were not added earlier not
      // non publicly readable documents.
      returnMap.put(rootXPath + AUTHSYSTEM_REL_XPATH, AUTHSYSTEM_VALUE);
      returnMap.put(rootXPath + ORDER_REL_XPATH, ORDER_VALUE);
    }

    Vector pagesProcessed = new Vector();

    for (Iterator it = rowLists.iterator(); it.hasNext(); ) {

      nextRowObj = it.next();
      if (nextRowObj == null) {
        continue;
      }

      nextRowList = (List) nextRowObj;
      //column 2 is user object - check it exists and isn't null:
      if (nextRowList.size() < 5) {
        continue;
      }
      nextUserObject = nextRowList.get(4);
      if (nextUserObject == null || pagesProcessed.contains(nextUserObject)) {
        continue;
      }
      pagesProcessed.add(nextUserObject);

      nextAccessPage = (AccessPage) nextUserObject;
      if (nextAccessPage.accessIsAllow) {
        nextNVPMap = nextAccessPage.getPageData(rootXPath + "allow[" +
            (allowIndex++) + "]");
      } else {
        nextNVPMap = nextAccessPage.getPageData(rootXPath + "deny[" +
            (denyIndex++) + "]");
      }

      returnMap.putAll(nextNVPMap);
    }

    return returnMap;
  }

  /**
   *  gets the unique ID for this wizard page
   *
   *  @return   the unique ID String for this wizard page
   */
  public String getPageID() {
    return pageID;
  }

  /**
   *  gets the title for this wizard page
   *
   *  @return   the String title for this wizard page
   */
  public String getTitle() {
    return title;
  }

  /**
   *  gets the subtitle for this wizard page
   *
   *  @return   the String subtitle for this wizard page
   */
  public String getSubtitle() {
    return subtitle;
  }

  /**
   *  Returns the ID of the page that the user will see next, after the "Next"
   *  button is pressed. If this is the last page, return value must be null
   *
   *  @return the String ID of the page that the user will see next, or null if
   *  this is te last page
   */
  public String getNextPageID() {
    return nextPageID;
  }

  /**
   *  Returns the serial number of the page
   *
   *  @return the serial number of the page
   */
  public String getPageNumber() {
    return pageNumber;
  }

  // resets all fields to blank
  private void resetBlankData() {
	  JPanel innerPanel = ( (JPanel) (radioPanel.getComponent(1)));
	  JRadioButton allowReadAccess = 
		  ( (JRadioButton) (innerPanel.getComponent(0)));
	  JRadioButton denyReadAccess = 
		  ( (JRadioButton) (innerPanel.getComponent(1)));
	  
	  if (isEntity) {  
		  JRadioButton inheritAccess = 
			  ( (JRadioButton) (innerPanel.getComponent(2)));
		  allowReadAccess.setSelected(false);
		  denyReadAccess.setSelected(false);
		  inheritAccess.setSelected(true);
		  publicReadAccess = false;
		  inherit = true;
	  }
	  else {
		  allowReadAccess.setSelected(false);
		  denyReadAccess.setSelected(true);
		  publicReadAccess = false;
		  inherit = false;
	  }
    accessList.removeAllRows();
  }

  OrderedMap publicMap = null;

  public boolean setPageData(OrderedMap map, String xPathRoot) {

    if (xPathRoot != null && xPathRoot.trim().length() > 0) {
      this.xPathRoot = xPathRoot;
    }

    if (map == null || map.isEmpty()) {

      // remove all access rules
      this.resetBlankData();

      return true;
    }

    List toDeleteList = new ArrayList();
    Iterator keyIt = map.keySet().iterator();
    Object nextXPathObj = null;
    String nextXPath = null;
    Object nextValObj = null;
    String nextVal = null;

    List accessAllowList = new ArrayList();
    List accessDenyList = new ArrayList();

    while (keyIt.hasNext()) {

      nextXPathObj = keyIt.next();
      if (nextXPathObj == null) {
        continue;
      }
      nextXPath = (String) nextXPathObj;

      nextValObj = map.get(nextXPathObj);
      nextVal = (nextValObj == null) ? "" : ( (String) nextValObj).trim();

      Log.debug(45, "Access:  nextXPath = " + nextXPath
          + "\n nextVal   = " + nextVal);

      // remove everything up to and including the last occurrence of
      // this.xPathRoot to get relative xpaths, in case we're handling a
      // project elsewhere in the tree...
      nextXPath = nextXPath.substring(nextXPath.lastIndexOf(this.xPathRoot)
          + this.xPathRoot.length());

      Log.debug(45, "Access: TRIMMED nextXPath   = " + nextXPath);

      if (nextXPath.startsWith(AUTHSYSTEM_REL_XPATH)) {
        AUTHSYSTEM_VALUE = nextVal;
        toDeleteList.add(nextXPathObj);
      } else if (nextXPath.startsWith(ORDER_REL_XPATH)) {
        ORDER_VALUE = nextVal;
        toDeleteList.add(nextXPathObj);
      } else if (nextXPath.startsWith(ALLOW_REL_XPATH)) {

        Log.debug(45, ">>>>>>>>>> adding to accessAllowList: nextXPathObj="
            + nextXPathObj + "; nextValObj=" + nextValObj);
        addToAccess(nextXPathObj, nextValObj, accessAllowList, ALLOW_REL_XPATH);
        toDeleteList.add(nextXPathObj);
      } else if (nextXPath.startsWith(DENY_REL_XPATH)) {

        Log.debug(45, ">>>>>>>>>> adding to accessDenystepList: nextXPathObj="
            + nextXPathObj + "; nextValObj=" + nextValObj);
        addToAccess(nextXPathObj, nextValObj, accessDenyList, DENY_REL_XPATH);
        toDeleteList.add(nextXPathObj);

      } else if (nextXPath.startsWith("@scope")) {

        //get rid of scope attribute, if it exists
        toDeleteList.add(nextXPathObj);
      }
    }

    //remove entries we have used from map:
    Iterator dlIt = toDeleteList.iterator();
    while (dlIt.hasNext()) {
      map.remove(dlIt.next());
    }

    //if anything left in map, then it included stuff we can't handle...
    boolean returnVal = map.isEmpty();

    if (!returnVal) {

      Log.debug(20, "Access.setPageData returning FALSE! Map still contains:"
          + map);

      return false;
    }

    Iterator allowIt = accessAllowList.iterator();
    Iterator denyIt = accessDenyList.iterator();
    Object nextStepMapObj = null;
    OrderedMap nextStepMap = null;
    int accessPredicate = 1;

    accessList.removeAllRows();
    boolean accessAllowRetVal = true;
    boolean accessDenyRetVal = true;

    while (allowIt.hasNext()) {

      nextStepMapObj = allowIt.next();
      if (nextStepMapObj == null) {
        continue;
      }
      nextStepMap = (OrderedMap) nextStepMapObj;
      if (nextStepMap.isEmpty() || nextStepMap == publicMap) {
        continue;
      }

      AccessPage nextStep = (AccessPage) WizardPageLibrary.getPage(
          DataPackageWizardInterface.ACCESS_PAGE);

      boolean checkAccess = nextStep.setPageData(nextStepMap,
          this.xPathRoot + "allow[" +
          + (accessPredicate++) + "]");

      if (!checkAccess) {
        accessAllowRetVal = false;
      }
      List newRows = nextStep.getSurrogate();
      Iterator itRow = newRows.iterator();
      while (itRow.hasNext()) {
        List newRow = (ArrayList) itRow.next();
        newRow.add(nextStep);
        accessList.addRow(newRow);
      }
    }

    while (denyIt.hasNext()) {

      nextStepMapObj = denyIt.next();
      if (nextStepMapObj == null) {
        continue;
      }
      nextStepMap = (OrderedMap) nextStepMapObj;
      if (nextStepMap.isEmpty() || nextStepMap == publicMap) {
        continue;
      }

      AccessPage nextStep = (AccessPage) WizardPageLibrary.getPage(
          DataPackageWizardInterface.ACCESS_PAGE);

      boolean checkAccess = nextStep.setPageData(nextStepMap,
          this.xPathRoot + "deny[" +
          + (accessPredicate++) + "]/");

      if (!checkAccess) {
        accessDenyRetVal = false;
      }
      List newRows = nextStep.getSurrogate();
      Iterator itRow = newRows.iterator();
      while (itRow.hasNext()) {
        List newRow = (ArrayList) itRow.next();
        newRow.add(nextStep);
        accessList.addRow(newRow);
      }
    }

    //check access return valuse...
    if (!accessAllowRetVal || !accessDenyRetVal) {
      Log.debug(20, "Access.setPageData - Access sub-class returned FALSE");
    }

    if (publicMap != null) {
      // TODO parse publicMap to set public access radio buttons...

      Iterator pmIt = publicMap.keySet().iterator();
      boolean invalidSizeError = false;
      boolean hasRead = false;
      boolean hasPublic = false;

      if (publicMap.size() != 2) {
        invalidSizeError = true;
      }

      while (pmIt.hasNext() && !invalidSizeError) {
        nextXPathObj = pmIt.next();
        if (nextXPathObj == null) {
          continue;
        }
        nextXPath = (String) nextXPathObj;

        nextValObj = publicMap.get(nextXPathObj);
        nextVal = (nextValObj == null) ? "" : ( (String) nextValObj).trim();

        if (nextVal.compareTo("read") == 0) {
          hasRead = true;

          JPanel innerPanel = ( (JPanel) (radioPanel.getComponent(1)));
          JRadioButton allowReadAccess = ( (JRadioButton) (innerPanel.
              getComponent(0)));
          JRadioButton denyReadAccess = ( (JRadioButton) (innerPanel.
              getComponent(1)));
          if (nextXPath.indexOf("allow") > -1) {
            allowReadAccess.setSelected(true);
            denyReadAccess.setSelected(false);
            publicReadAccess = true;
          } else {
            allowReadAccess.setSelected(false);
            denyReadAccess.setSelected(true);
            publicReadAccess = false;
          }

        } else if (nextVal.compareTo("public") == 0) {
          hasPublic = true;
        }

      }
      if (invalidSizeError && !hasRead && !hasPublic) {
        Log.debug(20,
            "Access.setPageData returning FALSE! Map contains invalid public access:"
            + publicMap);
      }

    } else {
      JPanel innerPanel = ( (JPanel) (radioPanel.getComponent(1)));
      JRadioButton allowReadAccess = ( (JRadioButton) (innerPanel.
          getComponent(0)));
      JRadioButton denyReadAccess = ( (JRadioButton) (innerPanel.
          getComponent(1)));
        allowReadAccess.setSelected(false);
        denyReadAccess.setSelected(true);
        publicReadAccess = false;
    }

    return (returnVal && accessAllowRetVal && accessDenyRetVal);
  }

  private void addToAccess(Object nextPersonnelXPathObj,
      Object nextPersonnelVal, List accessstepList,
      String xPath) {

    if (nextPersonnelXPathObj == null) {
      return;
    }
    String nextPersonnelXPath = (String) nextPersonnelXPathObj;
    int predicate = getFirstPredicate(nextPersonnelXPath, xPath);

    // NOTE predicate is 1-relative, but List indices are 0-relative!!!
    if (predicate >= accessstepList.size()) {

      for (int i = accessstepList.size(); i <= predicate; i++) {
        accessstepList.add(new OrderedMap());
      }
    }

    if (predicate < accessstepList.size()) {
      Object nextMapObj = accessstepList.get(predicate);
      OrderedMap nextMap = (OrderedMap) nextMapObj;
      nextMap.put(nextPersonnelXPathObj, nextPersonnelVal);

      if ( ( (String) nextPersonnelVal).compareTo("public") == 0) {
        publicMap = nextMap;
      }

    } else {
      Log.debug(15,
          "**** ERROR - Access.addToAccess() - predicate > accessstepList.size()");
    }
  }

  private int getFirstPredicate(String xpath, String firstSegment) {

    String tempXPath
        = xpath.substring(xpath.indexOf(firstSegment) + firstSegment.length());

    return Integer.parseInt(
        tempXPath.substring(0, tempXPath.indexOf("]")));
  }
}
