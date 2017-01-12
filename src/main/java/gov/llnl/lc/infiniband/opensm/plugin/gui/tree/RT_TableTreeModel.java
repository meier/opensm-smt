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
 *        file: RT_TableTreeModel.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.tree;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import gov.llnl.lc.infiniband.core.IB_Address;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.RT_Node;
import gov.llnl.lc.infiniband.opensm.plugin.data.RT_Table;
import gov.llnl.lc.logging.CommonLogger;

/**********************************************************************
 * Describe purpose and responsibility of RT_PortTreeModel
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version May 8, 2014 8:26:03 AM
 **********************************************************************/
public class RT_TableTreeModel extends DefaultTreeModel implements CommonLogger
{
  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = 656122180657363550L;
  
  private UserObjectTreeNode rootTreeNode;
  private RT_Table rootNode;
  private OSM_Fabric Fabric;

  public UserObjectTreeNode getRootNode()
  {
    return rootTreeNode;
  }
  
  public Object getRoot()
  {
    return rootNode;
  }
    
  /************************************************************
   * Method Name:
   *  getFabric
   **/
  /**
   * Returns the value of fabric
   *
   * @return the fabric
   *
   ***********************************************************/
  
  public OSM_Fabric getFabric()
  {
    return Fabric;
  }

  /************************************************************
   * Method Name:
   *  setFabric
   **/
  /**
   * Sets the value of fabric
   *
   * @param fabric the fabric to set
   *
   ***********************************************************/
  public void setFabric(OSM_Fabric fabric)
  {
    Fabric = fabric;
  }
 
  /************************************************************
   * Method Name:
   *  getTable
   **/
  /**
   * Returns the value of table
   *
   * @return the table
   *
   ***********************************************************/
  
  public RT_Table getTable()
  {
    return rootNode;
  }

  /************************************************************
   * Method Name:
   *  setTable
   **/
  /**
   * Sets the value of table
   *
   * @param table the table to set
   *
   ***********************************************************/
  public void setTable(RT_Table table)
  {
    rootNode = table;
  }

  /************************************************************
   * Method Name:
   *  RT_TableTreeModel
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param root
   * @param asksAllowsChildren
   ***********************************************************/
  public RT_TableTreeModel(TreeNode root)
  {
    // assumes already fully constructed
    super(root, true);
  }

  public RT_TableTreeModel(RT_Table table)
  {
    this(table, null);
  }
    
    public RT_TableTreeModel(RT_Table table, OSM_Fabric fabric)
    {
      super(null, true);
      
    // this is the normal preferred way to construct the tree model
    
    // the tree is made up of UserObjectTreeNodes, which contains a name/value pair
    //    the name is just type, and the value is the object
    //
    //  the root is an RT_Port
    this.rootNode = table;
    
    // these two fill in extra info (optional, but highly desired)
    this.Fabric = fabric;
    
    // go no further
    if(rootNode == null)
      return;

    HashMap<String, RT_Node> nMap = rootNode.getSwitchGuidMap();

    if(nMap == null)
    {
      logger.severe("could not find a switch map for this table (" + rootNode.toString() + ")");
      return;
    }
    
    NameValueNode      nvn = new NameValueNode("routing tables", rootNode.getSwitchGuidMap().size());
    UserObjectTreeNode uotn = new UserObjectTreeNode(nvn, true);
    rootTreeNode = uotn;
    
    addSwitchTables(rootTreeNode);
  }

  private boolean addSwitchTables(UserObjectTreeNode uotn)
  {
      // now uotn is a parent, so add its elements

    NameValueNode     n  = new NameValueNode("fabric name", Fabric.getFabricName());
    UserObjectTreeNode tn = new UserObjectTreeNode(n, false);
    uotn.add(tn);

    n  = new NameValueNode("total table size", rootNode.getNumRoutes());
    tn = new UserObjectTreeNode(n, false);
    uotn.add(tn);

      n  = new NameValueNode("type", rootNode.getTableType().getTypeName());
      tn = new UserObjectTreeNode(n, false);
      uotn.add(tn);

      n  = new NameValueNode("time stamp", rootNode.getTableAge());
      tn = new UserObjectTreeNode(n, false);
      uotn.add(tn);

      n  = new NameValueNode("num lids", rootNode.getLidGuidMap().size());
      tn = new UserObjectTreeNode(n, false);
      uotn.add(tn);

      n  = new NameValueNode("min lid", getLidValueString(rootNode.getMinLid()));
      tn = new UserObjectTreeNode(n, false);
      uotn.add(tn);

      n  = new NameValueNode("max lid", getLidValueString(rootNode.getMaxLid()));
      tn = new UserObjectTreeNode(n, false);
      uotn.add(tn);

      n  = new NameValueNode("num Channel Adapters", rootNode.getNumChannelAdapters());
      tn = new UserObjectTreeNode(n, false);
      uotn.add(tn);

      // add the individual routes 
      addRoutes(uotn);
      
      return true;
  }
  
public static String getLidValueString(int lid)
{
  return IB_Address.toLidHexString(lid) + " (" + lid + ")";
}

  
  private boolean addRoutes(UserObjectTreeNode parent)
  {
    NameValueNode          vmn = new NameValueNode("num Switches (with a routing table)", 0);
    UserObjectTreeNode    vmtn = new UserObjectTreeNode(vmn, true);
    parent.add(vmtn);
    
    for(Map.Entry<String, RT_Node> entry: rootNode.getSwitchGuidMap().entrySet())
    {
      // build and add the RT_NodeTreeModel here
      RT_NodeTreeModel rptm = new RT_NodeTreeModel(entry.getValue(), getTable(), getFabric());
      vmtn.add(rptm.getRootNode());
    }
    
    // update the more count
    vmn.setMemberValue(vmtn.getChildCount());
    return true;
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
    
    if(parentNode instanceof RT_Table)
    {
      RT_Table p = (RT_Table)parentNode;
      if(p.compareTo(rootNode) == 0)
        parent = rootTreeNode;
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


}
