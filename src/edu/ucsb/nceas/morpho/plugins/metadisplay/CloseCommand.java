/**
 *  '$RCSfile: CloseCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2002-09-06 07:12:15 $'
 * '$Revision: 1.1.2.1 $'
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

import  edu.ucsb.nceas.morpho.plugins.MetaDisplayInterface;

import  edu.ucsb.nceas.morpho.plugins.metadisplay.MetaDisplay;

import java.awt.event.ActionEvent;

public class CloseCommand implements Command {

    private final MetaDisplay controller;
    
    CloseCommand(MetaDisplay controller) 
    {
        this.controller = controller;
    }

    /**
     * execute this object's command
     */    
    public void execute(ActionEvent event) 
    {
        Log.debug(50,"CloseCommand.execute() called. Firing EDIT_BEGIN_EVENT...");
        controller.fireActionEvent( MetaDisplayInterface.EDIT_BEGIN_EVENT,
                                    controller.getIdentifier() );
    }
}
