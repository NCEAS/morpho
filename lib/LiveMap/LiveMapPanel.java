/**
 * @(#)LiveMap_30.java
 *
 *  This software was developed by the Thermal Modeling and Analysis
 *  Project(TMAP) of the National Oceanographic and Atmospheric
 *  Administration's (NOAA) Pacific Marine Environmental Lab(PMEL),
 *  hereafter referred to as NOAA/PMEL/TMAP.
 *
 *  Access and use of this software shall impose the following
 *  obligations and understandings on the user. The user is granted the
 *  right, without any fee or cost, to use, copy, modify, alter, enhance
 *  and distribute this software, and any derivative works thereof, and
 *  its supporting documentation for any purpose whatsoever, provided
 *  that this entire notice appears in all copies of the software,
 *  derivative works and supporting documentation.  Further, the user
 *  agrees to credit NOAA/PMEL/TMAP in any publications that result from
 *  the use of this software or in any product that includes this
 *  software. The names TMAP, NOAA and/or PMEL, however, may not be used
 *  in any advertising or publicity to endorse or promote any products
 *  or commercial entity unless specific written permission is obtained
 *  from NOAA/PMEL/TMAP. The user also understands that NOAA/PMEL/TMAP
 *  is not obligated to provide the user with any support, consulting,
 *  training or assistance of any kind with regard to the use, operation
 *  and performance of this software nor to provide the user with any
 *  updates, revisions, new versions or "bug fixes".
 *
 *  THIS SOFTWARE IS PROVIDED BY NOAA/PMEL/TMAP "AS IS" AND ANY EXPRESS
 *  OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL NOAA/PMEL/TMAP BE LIABLE FOR ANY SPECIAL,
 *  INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER
 *  RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF
 *  CONTRACT, NEGLIGENCE OR OTHER TORTUOUS ACTION, ARISING OUT OF OR IN
 *  CONNECTION WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE.
 *
 */

import java.io.*;
import java.util.StringTokenizer;

//1.1 import java.awt.AWTEvent;
import java.awt.Event;
import java.awt.Color;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.MediaTracker;

//1.1 import java.awt.event.ActionEvent;
//1.1 import java.awt.event.ActionListener;
//1.1 import java.awt.event.MouseEvent;
//1.1 import java.awt.event.MouseListener;
//1.1 import java.awt.event.MouseMotionListener;
//1.1 import java.awt.event.WindowAdapter;
//1.1 import java.awt.event.WindowEvent;

import java.applet.Applet;
import java.awt.TextField;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Frame;
import java.awt.Panel; 

import javax.swing.*;

import tmap_30.map.*;
import tmap_30.convert.*;

import java.awt.Container; //1.0

/**
 * A map selection applet.
 *
 * @version     3.0, 02 Sept 1999
 * @author      Jonathan Callahan
 */

public class LiveMapPanel extends JPanel
    implements MapConstants
{
 

	public LiveMapPanel()
	{    
      super();
	    init();
	}

  final static int IMAGE_SIZE_X = 450;  // 300
  final static int IMAGE_SIZE_Y = 225;  // 150
  final static Color MAPTOOL_COLOR1 = Color.white;

  // tool type constants
  final static int TOOL_TYPE_PT = 0;
  final static int TOOL_TYPE_X  = 1;
  final static int TOOL_TYPE_Y  = 2;
  final static int TOOL_TYPE_XY = 3;


  MediaTracker tracker;
  MapCanvas map;
  MapGrid grid;
  MapTool [] toolArray = new MapTool[1];
  MapRegion [] regionArray = new MapRegion[0];
  Convert XConvert, YConvert, XText, YText;

  TextField North;
  TextField West;
  TextField East;
  TextField South;

  Button zoom_in;
  Button zoom_out;

  Image img_0;
  int imgSizeX = IMAGE_SIZE_X;
  int imgSizeY = IMAGE_SIZE_Y;
  int tool_type=TOOL_TYPE_XY;
  boolean need_to_center = false;    



  public void init() {     

   System.out.println("Beginning init!");

    String img = "gifs/java_0_world_20k.jpg";
    String img_x_domain = "-180 180";
    String img_y_domain = "-90 90";
    String toolType = "XY";
    String tool_x_range = "-180 180";
    String tool_y_range = "-90 90";

    if ( img == null ) {
      img_0 = getToolkit().getImage("gifs/java_0_world.gif");
    } else {
      img_0 = getToolkit().getImage(img);
    }

    tracker = new MediaTracker(this);
    tracker.addImage(img_0, 1);

    System.out.println("Loading image");
    try {
      tracker.waitForID(1);
    } catch (InterruptedException e) {
      System.out.println("Caught InterruptedException while loading image.");
      //EHC: throw exception ?
      return;
    }
    if (tracker.isErrorID(1)) {
      System.out.println("Error loading image...");
   //   this.showStatus("Error loading image.");
   //   this.stop();
      //EHC: throw exception ?
      return;
    }



/* 1.0 ----------------------V-------------------------- */

    GridBagLayout gridbag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    JPanel entryPanel = new JPanel(); 
    entryPanel.setLayout(gridbag);

    // lat and lon text fields
    JPanel posPanel = new JPanel();
    posPanel.setLayout(gridbag);

    Font textFont = new Font("Courier", Font.PLAIN, 12);
    North = new TextField("90 N", 8);
    West  = new TextField("180 W", 8);
    East  = new TextField("180 E", 8);
    South = new TextField("90 S", 8);
    North.setFont(textFont);
    South.setFont(textFont);
    East.setFont(textFont);
    West.setFont(textFont);

    c.gridx = 1;
    c.gridy = 0;
    c.gridwidth = 2;
    c.gridheight = 1;
    c.anchor = GridBagConstraints.CENTER;
    c.insets.left = 4;
    c.insets.right = 4;
    gridbag.setConstraints(North, c);
    posPanel.add(North);

    c.gridx = 0;
    c.gridy = 1;
    gridbag.setConstraints(West, c);
    posPanel.add(West);

    c.gridx = 2;
    c.gridy = 1;
    gridbag.setConstraints(East, c);
    posPanel.add(East);

    c.gridx = 1;
    c.gridy = 2;
    c.gridwidth = GridBagConstraints.REMAINDER;
    gridbag.setConstraints(South, c);
    posPanel.add(South);

    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.insets.top = 4;
    c.insets.bottom = 4;
    gridbag.setConstraints(posPanel, c);
    entryPanel.add(posPanel);
 
    System.out.println("Beginning Zoom creation");

    // Zoom Panel
    zoom_in = new Button("Zoom In");
    zoom_out = new Button("Zoom Out");
    Font buttonFont = new Font("TimesRoman", Font.PLAIN, 12);
    zoom_in.setFont(buttonFont);
    zoom_out.setFont(buttonFont);

    JPanel zoomPanel = new JPanel(); //1.0
    zoomPanel.setLayout(gridbag);  //1.0
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.insets.top = 0;
    c.insets.bottom = 0;
    gridbag.setConstraints(zoom_in, c);
    zoomPanel.add(zoom_in);
    c.gridx = 1;
    gridbag.setConstraints(zoom_out, c);
    zoomPanel.add(zoom_out);

    c.gridx = 0;
    c.gridy = 1;
    c.insets.top = 4;
    c.insets.bottom = 4;
    gridbag.setConstraints(zoomPanel, c);
    entryPanel.add(zoomPanel);

/* 1.0 ----------------------^-------------------------- */

//------------------------------------------------------------
// OK.  Here's where we start
// 1) set the converters

    //
    // Set the converters for the input fields.
    XConvert = new ConvertLongitude(ConvertLongitude.SPACE_E_W);
    YConvert = new ConvertLatitude(ConvertLatitude.SPACE_N_S);

    XText = new ConvertLongitude(ConvertLongitude.SPACE_E_W);
    YText = new ConvertLatitude(ConvertLatitude.SPACE_N_S);
//------------------------------------------------------------
// 2) Create a MapGrid and two MapTools

    grid = new MapGrid(0.0, 360.0, -90.0, 90.0);

    if ( toolType.equals("PT") ) {
      toolArray[0] = new PTTool(50,50,1,1,MAPTOOL_COLOR1);
    } else if ( toolType.equals("X") ) {
      toolArray[0] = new XTool(50,50,100,1,MAPTOOL_COLOR1);
    } else if ( toolType.equals("Y") ) {
      toolArray[0] = new YTool(50,50,1,50,MAPTOOL_COLOR1);
    } else { // default to XY
      toolArray[0] = new XYTool(50,50,100,50,MAPTOOL_COLOR1);
    }

    toolArray[0].setRange_X(0.0, 360.0);
    toolArray[0].setRange_Y(-90.0, 90.0);
    toolArray[0].setSnapping(true, true);

//------------------------------------------------------------
// 3) Set the Domain on the MapGrid
// 4) Set the Ranges in the MapTools and Converters 

System.out.println("Reached step 4"); 

    //
    // Set the domain associated with the image
    {

      double lo = 0.0;
      double hi = 360.0;
      StringTokenizer st;

      if ( img_x_domain != null ) {
        st = new StringTokenizer(img_x_domain);
        if ( st.hasMoreTokens() ) {
          lo = Double.valueOf(st.nextToken()).doubleValue();
          if ( st.hasMoreTokens() )
            hi = Double.valueOf(st.nextToken()).doubleValue();
        }
      }

      grid.setDomain_X(lo, hi);
      toolArray[0].setRange_X(lo,hi);
      XConvert.setRange(lo, hi);

      lo = -90.0;
      hi = 90.0;
      if ( img_y_domain != null ) {
        st = new StringTokenizer(img_y_domain);
        if ( st.hasMoreTokens() ) {
          lo = Double.valueOf(st.nextToken()).doubleValue();
          if ( st.hasMoreTokens() )
            hi = Double.valueOf(st.nextToken()).doubleValue();
        }
      }

      grid.setDomain_Y(lo, hi);
      toolArray[0].setRange_Y(lo,hi);
      YConvert.setRange(lo, hi);

    }


//------------------------------------------------------------
// 5) Create a map canvas
  
System.out.println("Reached step 5"); 

    // map canvas: change size
    map = new MapCanvas(img_0,imgSizeX,imgSizeY,toolArray,grid);
    map.setToolArray(toolArray);


    // Reshape the first tool to the passed in parameters.
    {
      double x_lo = 0.0;
      double x_hi = 0.0;
      double y_lo = 0.0;
      double y_hi = 0.0;
      StringTokenizer st;

      try {
        if ( tool_x_range != null ) {
          st = new StringTokenizer(tool_x_range);
          x_lo = XConvert.toDouble(st.nextToken());
          if ( st.hasMoreTokens() )
            x_hi = XConvert.toDouble(st.nextToken());
        }

        if ( tool_y_range != null ) {
          st = new StringTokenizer(tool_y_range);
          y_lo = YConvert.toDouble(st.nextToken());
          if ( st.hasMoreTokens() )
            y_hi = YConvert.toDouble(st.nextToken());
        }
      } catch (IllegalArgumentException e) {
        System.out.println("During map canvas creation: " + e);
      }

      if ( toolType == null )
        toolType = "XY";

      if ( toolType.equals("PT") ) {
        tool_type = TOOL_TYPE_PT;
        x_hi = x_lo;
        y_hi = y_lo;
      } else if ( toolType.equals("X") ) {
        tool_type = TOOL_TYPE_X;
        y_hi = y_lo;
      } else if ( toolType.equals("Y") ) {
        tool_type = TOOL_TYPE_Y;
        x_hi = x_lo;
      } else {
        // default to XY
        tool_type = TOOL_TYPE_XY;
      }

      if ( x_lo < grid.domain_X[LO] || x_lo > grid.domain_X[HI] )
        { x_lo = grid.domain_X[LO]; need_to_center = true; }
      if ( x_hi < grid.domain_X[LO] || x_hi > grid.domain_X[HI] )
        { x_hi = grid.domain_X[HI]; need_to_center = true; }
      if ( y_lo < grid.domain_Y[LO] || y_lo > grid.domain_Y[HI] )
        { y_lo = grid.domain_Y[LO]; need_to_center = true; }
      if ( y_hi < grid.domain_Y[LO] || y_hi > grid.domain_Y[HI] )
        { y_hi = grid.domain_Y[HI]; need_to_center = true; }

      toolArray[0].setRange_X(x_lo, x_hi);
      toolArray[0].setRange_Y(y_lo, y_hi);
      toolArray[0].setSnapping(true, true);
      toolArray[0].setUserBounds(x_lo, x_hi, y_lo, y_hi);

    }

//------------------------------------------------------------
// 6) Create a regionArray
  System.out.println("Reached step 6"); 

//    regionArray[0] = new PointRegion(165.0,-8.0,Color.yellow);
//    regionArray[1] = new PointRegion(180.0,-8.0,Color.yellow);


    map.setRegionArray(regionArray);

    set_strings();
 
  //  setBackground(Color.gray);
    setLayout( new BorderLayout() );  

 if (map==null) System.out.println("map is null!");
 if (entryPanel==null) System.out.println("entryPanel is null!");

    //1.1 
    add(map, "Center");
    //1.1 
    add(BorderLayout.EAST, entryPanel);    
    //add("Center", map);      // 1.0   
    //add("East", entryPanel); // 1.0
    setVisible(true);
    // register listeners
    //1.1 map.addMouseListener( new ActionMouse() );
    //1.1 enableEvents(AWTEvent.MOUSE_EVENT_MASK|AWTEvent.MOUSE_MOTION_EVENT_MASK);

  }



  /*
   * We need this in order to get the frame so we can change the cursor.
   */
  private Frame getFrame()
  {
    Container parent = this.getParent();

    while ( (parent != null) && !(parent instanceof Frame))
      parent = parent.getParent();

    return ((Frame) parent);
  }


  /**
   *  the only action we handle is a <RET> in one of TextFields
   */
  public boolean action(Event ev, Object arg) {

    double top = 0.0;
    double bottom = 0.0;
    double left = 0.0;
    double right = 0.0;
    //1.1 Object target = ev.getSource();
    Object target = ev.target;

    if (target instanceof TextField) {

      try {

	switch (tool_type) {
	case TOOL_TYPE_PT:
	  if ( target == North ) {
	    bottom = top = YConvert.toDouble(North.getText());
	    right = left = XConvert.toDouble(West.getText());
	  } else if ( target == South ) {
	    bottom = top = YConvert.toDouble(South.getText());
	    right = left = XConvert.toDouble(West.getText());
	  } else if ( target == East ) {
	    bottom = top = YConvert.toDouble(North.getText());
	    right = left = XConvert.toDouble(East.getText());
	  } else if ( target == West ) {
	    bottom = top = YConvert.toDouble(North.getText());
	    right = left = XConvert.toDouble(West.getText());
	  }
	  break;

	case TOOL_TYPE_X:
	  if ( target == North ) {
	    bottom = top = YConvert.toDouble(North.getText());
	  } else if ( target == South ) {
	    bottom = top = YConvert.toDouble(South.getText());
	  } else {
	    bottom = top = YConvert.toDouble(North.getText());
	  }
	  left = XConvert.toDouble(West.getText());
	  right = XConvert.toDouble(East.getText());
	  break;

	case TOOL_TYPE_Y:
	  if ( target == West ) {
	    right = left = XConvert.toDouble(West.getText());
	  } else if ( target == East ) {
	    right = left = XConvert.toDouble(East.getText());
	  } else {
	    right = left = XConvert.toDouble(West.getText());
	  }
	  top = YConvert.toDouble(North.getText());
	  bottom = YConvert.toDouble(South.getText());
	  break;

	case TOOL_TYPE_XY:
	default:
	  top = YConvert.toDouble(North.getText());
	  bottom = YConvert.toDouble(South.getText());
	  left = XConvert.toDouble(West.getText());
	  right = XConvert.toDouble(East.getText());
	  break;
	}

	if ( top < bottom ) {
	  double old_bottom = bottom;
	  bottom = top;
	  top = old_bottom;
	}

      map.getTool().setUserBounds(left, right, bottom, top);
      map.center_tool(1.0);

      } catch (IllegalArgumentException e) {

	System.out.println(e.toString());

      } finally {

	map.repaint();
	set_strings();

      }

    }
    return super.action(ev, arg);
  }
 
/*1.1 -----------------------------------------------------

  class ActionZoom implements ActionListener
  {
    public void actionPerformed(ActionEvent evt) {

      Object target = evt.getSource();
      if ( target == zoom_in ) {
      zoom_in();
      } else if ( target == zoom_out ) {
      zoom_out();
      }
    }
  }


  class ActionMouse implements MouseListener
  {
    public void mouseClicked( MouseEvent evt )
    {
      //System.out.println("MouseClicked: mousePressed()??..."+evt.getID());
    }
    public void mouseEntered( MouseEvent evt ) { }
    public void mouseExited( MouseEvent evt ) { }
    public void mousePressed( MouseEvent evt ) { }

    public void mouseReleased( MouseEvent evt )
    {
      //System.out.println("MouseReleased: set_string()..."+evt.getID());
      set_strings();
    }
  }

---------------------------------------------------- 1.1 */

/* 1.0 ----------------------V-------------------------- */

  public boolean handleEvent(Event evt) {
    if (evt.target instanceof Button && evt.id == Event.ACTION_EVENT) {
      String which = ((Button)evt.target).getLabel();
      if (which.equals("Zoom In")) {
	try {
	  map.zoom_in();
	} catch (MaxZoomException mze) {
	  System.out.println(mze);
	} catch (MinZoomException mze) {
	  System.out.println(mze);
	}
          return true;
      } else if (which.equals("Zoom Out")) {
	try {
	  map.zoom_out();
	} catch (MaxZoomException mze) {
	  System.out.println(mze);
	} catch (MinZoomException mze) {
	  System.out.println(mze);
	}
          return true;
      }

    } else if (evt.target instanceof TextField && evt.id == Event.LOST_FOCUS) {

      this.action(evt, null);

    }

    return super.handleEvent(evt);
  }
 
/* 1.0 ----------------------^-------------------------- */

  public void set_strings() {
    XText.setRange(grid.domain_X[LO],grid.domain_X[HI]);
    YText.setRange(grid.domain_Y[LO],grid.domain_Y[HI]);

    try {
      if ( tool_type == TOOL_TYPE_PT || tool_type == TOOL_TYPE_X ) {
        North.setText(YText.toString(map.getTool().user_Y[PT]));
        South.setText(YText.toString(map.getTool().user_Y[PT]));
      } else {
        North.setText(YText.toString(map.getTool().user_Y[HI]));
        South.setText(YText.toString(map.getTool().user_Y[LO]));
      }

      if ( tool_type == TOOL_TYPE_PT || tool_type == TOOL_TYPE_Y ) {
        East.setText(XText.toString(map.getTool().user_X[PT]));
        West.setText(XText.toString(map.getTool().user_X[PT]));
      } else {
        East.setText(XText.toString(map.getTool().user_X[HI]));
        West.setText(XText.toString(map.getTool().user_X[LO]));
      }
    } catch (IllegalArgumentException e) {
      System.out.println("During set_strings(): " + e);
    }

  }
 
//1.0 method: 
  public boolean mouseUp(Event evt, int x, int y) {
    set_strings();
    return true;
  }

  public void zoom_in() {
    try {
      map.zoom_in();
    } catch (MaxZoomException mze) {
      System.out.println(mze);
    } catch (MinZoomException mze) {
      System.out.println(mze);
    }
  }

  public void zoom_out() {
    try {
      map.zoom_out();
    } catch (MaxZoomException mze) {
      System.out.println(mze);
    } catch (MinZoomException mze) {
      System.out.println(mze);
    }
  }


////////////////////////////////////////////////////////////////////
//
// The following are methods to be called from javascript.
//

  /**
   * Make a tool in the toolArray the 'selected' tool.
   * Mouse events will affect the selected tool only.
   * @param i index of the tool in the toolArray
   */
  public void selectTool(int i) {
System.out.println("selectTool(" + i + ")");
    map.selectTool(i);
  }


  /**
   * Set a tool's color.
   * @param color_name String name of a color
   */
  public void setToolColor(String color_name) {
System.out.println("setToolColor(" + color_name + ")");
    if ( color_name.equals("black") ) {
      map.getTool().setColor(Color.black);
    } else if ( color_name.equals("white") ) {
      map.getTool().setColor(Color.white);
    } else if ( color_name.equals("blue") ) {
      map.getTool().setColor(Color.blue);
    } else if ( color_name.equals("red") ) {
      map.getTool().setColor(Color.red);
    } else if ( color_name.equals("green") ) {
      map.getTool().setColor(Color.green);
    } else if ( color_name.equals("yellow") ) {
      map.getTool().setColor(Color.yellow);
    } else if ( color_name.equals("orange") ) {
      map.getTool().setColor(Color.orange);
    }
    map.repaint();
  }


  /**
   * Replace a tool in the toolArray.
   * @param i index of the tool in the toolArray
   * @param type the type of the new, replacement tool
   */
  public void setTool(String type) {
    setTool(map.getSelected(),type);
  }

  /**
   * Replace a tool in the toolArray.
   * @param i index of the tool in the toolArray
   * @param type the type of the new, replacement tool
   */
  public void setTool(int i, String type) {

System.out.println("setTool(" + i + ", " + type + ")");
    MapTool newTool;
    MapTool oldTool = map.getTool(i);

    if ( type.equals("PT") ) {
      newTool = new PTTool(oldTool.getRectangle(),oldTool.getColor());
      tool_type = TOOL_TYPE_PT;
    } else if ( type.equals("X") ) {
      newTool = new XTool(oldTool.getRectangle(),oldTool.getColor());
      tool_type = TOOL_TYPE_X;
    } else if ( type.equals("Y") ) {
      newTool = new YTool(oldTool.getRectangle(),oldTool.getColor());
      tool_type = TOOL_TYPE_Y;
    } else if ( type.equals("XY") ) {
      newTool = new XYTool(oldTool.getRectangle(),oldTool.getColor());
      tool_type = TOOL_TYPE_XY;
/*
    } else if ( type.equals("XcY") ) {
      newTool = new XcYTool(oldTool.getRectangle(),oldTool.getColor());
      tool_type = TOOL_TYPE_XY;
    } else if ( type.equals("YcX") ) {
      newTool = new YcXTool(oldTool.getRectangle(),oldTool.getColor());
      tool_type = TOOL_TYPE_XY;
    } else if ( type.equals("PTcX") ) {
      newTool = new PTcXTool(oldTool.getRectangle(),oldTool.getColor());
      tool_type = TOOL_TYPE_X;
    } else if ( type.equals("PTcY") ) {
      newTool = new PTcYTool(oldTool.getRectangle(),oldTool.getColor());
      tool_type = TOOL_TYPE_Y;
    } else if ( type.equals("PTcXY") ) {
      newTool = new PTcXYTool(oldTool.getRectangle(),oldTool.getColor());
      tool_type = TOOL_TYPE_XY;
*/
    } else { // default to XY
      newTool = new XYTool(oldTool.getRectangle(),oldTool.getColor());
      tool_type = TOOL_TYPE_XY;
    }

    map.newToolFromOld(i, newTool, oldTool);
    map.repaint();
    set_strings();
  }

  public void setToolRange(int i, double x_lo, double x_hi, double y_lo, double y_hi) {
System.out.println("setToolRange(" + i + ", " + x_lo + ", " + x_hi +
                                         ", " + y_lo + ", " + y_hi + ")");
    XConvert.setRange(x_lo, x_hi);
    YConvert.setRange(y_lo, y_hi);
    toolArray[i].setRange_X(x_lo, x_hi);
    toolArray[i].setRange_Y(y_lo, y_hi);
    positionTool(i, x_lo, x_hi, y_lo, y_hi);
  }


  public void restrictToolRange(int i, double x_lo, double x_hi, double y_lo, double y_hi) {
System.out.println("restrictToolRange(" + i + ", " + x_lo + ", " + x_hi +
                                         ", " + y_lo + ", " + y_hi + ")");
    double [] xvals = {map.getTool().user_X[LO], map.getTool().user_X[HI]};
    double [] yvals = {map.getTool().user_Y[LO], map.getTool().user_Y[HI]};

    // The converters (ie. XConvert) may alter the incoming values
    // so we need to use their getRange() method to set the ranges
    // for the tool.
    XConvert.setRange(x_lo, x_hi);
    YConvert.setRange(y_lo, y_hi);
    toolArray[i].setRange_X(XConvert.getRange(LO), XConvert.getRange(HI));
    toolArray[i].setRange_Y(YConvert.getRange(LO), YConvert.getRange(HI));

    // If there is no intersection, default to the new range.
    try {
      xvals = XConvert.intersectRange(xvals[LO], xvals[HI]);
      yvals = YConvert.intersectRange(yvals[LO], yvals[HI]);
    } catch (IllegalArgumentException e) {
      System.out.println(e);
      System.out.println(e);
      xvals[LO] = XConvert.getRange(LO);
      xvals[HI] = XConvert.getRange(HI);
      yvals[LO] = YConvert.getRange(LO);
      yvals[HI] = YConvert.getRange(HI);
    } finally {
      map.getTool(i).setUserBounds(xvals[LO], xvals[HI], yvals[LO], yvals[HI]);
      map.center_tool(1.0);
      map.repaint();
      set_strings();
    }
  }


  public void positionTool(int i, double x_lo, double x_hi, double y_lo, double y_hi) {
System.out.println("positionTool(" + i + ", " + x_lo + ", " + x_hi +
                                         ", " + y_lo + ", " + y_hi + ")");
    double [] xvals = {x_lo, x_hi};
    double [] yvals = {y_lo, y_hi};

    // If there is no intersection, default to the old tool
    // values which, presumably, lie within the range.
    try {
      xvals = XConvert.intersectRange(x_lo, x_hi);
      yvals = YConvert.intersectRange(y_lo, y_hi);
    } catch (IllegalArgumentException e) {
      System.out.println(e);
      xvals[LO] = map.getTool().user_X[LO];
      xvals[HI] = map.getTool().user_X[HI];
      yvals[LO] = map.getTool().user_Y[LO];
      yvals[HI] = map.getTool().user_Y[HI];
    } finally {
      // An XTool should be able to lie on the bottom of the data region
      // but an XYTool should not.
      System.out.println("Tool needs range.  Keeping old tool values.");
      if ( (xvals[LO] == xvals[HI] && map.getTool().needsRange_X) ||
           (yvals[LO] == yvals[HI] && map.getTool().needsRange_Y) ) {
        xvals[LO] = map.getTool().user_X[LO];
        xvals[HI] = map.getTool().user_X[HI];
        yvals[LO] = map.getTool().user_Y[LO];
        yvals[HI] = map.getTool().user_Y[HI];
      }   
      map.getTool(i).setUserBounds(xvals[LO], xvals[HI], yvals[LO], yvals[HI]);
      map.center_tool(1.0);
      map.repaint();
      set_strings();
    }
  }


  // JC_TODO: setDelta currently adjusts Delta only for the selected tool
  public void setDelta(double delta_x, double delta_y) {
System.out.println("setDelta(" + delta_x + ", " + delta_y + ")");
    map.getTool().setDelta_X(delta_x);
    map.getTool().setDelta_Y(delta_y);
    map.selectTool(map.getSelected());
  }


  public void setImage(String img_name, double x_lo, double x_hi, double y_lo, double y_hi) {

    Image img=null;
System.out.println("setImage(" + img_name + ", " + x_lo + ", " + x_hi +
                                         ", " + y_lo + ", " + y_hi + ")");

    if ( img_name == null ) {
      System.out.println("Error setting Image: null string given as image name.");
    } else {
      img = getToolkit().getImage(img_name);
    }

    tracker = new MediaTracker(this);
    tracker.addImage(img, 1);

    //this.showStatus("Loading image");
    try {
      tracker.waitForID(1);
    } catch (InterruptedException e) {
      System.out.println("Caught InterruptedException while loading image.");
      //EHC: throw exception ?
      return;
    }
    if (tracker.isErrorID(1)) {
      System.out.println("Error loading image...");
//      this.showStatus("Error loading image.");
      //this.stop();
      //EHC: throw exception ?
      return;
    }

    XConvert.setRange(x_lo, x_hi);
    YConvert.setRange(y_lo, y_hi);
    map.getTool().setRange_X(x_lo,x_hi);
    map.getTool().setRange_Y(y_lo,y_hi);
    grid.setDomain_X(x_lo, x_hi);
    grid.setDomain_Y(y_lo, y_hi);
    map.setGrid(grid);
    map.setImage(img);

    restrictToolRange(map.getSelected(), x_lo, x_hi, y_lo, y_hi);
  }


  public String get_x_range() {
/*--
    // JC_NOTE: The commented out code would return values
    // within the range defined in XConvert but this can
    // result in (x_hi < x_lo) which Ferret doesn't handle
    // properly.  So I've used simpler code which doesn't
    // have any Longitude-wrap-around smarts.
    //
    // The 'smart' code is left here in case we ever wish
    // to return to it.

    Convert Xout = new ConvertLongitude();
    Xout.setRange(XConvert.getRange(LO),XConvert.getRange(HI));

    if ( grid.x_type != LONGITUDE_AXIS ) {
      Xout = new ConvertLength();
      Xout.setRange(XConvert.getRange(LO),XConvert.getRange(HI));
    }

    switch (tool_type) {
    case TOOL_TYPE_PT:
      sbuf.append(Xout.toString(map.getTool().user_X[PT]));
      break;

    case TOOL_TYPE_X:
      if ( grid.x_type == LONGITUDE_AXIS && x_lo == x_hi) {
        sbuf.append("0.0 360.0");
      } else if ( x_hi < x_lo ) {
        x_hi += 360.0;
        sbuf.append(Xout.toString(x_lo) + " " + Xout.toString(x_hi));
      } else {
        sbuf.append(Xout.toString(x_lo) + " " + Xout.toString(x_hi));
      }
      break;

    case TOOL_TYPE_Y:
      sbuf.append(Xout.toString(map.getTool().user_X[PT]));
      break;

    case TOOL_TYPE_XY:
      if ( grid.x_type == LONGITUDE_AXIS && x_lo == x_hi) {
        sbuf.append("0.0 360.0");
      } else if ( x_hi < x_lo ) {
        x_hi += 360.0;
        sbuf.append(Xout.toString(x_lo) + " " + Xout.toString(x_hi));
      } else {
        sbuf.append(Xout.toString(x_lo) + " " + Xout.toString(x_hi));
      }
      break;

    default:
      sbuf.append("");
      break;

    }
    return sbuf.toString();
--*/

    StringBuffer sbuf = new StringBuffer("");
    double x_lo=map.getTool().user_X[LO];
    double x_hi=map.getTool().user_X[HI];

    switch (tool_type) {
    case TOOL_TYPE_PT:
      sbuf.append(map.getTool().user_X[PT]);
      break;

    case TOOL_TYPE_X:
      if ( grid.x_type == LONGITUDE_AXIS && x_lo == x_hi) {
        sbuf.append("0.0 360.0");
      } else if ( x_hi < x_lo ) {
        x_hi += 360.0;
        sbuf.append(x_lo + " " + x_hi);
      } else {
        sbuf.append(x_lo + " " + x_hi);
      }
      break;

    case TOOL_TYPE_Y:
      sbuf.append(map.getTool().user_X[PT]);
      break;

    case TOOL_TYPE_XY:
      if ( grid.x_type == LONGITUDE_AXIS && x_lo == x_hi) {
        sbuf.append("0.0 360.0");
      } else if ( x_hi < x_lo ) {
        x_hi += 360.0;
        sbuf.append(x_lo + " " + x_hi);
      } else {
        sbuf.append(x_lo + " " + x_hi);
      }
      break;

    default:
      sbuf.append("");
      break;

    }
    return sbuf.toString();
  }


  public String get_y_range() {
    ConvertLatitude Yout = new ConvertLatitude();
    Yout.setRange(YConvert.getRange(LO),YConvert.getRange(HI));

    StringBuffer sbuf = new StringBuffer("");

      switch (tool_type) {
        case TOOL_TYPE_PT:
        sbuf.append(Yout.toString(map.getTool().user_Y[PT]));
        break;

        case TOOL_TYPE_X:
        sbuf.append(Yout.toString(map.getTool().user_Y[PT]));
        break;

        case TOOL_TYPE_Y:
        sbuf.append(Yout.toString(map.getTool().user_Y[LO]) + " " +
            Yout.toString(map.getTool().user_Y[HI]));
        break;

        case TOOL_TYPE_XY:
        sbuf.append(Yout.toString(map.getTool().user_Y[LO]) + " " +
            Yout.toString(map.getTool().user_Y[HI]));
        break;

        default:
        sbuf.append("");
        break;
      }

    return sbuf.toString();
  }


} 
