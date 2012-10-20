/**
 *  '$RCSfile: CorrectEML201DocsFrame.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2008-07-01 02:07:13 $'
 * '$Revision: 1.8 $'
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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.FileNotFoundException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JProgressBar;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datastore.idmanagement.update.IdentifierFileMapUpdater;
import edu.ucsb.nceas.morpho.datastore.idmanagement.update.RevisionUpdater;
import edu.ucsb.nceas.utilities.Log;

public class IdentifierUpdaterFrame extends JFrame{
  private static final String TITLE = "Generating the identifier-fileName mapping and the revision properties.";
  private static final String NEEDUPDATEPATH = "updateIdFileMap";
  
  /**
   * Constructor. Do nothing.
   */
  public IdentifierUpdaterFrame() {
    
  }
  
  /**
   * Do the update job
   */
  public void run() throws FileNotFoundException {
    String runUpdateFromConfig = Morpho.thisStaticInstance.getConfiguration().get(NEEDUPDATEPATH, 0);
    boolean runUpdate = false;
    try {
      runUpdate = (new Boolean(runUpdateFromConfig)).booleanValue();
    } catch (Exception e) {
      if(this != null) {
        this.dispose();
      }
      Log.debug(11, "IdentifierUpdaterFrame.update - the value for the path "+NEEDUPDATEPATH+" is "+runUpdateFromConfig+
      ". However, it should be either \"true\" or \"false\".");
    }
    //System.out.println("the configure value is "+runUpdate);
    if(runUpdate) {
      try {
        IdentifierFileMapUpdater updater = new IdentifierFileMapUpdater();
        RevisionUpdater revisionUpdater = new RevisionUpdater();
        boolean needUpdate = updater.needUpdate();
        //System.out.println("the profile value is "+needUpdate);
        if(needUpdate) {
          revisionUpdater.setProfileInformationList(updater.getProfileInformationList());
          loadGUI();
          updater.update();
          revisionUpdater.update();
          
        }
        
      } finally {
        if(this != null) {
          this.dispose();
        }
      }
      
    }
    
  }
  
  /*
   * Load GUI of this frame. The GUI is very simple: a label and a progress bar.
   */
  private void loadGUI()
  {
       this.setTitle(TITLE);
       setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
          getContentPane().setLayout(
                 new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
       getContentPane().setBackground(java.awt.Color.white);      
    
       getContentPane().add(Box.createVerticalStrut(8));
       javax.swing.JLabel loadingLabel = new javax.swing.JLabel();
       loadingLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
       loadingLabel.setText("Generating the id-file mapping and revision properties. It may take a while...");
       //loadingLabel.setForeground(java.awt.Color.red);
       loadingLabel.setFont(new Font("Dialog", Font.BOLD, 14));
       getContentPane().add(loadingLabel);
       getContentPane().add(Box.createVerticalStrut(8));
       JProgressBar progBar = new JProgressBar();
       progBar.setIndeterminate(true);
        getContentPane().add(progBar);        
        Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle frameDim = getBounds();
        setLocation((screenDim.width - frameDim.width) / 2,
                    (screenDim.height - frameDim.height) / 2);
        pack();
        setVisible(true);
  }

}
