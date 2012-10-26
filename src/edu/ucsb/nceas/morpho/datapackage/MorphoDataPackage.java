/**
 *  '$RCSfile: AbstractDataPackage.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author$'
 *     '$Date$'
 * '$Revision$'
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


package edu.ucsb.nceas.morpho.datapackage;

import org.dataone.client.DataPackage;

/**
 * <p>A class that represents a data package in Morpho. This extends from DataONE's
 * DataPackage to include Morpho-specific features</p>
 *
 */
public class MorphoDataPackage extends DataPackage {
	
	// TODO: need to save in the D1Object collection, not just as member variable
	private AbstractDataPackage adp = null;
	
	public AbstractDataPackage getAbstractDataPackage() {
		// TODO: look up the ADP from the DataPackage D1Object collection
		return adp;
	}
	
	public void setAbstractDataPackage(AbstractDataPackage adp) {
		// TODO: save in the DataPackage collection
		this.adp = adp;
	}

}

