package edu.ucsb.nceas.morpho.dataone;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.dataone.service.types.v1.AccessPolicy;
import org.dataone.service.types.v1.AccessRule;
import org.dataone.service.types.v1.Permission;
import org.dataone.service.types.v1.Subject;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.util.XMLUtil;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;
import edu.ucsb.nceas.utilities.access.AccessControlInterface;
import edu.ucsb.nceas.utilities.access.DocInfoHandler;
import edu.ucsb.nceas.utilities.access.XMLAccessDAO;

public class AccessPolicyConverter {

	/**
	 * Infer the AccessPolicy from the EML content
	 * @param adp
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 */
	public static AccessPolicy getAccessPolicy(AbstractDataPackage adp) throws SAXException, IOException {
		// access policy
		AccessPolicy accessPolicy = null;
		// parse the EML
		return getAccessPolicy(new InputSource(new ByteArrayInputStream(adp.getData())));
		
	}
	
	/**
	 * Get an AccessPolicy object from an OrderedMap object.
	 * @param map  the orderedMap which contains the access information (it originates from the Access page)
	 * @return the AccessPolicy
	 * @throws IOException 
	 * @throws TransformerException 
	 * @throws DOMException 
	 * @throws SAXException 
	 */
	public static AccessPolicy getAccessPolicy(OrderedMap map) throws IOException, DOMException, TransformerException, SAXException {
	  AccessPolicy accessPolicy = null;
	  Node node = XMLUtilities.getXMLReaderAsDOMTreeRootNode(new StringReader("<eml:eml xmlns:eml=\"eml://ecoinformatics.org/eml-2.1.1\"><access></access></eml:eml>"));
	  XMLUtilities.getXPathMapAsDOMTree(map, node);
	  return getAccessPolicy(new InputSource(new ByteArrayInputStream(XMLUtil.getDOMTreeAsString(node).getBytes("UTF-8"))));
	}
	
	
	/*
	 * Get an AccessPolicy from an input source (xml format)
	 */
	private static AccessPolicy getAccessPolicy(InputSource source) throws SAXException, IOException {
	  AccessPolicy accessPolicy = null;
	  DocInfoHandler dih = new DocInfoHandler();
    XMLReader docinfoParser = XMLUtilities.initParser(dih, null);
    docinfoParser.parse(source);
    Vector<XMLAccessDAO> accessControlList = dih.getAccessControlList();
    if (accessControlList != null) {
      accessPolicy = new AccessPolicy();
      for (XMLAccessDAO accessDAO: accessControlList) {
        String permOrder = accessDAO.getPermOrder();
        String permType = accessDAO.getPermType();
        Long permission = accessDAO.getPermission();
        String principal = accessDAO.getPrincipalName();
        if (permOrder.equals(AccessControlInterface.ALLOWFIRST)) {
          if (permType.equals(AccessControlInterface.ALLOW)) {
            AccessRule allowRule = new AccessRule();
            // who
            Subject subject = new  Subject();
            subject.setValue(principal);
          allowRule.addSubject(subject);
          // what
          allowRule.setPermissionList(convertPermission(permission.intValue()));
          accessPolicy.addAllow(allowRule);
          }
        }
        else {
          // cannot use denyFirst, D1 does not support that
          break;
        }
        
      }
    }
    
    return accessPolicy;
	}
	
	/**
	 * Transform an AccessPolicy to an OrderedMap object with given root path
	 * @param accessPolicy  the AccessPolicy needs to be transformed.
	 * @return the OrderedMap of the AccessPolicy. Null will be returned if the accesPolicy is null.
	 */
	public static OrderedMap getOrderMapFromAccessPolicy(AccessPolicy accessPolicy, String path) throws ParserConfigurationException {
	  OrderedMap map = null;
	  if(accessPolicy != null) {
	    List<AccessRule> accessRules = accessPolicy.getAllowList();
	    if(accessRules != null && accessRules.size() >0) {
	      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	      DocumentBuilder builder = factory.newDocumentBuilder();
	      Document document = builder.newDocument();
	      Element root = document.createElement(AccessControlInterface.ACCESS);
	      Attr allowFirst = document.createAttribute(AccessControlInterface.ORDER);
	      allowFirst.setValue(AccessControlInterface.ALLOWFIRST);
	      root.setAttributeNode(allowFirst);
	      document.appendChild(root);
	      for(AccessRule rule : accessRules) {
	        Element allowElement = document.createElement(AccessControlInterface.ALLOW);
	        List<Subject> subjects = rule.getSubjectList();
	        List<Permission> permissions = rule.getPermissionList();
	        if(subjects == null || subjects.size() <=0 || permissions == null || permissions.size() <=0) {
	          continue;
	        }
	        for(Subject subject : subjects) {
	          Element subjectElement = document.createElement(AccessControlInterface.PRINCIPAL);
	          subjectElement.appendChild(document.createTextNode(subject.getValue()));
	          allowElement.appendChild(subjectElement);
	        }
	        for(Permission permission : permissions) {
	          Element permissionElement = document.createElement(AccessControlInterface.PERMISSION);
	          permissionElement.appendChild(document.createTextNode(permission.xmlValue()));
	          allowElement.appendChild(permissionElement);
	        }
	        root.appendChild(allowElement);
	      }
	      map = XMLUtilities.getDOMTreeAsXPathMap(root, path);
	    }
	  }
	  return map;
	}
	
	/**
	 * Translate between DataONE and EML permissions
	 * TODO: better place for this
	 * @param permission
	 * @return
	 */
	private static int convertPermission(Permission permission) {
    	if (permission.equals(Permission.READ)) {
    		return AccessControlInterface.READ;
    	}
    	if (permission.equals(Permission.WRITE)) {
    		return AccessControlInterface.WRITE;
    	}
    	if (permission.equals(Permission.CHANGE_PERMISSION)) {
    		return AccessControlInterface.CHMOD;
    	}
		return -1;
    }
    
	/**
	 * Translate between EML and DataONE permissions
	 * @param permission
	 * @return
	 */
    private static List<Permission> convertPermission(int permission) {
    	
    	List<Permission> permissions = new ArrayList<Permission>();
    	if (permission == AccessControlInterface.ALL) {
    		permissions.add(Permission.READ);
    		permissions.add(Permission.WRITE);
    		permissions.add(Permission.CHANGE_PERMISSION);
    		return permissions;
    	}
    	
    	if ((permission & AccessControlInterface.CHMOD) == AccessControlInterface.CHMOD) {
    		permissions.add(Permission.CHANGE_PERMISSION);
    	}
    	if ((permission & AccessControlInterface.READ) == AccessControlInterface.READ) {
    		permissions.add(Permission.READ);
    	}
    	if ((permission & AccessControlInterface.WRITE) == AccessControlInterface.WRITE) {
    		permissions.add(Permission.WRITE);
    	}
    	
		return permissions;
    }
}
