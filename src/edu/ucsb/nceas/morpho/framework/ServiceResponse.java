/**
 *  '$RCSfile: ServiceResponse.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2001-04-25 22:23:00 $'
 * '$Revision: 1.2 $'
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

package edu.ucsb.nceas.dtclient;

import java.util.Hashtable;

/**
 * This object is returned to the initiator of the service upon 
 * completion of the service request.  It contains a reference to
 * the service request (identifier) and the data resulting from the
 * service request.
 */
public class ServiceResponse
{
  private String serviceName = null;

  private String requestID = null;

  private Hashtable data = null;

  public ServiceResponse(String serviceName, String requestID)
  {
    this.data = new Hashtable();
    this.serviceName = serviceName;
    this.requestID = requestID;
  }

  public String getServiceName()
  {
    return serviceName;
  }

  public void setServiceName(String serviceName)
  {
    this.serviceName = serviceName;
  }

  public String getRequestID()
  {
    return requestID;
  }

  public void setRequestID(String requestID)
  {
    this.requestID = requestID;
  }

  public Hashtable getData()
  {
    return data;
  }

  public void setData(Hashtable data)
  {
    if (data != null) {
      this.data = data;
    }
  }

  public void addDataObject(String dataName, Object dataObject)
  {
    data.put(dataName, dataObject);
  }

  public Object getDataObject(String dataName)
  {
    return data.get(dataName);
  }
}
