package com.wutka.dtd;

import java.io.*;
import java.util.*;

/** Represents a sequence in an element's content.
 * A sequence is declared in the DTD as (value1,value2,value3,etc.)
 *
 * @author Mark Wutka
 * @version $Revision: 1.1 $ $Date: 2000-08-22 19:14:29 $ by $Author: higgins $
 */
public class DTDSequence extends DTDContainer
{
    public DTDSequence()
    {
    }

/** Writes out a declaration for this sequence */
    public void write(PrintWriter out)
        throws IOException
    {
        out.print("(");

        Enumeration e = getItemsVec().elements();
        boolean isFirst = true;

        while (e.hasMoreElements())
        {
            if (!isFirst) out.print(",");
            isFirst = false;

            DTDItem item = (DTDItem) e.nextElement();
            item.write(out);
        }
        out.print(")");
        cardinal.write(out);
    }

    public boolean equals(Object ob)
    {
        if (ob == this) return true;
        if (!(ob instanceof DTDSequence)) return false;

        return super.equals(ob);
    }
}
