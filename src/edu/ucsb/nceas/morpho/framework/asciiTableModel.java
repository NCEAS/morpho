/**
 *        Name: asciiTableModel.java
 *     Purpose: this class creates a Table model for use with JTable
 *     It assumes an input ascii string that defines a 'table';
 *     each row is a separate line; column items in each row are
 *     delimited by commas or tabs
 *     
 *    
 *   Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *     Authors: Dan Higgins
 *
 *     Version: '$Id: asciiTableModel.java,v 1.1 2000-05-31 15:37:07 higgins Exp $'
 */
 
package edu.ucsb.nceas.dtclient;
 
import java.io.*;
import java.util.StringTokenizer;
import javax.swing.table.AbstractTableModel;

public class asciiTableModel extends AbstractTableModel {
    int numrows;
    int numcols;
    String[][] table;
    
 public asciiTableModel(String str) {
	    String[] lines = {" "};
	// assume an ascii table is in a string; each row a line
	BufferedReader br = new BufferedReader(new StringReader(str));
	// first find out how many lines are in the string
	try{
	    while(br.readLine()!=null) {
	        numrows++;
	    }
	    lines = new String[numrows];
        br = new BufferedReader(new StringReader(str));
        for (int i=0;i<numrows;i++) {
	        lines[i] = br.readLine();
	    }
	}
	catch (Exception ee) {}
	// now each line is in the lines String array
    String dlm = ",\t";   // assume delimiter between tokens is comma or tab
    
	// assume the first line defines the number of tokens on each line ie num columns
	StringTokenizer s = new StringTokenizer(lines[0],dlm);
	numcols = 0;
	try{
	    for (;;) {
	        s.nextToken();
	        numcols++;
	    }
	}
	catch (Exception e) {}
	table = new String[numrows][numcols];
	for (int i=0;i<numrows;i++) {
	    StringTokenizer st = new StringTokenizer(lines[i],dlm);
	    for (int j=0;j<numcols;j++) {
	        try{
	          table[i][j] = st.nextToken();
	        }
	        catch (Exception eee) {}
	    }
	}
 }
 
 public int getRowCount() {
    return numrows;   
 }

 public int getColumnCount() {
    return numcols;   
 }
 
 public Object getValueAt(int row, int col) {
    return table[row][col];   
 }
    
}