/**
 *  '$RCSfile: HTMLPanel.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-08-21 03:26:06 $'
 * '$Revision: 1.2 $'
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

import javax.swing.JPanel;
import javax.swing.JTextArea;

import java.awt.Color;
import java.awt.BorderLayout;

//import edu.ucsb.nceas.morpho.plugins.DocumentNotFoundException;



/**
 *  Plugin that builds a display panel to display metadata.  Given a String ID, 
 *  does a lookup using a factory that must also be provided (and which 
 *  implements the ContentFactoryInterface) to get the XML document to display.  
 *  Then styles this document accordingly using XSLT, before displaying it in an 
 *  embedded HTML display.
 */
public class HTMLPanel extends JPanel
{
//  * * * * * * * C L A S S    V A R I A B L E S * * * * * * *

    private JTextArea textArea;
    
    /**
     *  constructor
     *
     */
    public HTMLPanel() {
        this("METADATA");
    }

    /**
     *  constructor
     *
     */
    public HTMLPanel(String html) {
        init();
        setHTML(html);
    }


    private void init()
    {
        this.setBackground(Color.red);
        this.setOpaque(true);
        this.setLayout(new BorderLayout());
        textArea = new JTextArea();
        textArea.setBackground(Color.white);
        textArea.setOpaque(true);
        textArea.setLineWrap(true);
        this.add(textArea, BorderLayout.CENTER);
    }


	/**
	 *  get a reference to the embedded HeaderPanel object
	 *
     *  @return  a reference to the embedded HeaderPanel object
	 */
	public void setHTML(String html)
	{
		textArea.setText(html);
        this.validate();
	}

	
}
