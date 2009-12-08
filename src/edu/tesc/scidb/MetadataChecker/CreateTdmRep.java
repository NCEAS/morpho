package edu.tesc.scidb.MetadataChecker;

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Iterator;
import java.util.Vector;

import edu.tesc.scidb.databank.sms.XMLObject;
import edu.tesc.scidb.databank.sms.tdm.Col;
import edu.tesc.scidb.databank.sms.tdm.ColImpl;
import edu.tesc.scidb.databank.sms.tdm.Database;
import edu.tesc.scidb.databank.sms.tdm.DatabaseImpl;
import edu.tesc.scidb.databank.sms.tdm.Table;
import edu.tesc.scidb.databank.sms.tdm.TableImpl;


/*
    Q: Where are the various properties that cause "constraint"
       elements to be generated in the resulting EML set?
       
    A: 1) "Not null" constraints are set in `addColumns()'
       2) Primary- and foreign-key-related stuff is done in,
          `addPrimaryKeys()', which is invoked from `addTables()'.
*/

/*
 	COLUMN PROPERTIES NOT SET HERE:
 		* autoIncrement		(PROPNAME_AUTOINCREMENT)
*/

public class CreateTdmRep {
    public static Database getRep(File mdbFile) {
        Connection       dbConn   = null;
        DatabaseMetaData metadata = null;
        
        // connect to the Access database
        try {
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
			String dbinfo = "jdbc:odbc:Driver={Microsoft Access Driver " +
							"(*.mdb)};DBQ=" + mdbFile.getAbsolutePath();
			dbConn = DriverManager.getConnection(dbinfo);
		}
		catch (SQLException e) {
            return null;
		} 
        catch (ClassNotFoundException e) {
            return null;
		}
        
        // grab a handle to a metadata object for this db
        try {
            metadata = dbConn.getMetaData();
        }
        catch (SQLException e) {
            return null;
        }
        
        // we'll provide the filename of this db as the db name to TDM
        StringBuffer filename = new StringBuffer(mdbFile.getName());
        
        int dotPos = filename.lastIndexOf(".");
        if(dotPos != -1)
            filename.delete(dotPos, filename.length() - 1);
        
        // begin putting together the TDM representation of this db
        Database db = new DatabaseImpl(filename.toString());
        addTables(db, metadata, mdbFile);
        
        return db;
    }
    
    private static void addTables(Database db, DatabaseMetaData metadata, File mdbFile) {
        ResultSet rs     = null;
        Vector    tables = new Vector();
        
        // no "system tables", "temporary tables", etc
        final String[] tableTypesAllowed = {"TABLE"};
        
        try {
            rs = metadata.getTables(null, null, "%", tableTypesAllowed);
            
            while(rs.next())
                tables.add(new TableImpl(rs.getString("TABLE_NAME"), mdbFile));
        }
        catch (SQLException e) {
            return;
        }
        finally {
        	try {
    			rs.close();
    		}
    		catch (SQLException e) {}
        }

        for(Iterator iterator = tables.iterator(); iterator.hasNext(); ) {
            Table table = (Table)iterator.next();
            
            addColumns(table, metadata);
            addPrimaryKeys(table, metadata);
            addForeignKeyReferences(table, metadata);
            db.addTable(table);
        }
    }
    
    private static void addColumns(Table table, DatabaseMetaData metadata) {
        ResultSet rs = null;
        
        try {
            rs = metadata.getColumns(null, null, table.getName(), "%");
            
            while(rs.next()) {
                Col column = new ColImpl(
                    rs.getString("COLUMN_NAME"),
                    convertSqlTypeToTdmType(rs.getInt("DATA_TYPE")));
                
                // set various properties on the column object where appropriate
                column.setProperty(Col.PROPNAME_DESCRIPTION, rs.getString("REMARKS"));
                
                String isNullable = rs.getString("IS_NULLABLE");
                if(isNullable.equalsIgnoreCase("NO"))
                    column.setProperty(Col.PROPNAME_NOTNULL, "1");
                
                
                table.addCol(column);
            }
        }
        catch (SQLException e) {
            return;
        }
        finally {
        	try {
    			rs.close();
    		}
        	catch (NullPointerException e) {}
    		catch (SQLException e) {}
        }
    }
    
    private static void addPrimaryKeys(Table table, DatabaseMetaData metadata) {
        ResultSet rs = null;
        
        try {
            rs = metadata.getIndexInfo(null, null, table.getName(), true, true);
            
            while(rs.next()) {
                String indexName = rs.getString("INDEX_NAME");
                String colName   = rs.getString("COLUMN_NAME");
                
                if(indexName != null && (indexName.endsWith("PK") || indexName.equals("PrimaryKey"))) {
                	Col primaryKeyColumn = table.getCol(colName);
                	primaryKeyColumn.setProperty(Col.PROPNAME_PK, "1");
                }
            }
        }
        catch (SQLException e) {
            System.err.println("Error while getting primary key information: " + e.getMessage());
        }
        finally {
        	try {
    			rs.close();
    		}
        	catch (NullPointerException e) {}
    		catch (SQLException e) {}
        }
    }
    
    private static void addForeignKeyReferences(Table table, DatabaseMetaData metadata) {
    	ResultSet rs = null;
    	
		try {
			Statement lookupStatement = metadata.getConnection().createStatement();
			rs = lookupStatement.executeQuery(
				  "SELECT szColumn, szReferencedObject FROM MSysRelationships WHERE szObject='" 
				+ table.getName()
				+ "'"
			);
			
			while(rs.next()) {
				Col column = table.getCol(rs.getString(1));
				column.setProperty(Col.PROPNAME_REFERENCES, rs.getString(2));
			}
		} 
		catch (SQLException e) {
			System.err.println("Error while getting foreign key information: " + e.getMessage());
		}
    	finally {
    		try {
    			rs.close();
    		}
    		catch (NullPointerException e) {}
    		catch (SQLException e) {}
    	}
    
    }
    
    
    private static String convertSqlTypeToTdmType(int sqlType) {
        switch(sqlType) {
        	case Types.BIGINT:
        	case Types.INTEGER:
        	case Types.SMALLINT:
        	case Types.TINYINT:
        	    return XMLObject.TYPE_INT;
        	
        	case Types.DECIMAL:
        	case Types.DOUBLE:
        	case Types.FLOAT:
        	    return XMLObject.TYPE_FLOAT;
        	
        	case Types.BIT:
        	case Types.BOOLEAN:
        	    return XMLObject.TYPE_BOOL;
        	
        	case Types.DATE:
        	    return XMLObject.TYPE_DATE;
        	
        	case Types.TIME:
        	    return XMLObject.TYPE_TIME;
        	
        	case Types.CHAR:
        	case Types.VARCHAR:
        	    return XMLObject.TYPE_STRING;
        	
        	default:
        	    // this is kind of stupid, be we want this to never fail
        	    return XMLObject.TYPE_STRING;
        	    /*
        	    throw new IllegalArgumentException(
        	            "convertSqlTypeToTdmType(): unconvertible SQL type: " + sqlType);
        	    */
        }
    }
}
