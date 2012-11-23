/**
 *  '$RCSfile: Query.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: sgarg $'
 *     '$Date: 2005-05-18 22:21:33 $'
 * '$Revision: 1.24 $'
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Stack;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datastore.DataStoreServiceController;
import edu.ucsb.nceas.morpho.datastore.DataStoreServiceInterface;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.QueryRefreshInterface;
import edu.ucsb.nceas.morpho.framework.SwingWorker;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;
import edu.ucsb.nceas.morpho.util.StateChangeMonitor;
import edu.ucsb.nceas.utilities.XMLUtilities;

/**
 * A Class that represents a structured query, and can be
 * constructed from an XML serialization conforming to @see pathquery.dtd.
 * The printSQL() method can be used to print a SQL serialization of the query.
 */
public class Query extends DefaultHandler {

  /** flag determining whether extended query terms are present */
  private boolean containsExtendedSQL=false;
  /** Identifier for this query document */
  private String meta_file_id;
  /** Title of this query */
  private String queryTitle;
  /** List of document types to be returned using package back tracing */
  private Vector<String> returnDocList;
  /** List of document types to be searched */
  private Vector<String> filterDocList;
  /** List of fields to be returned in result set */
  private Vector<String> returnFieldList;
  /** List of users owning documents to be searched */
  private Vector<String> ownerList;
  /** List of sites/scopes used to constrain search */
  private Vector<String> siteList;
  /** The root query group that contains the recursive query constraints */
  private QueryGroup rootQG = null;

  // Query data structures used temporarily during XML parsing
  private Stack<BasicNode> elementStack;
  private Stack<QueryGroup> queryStack;
  private String currentValue;
  private String currentPathexpr;

  /** A reference to the Morpho application */
  private Morpho morpho = null;

  /** Flag, true if network searches are performed for this query */
  private boolean searchNetwork = true;

  /** Flag, true if network searches are performed for this query */
  private boolean searchLocal = true;

  /**
   * construct an instance of the Query class from an XML Stream
   *
   * @param queryspec the XML representation of the query (should conform
   *                  to pathquery.dtd) as a Reader
   * @param morpho the Morpho application in which this Query is run
   */
  public Query(Reader queryspec, Morpho morpho)
  {
    this(morpho);

    // Initialize temporary variables
    elementStack = new Stack<BasicNode>();
    queryStack   = new Stack<QueryGroup>();

    // Initialize the parser and read the queryspec
    XMLReader parser = XMLUtilities.createSaxParser(this, this);

    if (parser == null) {
      Log.debug(1, "SAX parser not instantiated properly.");
    }
    try {
      //parser.parse(new InputSource(new StringReader(queryString.trim())));
      parser.parse(new InputSource(queryspec));
    } catch (IOException ioe) {
      Log.debug(4, "Error reading the query during parsing.");
    } catch (SAXException e) {
      Log.debug(4, "Error parsing Query (" + e.getClass().getName() +").");
      Log.debug(4, e.getMessage());
    }
  }

  /**
   * construct an instance of the Query class from an XML String
   *
   * @param queryspec the XML representation of the query (should conform
   *                  to pathquery.dtd) as a String
   * @param morpho the Morpho application which this Query is run
   */
  public Query( String queryspec, Morpho morpho)
  {
    this(new StringReader(queryspec), morpho);
  }

  /**
   * construct an instance of the Query class, manually setting the Query
   * constraints rather that readin from an XML stream
   *
   * @param morpho the Morpho application in which this Query is run
   */
  public Query(Morpho morpho)
  {
    // Initialize the members
    returnDocList = new Vector<String>();
    filterDocList = new Vector<String>();
    returnFieldList = new Vector<String>();
    ownerList = new Vector<String>();
    siteList = new Vector<String>();
    this.morpho = morpho;

    loadConfigurationParameters();
  }

  /**
   * Returns true if the parsed query contains and extended xml query
   * (i.e. there is at least one &lt;returnfield&gt; in the pathquery document)
   */
  public boolean containsExtendedSQL()
  {
    if(containsExtendedSQL)
    {
      return true;
    }
    else
    {
      return false;
    }
  }

  /**
   * Accessor method to return the identifier of this Query
   */
  public String getIdentifier()
  {
    return meta_file_id;
  }

  /**
   * method to set the identifier of this query
   */
  public void setIdentifier(String id) {
    this.meta_file_id = id;
  }

  /**
   * Accessor method to return the title of this Query
   */
  public String getQueryTitle()
  {
    return queryTitle;
  }

  /**
   * method to set the title of this query
   */
  public void setQueryTitle(String title)
  {
    this.queryTitle = title;
  }

  /**
   * Accessor method to return a vector of the return document types as
   * defined in the &lt;returndoctype&gt; tag in the pathquery dtd.
   */
  public Vector<String> getReturnDocList()
  {
    return this.returnDocList;
  }

  /**
   * method to set the list of return docs of this query
   */
  public void setReturnDocList(Vector<String> returnDocList)
  {
    this.returnDocList = returnDocList;
  }

  /**
   * Accessor method to return a vector of the filter doc types as
   * defined in the &lt;filterdoctype&gt; tag in the pathquery dtd.
   */
  public Vector<String> getFilterDocList()
  {
    return this.filterDocList;
  }

  /**
   * method to set the list of filter docs of this query
   */
  public void setFilterDocList(Vector<String> filterDocList)
  {
    this.filterDocList = filterDocList;
  }

  /**
   * Accessor method to return a vector of the extended return fields as
   * defined in the &lt;returnfield&gt; tag in the pathquery dtd.
   */
  public Vector<String> getReturnFieldList()
  {
    return this.returnFieldList;
  }

  /**
   * method to set the list of fields to be returned by this query
   */
  public void setReturnFieldList(Vector<String> returnFieldList)
  {
    this.returnFieldList = returnFieldList;
  }

  /**
   * Accessor method to return a vector of the owner fields as
   * defined in the &lt;owner&gt; tag in the pathquery dtd.
   */
  public Vector<String> getOwnerList()
  {
    return this.ownerList;
  }

  /**
   * method to set the list of owners used to constrain this query
   */
  public void setOwnerList(Vector<String> ownerList)
  {
    this.ownerList = ownerList;
  }

  /**
   * Accessor method to return a vector of the site fields as
   * defined in the &lt;site&gt; tag in the pathquery dtd.
   */
  public Vector<String> getSiteList()
  {
    return this.siteList;
  }

  /**
   * method to set the list of sites used to constrain this query
   */
  public void setSiteList(Vector<String> siteList)
  {
    this.siteList = siteList;
  }

  /**
   * determine if we should search network
   */
  public boolean getSearchNetwork()
  {
    return searchNetwork;
  }

  /**
   * method to set searchNetwork
   */
  public void setSearchNetwork(boolean searchNetwork)
  {
    this.searchNetwork = searchNetwork;
  }

  /**
   * determine if we should search locally
   */
  public boolean getSearchLocal()
  {
    return searchLocal;
  }

  /**
   * method to set searchLocal
   */
  public void setSearchLocal(boolean searchLocal) {
    this.searchLocal = searchLocal;
  }

  /**
   * get the QueryGroup used to express query constraints
   */
  public QueryGroup getQueryGroup()
  {
    return rootQG;
  }

  /**
   * set the QueryGroup used to express query constraints
   */
  public void setQueryGroup(QueryGroup qg)
  {
    this.rootQG = qg;
  }

  /**
   *  get the morpho from query
   * @return Morpho
   */
  public Morpho getMorpho()
  {
    return morpho;
  }

  /**
   * callback method used by the SAX Parser when the start tag of an
   * element is detected. Used in this context to parse and store
   * the query information in class variables.
   */
  public void startElement (String uri, String localName,
                            String qName, Attributes atts)
         throws SAXException {
    BasicNode currentNode = new BasicNode(localName);
    // add attributes to BasicNode here
    if (atts != null) {
      int len = atts.getLength();
      for (int i = 0; i < len; i++) {
        currentNode.setAttribute(atts.getLocalName(i), atts.getValue(i));
      }
    }

    elementStack.push(currentNode);
    if (currentNode.getTagName().equals("querygroup")) {
      QueryGroup currentGroup = new QueryGroup(
                                currentNode.getAttribute("operator"));
      if (rootQG == null) {
        Log.debug(30, "Created root query group.");
        rootQG = currentGroup;
      } else {
        QueryGroup parentGroup = queryStack.peek();
        parentGroup.addChild(currentGroup);
      }
      queryStack.push(currentGroup);
    }
  }

  /**
   * callback method used by the SAX Parser when the end tag of an
   * element is detected. Used in this context to parse and store
   * the query information in class variables.
   */
  public void endElement (String uri, String localName,
                          String qName) throws SAXException {
    BasicNode leaving = elementStack.pop();
    if (leaving.getTagName().equals("queryterm")) {
      boolean isCaseSensitive = (new Boolean(
              leaving.getAttribute("casesensitive"))).booleanValue();
      QueryTerm currentTerm = null;
      if (currentPathexpr == null) {
        currentTerm = new QueryTerm(isCaseSensitive,
                      leaving.getAttribute("searchmode"),currentValue);
      } else {
        currentTerm = new QueryTerm(isCaseSensitive,
                      leaving.getAttribute("searchmode"),currentValue,
                      currentPathexpr);
      }
      QueryGroup currentGroup = queryStack.peek();
      currentGroup.addChild(currentTerm);
      currentValue = null;
      currentPathexpr = null;
    } else if (leaving.getTagName().equals("querygroup")) {
      QueryGroup leavingGroup = queryStack.pop();
    }
  }

  /**
   * callback method used by the SAX Parser when the text sequences of an
   * xml stream are detected. Used in this context to parse and store
   * the query information in class variables.
   */
  public void characters(char ch[], int start, int length) {

    String inputString = new String(ch, start, length);
    BasicNode currentNode = elementStack.peek();
    String currentTag = currentNode.getTagName();
    if (currentTag.equals("meta_file_id")) {
      meta_file_id = inputString;
    } else if (currentTag.equals("local_search")) {
      if(inputString.equals("true")){
        this.searchLocal = true;
      } else {
        this.searchLocal = false;
      }
    } else if (currentTag.equals("network_search")) {
      if(inputString.equals("true")){
        this.searchNetwork = true;
      } else {
        this.searchNetwork = false;
      }
    } else if (currentTag.equals("querytitle")) {
    	// the whole string was not included in one call - collecting the complete value 
    	if (queryTitle == null) {
    	      queryTitle = inputString;
    	} else {
    		queryTitle += inputString;
    	}
    } else if (currentTag.equals("value")) {
      currentValue = inputString;
    } else if (currentTag.equals("pathexpr")) {
      currentPathexpr = inputString;
    } else if (currentTag.equals("returndoctype")) {
      returnDocList.addElement(inputString);
    } else if (currentTag.equals("filterdoctype")) {
      filterDocList.addElement(inputString);
    } else if (currentTag.equals("returnfield")) {
      returnFieldList.addElement(inputString);
      containsExtendedSQL = true;
    } else if (currentTag.equals("owner")) {
      ownerList.addElement(inputString);
    } else if (currentTag.equals("site")) {
      siteList.addElement(inputString);
    }
  }

  /**
   * create a XML  serialization of the query that this instance represents
   */
  public String toXml() {
    StringBuffer self = new StringBuffer();

    self.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    self.append("<pathquery version=\"1.2\">\n");

    // The identifier
    if (meta_file_id != null) {
      self.append("  <meta_file_id>"+meta_file_id+"</meta_file_id>\n");
    }

    // Local Search
    self.append("  <local_search>"+this.searchLocal+"</local_search>\n");

    // Metacat Search
    self.append("  <network_search>"+this.searchNetwork+"</network_search>\n");

    // The query title
    if (queryTitle != null) {
      self.append("  <querytitle>"+queryTitle+"</querytitle>\n");
    }

    // Add XML for the return doctype list
    if (!returnDocList.isEmpty()) {
      Enumeration<String> en = returnDocList.elements();
      while (en.hasMoreElements()) {
        String currentDoctype = en.nextElement();
        self.append("  <returndoctype>"+currentDoctype+"</returndoctype>\n");
      }
    }

    // Add XML for the filter doctype list
    if (!filterDocList.isEmpty()) {
      Enumeration<String> en = filterDocList.elements();
      while (en.hasMoreElements()) {
        String currentDoctype = en.nextElement();
        self.append("  <filterdoctype>"+currentDoctype+"</filterdoctype>\n");
      }
    }

    // Add XML for the return field list
    if (!returnFieldList.isEmpty()) {
      Enumeration<String> en = returnFieldList.elements();
      while (en.hasMoreElements()) {
        String current = en.nextElement();
        self.append("  <returnfield>"+current+"</returnfield>\n");
      }
    }

    // Add XML for the owner list
    if (!ownerList.isEmpty()) {
      Enumeration<String> en = ownerList.elements();
      while (en.hasMoreElements()) {
        String current = en.nextElement();
        self.append("  <owner>"+current+"</owner>\n");
      }
    }

    // Add XML for the site list
    if (!siteList.isEmpty()) {
      Enumeration<String> en = siteList.elements();
      while (en.hasMoreElements()) {
        String current = en.nextElement();
        self.append("  <site>"+current+"</site>\n");
      }
    }

    // Print the QueryGroup XML
    self.append(rootQG.toXml(2));

    // End the query
    self.append("</pathquery>\n");

    return self.toString();
  }


  /**
   * create a String description of the query that this instance represents.
   * This should become a way to get the XML serialization of the query.
   */
  public String toString() {
    return toXml();
  }
  
  /**
	 * Run the query against the local data store and metacat, depending on how
	 * the searchNetwork and searchLocal flags are set. If both local and
	 * metacat searches are run, merge the results into a single ResultSet and
	 * return it.
	 * 
	 * @returns ResultSet the results of the query(s)
	 */
	public ResultSet execute() {
		ResultSet results = null;

		// TODO: Run these queries in parallel threads

		Log.debug(30, "(1) Executing result set...");
		// if appropriate, query metacat
		ResultSet metacatResults = null;
		if (searchNetwork) {
			Log.debug(30, "(2) Executing metacat query...");
			InputStream queryResults = null;
			try {
				queryResults = DataStoreServiceController.getInstance().query(toXml(), DataPackageInterface.NETWORK);
			} catch (Exception e) {
				Log.debug(5, "Error querying network: " + e.getMessage());
				e.printStackTrace();
			}
			metacatResults = new HeadResultSet(this,
					DataStoreServiceInterface.NONEXIST,
					QueryRefreshInterface.NETWWORKCOMPLETE, queryResults,
					morpho);

		}

		Log.debug(30, "(2.5) Executing result set...");
		// if appropriate, query locally
		ResultSet localResults = null;
		if (searchLocal) {
			Log.debug(30, "(3) Executing local query...");
			LocalQuery lq = new LocalQuery(this, morpho);
			localResults = lq.execute();
		}

		// merge the results if needed, and return the right result set
		if (!searchLocal) {
			results = metacatResults;
		} else if (!searchNetwork) {
			results = localResults;
		} else {
			// must merge results
			// metacatResults.merge(localResults);
			// results = metacatResults;
			localResults.mergeWithMetacatResultset(metacatResults);
			results = localResults;
		}
		// return the merged results
		return results;
	}
 

  /**
 * This method will run the query and display the query results streamly in
 * a given panel
 * @param frame MorphoFrame   the frame which the rsult pane will be display
 * @param resultDisplayPanel  the result panel
 * @param sort                table need sort or not
 * @param sortIndex           column index to sort
 * @param sortOrder           acceend or decend
 * @param stateEvent          event of state change
 */
 public void displaySearchResult(final MorphoFrame resultWindow,
                                 final ResultPanel resultDisplayPanel,
                                 final boolean sort, final int sortIndex,
                                 final String sortOder,
                                 final boolean showSearchNumber,
                                 final StateChangeEvent stateEvent)

 {

   final SwingWorker worker = new SwingWorker()
   {
        public Object construct()
       {
         resultWindow.setBusy(true);
         // disable mouse listener
         resultDisplayPanel.setEnableMouseListener(false);
         if (!searchLocal)
         {
            Log.debug(30, "(3) Executing metacat query...");
            // since it is network search, so the local result is null
            HeadResultSet localResult = null;
            doMetacatSearchDisplay(resultDisplayPanel, morpho, localResult);

         }
         else if (!searchNetwork)
         {
           Log.debug(30, "(2) Executing local query...");
           doLocalSearchDisplay(resultDisplayPanel, morpho);

         }//else if
         else
         {
           Log.debug(30, "(2) Executing both local and metacat query...");
           // search local first
            HeadResultSet localResult = doLocalSearchDisplay(resultDisplayPanel, morpho);
            doMetacatSearchDisplay(resultDisplayPanel, morpho, localResult);

         }//else
         if (sort)
         {
           resultDisplayPanel.sortTable(sortIndex, sortOder);
         }
         if (stateEvent != null)
         {
           StateChangeMonitor.getInstance().notifyStateChange(stateEvent);
         }
         return null;
       }

       //Runs on the event-dispatching thread.
       public void finished()
       {
         if (showSearchNumber)
         {
           resultWindow.setMessage(resultDisplayPanel.getResultSet().getRowCount() + " data sets found");
         }
         //enable mouse listener in result panel
         resultDisplayPanel.setEnableMouseListener(true);
         resultWindow.setBusy(false);

       }
   };
   worker.start();  //required for SwingWorker 3

 }//excute

	/*
	 * Method to display the metacat search result
	 */
	private void doMetacatSearchDisplay(final ResultPanel resultDisplayPanel,
			final Morpho morpho, final HeadResultSet localResult) {

		SynchronizeVector dataVector = new SynchronizeVector();
		// parsing result set
		String localStatus = DataStoreServiceInterface.NONEXIST;
		String metacatStatus = QueryRefreshInterface.NETWWORKCOMPLETE;
		InputStream queryResults = null;
		try {
			queryResults = DataStoreServiceController.getInstance().query(toXml(), DataPackageInterface.NETWORK);
		} catch (Exception e) {
			Log.debug(5, "Error querying network: " + e.getMessage());
			e.printStackTrace();
		}
		ResultsetHandler handler = new ResultsetHandler(queryResults, dataVector, morpho, localStatus, metacatStatus);
		// start another thread for parser
		Thread parserThread = new Thread(handler);
		parserThread.start();

		Vector allResults = new Vector();
		// if the parsing is not finished, get the synchronzied vector
		// length of total resultset
		int length = 0;
		while (!handler.isDone()) {
			Vector partResult = dataVector.getVector();
			// add partReulst inot all Result
			for (int i = 0; i < partResult.size(); i++) {
				allResults.add(partResult.elementAt(i));
			}
			if (allResults.size() > length) {
				resultDisplayPanel.resetResultsVector(allResults);
				length = allResults.size();
			}

		}// while

		// merge the allResult into local result if it is not null
		if (localResult != null) {
			localResult.mergeWithMetacatResults(allResults);
			// transfer the mergered vector to reslult panel
			resultDisplayPanel.resetResultsVector(localResult.getResultsVector());
		}

	}

 /*
  * Method to display local search result. This include incomplete documents list too.
  */
 private HeadResultSet doLocalSearchDisplay(final ResultPanel resultDisplayPanel,
                                   final Morpho morpho)
 {
    HeadResultSet incompleteLocalResults = doLocalSearchIncompleteDocDisplay(resultDisplayPanel, morpho);
    final Query query = this;
    LocalQuery lq = new LocalQuery(query, morpho);
    HeadResultSet localResults = (HeadResultSet)lq.execute();
    if(incompleteLocalResults != null)
    {
      incompleteLocalResults.mergeWithLocalResults(localResults);
    }
    else
    {
      incompleteLocalResults = localResults;
    }
    resultDisplayPanel.setResultSet(incompleteLocalResults);
    return incompleteLocalResults;
 }
 
 /*
  * Method to display local search incomplete result
  */
 private HeadResultSet doLocalSearchIncompleteDocDisplay(final ResultPanel resultDisplayPanel,
                                   final Morpho morpho)
 {
    final Query query = this;
    LocalQuery lq = new LocalQuery(query, morpho);
    HeadResultSet localResults = (HeadResultSet)lq.executeInInCompleteDoc();
    resultDisplayPanel.setResultSet(localResults);
    return localResults;
 }

  /**
   * Load the configuration parameters that we need
   */
  private void loadConfigurationParameters()
  {
    ConfigXML profile = morpho.getProfile();
    String searchNetworkString = profile.get("searchnetwork", 0);
    searchNetwork = (new Boolean(searchNetworkString)).booleanValue();
    String searchLocalString = profile.get("searchlocal", 0);
    searchLocal = (new Boolean(searchLocalString)).booleanValue();
  }

  /** Main routine for testing */
  static public void main(String[] args)
  {
     if (args.length < 1) {
       Log.debug(1, "Wrong number of arguments!!!");
       Log.debug(1, "USAGE: java Query [-noindex] <xmlfile>");
       return;
     } else {
       int i = 0;
       boolean useXMLIndex = true;
       if ( args[i].equals( "-noindex" ) ) {
         useXMLIndex = false;
         i++;
       }
       String xmlfile  = args[i];

       try {
         Morpho morpho = new Morpho(new ConfigXML("lib/config.xml"));
         Reader xml = new InputStreamReader(new FileInputStream(xmlfile), Charset.forName("UTF-8"));

         Query qspec = new Query(xml, morpho);
         Log.debug(9, qspec.toXml());
/*
         InputStreamReader returnStream =
                       new InputStreamReader( qspec.queryMetacat());

         int len = 0;
         char[] characters = new char[512];
         while ((len = returnStream.read(characters, 0, 512)) != -1) {
           System.out.print(characters);
         }
*/
/*
         ResultSet results = qspec.execute();

         JFrame frame = new JFrame("SimpleTest");
         frame.setSize(new Dimension(700, 200));
         frame.addWindowListener(new WindowAdapter()
         {
           public void windowClosing(WindowEvent e)
           {
             System.exit(0);}
           }
         );
         ResultPanel resultsPanel = new ResultPanel(results);
         frame.getContentPane().add(resultsPanel);
         frame.pack();
         resultsPanel.invalidate();
         frame.setVisible(true);
*/
       } catch (IOException e) {
         Log.debug(4, e.getMessage());
       }
     }
  }
}
