/**
 *  '$Id$'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
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

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.ucsb.nceas.morpho.Language;
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.util.Log;

public class OpenByIdDialog extends JDialog {

	// DECLARE_CONTROLS
	private JPanel controlPanel = null;
	private JButton cancelButton = null;
	private JButton openButton = null;
	private JPanel centerPanel = null;
	private JTextField packageId = null;
	private JPanel optionsPanel;
	private String location = "";

	public OpenByIdDialog(Frame parent) {
		super(parent);

		setTitle(Language.getInstance().getMessage("Open"));
		setSize(400, 120);
		setVisible(false);

		// panels
		controlPanel = WidgetFactory.makePanel();
		centerPanel = WidgetFactory.makePanel();
		optionsPanel = WidgetFactory.makePanel();

		// buttons
		SymAction lSymAction = new SymAction();
		cancelButton = WidgetFactory.makeJButton(Language.getInstance()
				.getMessage("Cancel"), lSymAction);
		openButton = WidgetFactory.makeJButton(Language.getInstance()
				.getMessage("Open"), lSymAction);

		// options
		final String[] options = 
			new String[] {
				Language.getInstance().getMessage("Local"), 
				Language.getInstance().getMessage("Network")};
		JPanel radioPanel = WidgetFactory.makeRadioPanel(options, 0, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String cmd = e.getActionCommand();
				if (cmd.equals(options[0])) {
					location = AbstractDataPackage.LOCAL;
				}
				if (cmd.equals(options[1])) {
					location = AbstractDataPackage.METACAT;
				}
			}
		});
		
		// input
		packageId = WidgetFactory.makeOneLineTextField();
		centerPanel.add(WidgetFactory.makeLabel(Language.getInstance()
				.getMessage("DocumentID"), false));
		centerPanel.add(packageId);
		
		optionsPanel.add(Box.createHorizontalGlue());
		optionsPanel.add(radioPanel);

		controlPanel.add(Box.createHorizontalGlue());
		controlPanel.add(cancelButton);
		controlPanel.add(openButton);
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(BorderLayout.NORTH, optionsPanel);
		getContentPane().add(BorderLayout.CENTER, centerPanel);
		getContentPane().add(BorderLayout.SOUTH, controlPanel);

	}

	class SymAction implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			Object object = event.getSource();
			if (object == cancelButton)
				CancelButton_actionPerformed(event);
			else if (object == openButton)
				OpenButton_actionPerformed(event);
		}
	}

	void CancelButton_actionPerformed(ActionEvent event) {
		this.setVisible(false);
		this.dispose();
	}

	void OpenButton_actionPerformed(ActionEvent event) {
		
		// check id
		String id = packageId.getText();
		if (id == null || id.length() == 0) {
			Log.debug(5, Language.getInstance().getMessage("InvalidId"));
			return;
		}
		
		// check that the id is formatted correctly
		try {
			AccessionNumber an = new AccessionNumber(Morpho.thisStaticInstance);
			an.getParts(id);
		} catch (Exception e) {
			Log.debug(5, Language.getInstance().getMessage("InvalidId"));
			return;
		}

		// open it
		DataPackageInterface dataPackage;
		try {
			ServiceController services = ServiceController.getInstance();
			ServiceProvider provider = services
					.getServiceProvider(DataPackageInterface.class);
			dataPackage = (DataPackageInterface) provider;
		} catch (ServiceNotHandledException snhe) {
			Log.debug(6, snhe.getMessage());
			return;
		}

		dataPackage.openDataPackage(location, id, null, null, null);
		this.setVisible(false);
		this.dispose();
	}
}
