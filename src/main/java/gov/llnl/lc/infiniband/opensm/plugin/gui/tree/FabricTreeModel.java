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
 *        file: FabricTreeModel.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.tree;

import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Node;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_NodeType;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Edge;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class FabricTreeModel implements TreeModel
{  
  private FabricTreeNode rootVertexNode;
  private IB_Vertex rootVertex;
  private boolean rootReal = false;

  @Override
  public Object getRoot()
  {
    return rootVertexNode;
  }

  public IB_Vertex getRootVertex()
  {
    return rootVertex;
  }

  public FabricTreeModel(FabricTreeNode rootVertexNode)
  {
    super();
    this.rootVertexNode = rootVertexNode;
    rootVertex = (IB_Vertex)rootVertexNode.getUserObject();
  }

  public FabricTreeModel(IB_Vertex root)
  {
    rootVertex = root;
    rootVertexNode = new FabricTreeNode(root, true);
 }

  public FabricTreeModel(HashMap <String, IB_Vertex> VertexMap, String RootName)
  {
    // assume it already has depths?
    
    // this is the normal preferred way to construct the tree model
    
    // find the root, or roots, create a virtual root if necessary
    IB_Vertex top = null;
    int maxDepth = IB_Vertex.getMaxDepth(VertexMap);
    LinkedHashMap <String, IB_Vertex> topLevel = IB_Vertex.getVertexMapAtDepth(VertexMap, maxDepth);
    
    if(topLevel.size() > 1)
    {
      // there are many top level nodes, so must create an artificial (single) root node
      maxDepth++;
      OSM_Node n = new OSM_Node();  // null constructor for artificial node
      
//      logger.info("The artificial root (level " + maxDepth + ") has " + topLevel.size() + " children");
      
      top = new IB_Vertex(n, maxDepth, true, false, RootName);  // this is the artificial root node
      
      // connect up my "children" by creating new artificial edges
      int rootPortNum = 0;
      for (Entry<String, IB_Vertex> entry : topLevel.entrySet())
      {
        IB_Vertex v = entry.getValue();
        // create an artifical edge between them (make up some port numbers)
        OSM_Port rp= new OSM_Port(null, null, OSM_NodeType.SW_NODE);
        rp.setPortNumber(rootPortNum++);
        OSM_Port vp= new OSM_Port(null, null, OSM_NodeType.SW_NODE);
        vp.setPortNumber(-1);        
        
        IB_Edge e = new IB_Edge(top, rp, v, vp);
        top.addEdge(e);
      }
      
      // now I have a full VertexMap, so create all the VetexTreeNodes from the top down
      rootVertex = top;
      rootVertexNode = new FabricTreeNode(top, true);
      addChildNodes(rootVertexNode, topLevel, VertexMap);
    }
    else if (topLevel.size() == 1)
    {
      // there is a real node at the top, very unusual, assign it as top
      rootReal = true;
      // obtain the next depth, and connect it up
      for (Entry<String, IB_Vertex> entry : topLevel.entrySet())
      {
        IB_Vertex v = entry.getValue();
        top = v;
      }
      topLevel = IB_Vertex.getVertexMapAtDepth(VertexMap, maxDepth -1);
      rootVertex = top;
      rootVertexNode = new FabricTreeNode(top, true);
      addChildNodes(rootVertexNode, topLevel, VertexMap);
    }
    else
    {
      System.err.println("No top level nodes in the vertex map");
      System.exit(-1);
    }    
 }

  private FabricTreeNode addChildNodes(FabricTreeNode parent, HashMap <String, IB_Vertex> neighborMap, HashMap <String, IB_Vertex> vertexMap)
  {
    IB_Vertex pv = (IB_Vertex) parent.getUserObject();
    int myDepth = pv.getDepth(); // add neighbors with lower depth

    for (Entry<String, IB_Vertex> entry : neighborMap.entrySet())
    {
      // by definition, its my neighbor, so connected to me
      // its my child if its depth is lower
      IB_Vertex v = entry.getValue();
      if(v.getDepth() == (myDepth -1))
      {
        // direct child, create and add it
        FabricTreeNode vtn = new FabricTreeNode(v, true);
        parent.add(vtn);
        // logger.severe("Adding children at level: " + nn.getDepth());

        // now try to add its children
        addChildNodes(vtn, v.getNeighborMap(), vertexMap);
      }
    }
    return parent;
  }


  @Override
  public Object getChild(Object parent, int index)
  {
    Object [] ca = null;
    if (index >= 0)
    {
      ca = getChildSet(parent).toArray();
      if (index < ca.length)
      {
        return ca[index];
      }
    }
    System.err.println("Array out of bounds: num children(" + ca.length + ") and index is (" + index+ ")");

    return null;
  }

  @Override
  public int getChildCount(Object parent)
  {
    FabricTreeNode p = (FabricTreeNode)parent;    
    return getChildSet(parent).size();
  }
  
  private Set <FabricTreeNode> getChildSet(Object parentNode)
  {
    FabricTreeNode p = (FabricTreeNode)parentNode;    
    IB_Vertex parent = (IB_Vertex)rootVertexNode.getUserObject();
    
    // we are building the nodes and vertexes here

    HashMap <String, IB_Vertex> neighbors = parent.getNeighborMap();
    
    Set <FabricTreeNode> childSet = new HashSet <FabricTreeNode> ();
    
    int pDepth = parent.getDepth();  // this is my depth
    int nDepth = 0;
    
    for (Entry<String, IB_Vertex> entry : neighbors.entrySet())
    {
      IB_Vertex v = entry.getValue();
      nDepth = v.getDepth();
      
      // if this vertex has a depth "Lower" than mine, its a child
      if((pDepth > nDepth) && (nDepth > -1))
      {
//        System.err.println("My Depth: " + pDepth + ", Neighbor Depth: "+ nDepth);    

        // looks like a child to me
        childSet.add(new FabricTreeNode(v, true));
        
      }
    }
//    System.err.println("NumChildren: " + childSet.size());    
    return childSet;
  }



  @Override
  public boolean isLeaf(Object node)
  {
    FabricTreeNode n = (FabricTreeNode)node;
    IB_Vertex v = (IB_Vertex)n.getUserObject();
    int depth = v.getDepth();
    // true if the nodes depth is 0
    
    return depth == 0 ? true: false;
  }

  @Override
  public void valueForPathChanged(TreePath path, Object newValue)
  {
    // TODO Auto-generated method stub

  }

  @Override
  public int getIndexOfChild(Object parent, Object child)
  {
    Object [] ca = getChildSet(parent).toArray();
    
    // iterate until found
    for(int index = 0; index < ca.length; index++)
    {
       if(child.equals(ca[index]))
         return index;
    }
    System.err.println("Match Not Found: node is not a child of parent");
    return -1;
  }

  @Override
  public void addTreeModelListener(TreeModelListener l)
  {
    // TODO Auto-generated method stub

  }

  @Override
  public void removeTreeModelListener(TreeModelListener l)
  {
    // TODO Auto-generated method stub

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

}
