/**
 *  '$RCSfile: MetaDisplayUI.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-08-26 23:48:18 $'
 * '$Revision: 1.7 $'
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

import java.awt.Dimension;
import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.Box;
import javax.swing.BoxLayout;

import edu.ucsb.nceas.morpho.util.Log;

import edu.ucsb.nceas.morpho.plugins.MetaDisplayInterface;


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
    private final MetaDisplayInterface  controller;

    private final int TOTAL_WIDTH     = 400;
    private final int TOTAL_HEIGHT    = 600;
    
    private final int PADDING   = 0;

    private final int HEADER_HEIGHT    = 55;
    private final int HEADER_WIDTH     = TOTAL_WIDTH - 2 * PADDING;

    private final Dimension OVERALL_DIMS 
        = new Dimension(TOTAL_WIDTH, TOTAL_HEIGHT);
    protected final Dimension HEADER_DIMS 
        = new Dimension(HEADER_WIDTH, HEADER_HEIGHT);
    private final Dimension HTML_DIMS   
        = new Dimension(HEADER_WIDTH, TOTAL_HEIGHT - HEADER_HEIGHT - 3*PADDING);

    /**
     *  constructor
     *
     */
    public MetaDisplayUI(MetaDisplayInterface controller) {
    
        this.controller = controller;
        this.header     = new HeaderPanel(controller);
        try {
            htmlPanel = new HTMLPanel(controller);
        } catch (IOException ioe) {
            Log.debug(5, "Error trying to create MetaData display pane: "+ioe);
            ioe.printStackTrace();
        }
        initLayout();
    }

    private void initLayout()
    {
        header.setPreferredSize(HEADER_DIMS);
        htmlPanel.setPreferredSize(HTML_DIMS);
        this.setOpaque(true);
        
        this.setLayout(new BorderLayout());
        this.add(Box.createHorizontalStrut(PADDING),BorderLayout.WEST);
        Box centerPanel = Box.createVerticalBox();
        this.add(centerPanel, BorderLayout.CENTER) ;
        this.add(Box.createHorizontalStrut(PADDING),BorderLayout.EAST);

        centerPanel.add(Box.createVerticalStrut(PADDING));
        centerPanel.add(header);
        centerPanel.add(Box.createVerticalStrut(PADDING));
        centerPanel.add(htmlPanel);
        centerPanel.add(Box.createVerticalStrut(PADDING));
        
        this.setMinimumSize(OVERALL_DIMS);
        this.setPreferredSize(OVERALL_DIMS);
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
