package com.wutka.dtd;

import java.io.*;

/** Represents an external ID in an entity declaration
 *
 * @author Mark Wutka
 * @version $Revision: 1.3 $ $Date: 2001-06-06 22:44:57 $ by $Author: higgins $
 */
public abstract class DTDExternalID implements DTDOutput
{
    public String system;

    public DTDExternalID()
    {
    }

/** Writes out a declaration for this external ID */
    public abstract void write(PrintWriter out)
        throws IOException;
    
    public boolean equals(Object ob)
    {
        if (ob == this) return true;
        if (!(ob instanceof DTDExternalID)) return false;

        DTDExternalID other = (DTDExternalID) ob;

        if (system == null)
        {
            if (other.system != null) return false;
        }
        else
        {
            if (!system.equals(other.system)) return false;
        }

        return true;
    }

/** Sets the system ID */
    public void setSystem(String aSystem)
    {
        system = aSystem;
    }

/** Retrieves the system ID */
    public String getSystem()
    {
        return system;
    }
}
