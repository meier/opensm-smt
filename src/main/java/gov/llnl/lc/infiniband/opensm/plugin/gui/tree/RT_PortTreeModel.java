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
 *        file: RT_PortTreeModel.java
 *
 *  Created on: May 8, 2014
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.tree;

import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.RT_Node;
import gov.llnl.lc.infiniband.opensm.plugin.data.RT_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.RT_Table;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

/**********************************************************************
 * Describe purpose and responsibility of RT_PortTreeModel
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version May 8, 2014 8:26:03 AM
 **********************************************************************/
public class RT_PortTreeModel extends DefaultTreeModel
{
  private UserObjectTreeNode rootPortNode;
  private RT_Port rootRtPort;
  private OSM_Fabric Fabric;
  private RT_Table Table;

  public UserObjectTreeNode getRootNode()
  {
    return rootPortNode;
  }
  
  public Object getRoot()
  {
    return rootRtPort;
  }
  
  public RT_Port getRootPort()
  {
    return rootRtPort;
  }
  
  public RT_Node getParentNode()
  {
    return rootRtPort.getParentNode();
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
    return Table;
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
    Table = table;
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
  public RT_PortTreeModel(TreeNode root)
  {
    // assumes already fully constructed
    super(root, true);
  }

  public RT_PortTreeModel(RT_Port rtPort)
  {
    this(rtPort, null, null);
  }
    
    public RT_PortTreeModel(RT_Port rtPort, RT_Table table, OSM_Fabric fabric)
    {
      super(null, true);
      
    // this is the normal preferred way to construct the tree model
    
    // the tree is made up of UserObjectTreeNodes, which contains a name/value pair
    //    the name is just type, and the value is the object
    //
    //  the root is an RT_Port
    this.rootRtPort = rtPort;
    
    // these two fill in extra info (optional, but highly desired)
    this.Fabric = fabric;
    this.Table  = table;
    
    // go no further
    if(rtPort == null)
      return;

    int nCA = rtPort.getNumCaRoutes(table);
    int nSW = rtPort.getNumSwRoutes(table);
    
    StringBuffer sb = new StringBuffer();
    sb.append(nCA + " CA route");
    if(nCA != 1)
      sb.append("s");
    
    sb.append(", " + nSW + " SW route");
    if(nSW != 1)
      sb.append("s");
    
    sb.append(", total=" + (nCA+nSW));
      

    NameValueNode      nvn = new NameValueNode("routes for port # " + rtPort.getPortNumber(), rtPort);
//    NameValueNode      nvn = new NameValueNode("port # " + rtPort.getPortNumber(), sb.toString());
    UserObjectTreeNode uotn = new UserObjectTreeNode(nvn, true);
    rootPortNode = uotn;
    
    addPortRoutes(rootPortNode);
  }

  private boolean addRouting(UserObjectTreeNode uotn)
  {
      // now uotn is a parent, so add its elements

      NameValueNode     n  = new NameValueNode("parent", getParentNode().getName(getFabric()));
      UserObjectTreeNode tn = new UserObjectTreeNode(n, false);
      uotn.add(tn);

      n  = new NameValueNode("lid", RT_TableTreeModel.getLidValueString(rootRtPort.getLid()));
      tn = new UserObjectTreeNode(n, false);
      uotn.add(tn);

      n  = new NameValueNode("guid", getParentNode().getGuid().toColonString());
      tn = new UserObjectTreeNode(n, false);
      uotn.add(tn);

      // add the individual routes 
      addPortRoutes(uotn);
      
      return true;
  }
  
  private boolean addPortRoutes(UserObjectTreeNode parent)
  {
     for(Map.Entry<String, Integer> entry: rootRtPort.getPortGuidMap().entrySet())
    {
      String name = rootRtPort.getDestinationName(entry, getFabric());
       
      NameValueNode      mmn = new NameValueNode(name, rootRtPort.toPortRouteTreeString(entry, getTable(), getFabric()));
      UserObjectTreeNode mmtn = new UserObjectTreeNode(mmn, false);
      parent.add(mmtn);
     }
    
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
    
    if(parentNode instanceof RT_Port)
    {
      RT_Port p = (RT_Port)parentNode;
      if(p.compareTo(rootRtPort) == 0)
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


}
