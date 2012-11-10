/**  '$RCSfile: EML201DocumentCorrectorTest.java,v $'
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.dataone.client.D1Object;
import org.dataone.client.MNode;
import org.dataone.client.ObjectFormatCache;
import org.dataone.client.auth.CertificateManager;
import org.dataone.client.auth.ClientIdentityManager;
import org.dataone.ore.ResourceMapFactory;
import org.dataone.service.exceptions.BaseException;
import org.dataone.service.types.v1.AccessPolicy;
import org.dataone.service.types.v1.AccessRule;
import org.dataone.service.types.v1.Checksum;
import org.dataone.service.types.v1.Identifier;
import org.dataone.service.types.v1.Node;
import org.dataone.service.types.v1.NodeReference;
import org.dataone.service.types.v1.ObjectFormat;
import org.dataone.service.types.v1.ObjectFormatIdentifier;
import org.dataone.service.types.v1.Permission;
import org.dataone.service.types.v1.Subject;
import org.dataone.service.types.v1.SystemMetadata;
import org.dataone.service.types.v1.util.ChecksumUtil;
import org.dataone.service.util.Constants;

import junit.framework.Test;
import junit.framework.TestSuite;
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.DataPackageFactory;
import edu.ucsb.nceas.morpho.datapackage.Entity;
import edu.ucsb.nceas.morpho.datapackage.MorphoDataPackage;
import edu.ucsb.nceas.morpho.datastore.DataONEDataStoreService;
import edu.ucsb.nceas.morpho.datastore.idmanagement.IdentifierFileMap;
import edu.ucsb.nceas.morphotest.MorphoTestCase;

/**
 * A junit test class for DataStoreService
 * @author tao
 *
 */
public class DataONEDataStoreServiceTest extends MorphoTestCase {
  
  private static final String OREPATH = "tests/testfiles/ore-package/ore";
  private static final String EMLPATH = "tests/testfiles/ore-package/eml.xml";
  private static final String DATAPATH = "tests/testfiles/ore-package/data.txt";
  private static final String OREFORMATID = "http://www.openarchives.org/ore/terms";
  private static final String EMLFORMATID = "eml://ecoinformatics.org/eml-2.0.0";
  private static final String DATAFORMATID = "text/csv";
  private static final String DATAID = "data-identifier";
  private static final String OREIDFILEPATH = "build/oreid";
  private DataONEDataStoreService service = null;
  private Subject submitter = null;
  /**
   * Create a suite of tests to be run together
   */
  public static Test suite() throws Exception {
      TestSuite suite = new TestSuite();
      suite.addTest(new DataONEDataStoreServiceTest("initialize"));
      suite.addTest(new DataONEDataStoreServiceTest("testExists"));
      suite.addTest(new DataONEDataStoreServiceTest("testSave"));
      suite.addTest(new DataONEDataStoreServiceTest("testReadAndDelete"));
      deleteOREIdFile();
      return suite;
  }
  
  /**
   * Constructor a test
   * @param name the name of test
   */
  public DataONEDataStoreServiceTest(String name) {
    super(name);
    service = new DataONEDataStoreService(Morpho.thisStaticInstance);
  }
  
  /**
   * Check that the testing framework is functioning properly with 
   * a trivial assertion.
   */
  public void initialize() {
      assertTrue(true);
  }
  
 
  /**
   * Test the exists method
   * @throws Exception
   */
  public void testExists() throws Exception {
    String id = "ornl.mstmip.benchmark.global.gpp.modis.01";
    String id2= "tao.1";
    
    assertTrue("the id "+id+" should exist in the server ", service.exists(id));
    assertFalse("The id "+id2+" shouldn't exist in the server", service.exists(id2));
    
  }
  
  
  /**
   * Test the save method
   * @throws Exception
   */
  public void testSave() throws Exception {
    CertificateManager.getInstance().setCertificateLocation(getCertificateFileLocation());
    submitter = ClientIdentityManager.getCurrentIdentity();
    MorphoDataPackage dataPackage = createMorphoPackage();
    service.save(dataPackage);
  }
  
  /*
   * Create a morpho data package.
   */
  private MorphoDataPackage createMorphoPackage() throws Exception {
    //String oreIdStr = service.generateIdentifier("tao");
    String oreIdStr = generateId();
    System.out.println("the oreIdStr is "+oreIdStr);
    //String metadataIdStr = service.generateIdentifier("tao");
    String metadataIdStr = generateId();
    System.out.println(" the metadataIdStr is "+metadataIdStr);
    //String dataIdStr = service.generateIdentifier("tao");
    String dataIdStr = generateId();
    System.out.println(" the dataIdStr is "+dataIdStr);
    
    MNode activeNode = service.getActiveMNode();
    Node nodeAPI = activeNode.getCapabilities();
    NodeReference node = nodeAPI.getIdentifier();
    
    Identifier oreId = new Identifier();
    oreId.setValue(oreIdStr);
    MorphoDataPackage ore = new MorphoDataPackage();
    ore.setPackageId(oreId);
    Map<Identifier, List<Identifier>> metadataRelation = new HashMap();
    Identifier metadataId = new Identifier();
    metadataId.setValue(metadataIdStr);
    List<Identifier> list = new ArrayList<Identifier>();
    Identifier dataId = new Identifier();
    dataId.setValue(dataIdStr);
    list.add(dataId);
    metadataRelation.put(metadataId, list);
    ore.setMetadataMap(metadataRelation);
    ObjectFormatIdentifier oreFormatId = new ObjectFormatIdentifier();
    oreFormatId.setValue(OREFORMATID);
    SystemMetadata oreSysMeta = generateSystemMetadata(oreId, ore.serializePackage().getBytes(), 
                                                      oreFormatId, submitter,node); 
    ore.setSystemMetadata(oreSysMeta);
    
    String eml = IOUtils.toString(new FileInputStream(new File(EMLPATH)));
    eml = eml.replaceAll(DATAID, dataIdStr);
    //System.out.println("the eml is "+eml);
    AbstractDataPackage adp = DataPackageFactory.getDataPackage(new StringReader(eml));
    byte[] emlByte = eml.getBytes();
    adp.setData(emlByte);
    ObjectFormatIdentifier emlFormatId = new ObjectFormatIdentifier();
    emlFormatId.setValue(EMLFORMATID);
    SystemMetadata emlSysMeta =  generateSystemMetadata(metadataId, emlByte, 
        emlFormatId, submitter,node); 
    adp.setSystemMetadata(emlSysMeta);
    ore.addData(adp);
    ore.setAbstractDataPackage(adp);
    
    String data = IOUtils.toString(new FileInputStream(new File(DATAPATH)));
    D1Object dataObject = new D1Object();
    dataObject.setData(data.getBytes());
    ObjectFormatIdentifier dataFormatId = new ObjectFormatIdentifier();
    dataFormatId.setValue(DATAFORMATID);
    SystemMetadata dataSysMeta =  generateSystemMetadata(dataId, data.getBytes(), 
       dataFormatId, submitter,node); 
    dataObject.setSystemMetadata(dataSysMeta);
    ore.addData(dataObject);
    storeOREIdToFile(oreIdStr);
    return ore;
    
   /*InputStream ore = new FileInputStream(new File(OREPATH));
    String oreContent = IOUtils.toString(ore, IdentifierFileMap.UTF8);
    //DataPackage dataPackage = DataPackage.deserializePackage(oreContent);
    Map<Identifier, Map<Identifier, List<Identifier>>> packageMap = 
        ResourceMapFactory.getInstance().parseResourceMap(oreContent);
    Identifier pid = packageMap.keySet().iterator().next();
    System.out.println("the package id is "+pid.getValue());
    Map <Identifier, List<Identifier>> metadataMap = packageMap.get(pid);
    Identifier metadataId = metadataMap.keySet().iterator().next();
    System.out.println("the metadata id is "+metadataId.getValue());
    List<Identifier> list = metadataMap.get(metadataId);
    for (Identifier id : list) {
      System.out.println("the data ids are "+id.getValue());
    }*/
    
    
  }
  
  /**
   * Test the read action.
   * @throws Exception
   */
  public void testReadAndDelete() throws Exception {
    String oreid = readOREIdFromFile();
    //String oreid = "test-1352508561138507.76878722186103";
    MorphoDataPackage dataPackage = service.read(oreid);
    assertTrue("The size of d1object in this data package should be two", dataPackage.identifiers().size() ==2);
    Set<Identifier> set = dataPackage.identifiers();
    for(Identifier id :set) {
      System.out.println("The id is "+id.getValue());
      D1Object object = dataPackage.get(id);
      byte[] array = object.getData();
      if(object instanceof Entity) {   
        assertTrue("the arrary size should be 8", array.length==8);
      } else {
        System.out.println("the arrary size should be "+array.length);
      }
    }
    
    service.delete(dataPackage);
  }
  
  
  
  /*
   * Generate the system metadata.
   */
  private SystemMetadata generateSystemMetadata(Identifier id, byte[] data, 
      ObjectFormatIdentifier formatId, Subject submitter, NodeReference nodeId) 
          throws Exception{

    SystemMetadata sm = new SystemMetadata();
    sm.setIdentifier(id);
    ObjectFormat fmt;
    try {
      fmt = ObjectFormatCache.getInstance().getFormat(formatId);
    }
    catch (BaseException be) {
      formatId.setValue("application/octet-stream");
      fmt = ObjectFormatCache.getInstance().getFormat(formatId);
    }
    sm.setFormatId(fmt.getFormatId());

    //create the checksum
    InputStream is = new ByteArrayInputStream(data);

    Checksum checksum;
    checksum = ChecksumUtil.checksum(is, "MD5");
    sm.setChecksum(checksum);

    //set the size
    sm.setSize(new BigInteger(String.valueOf(data.length)));

    // serializer needs a value, though MN will ignore the value
    sm.setSerialVersion(BigInteger.ONE);
    
    // set submitter and rightholder from the associated string
    sm.setSubmitter(submitter);
    sm.setRightsHolder(submitter);
    
    Date dateCreated = new Date();
    sm.setDateUploaded(dateCreated);
    Date dateUpdated = new Date();
    sm.setDateSysMetadataModified(dateUpdated);

    // Node information
    sm.setOriginMemberNode(nodeId);
    sm.setAuthoritativeMemberNode(nodeId);
    
    AccessPolicy ap = new AccessPolicy();
    AccessRule ar = new AccessRule();
    Subject s = new Subject();
    s.setValue(Constants.SUBJECT_PUBLIC);
    ar.addSubject(s);
    ar.addPermission(Permission.READ);
    ap.addAllow(ar);
    sm.setAccessPolicy(ap);
    
    return sm;
  }

  /*
   * The certificate always locates on the temperate folder and it has a fixed name/tmp/x509up_u1000 
   * 
   */
  private String getCertificateFileLocation()
  {
    //String tmpDir = System.getProperty("java.io.tmpdir");
    String tmpDir="/tmp/";
    System.out.println("the tmp directory is "+tmpDir);
    File file =new File(tmpDir,"x509up_u502");
    return file.getAbsolutePath();
  }
  
  
  /**
   * Store the oreId into an external file
   * @param oreId
   * @throws Exception
   */
  private void storeOREIdToFile(String oreId) throws Exception {
    File oreIdFile = new File(OREPATH);
    oreIdFile.createNewFile();
    FileOutputStream out = new FileOutputStream(oreIdFile);
    try {
      IOUtils.write(oreId, out);
    } finally {
      if(out != null) {
        out.close();
      }
    }
  }
  
  /*
   * Delete the file storing the ore id
   */
  private static void deleteOREIdFile() {
   File oreIdFile = new File(OREPATH);
   if(oreIdFile.exists()) {
     oreIdFile.deleteOnExit();
   }
  }
  
  /**
   * Read the id from the storing file.
   * @return id of the ore; null will be returned if can't find file.
   * @throws Exception
   */
  private String readOREIdFromFile() throws Exception {
    String id = null;
    File oreIdFile = new File(OREPATH);
    if(oreIdFile.exists()) {
      id = IOUtils.toString(new FileInputStream(oreIdFile));
    }
    return id;
  }
  
  /*
   * Generate an id by combine current time and a random number
   */
  private String generateId () {
    double appendix = Math.random() *1000;
    String id = "test-"+System.currentTimeMillis()+appendix;
    return id;
  }
}

