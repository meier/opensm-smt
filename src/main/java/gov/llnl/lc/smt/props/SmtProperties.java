/************************************************************
 * Copyright (c) 2015, Lawrence Livermore National Security, LLC.
 * Produced at the Lawrence Livermore National Laboratory.
 * Written by Timothy Meier, meier3@llnl.gov, All rights reserved.
 * LLNL-CODE-673346
 *
 * This file is part of the OpenSM Monitoring Service (OMS) package.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (as published by
 * the Free Software Foundation) version 2.1 dated February 1999.
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
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * OUR NOTICE AND TERMS AND CONDITIONS OF THE GNU GENERAL PUBLIC LICENSE
 *
 * Our Preamble Notice
 *
 * A. This notice is required to be provided under our contract with the U.S.
 * Department of Energy (DOE). This work was produced at the Lawrence Livermore
 * National Laboratory under Contract No.  DE-AC52-07NA27344 with the DOE.
 *
 * B. Neither the United States Government nor Lawrence Livermore National
 * Security, LLC nor any of their employees, makes any warranty, express or
 * implied, or assumes any liability or responsibility for the accuracy,
 * completeness, or usefulness of any information, apparatus, product, or
 * process disclosed, or represents that its use would not infringe privately-
 * owned rights.
 *
 * C. Also, reference herein to any specific commercial products, process, or
 * services by trade name, trademark, manufacturer or otherwise does not
 * necessarily constitute or imply its endorsement, recommendation, or favoring
 * by the United States Government or Lawrence Livermore National Security,
 * LLC. The views and opinions of authors expressed herein do not necessarily
 * state or reflect those of the United States Government or Lawrence Livermore
 * National Security, LLC, and shall not be used for advertising or product
 * endorsement purposes.
 *
 *        file: SmtProperties.java
 *
 *  Created on: Jan 9, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.props;

import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.SmtConstants;

import java.util.Properties;


/**********************************************************************
 * Describe purpose and responsibility of SmtProperties
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Jan 9, 2013 1:31:02 PM
 **********************************************************************/
public class SmtProperties extends Properties implements SmtConstants, CommonLogger
{
  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = 8822647195977685687L;
  
  
  // these are the default system properties unless others are specified
  // on the command line
   public GlobalProperties gProp         = new GlobalProperties();
   public CommandProperties cProp        = new CommandProperties();
  
   static final String SMT_APPLICATION_NAME_KEY = "SubnetMonitorTool.application.name";
   static final String SMT_DATA_DIR_KEY         = "SubnetMonitorTool.data.dir";
   static final String SMT_IMAGE_DIR_KEY        = "SubnetMonitorTool.image.dir";
   static final String SMT_IMAGE_DIR            = "/gov/llnl/lc/smt/images/";
   static final String SMT_VERSION_KEY          = "SubnetMonitorTool.version";
   static final String SMT_VERSION              = "2.0.1";


   /************************************************************
   * Method Name:
   *  mergeUserProperties
  **/
  /**
   * If a users config (properties), file exists, then read it,
   * and overwrite the existing Global or Common Properties.
   * Ignore properties that don't exist in Global or Common.
   *
   * @see     describe related java objects
   *
   * @return
   ***********************************************************/
  public SmtProperties mergeUserProperties( )
  {
    return this;
  }
  
  public void printProperties()
  {
    System.out.println("*** Global Network Properties ***");
    gProp.nProp.list(System.out);
    System.out.println("*** Global Authentication Properties ***");
    gProp.aProp.list(System.out);
    System.out.println("*** Global Keystore Properties ***");
    gProp.kProp.list(System.out);
    System.out.println("*** Global Privilages Properties ***");
    gProp.pProp.list(System.out);
    System.out.println("*** Command Line Properties ***");
    cProp.list(System.out);
    System.out.println("*** System Properties ***");
    
    System.getProperties().list(System.out);
  }
  
  /**
  * @return Returns the directory name of the location that holds the raw cluster data files
  */
  public String getDataDir()
  {
    logger.info("DataDir is: " + getProperty(SMT_DATA_DIR_KEY));
    return getProperty(SMT_DATA_DIR_KEY);
  }

  /**
  * @return Returns the directory name of the location that holds the all the image files
  */
  public String getImageDir(boolean logIt)
  {
    if(logIt)
      logger.info("ImageDir is: " + getProperty(SMT_DATA_DIR_KEY, SMT_IMAGE_DIR));
    return getProperty(SMT_IMAGE_DIR_KEY, SMT_IMAGE_DIR);
  }

  public String getImageDir()
  {
    return getImageDir(true);
  }

  public String getVersion()
  {
    logger.info("SmtVersion is: " + getProperty(SMT_VERSION_KEY, SMT_VERSION));
    return getProperty(SMT_VERSION_KEY, SMT_VERSION);
  }

  /**
  * @return Returns the Applications Name
  */
  public String getApplicationName()
  {
    logger.info("Application Name is: " + getProperty(SMT_APPLICATION_NAME_KEY));
    return getProperty(SMT_APPLICATION_NAME_KEY, "default application name");
  }


  /************************************************************
   * Method Name:
   *  main
   **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   * @param args
   ***********************************************************/
  public static void main(String[] args)
  {
    System.err.println(new SmtProperties().getImageDir());

  }

}
