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
 *        file: IB_GraphFactory.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.graph;

import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Node;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.infiniband.opensm.plugin.graph.decorators.IB_Decorator;
import gov.llnl.lc.infiniband.opensm.plugin.graph.decorators.IB_LevelTransformer;
import gov.llnl.lc.logging.CommonLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author meier3
 *
 */
public class IB_GraphFactory implements CommonLogger
{
  
  public static UndirectedSparseMultigraph<IB_Vertex,IB_Edge> getGraph(String fileName) throws Exception 
  {
    // TODO handle various types of files, from Graphs, to OMS, to Fabrics
    OpenSmMonitorService OMS = OpenSmMonitorService.readOMS(fileName);
    if(OMS != null)
      return getGraph(OMS.getFabric());
    return null;
  }
  
  public static UndirectedSparseMultigraph<IB_Vertex,IB_Edge> getGraph(String hostname, String portNumber) 
  {
    OpenSmMonitorService OMS = null;
    try
    {
      OMS = OpenSmMonitorService.getOpenSmMonitorService(hostname, portNumber);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    if(OMS != null)
      return getGraph(OMS.getFabric());
    return null;
  }
  
  public static UndirectedSparseMultigraph<IB_Vertex,IB_Edge> getGraph(OSM_Fabric fabric) 
  {
    if(fabric != null)
      return getGraph(fabric.getOSM_Nodes(), fabric.getOSM_Ports(), fabric.getManagementNode(), fabric);
    return null;
  }
  
  private static UndirectedSparseMultigraph<IB_Vertex,IB_Edge> getGraph(HashMap<String, OSM_Node> nodeMap, HashMap<String, OSM_Port> portMap, OSM_Node mgrNode, OSM_Fabric fabric) 
  {
    UndirectedSparseMultigraph<IB_Vertex,IB_Edge> g = new UndirectedSparseMultigraph<IB_Vertex,IB_Edge>();
    LinkedHashMap<String, IB_Edge> edgeMap = IB_Edge.createEdgeMap(nodeMap, portMap);
    if((edgeMap != null) && (edgeMap.size() > 1))
    {
      // from these edges, create the vertices (also sets the decorator)
      LinkedHashMap<String, IB_Vertex> vertexMap = IB_Vertex.createVertexMap(edgeMap, fabric);
      IB_Vertex.setSBN_Switches(vertexMap, fabric);
      
      return getGraph(vertexMap, edgeMap, mgrNode);
     }
    return g;
  }
  
  private static UndirectedSparseMultigraph<IB_Vertex,IB_Edge> getGraph(LinkedHashMap<String, IB_Vertex> vertexMap, LinkedHashMap<String, IB_Edge> edgeMap, OSM_Node mgrNode) 
  {
    UndirectedSparseMultigraph<IB_Vertex,IB_Edge> g = new UndirectedSparseMultigraph<IB_Vertex,IB_Edge>();
    
    // there should be correlation between the edgemap and vertexmap, not checked, simply assumed
    if((edgeMap != null) && (edgeMap.size() > 1) && (vertexMap != null) && (vertexMap.size() > 1))
    {
      // by default, the Vertex decorators are Level based, you can change them here to Name based.
//      IB_Decorator.setAllNameDecorators(vertexMap, 3);
      IB_Decorator.setAllLevelDecorators(vertexMap);
      IB_Decorator.setManagerDecorator(vertexMap, mgrNode);
      
      for (Entry<String, IB_Edge> entry : edgeMap.entrySet())
      {
          IB_Edge e = entry.getValue();
          IB_Vertex o1 = IB_Vertex.getVertex(e.getEndpoint1().getKey(), vertexMap);
          IB_Vertex o2 = IB_Vertex.getVertex(e.getEndpoint2().getKey(), vertexMap);
          
            // a good link (edge) so add the nodes(vertices) to the graph
          g.addEdge(e, o1, o2);
      }
    }
    return g;
  }
  
  /************************************************************
   * Method Name:
   *  updateGraph
  **/
  /**
   * Update the edges and vertices in the supplied graph, using the supplied edgeMap and vertexMap.
   * Normally this update occurs with a new OMS, and the changed edges and vertex need to be
   * copied to the graphs perspective of these objects.
   * 
   *
   * @see     describe related java objects
   *
   * @param g
   * @param vertexMap
   * @param edgeMap
   * @return
   ***********************************************************/
  public static UndirectedSparseMultigraph<IB_Vertex,IB_Edge> updateGraph(UndirectedSparseMultigraph<IB_Vertex,IB_Edge> g, LinkedHashMap<String, IB_Vertex> vertexMap, LinkedHashMap<String, IB_Edge> edgeMap, OSM_Node mgr, boolean stickyDecorator) 
  {
    // there should be correlation between the edgemap and vertexmap, not checked, simply assumed
    if((g != null) && (g.getVertexCount() > 1) && (edgeMap != null) && (edgeMap.size() > 1) && (vertexMap != null) && (vertexMap.size() > 1))
    {
      // attempt to update the vertices and edges within the graph
      Collection <IB_Vertex> vColl = g.getVertices();
      Collection <IB_Edge>   eColl = g.getEdges();
      
      // if not sticky, then redo all the decorators
      if(!stickyDecorator)
         IB_Decorator.setAllLevelDecorators(new ArrayList<IB_Vertex>(vColl));

      
      // loop through all the vertices, and update the fabric (user) data
      for(IB_Vertex vc: vColl)
      {
        IB_Vertex v = IB_Vertex.getVertex(vc, vertexMap);
        if(v != null)
        {
          // just the node
          vc.resetNode(v.getNode());
        }
      }
      
      // loop through all the edges, and update the fabric (user) data
      for(IB_Edge ec: eColl)
      {
        IB_Edge e = IB_Edge.getEdge(ec, edgeMap);
        if(e != null)
        {
          ec.resetPorts(e.getEndPort1(), e.getEndPort2());
          ec.setPortChange1(e.getPortChange1());
          ec.setPortChange2(e.getPortChange2());
        }
      }
      
      // color the subnet manager (allow it to change red later if has an error)
      IB_Decorator.setManagerDecorator(vertexMap, mgr);
      
      // find the matching node in the map, and color it red
      for(IB_Vertex v: vColl)
      {
        if(v.getNode().equals(mgr))
        {
           v.getDecorator().setNumber(IB_LevelTransformer.getManagerNumber());
           break;
        }
      }
      
      // now change the decorator on the new vertex's to turn ERRORS RED
      for (Map.Entry<String, IB_Vertex> entry : vertexMap.entrySet())
      {
        IB_Vertex cv = entry.getValue();
        if((cv != null) && cv.hasDynamicError())
          {
            // find the matching node in the map, and color it red
            for(IB_Vertex v: vColl)
            {
              if(v.equals(cv))
              {
                 v.getDecorator().setNumber(0);
                 break;
              }
            }
          }
      }
       return g;
    }
    return g;
  }
  
  public static UndirectedSparseMultigraph<IB_Vertex,IB_Edge> updateGraph(UndirectedSparseMultigraph<IB_Vertex,IB_Edge> g, LinkedHashMap<String, IB_Vertex> vertexMap, LinkedHashMap<String, IB_Edge> edgeMap, OSM_Node mgr) 
  {
    return updateGraph(g, vertexMap, edgeMap, mgr, false);
  }

}
