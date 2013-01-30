/**
 *  '$RCSfile: CacheAccessException.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2002-08-06 21:10:39 $'
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
package edu.ucsb.nceas.morpho.datastore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;
import javax.activation.FileDataSource;

import org.dataone.client.MNode;
import org.dataone.service.types.v1.Identifier;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.util.Log;

/**
 * This class represents a DataSource object initially read from the DataONE network. 
 * However, it writes the new Outputstream into the local cache file store. In another word, 
 * it sets the new data to the local cache file store rather than the DataONE network.
 * After set the new data, it will read data from the local cache file store.
 * @author tao
 *
 */
public class DataONEDataSource implements DataSource {
    private Identifier id = null;
    private FileDataSource fileDataSource = null;
    private MNode node = null;
    private boolean resetData = false;
    
    /**
     * Define a constructor with specified id.
     * @param id
     */
    public DataONEDataSource(MNode node, Identifier id) throws NullPointerException {
        if(node == null) {
            throw new NullPointerException("DataONEDataSource.DataONEDataSource - the node can't be null as the parameter of the constructor.");
        }
        if(id == null) {
            throw new NullPointerException("DataONEDataSource.DataONEDataSource - the id can't be null as the parameter of the constructor.");
        }
        this.node = node;
        this.id = id;
    }

    
    /**
     * This method hasn't been implemented. The null will be returned.
     */
    @Override
    public String getContentType() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Initially it will return the InputStream from the DataONE network.
     * If the the DataSource was reset, it will return the InputStream from the local temporary file DataSource.
     */
    @Override
    public InputStream getInputStream() throws IOException {
        if(fileDataSource != null && resetData) {
            //if file data source available (the data source has been reset),
            //we get the input stream from the file data source
            return fileDataSource.getInputStream();
        } else if (node != null){
            InputStream input = null;
            try {
                input = node.get(id);
            } catch (Exception e) {
                Log.debug(15, "DataONEDataSource.getInputStream - Morpho can't read the data for "+id.getValue()+" from the node "+node.getNodeId()+":\n"+e.getMessage());
            }
            return input; 
        } else {
            return null;
        }
        
    }

    /**
     * Get the identifier's value as the name.
     */
    @Override
    public String getName() {
        // TODO Auto-generated method stub
        if(id != null) {
            return id.getValue();
        } else {
            return null;
        }
       
    }

    /**
     * Get the OutputStream from a local cache file DataSource for writing. 
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
       String identifier = id.getValue();
       File cache = null;
       try {
           cache = Morpho.thisStaticInstance.getLocalDataStoreService().openCacheFile(identifier);
       } catch (FileNotFoundException e) {
           //create a new cache file by set the file with null inputstream
           cache = Morpho.thisStaticInstance.getLocalDataStoreService().saveCacheDataFile(identifier, null);
       }
       
       fileDataSource = new FileDataSource(cache);
       OutputStream out = fileDataSource.getOutputStream();
       resetData = true;
       return out;
    }

    /**
     * Get the id of this data source.
     * @return the id of the data source.
     */
    public Identifier getId() {
        return id;
    }

    /**
     * Reset id for this data source.
     * @param id the id will be set.
     */
    public void setId(Identifier id) {
        if(id == null) {
            throw new NullPointerException("DataONEDataSource.setId - the id can't be null as the parameter of the method.");
        }
        this.id = id;
    }
}
