/**
 *  '$RCSfile: ServiceRequest.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2001-04-27 23:03:50 $'
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

package edu.ucsb.nceas.morpho.framework;

import java.util.Hashtable;

/* 
 * This class is used to pass data to an object performing a service. It
 * contains an id that can be used to identify the request and a reference
 * to the request originator to use to provide the results.
 */
public class ServiceRequest
{
  /* The name of the service requested */
  private String serviceName = null;

  /* A reference to the object making the request */
  private PluginInterface requestor;

  /* All of the data objects, indexed by objectID */
  private Hashtable data = null;

  /* A request identifier, unique within the scope of the requestor */
  private String requestID = null;

  /**
   * Construct a new request for service by service name.  Typically
   * plugins would call the addData() method to add any data needed to
   * process the request.
   *
   * @param requestor a reference to the object making the service request
   * @param service the name of the service being requested
   */
  public ServiceRequest(PluginInterface requestor, String service)
  {
    this.data = new Hashtable();
    this.requestor = requestor;
    this.serviceName = service;
  }

  /**
   * Return the name of the service requested
   */
  public String getServiceName()
  {
    return serviceName;
  }

  /**
   * Set the name of the service requested
   */
  public void setServiceName(String serviceName)
  {
    this.serviceName = serviceName;
  }

  /**
   * Return a refernce to the object requesting service
   */
  public PluginInterface getRequestor()
  {
    return (PluginInterface)requestor;
  }

  /**
   * Set a refernce to the object requesting service
   */
  public void setRequestor(PluginInterface requestor)
  {
    this.requestor = requestor;
  }

  /**
   * Return a Hashtable of all data in this request
   */
  public Hashtable getData()
  {
    return data;
  }

  /**
   * Set a Hashtable of all data in this request
   */
  public void setData(Hashtable data)
  {
    if (data != null) {
      this.data = data;
    }
  }

  /**
   * Get the identifier for this request
   */
  public String getRequestID()
  {
    return requestID;
  }

  /**
   * Set the identifier for this request
   */
  public void setRequestID(String requestID)
  {
    this.requestID = requestID;
  }

  /**
   * Add a data object to the request using its referenced name
   */
  public void addDataObject(String dataName, Object dataObject)
  {
    data.put(dataName, dataObject);
  }

  /**
   * Get a data object from the request using its referenced name
   */
  public Object getDataObject(String dataName)
  {
    return data.get(dataName);
  }
}
