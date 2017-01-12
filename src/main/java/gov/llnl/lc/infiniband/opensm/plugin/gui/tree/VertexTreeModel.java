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
 *        file: VertexTreeModel.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.tree;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_NodeType;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_Node;
import gov.llnl.lc.infiniband.opensm.plugin.data.SBN_Node;
import gov.llnl.lc.infiniband.opensm.plugin.data.SBN_Switch;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Edge;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;

//public class VertexTreeModel implements TreeModel
public class VertexTreeModel extends DefaultTreeModel
{  
  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = -1429373028786396386L;
  
  private UserObjectTreeNode rootVertexNode;
  private IB_Vertex rootVertex;
  
  private ArrayList<OSM_Port> PortList = null;

  public UserObjectTreeNode getRootNode()
  {
    return rootVertexNode;
  }
  
  public Object getRoot()
  {
    return rootVertexNode;
  }
  
  public IB_Vertex getRootVertex()
  {
    return rootVertex;
  }
  

  /************************************************************
   * Method Name:
   *  VertexTreeModel
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param root
   * @param asksAllowsChildren
   ***********************************************************/
  public VertexTreeModel(TreeNode root)
  {
    super(root, true);
  }


//  public VertexTreeModel(IB_Vertex vertex)
//  {
//    this(vertex, null);
//  }
//  
  public VertexTreeModel(IB_Vertex vertex, ArrayList<OSM_Port> portList)
  {
    super(null, true);
    // assume it is fully constructed
    
    // this is the normal preferred way to construct the tree model
    
    // the tree is made up of VertexMemberTreeNodes, which contains a name/value pair
    //    the name is just type, and the value is the object
    //
    //  the root is an IB_Vertex
    this.rootVertex = vertex;
    this.PortList   = portList;
    NameValueNode vmn = new NameValueNode("node", vertex);
    this.rootVertexNode = new UserObjectTreeNode(vmn, true);
    
    //     the next level of children are node attributes, and a collection of IB_Edges
    addVertexAttributes(rootVertexNode);
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
  
  public static IB_Guid getNodeGuid(UserObjectTreeNode node)
  {
    IB_Vertex v = getRootVertex(node);
    if (v != null)
      return v.getGuid();
    return null;
  }
  
  public static int getNumPorts(UserObjectTreeNode node)
  {
    // the supplied node should be the root node for this model
    IB_Vertex v = getRootVertex(node);
    if (v != null)
      return v.getNumPorts();
    return 0;
  }
  
  public static boolean hasErrors(UserObjectTreeNode node)
  {
    // the supplied node should be the root node for this model
    IB_Vertex v = getRootVertex(node);
    if (v != null)
      return v.hasError();
    return false;
  }
  
  public static boolean hasPortErrors(UserObjectTreeNode node, int portNum)
  {
    // the supplied node should be the root node for this model
    IB_Vertex v = getRootVertex(node);
    if (v != null)
    {
      IB_Edge e = v.getEdge(portNum);
      if(e != null)
        return e.hasError(v);
      else
        return true;
    }
    return false;
  }
  
  public static boolean hasDynamicError(UserObjectTreeNode node)
  {
    // the supplied node should be the root node for this model
    IB_Vertex v = getRootVertex(node);
    if (v != null)
      return v.hasDynamicError();
    return false;
  }
  
  public static IB_Vertex getRootVertex(UserObjectTreeNode node)
  {
    // the supplied node should be the root node for this model
    if(node != null)
    {
      Object obj = node.getUserObject();
      if((obj != null) && (obj instanceof NameValueNode))
      {
        NameValueNode vmn = (NameValueNode)obj;
        if(vmn.getMemberName().equals("node"))
        {
          return (IB_Vertex)vmn.getMemberObject();
        }
      }
    }
    return null;
  }
  
  public static int getPortNum(UserObjectTreeNode rootNode, UserObjectTreeNode childNode)
  {
    int portNum = -1;
    
    // the childNode must be port specific, else return -1
    
    // start at the child node, and work my way up the hierarchy until I discover the
    // the port number, or reach the root node
    while((rootNode != null) && (childNode != null) && (rootNode != childNode) && (portNum < 0))
    {
        portNum = PortTreeModel.getPortNum(childNode);
        childNode = (UserObjectTreeNode)childNode.getParent();
    }
    return portNum;
  }

  
  public boolean updateModel(VertexTreeModel model)
  {
    if(model == null)
      return false;
    
    // walk the model, and update its values
    updateNode(this.getRootNode(), model.getRootNode());
//    this.reload(this.getRootNode());
    return true;
  }
  
  protected boolean updateNode(UserObjectTreeNode origNode, UserObjectTreeNode newNode)
  {
    if((origNode == null) || (newNode == null))
      return false;
    
    // walk the children of this node
    if(origNode.getChildCount() != newNode.getChildCount())
    {
      System.err.println("new and old vertex nodes have different number of children (" + origNode.getChildCount() + " vs " + newNode.getChildCount() + ")");
      return false;
    }
    
    // update this node, and then its children
    NameValueNode ovmn = (NameValueNode) origNode.getUserObject();
    NameValueNode nvmn = (NameValueNode) newNode.getUserObject();
    
    // change just the Object portion
    ovmn.setMemberValue(nvmn.getMemberObject());
    
    // do the children (assume that if the same number of children, then they are in same order
    for(int index = 0; index < origNode.getChildCount(); index++)
    {
      UserObjectTreeNode origChild = (UserObjectTreeNode) origNode.getChildAt(index);
      UserObjectTreeNode newChild  = (UserObjectTreeNode) newNode.getChildAt(index);
      if(!updateNode(origChild, newChild))
      {
        // if it didn't work, then this child (node) must have issues with its children
        //   may have to just replace, instead of update
        replaceNode1(origChild, newChild);
//        break;  // only handle one change at a time  TODO, fix for loop so that child count can be modified
      }
    }
    this.reload(this.getRootNode());

    return true;
  }
  
  // make an attempt to handle the case where the new and old nodes have different number of children
  private boolean replaceNode1(UserObjectTreeNode origNode, UserObjectTreeNode newNode)
  {
    if((origNode == null) || (newNode == null))
      return false;
    
    UserObjectTreeNode p = (UserObjectTreeNode)origNode.getParent();
    int i = p.getIndex(origNode);
    System.err.println("the parents child at index (" + i  + ") has (" + origNode.getChildCount() + ") children");
    // completely remove, and replace the original node from its parent
//    p.remove(origNode);
    p.remove(i);
    p.insert(newNode, i);
    System.err.println("completely replacing it at the index (" + i  + ") with child which has (" + newNode.getChildCount() + ") children");

    return true;
  }

  // make an attempt to handle the case where the new and old nodes have different number of children
  private boolean replaceNode(UserObjectTreeNode origNode, UserObjectTreeNode newNode)
  {
    if((origNode == null) || (newNode == null))
      return false;
    
    int newChildren = newNode.getChildCount();
    int oldChildren = origNode.getChildCount();
    
    if(oldChildren == newChildren)
    {
      System.err.println("should not be here, number of children are the same (" + oldChildren  + ")");
      return false;
    }
    // do I have to add, or subtract children??
    boolean bAddChild = newChildren > oldChildren? true: false;
    
    // update this node, and then its children
    NameValueNode ovmn = (NameValueNode) origNode.getUserObject();
    NameValueNode nvmn = (NameValueNode) newNode.getUserObject();
    
    // change just the Object portion
    ovmn.setMemberValue(nvmn.getMemberObject());
    
    // 1. The orig child matches the new child
    //    just update it
    // 2. The orig child does not exist in new children
    //    remove orig child from parent
    // 3. The new children have more than the original
    //    add to the original somehow.

    if(bAddChild)
    {
      System.err.println("I have more new children, then I did before");
      // find the new one(s) and add them, update the rest
      for(int index = 0; index < newNode.getChildCount(); index++)
      {
        UserObjectTreeNode newChild  = (UserObjectTreeNode) newNode.getChildAt(index);
        UserObjectTreeNode oldChild  = getMatchingChildFromNode(newChild, origNode);
        
        // if there is no matching old Child, then it must be a new one, so add it
        if(oldChild == null)
        {
          origNode.add(newChild);
          NameValueNode ncv = (NameValueNode) newChild.getUserObject();
          System.err.println("Adding new child: " + ncv.getMemberName() + ncv.getMemberObject().toString() + " to original Node");
        }
        else
        {
          // found a match, so just update it with new info
          updateNode(oldChild, newChild);
        }
      }
    }
    else
    {
      System.err.println("I have less new children, then I did before");
      // find the missing one(s) and remove them, update the rest
      for(int index = 0; index < origNode.getChildCount(); index++)
      {
        UserObjectTreeNode oldChild  = (UserObjectTreeNode) origNode.getChildAt(index);
        UserObjectTreeNode newChild  = getMatchingChildFromNode(oldChild, newNode);
        
        // if there is no matching new Child, then it must be removed
        if(newChild == null)
        {
          // TODO - will this cause a bug with the getChildCount(), changing the array, while using it?
          NameValueNode ncv = (NameValueNode) oldChild.getUserObject();
          System.err.println("Removing missing child: " + ncv.getMemberName() + ncv.getMemberObject().toString() + " from original Node");

          origNode.remove(oldChild);
        }
        else
        {
          // found a match, so just update it with new info
          updateNode(oldChild, newChild);
        }
      }
    }
    return true;
  }

  private UserObjectTreeNode getMatchingChildFromNode(UserObjectTreeNode oChild, UserObjectTreeNode newNode)
  {
    // given a child node and a parent node, find and return a child (of the parent) that matches
    if((oChild != null) && (newNode != null) && (newNode.getChildCount() > 0))
    {
      NameValueNode ovmn = (NameValueNode) oChild.getUserObject();
      // walk through the children, until a match is found
      for(int index = 0; index < newNode.getChildCount(); index++)
      {
        UserObjectTreeNode nChild = (UserObjectTreeNode) newNode.getChildAt(index);
        // is this the same(ish) as oChild??, if so return it
        
        NameValueNode nvmn = (NameValueNode) nChild.getUserObject();
        String v1 = nvmn.getMemberName() + nvmn.getMemberObject().toString();
        String v2 = ovmn.getMemberName() + ovmn.getMemberObject().toString();
        System.err.println("v1: " + v1);
        System.err.println("v2: " + v2);
        if(v1.compareToIgnoreCase(v2) == 0)
          return nChild;
      }
    }
    return null;
  }

  private boolean addVertexAttributes(UserObjectTreeNode parent)
  {
    int num_ports = rootVertex.getNumPorts();
    int num_links = rootVertex.getEdges().size();
    int num_down  = num_ports - num_links;

    // name, guid, depth, lid, type
    NameValueNode      vmn = new NameValueNode("name", rootVertex.getName());
    UserObjectTreeNode vmtn = new UserObjectTreeNode(vmn, false);
    parent.add(vmtn);

    vmn = new NameValueNode("guid", rootVertex.getGuid().toColonString());
    vmtn = new UserObjectTreeNode(vmn, false);
    parent.add(vmtn);
    
    // if this is NOT A SWITCH, add the lid (ports lid is the nodes lid)
      IB_Edge e = rootVertex.getFirstEdge();
      if (e != null)
      {
        OSM_Port p = e.getEndPort(rootVertex);
        if (p != null)
        {
          vmn = new NameValueNode("lid", p.getAddress().getLocalIdHexString() + " (" + p.getAddress().getLocalId() + ")");
//          vmn = new NameValueNode("lid", p.getAddress().getLocalIdHexString());
          vmtn = new UserObjectTreeNode(vmn, false);
          parent.add(vmtn);
        }
      }

    vmn = new NameValueNode("depth", rootVertex.getDepth());
    vmtn = new UserObjectTreeNode(vmn, false);
    parent.add(vmtn);

    OSM_NodeType t = OSM_NodeType.get(rootVertex.getNode());
    
    // is this the subnet manager?
    String nType = t.getFullName();
    if(rootVertex.isManagementNode())
      nType = t.getFullName() + " (Subnet Manager)";
    

    vmn = new NameValueNode("type", nType);
    vmtn = new UserObjectTreeNode(vmn, false);
    parent.add(vmtn);
    
    // show all the "other" information that is a bit more esoteric
    addMore(parent);

    vmn = new NameValueNode("num ports", num_ports);
    vmtn = new UserObjectTreeNode(vmn, false);
    parent.add(vmtn);

    // ports are either connected or not.  Connected ones form links or edges.
    //  show the dangling ports first, then the ones associated with links
    if(num_down > 0)
      addDanglingPorts(parent);
    //  each IB_Edge has children which consists of attributes, and a collection of PortCounters
    
    int up = (rootVertex.getUpLinkNumbers() == null) ? 0: rootVertex.getUpLinkNumbers().size();
    int dn = (rootVertex.getDownLinkNumbers() == null) ? 0: rootVertex.getDownLinkNumbers().size();
    
    vmn = new NameValueNode("up links", up);
    vmtn = new UserObjectTreeNode(vmn, false);
    parent.add(vmtn);
    
    vmn = new NameValueNode("down links", dn);
    vmtn = new UserObjectTreeNode(vmn, false);
    parent.add(vmtn);
    
    // if this is a channel adapter, just show the port
    if (num_ports == 1)
      return addEdgePorts(parent);

     return addVertexEdges(parent);
  }
  
  private boolean addMore(UserObjectTreeNode parent)
  {
    SBN_Node sbnNode = rootVertex.getNode().sbnNode;
    PFM_Node pfmNode = rootVertex.getNode().pfmNode;
    SBN_Switch sbnSwitch = rootVertex.getSbnSwitch();
    
    int numMore = 0;
    
    NameValueNode          vmn = new NameValueNode("more", numMore);
    UserObjectTreeNode    vmtn = new UserObjectTreeNode(vmn, true);
    parent.add(vmtn);
    
    // add any "node" or "switch" based information here (sbn then pfm)
    
    NameValueNode      mmn = new NameValueNode("description", sbnNode.description);
    UserObjectTreeNode mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    /*=======================================================================*/
    // if this is a switch, then put the SBN_Switch info here.
    if(sbnSwitch != null)
    {
      mmn = new NameValueNode("lft table size", sbnSwitch.lft_size);
      mmtn = new UserObjectTreeNode(mmn, false);
      vmtn.add(mmtn);
      
      mmn = new NameValueNode("endport links", sbnSwitch.endport_links);
      mmtn = new UserObjectTreeNode(mmn, false);
      vmtn.add(mmtn);
      
      mmn = new NameValueNode("num hops", sbnSwitch.num_hops);
      mmtn = new UserObjectTreeNode(mmn, false);
      vmtn.add(mmtn);
      
//   same as above
//      mmn = new NameValueNode("hops", sbnSwitch.hops.length);
//      mmtn = new UserObjectTreeNode(mmn, false);
//      vmtn.add(mmtn);
      
      mmn = new NameValueNode("mft block num", sbnSwitch.mft_block_num);
      mmtn = new UserObjectTreeNode(mmn, false);
      vmtn.add(mmtn);
      
      mmn = new NameValueNode("mft position", sbnSwitch.mft_position);
      mmtn = new UserObjectTreeNode(mmn, false);
      vmtn.add(mmtn);
      
      mmn = new NameValueNode("num of mcm", sbnSwitch.num_of_mcm);
      mmtn = new UserObjectTreeNode(mmn, false);
      vmtn.add(mmtn);
      
      mmn = new NameValueNode("sbn guid", new IB_Guid(sbnSwitch.guid).toColonString());
      mmtn = new UserObjectTreeNode(mmn, false);
      vmtn.add(mmtn);
      
//      mmn = new NameValueNode("dimn ports", sbnSwitch.dimn_ports);
//      mmtn = new UserObjectTreeNode(mmn, false);
//      vmtn.add(mmtn);
//      
      mmn = new NameValueNode("is mc member", sbnSwitch.is_mc_member);
      mmtn = new UserObjectTreeNode(mmn, false);
      vmtn.add(mmtn);
     }
    
    /*=======================================================================*/
    
    mmn = new NameValueNode("base_version", sbnNode.base_version);
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    mmn = new NameValueNode("class_version", sbnNode.class_version);
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    mmn = new NameValueNode("device_id", sbnNode.device_id);
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    mmn = new NameValueNode("partition_cap", sbnNode.partition_cap);
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    mmn = new NameValueNode("port_num_vendor_id", sbnNode.port_num_vendor_id);
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    mmn = new NameValueNode("revision", sbnNode.revision);
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    mmn = new NameValueNode("sys_guid", new IB_Guid(sbnNode.sys_guid).toColonString());
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    mmn = new NameValueNode("port_guid", new IB_Guid(sbnNode.port_guid).toColonString());
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    // Perfmanager stuff
    mmn = new NameValueNode("node_name", pfmNode.node_name);
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    mmn = new NameValueNode("active", pfmNode.active);
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    mmn = new NameValueNode("esp0", pfmNode.esp0);
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);   
    
    // update the more count
    vmn.setMemberValue(vmtn.getChildCount());
    return true;
  }
  
  private boolean addDanglingPorts(UserObjectTreeNode parent)
  {
    int num_ports = rootVertex.getNumPorts();
    int num_links = rootVertex.getEdges().size();
    int num_down  = num_ports - num_links;
    boolean hasChildren = ((PortList != null) && (PortList.size() > 0) && (num_down > 0));
    
    if(PortList == null)
      System.err.println("The PortList is STILL null");
    
    NameValueNode          vmn = new NameValueNode("down ports", num_down);
    UserObjectTreeNode    vmtn = new UserObjectTreeNode(vmn, true);
    parent.add(vmtn);

    if(hasChildren)
        addDownPorts(vmtn);
    
    return true;
  }
  
  private boolean addDownPorts(UserObjectTreeNode parent)
  {
    // loop on the edges and ports, include things not in the edge list
    ArrayList <IB_Edge> el = rootVertex.getEdges();
    
    // The portList should be larger than or equal to the edge list
    for(OSM_Port p: PortList)
    {
      boolean found_it = false;
      for(IB_Edge e: el)
      {
        // if this port is in the edgelist, don't add (skip) it
        if(e.hasPort(p))
        {
          found_it = true;
          break;
        }
      }
      // gone through all the links, did I find the connected port?
      if(!found_it)
      {
        // not found in the edge/link list, so this must be a down port
        PortTreeModel ptm = new PortTreeModel(rootVertex, p);
        parent.add(ptm.getRootNode());
      }
   }
     return true;
  }
   

  /************************************************************
   * Method Name:
   *  addVertexEdges
  **/
  /**
   * Add the Edges, which are valid links.  Only ports that are connected
   * to remote ports are included here.
   *
   * @see     describe related java objects
   *
   * @param parent
   * @return
   ***********************************************************/
  private boolean addVertexEdges(UserObjectTreeNode parent)
  {
    //           each PortCounter has a name, value, change
    // num ports, num with errors, num up, down, etc
    NameValueNode      vmn = new NameValueNode("links", rootVertex.getEdges().size());
    UserObjectTreeNode vmtn = new UserObjectTreeNode(vmn, true);
    parent.add(vmtn);
    
    return addEdgePorts(vmtn);
  }
  
  
  private boolean addEdgePorts(UserObjectTreeNode parent)
  {
    // loop on the edges, and add stuff, but do it in "port" order
    int numPorts = rootVertex.getNumPorts();
     
    for(int portNum = 1; portNum <= numPorts; portNum++)
    {
      IB_Edge e = rootVertex.getEdge(portNum);
      if(e != null)
      {
        PortTreeModel ptm = new PortTreeModel(rootVertex, e);
        parent.add(ptm.getRootNode());
      }
     }
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
    UserObjectTreeNode parent = (UserObjectTreeNode) parentNode;
    Set <UserObjectTreeNode> childSet = new HashSet <UserObjectTreeNode> ();
    
    for (Enumeration <UserObjectTreeNode> c = parent.children(); c.hasMoreElements() ;)
    {
      childSet.add(c.nextElement());
    }
    System.err.println("NumChildren: " + childSet.size());    
    return childSet;
  }

  public boolean containsSameRoot(VertexTreeModel model)
  {
    // compare the root guids
    return(rootVertex.getGuid().equals(model.getRootVertex().getGuid()));
  }



}
