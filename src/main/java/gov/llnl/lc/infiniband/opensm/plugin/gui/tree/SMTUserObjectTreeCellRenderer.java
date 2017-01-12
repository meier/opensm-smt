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
 *        file: SMTUserObjectTreeCellRenderer.java
 *
 *  Created on: Jan 13, 2014
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.tree;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_FabricDeltaAnalyzer;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_Port.PortCounterName;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Edge;
import gov.llnl.lc.infiniband.opensm.plugin.gui.data.SmtIconType;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.manager.SMT_AnalysisManager;

/**********************************************************************
 * Describe purpose and responsibility of SMTUserObjectTreeCellRenderer
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Jan 13, 2014 3:14:48 PM
 **********************************************************************/
public class SMTUserObjectTreeCellRenderer extends DefaultTreeCellRenderer implements CommonLogger
{
  
  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = -3158458489318643199L;
  
  private boolean bVertexTreeModel = false;
  private boolean bLinkTreeModel = false;
  
  public SMTUserObjectTreeCellRenderer(boolean useDefault) 
  {
  }
  
  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
      boolean expanded, boolean leaf, int row, boolean hasFocus)
  {

    super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
    
    if((value == null) || !(value instanceof UserObjectTreeNode))
      return this;
    
    // set the model type flag, VertexTreeModel, or PortTreeModel
    setTreeModelType(tree);
    
    // Could be a node (switch or leaf), link, port, counter, or counter value
    
    // determine what type of Object this is, and what state it is in
    // then set the appropriate icon, if any
    UserObjectTreeNode v = (UserObjectTreeNode)value;
    NameValueNode node = (NameValueNode)v.getUserObject();
    
    if(isNode(node))
    {
      if(isSwitchNode(tree, value, node))
        setSwitchNodeIcon(tree, value, node);
      else
        setLeafNodeIcon(tree, value, node);
    }
    else if(isLink(node))
      setLinkIcon(tree, value, node);
    else if(isDownPort(node))
      setIcon(SmtIconType.SMT_PORT_DOWN_ICON.getIcon());
    else if(isPort(node))
      setPortIcon(tree, value, node);
     else if(isCounterValue(node))
      setCounterValueIcon(tree, value, node);
    else if(isCounter(node))
       setCounterIcon(tree, value, node);
    else if(isSpecialValue(node))
      setSpecialIcon(tree, value, node);
    else if(isMore(node))
      setIcon(SmtIconType.SMT_MORE_ICON.getIcon());
    return this;
  }
  
  private UserObjectTreeNode getRootNode(JTree tree)
  {
    // The root node of the tree model can be a node or a port or ede
    if (tree != null)
    {
      TreeModel tm = tree.getModel();
      if (tm != null)
      {
        if (tm instanceof DefaultTreeModel)
        {
          // the vertex guid is in the root node
          Object obj = tm.getRoot();
          if (obj != null)
          {
            if (obj instanceof UserObjectTreeNode)
              return (UserObjectTreeNode) obj;
            else
              logger.severe("couldn't get the root UserObjectTreeNode from this tree");
          }
        }
        else
        {
          // unknown type of model
          logger.severe("The UNKNOWN model is: " + tm.getClass().getName());
        }
      }
      else
        logger.severe("TreeModel is null");
    }
    else
      logger.severe("Tree or TreeNode is null");

    return null;
  }

  private IB_Edge getRootEdge(JTree tree)
  {
    IB_Edge edge = null;
    
    // The root node of the LinkTreeModel is an edge, return it or null
    if (tree != null)
    {
      if(isLinkTreeModel())
        edge = LinkTreeModel.getRootEdge(getRootNode(tree));
    }
    return edge;
  }

  private boolean setTreeModelType(JTree tree)
  {
    // this renderer supports both the VertexTreeModel and the PortTreeModel
    // although both trees are basically just specialized versions of
    // the DefaultTreeModel
    //
    // This method can determine the model type, using knowledge of the
    // structure, instead of using the normal reflection and type
    //
    // This is necessary because they both look like the DefaultTreeModel
    //
    // returns true for VertexTreeModel, and false for PortTreeModel

    boolean success = false;
    UserObjectTreeNode rootNode = getRootNode(tree);
    if(rootNode != null)
    {
      // the links node only exists in the VertexTreeModel, so if this succeeds
      // it must be a VertexTreeModel
      NameValueNode nvn = VertexTreeModel.getNameValueNode(rootNode, "links");
      if (nvn != null)
      {
        success = true; 
        bVertexTreeModel = true;
      }
      else
      {
        // the links endport1 only exists in the VertexTreeModel, so if this succeeds
        // it must be a VertexTreeModel
        nvn = LinkTreeModel.getNameValueNode(rootNode, "endport1");
        if (nvn != null)
        {
          success = true; 
          bLinkTreeModel = true;
        }
      }
    }
    return success;
  }
  
  private boolean hasNodeError(JTree tree, Object value)
  {
    // true if "any" port in this tree model (VertexTreeModel or PortTreeModel) has an error
    if(isVertexTreeModel())
      return VertexTreeModel.hasErrors(getRootNode(tree));
    else if(isLinkTreeModel())
      return hasLinkError(tree, value);
    return hasPortError(tree, getRootNode(tree));
  }

  private boolean hasLinkError(JTree tree, Object value)
  {
    // true if "either" port in this tree model (LinkTreeModel) has an error
    if(isLinkTreeModel())
      return LinkTreeModel.hasErrors(getRootNode(tree));
    
    // if here, either VertexTreeModel or PortTreeModel
    IB_Guid guid = getNodeGuid(tree, value);
    int portNum  = getPortNum(tree, value);

    OSM_FabricDeltaAnalyzer deltaAnalysis = SMT_AnalysisManager.getInstance().getDeltaAnalysis();
    if (deltaAnalysis != null)
    {
      IB_Edge e = deltaAnalysis.getIB_Edge(guid, portNum);
      if(e != null)
        return e.hasError();
    }
    return false;
  }

  private boolean hasPortError(JTree tree, Object value)
  {
    // model can be link, vertex or port
    // check to see if any (static) errors for this port

    // does this port have an error?
    if (isVertexTreeModel())
    {
      int portNum = getPortNum(tree, value);
      if (portNum > 0)
        return VertexTreeModel.hasPortErrors(getRootNode(tree), portNum);
    }
    else if(isLinkTreeModel())
    {
      IB_Edge edge = getRootEdge(tree);
      if (edge != null)
      {
        UserObjectTreeNode uotn = (UserObjectTreeNode) value;
        Object obj = uotn.getUserObject();
        if (obj != null)
        {
          if (obj instanceof NameValueNode)
          {
            NameValueNode nvn = (NameValueNode) obj;
            if (nvn.getMemberName().contains("endport1"))
               return edge.getEndPort1().hasError();
            else if (nvn.getMemberName().contains("endport2"))
              return edge.getEndPort2().hasError();
            // handle port counter  child of root edge
            else if(isCounter(nvn))
            {
              TreeNode ptn = uotn.getParent(); // the endport should be one up from the counter
              UserObjectTreeNode puotn = (UserObjectTreeNode) ptn;

              if (puotn != null)
              {
                // is this the endport?
                Object pobj = puotn.getUserObject();
                if (pobj instanceof NameValueNode)
                {
                  NameValueNode pnvn = (NameValueNode) pobj;
                   if (pnvn.getMemberName().contains("endport1"))
                     return edge.getEndPort1().hasError();
                  else if (pnvn.getMemberName().contains("endport2"))
                    return edge.getEndPort2().hasError();
                  }
                }
              }
          }
        }
      }
     
    }
    else
      return PortTreeModel.hasErrors(getRootNode(tree));
    return false;
  }

  private boolean hasCounterError(JTree tree, Object value, NameValueNode node)
  {
    // is this node an error counter, and if so, is its value non-zero
    String nodeName = node.getMemberName();
    PortCounterName pcn = PortCounterName.getByName(nodeName);

    if((pcn != null) && (PortCounterName.PFM_ERROR_COUNTERS.contains(pcn)))
    {
      // return true ONLY if this has an error
      if(node.getMemberObject() instanceof Long)
      {
        Long errCnt = (Long)node.getMemberObject();
        if(errCnt.longValue() > 0)
          return true;
       }
    }
    return false;
  }
  
  private boolean hasSpecialError(JTree tree, Object value, NameValueNode node)
  {
    // true for non-zero values in the error counters, otherwise false
    // all dynamic errors will also have this static error
    
    // works for port #, errors, counters, and then individual error counter values
    
    String nodeName = node.getMemberName();
    
    if(nodeName.equals("errors"))
    {
      if(node.getMemberObject() instanceof Boolean)
        return (Boolean)node.getMemberObject();      
    }
    return false;
  }

  private IB_Guid getNodeGuid(JTree tree, Object value)
  {
    // get the guid for this nodes root node.
    // The root node of the tree model can be a node or a port,
    // unless its a link model, then there are two guids
    
    IB_Guid guid = null;
    
    if(isVertexTreeModel())
      guid = VertexTreeModel.getNodeGuid(getRootNode(tree));
    else if (isLinkTreeModel())
    {
      // return one of two guids, depending on the value
      UserObjectTreeNode v = (UserObjectTreeNode)value;
      guid = LinkTreeModel.getNodeGuid(getRootNode(tree));
    }
    else if(isPortTreeModel())
      guid = PortTreeModel.getNodeGuid(getRootNode(tree));
    return guid;
  }

  private boolean isPortTreeModel()
  {
    return (!bLinkTreeModel) && (!bVertexTreeModel);
  }

  private boolean isLinkTreeModel()
  {
    return bLinkTreeModel;
  }

  private int getPortNum(JTree tree, Object value)
  {
    // get the port number for this tree node.
    // The root node of the tree model can be a node or a port
    // the tree node MUST be a child of a port node, or else it can't be determined
    int portNum = -1;
    
    if((tree != null) && (value != null))
    {
      UserObjectTreeNode v = (UserObjectTreeNode)value;
      if(isVertexTreeModel())
        portNum = VertexTreeModel.getPortNum(getRootNode(tree), (UserObjectTreeNode) v);
      else if(isLinkTreeModel())
      {
        // return one of two port numbers, depending on the value
        System.err.println("Kill off this code, cant determine port number of from this tree");
        portNum = -1;
      }
      else
        portNum = PortTreeModel.getPortNum(getRootNode(tree));
    }
    return portNum;
  }

  private boolean hasDynamicCounterError(JTree tree, Object value, NameValueNode node)
  {
    // get the Delta information, and see if the delta error for this port
    // counter
    // has a non-zero value
    IB_Guid guid = null;
    int portNum = -1;
    IB_Edge edge = getRootEdge(tree);
    if (edge != null)
    {
      // for link models only (need to walk up the tree to find the correct
      // endport)
      UserObjectTreeNode uotn = (UserObjectTreeNode) value;
      TreeNode ptn = uotn.getParent().getParent(); // the endport should be two
                                                   // up from the counter value
      UserObjectTreeNode puotn = (UserObjectTreeNode) ptn;
      Object obj = uotn.getUserObject();
      if (obj != null)
      {
        if (puotn != null)
        {
          // is this the endport?
          Object pobj = puotn.getUserObject();
          if (pobj instanceof NameValueNode)
          {
            NameValueNode pnvn = (NameValueNode) pobj;
            if (pnvn.getMemberName().contains("endport1"))
            {
              guid = edge.getEndPort1().getNodeGuid();
              portNum = edge.getEndPort1().getPortNumber();
            }
            if (pnvn.getMemberName().contains("endport2"))
            {
              guid = edge.getEndPort2().getNodeGuid();
              portNum = edge.getEndPort2().getPortNumber();
            }
          }
        }
      }
    }
    else
    {
      // this should work for vertex and port models
      guid = getNodeGuid(tree, value);
      portNum = getPortNum(tree, value);
    }

    if ((guid != null) && (portNum > 0))
    {
      // if here, I have a node guid, port number, and the counter name

      // does this counter have a dynamic error?
      OSM_FabricDeltaAnalyzer deltaAnalysis = SMT_AnalysisManager.getInstance().getDeltaAnalysis();
      if (deltaAnalysis != null)
        return deltaAnalysis.hasDynamicError(guid, portNum,
            PFM_Port.PortCounterName.getByName(node.getMemberName()));
    }
    else
    {
      logger.severe("Couldn't get a guid for this tree node (" + portNum + ")");
    }
    return false;
  }

  private boolean hasDynamicPortError(JTree tree, Object value)
  {
    // model can be link, vertex or port
    // check to see if any dynamic errors

    IB_Guid guid = null;
    int portNum = -1;
    IB_Edge edge = getRootEdge(tree);
    if (edge != null)
    {
      UserObjectTreeNode uotn = (UserObjectTreeNode) value;
      Object obj = uotn.getUserObject();
      if (obj != null)
      {
        if (obj instanceof NameValueNode)
        {
          NameValueNode nvn = (NameValueNode) obj;
          if (nvn.getMemberName().contains("endport1"))
          {
            guid = edge.getEndPort1().getNodeGuid();
            portNum = edge.getEndPort1().getPortNumber();
          }
          else if (nvn.getMemberName().contains("endport2"))
          {
            guid = edge.getEndPort2().getNodeGuid();
            portNum = edge.getEndPort2().getPortNumber();
          }
          // handle port counter  child of root edge
          else if(isCounter(nvn))
          {
            TreeNode ptn = uotn.getParent(); // the endport should be one up from the counter
            UserObjectTreeNode puotn = (UserObjectTreeNode) ptn;

            if (puotn != null)
            {
              // is this the endport?
              Object pobj = puotn.getUserObject();
              if (pobj instanceof NameValueNode)
              {
                NameValueNode pnvn = (NameValueNode) pobj;
                 if (pnvn.getMemberName().contains("endport1"))
                {
                  guid = edge.getEndPort1().getNodeGuid();
                  portNum = edge.getEndPort1().getPortNumber();
                }
                if (pnvn.getMemberName().contains("endport2"))
                {
                  guid = edge.getEndPort2().getNodeGuid();
                  portNum = edge.getEndPort2().getPortNumber();
                }
              }
            }
           }

        }
      }
    }
    else
    {
      // this should work for vertex and port models
      guid = getNodeGuid(tree, value);
      portNum = getPortNum(tree, value);
    }

    if ((guid != null) && (portNum > 0))
    {
      // if here, I have a node guid, port number

      // does this port have a dynamic error?
      OSM_FabricDeltaAnalyzer deltaAnalysis = SMT_AnalysisManager.getInstance().getDeltaAnalysis();
      if (deltaAnalysis != null)
        return deltaAnalysis.hasDynamicError(guid, portNum);
    }
    else
    {
      logger.severe("Couldn't get a guid or port for this tree node (" + guid + ":" + portNum + ")");
      logger.severe("NodeName is (" + getNameValueNodeName(value) + ")");
    }

    return false;
  }
  
  private boolean hasDynamicLinkError(JTree tree, Object value)
  {
    // model must be LinkTree
    // check to see if any dynamic errors on either port
    if((tree != null) && (value != null))
    {
      IB_Edge edge = getRootEdge(tree);
      
       OSM_FabricDeltaAnalyzer deltaAnalysis = SMT_AnalysisManager.getInstance().getDeltaAnalysis();
      if ((deltaAnalysis != null) && (edge != null))
        return deltaAnalysis.hasDynamicError(edge);

    }
    return false;
  }
  
  private boolean isVertexTreeModel()
  {
    return bVertexTreeModel;
  }

  private boolean hasDynamicNodeError(JTree tree, Object value)
  {
    // true if "any" port in this tree model (VertexTreeModel or PortTreeModel) has
    // dynamic errors

    if(isVertexTreeModel())
    {
      IB_Guid guid = getNodeGuid(tree, value);
      OSM_FabricDeltaAnalyzer deltaAnalysis = SMT_AnalysisManager.getInstance().getDeltaAnalysis();
      if ((deltaAnalysis != null) && (guid != null))
        return deltaAnalysis.hasDynamicError(guid);
    }
    else if(isLinkTreeModel())
    {
      return hasDynamicLinkError(tree, value);
    }
    else
    {
      return hasDynamicPortError(tree, getRootNode(tree));
    }
    return false;
  }

  private boolean isTopNode(JTree tree, Object value)
  {
    // true if "any" port in this tree model (VertexTreeModel or PortTreeModel) is in the top list

    if(tree != null)
    {
      IB_Guid guid = getNodeGuid(tree, value);
      OSM_FabricDeltaAnalyzer deltaAnalysis = SMT_AnalysisManager.getInstance().getDeltaAnalysis();
      if ((deltaAnalysis != null) && (guid != null))
        return deltaAnalysis.isTopNode(guid, SMTFabricTreeCellRenderer.maxTop);
    }
    return false;
  }

  private boolean isTopLink(JTree tree, Object value)
  {
    // true if "either" port in this tree model (LinkTreeModel) is in the top
    // list
    if ((tree != null) && (isLinkTreeModel()))
    {
      IB_Edge edge = getRootEdge(tree);

      OSM_FabricDeltaAnalyzer deltaAnalysis = SMT_AnalysisManager.getInstance().getDeltaAnalysis();
      if ((deltaAnalysis != null) && (edge != null))
        return deltaAnalysis.isTopLink(edge, SMTFabricTreeCellRenderer.maxTop);
    }
    else if ((tree != null) && (getNameValueNodeName(value).contains("links")))
    {
      return isTopNode(tree, value);
    }
    else if (tree != null)
    {
      // this should work for vertex and port models
      IB_Guid guid = getNodeGuid(tree, value);
      int portNum = getPortNum(tree, value);
      
      OSM_FabricDeltaAnalyzer deltaAnalysis = SMT_AnalysisManager.getInstance().getDeltaAnalysis();
      if ((deltaAnalysis != null) && (guid != null))
        return deltaAnalysis.isTopLink(guid, portNum, SMTFabricTreeCellRenderer.maxTop);
    }
    return false;
  }

  private String getNameValueNodeName(Object value)
  {
    String name = null;
    UserObjectTreeNode uotn = (UserObjectTreeNode) value;
    Object obj = uotn.getUserObject();
    if (obj != null)
    {
      if (obj instanceof NameValueNode)
      {
        NameValueNode nvn = (NameValueNode) obj;
        name = nvn.getMemberName();
      }
    }
    return name;
  }

  private boolean isTopPort(JTree tree, Object value)
  {
    // true if "any" counter in this port is is in the top list
    
    // model can be vertex or port
    // check to see if any dynamic errors
    IB_Guid guid = null;
    int portNum = -1;
    IB_Edge edge = getRootEdge(tree);
    if (edge != null)
    {

      UserObjectTreeNode uotn = (UserObjectTreeNode) value;
      Object obj = uotn.getUserObject();
      if (obj != null)
      {
        if (obj instanceof NameValueNode)
        {
          // support port, counter, and data counter
          NameValueNode nvn = (NameValueNode) obj;
          PortCounterName pcn = PortCounterName.getByName(nvn.getMemberName());
          if (nvn.getMemberName().contains("endport1"))
          {
            guid = edge.getEndPort1().getNodeGuid();
            portNum = edge.getEndPort1().getPortNumber();
          }
          else if (nvn.getMemberName().contains("endport2"))
          {
            guid = edge.getEndPort2().getNodeGuid();
            portNum = edge.getEndPort2().getPortNumber();
          }
          // handle port counter  child of root edge
          else if(isCounter(nvn))
          {
            TreeNode ptn = uotn.getParent(); // the endport should be one up from the counter
            UserObjectTreeNode puotn = (UserObjectTreeNode) ptn;

            if (puotn != null)
            {
              // is this the endport?
              Object pobj = puotn.getUserObject();
              if (pobj instanceof NameValueNode)
              {
                NameValueNode pnvn = (NameValueNode) pobj;
                if (pnvn.getMemberName().contains("endport1"))
                {
                  guid = edge.getEndPort1().getNodeGuid();
                  portNum = edge.getEndPort1().getPortNumber();
                }
                if (pnvn.getMemberName().contains("endport2"))
                {
                  guid = edge.getEndPort2().getNodeGuid();
                  portNum = edge.getEndPort2().getPortNumber();
                }
              }
            }
           }
          // handle data counter value children of root edge
          else if(PortCounterName.PFM_DATA_COUNTERS.contains(pcn))
          {
             TreeNode ptn = uotn.getParent().getParent(); // the endport should be two up from the counter value
            UserObjectTreeNode puotn = (UserObjectTreeNode) ptn;

            if (puotn != null)
            {
              // is this the endport?
              Object pobj = puotn.getUserObject();
              if (pobj instanceof NameValueNode)
              {
                NameValueNode pnvn = (NameValueNode) pobj;
                if (pnvn.getMemberName().contains("endport1"))
                {
                  guid = edge.getEndPort1().getNodeGuid();
                  portNum = edge.getEndPort1().getPortNumber();
                }
                if (pnvn.getMemberName().contains("endport2"))
                {
                  guid = edge.getEndPort2().getNodeGuid();
                  portNum = edge.getEndPort2().getPortNumber();
                }
              }
            }
           }
         }
      }
    }
    else
    {
      // this should work for vertex and port models
      guid    = getNodeGuid(tree, value);
      portNum = getPortNum(tree, value);
    }

    if ((guid != null) && (portNum > 0))
    {
      // if here, I have a node guid, port number
      OSM_FabricDeltaAnalyzer deltaAnalysis = SMT_AnalysisManager.getInstance().getDeltaAnalysis();
      if (deltaAnalysis != null)
        return deltaAnalysis.isTopPort(guid, portNum, SMTFabricTreeCellRenderer.maxTop);
    }
    else
    {
      logger.severe("Couldn't get a guid or port for this tree node (" + guid + ":" + portNum + ")");
      logger.severe("NodeName is (" + getNameValueNodeName(value) + ")");
    }
    return false;
  }

  private boolean isDownPort(NameValueNode value)
  {
    String nodeName = value.getMemberName();
    if(nodeName == null)
      return false;

    if(nodeName.contains("down port"))
      return true;

    return false;
  }

  private boolean isMore(NameValueNode value)
  {
    String nodeName = value.getMemberName();
    if(nodeName == null)
      return false;

    if(nodeName.contains("more"))
      return true;

    return false;
  }

  private boolean isTopCounter(JTree tree, Object value, NameValueNode node)
  {
    // true if this is a Traffic node, AND if
    // "any" counter in this port is is in the top list
    
    // model can be vertex or port
    // check to see if any dynamic errors
    
    PortCounterName pcn = PFM_Port.PortCounterName.getByName(node.getMemberName());
    if(PFM_Port.PortCounterName.PFM_DATA_COUNTERS.contains(pcn))
    {
      return isTopPort(tree, value);
    }
    return false;
  }

  private boolean isSwitchNode(JTree tree, Object value, NameValueNode node)
  {
    // must be a node, which has more than one link, or number of ports
    // has a non-zero value
    if((tree != null) && (isNode(node)))
    {
      UserObjectTreeNode rootNode = getRootNode(tree);
      int numPorts = VertexTreeModel.getNumPorts(rootNode);
      
      if(numPorts > 1)
        return true;
    }
    return false;
  }

  private boolean isNode(NameValueNode value)
  {
    // return true if this object is a counter node or value
    String nodeName = value.getMemberName();
    
    if((nodeName != null) && (nodeName.startsWith("node")))
      return true;

    return false;
  }

  private boolean isLink(NameValueNode value)
  {
    // return true if this object is a counter node or value
    String nodeName = value.getMemberName();
    if(nodeName == null)
      return false;

    if((nodeName.contains("links")) || (nodeName.contains("link >")) || (nodeName.contains("link <") || (nodeName.equals("link"))))
      return true;

    return false;
  }

  private boolean isPort(NameValueNode value)
  {
    // return true if this object is a counter node or value
    String nodeName = value.getMemberName();
    if(nodeName == null)
      return false;

    if(nodeName.contains("port #") || nodeName.contains("endport"))
      return true;

    return false;
  }

  private boolean isCounter(NameValueNode value)
  {
    // return true if this object is a counter node or value
    String nodeName = value.getMemberName();
    if(nodeName == null)
      return false;

    if(nodeName.contains("counters"))
      return true;
    return false;
  }

  private boolean isCounterValue(NameValueNode value)
  {
    // return true if this object is a counter node or value
    String nodeName = value.getMemberName();
    if(nodeName == null)
      return false;

    if(nodeName.contains("suppressed counters"))
      return true;
    
    PortCounterName pcn = PortCounterName.getByName(nodeName);
    if((pcn != null) && (PortCounterName.PFM_ALL_COUNTERS.contains(pcn)))
      return true;

    return false;
  }

  private boolean isSpecialValue(NameValueNode value)
  {
    // return true if this object is one of the special attribures
    String nodeName = value.getMemberName();
    if(nodeName == null)
      return false;
    if(nodeName.contains("errors"))
      return true;
    return false;
  }

  
  private boolean setSwitchNodeIcon(JTree tree, Object value, NameValueNode node)
  {
    // can ONLY occur if VertexTreeModel
    //
    // there are four different switch node icons
    // normal
    // dynamic error
    // top traffic
    // dynamic error with top traffic
    // one of four switch icons
    if(hasDynamicNodeError(tree, value))
    {
      if(isTopNode(tree, value))
        setIcon(SmtIconType.SMT_SWITCH_BOTH_ICON.getIcon());
      else
        setIcon(SmtIconType.SMT_SWITCH_ERR_ICON.getIcon());
    }
    else if(isTopNode(tree, value)) 
      setIcon(SmtIconType.SMT_SWITCH_TOP_ICON.getIcon());
    else
      setIcon(SmtIconType.SMT_SWITCH_ICON.getIcon());
    
    return false;
  }
  
  private boolean setLeafNodeIcon(JTree tree, Object value, NameValueNode node)
  {
    // can ONLY occur if VertexTreeModel
    //
    // there are four different leaf node icons
    // normal
    // dynamic error
    // top traffic
    // dynamic error with top traffic
    if(hasDynamicNodeError(tree, value))
    {
      if(isTopNode(tree, value))
        setIcon(SmtIconType.SMT_LEAF_BOTH_ICON.getIcon());
      else
        setIcon(SmtIconType.SMT_LEAF_ERR_ICON.getIcon());
    }
    else if(isTopNode(tree, value)) 
      setIcon(SmtIconType.SMT_LEAF_TOP_ICON.getIcon());
    else 
      setIcon(SmtIconType.SMT_LEAF_ICON.getIcon());
    
    return false;
  }

  private boolean setLinkIcon(JTree tree, Object value, NameValueNode node)
  {
    // occurs in VertexTreeModel, LinkTreeModel, and PortTreeModel
    //
    // there are a variety of link icons
    // normal
    // dynamic error
    // top traffic
    // dynamic error with top traffic
    // broken link
    if(hasDynamicNodeError(tree, value))
    {
      if(isTopLink(tree, value))
        setIcon(SmtIconType.SMT_LINK_BOTH_ICON.getIcon());
      else
        setIcon(SmtIconType.SMT_LINK_DERR_ICON.getIcon());
    }
    else if(isTopLink(tree, value)) 
      setIcon(SmtIconType.SMT_LINK_TOP_ICON.getIcon());
    else if(hasLinkError(tree, value))
      setIcon(SmtIconType.SMT_LINK_ERR_ICON.getIcon());
    else
      setIcon(SmtIconType.SMT_LINK_ICON.getIcon());
    return false;
  }

  private boolean setPortIcon(JTree tree, Object value, NameValueNode node)
  {
    // there are a variety of port icons
    // normal
    // dynamic error
    // top traffic
    // dynamic error with top traffic
    if(hasDynamicPortError(tree, value))
    {
      if(isTopPort(tree, value))
        setIcon(SmtIconType.SMT_PORT_BOTH_ICON.getIcon());
      else
        setIcon(SmtIconType.SMT_PORT_DERR_ICON.getIcon());
    }
    else if(isTopPort(tree, value))
      setIcon(SmtIconType.SMT_PORT_TOP_ICON.getIcon());
    else if(hasPortError(tree, value))
      setIcon(SmtIconType.SMT_PORT_ERR_ICON.getIcon());
    else
      setIcon(SmtIconType.SMT_PORT_ICON.getIcon());
    
    return false;
  }

  private boolean setCounterIcon(JTree tree, Object value, NameValueNode node)
  {
    // there are a variety of counter icons
    // normal
    // dynamic error
    // top traffic
    // dynamic error with top traffic
    // broken link
    if (hasDynamicPortError(tree, value))
    {
      if(isTopPort(tree, value))
        setIcon(SmtIconType.SMT_COUNTER_BOTH_ICON.getIcon());
      else
        setIcon(SmtIconType.SMT_COUNTER_DERR_ICON.getIcon());
    }
    else if(isTopPort(tree, value))
      setIcon(SmtIconType.SMT_COUNTER_TOP_ICON.getIcon());
    else if(hasPortError(tree, value))
      setIcon(SmtIconType.SMT_COUNTER_ERR_ICON.getIcon());
    else
      setIcon(SmtIconType.SMT_COUNTER_ICON.getIcon());
    
    return false;
  }

  private boolean setCounterValueIcon(JTree tree, Object value, NameValueNode node)
  {
    // support traffic (top), and error (static & dynamic) counters
    String nodeName = node.getMemberName();
    if(nodeName.contains("suppressed counters"))
    {
      String val = (String)node.getMemberObject();
      if((val != null) && (val.length() > 4))
        setIcon(SmtIconType.SMT_INFORMATION_ICON.getIcon());
      return false;
    }
    
    // normal is for zero values, so continue only if non-zero
    if(node.getMemberObject() instanceof Long)
    {
      Long cnt = (Long)node.getMemberObject();
      if(cnt.longValue() > 0)
      {
        // precedent is top, dynamic error, static error, then normal
        // top is for xmit and rcv
        // both errs is for the various error types

        if(hasDynamicCounterError(tree, value, node))
          setIcon(SmtIconType.SMT_CNT_VAL_DERR_ICON.getIcon());
        else if (hasCounterError(tree, value, node))
          setIcon(SmtIconType.SMT_CNT_VAL_ERR_ICON.getIcon());
        else if (isTopCounter(tree, value, node))
          setIcon(SmtIconType.SMT_CNT_VAL_TOP_ICON.getIcon());
       }
    }
    return false;
  }

  private boolean setSpecialIcon(JTree tree, Object value, NameValueNode node)
  {
    // currently only the "errors" tree node
    if(hasSpecialError(tree, value, node))
      setIcon(SmtIconType.SMT_CNT_VAL_ERR_ICON.getIcon());
    
    return false;
  }

}
