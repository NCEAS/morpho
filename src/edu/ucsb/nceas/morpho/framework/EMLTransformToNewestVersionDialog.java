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

import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
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
	  private int userChoice = -1;
	
	/**
	 * Constructor of this dialog
	 * @param frame  parent of this dialog
	 */
	public EMLTransformToNewestVersionDialog(MorphoFrame frame)
	{
		morphoFrame = frame;
		initializeUI(frame);
		//Only when user choose "Yes", the document will be transformed
		transfromEMLToNewestVersion();
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
			if (dataPackage != null)
			{
				//TODO transform the datapakcage to the newest version
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
