/**
 *  '$RCSfile: EML201DocumentCorrectorTest.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2008-09-25 22:56:13 $'
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
package edu.ucsb.nceas.morphotest.datastore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.Test;
import junit.framework.TestSuite;
import edu.ucsb.nceas.morpho.datastore.FileSystemDataStore;
import edu.ucsb.nceas.morpho.exception.IdentifierNotFoundException;
import edu.ucsb.nceas.morphotest.MorphoTestCase;


/**
 * A junit test for the FileSystemDataStore class.
 * @author tao
 *
 */
public class FileSystemDataStoreTest extends MorphoTestCase {
  
  private static final String objectStorePath1 = "build/tests";
  private static final String objectStorePath2 = "build/tmp";
  private static final String id1 = "tao.111";
  private static final String id2 = "tao.222";
  private static final String id3 = "jing.123";
  private static final String id4 = "jing.321";
  private static final String doi = "doi:10.6085/AA/YBHX00_XXXITBDXMMR01_20040720.50.5";
  private static final String filePath1 = "tests/testfiles/eml201-reference-system.xml";
  private static final String filePath2 = "tests/testfiles/eml201withadditionalMetacat.xml";
  /**
   * Create a suite of tests to be run together
   */
  public static Test suite() throws Exception {
      TestSuite suite = new TestSuite();
      suite.addTest(new FileSystemDataStoreTest("initialize"));
      suite.addTest(new FileSystemDataStoreTest("testSet"));
      suite.addTest(new FileSystemDataStoreTest("testGet"));
      return suite;
  }
  
  /**
   * Constructor a test
   * @param name the name of test
   */
  public FileSystemDataStoreTest(String name) {
    super(name);
  }
  
  /**
   * Check that the testing framework is functioning properly with 
   * a trivial assertion.
   */
  public void initialize() {
      assertTrue(true);
  }
  
  /**
   * Test set methods (including setDirectory and getDirectory methods)
   * @throws Exception
   */
  public void testSet() throws Exception {
    FileSystemDataStore fileStore = FileSystemDataStore.getInstance(objectStorePath1);
    fileStore.set(id1, new FileInputStream(new File(filePath1)));
    fileStore.set(id2, new FileInputStream(new File(filePath2)));
    fileStore.set(doi, new FileInputStream(new File(filePath2)));
    assertTrue("FileSystemDataStore.testSet - the directory should be "+objectStorePath1+ 
        " rather than "+fileStore.getDirectory(), fileStore.getDirectory().endsWith(objectStorePath1));

    // switch to different directory
    fileStore = FileSystemDataStore.getInstance(objectStorePath2);
    fileStore.set(id3, new FileInputStream(new File(filePath1)));
    fileStore.set(id4, new FileInputStream(new File(filePath2)));
    assertTrue("FileSystemDataStore.testSet - the directory should be "+objectStorePath2+ 
        " rather than "+fileStore.getDirectory(), fileStore.getDirectory().endsWith(objectStorePath2));
  }
  
  /**
   * Test get method
   * @throws Exception
   */
  public void testGet() throws Exception {
    FileSystemDataStore fileStore = FileSystemDataStore.getInstance(objectStorePath1);
    File inFile = fileStore.get(id1);
    InputStream in = new FileInputStream(inFile);
    File f1 = File.createTempFile("test", null);
    FileOutputStream output = new FileOutputStream(f1);
    byte[] array = new byte[8*1024];
    int index = -1;
    while ((index = in.read(array)) != -1) {
      output.write(array, 0, index);
    }
    in.close();
    output.close();
    assertTrue("The file "+f1.getAbsolutePath()+" should have the same size of file "+filePath1, 
        f1.length() == (new File(filePath1).length()));
    
    
    File inDOIFile = fileStore.get(doi);
    InputStream inDOI = new FileInputStream(inDOIFile);
    File temp = File.createTempFile("test", null);
    FileOutputStream outputDOI = new FileOutputStream(temp);
    byte[] arrayDOI = new byte[8*1024];
    while ((index = inDOI.read(arrayDOI)) != -1) {
      outputDOI.write(arrayDOI, 0, index);
    }
    inDOI.close();
    outputDOI.close();
    assertTrue("The file "+temp.getAbsolutePath()+" should have the same size of file "+filePath2, 
        temp.length() == (new File(filePath2).length()));
    
    
    // switch directory
    fileStore = FileSystemDataStore.getInstance(objectStorePath2);
    boolean inException = false;
    try {
        File inputFile = fileStore.get(id2);
    	InputStream input = new FileInputStream(inputFile);
    } catch(IdentifierNotFoundException e) {
      inException = true;
    }
    assertTrue("FileSystemDataStore.testGet - we shouldn't find the id "+id2+
        " in the store since the store location has been changed.", inException);
    
    File in2File = fileStore.get(id4);
    InputStream in2 = new FileInputStream(in2File);

    File f2 = File.createTempFile("test", null);
    FileOutputStream output2 = new FileOutputStream(f2);
    byte[] array2 = new byte[8*1024];
    int index2 = -1;
    while ((index2 = in2.read(array2)) != -1) {
      output2.write(array2, 0, index2);
    }
    in2.close();
    output2.close();
    assertTrue("The file "+f2.getAbsolutePath()+" should have the same size of file "+filePath2, 
        f2.length() == (new File(filePath2).length()));
    
  }
  

}
