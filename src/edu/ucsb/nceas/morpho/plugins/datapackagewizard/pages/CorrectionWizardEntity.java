package edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.OrderedMap;

/**
 * This class reprents a entity page without attribute list. It is dedicated to correction wizard.
 * @author tao
 *
 */
public class CorrectionWizardEntity extends Entity
{
	 /**
	    * Display the entity page without attribute list
	    * @param disableAttributeList
	    */
	   public CorrectionWizardEntity()
	   {
		  super(true);
	   }
}
