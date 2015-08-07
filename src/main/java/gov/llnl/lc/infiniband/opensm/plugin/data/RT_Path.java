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
 *        file: RT_Path.java
 *
 *  Created on: May 6, 2014
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.data;

import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.logging.CommonLogger;

import java.util.ArrayList;

/**********************************************************************
 * Describe purpose and responsibility of RT_Path
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version May 6, 2014 10:34:45 AM
 **********************************************************************/
public class RT_Path implements Comparable<RT_Path>, CommonLogger
{
  private IB_Guid Source;
  private IB_Guid Destination;
  
  private RT_Table RtTable;
  private OSM_Fabric Fabric;
  
  // an ordered list of path legs, that make up the path
  private ArrayList<RT_PathLeg> Legs = new ArrayList<RT_PathLeg>();


  /************************************************************
   * Method Name:
   *  RT_Path
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param source
   * @param destination
   ***********************************************************/
  public RT_Path(IB_Guid source, IB_Guid destination)
  {
    this(source, destination, null);
  }

  /************************************************************
   * Method Name:
   *  RT_Path
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param source
   * @param destination
   ***********************************************************/
  public RT_Path(IB_Guid source, IB_Guid destination, RT_Table table)
  {
    this(source, destination, table, null);
  }

  public RT_Path(IB_Guid source, IB_Guid destination, RT_Table table, OSM_Fabric fabric)
  {
    super();
    init(source, destination, table, fabric);
  }


  private void init(IB_Guid source, IB_Guid destination, RT_Table table, OSM_Fabric fabric)
  {
    Source = source;
    Destination = destination;
    if((table != null) && (source != null) && (destination != null) && (fabric != null))
    {
      RtTable = table;
      Fabric  = fabric;
      
      // conditionally adjust the Path, if the table doesn't contain the src/dst
      if(!table.contains(source) && !fabric.isSwitchGuid(source))
      {
        // adjust this guid, only if its a valid port guid
        IB_Guid ns = new IB_Guid(source.getGuid()+1);
        if(fabric.isUniquePortGuid(ns))
           Source = ns;         
      }

      if(!table.contains(destination) && !fabric.isSwitchGuid(destination))
      {
        // adjust this guid, only if its a valid port guid
        IB_Guid ns = new IB_Guid(destination.getGuid()+1);
        if(fabric.isUniquePortGuid(ns))
           Destination = ns;         
      }

      // quick check to see if the source and destination exist in the table
      if((table.contains(Source)) && (table.contains(Destination)))
      {
        // I "should" be able to create a path between these two
        buildPath(table, fabric);
      }
      else
      {
        logger.severe("Table does not contain both guids");
        logger.severe("src: " + Source.toColonString() + ", dst: " + Destination.toColonString());
        
        // sometimes a destination port guid is different than its node (no need, just different vendors)
        // if a src or destination is CA, then its port guid may be different than the node guid
        System.out.println("Is Src a switch?: " + fabric.isSwitchGuid(Source));
        System.out.println("Is Dst a switch?: " + fabric.isSwitchGuid(Destination));
        System.out.println("Table contains Src?: " + table.contains(Source));
        System.out.println("Table contains Dst?: " + table.contains(destination));
      }     
    }
  }

  private void buildPath(RT_Table table, OSM_Fabric fabric)
  {
    // start at the source, who is it connected to?
    
    // if this is a switch, I can just look into its own table to
    //    start looking for a path to the destination
    // but if this is a CA, then I need to find the switch it is connected
    //    to (out of the CA's port 1) to THEN look at that switches table
    
    // this will be the first, and re-used pathleg
    RT_PathLeg pathLeg = null;
    
    if(table.isSwitch(Source))
    {
      // the first leg can be obtained directly from the switchmap
      pathLeg = findNextPathLeg(Source, table, fabric);
    }
    else
    {
      // its a channel adapter, so look out port 1 to find its switch
      OSM_Port p = null;
      
      // some fabrics have port guids, which are different than their parent node
      //  if this is one of those, handle it
      if(fabric.isUniquePortGuid(Source))
        p = getOSM_Port(Source.getGuid(), fabric);
      else
        p = getOSM_Port(Source.getGuid(), (short) 1, fabric);
      
      if(p != null)
      {
        // the source is a CA, so the port connected to this
        // CA belongs to a switch
          OSM_Port lp = getLinkedOSM_Port(p, fabric);
          if(lp != null)
          {
            // I have both sides of the link, or path, so can create it
            pathLeg = new RT_PathLeg(p, lp, this);
           }
       }
      else
      {
        System.err.println("I did not find the src guid in the fabric! " + Source.toColonString());
      }
    }
    
    if(pathLeg == null)
    {
      // should never get here!
      System.err.println("The initial path leg for this route is NULL");
      // allow it to continue and crash
    }
    // I have the first path leg
     Legs.add(pathLeg);
     
     // now get the rest, until I reach the destination
     int hops = 1;
     while(!pathLeg.endsWith(Destination) && (hops++ < 10))
     {
       pathLeg = findNextPathLeg(pathLeg, table, fabric);
       if(pathLeg != null)
         Legs.add(pathLeg);
       else
       {
         System.err.println("Fatal path resolution after " + hops + " hops");
         for(RT_PathLeg pl: Legs)
           System.err.println(pl.toIB_TraceRtString());
       }

      }
  }
  
  private RT_PathLeg findNextPathLeg(IB_Guid swGuid, RT_Table table, OSM_Fabric fabric)
  {
    // from this guid (which should be a switch), construct the next path leg
    RT_PathLeg nextPathLeg = null;
    
    RT_Node rn = table.getRT_Node(swGuid);
    RT_Port rpd = null;
     
    if((rn != null) && (rn.contains(Destination)))
    {
      rpd = rn.getPortToDestination(Destination);
      if(rpd != null)
      {
        OSM_Port p = getOSM_Port(swGuid.getGuid(), (short) rpd.getPortNumber(), fabric);
        OSM_Port lp = getLinkedOSM_Port(p, fabric);
        if(lp != null)
        {
          // I have both sides of the link, or path, so can create it
          nextPathLeg = new RT_PathLeg(p, lp, this);
         }
        else
        {
          System.err.println("Could not find linked port in the fabric " + rpd.getLid() + ", and " + rpd.getPortNumber());
          System.err.println("Port is: " + p.toInfo());
        }
       }
      else
      {
        System.err.println("Could not find next path leg to destination guid " + Destination.toColonString());
      }
     }
    else
    {
      System.err.println("The rt_node or the swGuid is null ");
      if(rn != null)
         System.err.println("The switch guid is: " + swGuid.toColonString() + ", and the destination is: " + Destination.toColonString());
      else
        System.err.println("The switch is: " + swGuid + ", and the destination is: " + Destination.toColonString());

    }
    return nextPathLeg;
  }

  private RT_PathLeg findNextPathLeg(RT_PathLeg pathLeg, RT_Table table, OSM_Fabric fabric)
  {
    return findNextPathLeg(pathLeg.ToPort.getNodeGuid(), table, fabric);
  }

  private OSM_Port getLinkedOSM_Port(OSM_Port p, OSM_Fabric fabric)
  {
    // given a port and the fabric, find the remote (or linked) port

    if(p != null)
    {
      SBN_Port sp = p.getSbnPort();
//      System.err.println("Linked port guid is: " + new IB_Guid(sp.linked_node_guid).toColonString());
//      System.err.println("Linked port num  is: " + sp.linked_port_num);
      OSM_Port rp = getOSM_Port(p.getSbnPort().linked_node_guid, p.getSbnPort().linked_port_num, fabric);
      if((rp == null) && (sp.linked_port_num == 1))
      {
        // probably a CA, and sometimes the port guid is just the sw guid +1
        rp = getOSM_Port(p.getSbnPort().linked_node_guid + 1, p.getSbnPort().linked_port_num, fabric);
      }
      return rp;
    }
    return null;
  }

  private OSM_Port getOSM_Port(long guid, OSM_Fabric fabric)
  {
    short portNum = 1;
    return getOSM_Port(guid - portNum, portNum, fabric);
  }

  private OSM_Port getOSM_Port(long guid, short portNum, OSM_Fabric fabric)
  {
      String pKey = OSM_Fabric.getOSM_PortKey(guid, portNum);
      return fabric.getOSM_Port(pKey);
  }

  private OSM_Node getOSM_Node(long guid, OSM_Fabric fabric)
  {
      String pKey = OSM_Fabric.getOSM_NodeKey(guid);
      return fabric.getOSM_Node(pKey);
  }

  public void updateRoutingTable(RT_Table table, OSM_Fabric fabric)
  {
    init(Source, Destination, table, fabric);
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
    // TODO Auto-generated method s

  }
  
  
  
  /************************************************************
   * Method Name:
   *  getSource
   **/
  /**
   * Returns the value of source
   *
   * @return the source
   *
   ***********************************************************/
  
  public IB_Guid getSource()
  {
    return Source;
  }

  /************************************************************
   * Method Name:
   *  getDestination
   **/
  /**
   * Returns the value of destination
   *
   * @return the destination
   *
   ***********************************************************/
  
  public IB_Guid getDestination()
  {
    return Destination;
  }

  /************************************************************
   * Method Name:
   *  getRtTable
   **/
  /**
   * Returns the value of rtTable
   *
   * @return the rtTable
   *
   ***********************************************************/
  
  public RT_Table getRtTable()
  {
    return RtTable;
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
   *  getLegs
   **/
  /**
   * Returns the value of legs
   *
   * @return the legs
   *
   ***********************************************************/
  
  public ArrayList<RT_PathLeg> getLegs()
  {
    return Legs;
  }

  public boolean isSwitch(IB_Guid guid)
  {
    // return the lid for this guid
    return RtTable.isSwitch(guid);
  }

  public int getLid(IB_Guid guid)
  {
    // return the lid for this guid
    return RtTable.getLid(guid);
  }

  public String getNodeName(IB_Guid guid)
  {
    // return a quoted name string
    return getNodeName(guid, true);
  }

  public String getNodeName(IB_Guid guid, boolean quoted)
  {
    // return a quoted name string if true (mimics ibroute)
    
    // this guid may be a port, if so, find the parent node
    IB_Guid pg = Fabric.getParentGuid(guid);
    
    // if null, use original
    pg = pg == null ? guid: pg;
    OSM_Node n = getOSM_Node(pg.getGuid(), Fabric);
    String name = ((n != null) && (n.sbnNode != null)) ? n.sbnNode.description: "unknown";
    
    if(quoted)
      return "\"" + name + "\"";
    return name;
  }

  public String getTypeString(IB_Guid guid, boolean verbose)
  {
    // return "sw" or "ca"
    if(!verbose)
      return RtTable.isSwitch(guid) ? "switch": "ca";
    return RtTable.isSwitch(guid) ? "Switch": "Channel Adapter";
    
  }

  public String getTypeString(IB_Guid guid)
  {
    return getTypeString(guid, false);
  }

  public static String getRT_PathKey(IB_Guid src, IB_Guid dst)
  {
    // a string that uniquely identifies this path
    if((src != null) && (dst != null))
      return src.toColonString() + "->"+ dst.toColonString();
    return null;
  }

  public String getRT_PathKey()
  {
     return getRT_PathKey(Source, Destination);
  }

  private String getSrcString()
  {
    StringBuffer stringValue = new StringBuffer();
    
//    From ca {0x001175000077ef9e} portnum 1 lid 129-129 "hype281 qib0"
    
    int portNum = RtTable.isSwitch(Source) ? 0: 1;
    int lid = getLid(Source);
    
    stringValue.append("From " + getTypeString(Source) + " {0x" + Source + "} portnum " + portNum + " ");
    stringValue.append("lid " + lid + "-" + lid + " " + getNodeName(Source));
  
    return stringValue.toString();    
  }

  private String getDstString()
  {
    StringBuffer stringValue = new StringBuffer();
    
//  To ca {0x001175000077a00e} portnum 1 lid 332-332 "hype353 qib0"
  
  int portNum = RtTable.isSwitch(Destination) ? 0: 1;
  int lid = getLid(Destination);
  
  stringValue.append("To " + getTypeString(Destination) + " {0x" + Destination + "} portnum " + portNum + " ");
  stringValue.append("lid " + lid + "-" + lid + " " + getNodeName(Destination));

  return stringValue.toString();    
  }

  private String getPathString()
  {
    StringBuffer stringValue = new StringBuffer();
    
//    [1] -> switch port {0x00066a00ec002eec}[9] lid 17-17 "ibcore1 L109"
//    [27] -> switch port {0x00066a00eb002d14}[11] lid 8-8 "ibcore1 S205A"
//    [3] -> switch port {0x00066a02e8001313}[36] lid 7-7 "ibcore1 L101"
//    [9] -> ca port {0x001175000077a00e}[1] lid 332-332 "hype353 qib0"
    
    // iterate through the legs
    int lsize = Legs.size();
    int lcount = 0;
    for(RT_PathLeg leg: Legs)
    {
      stringValue.append(leg.toIB_TraceRtString(this));
      if(++lcount < lsize)
        stringValue.append("\n");
    }
    return stringValue.toString();    
  }

  public String toIB_TraceRtString()
  {
    // this should match (as closely as possible) the output
    // of the command; ibtracert -G src dst
    //
    StringBuffer stringValue = new StringBuffer();
    
//    From ca {0x001175000077ef9e} portnum 1 lid 129-129 "hype281 qib0"
//    [1] -> switch port {0x00066a00ec002eec}[9] lid 17-17 "ibcore1 L109"
//    [27] -> switch port {0x00066a00eb002d14}[11] lid 8-8 "ibcore1 S205A"
//    [3] -> switch port {0x00066a02e8001313}[36] lid 7-7 "ibcore1 L101"
//    [9] -> ca port {0x001175000077a00e}[1] lid 332-332 "hype353 qib0"
//    To ca {0x001175000077a00e} portnum 1 lid 332-332 "hype353 qib0"
    
    stringValue.append(getSrcString() + "\n");
    stringValue.append(getPathString() + "\n");
    stringValue.append(getDstString());
    
    return stringValue.toString();
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
    return getRT_PathKey();
  }

  public String getPathIdString()
  {
    return getPathIdString(true);
  }

  public String getPathIdString(boolean quoted)
  {
    // a string that uniquely identifies this path
    if((Source != null) && (Destination != null))
      return getNodeName(Source, quoted) + "->"+ getNodeName(Destination, quoted);
    return "unknown path";
  }

  @Override
  public int compareTo(RT_Path o)
  {
    // The same if the sources are the same, and the destinations are the same
    int rtnval = -1;
    if(Source.equals(o.getSource()))
      if(Destination.equals(o.getDestination()))
        rtnval = 0;
    return rtnval;
  }
  
  public boolean equals(Object obj) {
    return ((obj != null) && (obj instanceof RT_Path) && (this.compareTo((RT_Path)obj)==0));
  }

  public RT_Path getReturnPath()
  {
    // create a new path with the source and destination swapped
    if((Destination != null) && (Source != null) && (RtTable != null) && (Fabric != null))
      return new RT_Path(Destination, Source, RtTable, Fabric);
    return null;
  }

}
