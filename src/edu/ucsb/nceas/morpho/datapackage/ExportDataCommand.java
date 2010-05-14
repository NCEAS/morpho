/**
 *  '$RCSfile: DeleteTableCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author$'
 *     '$Date$'
 * '$Revision$'
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

package edu.ucsb.nceas.morpho.datapackage;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.util.Command;


/**
 * Class to handle export data file command
 */
public class ExportDataCommand implements Command {
	/** A reference to the MophorFrame */
	private MorphoFrame morphoFrame = null;

	/**
	 * Constructor
	 */
	public ExportDataCommand() {

	}

	/**
	 * export data file
	 * 
	 * @param event
	 *            ActionEvent
	 */
	public void execute(ActionEvent event) {
		DataViewContainerPanel resultPane = null;
		morphoFrame = UIController.getInstance().getCurrentActiveWindow();
		if (morphoFrame != null) {
			resultPane = morphoFrame.getDataViewContainerPanel();
		}
		
		// make sure resultPanel is not null
		if (resultPane != null) {

			final AbstractDataPackage adp = resultPane.getAbstractDataPackage();
			int entityIndex = resultPane.getLastTabSelected();
				
			// show the dialog
			String curdir = System.getProperty("user.dir");
			JFileChooser jfc = new JFileChooser(curdir);
			jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			jfc.setDialogTitle("Export data entity to directory");
			int response = jfc.showSaveDialog(morphoFrame);
			if (response == JFileChooser.APPROVE_OPTION) {
				File saveTarget = jfc.getSelectedFile();
				boolean success = adp.exportDataFiles(
						saveTarget.getAbsolutePath(), 
						new Integer(entityIndex));
				if (success) {
					JOptionPane.showMessageDialog(morphoFrame, "Export Complete");
				}
			}
		}

	}// execute

}
