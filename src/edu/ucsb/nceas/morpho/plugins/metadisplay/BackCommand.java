/**
 *  '$RCSfile: BackCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: sambasiv $'
 *     '$Date: 2004-04-05 21:58:20 $'
 * '$Revision: 1.6 $'
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

import  edu.ucsb.nceas.morpho.util.Log;
import  edu.ucsb.nceas.morpho.util.Command;
import  edu.ucsb.nceas.morpho.util.StateChangeEvent;
import  edu.ucsb.nceas.morpho.util.StateChangeMonitor;

import  edu.ucsb.nceas.morpho.plugins.metadisplay.MetaDisplay;
import  edu.ucsb.nceas.morpho.util.DocumentNotFoundException;

import java.awt.Component;
import java.awt.event.ActionEvent;

public class BackCommand implements Command {

    private final MetaDisplay controller;
    
    BackCommand(MetaDisplay controller) 
    {
        this.controller = controller;
    }

    /**
     * execute this object's command
     */    
    public void execute(ActionEvent event) 
    {
        Log.debug(50,
          "BackCommand.execute() called. Doing controller.displayPrevious()...");
        try {
            controller.displayPrevious();
        } catch (DocumentNotFoundException dnfe) {
            Log.debug(2,"Error accessing previous document: "+dnfe.getMessage());
            dnfe.printStackTrace();
            return;
        }
        StateChangeMonitor.getInstance().notifyStateChange(
                new StateChangeEvent((Component)(controller.getMetaDisplayUI()), 
                                     StateChangeEvent.METAVIEWER_HISTORY_BACK));
    }
}
