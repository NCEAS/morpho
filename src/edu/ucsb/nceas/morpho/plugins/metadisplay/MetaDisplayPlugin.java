/**
 *  '$RCSfile: MetaDisplayPlugin.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2003-10-14 21:25:09 $'
 * '$Revision: 1.7 $'
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

package edu.ucsb.nceas.morpho.plugins.metadisplay;

import java.util.List;
import java.util.ArrayList;

import java.io.Reader;
import java.io.StringReader;

import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.Component;

import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

import javax.swing.JFrame;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.plugins.DocumentNotFoundException;
import edu.ucsb.nceas.morpho.plugins.MetaDisplayInterface;
import edu.ucsb.nceas.morpho.plugins.MetaDisplayFactoryInterface;
import edu.ucsb.nceas.morpho.plugins.PluginInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceExistsException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.XMLFactoryInterface;
import edu.ucsb.nceas.morpho.plugins.xsltresolver.XSLTResolverPlugin;

import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.exception.NullArgumentException;



/**
 *  Plugin that builds a display panel to display metadata.  Given a String ID, 
 *  does a lookup using a factory that must also be provided (and which 
 *  implements the ContentFactoryInterface) to get the XML document to display.  
 *  Then styles this document accordingly using XSLT, before displaying it in an 
 *  embedded HTML display.
 */
public class MetaDisplayPlugin implements   PluginInterface, 
                                            ServiceProvider,
                                            MetaDisplayFactoryInterface
{

    private static List displayList;
    /**
     *  Required by PluginInterface; called automatically at runtime
     *
     *  @param morpho    a reference to the <code>Morpho</code>
     */
    public void initialize(Morpho morpho)
    {
        try 
        {
          ServiceController services = ServiceController.getInstance();
          services.addService(MetaDisplayFactoryInterface.class, this);
          Log.debug(20, "Service added: MetaDisplayFactoryInterface.");
        } 
        catch (ServiceExistsException see)
        {
          Log.debug(6, 
                    "Service registration failed: MetaDisplayFactoryInterface");
          Log.debug(6, see.toString());
        }
        displayList = new ArrayList();
    }
     
    
    /**
     *  Required by MetaDisplayFactoryInterface:
     *  Returns a new instance of an object that implements the 
     *  <code>MetaDisplayInterface</code>
     *
     *  @return     new instance of an object that implements the 
     *              <code>MetaDisplayInterface</code>
     */
    public MetaDisplayInterface getInstance() 
    {
        MetaDisplay display = new MetaDisplay();
        displayList.add(display);
        return display;
    }
    
    
    /**
     *  Required by MetaDisplayFactoryInterface:
     *  Returns a reference to an existing object that implements the 
     *  <code>MetaDisplayInterface</code>. The object is identified by the int
     *  index assigned to it in the getInstance() method at the time of creation
     *
     *  @param displayNum   int index assigned to the object in the 
     *                      getInstance() method at the time of creation
     *
     *  @return             reference to an existing object that implements the 
     *                      <code>MetaDisplayInterface</code> identified by 
     *                      displayNum.  Returns NULL if displayNum out of range
     */
    public MetaDisplayInterface getMetaDisplay(int displayNum)
    {
        if (displayList.size() < displayNum+1) return null;
        return (MetaDisplayInterface)(displayList.get(displayNum));
    }
    
    
    /**
     *  Main method can be used for testing this plugin. If you run it without 
     *  any command-line arguments, you'll just get a default display with some 
     *  test data in it.  If you want to actually style some XML, you must 
     *  provide 2 or 3 command-line arguments:
     *  
     *  @param args <ul><li>id - the identifier string that tells the 
     *                  MetaDisplay what XML document to display</li>
     *                  <li>XMLFactoryInterface - the full string classname of 
     *                  an object that implements the XMLFactoryInterface. Given
     *                  the id (see above), this factory then returns a Reader 
     *                  which allows the MetaDisplay to actually get the 
     *                  Document identified by the id.</li>
     *                  <li>listener (optional) - the full string classname of
     *                  an ActionListener that will receive callbacks each time 
     *                  an event occurs within the metaDisplay. Useful for 
     *                  responding to close actions, clicked links etc 
     */
    public static void main(String[] args) {
    
      String id = "DEFAULT";
      
      Log.getLog().setDebugLevel(51);

      XMLFactoryInterface xmlFactory  
      
        = new XMLFactoryInterface() {
        
            public Reader openAsReader(String id) 
                                              throws DocumentNotFoundException {
            
              Log.debug(50,"XMLFactoryInterface openAsReader got: "+id);
              
              Reader reader = null;
              
              if (id.trim().equals("DEFAULT")) {
              
                reader = new StringReader(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    +"<eml:eml packageId=\"eml.1.1\" system=\"knb\" "
                    +"xmlns:ds=\"eml://ecoinformatics.org/dataset-2.0.0\" "
                    +"xmlns:eml=\"eml://ecoinformatics.org/eml-2.0.0\" " 
                    +"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                    +"xsi:schemaLocation=\"eml://ecoinformatics.org/eml-2.0.0 eml.xsd\"> "
                    +"<dataset/> "
                  + "</eml:eml>");
              
              } else {
                throw new DocumentNotFoundException(
                                              "XMLFactory - no match for "+id);
              }
              Log.debug(50,"XMLFactoryInterface openAsReader returning: "+reader);
              return reader;
            }};

      ActionListener listener = null;
      
      for (int i=0; i<args.length; i++) {
      
        switch (i) {
        
          case 0:
            id = args[i];
            break;
        
          case 1:
            try {
              xmlFactory 
                = ((XMLFactoryInterface)(Class.forName(args[i]).newInstance()));
            }catch(ClassNotFoundException cnfe) {
              Log.debug(2,
                 "XMLFactory - CLASS NOT FOUND - trying to do Class.forName("
                                                                  +args[i]+")");
              System.exit(1);
            }catch(InstantiationException e) {
              Log.debug(2,
                  "XMLFactory - Error trying to do newInstance() for "+args[i]);
              e.printStackTrace();
              System.exit(1);
            }catch(IllegalAccessException iae) {
              Log.debug(2,
                  "XMLFactory - IllegalAccessException for "+args[i]);
              iae.printStackTrace();
              System.exit(1);
            }
            break;
        
          case 2:
            try {
              listener 
                    = ((ActionListener)(Class.forName(args[i]).newInstance()));
            }catch(ClassNotFoundException cnfe) {
              Log.debug(2,
                 "XMLFactory - CLASS NOT FOUND - trying to do Class.forName("
                                                                  +args[i]+")");
              System.exit(1);
            }catch(InstantiationException e) {
              Log.debug(2,
                  "XMLFactory - Error trying to do newInstance() for "+args[i]);
              e.printStackTrace();
              System.exit(1);
            }catch(IllegalAccessException iae) {
              Log.debug(50,
                  "XMLFactory - IllegalAccessException for "+args[i]);
              iae.printStackTrace();
              System.exit(1);
            }



            break;
        }
      }

      XSLTResolverPlugin xsltResolverPlugin = new XSLTResolverPlugin();
      // Start by creating the new plugin
      PluginInterface xPlugin = (PluginInterface)xsltResolverPlugin;
      xPlugin.initialize(null);

      MetaDisplayPlugin plugin = new MetaDisplayPlugin();
      plugin.initialize(null);
      
      MetaDisplayInterface metaDisplay = plugin.getInstance();
      
      Component comp = null;
      Log.debug(50,
         "Getting display component:\n id         = "+id
                                 +";\n XMLFactory = "+xmlFactory
                                 +";\n listener   = "+listener);
      try {
        comp = metaDisplay.getDisplayComponent(id, xmlFactory, listener);
        
      } catch (NullArgumentException nae) {
        Log.debug(50,"NullArgumentException getting metaDisplay! "+nae);
        nae.printStackTrace();
      } catch (DocumentNotFoundException dnfe) {
        Log.debug(50,"DocumentNotFoundException getting metaDisplay! "+dnfe);
        dnfe.printStackTrace();
      }

      JFrame frame = new JFrame();
      frame.setBounds(100,100,500,500);
      frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) { System.exit(0); 
            }});
      frame.getContentPane().setLayout(new BorderLayout(5,5));
      frame.getContentPane().add(comp, BorderLayout.CENTER);
      frame.setVisible(true);
   }
}
