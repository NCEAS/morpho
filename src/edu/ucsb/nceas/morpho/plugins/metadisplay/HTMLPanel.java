/**
 *  '$RCSfile: HTMLPanel.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-08-24 00:41:34 $'
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

import java.io.IOException;

import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import java.awt.Color;

import edu.ucsb.nceas.morpho.util.Log;

import edu.ucsb.nceas.morpho.plugins.MetaDisplayInterface;



/**
 *  Plugin that builds a display panel to display metadata.  Given a String ID, 
 *  does a lookup using a factory that must also be provided (and which 
 *  implements the ContentFactoryInterface) to get the XML document to display.  
 *  Then styles this document accordingly using XSLT, before displaying it in an 
 *  embedded HTML display.
 */
public class HTMLPanel extends JEditorPane implements HyperlinkListener
{
//  * * * * * * * C L A S S    V A R I A B L E S * * * * * * *

    private MetaDisplayInterface  controller;

    /**
     *  constructor
     *
     */
    public HTMLPanel(MetaDisplayInterface controller) throws IOException {
    
        this("DEFAULT_HTML");
        this.controller = controller;
    }

    /**
     *  constructor
     *
     */
    public HTMLPanel(String html) throws IOException {
        super();
        this.setContentType("text/html");
        init();
        addHyperlinkListener(this);
        setEditable(false);
    }

    public void hyperlinkUpdate(HyperlinkEvent e) {
        try {
            Log.debug(50,"hyperlinkUpdate called; eventType="+e.getEventType());
            if ( e.getEventType() == HyperlinkEvent.EventType.ACTIVATED ) {
                Log.debug(50,"eventType=ACTIVATED; description="
                                      +e.getDescription()+"; url="+e.getURL());
                controller.display(e.getDescription());
            }
        } catch (Exception ex) {
            Log.debug(12,"HTMLPanel.hyperlinkUpdate(): "
                            +"Exception trying to dispatch HyperlinkEvent:"+ex);
            ex.printStackTrace(System.err);
        }
    }

    private void init()
    {
        this.setBackground(Color.gray);
        this.setOpaque(true);
    }

	/**
	 *  get a reference to the embedded HeaderPanel object
	 *
     *  @return  a reference to the embedded HeaderPanel object
	 */
	public void setHTML(String html)
	{
        this.setText(html);
	}
    
    
	private static final String DEFAULT_HTML =
	     "<html><head></head>\n<body bgcolor=\"#dddddd\">\n"
	    +"<h3>no data</h3></body></html>";
	    
    
}
