/**
 *  '$RCSfile: HyperlinkButton.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-12-11 06:18:18 $'
 * '$Revision: 1.1 $'
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

package edu.ucsb.nceas.morpho.util;

import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.SwingConstants;

/**
 *  This class extends JButton to provide something that looks more like an 
 *  HTML-hyperlink 
 */
public class HyperlinkButton extends JButton{


    private GUIAction action;
    private String originalTextLabel;
    private StringBuffer buff = new StringBuffer(); 
    
    /**
     * Constructor
     *
     * @param guiAction the <CODE>GUIAction</CODE> object that provides this 
     *                  button's behavior.  Can set an Icon in the GUIAction,
     *                  which will be displayed to the left of the hyperlink.
     *                  The styling for the display text is done through HTML
     *                  @see UISettings.java
     */    
    public HyperlinkButton(GUIAction action) {
        super(action);
        this.action = action;
        originalTextLabel = action.getTextLabel();
        initButton();
        initRollover();
    }    

    private void initButton(){
    
        styleText();
        setFocusPainted(false); 
        setBorderPainted(false); 
        setContentAreaFilled(false); 
        setRolloverEnabled(true);
        setHorizontalAlignment(SwingConstants.LEFT);
        setMargin(new Insets(0,0,0,0));
    }
    
    private void initRollover(){
    
        styleRolloverText();
        setRolloverIcon(action.getRolloverSmallIcon());
        
        addMouseListener(new MouseAdapter() {
            
            String upText = null;
            
            public void mouseEntered(MouseEvent e) {
                upText = getText();
                setText(action.getRolloverTextLabel());
            }
            public void mouseExited(MouseEvent e) {
                setText(upText);
            }
            
        });
    }
    
    private void styleText(){
    
        buff.delete(0,buff.length());
        buff.append(UISettings.HYPERLINK_FONT_HTML_OPENTAGS);
        buff.append(originalTextLabel);
        buff.append(UISettings.HYPERLINK_FONT_HTML_CLOSETAGS);
        action.setTextLabel(buff.toString());
        this.setText(action.getTextLabel());
    }

    private void styleRolloverText(){

        buff.delete(0,buff.length());
        buff.append(UISettings.HYPERLINK_FONT_OVER_HTML_OPENTAGS);
        buff.append(originalTextLabel);
        buff.append(UISettings.HYPERLINK_FONT_OVER_HTML_CLOSETAGS);
        action.setRolloverTextLabel(buff.toString());
    }

}