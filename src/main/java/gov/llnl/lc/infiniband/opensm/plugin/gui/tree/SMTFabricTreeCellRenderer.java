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
 *        file: SMTFabricTreeCellRenderer.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.tree;

import java.awt.Component;
import java.util.Enumeration;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;

import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_FabricDeltaAnalyzer;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_NodeType;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;
import gov.llnl.lc.infiniband.opensm.plugin.gui.data.SmtIconType;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.manager.SMT_AnalysisManager;

/**********************************************************************
 * Describe purpose and responsibility of SMTFabricTreeCellRenderer
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Jan 13, 2014 3:14:48 PM
 **********************************************************************/
public class SMTFabricTreeCellRenderer extends DefaultTreeCellRenderer implements CommonLogger
{
  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = -3158458489318643199L;
  
  public static final int maxTop = 20;
  
  public SMTFabricTreeCellRenderer(boolean useDefault) 
  {
  }
  
  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
      boolean expanded, boolean leaf, int row, boolean hasFocus)
  {

    super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
    
    if((value == null) || !(value instanceof UserObjectTreeNode))
      return this;
    
    // determine what type of Object this is, and what state it is in
    // then set the appropriate icon, if any
    UserObjectTreeNode n = (UserObjectTreeNode)value;
    NameValueNode nvn = (NameValueNode) n.getUserObject();
    
    // this could be a vertex, a string, or an integer (see the SystemTreeModel)
    Object o = nvn.getMemberObject();
    
    if(o instanceof String)
    {
      return this;
    }

    if(o instanceof Integer)
    {
      return this;
    }
    
    // handle the vertex object
    if(o instanceof IB_Vertex)
    {
      IB_Vertex v = (IB_Vertex) o;
      
      // Root, Switch, Leaf - all nodes
      if(isRootFabricNode(tree, n, v))
      {
        if(hasFabricErrors(tree, n, v))
          setIcon(SmtIconType.SMT_FABRIC_DERR_ICON.getIcon());
        else
          setIcon(SmtIconType.SMT_FABRIC_ICON.getIcon());
      }
      else if(isLeafNode(tree, n, v))
      {
        // one of four leaf icons
        if(hasDynamicError(tree, n, v, false))
          if(isTopNode(tree, n, v))
            setIcon(SmtIconType.SMT_LEAF_BOTH_ICON.getIcon());
          else
            setIcon(SmtIconType.SMT_LEAF_ERR_ICON.getIcon());
        else if(isTopNode(tree, n, v))
          setIcon(SmtIconType.SMT_LEAF_TOP_ICON.getIcon());
        else
          setIcon(SmtIconType.SMT_LEAF_ICON.getIcon());
      }
      else
      {
        // one of four switch icons
        if(hasDynamicError(tree, n, v, true))
          if(hasTopTraffic(tree, n, v))
            setIcon(SmtIconType.SMT_SWITCH_BOTH_ICON.getIcon());
          else
            setIcon(SmtIconType.SMT_SWITCH_ERR_ICON.getIcon());
        else if(hasTopTraffic(tree, n, v))
          setIcon(SmtIconType.SMT_SWITCH_TOP_ICON.getIcon());
        else
          setIcon(SmtIconType.SMT_SWITCH_ICON.getIcon());
      }
    }

    return this;
  }

  private boolean hasErrors(JTree tree, UserObjectTreeNode node, IB_Vertex vertex)
  {
    // return true if this node has an error
    // has this changed since last time?)
    
    return false;
  }

  private boolean hasDynamicError(JTree tree, UserObjectTreeNode node, IB_Vertex vertex, boolean immediateChildren)
  {
    // return true if this node, or any of its children have a dynamic error
    // does this counter have a dynamic error?
    
    if(isErrorNode(tree, node, vertex))
      return true;
    
      // check the children, if any
      if(!isLeafNode(tree, node, vertex))
      {
        // iterate through the children
        Enumeration<UserObjectTreeNode> e = node.children();
        while(e.hasMoreElements())
        {
          UserObjectTreeNode ftn = e.nextElement();
          
          Object o = ftn.getUserObject();
          if((o != null) && (o instanceof NameValueNode))
          {
            NameValueNode nvn = (NameValueNode)o;
            Object mo         = nvn.getMemberObject();
            if((mo != null) && (mo instanceof IB_Vertex))
            {
              IB_Vertex v = (IB_Vertex) mo;
              // return immediately if this child has dynamic errors
              if(immediateChildren)
              {
                if(isErrorNode(tree, ftn, v))
                  return true;
              }
              else
                if(hasDynamicError(tree, ftn, v, false))
                  return true;
            }
          }
        }
      }
    return false;
  }

  private boolean hasTopTraffic(JTree tree, UserObjectTreeNode node, IB_Vertex vertex)
  {
    // return true if this node, or any of its children is a Top Node
    
    if(isTopNode(tree, node, vertex))
      return true;
    
      // check the children, if any
      if(!isLeafNode(tree, node, vertex))
      {
        // iterate through the children
        Enumeration<UserObjectTreeNode> e = node.children();
        while(e.hasMoreElements())
        {
          UserObjectTreeNode ftn = e.nextElement();
          
          
          Object o = ftn.getUserObject();
          if((o != null) && (o instanceof NameValueNode))
          {
            NameValueNode nvn = (NameValueNode)o;
            Object mo         = nvn.getMemberObject();
            if((mo != null) && (mo instanceof IB_Vertex))
            {
              // return immediately if this child is a Top Node
              if(hasTopTraffic(tree, ftn, (IB_Vertex)mo))
                return true;
            }
          }
        }
      }
    return false;
  }

  private boolean isRootFabricNode(JTree tree, UserObjectTreeNode node, IB_Vertex vertex)
  {
    // return true if this object is the top level node (abstract root)
    // that represents the fabric
   if(tree != null)
    {
      TreeModel tm = tree.getModel();
      if(tm != null)
      {
         Object obj = tm.getRoot();
         if((obj != null) && (obj instanceof UserObjectTreeNode))
         {
           UserObjectTreeNode ftn = (UserObjectTreeNode) obj;
           Object o = ftn.getUserObject();
           if((o != null) && (o instanceof NameValueNode))
           {
             NameValueNode nvn = (NameValueNode)o;
             Object mo         = nvn.getMemberObject();
             if((mo != null) && (mo instanceof IB_Vertex))
             {
               IB_Vertex v = (IB_Vertex) mo;
               if((v != null) && (v.equals(vertex)))
                 return true;               
             }
           }
         }
       }
    }
    return false;
  }

  private boolean isErrorNode(JTree tree, UserObjectTreeNode node, IB_Vertex vertex)
  {
    // return true if this node has a dynamic error
    OSM_FabricDeltaAnalyzer deltaAnalysis = SMT_AnalysisManager.getInstance().getDeltaAnalysis();
    if(deltaAnalysis != null)
    {
      if(deltaAnalysis.hasDynamicError(vertex.getGuid()))
        return true;
    }
    return false;
  }

  private boolean isLeafNode(JTree tree, UserObjectTreeNode node, IB_Vertex vertex)
  {
    // return true if this object doesn't have children and isn't a switch
    if((node != null) && (node.isLeaf()))
    {
      // return true if this is not a switch
      if(vertex != null)
        if(!(OSM_NodeType.SW_NODE.getFullName().equals(vertex.getNodeType())))
            return true;
    }
     return false;
  }

  private boolean isTopNode(JTree tree, UserObjectTreeNode node, IB_Vertex vertex)
  {
    // return true if this node is one with the most traffic
    OSM_FabricDeltaAnalyzer deltaAnalysis = SMT_AnalysisManager.getInstance().getDeltaAnalysis();
    if(deltaAnalysis != null)
    {
      if(deltaAnalysis.isTopNode(vertex.getGuid(), maxTop))
        return true;
    }
    return false;
  }

  private boolean hasFabricErrors(JTree tree, UserObjectTreeNode node, IB_Vertex vertex)
  {
    // return true if there was at least one dynamic error anywhere in the fabric
    return hasDynamicError(tree, node, vertex, false);
  }

}
