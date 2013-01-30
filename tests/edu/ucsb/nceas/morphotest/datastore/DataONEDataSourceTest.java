/**  '$RCSfile: EML201DocumentCorrectorTest.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2008-09-25 22:56:13 $'
 * '$Revision: 1.3 $'
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
package edu.ucsb.nceas.morphotest.datastore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.dataone.client.MNode;
import org.dataone.service.types.v1.Identifier;

import junit.framework.Test;
import junit.framework.TestSuite;
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datastore.DataONEDataSource;
import edu.ucsb.nceas.morpho.datastore.DataONEDataStoreService;
import edu.ucsb.nceas.morpho.datastore.LocalDataStoreService;
import edu.ucsb.nceas.morphotest.MorphoTestCase;

public class DataONEDataSourceTest extends MorphoTestCase {
    
    private final static String ID = "tao.464";
    private final static String NEWDATA = "new data";
    private final static String NEWID = "tao.1";
    private final static String MNURL = "https://mn-demo-5.test.dataone.org/knb/d1/mn";
    private final static String originalData = "\"1\",\"2\"\n\"3\",\"4\"\n\"5\",\"6\"\n";
    /**
     * Create a suite of tests to be run together
     */
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite();
        suite.addTest(new DataONEDataSourceTest("initialize"));
        suite.addTest(new DataONEDataSourceTest("testReadAndSet"));
        return suite;
    }
    
    /**
     * Constructor a test
     * @param name the name of test
     */
    public DataONEDataSourceTest(String name) {
      super(name);
      Morpho.thisStaticInstance.setLocalDataStoreService(new LocalDataStoreService(Morpho.thisStaticInstance));
    }
    
    /**
     * Check that the testing framework is functioning properly with 
     * a trivial assertion.
     */
    public void initialize() {
        assertTrue(true);
    }
    
    public void testReadAndSet() throws Exception {
       
        MNode node = new MNode(MNURL);
        Identifier id = new Identifier();
        id.setValue(ID);
        DataONEDataSource dataSource = new DataONEDataSource(node, id);
        InputStream input = dataSource.getInputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        int size = input.read(b);
        while (size != -1) {
            out.write(b, 0, size);
            size = input.read(b);
        }
        String outStr = out.toString();
        out.close();
        input.close();
        assertTrue("Output "+outStr+" doesn't match the orgininal data "+originalData, outStr.equals(originalData));
        
        //reset the data to the DataSource
        id.setValue(NEWID);
        dataSource.setId(id);
        OutputStream output = dataSource.getOutputStream();
        ByteArrayInputStream byteInput = new ByteArrayInputStream(NEWDATA.getBytes());
        size = byteInput.read(b);
        while (size !=-1) {
            output.write(b,0,size);
            size = byteInput.read(b);
        }
        output.close();
        byteInput.close();
        //read the new data
        input = dataSource.getInputStream();
        out = new ByteArrayOutputStream();   
        size = input.read(b);
        while (size != -1) {
            out.write(b, 0, size);
            size = input.read(b);
        }
        outStr = out.toString();
        out.close();
        input.close();
        assertTrue("Output "+outStr+" doesn't match the orgininal data "+originalData, outStr.equals(NEWDATA));
       
    }
}
