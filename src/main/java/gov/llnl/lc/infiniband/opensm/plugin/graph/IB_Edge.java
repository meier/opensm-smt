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
 *        file: IB_Edge.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.graph;

import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.core.IB_Link;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Node;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Nodes;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Ports;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_Port.PortCounterName;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_PortChange;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_PortRate;
import gov.llnl.lc.infiniband.opensm.plugin.data.RT_PathLeg;
import gov.llnl.lc.infiniband.opensm.plugin.graph.decorators.IB_EdgeDecorator;
import gov.llnl.lc.logging.CommonLogger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class IB_Edge implements Comparable<IB_Edge>, CommonLogger
{
  // the parent nodes for the endpoints
  public IB_Vertex Endpoint1;
  public IB_Vertex Endpoint2;
  
  // the ports for these endpoints
  private OSM_Port Endport1;
  private OSM_Port Endport2;

  // these are completely optional, and represent activity between the ports (see TopAnalyzer)
  private PFM_PortChange pChange1;
  private PFM_PortChange pChange2;
  
  public IB_EdgeDecorator Decorator;

  
  /************************************************************
   * Method Name:
   *  getDepth
  **/
  /**
   * The depth of an endport is the depth of its node, or IB_Vertex.
   *
   * @see     describe related java objects
   *
   * @return
   ***********************************************************/
  public int getDepth(OSM_Port endport)
  {
    // this edge has two endports, which one matches?
    int Depth = -1;
    
    if((endport != null) && (Endport1 != null) && (Endport2 != null))
    {
      // true if this edge contains this port
      if(Endport1.equals(endport))
      {
        // return the depth of this side
        if(Endpoint1 != null)
          Depth = Endpoint1.getDepth();
       }
      
      if(Endport2.equals(endport))
      {
        // return the depth of this side
        if(Endpoint2 != null)
          Depth = Endpoint2.getDepth();
      }
    }
     return Depth;
  }

  /************************************************************
   * Method Name:
   *  getDepth
  **/
  /**
   * The depth of an endport is the depth of its node, or IB_Vertex.
   *
   * @see     describe related java objects
   *
   * @return
   ***********************************************************/
  public int getDepth(PFM_Port endport)
  {
    // this edge has two endports, which one matches?
    int Depth = -1;
    
    if((endport != null) && (Endport1 != null) && (Endport2 != null))
    {
      // true if this edge contains this port
      if(Endport1.hasPort(endport))
      {
        // return the depth of this side
        if(Endpoint1 != null)
          Depth = Endpoint1.getDepth();
       }
      
      if(Endport2.hasPort(endport))
      {
        // return the depth of this side
        if(Endpoint2 != null)
          Depth = Endpoint2.getDepth();
      }
    }
    
    return Depth;
  }

  /************************************************************
   * Method Name:
   *  getDepth
  **/
  /**
   * The depth of an Edge is defined as the lowest depth of its
   * two endpoints.  An Edge whos depth is zero, is connected to
   * a leaf.  A depth of -1 is undefined.  A high value depth means
   * this edge is part of the core or backbone of the fabric.
   *
   * @see     describe related java objects
   *
   * @return
   ***********************************************************/
  public int getDepth()
  {
    // an edges depth is defined based on the depth of its endpoints.  It is the lower of the two
    int Depth = -1;
    
    if((Endpoint1 != null) && (Endpoint2 != null))
      Depth = Endpoint1.getDepth() > Endpoint2.getDepth() ? Endpoint2.getDepth(): Endpoint1.getDepth();
    
    return Depth;
  }

  public static LinkedHashMap<String, IB_Edge> createEdgeMap(HashMap<String, OSM_Node> nodeMap, HashMap<String, OSM_Port> portMap)
  {
    // refer to OSM_Fabric createIB_Links
    LinkedHashMap<String, IB_Edge>   edgeMap   = null;
    LinkedHashMap<String, IB_Vertex> vertexMap = null;
    if((nodeMap != null) && (nodeMap.size() > 1) && (portMap != null) && (portMap.size() > 1))
    {
      edgeMap   = new LinkedHashMap<String, IB_Edge>();
      vertexMap = new LinkedHashMap<String, IB_Vertex>();
      
      // link information is only contained in the SBN_Port, so this must exist
      // the remote, or linked, guid and port
      short lpn = 0;  // linked port number
      long lg = 0;    // linked node guid
      short p1 = 0;   // this port number
      long g1 = 0;    // this node guid
      
      // the local guid and port
      short pn = 0;
      long ng = 0;
      short p2 = 0;
      long g2 = 0;
      
      int bad_count = 0;
      int good_count = 0;
      
      // EDGES, PORTS, and LINKS ** ALWAYS ** use the parent node guid + port # for identification
      //   almost never need port guid, which doesn't seem to be handled consistently across vendors

      // iterate through all the ports
      for (Entry<String, OSM_Port> entry : portMap.entrySet())
      {
        OSM_Port p = entry.getValue();
        IB_Edge edge = null;
        
        if((p.getSbnPort() != null) && (p.getPfmPort() != null))
        {
        lg = p.getSbnPort().linked_node_guid;
        lpn = p.getSbnPort().linked_port_num;
        g1 = p.getSbnPort().node_guid;
        p1 = p.getSbnPort().port_num;
        
        // they all need to be valid or no need to go further
        if((lg == 0) || (lpn == 0) || (g1 == 0) || (p1 == 0))
        {
          // a downed port could cause this, perfectly normal - continue silently
          continue;
        }
        
        // find the linked port
        OSM_Port rp = portMap.get(OSM_Port.getOSM_PortKey(lg, lpn));
        if(rp == null)
        {
          logger.severe("Could not find a port that matches the linked attributes (" + portMap.size() + " ports)");
          logger.severe("Linked port should be: " + new IB_Guid(lg).toColonString() + " and port " + lpn);
          continue;
        }
        
        // check to make sure this "linked" port, also thinks its connected to the original
        // port, in other words, they need to think they are linked together
          if(rp.getSbnPort() != null)
          {
            g2 = rp.getSbnPort().linked_node_guid;
            p2 = rp.getSbnPort().linked_port_num;
            ng = rp.getSbnPort().node_guid;
            pn = rp.getSbnPort().port_num;
            
            // a link occurs if both ports think they are connected to each other
            boolean localToRemote = (ng == lg) && (pn == lpn);
            boolean remoteToLocal = (g1 == g2) && (p2 == p1);
            if(localToRemote && remoteToLocal)
            {
              // found two ports (p and rp) connected together, so create the vertices and edge/link
              String key1 = IB_Vertex.getVertexKey(p.getSbnPort().node_guid);
              String key2 = IB_Vertex.getVertexKey(rp.getSbnPort().node_guid);
              
              // find the first vertex, from existing list, or from OSM_Nodes             
              IB_Vertex v1 = vertexMap.get(key1);
              OSM_Node n = null;
              if(v1 == null)
              {
                // not in the vertexMap yet, so find this in the nodeMap, and create a vertex from it
                n = nodeMap.get(key1);
                if(n != null)
                {
                  v1 = new IB_Vertex(n);
                  vertexMap.put(v1.getKey(), v1);
                 }
              }
              
              // find the second vertex, from existing list, or from OSM_Nodes             
              IB_Vertex v2 = vertexMap.get(key2);
              n = null;
              if(v2 == null)
              {
                // find this in the nodeMap, and create a vertex from it
                n = nodeMap.get(key2);
                if(n != null)
                {
                  v2 = new IB_Vertex(n);
                  vertexMap.put(v2.getKey(), v2);
                 }
              }             
               
              // should have both endpoints, so create a link
              if((v1 != null) && (v2 != null))
              {
                good_count++;
                // check to see if this already exists in the Map
                edge = edgeMap.get(IB_Edge.getEdgeKey(v1, p, v2, rp));
                if(edge == null)
                {
                  edge = new IB_Edge(v1, p, v2, rp);
                  edgeMap.put(edge.getKey(), edge);
                }
                
                // now add this edge back to the vertices
                v1.addEdge(edge);
                v2.addEdge(edge);
              }
              else
              {
                logger.severe("Crap, I didn't create both endpoints");
                bad_count++;
                
              }
             }
          }
        }
      } // done with all ports
    }
    return edgeMap;
  }
  
  /************************************************************
   * Method Name:
   *  createEdges
  **/
  /**
   * Generate an array of IB_Edges, based on the arguments.  Also
   * includes creating the IB_Vertices.
   *
   * @see     describe related java objects
   *
   * @param AllNodes
   * @param AllPorts
   * @return
   ***********************************************************/
  public static ArrayList <IB_Edge> createEdges(OSM_Nodes AllNodes, OSM_Ports AllPorts)
  {
    ArrayList<IB_Edge> eL        = new ArrayList<IB_Edge>();
    
    logger.info("# Subnet ports:  " + AllPorts.getSubnPorts().length);
    logger.info("# PerfMgr ports: " + AllPorts.getPerfMgrPorts().length);
    
    LinkedHashMap <String, IB_Edge> edgeMap = createEdgeMap(OSM_Nodes.createOSM_NodeMap(AllNodes), OSM_Ports.createOSM_PortMap(AllNodes, AllPorts));
    eL.addAll(edgeMap.values());
    return eL;
  }

  public static String getEdgeKey(IB_Vertex e1, int pn1, IB_Vertex e2, int pn2)
  {
    // key is the edge vertices, 1 then 2 (including the port numbers)
    return getEdgeKey(e1.getGuid(), pn1, e2.getGuid(), pn2);
  }
  
  public static String getEdgeKey(IB_Link l)
  {
    // a helper method for getting the key from a link
    return getEdgeKey(l.getEndpoint1().getNodeGuid(), l.getEndpoint1().getPortNumber(),
                      l.getEndpoint2().getNodeGuid(), l.getEndpoint2().getPortNumber());
   }
  
  public static String getEdgeKeyOld(IB_Vertex e1, int pn1, IB_Vertex e2, int pn2)
  {
    // key is the edge vertices, 1 then 2 (including the port numbers)
    if((e1 != null) && (e2 != null))
    {
      // always put the smallest endpoint first
      IB_Vertex endpoint2 = e1;
      IB_Vertex endpoint1 = e2;
      int e2_PortNum = pn1;
      int e1_PortNum = pn2;
      if((e1 != null) && (e1.compareTo(e2) < 0))
      {
        endpoint1 = e1;
        endpoint2 = e2;      
        e1_PortNum = pn1;
        e2_PortNum = pn2;
      }
      return endpoint1.getGuid().toColonString() + ":" + Integer.toString(e1_PortNum) + "," + endpoint2.getGuid().toColonString() + ":" + Integer.toString(e2_PortNum);
    }
    return null;
  }
  
  public static String getEdgeKey(IB_Guid g1, int pn1, IB_Guid g2, int pn2)
  {
    // key is the edge vertices, 1 then 2 (including the port numbers)
    if((g1 != null) && (g2 != null))
    {
      // always put the smallest endpoint first
      IB_Guid endpoint2 = g1;
      IB_Guid endpoint1 = g2;
      int e2_PortNum = pn1;
      int e1_PortNum = pn2;
      if((g1 != null) && (g1.compareTo(g2) < 0))
      {
        endpoint1 = g1;
        endpoint2 = g2;      
        e1_PortNum = pn1;
        e2_PortNum = pn2;
      }
      return endpoint1.toColonString() + ":" + Integer.toString(e1_PortNum) + "," + endpoint2.toColonString() + ":" + Integer.toString(e2_PortNum);
    }
    return null;
  }
  
  public static String getEdgeKey(String edgeIdString)
  {
    // given the edgeIdString, defined below, attempt to construct the correct key
    
    if((edgeIdString != null) && (edgeIdString.length() > 40))
    {
      System.err.println("attempting to derive key from (" + edgeIdString + ")");
    }
    return null;
  }
  
  public static String getEdgeKey(IB_Vertex e1, OSM_Port p1, IB_Vertex e2, OSM_Port p2)
  {
    // key is the edge vertices, 1 then 2 (including the port numbers)
    if((e1 != null) && (e2 != null) && (p1 != null) && (p2 != null))
    {
      return getEdgeKey(e1, p1.getPortNumber(), e2, p2.getPortNumber());
    }
    return null;
  }
  
  public static String getEdgeKey(IB_Edge e)
  {
    // key is the edge vertices, 1 then 2 (including the port numbers)
    if(e != null)
    {
      return IB_Edge.getEdgeKey(e.Endpoint1, e.Endport1, e.Endpoint2, e.Endport2);
    }
    return null;
  }
  
  public static String getEdgeKey(RT_PathLeg leg)
  {
    // EDGES, PORTS, and LINKS ** ALWAYS ** use the parent node guid + port # for identification
    //   almost never need port guid, which doesn't seem to be handled consistently across vendors

    // key is the edge vertices, 1 then 2 (including the port numbers)
    if(leg != null)
       return IB_Edge.getEdgeKey(leg.getFromPort().getNodeGuid(), leg.getFromPort().getPortNumber(), leg.getToPort().getNodeGuid(), leg.getToPort().getPortNumber());
     return null;
  }
  
  public static String getEdgeLevelKey(IB_Edge e)
  {
    // this key will be used to sort edges by level, it will NOT uniquely identify an edge
    if(e != null)
    {
      String formatString = "%1d %s";
      return String.format(formatString, e.getDepth(), e.getEndpoint1().getGuid().toColonString());
     }
    return null;
  }
  
  public String getEdgeLevelKey()
  {
    return IB_Edge.getEdgeLevelKey(this);
  }
  
  public String getKey()
  {
    return IB_Edge.getEdgeKey(this);
  }
  
  public static IB_Edge getEdge( PFM_Port p, HashMap <String, IB_Edge> EdgeMap)
  {
    // look through the list of edges for anything that contains this port
    if((p != null) && (EdgeMap != null))
    {
      for (Entry<String, IB_Edge> entry : EdgeMap.entrySet())
      {
        IB_Edge e = entry.getValue();
        if(e.hasPort(p))
          return e;
      }
    }
    return null;
  }
  
  public static IB_Edge getEdge(RT_PathLeg leg, Collection<IB_Edge> edgeSet)
  {
    // return a (matching) edge from the List if it exists, otherwise return null
    if(leg != null)
      return getEdge(leg.getFromPort(), edgeSet);
    else
        logger.severe("Could not find an edge for this route path");
     return null;
 }
  
  public static IB_Edge getEdge(OSM_Port port, Collection<IB_Edge> edgeSet)
  {
    // look through the list of edges for anything that contains this port
    if((port != null) && (edgeSet != null))
    {
      for(IB_Edge e: edgeSet)
      {
        if(e.hasPort(port))
          return e;
      }
    }
    return null;
 }
  
  public static IB_Edge getEdge( OSM_Port p, HashMap <String, IB_Edge> EdgeMap)
  {
    // look through the list of edges for anything that contains this port
    if((p != null) && (EdgeMap != null))
    {
      for (Entry<String, IB_Edge> entry : EdgeMap.entrySet())
      {
        IB_Edge e = entry.getValue();
        if(e.hasPort(p))
          return e;
      }
    }
    return null;
  }
  
  public static IB_Edge getEdge( IB_Edge e, HashMap <String, IB_Edge> EdgeMap)
  {
    // return a (matching) edge from the List if it exists, otherwise return null
    if(e != null)
      return getEdge(e.getKey(), EdgeMap);
    
    return null;
  }
  
  public static IB_Edge getEdge( String edgeKey, HashMap <String, IB_Edge> EdgeMap)
  {
    // return a (matching) edge from the List if it exists, otherwise return null
    if(EdgeMap != null)
      return EdgeMap.get(edgeKey);
    
    return null;
  }
  
  public static IB_Edge getEdge( RT_PathLeg leg, HashMap <String, IB_Edge> EdgeMap)
  {
    // return a (matching) edge from the List if it exists, otherwise return null
    if(leg != null)
      return getEdge(IB_Edge.getEdgeKey(leg), EdgeMap);
    else
        logger.severe("Could not find an edge for this route path");
     return null;
  }

  
  @Override
  public int compareTo(IB_Edge o)
  {
    // The same if the endpoints are the same, order doesn't matter
    int rtnval = -1;
    if(!Endpoint1.equals(Endpoint2))
    {
      if(Endpoint1.equals(o.getEndpoint1()) || Endpoint1.equals(o.getEndpoint2()))
      {
        // same as one, so check the other
        if(Endpoint2.equals(o.getEndpoint1()) || Endpoint2.equals(o.getEndpoint2()))
        {
          if(!o.getEndpoint1().equals(o.getEndpoint2()))
            rtnval = 0;
        }
      }
    }
    return rtnval;
  }
  
  public boolean hasError(IB_Vertex endpoint)
  {
    // if this endpoint is part of this edge, check the corresponding
    // port and return its error status
    if(endpoint != null)
    {
      if(endpoint.equals(Endpoint1))
        return Endport1.hasError();
      if(endpoint.equals(Endpoint2))
        return Endport2.hasError();
    }
    return false;
  }
  
  public boolean hasDynamicError(IB_Vertex endpoint)
  {
    // if this endpoint is part of this edge, check the corresponding
    // port and return its error status
    if(endpoint != null)
    {
      if(endpoint.equals(Endpoint1) && (pChange1 != null))
        return pChange1.hasErrorChange();
      if(endpoint.equals(Endpoint2) && (pChange2 != null))
        return pChange2.hasErrorChange();
    }
    return false;
  }
  
  public boolean hasPort(PFM_Port p)
  {
    // true if this edge contains this port
    if(Endport1.hasPort(p) || Endport2.hasPort(p))
      return true;
    return false;
   }
  
  public boolean hasPort(OSM_Port p)
  {
    // true if this edge contains this port
    if(Endport1.equals(p) || Endport2.equals(p))
      return true;
    return false;
   }
  
  public boolean hasError()
  {
    // true if either endpoint has an error
    if(Endport1.hasError() || Endport2.hasError())
      return true;
    return false;
   }
  
  public boolean equals(Object obj) {
    return ((obj != null) && (obj instanceof IB_Edge) && (this.compareTo((IB_Edge)obj)==0));
  }

  public IB_Vertex getEndpoint1()
  {
    return Endpoint1;
  }

  public void setEndpoint1(IB_Vertex endpoint1)
  {
    Endpoint1 = endpoint1;
  }

  public IB_Vertex getEndpoint2()
  {
    return Endpoint2;
  }
  
  public IB_Vertex getOtherEndpoint(IB_Vertex tv)
  {
    // given a vertex (that is part of this edge)
    // return the vertex at the other end
    if(tv != null)
    {
      if(tv.equals(Endpoint1))
        return Endpoint2;
      else if(tv.equals(Endpoint2))
        return Endpoint1;
    }
    return null;
  }



  public IB_Vertex getEndpoint(OSM_Port endport)
  {
    // which vertex goes with this endport
    IB_Vertex v = null;

    if((endport != null) && (endport.getNodeGuid() != null) && (Endpoint1 != null) &&
        (Endpoint1.getGuid() != null) && (Endpoint2 != null) && (Endpoint2.getGuid() != null))
    {
      IB_Guid eg = endport.getNodeGuid();
      if(eg != null)
      {
        if(eg.equals(Endpoint1.getGuid()))
          v = Endpoint1;
        if(eg.equals(Endpoint2.getGuid()))
          v = Endpoint2;
      }
    }
    return v;
  }

  public OSM_Port getEndPort1()
  {
    return Endport1;
  }

  public OSM_Port getEndPort2()
  {
    return Endport2;
  }

  public OSM_Port getEndPort(IB_Vertex endpoint)
  {
    // which port goes with this vertex
    OSM_Port p = null;
    
    if((endpoint != null) && (Endport1 != null) && (Endport1.getNodeGuid() != null) && (Endport2.getNodeGuid() != null))
    {
      IB_Guid eg = endpoint.getGuid();
      if(eg != null)
      {
        if(eg.equals(Endport1.getNodeGuid()))
          p = Endport1;
        if(eg.equals(Endport2.getNodeGuid()))
          p = Endport2;
      }
    }
    return p;
  }
  
  public IB_Link getIB_Link()
  {
    return new IB_Link(this.getEndPort1(), this.getEndPort2());
  }

  public void setEndpoint2(IB_Vertex endpoint2)
  {
    Endpoint2 = endpoint2;
  }
  
  

  /************************************************************
   * Method Name:
   *  getpChange1
   **/
  /**
   * Returns the value of pChange1
   *
   * @return the pChange1
   *
   ***********************************************************/
  
  public PFM_PortChange getPortChange1()
  {
    return pChange1;
  }

  /************************************************************
   * Method Name:
   *  setpChange1
   **/
  /**
   * Sets the value of pChange1
   *
   * @param pChange1 the pChange1 to set
   *
   ***********************************************************/
  public void setPortChange1(PFM_PortChange pChange1)
  {
    this.pChange1 = pChange1;
  }

  /************************************************************
   * Method Name:
   *  getpChange2
   **/
  /**
   * Returns the value of pChange2
   *
   * @return the pChange2
   *
   ***********************************************************/
  
  public PFM_PortChange getPortChange2()
  {
    return pChange2;
  }

  /************************************************************
   * Method Name:
   *  setpChange2
   **/
  /**
   * Sets the value of pChange2
   *
   * @param pChange2 the pChange2 to set
   *
   ***********************************************************/
  public void setPortChange2(PFM_PortChange pChange2)
  {
    this.pChange2 = pChange2;
  }

  public IB_Edge(IB_Vertex endpoint1, OSM_Port endport1, IB_Vertex endpoint2, OSM_Port endport2)
  {
    super();
    // always put the smallest endpoint first
    Endpoint2 = endpoint1;
    Endpoint1 = endpoint2;
    Endport2  = endport1;
    Endport1  = endport2;
    if((endpoint1 != null) && (endpoint1.compareTo(endpoint2) < 0))
    {
      Endpoint1 = endpoint1;
      Endpoint2 = endpoint2;      
      Endport1  = endport1;
      Endport2  = endport2;
    }
    Decorator = new IB_EdgeDecorator();
  }
  
  public void resetPorts(OSM_Port endport1, OSM_Port endport2)
  {
    Endport1  = endport1;
    Endport2  = endport2;
  }


  
  /************************************************************
   * Method Name:
   *  toErrorCounterString
  **/
  /**
   * Describe the method here
   *
   * @see java.lang.Object#toString()
  
   * @param   describe the parameters
   *
   * @return  describe the value returned
   * @return
   ***********************************************************/
  
  public String toCounterString(EnumSet<PortCounterName> nameSet)
  {
    StringBuffer buff = new StringBuffer();
    boolean initial = true;
  
    if((this.getPortChange1() != null) && (this.getPortChange2() != null))
    {
      for (PortCounterName counter : nameSet)
      {
        // loop through the counters and build string for both changes
        long val1 = this.getPortChange1().getDelta_port_counter(counter);
        long val2 = this.getPortChange2().getDelta_port_counter(counter);
        if((val1 != 0L) || (val2 != 0L))
        {
          // at least one is non-zero, add this to the error string
          if(!initial)
            buff.append(", ");
          else
            initial = false;
          buff.append(counter.name() + "=" + val1 + "," +val2);
        }
      }
      
    }
    return buff.toString();
  }

  /************************************************************
   * Method Name:
   *  toErrorString
  **/
  /**
   * Describe the method here
   *
   * @see java.lang.Object#toString()
  
   * @param   describe the parameters
   *
   * @return  describe the value returned
   * @return
   ***********************************************************/
  
  public String toShortErrorString()
  {
    return toCounterString(PortCounterName.PFM_ERROR_COUNTERS);
  }

  public String toEdgeErrorString(int maxLen)
  {
    int mLen = (maxLen > 20) && (maxLen < 100) ? (maxLen -2)/2: 49;
    
    String p1e = (pChange1 != null) ? pChange1.toShortErrorString(): "";
    String p2e = (pChange2 != null) ? pChange2.toShortErrorString(): "";
    if(p1e.length() > mLen)
      p1e = p1e.substring(0,mLen -1 );
    if(p2e.length() > mLen)
      p2e = p2e.substring(0,mLen -1 );
    
    // side one errors -> side two errors
    // format centered around the arrow
    String formatString = "%" + mLen + "s->%s";
   
    return String.format(formatString, p1e, p2e);
  }


  public String toEdgeTrafficString()
  {
    StringBuffer sbuff = new StringBuffer();
    sbuff.append(this.toString() + "\n");
    
    // always return the edge/link traffic with respect to endport1
    if(pChange1 != null)
      sbuff.append(PFM_PortRate.toVerboseDiagnosticString(pChange1));
    
    return sbuff.toString();
  }
  
  public String toEdgeXmitString()
  {
    // always return the edge/link traffic with respect to endport1
    if(pChange1 != null)
      return String.format("%5d", PFM_PortRate.getTransmitRateMBs(pChange1));
    return "";
  }
  
  public String toEdgeRcvString()
  {
    // always return the edge/link traffic with respect to endport1
    if(pChange1 != null)    
      return String.format("%5d", PFM_PortRate.getReceiveRateMBs(pChange1));
   return "";
  }
  

  /************************************************************
   * Method Name:
   *  toEdgeIdString
  **/
  /**
   * Enforce a reasonably short identification string for this Edge/Link.
   * Since a link represents a relationships between two ports, the
   * two sets of guids/port# are really the only unique identification.
   * Although the node names have been abused to sometimes include inappropriate
   * information (location, other attributes), it is still the preferred way
   * of identifying a node, over the guid.
   * 
   * This short form of the ID omits the guids.
   *
   * @see     describe related java objects
   *
   * @return
   ***********************************************************/
  public String toEdgeIdString(int maxNameLen)
  {
    int maxNameLength = (maxNameLen > 4) && (maxNameLen < 40) ? maxNameLen: 16;
    
    // this should be a short, but complete identification string for the link
    StringBuffer sbuff = new StringBuffer();
    // first few characters of name
    String name = Endpoint1.getNode().sbnNode.description;
    if(name.length() > maxNameLength)
      name = name.substring(0,maxNameLength -1 );
    
    sbuff.append(name + ":" + Endport1.getPortNumber());
    sbuff.append("->");
    name = Endpoint2.getNode().sbnNode.description;
    if(name.length() > maxNameLength)
      name = name.substring(0,maxNameLength -1 );
    
    sbuff.append(name + ":" + Endport2.getPortNumber());

    return sbuff.toString();
  }
  
  /************************************************************
   * Method Name:
   *  toEdgeIdStringVerbose
  **/
  /**
   * Enforce a reasonably short identification string for this Edge/Link.
   * Since a link represents a relationships between two ports, the
   * two sets of guids/port# are really the only unique identification.
   * The names are added for readability, but the length and content of
   * the names have been abused to include everything except the zip code,
   * so the name is padded and/or truncated so that its size is deterministic.
   *
   * @see     describe related java objects
   *
   * @return
   ***********************************************************/
  public String toEdgeIdStringVerbose(int maxNameLen)
  {
    int maxPortNumLen = 2;
    int maxNameLength = (maxNameLen > 4) && (maxNameLen < 40) ? maxNameLen: 16;
    
    String formatString = "%" + maxNameLength + "s %s:%" + maxPortNumLen + "d";
    
    // this should be a short, but complete identification string for the link
    StringBuffer sbuff = new StringBuffer();
    // first 10 characters of name
    String name = Endpoint1.getNode().sbnNode.description;
    if(name.length() > maxNameLength)
      name = name.substring(0,maxNameLength -1 );
    
    sbuff.append(String.format(formatString, name, Endpoint1.getGuid().toColonString(), Endport1.getPortNumber()));
    sbuff.append("->");
    
    formatString = "%s:%" + maxPortNumLen + "d %" + maxNameLength + "s";
    name = Endpoint2.getNode().sbnNode.description;
    if(name.length() > maxNameLength)
      name = name.substring(0,maxNameLength -1 );
    sbuff.append(String.format(formatString, Endpoint2.getGuid().toColonString(), Endport2.getPortNumber(),  name));

    return sbuff.toString();
  }
  
  public String toStringOrig()
  {
    return "IB_Edge [Endpoint1=" + Endpoint1 + ":" + Endport1.getPortNumber() + ", Endpoint2=" + Endpoint2 + ":" + Endport2.getPortNumber() +  "]";
  }
  
  public String toString()
  {
    return Endpoint1 + ":" + Endport1.getPortNumber() + " <-> " + Endpoint2 + ":" + Endport2.getPortNumber();
  }

  
}
