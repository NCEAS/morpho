package edu.ucsb.nceas.morphotest.datapackage;

import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.datapackage.DataPackageFactory;
import edu.ucsb.nceas.morpho.datapackage.EML200DataPackage;
import edu.ucsb.nceas.morpho.datapackage.EMLTransformToNewestVersionDialog;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datastore.MetacatUploadException;
import edu.ucsb.nceas.morpho.framework.ButterflyFlapCoordinator;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.datapackage.DataViewContainerPanel;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.PluginInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceExistsException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;

import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;
import edu.ucsb.nceas.morpho.util.StateChangeMonitor;
import edu.ucsb.nceas.morpho.util.UISettings;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URLStreamHandler;
import java.util.Enumeration;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 * A junit test class for testing class EMLTransformToNewestVersionDialog.
 * @author tao
 *
 */
public class EMLTransformToNewestVersionDialogTest extends TestCase
{
	 /**
	   * Constructor to build the test
	   *
	   * @param name the name of the test method
	   */
	  public EMLTransformToNewestVersionDialogTest(String name)
	  {
	    super(name);
	  }

	  /**
	   * Establish a testing framework by initializing appropriate objects
	   */
	  public void setUp()
	  { 

	  }

	  /**
	   * Release any objects after tests are complete
	   */
	  public void tearDown()
	  {
	  }

	  /**
	   * Create a suite of tests to be run together
	   */
	  public static Test suite()
	  {
	    TestSuite suite = new TestSuite();
	    suite.addTest(new EMLTransformToNewestVersionDialogTest("initialize"));
	    //suite.addTest(new EMLTransformToNewestVersionDialogTest("testActions"));
	    return suite;
	  }
	  
	  /**
	   * Check that the testing framework is functioning properly with 
	   * a trivial assertion.
	   */
	  public void initialize()
	  {
	      assertTrue(true);
	  }
	  
	  /**
	   * Check the if the action buttons works.
	   */
	  public void testActions() throws IOException, Exception
	  {
		  
		    //create data package
		    FileReader reader = new FileReader("tests/testfiles/datos.200.xml");
		    StringBuffer buffer = new StringBuffer();
		    char [] array = new char[1024];
		    int index = reader.read(array);
		    while (index != -1)
		    {
		    	buffer.append(array, 0, index);
		    	index = reader.read(array);
		    }
		    String eml = buffer.toString();
		    EML200DataPackage adp = (EML200DataPackage)DataPackageFactory.getDataPackage(
                    new java.io.StringReader(eml), false, true);
		    
		    //display the data package in the window. 
		    long starttime = System.currentTimeMillis();
		    ConfigXML config = new ConfigXML("lib/config.xml");
		    Morpho morpho = new Morpho(config);
		    final MorphoFrame packageWindow = MorphoFrame.getInstance();
		    packageWindow.setBusy(true);
		    packageWindow.setVisible(true);


		    /*packageWindow.addWindowListener(
		                new WindowAdapter() {
		                public void windowActivated(WindowEvent e)
		                {
		                    Log.debug(50, "Processing window activated event");
		                    
		                      StateChangeMonitor.getInstance().notifyStateChange(
		                        new StateChangeEvent(packageWindow,
		                          StateChangeEvent.CLIPBOARD_HAS_NO_DATA_TO_PASTE));
		                
		                }
		            });*/


		    // Stop butterfly flapping for old window.
		    //packageWindow.setBusy(true);

		    long stoptime = System.currentTimeMillis();
		    Log.debug(20,"ViewContainer startUp time: "+(stoptime-starttime));

		    long starttime1 = System.currentTimeMillis();
		    DataViewContainerPanel dvcp = null;
		    dvcp = new DataViewContainerPanel(adp);
		    dvcp.setFramework(morpho);
		    dvcp.init();
		    long stoptime1 = System.currentTimeMillis();
		    Log.debug(20,"DVCP startUp time: "+(stoptime1-starttime1));

		    dvcp.setSize(packageWindow.getDefaultContentAreaSize());
		    dvcp.setPreferredSize(packageWindow.getDefaultContentAreaSize());
//		    dvcp.setVisible(true);
		    packageWindow.setMainContentPane(dvcp);

		    // Broadcast stored event int dvcp
		    dvcp.broadcastStoredStateChangeEvent();

		    // Create another events too
		    /*StateChangeMonitor monitor = StateChangeMonitor.getInstance();
		      // open a unsynchronize pakcage
		      monitor.notifyStateChange(
		                 new StateChangeEvent(
		                 dvcp,
		                 StateChangeEvent.CREATE_DATAPACKAGE_FRAME_UNSYNCHRONIZED));

		    // figure out whether there may be multiple versions, based on identifier
		    String identifier = adp.getAccessionNumber();
		    int lastDot = identifier.lastIndexOf(".");
		    String verNum = identifier.substring(lastDot+1,identifier.length());
		    if (verNum.equals("1")) {
		      monitor.notifyStateChange(
		                 new StateChangeEvent(
		                 dvcp,
		                 StateChangeEvent.CREATE_DATAPACKAGE_FRAME_NO_VERSIONS));
		    }
		    else {
		      monitor.notifyStateChange(
		                 new StateChangeEvent(
		                 dvcp,
		                 StateChangeEvent.CREATE_DATAPACKAGE_FRAME_VERSIONS));
		    }

		    monitor.notifyStateChange(
		                 new StateChangeEvent(
		                 dvcp,
		                 StateChangeEvent.CREATE_DATAPACKAGE_FRAME));*/
		  adp.loadCustomUnits();
		  packageWindow.setBusy(false);
		  EMLTransformToNewestVersionDialog dialog = new EMLTransformToNewestVersionDialog(packageWindow, null);
	  }


}
