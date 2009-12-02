package edu.ucsb.nceas.morpho.plugins;

import org.w3c.dom.Node;

import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.util.Log;

/**
 * Listener class for New Package Wizard
 * @author tao
 *
 */
public class EntityWizardListener implements  DataPackageWizardListener
{
  private AbstractDataPackage adp = null;
  private int nextEntityIndex = 0;
  private MorphoFrame oldMorphoFrame = null;
  
  public EntityWizardListener(AbstractDataPackage adp, int nextEntityIndex, MorphoFrame oldMorphoFrame)
  {
    this.adp = adp;
    this.nextEntityIndex = nextEntityIndex;
    this.oldMorphoFrame = oldMorphoFrame;
  }
  
  public void wizardComplete(Node newDOM, String autoSavedID) 
  {

    if(newDOM != null) 
    {

      Log.debug(30,"Entity Wizard complete - creating Entity object..");
      Log.debug(35, "Add/replace entity in incompleteDocumentloader.TableWizardListener with entity index "+nextEntityIndex);
      adp.replaceEntity(newDOM, nextEntityIndex);//we use replace method here because the auto-save file already adding the entity into datapackage.
      adp.setLocation("");  // we've changed it and not yet saved

    }
    MorphoFrame frame = NewPackageWizardListener.openMorphoFrameForDataPackage(adp);
    if(frame != null && oldMorphoFrame != null)
    {
      oldMorphoFrame.setVisible(false);
      UIController controller = UIController.getInstance();
      controller.removeWindow(oldMorphoFrame);
      oldMorphoFrame.dispose();
    }

  }

   public void wizardCanceled() 
   {

      Log.debug(45, "\n\n********** Wizard canceled!");
   }
   
   /**
    * Methods inherits from DataPackageWizardListener.
    */
   public void wizardSavedForLater()
   {
     if(oldMorphoFrame != null)
     {
       oldMorphoFrame.setVisible(false);
       UIController controller = UIController.getInstance();
       controller.removeWindow(oldMorphoFrame);
       oldMorphoFrame.dispose();
     }
     Log.debug(45, "\n\n********** Wizard saved for later!");
   }
    
} 

