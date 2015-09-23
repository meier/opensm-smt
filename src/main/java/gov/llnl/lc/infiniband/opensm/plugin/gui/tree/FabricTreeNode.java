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
 *        file: FabricTreeNode.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.tree;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

@Deprecated
public class FabricTreeNode extends DefaultMutableTreeNode
{

  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = 603732197128797132L;
  
  /************************************************************
   * Method Name:
   *  FabricTreeNode
  **/
  /**
   * This object is a node in a tree that contains only IB_Vertex
   * elements.  The tree should represent the nodes in a fabric.
   *
   * @see     describe related java objects
   *
   * @param userObject
   * @param allowsChildren
   ***********************************************************/
  public FabricTreeNode(NameValueNode userObject, boolean allowsChildren)
  {
    // the userObject is of type "IB_Vertex"
    super(userObject, allowsChildren);
    // TODO Auto-generated constructor stub
  }
  
  public static FabricTreeNode getTreeRootNode(JTree tree)
  {
    FabricTreeNode root = null;

    if ((tree != null) && (tree.getModel() != null))
    {
      DefaultTreeModel tm = (DefaultTreeModel)tree.getModel();
      Object obj = tm.getRoot();
      if(obj instanceof FabricTreeNode)
        root = (FabricTreeNode)obj;
     }
    
    return root;
  }
  


  @Override
  public String toString()
  {
    NameValueNode n = (NameValueNode)this.getUserObject();
    
    return n.toString();
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

}
