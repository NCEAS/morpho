package edu.ucsb.nceas.morpho.dataone;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.dataone.security.CertificateFetcher;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ConnectionFrame;

/**
 * This class is used to authenticate a user with the simple-ecp-client.
 * A combination of identity provider, username and password are required.
 * When authentication is successful, a certificate and private key are returned 
 * in a PEM file.
 * @author leinfelder
 *
 */
public class EcpAuthentication {
		
	// configurable in the config.xml file
	public static String ECP_SKIN_TAG = "cilogon_skin_name";
	public static String ECP_SERVICE_PROVIDER_TAG = "ecp_service_provider_url";
	public static String ECP_REMOTE_IDP_LIST_URL_TAG = "ecp_idp_list_url";
	public static String ECP_IDP_LIST_TAG = "ecp_idp_list";
	public static String ECP_IDP_NAME_TAG = "ecp_idp_name";
	public static String ECP_IDP_URL_TAG = "ecp_idp_url";

	private String skinName = null;
	private String spURL = null;
	private String idpListURL = null;
	
	private List<IdentityProviderSelectionItem> providers = null;

	private static EcpAuthentication instance = null;
	
	private EcpAuthentication() {
		// look up config values
		skinName = Morpho.getConfiguration().get(ECP_SKIN_TAG, 0);
		spURL = Morpho.getConfiguration().get(ECP_SERVICE_PROVIDER_TAG, 0);
		idpListURL = Morpho.getConfiguration().get(ECP_REMOTE_IDP_LIST_URL_TAG, 0);

		// use default from configuration
		providers = getDefaultIdPList();
		
		// then look up the available IdPs from CILogon
		try {
			//providers = getRemoteIdPList();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static EcpAuthentication getInstance() {
		if (instance == null) {
			instance = new EcpAuthentication();
		}
		return instance;
	}

	/**
	 * Retrieve remote list of IdPs that support ECP authentication.
	 * CILogon loosely maintains a text file listing of these as rows of:
	 * idpURL <space> idpName
	 * @return list of supported IdPs maintained by the service provider (CILogon)
	 * @throws Exception
	 */
	private List<IdentityProviderSelectionItem> getRemoteIdPList() throws Exception {
		List<IdentityProviderSelectionItem> providers = new ArrayList<IdentityProviderSelectionItem>();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(idpListURL).openStream()));
		String line = null;
		while ((line = reader.readLine()) != null){
			int delimIndex = line.indexOf(" ");
			String idpUrl = line.substring(0, delimIndex);
			String idpName = line.substring(delimIndex + 1);
			providers.add(new IdentityProviderSelectionItem(idpUrl, idpName));
		}
		
		return providers;
		
	}
	
	/**
	 * Identity providers we know about and currently have accounts with. 
	 * There are more listed by CILogon, but these are in our configuration
	 * @see getRemoteIdPList()
	 * @return list of IdPs
	 */
	private List<IdentityProviderSelectionItem> getDefaultIdPList() {
		List<IdentityProviderSelectionItem> providers = new ArrayList<IdentityProviderSelectionItem>();
		Map<String, String> idpMap = Morpho.getConfiguration().getHashtable(ECP_IDP_LIST_TAG, ECP_IDP_URL_TAG, ECP_IDP_NAME_TAG);
		for (Map.Entry<String, String> idpEntry: idpMap.entrySet()) {
			providers.add(new IdentityProviderSelectionItem(idpEntry.getKey(), idpEntry.getValue()));
		}
		return providers;
	}
	
	public IdentityProviderSelectionItem[] getAvailableIdentityProviders() {
		return providers.toArray(new IdentityProviderSelectionItem[0]);
	}
	
	/** Launch the login window */
	public void establishConnection() {
		ConnectionFrame cf = new ConnectionFrame(Morpho.thisStaticInstance);
		cf.setVisible(true);
	}
	
	/**
	 * Authenticate with the given IdP URL
	 * @param idp
	 * @param username
	 * @param password
	 * @return the certificate PEM File
	 */
	public File authenticate(String idpURL, String username, String password) {
		
		// from the ECP library
		CertificateFetcher certFetcher = new CertificateFetcher();
		certFetcher.setSkin(skinName);
		String pemContent = certFetcher.authenticate(spURL, idpURL, username, password);
		
		// save to a temp file
		File certificateFile = null;
		try {
			certificateFile = File.createTempFile("ecp", ".pem");
			IOUtils.write(pemContent, new FileOutputStream(certificateFile));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return certificateFile;
		
	}

}