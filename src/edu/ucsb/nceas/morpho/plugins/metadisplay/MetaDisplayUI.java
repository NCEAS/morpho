/**
 *  '$RCSfile: MetaDisplayUI.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-09-26 02:46:35 $'
 * '$Revision: 1.12 $'
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

import java.io.IOException;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;

import javax.swing.border.Border;

import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.UISettings;

import edu.ucsb.nceas.morpho.plugins.metadisplay.MetaDisplay;


/**
 *  Display component used by the MetaDisplay class to display:<ul>
 *  <li> an embedded header panel containing navigation controls, and </li>
 *  <li> an embedded HTML display showing the results of styling the XML 
 *  metadata document usig XSLT.</li></ul>
 */
public class MetaDisplayUI extends JPanel
{
//  * * * * * * * C L A S S    V A R I A B L E S * * * * * * *

    private       HTMLPanel             htmlPanel;
    private final HeaderPanel           header;
    private final MetaDisplay           controller;
    
    private static final Border focusedBorder 
        = BorderFactory.createMatteBorder(  
                  UISettings.FOCUS_BORDER_WIDTH, UISettings.FOCUS_BORDER_WIDTH, 
                  UISettings.FOCUS_BORDER_WIDTH, UISettings.FOCUS_BORDER_WIDTH,
                  UISettings.FOCUSED_BORDER_COLOR );

    private static final Border unfocusedBorder 
        = BorderFactory.createMatteBorder(  
                  UISettings.FOCUS_BORDER_WIDTH, UISettings.FOCUS_BORDER_WIDTH, 
                  UISettings.FOCUS_BORDER_WIDTH, UISettings.FOCUS_BORDER_WIDTH,
                  UISettings.UNFOCUSED_BORDER_COLOR );
    
    /**
     *  constructor
     *
     */
    public MetaDisplayUI(MetaDisplay controller) 
    {
        this.controller = controller;
        //init final vars:
        this.header     = new HeaderPanel(controller);
        try {
            this.htmlPanel = new HTMLPanel(controller);
        } catch (IOException ioe) {
            Log.debug(5, "Error trying to create MetaData display pane: "+ioe);
            ioe.printStackTrace();
        }
        ////
        initLayout();
        initFocusBorder();
    }

    private void initLayout()
    {
        JScrollPane htmlScroll = new JScrollPane(htmlPanel);
        
        this.setOpaque(true);
        this.setLayout(new BorderLayout());
        
        this.add(header, BorderLayout.NORTH);
        this.add(htmlScroll, BorderLayout.CENTER);
    }

    public boolean isFocusTraversable() 
    {
        return true;
    }
    
    private void initFocusBorder()
    {
        this.setBorder(unfocusedBorder);
//        final JComponent instance = this;
//        this.addMouseListener( new MouseAdapter() {
//            public void mouseClicked(MouseEvent e) {
//                Log.debug(50,"mouseClicked in MetaDisplayUI");
//                instance.setBorder(focusedBorder);
//            }
//        });
//        this.addFocusListener( new FocusAdapter() {
//            public void focusGained(FocusEvent e) {
//                Log.debug(50,"mouseClicked in MetaDisplayUI");
//                instance.setBorder(focusedBorder);
//            }
//        });
    }

	
	/**
	 *  add a PathElement object to the path
	 *
	 *  @param  pathElement the <code>PathElement</code> to add
	 */
	public void addToPath(PathElementInterface pathElement)
	{
//		getHeader().getPath().add(pathElement);
	}

	/**
	 *  set content in the embedded HTMLPanel object
	 *
	 *  @param  html  the HTML content to be displayed
	 */
	public void setHTML(String html)
	{
		getHTMLPanel().setHTML(html);
	}
	
	/**
	 *  protected method: get a reference to the embedded HeaderPanel object
	 *
     *  @return  a reference to the embedded HeaderPanel object
	 */
	protected HeaderPanel getHeader()
	{
		return this.header;
	}

	/**
	 *  protected method: get a reference to the embedded HTMLPanel object
	 *
	 *  @return  a reference to the embedded HTMLDisplay object
	 */
	protected HTMLPanel getHTMLPanel()
	{
		return this.htmlPanel;
	}
    
}
