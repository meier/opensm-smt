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
 *        file: HM_Port.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.data;

import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Collection;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Node;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Nodes;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Ports;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Depth;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Edge;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;
import gov.llnl.lc.logging.CommonLogger;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.SortedSet;
import java.util.TreeSet;

public class HM_Port implements Comparable<HM_Port>, CommonLogger
{
  /** this port **/
  private OSM_Port Port;
  /** the parent node **/
  private OSM_Node Node;
  /** the level, or depth - see IB_Vertex **/
  private int Depth = -1;  // number of levels removed from leaf nodes, 0 is a leaf node. (-1 == unknown)

  final private static String formatLevelString = "level %d-%s:%2d";
  final private static String formatString = "%s:%2d";

  /************************************************************
   * Method Name:
   *  HM_Port
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   ***********************************************************/
  public HM_Port(OSM_Port p, OSM_Node n, int d)
  {
    Port  = p;
    Node  = n;
    Depth = d;
  }
  
  /************************************************************
   * Method Name:
   *  getPort
   **/
  /**
   * Returns the value of port
   *
   * @return the port
   *
   ***********************************************************/
  
  public OSM_Port getPort()
  {
    return Port;
  }

  /************************************************************
   * Method Name:
   *  getNode
   **/
  /**
   * Returns the value of node
   *
   * @return the node
   *
   ***********************************************************/
  
  public OSM_Node getNode()
  {
    return Node;
  }

  public IB_Depth getIB_Depth()
  {
    return IB_Depth.get(Depth);
  }

  public int getDepth()
  {
    return Depth;
  }

  public static String getCompareString(OSM_Port p, OSM_Node n, int depth)
  {
    if((n != null) && (p != null))
      return String.format(formatLevelString, depth, n.sbnNode.description, p.getPortNumber());
//    return String.format(formatString, n.sbnNode.description, p.getPortNumber());
    
    return "unknown";
  }

  public String getCompareString()
  {
    return HM_Port.getCompareString(Port, Node, Depth);
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

  @Override
  public int compareTo(HM_Port p)
  {
    // return 1, if NOT COMPARABLE
    // this will push the results to the top of the sort, and then by
    // reversing the sort, the bad results will be pushed to the bottom
    //
    // -1 if first is less than second
    //  1 if first is greater than second (or if not comparable)
    
    // not comparable
    if((p == null) || (Node == null) || (Port == null))
      return 1;
    
    return getCompareString().compareTo(p.getCompareString());
  }

  public static SortedSet<HM_Port> getUniquePortSet(OMS_Collection history, boolean useEntireCollection)
  {
    // iterate over the entire history, if desired, but provide the option to only
    // use a subset, to improve performance
    SortedSet<HM_Port> sortedPorts = new TreeSet<HM_Port>();
    
    int numToAnalyze = history.getSize();
    if(!useEntireCollection)
       numToAnalyze = 2 < numToAnalyze ? 2: numToAnalyze;

    logger.info("Start - Identifying unique ports over this portion of the time frame: " + numToAnalyze + " of " + history.getSize());
    for(int n = 0; n < numToAnalyze; n++)
    {
      OpenSmMonitorService oms = history.getOMS(n);
      
      // now for each port in the OMS, add it to the tree
      OSM_Fabric fab = oms.getFabric();
      
      OSM_Nodes AllNodes = (fab == null) ? null : fab.getOsmNodes();
      OSM_Ports AllPorts = (fab == null) ? null : fab.getOsmPorts();

      HashMap<String, OSM_Node> nMap = OSM_Nodes.createOSM_NodeMap(AllNodes);
      HashMap<String, OSM_Port> pMap = OSM_Ports.createOSM_PortMap(AllNodes, AllPorts);

      LinkedHashMap<String, IB_Edge> edgeMap = IB_Edge.createEdgeMap(nMap, pMap);
      // from this edge map, create the vertex map (this sets the levels too)
      LinkedHashMap<String, IB_Vertex> vertexMap = IB_Vertex.createVertexMap(edgeMap, fab);
      IB_Vertex.setSBN_Switches(vertexMap, fab);
      
      for(IB_Edge e: edgeMap.values())
      {
        OSM_Node pNode = e.getEndpoint1().getNode();
        OSM_Port p     = e.getEndPort1();
        int      d     = e.getEndpoint1().getDepth();
        
        HM_Port hmp = new HM_Port(p, pNode, d);
        sortedPorts.add(hmp);
        
        // now do the other side of the link
        pNode = e.getEndpoint2().getNode();
        p     = e.getEndPort2();
        d     = e.getEndpoint2().getDepth();
        hmp = new HM_Port(p, pNode, d);
        
        sortedPorts.add(hmp);
      }
    }
    logger.info("End   - Identifying unique ports over this portion of the time frame: " + numToAnalyze + " of " + history.getSize());
    return sortedPorts;
  }

}
