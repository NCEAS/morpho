/**
 *  '$RCSfile: ReferencesHandler.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2004-04-05 07:06:52 $'
 * '$Revision: 1.10 $'
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

import java.awt.Frame;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import org.w3c.dom.Node;

public class ReferencesHandler {


  private final String DEFAULT_DROPDOWN_ITEM = " ";

  private final String EXT_DIALOG_DROPDOWN_ITEM = "Select from a different data package";


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
   * @param suppressRefID String
   * @return a List of ReferenceMapping objects containing all the IDs
   *   currently in the DataPackage that point to subtree root-nodes
   *   corresponding to the genericName used to instantiate this
   *   ReferencesHandler, and String surrogates for those referenced subtrees.
   *   An empty List is returned if the passed dataPkg parameter is null
   */
  private List getReferences(AbstractDataPackage dataPkg, String suppressRefID) {

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
    //doQueryAndPopulateDialog();

    //...and get corresponding node from external datapackage

    OrderedMap map = XMLUtilities.getDOMTreeAsXPathMap(returnNode);

    return event;
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

}


////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////


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
