package edu.ucsb.nceas.morpho.datapackage;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;

import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.Util;

/**
 * This handler can pop a dialog when docid confilction happens during save(synchronize) process.
 * User will give options to choose increase docid or just increase revision.
 * @author tao
 *
 */
public class DocidConflictHandler 
{
     
       public static final String INCREASEID = "increaseID";
       public static final String INCREASEREVISION = "increaseRevision";
       public static final String METACAT = "Metacat";
       public static final String LOCAL  = "local system";
       private static final int PADDING = 5;
       private static final int WIDTH = 800;
       private static final int HEIGHT = 350;
       private static final int EXTRAL = 75;
       private static final int HEADER = 30;
       private static final  Dimension LABELDIMENSION = new Dimension(WIDTH,60);
       private String docid = null;
       private String location = null;
       private String userChoice = null;
       private static final String LABELINCEASEDOCID = "Generate new document id";
       private static final String LABELINCREASEREVISION = "Increment revsion number";
       private static final String TITLE = "Resolve Identifier Conflict";
       private String message = null;
       private JPanel okButtonPanel = null;
       private JPanel choicePanel = null;
       private JPanel messagePanel = null;
       private JRadioButton increaseRevision = null;
       private JRadioButton increaseDocid = null;
       private JDialog dialog = null;
       
       /**
        * Constructor
        */
       public DocidConflictHandler(String docid, String location)
       {
    	    //super(parent, true);
    	    this.docid = docid;
    	    this.location = location;
    	    this.userChoice = INCREASEREVISION;
    	    message =  "Document id "+docid +" already exists in "+location+
            ". If the saving document is an updated version of the document, increment the revision number. "+ 
            "Otherwise, generate a new document id.";
    	    //intialGUI();
       }
       
       /*
        * Intializes the gui
        */
       public String showDialog()
       {
         dialog = new JDialog();
         dialog.setTitle(TITLE);
         dialog.setModal(true);
         dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
         MorphoFrame parent= UIController.getInstance().getCurrentActiveWindow();
         if(parent != null)
         {
           int parentWidth = parent.getWidth();
           int parentHeight = parent.getHeight();
           double parentX = parent.getLocation().getX();
           double parentY = parent.getLocation().getY();
           double centerX = parentX + 0.5 * parentWidth;
           double centerY = parentY + 0.5 * parentHeight;
           int dialogX = (new Double(centerX - 0.5 * WIDTH)).intValue();
           int dialogY = (new Double(centerY - 0.5 * HEIGHT)).intValue();
           dialog.setLocation(dialogX, dialogY);
         }        
         dialog.setSize(WIDTH, HEIGHT);
         dialog.setLayout(new BorderLayout());
         createMessagePanel();
         createChoicePanel();
         createOKButtonPanel();
         dialog.add(messagePanel, BorderLayout.NORTH);
         dialog.add(choicePanel, BorderLayout.CENTER);
         dialog.add(okButtonPanel, BorderLayout.SOUTH);
    	   dialog.setVisible(true);
    	   return userChoice;
       }
       
       /*
        * Create a message panel. It locates on the northern part of the dialog
        */
       private void createMessagePanel()
       {
         messagePanel = new JPanel();
         JLabel messageLabel = new JLabel(message);
         Util.setPrefMaxSizes(messageLabel, LABELDIMENSION);
         messagePanel.add(messageLabel);
       }
       
       /*
        * Create a JPnale containing two radio buttons. It will be on the central position
        */
       private void createChoicePanel()
       {
         choicePanel = new JPanel();
         choicePanel.setLayout(new BoxLayout(choicePanel, BoxLayout.Y_AXIS));
         choicePanel.add(Box.createVerticalStrut(HEADER));
         //Creates two radio button for options
         increaseRevision = new JRadioButton(LABELINCREASEREVISION);
         increaseRevision.setSelected(true);
         increaseDocid = new JRadioButton(LABELINCEASEDOCID);       
         ButtonGroup group = new ButtonGroup();
         group.add(increaseRevision);
         group.add(increaseDocid);
         choicePanel.add(increaseRevision);
         choicePanel.add(increaseDocid);
       }
       
       
       
       /*
        * Create a panel containing cancel and transform button.
        * It will locate on south of dialog.
        */
       private void createOKButtonPanel()
       {
         okButtonPanel = new JPanel();
         JButton okButton = new JButton("Ok");
         ActionListener okAction = new DialogOKAction();
         okButton.addActionListener(okAction);
         okButtonPanel.setLayout(new BoxLayout(okButtonPanel, BoxLayout.X_AXIS));
         okButtonPanel.setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));
         okButtonPanel.add(Box.createHorizontalGlue());
         okButtonPanel.add(okButton);
         okButtonPanel.add(Box.createHorizontalStrut(PADDING));
         okButtonPanel.add(okButton);
       }
       
       /**
        * Gets the value of user's choice
        * @return
        */
       /*public String getUserChoice()
       {
         Log.debug(30, "The user's choice on DocidIncreaseDialog.getUserChoice is "+userChoice);
    	   return userChoice;
       }*/
       
       
       /*
        * Listener class for OK button. It will get user's choice.
        */
       private class DialogOKAction implements ActionListener
       {
         /** 
          * Listens to the radio buttons.
         */
         public void actionPerformed(ActionEvent e) 
         {
            //userChoice = e.getActionCommand();
           //Log.debug(5, "action peformed");
           if(increaseRevision != null && increaseRevision.isSelected())
           {
             //Log.debug(5, "in crease revisoin branch");
             userChoice = INCREASEREVISION;
           }
           else if(increaseDocid != null && increaseDocid.isSelected())
           {
             //Log.debug(5, "in crease id branch");
             userChoice = INCREASEID;
           }
           else
           {
             //Log.debug(5, "in other brach");
             userChoice = INCREASEREVISION;
           }
           if(dialog != null)
           {
             dialog.setVisible(false);
             dialog.dispose();
           }
           //Log.debug(5, "final choice is "+userChoice);
         }
       }
}
