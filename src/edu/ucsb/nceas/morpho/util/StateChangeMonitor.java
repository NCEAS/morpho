/**
 *  '$RCSfile: StateChangeMonitor.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2002-08-29 23:41:31 $'
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

package edu.ucsb.nceas.morpho.util;

import edu.ucsb.nceas.morpho.util.Log;

import java.util.Hashtable;
import java.util.Vector;

/**
 * Maintain a registry of objects that are interested in changes in 
 * application state.  When the application state changes (through
 * posting a notification to this class), distribute the StateChangeEvent
 * to all of the registered listeners. This class follows the singleton
 * pattern because we never need or want more than a single instance to
 * manage all of the change events.
 *
 * @author   Matt Jones
 */
public class StateChangeMonitor
{
    private static StateChangeMonitor monitor;
    private static final Object classLock = StateChangeMonitor.class;
    private Hashtable listeners = null;

    /**
     * Creates a new instance of the StateChangeMonitor, and is private
     * because this is a singleton.
     */
    private StateChangeMonitor()
    {
        // Create the registry of StateChangeListeners
        listeners = new Hashtable();
    }

    /**
     * Get the single instance of the StateChangeMonitor, 
     * creating it if needed.
     *
     * @return the single instance of the StateChangeMonitor
     */
    public static StateChangeMonitor getInstance()
    {
        synchronized (classLock) {
            if (monitor == null) {
                monitor = new StateChangeMonitor();
            }
            return monitor;
        }
    }
    
    /**
     * This method is called by objects to register a listener for changes in
     * the application state. Any change in the state will trigger 
     * notification.
     *
     * @param stateChange the name of the state change for which notifications
     *                    should be sent
     * @param listener  a reference to the object to be notified of changes
     */
    public void addStateChangeListener(String stateChange,
            StateChangeListener listener)
    {
        Vector currentStateListeners = null;
        if (!listeners.containsKey(stateChange)) {
            Log.debug(50, "Adding state vector: " + stateChange);
            currentStateListeners = new Vector();
            listeners.put(stateChange, currentStateListeners);
        } else {
            currentStateListeners = (Vector)listeners.get(stateChange);
        }

        if (!currentStateListeners.contains(listener)) {
            Log.debug(50, "Adding listener: " + listener.toString());
            currentStateListeners.addElement(listener);
        }
    }
    
    /**
     * This method is called by objects to remove a listener for changes in
     * the application state. Any change in the state will trigger 
     * notification.
     *
     * @param stateChange the name of the state change for which the listener
     *                    should be removed
     * @param listener  a reference to the object to be removed
     */
    public void removeStateChangeListener(String stateChange,
            StateChangeListener listener)
    {
        Vector currentStateListeners = null;
        if (listeners.containsKey(stateChange)) {
            currentStateListeners = (Vector)listeners.get(stateChange);
            Log.debug(50, "Removing listener: " + listener.toString());
            currentStateListeners.removeElement(listener);
            if (currentStateListeners.size() == 0) {
                listeners.remove(stateChange);
            }
        }
    }

    /**
     * Notify the monitor of an application state change so that it in turn
     * can notify all of the registered listeners of that state change.
     *
     * @param event the StateChangeEvent that has occurred
     */
    public void notifyStateChange(StateChangeEvent event)
    {
        String stateChange = event.getChangedState();
        Vector currentStateListeners = null;
        if (listeners.containsKey(stateChange)) {
            currentStateListeners = (Vector)listeners.get(stateChange);
            for (int i = 0; i < currentStateListeners.size(); i++) {
                StateChangeListener listener =
                    (StateChangeListener)currentStateListeners.elementAt(i);
                if (listener != null) {
                    listener.handleStateChange(event);
                }
            }
        }
    }
}
