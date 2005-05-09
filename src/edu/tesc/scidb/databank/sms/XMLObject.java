package edu.tesc.scidb.databank.sms;


/**
 *  An XML Object is simply a collection of properties.
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
public interface XMLObject
{

    public final static String
	    TEM_TYPE_ENTITY = "Entity",
	    TEM_TYPE = "type",
	    TEM_TYPE_OBSERVATION = "Observation",
	    TEM_TYPE_TEMPLATE = "Template",
	    TEM_TYPE_ATTRIBUTE = "Attribute",
	    TEM_TYPE_DESCRIPTIVEDATA = "DescriptiveData" ,
		TEM_TYPE_DEPENDENCY = "Dependency",
	
	    TEM_DEP_TYPE = "type",
	    TEM_DEP_TYPE_SPECIFIC = "specific",
	    TEM_DEP_TYPE_LIST = "list",
	    TEM_DEP_TYPE_GENERAL = "general",
	
	    TEM_DEP_RESOLVED_STATUS = "dependancyResolvedStatus" ,
	    TEM_DEP_RESOLVED_BY_SYSTEM = "dependancyResolvedBySystem" ,
	    TEM_DEP_RESOLVED_BY_USER = "dependancyResolvedByUser" ,
	
	    TEM_DEP_RESOLVED_BY = "dependancyResolvedBy",
	    TEM_TEMPLATE_BASE_LOCATION ="schema/",
				TEM_CATALOG = "catalog" ,
	            TEM_WORKSPACE = "workspace",
	            TEM_PATH = "path",
	    TEM_CLONEABLE = "cloneable",
	    TEM_LINKABLE = "linkable" ,
	    TEM_DBNAME = "DBName",
	    TEM_DATE_ADDED = "dateAdded" ,
	    TEM_MASK = "mask" ,
		MASK_BASE_LOCATION ="mask/";
    
    

    public final static String
	    TEM_INSERT_TARGET_NAME = "name",
	    TEM_INSERT_TARGET_TABLE = "targetTable",
	    TEM_INSERT_TARGET_SCHEMA = "targetSchema";
    
    
    public final static String
	    TEM_INSERT_COL_NAME = "name",
	    TEM_INSERT_COL_VALUE = "value";

    public final static String
    	MASK_CHILD_OF = "childOf";

    public final static String
	    PROPNAME_NAME = "name",
	    PROPNAME_DISPLAY_NAME = "displayName",
	    PROPNAME_ID = "id",
	    PROPNAME_DESCRIPTION = "description",
	    PROPNAME_RQ = "rq",
	    PROPNAME_INHERITSFROM = "inheritsFrom",
	    PROPNAME_DATATYPE = "dataType",
	    PROPNAME_TYPE = "type",
	    PROPNAME_PATH = "path",
	    PROPNAME_PATH_NAME = "pathname",
	    PROPNAME_MASK_PATH = "maskPath",
	    PROPNAME_IMAGE = "image",
	    PROPNAME_ENUMERATION = "enumeration",
	    PROPNAME_ENUMERATION_LIST = "enumerationList",
	    PROPNAME_DATA_TYPE = "dataType",
	    PROPNAME_CONSTRAINT = "constraint",
	    PROPNAME_UNIT = "unit",
	    PROPNAME_REFERENCE_TARGET = "referenceTarget",
	    PROPNAME_DEP_TYPE_GENERAL = "general",
	    PROPNAME_DEP_TYPE_LIST = "list",
	    PROPNAME_DEP_TYPE_SPECIFIC = "specific",
    	PROPNAME_DOMAIN = "domain",
		PROPNAME_DISPLAY_DESCRIPTION = "displayDescription",
		PROPNAME_IMAGE_SMALL = "imageSmall";


    /**
     *  Types for data in database models
     */
    public final static String
	    TYPE_INT = "Integer",
	    //begin
	    TYPE_PERCENT = "Percent",
	    //begin
	    TYPE_FLOAT = "Decimal",
	    //
	    TYPE_STRING = "String",
	    //These types are derived from the base types in Access
	    TYPE_BIGSTRING = "BigString",
	    TYPE_TEXT = "Text",
	    TYPE_MEMO = "Memo",
	    //
	    TYPE_DATE = "Date",
	    //
	    TYPE_TIME = "Time",
	    //
	    TYPE_DATETIME = "DateTime",
	    //
	    TYPE_BOOL = "Boolean",
	    //end
	    TYPE_ID = "ID",
	    //ID Field with ID type
	    TYPE_IDREF = "Reference",
	    //specifies a reference to an Entity
	    TYPE_DATAIDREF = "StaticReference";
	    //specifies a reference to a Data Entity

   /**
     *  these is a property values for observations
     */ 
    public final static String
	    RQ_ONETOONE = "oneToOne",
	    RQ_NONE = "none",
	    RQ_ONETOMANY = "oneToMany",
	    RQ_MANYTOMANY = "manyToMany",
	    PROPNAME_GROUP = "group",
	    PROPNAME_TYPE_STRUCTURE = "structure",
	    PROPNAME_TYPE_FUNCTION = "function",
	    PROPNAME_REFERENCETARGET = "referenceTarget",
	    DEFAULTNAME = "";
    
    /**
     *  these is a property values for columns in database models
     */
    public final static String
	    PROP_PRIMARY_KEY = "primaryKey",
	    PROP_NOT_NULL = "notNull",
	    PROP_REFERENCES = "references",
	    //specifies an Entity to reference
	    PROP_REFERENCES_SPECIFIER = "referencesSpecifier",
	    //specifies an Archtypel ObservationGroup in the specified 
		//	Entity to Reference
	    PROP_REFERENCES_DISPLAY_NAME = "referencesDisplayName",
	    //specifies the name used in displaying and creating the Reference
	    PROP_REFERENCES_DATA = "referencesData",
	    //specifies a Data Entity to reference
	    PROP_REFERENCES_DATA_SPECIFIER = "referencesDataSpecifier",
	    //further specifies a non defualt Data Entity to reference
	    PROP_AUTO_INCREMENT = "autoIncrement",
	    PROP_NAME = "name",
	    PROP_TYPE = "type",
	    PROP_ORDER = "order",
	    PROP_NULLABLE = "nullable",
	    PROP_DESCRIPTION = "description",
	    PROP_UPPER_LIMIT = "upperLimit",
	    PROP_LOWER_LIMIT = "lowerLimit",
	    PROP_ENUMERATED_VALUES = "enumeratedValues",
	    PROP_UNIT_OF_MEASUREMENT = "unitOfMeasurment",
	    PROP_MEASUREMENT_FREQUENCY = "measurementFrequency",
	    PROP_MISSING_VALUE_CODE = "missingValueCode",
	    PROP_DOMAIN_TYPE = "domainType",
	    PROP_VALUE_ACCURACY = "valueAccuracy",
	    PROP_MEASUREMENT_RESOLUTION = "measurementResolution";

    /**
     *  These are Atrributes that are of specific use to Projects and Studies
     */
    public final static String
	    ATT_NAME = "name",
	    ATT_OBJECTIVE = "objective",
	    ATT_ABSTRACT = "abstract",
	    ATT_START_DATE = "startDate",
	    ATT_DURATION = "duration",
	    ATT_FUNDING = "funding",
	    ATT_DATA_ACCESS_POLICY = "dataAccessPolicy",
	    ATT_DISTRIBUTION_LIABILITY = "distributionLiabitiy",
	    ATT_DATABASE_CITATION = "databaseCitation",
	    ATT_QUALITITY_ASSURANCE_PROCEDURE = "qualitityAssuranceProcedure",
	    ATT_DATA_ENTRY_PROCEDURE = "dataEntryProcedure",
	    ATT_DATA_ARCHIVE_PROCEDURE = "dataArchiveProcedure";

	public final static String 
		PROPNAME_RENDER_OBS = "renderObs";
		
}
