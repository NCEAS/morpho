/**
 *  '$RCSfile: ReferencesHandler.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2004-03-30 18:39:21 $'
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

package edu.ucsb.nceas.morpho.datapackage;

import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.framework.ModalDialog;
import edu.ucsb.nceas.morpho.framework.QueryRefreshInterface;
import edu.ucsb.nceas.morpho.framework.SwingWorker;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.UISettings;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.w3c.dom.Node;
import java.util.Comparator;

public class ReferencesHandler {


  private final String DEFAULT_DROPDOWN_ITEM = " ";

  private final String EXT_DIALOG_DROPDOWN_ITEM = "Select from a different data package";

  private QueryRefreshInterface queryRefreshInterface;
  private String genericName;
  private String[] surrogateXPaths;
  private ModalDialog externalRefsDialog;
  private ExternalRefsPage externalRefsPage;
  private List listenerList = new ArrayList();

  /**
   * constructor
   *
   * @param genericName String that defines which part of the EML schema this
   *   ReferencesHandler will be handling (eg Party, etc). Generic name strings
   *   are defined in schema-specific KeymapConfig xml file (e.g.
   *   eml200KeymapConfig.xml)
   * @param surrogateXPaths String[] array containing any mixture of:<br />
   *
   * <p>(a) xpaths pointing to the fields within the DOM subtree that should be
   * used to construct the surrogate strings returned by this class. XPaths must
   * start with a "/" and be relative to the subtree root-node, but must *NOT*
   * include the subtree root node itself (because, for example, if the subtree
   * is "Party", its root node could be "Creator" or "Owner" or any number of
   * other things</p>
   *
   * <p>(b) strings that will be inserted between the values returned from the
   * above xpaths (these strings are identified by their first character not
   * being a "/").</p>
   * <p>So for example, if the genericName is set to "parties" and you want the
   * surrogate to be: "firstname lastname - (role)", then the array will look
   * like this for EML2.0:</p>
   * <pre>
   *     {
   *       "/individualName/givenName",
   *       "/individualName/surName",
   *       " - (",
   *       "/role",
   *       ")",
  *     }</pre>
   */
  public ReferencesHandler(String genericName, String[] surrogateXPaths) {

    this.genericName = genericName;
    this.surrogateXPaths = surrogateXPaths;
  }


  /**
   * get a Map whose keys are all the IDs currently in the DataPackage that
   * point to subtree root-nodes corresponding to the genericName used to
   * instantiate this ReferencesHandler, and whose values are String surrogates
   * for those referenced subtrees
   *
   * @param dataPkg the AbstractDataPackage from whence the references should
   *   be obtained. If this is null, an empty Map is returned
   * @param extraSlots int the number of additional array slots to add to the
   * return-array size for subsequent use by the calling code
   * @return an array of ReferenceMapping objects containing all the IDs
   *   currently in the DataPackage that point to subtree root-nodes
   *   corresponding to the genericName used to instantiate this
   *   ReferencesHandler, and String surrogates for those referenced subtrees.
   *   An empty List is returned if the passed dataPkg parameter is null
   */
  private List getReferences(AbstractDataPackage dataPkg) {

    if (dataPkg==null) return new ArrayList(0);

    List idsList = dataPkg.getIDsForNodesWithName(this.genericName);

    Iterator idIt = idsList.iterator();

    List returnVals = new ArrayList();

    while (idIt.hasNext()) {

      String nextID = (String)idIt.next();
      if (nextID == null || nextID.trim().length() < 1)continue;

      Node nextSubtree = dataPkg.getSubtreeAtReference(nextID);

      if (nextSubtree == null)continue;

      returnVals.add(new ReferenceMapping(nextID, getSurrogate(nextSubtree)));
    }
    return returnVals;
  }


  /**
   * get a JComboBox containing a blank entry at location (0), an entry to
   * launch an ExternalRefsDialog at location (1), and all the IDs in the
   * current DataPackage that point to subtree root-nodes corresponding to the
   * genericName used to instantiate this ReferencesHandler, and whose values
   * are String surrogates for those referenced subtrees
   *
   * @param dataPkg the AbstractDataPackage from whence the references should
   *   be obtained. If this is null, an empty JComboBox is returned
   * @param listener ReferencesListener to be called back when a selection is
   *   made.
   * @param parent JFrame that will be the parent of any modal dialogs that
   *   this class may create
   * @return a JComboBox containing a blank entry at location (0), an entry to
   *   launch an ExternalRefsDialog at location (1), and all the IDs in the
   *   current DataPackage that point to subtree root-nodes corresponding to
   *   the genericName used to instantiate this ReferencesHandler, and whose
   *   values are String surrogates for those referenced subtrees. An empty
   *   JComboBox is returned if passed AbstractDataPackage is null
   */
  public JComboBox getJComboBox(AbstractDataPackage dataPkg,
                                ReferencesListener listener,
                                Frame parent) {

    JComboBox dropdown = new JComboBox();

    if (dataPkg==null) return dropdown;

    updateJComboBox(dataPkg, dropdown);

    final ReferencesHandler   instance = this;
    final AbstractDataPackage finalPkg = dataPkg;
    final Frame finalParent = parent;
    final JComboBox finalDropdown = dropdown;

    // listens for selection events. List consistes of an entry at the top which
    // is the default "none selected" entry (probably just a blank entry), then
    // the second list item is the one that launches the CopyExternalRefsDialog.
    // The remaining list items are the available IDs in the current datapackage
    dropdown.addItemListener(new ItemListener() {

      public void itemStateChanged(ItemEvent e) {

        if (e.getStateChange()!=e.SELECTED) return;

        JComboBox source = (JComboBox) e.getSource();
        ReferenceSelectionEvent event = null;

        switch (source.getSelectedIndex()) {


          case 0: //top item ("none selected" - blank) selected
            Log.debug(45, "ReferenceHandler ItemListener - top item "
                      + "('none selected' - blank) selected ");
            event = new ReferenceSelectionEvent(
                null, ReferenceSelectionEvent.UNDEFINED, null);
            break;

          case 1: //second item (copy from external package) selected
            Log.debug(45, "ReferenceHandler ItemListener - second item "
                      + "(copy from external package) selected");
            event = instance.showCopyExternalRefsDialog(finalPkg, finalParent);
            break;

          default: //third or subsequent item selected (ref from current pkg)
            ReferenceMapping refMap = (ReferenceMapping)(source.getSelectedItem());
            Log.debug(45, "ReferenceHandler ItemListener - third or subsequent "
                      + "item selected (ref from current pkg); refID = "+refMap.getID());
            Node subtree = finalPkg.getSubtreeAtReference(refMap.getID());
            if (subtree == null) {
              Log.debug(15,
                        "ReferenceHandler ItemListener - no subtree found with refID: "
                        + refMap.getID());
              //list contained an incorrect entry - so refresh list...
              updateJComboBox(finalPkg, finalDropdown);
              return;
            }
            OrderedMap map = XMLUtilities.getDOMTreeAsXPathMap(subtree);

            event = new ReferenceSelectionEvent(
              refMap.getID(), ReferenceSelectionEvent.CURRENT_DATA_PACKAGE, map);
        }
        fireReferencesSelectionEvent(event);
      }
    });

    return dropdown;
  }


  /**
   * update the ListModel backing the passed JComboBox - @see getJComboBox()
   *
   * @param dataPkg the AbstractDataPackage from whence the references should
   *   be obtained. If this is null, an empty JComboBox is returned
   * @param dropdown ReferencesListener to be called back when a selection is
   *   made.
   */
  public void updateJComboBox(AbstractDataPackage dataPkg, JComboBox dropdown) {

    List refMapList = this.getReferences(dataPkg);

    refMapList.add(new ReferenceMapping("", ""));
    refMapList.add(new ReferenceMapping(" ", " "));

    Object[] array = refMapList.toArray();

    Arrays.sort(array, new Comparator() {

      public int compare(Object o1, Object o2) {
        String s1 = o1.toString();
        String s2 = o2.toString();
        return (s1.compareTo(s2));
      }
    });

    int refMappingsLength = 2 + array.length;
    ReferenceMapping[] refMappings = new ReferenceMapping[refMappingsLength];

    refMappings[0] = new ReferenceMapping(DEFAULT_DROPDOWN_ITEM + "0",
                                          DEFAULT_DROPDOWN_ITEM);
    refMappings[1] = new ReferenceMapping(EXT_DIALOG_DROPDOWN_ITEM + "1",
                                          EXT_DIALOG_DROPDOWN_ITEM);

    for (int i=0; i < array.length; i++) {

      if (i > refMappingsLength - 2) break;
      refMappings[i + 2] = (ReferenceMapping)array[i];
    }

    dropdown.setModel(new DefaultComboBoxModel(refMappings));
    dropdown.invalidate();
    dropdown.validate();
  }


  private String dumpArray(Object[] array) {

    if (array==null) return "";
    StringBuffer buff = new StringBuffer(0);

    for (int i = 0; i < array.length; i++) {

      buff.append(array[i]);
      buff.append("\n");
    }
    return buff.toString();
  }

  /**
   * Open a dialog to allow the user to browse a list of existing local
   * DataPackages and discover what metadata is available of the same type as is
   * handled by this instance of ReferencesHandler (eg parties), so entries can
   * be copied to the current package and used for subsequent references
   *
   * @param dataPkg the current AbstractDataPackage - used only to determine
   *   which is the current datapackage, and thus not show it in the dialog
   *   listing
   * @param parent JFrame the parent for the modal dialog
   * @return the subtree root Node for the selected entry in an external
   *   datapackage
   */
  private ReferenceSelectionEvent showCopyExternalRefsDialog(
      AbstractDataPackage dataPkg, Frame parent) {

    Node returnNode = null;
    //event object will be populated by dialog...
    ReferenceSelectionEvent event = new ReferenceSelectionEvent();

    if (externalRefsDialog == null) {

      externalRefsPage = new ExternalRefsPage();
      externalRefsDialog = new ModalDialog(externalRefsPage,
                                           parent,
                                           UISettings.POPUPDIALOG_WIDTH,
                                           UISettings.POPUPDIALOG_HEIGHT);
    }
    externalRefsPage.setReferenceSelectionEvent(event);
    externalRefsPage.setCurrentDataPackageID(dataPkg.getPackageId());
    externalRefsDialog.setVisible(true);

    //first get a list of available local datapackages
    //then set these in dialog and show it
    doQueryAndPopulateDialog();



    //...and get corresponding node from external datapackage

    OrderedMap map = XMLUtilities.getDOMTreeAsXPathMap(returnNode);

    return event;
  }




////////////////////////////////////////////////////////////////////////////////


  /**
   * Run the local search query
   *
   * @return boolean
   */
  private boolean doQueryAndPopulateDialog() {

    final QueryRefreshInterface queryPlugin = getQueryPlugin();

    if (queryPlugin == null) return false;


    final SwingWorker worker = new SwingWorker() {

      private AbstractTableModel resultsModel;

      public Object construct() {

        resultsModel = queryPlugin.doOwnerQueryForCurrentUser();

        return null;
      }


      //Runs on the event-dispatching thread.
      public void finished() {
        externalRefsPage.setQueryResults(resultsModel);
      }
    };
    worker.start(); //required for SwingWorker 3
    return true;
  }//doQuery


  private QueryRefreshInterface getQueryPlugin() {

    if (queryRefreshInterface == null) {

      ServiceController sc;
      QueryRefreshInterface queryRefreshInterface = null;
      try {
        sc = ServiceController.getInstance();
        queryRefreshInterface = (QueryRefreshInterface)sc.getServiceProvider(
            QueryRefreshInterface.class);
      } catch (ServiceNotHandledException se) {
        Log.debug(6, se.getMessage());
        se.printStackTrace();
      }
    }
    return queryRefreshInterface;
  }



  private StringBuffer surrogateBuff = new StringBuffer();
  //
  private String getSurrogate(Node subtreeRoot) {

    if (surrogateXPaths==null || subtreeRoot==null) return "";
    surrogateBuff.delete(0, surrogateBuff.length());
    String baseXPath = "/" + subtreeRoot.getNodeName();
    Log.debug(45, "ReferencesHandler.getSurrogate() - baseXPath = "+baseXPath);
    Node textNode = null;

    for (int i=0; i<surrogateXPaths.length; i++) {

      String nextEntry = surrogateXPaths[i];
      if (nextEntry.startsWith("/")) {

        try {
          textNode = XMLUtilities.getTextNodeWithXPath(subtreeRoot,
                                                      baseXPath + nextEntry);
        } catch (Exception ex) {
          Log.debug(15, "exception in ReferenceHandler.getSurrogate() - "+ex);
          ex.printStackTrace();
        }
        if (textNode!=null) surrogateBuff.append(textNode.getNodeValue());
        else surrogateBuff.append(" ");

      } else {
        surrogateBuff.append(nextEntry);
      }
    }
    return surrogateBuff.toString();
  }


  /**
   * register a listener
   * @param listener ReferencesListener
   */
  public void addReferencesListener(ReferencesListener listener) {

    if (listener==null) {
      Log.debug(15, "addReferencesListener() received NULL listener");
      return;
    }

    if (listenerList.contains(listener)) {
      Log.debug(15, "Not adding; listener already registered: " + listener);
      return;
    }
    listenerList.add(listener);
  }

  /**
   * unregister a listener
   * @param listener ReferencesListener
   */
  public void removeReferencesListener(ReferencesListener listener) {

    if (listener==null) {
      Log.debug(15, "removeReferencesListener() received NULL listener");
      return;
    }

    if (!listenerList.contains(listener)) {
      Log.debug(15, "Cannot remove - listener wasn't registered: " + listener);
      return;
    }
    listenerList.remove(listener);
  }

  private void fireReferencesSelectionEvent(ReferenceSelectionEvent event) {

    Iterator listenerIt = listenerList.iterator();
    while (listenerIt.hasNext()) {
      ReferencesListener nextListener = (ReferencesListener)listenerIt.next();
      if (nextListener!=null) nextListener.referenceSelected(event);
    }
  }

}


////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////


class ExternalRefsPage extends AbstractUIPage {


  private JTable table;
  private ReferenceSelectionEvent event;
  private String refID;
  private String currentDataPackageID;
  private Node referencedSubtree;


  ExternalRefsPage() {
    init();
  }

  protected void setReferenceSelectionEvent(ReferenceSelectionEvent event) {

    this.event = event;
  }

  protected void setCurrentDataPackageID(String currentDataPackageID) {

    this.currentDataPackageID = currentDataPackageID;
  }

  private void init() {

    this.setLayout(new BorderLayout());

    table = new JTable();
    JScrollPane scroll = new JScrollPane(table);
    scroll.getViewport().setBackground(Color.white);
    this.add(scroll, BorderLayout.WEST);

    JPanel refsPanel = new JPanel();
    refsPanel.setLayout(new BoxLayout(refsPanel, BoxLayout.Y_AXIS));

    refsPanel.setOpaque(true);
    refsPanel.setBackground(Color.green);

    this.add(refsPanel, BorderLayout.CENTER);
  }


  /**
   * sets the AbstractTableModel to be used as the basis of the query results
   * listing (shows all packages owned by current user on local system)
   *
   * @param model AbstractTableModel to be used as basis of query results
   * listing (shows all packages owned by current user on local system)
   */
  public void setQueryResults(AbstractTableModel model) {

    table.setModel(model);
  }


  /**
   *  gets the unique ID for this UI page
   *
   *  @return   the unique ID String for this UI page
   */
  public String getPageID() {
    throw new UnsupportedOperationException(
      "ExternalRefsPage -> getPageID() method not implemented!");
  }


  /**
   *  gets the title for this UI page
   *
   *  @return   the String title for this UI page
   */
  public String getTitle() {
    throw new UnsupportedOperationException(
      "ExternalRefsPage -> getTitle() method not implemented!");
  }



  /**
   *  gets the subtitle for this UI page
   *
   *  @return   the String subtitle for this UI page
   */
  public String getSubtitle() {
    throw new UnsupportedOperationException(
      "ExternalRefsPage -> getSubtitle() method not implemented!");
  }



  /**
   *  Returns the ID of the page that the user will see next, after the "Next"
   *  button is pressed. If this is the last page, return value must be null
   *
   *  @return the String ID of the page that the user will see next, or null if
   *  this is te last page
   */
  public String getNextPageID() {
    throw new UnsupportedOperationException(
      "ExternalRefsPage -> getNextPageID() method not implemented!");
  }



  /**
   *  Returns the serial number of the page
   *
   *  @return the serial number of the page
   */
  public String getPageNumber() {
    throw new UnsupportedOperationException(
      "ExternalRefsPage -> getPageNumber() method not implemented!");
  }



  /**
   *  The action to be executed when the page is displayed. May be empty
   */
  public void onLoadAction() {}


  /**
   *  The action to be executed when the "Prev" button is pressed. May be empty
   *
   */
  public void onRewindAction() {}


  /**
   *  The action to be executed when the "Next" button (pages 1 to last-but-one)
   *  or "Finish" button(last page) is pressed. May be empty
   *
   *  @return boolean true if wizard should advance, false if not
   *          (e.g. if a required field hasn't been filled in)
   */
  public boolean onAdvanceAction() {

    if (refID==null) return false;

    event.setReferenceID(refID);
    event.setXPathValsMap(XMLUtilities.getDOMTreeAsXPathMap(referencedSubtree));
    return true;
  }



  /**
   *  gets the Map object that contains all the key/value paired
   *  settings for this particular UI page
   *
   *  @return   data the Map object that contains all the
   *            key/value paired settings for this particular UI page
   */
  public OrderedMap getPageData()  {
    throw new UnsupportedOperationException(
      "ExternalRefsPage -> getPageData() method not implemented!");
  }


  /**
   * gets the Map object that contains all the key/value paired settings for
   * this particular UI page
   *
   * @param rootXPath the root xpath to prepend to all the xpaths returned by
   *   this method
   * @return data the Map object that contains all the key/value paired
   *   settings for this particular UI page
   */
  public OrderedMap getPageData(String rootXPath) {
    throw new UnsupportedOperationException(
      "ExternalRefsPage -> getPageData(rootXPath) method not implemented!");
  }



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
  public boolean setPageData(OrderedMap data, String rootXPath) {
    throw new UnsupportedOperationException(
      "ExternalRefsPage -> setPageData() method not implemented!");
  }


}

class ReferenceMapping {

  private String surrogate;
  private String ID;

  public ReferenceMapping(String ID, String surrogate) {

    this.ID = ID;
    this.surrogate = surrogate;
  }

  public String toString() {

    return surrogate;
  }


  public String getID() {

    return ID;
  }

}
