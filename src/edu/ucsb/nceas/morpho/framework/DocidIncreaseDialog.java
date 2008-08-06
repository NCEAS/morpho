package edu.ucsb.nceas.morpho.framework;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
/**
 * This dialog will pop up when docid confilction happens during save(synchronize) process.
 * User will give options to choose increase docid or just increase revision.
 * @author tao
 *
 */
public class DocidIncreaseDialog extends JDialog implements ActionListener
{
     
       public static final String INCEASEID = "increaseID";
       public static final String INCRASEREVISION = "increaseRevision";
       public static final String METACAT = "Metacat";
       public static final String LOCAL  = "local system";
       private String docid = null;
       private String location = null;
       private String userChoice = null;
       private static final String LABELINCEASEDOCID = "Generate new document id";
       private static final String LABELINCREASEREVISION = "Increment revsion number";
       private static final String TITLE = "Resolving Identifier Conflict";
       private String message = null;
       
       /**
        * Constructor
        */
       public DocidIncreaseDialog(String docid, String location)
       {
    	    //super(parent, true);
    	    this.docid = docid;
    	    this.location = location;
    	    this.userChoice = INCRASEREVISION;
    	    message =  "Document id "+docid +" already exists in "+location+
            ".  \nIf the saving document is an updated version of the document, increment the revision number. "+ 
            "\n Otherwise, generate a new document id. Then close this window.";
    	    intialGUI();
       }
       
       /*
        * Intializes the gui
        */
       private void intialGUI()
       {
    	   //Creates two radio button for options
    	   JRadioButton increaseRevision = new JRadioButton(LABELINCREASEREVISION);
    	   increaseRevision.setActionCommand(INCRASEREVISION);
    	   increaseRevision.setSelected(true);
    	   JRadioButton increaseDocid = new JRadioButton(LABELINCEASEDOCID);
    	   increaseDocid.setActionCommand(INCEASEID);
    	   ButtonGroup group = new ButtonGroup();
    	   group.add(increaseRevision);
    	   group.add(increaseDocid);
           // Registers a listener for the radio buttons
    	   increaseRevision.addActionListener(this);
    	   increaseDocid.addActionListener(this);
    	   
    	   //Creates JOptionPanel to contain the radio buttons
    	   Object[] choices = {increaseDocid, increaseRevision};
    	   JOptionPane.showOptionDialog(this, //parent
                   message, // Message
                   TITLE, //title
                   JOptionPane.YES_OPTION, //optionType
                   JOptionPane.PLAIN_MESSAGE,//messageTye
                   null ,
                   choices,
                   null);
       }
       
       /**
        * Gets the value of user's choice
        * @return
        */
       public String getUserChoice()
       {
    	   return userChoice;
       }
       
       /** 
        * Listens to the radio buttons.
       */
       public void actionPerformed(ActionEvent e) 
       {
          userChoice = e.getActionCommand();                                   
       }
}
