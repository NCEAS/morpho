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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dataone.client.D1Object;
import org.dataone.client.DataPackage;
import org.dataone.service.types.v1.Identifier;
import org.dataone.service.types.v1.SystemMetadata;

/**
 * <p>A class that represents a data package in Morpho. This extends from DataONE's
 * DataPackage to include Morpho-specific features</p>
 *
 */
public class MorphoDataPackage extends DataPackage {
	
 
	/**
	 * TODO: remove this field
	 * @deprecated need to save in the D1Object collection, not just as member variable
	 */
	private AbstractDataPackage adp = null;
	
	public AbstractDataPackage getAbstractDataPackage() {
		// look up the ADP from the DataPackage D1Object collection?
		AbstractDataPackage tempAdp = null;
		Iterator<Identifier> metadataIdentifiers = this.getMetadataMap().keySet().iterator();
		while (metadataIdentifiers.hasNext()) {
			Identifier id = metadataIdentifiers.next();
			D1Object d1Object = this.get(id);
			if (d1Object instanceof AbstractDataPackage) {
				tempAdp = (AbstractDataPackage) d1Object;
				return tempAdp;
			}
		}
		
		// TODO: don't keep reference here
		return adp;
	}
	
	public void setAbstractDataPackage(AbstractDataPackage adp) {
		// save in the DataPackage collection
		this.addData(adp);

		// make sure the map has everything
		Map<Identifier, List<Identifier>> adpMetadataMap = getMetadataMap();
		List<Identifier> dataIds = new ArrayList<Identifier>();;
		if (adp.getEntityArray() != null) {
			int entityIndex = 0;
			for (Entity entity: adp.getEntityArray()) {
				String URLinfo = adp.getDistributionUrl(entityIndex, 0, 0);
				String dataId = AbstractDataPackage.getUrlInfo(URLinfo);
				Identifier identifier = new Identifier();
				identifier.setValue(dataId);
				dataIds.add(identifier);
				
				this.addData(entity);
			}
		}
		adpMetadataMap.put(adp.getIdentifier(), dataIds);
		this.setMetadataMap(adpMetadataMap);
		
		// TODO: don't keep reference here
		this.adp = adp;
	}
	
	
	/**
	 * Update the resourceMap and d1Object's identifier
	 * @param oldId
	 * @param newId
	 */
  public void updateIdentifier(String oldId, String newId) {
    if(oldId != null && newId != null) {
      Identifier oldIdentifier = new Identifier();
      oldIdentifier.setValue(oldId);
      Identifier newIdentifier = new Identifier();
      newIdentifier.setValue(newId);
      //update metadata map first
      Map<Identifier, List<Identifier>> metadataMap = getMetadataMap();
      if(metadataMap != null) {
       
        List<Identifier> list = metadataMap.get(oldIdentifier);
        if(list != null) {
          //the oldId is a metadata id. update the key (metadata id) and keep the value (list)
          metadataMap.remove(oldIdentifier);
          metadataMap.put(newIdentifier, list);
          setMetadataMap(metadataMap);
          
        } else {
          // the oldId is not a metadata id
          AbstractDataPackage adp = getAbstractDataPackage();
          if(adp != null) {
            String metadataId = adp.getAccessionNumber();
            Identifier metadataIdentifier = new Identifier();
            metadataIdentifier.setValue(metadataId);
            list = metadataMap.get(metadataIdentifier);
            if(list != null) {
              if(list.contains(oldIdentifier)) {
                list.remove(oldIdentifier);
              }
              list.add(newIdentifier);
            }
          }
        }
      }
      // update D1Object as well.
      D1Object d1Object = get(oldIdentifier);
      if(d1Object != null ) {
        SystemMetadata systeMetadata = d1Object.getSystemMetadata();
        if(systeMetadata != null) {
          systeMetadata.setIdentifier(newIdentifier);
        }
        // replace in the object map
        remove(oldIdentifier);
        addData(d1Object);
      }
    }
  }

}

