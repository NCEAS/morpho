/*
 * Created by IntelliJ IDEA.
 * User: crainm
 * Date: May 20, 2002
 * Time: 9:12:07 AM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package edu.tesc.scidb.databank.sms.tdm;

import java.util.LinkedList;
import java.util.List;

public class InsertImpl implements Insert
{

    LinkedList rows;
    String targetTable;
    String targetSchema;

    public InsertImpl(String targetTable, String targetSchema)
    {
        this.targetTable = targetTable;
        this.targetSchema = targetSchema;
        rows = new LinkedList();
    }

    public void addRow(Row row)
    {
        rows.add(row);
    }

    public List getRows()
    {
        return rows;
    }

    public String getTargetTable()
    {
        return targetTable;
    }

    public String getTargetSchema()
    {
        return targetSchema;
    }


}
