/**
 *  '$RCSfile: GeographicPage.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Saurabh Garg
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2004-01-21 20:13:44 $'
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

package edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.AbstractWizardPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;

import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.morpho.query.LiveMapPanel;
import edu.ucsb.nceas.morpho.util.Log;

import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.*;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import edu.ucsb.nceas.morpho.framework.ConfigXML;


public class GeographicPage extends AbstractWizardPage {

  public final String pageID     = DataPackageWizardInterface.GEOGRAPHIC;
  public final String nextPageID = DataPackageWizardInterface.TEMPORAL;
  public final String pageNumber = "1";
//////////////////////////////////////////////////////////

  public final String title      = "Geographic Information";
  public final String subtitle   = "";
  
  private JTextArea   covDescField;
  private JLabel covDescLabel;
  private JLabel regionSelectionLabel;
  private JList regionList;
  private LiveMapPanel lmp;

  private final String xPathRoot  = "/eml:eml/dataset/coverage/geographicCoverage";

  private ConfigXML locationsXML = null;
  
  public GeographicPage() {

    init();
  }

  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    JPanel vbox = this;

//DFH    vbox.add(WidgetFactory.makeHalfSpacer());

    JLabel coverageDesc = WidgetFactory.makeHTMLLabel(
        "<b>Enter a description of the geographic coverage.</b> Enter a general "
        +"description of the geographic area in which the data were collected. "
        +"This can be a simple place name (e.g., Santa Barbara) or a fuller description.", 2);
    vbox.add(coverageDesc);

    JPanel covDescPanel = WidgetFactory.makePanel();

    covDescLabel = WidgetFactory.makeLabel(" Description:", true);
    covDescLabel.setVerticalAlignment(SwingConstants.TOP);
    covDescLabel.setAlignmentY(SwingConstants.TOP);
    covDescPanel.add(covDescLabel);

    covDescField = WidgetFactory.makeTextArea("", 6, true);
    JScrollPane jscrl = new JScrollPane(covDescField);
    covDescPanel.add(jscrl);

    covDescPanel.setBorder(new javax.swing.border.EmptyBorder(0,0,0,5*WizardSettings.PADDING));
    vbox.add(covDescPanel);

//DFH    vbox.add(WidgetFactory.makeDefaultSpacer());

    JLabel bbDesc = WidgetFactory.makeHTMLLabel(
        "<p><b>Set the geographic coordinates which bound the coverage</b> Latitude and longitude"
       +"values are used to create a 'bounding box' containing the region of interest. "
       +"Drag or click on the map. Then edit the text boxes if necessary. "
       +"[Default entries are in fractional degrees. To enter in degreea/minutes/seconds, simply "
       +"type a space between the degrees, minutes, and seconds values]</p>", 3);
    vbox.add(bbDesc);


    JPanel bboxPanel = WidgetFactory.makePanel();
    
    JLabel bboxLabel = WidgetFactory.makeLabel(" Bounding Box:", true);
    bboxLabel.setVerticalAlignment(SwingConstants.TOP);
    bboxLabel.setAlignmentY(SwingConstants.TOP);
    bboxPanel.add(bboxLabel);
    
    lmp = new LiveMapPanel(true);
    bboxPanel.add(lmp);

    bboxPanel.setBorder(new javax.swing.border.EmptyBorder(0,0,0,5*WizardSettings.PADDING));
    vbox.add(bboxPanel);

  ////////////////////////////////////////////////////////////////////////////

    JPanel regionPanel = new JPanel();
    regionPanel.setLayout(new GridLayout(1,2));
    JPanel regionSelectionPanel = WidgetFactory.makePanel(4);

    regionSelectionLabel = WidgetFactory.makeLabel(" Named Regions:", false, WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
    regionSelectionLabel.setVerticalAlignment(SwingConstants.TOP);
    regionSelectionLabel.setAlignmentY(SwingConstants.TOP);
    regionSelectionPanel.add(regionSelectionLabel);

    
    Vector names = getLocationNames();
    final DefaultListModel model = new DefaultListModel();
    for (int i=0;i<names.size();i++) {
      model.addElement(names.elementAt(i));
    }
    regionList = new JList(model);
    regionList.setFont(WizardSettings.WIZARD_CONTENT_FONT);
    regionList.setForeground(WizardSettings.WIZARD_CONTENT_TEXT_COLOR);
    regionList.setSelectedIndex(0);
    JScrollPane jscr2 = new JScrollPane(regionList);
    regionSelectionPanel.add(jscr2);

    JLabel selectHelpLabel = getLabel("Click button to display selected region.");
    
    JButton selectButton = new JButton("Select");
    selectButton.setPreferredSize(new Dimension(60,24));
    selectButton.setMaximumSize(new Dimension(60,24));
    selectButton.setMargin(new Insets(0, 2, 1, 2));
    selectButton.setEnabled(true);
    selectButton.setFont(WizardSettings.WIZARD_CONTENT_FONT);
    selectButton.setFocusPainted(false);
    selectButton.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        String selection = (String)regionList.getSelectedValue();
//        Log.debug(1,"Selection: "+selection);
        double n = (new Double(getNorth(selection))).doubleValue();
        double w = (new Double(getWest(selection))).doubleValue();
        double s = (new Double(getSouth(selection))).doubleValue();
        double e = (new Double(getEast(selection))).doubleValue();
        lmp.setBoundingBox(n, w, s, e);
      }
    });

    final GeographicPage currentInstance = this;
    final JTextField textField = new JTextField(20);
    final String msg1 = "Enter short name to appear in list.";
    final Object[] array = {msg1, textField};
    final String descText = (covDescField.getText()).trim();
    JLabel addHelpLabel = getLabel("Click to add current selection to list.");
    JButton addButton = new JButton("Add");
    addButton.setPreferredSize(new Dimension(60,24));
    addButton.setMaximumSize(new Dimension(60,24));
    addButton.setMargin(new Insets(0, 2, 1, 2));
    addButton.setEnabled(true);
    addButton.setFont(WizardSettings.WIZARD_CONTENT_FONT);
    addButton.setFocusPainted(false);
    addButton.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        
        String btnString1 = "Enter";
        String btnString2 = "Cancel";
        Object[] options = {btnString1, btnString2};

        JOptionPane optionPane = new JOptionPane(array, 
                                    JOptionPane.QUESTION_MESSAGE,
                                    JOptionPane.YES_NO_OPTION,
                                    null,
                                    options,
                                    options[0]);        
        JDialog dialog = optionPane.createDialog(currentInstance, "Add Current Selection to Named Region List");
        dialog.show();
        String selectedValue = (String)(optionPane.getValue());
        if ((selectedValue!=null)&&(selectedValue.equals("Enter"))) {
          String inputName = (textField.getText()).trim();
          if (inputName.length()==0) {
            Log.debug(1, "Sorry, but a Name must be entered.");
          } else {
            // create new location here
          
            addLocation(inputName, descText, lmp.getNorth(), lmp.getWest(), 
                                lmp.getSouth(), lmp.getEast());
            model.addElement(inputName);
          }
        }
      }
    });

    JLabel deleteHelpLabel = getLabel("Click to remove selected region from list.");
    JButton deleteButton = new JButton("Delete");
    deleteButton.setPreferredSize(new Dimension(60,24));
    deleteButton.setMaximumSize(new Dimension(60,24));
    deleteButton.setMargin(new Insets(0, 2, 1, 2));
    deleteButton.setEnabled(true);
    deleteButton.setFont(WizardSettings.WIZARD_CONTENT_FONT);
    deleteButton.setFocusPainted(false);
    deleteButton.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        int selindex = regionList.getSelectedIndex();
        model.remove(selindex);
        locationsXML.removeNode("location", selindex);
        locationsXML.save();
      }
    });

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridLayout(4, 1));
    JPanel buttonSubpanel1 = new JPanel();
    JPanel buttonSubpanel2 = new JPanel();
    JPanel buttonSubpanel3 = new JPanel();
    
    buttonSubpanel1.setLayout(new FlowLayout(FlowLayout.LEFT));
    buttonSubpanel1.add(selectButton);
    buttonSubpanel1.add(selectHelpLabel);
    
    buttonSubpanel2.setLayout(new FlowLayout(FlowLayout.LEFT));
    buttonSubpanel2.add(addButton);
    buttonSubpanel2.add(addHelpLabel);

    buttonSubpanel3.setLayout(new FlowLayout(FlowLayout.LEFT));
    buttonSubpanel3.add(deleteButton);
    buttonSubpanel3.add(deleteHelpLabel);

    buttonPanel.add(buttonSubpanel1);
    buttonPanel.add(buttonSubpanel2);
    buttonPanel.add(buttonSubpanel3);
    
    
    
    
    regionPanel.add(regionSelectionPanel);
    regionPanel.add(buttonPanel);

    vbox.add(regionPanel);

    vbox.add(WidgetFactory.makeDefaultSpacer());
    
  }

  
  /**
   *  gets info for location list from file
   */
  private Vector getLocationNames() {
    try{
      locationsXML = new ConfigXML("./lib/locations.xml");
      Vector vec = locationsXML.getValuesForPath("name");
      return vec;
    } catch (Exception w) {
      Log.debug(5, "problem reading locations file!");      
    }
    return null;
  }
  
  private String getNorth(String locname) {
    String res = "";
    if (locationsXML!=null) {
     Vector vec = locationsXML.getValuesForPath("location/name[.='"+locname+"']/../north");
     res = (String)vec.firstElement();
    }
    return res;
  }
  private String getWest(String locname) {
    String res = "";
    if (locationsXML!=null) {
     Vector vec = locationsXML.getValuesForPath("location/name[.='"+locname+"']/../west");
     res = (String)vec.firstElement();
    }
    return res;
  }
  private String getSouth(String locname) {
    String res = "";
    if (locationsXML!=null) {
     Vector vec = locationsXML.getValuesForPath("location/name[.='"+locname+"']/../south");
     res = (String)vec.firstElement();
    }
    return res;
  }
  private String getEast(String locname) {
    String res = "";
    if (locationsXML!=null) {
     Vector vec = locationsXML.getValuesForPath("location/name[.='"+locname+"']/../east");
     res = (String)vec.firstElement();
    }
    return res;
  }
	
	/**
	 *  Create a dom subtree with a new location
	 */
	private Node createNewLocation( String name, String desc, double north, double west,
	                        double south, double east) {
		Node head = null;												
		if (locationsXML!=null) {
			Document doc = locationsXML.getDocument();
			head = doc.createElement("location");
			Node temp = doc.createElement("name");
			Node temp1 = doc.createTextNode(name);
			temp.appendChild(temp1);
			head.appendChild(temp);
			
			temp = doc.createElement("description");
			temp1 = doc.createTextNode(desc);
			temp.appendChild(temp1);
			head.appendChild(temp);

			temp = doc.createElement("north");
			temp1 = doc.createTextNode((new Double(north)).toString());
			temp.appendChild(temp1);
			head.appendChild(temp);

			temp = doc.createElement("west");
			temp1 = doc.createTextNode((new Double(west)).toString());
			temp.appendChild(temp1);
			head.appendChild(temp);

			temp = doc.createElement("south");
			temp1 = doc.createTextNode((new Double(south)).toString());
			temp.appendChild(temp1);
			head.appendChild(temp);

			temp = doc.createElement("east");
			temp1 = doc.createTextNode((new Double(east)).toString());
			temp.appendChild(temp1);
			head.appendChild(temp);
			
		}
		return head;
	}
	
	/**
	 *  adds a location to the config file
	 */
	public void addLocation( String name, String desc, double north, double west,
	                        double south, double east) {
		if (locationsXML!=null) {												
	    Node nd = createNewLocation(name, desc, north, west, south, east);
      Node root = locationsXML.getRoot();	
      root.appendChild(nd);	
			locationsXML.save();
    }											
	}

  /**
   *  The action to be executed when the page is displayed. May be empty
   */
  public void onLoadAction() {
  }


  /**
   *  The action to be executed when the "Prev" button is pressed. May be empty
   *
   */
  public void onRewindAction() {
  }


  /**
   *  The action to be executed when the "Next" button (pages 1 to last-but-one)
   *  or "Finish" button(last page) is pressed. May be empty, but if so, must
   *  return true
   *
   *  @return boolean true if wizard should advance, false if not
   *          (e.g. if a required field hasn't been filled in)
   */
  public boolean onAdvanceAction() {
    if (covDescField.getText().trim().equals("")) {
      WidgetFactory.hiliteComponent(covDescLabel);
      covDescField.requestFocus();
      return false;
    }
    return true;
  }


  /**
   *  gets the Map object that contains all the key/value paired
   *  settings for this particular wizard page
   *
   *  @return   data the Map object that contains all the
   *            key/value paired settings for this particular wizard page
   */
  private OrderedMap returnMap = new OrderedMap();

  public OrderedMap getPageData() {
    return getPageData(xPathRoot);  
  }

  public OrderedMap getPageData(String xPathRoot) {
    returnMap.clear();
    
    returnMap.put(xPathRoot + "/geographicDescription",
                    covDescField.getText().trim());

    returnMap.put(xPathRoot + "/boundingCoordinates/westBoundingCoordinate" ,
                    (new Double(lmp.getWest())).toString());

    returnMap.put(xPathRoot + "/boundingCoordinates/eastBoundingCoordinate" ,
                    (new Double(lmp.getEast())).toString());

    returnMap.put(xPathRoot + "/boundingCoordinates/northBoundingCoordinate" ,
                    (new Double(lmp.getNorth())).toString());
                    
    returnMap.put(xPathRoot + "/boundingCoordinates/southBoundingCoordinate" ,
                    (new Double(lmp.getSouth())).toString());

                    
                    return returnMap;
  }



  /**
   *  gets the unique ID for this wizard page
   *
   *  @return   the unique ID String for this wizard page
   */
  public String getPageID() { return pageID; }

  /**
   *  gets the title for this wizard page
   *
   *  @return   the String title for this wizard page
   */
  public String getTitle() { return title; }

  /**
   *  gets the subtitle for this wizard page
   *
   *  @return   the String subtitle for this wizard page
   */
  public String getSubtitle() { return subtitle; }

  /**
   *  Returns the ID of the page that the user will see next, after the "Next"
   *  button is pressed. If this is the last page, return value must be null
   *
   *  @return the String ID of the page that the user will see next, or null if
   *  this is te last page
   */
  public String getNextPageID() { return nextPageID; }

  /**
     *  Returns the serial number of the page
     *
     *  @return the serial number of the page
     */
  public String getPageNumber() { return pageNumber; }

  public void setPageData(OrderedMap data) { }
  
  private JLabel getLabel(String text) {
    if (text==null) text="";
    JLabel label = new JLabel(text);

    label.setAlignmentX(1.0f);
    label.setFont(WizardSettings.WIZARD_CONTENT_FONT);
    label.setBorder(BorderFactory.createMatteBorder(1,10,1,3, (Color)null));

    return label;
  }
  
  /**
   *  @return a List contaiing 2 String elements - one for each column of the
   *  2-col list in which this surrogate is displayed
   *
   */
  private final StringBuffer surrogateBuff = new StringBuffer();
  //
  public List getSurrogate() {

    List surrogate = new ArrayList();
//    surrogate.add(covDescField.getText().trim());
    String temp = "West: "+(new Double(lmp.getWest())).toString() +
                  "; East: "+(new Double(lmp.getEast())).toString() +
                  "; North: "+(new Double(lmp.getNorth())).toString() +
                  "; South: "+(new Double(lmp.getSouth())).toString()
                  + "; -----" + covDescField.getText().trim();
    surrogate.add(temp);
    
    return surrogate;
  }
	
	
    /**
   *  This is a static main method configured to test the class 
   */
  static public void main(String args[]) {
    JFrame frame = new JFrame("Demo/Test");
    frame.setSize(800, 600);
    frame.getContentPane().setLayout(new BorderLayout());
    GeographicPage geo = new GeographicPage();
    
//		geo.addLocation("Test", "Test Location", 1.0, 2.0, 3.0, 4.0);
    frame.getContentPane().add(geo, BorderLayout.CENTER);
    frame.setVisible(true);

  }

}