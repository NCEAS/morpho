/**
 * A plugin that imports the design of a Microsoft Access Database.
 * A dataTable node is added for each table found in the database.
 * Imports tables, attributes, attribute descriptions, primary keys and
 * notNullable constraints.
 * 
 * The plugin does not yet import forien keys.
 * 
 * @author Michael Finch
 * @version 1.0
 *
 */

package org.ecoinformatics.sms.plugins;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datapackage.DataPackagePlugin;
import edu.ucsb.nceas.morpho.datapackage.DataViewContainerPanel;
import edu.ucsb.nceas.morpho.datapackage.DataViewer;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.PluginInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceExistsException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;
import edu.ucsb.nceas.morpho.util.StateChangeListener;
import edu.ucsb.nceas.morpho.util.StateChangeMonitor;

public class AnnotationPlugin
	implements PluginInterface, ServiceProvider, StateChangeListener {
		
	private MorphoFrame morphoFrame = null;
	
	private GUIAction annotateAction = null;
	

	/**
	 * Called when the plugin is created
	 * @param morpho	The running morpho instance
	 * @see edu.ucsb.nceas.morpho.plugins.PluginInterface#initialize(edu.ucsb.nceas.morpho.Morpho)
	 */
	public void initialize(Morpho morpho) {

		try {
			ServiceController services = ServiceController.getInstance();
			services.addService(AnnotationPlugin.class, this);
			Log.debug(20, "Service added: " + this.getClass().getName());

		} catch (ServiceExistsException see) {
			Log.debug(6, "Service registration failed: " + this.getClass().getName());
			Log.debug(6, see.toString());
		}

		annotateAction =
			new GUIAction(
				"Annotate current column...",
				null,
				new AnnotationCommand());
		annotateAction.setToolTipText(
			"Add/edit annotation or this data table attribute");
		// I figure a position of 100 will always place this at the bottom of the Data Menu.
		annotateAction.setSeparatorPosition(Morpho.SEPARATOR_PRECEDING);
		annotateAction.setMenuItemPosition(100);
		annotateAction.setMenu(DataPackagePlugin.DATA_MENU_LABEL, DataPackagePlugin.DATAMENUPOSITION);
		annotateAction.setEnabled(false);

		annotateAction.setEnabledOnStateChange(
				StateChangeEvent.SELECT_DATATABLE_COLUMN,
				true,
				GUIAction.EVENT_LOCAL);
		annotateAction.setEnabledOnStateChange(
                StateChangeEvent.CREATE_ENTITY_DATAPACKAGE_FRAME,
                true, GUIAction.EVENT_LOCAL);
		annotateAction.setEnabledOnStateChange(
                StateChangeEvent.CREATE_SEARCH_RESULT_FRAME,
                false, GUIAction.EVENT_LOCAL);
		annotateAction.setEnabledOnStateChange(
                StateChangeEvent.CREATE_NOENTITY_DATAPACKAGE_FRAME,
                false, GUIAction.EVENT_LOCAL);

		UIController controller = UIController.getInstance();
		controller.addGuiAction(annotateAction);

		//register as a listener for data frame opening
		StateChangeMonitor.getInstance().addStateChangeListener(StateChangeEvent.CREATE_ENTITY_DATAPACKAGE_FRAME, this);
	}

	public void handleStateChange(StateChangeEvent event) {

		// alter the popup menu
		if (event.getChangedState().equals(
				StateChangeEvent.CREATE_ENTITY_DATAPACKAGE_FRAME)) {
			morphoFrame = UIController.getInstance().getCurrentActiveWindow();

			if (morphoFrame != null) {
				DataViewContainerPanel resultPane = morphoFrame
						.getDataViewContainerPanel();

				if (resultPane != null) {
					DataViewer dataView = resultPane.getCurrentDataViewer();
					if (dataView != null) {
						annotateAction.setEnabled(true);
						dataView.addPopupMenuItem(annotateAction);
					}
				}
			}

		}

	}

}
