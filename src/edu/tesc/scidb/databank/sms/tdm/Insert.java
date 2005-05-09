/*
 * Created by IntelliJ IDEA.
 * User: crainm
 * Date: May 20, 2002
 * Time: 9:11:53 AM
 * To change template for new interface use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package edu.tesc.scidb.databank.sms.tdm;

import java.util.List;

public interface Insert
{
    void addRow(Row row);

    List getRows();

    public String getTargetTable();

    public String getTargetSchema();

}
