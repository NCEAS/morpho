/**
 *  '$RCSfile: ReferencesHandler.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2004-04-07 06:07:08 $'
 * '$Revision: 1.19 $'
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

import edu.ucsb.nceas.morpho.framework.ModalDialog;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.UISettings;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.TransformerException;

import java.awt.Frame;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import org.apache.xerces.dom.DOMImplementationImpl;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ReferencesHandler {


  private final String DEFAULT_DROPDOWN_ITEM = " ";

  private final String EXT_DIALOG_DROPDOWN_ITEM = "Select from a different data package";

  private String displayName;
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

    this.displayName = genericName;
    this.genericName = genericName;
    this.surrogateXPaths = surrogateXPaths;
  }


  /**
   * Method to set displayName for the ExternalRefsDialog
   *
   * @param displayName displayName for the ExternalRefsDialog
   */
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   * Method to return genericName
   * @return String
   */
  public String getGenericName()
  {
    return this.genericName;
  }


  /**
   * get a Map whose keys are all the IDs currently in the DataPackage that
   * point to subtree root-nodes corresponding to the genericName used to
   * instantiate this ReferencesHandler, and whose values are String surrogates
   * for those referenced subtrees
   *
   * @param dataPkg the AbstractDataPackage from whence the references should
   *   be obtained. If this is null, an empty Map is returned
   * @param suppressRefID String
   * @return a List of ReferenceMapping objects containing all the IDs
   *   currently in the DataPackage that point to subtree root-nodes
   *   corresponding to the genericName used to instantiate this
   *   ReferencesHandler, and String surrogates for those referenced subtrees.
   *   An empty List is returned if the passed dataPkg parameter is null
   */
  protected List getReferences(AbstractDataPackage dataPkg, String suppressRefID) {

    if (dataPkg==null) return new ArrayList(0);
    if (suppressRefID==null) suppressRefID = "";

    List idsList = dataPkg.getIDsForNodesWithName(this.genericName);

    Iterator idIt = idsList.iterator();

    List returnVals = new ArrayList();

    while (idIt.hasNext()) {

      String nextID = (String)idIt.next();
      if (nextID == null
          || nextID.trim().length() < 1 || nextID.equals(suppressRefID)) continue;

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

    updateJComboBox(dataPkg, dropdown, null);

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
                null, ReferenceSelectionEvent.UNDEFINED, null, null);
            break;

          case 1: //second item (copy from external package) selected
            Log.debug(45, "ReferenceHandler ItemListener - second item "
                      + "(copy from external package) selected");
            event = instance.showCopyExternalRefsDialog(finalPkg, finalParent);
            finalDropdown.setSelectedIndex(0);
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
              updateJComboBox(finalPkg, finalDropdown, null);
              return;
            }
            OrderedMap map = XMLUtilities.getDOMTreeAsXPathMap(subtree);

            event = new ReferenceSelectionEvent(
              refMap.getID(),
              ReferenceSelectionEvent.CURRENT_DATA_PACKAGE,
              map, subtree.getNodeName());
        }
        fireReferencesSelectionEvent(event);
      }
    });

    addReferencesListener(listener);

    return dropdown;
  }


  /**
   * update the ListModel backing the passed JComboBox - @see getJComboBox()
   *
   * @param dataPkg the AbstractDataPackage from whence the references should
   *   be obtained. If this is null, an empty JComboBox is returned
   * @param dropdown ReferencesListener to be called back when a selection is
   *   made.
   * @param suppressRefID String refID whose list entry will be removed - set
   *   this to the refID of the subtree represented by the calling dialog, so
   *   the dialog can't reference itself
   */
  public void updateJComboBox(AbstractDataPackage dataPkg,
                              JComboBox dropdown, String suppressRefID) {

    List refMapList = this.getReferences(dataPkg, suppressRefID);

    Object[] array = refMapList.toArray();

    Arrays.sort(array, new Comparator() {

      public int compare(Object o1, Object o2) {

        return (o1.toString().compareTo(o2.toString()));
      }
    });

    int refMappingsLength = 2 + array.length;
    ReferenceMapping[] refMappings = new ReferenceMapping[refMappingsLength];

    refMappings[0] = new ReferenceMapping(DEFAULT_DROPDOWN_ITEM + "0",
                                          DEFAULT_DROPDOWN_ITEM);
    refMappings[1] = new ReferenceMapping(EXT_DIALOG_DROPDOWN_ITEM + "1",
                                          EXT_DIALOG_DROPDOWN_ITEM);
    for (int i=0; i < array.length; i++) {
      refMappings[i + 2] = (ReferenceMapping)array[i];

    }
    dropdown.setModel(new DefaultComboBoxModel(refMappings));
    dropdown.validate();
    dropdown.repaint();
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

    //event object will be populated by dialog...
    ReferenceSelectionEvent event = new ReferenceSelectionEvent();

    if (externalRefsDialog == null) {

    externalRefsPage = new ExternalRefsPage(this);

    externalRefsDialog = new ModalDialog(externalRefsPage,
                                           parent,
                                           UISettings.POPUPDIALOG_WIDTH,
                                           UISettings.POPUPDIALOG_HEIGHT,
                                           false);
    }
    externalRefsPage.setReferenceSelectionEvent(event);
    externalRefsPage.setCurrentDataPackageID(dataPkg.getPackageId());
    externalRefsPage.setDisplayName(this.displayName);
    externalRefsDialog.setVisible(true);
    //...and get corresponding node from external datapackage
    if (externalRefsDialog.USER_RESPONSE==ModalDialog.OK_OPTION) {

        event = externalRefsPage.getReferenceSelectionEvent();
    }
    return event;
  }



  /**
   * gets the subtree with refID "subtreeID" from the datapackage "adp", and
   * replaces it with the subtree described in the newData OrderedMap
   *
   * @param adp AbstractDataPackage the current datapkg
   * @param subtreeID String refID of the subtree to be replaced
   * @param newData OrderedMap containing new data to replace referenced subtree
   * @return Node the replacement subtree, or null if original subtree not found,
   * or if any of the input parameters are invalid
   */
  public static Node updateOriginalReferenceSubtree(AbstractDataPackage adp,
                                             String subtreeID,
                                             OrderedMap newData) {

    Log.debug(45, "updateOriginalReferenceSubtree() Got subtreeID="+subtreeID
              +"\nnewData map:"+newData);
    if (subtreeID == null || subtreeID.trim().length()<1) {
      Log.debug(15, "\n** ERROR - subtreeID map is NULL/blank!");
      return null;
    }
    if (newData == null) {
      Log.debug(15, "\n** ERROR - newData map is NULL!");
      return null;
    }
    if (adp == null) {
      Log.debug(15, "\n** ERROR - AbstractDataPackage map is NULL!");
      return null;
    }

    Node referencedSubtree = adp.getSubtreeAtReference(subtreeID);
    if (referencedSubtree==null) {
      Log.debug(15, "ReferencesHandler.updateOriginalReferenceSubtree() - "
                +"got null back from datapackage - couldn't find subtree with "
                +"this refID: " + subtreeID);
      return null;
    }
    String rootNodeName = referencedSubtree.getNodeName();

    //now need to change map so all xpaths are rooted at this new value
    newData = replaceXPathRootWith(newData, "/" + rootNodeName);

    DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
    Document doc = impl.createDocument("", rootNodeName, null);

    Node subtreeRoot = doc.getDocumentElement();

    try {
      XMLUtilities.getXPathMapAsDOMTree(newData, subtreeRoot);

    } catch (TransformerException w) {
      Log.debug(15, "TransformerException (" + w + ") calling "
                +
                "XMLUtilities.getXPathMapAsDOMTree(map, subtreeRoot) with \n"
                + "newData = " + newData
                + " and subtreeRoot = " + subtreeRoot);
      w.printStackTrace();
      Log.debug(5, "ERROR: Unable to update original reference data!");
      return null;
    }
    Node check = null;

    Log.debug(45,
              "updateOriginalReferenceSubtree() adding subtree to package...");
    Log.debug(45,"subtreeRoot=" + XMLUtilities.getDOMTreeAsString(subtreeRoot));

    check = adp.replaceSubtreeAtReference(subtreeID, subtreeRoot);

    if (check == null) {

      Log.debug(15, "updateOriginalReferenceSubtree(): NULL returned from "
                + "ADP.replaceSubtreeAtReference(); subtreeID="+subtreeID
                +";\nsubtreeRoot="+subtreeRoot);
      Log.debug(5, "** ERROR: Unable to update original ref **\n");
    }
    return referencedSubtree;
  }


  /**
   * deletes referenced subtree and transfers its children to te first subtree
   * that references it (if any)
   *
   * @param adp CustomList
   * @param subtreeID String
   * @return Node pointer to the new subtree, or null if unsuccessful
   */
  public static Node deleteOriginalReferenceSubtree(AbstractDataPackage adp,
                                             String subtreeID) {

    Log.debug(45, "deleteOriginalReferenceSubtree() Got subtreeID="+subtreeID);
    if (subtreeID == null || subtreeID.trim().length()<1) {
      Log.debug(15, "\n** ERROR - subtreeID is NULL/blank!");
      return null;
    }
    if (adp == null) {
      Log.debug(15, "\n** ERROR - AbstractDataPackage map is NULL!");
      return null;
    }


    //get a list of nodes that reference this subtree (refer-ers)
    List referencers = adp.getSubtreesThatReference(subtreeID);

    if (referencers == null || referencers.size() < 1) {
      Log.debug(15,
        "deleteOriginalReferenceSubtree() found no subtrees that reference id: "
        +subtreeID);
      return null;
    }

    //get the subtree to be deleted, at this refID (the refer-ee)
    Node deletedSubtree = adp.getSubtreeAtReferenceNoClone(subtreeID);
    if (deletedSubtree == null) {
      Log.debug(15,"adp.getSubtreeAtReference(" + subtreeID + ") returned null");
      Log.debug(5, "ERROR: cannot delete!");
      return null;
    }

    //and use (subtree to be deleted) to replace the subtree at the first
    //refer-er
    Node firstReferer = null;
    int idx = 0;
    //ensure we get a non-null entry
    while (firstReferer == null && idx < referencers.size()) {
      firstReferer = (Node)referencers.get(idx++);
    }

    Node[] firstRefererKids
        = XMLUtilities.getNodeListAsNodeArray(firstReferer.getChildNodes());

    //remove first referer children
    for (int indx = 0; indx < firstRefererKids.length; indx++) {
      firstReferer.removeChild(firstRefererKids[indx]);
    }

    Node[] deletedKids
        = XMLUtilities.getNodeListAsNodeArray(deletedSubtree.getChildNodes());

    //add (subtree to be deleted) children:
    for (int index = 0; index < deletedKids.length; index++) {
      firstReferer.appendChild(deletedKids[index]);
    }

    ((Element)deletedSubtree).setAttribute("id", "");
    ((Element)firstReferer).setAttribute("id", subtreeID);

    deletedSubtree.getParentNode().removeChild(deletedSubtree);

    return firstReferer;
  }





  private StringBuffer surrogateBuff = new StringBuffer();
  //
  private String getSurrogate(Node subtreeRoot) {

    if (surrogateXPaths==null || subtreeRoot==null) return "";
    surrogateBuff.delete(0, surrogateBuff.length());
    String baseXPath = "/" + subtreeRoot.getNodeName();
    Log.debug(45, "ReferencesHandler.getSurrogate() - baseXPath = "+baseXPath);

    Node textNode = null;

    for (int i=0; i < surrogateXPaths.length; i++) {

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


  private static OrderedMap returnMap = new OrderedMap();
  //
  // newRoot MUST start with a slash, and MUST NOT end with a slash!
  private static OrderedMap replaceXPathRootWith(OrderedMap map, String newRoot) {

    boolean firstLoop = true;
    String xpathRoot = null;
    returnMap.clear();

    for (Iterator it = map.keySet().iterator(); it.hasNext();) {

      String xpath = (String)it.next();
      Object value = map.get(xpath);

      //remove leading slash(es)
      while (xpath.startsWith("/")) xpath = xpath.substring(1);

      //find next slash
      int firstSlashIdx = xpath.indexOf("/");
      if (firstSlashIdx < 0) firstSlashIdx = xpath.length();

      if (firstLoop) {
        //get first element in xpath -> assume it's the root
        xpathRoot = xpath.substring(0, firstSlashIdx);
        Log.debug(45, "\nReferencesHandler.replaceXPathRootWith(); xpathRoot="
                  +xpathRoot);
        firstLoop = false;
      }

      xpath = newRoot + xpath.substring(firstSlashIdx);
      Log.debug(45, "\nReferencesHandler.replaceXPathRootWith(); adding xpath "
                +"to map: "+xpath);

      returnMap.put(xpath, value);
    }
    return returnMap;
  }

}



////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////


