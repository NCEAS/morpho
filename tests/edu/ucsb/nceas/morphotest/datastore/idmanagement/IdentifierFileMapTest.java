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
package edu.ucsb.nceas.morphotest.datastore.idmanagement;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestSuite;
import edu.ucsb.nceas.morpho.datastore.idmanagement.IdentifierFileMap;
import edu.ucsb.nceas.morpho.datastore.idmanagement.IdentifierManager;
import edu.ucsb.nceas.morpho.exception.IdentifierNotFoundException;
import edu.ucsb.nceas.morphotest.MorphoTestCase;

public class IdentifierFileMapTest extends MorphoTestCase {
  
  private static final String objectStorePath = "build/tests/testfiles";
  private static final String id1 = "10:0=@=10;0 /10\\0";
  private static final String id2 = "tao.10.1";
  private static final String id3 = "tmp.1.1";
  private static final String filePath1 = "build/tests/testfiles/eml201-reference-system.xml";
  private static final String filePath2 = "build/tests/testfiles/eml201withadditionalMetacat.xml";
  private static final String tmpDir = "build/tests/testfiles/tmp";
  private static final String tmpName ="1.1";
  private File file1 = null;
  private File file2 = null;
  private static File objectStore = null;
  
  /**
   * Constructor to build the test
   *
   * @param name the name of the test method
   */
  public IdentifierFileMapTest(String name) {
      super(name);
      objectStore = new File(objectStorePath);
      file1 = new File(filePath1);
      file2 = new File(filePath2);
  }
  /**
   * Create a suite of tests to be run together
   */
  public static Test suite() {
      TestSuite suite = new TestSuite();
      suite.addTest(new IdentifierFileMapTest("initialize"));
      suite.addTest(new IdentifierFileMapTest("testSet"));
      suite.addTest(new IdentifierFileMapTest("testGet"));
      suite.addTest(new IdentifierFileMapTest("testRemove"));
      
      return suite;
  }
  
  /**
   * Check that the testing framework is functioning properly with 
   * a trivial assertion.
   */
  public void initialize() {
      assertTrue(true);
  }
  
  /**
   * Test the set method
   */
  public void testSet() throws Exception {   
   IdentifierFileMap map = new IdentifierFileMap(objectStore);
   map.setMap(id1, file1);
   map.setMap(id2, file2);
   
   File tmp = new File(tmpDir);
   tmp.mkdir();
   File file = new File(tmpDir+"/"+tmpName);
   file.createNewFile();
   map.setMap(id3, file);
   
  }
  
  /**
   * Test the get method
   * @throws Exception
   */
  public void testGet() throws Exception {
    IdentifierFileMap map = new IdentifierFileMap(objectStore);
    File fileOne = map.getFile(id1);
    File file1 = new File(filePath1);
    System.out.println("the fileone path "+fileOne.getPath());
    assertTrue("The file associated with id "+id1+ " should be "+filePath1
        ,fileOne.getPath().equals(file1.getPath()));
    File fileTwo = map.getFile(id2);
    File file2 = new File(filePath2);
    System.out.println("the filetwo path "+fileTwo.getPath());
    assertTrue("The file associated with id "+id2+ " should be "+filePath2
        ,fileTwo.getPath().equals(file2.getPath()));
    File fileThree = map.getFile(id3);
    File file3 = new File(tmpDir+"/"+tmpName);
    System.out.println("the filethree path "+fileThree.getPath());
    assertTrue("The file associated with id "+id3+ " should end with "+tmpDir+"/"+tmpName
        ,fileThree.getPath().equals(file3.getPath()));
  }
  
  public void testRemove() throws Exception {
    IdentifierFileMap map = new IdentifierFileMap(objectStore);
    boolean reachException = false;
    map.remove(id2);
    try {
      File fileOne = map.getFile(id2);
    } catch (IdentifierNotFoundException e) {
      reachException = true;
    }
    assertTrue("id2 should be removed and an exception should be thrown.", reachException);
    reachException = false;
    IdentifierFileMap map2 = new IdentifierFileMap(objectStore);
    try {
      File fileOne = map2.getFile(id2);
    } catch (IdentifierNotFoundException e) {
      reachException = true;
    }
    assertTrue("id2 should be removed and an exception should be thrown.", reachException);
    map2.remove(id1);
    map2.remove(id3);
  }

}
