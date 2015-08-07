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
 *        file: IB_VertexFillTransformer.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.graph.decorators;

import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Node;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Edge;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.util.BinList;

import java.awt.Paint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.collections15.Transformer;

public class IB_VertexFillTransformer implements Transformer<IB_Vertex, Paint>, CommonLogger
{
  VisualizationViewer<IB_Vertex,IB_Edge> visViewer = null;
  OSM_Node MgrNode;

  public IB_VertexFillTransformer()
  {
    this(null, null);
  }
  
  public IB_VertexFillTransformer(VisualizationViewer<IB_Vertex, IB_Edge> vv, OSM_Node mgr)
  {
    this.setVisualizationViewer(vv);
    this.setManagementNode(mgr);
  }
  
  private static void setManagerDecorator(OSM_Node mgr, Collection<IB_Vertex> vertexSet)
  {
    // find the vertex in the set that matches this node, and decorate it in a special way
    if((mgr != null) && (vertexSet != null))
    {
         for(IB_Vertex v: vertexSet)
        {
          if(v.getNode().equals(mgr))
          {
            v.setDecorator(new IB_Decorator("Manager", IB_LevelTransformer.getManagerNumber()));
            v.Decorator.SubnetManager = true;
            return;
          }
        }
     }
    else
    {
      // something is null, which one?
      if(mgr == null)
        logger.warning("Could not decorate a manager node that is NULL");
      if(vertexSet == null)
        logger.warning("Could not decorate a manager node using NULL vertex set");
    }
  }

  public int getVertexDecoratorNumber(IB_Vertex v)
  {
    if (v instanceof IB_Vertex)
    {
      // return a color number (see palette) in order
      // of precedence.  The node can only fill with a
      // single color, so if multiple conditions exist
      // the dominant one will win.  The if statement
      // below should be ordered based on dominant to
      // subordinant condition
      
      if(visViewer.getPickedVertexState().isPicked(v))
        return IB_TransformerFactory.getPickedNumber();
      
      if(v.Decorator.Congestion)
        return IB_TransformerFactory.getCongestionNumber();
      if(v.Decorator.ForwardPath)
        return IB_TransformerFactory.getForwardPathNumber();
      if(v.Decorator.ReturnPath)
        return IB_TransformerFactory.getReturnPathNumber();

      if(v.Decorator.SubnetManager)
        return IB_TransformerFactory.getManagerNumber();

      // finally, return the number from the decorator
       return v.getDecorator().getNumber();
    }
    return 0;
  }

  public int getGraphDecoratorNumber(Object G)
  {
    // the decorator number for a graph is the DOMINANT decorator number
    //      this is used when the vertices are collapsed into a graph
    if (G instanceof UndirectedSparseMultigraph)
    {
      UndirectedSparseMultigraph ug = (UndirectedSparseMultigraph) G;
      Collection picked = new HashSet(visViewer.getPickedVertexState().getPicked());
      for (Object pv : picked)
      {
        if (pv instanceof UndirectedSparseMultigraph)
        {
          if (pv.equals(ug))
            return IB_TransformerFactory.getPickedNumber();
        }
      }

      BinList<Integer> Number = new BinList<Integer>();
      // get ALL the decorator numbers from the graph
      ArrayList<Integer> list = getGraphDecoratorNumberArray(G);

      // loop through the number list and bin them up
      for (Integer i : list)
      {
        Number.add(i, i.toString());
      }
      // use the biggest bin
      long max = 0;
      for (long s : Number.getBinSizes())
      {
        max = max < s ? s : max;
      }
      // there may be more than one this size, just use the first one I find
      ArrayList<ArrayList<Integer>> binList = Number.getBinsWithSize((new Long(max)).intValue());
      ArrayList<Integer> bin = binList.get(0);
      return bin.get(0).intValue();
    }
    return 0;
  }

  public ArrayList<Integer> getGraphDecoratorNumberArray(Object G)
  {
    // this is RECURSIVE
    ArrayList<Integer> list = new ArrayList<Integer>();

    // the decorator number for a graph is the DOMINANT decorator number
    if (G instanceof UndirectedSparseMultigraph)
    {
      // loop through the vertices and get each decorator number
      UndirectedSparseMultigraph<IB_Vertex,IB_Edge> g = (UndirectedSparseMultigraph<IB_Vertex,IB_Edge>) G;

      // this can be a mixed collection of objects
      Collection c = g.getVertices();
      for (Object o : c)
      {
        if (o instanceof IB_Vertex)
        {
          Integer I = new Integer(getVertexDecoratorNumber((IB_Vertex)o));
          list.add(I);
        }
        if (o instanceof UndirectedSparseMultigraph)
        {
          list.addAll(getGraphDecoratorNumberArray(o));
        }
      }
    }
    return list;
  }

  public int getDecoratorNumber(Object o)
  {
    // if picked, always return the number for yellow
    if (o instanceof IB_Vertex)
    {
      return getVertexDecoratorNumber((IB_Vertex)o);
    }
    if (o instanceof UndirectedSparseMultigraph)
    {
      return getGraphDecoratorNumber(o);
    }
    return 0;
  }

  public Paint transform(Object v)
  {
    return IB_TransformerFactory.getPaint(getDecoratorNumber(v));
  }

  public Paint transform(IB_Vertex v)
  {
    return IB_TransformerFactory.getPaint(getDecoratorNumber(v));
  }

  public void setVisualizationViewer(VisualizationViewer<IB_Vertex,IB_Edge> vv)
  {
    visViewer = vv;
  }

  public void setManagementNode(OSM_Node mgr)
  {
    MgrNode = mgr;
    if(visViewer != null)
      setManagerDecorator(mgr, visViewer.getModel().getGraphLayout().getGraph().getVertices());
  }

}
