package edu.ucsb.nceas.morpho.util;

/**
 * This class represents the info for loading a node: 
 * 1. path for node
 * 2. index for node. default value is 0
 * @author tao
 *
 */
public class LoadDataPath 
{
	private String path = null;
	private int position = 0;
	
	/**
	 * Default Constructor
	 * @param path
	 */
    public LoadDataPath()
    {
   
    }
 
	
	/**
	 * Constructor
	 * @param path
	 */
    public LoadDataPath(String path)
    {
    	this.path = path;
    }
 
    /**
     * Gets the postion of the node
     * @return
     */
	public int getPosition() 
	{
		return this.position;
	}

	/**
	 * Set the postion for the node
	 * @param position
	 */
	public void setPosition(int position) {
		this.position = position;
	}
	
	/**
	 * Gets the path
	 * @return
	 */
	public String getPath()
	{
		return this.path;
	}
	
	
	/**
	 * Compare if two LoadDataPath objects are same.
	 * If path and position are same, we consider they are same.
	 * @param dataPath
	 * @return
	 */
	public boolean compareTo(LoadDataPath dataPath)
	{
		boolean same = false;
		String givenPath = dataPath.getPath();
		int givenPosition = dataPath.getPosition();
		Log.debug(45, "the obj with path "+path+" and position "+position+" compare object with path"+givenPath+" and postion "+givenPosition);
		if (path != null && path.equals(givenPath) && position == givenPosition)
		{
			Log.debug(45, "They are same LoadDataPath");
			same = true;
		}
		return same;
	}
	
	/**
	 * Clone a object
	 * @param dataPath
	 * @return
	 */
	public static LoadDataPath copy(LoadDataPath dataPath)
	{
		LoadDataPath newDataPath = new LoadDataPath();
		if(dataPath != null)
		{
			newDataPath = new LoadDataPath(dataPath.getPath());
			newDataPath.setPosition(dataPath.getPosition());
		}
		return newDataPath;
	}
    
}
