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
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JOptionPane;

import edu.ucsb.nceas.morpho.Language;
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.query.LocalQuery;
import edu.ucsb.nceas.morpho.util.IOUtil;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.XMLTransformer;

/**
 * This singleton class allows callers to interact with data store services for
 * a given location.
 * 
 * @author leinfelder
 * 
 */
public class DataStoreServiceController {

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
	 * Read file from given location
	 * 
	 * @param id
	 * @param location
	 * @return
	 * @throws Throwable
	 */
	public File openFile(String id, String location) throws Throwable {

		File returnFile = null;
		if (location.equals(DataPackageInterface.METACAT)) {
			try {
				Log.debug(11, "opening metacat file");
				returnFile = Morpho.thisStaticInstance.getMetacatDataStore().openFile(id);
				Log.debug(11, "metacat file opened");
			} catch (FileNotFoundException fnfe) {
				Log.debug(0, "Error in DataPackage.getFileFromDataStore(): "
						+ "metacat file not found: " + fnfe.getMessage());
				fnfe.printStackTrace();
				throw fnfe.fillInStackTrace();

			} catch (CacheAccessException cae) {
				Log.debug(0, "Error in DataPackage.getFileFromDataStore(): "
						+ "metacat cache problem: " + cae.getMessage());
				cae.printStackTrace();
				throw cae.fillInStackTrace();
			}
		} else { // not metacat
			try {
				Log.debug(11, "opening local file");
				returnFile = Morpho.thisStaticInstance.getFileSystemDataStore().openFile(id);
				Log.debug(11, "local file opened");
			} catch (FileNotFoundException fnfe) {
				Log.debug(0, "Error in DataPackage.getFileFromDataStore(): "
						+ "local file not found: " + fnfe.getMessage());
				fnfe.printStackTrace();
				throw fnfe.fillInStackTrace();
			}
		}
		return returnFile;
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

}
