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
 *        file: FabricConfTreeModel.java
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

import org.w3c.dom.Comment;

import gov.llnl.lc.infiniband.opensm.xml.IB_FabricConf;
import gov.llnl.lc.infiniband.opensm.xml.IB_FabricNameElement;
import gov.llnl.lc.infiniband.opensm.xml.IB_LinkListElement;
import gov.llnl.lc.infiniband.opensm.xml.IB_PortElement;

public class FabricConfTreeModel extends DefaultTreeModel
{  
  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = -8143137480192378152L;

  private UserObjectTreeNode rootConfNode;
  private IB_FabricConf rootConf;

  public UserObjectTreeNode getRootNode()
  {
    return rootConfNode;
  }
  
  public Object getRoot()
  {
    return rootConf;
  }
  
  public IB_FabricConf getRootConf()
  {
    return rootConf;
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
  public FabricConfTreeModel(TreeNode root)
  {
    super(root, true);
    // TODO Auto-generated constructor stub
  }

  public FabricConfTreeModel(String fileName)
  {
    this(new IB_FabricConf(fileName));
  }


  public FabricConfTreeModel(IB_FabricConf fabConf)
  {
    super(null, true);
    // assume it is fully constructed
    
    // this is the normal preferred way to construct the tree model
    
    this.rootConf   = fabConf;
    
    if(fabConf == null)
      return;
    
    NameValueNode      vmn = new NameValueNode("fabric", rootConf.getFabricName());
//    NameValueNode      vmn = new NameValueNode("fabric", rootConf.getIB_FabricNameElement());
    UserObjectTreeNode vmtn = new UserObjectTreeNode(vmn, true);
    rootConfNode = vmtn;
    
    addFabricNode(rootConfNode, rootConf);
    
    addCommentElements(rootConfNode, rootConf.getCommentElements());
    
    addNodeElements(rootConfNode);
  }
  
  private void addFabricNode(UserObjectTreeNode parent, IB_FabricConf conf)
  {
    NameValueNode          vmn = new NameValueNode("file name", conf.getFileName());
    UserObjectTreeNode    vmtn = new UserObjectTreeNode(vmn, false);
    parent.add(vmtn);
    
    IB_FabricNameElement fe = conf.getIB_FabricNameElement();
    fe.getSpeed();
    
    vmn = new NameValueNode("speed", fe.getSpeed());
    vmtn = new UserObjectTreeNode(vmn, false);
    parent.add(vmtn);
    
    vmn = new NameValueNode("width", fe.getWidth());
    vmtn = new UserObjectTreeNode(vmn, false);
    parent.add(vmtn);
    
    vmn = new NameValueNode("# nodes", conf.getNumNodes());
    vmtn = new UserObjectTreeNode(vmn, false);
    parent.add(vmtn);
    
    vmn = new NameValueNode("# ports", conf.getNumPorts());
    vmtn = new UserObjectTreeNode(vmn, false);
    parent.add(vmtn);
    vmn = new NameValueNode("# down ports (suspected)", conf.getNumDownPorts());
    vmtn = new UserObjectTreeNode(vmn, false);
    parent.add(vmtn);
    
    vmn = new NameValueNode("# links", conf.getNumLinks());
    vmtn = new UserObjectTreeNode(vmn, false);
    parent.add(vmtn);
  }

  private void addCommentElements(UserObjectTreeNode parent, ArrayList<Comment> commentList)
  {
     if(commentList.size() > 0)
    {
      // create the parent node, and add the elements as children
      NameValueNode          vmn = new NameValueNode("# comments", commentList.size());
      UserObjectTreeNode    vmtn = new UserObjectTreeNode(vmn, true);
      parent.add(vmtn);
      
    for(Comment ce: commentList)
    {
      addCommentElement(vmtn, ce);
    }
    }
    return;
  }

  private void addCommentElement(UserObjectTreeNode parent, Comment ce)
  {
    NameValueNode          vmn = new NameValueNode("!--", ce.getData().trim());
    UserObjectTreeNode    vmtn = new UserObjectTreeNode(vmn, false);
    parent.add(vmtn);
   }

  private void addNodeElements(UserObjectTreeNode parent)
  {
    // this edge has two ports, get the one for THIS vertex
    IB_FabricConf  conf = rootConf;
    
    NameValueNode          vmn = new NameValueNode("# switches", conf.getNodeElements().size());
    UserObjectTreeNode    vmtn = new UserObjectTreeNode(vmn, false);
    parent.add(vmtn);
    
    for(IB_LinkListElement lle: conf.getNodeElements())
    {
      addNodeElement(parent, lle);
    }
    return;
  }

  private void addNodeElement(UserObjectTreeNode parent, IB_LinkListElement lle)
  {
    
    NameValueNode          vmn = new NameValueNode("switch", lle.getName());
    
    if(lle.getPortElements().size() < 3)
      vmn = new NameValueNode("channel adapter", lle.getName());
//    NameValueNode          vmn = new NameValueNode("link list", lle);
    UserObjectTreeNode    vmtn = new UserObjectTreeNode(vmn, true);
    parent.add(vmtn);
    
//    NameValueNode      mmn = new NameValueNode("name", lle.getName());
//    UserObjectTreeNode mmtn = new UserObjectTreeNode(mmn, false);
//    vmtn.add(mmtn);
//    
//    mmn = new NameValueNode("element name", lle.getElementName());
//    mmtn = new UserObjectTreeNode(mmn, false);
//    vmtn.add(mmtn);
//    
//    mmn = new NameValueNode("num ports", lle.getPortElements().size());
//    mmtn = new UserObjectTreeNode(mmn, false);
//    vmtn.add(mmtn);
    
    // conditionally add speed and width
    if(!lle.getSpeedAttribute().equals("unspecified"))
    {
      vmn = new NameValueNode("speed", lle.getSpeedAttribute());
      vmtn.add(new UserObjectTreeNode(vmn, false));
    }
    
    if(!lle.getWidthAttribute().equals("unspecified"))
    {
      vmn = new NameValueNode("width", lle.getWidthAttribute());
      vmtn.add(new UserObjectTreeNode(vmn, false));
    }
    


    addCommentElements(vmtn, lle.getCommentElements());

    addPortElements(vmtn, lle.getPortElements(), true);
  }

  private void addPortElements(UserObjectTreeNode parent, ArrayList<IB_PortElement> portList, boolean allowsChildren)
  {
    // create the parent node, and conditionally add the elements as children
    NameValueNode          vmn = new NameValueNode("# links", portList.size());
    UserObjectTreeNode    vmtn = new UserObjectTreeNode(vmn, allowsChildren);
    parent.add(vmtn);
    
    UserObjectTreeNode par = allowsChildren ? vmtn: parent;
    
    for(IB_PortElement pe: portList)
    {
      addPortElement(par, pe);
    }
    return;
  }

  private void addPortElement(UserObjectTreeNode parent, IB_PortElement pe)
  {
    NameValueNode          vmn = new NameValueNode("port", pe.getNumber());
//    NameValueNode          vmn = new NameValueNode("link", pe);
    UserObjectTreeNode    vmtn = new UserObjectTreeNode(vmn, true);
    parent.add(vmtn);
    
    // conditionally add speed and width
    if(!pe.getSpeed().equals("unspecified"))
    {
      vmn = new NameValueNode("speed", pe.getSpeed());
      vmtn.add(new UserObjectTreeNode(vmn, false));
    }
    
    if(!pe.getWidth().equals("unspecified"))
    {
      vmn = new NameValueNode("width", pe.getWidth());
      vmtn.add(new UserObjectTreeNode(vmn, false));
    }
    
    NameValueNode      mmn = new NameValueNode("remote node", pe.getIB_RemoteNodeElement().getName());
//    NameValueNode      mmn = new NameValueNode("port", pe.getNumber());
    UserObjectTreeNode mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    mmn = new NameValueNode("remote port", pe.getIB_RemotePortElement().getNumber());
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);

    mmn = new NameValueNode("XML", pe.toXMLString(1));
    mmtn = new UserObjectTreeNode(mmn, false);
    vmtn.add(mmtn);
    
    addCommentElements(vmtn, pe.getCommentElements());
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
  
  public boolean updateModel(FabricConfTreeModel model)
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
      System.err.println("new and old nodes have different number of children");
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
    
    // the parentNode is either a UserObjectTreeNode, or an IB_FabricConf
    if(parentNode instanceof UserObjectTreeNode)
    {
      parent = (UserObjectTreeNode) parentNode;
    }
    
    if(parentNode instanceof IB_FabricConf)
    {
      IB_FabricConf p = (IB_FabricConf)parentNode;
      if(p.compareTo(rootConf) == 0)
        parent = rootConfNode;
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

  public boolean containsSameRoot(FabricConfTreeModel model)
  {
    return (rootConf.equals(model.getRootConf()));
  }


}
