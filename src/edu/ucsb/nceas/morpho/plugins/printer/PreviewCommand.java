/**
 *  '$RCSfile: PreviewCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: sambasiv $'
 *     '$Date: 2004-04-05 22:00:30 $'
 * '$Revision: 1.1 $'
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

package edu.ucsb.nceas.morpho.plugins.printer;

import java.awt.event.ActionEvent;
import java.io.Reader;
import java.io.IOException;
import java.io.File;
import org.w3c.dom.Document;
import java.awt.Component;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.plugins.PrinterInterface;
import edu.ucsb.nceas.morpho.plugins.printer.PrinterPlugin;
import edu.ucsb.nceas.morpho.plugins.MetaDisplayInterface;
import edu.ucsb.nceas.morpho.plugins.MetaDisplayFactoryInterface;
import edu.ucsb.nceas.morpho.plugins.XMLFactoryInterface;
import edu.ucsb.nceas.morpho.util.DocumentNotFoundException;
import edu.ucsb.nceas.morpho.util.XMLTransformer;
import edu.ucsb.nceas.morpho.util.IOUtil;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.DataViewContainerPanel;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.framework.ConfigXML;

import edu.ucsb.nceas.morpho.util.Log;


public class PreviewCommand implements Command
{
	
	private Morpho instanceOfMorpho;
	private PrinterPlugin printerPlugin;
	private ConfigXML config;
	private final String CONFIG_KEY_STYLESHEET_LOCATION = "stylesheetLocation";
  private final String CONFIG_KEY_MCONFJAR_LOC   = "morphoConfigJarLocation";
		
	public void execute(ActionEvent ae) {
		
		AbstractDataPackage adp = UIController.getInstance().getCurrentAbstractDataPackage();
    if(adp == null) {
			Log.debug(16, " Abstract Data Package is null in the Print Plugin");
			return;
		}
																	
		XMLTransformer transformer = XMLTransformer.getInstance();
		transformer.addTransformerProperty(XMLTransformer.SELECTED_DISPLAY_XSLPROP, 	
						XMLTransformer.XSLVALU_DISPLAY_PRNT);
		transformer.addTransformerProperty( XMLTransformer.CSS_PATH_XSLPROP, 
                                            getFullStylePath());
		Reader xmlReader = null, resultReader = null;
		String htmlDoc = "<html><head><h2>Error displaying the requested Document</h2></head></html>";
		String ID = "";
    try{
        ID = adp.getPackageId();
        if ((ID==null)||(ID.equals(""))) ID = "tempid";
				resultReader = null;
				Document doc = adp.openAsDom(ID);
				if (doc == null) {
	          xmlReader = adp.openAsReader(ID);
						resultReader = transformer.transform(xmlReader);
				} else {
	        resultReader = transformer.transform(doc);
				}
				StringBuffer sb = IOUtil.getAsStringBuffer(resultReader, true);
        htmlDoc = sb.toString();
		} catch (DocumentNotFoundException dnfe) {
	     	Log.debug(12, "DocumentNotFoundException getting Reader for ID: "
	                                          +ID+"; "+dnfe.getMessage());
	  } catch (IOException io) {
				Log.debug(12, "IOException while getting the string for ID:" + ID + "; "+ io);
		}	catch (Exception e) {
				Log.debug(12, "Exception during Transformation in PrintCommand - " + e);
		}
		printerPlugin.display( htmlDoc , "text/html");
	}
	
	
	public PreviewCommand(Morpho morpho, PrinterPlugin prPlugin)
	{
		instanceOfMorpho = morpho;
		printerPlugin = prPlugin;
		config = Morpho.getConfiguration();
	}
	
	private String getFullStylePath() 
  {
			StringBuffer pathBuff = new StringBuffer();
      pathBuff.append("jar:file:");
      pathBuff.append(new File("").getAbsolutePath());
      pathBuff.append("/");
      pathBuff.append(config.get(CONFIG_KEY_MCONFJAR_LOC, 0));
      pathBuff.append("!/");
      pathBuff.append(config.get(CONFIG_KEY_STYLESHEET_LOCATION, 0));
      Log.debug(50,"PrinterCommand.getFullStylePath() returning: "
                                                              +pathBuff.toString());
      return pathBuff.toString();
  }
}
