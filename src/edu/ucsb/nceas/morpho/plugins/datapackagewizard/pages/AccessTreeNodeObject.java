package edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages;

import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.CustomList;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;

class AccessTreeNodeObject{

  String DNinfo = null;
  int  nodeType = 0;

  public AccessTreeNodeObject(String DNinfo, int nodeType) {
    if(nodeType == WizardSettings.ACCESS_PAGE_AUTHSYS){
      try{
        DNinfo = DNinfo.substring(DNinfo.indexOf("o="));
      }catch (Exception e){
       Log.debug(1,DNinfo);
       Log.debug(10, e.getMessage());
      }
    }

    this.DNinfo = DNinfo;
    this.nodeType = nodeType;

  }

  public String getDN(){
    return DNinfo;
  }
  public String toString(){
    String value = null;
    String key = null;

    if(nodeType == WizardSettings.ACCESS_PAGE_AUTHSYS){
      key = "o=";
    } else if(nodeType == WizardSettings.ACCESS_PAGE_GROUP){
      key = "cn=";
    } else if(nodeType == WizardSettings.ACCESS_PAGE_USER){
      key = "uid=";
    }

    value = DNinfo.substring(DNinfo.indexOf(key) + key.length());
    value = value.substring(0, value.indexOf(","));

    return value;
 }


}
