/**
 *  '$RCSfile: CustomPickList.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Saurabh Garg
 *    Release: @release@
 *
 *   '$Author: sgarg $'
 *     '$Date: 2003-12-12 03:05:35 $'
 * '$Revision: 1.2 $'
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

package edu.ucsb.nceas.morpho.plugins.datapackagewizard;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.PartyPage;

import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.morpho.util.Log;

import java.util.EventObject;
import java.util.Vector;
import java.util.List;
import java.util.Iterator;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.Box;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.DefaultCellEditor;
import javax.swing.ListSelectionModel;
import javax.swing.ListModel;
import javax.swing.border.EmptyBorder;

public class CustomPickList extends JPanel {

  private JButton addButton;
  private JComboBox partiesPickList;
  protected static AddListAction addAction;
  private Action customAddAction;

  /**
   *  constructor - creates a multi-column list based on the parameters passed:
   *
   *
   */
  public CustomPickList() {
    init();
  }

  private void init() {
    this.setLayout(new BorderLayout());

    JLabel desc = WidgetFactory.makeHTMLLabel(
    "You can also select from the enteries that you have made earlier. You "
    +"can choose from the list below and click the button to edit the earlier entry", 2);
    this.add(desc, BorderLayout.NORTH);

    partiesPickList = new JComboBox();
    for (int count=0; count < WidgetFactory.responsiblePartyList.size(); count++){
      List rowList = (List)WidgetFactory.responsiblePartyList.get(count);
      String name = (String)rowList.get(0);
      String role = (String)rowList.get(1);
      String row  = name + ", " + role;
      partiesPickList.addItem(row);
    }
//    partiesPickList.setBorder(new EmptyBorder(0,WizardSettings.COMPONENT_PADDING,
  //                                      0, 10 * WizardSettings.COMPONENT_PADDING));

    this.add(partiesPickList, BorderLayout.CENTER);

    addAction = new AddListAction(this);
    addButton      = new JButton(addAction);
    addButton.setFont(WizardSettings.WIZARD_CONTENT_FONT);
    addButton.setLabel("Modify this entry and add");
    this.add(addButton, BorderLayout.EAST);

  }


  /**
   *  returns the index of the currently-selected row, or -1 if none selected
   *
   *  @return the index of the currently-selected row, or -1 if none selected
   */
  public List getSelectedRowList() {
    int index = partiesPickList.getSelectedIndex();
    if (index >= 0) {
      return (List) WidgetFactory.responsiblePartyList.get(index);
    } else {
      return null;
    }
  }

  public void getList(){
    this.removeAllRows();
    for (int count=0; count < WidgetFactory.responsiblePartyList.size(); count++){
      List rowList = (List)WidgetFactory.responsiblePartyList.get(count);
      String name = (String)rowList.get(0);
      String role = (String)rowList.get(1);
      String row  = name +  ", " + role;
      partiesPickList.addItem(row);
     }
  }
  /**
   *  adds the row to the list after the currently-selected row, or at the end
   *  if no row is selected. Then scrolls to make the new row visible.
   *
   */
  public void addRow(List rowList) {
    for (int count=0; count < WidgetFactory.responsiblePartyList.size(); count++){
      List tempList = (List)WidgetFactory.responsiblePartyList.get(count);
      if(tempList.get(3) == rowList.get(3)){
        WidgetFactory.responsiblePartyList.remove(count);
      }
    }
    WidgetFactory.responsiblePartyList.add(rowList);
    this.getList();
  }

  /**
   *  removes all the rows from the list
   *
   */
  public void removeAllRows() {
    partiesPickList.removeAllItems();
  }

  /**
    *  Gets the <code>javax.swing.Action</code> to be executed on pressing the
    *  ADD button. NOTE that if no custom add action is set, or if a null action
    *  is set, the ADD button's 'private' Action (defined elsewhere in this
    *  class) will be executed; otherwise the custom action will be executed (and
    *  the 'private' Action will NOT be executed).
    *  <em>Note that this behavior is different for the other custom action
    *  get/set methods, which are executed IN ADDITION to private actions</em>
    *
    *
    *  @return the <code>javax.swing.Action</code> to be executed
    */
   public Action getCustomAddAction() {

     return this.customAddAction;
   }

   /**
    *  Sets the <code>javax.swing.Action</code> to be executed on pressing the
    *  ADD button. NOTE that if no custom add action is set, or if a null action
    *  is set, the ADD button's 'private' Action (defined elsewhere in this
    *  class) will be executed; otherwise the custom action will be executed (and
    *  the 'private' Action will NOT be executed).
    *  <em>Note that this behavior is different for the other custom action
    *  get/set methods, which are executed IN ADDITION to private actions</em>
    *
    *  @param the <code>javax.swing.Action</code> to be executed
    */
   public void setCustomAddAction(Action a) {

     this.customAddAction = a;
   }
}



class AddListAction extends AbstractAction {

  private CustomPickList parentList;

  public AddListAction(CustomPickList partiesPickList) {
    super("Add");
    this.parentList = partiesPickList;
  }


  public void actionPerformed(ActionEvent e) {

    Log.debug(45, "CustomPickList Add action");
    // get object here - only a String surrogate is shown by the cell renderer,
    // but the entire object is actually in the table model!

    //execute the user's custom action:
    if (parentList.getCustomAddAction()!=null) {
      parentList.getCustomAddAction().actionPerformed(null);
    }
  }
}

