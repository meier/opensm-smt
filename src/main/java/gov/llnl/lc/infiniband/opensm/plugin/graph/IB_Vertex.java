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
 *        file: IB_Vertex.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.graph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Node;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_NodeType;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_Port.PortCounterName;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_PortChange;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_PortChangeComparator;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_PortRate;
import gov.llnl.lc.infiniband.opensm.plugin.data.RT_PathLeg;
import gov.llnl.lc.infiniband.opensm.plugin.data.SBN_Switch;
import gov.llnl.lc.infiniband.opensm.plugin.graph.decorators.IB_Decorator;
import gov.llnl.lc.infiniband.opensm.plugin.graph.decorators.IB_VertexDecorator;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.util.BinList;

/**********************************************************************
 * Given a set of Edges, compile a list of the Vertices
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Feb 6, 2013 11:18:33 AM
 **********************************************************************/
public class IB_Vertex implements Comparable<IB_Vertex>, CommonLogger
{
  private OSM_Node node;
  private LinkedHashMap <String, IB_Edge> Edges;
  private BinList<Integer> PortDepthBins;  // key is the depth value (as a string), and the bin contains the port numbers connected to that depth
                                           // applies only to links, or edges.  ports that aren't connected can't be determined

  public IB_VertexDecorator Decorator;
  private IB_Decorator DecoratorOld;
  private int Depth = -1;  // number of levels removed from leaf nodes, 0 is a leaf node. (-1 == unknown)
  private boolean root = false;  // true only if this is the top most vertex
  private boolean mgrNode = false; // true only if this is THE subnet manager (this is dynamic)
  private String FabricName = null;
  private SBN_Switch sbnSwitch;  // if this Vertex/Node is a switch, then it contains additional information
  
  public IB_Decorator getDecorator()
  {
    return DecoratorOld;
  }

  public void setDecorator(IB_Decorator decorator)
  {
    DecoratorOld = decorator;
  }

  public void setDepth(int depth)
  {
    Depth = depth;
  }
  
  public boolean hasPort(OSM_Port p)
  {
    return hasPort(p.getPfmPort());
   }
  
  public boolean hasPort(PFM_Port p)
  {
     return node.getNodeGuid().equals(p.getNodeGuid());
   }
  
  public boolean hasEdge(OSM_Port p)
  {
    IB_Edge e = IB_Edge.getEdge(p, Edges);
    return e!= null;
   }
  
  public boolean hasEdge(PFM_Port p)
  {
    IB_Edge e = IB_Edge.getEdge(p, Edges);
    return e!= null;
   }
  
  public boolean hasEdge(IB_Edge ed)
  {
    IB_Edge e = IB_Edge.getEdge(ed, Edges);
    return e!= null;
   }
  
  public boolean hasError()
  {
    // the node has an error if any of its ports
    // have an error
    for (Entry<String, IB_Edge> entry : Edges.entrySet())
    {
      IB_Edge e = entry.getValue();
      if(e.hasError(this))
        return true;
    }
    return false;
  }
  
  public boolean hasDynamicError()
  {
    // the node has an error if any of its ports
    // have an error
    for (Entry<String, IB_Edge> entry : Edges.entrySet())
    {
      IB_Edge e = entry.getValue();
      if(e.hasDynamicError(this))
        return true;
    }
    return false;
  }
  
  public int getDepth()
  {
    return Depth;
  }
  
  /************************************************************
   * Method Name:
   *  getUpPorts
   **/
  /**
   * Returns the value of upPorts
   *
   * @return the upPorts
   *
   ***********************************************************/
  
  public ArrayList<Integer> getUpLinkNumbers()
  {
    // return all of the port numbers, connected to lower nodes than this
    
    // create an EnuSet of lower depths
    EnumSet<IB_Depth> depthSet = IB_Depth.getHigherDepths(IB_Depth.get(this.getDepth()));
   
    return getPortNumbersAtDepths(depthSet);
  }

  /************************************************************
   * Method Name:
   *  getDownPorts
   **/
  /**
   * Returns the value of downPorts
   *
   * @return the downPorts
   *
   ***********************************************************/
  
  public ArrayList<Integer> getDownLinkNumbers()
  {
    // return all of the port numbers, connected to lower nodes than this
    
    // create an EnuSet of lower depths
    EnumSet<IB_Depth> depthSet = IB_Depth.getLowerDepths(IB_Depth.get(this.getDepth()));
   
    return getPortNumbersAtDepths(depthSet);
  }

  public ArrayList<Integer> getPortNumbersAtDepths(EnumSet<IB_Depth> depthSet)
  {
    ArrayList<Integer> portNums = new ArrayList<Integer>();
    
    if(depthSet != null)
    {
      int k = 0;
      for (ArrayList<Integer> portList : PortDepthBins)
      {
        if (portList != null)
        {
          String key = (PortDepthBins.getKey(k)).trim();
          Integer depth = Integer.parseInt(key);
          if(depthSet.contains(IB_Depth.get(depth.intValue())))
          {
            // all all of the ports at this depth
            portNums.addAll(portList);
          }
         }
        k++;
      }
    }
    Collections.sort(portNums);
    return portNums;
  }

  public boolean isTouching(String name)
  {
    // nearest neighbor
    
    // return true if ANY of the edges in this
    // vertex touch another vertex with a matching
    // name in its decorator
    
    // iterate through all the edges
    for (Entry<String, IB_Edge> entry : Edges.entrySet())
    {
      IB_Edge e = entry.getValue();
      
      // only check the OTHER end of the edge, not my side
      if(e.getEndpoint1().equals(this))
      {
        if((e.getEndpoint2().getDecorator() != null) && (e.getEndpoint2().getDecorator().getName() != null) &&  (e.getEndpoint2().getDecorator().getName().equalsIgnoreCase(name)))
          return true;
      }
      else
      {
        if((e.getEndpoint1().getDecorator() != null) && (e.getEndpoint1().getDecorator().getName() != null) &&  (e.getEndpoint1().getDecorator().getName().equalsIgnoreCase(name)))
          return true;
      }
    }
    return false;
  }

  public boolean isTouching(int num)
  {
    // return true if ANY of the edges in this
    // vertex touch another vertex with a matching
    // number in its decorator
    
    for (Entry<String, IB_Edge> entry : Edges.entrySet())
    {
      IB_Edge e = entry.getValue();
      
      // only check the OTHER end of the edge, not my side
      if(e.getEndpoint1().equals(this))
      {
        if((e.getEndpoint2().getDecorator() != null) && (e.getEndpoint2().getDecorator().getNumber() == num))
          return true;
      }
      else
      {
        if((e.getEndpoint1().getDecorator() != null) && (e.getEndpoint1().getDecorator().getNumber() == num))
          return true;
      }
    }
    return false;
  }

  private boolean isTouchingDepth(int depth)
  {
    // return true if ANY of the edges in this
    // vertex touch another vertex with the specified
    // depth number
    
    for (Entry<String, IB_Edge> entry : Edges.entrySet())
    {
      IB_Edge e = entry.getValue();
      
      // only check the OTHER end of the edge, not my side
      if(e.getEndpoint1().equals(this))
      {
        if((e.getEndpoint2().getDepth() == depth))
          return true;
      }
      else
      {
        if((e.getEndpoint1().getDepth() == depth))
          return true;
      }
    }
    return false;
  }

  public static IB_Vertex getVertex(ArrayList <IB_Vertex> AllVertices, IB_Vertex v)
  {
    // return a (matching) vertex from the List if it exists, otherwise return null
    IB_Vertex matching = null;
    
    if(v != null && AllVertices != null && AllVertices.size() > 0)
    {
      for(IB_Vertex vloop: AllVertices)
      {
        if(vloop.equals(v))
        {
          matching = vloop;
          break;
        }
      }
    }
    if(matching == null)
      System.err.println("NO MATCH FOUND");
    return matching;
  }
  
  public static String getVertexKey(IB_Vertex v)
  {
    // key is the vertex guid
    if(v != null)
    {
      return IB_Vertex.getVertexKey(v.node.getNodeGuid().getGuid());
    }
    return null;
  }
  
  public static String getVertexKey(IB_Guid guid)
  {
    if(guid != null)
      return guid.toColonString();
    return null;
  }
  
  public static String getVertexKey(long guid)
  {
    return (new IB_Guid(guid)).toColonString();      
  }
  
  public String getKey()
  {
    // key is the vertex guid
    return IB_Vertex.getVertexKey(this);
  }
  
  public static int getMaxDepth(HashMap <String, IB_Vertex> VerticesMap)
  {
    int maxDepth = -1;
    if((VerticesMap != null) && (VerticesMap.size() > 0))
    {
      // iterate through the map, and find the highest depth
      for (Entry<String, IB_Vertex> entry : VerticesMap.entrySet())
      {
          IB_Vertex v = entry.getValue();
          if(v != null)
          {
            maxDepth = maxDepth > v.getDepth() ? maxDepth: v.getDepth();
          }
      }
    }
    return maxDepth;
  }
  
  public static LinkedHashMap <String, IB_Vertex> getVertexMapAtDepth(HashMap <String, IB_Vertex> VerticesMap, int depth)
  {
    LinkedHashMap<String, IB_Vertex> vertexMap = null;
    if((VerticesMap != null) && (VerticesMap.size() > 0))
    {
      // iterate through the map, and collect vertices at depth
      vertexMap = new LinkedHashMap<String, IB_Vertex>();
      for (Entry<String, IB_Vertex> entry : VerticesMap.entrySet())
      {
          IB_Vertex v = entry.getValue();
          if((v != null) && (v.getDepth() == depth))
          {
            vertexMap.put(v.getKey(), v);
          }
      }
    }
    return vertexMap;
  }
  
  public static int getNumVertexAtDepth(HashMap <String, IB_Vertex> VerticesMap, int depth)
  {
    int num = 0;
    if((VerticesMap != null) && (VerticesMap.size() > 0))
    {
      // get the map at this depth
      HashMap<String, IB_Vertex> depthMap = IB_Vertex.getVertexMapAtDepth(VerticesMap, depth);
      num = depthMap.size();
    }
    return num;
  }
  
  public static IB_Vertex getVertex( OSM_Port p, HashMap <String, IB_Vertex> VerticesMap)
  {
    // requires that the OSM_Port contains a PFM_Port
    if(p != null)
      return getVertex(p.getPfmPort(), VerticesMap);
    return null;
  }
  
  public static IB_Vertex getVertex( PFM_Port p, HashMap <String, IB_Vertex> VerticesMap)
  {
    // return a vertex which contains this port
    if((p != null) && (VerticesMap != null))
    {
      String key = IB_Vertex.getVertexKey(p.node_guid);
      return getVertex(key, VerticesMap);
    }
    return null;
  }
  
  /************************************************************
   * Method Name:
   *  getVertex
  **/
  /**
   * Return a matching vertex from the hashmap if it exists, otherwise
   * return null.
   *
   * @see     describe related java objects
   *
   * @param v           the vertex to match (used to create a lookup key)
   * @param VerticesMap the hashmap that may contain the desired vertex
   * @return a vertex from the hashmap, if a match is found
   ***********************************************************/
  public static IB_Vertex getVertex( IB_Vertex v, HashMap <String, IB_Vertex> VerticesMap)
  {
     if(v != null)
      return getVertex(v.getKey(), VerticesMap);
    
    return null;
  }
  
  public static IB_Vertex getVertexByName( String name, HashMap <String, IB_Vertex> VerticesMap)
  {
    // return a (matching) vertex from the List if it exists, otherwise return null
    if((VerticesMap != null) && (name != null))
    {
       for (Entry<String, IB_Vertex> entry : VerticesMap.entrySet())
      {
          IB_Vertex v = entry.getValue();
          if((v != null) && (v.getName() != null) && name.equalsIgnoreCase(v.getName()))
          {
            return v;
          }
      }
    }
    return null;
  }
  
  public static IB_Vertex getVertex( String vertexKey, HashMap <String, IB_Vertex> VerticesMap)
  {
    // return a (matching) vertex from the List if it exists, otherwise return null
    //
    // the vertexKey is
    if(VerticesMap != null)
      return VerticesMap.get(vertexKey);
    
    return null;
  }
  
  public static LinkedHashMap <String, IB_Vertex> getVertexMap(String fileName) throws Exception 
  {
    // TODO handle various types of files, from Graphs, to OMS, to Fabrics
    OpenSmMonitorService OMS = OpenSmMonitorService.readOMS(fileName);
    if(OMS != null)
      return createVertexMap(OMS.getFabric());
    return null;
  }
  
  public static LinkedHashMap <String, IB_Vertex> createVertexMap(String hostname, String portNumber)
  {
    OpenSmMonitorService OMS = null;
    try
    {
      OMS = OpenSmMonitorService.getOpenSmMonitorService(hostname, portNumber);
    }
    catch (IOException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    if(OMS != null)
      return createVertexMap(OMS.getFabric());
    return null;
  }
  
  public static LinkedHashMap <String, IB_Vertex> createVertexMap(OSM_Fabric fabric) 
  {
    return createVertexMap(fabric, null);
  }
    
  public static LinkedHashMap <String, IB_Vertex> createVertexMap(OSM_Fabric fabric, ArrayList<IB_Guid> guidArray) 
  {
    
    if(fabric != null)
    {
      LinkedHashMap <String, IB_Vertex> vMap = createVertexMap(fabric.getOSM_Nodes(), fabric.getOSM_Ports(), fabric, guidArray);
      IB_Vertex.setSBN_Switches(vMap, fabric);
//      IB_Vertex.setMgmtNode(vMap, fabric);
      return vMap;
    }
    return null;
  }
    
//  public static HashMap <String, IB_Vertex> getVertexMap3(HashMap<String, OSM_Node> nodeMap, HashMap<String, OSM_Port> portMap) 
//  {
//    System.out.println("Attempting to create an edgemap");
//    LinkedHashMap<String, IB_Edge> edgeMap = IB_Edge.createEdgeMap(nodeMap, portMap);
//    LinkedHashMap<String, IB_Vertex> vertexMap = null;
//    if((edgeMap != null) && (edgeMap.size() > 1))
//    {
//      System.out.println("Attempting to create the vertex map from an edgemap");
//      // from these edges, create the vertices (also sets the decorator)
//      vertexMap = IB_Vertex.createVertexMap(edgeMap);
//      
//      // by default, the Vertex decorators are Level based, you can change them here to Name based.
//      IB_Decorator.setAllNameDecorators(vertexMap, 3);
//      IB_Vertex.setDepths(vertexMap);
//    }
//    else
//      System.err.println("Could not create an edgeMap");
//    return vertexMap;
//  }
//  
  
  public static LinkedHashMap<String, IB_Vertex> createVertexMap(ArrayList <IB_Vertex> VertexArray)
  {
    LinkedHashMap<String, IB_Vertex> vertexMap = null;
    if((VertexArray != null) && (VertexArray.size() > 1))
    {
      vertexMap = new LinkedHashMap<String, IB_Vertex>();
      for(IB_Vertex v: VertexArray)
      {
        vertexMap.put(v.getKey(), v);
      }
    }
    return vertexMap;
  }
  
  public static LinkedHashMap<String, IB_Vertex> createVertexMap(HashMap<String, OSM_Node> nodeMap, HashMap<String, OSM_Port> portMap, OSM_Fabric fabric, ArrayList<IB_Guid> guidArray)
  {
    LinkedHashMap<String, IB_Edge> edgeMap = IB_Edge.createEdgeMap(nodeMap, portMap);
    return IB_Vertex.createVertexMap(edgeMap, fabric, guidArray);
  }
  
  public static LinkedHashMap<String, IB_Vertex> createVertexMap(HashMap<String, IB_Edge> edgeMap, OSM_Fabric fabric)
  {
    return createVertexMap(edgeMap, fabric, null);
  }
  
  public static LinkedHashMap<String, IB_Vertex> createVertexMap(HashMap<String, IB_Edge> edgeMap, OSM_Fabric fabric, ArrayList<IB_Guid> guidArray)
  {
    // pull the vertices out of the edges (and initialize the decorators)
    // if the guidArray is null, include all, but if it contains guids, then only include those guids
    
    LinkedHashMap<String, IB_Vertex> vertexMap = null;
    if((edgeMap != null) && (edgeMap.size() > 1))
    {
      vertexMap = new LinkedHashMap<String, IB_Vertex>();
      
      for (Entry<String, IB_Edge> entry : edgeMap.entrySet())
      {
          IB_Edge e = entry.getValue();
          // both sides fo the edge
              vertexMap.put(e.getEndpoint1().getKey(), e.getEndpoint1());
              vertexMap.put(e.getEndpoint2().getKey(), e.getEndpoint2());
              
      }
//      logger.info("The size of the vertexMap is: " + vertexMap.size());
      IB_Decorator.setAllLevelDecorators(vertexMap);
      IB_Vertex.setDepths(vertexMap);
      IB_Vertex.setMgmtNode(vertexMap, fabric);
      vertexMap = sortVertexMap(vertexMap, true);
     
      if((guidArray != null) && !(guidArray.isEmpty()))
      {
        LinkedHashMap<String, IB_Vertex> vertexMap2 = new LinkedHashMap<String, IB_Vertex>();
        
        // only include the vertex that have the guid
        for (Entry<String, IB_Vertex> entry : vertexMap.entrySet())
        {
            IB_Vertex v = entry.getValue();
            if(guidArray.contains(v.getGuid()))
              vertexMap2.put(v.getKey(), v);
        }
//        logger.info("The size of the guid vertex map is: " + vertexMap2.size());
        // if the size is greater than one, sort it
        if(vertexMap2.size() > 1)
          vertexMap = sortVertexMap(vertexMap2, true);
        else
          vertexMap = vertexMap2;
      }
    }
    
    // return a sorted (by name) map
    return vertexMap;
  }
  
//  public static LinkedHashMap<String, IB_Vertex> createVertexMapOrig(HashMap<String, IB_Edge> edgeMap, OSM_Fabric fabric, ArrayList<IB_Guid> guidArray)
//  {
//    // pull the vertices out of the edges (and initialize the decorators)
//    // if the guidArray is null, include all, but if it contains guids, then only include those guids
//    
//    LinkedHashMap<String, IB_Vertex> vertexMap = null;
//    if((edgeMap != null) && (edgeMap.size() > 1))
//    {
//      vertexMap = new LinkedHashMap<String, IB_Vertex>();
//      
//      for (Entry<String, IB_Edge> entry : edgeMap.entrySet())
//      {
//          IB_Edge e = entry.getValue();
//
//              // conditionally add both the ends of the edge
//            if((guidArray == null) || (guidArray.isEmpty()) || (guidArray.contains(e.getEndpoint1().getGuid())))
//              vertexMap.put(e.getEndpoint1().getKey(), e.getEndpoint1());
//              
//            if((guidArray == null) || (guidArray.isEmpty()) || (guidArray.contains(e.getEndpoint2().getGuid())))
//              vertexMap.put(e.getEndpoint2().getKey(), e.getEndpoint2());
//              
//      }
//      logger.info("The size of the vertexMap is: " + vertexMap.size());
//      
//      if(guidArray == null)
//      {
//      IB_Decorator.setAllLevelDecorators(vertexMap);
//      IB_Vertex.setDepths(vertexMap);
//      }
//    }
//    
//    // return a sorted (by name) map
//    vertexMap = sortVertexMap(vertexMap, true);
//    IB_Vertex.setMgmtNode(vertexMap, fabric);
//
//    
//    return vertexMap;
//  }
//  
  public static LinkedHashMap<String, IB_Vertex> sortVertexMap(HashMap<String, IB_Vertex> vertexMap, boolean byName)
  {
    // create a new sorted version of this map. Sort by Name if true, otherwise
    // by depth
    LinkedHashMap<String, IB_Vertex> vMap = null;

    if ((vertexMap != null) && (!vertexMap.isEmpty()) && (vertexMap.entrySet() != null))
    {
      vMap = new LinkedHashMap<String, IB_Vertex>();

      List<Map.Entry<String, IB_Vertex>> entries = new LinkedList<Map.Entry<String, IB_Vertex>>(vertexMap.entrySet());
      Collections.sort(entries, new SortVertexByMapEntry());
      for (Entry<String, IB_Vertex> entry : entries)
      {
        // create a new (sorted) hashmap
        IB_Vertex v = entry.getValue();
        vMap.put(entry.getKey(), v);
      }
    }
    return vMap;
  }
  
  public static class SortVertexByMapEntry implements Comparator <Map.Entry<String, IB_Vertex>>
  {
    // sort alphabetically by name
    public int compare(Entry<String, IB_Vertex> o1, Entry<String, IB_Vertex> o2)
    {
      return new SortVertexByName().compare(o1.getValue(), o2.getValue());
    }
  }
  
  public static class SortVertexByName implements Comparator <IB_Vertex>
  {
    // sort alphabetically by name
    public int compare(IB_Vertex o1, IB_Vertex o2)
    {
      return o1.getNode().sbnNode.description.compareToIgnoreCase(o2.getNode().sbnNode.description);
    }
  }
  
  public static class SortVertexByDepth implements Comparator <IB_Vertex>
  {
    public int compare(IB_Vertex o1, IB_Vertex o2)
    {
      return o1.getDepth() - o2.getDepth();
    }
  }
  

  public static boolean setSBN_Switches(HashMap<String, IB_Vertex> vMap, OSM_Fabric fabric)
  {
    // using the switches in the fabric, find a match in the vertex map, and set its member
    if((vMap != null) && (vMap.size() > 1))
    {
      for(SBN_Switch sw: fabric.getOsmSubnet().Switches)
      {
        // find a vertex with a matching guid
        IB_Vertex v = vMap.get(IB_Vertex.getVertexKey(sw.guid));
        if(v != null)
        {
          v.setSbnSwitch(sw);
        }
      }
    }
    return true;
  }

  public static boolean setMgmtNode(HashMap<String, IB_Vertex> vMap, OSM_Fabric fabric)
  {
    // if the management node is in the provided vectormap, then set its state
    if((vMap != null) && (vMap.size() > 1) && (fabric != null))
    {
      OSM_Node n = fabric.getManagementNode();
      if(n != null)
      {
        IB_Vertex v = vMap.get(IB_Vertex.getVertexKey(n.getNodeGuid()));
        if(v != null)
          return (v.mgrNode = true);        
      }
    }
    return false;
  }

  public static LinkedHashMap<String, IB_Vertex> createVertexErrorMap(HashMap<String, IB_Vertex> vMap )
  {
    // pull the vertices out of the edges (and initialize the decorators)
    LinkedHashMap<String, IB_Vertex> vertexMap = null;
    if((vMap != null) && (vMap.size() > 1))
    {
      vertexMap = new LinkedHashMap<String, IB_Vertex>();
      
      for (Entry<String, IB_Vertex> entry : vMap.entrySet())
      {
          IB_Vertex v = entry.getValue();
          
          if(v.hasError())
            vertexMap.put(v.getKey(), v);
      }
    }
    return vertexMap;
  }

  private static void setPortDirections(ArrayList<IB_Vertex> vL)
  {
    // ATTN: All vertices MUST have their depth set BEFORE calling this method
    //
    // walk through the vertices, and walk through their ports to determine up/down status
    // then create the two ArrayLists
    
    if((vL != null) && (vL.size() > 0))
    {
      // now loop through the vertices
      for(IB_Vertex tv: vL)
      {
          if(tv.getEdgeMap().size() > 0)
          {
            // loop through the edges, and determine the depth of the vertex on
            // the other end of the edge
            for(IB_Edge e: tv.getEdgeMap().values())
            {
              int pNum = e.getEndPort(tv).getPortNumber();
              IB_Vertex rv = e.getOtherEndpoint(tv);
              if(rv != null)
              {
                // TODO - I can actually set both sides, but attempt this optimization later
                //        only if necessary
                
                tv.PortDepthBins.add(pNum, Integer.toString(rv.getDepth()));
//                if(tv.getDepth() > rv.getDepth())
//                {
//                  tv.downPorts.add(pNum);
//                }
//                else if (tv.getDepth() < rv.getDepth())
//                  tv.upPorts.add(pNum);
//                else
//                  logger.severe("Two connected vertex at the same level: " + tv.getDepth());
               }
            }
//            Collections.sort(tv.downPorts);
//            Collections.sort(tv.upPorts);
         }
          else
            logger.warning("No edges for this vertex");
      }
    }
    else
    {
      logger.severe("Error, there must be at least one Leaf vertex!");
      System.exit(-1);
    }
    return;   
  }  
  

  private static int setDepthLevel(ArrayList<IB_Vertex> vL, int depthNumber)
  {
    // walk through the vertices, and set all leaf vertex (with only a single
    // edge) to
    // LEVEL 0
    // walk through the vertieses again, skip LEVEL 0, set all vertex that touch
    // LEVEL 0
    // vertex to LEVEL 1
    // walk through the verteces again, skip LEVEL 1, 2, set all vertex that
    // touch LEVEL 2
    // vertex to LEVEL 3
    // repeat until done.

    boolean newlyAssigned = false;

    int num = 0;
    if ((vL != null) && (vL.size() > 0))
    {
      // now loop through the vertices, check their edges to see if they touch a
      // lower level edge
      for (IB_Vertex tv : vL)
      {
        // Special case for level 1
        if (depthNumber == 0)
        {
          // TODO - check if the node in this Vertex thinks its a switch
          // Don't make a Switch Level 0
          // Some HCAs or leaf nodes (depth 0) may have 2 ports, or edges
          if (tv.getEdgeMap().size() <= 1)
          {
            tv.setDepth(depthNumber);
            newlyAssigned = true;
            num++;
            if (tv.getEdgeMap().size() == 0)
              logger.warning("No edges for this vertex");
          }
        }
        else
        {
          // skip vertices already assigned a depth
          if (tv.getDepth() > -1)
          {
            continue;
          }

          // assign only this level
          if (tv.isTouchingDepth(depthNumber - 1))
          {
            tv.setDepth(depthNumber);
            newlyAssigned = true;
            num++;
          }
        }
      }
    }
    if (newlyAssigned == false)
    {
      // this can happen (and normally does) if you work with a vertex map
      // (partial)
      // that has already been assigned levels. Not unusual...
      logger.fine("No depths were assigned for this vertex map, already previously assigned??");
    }

    if ((num <= 0) && (depthNumber == 0))
    {
      logger.severe("Error, there must be at least one Leaf vertex!");
      System.exit(-1);
    }
    return num;
  }  

  public static void setDepths(ArrayList<IB_Vertex> vL)
  {
    // Level 1 (depth 0) is the leaf node level, and should only have a single edge or link
    // to an edge switch (Level 2)
    
    // Work your way up from level 1, assign levels, and determine the next level based
    // on previous work.
    
    // Stop when no vertices are assigned a level - must be done!
    
    int levelNumber  = 0;
    int num = 1;  // just to get it started
    
    while(num > 0)
      num = setDepthLevel(vL, levelNumber++);  // do this level, go to next
    
    // depths are set, assign up and down to each port
    IB_Vertex.setPortDirections(vL);
  }

  public static void setDepths(HashMap<String, IB_Vertex> vMap)
  {
    if(vMap != null)
      IB_Vertex.setDepths(new ArrayList<IB_Vertex>(vMap.values()));
  }
  
  
  public static LinkedHashMap<String, IB_Edge> createEdgeMap(HashMap<String, IB_Vertex> vertexMap)
  {
    // TODO transform interface
    // transform a vertex map into an edge map
    LinkedHashMap<String, IB_Edge> edgeMap = null;
    if((vertexMap != null) && (vertexMap.size() > 1))
    {
      edgeMap = new LinkedHashMap<String, IB_Edge>();
      
      // combine all edges from all vertices
      for (Entry<String, IB_Vertex> entry : vertexMap.entrySet())
      {
          IB_Vertex v = entry.getValue();
          if(v != null)
            edgeMap.putAll(v.getEdgeMap());
      }
    }
    return edgeMap;
  }


  public OSM_Node getNode()
  {
    return node;
  }

  /************************************************************
   * Method Name:
   *  resetNode
  **/
  /**
   * Replaces the user data (an OSM_Node) within this vertex, with
   * the supplied user data.  Does not change the structure of the
   * vertex, or its edgemap
   *
   * @see     describe related java objects
   *
   * @param node  the user data of the vertex, containing its state
   ***********************************************************/
  public void resetNode(OSM_Node node)
  {
    this.node = node;
  }

  public String getName()
  {
    if(this.isRoot())
      return FabricName;
    return getNode().sbnNode.description;
  }

  public String setName(String name)
  {
    if(this.isRoot())
      return FabricName = name;
    return getNode().sbnNode.description;
  }

  public String getNodeType()
  {
    return OSM_NodeType.get(this.getNode()).getFullName();
  }

  public IB_Guid getGuid()
  {
    return getNode().getNodeGuid();
  }

  public int addEdge(IB_Edge edge)
  {
    // add by key, no duplicates
    if(edge != null)
    {
    Edges.put(edge.getKey(), edge);
//    Edges.add(edge);
    }
    return Edges.size();
  }

  public IB_Edge getEdge(int portNum)
  {
    // look through the edges associated with this vertex
    // and return the one for the specified port number
    for (Entry<String, IB_Edge> entry : Edges.entrySet())
    {
      IB_Edge e = entry.getValue();
      if(e.getEndPort(this).getPortNumber() == portNum)
        return e;
    }
    return null;
  }
  
  public IB_Edge getFirstEdge()
  {
    // the edge map is not in any order, but sometimes you
    // just need any edge
    for (Entry<String, IB_Edge> entry : Edges.entrySet())
    {
      IB_Edge e = entry.getValue();
        return e;
    }
    return null;
  }
  
  public OSM_Port getOSM_Port(int portNum)
  {
    // this port may be up or down, active or idle, fine or with errors
      // see if the port is part of this vertex
      IB_Edge e = getEdge(portNum);
      if(e != null)
        return e.getEndPort(this);
    return null;
   }

 
  
  /************************************************************
   * Method Name:
   *  getSbnSwitch
   **/
  /**
   * Returns the value of sbnSwitch
   *
   * @return the sbnSwitch
   *
   ***********************************************************/
  
  public SBN_Switch getSbnSwitch()
  {
    return sbnSwitch;
  }

  /************************************************************
   * Method Name:
   *  setSbnSwitch
   **/
  /**
   * Sets the value of sbnSwitch
   *
   * @param sbnSwitch the sbnSwitch to set
   *
   ***********************************************************/
  public void setSbnSwitch(SBN_Switch sbnSwitch)
  {
    this.sbnSwitch = sbnSwitch;
  }

  /************************************************************
   * Method Name:
   *  getNumPorts
  **/
  /**
   * Returns the number of ports for the node (SW or CA) associated
   * to this Vertex.  The number of ports will be greater than or
   * equal to the number of links or Edges.  Links or Edges are
   * "connected" and usually active or live ports.  A port can still
   * exist if not part of link.
   *
   * @see     describe related java objects
   *
   * @return
   ***********************************************************/
  public int getNumPorts()
  {
    return getNode().sbnNode.num_ports;
  }

  /************************************************************
   * Method Name:
   *  getEdgeMap
  **/
  /**
   * Returns the collection of Edges associated with this Vertex
   *
   * @see     describe related java objects
   *
   * @return
   ***********************************************************/
  public LinkedHashMap <String, IB_Edge> getEdgeMap()
  {
    return Edges;
  }

  public LinkedHashMap <String, IB_Vertex> getNeighborMap()
  {
    // a list of all the IB_Vertex at the other end of the edges
    LinkedHashMap<String, IB_Vertex> vertexMap = null;
    if (Edges != null)
    {
      vertexMap = new LinkedHashMap<String, IB_Vertex>();
      for (Entry<String, IB_Edge> entry : Edges.entrySet())
      {
        IB_Edge e = entry.getValue();

        // add only the other end, not me!
        if(!this.equals(e.getEndpoint1()))
          vertexMap.put(e.getEndpoint1().getKey(), e.getEndpoint1());
        
        if(!this.equals(e.getEndpoint2()))
          vertexMap.put(e.getEndpoint2().getKey(), e.getEndpoint2());
        
      }
    }
    return vertexMap;
  }

  public ArrayList <IB_Edge> getEdges()
  {
    return new ArrayList<IB_Edge>(Edges.values());
  }

  public IB_Vertex(OSM_Node Node)
  {
    super();
    node = Node;
    Edges = new LinkedHashMap<String, IB_Edge>();
    Decorator = new IB_VertexDecorator();
    PortDepthBins = new BinList<Integer>();
//    upPorts   = new java.util.ArrayList<Integer>();
//    downPorts = new java.util.ArrayList<Integer>();
  }

  public IB_Vertex(OSM_Node Node, int depth, boolean root, boolean mgrNode, String name)
  {
    this(Node);
    this.Depth = depth;
    this.root = root;
    this.mgrNode = mgrNode;
    this.FabricName = name;
  }
  
  public boolean isRoot()
  {
    return root;
  }
  
  public String getPortDirection(int portNumber)
  {
    // return up, down, or unknown
    if(isUpLink(portNumber))
      return "up";
    if(isDownLink(portNumber))
      return "down";
    return "unknown";
  }
  
  public boolean setManagementNode(OSM_Fabric fabric)
  {
    if(fabric != null)
      mgrNode = fabric.isManagementNode(node);
    return false;
  }

  public boolean isManagementNode()
  {
    return mgrNode;
  }

  public boolean isUpLink(int portNumber)
  {
    ArrayList<Integer> upPorts = getUpLinkNumbers();
    //return true if this portnumber is in the up list, otherwise return false
    if((upPorts != null) && (upPorts.size() > 0))
      return upPorts.contains(new Integer(portNumber));
    return false;
  }

  public boolean isDownLink(int portNumber)
  {
    ArrayList<Integer> downPorts = getDownLinkNumbers();
    //return true if this portnumber is in the down list, otherwise return false
    if((downPorts != null) && (downPorts.size() > 0))
      return downPorts.contains(new Integer(portNumber));
    return false;
  }

  @Override
  public int compareTo(IB_Vertex o)
  {
    // compare the guids
    return node.getNodeGuid().compareTo(o.getNode().getNodeGuid());
  }
  
  public boolean equals(Object obj) {
    return ((obj != null) && (obj instanceof IB_Vertex) && (this.compareTo((IB_Vertex)obj)==0));
  }
  
  public String toVertexErrorString()
  {
    StringBuffer sbuff = new StringBuffer();
    ArrayList<PFM_PortChange> pcList = getErrorPortChangeList();
    // return the MAX traffic
    if((pcList != null) && (pcList.size() > 0))
    {
      // build up a string listing ports with the same error, for example
      //   (link_downed: 1,4,20)(symbol_err:3,7,9)
      for (PortCounterName counter : PFM_Port.PortCounterName.PFM_ERROR_COUNTERS)
      {
          boolean errorExists = false;
        for(PFM_PortChange pc: pcList)
        {
          // does this port change have this error?
          if(pc.getDelta_port_counter(counter) > 0L)
          {
            if(!errorExists)
            {
              sbuff.append("("+counter.name()+":"+pc.getPortNumber());
              errorExists = true;
            }
            else
            {
              // already here, so just add the port number
              sbuff.append(","+pc.getPortNumber());
            }
          }
         }
         // all done with this list of portChanges, and this counter, so terminate
        if(errorExists)
          sbuff.append(")");
     }
      
      return sbuff.toString();
    }
    logger.severe("Change list appears empty");
    return "no errors on this node";
  }
  
  public PFM_PortChange getTopPortChange()
  {
    ArrayList<PFM_PortChange> pcList = getTrafficPortChangeList();
    // return the MAX traffic
    if((pcList != null) && (pcList.size() > 0))
    {
      return pcList.get(0);  // the first one should be the TOP
    }
    logger.severe("Change list appears empty");
    return null;
    
  }
  
  public ArrayList<PFM_PortChange> getErrorPortChangeList()
  {
    // assumes the changes are contained within the edges
    PFM_PortChangeComparator pcCompare = new PFM_PortChangeComparator(PFM_Port.PortCounterName.PFM_ERROR_COUNTERS);
    ArrayList<PFM_PortChange> pcList = new ArrayList<PFM_PortChange>();
    
    if((Edges == null) || (Edges.size() < 1))
    {
      logger.severe("This Vertex doesn't have any edges, not yet initialized?");
      return pcList;
    }
    
    // create a list of all this nodes port errors
    for (Entry<String, IB_Edge> entry : Edges.entrySet())
    {
      IB_Edge e = entry.getValue();
      PFM_PortChange pChange = null;
      
      // only check my side
      if(e.getEndpoint1().equals(this))
        pChange = e.getPortChange1();
       else
        pChange = e.getPortChange2();
      
      // add this change to the list, only if there is a non zero error value
      if(pChange != null)
      {
        for (PortCounterName counter : PFM_Port.PortCounterName.PFM_ERROR_COUNTERS)
        {
          if(pChange.getDelta_port_counter(counter) > 0L)
          {
            pcList.add(pChange);
            // no need to add more than once, since it will only overwrite itself
            break;
          }
        }
      }
    }
    
    if (pcList.size() > 1)
    {
      // sort the list of active error ports
      try
      {
        Collections.sort(pcList, pcCompare);
      }
      catch (Exception e)
      {
        logger.severe("Caught an exception while sorting");
      }
      Collections.reverse(pcList);
    }
    
    return pcList;
   }
  
  public ArrayList<PFM_PortChange> getTrafficPortChangeList()
  {
    // assumes the changes are contained within the edges
    PFM_PortChangeComparator pcCompare = new PFM_PortChangeComparator(PFM_Port.PortCounterName.PFM_DATA_COUNTERS);
    ArrayList<PFM_PortChange> pcList = new ArrayList<PFM_PortChange>();
    
    if((Edges == null) || (Edges.size() < 1))
    {
      logger.severe("This Vertex doesn't have any edges, not yet initialized?");
      return pcList;
    }
    
    // create a list of all this nodes port traffic
    for (Entry<String, IB_Edge> entry : Edges.entrySet())
    {
      IB_Edge e = entry.getValue();
      PFM_PortChange pChange = null;
      
      // only check my side
      if(e.getEndpoint1().equals(this))
        pChange = e.getPortChange1();
       else
        pChange = e.getPortChange2();
      
      if(pChange != null)
        pcList.add(pChange);
    }
    
    if (pcList.size() > 1)
    {
      // sort the list of active traffic ports
      try
      {
        Collections.sort(pcList, pcCompare);
      }
      catch (Exception e)
      {
        logger.severe("Caught an exception while sorting");
      }
      Collections.reverse(pcList);
    }
    
    return pcList;
   }
  
  public String toVertexTrafficString()
  {
    // print the top xmit/rcv rate from this vertex
    ArrayList<PFM_PortChange> pcList = getTrafficPortChangeList();
    
    StringBuffer sbuff = new StringBuffer();
    sbuff.append(this.toString() + "\n");
    
    // print the MAX traffic
    if((pcList != null) && (pcList.size() > 0))
    {
      PFM_PortChange pChange1 = pcList.get(0);  // the first one should be the top
      sbuff.append(" port " + pChange1.getPortNumber() + ":");
      sbuff.append(PFM_PortRate.toVerboseDiagnosticString(pChange1));
    }
    return sbuff.toString();
  }
  
  public String toVertexXmitString()
  {
    PFM_PortChange pChange1 = getTopPortChange();
    if (pChange1 != null)
      return String.format("%5d", PFM_PortRate.getTransmitRateMBs(pChange1));
    return "";
  }
  
  public String toVertexRcvString()
  {
    PFM_PortChange pChange1 = getTopPortChange();
    if (pChange1 != null)
      return String.format("%5d", PFM_PortRate.getReceiveRateMBs(pChange1));
    return "";
  }
  

  /************************************************************
   * Method Name:
   *  toVertexIdString
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
  public String toVertexIdString(int maxNameLen)
  {
    int maxNameLength = (maxNameLen > 4) && (maxNameLen < 40) ? maxNameLen: 16;
    
    String formatString = "%" + maxNameLength + "s %s";
    
    // this should be a short, but complete identification string for the node
    StringBuffer sbuff = new StringBuffer();
    // first 10 characters of name
    String name = getNode().sbnNode.description;
    if(name.length() > maxNameLength)
      name = name.substring(0,maxNameLength -1 );
    
    sbuff.append(String.format(formatString, name, getGuid().toColonString()));

    return sbuff.toString();
  }
  
  /************************************************************
   * Method Name:
   *  toTopPortIdString
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
  public String toTopPortIdString(int maxNameLen)
  {
    int pn = -1;
    
    PFM_PortChange pChange1 = getTopPortChange();
    if (pChange1 != null)
      pn = pChange1.getPortNumber();

    return String.format("%s:%2d", toVertexIdString(maxNameLen), pn);
  }
  
  public String toStringOrig()
  {
    StringBuffer stringValue = new StringBuffer();
    
    stringValue.append(node.pfmNode.node_name + " -> " + node.getNodeGuid().toColonString());
  
    return stringValue.toString();
  }
  
  @Override
  public String toString()
  {
    if(this.isRoot())
      return FabricName;

    StringBuffer stringValue = new StringBuffer();
    
    stringValue.append(node.pfmNode.node_name + " = " + node.getNodeGuid().toColonString());
  
    return stringValue.toString();
  }
  
  public String EdgesString()
  {
    StringBuffer stringValue = new StringBuffer();
    stringValue.append("Num Edges: " + Edges.size() + "\n");
    for (Entry<String, IB_Edge> entry : Edges.entrySet())
    {
      IB_Edge e = entry.getValue();
    
      stringValue.append(e.getKey() + "\n");
    }
    return stringValue.toString();
  }

  public static IB_Vertex getFromVertex(RT_PathLeg leg, Collection<IB_Vertex> vertexSet)
  {
    // look through the list of vertices for anything that contains this leg
    if((leg != null) && (vertexSet != null))
      return getVertex(leg.getFromPort(), vertexSet);
    return null;
  }

  public static IB_Vertex getToVertex(RT_PathLeg leg, Collection<IB_Vertex> vertexSet)
  {
    // look through the list of vertices for anything that contains this leg
    if((leg != null) && (vertexSet != null))
      return getVertex(leg.getToPort(), vertexSet);
    return null;
  }

  public static IB_Vertex getVertex(OSM_Port port, Collection<IB_Vertex> vertexSet)
  {
    // look through the list of vertices for anything that contains this port
    if((port != null) && (vertexSet != null))
    {
      for(IB_Vertex v: vertexSet)
      {
        if(v.hasPort(port))
          return v;
      }
    }
    return null;
  }

  public BinList<Integer> getPortDepthBins()
  {
    // typically there should only one or two bins, an upper level
    // and a lower level.  A top level Vertex will not have ports
    // connected to higher Vertex, simply because there are none
    return PortDepthBins;
  }

  public ArrayList<Integer> getPortDepths()
  {
    // return the Depths connected to this Vertex
    // the keys
    // (should have one or two values, around this vertex's depth)
    ArrayList<Integer> depths = new ArrayList<Integer>();
    
    int k = 0;
    for (ArrayList<Integer> portList : PortDepthBins)
    {
      if (portList != null)
      {
        String key = (PortDepthBins.getKey(k)).trim();
        depths.add(Integer.parseInt(key));
      }
      k++;
    }
    return depths;
  }

}
