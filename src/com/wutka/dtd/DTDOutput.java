package com.wutka.dtd;

import java.io.*;

/** Defines the method used for writing DTD information to a PrintWriter
 *
 * @author Mark Wutka
 * @version $Revision: 1.3 $ $Date: 2001-06-06 22:44:57 $ by $Author: higgins $
 */
public interface DTDOutput
{
    public void write(PrintWriter out) throws IOException;
}
