/**
 *        Name: MyAction.java
 *     Purpose: this class creates Action items that
 *     can be launched from menu.
 *    
 *   Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *     Authors: Dan Higgins
 *
 *     Version: '$Id: MyAction.java,v 1.1 2000-05-31 15:37:07 higgins Exp $'
 */
 
package edu.ucsb.nceas.dtclient;
 
 
import javax.swing.*;
import java.io.*;
import java.util.Vector;

import java.awt.event.*;
public class MyAction extends javax.swing.AbstractAction
{
    
    static public Vector ActionList; //list of actions
    static {
        ActionList=new Vector();
        try{
        FileInputStream in = new FileInputStream("LaunchMenuActionList.ser");
        ObjectInputStream s = new ObjectInputStream(in);
        ActionList = (Vector)s.readObject();
        }
        catch (Exception e) {System.out.println("Could not read Launch Menu list.");}
    }
    
    String execAction;
    
    public MyAction( String name, String executeString) {
        super(name); 
        execAction = executeString;
        ActionList.add(this);
    }
    
    static public void saveActionList() {
      if (ActionList.size()>0){
        try{
            FileOutputStream out = new FileOutputStream("LaunchMenuActionList.ser");
            ObjectOutputStream s = new ObjectOutputStream(out);
            s.writeObject(ActionList);
            s.flush();        
        }
        catch (Exception w) {System.out.println("Error in saving ActionList");}
      }
    }
    
    static public Vector getActionList() {
        return ActionList;
    }
    
    public void actionPerformed(ActionEvent e)
    {
        System.out.println("Action is performed: " + execAction);
        try {
            Runtime.getRuntime().exec(execAction);
        }
        catch (Exception ex) {System.out.println("Error trying to execute command");}
        
    }

}