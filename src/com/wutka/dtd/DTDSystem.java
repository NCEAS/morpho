package com.wutka.dtd;

import java.io.*;

/** Represents an external System ID in an entity declaration
 *
 * @author Mark Wutka
 * @version $Revision: 1.1 $ $Date: 2000-08-22 19:14:29 $ by $Author: higgins $
 */

public class DTDSystem extends DTDExternalID
{
    public DTDSystem()
    {
    }

/** Writes out a declaration for this SYSTEM ID */
    public void write(PrintWriter out)
    {
        if (system != null)
        {
            out.print("SYSTEM \"");
            out.print(system);
            out.print("\"");
        }
    }

    public boolean equals(Object ob)
    {
        if (ob == this) return true;
        if (!(ob instanceof DTDSystem)) return false;

        return super.equals(ob);
    }
}
