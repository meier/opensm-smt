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
 *        file: LinkTreeModel.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.tree;

import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Edge;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class LinkTreeModel extends DefaultTreeModel
{  
  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = 3191404221487003603L;
  /**  describe serialVersionUID here **/
  
  private UserObjectTreeNode rootLinkNode;
  private IB_Vertex rootVertex;
  private IB_Edge rootEdge;


  public UserObjectTreeNode getRootNode()
  {
    return rootLinkNode;
  }
  
  public IB_Edge getRootEdge()
  {
    return rootEdge;
  }
  
  public Object getRoot()
  {
    return rootEdge;
  }
  
  public IB_Vertex getRootVertex()
  {
    return rootVertex;
  }
  

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
  public LinkTreeModel(TreeNode root)
  {
    super(root, true);
    // TODO Auto-generated constructor stub
  }

  public LinkTreeModel(IB_Vertex vertex, int portNumber)
  {
    this(vertex, vertex.getEdge(portNumber));
  }


  public LinkTreeModel(IB_Vertex vertex, IB_Edge edge)
  {
    super(null, true);
    // assume it is fully constructed
    
    // this is the normal preferred way to construct the tree model
    
    // the tree is made up of VertexMemberTreeNodes, which contains a name/value pair
    //    the name is just type, and the value is the object
    //
    //  the root is an OSM_Port, determined by an IB_Edge and an IB_Vertex
    this.rootVertex = vertex;
    this.rootEdge   = edge;
        
    if((edge == null) || (edge.getEndPort1() == null) || (edge.getEndPort2() == null))
      return;
    
    NameValueNode      vmn = new NameValueNode("link", edge);
    UserObjectTreeNode vmtn = new UserObjectTreeNode(vmn, true);
    rootLinkNode = vmtn;
    
    addEdgePorts(rootLinkNode);

    
  }
  

  public LinkTreeModel(IB_Edge edge)
  {
    this(null, edge);
  }
  
 public static IB_Guid getNodeGuid(UserObjectTreeNode node)
  {
    if(node != null)
    {
      String guidString = getGuidString(node);
      if(guidString != null)
        return new IB_Guid(guidString);
    }
    return null;
  }
  
  public static String getGuidString(UserObjectTreeNode node)
  {
    String portAddress = getPortAddressString(node);
    if(portAddress != null)
    {
      // strip the last colon, which is the port number
       int ndex = portAddress.lastIndexOf(":");
      return portAddress.substring(0, ndex);
    }
    return null;
  }
  
  public static String getPortAddressString(UserObjectTreeNode node)
  {
    NameValueNode nvn = getNameValueNode(node, "this port address");
    if(nvn != null)
      return (String)nvn.getMemberObject();
    return null;
  }
  
  public static IB_Edge getRootEdge(UserObjectTreeNode node)
  {
    // the supplied node should be the root node for this model
    if(node != null)
    {
      Object obj = node.getUserObject();
      if((obj != null) && (obj instanceof NameValueNode))
      {
        NameValueNode vmn = (NameValueNode)obj;
        if(vmn.getMemberName().equals("link"))
        {
          return (IB_Edge)vmn.getMemberObject();
        }
      }
    }
    return null;
  }
  
  public boolean isLink()
  {
    // return true if vertex and edge are not null
    return ((rootEdge != null) && (rootVertex != null));
  }


  
  public static boolean hasErrors(UserObjectTreeNode node)
  {
    // the supplied node should be the root node for this model
    IB_Edge e = getRootEdge(node);
    if (e != null)
      return e.hasError();
    return false;
  }

  
  public static NameValueNode getNameValueNode(UserObjectTreeNode node, String name)
  {
    // given a parent node, look for the child node with the given name
    //     must match exactly or return null
    for (Enumeration <UserObjectTreeNode> c = node.children(); c.hasMoreElements() ;)
    {
      UserObjectTreeNode uotn = (UserObjectTreeNode)c.nextElement();
      NameValueNode tst = (NameValueNode)uotn.getUserObject();
      if(name.equals(tst.getMemberName()))
        return tst;
     }
    return null;
  }
  
  public boolean updateModel(LinkTreeModel model)
  {
    if(model == null)
      return false;
    
    // walk the model, and update its values
    updateNode(this.getRootNode(), model.getRootNode());
    this.reload(this.getRootNode());
    return true;
  }
  
  public boolean updateNode(UserObjectTreeNode origNode, UserObjectTreeNode newNode)
  {
    if((origNode == null) || (newNode == null))
      return false;
    
    // walk the children of this node
    if(origNode.getChildCount() != newNode.getChildCount())
    {
      System.err.println("new and old link nodes have different number of children");
//      origNode = newNode;
      return false;
    }
    
    // update this node, and then its children
    NameValueNode ovmn = (NameValueNode) origNode.getUserObject();
    NameValueNode nvmn = (NameValueNode) newNode.getUserObject();
    
    // change just the Object portion
    ovmn.setMemberValue(nvmn.getMemberObject());
    
    // do the children
    for(int index = 0; index < origNode.getChildCount(); index++)
    {
      UserObjectTreeNode origChild = (UserObjectTreeNode) origNode.getChildAt(index);
      UserObjectTreeNode newChild  = (UserObjectTreeNode) newNode.getChildAt(index);
      updateNode(origChild, newChild);
    }
    return true;
  }
  
  
  private boolean addEdgePorts(UserObjectTreeNode vmtn)
  {
      // this edge has two ports
    OSM_Port p = rootEdge.getEndPort1();
      
      // now vmtn is a parent, so add its elements

    NameValueNode     n  = new NameValueNode("state", p.getStateString());
    UserObjectTreeNode tn = new UserObjectTreeNode(n, false);
      vmtn.add(tn);

      n  = new NameValueNode("rate", p.getRateString());
      tn = new UserObjectTreeNode(n, false);
      vmtn.add(tn);

      n  = new NameValueNode("speed", p.getSpeedString());
      tn = new UserObjectTreeNode(n, false);
      vmtn.add(tn);

      n  = new NameValueNode("width", p.getWidthString());
      tn = new UserObjectTreeNode(n, false);
      vmtn.add(tn);
      
      n = new NameValueNode("depth", rootEdge.getDepth());
      tn = new UserObjectTreeNode(n, false);
      vmtn.add(tn);

      n  = new NameValueNode("endport1", p);
      tn = new UserObjectTreeNode(n, true);
      addEdgePort(tn, p);
      vmtn.add(tn);
        
      p = rootEdge.getEndPort2();
      n  = new NameValueNode("endport2", p);
      tn = new UserObjectTreeNode(n, true);
      addEdgePort(tn, p);
      vmtn.add(tn);
        
      return true;
  }
  
  private boolean addEdgePort(UserObjectTreeNode vmtn, OSM_Port p)
  {
      // this edge has two ports
      
      // now vmtn is a parent, so add its elements
      PFM_Port pf = p.pfmPort;

      NameValueNode     n  = new NameValueNode("lid", p.getAddress().getLocalId());
      UserObjectTreeNode tn = new UserObjectTreeNode(n, false);
      vmtn.add(tn);
       
      PortTreeModel.addMore(vmtn, p);

      n  = new NameValueNode("errors", p.hasError());
      tn = new UserObjectTreeNode(n, false);
      vmtn.add(tn);
      
      n  = new NameValueNode("this port address", p.getAddress().getGuid().toColonString() + ":" + p.getPortNumber());
      tn = new UserObjectTreeNode(n, false);
      vmtn.add(tn);
      
    if (rootEdge != null)
    {
      OSM_Port rp = rootEdge.getEndPort1();
      if (rootEdge.getEndPort1().equals(p))
         rp = rootEdge.getEndPort2();
 
       n = new NameValueNode("linked port address", rp.getAddress().getGuid().toColonString() + ":"
          + rp.getPortNumber());
      tn = new UserObjectTreeNode(n, false);
      vmtn.add(tn);

    }
    n = new NameValueNode("depth", rootEdge.getDepth(p));
    tn = new UserObjectTreeNode(n, false);
    vmtn.add(tn);

    // now add all the counters
      PortTreeModel.addPortCounters(vmtn, pf);
      
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
    
    // the parentNode is either a UserObjectTreeNode, or an IB_Edge
    if(parentNode instanceof UserObjectTreeNode)
    {
      parent = (UserObjectTreeNode) parentNode;
    }
    
    if(parentNode instanceof IB_Edge)
    {
      IB_Edge e = (IB_Edge)parentNode;
      if(e.compareTo(rootEdge) == 0)
        parent = rootLinkNode;
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
  
  public boolean containsSameRoot(LinkTreeModel model)
  {
    // compare the root edges
    return(rootEdge.getKey().equals(model.getRootEdge().getKey()));
  }

}
