package com.wutka.dtd;

import java.io.*;

/** Represents the #PCDATA keyword in an Element's content spec
 *
 * @author Mark Wutka
 * @version $Revision: 1.2 $ $Date: 2001-01-15 02:21:26 $ by $Author: higgins $
 */
public class DTDPCData extends DTDItem
{
    public DTDPCData()
    {
    }

/** Writes out the #PCDATA keyword */
    public void write(PrintWriter out)
        throws IOException
    {
        out.print("#PCDATA");
        cardinal.write(out);
    }

    public boolean equals(Object ob)
    {
        if (ob == this) return true;
        if (!(ob instanceof DTDPCData)) return false;

        return super.equals(ob);
    }
}
