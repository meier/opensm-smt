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
 *        file: RT_PathTreeModel.java
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
import gov.llnl.lc.infiniband.opensm.plugin.data.RT_Path;
import gov.llnl.lc.infiniband.opensm.plugin.data.RT_PathLeg;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Edge;

public class RT_PathTreeModel extends DefaultTreeModel
{  
  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = -235035673794380855L;
  
  private UserObjectTreeNode rootPathNode;
  private RT_Path rootPath;
  private boolean includeNodes;


  public UserObjectTreeNode getRootNode()
  {
    return rootPathNode;
  }
  
  public Object getRoot()
  {
    return rootPath;
  }
  
  public RT_Path getRootPath()
  {
    return rootPath;
  }
  
  public boolean includesNodes()
  {
    return includeNodes;
  }
  

  /************************************************************
   * Method Name:
   *  RT_PathTreeModel
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param root
   * @param asksAllowsChildren
   ***********************************************************/
  public RT_PathTreeModel(TreeNode root)
  {
    super(root, true);
    // TODO Auto-generated constructor stub
  }


  public RT_PathTreeModel(RT_Path path, boolean includeNodes)
  {
    super(null, true);
    // assume it is fully constructed
    this.includeNodes = includeNodes;
    
    // this is the normal preferred way to construct the tree model
    
    // the tree is made up of VertexMemberTreeNodes, which contains a name/value pair
    //    the name is just type, and the value is the object
    //
    //  the root is an OSM_Port, determined by an IB_Edge and an IB_Vertex
    this.rootPath = path;
        
    if((rootPath == null) || (rootPath.getLegs() == null) || (rootPath.getFabric() == null))
      return;
    
    NameValueNode      vmn = new NameValueNode("path", path.getPathIdString());
    UserObjectTreeNode vmtn = new UserObjectTreeNode(vmn, true);
    rootPathNode = vmtn;
    
    NameValueNode      n  = new NameValueNode("# hops", path.getLegs().size());
    UserObjectTreeNode ctn = new UserObjectTreeNode(n, false);
    vmtn.add(ctn);

    IB_Guid src = path.getSource();
    IB_Guid dst = path.getDestination();
    int portNum = path.isSwitch(src) ? 0: 1;
        
    n  = new NameValueNode("src node", path.getNodeName(src));
    UserObjectTreeNode sn = new UserObjectTreeNode(n, true);
    vmtn.add(sn);

    n  = new NameValueNode("src guid", src.toColonString());
    ctn = new UserObjectTreeNode(n, false);
    sn.add(ctn);

    n  = new NameValueNode("src lid", RT_TableTreeModel.getLidValueString(path.getLid(src)));
    ctn = new UserObjectTreeNode(n, false);
    sn.add(ctn);

    n  = new NameValueNode("src type", path.getTypeString(src, true));
    ctn = new UserObjectTreeNode(n, false);
    sn.add(ctn);

    n  = new NameValueNode("src exit port #", portNum);
    ctn = new UserObjectTreeNode(n, false);
    sn.add(ctn);

    addLinksAndNodes(vmtn, sn);
    
    n  = new NameValueNode("dst node", path.getNodeName(dst));
    UserObjectTreeNode dn = new UserObjectTreeNode(n, true);
    vmtn.add(dn);

    n  = new NameValueNode("dst guid", dst.toColonString());
    ctn = new UserObjectTreeNode(n, false);
    dn.add(ctn);

    n  = new NameValueNode("dst lid", RT_TableTreeModel.getLidValueString(path.getLid(dst)));
    ctn = new UserObjectTreeNode(n, false);
    dn.add(ctn);

    n  = new NameValueNode("dst type", path.getTypeString(dst, true));
    ctn = new UserObjectTreeNode(n, false);
    dn.add(ctn);

    portNum = path.isSwitch(dst) ? 0: 1;
    n  = new NameValueNode("dst entry port #", portNum);
    ctn = new UserObjectTreeNode(n, false);
    dn.add(ctn);


  }
  
  public static RT_Path getRootPath(UserObjectTreeNode node)
  {
    // the supplied node should be the root node for this model
    if(node != null)
    {
      Object obj = node.getUserObject();
      if((obj != null) && (obj instanceof NameValueNode))
      {
        NameValueNode vmn = (NameValueNode)obj;
        if(vmn.getMemberName().equals("path"))
        {
          return (RT_Path)vmn.getMemberObject();
        }
      }
    }
    return null;
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
  
  public boolean updateModel(RT_PathTreeModel model, boolean includeNodes)
  {
    if(model == null)
      return false;
    
    // walk the model, and update its values
    updateNode(this.getRootNode(), model.getRootNode(), includeNodes);
    this.reload(this.getRootNode());
    return true;
  }
  
  public boolean updateNode(UserObjectTreeNode origNode, UserObjectTreeNode newNode, boolean includeNodes)
  {
    if((origNode == null) || (newNode == null))
      return false;
    
    // walk the children of this node
    if(origNode.getChildCount() != newNode.getChildCount())
    {
      System.err.println("new and old path nodes have different number of children");
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
      updateNode(origChild, newChild, includeNodes);
    }
    return true;
  }
  
  private boolean addLinksAndNodes(UserObjectTreeNode vmtn, UserObjectTreeNode sn)
  {
      // a LINK from the previous node, to the next NODE
      //
      // use the pathlegs to obtain the info
    ArrayList<RT_PathLeg> legs = rootPath.getLegs();
    
    int num = 0;
    int pnum = 1;
    for(RT_PathLeg leg: legs)
    {
      NameValueNode     n  = new NameValueNode("next node", "->" + leg.getFromNodeName());
      UserObjectTreeNode tn = new UserObjectTreeNode(n, true);
      UserObjectTreeNode nn = includeNodes ? tn: vmtn;

      // skip the first one (redundant, its the source)
      if(num != 0)
      {
        if(includeNodes)
        {
          vmtn.add(tn);
          pnum = addPathLeg(tn, leg, pnum);         
        }
        addPathLink(nn, leg);
      }
      else
      {
        // this is the initial leg, just add its link to the parent
        pnum = leg.getToPort().getPortNumber();
        if(includeNodes)
          addPathLink(sn, leg);
        else
          addPathLink(nn, leg);

      }
      num++;
    }
      return true;
  }
  
  private int addPathLeg(UserObjectTreeNode vmtn, RT_PathLeg leg, int entryPort)
  {
    // edges or links are direction-less, whereas path legs have a source and destination
    
    /** use this LEG to show the previous (FROM) node info **/
    
    NameValueNode      n  = new NameValueNode("guid", leg.getFromPort().getAddress().getGuid().toColonString());
    UserObjectTreeNode tn = new UserObjectTreeNode(n, false);
    vmtn.add(tn);
    
    n  = new NameValueNode("lid", RT_TableTreeModel.getLidValueString(rootPath.getLid(leg.getFromPort().getAddress().getGuid())));
    tn = new UserObjectTreeNode(n, false);
    vmtn.add(tn);
    
    n  = new NameValueNode("type", rootPath.getTypeString(leg.getFromPort().getAddress().getGuid(), true));
    tn = new UserObjectTreeNode(n, false);
    vmtn.add(tn);
    
    /* passed in, because this LEG doesn't have the previous exit port number */
    n  = new NameValueNode("entry port #", entryPort);
    tn = new UserObjectTreeNode(n, false);
    vmtn.add(tn);
    
    n  = new NameValueNode("exit port #", leg.getFromPort().getPortNumber());
    tn = new UserObjectTreeNode(n, false);
    vmtn.add(tn);
    
    return leg.getToPort().getPortNumber();
  }
 
  private boolean addPathLink(UserObjectTreeNode vmtn, RT_PathLeg leg)
  {
    // find the link associated with this leg, and add it
    String edgeKey = IB_Edge.getEdgeKey(leg);
    LinkTreeModel ltm = LinkTreePanel.getTreeModel(rootPath.getFabric(), edgeKey);

    vmtn.add(ltm.getRootNode());
      
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
    
    if(parentNode instanceof RT_Path)
    {
      RT_Path e = (RT_Path)parentNode;
      if(e.compareTo(rootPath) == 0)
        parent = rootPathNode;
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
  
  public boolean containsSameRoot(RT_PathTreeModel model)
  {
    // compare the root paths
    return(rootPath.getRT_PathKey().equals(model.getRootPath().getRT_PathKey()));
  }

}
