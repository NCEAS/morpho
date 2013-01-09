package edu.ucsb.nceas.morphotest.dataone;

import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dataone.service.types.v1.AccessPolicy;
import org.dataone.service.types.v1.AccessRule;
import org.dataone.service.types.v1.Permission;
import org.dataone.service.types.v1.Subject;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import junit.framework.Test;
import junit.framework.TestSuite;
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.dataone.AccessPolicyConverter;
import edu.ucsb.nceas.morphotest.MorphoTestCase;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.access.AccessControlInterface;


public class AccessPolicyConverterTest extends MorphoTestCase {
  
  /**
   * Create a suite of tests to be run together
   */
  public static Test suite() throws Exception {
      TestSuite suite = new TestSuite();
      suite.addTest(new AccessPolicyConverterTest("initialize"));
      suite.addTest(new AccessPolicyConverterTest("testGetAccessPolicyFromOrderedMap"));
      suite.addTest(new AccessPolicyConverterTest("testGetOrderMapFromAccessPolicy"));
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
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document document = builder.newDocument();
    Element root = document.createElement(AccessControlInterface.ACCESS);
    Attr allowFirst = document.createAttribute(AccessControlInterface.ORDER);
    allowFirst.setValue(AccessControlInterface.ALLOWFIRST);
    root.setAttributeNode(allowFirst);
    document.appendChild(root);
    Element allowElement = document.createElement(AccessControlInterface.ALLOW);
    Element subjectElement = document.createElement(AccessControlInterface.PRINCIPAL);
    subjectElement.appendChild(document.createTextNode("public"));
    allowElement.appendChild(subjectElement);
    Element permissionElement = document.createElement(AccessControlInterface.PERMISSION);
    permissionElement.appendChild(document.createTextNode("read"));
    allowElement.appendChild(permissionElement);
    root.appendChild(allowElement);
    AccessPolicy policy = AccessPolicyConverter.getAccessPolicy(root);
    AccessRule rule = policy.getAllow(0);
    Subject sub1 = rule.getSubject(0);
    assertTrue("The first subject should be public", sub1.getValue().equals("public"));
    Permission permission1 = rule.getPermission(0);
    assertTrue("The first permission should be READ", permission1.compareTo(Permission.READ) ==0);
  }
  
  public void testGetOrderMapFromAccessPolicy() throws Exception {
    AccessPolicy policy = null;
    policy = new AccessPolicy();
    OrderedMap map = AccessPolicyConverter.getOrderMapFromAccessPolicy(policy, "/eml:eml");
    assertTrue("The map should be null since the policy is empty", map == null);
    map = AccessPolicyConverter.getOrderMapFromAccessPolicy(policy, "/eml:eml");
    assertTrue("The map should be null since the policy is empty", map == null);
    AccessRule rule1 = new AccessRule();
    Subject subject1= new Subject();
    subject1.setValue("user1");
    rule1.addSubject(subject1);
    Subject subject2 = new Subject();
    subject2.setValue("user2");
    rule1.addSubject(subject2);
    rule1.addPermission(Permission.READ);
    rule1.addPermission(Permission.WRITE);
    policy.addAllow(rule1);
    AccessRule rule2 = new AccessRule();
    Subject subject3 = new Subject();
    subject3.setValue("user3");
    rule2.addSubject(subject3);
    rule2.addPermission(Permission.CHANGE_PERMISSION);
    policy.addAllow(rule2);
    map = AccessPolicyConverter.getOrderMapFromAccessPolicy(policy, "/eml:eml");
    System.out.println(map.toString());
    assertTrue("The size of map should be 7", map.size() == 7);
    assertTrue("The value for /eml:eml/access/@order should be allowFirst", map.get("/eml:eml/access/@order").equals("allowFirst"));
   
    assertTrue("The value for /eml:eml/access/allow[1]/principal[2] should be user2", map.get("/eml:eml/access/allow[1]/principal[2]").equals("user2"));
    
  }

}
