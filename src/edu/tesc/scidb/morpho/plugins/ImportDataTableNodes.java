/*
 * Created on Jul 23, 2004
 *
 * A import plugin that allows importing of datatable nodes from an existing EML document
 * 
 */
package edu.tesc.scidb.morpho.plugins;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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

/**
 * @author fickerm
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ImportDataTableNodes implements PluginInterface, ServiceProvider, Command {

	/* (non-Javadoc)
	 * @see edu.ucsb.nceas.morpho.plugins.PluginInterface#initialize(edu.ucsb.nceas.morpho.Morpho)
	 */

	private Morpho myMorpho;

	/* Referrence to  morphoframe */
	private MorphoFrame morphoFrame = null;
	private DataViewContainerPanel resultPane;
	
	public void initialize(Morpho morpho) {

		myMorpho = morpho;

		try {
			ServiceController services = ServiceController.getInstance();
			services.addService(ImportDataTableNodes.class, this);
			Log.debug(20, "Service added: ImportDataTableNodes.");

		} catch (ServiceExistsException see) {
			Log.debug(6, "Service registration failed: ImportDataTableNodes");
			Log.debug(6, see.toString());
		}

		GUIAction ImportDataTableNodesAction =
			new GUIAction(
				"Import Data Table Nodes",
				null,
				new ImportDataTableNodes());
		ImportDataTableNodesAction.setToolTipText(
			"Imports the data table descriptions from an existing EML document");
		ImportDataTableNodesAction.setMenuItemPosition(0);
		ImportDataTableNodesAction.setMenu(DataPackagePlugin.DATA_MENU_LABEL, DataPackagePlugin.DATAMENUPOSITION);
		ImportDataTableNodesAction.setEnabled(false);
		ImportDataTableNodesAction.setEnabledOnStateChange(
			StateChangeEvent.CREATE_DATAPACKAGE_FRAME,
			true,
			GUIAction.EVENT_LOCAL);
		ImportDataTableNodesAction.setEnabledOnStateChange(
			StateChangeEvent.CREATE_SEARCH_RESULT_FRAME,
			false,
			GUIAction.EVENT_LOCAL);
		UIController controller = UIController.getInstance();
		controller.addGuiAction(ImportDataTableNodesAction);

	}
	public static void main(String[] args) {
	}
	/* (non-Javadoc)
	 * @see edu.ucsb.nceas.morpho.util.Command#execute(java.awt.event.ActionEvent)
	 * 
	 * 
	 *
	 *
	 */
	public void execute(ActionEvent event) {
		DataPackageInterface dataPackageInterface;

		try {
			ServiceController services = ServiceController.getInstance();
			ServiceProvider provider =
				services.getServiceProvider(DataPackageInterface.class);
			dataPackageInterface = (DataPackageInterface) provider;
		} catch (ServiceNotHandledException snhe) {
			Log.debug(6, "Error in ImportDataTableNodes...");
			return;
		}

		resultPane = null;
		morphoFrame = UIController.getInstance().getCurrentActiveWindow();
		if (morphoFrame != null) {
			resultPane = morphoFrame.getDataViewContainerPanel();
		} //if

		// make sure resulPanel is not null
		if (resultPane != null) {
			//dataView = resultPane.getCurrentDataViewer();
			AbstractDataPackage pkg = resultPane.getAbstractDataPackage();
			Node metadataNode = pkg.getMetadataNode();

			// open an EML document and strip out the datatableNodes
			JFileChooser chooser = new JFileChooser();
			

			chooser.showOpenDialog(morphoFrame);
			chooser.getSelectedFile();
			
			// strip out the datatable nodes...
			
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			try {
				DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
				Document emlDoc = documentBuilder.parse(chooser.getSelectedFile());
				NodeList dataTableNodes = emlDoc.getElementsByTagName("dataTable");
				
				for(int i=0; i<dataTableNodes.getLength(); i++)
					{
						Entity dataTableEntity = new Entity(dataTableNodes.item(i), pkg);
						pkg.addEntity(dataTableEntity);
					}
				
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}





			// reopen the datapackage			
			MorphoFrame morphoFrame = UIController.getInstance().getCurrentActiveWindow();
			Point pos = morphoFrame.getLocation();
			Dimension size = morphoFrame.getSize();
			
			dataPackageInterface.openNewDataPackage(pkg,null);
			
			MorphoFrame newMorphoFrame = UIController.getInstance().getCurrentActiveWindow();
			newMorphoFrame.setLocation(pos);
			newMorphoFrame.setSize(size);
			newMorphoFrame.setVisible(true);
			
			UIController.getInstance().removeWindow(morphoFrame);
			morphoFrame.dispose();
			
			
			
	

		}

	}

}
