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
 *        file: IB_TransformerFactory.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.graph.decorators;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Node;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Edge;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.util.Collection;

import org.apache.commons.collections15.Transformer;

public class IB_TransformerFactory
{
  private static final Color[] palette = { Color.RED,
    Color.LIGHT_GRAY,
    Color.BLUE,
    Color.GREEN,
    Color.CYAN,
    Color.PINK,
    Color.MAGENTA,
    Color.ORANGE,
    Color.WHITE,
    Color.GRAY,
    Color.DARK_GRAY,
    Color.YELLOW };

  // wraps through the palette based on the number
  //
  // put special use colors high in the palette
  // to hopefully maintain uniqueness of color/meaning
  public static Paint getPaint(int num)
  {
    return palette[num % palette.length];
  }

  public static int getErrNumber()
  {
    // red is the first one
    return 0;
  }

  public static int getLeafNumber()
  {
    // the leafs are #1
    return 1;
  }

  public static int getForwardPathNumber()
  {
    // the leafs are #1
    return 1;
  }

  public static int getReturnPathNumber()
  {
    // the leafs are #1
    return 1;
  }

  public static int getCongestionNumber()
  {
    // the leafs are #1
    return 1;
  }

  public static int getStaticErrorNumber()
  {
    // the leafs are #1
    return 1;
  }

  public static int getDynamicErrorNumber()
  {
    // the leafs are #1
    return 1;
  }

  public static int getManagerNumber()
  {
    // the manager is #7
    return 7;
  }

  public static int getPickedNumber()
  {
    // if picked, be yellow (last one)
    return palette.length -1;
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

  public static IB_Edge getEdge(OSM_Port port, Collection<IB_Edge> edgeSet)
  {
    // look through the list of vertices for anything that contains this port
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


  
  public static Transformer<Object, Paint> getDefaultPaintTransformer(VisualizationViewer visViewer)
  {
    // currently there is only the level transformer, but could be name or error or other type
    
    // color vertices (The Object is the Vertex, or collection of Vertices)
    IB_LevelTransformer vertexPaint = new IB_LevelTransformer();
    vertexPaint.setVisualizationViewer(visViewer);
    return vertexPaint;
  }

  public static Transformer<IB_Vertex, Paint> getVertexFillPaintTransformer(VisualizationViewer<IB_Vertex, IB_Edge> vv, OSM_Node mgr)
  {
    // colors the nodes, based on criteria in the vertex, and the decoration preferences
    
//    IB_VertexLevelTransformer t = new IB_VertexLevelTransformer();
    IB_VertexFillTransformer t = new IB_VertexFillTransformer(vv, mgr);
    return t;
  }
  
  public static Transformer<IB_Vertex, Paint> getVertexDrawPaintTransformer(VisualizationViewer<IB_Vertex, IB_Edge> vv)
  {
    IB_VertexDrawTransformer t = new IB_VertexDrawTransformer();
    t.setVisualizationViewer(vv);
    return t;
  }
  
  public static Transformer<IB_Vertex, String> getVertexLabelTransformer(VisualizationViewer<IB_Vertex, IB_Edge> vv)
  {
    IB_VertexLabelTransformer t = new IB_VertexLabelTransformer();
    t.setVisualizationViewer(vv);
    return t;
  }
  
  public static Transformer<IB_Vertex, String> getVertexToolTipTransformer(VisualizationViewer<IB_Vertex, IB_Edge> vv)
  {
    IB_VertexToolTipTransformer t = new IB_VertexToolTipTransformer();
    t.setVisualizationViewer(vv);
    return t;
  }
  
  public static Transformer<IB_Vertex, Shape> getVertexShapeTransformer(VisualizationViewer<IB_Vertex, IB_Edge> vv)
  {
    IB_VertexShapeTransformer t = new IB_VertexShapeTransformer();
    t.setVisualizationViewer(vv);
    return t;
  }
  
  public static Transformer<IB_Vertex, Stroke> getVertexStrokeTransformer(VisualizationViewer<IB_Vertex, IB_Edge> vv)
  {
    IB_VertexStrokeTransformer t = new IB_VertexStrokeTransformer();
    t.setVisualizationViewer(vv);
    return t;
  }
  
  public static Transformer<IB_Edge, Paint> getEdgeFillPaintTransformer(VisualizationViewer<IB_Vertex, IB_Edge> vv)
  {
    IB_EdgeFillTransformer t = new IB_EdgeFillTransformer();
    t.setVisualizationViewer(vv);
    return t;
  }
  
  public static Transformer<IB_Edge, Paint> getEdgeDrawPaintTransformer(VisualizationViewer<IB_Vertex, IB_Edge> vv)
  {
    IB_EdgeDrawTransformer t = new IB_EdgeDrawTransformer();
    t.setVisualizationViewer(vv);
    return t;
  }
  
  public static Transformer<IB_Edge, String> getEdgeToolTipTransformer(VisualizationViewer<IB_Vertex, IB_Edge> vv)
  {
    IB_EdgeToolTipTransformer t = new IB_EdgeToolTipTransformer();
    t.setVisualizationViewer(vv);
    return t;
  }
  
  public static Transformer<IB_Edge, String> getEdgeLabelTransformer(VisualizationViewer<IB_Vertex, IB_Edge> vv)
  {
    IB_EdgeLabelTransformer t = new IB_EdgeLabelTransformer();
    t.setVisualizationViewer(vv);
    return t;
  }
  
  public static Transformer<Context<Graph<IB_Vertex,IB_Edge>,IB_Edge>,Shape> getEdgeShapeTransformer(VisualizationViewer<IB_Vertex, IB_Edge> vv)
  {
    IB_EdgeShapeTransformer t = new IB_EdgeShapeTransformer();
    t.setVisualizationViewer(vv);
    return t;
  }
  
  public static Transformer<IB_Edge, Stroke> getEdgeStrokeTransformer(VisualizationViewer<IB_Vertex, IB_Edge> vv)
  {
    IB_EdgeStrokeTransformer t = new IB_EdgeStrokeTransformer();
    t.setVisualizationViewer(vv);
    return t;
  }
  

}
