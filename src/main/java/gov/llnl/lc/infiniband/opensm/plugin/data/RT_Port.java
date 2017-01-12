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
 *        file: RT_Port.java
 *
 *  Created on: May 5, 2014
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import gov.llnl.lc.infiniband.core.IB_Address;
import gov.llnl.lc.infiniband.core.IB_Guid;

/**********************************************************************
 * This represents the subset of routes from a node that passes through
 * this particular port.  The collection of RT_Port routes from a node
 * makes up the nodes routing table.
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version May 5, 2014 9:00:35 AM
 **********************************************************************/
public class RT_Port implements Serializable, gov.llnl.lc.logging.CommonLogger, Comparable <RT_Port>
{
  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = -8979129116696533224L;
  private RT_Node parentNode;
  private int portNumber;
  
  /* a remote guid and lid lookup map 
   *   the key will be the port guid, and the value is the LID
   *   
   *   If the LID Mask control (LMC) is non-zero, then a node in the fabric
   *   can have more than one LID.  Normally LMC is zero, which means there is
   *   a one-to-one mapping between LID and GUID.
   *   
   *   Currently, I only support LMC of 0, or a one to one mapping (PortGuidMap).
   *   
   *   Supporting multiple LIDs is more correct, but less efficient.  If it is
   *   never used, it seems unnecessary, however....
   *   
   *   */
  private HashMap<String, Integer> PortGuidMap = new HashMap<String, Integer>();
  
  /* this is the more correct one,but not yet fully supported.  It is possible
   * there are multiple LIDs for a single guid if the LMC is non-zero.  We don't
   * operate this way, but it is valid.  Doing it this way enables more exotic
   * routing algorithms.
   */
  private HashMap<Integer, IB_Guid> GuidLidMap = new HashMap<Integer, IB_Guid>();
  
  public RT_Port(RT_Node rt_Node, int portNum, int lid, IB_Guid destGuid)
  {
    parentNode = rt_Node;
    portNumber = portNum;
    
    // add it to both maps,for now
    add(lid, destGuid);
  }

  public void add(int lid, IB_Guid destGuid)
  {
    // current one, to be deprecated
    PortGuidMap.put(destGuid.toColonString(), new Integer(lid));
    
    // this is for future
    GuidLidMap.put(new Integer(lid), destGuid);
  }
  
  public int getLid()
  {
    return parentNode.getLid();
  }
  
  public IB_Guid getGuidForLid(int lid)
  {
    return GuidLidMap.get(new Integer(lid));
  }
  
  public int getLidForGuid(IB_Guid destGuid)
  {
    return PortGuidMap.get(destGuid.toColonString()).intValue();
  }
  
  public ArrayList<Integer> getLidsForGuid(IB_Guid destGuid)
  {
    // check the entire LidGuidMap, and return all lids for this guid
    ArrayList<Integer> lidArray = new ArrayList<Integer>();
    for(Map.Entry<Integer, IB_Guid> entry: GuidLidMap.entrySet())
    {
      IB_Guid g = entry.getValue();
      if(g.equals(destGuid))
        lidArray.add(entry.getKey());
    }
    return lidArray;
  }
  
  /************************************************************
   * Method Name:
   *  getLidGuidMap
   **/
  /**
   * Returns the value of lidGuidMap
   *
   * @return the lidGuidMap
   *
   ***********************************************************/
  
  public HashMap<String, Integer> getLidGuidMap()
  {
    /* different names for the same Map */
    return getPortGuidMap();
  }

  /************************************************************
   * Method Name:
   *  getGuidLidMap
   **/
  /**
   * Returns the map which contains Lids to Guids.  The Lids will
   * be unique, and the guids can appear more than once.  In other
   * words this is the Map that supports multiple lids for a single
   * guid.  Or when LMC is greater than zero.
   *
   * @return the lidGuidMap
   *
   ***********************************************************/
  
  public HashMap<Integer, IB_Guid> getGuidLidMap()
  {
    /* lids are unique, and therefore used as the key in this map
     * and the same guid can appear for different lids */
    return GuidLidMap;
  }

  /************************************************************
   * Method Name:
   *  toString
  **/
  /**
   * Describe the method here
   *
   * @see java.lang.Object#toString()
   *
   * @return
   ***********************************************************/
  
  @Override
  public String toString()
  {
    if((parentNode != null) && (parentNode.getParentTable() != null))
    {
      return getStats(parentNode.getParentTable());
    }

    StringBuffer buff = new StringBuffer();
    
    buff.append("Port #: " + portNumber + ", with " + PortGuidMap.size() + " guids in the list");
    return buff.toString();
  }
  
  public String getStats(RT_Table table)
  {
    int nCA = getNumCaRoutes(table);
    int nSW = getNumSwRoutes(table);
    
    StringBuffer sb = new StringBuffer();
    sb.append(nCA + " CA route");
    if(nCA != 1)
      sb.append("s");
    
    sb.append(", " + nSW + " SW route");
    if(nSW != 1)
      sb.append("s");
    
    sb.append(", total=" + (nCA+nSW));
    return sb.toString();
  }
  
  public String toLongString()
  {
    StringBuffer buff = new StringBuffer();
    
    buff.append("port: " + portNumber + ", " + toString() + "\n");
    buff.append(PortGuidMap + "\n");
    return buff.toString();
  }
  
  public String toIB_RouteString(RT_Table table, OSM_Fabric fabric)
  {
    // this should match (as closely as possible) the output
    // of the command; ibroute -G switchguid
    //
    // 0x0012 033 : (Switch portguid 0x00066a00eb002cee: 'ibcore1 S201A')
//    0x0013 032 : (Switch portguid 0x00066a00eb002cf5: 'ibcore1 S207A')
//    0x0016 029 : (Channel Adapter portguid 0x001175000077f91e: 'hype356 qib0')
//    0x0017 026 : (Channel Adapter portguid 0x0011750000792d72: 'hype209 qib0')

    StringBuffer stringValue = new StringBuffer();
    
    for(Map.Entry<String, Integer> entry: PortGuidMap.entrySet())
    {
      stringValue.append(toPortRouteString(entry, table, fabric, true));
//      
//      Integer lid = entry.getValue();
//      stringValue.append("0x" + Integer.toHexString(lid) + " " + portNumber + " : (");
//      IB_Guid destGuid = new IB_Guid(entry.getKey());
//      if(isSwitch(destGuid, table))
//        stringValue.append("Switch");
//      else
//        stringValue.append("Channel Adapter");
//      stringValue.append(" portguid 0x" + destGuid + ": '" + fabric.getOSM_Node(destGuid).sbnNode.description + "')");
//      
//      stringValue.append("\n");
     }

     return stringValue.toString();
  }
  
  public String toPortRouteString(Map.Entry<String, Integer> entry, RT_Table table, OSM_Fabric fabric, boolean prependPortNum)
  {
    // this should match (as closely as possible) the output
    // of the command; ibroute -G switchguid
    //
    // 0x0012 033 : (Switch portguid 0x00066a00eb002cee: 'ibcore1 S201A')
//    0x0013 032 : (Switch portguid 0x00066a00eb002cf5: 'ibcore1 S207A')
//    0x0016 029 : (Channel Adapter portguid 0x001175000077f91e: 'hype356 qib0')
//    0x0017 026 : (Channel Adapter portguid 0x0011750000792d72: 'hype209 qib0')

    StringBuffer stringValue = new StringBuffer();
    
      Integer lid = entry.getValue();
      if(prependPortNum)
        stringValue.append(IB_Address.toLidHexString(lid) + " " + portNumber + " : (");
      else
        stringValue.append(IB_Address.toLidHexString(lid) + " : (");
        
      IB_Guid destGuid = new IB_Guid(entry.getKey());
      if(isSwitch(destGuid, table))
        stringValue.append("Switch");
      else
        stringValue.append("Channel Adapter");
      
      String name = fabric.getNameFromGuid(destGuid);
      stringValue.append(" portguid 0x" + destGuid + ": '" + name + "')");
      
      stringValue.append("\n");

     return stringValue.toString();
  }
  
  public static IB_Guid getDestinationGuid(Map.Entry<String, Integer> entry)
  {
    return new IB_Guid(entry.getKey());
  }
  
  public static String getDestinationName(Map.Entry<String, Integer> entry, OSM_Fabric fabric)
  {
    // if this port belongs to a switch, then guid is the switches guid
    // but if this port belongs to a CA, then subtract one, and use that guid
    IB_Guid g = getDestinationGuid(entry);
    
    String name = fabric.getNameFromGuid(g);
    if(name != null)
      return name;
    return fabric.getNameFromGuid(new IB_Guid(g.getGuid() - 1));
  }
  
  public static boolean isSwitch(IB_Guid destGuid, RT_Table table)
  {
    if(table != null)
      return table.isSwitch(destGuid);
    return false;
  }

  public int getNumRoutes()
  {
    if(PortGuidMap != null)
      return PortGuidMap.size();
    return 0;
  }

  public int getNumCaRoutes(RT_Table table)
  {
    // if its not a switch, then its a CA
    int total = 0;
    if(PortGuidMap != null)
    {
      for(Map.Entry<String, Integer> entry: PortGuidMap.entrySet())
        total += isSwitch(RT_Port.getDestinationGuid(entry), table) ? 0: 1;
     }
    return total;
  }
  
  public int getNumSwRoutes(RT_Table table)
  {
    // if its not a Channel Adapter, then its a Switch
    return getNumRoutes() - getNumCaRoutes(table);
  }
  
  public int getMinLid()
  {
    int min = 600000;
    
    for(Map.Entry<String, Integer> entry: PortGuidMap.entrySet())
    {
      Integer lid = entry.getValue();
      min = lid > min ? min: lid.intValue();
    }
    return min;
  }

  public int getMaxLid()
  {
    int max = 0;
    
    for(Map.Entry<String, Integer> entry: PortGuidMap.entrySet())
    {
      Integer lid = entry.getValue();
      max = lid < max ? max: lid.intValue();
    }
    return max;
  }
  
  

  /************************************************************
   * Method Name:
   *  getParentNode
   **/
  /**
   * Returns the value of parentNode
   *
   * @return the parentNode
   *
   ***********************************************************/
  
  public RT_Node getParentNode()
  {
    return parentNode;
  }

  /************************************************************
   * Method Name:
   *  getPortNumber
   **/
  /**
   * Returns the value of portNumber
   *
   * @return the portNumber
   *
   ***********************************************************/
  
  public int getPortNumber()
  {
    return portNumber;
  }

  /************************************************************
   * Method Name:
   *  getPortGuidMap
   **/
  /**
   * Returns the value of portGuidMap
   *
   * @return the portGuidMap
   *
   ***********************************************************/
  
  public HashMap<String, Integer> getPortGuidMap()
  {
    return PortGuidMap;
  }

  public boolean contains(IB_Guid srcGuid)
  {
    return PortGuidMap.containsKey(srcGuid.toColonString());
  }

  public boolean contains(int lid)
  {
    return GuidLidMap.containsKey(new Integer(lid));
  }

  /************************************************************
   * Method Name:
   *  compareTo
  **/
  /**
   * RT_Ports are considered to be the same, if their parent guids
   * and port numbers match.
   *
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   * @param   describe the parameters
   *
   * @return  describe the value returned
   ***********************************************************/
  
  @Override
  public int compareTo(RT_Port rtPort)
  {    
    // the Address is the only thing that MUST be unique
        //
    // both object must exist (and of the same class)
    // and should be consistent with equals
    //
    // -1 if less than
    // 0 if the same
    // 1 if greater than
    //
    if(rtPort == null)
            return -1;
    
    if(this.parentNode == null)
    {
      if(rtPort.getParentNode() == null)
        return 0;
      return 1;
    }
    
    if(rtPort.getParentNode().getGuid() == null)
      return -1;
    
    int result = this.parentNode.getGuid().compareTo(rtPort.getParentNode().getGuid());

    // if the guids are the same, compare the port numbers
    if(result == 0)
      result = this.portNumber - rtPort.getPortNumber();
    return result;
  }

  /************************************************************
   * Method Name:
   *  compareTo
  **/
  /**
   * RT_Ports are considered to be the same, if their parent guids
   * and port numbers match.
   *
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   * @param   describe the parameters
   *
   * @return  describe the value returned
   ***********************************************************/
  
  public int compareNumRoutes(RT_Port rtPort)
  {    
    // the Address is the only thing that MUST be unique
        //
    // both object must exist (and of the same class)
    // and should be consistent with equals
    //
    // -1 if less than
    // 0 if the same
    // 1 if greater than
    //
    if(rtPort == null)
            return -1;
    
    if(this.parentNode == null)
    {
      if(rtPort.getParentNode() == null)
        return 0;
      return 1;
    }
    
    if(rtPort.getParentNode().getGuid() == null)
      return -1;
    
    int result = this.parentNode.getGuid().compareTo(rtPort.getParentNode().getGuid());

    // if the guids are the same, compare the number of routes
    if(result == 0)     
      result = this.getNumRoutes() - rtPort.getNumRoutes();
    return result;
  }

  public String toPortRouteTreeString(Entry<String, Integer> entry, RT_Table table, OSM_Fabric fabric)
  {
    StringBuffer stringValue = new StringBuffer();
    String formatString = "lid: %5s, guid: %20s";
    
    IB_Guid destGuid = new IB_Guid(entry.getKey());
    Integer lid = entry.getValue();
    
      
    stringValue.append(String.format(formatString, IB_Address.toLidHexString(lid), destGuid.toColonString()));
//    stringValue.append("lid: " + IB_Address.toLidHexString(lid) + " guid: " + destGuid.toColonString());
      
    if(isSwitch(destGuid, table))
      stringValue.append(" - Switch");
    else
      stringValue.append(" - Channel Adapter");

   return stringValue.toString();
  }

  public String toNumRouteString(RT_Table table)
  {
    StringBuffer buff = new StringBuffer();
    String formatString = "CA routes=%3d, SW routes=%3d, total=%4d";
    buff.append(String.format(formatString, this.getNumCaRoutes(table), this.getNumSwRoutes(table), this.getNumRoutes()));
    return buff.toString();
  }

}
