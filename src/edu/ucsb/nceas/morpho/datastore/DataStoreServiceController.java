/**
 * 
 */
package edu.ucsb.nceas.morpho.datastore;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.activation.FileDataSource;
import javax.swing.JOptionPane;

import org.dataone.client.D1Object;
import org.dataone.service.types.v1.Checksum;
import org.dataone.service.types.v1.Identifier;
import org.dataone.service.types.v1.NodeReference;
import org.dataone.service.types.v1.ObjectFormatIdentifier;
import org.dataone.service.types.v1.Subject;
import org.dataone.service.types.v1.SystemMetadata;
import org.dataone.service.types.v1.util.ChecksumUtil;

import com.ibm.icu.util.Calendar;

import edu.ucsb.nceas.morpho.Language;
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.Entity;
import edu.ucsb.nceas.morpho.datapackage.MorphoDataPackage;
import edu.ucsb.nceas.morpho.datastore.idmanagement.IdentifierFileMap;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.DataLocation;
import edu.ucsb.nceas.morpho.query.LocalQuery;
import edu.ucsb.nceas.morpho.util.Base64;
import edu.ucsb.nceas.morpho.util.IOUtil;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.XMLTransformer;
import edu.ucsb.nceas.morpho.util.XMLUtil;

/**
 * This singleton class allows callers to interact with data store services for
 * a given location.
 * 
 * @author leinfelder
 * 
 */
public class DataStoreServiceController {

	/**
	* TODO: the service should probably tell us what schemes it uses
	* But for now we will hope for the best with these
	*/
	public static final String DOI = "DOI";
	public static final String UUID = "UUID";
	public static final String[] IDENTIFIER_SCHEMES = {UUID, DOI};

	private static DataStoreServiceController instance;
	
	private DataStoreServiceController() {

	}

	public static DataStoreServiceController getInstance() {
		if (instance == null) {
			instance = new DataStoreServiceController();
		}
		return instance;
	}
	
	/**
	 * Gets next revision for this doc for the given location
	 * @param docid the partial identifier (no rev)
	 * @param location of the doc
	 * @return  the next revision number
	 * @throws Exception 
	 */
	public List<String> getAllRevisions(String docid, String location)
	{
		
		List<String> versions = null;
		try {
			if (location.equals(DataPackageInterface.LOCAL)) {
				versions  = Morpho.thisStaticInstance.getLocalDataStoreService().getRevisionManager().getAllRevisions(docid);
			}
			if (location.equals(DataPackageInterface.NETWORK)) {
				versions = Morpho.thisStaticInstance.getDataONEDataStoreService().getRevisionManager().getAllRevisions(docid);
			}
			if (location.equals(DataPackageInterface.BOTH)) {
				// merge the local and network list
				versions  = Morpho.thisStaticInstance.getLocalDataStoreService().getRevisionManager().getAllRevisions(docid);
				List<String> networkVersions =  Morpho.thisStaticInstance.getDataONEDataStoreService().getRevisionManager().getAllRevisions(docid);
				for (String version: networkVersions) {
					if (!versions.contains(version)) {
						versions.add(version);
					}
				}
			}
		} catch (Exception e) {
			Log.debug(5, e.getMessage());
		}
		
		return versions;
	}
	
	/**
	 * Create a new Datapackage given a docid of a metadata object and a
	 * location
	 */
	public MorphoDataPackage read(String docid, String location) {
		// get an MDP from the desired source
		MorphoDataPackage mdp = null;
		try {
			if (location.equals(DataPackageInterface.LOCAL)) {
				mdp = Morpho.thisStaticInstance.getLocalDataStoreService().read(docid);
			} else if (location.equals(DataPackageInterface.BOTH)) {
				mdp = Morpho.thisStaticInstance.getLocalDataStoreService().read(docid);
				// indicate it exists in both locations
				mdp.getAbstractDataPackage().setLocation(DataPackageInterface.BOTH);
			}
			else { 
				// must be on network only
				mdp = Morpho.thisStaticInstance.getDataONEDataStoreService().read(docid);
			}
		} catch (Exception e) {
			Log.debug(20, "Could not read package: " + e.getMessage());
		}
		
		return mdp;
	}
	
	public File openFile(String identifier, String location) throws FileNotFoundException, CacheAccessException {
		File file = null;
		// get from network only if we have to
		if (location.equals(DataPackageInterface.NETWORK)) {
			file  = Morpho.thisStaticInstance.getDataONEDataStoreService().openFile(identifier);
		} if (location.equals(DataPackageInterface.LOCAL)) {
			file = Morpho.thisStaticInstance.getLocalDataStoreService().openFile(identifier);
		} if (location.equals(DataPackageInterface.BOTH)) {
			// try local then network if not in local
			try {
				file = Morpho.thisStaticInstance.getLocalDataStoreService().openFile(identifier);
			} catch (Exception e) {
				file  = Morpho.thisStaticInstance.getDataONEDataStoreService().openFile(identifier);
			}
		}
		return file;
	}
    
    /**
	 * returns the next id for the given location
	 * for the current scope ("fragment")
	 * 
	 */
	public synchronized String generateIdentifier(String scheme, String location) {
		// default to UUID
		if (scheme == null) {
			scheme = DataStoreServiceController.UUID;
		}
		if (location.equals(DataPackageInterface.LOCAL)) {
			String fragment = Morpho.thisStaticInstance.getProfile().get("scope", 0);
			return Morpho.thisStaticInstance.getLocalDataStoreService().generateIdentifier(scheme, fragment);
		}
		if (location.equals(DataPackageInterface.NETWORK) || location.equals(DataPackageInterface.BOTH)) {
		  String fragment = Morpho.thisStaticInstance.getProfile().get("scope", 0);
			try {
				return Morpho.thisStaticInstance.getDataONEDataStoreService().generateIdentifier(scheme, fragment);
			} catch (Exception e) {
				Log.debug(5, e.getMessage());
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Deletes the package from the specified location
	 * 
	 * @param locattion
	 *            the location of the package that you want to delete use either
	 *            BOTH, METACAT or LOCAL
	 */

	public void delete(MorphoDataPackage mdp, String location)
			throws Exception {


		if (location.equals(DataPackageInterface.LOCAL) || location.equals(DataPackageInterface.BOTH)) {
			boolean localSuccess = Morpho.thisStaticInstance.getLocalDataStoreService().delete(mdp);
			if (!localSuccess) {
				throw new Exception("User couldn't delete the local copy");
			}
			String accnum = mdp.getAbstractDataPackage().getAccessionNumber();
			LocalQuery.removeFromCache(accnum);
		}
		if (location.equals(DataPackageInterface.NETWORK) || location.equals(DataPackageInterface.BOTH)) {
			boolean success = Morpho.thisStaticInstance.getDataONEDataStoreService().delete(mdp);
			if (!success) {
				throw new Exception("User couldn't delete the network copy");
			}
		}
	}

	/**
	 * exports a package to a given path
	 * 
	 * @param path
	 *            the path to which this package should be exported.
	 */
	public void export(MorphoDataPackage mdp, String path) {
		AbstractDataPackage adp = mdp.getAbstractDataPackage();
		String location = adp.getLocation();
		String id = adp.getAccessionNumber();
		Log.debug(20, "exporting...");
		Log.debug(20, "path: " + path);
		Log.debug(20, "id: " + id);
		Log.debug(20, "location: " + location);
		File f = null;
		boolean localloc = false;
		boolean metacatloc = false;
		if (location.equals(DataPackageInterface.BOTH)) {
			localloc = true;
			metacatloc = true;
		} else if (location.equals(DataPackageInterface.NETWORK)) {
			metacatloc = true;
		} else if (location.equals(DataPackageInterface.LOCAL)) {
			localloc = true;
		} else {
			Log.debug(1, "Package has not been saved! Unable to export!");
			return;
		}

		// get a list of the files and save them to the new location. if the
		// file
		// is a data file, save it with its original name.
		// With the use of AbstractDataPackage, there is only a single metadata
		// doc
		// and we will use the DOM; may be multiple data files, however
		String packagePath = path + "/" + id + ".package";
		String sourcePath = packagePath + "/metadata";
		String cssPath = packagePath + "/export";
		String dataPath = packagePath + "/data";
		File savedir = new File(packagePath);
		File savedirSub = new File(sourcePath);
		File cssdirSub = new File(cssPath);
		File savedirDataSub = new File(dataPath);
		savedir.mkdirs(); // create the new directories
		savedirSub.mkdirs();
		cssdirSub.mkdirs();
		savedirDataSub.mkdirs();
		StringBuffer[] htmldoc = new StringBuffer[2]; // DFH

		// for css
		try {
			InputStream input = this.getClass().getResourceAsStream("/style/CSS/export.css");
			InputStreamReader styleSheetReader = new InputStreamReader(input);
			// FileReader styleSheetReader = new FileReader(styleSheetSource);
			StringBuffer buffer = IOUtil.getAsStringBuffer(styleSheetReader, true);
			// Create a wrter
			String fileName = cssPath + "/export.css";
			Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), Charset.forName("UTF-8")));
			IOUtil.writeToWriter(buffer, writer, true);
		} catch (Exception e) {
			Log.debug(30, "Error in copying css: " + e.getMessage());
		}

		// for metadata file
		f = new File(sourcePath + "/" + id);

		File openfile = null;
		try {
			if (localloc) { // get the file locally and save it
				openfile = Morpho.thisStaticInstance.getLocalDataStoreService().openFile(id);
			} else if (metacatloc) { // get the file from network
				openfile = Morpho.thisStaticInstance.getDataONEDataStoreService().openFile(id);
			}
			FileInputStream fis = new FileInputStream(openfile);
			BufferedInputStream bfis = new BufferedInputStream(fis);
			FileOutputStream fos = new FileOutputStream(f);
			BufferedOutputStream bfos = new BufferedOutputStream(fos);
			int c = bfis.read();
			while (c != -1) { // copy the files to the source directory
				bfos.write(c);
				c = bfis.read();
			}
			bfos.flush();
			bfis.close();
			bfos.close();

			// for html
			Reader xmlInputReader = null;
			Reader result = null;
			StringBuffer tempPathBuff = new StringBuffer();
			xmlInputReader = new InputStreamReader(new FileInputStream(openfile), Charset.forName("UTF-8"));

			XMLTransformer transformer = XMLTransformer.getInstance();
			// add some property for style sheet
			transformer.removeAllTransformerProperties();
			transformer.addTransformerProperty(
					XMLTransformer.SELECTED_DISPLAY_XSLPROP,
					XMLTransformer.XSLVALU_DISPLAY_PRNT);
			transformer.addTransformerProperty(
					XMLTransformer.HREF_PATH_EXTENSION_XSLPROP, AbstractDataPackage.HTMLEXTENSION);
			transformer.addTransformerProperty(
					XMLTransformer.PACKAGE_ID_XSLPROP, id);
			transformer.addTransformerProperty(
					XMLTransformer.PACKAGE_INDEX_NAME_XSLPROP, AbstractDataPackage.METADATAHTML);
			transformer.addTransformerProperty(
					XMLTransformer.DEFAULT_CSS_XSLPROP, AbstractDataPackage.EXPORTSYLE);
			transformer.addTransformerProperty(
					XMLTransformer.ENTITY_CSS_XSLPROP, AbstractDataPackage.EXPORTSYLE);
			transformer.addTransformerProperty(XMLTransformer.CSS_PATH_XSLPROP,
					".");
			try {
				result = transformer.transform(xmlInputReader);
			} catch (IOException e) {
				e.printStackTrace();
				Log.debug(9, "Unexpected Error Styling Document: " + e.getMessage());
				e.fillInStackTrace();
				throw e;
			} finally {
				xmlInputReader.close();
			}
			transformer.removeAllTransformerProperties();

			try {
				htmldoc[0] = IOUtil.getAsStringBuffer(result, true);
				// "true" closes Reader after reading
			} catch (IOException e) {
				e.printStackTrace();
				Log.debug(9, "Unexpected Error Reading Styled Document: " + e.getMessage());
				e.fillInStackTrace();
				throw e;
			}

			tempPathBuff.delete(0, tempPathBuff.length());

			tempPathBuff.append(packagePath);
			tempPathBuff.append("/");
			tempPathBuff.append(AbstractDataPackage.METADATAHTML);
			tempPathBuff.append(AbstractDataPackage.HTMLEXTENSION);
			
		    IOUtil.writeToWriter(htmldoc[0], new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempPathBuff.toString()), Charset.forName("UTF-8"))), true);
			
		} catch (Exception w) {
			w.printStackTrace();
			Log.debug(9, "Unexpected Error Reading Styled Document: "
					+ w.getMessage());
		}

		// export all entities
		exportDataFiles(adp, savedirDataSub.getAbsolutePath(), null);
		JOptionPane.showMessageDialog(UIController.getInstance()
				.getCurrentActiveWindow(),
		/* "Package export is complete ! " */Language.getInstance().getMessage(
				"PackageExportComplete")
				+ " !");
	}

	/**
	 * Exports a package to a zip file at the given path
	 * 
	 * @param path
	 *            the path to export the zip file to
	 */
	public void exportToZip(MorphoDataPackage mdp, String path) {
		try {
			AbstractDataPackage adp = mdp.getAbstractDataPackage();
			String id = adp.getAccessionNumber();
			// export the package in an uncompressed format to the temp
			// directory
			// then zip it up and save it to the specified path
			String tempdir = ConfigXML.getConfigDirectory() + File.separator + Morpho.getConfiguration().get("tempDir", 0);
			export(mdp, tempdir + "/tmppackage");
			File zipfile = new File(path);
			FileOutputStream fos = new FileOutputStream(zipfile);
			ZipOutputStream zos = new ZipOutputStream(fos);
			String temppackdir = tempdir + "/tmppackage/" + id + ".package";
			File packdirfile = new File(temppackdir);
			String[] dirlist = packdirfile.list();
			String packdir = id + ".package";
			// zos.putNextEntry(new ZipEntry(packdir));
			for (int i = 0; i < dirlist.length; i++) {
				String entry = temppackdir + "/" + dirlist[i];
				ZipEntry ze = new ZipEntry(packdir + "/" + dirlist[i]);
				File entryFile = new File(entry);
				if (!entryFile.isDirectory()) {
					ze.setSize(entryFile.length());
					zos.putNextEntry(ze);
					FileInputStream fis = new FileInputStream(entryFile);
					int c = fis.read();
					while (c != -1) {
						zos.write(c);
						c = fis.read();
					}
					zos.closeEntry();
				}
			}
			// for data file
			String dataPackdir = packdir + "/data";
			String tempDatapackdir = temppackdir + "/data";
			File dataFile = new File(tempDatapackdir);
			String[] dataFileList = dataFile.list();
			if (dataFileList != null) {
				for (int i = 0; i < dataFileList.length; i++) {
					String entry = tempDatapackdir + "/" + dataFileList[i];
					ZipEntry ze = new ZipEntry(dataPackdir + "/"
							+ dataFileList[i]);
					File entryFile = new File(entry);
					if (!entryFile.isDirectory()) {
						ze.setSize(entryFile.length());
						zos.putNextEntry(ze);
						FileInputStream fis = new FileInputStream(entryFile);
						int c = fis.read();
						while (c != -1) {
							zos.write(c);
							c = fis.read();
						}
						zos.closeEntry();
					} else {
						String[] secondaryDataFileList = entryFile.list();
						if (secondaryDataFileList != null) {
							for (int j = 0; j < secondaryDataFileList.length; j++) {
								String secondaryEntry = entry + "/"
										+ secondaryDataFileList[j];
								ZipEntry secondaryZE = new ZipEntry(dataPackdir
										+ "/" + dataFileList[i] + "/"
										+ secondaryDataFileList[j]);
								File secondaryEntryFile = new File(
										secondaryEntry);
								secondaryZE
										.setSize(secondaryEntryFile.length());
								zos.putNextEntry(secondaryZE);
								FileInputStream fis = new FileInputStream(
										secondaryEntryFile);
								int c = fis.read();
								while (c != -1) {
									zos.write(c);
									c = fis.read();
								}
								zos.closeEntry();
							}
						}
					}
				}
			}

			// for css
			try {
				String cssPath = packdir + "/export";
				InputStream input = this.getClass().getResourceAsStream(
						"/style/CSS/export.css");
				InputStreamReader styleSheetReader = new InputStreamReader(
						input);
				// FileReader styleSheetReader = new
				// FileReader(styleSheetSource);
				StringBuffer buffer = IOUtil.getAsStringBuffer(
						styleSheetReader, true);
				// Create a wrter
				ZipEntry ze = new ZipEntry(cssPath + "/export.css");
				ze.setSize(buffer.length());
				zos.putNextEntry(ze);
				int count = 0;
				int c = buffer.charAt(count);
				while (c != -1) {
					zos.write(c);
					count++;
					c = buffer.charAt(count);
				}
				zos.closeEntry();
			} catch (Exception e) {
				Log.debug(30, "Error in copying css: " + e.getMessage());
			}

			packdir += "/metadata";
			temppackdir += "/metadata";
			File sourcedir = new File(temppackdir);
			File[] sourcefiles = sourcedir.listFiles();
			for (int i = 0; i < sourcefiles.length; i++) {
				File f = sourcefiles[i];

				ZipEntry ze = new ZipEntry(packdir + "/" + f.getName());
				ze.setSize(f.length());
				zos.putNextEntry(ze);
				FileInputStream fis = new FileInputStream(f);
				int c = fis.read();
				while (c != -1) {
					zos.write(c);
					c = fis.read();
				}
				zos.closeEntry();
			}
			zos.flush();
			zos.close();
		} catch (Exception e) {
			Log.debug(5,
					"Exception in exporting to zip file (AbstractDataPackage)"
							+ e.getMessage());
		}
	}

	/**
	 * copies all the data files in a package to a directory indicated by
	 * 'path'. Files are given the original file name, if available
	 */
	public boolean exportDataFiles(AbstractDataPackage adp, String path, Integer entityIndex) {
		
		String location = adp.getLocation();
		
		if (location.equals(DataPackageInterface.TEMPLOCATION)) {
			Log.debug(5, "Morpho cannot export a data package that has not been saved!");
			return false;
		}
		String origFileName;
		File dataFile = null;
		if (adp.getEntityArray() == null) {
			Log.debug(20, "there is no data!");
			return true; // there is no data, but is that an error?!
		}
		// assume the package has been saved so that location is either LOCAL or
		// METACAT
		for (int i = 0; i < adp.getEntityArray().length; i++) {
			// if given a particular entity, on;y export it, otherwise do all
			if (entityIndex != null && i != entityIndex) {
				continue;
			}
			String urlinfo = adp.getDistributionUrl(i, 0, 0);
			// assumed that urlinfo is of the form
			// 'protocol://systemname/localid/other'
			// protocol is probably 'ecogrid'; system name is 'knb'
			// we just want the local id here
			int indx2 = urlinfo.indexOf("//");
			if (indx2 > -1) {
				urlinfo = urlinfo.substring(indx2 + 2);
				// now start should be just past the '//'
			}
			indx2 = urlinfo.indexOf("/");
			if (indx2 > -1) {
				urlinfo = urlinfo.substring(indx2 + 1);
				// now should be past the system name
			}
			indx2 = urlinfo.indexOf("/");
			if (indx2 > -1) {
				urlinfo = urlinfo.substring(0, indx2);
				// should have trimmed 'other'
			}
			if (urlinfo.length() == 0) {
				continue;
			}
			// if we reach here, urlinfo should be the id in a string
			// now try to get the original filename
			origFileName = adp.getPhysicalName(i, 0);
			if (origFileName.trim().equals("")) { // original file name missing
				origFileName = urlinfo;
			}
			try {
				if ((location.equals(DataPackageInterface.LOCAL))
						|| (location.equals(DataPackageInterface.BOTH))) {
					dataFile = Morpho.thisStaticInstance
							.getLocalDataStoreService().openFile(urlinfo);
				} else if (location.equals(DataPackageInterface.NETWORK)) {
					dataFile = Morpho.thisStaticInstance.getDataONEDataStoreService()
							.openFile(urlinfo);
				}
			} catch (FileNotFoundException fnf) {
				// if the datfile has NOT been located, a FileNotFoundException
				// will be thrown.
				// this indicates that the datafile with the url has NOT been
				// saved
				// the datafile should be stored in the profile temp dir
				// Log.debug(1, "FileNotFoundException");
				/*
				 * ConfigXML profile = morpho.getProfile(); String separator =
				 * profile.get("separator", 0); separator = separator.trim();
				 * String temp = new String(); temp = urlinfo.substring(0,
				 * urlinfo.indexOf(separator)); temp += "/" +
				 * urlinfo.substring(urlinfo.indexOf(separator) + 1,
				 * urlinfo.length());
				 */
				try {
					// dataFile = fds.openTempFile(temp);
					dataFile = Morpho.thisStaticInstance
							.getLocalDataStoreService().openTempFile(urlinfo);
				} catch (Exception ex) {
					Log
							.debug(5,
									"Some problem while writing data files has occurred!");
					ex.printStackTrace();
					return false;
				}
			} catch (Exception q) {
				// some other problem has occured
				Log.debug(5,
						"Some problem with saving data files has occurred!");
				q.printStackTrace();
				return false;
			}
			// now copy dataFile
			try {
				String dataPath = path + "/" + urlinfo;
				File saveDatadir = new File(dataPath);
				saveDatadir.mkdirs(); // create the new directory

				String fosname = dataPath + "/" + origFileName;
				FileInputStream fis = new FileInputStream(dataFile);
				FileOutputStream fos = new FileOutputStream(fosname);
				BufferedInputStream bis = new BufferedInputStream(fis);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				int d = bis.read();
				while (d != -1) {
					bos.write(d); // write out everything in the reader
					d = bis.read();
				}
				bis.close();
				bos.flush();
				bos.close();
			} catch (Exception f) {
				Log.debug(20, "Error exporting data file! (AbstractDataPackage)");
				return false;
			}

		}
		return true;
	}
	
	/**
	 * Returns File for given entityIndex for the given ADP
	 * @param adp
	 * @param entityIndex
	 * @return
	 */
	public File getEntityFile(AbstractDataPackage adp, int entityIndex) {

		File entityFile = null;
		String inline = adp.getDistributionInlineData(entityIndex, 0, 0);

		if (inline.length() > 0) { // there is inline data

			String encMethod = adp.getEncodingMethod(entityIndex, 0);
			if ((encMethod.indexOf("Base64") > -1)
					|| (encMethod.indexOf("base64") > -1)
					|| (encMethod.indexOf("Base 64") > -1)
					|| (encMethod.indexOf("base 64") > -1)) {
				// is Base64
				byte[] decodedData = Base64.decode(inline);
				ByteArrayInputStream bais = new ByteArrayInputStream(decodedData);
				entityFile = Morpho.thisStaticInstance.getLocalDataStoreService().saveTempDataFile(adp.getAccessionNumber(), bais);
			} else {
				// is assumed to be text
				InputStream inlineStream = new ByteArrayInputStream(inline.getBytes(Charset.forName("UTF-8")));
				entityFile = Morpho.thisStaticInstance.getLocalDataStoreService().saveTempDataFile(adp.getAccessionNumber(), inlineStream);
			}
		} else if (adp.getDistributionUrl(entityIndex, 0, 0).length() > 0) {

			String urlinfo = adp.getDistributionUrl(entityIndex, 0, 0);
			// assumed that urlinfo is of the form
			// 'protocol://systemname/localid/other'
			// protocol is probably 'ecogrid'; system name is 'knb'
			// we just want the local id here
			int indx2 = urlinfo.lastIndexOf("/");
			if (indx2 == -1) {
				Log.debug(15, "Distribution URL is not in the right format! So data couldnt be retrieved");
				return null;
			}
			urlinfo = urlinfo.substring(indx2 + 1);
			if (urlinfo.length() == 0) {
				Log.debug(15, "Distribution URL is not in the right format! So data couldnt be retrieved");
				return null;
			}
			// we now have the id
			try {
				String loc = adp.getLocation();
				if ((loc.equals(DataPackageInterface.LOCAL)) || (loc.equals(DataPackageInterface.BOTH))) {
					entityFile = Morpho.thisStaticInstance.getLocalDataStoreService().openFile(urlinfo);
				} else if (loc.equals(DataPackageInterface.NETWORK)) {
					entityFile = Morpho.thisStaticInstance.getDataONEDataStoreService().openFile(urlinfo);
				} else if (loc.equals("")) { // just created the package; not yet saved!!!
					try {
						entityFile = Morpho.thisStaticInstance.getLocalDataStoreService().getDataFileFromAllLocalSources(urlinfo);
					} catch (Exception eee) {
						Log.debug(15, "Exception opening datafile after trying all sources!");
						// try getting it from 
						entityFile = Morpho.thisStaticInstance.getDataONEDataStoreService().openFile(urlinfo);
						return null;
					}
				}
			} catch (Exception q) {
				Log.debug(15, "Exception opening file!");
				q.printStackTrace();
			}
		} else if (adp.getDistributionArray(entityIndex, 0) == null) {
			// case where there is no distribution data in the package
			Log.debug(10, "The selected entity has NO distribution information!");
			return null;
		}

		if (entityFile == null) {
			Log.debug(15, "Unable to get the selected entity's data file!");
			return null;
		}

		return entityFile;
	}
	
	/**
	 * serialize the package to the indicated location
	 * @param mdp
	 * @param location
	 * @throws Exception 
	 */
	public void save(MorphoDataPackage mdp, String location) throws Exception {
		save(mdp, location, false);
	}
	
	/**
	 * serialize the package to the indicated location
	 * 
	 * @param adp
	 * @param location
	 * @param overwrite -- can bypass the id conflict for local saves - consider removing!!
	 * @throws Exception 
	 */
	public void save(MorphoDataPackage mdp, String location, boolean overwrite) throws Exception {

		// handle identifier conflicts
		mdp = resolveAllIdentifierConflicts(mdp, location);
		
		// make sure the size and checksum are correct
		calculateStats(mdp);

		if (location.equals(DataPackageInterface.NETWORK)) {
			Morpho.thisStaticInstance.getDataONEDataStoreService().save(mdp);
		}

		// save doc to local file system
		if (location.equals(DataPackageInterface.LOCAL)) {
			Morpho.thisStaticInstance.getLocalDataStoreService().save(mdp);
		}
		
		if (location.equals(DataPackageInterface.BOTH)) {
			// save locally
			Morpho.thisStaticInstance.getLocalDataStoreService().save(mdp);
			// save to network
			Morpho.thisStaticInstance.getDataONEDataStoreService().save(mdp);
		}
		
		// special case
		if (location.equals(DataPackageInterface.INCOMPLETE)) {
			Morpho.thisStaticInstance.getLocalDataStoreService().saveIncomplete(mdp);
		}
	}
	
	/**
	 * Calculates crucial SystemMetadata fields before saving. These include:
	 * size (bytes)
	 * checksum
	 * formatId (in cases where the EML version was updated since initial open)
	 * owner (login changed)
	 * authoritative MN (switched MN preference)
	 * @throws Exception
	 */
	private void calculateStats(MorphoDataPackage mdp) throws Exception {
		// calculate and set crucial fields that may have changed
		AbstractDataPackage adp = mdp.getAbstractDataPackage();
		
            File metadataFile = null;
            try {
                metadataFile = Morpho.thisStaticInstance.getLocalDataStoreService().lookUpLocalFile(adp.getIdentifier().getValue());
                adp.setDataSource(new FileDataSource(metadataFile));
            } catch (FileNotFoundException e) {
                metadataFile = null;
            }
            if(metadataFile == null) {
                String temp = XMLUtil.getDOMTreeAsString(adp.getMetadataNode().getOwnerDocument());
                File metadataCacheFile = Morpho.thisStaticInstance.getLocalDataStoreService().saveCacheDataFile(adp.getIdentifier().getValue(), new ByteArrayInputStream(temp.getBytes(IdentifierFileMap.UTF8)));
                adp.setDataSource(new FileDataSource(metadataCacheFile));
            }
           
      
		SystemMetadata sysmeta = adp.getSystemMetadata();
		
		// checksum and size
		Checksum checksum = ChecksumUtil.checksum(adp.getData(), "MD5");
		sysmeta.setChecksum(checksum);
		BigInteger size = BigInteger.valueOf(adp.getData().length);
		sysmeta.setSize(size);
		sysmeta.setDateSysMetadataModified(Calendar.getInstance().getTime());
		ObjectFormatIdentifier formatId = new ObjectFormatIdentifier();
		formatId.setValue(adp.getXMLNamespace());	
		sysmeta.setFormatId(formatId);
		// set owner as the current user
		Subject rightsHolder = new Subject();
		rightsHolder.setValue(Morpho.thisStaticInstance.getUserName());
		sysmeta.setRightsHolder(rightsHolder);
				
		// try to set authoritative MN
		String authMnString = Morpho.thisStaticInstance.getDataONEDataStoreService().getActiveMNode().getNodeId();
		if (authMnString != null) {
			NodeReference authMn = new NodeReference();
			authMn.setValue(authMnString);
			sysmeta.setAuthoritativeMemberNode(authMn);
		}

	}
	
	private String resolveIdentifierConflict(String originalIdentifier, String location) throws Exception {

		String newIdentifier = originalIdentifier;
		// TODO provide these from input?
		String scheme = UUID;
		String fragment = Morpho.thisStaticInstance.getProfile().get("scope", 0);
		boolean local = false;
		boolean network = false;
		if (location.equals(DataPackageInterface.NETWORK) || location.equals(DataPackageInterface.BOTH)) {
			network = Morpho.thisStaticInstance.getDataONEDataStoreService().exists(originalIdentifier);
		}
		if (location.equals(DataPackageInterface.LOCAL) || location.equals(DataPackageInterface.BOTH)) {
			local = Morpho.thisStaticInstance.getLocalDataStoreService().exists(originalIdentifier);
		}
		
		// check conflict on network
		if (network) {
			// generate network id
			newIdentifier = Morpho.thisStaticInstance.getDataONEDataStoreService().generateIdentifier(scheme, fragment);
			// let this cascade to local
			local = Morpho.thisStaticInstance.getLocalDataStoreService().exists(newIdentifier);
			// TODO: what if it is already taken?
		}
		
		// resolve conflict locally
		if (local) {
			// generate a local id
			newIdentifier = Morpho.thisStaticInstance.getLocalDataStoreService().generateIdentifier(scheme, fragment);
		}
		
		return newIdentifier;
	}
	
	/**
	 * 
	 * @param mdp
	 * @param location
	 */
	private MorphoDataPackage resolveAllIdentifierConflicts(MorphoDataPackage mdp, String location) throws Exception {

		AbstractDataPackage adp = mdp.getAbstractDataPackage();
		String originalIdentifier = adp.getAccessionNumber();
		String newIdentifier = resolveIdentifierConflict(originalIdentifier, location);
		
		// update the package to use new id if we need to
		if (!newIdentifier.equals(originalIdentifier)) {
			
			// set the new id
			adp.setAccessionNumber(newIdentifier);
			adp.setPackageIDChanged(true);

			// set the SM to reflect this change
			Identifier originalIdentifierObject = new Identifier();
			originalIdentifierObject.setValue(originalIdentifier);
			adp.getSystemMetadata().setObsoletes(originalIdentifierObject);
			
			// make sure the package reflects the updated IDs
			mdp.updateIdentifier(originalIdentifier, newIdentifier);

			// record this in revision manager (TODO: is this needed?)
			if (location.equals(DataPackageInterface.NETWORK) || location.equals(DataPackageInterface.BOTH)) {
				Morpho.thisStaticInstance.getDataONEDataStoreService().getRevisionManager().setObsoletes(newIdentifier, originalIdentifier);
			}
			if (location.equals(DataPackageInterface.LOCAL) || location.equals(DataPackageInterface.BOTH)) {
				Morpho.thisStaticInstance.getLocalDataStoreService().getRevisionManager().setObsoletes(newIdentifier, originalIdentifier);
			}
			
		}
		
		// handle the data files
		if (adp.getEntityArray() != null) {

			for (int i = 0; i < adp.getEntityArray().length; i++) {
				Entity entity = adp.getEntity(i);
				String URLinfo = adp.getDistributionUrl(i, 0, 0);
				String protocol = AbstractDataPackage.getUrlProtocol(URLinfo);
				if (protocol != null && protocol.equals(AbstractDataPackage.ECOGRID)) {

					String originalDataIdentifier = AbstractDataPackage.getUrlInfo(URLinfo);
					Log.debug(30, "handle data file  with index " + i + "" + originalDataIdentifier);
					if (originalDataIdentifier != null) {
						boolean isDirty = adp.containsDirtyEntityIndex(i);
						Log.debug(30, "url " + originalDataIdentifier + " with index " + i + " is dirty " + isDirty);

						// if we need to save, then we check ID
						// TODO: what about new packages?
						if (isDirty) {
							// see what the next identifier should be
							String newDataIdentifier = resolveIdentifierConflict(originalDataIdentifier, location);
							
							// update the docid if a change is needed
							if (!originalDataIdentifier.equals(newDataIdentifier)) {
								// new id
								Identifier newId = new Identifier();
								newId.setValue(newDataIdentifier);
								entity.getSystemMetadata().setIdentifier(newId);
								// obsoletes chain
								Identifier obsoletes = new Identifier();
								obsoletes.setValue(originalDataIdentifier);
								entity.getSystemMetadata().setObsoletes(obsoletes);
								
								// save the revision history
								if (location.equals(DataPackageInterface.NETWORK) || location.equals(DataPackageInterface.BOTH)) {
									Morpho.thisStaticInstance.getDataONEDataStoreService().getRevisionManager().setObsoletes(newDataIdentifier, originalDataIdentifier);
								}
								if (location.equals(DataPackageInterface.LOCAL) || location.equals(DataPackageInterface.BOTH)) {
									Morpho.thisStaticInstance.getLocalDataStoreService().getRevisionManager().setObsoletes(newDataIdentifier, originalDataIdentifier);
								}
								
								// update the package with new id information
								mdp.updateIdentifier(originalDataIdentifier, newDataIdentifier);
								// we changed the identifier
								Log.debug(30,
										"The identifier "
												+ originalDataIdentifier
												+ " exists and has unsaved data. The identifier for next revision is "
												+ newDataIdentifier);
	
								// update the EML with new id information
								String urlinfo = DataLocation.URN_ROOT + newDataIdentifier;
								adp.setDistributionUrl(i, 0, 0, urlinfo);
							}
						}
					}
				}
			}
		}

		return mdp;

	}

	/**
	 * serialize the package to the indicated location
	 * 
	 * @param adp
	 * @param location
	 * @param overwrite -- can bypass the id conflict for local saves - consider removing!!
	 * @throws Exception 
	 */
	public InputStream query(String query, String location) throws Exception {

		InputStream results = null;
		if (location.equals(DataPackageInterface.NETWORK)) {
			results = Morpho.thisStaticInstance.getDataONEDataStoreService().query(query);
		}

		// TODO: other query locations
		
		return results;
		
	}
	
	
	
	/**
	 * Does this id exist at the given location?
	 * @param docid
	 * @param location
	 * @return
	 */
	public boolean exists(String docid, String location) {
		
		boolean exists = false;
		if ((location.equals(DataPackageInterface.LOCAL)) || (location.equals(DataPackageInterface.BOTH))) {
			exists = Morpho.thisStaticInstance.getLocalDataStoreService().exists(docid);
		} else {
			try {
				exists = Morpho.thisStaticInstance.getDataONEDataStoreService().exists(docid);
			} catch (Exception e) {
				Log.debug(5, e.getMessage());
				e.printStackTrace();
			}
		}
		return exists;
		
	}

	/**
	 * Set the ReplicationPolicy for the given object at the given location
	 * @param d1Object
	 * @param location
	 * @return
	 */
	public boolean setReplicationPolicy(D1Object d1Object, String location) throws Exception {
		boolean success = false;
		// try local first -- this should go off without a hitch
		if ((location .equals(DataPackageInterface.LOCAL)) || (location.equals(DataPackageInterface.BOTH))) {
			success = Morpho.thisStaticInstance.getLocalDataStoreService().setReplicationPolicy(d1Object.getSystemMetadata());
		}
		if ((location .equals(DataPackageInterface.NETWORK)) || (location.equals(DataPackageInterface.BOTH))) {
			success = success && Morpho.thisStaticInstance.getDataONEDataStoreService().setReplicationPolicy(d1Object.getSystemMetadata());		
		}
		return success;
	}
	
	/**
	 * Set the AccessPolicy for the given object at the given location
	 * @param d1Object
	 * @param location
	 * @return
	 */
	public boolean setAccessPolicy(D1Object d1Object, String location) throws Exception {
		boolean success = false;
		// try local first -- this should go off without a hitch
		if ((location.equals(DataPackageInterface.LOCAL)) || (location.equals(DataPackageInterface.BOTH))) {
			success = Morpho.thisStaticInstance.getLocalDataStoreService().setAccessPolicy(d1Object.getSystemMetadata());
		}
		if ((location.equals(DataPackageInterface.NETWORK)) || (location.equals(DataPackageInterface.BOTH))) {
			success = success && Morpho.thisStaticInstance.getDataONEDataStoreService().setAccessPolicy(d1Object.getSystemMetadata());		
		}
		// TODO: allow this? save change locally so they are not lost?
		if (location.equals(DataPackageInterface.TEMPLOCATION)) {
			//success = Morpho.thisStaticInstance.getLocalDataStoreService().setAccessPolicy(d1Object.getSystemMetadata());
			throw new Exception("Cannot set AccessPolicy on unsaved object");
		}
		return success;
	}

}
