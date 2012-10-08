package edu.ucsb.nceas.morphotest.datapackage;

import edu.ucsb.nceas.morpho.editor.EditorPlugin;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.datapackage.DataPackageFactory;
import edu.ucsb.nceas.morpho.datapackage.DataPackagePlugin;
import edu.ucsb.nceas.morpho.datapackage.EML200DataPackage;
import edu.ucsb.nceas.morpho.datapackage.EMLTransformToNewestVersionDialog;
import edu.ucsb.nceas.morpho.datapackage.ImportEMLFileCommand;
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
import edu.ucsb.nceas.morpho.plugins.xsltresolver.XSLTResolverPlugin;

import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;
import edu.ucsb.nceas.morpho.util.StateChangeMonitor;
import edu.ucsb.nceas.morpho.util.UISettings;

import java.io.File;
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
 * A junit test class for testing class ImportEMLFileCommand.
 * @author tao
 *
 */
public class ImportEMLFileCommandTest extends TestCase
{
   /**
     * Constructor to build the test
     *
     * @param name the name of the test method
     */
    public ImportEMLFileCommandTest(String name)
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
      suite.addTest(new ImportEMLFileCommandTest("initialize"));
      suite.addTest(new ImportEMLFileCommandTest("testExecute"));
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
     * Check if execute command works.
     */
    public void testExecute() throws IOException, Exception
    {
       Morpho.createMorphoTestInstance();
       UIController.initialize(Morpho.thisStaticInstance);
       ImportEMLFileCommand importEML = new ImportEMLFileCommand();
       importEML.execute(null);
        
    }
    


}