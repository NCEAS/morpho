/**
 *  '$RCSfile: ServiceController.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2002-08-16 21:54:29 $'
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

package edu.ucsb.nceas.morpho.plugins;

import edu.ucsb.nceas.morpho.util.Log;

import java.util.Hashtable;

/**
 * The ServiceController handles the registration of services and service lookup
 * for other objects. This is a singleton class because only one instance is
 * ever needed. Plugins that need to utilize a service should call
 * getServiceProvider(). The single instance of ServiceController can be
 * obtained statically using getInstance().
 *
 * @author   jones
 */
public class ServiceController
{
    private Hashtable servicesRegistry = null;
    private static ServiceController services;

    /**
     * Creates a new instance of ServiceController, but is private because this
     * is a singleton.
     */
    private ServiceController()
    {
        servicesRegistry = new Hashtable();
    }

    /**
     * Get the single instance of the ServiceController, creating it if needed.
     *
     * @return the single instance of the ServiceController
     */
    public static ServiceController getInstance()
    {
        if (services == null) {
            services = new ServiceController();
        }
        return services;
    }
    
    /**
     * This method is called by plugins to get a reference to an object that
     * implements a particular interface
     *
     * @param serviceInterface the service interface desired
     * @exception ServiceNotHandledException  Description of Exception
     * @return ServiceProvider a reference to the object providing the service
     */
    public ServiceProvider getServiceProvider(Class serviceInterface)
             throws ServiceNotHandledException
    {
        if (servicesRegistry.containsKey(serviceInterface)) {
            return (ServiceProvider) servicesRegistry.get(serviceInterface);
        } else {
            throw (new ServiceNotHandledException("No such service registered."));
        }
    }

    /**
     * This method is called by plugins to register a particular service that
     * the plugin can perform. The service is identified by the class of an
     * interface that the service implements.
     *
     * @param serviceInterface the interface representing this service
     * @param provider a reference to the object providing the service
     * @throws ServiceExistsException
     */
    public void addService(Class serviceInterface, ServiceProvider provider)
             throws ServiceExistsException
    {
        if (servicesRegistry.containsKey(serviceInterface)) {
            throw (new ServiceExistsException(serviceInterface.getName()));
        } else {
            Log.debug(20, "Adding service: " + serviceInterface.getName());
            servicesRegistry.put(serviceInterface, provider);
        }
    }

    /**
     * This method is called by plugins to determine if a particular service has
     * been registered and is available.
     *
     * @param serviceInterface  the service interface desired
     * @return boolean true if the service exists, false otherwise
     */
    public boolean checkForService(Class serviceInterface)
    {
        if (servicesRegistry.containsKey(serviceInterface)) {
            return true;
        } else {
            return false;
        }
    }
}

