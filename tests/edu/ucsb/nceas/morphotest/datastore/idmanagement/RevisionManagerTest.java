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
import java.util.List;
import java.util.Vector;

import junit.framework.Test;
import junit.framework.TestSuite;
import edu.ucsb.nceas.morpho.datastore.DataStoreService;
import edu.ucsb.nceas.morpho.datastore.idmanagement.IdentifierFileMap;
import edu.ucsb.nceas.morpho.datastore.idmanagement.IdentifierManager;
import edu.ucsb.nceas.morpho.datastore.idmanagement.RevisionManager;
import edu.ucsb.nceas.morpho.exception.IdentifierNotFoundException;
import edu.ucsb.nceas.morphotest.MorphoTestCase;

public class RevisionManagerTest extends MorphoTestCase {
  
  private static final String PREFIX = "test";
  /**
   * Constructor to build the test
   *
   * @param name the name of the test method
   */
  public RevisionManagerTest(String name) {
      super(name);

  }
  /**
   * Create a suite of tests to be run together
   */
  public static Test suite() {
      TestSuite suite = new TestSuite();
      suite.addTest(new RevisionManagerTest("initialize"));
      suite.addTest(new RevisionManagerTest("testScatch"));
      suite.addTest(new RevisionManagerTest("testExistedConfigurationFile"));
      
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
   * Run the test when the configuration file doesn't exist.
   * @throws Exception
   */
  public void testScatch() throws Exception {
    RevisionManager manager = new RevisionManager(DataStoreService.getProfileDir(),PREFIX);
    manager.setObsoletedBy("tao.1.1", "tao.1.2");
    manager.setObsoletedBy("tao.1.2", "tao.1.3");
    manager.setObsoletes("tao.1.2", "tao.1.1");
    manager.setObsoletes("tao.1.3", "tao.1.2");
    manager.setObsoletedBy("tao.1.3", "tao.1.4");
    manager.setObsoletes("tao.1.4", "tao.1.3");
    
    manager.setObsoletedBy("tao.2.1", "tao.2.2");
    manager.setObsoletedBy("tao.2.2", "tao.2.3");
    manager.setObsoletes("tao.2.2", "tao.2.1");
    manager.setObsoletes("tao.2.3", "tao.2.2");
    manager.setObsoletes("tao.2.4", "tao.2.3");
    manager.setObsoletedBy("tao.2.3", "tao.2.4");
    
    manager.setObsoletedBy("jing.3.1", "jing.3.2");
    manager.setObsoletedBy("jing.3.2", "jing.3.3");
    manager.setObsoletes("jing.3.2", "jing.3.1");
    manager.setObsoletes("jing.3.3", "jing.3.2");
    manager.setObsoletes("jing.3.4", "jing.3.3");
    manager.setObsoletedBy("jing.3.3", "jing.3.4");
    
    String latest = manager.getLatestRevision("jing.3.4");
    assertTrue("the last version should be jing.3.4.", latest.equals("jing.3.4"));
    List<String> allVersions = manager.getAllRevisions("jing.3.4");
    //System.out.println("the size is "+allVersions.size());
    assertTrue("the size of the all versions for jing.3.4 should be 4.", allVersions.size()==4);
    for(int i=0; i< allVersions.size(); i++) {
      String version = allVersions.get(i);
      //System.out.println("the version is "+version);
      if(i==0) {
        assertTrue("the first element at the version list should be jing.3.4", version.equals("jing.3.4"));
      } else if(i==1) {
        assertTrue("the second element at the version list should be jing.3.3", version.equals("jing.3.3"));
      } else if(i==2) {
        assertTrue("the third element at the version list should be jing.3.2", version.equals("jing.3.2"));
      } else if(i==3) {
        assertTrue("the forth element at the version list should be jing.3.1", version.equals("jing.3.1"));
      }
    }   
    String obsoletedBy = manager.getObsoletedBy("jing.3.4");
    assertTrue("The obsoletedBy for jing.3.4 should be null.", obsoletedBy== null);
    String obsoletes = manager.getObsoletes("jing.3.4");
    assertTrue("The obsoletes for jing.3.4 should be jing.3.3.", obsoletes.equals("jing.3.3"));
    
    
    String latest2 = manager.getLatestRevision("tao.1.1");
    assertTrue("the last version should be tao.1.4.", latest2.equals("tao.1.4"));
    List<String> allVersions2 = manager.getAllRevisions("tao.1.1");
    //System.out.println("the size is "+allVersions.size());
    assertTrue("the size of the all versions for tao.1.1 should be 4.", allVersions2.size()==4);
    for(int i=0; i< allVersions2.size(); i++) {
      String version = allVersions2.get(i);
      //System.out.println("the version is "+version);
      if(i==0) {
        assertTrue("the first element at the version list should be tao.1.4", version.equals("tao.1.4"));
      } else if(i==1) {
        assertTrue("the second element at the version list should be tao.1.3", version.equals("tao.1.3"));
      } else if(i==2) {
        assertTrue("the third element at the version list should be tao.1.2", version.equals("tao.1.2"));
      } else if(i==3) {
        assertTrue("the forth element at the version list should be tao.1.1", version.equals("tao.1.1"));
      }
    }   
    String obsoletedBy2 = manager.getObsoletedBy("tao.1.1");
    assertTrue("The obsoletedBy for tao.1.1 should be tao.1.2.", obsoletedBy2.equals("tao.1.2"));
    String obsoletes2 = manager.getObsoletes("tao.1.1");
    assertTrue("The obsoletes for tao.1.1 should be null.", obsoletes2==null);
    
    
    String latest3 = manager.getLatestRevision("tao.2.2");
    assertTrue("the last version should be tao.2.4.", latest3.equals("tao.2.4"));
    List<String> allVersions3 = manager.getAllRevisions("tao.2.2");
    //System.out.println("the size is "+allVersions.size());
    assertTrue("the size of the all versions for tao.2.2 should be 4.", allVersions3.size()==4);
    for(int i=0; i< allVersions3.size(); i++) {
      String version = allVersions3.get(i);
      //System.out.println("the version is "+version);
      if(i==0) {
        assertTrue("the first element at the version list should be tao.2.4", version.equals("tao.2.4"));
      } else if(i==1) {
        assertTrue("the second element at the version list should be tao.2.3", version.equals("tao.2.3"));
      } else if(i==2) {
        assertTrue("the third element at the version list should be tao.2.2", version.equals("tao.2.2"));
      } else if(i==3) {
        assertTrue("the forth element at the version list should be tao.2.1", version.equals("tao.2.1"));
      }
    }   
    String obsoletedBy3 = manager.getObsoletedBy("tao.2.2");
    assertTrue("The obsoletedBy for tao.2.2 should be tao.2.3.", obsoletedBy3.equals("tao.2.3"));
    String obsoletes3 = manager.getObsoletes("tao.2.2");
    assertTrue("The obsoletes for tao.2.2 should be tao.2.1.", obsoletes3.equals("tao.2.1"));
    
  }
  
  /**
   * Run test against an existed configuration file.
   * @throws Exception
   */
  public void testExistedConfigurationFile() throws Exception {
    RevisionManager manager = new RevisionManager(DataStoreService.getProfileDir(),PREFIX);
    manager.setObsoletedBy("tao.3.1", "tao.3.2");
    manager.setObsoletedBy("tao.3.2", "tao.3.3");
    manager.setObsoletes("tao.3.2", "tao.3.1");
    manager.setObsoletes("tao.3.3", "tao.3.2");
    manager.setObsoletedBy("tao.3.3", "tao.3.4");
    manager.setObsoletes("tao.3.4", "tao.3.3");
    
    manager.setObsoletes("jing.3.5", "jing.3.4");
    manager.setObsoletedBy("jing.3.4", "jing.3.5");
    manager.setObsoletes("jing.3.6", "jing.3.5");
    manager.setObsoletedBy("jing.3.5", "jing.3.6");
    
    String latest = manager.getLatestRevision("jing.3.6");
    assertTrue("the last version should be jing.3.6.", latest.equals("jing.3.6"));
    List<String> allVersions = manager.getAllRevisions("jing.3.6");
    //System.out.println("the size is "+allVersions.size());
    assertTrue("the size of the all versions for jing.3.6 should be 6.", allVersions.size()==6);
    for(int i=0; i< allVersions.size(); i++) {
      String version = allVersions.get(i);
      //System.out.println("the version is "+version);
      if(i==0) {
        assertTrue("the first element at the version list should be jing.3.6", version.equals("jing.3.6"));
      } else if(i==1) {
        assertTrue("the second element at the version list should be jing.3.5", version.equals("jing.3.5"));
      } else if(i==2) {
        assertTrue("the third element at the version list should be jing.3.4", version.equals("jing.3.4"));
      } else if(i==3) {
        assertTrue("the forth element at the version list should be jing.3.3", version.equals("jing.3.3"));
      } else if(i==4) {
        assertTrue("the fifth element at the version list should be jing.3.2", version.equals("jing.3.2"));
      } else if(i==5) {
        assertTrue("the sixth element at the version list should be jing.3.1", version.equals("jing.3.1"));
      } 
    }   
    String obsoletedBy = manager.getObsoletedBy("jing.3.6");
    assertTrue("The obsoletedBy for jing.3.6 should be null.", obsoletedBy== null);
    String obsoletes = manager.getObsoletes("jing.3.6");
    assertTrue("The obsoletes for jing.3.6 should be jing.3.5.", obsoletes.equals("jing.3.5"));
    
    
    String latest2 = manager.getLatestRevision("tao.3.1");
    assertTrue("the last version should be tao.3.4.", latest2.equals("tao.3.4"));
    List<String> allVersions2 = manager.getAllRevisions("tao.3.1");
    //System.out.println("the size is "+allVersions.size());
    assertTrue("the size of the all versions for tao.3.1 should be 4.", allVersions2.size()==4);
    for(int i=0; i< allVersions2.size(); i++) {
      String version = allVersions2.get(i);
      //System.out.println("the version is "+version);
      if(i==0) {
        assertTrue("the first element at the version list should be tao.3.4", version.equals("tao.3.4"));
      } else if(i==1) {
        assertTrue("the second element at the version list should be tao.3.3", version.equals("tao.3.3"));
      } else if(i==2) {
        assertTrue("the third element at the version list should be tao.3.2", version.equals("tao.3.2"));
      } else if(i==3) {
        assertTrue("the forth element at the version list should be tao.3.1", version.equals("tao.3.1"));
      }
    }   
    String obsoletedBy2 = manager.getObsoletedBy("tao.1.1");
    assertTrue("The obsoletedBy for tao.1.1 should be tao.1.2.", obsoletedBy2.equals("tao.1.2"));
    String obsoletes2 = manager.getObsoletes("tao.1.1");
    assertTrue("The obsoletes for tao.1.1 should be null.", obsoletes2==null);
    
    
    
    String latest3 = manager.getLatestRevision("tao.2.2");
    assertTrue("the last version should be tao.2.4.", latest3.equals("tao.2.4"));
    List<String> allVersions3 = manager.getAllRevisions("tao.2.2");
    //System.out.println("the size is "+allVersions.size());
    assertTrue("the size of the all versions for tao.2.2 should be 4.", allVersions3.size()==4);
    for(int i=0; i< allVersions3.size(); i++) {
      String version = allVersions3.get(i);
      //System.out.println("the version is "+version);
      if(i==0) {
        assertTrue("the first element at the version list should be tao.2.4", version.equals("tao.2.4"));
      } else if(i==1) {
        assertTrue("the second element at the version list should be tao.2.3", version.equals("tao.2.3"));
      } else if(i==2) {
        assertTrue("the third element at the version list should be tao.2.2", version.equals("tao.2.2"));
      } else if(i==3) {
        assertTrue("the forth element at the version list should be tao.2.1", version.equals("tao.2.1"));
      }
    }   
    String obsoletedBy3 = manager.getObsoletedBy("tao.2.2");
    assertTrue("The obsoletedBy for tao.2.2 should be tao.2.3.", obsoletedBy3.equals("tao.2.3"));
    String obsoletes3 = manager.getObsoletes("tao.2.2");
    assertTrue("The obsoletes for tao.2.2 should be tao.2.1.", obsoletes3.equals("tao.2.1"));
    File configurationFile = new File(DataStoreService.getProfileDir(), PREFIX+RevisionManager.SUFFIX);
    configurationFile.deleteOnExit();
  }

}
