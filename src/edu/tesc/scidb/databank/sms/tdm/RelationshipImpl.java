package edu.tesc.scidb.databank.sms.tdm;

import java.util.Collections;
import java.util.List;

/**
 *  Description of the Class
 *
 *  Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *     must display the following acknowledgement:
 *     This product includes software developed by the The Evergreen State
 *     College-- Scientific Laboritory and Canopy Research Group and its
 *     contributors.  With Funding provived by the NSF Database Activities
 *     Program [BIR 9975510]
 * 4. Neither the name of the College or NSF nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 *@author     Erik Ordway ordwayeATevergreen.edu
 *@since    November 20, 2001
 */
public class RelationshipImpl implements Relationship
{
    private String baseTable;
    private String baseCol;
    private String targetTable;
    private String targetCol;


    /**
     *  Constructor for the RelationshipImpl object
     *
     *@param  baseTable    Description of Parameter
     *@param  baseCol      Description of Parameter
     *@param  targetTable  Description of Parameter
     *@param  targetCol    Description of Parameter
     *@since
     */
    public RelationshipImpl(String baseTable, String baseCol, String targetTable, String targetCol)
    {
        this.baseTable = baseTable;
        this.baseCol = baseCol;
        this.targetTable = targetTable;
        this.targetCol = targetCol;
    }


    /**
     *  Gets the baseTable attribute of the RelationshipImpl object
     *
     *@return    The baseTable value
     *@since
     */
    public String getBaseTable()
    {
        return baseTable;
    }

    //public String getBaseCol(){return baseCol;}

    /**
     *  Gets the baseCols attribute of the RelationshipImpl object
     *
     *@return    The baseCols value
     *@since
     */
    public List getBaseCols()
    {
        return Collections.nCopies(1, baseCol);
    }


    /**
     *  Gets the targetTable attribute of the RelationshipImpl object
     *
     *@return    The targetTable value
     *@since
     */
    public String getTargetTable()
    {
        return targetTable;
    }

    //public String getTargetCol(){return targetCol;}

    /**
     *  Gets the targetCols attribute of the RelationshipImpl object
     *
     *@return    The targetCols value
     *@since
     */
    public List getTargetCols()
    {
        return Collections.nCopies(1, targetCol);
    }


    /**
     *  Gets the name attribute of the RelationshipImpl object
     *
     *@return    The name value
     *@since
     */
    public String getName()
    {
        return "Relationship" + getBaseTable() + baseCol + getTargetTable() + targetCol;
    }


    /**
     *  Description of the Method
     *
     *@return    Description of the Returned Value
     *@since
     */
    public String toString()
    {
        return "RelationshipImpl : " + getBaseTable() + " -> " + getTargetTable();
    }


}

