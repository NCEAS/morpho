/**
 *  '$RCSfile: AccessTreeNodeObject.java,v $'
 *    Purpose: A class that creates node object for tree in AccessPage.java
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Saurabh Garg
 *    Release: @release@
 *
 *   '$Author: sgarg $'
 *     '$Date: 2005-09-07 17:18:09 $'
 * '$Revision: 1.5 $'
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

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.dataone.service.types.v1.Group;
import org.dataone.service.types.v1.Person;
import org.dataone.service.types.v1.Subject;

/**
 * This class overwrite the toString method of the DefaultMutableTreeNode class.
 * The tree node display the persons' and the groups' name
 * @author tao
 *
 */
public class AccessTreeNode extends DefaultMutableTreeNode {

  /**
   * Overwrite the toString method of the super class.
   */
  public String toString() {
    String str = null;
    if(getUserObject() != null && getUserObject() instanceof Person ) {
      Person person = (Person) getUserObject();
      str = AccessPage.getPersonName(person);
    } else if (getUserObject() != null && getUserObject() instanceof Group) {
      Group group = (Group) getUserObject();
      str =  group.getGroupName();
    } else if (getUserObject() != null && getUserObject() instanceof Subject) {
      Subject subject = (Subject) getUserObject();
      str =  subject.getValue();
    } else if (getUserObject() != null) {
      str = getUserObject().toString();
    }
    return str;
  }
}
