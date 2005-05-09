package edu.tesc.scidb.databank.sms.tdm;

import java.util.List;

/**
 *  Title: Description: Copyright: Copyright (c) 2000 Company:
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
 *@version    1.0
 */

public interface Relationship
{

    /**
     *  Gets the baseTable attribute of the Relationship object
     *
     *@return    The baseTable value
     *@since
     */
    public String getBaseTable();

    //  public String getBaseCol();

    /**
     *  Gets the baseCols attribute of the Relationship object
     *
     *@return    The baseCols value
     *@since
     */
    public List getBaseCols();


    /**
     *  Gets the targetTable attribute of the Relationship object
     *
     *@return    The targetTable value
     *@since
     */
    public String getTargetTable();

    // public String getTargetCol();

    /**
     *  Gets the targetCols attribute of the Relationship object
     *
     *@return    The targetCols value
     *@since
     */
    public List getTargetCols();

    //  public String toString2();

    /**
     *  Gets the name attribute of the Relationship object
     *
     *@return    The name value
     *@since
     */
    public String getName();


}