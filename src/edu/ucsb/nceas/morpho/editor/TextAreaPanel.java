/**
 *       Name: TextAreaPanel.java
 *    Purpose: Example dynamic editor class for XMLPanel
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Dan Higgins
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2001-06-18 21:18:36 $'
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
package edu.ucsb.nceas.morpho.editor;

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;
import java.util.*;

public class TextAreaPanel extends JPanel
{  
    
  DefaultMutableTreeNode nd = null;  
    
  public TextAreaPanel(DefaultMutableTreeNode node) { 
        nd = node;
        JPanel jp = this;
        jp.setLayout(new BoxLayout(jp,BoxLayout.Y_AXIS));
        jp.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel jp1 = new JPanel();
        jp1.setLayout(new BoxLayout(jp1,BoxLayout.Y_AXIS));
        jp1.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel jp2 = new JPanel();
        jp2.setLayout(new BorderLayout(0,0));
        jp2.setAlignmentX(Component.LEFT_ALIGNMENT);
        jp1.setMaximumSize(new Dimension(300,30));
        jp2.setMaximumSize(new Dimension(300,200));
        jp2.setMinimumSize(new Dimension(300,200));
        jp2.setPreferredSize(new Dimension(300,200));
        JScrollPane jsp = new JScrollPane();
        jp2.add(BorderLayout.CENTER,jsp);
        jp.add(jp1);
        jp.add(jp2);
		    NodeInfo info = (NodeInfo)(nd.getUserObject());
        JLabel jl = new JLabel(info.name);
        jl.setForeground(java.awt.Color.black);
		    jl.setFont(new Font("Dialog", Font.PLAIN, 12));
		    jl.setVisible(true);
        jp1.add(jl);


        StringBuffer name = new StringBuffer();
        if (info.getHelp()!=null) {
            name.append(info.getHelp()); 
        }
        String helpString = name.toString();
        if (helpString.length()>0) {
          jl.setForeground(Color.black);
          jl.setText("<html><font size='-1'>"+helpString+"</html>");
        }
        //now check if there are child TEXT nodes
        Enumeration nodes = nd.children();
        // loop over child node
        String txt ="";
        while(nodes.hasMoreElements()) {
            DefaultMutableTreeNode nd1 = (DefaultMutableTreeNode)(nodes.nextElement());
		        NodeInfo info1 = (NodeInfo)(nd1.getUserObject());
		        if ((info1.name).equals("#PCDATA")) {
		          txt = info1.getPCValue();
            }
            if (txt.length()>0) {
                JTextArea jta = new JTextArea();
                jta.setLineWrap(true);
                jta.setWrapStyleWord(true);
                jsp.getViewport().add(jta);
                if (txt.equals("text")) { txt = " "; }
                jta.setText(txt);
            }
        }
    }
    
class dfhAction implements java.awt.event.ActionListener
{
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			Object object = event.getSource();
			if (object instanceof JTextField)
				{
		            NodeInfo info = (NodeInfo)(nd.getUserObject());
                    info.setPCValue(((JTextField)object).getText());
				}
		}
}

	class dfhFocus extends java.awt.event.FocusAdapter
	{
		public void focusLost(java.awt.event.FocusEvent event)
		{
			Object object = event.getSource();
			if (object instanceof JTextField)
				{
		        NodeInfo info = (NodeInfo)(nd.getUserObject());
                info.setPCValue(((JTextField)object).getText());
				}
		}
		
		public void focusGained(java.awt.event.FocusEvent event)
		{
			Object object = event.getSource();
			if (object instanceof JTextField)
				{
				}
		}
	}
    
}
