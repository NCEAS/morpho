/**
 *  '$RCSfile: HTMLPanel.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-09-11 15:53:44 $'
 * '$Revision: 1.6 $'
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

import javax.swing.border.EmptyBorder;
import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import edu.ucsb.nceas.morpho.util.Log;

import edu.ucsb.nceas.morpho.plugins.MetaDisplayInterface;
import edu.ucsb.nceas.morpho.plugins.DocumentNotFoundException;


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

	private static final String DEFAULT_HTML =
	     "<html><head></head>\n<body bgcolor=\"#eeeeee\">&nbsp;</body></html>";

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
    public HTMLPanel(String html) throws IOException 
    {
        super();
        init();
        addHyperlinkListener(this);
    }

    public void hyperlinkUpdate(HyperlinkEvent e) 
    {
        Log.debug(50,"hyperlinkUpdate called; eventType=" + e.getEventType());
        if ( e.getEventType() == HyperlinkEvent.EventType.ACTIVATED ) {
            Log.debug(50,"eventType=ACTIVATED; description="
                                  +e.getDescription()+"; url="+e.getURL());
            try {
                    controller.display(e.getDescription());
            } catch (DocumentNotFoundException ex) {
                Log.debug(12,"HTMLPanel.hyperlinkUpdate(): "
                    +"DocumentNotFoundException from HyperlinkEvent: "
                                  + e.getEventType() + "; exception is: "+ex);
                ex.printStackTrace(System.err);
            }
        }
    }

    private void init()
    {
        this.setBorder(new EmptyBorder(0,0,0,0));
        this.setBackground(Color.white);
        this.setContentType("text/html");
        this.setEditable(false);
    }

	/**
	 *  set the HTML content
	 *
     *  @param  the HTML String defining the content to be displayed
	 */
	public void setHTML(String html)
	{
        Log.debug(50, "\nHTMLPanel.setHTML() received HTML: \n"+html+"\n");
        html = stripHTMLMetaTags(html);
        this.setText(html);
	}
    
    //strips <META ... > tags out of html String 
	private String stripHTMLMetaTags(String html)
	{
        int META_END = 0;
        final int META_START = html.indexOf("<META");
        if (META_START>=0)  {  
            final char[] htmlChars = html.toCharArray();
            int charIndex = META_START;
            char nextChar = ' '; 
            do {
                nextChar = htmlChars[charIndex];
                htmlChars[charIndex] = ' ';
                charIndex++;
            } while ((nextChar!='>') && (charIndex < htmlChars.length));
            html = String.valueOf(htmlChars);
            return stripHTMLMetaTags(html);
	    }
        return html;
	}
}
