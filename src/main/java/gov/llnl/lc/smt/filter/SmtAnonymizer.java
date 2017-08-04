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
 *        file: SmtAnonymizer.java
 *
 *  Created on: Aug 4, 2017
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.filter;

import java.io.IOException;

import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.infiniband.opensm.plugin.net.OsmServerStatus;
import gov.llnl.lc.net.ObjectSession;
import gov.llnl.lc.smt.command.SmtCommand;

/**********************************************************************
 * Describe purpose and responsibility of SmtAnonymizer
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Aug 4, 2017 10:52:15 AM
 **********************************************************************/
public class SmtAnonymizer
{

  String AnonymizerFileName = "unknown";
  String Description        = "unknown";
 
  /************************************************************
   * Method Name:
   *  SmtAnonymizer
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   ***********************************************************/
  public SmtAnonymizer()
  {
    super();
  }
  
  
  /************************************************************
   * Method Name:
   *  SmtAnonymizer
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param anonymizerFileName
   * @throws IOException 
   ***********************************************************/
  public SmtAnonymizer(String anonymizerFileName) throws IOException
  {
    this(anonymizerFileName, null);
  }
  
  /************************************************************
   * Method Name:
   *  SmtAnonymizer
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param val
   * @param oMService
   ***********************************************************/
  public SmtAnonymizer(String anonymizerFileName, OpenSmMonitorService OMS) throws IOException
  {
    super();
    String fName = SmtCommand.convertSpecialFileName(anonymizerFileName);
    if(fName != null)
      AnonymizerFileName = fName;
    
  }




  /************************************************************
   * Method Name:
   *  getAnonymizerFileName
  **/
  /**
   * Returns the value of anonymizerFileName
   *
   * @return the anonymizerFileName
   *
   ***********************************************************/
  
  public String getAnonymizerFileName()
  {
    return AnonymizerFileName;
  }


  /************************************************************
   * Method Name:
   *  setAnonymizerFileName
  **/
  /**
   * Sets the value of anonymizerFileName
   *
   * @param anonymizerFileName the anonymizerFileName to set
   *
   ***********************************************************/
  public void setAnonymizerFileName(String anonymizerFileName)
  {
    AnonymizerFileName = anonymizerFileName;
  }


  /************************************************************
   * Method Name:
   *  getDescription
  **/
  /**
   * Returns the value of description
   *
   * @return the description
   *
   ***********************************************************/
  
  public String getDescription()
  {
    return Description;
  }


  /************************************************************
   * Method Name:
   *  setDescription
  **/
  /**
   * Sets the value of description
   *
   * @param description the description to set
   *
   ***********************************************************/
  public void setDescription(String description)
  {
    Description = description;
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
    // TODO Auto-generated method stub

  }

  /************************************************************
   * Method Name:
   *  hasEmptyAnonymizer
  **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   * @return
   ***********************************************************/
  public boolean hasEmptyAnonymizer()
  {
    // TODO Auto-generated method stub
    return false;
  }


  /************************************************************
   * Method Name:
   *  getOpenSmMonitorService
  **/
  /**
   * Anonymize the provided OMS using the rules in the provided anonymizer.
   *
   * @see     describe related java objects
   *
   * @param oms
   * @param anonymizer
   * @return
   ***********************************************************/
  public static OpenSmMonitorService getOpenSmMonitorService(OpenSmMonitorService oms, SmtAnonymizer anonymizer)
  {
    
    
    // anonymize the original, and add to new History
    // the fabric is the only thing that is filtered
    //return new OpenSmMonitorService(oms.getParentSessionStatus(), oms.getRemoteServerStatus(), OSM_Fabric.getOSM_Fabric(oms.getFabric(), filter));


    // take apart the OMS, and anonymize each piece, before putting it back together
    
    // names, descriptions
    // guids
    
    ObjectSession session  = anonymizer.anonymize(oms.getParentSessionStatus());
    OsmServerStatus server = anonymizer.anonymize(oms.getRemoteServerStatus());
    OSM_Fabric fabric      = anonymizer.anonymize(oms.getFabric());
    
    OpenSmMonitorService newOMS = new OpenSmMonitorService(session, server, fabric);

    return newOMS;
  }


  /************************************************************
   * Method Name:
   *  anonymize
  **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   * @param fabric
   * @return
   ***********************************************************/
  private OSM_Fabric anonymize(OSM_Fabric fabric)
  {
    System.err.println("Anonymize Fabric");
    return fabric;
  }


  /************************************************************
   * Method Name:
   *  anonymize
  **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   * @param remoteServerStatus
   * @return
   ***********************************************************/
  private OsmServerStatus anonymize(OsmServerStatus remoteServerStatus)
  {
    System.err.println("Anonymize ServerStatus");
    return remoteServerStatus;
  }


  /************************************************************
   * Method Name:
   *  anonymize
  **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   * @param parentSessionStatus
   * @return
   ***********************************************************/
  private ObjectSession anonymize(ObjectSession parentSessionStatus)
  {
    System.err.println("Anonymize SessionStatus");
    return parentSessionStatus;
  }

}
