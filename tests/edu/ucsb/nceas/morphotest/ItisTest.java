/**
 *  '$RCSfile: ItisTest.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-10-05 00:09:29 $'
 * '$Revision: 1.1 $'
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

package edu.ucsb.nceas.morphotest;

import edu.ucsb.nceas.itis.Itis;
import edu.ucsb.nceas.itis.ItisException;
import edu.ucsb.nceas.itis.ItisInterface;
import edu.ucsb.nceas.itis.Taxon;
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.util.Log;

import java.io.FileNotFoundException;
import java.net.URLStreamHandler;
import java.util.Vector;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 * A JUnit test for testing if morpho get correct information from itis web.
 */
public class ItisTest extends TestCase
{
  // The instance of Itis
  private static Itis itis = null;
  
  // The species name of search
  private static String searchName = null;
  
  // Taxon serial number for this species
  private static long searchTsn = 0;
  private static Taxon taxon    = null;
  // Taxon serial number for parent
  private static long  parentTsn   = 0;
  private static Taxon parentTaxon = null;
  // Taxon serial number for synonym
  private static long   synonymTsn   = 0;
  private static Taxon  synonymTaxon = null;
  
  // Constant for result
  private long   SEARCHTSN   = 11350;
  private long   PARENTTSN   = 11347;
  private long   SYNONYMTSN  = 11351;
  private String SEARCHRANK  = "Species";
  private String PARENTRANK  = "Genus";
  private String SYNONYMRNAK = "Species";
  private String SEARCHNAME  = "Pelvetia canaliculata";
  private String PARENTNAME  = "Pelvetia";
  private String SYNONYMNAME = "Fucus canaliculatus";

  /**
   * Constructor to build the test
   *
   * @param name the name of the test method
   */
  public ItisTest(String name)
  {
    super(name);
  }

  /**
   * Establish a testing framework by initializing appropriate objects
   */
  public void setUp()
  {
    itis  = new Itis();
    searchName = "Pelvetia+canaliculata";
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
    suite.addTest(new ItisTest("initialize"));
    suite.addTest(new ItisTest("findTaxonTsnTest"));
    suite.addTest(new ItisTest("getScientificNameTest"));
    suite.addTest(new ItisTest("getTaxonRankTest"));
    suite.addTest(new ItisTest("findParentTaxonTsnTest"));
    suite.addTest(new ItisTest("getParentScientificNameTest"));
    suite.addTest(new ItisTest("getParentTaxonRankTest"));
    suite.addTest(new ItisTest("findSynonymTaxonTsnTest"));
    suite.addTest(new ItisTest("getSynoymScientificNameTest"));
    suite.addTest(new ItisTest("getSynonymTaxonRankTest"));
    return suite;
  }

  /**
   * Check that the testing itis is functioning properly with 
   * a trivial assertion.
   */
  public void initialize()
  {
      assertTrue(true);
  }
  
  /**
   * Test method to get the tsn (taxonomic serial number).
   */
  public void findTaxonTsnTest()
  {
    try
    {
     
      searchTsn = itis.findTaxonTsn(searchName);
      System.out.println("search tsn is: "+searchTsn);
    }
    catch (ItisException ie) 
    {
      Log.debug(20, ie.getMessage());
    }
    assertTrue(searchTsn == SEARCHTSN);
  }
  
  /**
   * Test method to get scientific name for this search name
   */ 
  public void getScientificNameTest()
  {
    try
    {
      System.out.println("searchTsn in getScientificNameTest: "+searchTsn);
      taxon = itis.getTaxon(searchTsn);
    }
    catch (ItisException ie) 
    {
      Log.debug(20, ie.getMessage());
    }
    String scientificName = taxon.getScientificName();
    assertTrue(scientificName.equals(SEARCHNAME));
  }
  
  /**
   * Test method to get the taxon rank
   */
  public void getTaxonRankTest()
  {
    String rank = taxon.getTaxonRank();
    assertTrue(rank.equals(SEARCHRANK));
  }
  
  /**
   * Test method to get the parent tsn (taxonomic serial number).
   */
  public void findParentTaxonTsnTest()
  {
    parentTsn = taxon.getParentTsn();
    assertTrue(parentTsn == PARENTTSN);
  }
  
  /**
   * Test method to get parent's scientific name for this search name
   */ 
  public void getParentScientificNameTest()
  {
    try
    {
      parentTaxon = itis.getTaxon(parentTsn);
    }
    catch (ItisException ie) 
    {
      Log.debug(20, ie.getMessage());
    }
    String scientificName = parentTaxon.getScientificName();
    assertTrue(scientificName.equals(PARENTNAME));
  }
  
  /**
   * Test method to get the parent taxon rank
   */
  public void getParentTaxonRankTest()
  {
    String rank = parentTaxon.getTaxonRank();
    assertTrue(rank.equals(PARENTRANK));
  }
  
  /**
   * Test method to get the synonym tsn (taxonomic serial number).
   */
  public void findSynonymTaxonTsnTest()
  {
    Vector synonyms = null;
    try
    {
      synonyms = itis.getSynonymTsnList(searchTsn);
    }
    catch (ItisException ie) 
    {
      Log.debug(20, ie.getMessage());
    }
    synonymTsn = ((Long)synonyms.get(0)).longValue();
    assertTrue(synonymTsn == SYNONYMTSN);
  }
  
  /**
   * Test method to get synonym scientific name for this search name
   */ 
  public void getSynoymScientificNameTest()
  {
    try
    {
      synonymTaxon = itis.getTaxon(synonymTsn);
    }
    catch (ItisException ie) 
    {
      Log.debug(20, ie.getMessage());
    }
    String scientificName = synonymTaxon.getScientificName();
    assertTrue(scientificName.equals(SYNONYMNAME));
  }
  
  /**
   * Test method to get the synonym taxon rank
   */
  public void getSynonymTaxonRankTest()
  {
    String rank = synonymTaxon.getTaxonRank();
    assertTrue(rank.equals(SYNONYMRNAK));
  }
  
 

}
