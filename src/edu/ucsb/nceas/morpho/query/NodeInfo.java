/**
 *        Name: NodeInfo.java
 *     Purpose: A Class for storing tree node data
 *   Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *     Authors: Dan Higgins
 *
 *     Version: '$Id: NodeInfo.java,v 1.1 2000-09-21 22:50:58 higgins Exp $'
 */

package edu.ucsb.nceas.querybean;

import java.util.Hashtable;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.*;
import java.io.*;
import com.wutka.dtd.*;

public class NodeInfo implements Serializable
{
    static Hashtable icons;
    static Hashtable images;
    static { icons = new Hashtable(); 
             images = new Hashtable();}
    String name;
    String target;
    String iconName;    // iconName is string used to get icon from icons Hashtable; assumed to be filename
    String cardinality = "ONE";  // allowed values - ONE, ZERO to MANY, ONE to MANY, OPTIONAL
    boolean Item;
    Hashtable attr;
 public NodeInfo(String name) {
    attr = new Hashtable();
    this.name = name;
    this.target = null;
    this.iconName = null;
 }
    public String toString() {
        if (name.equalsIgnoreCase("#PCDATA")) {
          return ((String)attr.get("Value"));
        }
        else {return name;}
    }
    public void setCardinality(String card) {
        this.cardinality = card;
/*        if(card.equalsIgnoreCase("ONE")) {
            setIcon("greensq.gif");    
        }
        if(card.equalsIgnoreCase("OPTIONAL")) {
            setIcon("greenqumark.gif");    
        }
        if(card.equalsIgnoreCase("ZERO to MANY")) {
            setIcon("greenasterisk.gif");    
        }
        if(card.equalsIgnoreCase("ONE to MANY")) {
            setIcon("greenplus.gif");    
        }
        if(card.equalsIgnoreCase("SELECTED")) {
            setIcon("sel.gif");    
        }
        if(card.equalsIgnoreCase("NOT SELECTED")) {
            setIcon("unsel.gif");    
        }
 */       
    }
    public String getCardinality() {
        return cardinality;    
    }
    
    public boolean getItem() {
        return Item;
    }
    public void setItem(boolean o) {
        Item = o;
    }
    
    public void setName (String name) {
        this.name = name;
    }
    public void setTarget (String target) {
        this.target = target;
    }
    public String getName() {
        return name;
    }
    public String getTarget() {
        return target;
    }
    public void setIcon(String name) {
        iconName = name;
        if (!icons.containsKey(name)) {    //see if icon is not already in hashtable
            ImageIcon temp = new ImageIcon("icons"+System.getProperty("file.separator")+name);
            icons.put(name,temp);
        }
    }
    public ImageIcon getIcon() {
        if (iconName!=null) {
        if (icons.containsKey(iconName)) {
            return (ImageIcon)(icons.get(iconName));
        }
        else { return null; }
        }
        return null;
    }
    public void setImage(String name) {
        iconName = name;
        if (!name.equalsIgnoreCase("")) {
        if (!images.containsKey(name)) {    //see if image is not already in hashtable
            Image temp = Toolkit.getDefaultToolkit().getImage("icons"+System.getProperty("file.separator")+name);
            images.put(name,temp);
        }}
    }
    public Image getImage() {
        if (iconName!=null) {
        if (icons.containsKey(iconName)) {
            return (Image)(images.get(iconName));
        }
        else { return null; }
        }
        return null;
    }
    
    public String getIconName() {
        return iconName;
    }
	//{{DECLARE_CONTROLS
	//}}
}