/*
 * Created by IntelliJ IDEA.
 * User: crainm
 * Date: May 20, 2002
 * Time: 9:13:09 AM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package edu.tesc.scidb.databank.sms.tdm;

import java.util.LinkedList;
import java.util.List;

public class RowImpl implements Row
{
    LinkedList data;

    public RowImpl()
    {

        data = new LinkedList();
    }

    public void addData(String name, String value)
    {
        data.add(new DataImpl(name, value));


        System.err.println("adding new data to Row");
    }

    public List getData()
    {
        return data;
    }


}
