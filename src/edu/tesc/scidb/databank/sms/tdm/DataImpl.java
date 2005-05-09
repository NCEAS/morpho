/*
 * Created by IntelliJ IDEA.
 * User: crainm
 * Date: May 20, 2002
 * Time: 9:40:40 AM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package edu.tesc.scidb.databank.sms.tdm;


public class DataImpl implements Data
{
    String name;
    String value;


    public DataImpl(String name, String value)
    {
        this.name = name;
        this.value = value;
    }

    public String getName()
    {
        return name;
    }

    public String getValue()
    {
        return value;
    }
}
