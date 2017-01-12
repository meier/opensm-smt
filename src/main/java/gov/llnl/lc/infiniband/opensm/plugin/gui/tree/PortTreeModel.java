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
 *        file: PortTreeModel.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.tree;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.RT_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.RT_Table;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Edge;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;
import gov.llnl.lc.smt.manager.SMT_RouteManager;

public class PortTreeModel extends AbstractPortTreeModel
{  
  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = 3191404221487003603L;
  /**  describe serialVersionUID here **/
  
  private IB_Edge rootEdge;

  /************************************************************
   * Method Name:
   *  PortTreeModel
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param root
   * @param asksAllowsChildren
   ***********************************************************/
  public PortTreeModel(TreeNode root)
  {
    super(root);
  }

  public PortTreeModel(IB_Vertex vertex, int portNumber)
  {
    this(vertex, vertex.getEdge(portNumber));
  }


  public PortTreeModel(IB_Vertex vertex, IB_Edge edge)
  {
    super(null);
    // assume it is fully constructed
    
    // this is the normal preferred way to construct the tree model
    
    // the tree is made up of VertexMemberTreeNodes, which contains a name/value pair
    //    the name is just type, and the value is the object
    //
    //  the root is an OSM_Port, determined by an IB_Edge and an IB_Vertex
    this.rootVertex = vertex;
    this.rootEdge   = edge;
    
    // go no further, we should have both parts
    if((vertex == null) || (edge == null))
      return;

    OSM_Port p = edge.getEndPort(rootVertex);
    if(p == null)
    {
      System.err.println("could not find an endport for this vertex (" + rootVertex.getGuid().toColonString() + ")");
      return;
    }
    this.rootPort = p;
    
    NameValueNode      vmn = new NameValueNode("port #", p );
    UserObjectTreeNode vmtn = new UserObjectTreeNode(vmn, true);
    rootPortNode = vmtn;
    
    addEdgePort(rootPortNode);
  }
  
  public PortTreeModel(IB_Vertex vertex, OSM_Port port)
  {
    super(null);
    // assume it is poorly constructed, lacking link/edge info
    
    // this is the constructor for downed ports or ports without links
    
    // the tree is made up of VertexMemberTreeNodes, which contains a name/value pair
    //    the name is just type, and the value is the object
    //
    //  the root is an OSM_Port, determined by an IB_Edge and an IB_Vertex
    this.rootVertex = vertex;
    this.rootEdge   = null;
    
    OSM_Port p = port;
    if(p == null)
    {
      System.err.println("could not find an endport for this vertex (" + rootVertex.getGuid().toColonString() + ")");
      return;
    }
    this.rootPort = p;
    
//    NameValueNode      vmn = new NameValueNode("port #", p.getPortNumber());
    NameValueNode      vmn = new NameValueNode("port #", p);
    UserObjectTreeNode vmtn = new UserObjectTreeNode(vmn, true);
    rootPortNode = vmtn;
    
    addEdgePort(rootPortNode);
  }
  
  public static IB_Guid getLinkedPortGuid(UserObjectTreeNode node)
  {
    if(node != null)
    {
      String guidString = getLinkedGuidString(node);
      if(guidString != null)
        return new IB_Guid(guidString);
    }
    return null;
  }
  
  public static int getLinkedPortNum(UserObjectTreeNode node)
  {
    if(node != null)
    {
      String numString = getLinkedPortString(node);
      if(numString != null)
        return Integer.valueOf(numString);
    }
    return -1;
  }
  
  public static String getLinkedGuidString(UserObjectTreeNode node)
  {
    String portAddress = getLinkedPortAddressString(node);
    if(portAddress != null)
    {
      // strip the last colon, which is the port number
       int ndex = portAddress.lastIndexOf(":");
      return portAddress.substring(0, ndex);
    }
    return null;
  }
  
  public static String getLinkedPortString(UserObjectTreeNode node)
  {
    String portAddress = getLinkedPortAddressString(node);
    if(portAddress != null)
    {
      // strip the last colon, which is the port number
       int ndex = portAddress.lastIndexOf(":");
       return portAddress.substring(ndex+1);
     }
    return null;
  }
  
  public static String getLinkedPortAddressString(UserObjectTreeNode node)
  {
    NameValueNode nvn = getNameValueNode(node, "linked port address");
    if(nvn != null)
      return (String)nvn.getMemberObject();
    return null;
  }
  
  public boolean isLink()
  {
    // return true if vertex and edge are not null
    return ((rootEdge != null) && (rootVertex != null));
  }
  
  private boolean addEdgePort(UserObjectTreeNode vmtn)
  {
      // this edge has two ports, get the one for THIS vertex
      OSM_Port p = rootPort;
      if(p == null)
      {
        System.err.println("could not find an endport for this vertex (" + rootVertex.getGuid().toColonString() + ")");
        return false;
      }
      
      // now vmtn is a parent, so add its elements
      PFM_Port pf = p.pfmPort;

      NameValueNode     n  = new NameValueNode("rate", p.getRateString());
      UserObjectTreeNode tn = new UserObjectTreeNode(n, false);
      vmtn.add(tn);

      // only display the lid if its non-zero (switch ports don't have lids?? )
      int lid = p.getAddress().getLocalId();
      if(lid > 0)
      {
        n  = new NameValueNode("lid", p.getAddress().getLocalIdHexString() + " (" + p.getAddress().getLocalId() + ")");
//      n  = new NameValueNode("lid", p.getAddress().getLocalId());
        tn = new UserObjectTreeNode(n, false);
        vmtn.add(tn);
      }

      n  = new NameValueNode("speed", p.getSpeedString());
      tn = new UserObjectTreeNode(n, false);
      vmtn.add(tn);

      n  = new NameValueNode("state", p.getStateString());
      tn = new UserObjectTreeNode(n, false);
      vmtn.add(tn);

      n  = new NameValueNode("width", p.getWidthString());
      tn = new UserObjectTreeNode(n, false);
      vmtn.add(tn);
      
      addMore(vmtn, p);

      n  = new NameValueNode("errors", p.hasError());
      tn = new UserObjectTreeNode(n, false);
      vmtn.add(tn);
      
      n  = new NameValueNode("this port address", p.getAddress().getGuid().toColonString() + ":" + p.getPortNumber());
      tn = new UserObjectTreeNode(n, false);
      vmtn.add(tn);
      
    if (rootEdge != null)
    {
      // add the edge id string, but tag it for which side of the link
      String linkString = "link >";
      OSM_Port rp = rootEdge.getEndPort1();

      if (rootEdge.getEndPort1().equals(p))
      {
        linkString = "link <";
        rp = rootEdge.getEndPort2();
      }

      n = new NameValueNode("linked port address", rp.getAddress().getGuid().toColonString() + ":" + rp.getPortNumber());
      tn = new UserObjectTreeNode(n, false);
      vmtn.add(tn);

      // is this an up or down port (check the vertex)
      n = new NameValueNode("link direction", rootVertex.getPortDirection(p.getPortNumber()));
      tn = new UserObjectTreeNode(n, false);
      vmtn.add(tn);

      n = new NameValueNode(linkString, rootEdge.toEdgeIdString(40));
      tn = new UserObjectTreeNode(n, false);
      vmtn.add(tn);

      n = new NameValueNode("depth", rootEdge.getDepth());
      tn = new UserObjectTreeNode(n, false);
      vmtn.add(tn);

    }
      // if this is a switch (and has a routing table) add it
      addPortRoutes(vmtn, pf);
      
      
      // now add all the counters (last thing)
      addPortCounters(vmtn, pf);
      
     
      return true;
  }
  

  private boolean addPortRoutes(UserObjectTreeNode parent, PFM_Port p)
 {
   // find this port in the switches table, and add it here
    RT_Table rTable = SMT_RouteManager.getInstance().getRouteTable();
    RT_Port pTable = rTable.getRTPort(p.getNodeGuid(), p.port_num);
    OSM_Fabric fabric = SMT_RouteManager.getInstance().getFabric();
    
    RT_PortTreeModel rptm = new RT_PortTreeModel(pTable, rTable, fabric);
    
    // not all ports have routes - they need to be switch ports
    if(rptm.getRootNode() == null)
      return false;
    
   parent.add(rptm.getRootNode());
   return true;
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

  @Override
  public boolean isLeaf(Object node)
  {
    return getChildCount(node) < 1 ? true: false;
  }

  @Override
  public int getChildCount(Object parent)
  {
    return getChildSet(parent).size();
  }
  
  private Set <UserObjectTreeNode> getChildSet(Object parentNode)
  {
    UserObjectTreeNode parent = null;
    
    // the parentNode is either a UserObjectTreeNode, or an OSM_Port
    if(parentNode instanceof UserObjectTreeNode)
    {
      parent = (UserObjectTreeNode) parentNode;
    }
    
    if(parentNode instanceof OSM_Port)
    {
      OSM_Port p = (OSM_Port)parentNode;
      if(p.compareTo(rootPort) == 0)
        parent = rootPortNode;
    }
    
    if(parent == null)
      return null;
 
       Set <UserObjectTreeNode> childSet = new HashSet <UserObjectTreeNode> ();
      
      for (Enumeration <UserObjectTreeNode> c = parent.children(); c.hasMoreElements() ;)
      {
        childSet.add(c.nextElement());
      }
      System.err.println("NumChildren: " + childSet.size());    
      return childSet;
  }
  
  public boolean containsSameRoot(PortTreeModel model)
  {
    // compare the root guids
    return(rootVertex.getGuid().equals(model.getRootVertex().getGuid()));
  }


}
