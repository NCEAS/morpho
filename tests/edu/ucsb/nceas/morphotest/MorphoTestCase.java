package edu.ucsb.nceas.morphotest;

import java.io.File;
import java.io.IOException;


import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 * Based test case for morpho
 * @author tao
 *
 */
public class MorphoTestCase extends TestCase
{
  
  
  protected static ConfigXML config = null;
  protected static ConfigXML profile = null;

  
  static {
    try {
     /*File configDir = new File(ConfigXML.getConfigDirectory());
      File configFile = new File(configDir, "config.xml");
      config = new ConfigXML(configFile.getAbsolutePath());
      File currentProfileLocation = new File(configDir, "currentprofile.xml");
      ConfigXML currentProfileConfig = new ConfigXML(currentProfileLocation.getAbsolutePath());
      String currentProfileName = currentProfileConfig.get("current_profile", 0);
      String profileDirName = config.getConfigDirectory()+
      File.separator+config.get("profile_directory", 0)+
      File.separator+currentProfileName;
      //System.out.println("the profile dir is "+profileDirName);
      File profileLocation = new File(profileDirName, currentProfileName+".xml");
      profile = new ConfigXML(profileLocation.getAbsolutePath());*/
      Morpho.createMorphoInstance();
      config = Morpho.thisStaticInstance.getConfiguration();
      profile = Morpho.thisStaticInstance.getProfile();
  } catch (Exception ioe) {
    fail("Test failed, couldn't create morpho instance."+ioe.getMessage());
  }
  }
  /**
   * Constructor to build the test
   */
  public MorphoTestCase() 
  {
      super();
  }

  /**
   * Constructor to build the test
   *
   * @param name the name of the test method
   */
  public MorphoTestCase(String name) 
  {
      super(name);
  }
  
 

}
