/**
 *  '$RCSfile: DataPackageWizardListener.java,v $'
 *    Purpose: A class that handles xml messages passed by the 
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2003-09-26 20:50:11 $'
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

package edu.ucsb.nceas.morpho.plugins;

import org.w3c.dom.Node;


/**
 *  Interface for Data Package Wizard Plugin
 */

public interface DataPackageWizardListener {


  /**
   *  callback method when wizard has finished, upon completion
   *
   *  @param newDOM the root Node of the newly-created DOM document
   */
  public void wizardComplete(Node newDOM);

  /**
   *  callback method when wizard has been canceled before completion
   *
   *  @param newDOM the root Node of the newly-created DOM document
   */
  public void wizardCanceled();
}
