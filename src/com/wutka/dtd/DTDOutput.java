package com.wutka.dtd;

import java.io.*;

/** Defines the method used for writing DTD information to a PrintWriter
 *
 * @author Mark Wutka
 * @version $Revision: 1.1 $ $Date: 2000-08-22 19:14:29 $ by $Author: higgins $
 */
public interface DTDOutput
{
    public void write(PrintWriter out) throws IOException;
}
