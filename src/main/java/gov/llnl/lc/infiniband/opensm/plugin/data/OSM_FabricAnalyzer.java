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
 *        file: OSM_FabricAnalyzer.java
 *
 *  Created on: Nov 4, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.data;

import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.core.IB_Link;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Edge;
import gov.llnl.lc.infiniband.opensm.xml.IB_FabricConf;
import gov.llnl.lc.infiniband.opensm.xml.IB_LinkListElement;
import gov.llnl.lc.infiniband.opensm.xml.IB_PortElement;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.time.TimeStamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**********************************************************************
 * Describe purpose and responsibility of OSM_FabricAnalyzer
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Nov 4, 2013 10:09:48 AM
 **********************************************************************/
public class OSM_FabricAnalyzer implements CommonLogger
{
  private OSM_Fabric Fabric;
  
  private OSM_LinkRate MaxRate;
  
  private HashMap<Integer, IB_Guid> LidToGuidMap = new HashMap<Integer, IB_Guid>();
  private HashMap<IB_Guid, Integer[]> GuidToLidMap = new HashMap<IB_Guid, Integer[]>();

  
  /************************************************************
   * Method Name:
   *  getFabric
   **/
  /**
   * Returns the value of fabric
   *
   * @return the fabric
   *
   ***********************************************************/
  
  public OSM_Fabric getFabric()
  {
    return Fabric;
  }



  /************************************************************
   * Method Name:
   *  getFabricName
   **/
  /**
   * Returns the value of fabricName
   *
   * @return the fabricName
   *
   ***********************************************************/
  
  public String getFabricName()
  {
    return Fabric.getFabricName();
  }



  /************************************************************
   * Method Name:
   *  getFabricTimeStamp
   **/
  /**
   * Returns the value of fabricTimeStamp
   *
   * @return the fabricTimeStamp
   *
   ***********************************************************/
  
  public TimeStamp getFabricTimeStamp()
  {
    return Fabric.getTimeStamp();
  }

  /************************************************************
   * Method Name:
   *  OSM_FabricAnalyzer
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param fabric
   ***********************************************************/
  public OSM_FabricAnalyzer(OSM_Fabric fabric)
  {
    super();
    Fabric = fabric;
    init();
  }
  
  private boolean init()
  {
    // looks through the entire fabric, and returns the fastest link rate
    // which is essentially the theoretical bw
    
    if((Fabric == null) || (Fabric.getOSM_Ports() == null))
      logger.severe("NULL problem finding max rate (fabric or OSM_Port map)");

    MaxRate = getMaxLinkRate(Fabric.getOSM_Ports());
    
    // build the maps, used for quick lookups
    buildGuidToLidMap();
    buildLidToGuidMap();
    
    return true;
   }
  
  private boolean buildGuidToLidMap()
  {
    /* every guid has a lid, but the lid can change, only the guid is guaranteed to remain constant
     *   the key will be the guid, and the value is the lid (support up to two lids per guid)
     *
     * although multiple lids are not yet supported, this MAP is the the preferred
     * mechanism to use to use a GUID to find its array of LIDS

     */
    if((Fabric == null) || (Fabric.getOSM_Ports() == null))
    {
      logger.severe("Could not build the GuidToLidMap");
      return false;
    }

    LinkedHashMap<String, OSM_Port> portMap = Fabric.getOSM_Ports();
    
    for (OSM_Port p : portMap.values())
    {
      // just skip over bad ports
      if((p == null) || (p.getAddress() == null) || (p.getAddress().getGuid() == null) || (p.getAddress().getLocalId() == 0))
        continue;
      
      IB_Guid g = p.getAddress().getGuid();
      int lid = p.getAddress().getLocalId();
      
      Integer[] lidArray = GuidToLidMap.get(p.getAddress().getGuid());
      if(lidArray == null)
      {
        lidArray = new Integer[2];
        lidArray[0] = lid;
        GuidToLidMap.put(g, lidArray);
      }
      else
      {
        // already exists?  Duplicate, or additional (write over #1)
        if(lidArray[0] != lid)
          lidArray[1] = lid;
       }
      // save the results
      GuidToLidMap.put(g, lidArray);
    }
    return true;
  }

  private boolean buildLidToGuidMap()
  {
    /* every guid has at least one lid, but can have multiple if LMC is non-zero
     * 
     * although multiple lids are not yet supported, this MAP is the the preferred
     * mechanism to use to use a LID to lookup a GUID
     */
    
    if((Fabric == null) || (Fabric.getOSM_Ports() == null))
    {
      logger.severe("Could not build the LidToGuidMap");
      return false;
    }

    LinkedHashMap<String, OSM_Port> portMap = Fabric.getOSM_Ports();
    
    for (OSM_Port p : portMap.values())
    {
      // just skip over bad ports
      if((p == null) || (p.getAddress() == null) || (p.getAddress().getGuid() == null) || (p.getAddress().getLocalId() == 0))
        continue;
      
      IB_Guid g = p.getAddress().getGuid();
      int lid = p.getAddress().getLocalId();
      
      LidToGuidMap.put(new Integer(lid), g);
    }
    return true;
  }
  
//  public void checkFabricStructure(IB_FabricConf config)
//  {
//    LinkedHashMap<String, IB_Link> linkMap = Fabric.getIB_Links();
//    
//    // the fabric conf is supposed to be the reference, so report differences between it and actual
//    for(IB_LinkListElement lle: config.getNodeElements())
//    {
//      lle.getName();  // name of the node
//      lle.getElementName();
//      for(IB_PortElement pe: lle.getPortElements())
//      {
//        pe.getNumber();  // this port number
//        pe.getIB_RemoteNodeElement().getName();   // the name of the remote node
//        pe.getIB_RemotePortElement().getNumber(); // the remote port number
//        
//        // attempt to find this "ideal" link in the edge array
//        
//      }
//    }
//    
//  }

  


  public static OSM_LinkRate getMaxLinkRate(LinkedHashMap<String, OSM_Port> portMap)
  {
    // looks through the provided portMap, and returns the fastest link rate
    // which is essentially the theoretical bw

    OSM_LinkRate maxLinkRate = OSM_LinkRate.ZERO;

    // iterate through all the ports
    if((portMap == null) || (portMap.values() == null))
    {
      logger.severe("Problem with the portMap, cannot get the RATE");
      return maxLinkRate;
     }
     
    for (OSM_Port p : portMap.values())
    {
      // just skip over bad ports
      if((p == null) || (p.getRate() == null))
        continue;
      
      maxLinkRate = p.getRate().compareAgainst(maxLinkRate) > 0 ? p.getRate() : maxLinkRate;
    }
    return maxLinkRate;
   }


  public OSM_LinkRate getMaxLinkRate()
  {
    return MaxRate;
  }

  public IB_Guid getGuidUsingLidMap(int lid)
  {
    return LidToGuidMap.get(new Integer(lid));
  }

  public int getLidUsingGuidMap(IB_Guid guid)
  {
    Integer[] lidArray = GuidToLidMap.get(guid);
    if(lidArray == null)
      return 0;
    return lidArray[0];
  }

  public int getLidFromPortGuid(IB_Guid guid) 
  {
    // iterates, so is potentially slower than the Map form
    return Fabric.getOsmPorts().getLidFromPortGuid(guid);
  }

  public String getNameFromGuid(IB_Guid guid) 
  {
    return Fabric.getOsmNodes().getNameFromGuid(guid);
  }

  public IB_Guid getPortGuidFromLid(int lid)
  {
    // iterates, so is potentially slower than the Map form
    return Fabric.getOsmPorts().getPortGuidFromLid(lid);
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

}
