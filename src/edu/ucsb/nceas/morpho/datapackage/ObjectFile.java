/**
 *  '$RCSfile: ObjectFile.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2002-08-21 22:20:11 $'
 * '$Revision: 1.2 $'
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

 /**
 * Class that implements a random access file for storing multiple objects that would take up too
 * much room in memory. Used here for storing a collection of Strings (records) for very large
 * text based data sets. When combined with the PersistentVector class and PersistentTableModel class
 * this gives the ability to have tables of almost unlimited size (since only a vector pointing to
 * locations in the object file is stored in RAM)
 */
 
package edu.ucsb.nceas.morpho.datapackage;
import java.io.*;
import java.util.*;

public class ObjectFile
{
    RandomAccessFile dataFile;
    String sFileName;
    
    public ObjectFile(String sName) throws IOException
    {
        sFileName = sName;
        File f = new File(sName);
        if (f.exists()) f.delete();
        dataFile = new RandomAccessFile(sName, "rw");
    }
    
    // returns file postion object was written to.
    public synchronized long writeObject(Serializable obj) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        oos.flush();
        
        int datalen = baos.size();
        
        //append record
        long pos = dataFile.length();
        dataFile.seek(pos);
        
        //write the length of the output
        dataFile.writeInt(datalen);
        dataFile.write(baos.toByteArray());
        
        baos = null;
        oos = null;
        
        return pos;
    }
    
    // get the current object length
    public synchronized int getObjectLength(long lPos) throws IOException
    {
        dataFile.seek(lPos);
        return dataFile.readInt();
    }
    
    public synchronized Object readObject(long lPos) throws IOException, ClassNotFoundException
    {
        dataFile.seek(lPos);
        int datalen = dataFile.readInt();
        if (datalen > dataFile.length()) {
            throw new IOException("Data file is corrupted. datalen: "+ datalen);
        }
        byte[] data = new byte[datalen];
        dataFile.readFully(data);
        
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object o = ois.readObject();
        
        bais = null;
        ois = null;
        data = null;
        
        return o;
    }
    
    public long length() throws IOException
    {
        return dataFile.length();
    }
    
    public void close() throws IOException
    {
        dataFile.close();
    }
    
    public void delete() {
      try{  
        dataFile.close();
        File f = new File(sFileName);
        if (f.exists()) f.delete();
      }
      catch (Exception w) {}
    }
    
    
	static public void main(String args[])
	{
		String testString = "This is a test!!!";
		try {
		    ObjectFile of = new ObjectFile("ObjectFile");
		    long pos = of.writeObject(testString);
		    Object res = of.readObject(pos);
		    System.out.println("Result: "+(String)res);
		}
		catch(Exception e) {}
	}
    
    
}