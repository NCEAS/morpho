package edu.ucsb.nceas.morpho.datapackage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.io.Reader;
import java.io.StringReader;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.AccessionNumber;
import edu.ucsb.nceas.morpho.datapackage.DataPackageFactory;
import edu.ucsb.nceas.morpho.datapackage.DataViewContainerPanel;
import edu.ucsb.nceas.morpho.datapackage.EML200DataPackage;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardListener;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.util.Log;


/**
 * This class represents a dialog which will ask if user want to upgrade the EML datapckage
 * of a morpho frame to the newest EML version. There is tow buttons - "Yes" and "No"
 * We strongly recommend user to answer yes. If the answer is yes, upgrade will happen. 
 * Otherwise, nothing will happen. 
 * @author tao
 *
 */
public class EMLTransformToNewestVersionDialog 
{
	//Dialog will base on this frame.
	MorphoFrame morphoFrame = null;
	
	private static final int PADDINGWIDTH = 8;
	private static final int NORTHPADDINGWIDTH = 8;
	private static final String TITLE = "Upgrade EML Document";
	private static final String WARNING = "This data package uses an older version of EML.\n"+
	                                                 "You will not be able to edit it without upgrading to the newest version ("
	                                                 +EML200DataPackage.LATEST_EML_VER+")"+
	                                                ".\nDo you want to upgrade the data package now?";
	 /* Control button */
	  private JButton executeButton = null;
	  private JButton cancelButton = null;
	  
	  /* the user's choice of */
	  private int userChoice = -2;
	  
	  /* the eml2 package embedded in the morpho frame*/
	  private EML200DataPackage eml200Package = null;
	  
	  /* if user want to use correction wizard to correct errors */
	  //private boolean useCorrectionWizard = true;
	  
	  private String USECORRECTIONWIZARD =  "useCorrectionWizard";
	  
	  private DataPackageWizardListener listener = null;
	

	
	/**
	 * Constructor of this dialog
	 * @param frame  parent of this dialog
	 * @param listener  the listener for completing correction wizard
	 */
	public EMLTransformToNewestVersionDialog(MorphoFrame frame, DataPackageWizardListener listener) throws Exception
	{
		this.morphoFrame = frame;
		this.listener = listener;
		AbstractDataPackage dataPackage = morphoFrame.getAbstractDataPackage();
		if (dataPackage != null && dataPackage instanceof EML200DataPackage)
		{
			eml200Package = (EML200DataPackage)dataPackage;
			if(eml200Package != null && !eml200Package.isLatestEMLVersion())
			{
				initializeUI(frame);
				//Only when user choose "Yes", the document will be transformed
				transfromEMLToNewestVersion();
			}
			else if(eml200Package != null)
			{
				 //calling the wizardComplete method in listener to make the dialog show up.
                if(listener != null)
                {
                	listener.wizardComplete(eml200Package.getMetadataNode(), null);
                }
			}
			
		}
	}
	
	/*
	 * initialize UI
	 */
	private void initializeUI(Window parent)
	{
	   
 	   Object[] choices = {"Yes", "No"};
 	   userChoice= JOptionPane.showOptionDialog(parent, //parent
                WARNING, // Message
                TITLE, //title
                JOptionPane.YES_NO_OPTION, //optionType
                JOptionPane.PLAIN_MESSAGE,//messageTye
                null ,
                choices,
                null);
 	
 		   
 		
	}
	
	
	/*
	 * transform the data package from old version to the newest version
	 */
	private void transfromEMLToNewestVersion() throws Exception
	{
		if (userChoice == JOptionPane.YES_OPTION && morphoFrame != null)
		{
			AbstractDataPackage dataPackage = morphoFrame.getAbstractDataPackage();
			if (eml200Package != null)
			{
				//TODO transform the datapakcage to the newest version
				String id = eml200Package.getAccessionNumber();
				String newString = null;
				boolean hasError = false;
				try
				{
				    newString = eml200Package.transformToLastestEML();
				}
				catch(EMLVersionTransformationException e)
				{
					hasError = true;
					newString = e.getNewEMLOutput();//this part of exception is eml output.
				}
				
				if (newString != null)
				{
					try
		            {
						Vector errorPathList = null;
						eml200Package= (EML200DataPackage)DataPackageFactory.getDataPackage(
			                      new java.io.StringReader(newString), false, true);
		                eml200Package.setEMLVersion(EML200DataPackage.LATEST_EML_VER);
		                Morpho morpho = Morpho.thisStaticInstance;
		                //AccessionNumber an = new AccessionNumber(morpho);
		                //String newid = an.incRev(id);
		                //eml200Package.setAccessionNumber(newid);
		                eml200Package.setLocation("");//not save it yet
		                
		                 if(hasError)
		                 {
		                	//it may be invalid document since our eml201to210 transform
		                	//style sheet gives warning to user no matter is really invalid or not.
		                	// so we use EML210Validate to get error list vector
		                	 Reader xml = new StringReader(newString);
		                	 EML210Validate validate = new EML210Validate();
		        	  	     validate.parse(xml);
		        	  	     xml.close();
		        	  	     errorPathList = validate.getInvalidPathList();
		                }
		                 
		                if (errorPathList == null || errorPathList.isEmpty())
		                {
		                	Log.debug(40, "In there is no errors path list branch or not useCorrectionWizard");
		                    //this is a valid new version eml document (or uses don't like to use correctionwizard. Display it and depose old frame
		                    DataPackagePlugin plugin = new DataPackagePlugin(morpho);
		                    plugin.openNewDataPackage(eml200Package,null);
		                    morphoFrame.setVisible(false);                
			                UIController controller = UIController.getInstance();
			                controller.removeWindow(morphoFrame);
			                morphoFrame.dispose();	 
			                //calling the wizardComplete method in listener
			                if(listener != null)
			                {
			                	listener.wizardComplete(eml200Package.getMetadataNode(), null);
			                }
		                }
		                else
		                {
		                	//it is invalid document. We should start correction wizard to start it.
		                	
		                	 try 
		                	 {
		                		Log.debug(40, "In there are errors path list branch");
		 			            ServiceController services = ServiceController.getInstance();
		 			            ServiceProvider provider =
		 			                services.getServiceProvider(DataPackageWizardInterface.class);
		 			            DataPackageWizardInterface wizard = (DataPackageWizardInterface)provider;
		 			            //we pass the listener to correction wizard and let it handle the listener.
		 			            wizard.startCorrectionWizard(eml200Package, errorPathList, morphoFrame, listener);
		 		
		 			         } 
		                	 catch (ServiceNotHandledException snhe) 
		 			         {		 		
		 			               Log.debug(20, snhe.getMessage());
		 			               throw snhe;
		 			          }		                	
		                }
		            }
	                catch (Exception snhe)
	                {
	                  Log.debug(20, snhe.getMessage());
	                  throw new Exception("Couldn't transform it since "+snhe.getMessage());
	                }	                
	                
				}
				else
				{
					Log.debug(20, "Couldn't tranform the eml document to the newest version");
				    throw new Exception("Couldn't tranform the eml document to the newest version");
				}
			}
			else
			{
				Log.debug(20, "Couldn't transform it since the this morpho frame doesn't contain a package");
				throw new Exception("Couldn't transform it since the this morpho frame doesn't contain a package");
			}
		}
	}
	
	
	
	/**
	 * Gets the user choice from the optional panel.
	 * @return
	 */
	public int getUserChoice()
	{
		return userChoice;
	}
     
     
}
