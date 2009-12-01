package edu.ucsb.nceas.morpho.plugins;

import edu.ucsb.nceas.utilities.OrderedMap;

/**
 * This class represents an attribute is edited in DataPackage command.
 * @author tao
 *
 */
public class EditingAttributeInfo
{
  private int entityIndex = -1;
  private int attributeIndex = -1; 
  private OrderedMap data = null;
  private Boolean insertionBeforeSelection = null;
  
  /**
   * Constructor
   * @param entityIndex  index of entity in abstract data package
   * @param attributeIndex index of attribute in the entity
   * @param data the orderedMap contains the attribute info
   * @param insertionBeforeSelection inserting a new column before selection
   */
  public EditingAttributeInfo(int entityIndex, int attributeIndex, OrderedMap data, Boolean insertionBeforeSelection)
  {
    this.entityIndex = entityIndex;
    this.attributeIndex = attributeIndex;
    this.data = data;
    this.insertionBeforeSelection = insertionBeforeSelection;
  }

  /**
   * Gets the entity index
   * @return
   */
  public int getEntityIndex()
  {
    return entityIndex;
  }

  /**
   * Gets the attribute index
   * @return
   */
  public int getAttributeIndex()
  {
    return attributeIndex;
  }

  /**
   * Gets the ordered map
   * @return
   */
  public OrderedMap getData()
  {
    return data;
  }

  /**
   * Gets the value of insertion before the column selection 
   * @return
   */
  public Boolean getInsertionBeforeSelection()
  {
    return insertionBeforeSelection;
  }
  
  
}
