package edu.ucsb.nceas.morpho.dataone;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.dataone.security.CertificateFetcher;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ConnectionFrame;

public class EcpAuthentication {
	
	// TODO: make this configurable
	private static final String spURL = "https://ecp.cilogon.org:443/secure/getcert/";

	public static String[] getAvailableIdentityProviders() {
		Map<String, String> providers = new TreeMap<String, String>();
		providers.put("https://login.ligo.org:443/idp/profile/SAML2/SOAP/ECP", "LIGO Scientific Collaboration");
		providers.put("https://shib.lternet.edu:443/idp/profile/SAML2/SOAP/ECP", "LTER Network");
		providers.put("https://idp.protectnetwork.org:443/protectnetwork-idp/profile/SAML2/SOAP/ECP", "ProtectNetwork");
		providers.put("https://shibboleth2.uchicago.edu:443/idp/profile/SAML2/SOAP/ECP", "University of Chicago");
		providers.put("https://idp.u.washington.edu:443/idp/profile/SAML2/SOAP/ECP", "University of Washington");
		providers.put("https://login.wisc.edu:443/idp/profile/SAML2/SOAP/ECP", "University of Wisconsin-Madison");
		
		return providers.keySet().toArray(new String[0]);
	}
	
	/** Launch the login window */
	public static void establishConnection() {
		ConnectionFrame cf = new ConnectionFrame(Morpho.thisStaticInstance);
		cf.setVisible(true);
	}
	
	/**
	 * Authenticate with the given Idp
	 * @param idp
	 * @param username
	 * @param password
	 */
	public static File authenticate(String idpURL, String username, String password) {
		
		// from the ECP library
		CertificateFetcher certFetcher = new CertificateFetcher();
		String pemContent = certFetcher.authenticate(spURL, idpURL, username, password);
		
		// save to a temp file
		File certificateFile = null;
		try {
			certificateFile = File.createTempFile("ecp", "pem");
			IOUtils.write(pemContent, new FileOutputStream(certificateFile));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return certificateFile;
		
	}

}
