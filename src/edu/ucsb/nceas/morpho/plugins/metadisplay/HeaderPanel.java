/**
 *  '$RCSfile: HeaderPanel.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-09-11 15:53:44 $'
 * '$Revision: 1.8 $'
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

package edu.ucsb.nceas.morpho.plugins.metadisplay;

import java.awt.Font;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.BorderLayout;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import  edu.ucsb.nceas.morpho.util.Command;
import  edu.ucsb.nceas.morpho.util.GUIAction;

import edu.ucsb.nceas.morpho.plugins.MetaDisplayInterface;

//import edu.ucsb.nceas.morpho.plugins.DocumentNotFoundException;

/**
 *  Plugin that builds a display panel to display metadata.  Given a String ID, 
 *  does a lookup using a factory that must also be provided (and which 
 *  implements the ContentFactoryInterface) to get the XML document to display.  
 *  Then styles this document accordingly using XSLT, before displaying it in an 
 *  embedded HTML display.
 */
public class HeaderPanel extends JPanel
{
    
    // * * * * * * * *  D E F A U L T   T E X T   L A B E L S   * * * * * * * * 
    private static final String BACK_BUTTON_TEXT = "< back";
    private static final String EDIT_BUTTON_TEXT = "edit";
    private static final String CLOSE_BUTTON_TEXT = "hide X";
    private static final String TITLEBAR_INIT_TEXT = "Metadata View";
    private static final String PATH_INIT_TEXT = "You are here:\n";
    private static final String PATH_SEPARATOR = ">>";
 

    // * * * *  D E F A U L T   F O N T S  &  T E X T - C O L O R S   * * * * * 

    private final Font TITLE_FONT  = new Font(null, Font.BOLD, 14);
    private final Font BUTTON_FONT = new Font(null, Font.BOLD, 11);
    
    private final static Color TITLE_TEXT_COLOR       = Color.white;
    private final static Color BACKBUTTON_TEXT_COLOR  = new Color(0, 198, 255);
    private final static Color CLOSEBUTTON_TEXT_COLOR = BACKBUTTON_TEXT_COLOR;
    private final static Color EDITBUTTON_TEXT_COLOR  = new Color(0, 255, 0);
    
    
    // * * * * * *  D E F A U L T   D I M E N S I O N S  * * * * * * * * * * * * 
    
    private final int TITLEBAR_HEIGHT           = 27;
    private final int TITLEBAR_TOP_PADDING      = 0;
    private final int TITLEBAR_SIDES_PADDING    = 0;
    private final int TITLEBAR_BOTTOM_PADDING   = 2;

    private final int PATHBAR_TOP_PADDING       = 2;
    private final int PATHBAR_SIDES_PADDING     = 0;
    private final int PATHBAR_BOTTOM_PADDING    = 2;

    private final int DUMMY_WIDTH = 100;    //ignored by BorderLayout

    protected final Dimension TITLEBAR_DIMS 
                                  = new Dimension(DUMMY_WIDTH,TITLEBAR_HEIGHT);
    private final Dimension BOTTOMLINE_DIMS = new Dimension(DUMMY_WIDTH, 2);
    private final int TITLEBAR_COMPONENT_HEIGHT 
            = TITLEBAR_HEIGHT - TITLEBAR_TOP_PADDING - TITLEBAR_BOTTOM_PADDING;
        
    // * * * * *  D E F A U L T   C O M P O N E N T   C O L O R S  * * * * * * *
    
    private final static Color TITLEBAR_COLOR       = Color.gray;
    private final static Color BACKGROUND_COLOR     = Color.lightGray;
    private final static Color BOTTOMLINE_COLOR     = Color.darkGray;
    private final static Color BACKBUTTON_COLOR     = TITLEBAR_COLOR;
    private final static Color CLOSEBUTTON_COLOR    = TITLEBAR_COLOR;
    private final static Color EDITBUTTON_COLOR     = TITLEBAR_COLOR;

    
    /**
    *  constructor
    *
    */
    public HeaderPanel(MetaDisplay controller) 
    {
        this.controller = controller;
        init();
    }

    private void init() 
    {
        this.setLayout(new BorderLayout(0,0));
        this.setOpaque(true);
        this.setBackground(BACKGROUND_COLOR);
        addTitleBar();
        addPathBar();
        addBottomLine();
    }
    
    //titlebar has a fixed height, and is the topmost component in the display 
    //(ie is loosely analogous to a JFrame titlebar).  It also has a back button 
    //and a close button.
    private void addTitleBar() 
    {
        //add titlebar itself:
        titleBar = new JPanel();
        titleBar.setLayout(new BorderLayout(0,0));
        titleBar.setPreferredSize(TITLEBAR_DIMS);
        titleBar.setBorder(new EmptyBorder( TITLEBAR_TOP_PADDING,
                                            TITLEBAR_SIDES_PADDING,
                                            TITLEBAR_BOTTOM_PADDING,
                                            TITLEBAR_SIDES_PADDING ));
        setTitleBarColor(TITLEBAR_COLOR);
        titleBar.setOpaque(true);
        this.add(titleBar, BorderLayout.NORTH);

        //add back button:
        GUIAction backAction 
            = new GUIAction(BACK_BUTTON_TEXT, null,new BackCommand(controller));
        JButton backButton = new JButton(backAction);
        backButton.setBackground(BACKBUTTON_COLOR);
        backButton.setForeground(BACKBUTTON_TEXT_COLOR);
        backButton.setFocusPainted(false);
        backButton.setFont(BUTTON_FONT);
        titleBar.add(backButton, BorderLayout.WEST);
        
        //add title text label
        titleTextLabel = new JLabel(TITLEBAR_INIT_TEXT);
        titleTextLabel.setForeground(TITLE_TEXT_COLOR);
        titleTextLabel.setFont(TITLE_FONT);
        titleTextLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleBar.add(titleTextLabel, BorderLayout.CENTER);
        
        //add close button:
        GUIAction closeAction 
            = new GUIAction(CLOSE_BUTTON_TEXT, null, 
                                                new CloseCommand(controller));
        JButton closeButton = new JButton(closeAction);
        closeButton.setBackground(CLOSEBUTTON_COLOR);
        closeButton.setForeground(CLOSEBUTTON_TEXT_COLOR);
        closeButton.setFocusPainted(false);
        closeButton.setFont(BUTTON_FONT);
        titleBar.add(closeButton, BorderLayout.EAST);
    }

    //the pathbar contains the actual component that displays the path itself, 
    //on the left, and the edit button to the right of the path display 
    //component.  If the path text exceeds the path display component's maximum 
    //width, the text will wrap onto a new line, and the path display component 
    //will consequently expand in height to accomodate this wrapping. This would
    //also cause the overall pathbar height to increase accordingly.
    //NOTE that, although the pathbar needs to expand and contract in this 
    //manner, it also needs to keep its height fixed at its current value if the 
    //overall MetaDisplay dimensions change (e.g. due to window resizing); also,
    //the edit button needs to have a fixed height, whatever happens.
    private void addPathBar() 
    {
        //create and add the container
        JPanel pathBar = new JPanel();
        
        pathBar.setLayout(new BorderLayout(0,0));
        pathBar.setBorder(new EmptyBorder(  PATHBAR_TOP_PADDING,
                                            PATHBAR_SIDES_PADDING,
                                            PATHBAR_BOTTOM_PADDING,
                                            PATHBAR_SIDES_PADDING ));
        this.add(pathBar, BorderLayout.CENTER);
 
        //add path display
        JTextArea pathDisplayComponent = new JTextArea();
        pathBar.add(pathDisplayComponent, BorderLayout.CENTER);
        pathDisplayComponent.setBackground(pathBar.getBackground());
        pathDisplayComponent.setText(PATH_INIT_TEXT);
        pathDisplayComponent.setEditable(false);
        
        //add edit button:
        GUIAction editAction
            = new GUIAction(EDIT_BUTTON_TEXT, null,new EditCommand(controller));
        JButton editButton = new JButton(editAction);
        editButton.setBackground(EDITBUTTON_COLOR);
        editButton.setForeground(EDITBUTTON_TEXT_COLOR);
        editButton.setFocusPainted(false);
        editButton.setFont(BUTTON_FONT);
        Box buttonBox = Box.createVerticalBox();
        buttonBox.add(Box.createVerticalGlue());
        buttonBox.add(editButton);
        buttonBox.add(Box.createVerticalGlue());
        pathBar.add(buttonBox, BorderLayout.EAST);
    }

    private void addBottomLine() 
    {
        JPanel bottomLine = new JPanel();
        bottomLine.setPreferredSize(BOTTOMLINE_DIMS);
        bottomLine.setBackground(BOTTOMLINE_COLOR);
        bottomLine.setOpaque(true);
        this.add(bottomLine, BorderLayout.SOUTH);
    }

    /**
    *  set color of title bar (ie main part of header above path bar)
    *   this may need to be called for example to change titlebar color for 
    *   focus/non-focus indocation
    *
    *  @param color the <code>java.awt.Color</code> to use
    */
    protected void setTitleBarColor(Color color) 
    {
        titleBar.setBackground(color);
        titleBar.invalidate();
    }
    
   
    //VARIABLES

    private final MetaDisplay  controller;
    
    private JPanel  titleBar;
    private JLabel  titleTextLabel;
}


