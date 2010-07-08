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

package edu.tesc.scidb.morpho.plugins;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.w3c.dom.Node;

import edu.tesc.scidb.MetadataChecker.CreateTdmRep;
import edu.tesc.scidb.databank.sms.tdm.Database;
import edu.tesc.scidb.databank.sms.tdm.Table;
import edu.ucsb.nceas.morpho.Language;
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.DataPackagePlugin;
import edu.ucsb.nceas.morpho.datapackage.DataViewContainerPanel;
import edu.ucsb.nceas.morpho.datapackage.Entity;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.PluginInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceExistsException;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;

public class ImportMSAccessPlugin
	implements PluginInterface, ServiceProvider, Command {
		
	private MorphoFrame morphoFrame = null;
	private Morpho myMorpho;
	private DataViewContainerPanel resultPane;
	
	private AbstractDataPackage adp;
	private Database accessDatabase;

	/** 
	 * Entry point for the plugin.
	 * 
	 * @param event		The event that called execute
	 * @see edu.ucsb.nceas.morpho.util.Command#execute(java.awt.event.ActionEvent)
	 */
	public void execute(ActionEvent event) {
		Log.debug(11, "ImportAccess: Importing an MS Access Database design");

		adp = UIController.getInstance().getCurrentAbstractDataPackage();

		if (pickDatabase()) {

		  try {
			insertDatabase();
			this.reopenPackage();			
		  } catch (Exception w) {
			Log.debug(20, "Exception trying to import an MS Access Database!");
		  }
		}
	  }

	/*
	 *  This method lets the user choose an access database and the creates it's TDM representation
	 */
	private boolean pickDatabase()
		{
				//	open an access database and generate the tdm representation
				JFileChooser chooser = new JFileChooser();
				chooser.addChoosableFileFilter(new MdbFilter());
				chooser.showOpenDialog(morphoFrame);
			    if(chooser.getSelectedFile() == null)
			    	return false;
	
				// Create a TDM representation of this access database
				accessDatabase = CreateTdmRep.getRep(chooser.getSelectedFile());
				
				if(accessDatabase != null)
					return true;
				else 
					return false;
		}
		
		
	/*
	 *  This method takes the TDM database and loads it into the datapackage 
	 */
	private void insertDatabase()
	{
			 //	iterate over the tables and add them all
			 Collection tables = accessDatabase.getTables();
			 Iterator iter = tables.iterator();
	
			 while (iter.hasNext())
			 {
			   	// get the next database table from the collection
			    Table table = (Table) iter.next();
	
				// get the DOM node for this table
				Node dataTableNode = table.getDataTableNode();
				
				// create a Morpho Entity with the new dataTable node
				Entity dataTableEntity = new Entity(dataTableNode);
				
				// add the entity to the dataPackage
				adp.addEntity(dataTableEntity);
			 }
		
	}
	 
	
	/*
	 * This method reopens the datapackage
	 * because of a little bug in morpho I can't use UIContoller.showNewPackage()
	 * so I have to do it this way...
	 * see http://bugzilla.ecoinformatics.org/show_bug.cgi?id=1715
	 */
	private void reopenPackage()
	{
		adp.setLocation("");

		MorphoFrame morphoFrame = UIController.getInstance().getCurrentActiveWindow();
		Point pos = morphoFrame.getLocation();
		Dimension size = morphoFrame.getSize();

		try {
		  ServiceController services = ServiceController.getInstance();
		  ServiceProvider provider =
					services.getServiceProvider(DataPackageInterface.class);
		  DataPackageInterface dataPackage = (DataPackageInterface)provider;
		  //dataPackage.openHiddenNewDataPackage(adp, null);
		  morphoFrame.setVisible(false);
		  dataPackage.openNewDataPackage(adp, null);
		  UIController controller = UIController.getInstance();
		  MorphoFrame newMorphoFrame = controller.getCurrentActiveWindow();
		  newMorphoFrame.setLocation(pos);
		  newMorphoFrame.setSize(size);
		  newMorphoFrame.setVisible(true);
		  
		  controller.removeWindow(morphoFrame);
		  morphoFrame.dispose();
		}
		catch (ServiceNotHandledException snhe) {
		  Log.debug(6, snhe.getMessage());
		  morphoFrame.setVisible(true);
		}
	  }
		
	
	/**
	 * Called when the plugin is created
	 * @param morpho	The running morpho instance
	 * @see edu.ucsb.nceas.morpho.plugins.PluginInterface#initialize(edu.ucsb.nceas.morpho.Morpho)
	 */
	public void initialize(Morpho morpho) {


		myMorpho = morpho;

		try {
			ServiceController services = ServiceController.getInstance();
			services.addService(ImportMSAccessPlugin.class, this);
			Log.debug(20, "Service added: ImportMSAccessPlugin.");

		} catch (ServiceExistsException see) {
			Log.debug(6, "Service registration failed: ImportMSAccessPlugin");
			Log.debug(6, see.toString());
		}

		GUIAction ImportMSAccessDesign =
			new GUIAction(
				Language.getInstance().getMessage("ImportMSAccessDatabase") + Language.getInstance().getMessage("..."),
				null,
				new ImportMSAccessPlugin());
		ImportMSAccessDesign.setToolTipText(
			"Imports a Microsoft Access database design");
		// I figure a position of 100 will always place this at the bottom of the Data Menu.
		ImportMSAccessDesign.setSeparatorPosition(Morpho.SEPARATOR_PRECEDING);
		ImportMSAccessDesign.setMenuItemPosition(100);
		ImportMSAccessDesign.setMenu(DataPackagePlugin.DATA_MENU_LABEL, DataPackagePlugin.DATAMENUPOSITION);
		ImportMSAccessDesign.setEnabled(false);

		// if they are NOT using windows don't enable the plugin
		String OS = System.getProperty("os.name").toLowerCase();
		if(OS.indexOf("windows") <= -1) {
			// do nothing to enable
		} else {
			// enable it for data packages
			ImportMSAccessDesign.setEnabledOnStateChange(
				StateChangeEvent.CREATE_DATAPACKAGE_FRAME,
				true,
				GUIAction.EVENT_LOCAL);
	
			ImportMSAccessDesign.setEnabledOnStateChange(
				StateChangeEvent.CREATE_SEARCH_RESULT_FRAME,
				false,
				GUIAction.EVENT_LOCAL);
		}

		UIController controller = UIController.getInstance();

		controller.addGuiAction(ImportMSAccessDesign);

	}

	/**
	 *  MdbFilter.java filters MDB files
	 */
	public class MdbFilter extends FileFilter {

		//Accept all directories and all mdb files
		public boolean accept(File f) {
			if (f.isDirectory()) {
				return true;
			}

			String filename = f.getName();
			int lastPeriodIndex = filename.lastIndexOf(".");
			if (lastPeriodIndex > 0) {
				String ext = filename.substring(lastPeriodIndex);
				if (ext.equalsIgnoreCase(".MDB"))
					return true;
			}

			return false;
		}

		//The description of this filter
		public String getDescription() {
			return "Microsoft Access Database (*.mdb)";
		}
	}

}
