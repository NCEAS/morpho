/*
 * Created by IntelliJ IDEA.
 * User: crainm
 * Date: May 20, 2002
 * Time: 9:12:53 AM
 * To change template for new interface use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package edu.tesc.scidb.databank.sms.tdm;

import java.util.List;

public interface Row
{
    void addData(String name, String Value);

    List getData();

}
