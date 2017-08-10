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
 *        file: OSM_System.java
 *
 *  Created on: May 27, 2016
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Edge;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.SmtConstants;
import gov.llnl.lc.util.BinList;

/**********************************************************************
 * An OSM System describes a collection of IB Switches that have a
 * predetermined relationship, indicated by a common System GUID.
 * 
 * Normally, this is some form of "chassis" or core switch.
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version May 27, 2016 12:16:33 PM
 **********************************************************************/
public class OSM_System implements Serializable, CommonLogger, Comparable<OSM_System>
{
  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = 1041006543738586924L;

  private IB_Guid SysGuid;
  private OSM_Fabric Fabric;
  
  private ArrayList <IB_Guid> GuidList;
  private LinkedHashMap <String, IB_Vertex> VertexMap;
  
  private String Name;
  private int MaxDepth;
  private int MinDepth;
  
  private int TotalSwitches;
  private int TotalPorts;
  private int TotalActivePorts;
  
  private int ActiveInternalPorts;
  private int TotalExternalPorts;
  private int ActiveExternalPorts;
  
  /************************************************************
   * Method Name:
   *  OSM_System
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param sysGuid
   * @param fabric
   ***********************************************************/
  public OSM_System(IB_Guid sysGuid, OSM_Fabric fabric)
  {
    super();
    SysGuid = sysGuid;
    Fabric  = fabric;
    Name    = "Unknown";
    
    if((sysGuid == null) || (fabric == null))
      return;
    
    determineStructure(fabric);
  }
  
  public String toContent()
  {
    StringBuffer buff = new StringBuffer();
    if(isSystemGuid())
    {
      buff.append("This guid: " + SysGuid.toColonString() + " has " + GuidList.size() + " guids in it");
      buff.append("\n");
      buff.append(toString());
      buff.append("\n");
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
  
  @Override
  public String toString()
  {
    return "OSM_System [SysGuid=" + SysGuid + ", Fabric=" + Fabric + ", Name=" + Name
        + ", MaxDepth=" + MaxDepth + ", MinDepth=" + MinDepth + ", TotalSwitches=" + TotalSwitches
        + ", TotalPorts=" + TotalPorts + ", TotalActivePorts=" + TotalActivePorts
        + ", TotalInternalPorts=" + getTotalInternalPorts() + ", ActiveInternalPorts="
        + ActiveInternalPorts + ", InactiveInternalPorts=" + getInactiveInternalPorts()
        + ", TotalExternalPorts=" + TotalExternalPorts + ", ActiveExternalPorts="
        + ActiveExternalPorts + ", InactiveExternalPorts=" + getInactiveExternalPorts() + "]";
  }

  public boolean isSystemGuid()
  {
    return (GuidList != null) && (!GuidList.isEmpty());
  }
  

  public ArrayList<IB_Guid> getGuidList(int Depth)
  {
    // return the guids for this depth in the vertex map
    // assumes VertexMap exists
    
    MaxDepth = IB_Vertex.getMaxDepth(VertexMap);
    
    // is the requested depth legal?  (Max is the top level, and 1 is the bottom)
    if((Depth < MinDepth) || (Depth > MaxDepth))
      return null;
    
    LinkedHashMap <String, IB_Vertex> levelMap = IB_Vertex.getVertexMapAtDepth(VertexMap, Depth);
    ArrayList <IB_Guid> gList = new ArrayList<IB_Guid>();
    
    for (Entry<String, IB_Vertex> entry : levelMap.entrySet())
    {
      // just create an array of guids to return
      IB_Vertex v = entry.getValue();
      gList.add(v.getGuid());
    }
    return gList;
  }
  /************************************************************
   * Method Name:
   *  compareTo
  **/
  /**
   * Describe the method here
   *
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   *
   * @param o
   * @return
   ***********************************************************/
  
  @Override
  public int compareTo(OSM_System o)
  {
    // the SysGuid is the only thing that MUST be unique

   return SysGuid.compareTo(o.SysGuid);
  }

  /************************************************************
   * Method Name:
   *  determineStructure
  **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   * @param fabric2
   * @param vertexMap
   ***********************************************************/
  public boolean determineStructure(OSM_Fabric fabric)
  {
    MinDepth = 1;
    MaxDepth = MinDepth;

    // this may be a "new" copy of the fabric
    if(fabric == null)
      return false;
    
    fabric.createSystemGuidBins(false);

    BinList <IB_Guid> guidBins = fabric.getSystemGuidBins();
    if(guidBins.size() < 1)
      return false;
    
    // we have at least one core switch (a system guid associated with multiple node guids)
    Fabric = fabric;
    int k = 0;
    for(ArrayList <IB_Guid> gList: guidBins)
    {
      String ssGuid = guidBins.getKey(k);
      IB_Guid sGuid = new IB_Guid(ssGuid);
      if(SysGuid.equals(sGuid))
      {
        GuidList = gList;
        break;
      }
      k++;
    }
    
    // build the vertex map, which shows the structure
    LinkedHashMap<String, IB_Vertex> vertexMap = IB_Vertex.createVertexMap(fabric, GuidList);
    if((vertexMap == null) || (vertexMap.size() == 0))
    {
      logger.severe("The VetexMap for System " + SysGuid.toColonString() + " could not be built (no matching guids?)");
    }
    else
      VertexMap = vertexMap;
    
    // iterate through each node to find out the name of the "system"
    String sysName = "";
    for (Entry<String, IB_Vertex> entry : VertexMap.entrySet())
    {
      IB_Vertex v = entry.getValue();
      if((SysGuid.equals(new IB_Guid(v.getNode().sbnNode.sys_guid))))
        sysName = getCommonName(sysName, v.getName());
    }
    Name = sysName;
    
    // count the number of switches, and ports (sometimes core or chassis report an extra port)
    if((GuidList != null) && (GuidList.size() > 1))
    {
      TotalPorts = 0;
      TotalSwitches = GuidList.size();
      for(IB_Guid g: GuidList)
      {
        OSM_Node n = fabric.getOSM_Node(g);
        TotalPorts += n.pfmNode.getNum_ports();
      }
    }
    
    // iterate through each node to count up all the active ports (internal + external)
    TotalActivePorts = 0;
    for (Entry<String, IB_Vertex> entry : VertexMap.entrySet())
    {
      IB_Vertex v = entry.getValue();
      TotalActivePorts += v.getEdges().size();
    }
    
    // how many levels, or depths is this system
    MaxDepth = IB_Vertex.getMaxDepth(VertexMap);
    
    // find the minimum depth of the system (just one or two)
    LinkedHashMap <String, IB_Vertex> bottomLevel = IB_Vertex.getVertexMapAtDepth(VertexMap, MinDepth);
    if(bottomLevel.size() < 1)
      MinDepth++;
    
    // the bottom level contains internal and external ports
    bottomLevel = IB_Vertex.getVertexMapAtDepth(VertexMap, MinDepth);
    
    ActiveExternalPorts = 0;      // edges that have a depth lower than the minimum (only from bottom level)
    int blActiveInternalPorts = 0;  // bottom level ports pointing "in" or internal
    int tot_p = 0;                // total bottom level ports
    int tot_ap = 0;               // total bottom level "active" ports
    for (Entry<String, IB_Vertex> entry : bottomLevel.entrySet())
    {
      IB_Vertex v = entry.getValue();
      
      tot_p += v.getNumPorts();
      
      // iterate through the edges of this vertex and count only its external ports
      ArrayList<IB_Edge> el = v.getEdges();
      tot_ap += el.size();
      
      // if associated with an edge, then it is active (add up the ones at the lowest which will be external)
      for(IB_Edge e: el)
      {
        if(e.getDepth() < MinDepth)
          ActiveExternalPorts++;
        else
          blActiveInternalPorts++;
      }
    }
 
    // total external ports is total ports minus internal ports
    TotalExternalPorts = tot_p - blActiveInternalPorts;
    int inactiveInternalPorts = getTotalInactivePorts() - getInactiveExternalPorts();
    
    ActiveInternalPorts = getTotalInternalPorts() - inactiveInternalPorts;
    
    // Assume all Internal ports are active (FIXME: bad assumption) therefore
    // remaining ports must be "inactive" external ports    
    
//    System.err.println("**************** Total Ports[" + TotalPorts + "]");
//    System.err.println("**************** Total Active Ports[" + TotalActivePorts + "]");
//    System.err.println("**************** Total Internal Ports[" + getTotalInternalPorts() + "]");
//    System.err.println("**************** Total Active Internal Ports[" + ActiveInternalPorts + "]");
//    System.err.println("**************** Total Bottom Level System Ports v [" + tot_p + "]");
//    System.err.println("**************** Total Active Bottom Level System Ports v [" + tot_ap + "]");
//    System.err.println("**************** Active Internal Ports n [" + ActiveInternalPorts + "]");
//    System.err.println("**************** Active External Ports n [" + ActiveExternalPorts + "]");
//    System.err.println("**************** Inactive Internal Ports n [" + getInactiveInternalPorts() + "]");
//    System.err.println("**************** Inactive External Ports n [" + getInactiveExternalPorts() + "]");
//    System.err.println("**************** Total External Ports n [" + TotalExternalPorts + "]");
    return true;
  }
  
  public static String getCommonName(String name1, String name2)
  {
    int minSame = 3;
    
    // case insensitive, but return lower case
    // strip spaces and special characters from end
    
    // handle special first case, when don't have two to compare
    if((name1 == null) || (name1.length() < minSame))
      return name2;
    
    // compare the two strings, using the first one as a template
    // if the first string is a subset of the second, only include the common parts
    //
    String n1 = name1 == null ? "RootNode": name1.toLowerCase().trim();
    String n2 = name2 == null ? "RootNode": name2.toLowerCase().trim();
    String common = "";
    
    // compare the two strings, stop at the first difference
    for(int i=0; i< n1.length(); i++)
    {
      // the characters and positions have to match
      if(n1.charAt(i) == n2.charAt(i))
        common += n1.charAt(i);
      else
        break;
    }
    
    // if they have nothing in common, then just return the original (first) name
    
    if(common.length() >= minSame)
    {
      // don't allow backslashes, colons or semi-colons at the end
      String match = common.trim();
      int len = match.length();
      if(match.endsWith(":") || match.endsWith(";") || match.endsWith("/") || match.endsWith("."))
        return match.substring(0, len-1);
      return match;
    }
    return name1;
  }

  /************************************************************
   * Method Name:
   *  getSysGuid
   **/
  /**
   * Returns the value of sysGuid
   *
   * @return the sysGuid
   *
   ***********************************************************/
  
  public IB_Guid getSysGuid()
  {
    return SysGuid;
  }

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
   *  getGuidList
   **/
  /**
   * Returns the value of guidList
   *
   * @return the guidList
   *
   ***********************************************************/
  
  public ArrayList<IB_Guid> getGuidList()
  {
    return GuidList;
  }
  
  
  public static java.util.ArrayList <OSM_System> getArrayOfSystems(OSM_Fabric fabric)
  {
    ArrayList <OSM_System> sysArray = new ArrayList<OSM_System>();

        // assume nothing
      if(fabric == null)
        return null;
      
      if(!fabric.isInitialized())
      {
        System.err.println("Fabric not initialized???");
        
      }
      
      fabric.createSystemGuidBins(false);
      BinList <IB_Guid> guidBins = fabric.getSystemGuidBins();
      
      int k = 0;
      
      for(ArrayList <IB_Guid> gList: guidBins)
      {
        String ssGuid = guidBins.getKey(k);
        IB_Guid sGuid = new IB_Guid(ssGuid);
        OSM_System sys = new OSM_System(sGuid, fabric);
        sysArray.add(sys);
        k++;
      }
      
    return forceUniqueNames(sysArray);
  }
  
  private static java.util.ArrayList <OSM_System> forceUniqueNames(java.util.ArrayList <OSM_System> sysArray)
  {
    if((sysArray == null) || (sysArray.isEmpty()))
      return null;

    // force the system names to be unique
    int same = 0;
    String postFix = "ABCDEFG";
    for(OSM_System s1: sysArray)
    {
      String n1 = s1.getName();
      for(OSM_System s2: sysArray)
      {
        String n2 = s2.getName();
        if(!(s1.equals(s2)) && (n1.equals(n2)))
        {
          s2.setName(n2 + "-" + postFix.charAt(same)); 
          same++;
        }
      }
    }
    return sysArray;
  }
  
  public static OSM_System getOSM_System(ArrayList <OSM_System> sysArray, IB_Guid sysGuid)
  {
    OSM_System sys = null;
    
    // find the OSM_System, and show the levels
    if((sysArray != null) && (sysGuid != null))
    {
      for(OSM_System s: sysArray)
      {
        if(s.getSysGuid().equals(sysGuid))
        {
          // found it
          sys = s;
        }
      }
    }
    return sys;
   }
  
  public static OSM_System getOSM_System(OSM_Fabric fabric, IB_Guid sysGuid)
  {
    java.util.ArrayList <OSM_System> sysArray =  getArrayOfSystems(fabric);
    
    if((sysArray == null) || (sysArray.isEmpty()))
      return null;
    
    return OSM_System.getOSM_System(sysArray, sysGuid);
   }

  /************************************************************
   * Method Name:
   *  getVertexMap
   **/
  /**
   * Returns the value of vertexMap
   *
   * @return the vertexMap
   *
   ***********************************************************/
  
  public LinkedHashMap<String, IB_Vertex> getVertexMap()
  {
    return VertexMap;
  }

  /************************************************************
   * Method Name:
   *  getName
   **/
  /**
   * Returns the value of name
   *
   * @return the name
   *
   ***********************************************************/
  
  public String getName()
  {
    return Name;
  }

  public void setName(String name)
  {
    Name = name;
  }

  /************************************************************
   * Method Name:
   *  getMaxDepth
   **/
  /**
   * Returns the value of maxDepth
   *
   * @return the maxDepth
   *
   ***********************************************************/
  
  public int getMaxDepth()
  {
    return MaxDepth;
  }

  /************************************************************
   * Method Name:
   *  getMinDepth
   **/
  /**
   * Returns the value of minDepth
   *
   * @return the minDepth
   *
   ***********************************************************/
  
  public int getMinDepth()
  {
    return MinDepth;
  }

  /************************************************************
   * Method Name:
   *  getTotalSwitches
   **/
  /**
   * Returns the value of totalSwitches
   *
   * @return the totalSwitches
   *
   ***********************************************************/
  
  public int getTotalSwitches()
  {
    return TotalSwitches;
  }

  /************************************************************
   * Method Name:
   *  getTotalPorts
   **/
  /**
   * Returns the value of totalPorts
   *
   * @return the totalPorts
   *
   ***********************************************************/
  
  public int getTotalPorts()
  {
    return TotalPorts;
  }

  /************************************************************
   * Method Name:
   *  getTotalActivePorts
   **/
  /**
   * Returns the value of totalActivePorts
   *
   * @return the totalActivePorts
   *
   ***********************************************************/
  
  public int getTotalActivePorts()
  {
    return TotalActivePorts;
  }

  /************************************************************
   * Method Name:
   *  getTotalInactivePorts
   **/
  /**
   * Returns the value of totalActivePorts
   *
   * @return the totalActivePorts
   *
   ***********************************************************/
  
  public int getTotalInactivePorts()
  {
    return TotalPorts - TotalActivePorts;
  }

  /************************************************************
   * Method Name:
   *  getTotalInternalPorts
   **/
  /**
   * Returns the value of totalInternalPorts
   *
   * @return the totalInternalPorts
   *
   ***********************************************************/
  
  public int getTotalInternalPorts()
  {
    return TotalPorts - TotalExternalPorts;
  }

  /************************************************************
   * Method Name:
   *  getActiveInternalPorts
   **/
  /**
   * Returns the value of activeInternalPorts
   *
   * @return the activeInternalPorts
   *
   ***********************************************************/
  
  public int getActiveInternalPorts()
  {
    return ActiveInternalPorts;
  }

  /************************************************************
   * Method Name:
   *  getInactiveInternalPorts
   **/
  /**
   * Returns the value of inactiveInternalPorts
   *
   * @return the inactiveInternalPorts
   *
   ***********************************************************/
  
  public int getInactiveInternalPorts()
  {
    return getTotalInternalPorts() - ActiveInternalPorts;
  }

  /************************************************************
   * Method Name:
   *  getTotalExternalPorts
   **/
  /**
   * Returns the value of totalExternalPorts
   *
   * @return the totalExternalPorts
   *
   ***********************************************************/
  
  public int getTotalExternalPorts()
  {
    return TotalExternalPorts;
  }

  /************************************************************
   * Method Name:
   *  getActiveExternalPorts
   **/
  /**
   * Returns the value of activeExternalPorts
   *
   * @return the activeExternalPorts
   *
   ***********************************************************/
  
  public int getActiveExternalPorts()
  {
    return ActiveExternalPorts;
  }

  /************************************************************
   * Method Name:
   *  getInactiveExternalPorts
   **/
  /**
   * Returns the value of inactiveExternalPorts
   *
   * @return the inactiveExternalPorts
   *
   ***********************************************************/
  
  public int getInactiveExternalPorts()
  {
    return TotalExternalPorts - ActiveExternalPorts;
  }

  /************************************************************
   * Method Name:
   *  toLevels
  **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   * @return
   ***********************************************************/
  public String toLevels()
  {
    // A collection of switches are organized in a tree hierarchy, there
    // may or may not be a single root top level node.
    //
    // ports from those switches are either connected;
    //  up to higher level switches
    //  down to lower level switches
    //  down and out of the system, available for use
    
    StringBuffer buff = new StringBuffer();
    
    LinkedHashMap<String, IB_Vertex> vMap = getVertexMap();
    int maxDepth = getMaxDepth();
    int minDepth = getMinDepth();
    
    buff.append("  level         guid          up ports   down ports   total" + SmtConstants.NEW_LINE);
    String format = "   %2d   %20s    %3d        %3d         %3d";
    
    for(int d = maxDepth; d>= minDepth; d--)
    {
      LinkedHashMap <String, IB_Vertex> level = IB_Vertex.getVertexMapAtDepth(vMap, d);
      for (Entry<String, IB_Vertex> entry : level.entrySet())
      {
        IB_Vertex v = entry.getValue();
        ArrayList<IB_Edge> el = v.getEdges();
        int upPorts   = 0;
        int downPorts = 0;
        
        // if associated with an edge, then it is active (add up the ones at the lowest which will be external)
        for(IB_Edge e: el)
        {
           if(e.getDepth() < v.getDepth())
             downPorts++;
           else
             upPorts++;
        }
        buff.append(String.format(format, v.getDepth(), v.getGuid().toColonString(), upPorts, downPorts, v.getNumPorts()) + SmtConstants.NEW_LINE);
     }    
    }
    return buff.toString();
  }

}
