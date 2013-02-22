/**
 *  '$RCSfile: ResultSet.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: leinfelder $'
 *     '$Date: 2008-11-27 00:47:17 $'
 * '$Revision: 1.50 $'
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

package edu.ucsb.nceas.morpho.query;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.QueryRefreshInterface;
import edu.ucsb.nceas.morpho.util.ColumnSortableTableModel;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.SortableJTable;
import edu.ucsb.nceas.utilities.XMLUtilities;

/**
 * A ResultSet encapsulates the list of results returned from either a
 * local query or a Metacat query. It contains a reference to its
 * original query, so the result set can be refreshed by re-running the query.
 * Current MetaCat query returns a <document> element for each 'hit'
 * in query. That <document> element has 5 fixed children: <docid>, <docname>,
 * <doctype>, <createdate>, and <updatadate>.
 * Other child elements are determined by query and are returned as <param>
 * elements with a "name" attribute and the value as the content.
 */
public class ResultSet extends AbstractTableModel implements ColumnSortableTableModel
{
  /** store a private copy of the Query run to create this resultset */
  private Query savedQuery = null;

  /** Store each row of the result set as a row in a Vector */
  protected Vector resultsVector = null;

  /** A reference to the Morpho */
  private Morpho morpho = null;

  private String[] headers;
  
  protected HashSet<String> incompleteDocidSet = new HashSet<String>();

  // A hash table to store mapping between column name and resultSVector Index
  private Hashtable<String, Integer> mapColumnNameAndVectorIndex = new Hashtable<String, Integer>();

  /** The icon for representing local storage. */
  protected static ImageIcon localIcon = null;
  /** The icon for representing usere saved incomplete package*/
  protected static ImageIcon localUserSavedIncompleteIcon = null;
  /** The icon for representing automatically saved incomplete package*/
  protected static ImageIcon localAutoSavedIncompleteIcon = null;
  /** The icon for representing metacat storage. */
  protected static ImageIcon metacatIcon = null;
  /** the icon for blank, nothing there */
  protected static ImageIcon blankIcon = null;
  /** The icon for representing package */
  protected static ImageIcon packageIcon = null;
  /** The icon for representing pakcage and data file */
  protected static ImageIcon packageDataIcon = null;

  /** The icon for representing both local and metacat storage. */
  //private ImageIcon bothIcon = null;
  /** The icon for representing local storage with data. */
  //private ImageIcon localDataIcon = null;
  /** The icon for representing metacat storage with data. */
  //private ImageIcon metacatDataIcon = null;
  /** The icon for representing both local and metacat storage with data. */
  //private ImageIcon bothDataIcon = null;

  /** Store the index of package icon in resultsVector */
  public static final int PACKAGEICONINDEX = 0;

  /** Store the index of titl in resultsVector */
  public static final int TITLEINDEX = 1;

  /** Store the index of surname in resultsVector */
  public static final int SURNAMEINDEX = 2;

  /** Store the index of keywords in resultsVector */
  public static final int KEYWORDSINDEX = 3;

  /** Store the index of createdate in resultsVector */
  public static final int CREATEDATEINDEX = 4;

  /** Store the index of update in resultsVector */
  public static final int UPDATEDATEINDEX = 5;

  /** Store the index of docid in resultsVector */
  public static final int DOCIDINDEX = 6;

  /** Store the index of doc name in resultsVector */
  public static final int DOCNAMEINDEX = 7;

  /** Store the index of doc type in resultsVector */
  public static final int DOCTYPEINDEX = 8;

  /** Store the index of islocal in resultsVector */
  public static final int ISLOCALINDEX = 9;

  /** Store the index of ismetacat in resultsVector */
  public static final int ISMETACATINDEX = 10;

  /** Store the index of triple in resultsVector*/
  public static final int TRIPLEINDEX =11;
  
  /** Store the height fact for table row height */
  private static final int HEIGHTFACTOR = 2;

  /**
   * Construct a ResultSet instance from a vector of vectors;
   * for use with LocalQuery
   */
  public ResultSet(Query query, Vector vec, Morpho morpho) {

    initIcons();
    init(query, morpho);
    initMapping();
    this.resultsVector = vec;
  }

  /**
   * Construct a ResultSet instance given a query object and a
   * InputStream that represents an XML encoding of the results.
   */
  public ResultSet( Query query, String localStatus, String metacatStatus,
                    InputStream resultsXMLStream, Morpho morpho) {

    initIcons();
    init(query, morpho);
    initMapping();
    Log.debug(30, "(2.41) Creating result set ...");
     resultsVector = new Vector();

    // Parse the incoming XML stream and extract the data
    XMLReader parser = null;
    // Set up the SAX document handlers for parsing
    try {
      // Get an instance of the parser
      ResultsetHandler handler = new ResultsetHandler(morpho, localStatus, metacatStatus);
      parser = XMLUtilities.createSaxParser(handler, null);
      Log.debug(30, "(2.43) Creating result set ...");
      // Set the ContentHandler to this instance
      parser.parse(new InputSource(new InputStreamReader(resultsXMLStream)));
      SynchronizeVector parseResult = handler.getSynchronizeVector();
      resultsVector =parseResult.getVector();
      Log.debug(30, "(2.44) Creating result set ...");
    } catch (Exception e) {
      Log.debug(30, "(2.431) Exception creating result set ...");
      Log.debug(6, "(2.432) " + e.toString());
      Log.debug(30, "(2.433) Exception is: " + e.getClass().getName());
    }

  }


  // common initialization functionality for constructors
  private void init(Query query, Morpho morpho) {

    this.savedQuery   = query;
    this.morpho       = morpho;
    // Set up the headers
    createTableHeader();
    //int cnt = (returnFields==null)? 0 : returnFields.size();
    //int numberFixedHeaders = 1;
    //headers = new String[numberFixedHeaders+cnt];
    //headers[0] = " "; // This is for the icon column;
                      // *NOTE* we *must* use a space here, *NOT* an empty
                      // string ("") - otherwise header height is set too
                      // small in windows L&F
    //for (int i=0;i<cnt;i++) {
      //headers[1+i] = getLastPathElement((String)returnFields.elementAt(i));
    //}
  }

  //initialize icons - called from constructor
  private void initIcons() {

    localIcon
      = new ImageIcon(getClass().getResource("local-package-small.png"));
    localIcon.setDescription(ImageRenderer.LOCALTOOLTIP);
    localUserSavedIncompleteIcon
    = new ImageIcon(getClass().getResource("local-user-saved-inomplete-package-small.png"));
    localUserSavedIncompleteIcon.setDescription(ImageRenderer.LOCALUSERSAVEDINCOMPLETETOOLTIP);
    localAutoSavedIncompleteIcon
    = new ImageIcon(getClass().getResource("local-auto-saved-inomplete-package-small.png"));
    localAutoSavedIncompleteIcon.setDescription(ImageRenderer.LOCALAUTOSAVEDINCOMPLETETOOLTIP);
    metacatIcon
      = new ImageIcon(getClass().getResource("network-package-small.png"));
    metacatIcon.setDescription(ImageRenderer.METACATTOOLTIP);
    blankIcon
      = new ImageIcon(getClass().getResource("blank.gif"));
    blankIcon.setDescription(ImageRenderer.BLANK);
    packageIcon
      = new ImageIcon(getClass().getResource("metadata-only-small.png"));
    packageIcon.setDescription(ImageRenderer.PACKAGETOOLTIP);
    packageDataIcon
      = new ImageIcon(getClass().getResource("metadata+data-small.png"));
    packageDataIcon.setDescription(ImageRenderer.PACKAGEDATATOOLTIP);
    /*bothIcon
      = new ImageIcon(getClass().getResource("local+network-metadata.gif"));
    localDataIcon
      = new ImageIcon(getClass().getResource("local-metadata+data.gif"));
    metacatDataIcon
      = new ImageIcon(getClass().getResource("network-metadata+data.gif"));
    bothDataIcon
    =new ImageIcon(getClass().getResource("local+network-metadata+data.gif"));*/
  }

  /*
   * Initial mapping bewteen coloumn name and vector index
   */
  private void initMapping()
  {
    mapColumnNameAndVectorIndex.put(
                  QueryRefreshInterface.HASDATA, new Integer(PACKAGEICONINDEX));
    mapColumnNameAndVectorIndex.put(
                  QueryRefreshInterface.TITLE, new Integer(TITLEINDEX));
    mapColumnNameAndVectorIndex.put(
                  QueryRefreshInterface.DOCID, new Integer(DOCIDINDEX));
    mapColumnNameAndVectorIndex.put(
                  QueryRefreshInterface.SURNAME, new Integer(SURNAMEINDEX));
    mapColumnNameAndVectorIndex.put(
                  QueryRefreshInterface.KEYWORDS, new Integer(KEYWORDSINDEX));
    mapColumnNameAndVectorIndex.put(
               QueryRefreshInterface.LASTMODIFIED, new Integer(UPDATEDATEINDEX));
    mapColumnNameAndVectorIndex.put(
                  QueryRefreshInterface.LOCAL, new Integer(ISLOCALINDEX));
    mapColumnNameAndVectorIndex.put(
                  QueryRefreshInterface.NET, new Integer(ISMETACATINDEX));

  }

  /**
   *  get the resultsVector
   */
  public Vector getResultsVector() {
    return this.resultsVector;
  }

  /**
   *  set the resultsVector
   */
  public void setResultsVector(Vector rv) {
    this.resultsVector = rv;
  }

  /**
   * Return the number of columns in this result set
   */
  public int getColumnCount()
  {
    return headers.length;
  }

  /**
   * Return the number of records in this result set
   */
  public int getRowCount()
  {
    return resultsVector.size();
  }

  /**
   * Return the correct row height for table rows
   */
  public int getRowHeight()
  {
    if (localIcon != null)
    {
      int height = (localIcon.getIconHeight())*HEIGHTFACTOR;
      return height ;
    }
    else
    {
      return 1;
    }
  }

  /**
   * Determine the name of a column by its index
   */
  public String getColumnName(int col)
  {
    return headers[col];
  }

  /**
   * Determine the value of a column by its row and column index
   */
  public Object getValueAt(int row, int col)
  {
    Object value = null;
    return value;
  }

  /**
   * Get the morpho attribute
   * @return Morpho
   */
  public Morpho getMorpho()
  {
    return morpho;
  }

  /**
   * This method will change the clonum name of model
   * @param anotherHeader String[]
   */
  public void setHeader(String [] anotherHeader)
  {
    headers = new String[anotherHeader.length];
    for (int i= 0; i < headers.length; i++)
    {
      headers[i] = anotherHeader[i];
    }
  }

  /**
   * This method will set a mapping table
   * @param hash Hashtable
   */
  public void setMapping(Hashtable<String, Integer> hash)
  {
    mapColumnNameAndVectorIndex = hash;
  }

  /**
   * Lookup an array to find resultsVector index for header index
   *  header index              resultVector index
   *      0                       PACKAGEICONEX(0)
   *      1                       TITLEINDEX(1)
   *      2                       DOCIDINDEX(6)
   *      3                       SURNAMEINDEX(2)
   *      4                       KEYWORKDINDEX(3)
   *      5                       UPDATEDATEINDEX(5)
   *      6                       ISLOCALINDEX(9)
   *      7                       ISMETACATINDEX(10)
   */
  public int lookupResultsVectorIndex(int headerIndex)
  {
    // get the column name (head)
    String head = headers[headerIndex];
    return lookupResultsVectorIndex(head);

  }//lookupResultsVectorIndex

  public int lookupResultsVectorIndex(String headName)
 {
   int vectorIndex = -1;
   Object obj = mapColumnNameAndVectorIndex.get(headName);
   if (obj != null)
   {
      Integer value = (Integer)obj;
      vectorIndex = value.intValue();
   }
   return vectorIndex;

 }//lookupResultsVectorIndex


  /**
   * Return the Class for each column so that they can be
   * rendered correctly.
   */
  public Class getColumnClass(int c)
  {
    Class currentClass = null;
    try {
      currentClass = this.getValueAt(0, c).getClass();
    } catch (NullPointerException npe) {
      try {
        currentClass = Class.forName("java.lang.String");
      } catch (ClassNotFoundException cnfe) {
      }
    }
    return currentClass;
  }

  /**
   * Create the header for table model
   * The header include a package icon, title, documentid,  surname, keywords
   * last modified, local and net icon.
   */
  private void createTableHeader()
  {
    // DFH - using the number of returnFields to setup the table creates problems
    // (especialy with column percent size array in 'ResultPanel' class)
    // And we may want to have returnFields that are not displayed (e.g. a field
    // to indicated whether data is included with the package).
    // Thus, for now, just fix the 'cnt' variable
	//int cnt = (returnFields==null)? 0 : returnFields.size();
    int cnt = 3;
    int numberFixedHeaders = 5;
    headers = new String[numberFixedHeaders+cnt];
    headers[0] = QueryRefreshInterface.HASDATA;
    headers[1] = QueryRefreshInterface.TITLE;
    headers[2] = QueryRefreshInterface.DOCID;// This for third column header
    // SurName
    headers[3] = QueryRefreshInterface.SURNAME;
    // Keyworkds
    headers[4] = QueryRefreshInterface.KEYWORDS;
    headers[5]= QueryRefreshInterface.LASTMODIFIED;
    headers[6]= QueryRefreshInterface.LOCAL;
    headers[7]= QueryRefreshInterface.NET;
  }




  /**
   * Get the query that was used to construct these results
   */
  public Query getQuery() {
    return savedQuery;
  }

  /**
   * Set the query that was used to construct these results
   * (for use by LocalQuery)
   */
  public void setQuery(Query query) {
    this.savedQuery = query;
  }
  
  /**
   * Merge a ResultSet onto this one.
   * Merging also consolidate results.
   */
  public void mergeWithMetacatResultset(ResultSet metacatResults)
  {
    if (metacatResults != null)
    {
      mergeWithCompleteDocResultset(metacatResults, true);  
    }
  }


  /**
   * Merge a ResultSet onto this one using the docid as the join column.
   * Merging also consolidate results.
   */
   public void mergeWithLocalResults(ResultSet localResults, boolean checkRevisionHistory)
   {
     mergeWithCompleteDocResultset(localResults, checkRevisionHistory);  
   }
   
   /*
    * Merge a complete-document search result set onto this one.
    * Merging also consolidate the reuslt.
    */
   private void mergeWithCompleteDocResultset(ResultSet anotherResultSet, boolean checkRevisionHistory)
   {
     if(anotherResultSet != null)
     {
       Vector r2Rows = anotherResultSet.getResultsVector();
       mergeWithCompleteDocResultVectors(r2Rows, checkRevisionHistory);
     }
   }
 
  
  /*
   * resultsVector may contain incomplete doc information. Merge it with
   * another resultVector which only contains complete document information.
   * The completeDocResult comes from either local data folder or network.
   * This merge also consolidate the results.
   * Note: if order changed, the result may be wrong
   */
  protected void mergeWithCompleteDocResultVectors(Vector completeDocResult, boolean checkRevisionHistory)
  {
    // Create a hash of  docids for easy comparison, key is docidWithoutRev, value is DocInfo object
    Hashtable<String, DocInfo> completeDocidMap = new Hashtable<String, DocInfo>();
    Hashtable<String, DocInfo> incompleteDocidMap = new Hashtable<String, DocInfo>();
    for (int i=0; i < getRowCount(); i++) 
    {
      Vector rowVector = (Vector)resultsVector.elementAt(i);
      String currentDocid = (String)rowVector.elementAt(DOCIDINDEX);
      if (currentDocid != null)
      {
        DocInfo currentDocInfo = new DocInfo(currentDocid, i);
        String localStatus = (String)rowVector.elementAt(ISLOCALINDEX);
        if(localStatus != null && (localStatus.equals(QueryRefreshInterface.LOCALAUTOSAVEDINCOMPLETE)||
            localStatus.equals(QueryRefreshInterface.LOCALUSERSAVEDINCOMPLETE)))
        {
          incompleteDocidMap.put(currentDocid, currentDocInfo);
        }
        else
        {
          completeDocidMap.put(currentDocid, currentDocInfo);
        }
      }    
    }
    
    Enumeration ee =completeDocResult.elements();
    while (ee.hasMoreElements()) 
    {
      Vector row = (Vector)ee.nextElement();
      String currentDocid = (String)row.elementAt(DOCIDINDEX);
      String localStatus = (String)row.elementAt(ISLOCALINDEX);   

      if (currentDocid != null)
      {
        if (incompleteDocidMap.containsKey(currentDocid)) 
        {
         //merge a complete documents vector to a incomplete documents vector
          DocInfo info = incompleteDocidMap.get(currentDocid);
          String existIdentifier = info.getDocid();
          // check the local revision history for the latest version of it
          String latestIdentifier = null;
			try {
				latestIdentifier = Morpho.thisStaticInstance.getLocalDataStoreService().getRevisionManager().getLatestRevision(existIdentifier);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
          
          if (existIdentifier.equals(latestIdentifier)) {
          
            Log.debug(30, "ResultSet.mergeWithCompleteDocResult - the exist revision "+existIdentifier+
                           " of docid "+ currentDocid+" is greater than or equals the complete doc revision. " +
                           "So the complete document will be hidden on the search result");
          }
          else
          {
            //check if the docid is in complete docid map too. If it doesn, merge it.
            if (completeDocidMap.containsKey(currentDocid))
            {
              DocInfo completeInfo = completeDocidMap.get(currentDocid);
              mergeLocalAndNetworkCompleteDoc(completeInfo, currentDocid, row);
            }
            else
            {
              resultsVector.addElement(row);
            }          
          
          }
          
        } 
        else if(completeDocidMap.containsKey(currentDocid))
        {
          //merge two complete documents vector
          DocInfo info = completeDocidMap.get(currentDocid);
          mergeLocalAndNetworkCompleteDoc(info, currentDocid, row);      
        }          
        else 
        {
        	// check network revision history for local results to see if they have been obsoleted
        	String latestRevision = null;
        	if (localStatus.equals(QueryRefreshInterface.LOCAL) || localStatus.equals(QueryRefreshInterface.LOCALCOMPLETE)) {
            	if (checkRevisionHistory) {
	        		try {
						latestRevision = morpho.getDataONEDataStoreService().getRevisionManager().getLatestRevision(currentDocid);
					} catch (Exception e) {
						// this is expected much of the time
						Log.debug(30, "could not find revision history for: " + currentDocid);
					}
            	}
        	}
        	
        	// if this is the latest version or a totally new record just add it
        	if (latestRevision == null || latestRevision.equals(currentDocid)) {
                resultsVector.addElement(row);
        	}
          
        }
      }
   
    }

  }
  
  
  /*
	 * merge local and network complete doc base on the revision history
	 */
	private void mergeLocalAndNetworkCompleteDoc(DocInfo info, String newIdentifier, Vector newRow) {
		if (info != null) {
			// merge two complete documents vector
			String existingIdentifier = info.getDocid();
			
			if (existingIdentifier.equals(newIdentifier)) {
				// existRev == newRev
				int rowIndex = info.getRowNumber();
				Vector originalRow = (Vector) resultsVector.elementAt(rowIndex);
				originalRow.setElementAt(QueryRefreshInterface.LOCALCOMPLETE, ISLOCALINDEX);
				originalRow.setElementAt( QueryRefreshInterface.NETWWORKCOMPLETE, ISMETACATINDEX);
			} 
			else {
				// replace with newer row if applicable
				try {
					List<String> revisions = Morpho.thisStaticInstance.getLocalDataStoreService().getRevisionManager().getAllRevisions(existingIdentifier);
					int existRev = revisions.indexOf(existingIdentifier);
					int newRev = revisions.indexOf(newIdentifier);
					if (existRev < newRev) {
				        int rowIndex = info.getRowNumber();
				        Vector originalRow = (Vector)resultsVector.elementAt(rowIndex);
				        replaceResultRowValue(originalRow, newRow);
					} 
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			// Log.debug(5, "add directly2");
			// a totally new record, just add it.
			resultsVector.addElement(newRow);
		}
	}
  
  
  /*
   * Replace a row vector's element value
   */
  private void replaceResultRowValue(Vector original, Vector newValue)
  {
    if(original != null && newValue != null)
    {
      int size = original.size();
      original.removeAllElements();
      for(int i=0; i<size; i++)
      {
        original.add(newValue.elementAt(i));
      }
    }
  }
  
  /**
   * Get a reference to the Morpho application framework
   */
  public Morpho getFramework()
  {
    return this.morpho;
  }

  public void sortTable(Comparator comparator) { 
	  Collections.sort(resultsVector, comparator);
  }

  /**
   * Method implements from SortTableModel. To make sure a col can be sort
   * or not. We decide it always be sortable.
   * @param col, the index of column which need to be sorted
   * @param order, the sort order
   */
  public void sortTableByColumn(int col, String order)
  {

     sortVector(resultsVector, col, order);
  }//sortTableColumn

  protected void sortVector(Vector vector, int col, String order)
  {
    boolean sort = false;
    boolean ascending = false;

   // look up sort and ascending
   if (order.equals(SortableJTable.ASCENDING))
   {
     sort = true;
     ascending = true;
   }
   else if (order.equals(SortableJTable.DECENDING))
   {
     sort = true;
     ascending = false;
   }
   else if (order.equals(SortableJTable.NONORDERED))
   {
     sort = false;
   }
   // sorting
   if (sort)
   {

     //look up index in result vector
     int resultColIndex = lookupResultsVectorIndex(col);
     // sort the result vector
     Collections.sort(vector,
                   new CellComparator(resultColIndex, ascending));

   }

  }
  
  /**
   * This class represents the following info for document: docidWithoutRev, rev and row number
   * in resultset vector.
   *
   * @author tao
   *
   */
  private class DocInfo
  {
    private String docid = null;
    private int rowNumber = -1;
    
    /**
     * Constructor
     * @param docidWithoutRev
     * @param rev
     * @param rowNumber
     */
    public DocInfo(String identifier, int rowNumber)
    {
      this.docid = identifier;
      this.rowNumber = rowNumber;
    }
    
    /**
     * Gets the docid
     * @return
     */
    public String getDocid()
    {
      return this.docid;
    }
    
    /**
     * Gets row number
     * @return
     */
    public int getRowNumber()
    {
      return this.rowNumber;
    }
  }
}
