/**
 *  '$RCSfile: MetaDisplayUI.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-08-21 20:15:19 $'
 * '$Revision: 1.3 $'
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

import java.awt.Dimension;
import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.Box;
import javax.swing.BoxLayout;


//import edu.ucsb.nceas.morpho.plugins.DocumentNotFoundException;



/**
 *  Plugin that builds a display panel to display metadata.  Given a String ID, 
 *  does a lookup using a factory that must also be provided (and which 
 *  implements the ContentFactoryInterface) to get the XML document to display.  
 *  Then styles this document accordingly using XSLT, before displaying it in an 
 *  embedded HTML display.
 */
public class MetaDisplayUI extends JPanel
{
//  * * * * * * * C L A S S    V A R I A B L E S * * * * * * *

    private final HeaderPanel header;
    private final HTMLPanel htmlPanel;

    private final int TOTAL_WIDTH     = 400;
    private final int TOTAL_HEIGHT    = 600;
    
    private final int PADDING   = 10;

    private final int HEADER_HEIGHT    = 100;
    private final int HEADER_WIDTH     = TOTAL_WIDTH - 2 * PADDING;

    private final Dimension OVERALL_DIMS 
        = new Dimension(TOTAL_WIDTH, TOTAL_HEIGHT);
    private final Dimension HEADER_DIMS 
        = new Dimension(HEADER_WIDTH, HEADER_HEIGHT);
    private final Dimension HTML_DIMS   
        = new Dimension(HEADER_WIDTH, TOTAL_HEIGHT - HEADER_HEIGHT - 3*PADDING);

    /**
     *  constructor
     *
     */
    public MetaDisplayUI() {
        header = new HeaderPanel();
        htmlPanel = new HTMLPanel();
        initLayout();
    }

    private void initLayout()
    {
        header.setPreferredSize(HEADER_DIMS);
        htmlPanel.setPreferredSize(HTML_DIMS);
        this.setOpaque(true);
        
        this.setLayout(new BorderLayout());
        this.add(Box.createHorizontalStrut(PADDING),BorderLayout.WEST);
        JPanel centerPanel = new JPanel();
        this.add(centerPanel, BorderLayout.CENTER) ;
        this.add(Box.createHorizontalStrut(PADDING),BorderLayout.EAST);

        centerPanel.setLayout(new BoxLayout(centerPanel,BoxLayout.Y_AXIS));
        centerPanel.add(Box.createVerticalStrut(PADDING));
        centerPanel.add(header);
        centerPanel.add(Box.createVerticalStrut(PADDING));
        centerPanel.add(htmlPanel);
        centerPanel.add(Box.createVerticalStrut(PADDING));
        
        this.setMinimumSize(OVERALL_DIMS);
        this.setPreferredSize(OVERALL_DIMS);
    }

	
	/**
	 *  get a reference to the embedded HeaderPanel object
	 *
     *  @return  a reference to the embedded HeaderPanel object
	 */
	public HeaderPanel getHeader()
	{
		return this.header;
	}

	
	/**
	 *  get a reference to the embedded HTMLPanel object
	 *
	 *  @return  a reference to the embedded HTMLDisplay object
	 */
	public void setHTML(String html)
	{
		getHTMLPanel().setHTML(html);
	}

	/**
	 *  get a reference to the embedded HTMLPanel object
	 *
	 *  @return  a reference to the embedded HTMLDisplay object
	 */
	protected HTMLPanel getHTMLPanel()
	{
		return this.htmlPanel;
	}
}
