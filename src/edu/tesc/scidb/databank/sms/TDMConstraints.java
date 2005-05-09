package edu.tesc.scidb.databank.sms;

/**
 *  Description of the Interface
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
 *  Defines a large number of static values for use with TDM objects
 *
 *@author     Erik Ordway ordwayeATevergreen.edu
 *@since    November 20, 2001
 */
public interface TDMConstraints extends XMLObject
{

    /**
     *  These represent names for common things*
     *
     *@since
     */
    public final static String
            NAME_DEFAULT = "";
    //dep




    /**
     *  relation quantifier
     *
     *@since
     */
    public final static int
            REL_NONE = 0,
    REL_ONE_TO_ONE = 1,
    REL_ONE_TO_MANY = 2,
    REL_MANY_TO_ONE = 4,
    REL_MANY_TO_MANY = 8;

}

