/**
 *        Name: NodeInfoDG.java
 *     Purpose: A Class for creating a DataGuide JavaBean for use Desktop Client
 *   Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *     Authors: Dan Higgins
 *
 *     Version: '$Id: NodeInfoDG.java,v 1.1 2000-08-22 19:16:09 higgins Exp $'
 */

package edu.ucsb.nceas.querybean;
import java.util.Hashtable;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.*;

public class NodeInfoDG
{
    static Hashtable icons;
    static Hashtable images;
    static { icons = new Hashtable(); 
             images = new Hashtable();}
    String name;
    String target;
    String iconName;    // iconName is string used to get icon from icons Hashtable; assumed to be filename
    String cardinality = "NONE";  // allowed values - NONE, ZEROMANY, ONEMANY, OPTIONAL
    
    String type;
    String matchText;
    
    Hashtable attr;
 public NodeInfoDG(String name) {
    attr = new Hashtable();
    this.name = name;
    this.target = null;
    this.iconName = null;
 
		//{{INIT_CONTROLS
		//}}
	}
    public String toString() {
        if ((type!=null)&&(matchText!=null)) {
            return (name+ ":-"+type+" '"+matchText+"'");
        }
        else {return name;}
    }
    public void setCardinality(String card) {
        this.cardinality = card;
/*        if(card.equalsIgnoreCase("NONE")) {
            setIcon("greensq.gif");    
        }
        if(card.equalsIgnoreCase("OPTIONAL")) {
            setIcon("greenqumark.gif");    
        }
        if(card.equalsIgnoreCase("ZEROMANY")) {
            setIcon("greenball.gif");    
        }
        if(card.equalsIgnoreCase("ONEMANY")) {
            setIcon("greenplus.gif");    
        }
  */      
    }
    public String getCardinality() {
        return cardinality;    
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
            ImageIcon temp = null;
//            ImageIcon temp = new ImageIcon("icons"+System.getProperty("file.separator")+name);
//            if (temp==null) {
                try{
                temp = new ImageIcon(getClass().getResource(name)); 
                }
                catch (Exception e) {}
//            }
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