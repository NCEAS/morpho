package edu.ucsb.nceas.editor;

import java.net.URL;
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
  // iconName is string used to get icon from the
  // icons Hashtable; assumed to be filename
  String iconName;  

  // allowed values - ONE, ZERO to MANY, ONE to MANY, OPTIONAL
  String cardinality = "ONE";  

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
    if(card.equalsIgnoreCase("ONE")) {
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
    //see if icon is not already in hashtable
    if (!icons.containsKey(name)) {  
      URL iconURL = ClassLoader.getSystemResource(
              "edu/ucsb/nceas/editor/icons/" + name);
      ImageIcon temp = new ImageIcon(iconURL);
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
  
  public String getIconName() {
    return iconName;
  }
}
