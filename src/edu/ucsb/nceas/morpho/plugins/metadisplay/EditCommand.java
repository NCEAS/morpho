/**
 *  '$RCSfile: EditCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-09-06 00:11:44 $'
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

import java.io.IOException;

import  edu.ucsb.nceas.morpho.util.Log;
import  edu.ucsb.nceas.morpho.util.IOUtil;
import  edu.ucsb.nceas.morpho.util.Command;

import  edu.ucsb.nceas.morpho.framework.EditorInterface;
import  edu.ucsb.nceas.morpho.framework.EditingCompleteListener;

import  edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import  edu.ucsb.nceas.morpho.plugins.ServiceController;
import  edu.ucsb.nceas.morpho.plugins.MetaDisplayInterface;
import  edu.ucsb.nceas.morpho.plugins.DocumentNotFoundException;

import  edu.ucsb.nceas.morpho.plugins.metadisplay.MetaDisplay;


public class EditCommand implements Command {

    private final MetaDisplay controller;
    
    EditCommand(MetaDisplay controller) 
    {
        this.controller = controller;
    }

    /**
     * execute this object's command
     */    
    public void execute() 
    {
        EditorInterface editor = null;
        String id = controller.getIdentifier();
        Log.debug(50,"EditCommand.execute() firing EDIT_BEGIN_EVENT. ID = "+id);
        controller.fireActionEvent( MetaDisplayInterface.EDIT_BEGIN_EVENT,  id);
        try
        {
          ServiceController services = ServiceController.getInstance();
          ServiceProvider provider = 
                          services.getServiceProvider(EditorInterface.class);
          editor = (EditorInterface)provider;
        }
        catch(Exception ee)
        {
          Log.debug(0, "Error acquiring editor plugin: " + ee.getMessage());
          ee.printStackTrace();
          return;
        }
        
        StringBuffer buffer = new StringBuffer();
        try {
            buffer = IOUtil.getAsStringBuffer(
                                controller.getFactory().openAsReader(id),true);
        } catch (DocumentNotFoundException dnfe) {
            Log.debug(0, "Error finding file : "+id+" "+dnfe.getMessage());
            dnfe.printStackTrace();
            return;
        } catch (IOException ioe) {
            Log.debug(0, "Error reading file : "+id+" "+ioe.getMessage());
            ioe.printStackTrace();
            return;
        }
        // * * * * * * * * * * * * * * * * * * * * * * * * * * * *
        // * *      WHAT ABOUT THE "LOCATION" PARAMETER?       * *
        // * * * * * * * * * * * * * * * * * * * * * * * * * * * *
        String location = null;
        editor.openEditor(buffer.toString(), id, location, controller);
    }
}
