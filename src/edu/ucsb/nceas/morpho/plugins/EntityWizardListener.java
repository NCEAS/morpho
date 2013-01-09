package edu.ucsb.nceas.morpho.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;

import org.apache.commons.beanutils.PropertyUtils;
import org.dataone.service.types.v1.Checksum;
import org.dataone.service.types.v1.Identifier;
import org.dataone.service.types.v1.ObjectFormatIdentifier;
import org.dataone.service.types.v1.util.ChecksumUtil;
import org.w3c.dom.Node;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.Entity;
import edu.ucsb.nceas.morpho.datapackage.MorphoDataPackage;
import edu.ucsb.nceas.morpho.datastore.DataStoreServiceController;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
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
  private MorphoDataPackage mdp = null;
  private int nextEntityIndex = 0;
  private MorphoFrame oldMorphoFrame = null;
  
  public EntityWizardListener(MorphoDataPackage mdp, int nextEntityIndex, MorphoFrame oldMorphoFrame)
  {
    this.mdp = mdp;
    this.nextEntityIndex = nextEntityIndex;
    this.oldMorphoFrame = oldMorphoFrame;
  }
  
  public void wizardComplete(Node newDOM, String autoSavedID) 
  {

    if(newDOM != null) 
    {
    	AbstractDataPackage adp = mdp.getAbstractDataPackage();
      Log.debug(30,"Entity Wizard complete - creating Entity object..");
      Log.debug(35, "Add/replace entity in incompleteDocumentloader.TableWizardListener with entity index "+nextEntityIndex);
      adp.replaceEntity(newDOM, nextEntityIndex);//we use replace method here because the auto-save file already adding the entity into datapackage.
      adp.setLocation("");  // we've changed it and not yet saved
      try {
        initializeSystemMetadata(nextEntityIndex, adp);
      } catch (Exception e) {
        Log.debug(20, "Can't generate system metadata for the entity with the index "+nextEntityIndex);
      }

    }
    MorphoFrame frame = NewPackageWizardListener.openMorphoFrameForDataPackage(mdp);
    if(frame != null && oldMorphoFrame != null)
    {
      oldMorphoFrame.setVisible(false);
      UIController controller = UIController.getInstance();
      controller.removeWindow(oldMorphoFrame);
      oldMorphoFrame.dispose();
    }

  }
  
  private void initializeSystemMetadata(int entityIndex, AbstractDataPackage adp) throws Exception {
    Entity entity = adp.getEntity(entityIndex);
    if(entity != null) {
      PropertyUtils.copyProperties(entity.getSystemMetadata(), adp.getSystemMetadata());
      entity.getSystemMetadata().setObsoletedBy(null);
      entity.getSystemMetadata().setObsoletes(null);
      String URLinfo = adp.getDistributionUrl(entityIndex, 0, 0);
      String dataId = AbstractDataPackage.getUrlInfo(URLinfo);
      Identifier identifier = new Identifier();
      identifier.setValue(dataId);
      entity.getSystemMetadata().setIdentifier(identifier);
      ObjectFormatIdentifier dataFormatId = new ObjectFormatIdentifier();
      dataFormatId.setValue(adp.getPhysicalFormat(entityIndex, 0));
      entity.getSystemMetadata().setFormatId(dataFormatId);
      //File dataFile = DataStoreServiceController.getInstance().openFile(identifier.getValue(), DataPackageInterface.LOCAL);
      //Here the data file hasn't been saved. So we have to open it from temp
      File dataFile = Morpho.thisStaticInstance.getLocalDataStoreService().openTempFile(identifier.getValue());
      Checksum dataChecksum = ChecksumUtil.checksum(new FileInputStream(dataFile), entity.getSystemMetadata().getChecksum().getAlgorithm());
      entity.getSystemMetadata().setChecksum(dataChecksum);
      entity.getSystemMetadata().setSize(BigInteger.valueOf(dataFile.length()));
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

