package com.wutka.dtd;

import java.io.*;

/** Defines the method used for writing DTD information to a PrintWriter
 *
 * @author Mark Wutka
 * @version $Revision: 1.2 $ $Date: 2001-01-15 02:21:26 $ by $Author: higgins $
 */
public interface DTDOutput
{
    public void write(PrintWriter out) throws IOException;
}
