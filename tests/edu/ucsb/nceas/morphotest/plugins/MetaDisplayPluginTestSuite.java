/**
 *  '$RCSfile: MetaDisplayPluginTestSuite.java,v $'
 *  Copyright: 2002 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-08-21 18:10:12 $'
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

package edu.ucsb.nceas.morphotest.plugins;

import edu.ucsb.nceas.morphotest.plugins.metadisplay.*;

import edu.ucsb.nceas.morpho.util.Log;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit test suite that is composed of 
 * individual test cases 
 * 
 */
public class MetaDisplayPluginTestSuite extends TestCase {

    public MetaDisplayPluginTestSuite(String name) {
        super(name);
        Log.getLog().setDebugLevel(50);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();    
        suite.addTest(new TestSuite(MetaDisplayTest.class));
        
        //etc
        
        return suite;
    }
    
}



