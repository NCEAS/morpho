/**
 *       Name: LiveMapPanel.java
 *    Purpose: Visual display for collecting query info from user
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2004-01-15 20:30:39 $'
 * '$Revision: 1.3 $'
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
 
 *  The LiveMap software used here
 *  was developed by the Thermal Modeling and Analysis
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
package edu.ucsb.nceas.morpho.query; 

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

import edu.ucsb.nceas.morpho.util.Log;

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

  final static int IMAGE_SIZE_X = 450;  // 450
  final static int IMAGE_SIZE_Y = 225;  // 225
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

    String img = "java_0_world_234k.jpg";
    String img_x_domain = "-180 180";
    String img_y_domain = "-90 90";
    String toolType = "XY";
    String tool_x_range = "-180 180";
    String tool_y_range = "-90 90";

    img_0 = img_0 = getToolkit().getImage(
              getClass().getResource("java_0_world_234k.jpg"));
   
    tracker = new MediaTracker(this);
    tracker.addImage(img_0, 1);

    try {
      tracker.waitForID(1);
    } catch (InterruptedException e) {
      System.out.println("Caught InterruptedException while loading image.");
      //EHC: throw exception ?
      return;
    }
    if (tracker.isErrorID(1)) {
      System.out.println("Error loading image...");
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
 
// added by DFH
  public void setBoundingBox(double top, double left, double bottom, double right) {
    try{
      map.getTool().setUserBounds(left, right, bottom, top);
      map.center_tool(1.0);
    } catch (Exception w) {
      Log.debug(9, "error in setting Bounding Box in LiveMapPanel!");
    }
	  map.repaint();
	  set_strings();  
  }
 
 public double getNorth() {
   double ret = 0.0;
   ret = YConvert.toDouble(North.getText());
   return ret;
 }
 
 public double getWest() {
   double ret = 0.0;
   ret = XConvert.toDouble(West.getText());
   return ret;
 }
 
 public double getSouth() {
   double ret = 0.0;
   ret = YConvert.toDouble(South.getText());
   return ret;
 }
 
 public double getEast() {
   double ret = 0.0;
   ret = XConvert.toDouble(East.getText());
   return ret;
 }
 
 
 
 
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

  public double getTop() {
	  double top = YConvert.toDouble(North.getText());
    return top;
  }
  public double getBottom() {
	  double bottom = YConvert.toDouble(South.getText());
    return bottom;
  }
  public double getLeft() {
	  double left = XConvert.toDouble(West.getText());
    return left;
  }
  public double getRight() {
	  double right = XConvert.toDouble(East.getText());
    return right;
  }
  


} 
