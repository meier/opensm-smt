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
 *        file: AnonymizeProperties.java
 *
 *  Created on: Jan 9, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.props;

import java.util.Properties;

import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.parser.ParserUtils;


/**********************************************************************
 * Describe purpose and responsibility of AnonymizeProperties
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Jan 9, 2013 1:31:02 PM
 **********************************************************************/
public class AnonymizeProperties extends Properties implements CommonLogger
{
  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = 8822647183048685687L;
  
  static final String ANON_FILENAME_KEY    = "Anonymize.name";
  static final String ANON_DESCRIPTION_KEY = "Anonymize.description";
  static final String ANON_FABRIC_KEY      = "Anonymize.fabric.name";
  static final String ANON_NAME_KEY        = "Anonymize.object.name";
  static final String ANON_GUID_KEY        = "Anonymize.guid.offset.inHex";
  static final String ANON_SUBNET_KEY      = "Anonymize.subnet.offset.inHex";
  static final String ANON_KEY_KEY         = "Anonymize.key.offset.inHex";
  static final String ANON_SERVER_KEY      = "Anonymize.server.name";
  static final String ANON_USER_KEY        = "Anonymize.user.name";
  static final String ANON_HOST_KEY        = "Anonymize.host.name";
  
  /* the default values */
  protected static String AnonymizerFileName  = "unknown";
  protected static String Description         = "unknown";
  protected static String FabricName          = "anonymized";
  protected static String ObjectNameAnonymize = "anonymized";
  protected static String guidAnonymize       = "0xff";
  protected static String subnetAnonymize     = "0xff";
  protected static String keyAnonymize        = "0xff";
  protected static String serverAnonymize     = "AnonymousServer";
  protected static String userAnonymize       = "AnonymousUser";
  protected static String hostAnonymize       = "AnonymousHost";
 
  public void printProperties()
  {
    System.out.println("*** Anonymizer Properties ***");
    list(System.out);
  }
  
  /**
  * @return Returns the name of the file containing the anonymizer instructions
  */
  public String getAnonymizerFileName()
  {
    return getProperty(ANON_FILENAME_KEY, AnonymizerFileName);
  }

  public void setAnonymizerFileName(String filename)
  {
    this.setProperty(ANON_FILENAME_KEY, filename);
  }

  public String getDescription()
  {
    return getProperty(ANON_DESCRIPTION_KEY, Description);
  }

  public String getName()
  {
    return getProperty(ANON_NAME_KEY, ObjectNameAnonymize);
  }

  /**
  * @return Returns the Anonymous Fabric Name
  */
  public String getFabricName()
  {
    return getProperty(ANON_FABRIC_KEY, FabricName);
  }

  public long getGuidOffset()
  {
    String sOffset = getProperty(ANON_GUID_KEY, guidAnonymize);
    return ParserUtils.convertHexStringToLong(sOffset);
  }

  public long getKeyOffset()
  {
    String sOffset = getProperty(ANON_KEY_KEY, keyAnonymize);
    return ParserUtils.convertHexStringToLong(sOffset);
  }

  public long getSubnetOffset()
  {
    String sOffset = getProperty(ANON_SUBNET_KEY, subnetAnonymize);
    return ParserUtils.convertHexStringToLong(sOffset);
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
    System.err.println(new AnonymizeProperties().getAnonymizerFileName());
  }
  
  public AnonymizeProperties()
  {
    this(AnonymizerFileName);
  }

  public AnonymizeProperties(String fileName)
  {
    super();
    String propFile = System.getProperty(ANON_FILENAME_KEY, fileName);
    try
    {
      java.io.InputStream in = new java.io.FileInputStream(propFile);
      load(in);
      in.close();
      setAnonymizerFileName(propFile);
//      this.printProperties();
    }
    catch (Exception e)
    {
      logger.severe("Error reading Anonymize Properties File [" + propFile + "]");
    }
  }

  /************************************************************
   * Method Name:
   *  getServer
  **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   * @return
   ***********************************************************/
  public String getServer()
  {
    return getProperty(ANON_SERVER_KEY, serverAnonymize);
  }

  /************************************************************
   * Method Name:
   *  getUser
  **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   * @return
   ***********************************************************/
  public String getUser()
  {
    return getProperty(ANON_USER_KEY, userAnonymize);
  }

  /************************************************************
   * Method Name:
   *  getHost
  **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   * @return
   ***********************************************************/
  public String getHost()
  {
    return getProperty(ANON_HOST_KEY, hostAnonymize);
  }
}
