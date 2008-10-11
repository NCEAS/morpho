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
	 /** Control button */
	  private JButton executeButton = null;
	  private JButton cancelButton = null;
	
	/**
	 * Constructor of this dialog
	 * @param frame  parent of this dialog
	 */
	public EMLTransformToNewestVersionDialog(MorphoFrame frame)
	{
		morphoFrame = frame;
		initializeUI(frame);
	}
	
	/*
	 * initialize UI
	 */
	private void initializeUI(Window parent)
	{
		
		// Save button
	    executeButton = new JButton("Yes");	    
	    //Cancel button
	    cancelButton = new JButton("No");
	    SymAction lSymAction = new SymAction();
		executeButton.addActionListener(lSymAction);
		cancelButton.addActionListener(lSymAction);
		//Creates JOptionPanel to contain the radio buttons
 	   Object[] choices = {"Yes", "No"};
 	   int choice = JOptionPane.showOptionDialog(parent, //parent
                WARNING, // Message
                TITLE, //title
                JOptionPane.YES_NO_OPTION, //optionType
                JOptionPane.PLAIN_MESSAGE,//messageTye
                null ,
                choices,
                null);
 	   if (choice == JOptionPane.YES_OPTION)
 	   {
 		   //System.out.println("==============yes!");
 	   }
 	   else if (choice == JOptionPane.NO_OPTION)
 	   {
 		   //System.out.println("==============no!");
 	   }
 		   
 		
	}
	
	/*
	 * initialize UI
	 */
	/*private void initializeUI(Window parent)
	{
		setModal(true);
	     // Set OpenDialog size depend on parent size
	    int parentWidth = parent.getWidth();
	    int parentHeight = parent.getHeight();
	    int dialogWidth = 400;
	    int dialogHeight = 400;
	    setSize(dialogWidth, dialogHeight);
	    setResizable(false);

	    // Set location of dialog, it shared same center of parent
	    double parentX = parent.getLocation().getX();
	    double parentY = parent.getLocation().getY();
	    double centerX = parentX + 0.5 * parentWidth;
	    double centerY = parentY + 0.5 * parentHeight;
	    int dialogX = (new Double(centerX - 0.5 * dialogWidth)).intValue();
	    int dialogY = (new Double(centerY - 0.5 * dialogHeight)).intValue();
	    setLocation(dialogX, dialogY);

	    setTitle("Upgrade EML Document");
	    // Set the default close operation is dispose
	    setDefaultCloseOperation(DISPOSE_ON_CLOSE);

	    // Set the border layout as layout
	    getContentPane().setLayout(new BorderLayout(0, 0));
	     // Add padding for left and right
	    getContentPane().add(BorderLayout.EAST,
	                                      Box.createHorizontalStrut(PADDINGWIDTH));
	    getContentPane().add(BorderLayout.WEST,
	                                      Box.createHorizontalStrut(PADDINGWIDTH));
	    getContentPane().add(BorderLayout.NORTH,
                Box.createHorizontalStrut(NORTHPADDINGWIDTH));



	    // Create JPanel and set it border layout
	    JPanel mainPanel = new JPanel();
	    mainPanel.setLayout(new BorderLayout(0, 0));

	    // Create note box and add it to the north of mainPanel
	    Box noteBox = Box.createVerticalBox();
	    noteBox.add(Box.createVerticalStrut(PADDINGWIDTH));
	    JLabel note = WidgetFactory.makeHTMLLabel(WARNING, 3);
	   
	    //JLabel note = new JLabel();
	    
		//note.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		//note.setText(WARNING);
		note.setFont(new Font("Label", Font.PLAIN, 14));
		//loadingLabel.setForeground(java.awt.Color.red);
		
	    
	    noteBox.add(note);
	    noteBox.add(Box.createVerticalStrut(PADDINGWIDTH));
	    mainPanel.add(BorderLayout.CENTER, noteBox);

	    // Finish mainPanel and add it the certer of contentpanel
	    getContentPane().add(BorderLayout.CENTER, mainPanel);

	    // Create bottom box
	    Box bottomBox = Box.createVerticalBox();
	    getContentPane().add(BorderLayout.SOUTH, bottomBox);
	    //Create padding between result panel and Contorl button box
	    bottomBox.add(Box.createVerticalStrut(PADDINGWIDTH));
	    // Create a controlbuttionBox
	    Box controlButtonsBox = Box.createHorizontalBox();
	    controlButtonsBox.add(Box.createHorizontalGlue());

	    // Save button
	    executeButton = new JButton("Yes");
	    controlButtonsBox.add(executeButton);
	    controlButtonsBox.add(Box.createHorizontalStrut(PADDINGWIDTH));

	    //Cancel button
	    cancelButton = new JButton("No");
	    controlButtonsBox.add(cancelButton);
	    controlButtonsBox.add(Box.createHorizontalStrut(PADDINGWIDTH));

	    // Add controlButtonsBox to bottomBox
	    bottomBox.add(controlButtonsBox);
	    // Add the margin between controlButtonPanel to the bottom line
	    bottomBox.add(Box.createVerticalStrut(10));

			SymAction lSymAction = new SymAction();
			executeButton.addActionListener(lSymAction);
			cancelButton.addActionListener(lSymAction);


	    setVisible(true);

	}*/
	
	/*
	 * Class to handle the action of buttions
	 */
	class SymAction implements java.awt.event.ActionListener
	{
		/*
		 * Performs action 
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			Object object = event.getSource();
			if (object == executeButton) 
			{
				executeButton_actionPerformed(event);
             }
             else if (object == cancelButton) 
             {
                cancelButton_actionPerformed(event);
             }
		  }
	}
    
	/*
	 * cancel the action
	 */
	 private void cancelButton_actionPerformed(java.awt.event.ActionEvent event)
	{
		 //TODO: add code to handle export
		//this.setVisible(false);
		//this.dispose();
	}
     
	 /*
	  * transform to the newest eml version
	  */
     private void executeButton_actionPerformed(java.awt.event.ActionEvent event)
	{
    	 //this.setVisible(false);
 		 //this.dispose();
	}
     
     
}
