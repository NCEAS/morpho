/**
 *  '$RCSfile: HeaderPanel.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-08-26 23:48:18 $'
 * '$Revision: 1.4 $'
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Container;
import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.Box;
import javax.swing.BoxLayout;

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
    //  * * * * * * * C L A S S    V A R I A B L E S * * * * * * *

    
    private final int DUMMY_WIDTH = 100;    //ignored by BorderLayout

    protected final Dimension DEFAULT_TITLEBAR_DIMS 
                                                = new Dimension(DUMMY_WIDTH,27);

    protected final Dimension DEFAULT_BOTTOMLINE_DIMS 
                                                = new Dimension(DUMMY_WIDTH, 2);
        
    private final static Color DEFAULT_TITLEBAR_COLOR   = Color.gray;
    private final static Color DEFAULT_BACKGROUND_COLOR = Color.lightGray;
    private final static Color DEFAULT_BOTTOMLINE_COLOR = Color.darkGray;
    
    private final MetaDisplayInterface  controller;
    
    private JButton backButton;
    private JPanel  titleBar;
    private Color   titleBarColor;
    private Color   backgroundColor;

    /**
    *  constructor
    *
    */
    public HeaderPanel(MetaDisplayInterface controller) 
    {
        this.controller = controller;
        init();
    }

    private void init() 
    {
        this.setLayout(new BorderLayout(0,0));
        this.setOpaque(true);
        this.setBackground(DEFAULT_BACKGROUND_COLOR);
        addTitleBar();
        addPathBar();
        addBottomLine();
    }

    private void addTitleBar() 
    {
        //add titlebar itself:
        titleBar = new JPanel();
        titleBar.setLayout(new BoxLayout(titleBar, BoxLayout.Y_AXIS));
        titleBar.setPreferredSize(DEFAULT_TITLEBAR_DIMS);
        setTitleBarColor(DEFAULT_TITLEBAR_COLOR);
        titleBar.setOpaque(true);
        this.add(titleBar, BorderLayout.NORTH);
        
        //add back button:
        GUIAction backAction 
                    = new GUIAction("< back", null, new BackCommand(controller));
        backButton = new JButton(backAction);
        titleBar.add(backButton);
        //add title text label
        //add close button:

    }

    private void addPathBar() 
    {
        //add path display
        //add edit button:
    }

    private void addBottomLine() 
    {
        JLabel bottomLine = new JLabel();
        bottomLine.setPreferredSize(DEFAULT_BOTTOMLINE_DIMS);
        bottomLine.setBackground(DEFAULT_BOTTOMLINE_COLOR);
        bottomLine.setOpaque(true);
        this.add(bottomLine, BorderLayout.SOUTH);
    }

    /**
    *  set color of title bar (ie main part of header above path bar)
    *
    *  @param color the <code>java.awt.Color</code> to use
    */
    public void setTitleBarColor(Color color) 
    {
        titleBar.setBackground(color);
        titleBar.invalidate();
    }
}


