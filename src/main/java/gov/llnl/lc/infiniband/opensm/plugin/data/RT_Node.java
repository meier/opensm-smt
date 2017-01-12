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
 *        file: RT_Node.java
 *
 *  Created on: May 5, 2014
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.data;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import gov.llnl.lc.infiniband.core.IB_Address;
import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.opensm.plugin.utils.IB_RouteParser;

/**********************************************************************
 * This represents the routing table for a switch node.  Every switch
 * node maintains its own information for how to route to every node
 * (CA and SW) in the fabric.
 * 
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version May 5, 2014 8:49:23 AM
 **********************************************************************/
public class RT_Node implements Serializable, gov.llnl.lc.logging.CommonLogger, Comparable<RT_Node>
{
  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = 1078019847590331759L;
  private RT_Table parentTable;
  private IB_Guid guid;
  private int lid;
  
  private OSM_Fabric Fabric;
  
  /* the routing table for this node, organized per port 
   *   the key will be the port number, and the value is the RT_Port
   *   
   *   */
  private LinkedHashMap<String, RT_Port> PortRouteMap = new LinkedHashMap<String, RT_Port>();
  
  public RT_Node(int lid, IB_Guid guid)
  {
    this.lid = lid;
    this.guid = guid;
  }

  public RT_Node(String fileName)
  {
    RT_Node tmp = RT_Node.readFromTextFile(fileName);
    if(tmp != null)
    {
      // clone
      tmp.copy(this);
    }
   }

  public RT_Node(SBN_Switch sw, OSM_Fabric fab)
  {
    // this contains the Linear Forwarding Table
    if((sw != null) && (sw.lft != null) && (sw.lft.length > 0) && (fab != null))
    {
      // build the node from the switch
      // output port number
      // destination lid
      // destination guid
        guid = new IB_Guid(sw.guid);
        lid  = fab.getLidFromGuid(guid);
        Fabric = fab;
      
        int dlid = 0;
        for(short pn: sw.lft)
        {
          if(pn < 255)
          {
 //           System.out.println("Guid: " + guid.toColonString() + ", pn: " + pn + ", lid: " + dlid);
            IB_Guid dguid = fab.getGuidFromLid(dlid);
            add((int)pn, dlid, dguid );
          }
          dlid++;
         }
       }
    }

  public boolean copy(RT_Node rtNode)
  {
    // copy constructor
    if(rtNode == null)
      return false;
    
    // clone
    rtNode.guid = this.guid;
    rtNode.lid = this.lid;
    rtNode.Fabric = this.Fabric;
    rtNode.PortRouteMap = this.PortRouteMap;
    return true;
   }

  public static RT_Node readFromTextFile(String fileName)
  {
    // the filename should be the output of the 
    // ibroute -G 0x000guidhere000 command
    
    IB_RouteParser parser = new IB_RouteParser();
    try
    {
      parser.parseFile(new File(fileName));
    }
    catch (IOException e)
    {
      logger.severe("Parse exception: " + e.getMessage());
    }
    RT_Node tmp = parser.getRT_node();
    
    if(tmp != null)
      return tmp;
    return null;
   }

  public void add(int portNum, int lid, IB_Guid destGuid)
  {
    // if portNum is ZERO, ignore it, its stupid
    if(portNum < 1)
      return;
    
    // get the map for this port number, or create it if it doesn't exist
    RT_Port rp = PortRouteMap.get(Integer.toString(portNum));
    if(rp == null)
    {
       rp = new RT_Port(this, portNum, lid, destGuid);
      PortRouteMap.put(Integer.toString(portNum), rp);
    }
    else
    {
      rp.add(lid, destGuid);
    }
  }
  
  public int getNumRoutes()
  {
    int total = 0;
    if(PortRouteMap != null)
    {
      for(Map.Entry<String, RT_Port> entry: PortRouteMap.entrySet())
      {
        RT_Port rp = entry.getValue();
        total += rp.getNumRoutes();
      }
    }
    return total;
  }
  
  /************************************************************
   * Method Name:
   *  getParentTable
   **/
  /**
   * Returns the value of parentTable
   *
   * @return the parentTable
   *
   ***********************************************************/
  
  public RT_Table getParentTable()
  {
    return parentTable;
  }

  /************************************************************
   * Method Name:
   *  setParentTable
   **/
  /**
   * Sets the value of parentTable
   *
   * @param parentTable the parentTable to set
   *
   ***********************************************************/
  public void setParentTable(RT_Table parentTable)
  {
    this.parentTable = parentTable;
  }

  public int getNumCaRoutes(RT_Table table)
  {
    int total = 0;
    if(PortRouteMap != null)
    {
      for(Map.Entry<String, RT_Port> entry: PortRouteMap.entrySet())
      {
        RT_Port rp = entry.getValue();
        total += rp.getNumCaRoutes(table);
      }
    }
    return total;
  }
  
  public int getNumSwRoutes(RT_Table table)
  {
    int total = 0;
    if(PortRouteMap != null)
    {
      for(Map.Entry<String, RT_Port> entry: PortRouteMap.entrySet())
      {
        RT_Port rp = entry.getValue();
        total += rp.getNumSwRoutes(table);
      }
    }
    return total;
  }
  
  public boolean contains(IB_Guid source)
  {
    // return true if this guid is anywhere in the Port Route Map
    if(PortRouteMap != null)
    {
      for(Map.Entry<String, RT_Port> entry: PortRouteMap.entrySet())
      {
        RT_Port rp = entry.getValue();
        if(rp.contains(source))
          return true;
      }
    }
    return false;
  }
  
  public boolean contains(int lid)
  {
    // return true if this lid is anywhere in the Port Route Map
    if(PortRouteMap != null)
    {
      for(Map.Entry<String, RT_Port> entry: PortRouteMap.entrySet())
      {
        RT_Port rp = entry.getValue();
        if(rp.contains(lid))
          return true;
      }
    }
    return false;
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
  
   public String toLongString()
  {
    StringBuffer buff = new StringBuffer();
    
    if(Fabric != null)
      buff.append(getFullName(Fabric) + "\n");
    else
      buff.append(toString() + "\n");
    
    if(PortRouteMap != null)
    {
      for(Map.Entry<String, RT_Port> entry: PortRouteMap.entrySet())
      {
        RT_Port rp = entry.getValue();
        buff.append(rp.toLongString() + "\n");
      }
    }
    return buff.toString();
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
   
   public String toIB_RouteString(RT_Table table, OSM_Fabric fabric)
   {
     // this should match (as closely as possible) the output
     // of the command; ibroute -G switchguid
     //
     StringBuffer stringValue = new StringBuffer();
     
//     Unicast lids [0x0-0x16f] of switch Lid 17 guid 0x00066a00ec002eec (ibcore1 L109):
//       Lid  Out   Destination
//            Port     Info 
//     0x0001 028 : (Channel Adapter portguid 0x001175000077ed40: 'hype355 qib0')
//     0x0002 026 : (Switch portguid 0x00066a00ec002d00: 'ibcore1 L113')
//     0x0003 026 : (Switch portguid 0x00066a00ec003003: 'ibcore1 L107')
//     0x0004 026 : (Switch portguid 0x00066a00ec003004: 'ibcore1 L103')
//     0x0005 026 : (Switch portguid 0x00066a00ec002d09: 'ibcore1 L105')
//     0x0016 029 : (Channel Adapter portguid 0x001175000077f91e: 'hype356 qib0')
//     0x00b8 026 : (Switch portguid 0x00066a01e8001313: 'ibcore1 L102')
//     ...
//     0x00b9 026 : (Switch portguid 0x00066a00e3003b62: 'QLogic 12200 GUID=0x00066a00e3003b62')
//     0x0119 019 : (Channel Adapter portguid 0x001175000077a75a: 'hype202 qib0')
//     0x0148 029 : (Channel Adapter portguid 0x00117500007735f2: 'hype229 qib0')
//     171 valid lids dumped 

     
     stringValue.append(getHeader(table, fabric) + "\n");
     stringValue.append(getPortString(table, fabric));
     stringValue.append(getFooterString());
     
     return stringValue.toString();
   }
   
   private String getFooterString()
  {
    return getNumRoutes() + " valid lids dumped";
  }

  private String getPortString(RT_Table table, OSM_Fabric fabric)
  {
    StringBuffer stringValue = new StringBuffer();

    if(PortRouteMap != null)
    {
      for(Map.Entry<String, RT_Port> entry: PortRouteMap.entrySet())
      {
        RT_Port rp = entry.getValue();
        stringValue.append(rp.toIB_RouteString(table, fabric));
      }
    }
    return stringValue.toString();
  }

  private String getHeader(RT_Table table, OSM_Fabric fabric)
  {
    StringBuffer stringValue = new StringBuffer();
//  Unicast lids [0x0-0x16f] of switch Lid 17 guid 0x00066a00ec002eec (ibcore1 L109):
//  Lid  Out   Destination
//       Port     Info 
    stringValue.append(table.getTableType().getTypeName() + " lids [0x");
    stringValue.append(Integer.toHexString(getMinLid()) + "-0x" + Integer.toHexString(getMaxLid()));
    stringValue.append("] of switch Lid " + lid + " guid 0x" + guid + " (" + getName(fabric) + "):\n");
    stringValue.append("Lid  Out  Destination\n");
    stringValue.append("     Port    Info");
    return stringValue.toString();
  }

  public String getName(OSM_Fabric fabric)
  {
    setFabric(fabric);
    
    // return the name of this switch
    return fabric.getNameFromGuid(guid);
  }

  public String getFullName(OSM_Fabric fabric)
  {
    setFabric(fabric);
    
    // return the name, guid, and lid of this switch
    return toString(fabric);
  }

  public void setFabric(OSM_Fabric fabric)
  {
    Fabric = fabric;
  }

  /************************************************************
   * Method Name:
   *  getGuid
   **/
  /**
   * Returns the value of guid
   *
   * @return the guid
   *
   ***********************************************************/
  
  public IB_Guid getGuid()
  {
    return guid;
  }

  /************************************************************
   * Method Name:
   *  getLid
   **/
  /**
   * Returns the value of lid
   *
   * @return the lid
   *
   ***********************************************************/
  
  public int getLid()
  {
    return lid;
  }

  /************************************************************
   * Method Name:
   *  getPortRouteMap
   **/
  /**
   * Returns the value of portRouteMap
   *
   * @return the portRouteMap
   *
   ***********************************************************/
  
  public LinkedHashMap<String, RT_Port> getPortRouteMap()
  {
    return PortRouteMap;
  }
  
  
  public static class SortPortMapByPortNum implements Comparator <Map.Entry<String, RT_Port>>
  {
    // sort by port
    public int compare(Entry<String, RT_Port> p1, Entry<String, RT_Port> p2)
    {
      return p1.getValue().compareTo(p2.getValue());
    }
  }
  
  public static class SortPortMapByRoutes implements Comparator <Map.Entry<String, RT_Port>>
  {
    // sort by number of routes (small to large)
    public int compare(Entry<String, RT_Port> p1, Entry<String, RT_Port> p2)
    {
      return p1.getValue().compareNumRoutes(p2.getValue());
    }
  }
  
  public static LinkedHashMap <String, RT_Port > sortPortRouteTable(LinkedHashMap <String, RT_Port > portRouteMap, boolean byPortNum)
  {
    // create a new sorted version of this map.  Sort by Name if true, otherwise by depth
   LinkedHashMap<String, RT_Port> pMap = new LinkedHashMap<String, RT_Port>();
   
   if((portRouteMap != null) && (portRouteMap.entrySet() != null))
   {   
   List<Map.Entry<String, RT_Port>> entries = new LinkedList<Map.Entry<String, RT_Port>>(portRouteMap.entrySet());
   
   // sort by portNumber, or by number of routes
   if(byPortNum)
     Collections.sort(entries, new SortPortMapByPortNum());
   else
   {
     Collections.sort(entries, new SortPortMapByPortNum());
     Collections.sort(entries, new SortPortMapByRoutes());
   }
     
   for (Entry<String, RT_Port> entry : entries)
   {
       // create a new (sorted) hashmap
     RT_Port v = entry.getValue();
     pMap.put(entry.getKey(), v);
   }
   }
   return pMap;
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
    /* every guid has a lid, but the lid can change, only the guid is guaranteed to remain constant
     *   the key will be the guid, and the value is the lid
     */
    HashMap<String, Integer> LidGuidMap = new HashMap<String, Integer>();

    if(PortRouteMap != null)
    {
      for(Map.Entry<String, RT_Port> entry: PortRouteMap.entrySet())
      {
        RT_Port rp = entry.getValue();
        LidGuidMap.putAll(rp.getLidGuidMap());
      }
    }
    return LidGuidMap;
  }

  public HashMap<Integer, IB_Guid> getGuidLidMap()
  {
    /* every guid has at least one lid, but can have multiple if LMC is non-zero
     */
    HashMap<Integer, IB_Guid> GuidLidMap = new HashMap<Integer, IB_Guid>();

    if(PortRouteMap != null)
    {
      for(Map.Entry<String, RT_Port> entry: PortRouteMap.entrySet())
      {
        RT_Port rp = entry.getValue();
        GuidLidMap.putAll(rp.getGuidLidMap());
      }
    }
    return GuidLidMap;
  }

  
  public int getMinLid()
  {
    // not sure if this is supposed to be the min in this node, or in the whole table
    int min = 60000;
    if(PortRouteMap != null)
    {
      for(Map.Entry<String, RT_Port> entry: PortRouteMap.entrySet())
      {
        RT_Port rp = entry.getValue();
        int pMin = rp.getMinLid();
        min = pMin > min ? min: pMin;
      }
    }
    return min;
  }

  public int getMaxLid()
  {
    // not sure if this is supposed to be the max in this node, or in the whole table
    int max = 0;
    if(PortRouteMap != null)
    {
      for(Map.Entry<String, RT_Port> entry: PortRouteMap.entrySet())
      {
        RT_Port rp = entry.getValue();
        int pMax = rp.getMaxLid();
        max = pMax < max ? max: pMax;
      }
    }
    return max;
  }

  public int getMinRoutes()
  {
    // not sure if this is supposed to be the min in this node, or in the whole table
    int min = 60000;
    if(PortRouteMap != null)
    {
      for(Map.Entry<String, RT_Port> entry: PortRouteMap.entrySet())
      {
        RT_Port rp = entry.getValue();
        int pMin = rp.getNumRoutes();
        min = pMin > min ? min: pMin;
      }
    }
    return min;
  }

  public int getAveRoutes()
  {
    int tot = 0;
    if(PortRouteMap != null)
    {
      for(Map.Entry<String, RT_Port> entry: PortRouteMap.entrySet())
      {
        RT_Port rp = entry.getValue();
        tot += rp.getNumRoutes();
      }
    }
    return tot/PortRouteMap.size();
  }

  public int getMaxRoutes()
  {
    // not sure if this is supposed to be the max in this node, or in the whole table
    int max = 0;
    if(PortRouteMap != null)
    {
      for(Map.Entry<String, RT_Port> entry: PortRouteMap.entrySet())
      {
        RT_Port rp = entry.getValue();
        int pMax = rp.getNumRoutes();
        max = pMax < max ? max: pMax;
      }
    }
    return max;
  }

  public static String getHeaderString(OSM_Fabric fab)
  {
    String name = fab == null ? "": "   ( name )";
    return "lid        switch guid      # ports with routes " + name;
  }

  public static String getHeaderString()
  {
    return getHeaderString(null);
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
     if(Fabric != null)
       return getName(Fabric);
     return toString(null);
   }
   
   public String toString(OSM_Fabric fab)
   {
     StringBuffer buff = new StringBuffer();
     String name = fab == null ? "": getName(fab);
     String formatString = "%-5s %20s           %2d         ( %s )";
     buff.append(String.format(formatString,  IB_Address.toLidHexString(lid), guid.toColonString(), PortRouteMap.size(), name  )); 
     return buff.toString();
   }
   
  /************************************************************
   * Method Name:
   *  main
   **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects

   * @param   describe the parameters
   *
   * @return  describe the value returned
   * @param args
   ***********************************************************/
  public static void main(String[] args)
  {
    IB_RouteParser parser = new IB_RouteParser();
    String fileName = "/home/meier3/.smt/cache/routes/route_results.txt";
    
    try
    {
      parser.parseFile(new File(fileName));
    }
    catch (IOException e)
    {
      System.out.println("Parse exception: " + e.getMessage());
    }
    
    System.out.println(parser.getSummary());    
    System.out.println(parser.getRT_node().toLongString());    
  }
  

  public RT_Port getPortToDestination(IB_Guid destination)
  {
    // return the port that contains this destination 
    if(PortRouteMap != null)
    {
      for(Map.Entry<String, RT_Port> entry: PortRouteMap.entrySet())
      {
        RT_Port rp = entry.getValue();
        if(rp.contains(destination))
          return rp;
      }
    }
    return null;
  }
  
  /************************************************************
   * Method Name:
   *  getRT_Port
  **/
  /**
   * This RT_Node has a PortRouteMap, which contains a list of
   * routes for each port number. 
   *
   * @see     describe related java objects
   *
   * @param pNum  the port number
   * @return      the RT_Port for the supplied port number.
   ***********************************************************/
  public RT_Port getRT_Port(int pNum)
  {
    if(PortRouteMap != null)
    {
     return PortRouteMap.get(Integer.toString(pNum));
    }
    return null;
  }


  /************************************************************
   * Method Name:
   *  getLid
  **/
  /**
   * Returns the LID withing the table for the supplied destination
   * guid.  If the guid is not in the table, -1 is returned.
   *
   * @see     describe related java objects
   *
   * @param destination
   * @return
   ***********************************************************/
  public int getLid(IB_Guid destination)
  {
    // return the port that contains this destination 
    if(PortRouteMap != null)
    {
      for(Map.Entry<String, RT_Port> entry: PortRouteMap.entrySet())
      {
        RT_Port rp = entry.getValue();
        if(rp.contains(destination))
          return rp.getLidForGuid(destination);
      }
    }
    return -1;
  }
  
  public ArrayList<Integer> getLids(IB_Guid destGuid)
  {
    // return all the lids for this guid (no duplicates)
    HashSet<Integer> lidSet = new HashSet<Integer>();

    if(PortRouteMap != null)
    {
      for(Map.Entry<String, RT_Port> entry: PortRouteMap.entrySet())
      {
        RT_Port rp = entry.getValue();
        if(rp.contains(destGuid))
          lidSet.addAll(rp.getLidsForGuid(destGuid));
      }
    }
    return new ArrayList<Integer>(Arrays.asList(lidSet.toArray(new Integer[0])));
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
  public int compareTo(RT_Node rtNode)
  {    
    // the guid is the only thing that MUST be unique
        //
    // both object must exist (and of the same class)
    // and should be consistent with equals
    //
    // -1 if less than
    // 0 if the same
    // 1 if greater than
    //
    if(rtNode == null)
            return -1;
    
    if(rtNode.getGuid() == null)
      return -1;
    
    int result = this.getGuid().compareTo(rtNode.getGuid());

    // if the guids are the same, compare the number of routes
    if(result == 0)
      result = this.getNumRoutes() - rtNode.getNumRoutes();
    return result;
  }

  public static String getMapString(HashMap<String, RT_Node> switchGuidMap)
  {
    // iterate through the map, and return the long version
    if(switchGuidMap == null)
      return null;
    
    StringBuffer buff = new StringBuffer();
    
    for(RT_Node n: switchGuidMap.values())
      buff.append(n.toLongString());
      
    return buff.toString();
  }



}
