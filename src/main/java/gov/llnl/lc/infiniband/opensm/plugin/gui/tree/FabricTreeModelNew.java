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

import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Node;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_NodeType;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.SBN_Node;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Edge;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;
import gov.llnl.lc.util.BinList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

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
    String RootName = fabric.getFabricName();
    int maxDepth = IB_Vertex.getMaxDepth(VertexMap);
    LinkedHashMap <String, IB_Vertex> topLevel = IB_Vertex.getVertexMapAtDepth(VertexMap, maxDepth);
    fabric.createSystemGuidBins(false);
    BinList <IB_Guid> guidBins = fabric.getSystemGuidBins();
    
    System.err.println("************ There are " + guidBins.size() + " valid system guids");
    
    // always create a virtual root node for the fabric (everything is a child of it)
    IB_Vertex top = new IB_Vertex(new OSM_Node(), maxDepth+1, true, false, RootName);  // this is the artificial root node
    rootVertex = top;
    NameValueNode vmn = new NameValueNode("", top);
    rootVertexNode = new UserObjectTreeNode(vmn, true);
    HashMap <String, IB_Vertex> neighborMap = new LinkedHashMap <String, IB_Vertex>();

    
    if(guidBins.size() == 1)
    {
      if(guidBins.getMaxBinSize() < 2)
      {
        System.err.println("There is a single system, and it only contains a single guid");
        System.exit(0);
      }
      else
        System.err.println("************* There is a single system, and it contains " + guidBins.getMaxBinSize() + " guids");
    }
        
    // If there are any core, chassis, or system switches, add them first
    IB_Vertex sysVertex [] = null;
    if(guidBins.size() > 0)
    {
      sysVertex= new IB_Vertex[guidBins.size()];
      
      // create virtual Vertex for each guid, representing a core switch, and put them at the top (if only core switch)
      // or one down from the top if more than one
      
      // for each system guid, create a vertex, and then attach the existing vertex (with matching sys guids) as children
      int k=0;  // the core switch index
      for (ArrayList<IB_Guid> gList : guidBins)
      {
        // create a dummy Vertex for each system guid, and give it the system
        // guid
        String sGuid = guidBins.getKey(k);
        IB_Guid sysGuid = new IB_Guid(sGuid);
        SystemTreeModel treeModel = new SystemTreeModel(fabric, sysGuid);
        int numChildren = treeModel.getChildCount(treeModel.rootVertexNode);
        // did the model get created??
        
        int depth = treeModel.getRootVertex().getDepth();
        maxDepth = depth;
        String name = treeModel.getSystemNameString();
        SBN_Node sn = new SBN_Node();
        sn.node_guid = sysGuid.getGuid();
        OSM_Node n = new OSM_Node(sn); // null constructor for artificial node
        sysVertex[k] = new IB_Vertex(n, depth, false, false, name); // this is
                                                                   // the core
                                                                   // switch
                                                                   // node
        
        System.err.println("There are " + numChildren + " child nodes in this system tree model");
        System.err.println("There are " + depth + " depths in this system tree model");
        System.err.println("The root vertex guid in this system tree model is: " + treeModel.getRootVertex().getGuid().toColonString());
        System.err.println("The system guid is:                                " + sysGuid.toColonString());

        // ASSUMPTION: core switches are part of the top most levels
        // connect up the top level switches to these dummy Vertex

        int rootPortNum = 0;
        int numAdded = 0;
        for (Entry<String, IB_Vertex> entry : topLevel.entrySet())
        {
          IB_Vertex v = entry.getValue();
          // connect this vertex if its one of my children
          if (gList.contains(v.getGuid()))
          {
            // create an artificial edge between them (make up some port
            // numbers)
            OSM_Port rp = new OSM_Port(null, null, OSM_NodeType.SW_NODE);
            rp.setPortNumber(rootPortNum++);
            OSM_Port vp = new OSM_Port(null, null, OSM_NodeType.SW_NODE);
            vp.setPortNumber(-1);

            IB_Edge e = new IB_Edge(sysVertex[k], rp, v, vp);
            sysVertex[k].addEdge(e);
            numAdded++;
          }
          
          if(numAdded > 0)
          {
            // add this system to the root vertex
            // create an artificial edge between them (make up some port numbers)
            OSM_Port rp= new OSM_Port(null, null, OSM_NodeType.SW_NODE);
            rp.setPortNumber(rootPortNum);
            OSM_Port vp= new OSM_Port(null, null, OSM_NodeType.SW_NODE);
            vp.setPortNumber(-1);        
            
            IB_Edge e = new IB_Edge(top, rp, v, vp);
            top.addEdge(e);
            
            neighborMap.put(sysVertex[k].getKey(), v);
            rootPortNum++;
          }
        }
        k++;
      }
    }

    // done creating core switch nodes, and hooking them up
    if((sysVertex != null) && (sysVertex.length > 0))
    {
      // if there are more than one core switches, then create a parent or top vertex for these
      // otherwise just use the core switch vertex for the top level node
      if(sysVertex.length == 1)
      {
        System.err.println("I only have a single system guid");
      }
//        // connect the core switches
//        int rootPortNum = 0;
//        for(IB_Vertex s: sysVertex)
//        {
//          // create an artificial edge between them (make up some port numbers)
//          OSM_Port rp= new OSM_Port(null, null, OSM_NodeType.SW_NODE);
//          rp.setPortNumber(rootPortNum);
//          OSM_Port vp= new OSM_Port(null, null, OSM_NodeType.SW_NODE);
//          vp.setPortNumber(-1);        
//          
//          IB_Edge e = new IB_Edge(top, rp, s, vp);
//          top.addEdge(e);
//          
//          neighborMap.put(s.getKey(), s);
//          rootPortNum++;
//        }
        
        // now add the children
        addChildNodes(rootVertexNode, neighborMap, VertexMap);
    }
    else
    {
      // no system or core switches, just normal ones
       // connect up my "children" by creating new artificial edges
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
        top.addEdge(e);
      }
      addChildNodes(rootVertexNode, topLevel, VertexMap);
    }




 }
  
  
  private UserObjectTreeNode addChildNodes(UserObjectTreeNode parent, HashMap <String, IB_Vertex> neighborMap, HashMap <String, IB_Vertex> vertexMap)
  {
    NameValueNode nvn = (NameValueNode) parent.getUserObject();
    IB_Vertex pv = (IB_Vertex) nvn.getMemberObject();
    int myDepth = pv.getDepth(); // add neighbors with lower depth
    HashMap <String, IB_Vertex> NeighborMap = IB_Vertex.sortVertexMap(neighborMap, true);

    for (Entry<String, IB_Vertex> entry : NeighborMap.entrySet())
    {
      // by definition, its my neighbor, so connected to me
      // its my child if its depth is lower
      IB_Vertex v = entry.getValue();
      if(v.getDepth() == (myDepth -1))
      {
        // direct child, create and add it
        NameValueNode vmn = new NameValueNode("", v);
        UserObjectTreeNode vtn = new UserObjectTreeNode(vmn, true);
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
