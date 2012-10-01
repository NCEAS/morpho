/**
 * 
 */
package edu.ucsb.nceas.morpho.datastore;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JOptionPane;

import edu.ucsb.nceas.morpho.Language;
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.DataPackageFactory;
import edu.ucsb.nceas.morpho.datapackage.DocidConflictHandler;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.query.LocalQuery;
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

	private static DataStoreServiceController instance;
	
	// store the map between new data id and old data id
	private Hashtable<String, String> original_new_id_map = new Hashtable<String, String>(); 

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
	 */
	public int getNextRevisionNumber(String docid, String location)
	{
		int version = AbstractDataPackage.ORIGINAL_REVISION;
		if (location.equals(DataPackageInterface.LOCAL)) {
			version = Morpho.thisStaticInstance.getFileSystemDataStore().getNextRevisionNumber(docid);
		}
		if (location.equals(DataPackageInterface.METACAT)) {
			version = Morpho.thisStaticInstance.getMetacatDataStore().getNextRevisionNumberFromMetacat(docid);
		}
		if (location.equals(DataPackageInterface.BOTH)) {
			int localNextRevision = Morpho.thisStaticInstance.getFileSystemDataStore().getNextRevisionNumber(docid);
			int metacatNextRevision = Morpho.thisStaticInstance.getMetacatDataStore().getNextRevisionNumberFromMetacat(docid);
			version = Math.max(localNextRevision, metacatNextRevision);
		}
		
		return version;
	}
	
	/**
	 * Create a new Datapackage given a docid of a metadata object and a
	 * location
	 */
	public AbstractDataPackage read(String docid, String location) {
		// first use datastore package to get a stream for the metadata
		// read the stream. figure out the docType(i.e. emlbeta6, eml2, nbii, etc)
		// then create the appropriate subclass of AbstractDataPackage and
		// return it.

		Reader in = null;
		if ((location.equals(DataPackageInterface.LOCAL)) || (location.equals(DataPackageInterface.BOTH))) {
			try {
				File file = Morpho.thisStaticInstance.getFileSystemDataStore().openFile(docid);
				in = new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8"));
			} catch (Exception w) {
				Log.debug(20, "Problem opening file!");
			}
		} else { 
			// must be on metacat only
			try {
				File file = Morpho.thisStaticInstance.getMetacatDataStore().openFile(docid);
				in = new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8"));
			} catch (Exception e) {
				Log.debug(20, "Problem opening file from Metacat!");
			}
		}
		
		return DataPackageFactory.getDataPackage(in);
	}
	
	/**
     * Gets the docid attribute for given location. 
     *
     * @param scope  scope of the docid
     * @return       the last docid used for the location (n.b., "scope.docid.rev")
     */
    public String getLastDocid(String scope, String location)
    {
    	
    	// during start up this can be null
    	if (Morpho.thisStaticInstance.getFileSystemDataStore() == null || Morpho.thisStaticInstance.getFileSystemDataStore() == null) {
    		return null;
    	}
    	
        String result = null;
        
        //localMaxDocid will be 54 if the biggest file name is 54.2
        int localMaxDocid = Morpho.thisStaticInstance.getFileSystemDataStore().getLastDocid(scope);
        Log.debug(30, "the last id locally ===== " + localMaxDocid);

        //get the metacat last docid
        int metacatId  =  Morpho.thisStaticInstance.getMetacatDataStore().getLastDocid(scope);
        Log.debug(30, "the last id from metacat ===== " + metacatId);
		
		if (location.equals(DataPackageInterface.LOCAL)) {
	        result = String.valueOf(localMaxDocid);
		}
		if (location.equals(DataPackageInterface.METACAT)) {
	        result = String.valueOf(metacatId);
		}
		if (location.equals(DataPackageInterface.BOTH)) {
			// pick the highest
	        result = String.valueOf(Math.max(localMaxDocid, metacatId));
		}
              
        Log.debug(30, "Final Last id is " + result);
        return result;
    }
    
    /**
	 * returns the next id for the given location
	 * for the current scope
	 * 
	 */
	public synchronized String getNextId(String location) {
		long lastid = -1;
		String scope = Morpho.thisStaticInstance.getProfile().get("scope", 0);
		// Get last id from metacat and local system
		String lastIdString = DataStoreServiceController.getInstance().getLastDocid(scope, location);
		if (lastIdString != null) {
			lastid = (new Long(lastIdString).longValue());
			// in order to get next id, this number should be increase 1
			lastid++;
		}

		Log.debug(30, "the last id: " + lastid);

		String separator = Morpho.thisStaticInstance.getProfile().get("separator", 0);

		String identifier = scope + separator + lastid;
		lastid++;
		String s = "" + lastid;
		if (!Morpho.thisStaticInstance.getProfile().set("lastId", 0, s)) {
			Log.debug(1, "Error incrementing the accession number id");
			return null;
		} else {
			Morpho.thisStaticInstance.getProfile().save();
			Log.debug(30, "the next id is " + identifier + ".1");
			return identifier + ".1";
		}
	}

	/**
	 * Deletes the package from the specified location
	 * 
	 * @param locattion
	 *            the location of the package that you want to delete use either
	 *            BOTH, METACAT or LOCAL
	 */

	public void delete(AbstractDataPackage adp, String location)
			throws Exception {
		boolean metacatLoc = false;
		boolean localLoc = false;
		String accnum = adp.getAccessionNumber();

		if (location.equals(DataPackageInterface.METACAT) || location.equals(DataPackageInterface.BOTH)) {
			metacatLoc = true;
		}
		if (location.equals(DataPackageInterface.LOCAL) || location.equals(DataPackageInterface.BOTH)) {
			localLoc = true;
		}
		if (localLoc) {
			boolean localSuccess = Morpho.thisStaticInstance.getFileSystemDataStore().deleteFile(accnum);
			if (!localSuccess) {
				throw new Exception("User couldn't delete the local copy");
			}
			LocalQuery.removeFromCache(accnum);
		}
		if (metacatLoc) {
			boolean success = Morpho.thisStaticInstance.getMetacatDataStore().deleteFile(accnum);
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
	public void export(AbstractDataPackage adp, String path) {
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
		} else if (location.equals(DataPackageInterface.METACAT)) {
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
			InputStream input = this.getClass().getResourceAsStream(
					"/style/CSS/export.css");
			InputStreamReader styleSheetReader = new InputStreamReader(input);
			// FileReader styleSheetReader = new FileReader(styleSheetSource);
			StringBuffer buffer = IOUtil.getAsStringBuffer(styleSheetReader,
					true);
			// Create a wrter
			String fileName = cssPath + "/export.css";
			Writer writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(fileName), Charset.forName("UTF-8")));
			IOUtil.writeToWriter(buffer, writer, true);
		} catch (Exception e) {
			Log.debug(30, "Error in copying css: " + e.getMessage());
		}

		// for metadata file
		f = new File(sourcePath + "/" + id);

		File openfile = null;
		try {
			if (localloc) { // get the file locally and save it
				openfile = Morpho.thisStaticInstance.getFileSystemDataStore()
						.openFile(id);
			} else if (metacatloc) { // get the file from metacat
				openfile = Morpho.thisStaticInstance.getMetacatDataStore()
						.openFile(id);
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

			// css file
			/*
			 * File outputCSSFile = new File(cssPath + "/export.css"); // File
			 * inputCSSFile = new
			 * File(getClass().getResource("/style/export.css")); //
			 * FileInputStream inputCSS = new FileInputStream(inputCSSFile);
			 * FileInputStream inputCSS = (FileInputStream)
			 * getClass().getResource("/style/export.css"); BufferedInputStream
			 * inputBufferedCSS = new BufferedInputStream(inputCSS);
			 * FileOutputStream outputCSS = new FileOutputStream(outputCSSFile);
			 * BufferedOutputStream outputBufferedCSS = new
			 * BufferedOutputStream( outputCSS); c = inputBufferedCSS.read();
			 * while (c != -1) { //copy the files to the source directory
			 * outputBufferedCSS.write(c); c = inputBufferedCSS.read(); }
			 * outputBufferedCSS.flush(); inputBufferedCSS.close();
			 * outputBufferedCSS.close();
			 */
			// for html
			Reader xmlInputReader = null;
			Reader result = null;
			StringBuffer tempPathBuff = new StringBuffer();
			xmlInputReader = new InputStreamReader(
					new FileInputStream(openfile), Charset.forName("UTF-8"));

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
				Log.debug(9, "Unexpected Error Styling Document: "
						+ e.getMessage());
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
				Log.debug(9, "Unexpected Error Reading Styled Document: "
						+ e.getMessage());
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
	public void exportToZip(AbstractDataPackage adp, String path) {
		try {
			String id = adp.getAccessionNumber();
			// export the package in an uncompressed format to the temp
			// directory
			// then zip it up and save it to the specified path
			String tempdir = ConfigXML.getConfigDirectory() + File.separator + Morpho.getConfiguration().get("tempDir", 0);
			export(adp, tempdir + "/tmppackage");
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
							.getFileSystemDataStore().openFile(urlinfo);
				} else if (location.equals(DataPackageInterface.METACAT)) {
					dataFile = Morpho.thisStaticInstance.getMetacatDataStore()
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
							.getFileSystemDataStore().openTempFile(urlinfo);
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
	 * This method loops through all the entities in a package and checks for
	 * url references to data files (i.e. data external to the data package).
	 * Both metatcat and local file stores are checked to see if the data has
	 * already been saved. If not, the temp directory is checked. Note that it
	 * is assumed that the data file has been assigned an id and stored in the
	 * temp directory if it has not been saved to one of the stores
	 * 
	 * It has been assumed that the 'location' has been set to point to the
	 * place where the data is to be saved.
	 */
	private void serializeData(AbstractDataPackage adp, String dataDestination) {
		Log.debug(30, "serilaize data =====================");
		adp.getEntityArray();
		// Log.debug(1, "About to check entityArray!");
		if (adp.getEntityArray() == null) {
			Log.debug(30, "Entity array is null, no need to serialize data");
			return; // there is no data!
		}
		if (dataDestination == null) {
			Log.debug(30, "User didn't specify the data destination");
			return;
		}

		adp.setDataIDChanged(false);
		for (int i = 0; i < adp.getEntityArray().length; i++) {
			String URLinfo = adp.getDistributionUrl(i, 0, 0);
			String protocol = AbstractDataPackage.getUrlProtocol(URLinfo);
			String objectName = adp.getPhysicalName(i, 0);
			Log.debug(25, "object name is ===================== " + objectName);
			if (protocol != null && protocol.equals(AbstractDataPackage.ECOGRID)) {

				String docid = AbstractDataPackage.getUrlInfo(URLinfo);
				Log.debug(30, "handle data file  with index " + i + "" + docid);
				if (docid != null) {
					boolean isDirty = adp.containsDirtyEntityIndex(i);
					Log.debug(30, "url " + docid + " with index " + i
							+ " is dirty " + isDirty);
					// Detects if docid conflict occurs
					// boolean updateFlag = !(version.equals("1"));
					// boolean existFlag = false;
					boolean existInMetacat = false;
					boolean existInLocal = false;
					String statusInMetacat = null;
					String statusInLocal = null;
					String conflictLocation = null;
					// Check to see if id conflict or not
					if ((dataDestination.equals(DataPackageInterface.METACAT))) {
						statusInMetacat = Morpho.thisStaticInstance.getMetacatDataStore().status(docid);
						Log.debug(30, "docid " + docid
								+ " status in metacat is " + statusInMetacat);
						if (statusInMetacat != null
								&& statusInMetacat.equals(DataStoreInterface.CONFLICT)) {
							conflictLocation = DocidConflictHandler.METACAT;
							existInMetacat = true;
						}
					} else if ((dataDestination.equals(DataPackageInterface.LOCAL))) {
						statusInLocal = Morpho.thisStaticInstance.getFileSystemDataStore().status(docid);
						Log.debug(30, "docid " + docid + " status in local is "
								+ statusInLocal);
						if (statusInLocal != null
								&& statusInLocal.equals(DataStoreInterface.CONFLICT)) {
							conflictLocation = DocidConflictHandler.LOCAL;
							existInLocal = true;
						}
					} else if (dataDestination.equals(DataPackageInterface.BOTH)) {
						statusInMetacat = Morpho.thisStaticInstance
								.getMetacatDataStore().status(docid);
						statusInLocal = Morpho.thisStaticInstance
								.getFileSystemDataStore().status(docid);
						Log.debug(30, "docid " + docid + " status in local is "
								+ statusInLocal + " and status in metacat is"
								+ statusInMetacat);
						if (statusInMetacat != null
								&& statusInLocal != null
								&& statusInLocal.equals(DataStoreInterface.CONFLICT)
								&& statusInMetacat.equals(DataStoreInterface.CONFLICT)) {
							conflictLocation = DocidConflictHandler.LOCAL
									+ " and " + DocidConflictHandler.METACAT;
							existInMetacat = true;
							existInLocal = true;
							// existFlag = true;
							//this.setIdentifierChangedInLocalSerialization(true
							// );
							// this.setIdentifierChangedInMetacatSerialization(
							// true);
						} else if (statusInMetacat != null
								&& statusInMetacat
										.equals(DataStoreInterface.CONFLICT)) {
							conflictLocation = DocidConflictHandler.METACAT;
							existInMetacat = true;
							// existFlag = true;
							// this.setIdentifierChangedInMetacatSerialization(
							// true);
						} else if (statusInLocal != null
								&& statusInLocal
										.equals(DataStoreInterface.CONFLICT)) {
							conflictLocation = DocidConflictHandler.LOCAL;
							existInLocal = true;
							// existFlag = true;
							//this.setIdentifierChangedInLocalSerialization(true
							// );
						}

					}

					if (conflictLocation != null && isDirty) {
						// If docid conflict and the entity is dirty, we need to
						// pop-out an window to change the docid
						Log.debug(30, "The docid "
									+ docid
									+ " exists and has unsaved data. So increase docid for it");
						docid = handleDataIdConfiction(adp, docid, conflictLocation);

					}

					// reset urlinfo with new docid (if docid was not changed, the url will still be same).

					// urlinfo should be the id in a string
					if (dataDestination.equals(DataPackageInterface.LOCAL)
							|| dataDestination.equals(DataPackageInterface.BOTH)) {
						if (isDirty || !existInLocal) {
							handleLocal(docid);
						}
					}

					if (dataDestination.equals(DataPackageInterface.METACAT)
							|| dataDestination.equals(DataPackageInterface.BOTH)) {
						if (isDirty || !existInMetacat) {
							handleMetacat(docid, objectName);
						}
					}

					if (dataDestination.equals(DataPackageInterface.INCOMPLETE)) {
						handleIncompleteDir(docid);
					}

					// reset the map after finishing save. There is no need for
					// this pair after saving
					original_new_id_map = new Hashtable<String, String>();

					// newDataFile must have worked; thus update the package
					String urlinfo = "ecogrid://knb/" + docid;
					adp.setDistributionUrl(i, 0, 0, urlinfo);
					// File was saved successfully, we need to remove the index
					// from the vector.
					if (isDirty) {
						adp.removeDirtyEntityIndex(i);
					}
				}
				// }
			}
		}

		// Log.debug(1, "~~~~~~~~~~~~~~~~~~~~~~set bothLoation false ");
		// serializeDataAtBothLocation =false;
	}

	/**
	 * Saves the entity into local system
	 */
	private void handleLocal(String docid) {
		Log.debug(30, "~~~~~~~~~~~~~~~~~~~~~~handle local " + docid);
		File dataFile = null;
		String oldDocid = null;
		// if morpho serilaize data into local or metacat, and docid was
		// changed,
		// we need to get the old docid and find the it in temp dir
		if (!original_new_id_map.isEmpty()) {
			// System.out.println("the key is "+urlinfo);
			// System.out.println("the hashtable is "+original_new_id_map);
			// Log.debug(1,
			// "~~~~~~~~~~~~~~~~~~~~~~change id in local serialization ");
			oldDocid = (String) original_new_id_map.get(docid);
			Log.debug(30, "~~~~~~~~~~~~~~~~~~~~~~the id from map is " + oldDocid);

		}
		// if oldDocid is null, that means docid change. So we set old docid to
		// be the current id
		if (oldDocid == null) {
			oldDocid = docid;
		}
		Log.debug(30, "~~~~~~~~~~~~~~~~~~~~~~eventually old id is  " + oldDocid);
		
		try {
			// dataFile = fds.openTempFile(temp);
			dataFile = Morpho.thisStaticInstance.getFileSystemDataStore().openTempFile(oldDocid);
			// open old file name (if no file change, the old file name will be as same as docid).
			InputStream dfis = new FileInputStream(dataFile);
			// Log.debug(1, "ready to save: urlinfo: "+urlinfo);
			Morpho.thisStaticInstance.getFileSystemDataStore().saveDataFile(docid, dfis);
			// the temp file has been saved; thus delete
			dfis.close();
			// dataFile.delete();
		} catch (Exception qq) {
			// try to open incomplete file
			try {
				dataFile = Morpho.thisStaticInstance.getFileSystemDataStore().openIncompleteFile(oldDocid);
				// open old file name (if no file change, the old file name will
				// be as same as docid).
				InputStream dfis = new FileInputStream(dataFile);
				// Log.debug(1, "ready to save: urlinfo: "+urlinfo);
				Morpho.thisStaticInstance.getFileSystemDataStore().saveDataFile(docid, dfis);
				// the temp file has been saved; thus delete
				dfis.close();
			} catch (Exception e) {
				// if a datafile is on metacat and one wants to save locally
				try {
					// open old file name (if no file change, the old file name
					// will be as same as docid).
					dataFile = Morpho.thisStaticInstance.getMetacatDataStore().openDataFile(oldDocid);
					InputStream dfis = new FileInputStream(dataFile);
					Morpho.thisStaticInstance.getFileSystemDataStore().saveDataFile(docid, dfis);
					dfis.close();
				} catch (Exception qqq) {
					// some other problem has occured
					Log.debug(5, "Some problem with saving local data files has occurred! "
									+ qqq.getMessage());
					qq.printStackTrace();
				}// end catch
			}
		}

	}

	/**
	 * Saves data files to Metacat
	 */
	private void handleMetacat(String docid, String objectName) {
		Log.debug(30, "----------------------------------------handle metacat "
				+ docid);
		File dataFile = null;
		String oldDocid = null;
		ConfigXML profile = Morpho.thisStaticInstance.getProfile();
		String separator = profile.get("separator", 0);
		separator = separator.trim();
		if (!original_new_id_map.isEmpty()) {
			// System.out.println("the key is "+urlinfo);
			// System.out.println("the hashtable is "+original_new_id_map);
			// Log.debug(1,
			// "~~~~~~~~~~~~~~~~~~~~~~change id in local serialization ");
			oldDocid = (String) original_new_id_map.get(docid);
			Log.debug(30, "~~~~~~~~~~~~~~~~~~~~~~the id from map is "
					+ oldDocid);

		}
		// if oldDocid is null, that means docid change. So we set old docid to
		// be the current id
		if (oldDocid == null) {
			oldDocid = docid;
		}
		try {
			dataFile = Morpho.thisStaticInstance.getFileSystemDataStore().getDataFileFromAllLocalSources(docid);
		} catch (Exception eee) {
			Log.debug(5,"Couldn't find "
						+ oldDocid
						+ " in local system, so morpho couldn't upload it to metacat");
			return;
		}

		try {
			Morpho.thisStaticInstance.getMetacatDataStore().newDataFile(docid, dataFile, objectName);
		} catch (Exception e) {
			// Gets some error from metacat
			Log.debug(5, "Some problem with saving data files has occurred! "
					+ e.getMessage());
		}

	}

	/**
	 * If the docid is revision 1, automatically increase data file identifier
	 * number without notifying user. If the docid is bigger than revision 1,
	 * user will be asked to make a chioce: increasing docid or increasing
	 * revision.
	 */
	private String handleDataIdConfiction(AbstractDataPackage adp, String identifier, String conflictLocation) {
		String version = null;
		int revision = -1;
		String scope = null;
		boolean update = true;
		String originalIdentifier = null;
		if (identifier != null) {
			originalIdentifier = identifier;
			// get revision number
			int lastperiod = identifier.lastIndexOf(".");
			if (lastperiod > -1) {
				version = identifier.substring(lastperiod + 1, identifier.length());
				scope = identifier.substring(0, lastperiod);
				Log.debug(55, "scope: " + scope + "---version: " + version);
			}
			try {
				revision = (new Integer(version).intValue());
				if (revision == 1) {
					update = false;
				}
			} catch (Exception e) {
				Log.debug(5, "Couldn't find the revison in docid " + identifier
						+ " since " + e.getMessage());
			}

			// if it is update, we need give user options to choose: increase
			// docid or revision number
			if (update) {
				DocidConflictHandler docidIncreaseDialog = new DocidConflictHandler(
						identifier, conflictLocation);
				String choice = docidIncreaseDialog.showDialog();
				// Log.debug(5, "choice is "+choice);
				if (choice != null
						&& choice.equals(DocidConflictHandler.INCREASEID)) {
					update = false;
				} else {
					update = true;
				}
			}

			// decides docid base on user choice
			if (!update) {
				identifier = DataStoreServiceController.getInstance().getNextId(conflictLocation);
			} else {
				int newRevision = 
					DataStoreServiceController.getInstance().getNextRevisionNumber(identifier, DataPackageInterface.BOTH);
				identifier = scope + "." + newRevision;
			}
			// store the new id and original id into a map.
			// So when morpho know the new id when it serialize
			original_new_id_map.put(identifier, originalIdentifier);
			adp.setDataIDChanged(true);

		}
		Log.debug(30, "======================new identifier is " + identifier);
		return identifier;
	}

	/**
	 * Save the data file in the incomplete dir
	 */
	private void handleIncompleteDir(String docid) {
		Log.debug(30, "~~~~~~~~~~~~~~~~~~~~~~handle incomplete " + docid);
		File dataFile = null;

		try {
			dataFile = Morpho.thisStaticInstance.getFileSystemDataStore().openFile(docid);
			Log.debug(30, "Docid " + docid
							+ " exist in data dir in AbstractDataPackage.handleIncompleteDir");
			return;
		} catch (Exception m) {
			try {
				dataFile = Morpho.thisStaticInstance.getFileSystemDataStore().openTempFile(docid);
				InputStream dfis = new FileInputStream(dataFile);
				Morpho.thisStaticInstance.getFileSystemDataStore().saveIncompleteDataFile(docid, dfis);
				dfis.close();
			} catch (Exception qq) {
				// if a datafile is on metacat and user wants to save locally
				try {
					// open old file name (if no file change, the old file name will be as same as docid).
					InputStream dfis = new FileInputStream(dataFile);
					Morpho.thisStaticInstance.getFileSystemDataStore().saveIncompleteDataFile(docid, dfis);
					dfis.close();
				} catch (Exception qqq) {
					// some other problem has occured
					Log.debug(5, "Some problem with saving local data files has occurred! "
									+ qqq.getMessage());
					qq.printStackTrace();
				}// end catch
			}
		}

	}
	
	/**
	 * serialize the package to the indicated location
	 * @param adp
	 * @param location
	 */
	public void serialize(AbstractDataPackage adp, String location) {
		serialize(adp, location, false);
	}
	
	/**
	 * serialize the package to the indicated location
	 * 
	 * @param adp
	 * @param location
	 * @param overwrite
	 *            -- can bypass the id conflict for local saves - consider
	 *            removing!!
	 */
	public void serialize(AbstractDataPackage adp, String location,
			boolean overwrite) {

		// save the data first
		serializeData(adp, location);

		// now save the metadata
		adp.setSerializeLocalSuccess(false);
		adp.setSerializeMetacatSuccess(false);
		// this.setIdentifierChangedInLocalSerialization(false);
		// this.setIdentifierChangedInMetacatSerialization(false);
		adp.setPackageIDChanged(false);
		// System.out.println("serialize metadata ===============");
		String statusInMetacat = null;
		String statusInLocal = null;
		// boolean existFlag = true;
		String conflictLocation = null;
		// String temp = XMLUtilities.getDOMTreeAsString(getMetadataNode(),
		// false);
		String temp = XMLUtil.getDOMTreeAsString(adp.getMetadataNode().getOwnerDocument());
		// To check if this update or insert action
		String identifier = adp.getAccessionNumber();
		String temp2 = identifier;
		String version = null;
		int lastperiod = identifier.lastIndexOf(".");
		if (lastperiod > -1) {
			version = identifier.substring(lastperiod + 1, identifier.length());
			temp2 = temp2.substring(0, lastperiod);
			// Log.debug(1, "temp1: "+temp1+"---temp2: "+temp2);
		}
		// boolean existsFlag = mds.exists(temp2+".1");

		boolean isRevisionOne = false;
		if (version != null) {
			isRevisionOne = version.equals("1");
		} else {
			Log.debug(5,"No revision number assigned to docid. Couldn't save it");
			return;
		}
		// Check to see if id confilct or not
		if ((location.equals(DataPackageInterface.METACAT))) {
			statusInMetacat = Morpho.thisStaticInstance.getMetacatDataStore()
					.status(adp.getAccessionNumber());
			if (statusInMetacat != null
					&& statusInMetacat.equals(DataStoreInterface.CONFLICT)) {
				conflictLocation = DocidConflictHandler.METACAT;
				// this.setIdentifierChangedInMetacatSerialization(true);
			}
		} else if ((location.equals(DataPackageInterface.LOCAL))) {
			statusInLocal = Morpho.thisStaticInstance.getFileSystemDataStore()
					.status(adp.getAccessionNumber());
			// existFlag = existInLocal;
			if (statusInLocal != null
					&& statusInLocal.equals(DataStoreInterface.CONFLICT)) {
				conflictLocation = DocidConflictHandler.LOCAL;
				// this.setIdentifierChangedInLocalSerialization(true);
			}
		} else if (location.equals(DataPackageInterface.BOTH)) {
			statusInMetacat = Morpho.thisStaticInstance.getMetacatDataStore()
					.status(adp.getAccessionNumber());
			statusInLocal = Morpho.thisStaticInstance.getFileSystemDataStore()
					.status(adp.getAccessionNumber());
			// if (existFlag)
			// {
			if (statusInMetacat != null && statusInLocal != null
					&& statusInLocal.equals(DataStoreInterface.CONFLICT)
					&& statusInMetacat.equals(DataStoreInterface.CONFLICT)) {
				conflictLocation = DocidConflictHandler.LOCAL + " and " + DocidConflictHandler.METACAT;
				// this.setIdentifierChangedInLocalSerialization(true);
				// this.setIdentifierChangedInMetacatSerialization(true);
			} else if (statusInMetacat != null
					&& statusInMetacat.equals(DataStoreInterface.CONFLICT)) {
				conflictLocation = DocidConflictHandler.METACAT;
				// this.setIdentifierChangedInMetacatSerialization(true);
			} else if (statusInLocal != null
					&& statusInLocal.equals(DataStoreInterface.CONFLICT)) {
				conflictLocation = DocidConflictHandler.LOCAL;
				// this.setIdentifierChangedInLocalSerialization(true);
			}
			// }
		}

		// if we allow local overwrite, we reset confilcLocation. It will skip
		// the code to handle conflict
		if (overwrite) {
			conflictLocation = null;
		}

		// We need to change id to resolve id confilcition
		if (conflictLocation != null && !isRevisionOne) {
			Log.debug(30, "=============In existFlag and update branch");
			// ToDo - add a frame to give user option to increase docid or
			// revision
			DocidConflictHandler docidIncreaseDialog = new DocidConflictHandler(identifier, conflictLocation);
			String choice = docidIncreaseDialog.showDialog();
			// Log.debug(5, "choice is "+choice);
			if (choice != null && choice.equals(DocidConflictHandler.INCREASEID)) {
				// increase to a new id
				identifier = DataStoreServiceController.getInstance().getNextId(location);
				adp.setAccessionNumber(identifier);
				adp.setPackageIDChanged(true);
				temp = XMLUtil.getDOMTreeAsString(adp.getMetadataNode().getOwnerDocument());
				// since we changed the revision number, the status of docid
				// will be changed
				statusInMetacat = DataStoreInterface.NONEXIST;
				statusInLocal = DataStoreInterface.NONEXIST;
			} else {
				// increase revision number
				int newRevision = DataStoreServiceController.getInstance().getNextRevisionNumber(adp.getAccessionNumber(), DataPackageInterface.BOTH);
				identifier = temp2 + "." + newRevision;
				adp.setAccessionNumber(identifier);
				adp.setPackageIDChanged(true);
				temp = XMLUtil.getDOMTreeAsString(adp.getMetadataNode().getOwnerDocument());
				Log.debug(30, "The new id (after increase revision number) is " + identifier);
				statusInMetacat = DataStoreInterface.UPDATE;
				statusInLocal = DataStoreInterface.UPDATE;
			}
		} else if (conflictLocation != null) {
			Log.debug(30, "==============In existFlag and insert revision 1 branch");
			// since it is saving a new package, increase docid silently
			identifier = DataStoreServiceController.getInstance().getNextId(
					location);
			adp.setAccessionNumber(identifier);
			adp.setPackageIDChanged(true);
			temp = XMLUtil.getDOMTreeAsString(adp.getMetadataNode().getOwnerDocument());
			statusInMetacat = DataStoreInterface.NONEXIST;
			statusInLocal = DataStoreInterface.NONEXIST;
		}
		// Log.debug(30, temp);

		// save doc to metacat
		StringReader sr1 = new StringReader(temp);
		if ((location.equals(DataPackageInterface.METACAT))
				|| (location.equals(DataPackageInterface.BOTH))) {
			if (statusInMetacat != null && statusInMetacat.equals(DataStoreInterface.UPDATE)) {
				try {
					Morpho.thisStaticInstance.getMetacatDataStore().saveFile(adp.getAccessionNumber(), sr1);
					adp.setSerializeMetacatSuccess(true);
				} catch (Exception e) {
					adp.setSerializeMetacatSuccess(false);
					// this.setIdentifierChangedInMetacatSerialization(false);
					// System.out.println(
					// " in other exception Exception==========  "
					// +e.getMessage());
					Log.debug(5, "Problem with saving to metacat in EML200DataPackage!\n"
									+ e.getMessage());
				}
			} else if (statusInMetacat != null && statusInMetacat.equals(DataStoreInterface.NONEXIST)) {
				try {
					Morpho.thisStaticInstance.getMetacatDataStore().newFile(adp.getAccessionNumber(), sr1);
					adp.setSerializeMetacatSuccess(true);
					// setAccessionNumber(temp_an);
				} catch (Exception e) {
					adp.setSerializeMetacatSuccess(false);
					// this.setIdentifierChangedInMetacatSerialization(false);
					Log.debug(5, "Problem with saving to metacat in EML200DataPackage!\n"
									+ e.getMessage());
				}
			} else {
				adp.setSerializeMetacatSuccess(false);
				// this.setIdentifierChangedInMetacatSerialization(false);
				Log.debug(5, "Problem with saving to metacat in EML200DataPackage since couldn't get the docid status in Metacat");
			}
		}

		// save doc to local file system
		StringReader sr = new StringReader(temp);
		if ((location.equals(DataPackageInterface.LOCAL))
				|| (location.equals(DataPackageInterface.BOTH))) {
			// Log.debug(10,
			// "XXXXXXXXX: serializing to hardcoded /tmp/emldoc.xml");
			// fsds.saveFile("100.0",sr);
			File newFile = Morpho.thisStaticInstance.getFileSystemDataStore().saveFile(adp.getAccessionNumber(), sr);
			if (newFile != null) {
				adp.setSerializeLocalSuccess(true);
			} else {
				adp.setSerializeLocalSuccess(false);
				// this.setIdentifierChangedInLocalSerialization(false);
			}
		} else if (location.equals(DataPackageInterface.INCOMPLETE)) {
			String id = adp.getAccessionNumber();
			Log.debug(30, "Serialize metadata into incomplete dir with docid " + id);
			File newFile = Morpho.thisStaticInstance.getFileSystemDataStore().saveIncompleteFile(id, sr);
			if (newFile != null) {
				adp.setSerializeLocalSuccess(true);
			} else {
				adp.setSerializeLocalSuccess(false);
				// this.setIdentifierChangedInLocalSerialization(false);
			}
		}
	}

}
