package edu.ucsb.nceas.morphotest.dataone;

import org.dataone.service.types.v1.AccessPolicy;
import org.dataone.service.types.v1.AccessRule;
import org.dataone.service.types.v1.Permission;
import org.dataone.service.types.v1.Subject;

import junit.framework.Test;
import junit.framework.TestSuite;
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.dataone.AccessPolicyConverter;
import edu.ucsb.nceas.morphotest.MorphoTestCase;
import edu.ucsb.nceas.utilities.OrderedMap;


public class AccessPolicyConverterTest extends MorphoTestCase {
  
  /**
   * Create a suite of tests to be run together
   */
  public static Test suite() throws Exception {
      TestSuite suite = new TestSuite();
      suite.addTest(new AccessPolicyConverterTest("initialize"));
      suite.addTest(new AccessPolicyConverterTest("testGetAccessPolicyFromOrderedMap"));
      return suite;
  }
  
  /**
   * Constructor a test
   * @param name the name of test
   */
  public AccessPolicyConverterTest(String name) {
    super(name);
    
  }
  
  /**
   * Check that the testing framework is functioning properly with 
   * a trivial assertion.
   */
  public void initialize() {
      assertTrue(true);
  }
  
  public void testGetAccessPolicyFromOrderedMap() throws Exception {
    OrderedMap map = new OrderedMap();
    map.put("/eml:eml/access/@authSystem", "knb");
    map.put("/eml:eml/access/@order", "allowFirst");
    map.put("/eml:eml/access/allow[1]/principal", "public");
    map.put("/eml:eml/access/allow[1]/permission", "read");
    map.put("/eml:eml/access/allow[2]/principal[1]", "CN=Jim Basney A426,O=Google,C=US,DC=cilogon,DC=org");
    map.put("/eml:eml/access/allow[2]/permission[1]", "read");
    map.put("/eml:eml/access/allow[2]/permission[2]", "write");
    map.put("/eml:eml/access/deny[1]/principal[1]", "CN=Giriprakash Palanisamy A613,O=Google,C=US,DC=cilogon,DC=org");
    map.put("/eml:eml/access/deny[1]/permission", "read");
    AccessPolicy policy = AccessPolicyConverter.getAccessPolicy(map);
    AccessRule rule = policy.getAllow(0);
    Subject sub1 = rule.getSubject(0);
    assertTrue("The first subject should be public", sub1.getValue().equals("public"));
    Permission permission1 = rule.getPermission(0);
    assertTrue("The first permission should be READ", permission1.compareTo(Permission.READ) ==0);
  }
  

}
