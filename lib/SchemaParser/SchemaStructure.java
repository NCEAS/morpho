
// This is a class to use the xerces Package org.apache.xerces.impl.xs.psvi 
// to obtain information from a schema about its structure



import org.apache.xerces.parsers.XMLGrammarPreparser;
import org.apache.xerces.parsers.IntegratedParserConfiguration;
import org.apache.xerces.util.ObjectFactory;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.XMLGrammarPoolImpl;
import org.apache.xerces.impl.Constants;

import org.apache.xerces.xni.grammars.XMLGrammarDescription;
import org.apache.xerces.xni.grammars.Grammar;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParserConfiguration;
import org.apache.xerces.impl.xs.psvi.*;

import java.util.Vector;
import javax.swing.tree.*;
import java.util.Hashtable;

public class SchemaStructure
{
    //
    // Constants
    //
    
    /*
     * num_levels is used to control the level of recursions 
     * expansion of the tree stops when the node is at 'num_levels' from the root
     */
    public static int num_levels = 12;
    
    // property IDs:

    /** Property identifier: symbol table. */
    public static final String SYMBOL_TABLE =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SYMBOL_TABLE_PROPERTY;

    /** Property identifier: grammar pool. */
    public static final String GRAMMAR_POOL =
        Constants.XERCES_PROPERTY_PREFIX + Constants.XMLGRAMMAR_POOL_PROPERTY;

    // feature ids

    /** Namespaces feature id (http://xml.org/sax/features/namespaces). */
    protected static final String NAMESPACES_FEATURE_ID = "http://xml.org/sax/features/namespaces";

    /** Validation feature id (http://xml.org/sax/features/validation). */
    protected static final String VALIDATION_FEATURE_ID = "http://xml.org/sax/features/validation";

    /** Schema validation feature id (http://apache.org/xml/features/validation/schema). */
    protected static final String SCHEMA_VALIDATION_FEATURE_ID = "http://apache.org/xml/features/validation/schema";

    /** Schema full checking feature id (http://apache.org/xml/features/validation/schema-full-checking). */
    protected static final String SCHEMA_FULL_CHECKING_FEATURE_ID = "http://apache.org/xml/features/validation/schema-full-checking";

    // a larg(ish) prime to use for a symbol table to be shared
    // among
    // potentially man parsers.  Start one as close to 2K (20
    // times larger than normal) and see what happens...
    public static final int BIG_PRIME = 2039;

    // default settings

    /** Default Schema full checking support (false). */
    protected static final boolean DEFAULT_SCHEMA_FULL_CHECKING = false;

    // The code for getting an XSModel is borrowed from the XMLGrammarBuilder 
    // sample code in Xerces 2.2.1

    /**  
     * Main program entry point (of original XMLGrammarBuilder code).
     * Now this is a method the builds a grammar based on the schema name
     * and then casts the grammar as a Xerces 2.2.1 XSModel
     * (see javadoc for xerces Package org.apache.xerces.impl.xs.psvi
     */
    public static XSModel getXSModel(String schemaName) {

        XSModel xsmodel = null;
        
        XMLParserConfiguration parserConfiguration = null;
        String arg = null;
        int i = 0;

        Vector schemas = null;
        boolean schemaFullChecking = DEFAULT_SCHEMA_FULL_CHECKING;
        // process -a: schema files

        schemas= new Vector();
        schemas.addElement(schemaName);

        // has to be at least one dTD or schema , and there has to be other parameters
        if (schemas.size() == 0) {
          System.out.println("No schema files!!!");
          return null;
        }
        // process -i: instance files, if any
        Vector ifiles = null;

        // now we have all our arguments.  We only
        // need to parse the DTD's/schemas, put them
        // in a grammar pool, possibly instantiate an 
        // appropriate configuration, and we're on our way.

        SymbolTable sym = new SymbolTable(BIG_PRIME);
        XMLGrammarPreparser preparser = new XMLGrammarPreparser(sym);
        XMLGrammarPoolImpl grammarPool = new XMLGrammarPoolImpl();
        boolean isDTD = false;
        if(schemas != null) {
            preparser.registerPreparser(XMLGrammarDescription.XML_SCHEMA, null);
            isDTD = false;
        } else {
            System.err.println("No schema specified!");
            System.exit(1);
        }
        preparser.setProperty(GRAMMAR_POOL, grammarPool);
        preparser.setFeature(NAMESPACES_FEATURE_ID, true);
        preparser.setFeature(VALIDATION_FEATURE_ID, true);
        // note we can set schema features just in case...
        preparser.setFeature(SCHEMA_VALIDATION_FEATURE_ID, true);
        preparser.setFeature(SCHEMA_FULL_CHECKING_FEATURE_ID, schemaFullChecking);
        // parse the grammar...

        try {
                for (i = 0; i < schemas.size(); i++) {
                    Grammar g = preparser.preparseGrammar(XMLGrammarDescription.XML_SCHEMA, stringToXIS((String)schemas.elementAt(i)));
                    // we don't really care about g; grammarPool will take care of everything.   
                    xsmodel = ((org.apache.xerces.xni.grammars.XSGrammar)g).toXSModel();
                    if (xsmodel!=null) System.out.println("XSModel has been created!!");
                    return xsmodel;
                    
                }
            
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
      return xsmodel;
    } // end getXSModel

  /**
   * produces a list of global element names as a
   * vector
   * 
   * @param xsm the XS model
   */
  static Vector getGlobalElements(XSModel xsm) {
    // vector of element names as strings
    Vector names = new Vector();
    XSNamedMap elemNames = xsm.getComponents(XSConstants.ELEMENT_DECLARATION); 
    int len = elemNames.getMapLength();
    for (int ii=0;ii<len;ii++) { 
      XSObject obj = elemNames.getItem(ii);
      names.addElement(obj.getName());
    }    
    return names;
  }
    
  /**
   * produces a list of global attribute names as a
   * vector
   * 
   * @param xsm
   */
  static Vector getGlobalAttributes(XSModel xsm) {
    // vector of attribute names as strings
    Vector names = new Vector();
    XSNamedMap attrNames = xsm.getComponents(XSConstants.ATTRIBUTE_DECLARATION); 
    int len = attrNames.getMapLength();
    for (int ii=0;ii<len;ii++) { 
      XSObject obj = attrNames.getItem(ii);
      names.addElement(obj.getName());
    }    
    return names;
  }

  /**
   * produces a list of global modal groups names as
   *  a vector
   * 
   * @param xsm
   */
  static Vector getGlobalModelGroups(XSModel xsm) {
    // vector of group names as strings
    Vector names = new Vector();
    XSNamedMap modelGroupNames = xsm.getComponents(XSConstants.MODEL_GROUP_DEFINITION); 
    int len = modelGroupNames.getMapLength();
    for (int ii=0;ii<len;ii++) { 
      XSObject obj = modelGroupNames.getItem(ii);
      names.addElement(obj.getName());
    }    
    return names;
  }
  
  /**
   * get namespaces as a Vector of strings
   * 
   * @param xsm
   */
  static Vector getNameSpaces(XSModel xsm) {
    // vector of namespace names as strings
    Vector names = new Vector();
    StringList sl = xsm.getNamespaces();
    for (int i=0;i<sl.getLength();i++) {
        names.addElement(sl.item(i));   
    }
    return names;
  }

  /**
   * get global annotations (not working)
   * 
   * @param xsm
   */
  static Vector getAnnotations(XSModel xsm) {
    // vector of annotation names as strings
    Vector names = new Vector();
    XSNamedMap annNames = xsm.getComponents(XSConstants.ANNOTATION); 
    int len = annNames.getMapLength();
    for (int ii=0;ii<len;ii++) { 
      XSObject obj = annNames.getItem(ii);
      names.addElement(obj.getName());
    }    
    return names;
  }
   
  /**
   * prints element info to System.out
   * 
   * @param xsm
   * @param iii
   */
  static void showElementInfo(XSModel xsm, int iii) {
    // iii is the index of the elementDecl
    XSNamedMap elemNames = xsm.getComponents(XSConstants.ELEMENT_DECLARATION); 
    int len = elemNames.getMapLength();
    XSElementDeclaration test = (XSElementDeclaration)elemNames.getItem(iii);   
    XSTypeDefinition testtd = test.getTypeDefinition();
    if (testtd.getTypeCategory()==XSTypeDefinition.COMPLEX_TYPE) {
      System.out.println("Complex type!");
       XSComplexTypeDefinition comp = (XSComplexTypeDefinition)testtd;    
       XSParticle part = comp.getParticle(); 
       System.out.println("Minimum occurances: "+part.getMinOccurs());
       if (part.getIsMaxOccursUnbounded()) {
         System.out.println("unlimited number of occurances");
       }
       else {
         System.out.println("Maximum occurances: "+part.getMaxOccurs());
       }
       XSTerm term = part.getTerm(); 
       XSModelGroup mg = (XSModelGroup)term;
       if (mg.getCompositor()==XSModelGroup.COMPOSITOR_SEQUENCE) {
         System.out.println("SEQUENCE");
       }
       else if (mg.getCompositor()==XSModelGroup.COMPOSITOR_CHOICE) {
         System.out.println("CHOICE");
       }
       else System.out.println("term:unknown");
       XSObjectList list = mg.getParticles();
       for (int k=0;k<list.getLength();k++) {
         getParticleInfo((XSParticle)list.getItem(k)); 
       }
    }
    if (testtd.getTypeCategory()==XSTypeDefinition.SIMPLE_TYPE) {
      System.out.println("Simple type!");
    }
  }
  
    /**
     * prints particle info to System.out
     * 
     * @param part
     */
    static void getParticleInfo(XSParticle part) {
        if (part.getIsMaxOccursUnbounded()) {
            System.out.println("particle MaxOccurs unbounded");
        }
        else {
            int maxocc = part.getMaxOccurs();
            System.out.println("particle MaxOccurs: "+maxocc);
        }
        int minocc = part.getMinOccurs();
        System.out.println("particle MinOccurs: "+minocc);
        XSTerm xst = part.getTerm();
        // a term can be an elementDeclaration, a modelGroup, or a wildcard (I think)
        if (xst.getType()==XSConstants.ELEMENT_DECLARATION) {
            System.out.println("Particle is an Element Declaration");
            System.out.println("Element Name: "+((XSElementDeclaration)xst).getName());
        }
        else if (xst.getType()==XSConstants.MODEL_GROUP) {
            System.out.println("Particle is an Model Group"); 
            XSModelGroup mg = (XSModelGroup)xst;
            getModelGroupInfo(mg);
        }
        else if (xst.getType()==XSConstants.WILDCARD) {
            System.out.println("Particle is an Wildcard");            
        }
    }
 
    /**
     * prints ModelGroup information to System.out
     * 
     * @param mg XSModelGroup
     */
    static void getModelGroupInfo(XSModelGroup mg) {
       if (mg.getCompositor()==XSModelGroup.COMPOSITOR_SEQUENCE) {
         System.out.println("SEQUENCE");
       }
       else if (mg.getCompositor()==XSModelGroup.COMPOSITOR_CHOICE) {
         System.out.println("CHOICE");
       }
       else System.out.println("term:unknown");
       XSObjectList list = mg.getParticles();
       for (int k=0;k<list.getLength();k++) {
         getParticleInfo((XSParticle)list.getItem(k)); 
       }
    }

  /**
   * this method starts with an XSElement object and recursively 
   * finds its logical children, linking them in a 
   * DefaultMutableTreeNode tree, starting with the input node
   * 
   * @param xsm
   * @param name
   */
  static DefaultMutableTreeNode getElementInfoFromName(XSModel xsm, String name) {
    DefaultMutableTreeNode node = null;
    XSNamedMap elemNames = xsm.getComponents(XSConstants.ELEMENT_DECLARATION); 
    int len = elemNames.getMapLength();
    for (int ii=0;ii<len;ii++) { 
      XSObject obj = elemNames.getItem(ii);
      if (name.equals(obj.getName())) {
        node = getElementInfo((XSElementDeclaration)obj, null);
      }
    }    
    return node;
  }
    
    
  /**
   * This method examines an XSElementDeclaration object and builds
   * a tree of its children (as defined in complex type descriptions).
   * 
   * @param test the XSElementDeclaration starting point
   * @param nd a DefaultMutableTreeNode node associated with the starting element (or null)
   * @return a DefaultMutableTreeNode which should contain info about the 
   * children of the original element
   */
  static DefaultMutableTreeNode getElementInfo(XSElementDeclaration test, DefaultMutableTreeNode nd) {
    DefaultMutableTreeNode child = null;
    DefaultMutableTreeNode node = null;
    if (nd==null) {
      NodeInfo nodeni = new NodeInfo(test.getName());
      node = new DefaultMutableTreeNode(nodeni);
    }
    else {
      node = nd; 
    }
    XSTypeDefinition testtd = test.getTypeDefinition();
    if (testtd.getTypeCategory()==XSTypeDefinition.COMPLEX_TYPE) {
       XSComplexTypeDefinition comp = (XSComplexTypeDefinition)testtd;   
       if (comp==null) System.out.println("complextype defn is null!");
       Hashtable attr_hash = getAttributes(comp);
       if (!attr_hash.isEmpty()) {
         ((NodeInfo)(node.getUserObject())).setAttributes(attr_hash);
       }
       XSParticle part = comp.getParticle(); 
       if (part==null) {
         System.out.println("particle defn is null!");
       }
       else {
         String maxocc = "";
         String minocc = "";
         minocc = (new Integer(part.getMinOccurs())).toString();
         if (part.getIsMaxOccursUnbounded()) {
           maxocc = "unbounded";
         }
         else {
           maxocc = (new Integer(part.getMaxOccurs())).toString();
         }
         XSTerm term = part.getTerm(); 
         XSModelGroup mg = (XSModelGroup)term;
         if (mg.getCompositor()==XSModelGroup.COMPOSITOR_SEQUENCE) {
           NodeInfo seqni = new NodeInfo("SEQUENCE");
           seqni.setMinOcc(minocc);
           seqni.setMaxOcc(maxocc);
           child = new DefaultMutableTreeNode(seqni);
           node.add(child);
         }
         else if (mg.getCompositor()==XSModelGroup.COMPOSITOR_CHOICE) {
           NodeInfo choiceni = new NodeInfo("CHOICE");
           choiceni.setMinOcc(minocc);
           choiceni.setMaxOcc(maxocc);
           child = new DefaultMutableTreeNode(choiceni);
           node.add(child);
         }
         else System.out.println("term:unknown");
         XSObjectList list = mg.getParticles();
         for (int k=0;k<list.getLength();k++) {
           getParticleInfo((XSParticle)list.getItem(k), child); 
         }
      }
      if (testtd.getTypeCategory()==XSTypeDefinition.SIMPLE_TYPE) {
        System.out.println("Simple type!");
      }
    }
    return node;
  }
  
    /**
     * part of several recusive routines which get particle information.
     * 
     * @param part XSParticle
     * @param cnode DefaultMutableTreeNode
     */
    static void getParticleInfo(XSParticle part, DefaultMutableTreeNode cnode) {
        String maxocc = "";
        String minocc = "";
        if (part.getIsMaxOccursUnbounded()) {
            maxocc = "unbounded";
        }
        else {
            int maxocc1 = part.getMaxOccurs();
            maxocc = (new Integer(part.getMaxOccurs())).toString();
        }
        int minocc1 = part.getMinOccurs();
        minocc = (new Integer(part.getMinOccurs())).toString();
        XSTerm xst = part.getTerm();
        // a term can be an elementDeclaration, a modelGroup, or a wildcard (I think)
        if (xst.getType()==XSConstants.ELEMENT_DECLARATION) {
            NodeInfo gcnodeni = new NodeInfo(((XSElementDeclaration)xst).getName());
            gcnodeni.setMinOcc(minocc);
            gcnodeni.setMaxOcc(maxocc);
            DefaultMutableTreeNode gcnode = new DefaultMutableTreeNode(gcnodeni);
            cnode.add(gcnode);
            
            if (gcnode.getLevel()<num_levels) {
              DefaultMutableTreeNode temp = getElementInfo((XSElementDeclaration)xst, gcnode);
            }
            
            
        }
        else if (xst.getType()==XSConstants.MODEL_GROUP) {
            XSModelGroup mg = (XSModelGroup)xst;
            getModelGroupInfo(mg, cnode, minocc, maxocc);
        }
        else if (xst.getType()==XSConstants.WILDCARD) {
            // an element wildcard is just an 'ANY' element
//            System.out.println("Particle is an Wildcard"); 
            NodeInfo wcni = new NodeInfo("ANY");
            wcni.setMinOcc(minocc);
            wcni.setMaxOcc(maxocc);
            DefaultMutableTreeNode wcnode = new DefaultMutableTreeNode(wcni);
            cnode.add(wcnode);
        }
    }
 
    /**
     * part of several recusive routines which get 
     * ModelGroup information.
     * 
     * @param mg XSModelGroup
     * @param cnode
     * @param minocc
     * @param maxocc
     */
    static void getModelGroupInfo(XSModelGroup mg, DefaultMutableTreeNode cnode, String minocc, String maxocc) {
        DefaultMutableTreeNode child = null;
        NodeInfo childni = null;
       if (mg.getCompositor()==XSModelGroup.COMPOSITOR_SEQUENCE) {
         childni = new NodeInfo("SEQUENCE");
         childni.setMinOcc(minocc);
         childni.setMaxOcc(maxocc);
         child = new DefaultMutableTreeNode(childni);
         cnode.add(child);
       }
       else if (mg.getCompositor()==XSModelGroup.COMPOSITOR_CHOICE) {
         childni = new NodeInfo("CHOICE");
         childni.setMinOcc(minocc);
         childni.setMaxOcc(maxocc);
         child = new DefaultMutableTreeNode(childni);
         cnode.add(child);
       }
       else System.out.println("term:unknown");
       XSObjectList list = mg.getParticles();
       for (int k=0;k<list.getLength();k++) {
         getParticleInfo((XSParticle)list.getItem(k), child); 
       }
    }
    
    /**
     * gets the Attribute information from a ComplexType
     * 
     * @param compl XSComplexType
     */
    static Hashtable getAttributes(XSComplexTypeDefinition compl) {
      Hashtable attr_hash = new Hashtable();
      XSObjectList attlist = compl.getAttributeUses();
      for (int i=0; i<attlist.getLength();i++) {
        XSAttributeUse au = (XSAttributeUse)(attlist.getItem(i)); 
        if (au==null) System.out.println("au is null!");
        String name = (au.getAttrDeclaration()).getName();
        if (name==null) System.out.println("Attribute name is null!");
        String val = au.getConstraintValue();
        if (val==null) val = "";
        attr_hash.put(name,val);
      }
      return attr_hash;
    }
    
    /**
     * gets the current setting for number of recursion levels
     * in the tree
     */
    static public int getNumLevels() {
        return num_levels;
    }
    /**
     * sets the current setting for number of recursion levels
     * in the tree
     * 
     * @param nlev the number of levels to recurse
     */
    
    static public void setNumLevels(int nlev) {
        num_levels = nlev;
    }
    
    
    //
    // Private static methods
    //


    private static XMLInputSource stringToXIS(String uri) {
        return new XMLInputSource(null, uri, null);
    }  


    
    
}