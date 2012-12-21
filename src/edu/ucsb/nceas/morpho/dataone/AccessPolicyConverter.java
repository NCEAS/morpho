package edu.ucsb.nceas.morpho.dataone;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.dataone.service.types.v1.AccessPolicy;
import org.dataone.service.types.v1.AccessRule;
import org.dataone.service.types.v1.Permission;
import org.dataone.service.types.v1.Subject;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
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
		DocInfoHandler dih = new DocInfoHandler();
	    XMLReader docinfoParser = XMLUtilities.initParser(dih, null);
	    docinfoParser.parse(new InputSource(new ByteArrayInputStream(adp.getData())));
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
