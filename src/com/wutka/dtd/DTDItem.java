package com.wutka.dtd;

import java.io.*;

/** Represents any item in the DTD
 *
 * @author Mark Wutka
 * @version $Revision: 1.2 $ $Date: 2001-01-15 02:21:26 $ by $Author: higgins $
 */
public abstract class DTDItem implements DTDOutput
{
/** Indicates how often the item may occur */
    public DTDCardinal cardinal;

    public DTDItem()
    {
        cardinal = DTDCardinal.NONE;
    }

    public DTDItem(DTDCardinal aCardinal)
    {
        cardinal = aCardinal;
    }

/** Writes out a declaration for this item */
    public abstract void write(PrintWriter out)
        throws IOException;

    public boolean equals(Object ob)
    {
        if (ob == this) return true;
        if (!(ob instanceof DTDItem)) return false;

        DTDItem other = (DTDItem) ob;

        if (cardinal == null)
        {
            if (other.cardinal != null) return false;
        }
        else
        {
            if (!cardinal.equals(other.cardinal)) return false;
        }

        return true;
    }

/** Sets the cardinality of the item */
    public void setCardinal(DTDCardinal aCardinal)
    {
        cardinal = aCardinal;
    }

/** Retrieves the cardinality of the item */
    public DTDCardinal getCardinal()
    {
        return cardinal;
    }
}
