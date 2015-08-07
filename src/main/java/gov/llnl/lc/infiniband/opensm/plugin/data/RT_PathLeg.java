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
 *        file: RT_PathLeg.java
 *
 *  Created on: May 6, 2014
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.data;

import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.logging.CommonLogger;

/**********************************************************************
 * Describe purpose and responsibility of RT_PathLeg
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version May 6, 2014 2:27:16 PM
 **********************************************************************/
public class RT_PathLeg implements CommonLogger
{
  OSM_Port FromPort;
  OSM_Port ToPort;
  
  RT_Path ParentPath;
  
  
  /************************************************************
   * Method Name:
   *  RT_PathLeg
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param fromPort
   * @param toPort
   ***********************************************************/
  public RT_PathLeg(OSM_Port fromPort, OSM_Port toPort)
  {
    this(fromPort, toPort, null);
  }
  
  public RT_PathLeg(OSM_Port fromPort, OSM_Port toPort, RT_Path path)
  {
    super();
    FromPort = fromPort;
    ToPort = toPort;
    ParentPath = path;
  }
  
  private int getLid(RT_Path path)
  {
    // get the lid out of the table, because they don't seem to be
    // in the fabric data structure
    
    return path.getLid(ToPort.getNodeGuid());
  }

  public int getLid()
  {
    return getLid(ParentPath);
  }
  
  public String toIB_TraceRtString()
  {
    return toIB_TraceRtString( ParentPath);
  }


  public String toIB_TraceRtString( RT_Path path)
  {
    // this should match (as closely as possible) the output
    // of the command; ibtracert -G src dst
    //
    StringBuffer stringValue = new StringBuffer();
//    stringValue.append("toIB_TraceRtString" + "\n");
//    stringValue.append("{" + FromPort.getAddress().getGuid() + "}\n");

    
//    [1] -> switch port {0x00066a00ec002eec}[9] lid 17-17 "ibcore1 L109"
//    [27] -> switch port {0x00066a00eb002d14}[11] lid 8-8 "ibcore1 S205A"
//    [3] -> switch port {0x00066a02e8001313}[36] lid 7-7 "ibcore1 L101"
//    [9] -> ca port {0x001175000077a00e}[1] lid 332-332 "hype353 qib0"
    
    int lid = FromPort.getAddress().getLocalId();
    lid = getLid(path);
    
    
    stringValue.append("[" + FromPort.getPortNumber() + "] -> " + path.getTypeString(ToPort.getAddress().getGuid()) + " port ");
    stringValue.append("{0x" + ToPort.getAddress().getGuid() + "}[" + ToPort.getPortNumber() + "] lid " + lid + "-" + lid + " ");
    stringValue.append(getNodeName(path, ToPort.getAddress().getGuid()));
    
    return stringValue.toString();
  }

  public boolean startsWith(IB_Guid source)
  {
    // true if this guid is the same as the FromPort Guid
    if((FromPort != null) && (FromPort.getNodeGuid() != null))
      return FromPort.getNodeGuid().equals(source);
    return false;
  }

  public boolean endsWith(IB_Guid destination)
  {
    // true if this guid is the same as the ToPort Guid
    if((ToPort != null) && (ToPort.getNodeGuid() != null))
    {
      // special case for CA, handle this destination guid
      //  and if fails, and if ToPort is #1, also check guid +1
      boolean match = ToPort.getNodeGuid().equals(destination);
      if((!match) && (ToPort.getPortNumber() == 1))
      {
        IB_Guid pg = new IB_Guid(ToPort.getNodeGuid().getGuid() +1);
//        System.err.println("To Port Guid is: " + pg.toColonString());
//        System.err.println("Destina Guid is: " + destination.toColonString());
        match = pg.equals(destination);
      }
      return match;
    }
    return false;
  }

  /************************************************************
   * Method Name:
   *  getFromPort
   **/
  /**
   * Returns the value of fromPort
   *
   * @return the fromPort
   *
   ***********************************************************/
  
  public OSM_Port getFromPort()
  {
    return FromPort;
  }

  public String getNodeName(RT_Path path, IB_Guid guid)
  {
    if((path != null) && (guid != null))
      return path.getNodeName(guid);
    return "unknown";
  }

  public String getFromNodeName()
  {
    return getNodeName(ParentPath, FromPort.getAddress().getGuid());
  }

  /************************************************************
   * Method Name:
   *  getToPort
   **/
  /**
   * Returns the value of toPort
   *
   * @return the toPort
   *
   ***********************************************************/
  
  public OSM_Port getToPort()
  {
    return ToPort;
  }
  
  public String getToNodeName()
  {
    return getNodeName(ParentPath, ToPort.getAddress().getGuid());
  }

  /************************************************************
   * Method Name:
   *  getParentPath
   **/
  /**
   * Returns the value of parentPath
   *
   * @return the parentPath
   *
   ***********************************************************/
  
  public RT_Path getParentPath()
  {
    return ParentPath;
  }


  
}
