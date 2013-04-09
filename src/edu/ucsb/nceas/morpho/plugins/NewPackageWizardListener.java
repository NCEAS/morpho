/**
 *  '$RCSfile: DataPackageWizardInterface.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-05-02 21:48:38 $'
 * '$Revision: 1.28 $'
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

import org.dataone.service.types.v1.AccessPolicy;
import org.dataone.service.types.v1.ReplicationPolicy;
import org.w3c.dom.Node;

import edu.ucsb.nceas.morpho.dataone.AccessPolicyConverter;
import edu.ucsb.nceas.morpho.dataone.ReplicationPolicyComparator;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.AddAccessCommand;
import edu.ucsb.nceas.morpho.datapackage.DataPackageFactory;
import edu.ucsb.nceas.morpho.datapackage.MorphoDataPackage;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.XMLUtil;

/**
   * Listener class for New Package Wizard
   * @author tao
   *
   */
public class NewPackageWizardListener implements  DataPackageWizardListener
{
  
   private String accessionNumber = null;
   /**
    * Default constructor
    */
   public NewPackageWizardListener()
   {
     
   }
   
    /**
     * Methods inherits from DataPackageWizardListener
     */
    public void wizardComplete(Node newDOM, String autoSavedID) 
    {
    
      Log.debug(30,
          "Wizard complete - Will now create an AbstractDataPackage..");
  
      AbstractDataPackage adp = DataPackageFactory.getDataPackage(newDOM);
      accessionNumber = adp.getAccessionNumber();
      if(accessionNumber == null || accessionNumber.equals(""))
      {
        adp.setAccessionNumber(autoSavedID);
      }
      Log.debug(30, "AbstractDataPackage complete");
      adp.setAutoSavedID(autoSavedID);
      adp.setLocation(DataPackageInterface.TEMPLOCATION);
      adp.setIsNewGernated(true);
      
	try {
		// make sure to take out the access section from EML
		// see: http://bugzilla.ecoinformatics.org/show_bug.cgi?id=5829
		AccessPolicy accessPolicy= AccessPolicyConverter.getAccessPolicy(adp);
		adp.getSystemMetadata().setAccessPolicy(accessPolicy);
		adp.deleteSubtree(AddAccessCommand.DATAPACKAGE_ACCESS_GENERIC_NAME, 0);
	} catch (Exception e) {
		Log.debug(10, "Could not transfer access block to AccessPolicy: " + e.getMessage());
		e.printStackTrace();
	}
	
	try {
		// make sure to take out the access policy section from EML
		ReplicationPolicy replicationPolicy = ReplicationPolicyComparator.getReplicationPolicy(adp);
		adp.getSystemMetadata().setReplicationPolicy(replicationPolicy);
		adp.deleteSubtree("additionalMetadataReplicationPolicyParent", 0);
	} catch (Exception e) {
		Log.debug(10, "Could not transfer replication additional metadata block to ReplicationPolicy: " + e.getMessage());
		e.printStackTrace();
	}

      MorphoDataPackage mdp = new MorphoDataPackage();
      mdp.setAbstractDataPackage(adp);
      openMorphoFrameForDataPackage(mdp);
      Log.debug(45, "\n\n********** Wizard finished: DOM:");
      Log.debug(45, XMLUtil.getDOMTreeAsString(newDOM));
    }
    
    /**
     * Methods inherits from DataPackageWizardListener
     */
    public void wizardCanceled() 
    {
    
       Log.debug(45, "\n\n********** Wizard canceled!");
     }
    
    /**
     * Methods inherits from DataPackageWizardListener.
     */
    public void wizardSavedForLater()
    {
      Log.debug(45, "\n\n********** Wizard saved for later!");
    }
    
    /**
     * Open a morpho frame for given DataPacakge
     */
    public static  MorphoFrame openMorphoFrameForDataPackage(MorphoDataPackage mdp)
    {
       MorphoFrame frame = null;
       try 
       {
        ServiceController services = ServiceController.getInstance();
        ServiceProvider provider =
              services.getServiceProvider(DataPackageInterface.class);
        DataPackageInterface dataPackage = (DataPackageInterface)provider;
        frame = dataPackage.openNewDataPackage(mdp, null);
              
       } 
       catch (ServiceNotHandledException snhe) 
       {

         Log.debug(6, snhe.getMessage());
       }
       return frame;
    }
      
} 
