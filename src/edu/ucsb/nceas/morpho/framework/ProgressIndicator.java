/**
 *  '$RCSfile: ProgressIndicator.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2002-08-13 21:35:54 $'
 * '$Revision: 1.1.2.2 $'
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

package edu.ucsb.nceas.morpho.framework;

import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * The ProgressIndicator extends JLabel to display a progress image.  It
 * can have two states, BUSY or NOTBUSY.  When its state is "BUSY", it generally
 * displays an animated image.  Otherwise, a static image is displayed.
 *
 * @author   jones
 */
public class ProgressIndicator extends JPanel
{
    private static final int BORDER_WIDTH = 4;
    
    private ImageIcon busyIcon;
    private ImageIcon notBusyIcon;
    private JLabel imageLabel;  
    
    /**
     * Creates a new instance of ProgressIndicator
     */
    public ProgressIndicator(ImageIcon notBusyIcon, ImageIcon busyIcon)
    {
        super();
        this.notBusyIcon = notBusyIcon;
        this.busyIcon = busyIcon;
        
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        //setBackground(Color.red);
        add(Box.createHorizontalStrut(BORDER_WIDTH));
        
        Box verticalBox = Box.createVerticalBox();
        add(verticalBox);
        verticalBox.add(Box.createVerticalStrut(BORDER_WIDTH));
        
        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
        innerPanel.setBackground(Color.white);
        verticalBox.add(innerPanel);
        innerPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        
        imageLabel = new JLabel(notBusyIcon);
        innerPanel.add(imageLabel);
        
        verticalBox.add(Box.createVerticalStrut(BORDER_WIDTH));
        add(Box.createHorizontalStrut(BORDER_WIDTH));
        
        setBounds(100, 100,
                  (int)getPreferredSize().getWidth(),
                  (int)getPreferredSize().getHeight());
    }
    
    public void setBusy(boolean isBusy)
    {
        if (isBusy) {
            imageLabel.setIcon(busyIcon);
        } else {
            imageLabel.setIcon(notBusyIcon);
        }
    }
}

