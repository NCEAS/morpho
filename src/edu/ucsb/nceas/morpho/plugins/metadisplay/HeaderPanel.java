/**
 *  '$RCSfile: HeaderPanel.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-12-18 19:53:55 $'
 * '$Revision: 1.14 $'
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
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import  edu.ucsb.nceas.morpho.util.Command;
import  edu.ucsb.nceas.morpho.util.GUIAction;
import  edu.ucsb.nceas.morpho.util.UISettings;

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
    private static final String BACK_BUTTON_TEXT    = "< back";
    private static final String EDIT_BUTTON_TEXT    = "edit";
    private static final String CLOSE_BUTTON_TEXT   = "hide X";
    private static final String TITLEBAR_INIT_TEXT  = "Documentation";
    private static final String PATH_INIT_TEXT      = ""; //You are here:\n";
    private static final String PATH_SEPARATOR      = ">>";
 

    private eJButton backButton;
    private eJButton closeButton;
    
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
        this.setBackground(UISettings.BACKGROUND_COLOR);
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
        titleBar.setPreferredSize(UISettings.TITLEBAR_DIMS);
        titleBar.setBorder(new EmptyBorder( UISettings.TITLEBAR_TOP_PADDING,
                                            UISettings.TITLEBAR_SIDES_PADDING,
                                            UISettings.TITLEBAR_BOTTOM_PADDING,
                                            UISettings.TITLEBAR_SIDES_PADDING ));
        setTitleBarColor(UISettings.TITLEBAR_COLOR);
        titleBar.setOpaque(true);
        this.add(titleBar, BorderLayout.NORTH);

        //add back button:
        GUIAction backAction 
            = new GUIAction(BACK_BUTTON_TEXT, null,new BackCommand(controller));
        backButton = new eJButton(backAction);
        backButton.setBackground(UISettings.BACKBUTTON_COLOR);
        backButton.setForeground(UISettings.BACKBUTTON_TEXT_COLOR);
        backButton.setFocusPainted(false);
        backButton.setFont(UISettings.BUTTON_FONT);
        titleBar.add(backButton, BorderLayout.WEST);
        
        //add title text label
        titleTextLabel = new JLabel(TITLEBAR_INIT_TEXT);
        titleTextLabel.setForeground(UISettings.TITLE_TEXT_COLOR);
        titleTextLabel.setFont(UISettings.SUBPANEL_TITLE_FONT);
        titleTextLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleBar.add(titleTextLabel, BorderLayout.CENTER);
        
        //add close button:
        GUIAction closeAction 
            = new GUIAction(CLOSE_BUTTON_TEXT, null, 
                                                new CloseCommand(controller));
        closeButton = new eJButton(closeAction);
        closeButton.setBackground(UISettings.CLOSEBUTTON_COLOR);
        closeButton.setForeground(UISettings.CLOSEBUTTON_TEXT_COLOR);
        closeButton.setFocusPainted(false);
        closeButton.setFont(UISettings.BUTTON_FONT);
        titleBar.add(closeButton, BorderLayout.EAST);
//        closeButton.setEnabled(false);  //hack for now, since dataviewer not 
                                        //listening to close events
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
        pathBar.setBorder(new EmptyBorder(  UISettings.PATHBAR_TOP_PADDING,
                                            UISettings.PATHBAR_SIDES_PADDING,
                                            UISettings.PATHBAR_BOTTOM_PADDING,
                                            UISettings.PATHBAR_SIDES_PADDING ));
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
        eJButton editButton = new eJButton(editAction);
        editButton.setBackground(UISettings.EDITBUTTON_COLOR);
        editButton.setForeground(UISettings.EDITBUTTON_TEXT_COLOR);
        editButton.setFocusPainted(false);
        editButton.setFont(UISettings.BUTTON_FONT);
        editButton.setPreferredSize(closeButton.getPreferredSize());
        editButton.setMinimumSize(closeButton.getMinimumSize());
        editButton.setMaximumSize(closeButton.getMaximumSize());
        Box buttonBox = Box.createVerticalBox();
        buttonBox.add(Box.createVerticalGlue());
        buttonBox.add(editButton);
        buttonBox.add(Box.createVerticalGlue());
        pathBar.add(buttonBox, BorderLayout.EAST);
    }

    private void addBottomLine() 
    {
        JPanel bottomLine = new JPanel();
        bottomLine.setPreferredSize(UISettings.HEADER_BOTTOMLINE_DIMS);
        bottomLine.setBackground(UISettings.BOTTOMLINE_COLOR);
        bottomLine.setOpaque(true);
        this.add(bottomLine, BorderLayout.SOUTH);
    }

    /**
     *   set enabled/disabled state of Back Button (primarily to allow disabling 
     *   when there is no previous page in History to go back to)
     *
     *   @param enabled  A <code>boolean</code> value - <ul>
     *                   <li><code>true</code> enables the button, </li>
     *                   <li><code>false</code> disables it</li></ul>
     */
    protected void setBackButtonEnabled(boolean enabled) 
    {
        backButton.setEnabled(enabled);
        titleBar.invalidate();
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
    
    /**
     *  set text of title bar (ie main part of header above path bar)
     *
     *  @param color the <code>java.awt.Color</code> to use
     */
    protected void setTitleBarText(String text) 
    {
        titleTextLabel.setText(text);
        titleTextLabel.invalidate();
    }
    
   
    //VARIABLES

    private final MetaDisplay  controller;
    
    private JPanel  titleBar;
    private JLabel  titleTextLabel;

    //INNER CLASS - EXTENDS JBUTTON SO WE CAN CONTROL BG COLOR ON MAC OSX
    // (OTHERWISE, OSX IGNORES BGCOLOR, WHICH MAKES TEXT UNREADABLE)
    class eJButton extends JButton 
    {
        Color enabledForeGroundColor;
        
        eJButton(Action a) {
            super(a);
            super.setUI(new javax.swing.plaf.metal.MetalButtonUI());
            UIManager.put(  "Button.disabledText",
                            UISettings.BUTTON_DISABLED_TEXT_COLOR);
            UIManager.put(  "Button.margin",
                            UISettings.METAVIEW_BUTTON_INSETS);
            updateUI();
        }
    }
}
