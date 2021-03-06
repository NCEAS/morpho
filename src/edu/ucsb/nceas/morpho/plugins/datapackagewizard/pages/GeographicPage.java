/**
 *  '$RCSfile: GeographicPage.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Saurabh Garg
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-05-06 00:09:08 $'
 * '$Revision: 1.23 $'
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

import edu.ucsb.nceas.morpho.Language;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.query.LiveMapPanel;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.Util;
import edu.ucsb.nceas.utilities.OrderedMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.w3c.dom.Document;
import org.w3c.dom.Node;


public class GeographicPage extends AbstractUIPage {

  public final String pageID     = DataPackageWizardInterface.GEOGRAPHIC;
  public final String pageNumber = "1";
//////////////////////////////////////////////////////////

  public final String title      = "Geographic Information";
  public final String subtitle   = "";

  private JTextArea   covDescField;
  private JLabel covDescLabel;
  private JLabel regionSelectionLabel;
  private JList regionList;
  private LiveMapPanel lmp;

  private boolean deleteFlag = false;

  private String xPathRoot  = "/eml:eml/dataset/coverage/geographicCoverage";

  private ConfigXML locationsXML = null;

  double north = -1000;
  double west = -1000;
  double south = -1000;
  double east = -1000;

  // use to indicate that text in the ooverage Description field has been changed by user
  private boolean covDescFieldChangedFlag = false;
  // user to indicate the event from sort command
  private boolean eventFromSortCommand = false;
  public GeographicPage() {
	nextPageID = DataPackageWizardInterface.TEMPORAL;
    init();
  }

  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    final JPanel vbox = this;

//DFH    vbox.add(WidgetFactory.makeHalfSpacer());

    JLabel coverageDesc = WidgetFactory.makeHTMLLabel(
        /*"<b>Enter a description of the geographic coverage.</b> "*/
    	"<b>" + Language.getInstance().getMessage("GeographicPage.coverageDesc_1") +"</b> "	
        /*
        + "Enter a general description of the geographic area in which the data were collected. "
        +"This can be a simple place name (e.g., Santa Barbara) or a fuller description."
        */
    	+ Language.getInstance().getMessage("GeographicPage.coverageDesc_2")
        , 2);
    vbox.add(coverageDesc);

    JPanel covDescPanel = WidgetFactory.makePanel();

    covDescLabel = WidgetFactory.makeLabel(/*" Description:"*/ " " + Language.getInstance().getMessage("Description") + ":" , true);
    covDescLabel.setVerticalAlignment(SwingConstants.TOP);
    covDescLabel.setAlignmentY(SwingConstants.TOP);
    covDescPanel.add(covDescLabel);

    covDescField = WidgetFactory.makeTextArea("", 2, true);
    JScrollPane jscrl = new JScrollPane(covDescField);
    covDescPanel.add(jscrl);

    SymFocus aSymFocus = new SymFocus();
    covDescField.addFocusListener(aSymFocus);

    covDescPanel.setBorder(new javax.swing.border.EmptyBorder(0,0,0,5*WizardSettings.PADDING));
    vbox.add(covDescPanel);

//DFH    vbox.add(WidgetFactory.makeDefaultSpacer());
    vbox.add(WidgetFactory.makeHalfSpacer());

    JLabel bbDesc = WidgetFactory.makeHTMLLabel(
        "<p>" 
        /*+"<b>Set the geographic coordinates which bound the coverage:</b> "*/
       +"<b>" + Language.getInstance().getMessage("GeographicPage.bbDesc_1") + " :</b> "
       /*
       + "Latitude and longitude "
       +"values are used to create a 'bounding box' containing the region of interest. "
       +"Drag or click on the map and then edit the text boxes if necessary. "
       +"[Default entries are in fractional degrees. To enter in degrees/minutes/seconds, simply "
       +"type a space between the degrees, minutes, and seconds values]" 
       */
       + Language.getInstance().getMessage("GeographicPage.bbDesc_2")
       +"</p>", 3);
    vbox.add(bbDesc);


    JPanel bboxPanel = WidgetFactory.makePanel();

    JLabel bboxLabel = WidgetFactory.makeLabel(/*" Bounding Box:"*/ " " + Language.getInstance().getMessage("BoundingBox") + ":", true);
    bboxLabel.setVerticalAlignment(SwingConstants.TOP);
    bboxLabel.setAlignmentY(SwingConstants.TOP);
    bboxPanel.add(bboxLabel);

    lmp = new LiveMapPanel(true);
    bboxPanel.add(lmp);

    bboxPanel.setBorder(new javax.swing.border.EmptyBorder(0,0,4,5*WizardSettings.PADDING));
    vbox.add(bboxPanel);

  ////////////////////////////////////////////////////////////////////////////

    final JPanel regionPanel = new JPanel();
    regionPanel.setLayout(new GridLayout(1,2));
    JPanel regionSelectionPanel = WidgetFactory.makePanel(4);

    regionSelectionLabel = WidgetFactory.makeLabel(/*" Named Regions:"*/ " " + Language.getInstance().getMessage("GeographicPage.NamedRegions") + ":"
    												, false, WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
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
    regionList.setSelectedIndex(-1);
    regionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    ListSelectionModel lsm = regionList.getSelectionModel();
    lsm.addListSelectionListener(new RegionSelectionHandler());
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
        // Log.debug(1,"Selection: "+selection);
        double n = (new Double(getNorth(selection))).doubleValue();
        double w = (new Double(getWest(selection))).doubleValue();
        double s = (new Double(getSouth(selection))).doubleValue();
        double e = (new Double(getEast(selection))).doubleValue();
        lmp.setBoundingBox(n, w, s, e);
        covDescField.setText(getDescription(selection));
      }
    });

    final GeographicPage currentInstance = this;
    final JTextField textField = new JTextField(20);
    final String msg1 = /*"Enter short name to appear in list."*/ Language.getInstance().getMessage("GeographicPage.ShortNameList");
    final Object[] array = {msg1, textField};
    final String descText = (covDescField.getText()).trim();
    JLabel addHelpLabel = getLabel(/*"Click to add current selection to list."*/ Language.getInstance().getMessage("GeographicPage.AddToList"));
    JButton addButton = new JButton(/*"Add"*/ Language.getInstance().getMessage("Add"));
    addButton.setPreferredSize(new Dimension(60,24));
    addButton.setMaximumSize(new Dimension(60,24));
    addButton.setMargin(new Insets(0, 2, 1, 2));
    addButton.setEnabled(true);
    addButton.setFont(WizardSettings.WIZARD_CONTENT_FONT);
    addButton.setFocusPainted(false);
    addButton.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent ae) {

        String btnString1 = /*"Enter"*/ Language.getInstance().getMessage("Enter");
        String btnString2 = /*"Cancel"*/ Language.getInstance().getMessage("Cancel");
        Object[] options = {btnString1, btnString2};

        JOptionPane optionPane = new JOptionPane(array,
                                    JOptionPane.QUESTION_MESSAGE,
                                    JOptionPane.YES_NO_OPTION,
                                    null,
                                    options,
                                    options[0]);
        JDialog dialog = optionPane.createDialog(currentInstance,
           /*"Add Current Selection to Named Region List?"*/ Language.getInstance().getMessage("GeographicPage.AddCurrentSelectToList")
        	);
        dialog.show();
        String selectedValue = (String)(optionPane.getValue());
        if ((selectedValue!=null)&&(selectedValue.equals(btnString1))) {
          String inputName = (textField.getText()).trim();
          if (inputName.length()==0) {
            Log.debug(1, "Sorry, but a Name must be entered.");
          } else {
            // create new location here
            //if description is empty, put the name as description
            String description = (covDescField.getText()).trim();
            if( description.equals(""))
            {
              description = inputName;
            }
            addLocation(inputName, description,
                                lmp.getNorth(), lmp.getWest(),
                                lmp.getSouth(), lmp.getEast());
            model.addElement(inputName);
          }
        }
      }
    });

    JLabel deleteHelpLabel = getLabel(/*"Click to remove selected region from list."*/ 
    									Language.getInstance().getMessage("GeographicPage.RemoveFromList"));
    JButton deleteButton = new JButton(/*"Delete"*/ Language.getInstance().getMessage("Delete"));
    deleteButton.setPreferredSize(new Dimension(60,24));
    deleteButton.setMaximumSize(new Dimension(60,24));
    deleteButton.setMargin(new Insets(0, 2, 1, 2));
    deleteButton.setEnabled(true);
    deleteButton.setFont(WizardSettings.WIZARD_CONTENT_FONT);
    deleteButton.setFocusPainted(false);
    deleteButton.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        deleteFlag = true;
        int selindex = regionList.getSelectedIndex();
        model.remove(selindex);
        locationsXML.removeNode("location", selindex);
        locationsXML.save();
        regionList.setSelectedIndex(0);
        deleteFlag = false;
      }
    });

    JLabel sortHelpLabel = getLabel(/*"Click to sort the list of locations."*/
    								Language.getInstance().getMessage("GeographicPage.SortList"));
    JButton sortButton = new JButton(/*"Sort"*/ Language.getInstance().getMessage("Sort"));
    sortButton.setPreferredSize(new Dimension(60,24));
    sortButton.setMaximumSize(new Dimension(60,24));
    sortButton.setMargin(new Insets(0, 2, 1, 2));
    sortButton.setEnabled(true);
    sortButton.setFont(WizardSettings.WIZARD_CONTENT_FONT);
    sortButton.setFocusPainted(false);
    sortButton.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        eventFromSortCommand = true;
        //create the sorting stylesheet
        StringBuffer sb = new StringBuffer();
        sb.append("<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"");
        sb.append(" version=\"1.0\">\n");
        sb.append("<xsl:template match=\"locationList\">\n");
        sb.append("<locationList>\n");
        sb.append("<xsl:for-each select=\"./location\">\n");
        sb.append("<xsl:sort select=\"./name\" />\n");
        sb.append("<location>\n");
        sb.append("<xsl:apply-templates />\n");
        sb.append("</location>\n");
        sb.append("</xsl:for-each>\n");
        sb.append("</locationList>\n");
        sb.append("</xsl:template>\n");
        sb.append("<xsl:template ");
        sb.append("match=\"*|@*|comment()|processing-instruction()|text()\">\n");
        sb.append("<xsl:copy>\n");
        sb.append("<xsl:apply-templates ");
        sb.append("select=\"*|@*|comment()|processing-instruction()|text()\"/>\n");
        sb.append("</xsl:copy>\n");
        sb.append("</xsl:template>");
        sb.append("</xsl:stylesheet>\n");
        //sort the list
        try
        {
          locationsXML.sort(sb.toString());
        }
        catch(Exception e)
        {
          JOptionPane optionPane = new JOptionPane(
            "Sorry, the list can't be sorted.",
            JOptionPane.ERROR_MESSAGE,
            JOptionPane.OK_OPTION);
          Log.debug(1, "Error sorting regions: " + e.getMessage());
        }
        locationsXML.save();
        Vector names = getLocationNames();
        DefaultListModel model = (DefaultListModel)regionList.getModel();
        //clear and repopulate the model
        model.clear();
        for (int i=0;i<names.size();i++)
        {
          model.addElement(names.elementAt(i));
        }
      }
    });

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridLayout(4, 1));
    JPanel buttonSubpanel2 = new JPanel();
    JPanel buttonSubpanel3 = new JPanel();
    JPanel buttonSubpanel4 = new JPanel();

    buttonSubpanel2.setLayout(new FlowLayout(FlowLayout.LEFT));
    buttonSubpanel2.add(addButton);
    buttonSubpanel2.add(addHelpLabel);

    buttonSubpanel3.setLayout(new FlowLayout(FlowLayout.LEFT));
    buttonSubpanel3.add(deleteButton);
    buttonSubpanel3.add(deleteHelpLabel);

    buttonSubpanel4.setLayout(new FlowLayout(FlowLayout.LEFT));
    buttonSubpanel4.add(sortButton);
    buttonSubpanel4.add(sortHelpLabel);

    buttonPanel.add(buttonSubpanel2);
    buttonPanel.add(buttonSubpanel3);
    buttonPanel.add(buttonSubpanel4);

    regionPanel.add(regionSelectionPanel);
    regionPanel.add(buttonPanel);

    vbox.add(regionPanel);
  }


  class RegionSelectionHandler implements ListSelectionListener {
    public void valueChanged(ListSelectionEvent eee) {
      boolean setTextFlag = true;
      if (deleteFlag) return;
      // if this event come from sort command, do nothing except set
      // the indicator false
      if (eventFromSortCommand)
      {
        eventFromSortCommand = false;
        return;
      }
      String selection = (String)regionList.getSelectedValue();
      if (covDescField.hasFocus()) return;
      if ((covDescFieldChangedFlag)&&(covDescField.getText().length()>0) ) {
         int res = JOptionPane.showConfirmDialog(covDescField,
           "Replace existing description",
           "Confirm:", JOptionPane.YES_NO_OPTION);
         if (res==JOptionPane.YES_OPTION) {
           setTextFlag = true;
         }
         else {
           setTextFlag = false;
         }
      }

      double n = (new Double(getNorth(selection))).doubleValue();
      double w = (new Double(getWest(selection))).doubleValue();
      double s = (new Double(getSouth(selection))).doubleValue();
      double e = (new Double(getEast(selection))).doubleValue();
      lmp.setBoundingBox(n, w, s, e);
      if (setTextFlag) {
        covDescField.setText(getDescription(selection));
        covDescFieldChangedFlag = false;
      }
    }
  }

  /**
   * gets info for location list from file
   *
   * @return Vector
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

  /**
   * getNorth
   */
  private String getNorth(String locname) {
    String res = "";
    if (locationsXML!=null) {
     Vector vec = locationsXML.getValuesForPath(
       "location/name[.='"+locname+"']/../north");
     res = (String)vec.firstElement();
    }
    return res;
  }

  /**
   * getWest
   */
  private String getWest(String locname) {
    String res = "";
    if (locationsXML!=null) {
     Vector vec = locationsXML.getValuesForPath(
       "location/name[.='"+locname+"']/../west");
     res = (String)vec.firstElement();
    }
    return res;
  }

  /**
   * getSouth
   */
  private String getSouth(String locname) {
    String res = "";
    if (locationsXML!=null) {
     Vector vec = locationsXML.getValuesForPath("location/name[.='"+locname+"']/../south");
     res = (String)vec.firstElement();
    }
    return res;
  }

  /**
   * getEast
   */
  private String getEast(String locname) {
    String res = "";
    if (locationsXML!=null) {
     Vector vec = locationsXML.getValuesForPath("location/name[.='"+locname+"']/../east");
     res = (String)vec.firstElement();
    }
    return res;
  }

  /**
   * getDescription
   */
  private String getDescription(String locname) {
    String res = "";
    if (locationsXML!=null) {
     Vector vec = locationsXML.getValuesForPath("location/name[.='"+locname+"']/../description");
     res = (String)vec.firstElement();
    }
    return res;
  }


  /**
   * Create a dom subtree with a new location
   *
   * @param name String
   * @param desc String
   * @param north double
   * @param west double
   * @param south double
   * @param east double
   * @return Node
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
   * adds a location to the config file
   *
   * @param name String
   * @param desc String
   * @param north double
   * @param west double
   * @param south double
   * @param east double
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
    lmp.addMap();
    if(north>-1000){
      lmp.setBoundingBox(north, west, south, east);
    }
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
    north = lmp.getNorth();
    west  = lmp.getWest();
    east  = lmp.getEast();
    south = lmp.getSouth();
    //if (covDescField.getText().trim().equals("")) {
    if (Util.isBlank(covDescField.getText())) {
      WidgetFactory.hiliteComponent(covDescLabel);
      covDescField.requestFocus();
      return false;
    } else {
      WidgetFactory.unhiliteComponent(covDescLabel);
      lmp.removeMap();
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
                    (new Double(west)).toString());

    returnMap.put(xPathRoot + "/boundingCoordinates/eastBoundingCoordinate" ,
                    (new Double(east)).toString());

    returnMap.put(xPathRoot + "/boundingCoordinates/northBoundingCoordinate" ,
                    (new Double(north)).toString());

    returnMap.put(xPathRoot + "/boundingCoordinates/southBoundingCoordinate" ,
                    (new Double(south)).toString());


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

  public boolean setPageData(OrderedMap map, String _xPathRoot) {
    
    if (_xPathRoot!=null && _xPathRoot.trim().length() > 0) this.xPathRoot = _xPathRoot;
    Log.debug(40, "Map at the begining of setPageData with xpathRoot" +_xPathRoot+map);
    /*String name = (String)map.get(_xPathRoot + "/geographicDescription[1]");
    if(name != null) {
      
      map.remove(xPathRoot + "/geographicDescription[1]");
    }*/
    
    map = stripIndexOneFromMapKeys(map);
    String name = (String)map.get(xPathRoot + "/geographicDescription");
    if (name!=null) {
      covDescField.setText(name);
      map.remove(xPathRoot + "/geographicDescription");
    }


    name = (String)map.get(xPathRoot + "/boundingCoordinates/northBoundingCoordinate");
    if (name!=null) {
      try
      {
	      Double N = new Double(name);
	      if (N!=null) {
	        north = N.doubleValue();
	      }
      }
      catch(Exception e)
      {
    	  Log.debug(30, "Couldn't transform the "+name+ "to northBoundingCoordinate");
      }
      map.remove(xPathRoot + "/boundingCoordinates/northBoundingCoordinate");
    }

    name = (String)map.get(xPathRoot + "/boundingCoordinates/westBoundingCoordinate");
    if (name!=null) {
      try
      {
	      Double W = new Double(name);
	      if (W!=null) {
	        west = W.doubleValue();
	      }
      }
      catch(Exception e)
      {
    	  Log.debug(30, "Couldn't transform the "+name+ "to westBoundingCoordinate");
      }
      map.remove(xPathRoot + "/boundingCoordinates/westBoundingCoordinate");
    }

    name = (String)map.get(xPathRoot + "/boundingCoordinates/southBoundingCoordinate");
    if (name!=null) {
      try
      {
      Double S = new Double(name);
      if (S!=null) {
        south = S.doubleValue();
      }
      }
      catch(Exception e)
      {
    	  Log.debug(30, "Couldn't transform the "+name+ "to southBoundingCoordinate");
      }
      map.remove(xPathRoot + "/boundingCoordinates/southBoundingCoordinate");
    }

    name = (String)map.get(xPathRoot + "/boundingCoordinates/eastBoundingCoordinate");
    if (name!=null) {
      try
      {
	      Double E = new Double(name);
	      if (E!=null) {
	        east = E.doubleValue();
	      }
      }
      catch(Exception e)
      {
    	  Log.debug(30, "Couldn't transform the "+name+ "to eastBoundingCoordinate");
      }
      map.remove(xPathRoot + "/boundingCoordinates/eastBoundingCoordinate");
    }

    if ((east==west)&&(north==south)) lmp.setTool("PT");
    lmp.setBoundingBox(north, west, south, east);

    //if anything left in map, then it included stuff we can't handle...
     boolean returnVal = map.isEmpty();

     if (!returnVal) {

       Log.debug(20,
                 "GeographicPage.setPageData returning FALSE! Map still contains:"
                 + map);
     }
     return returnVal;
  }

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
    String temp1 = covDescField.getText().trim();
    String temp2 = "West: "+(new Double(lmp.getWest())).toString() +
                  "; East: "+(new Double(lmp.getEast())).toString() +
                  "; North: "+(new Double(lmp.getNorth())).toString() +
                  "; South: "+(new Double(lmp.getSouth())).toString();
    surrogate.add(temp1);

    surrogate.add(temp2);

    return surrogate;
  }


  /**
   * This is a static main method configured to test the class
   *
   * @param args String[]
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

     private OrderedMap stripIndexOneFromMapKeys(OrderedMap map) {

     OrderedMap newMap = new OrderedMap();
     Iterator it = map.keySet().iterator();
     while(it.hasNext()) {
       String key = (String) it.next();
       String val = (String)map.get(key);
       int pos;
       if((pos = key.indexOf("[1]")) < 0) {
         newMap.put(key, val);
         continue;
       }
       String newKey = "";
       for(;pos != -1; pos = key.indexOf("[1]")){
         newKey += key.substring(0,pos);
         key = key.substring(pos + 3);
       }
       newKey += key;
       newMap.put(newKey, val);
     }
     return newMap;
   }


  class SymFocus extends java.awt.event.FocusAdapter
  {
    private String startingString = "";

    public void focusLost(java.awt.event.FocusEvent event)
    {
//			System.out.println("FocusLost fired!");
      String currentString = covDescField.getText();
      if ((!startingString.equals(currentString))&&
          (currentString.length()>0)) {
        covDescFieldChangedFlag = true;
      }
    }

    public void focusGained(java.awt.event.FocusEvent event) {
      startingString = covDescField.getText();
    }

  }

}
