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

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.Log;

class AccessTreeNodeObject
    implements Comparable {

  private String DNinfo = null;
  private String name = null;
  private String organization = null;
  private String email = null;
  private String description = null;
  private String EMPTY_STRING = "";

  int nodeType = 0;

  public AccessTreeNodeObject(int nodeType) {
    DNinfo = "";
    this.nodeType = nodeType;
  }

  public AccessTreeNodeObject(String DNinfo, int nodeType) {
    setDN(DNinfo);
    this.nodeType = nodeType;
  }

  public String getDN() {
    return DNinfo;
  }

  public void setDN(String DNinfo) {
    if (nodeType == WizardSettings.ACCESS_PAGE_AUTHSYS) {
      try {
        DNinfo = DNinfo.substring(DNinfo.indexOf("o="));
      }
      catch (Exception e) {
        Log.debug(10, e.getMessage());
      }
    }
    this.DNinfo = DNinfo;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getEmail() {
    return email;
  }

  public String getOrganization() {
    return organization;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setOrganization(String organization) {
    this.organization = organization;
  }

  public String toString() {
    String value = null;
    String key = null;

    if (nodeType == WizardSettings.ACCESS_PAGE_AUTHSYS) {
      if (organization == null) {
        key = "o=";
        value = DNinfo.substring(DNinfo.indexOf(key) + key.length());
        value = value.substring(0, value.indexOf(","));
      } else {
        value = organization;
      }
    } else if (nodeType == WizardSettings.ACCESS_PAGE_GROUP) {
      key = "cn=";
      value = DNinfo.substring(DNinfo.indexOf(key) + key.length());
      value = value.substring(0, value.indexOf(","));
    } else if (nodeType == WizardSettings.ACCESS_PAGE_USER) {
      key = "uid=";
      value = DNinfo.substring(DNinfo.indexOf(key) + key.length());
      value = value.substring(0, value.indexOf(","));

      if (name != null && name.compareTo("") != 0) {
        value = name + " (" + value + ")";
      }
    }

    return value;
  }

  public int compareTo(Object o) {
    String thisString = (this.toString()).toLowerCase();
    String otherString = ( ( (AccessTreeNodeObject) o).toString()).toLowerCase();
    return thisString.compareTo(otherString);
  }
}
