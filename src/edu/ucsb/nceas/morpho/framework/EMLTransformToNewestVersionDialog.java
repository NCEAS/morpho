package edu.ucsb.nceas.morpho.framework;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;

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
	private static final String WARNING = "EML version of the package is not the newest version.\n"+
	                                                 "You are strongly recommended to upgrade to the newest version "
	                                                 +EML200DataPackage.LATEST_EML_VER+
	                                                ".\nDo you want to upgrade the data package?";
	 /* Control button */
	  private JButton executeButton = null;
	  private JButton cancelButton = null;
	  
	  /* the user's choice of */
	  private int userChoice = -2;
	  
	  /* the eml2 package embedded in the morpho frame*/
	  private EML200DataPackage eml200Package = null;
	
	/**
	 * Constructor of this dialog
	 * @param frame  parent of this dialog
	 */
	public EMLTransformToNewestVersionDialog(MorphoFrame frame)
	{
		morphoFrame = frame;
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
	private void transfromEMLToNewestVersion()
	{
		if (userChoice == JOptionPane.YES_OPTION && morphoFrame != null)
		{
			AbstractDataPackage dataPackage = morphoFrame.getAbstractDataPackage();
			if (eml200Package != null)
			{
				//TODO transform the datapakcage to the newest version
				String id = eml200Package.getAccessionNumber();
				String newString = eml200Package.transformToLastestEML();
				if (newString != null)
				{
					eml200Package= (EML200DataPackage)DataPackageFactory.getDataPackage(
		                      new java.io.StringReader(newString), false, true);
	                eml200Package.setEMLVersion(EML200DataPackage.LATEST_EML_VER);
	                Morpho morpho = Morpho.thisStaticInstance;
	                //AccessionNumber an = new AccessionNumber(morpho);
	                //String newid = an.incRev(id);
	                //eml200Package.setAccessionNumber(newid);
	                eml200Package.setLocation("");//not save it yet
	                UIController.showNewPackage(eml200Package);
				}
				else
				{
					Log.debug(20, "Couldn't tranform the eml document to the newest version");
				}
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
