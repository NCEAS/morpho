/**
 *  '$RCSfile: BackCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-08-26 23:49:27 $'
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

package edu.ucsb.nceas.morpho.plugins.metadisplay;

import  edu.ucsb.nceas.morpho.util.Log;
import  edu.ucsb.nceas.morpho.util.Command;

import  edu.ucsb.nceas.morpho.plugins.MetaDisplayInterface;
import  edu.ucsb.nceas.morpho.plugins.DocumentNotFoundException;


public class BackCommand implements Command {

    private final MetaDisplayInterface controller;
    
    BackCommand(MetaDisplayInterface controller) 
    {
        this.controller = controller;
    }

    /**
     * execute this object's command
     */    
    public void execute() 
    {
        try {
            controller.displayPrevious();
        } catch (DocumentNotFoundException dnfe) {
            Log.debug(2,"Error accessing previous document: "+dnfe.getMessage());
            dnfe.printStackTrace();
        }
    }
}
