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
 *        file: SystemTreeModel.java
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
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_System;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Edge;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;
import gov.llnl.lc.logging.CommonLogger;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;


public class SystemTreeModel extends FabricTreeModelNew implements CommonLogger
{
  private IB_Guid SysGuid;
  private HashMap <String, IB_Vertex> VertexMap;
  private OSM_System OSM_sys;

  /************************************************************
   * Method Name:
   *  SystemTreeModel
  **/
  /**
   * The SystemTreeModel represents (in tree form) an assembly of
   * switch nodes, identified by a common sys_guid.  A collection
   * of switches assembled in a "chassis" should have the same
   * sys_guid attribute.
   * 
   * A typical "chassis" or "system" might have 18 top level switches
   * with 36 ports each, connected to 36 second level switches, also
   * with 36 ports each (half connected up to the top level, the other
   * half available).  This arrangement would mean the "chassis" or
   * "system" has 54 switches, and 1944 ports, only 648 of which are
   * downward facing, or available, ports.
   * 
   * name:
   * sys_guid:
   * #top level
   * #second level
   * #total ports
   * #internal ports
   * #external ports
   * list of top level
   *
   * @see     describe related java objects
   *
   * @param root
   ***********************************************************/
  public SystemTreeModel(IB_Vertex root)
  {
    super(root);
    // TODO Auto-generated constructor stub
  }
  
  public SystemTreeModel(OSM_Fabric fabric, IB_Guid sysGuid)
  {
    this(null);
    // get an OSM_System object, which contains the structure of the chassis
    OSM_System os = new OSM_System(sysGuid, fabric);

    // Build the Tree Model for System Image Guid
    
    LinkedHashMap<String, IB_Vertex> vertexMap = os.getVertexMap();
    if((vertexMap == null) || (vertexMap.size() == 0))
    {
      logger.severe("The VetexMap for System " + sysGuid.toColonString() + " could not be built (no matching guids?)");
    }
    else
      createModel(os);
  }
  
  private void createModel(OSM_System osm_sys)
  {
    // this is the normal preferred way to construct the tree model
    
    // find the root, or roots, create a virtual root if necessary
    OSM_sys = osm_sys;
    SysGuid = osm_sys.getSysGuid();
    VertexMap = osm_sys.getVertexMap();
    String sysName = osm_sys.getName();
    
    IB_Vertex top = null;
    int maxDepth = IB_Vertex.getMaxDepth(VertexMap);
    LinkedHashMap <String, IB_Vertex> topLevel = IB_Vertex.getVertexMapAtDepth(VertexMap, maxDepth);
    
    
    if(topLevel.size() > 1)
    {
//      System.err.println("Many top levels");
      // there are many top level nodes, so must create an artificial (single) root node
      maxDepth++;
      OSM_Node n = new OSM_Node();  // null constructor for artificial node
      n.sbnNode.node_type = (short) OSM_NodeType.SW_NODE.getType();  // force this to be a switch
      
//      logger.info("The artificial root (level " + maxDepth + ") has " + topLevel.size() + " children");
      top = new IB_Vertex(n, maxDepth, true, false, sysName);  // this is the artificial root node
      
      // connect up my "children" by creating new artificial edges
      int rootPortNum = 0;
      for (Entry<String, IB_Vertex> entry : topLevel.entrySet())
      {
        IB_Vertex v = entry.getValue();
        if((SysGuid.equals(new IB_Guid(v.getNode().sbnNode.sys_guid))))
        {
          // create an artificial edge between them (make up some port numbers)
          OSM_Port rp= new OSM_Port(null, null, OSM_NodeType.SW_NODE);
          rp.setPortNumber(rootPortNum++);
          OSM_Port vp= new OSM_Port(null, null, OSM_NodeType.SW_NODE);
          vp.setPortNumber(-1);        
          
          IB_Edge e = new IB_Edge(top, rp, v, vp);
          top.addEdge(e);
        }
        top.setName(sysName);
      }
    }
    else if (topLevel.size() == 1)
    {
      System.err.println("Single top level");
      // there may be a single vertex at the top of a tree, but more likely this is just
      // a normal switch - not an assembly of switches
      rootReal = true;
      // obtain the next depth, and connect it up
      for (Entry<String, IB_Vertex> entry : topLevel.entrySet())
      {
        IB_Vertex v = entry.getValue();
        top = v;
      }
      // point to the next level down
      topLevel = IB_Vertex.getVertexMapAtDepth(VertexMap, maxDepth -1);
    }
    else
    {
      System.err.println("No top level nodes in the vertex map");
      System.exit(-1);
    }
    
    // Root node for this model has been created as top
    // so fill in its attributes, and then do the next level (children)
    
    
    // create the top level NVN for this System, and fill in its attributes    
    rootVertex = top;
    NameValueNode vmn = new NameValueNode("system", top);
    rootVertexNode = new UserObjectTreeNode(vmn, true);
    
    // these are the details of the structure of the "system"
    addSystemAttributes(rootVertexNode);

    if(rootVertexNode == null)
      System.err.println("The UserObjectTreeNode for the root is NULL");
    else
    {
      addSwitches(rootVertexNode, topLevel, VertexMap);
    }
 } 
  
  private boolean addSystemAttributes(UserObjectTreeNode parent)
  {
    /*  Refer to OSM_System object
     * 
     * name
     * guid
     * # switches (and levels)
     * # ports (active & inactive)
     * # internal ports (active & inactive)
     * # external ports (active & inactive)
     * 
     * #switch list
     */

    NameValueNode      vmn = new NameValueNode("name", OSM_sys.getName());
    UserObjectTreeNode vmtn = new UserObjectTreeNode(vmn, false);
    parent.add(vmtn);

    vmn = new NameValueNode("guid", OSM_sys.getSysGuid().toColonString());
    vmtn = new UserObjectTreeNode(vmn, false);
    parent.add(vmtn);
    
    // is this vertex a switch, or an hca ?
    if(rootVertex.getNode().isSwitch())
    {      
      StringBuffer swStr = new StringBuffer();
      swStr.append(OSM_sys.getTotalSwitches() + " (");
      
      for (int j=OSM_sys.getMaxDepth(); j >= OSM_sys.getMinDepth(); j--)
      {
        swStr.append(OSM_sys.getGuidList(j).size());
        if(j == OSM_sys.getMinDepth())
          swStr.append(")");
        else
          swStr.append("/");
      }
      
      vmn = new NameValueNode("# switches (top/next/etc)", swStr.toString());
      vmtn = new UserObjectTreeNode(vmn, false);
      parent.add(vmtn);
      
      vmn = new NameValueNode("# ports (active/inactive)", OSM_sys.getTotalPorts() + "  (" + OSM_sys.getTotalActivePorts() + "/" + OSM_sys.getTotalInactivePorts() + ")");
      vmtn = new UserObjectTreeNode(vmn, false);
      parent.add(vmtn);
      
      vmn = new NameValueNode("# internal ports (active/inactive)", OSM_sys.getTotalInternalPorts() + "  (" + OSM_sys.getActiveInternalPorts() + "/" + OSM_sys.getInactiveInternalPorts() + ")");
      vmtn = new UserObjectTreeNode(vmn, false);
      parent.add(vmtn);
      
      vmn = new NameValueNode("# external ports (active/inactive)", OSM_sys.getTotalExternalPorts() + "  (" + OSM_sys.getActiveExternalPorts() + "/" + OSM_sys.getInactiveExternalPorts() + ")");
      vmtn = new UserObjectTreeNode(vmn, false);
      parent.add(vmtn);
    }
    
    return true;
  }
  
  private UserObjectTreeNode addSwitches(UserObjectTreeNode parent, HashMap <String, IB_Vertex> neighborMap, HashMap <String, IB_Vertex> vertexMap)
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
      if((v.getDepth() == (myDepth -1)) && (SysGuid.equals(new IB_Guid(v.getNode().sbnNode.sys_guid))))
      {
        // direct child, create and add it
        NameValueNode vmn = new NameValueNode("switch", v);
        UserObjectTreeNode vtn = new UserObjectTreeNode(vmn, true);
        parent.add(vtn);

        // now try to add its children
        addSwitches(vtn, v.getNeighborMap(), vertexMap);
      }
    }
    return parent;
  }

  public IB_Guid getSystemGuid()
  {
      return SysGuid;
  }
    
  public void setSystemGuid(IB_Guid sysGuid)
  {
      SysGuid = sysGuid;
  }
  
  public static String getCommonName(String name1, String name2)
  {
    int minSame = 3;
    
    // case insensitive, but return lower case
    // strip spaces and special characters from end
    
    // handle special first case, when don't have two to compare
    if((name1 == null) || (name1.length() < minSame))
      return name2;
    
    // compare the two strings, using the first one as a template
    // if the first string is a subset of the second, only include the common parts
    //
    String n1 = name1 == null ? "RootNode": name1.toLowerCase().trim();
    String n2 = name2 == null ? "RootNode": name2.toLowerCase().trim();
    String common = "";
    
    // compare the two strings, stop at the first difference
    for(int i=0; i< n1.length(); i++)
    {
      // the characters and positions have to match
      if(n1.charAt(i) == n2.charAt(i))
        common += n1.charAt(i);
      else
        break;
    }
    
    // if they have nothing in common, then just return the original (first) name
    
    if(common.length() >= minSame)
    {
      // don't allow backslashes, colons or semi-colons at the end
      String match = common.trim();
      int len = match.length();
      if(match.endsWith(":") || match.endsWith(";") || match.endsWith("/") || match.endsWith("."))
        return match.substring(0, len-1);
      return match;
    }
    return name1;
  }

  /************************************************************
   * Method Name:
   *  getSystemGuidString
  **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   * @param tn
   * @return
   ***********************************************************/
  public static String getSystemGuidString(UserObjectTreeNode node)
  {
    NameValueNode nvn = getNameValueNode(node, "guid");
    if(nvn != null)
      return (String)nvn.getMemberObject();
    return null;
  }
  
  public static String getSystemNameString(UserObjectTreeNode node)
  {
    NameValueNode nvn = getNameValueNode(node, "name");
    if(nvn != null)
      return (String)nvn.getMemberObject();
    return null;
  }
  
  public String getSystemNameString()
  {
    return rootVertex.getName();
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

  /************************************************************
   * Method Name:
   *  getVertexList
  **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   * @return
   ***********************************************************/
  public ArrayList<IB_Vertex> getVertexList()
  {
    ArrayList<IB_Vertex> vl = new ArrayList<IB_Vertex>(VertexMap.values());
    return vl;
  }
  
}
