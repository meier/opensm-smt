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
 *        file: AbstractPortTreeModel.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.tree;

import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.opensm.plugin.data.MLX_ExtPortInfo;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_Port.PortCounterName;
import gov.llnl.lc.infiniband.opensm.plugin.data.SBN_PortInfo;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public abstract class AbstractPortTreeModel extends DefaultTreeModel
{  
  protected UserObjectTreeNode rootPortNode;
  protected IB_Vertex rootVertex;
  protected OSM_Port rootPort;


  public UserObjectTreeNode getRootNode()
  {
    return rootPortNode;
  }
  
  public Object getRoot()
  {
    return rootPort;
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
  public AbstractPortTreeModel(TreeNode root)
  {
    super(root, true);
  }

  public AbstractPortTreeModel(IB_Vertex vertex, OSM_Port port)
  {
    super(null, true);
    // assume it is poorly constructed, lacking link/edge info
    
    // this is the constructor for downed ports or ports without links
    
    // the tree is made up of VertexMemberTreeNodes, which contains a name/value pair
    //    the name is just type, and the value is the object
    //
    //  the root is an OSM_Port, determined by an IB_Edge and an IB_Vertex
    this.rootVertex = vertex;
    
    OSM_Port p = port;
    if(p == null)
    {
      System.err.println("could not find an endport for this vertex (" + rootVertex.getGuid().toColonString() + ")");
      return;
    }
    this.rootPort = p;
    
    NameValueNode      vmn = new NameValueNode("port #", p);
    UserObjectTreeNode vmtn = new UserObjectTreeNode(vmn, true);
    rootPortNode = vmtn;
    
    addPort(rootPortNode);
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
  
  public static OSM_Port getRootPort(UserObjectTreeNode node)
  {
    // the supplied node should be the root node for this model
    if(node != null)
    {
      Object obj = node.getUserObject();
      if((obj != null) && (obj instanceof NameValueNode))
      {
        NameValueNode vmn = (NameValueNode)obj;
        if(vmn.getMemberName().equals("port #"))
        {
          return (OSM_Port)vmn.getMemberObject();
        }
      }
    }
    return null;
  }
  
  public static int getPortNum(UserObjectTreeNode node)
  {
    // this node should be the root node of the model
    OSM_Port port = getRootPort(node);
    if(port != null)
      return port.getPortNumber();
    return -1;
  }
  
  public boolean isLink()
  {
    // return true if vertex and edge are not null
    return false;
  }


  
  public static boolean hasErrors(UserObjectTreeNode node)
  {
    // the supplied node should be the root node for this model
    OSM_Port p = getRootPort(node);
    if (p != null)
      return p.hasError();
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
  
  public boolean updateModel(AbstractPortTreeModel model)
  {
    if(model == null)
      return false;
    
    // walk the model, and update its values
    updateNode(this.getRootNode(), model.getRootNode());
//    this.reload(this.getRootNode());
    return true;
  }
  
  public boolean updateNode(UserObjectTreeNode origNode, UserObjectTreeNode newNode)
  {
    if((origNode == null) || (newNode == null))
      return false;
    
    // walk the children of this node
    if(origNode.getChildCount() != newNode.getChildCount())
    {
      System.err.println("new and old port nodes have different number of children");
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
    this.reload(this.getRootNode());
    return true;
  }
  
  
  private boolean addPort(UserObjectTreeNode vmtn)
  {
      // this edge has two ports, get the one for THIS vertex
      OSM_Port p = rootPort;
      if(p == null)
      {
        System.err.println("could not find an endport for this vertex (" + rootVertex.getGuid().toColonString() + ")");
        return false;
      }
      
      // now vmtn is a parent, so add its elements
      NameValueNode     n  = new NameValueNode("rate", p.getRateString());
      UserObjectTreeNode tn = new UserObjectTreeNode(n, false);
      vmtn.add(tn);

      // only display the lid if its non-zero (switch ports don't have lids?? )
      int lid = p.getAddress().getLocalId();
      if(lid > 0)
      {
        n  = new NameValueNode("lid", p.getAddress().getLocalIdHexString() + " (" + p.getAddress().getLocalId() + ")");
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
      
      addPortCounters(vmtn, p.pfmPort);
     
      return true;
  }
  
  
  protected static boolean addMore(UserObjectTreeNode parent, OSM_Port p)
  {
    SBN_PortInfo pi = p.sbnPort.port_info;
    int numMore = 0;
    
    NameValueNode          vmn = new NameValueNode("more", numMore);
    UserObjectTreeNode    vmtn = new UserObjectTreeNode(vmn, true);
    parent.add(vmtn);
    
    // add the SBN_PortInfo here, which is the ib_port_info data
    
    NameValueNode      mmn = new NameValueNode("local_port_num", pi.local_port_num);
    UserObjectTreeNode mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    mmn = new NameValueNode("link_width_enabled", pi.link_width_enabled);
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    mmn = new NameValueNode("link_width_supported", pi.link_width_supported);
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    mmn = new NameValueNode("link_width_active", pi.link_width_active);
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    mmn = new NameValueNode("state_info1", pi.state_info1);
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    mmn = new NameValueNode("state_info2", pi.state_info2);
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    mmn = new NameValueNode("mkey_lmc", pi.mkey_lmc);
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    mmn = new NameValueNode("link_speed", pi.link_speed);
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    mmn = new NameValueNode("link_speed_ext", pi.link_speed_ext);
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    mmn = new NameValueNode("link_speed_ext_enabled", pi.link_speed_ext_enabled);
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    mmn = new NameValueNode("mtu_smsl", pi.mtu_smsl);
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    mmn = new NameValueNode("vl_cap", pi.vl_cap);
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    mmn = new NameValueNode("vl_high_limit", pi.vl_high_limit);
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    mmn = new NameValueNode("vl_arb_high_cap", pi.vl_arb_high_cap);
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    mmn = new NameValueNode("vl_arb_low_cap", pi.vl_arb_low_cap);
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    mmn = new NameValueNode("mtu_cap", pi.mtu_cap);
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    mmn = new NameValueNode("vl_stall_life", pi.vl_stall_life);
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    mmn = new NameValueNode("vl_enforce", pi.vl_enforce);
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    mmn = new NameValueNode("guid_cap", pi.guid_cap);
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    mmn = new NameValueNode("subnet_timeout", pi.subnet_timeout);
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    mmn = new NameValueNode("resp_time_value", pi.resp_time_value);
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    mmn = new NameValueNode("error_threshold", pi.error_threshold);
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    mmn = new NameValueNode("base_lid", pi.base_lid);
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    mmn = new NameValueNode("master_sm_base_lid", pi.master_sm_base_lid);
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    mmn = new NameValueNode("capability_mask", "0x" + Integer.toHexString(pi.capability_mask));
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);

    mmn = new NameValueNode("capability_mask2", "0x" + Integer.toHexString(pi.capability_mask2));
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);

    mmn = new NameValueNode("diag_code", pi.diag_code);
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    mmn = new NameValueNode("m_key_lease_period", pi.m_key_lease_period);
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    mmn = new NameValueNode("m_key_violations", pi.m_key_violations);
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    mmn = new NameValueNode("p_key_violations", pi.p_key_violations);
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    mmn = new NameValueNode("q_key_violations", pi.q_key_violations);
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    mmn = new NameValueNode("max_credit_hint", pi.max_credit_hint);
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    mmn = new NameValueNode("link_rt_latency", pi.link_rt_latency);
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    mmn = new NameValueNode("m_key", pi.m_key);
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    mmn = new NameValueNode("subnet_prefix", new IB_Guid(pi.subnet_prefix).toColonString());
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    addExtPortInfo(vmtn, p);
    
    // update the more count
    vmn.setMemberValue(vmtn.getChildCount());
    return true;
  }
  
  protected static boolean addExtPortInfo(UserObjectTreeNode parent, OSM_Port p)
  {
    MLX_ExtPortInfo pix = p.sbnPort.ext_port_info;
    int numExt = 0;
    
    NameValueNode          vmn = new NameValueNode("EXT", numExt);
    UserObjectTreeNode    vmtn = new UserObjectTreeNode(vmn, true);
    parent.add(vmtn);
    
    if(pix != null)
    {
      // add the MLX_ExtPortInfo here
      
      NameValueNode      mmn = new NameValueNode("link_speed_active", pix.link_speed_active);
      UserObjectTreeNode mmtn = new UserObjectTreeNode(mmn, false);
      vmtn.add(mmtn);
      
      mmn = new NameValueNode("link_speed_enabled", pix.link_speed_enabled);
      mmtn = new UserObjectTreeNode(mmn, false);
      vmtn.add(mmtn);
      
      mmn = new NameValueNode("link_speed_supported", pix.link_speed_supported);
      mmtn = new UserObjectTreeNode(mmn, false);
      vmtn.add(mmtn);
      
      mmn = new NameValueNode("state_change_enable", pix.state_change_enable);
      mmtn = new UserObjectTreeNode(mmn, false);
      vmtn.add(mmtn);
    }
    
    // update the mlx ext count
    vmn.setMemberValue(vmtn.getChildCount());
    return true;
  }
  
  protected static boolean addPortCounters(UserObjectTreeNode parent, PFM_Port p)
 {
   // active traffic, active errors?, num counters, excluded

   NameValueNode      vmn = new NameValueNode("counters", p.getCounterTimeStamp().toString());
   UserObjectTreeNode vmtn = new UserObjectTreeNode(vmn, true);
   parent.add(vmtn);
   
   addCounterAttributes(vmtn, p);
   return true;
 }
  
  protected static boolean addCounterAttributes(UserObjectTreeNode parent, PFM_Port p)
 {
   // active traffic, active errors?, num counters, excluded
    EnumSet<PortCounterName> sc = p.getSuppressed_Counters();
    if((sc != null) && (sc.size() > 0))
    {
      String suppressed = Arrays.asList(sc.toArray()).toString();
      NameValueNode      vmn = new NameValueNode("suppressed counters", suppressed);
      UserObjectTreeNode vmtn = new UserObjectTreeNode(vmn, false);
      parent.add(vmtn);
    }

     for(PFM_Port.PortCounterName n : PortCounterName.PFM_ALL_COUNTERS)
    {
      // skip supressed counters
      if(!p.getSuppressed_Counters().contains(n))
      {
        NameValueNode      vmn = new NameValueNode(n.getName(), p.getCounter(n));
        UserObjectTreeNode vmtn = new UserObjectTreeNode(vmn, false);
        parent.add(vmtn);
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
  public boolean containsSameRoot(AbstractPortTreeModel model)
  {
    // compare the root guids
    if((model == null) || (model.getRootVertex() == null) || (rootVertex == null))
      return false;
    return(rootVertex.getGuid().equals(model.getRootVertex().getGuid()));
  }


}
