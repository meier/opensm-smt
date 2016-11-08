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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Node;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_NodeType;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Edge;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;
import gov.llnl.lc.util.BinList;

public class FabricTreeModelNew implements TreeModel
{  
  protected UserObjectTreeNode rootVertexNode;
  protected IB_Vertex rootVertex;
  protected boolean rootReal = false;

  @Override
  public Object getRoot()
  {
    return rootVertexNode;
  }

  public IB_Vertex getRootVertex()
  {
    return rootVertex;
  }

  public FabricTreeModelNew(UserObjectTreeNode RootVertexNode)
  {
    super();
    rootVertexNode = RootVertexNode;
    NameValueNode nvn = (NameValueNode)RootVertexNode.getUserObject();
    rootVertex = (IB_Vertex)nvn.getMemberObject();
  }

  public FabricTreeModelNew(IB_Vertex root)
  {
    this(new UserObjectTreeNode(new NameValueNode((root == null ? "": root.getName()), root ), true));
 }

  public FabricTreeModelNew(HashMap <String, IB_Vertex> VertexMap, OSM_Fabric fabric)
  {
    // this is the normal preferred way to construct the tree model
    
    // Name of Fabric is always the root, or top level node
    //  selecting it gives you fabric level options
    //
    // next child is a chassis, or system guid, if they exist
    //  selecting it gives you the system tree, or shows its next level set of switches
    //
    // if a chassis exists, but only contains a single switch, treat it as a switch
    //
    // next is the top level switches.  This only appears if no system guids, or only
    //  a single switch with a system guid.
    
    
    // determine the basic nature of the fabric
    String RootName                            = fabric.getFabricName();
    int maxDepth                               = IB_Vertex.getMaxDepth(VertexMap);

    // make sure its completely initialized, by creating the systems
    fabric.createSystemGuidBins(false);
    
    // if no systems, then the "root" is the fake top level             = 1
    //   if systems, then each system gets a fake parent level for it   = 2
    //      (unless it has its own fake top parent)                     = 1
    int numVirtualLevels = fabric.getSystemGuidBins().size() > 0 ? 2: 1;
    
    // always create a virtual root node for the fabric (everything is a child of it)
    IB_Vertex top     = new IB_Vertex(new OSM_Node(), maxDepth+numVirtualLevels, true, false, RootName);  // this is the artificial root node
    rootVertex        = top;
    NameValueNode vmn = new NameValueNode("", top);
    rootVertexNode    = new UserObjectTreeNode(vmn, true);
    
     // If there are any core, chassis, or system switches, add them first
    if(fabric.getSystemGuidBins().size() > 0)
      addSystems(rootVertexNode, fabric, VertexMap );
    
    // if no systems (just normal switches), then add all the nodes
    if(rootVertexNode.getChildCount() < 1)
    {
      // connect up my "children" by creating new artificial edges
     LinkedHashMap <String, IB_Vertex> topLevel = IB_Vertex.getVertexMapAtDepth(VertexMap, maxDepth);
     int rootPortNum = 0;
     for (Entry<String, IB_Vertex> entry : topLevel.entrySet())
     {
       IB_Vertex v = entry.getValue();
       // create an artificial edge between them (make up some port numbers)
       OSM_Port rp= new OSM_Port(null, null, OSM_NodeType.SW_NODE);
       rp.setPortNumber(rootPortNum++);
       OSM_Port vp= new OSM_Port(null, null, OSM_NodeType.SW_NODE);
       vp.setPortNumber(-1);        
       
       IB_Edge e = new IB_Edge(top, rp, v, vp);
       rootVertex.addEdge(e);
     }
     addChildNodes(rootVertexNode, topLevel);
    }
 }
  
  private static UserObjectTreeNode addSwitches(UserObjectTreeNode parent, HashMap <String, IB_Vertex> neighborMap, IB_Guid sysGuid)
  {
    NameValueNode nvn = (NameValueNode) parent.getUserObject();
    IB_Vertex pv = (IB_Vertex) nvn.getMemberObject();
    int myDepth = pv.getDepth(); // add neighbors with lower depth

    HashMap <String, IB_Vertex> NeighborMap = IB_Vertex.sortVertexMap(neighborMap, true);

    for (Entry<String, IB_Vertex> entry : NeighborMap.entrySet())
    {
      // only add children that match my system guid
      // its my child if its depth is lower
      IB_Vertex v = entry.getValue();
      if((v.getDepth() == (myDepth -1)) && (sysGuid.equals(new IB_Guid(v.getNode().sbnNode.sys_guid))))
      {
        // direct child, create and add it
        NameValueNode vmn = new NameValueNode("switch", v);
        UserObjectTreeNode vtn = new UserObjectTreeNode(vmn, true);
        parent.add(vtn);

        // now try to add its children
        addChildNodes(vtn, v.getNeighborMap());
      }
    }
    return parent;
  }

  
  private static UserObjectTreeNode addSystems(UserObjectTreeNode parent, OSM_Fabric fabric, HashMap <String, IB_Vertex> VertexMap )
  {
    int maxDepth                                     = IB_Vertex.getMaxDepth(VertexMap);
    BinList <IB_Guid> systemGuidBins                 = fabric.getSystemGuidBins();
    
    // for each system guid, create a vertex, and then attach the existing vertex (with matching sys guids) as children
    int k=0;  // the core switch index
    for (ArrayList<IB_Guid> gList : systemGuidBins)
    {
      // get this system guid
      String sGuid = systemGuidBins.getKey(k);
      IB_Guid sysGuid = new IB_Guid(sGuid);
      
      // create a system model from the fabric
      SystemTreeModel sTreeModel = new SystemTreeModel(fabric, sysGuid);
      HashMap <String, IB_Vertex> systemVertexMap = sTreeModel.getVertexMap();
      int sDepth = IB_Vertex.getMaxDepth(systemVertexMap);
      UserObjectTreeNode systemVertexNode = (UserObjectTreeNode)sTreeModel.getRoot();
      
      // start with the top level switches in the system
      LinkedHashMap <String, IB_Vertex> topSwitches = IB_Vertex.getVertexMapAtDepth(VertexMap, maxDepth);

      IB_Vertex sysRootVertex    = sTreeModel.getRootVertex();
      String name = sTreeModel.getSystemNameString();
      
      // if there is more than one switch at the max level, then create a fake vertex (with a dummy node), provided by the SystemTreeModel
      if(topSwitches.size() > 1)
      {
        // create the fake vertex with a dummy node, provided by the SystemTreeModel
        OSM_Node n = sysRootVertex.getNode();
        n.sbnNode.node_type = (short) OSM_NodeType.SW_NODE.getType();  // force this to be a switch

        // always create a virtual root node for the fabric (everything is a child of it)
        IB_Vertex sv       = new IB_Vertex(n, maxDepth+1, false, false, name);  // this is the core switch node
        NameValueNode svmn = new NameValueNode("", sv);
        systemVertexNode   = new UserObjectTreeNode(svmn, true);
        
        // this is a top level (fake) node of a system
        parent.add(systemVertexNode);
      }
      else
      {
        // decrement the depth, not using the fake node
        NameValueNode nvn = (NameValueNode) parent.getUserObject();
        IB_Vertex pv = (IB_Vertex) nvn.getMemberObject();
        pv.setDepth(pv.getDepth() -1);

        // add this node to the root, or parent node
        systemVertexNode   = parent;
       
      }
      // add all its top level switches, which in turn adds their children
       addSwitches(systemVertexNode, topSwitches, sysGuid);
       k++;
    }
    return parent;
  }

  private static UserObjectTreeNode addChildNodes(UserObjectTreeNode parent, HashMap <String, IB_Vertex> neighborMap)
  {
    if(neighborMap == null)
      return parent;
    
    NameValueNode nvn = (NameValueNode) parent.getUserObject();
    IB_Vertex pv = (IB_Vertex) nvn.getMemberObject();
    int myDepth = pv.getDepth(); // add neighbors with equal and lower depth
    HashMap <String, IB_Vertex> NeighborMap = IB_Vertex.sortVertexMap(neighborMap, true);
    if((NeighborMap != null) && !(NeighborMap.isEmpty()))
    {
      for (Entry<String, IB_Vertex> entry : NeighborMap.entrySet())
      {
        // by definition, its my neighbor, so connected to me
        // its my child if its depth is lower
        IB_Vertex v = entry.getValue();
        if(isNeighborMyChild(pv, v))
        {
          // direct child, create and add it
          NameValueNode vmn = new NameValueNode("", v);
          UserObjectTreeNode vtn = new UserObjectTreeNode(vmn, true);
          parent.add(vtn);
          // logger.severe("Adding children at level: " + nn.getDepth());

          // now try to add its children
          addChildNodes(vtn, v.getNeighborMap());
        }
      }      
    }
    return parent;
  }

  private static boolean isNeighborMyChild(IB_Vertex parent, IB_Vertex neighbor)
  {
    if((parent == null) || (neighbor == null))
      return false;
    
    int myDepth          = parent.getDepth();
    int neighborDepth    = neighbor.getDepth();
    
    // a child is exactly one less
    if(myDepth == (neighborDepth +1))
      return true;
    
    // is this neighbor a "peer", same depth, but connected to me?
    if((myDepth == neighborDepth) && (neighborDepth == getMaxNeighborDepth(neighbor)))
        return true;

    return false;    
  }
  
  
  private static int getMaxNeighborDepth(IB_Vertex pv)
  {
    int maxD = 0;
    if (pv != null)
    {
      LinkedHashMap<String, IB_Vertex> nMap = pv.getNeighborMap();

      for (Entry<String, IB_Vertex> entry : nMap.entrySet())
      {
        // by definition, its my neighbor, so connected to me
        // its my child if its depth is lower
        IB_Vertex v = entry.getValue();
        maxD = v.getDepth() > maxD ? v.getDepth() : maxD;
      }
    }
    return maxD;
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
    UserObjectTreeNode p = (UserObjectTreeNode)parent;    
    return getChildSet(parent).size();
  }
  
  private Set <UserObjectTreeNode> getChildSet(Object parentNode)
  {
    // support both IB_Vertex and NamedValueNode parent objects
    UserObjectTreeNode p = (UserObjectTreeNode)parentNode;    
    Object parent = rootVertexNode.getUserObject();
    IB_Vertex pv       = null;
    NameValueNode nvn = null;
    if(parent instanceof IB_Vertex)
      pv = (IB_Vertex)parent;
    if(parent instanceof NameValueNode)
    {
      nvn = (NameValueNode)parent;
      if(nvn != null)
        pv = (IB_Vertex)nvn.getMemberObject();
      if(pv == null)
      {
        System.err.println("The parent vertext is NULL");
      }
    }
    
    
    // we are building the nodes and vertexes here

    HashMap <String, IB_Vertex> neighbors = pv.getNeighborMap();
    
    Set <UserObjectTreeNode> childSet = new HashSet <UserObjectTreeNode> ();
    
    int pDepth = pv.getDepth();  // this is my depth
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
        NameValueNode vmn = new NameValueNode("", v);
        childSet.add(new UserObjectTreeNode(vmn, true));
        
      }
    }
//    System.err.println("NumChildren: " + childSet.size());    
    return childSet;
  }



  @Override
  public boolean isLeaf(Object node)
  {
    UserObjectTreeNode n = (UserObjectTreeNode)node;
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
