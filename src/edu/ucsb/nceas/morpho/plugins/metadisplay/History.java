/**
 *  '$RCSfile: History.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-10-26 08:07:00 $'
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
 */

package edu.ucsb.nceas.morpho.plugins.metadisplay;

import java.util.Stack;
import java.util.Iterator;
import java.util.Properties;


/**
 *  A history implementation to wrap the underlying data structure
 */
public class History
{
//  * * * * * * * C L A S S    V A R I A B L E S * * * * * * *

    private Stack historyStack;
    
    /**
     *  constructor
     *
     */
    public History() 
    {
        historyStack = new Stack();
    }

    /**
     *  constructor
     *
     */
    public History(HistoryItem item) {
        this();
        add(item);
    }


	  /**
	   *  add the String identifier to the most-recent slot in history
	   *
       *  @param  the identifier to be added
	   */
	  public void add(HistoryItem item)
	  {
        if (item!=null) historyStack.push(item);
	  }

	  /**
	   *  Deletes the String identifier that was most recently added to history
	   */
	  public void deletePrevious()
	  {
	      getPrevious();  //...but don't return it!
	  }

	  /**
	   *  Gets the String identifier that was most recently added to history,
	   *  <em>and actually removes it from history</em>
	   *
	   *  @return  the String identifier that was most recently added to history
	   */
	  public HistoryItem getPrevious()
	  {
	      if (historyStack.empty()) return null;
	      return (HistoryItem)(historyStack.pop());
	  }

	  /**
	   *  Looks at the String identifier that was most recently added to history,
       *  <em>without actually removing it from history</em>
	   *
	   *  @return  the String identifier that was most recently added to history.
       *           Returns null if the History is empty
	   */
  	public String previewPreviousID()
	  {
	      if (historyStack.empty()) return null;
        return ( (HistoryItem)historyStack.peek() ).identifier;
	  }
    
	  /**
	   *  returns a String representation of all the elements in the History,
	   *  <em>without actually removing them from history</em>
	   *
	   *  @return  the String representation of all the elements in the History
	   */
	  public String toString()
	  {
	      if (historyStack.empty()) return "\n- - - History is Empty - - -\n";
        Stack tempStack = new Stack();  //copy stack so we can restore it later
        HistoryItem nextItem = null;
        StringBuffer buff = new StringBuffer("\n- - - History - - -\n");
        while (previewPreviousID()!=null) {
            nextItem = (HistoryItem)(historyStack.pop());
            if (nextItem==null) continue;
            tempStack.push(nextItem);
            buff.append("* ID: ");
            buff.append(nextItem.identifier);
            buff.append("\n  Transformer Parameters: \n");
            if (nextItem.transformProperties!=null){
                Iterator keys = nextItem.transformProperties.keySet().iterator();
                Object nextKey = null;
                while (keys.hasNext()) {
                    nextKey = keys.next();
                    buff.append("  -> ");
                    buff.append(String.valueOf(nextKey));
                    buff.append(" = ");
                    buff.append(String.valueOf(nextItem.transformProperties.get(nextKey)));
                    buff.append("\n");
                }            
            }
        }
	      buff.append("- - End History - -\n");
        
	      while (!tempStack.empty()) {
	      
            historyStack.push(tempStack.pop());
        }
        
	      return buff.toString();
	  }
    
    /**
     *  public accessro for protected class; primarily for testing
     */
    public HistoryItem getNewHistoryItemInstance(String id, Properties params){
    
        return new HistoryItem(id, params);
    }
}

class HistoryItem
{
    public String     identifier;
    public Properties transformProperties;
    
    public HistoryItem(String id, Properties params){
    
        identifier = id;
        transformProperties = params;
    }
}
