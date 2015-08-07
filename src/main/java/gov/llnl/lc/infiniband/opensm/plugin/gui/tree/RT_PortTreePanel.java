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
 *        file: RT_PortTreePanel.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.tree;

import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Updater;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_ServiceChangeListener;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.infiniband.opensm.plugin.data.RT_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.RT_Table;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionEvent;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionListener;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionUpdater;
import gov.llnl.lc.logging.CommonLogger;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class RT_PortTreePanel extends JPanel implements OSM_ServiceChangeListener, CommonLogger, IB_GraphSelectionListener
{
  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = 3699399417306520581L;
  private static final SMTUserObjectTreeCellRenderer RT_PortCellRenderer = new SMTUserObjectTreeCellRenderer(true);
  /**
   * Create the panel.
   */
  private JTree                                      tree;
  private RT_PortTreeModel                           Model;

  private int                                        HistorySize      = 0;

  public RT_PortTreePanel()
  {
    setBorder(new TitledBorder(null, "Port Route Tree", TitledBorder.LEADING, TitledBorder.TOP, null, null));
    setLayout(new BorderLayout(0, 0));
  }

  private void setTreeRootNode(UserObjectTreeNode root)
  {
    // RT_PortTreeModel tm = new RT_PortTreeModel(root);
    DefaultTreeModel tm = new DefaultTreeModel(root);
    if (tm != null)
    {
      // Do I have to recreate the tree, and put it here?
      logger.info("Setting the RT_Port Tree model here");
      initTree();
      this.tree.setModel(tm);
    }
    else
      logger.info("The DefaultTreeModel for the RT_PortTreePanel is null");
  }

  private void initTree()
  {
    // create the tree and add it to the panel, delete any old tree first
    if (tree != null)
    {
      // check to see if its contained by the panel, and remove it if necessary
      this.remove(tree);
    }

    // create a brand new tree, and set it all up
    tree = new JTree();
    final RT_PortTreePanel thisPanel = this;

    tree.setToolTipText("Explore the elements and attributes of a port route.");

//    tree.setCellRenderer(RT_PortCellRenderer);
    tree.addTreeSelectionListener(new TreeSelectionListener()
    {
      public void valueChanged(TreeSelectionEvent arg0)
      {
        // happens when a component is selected
        // arg is the tree, and lastSelectedPathComponent is the
        // UserObjectTreeNode
        if (tree.getLastSelectedPathComponent() instanceof UserObjectTreeNode)
        {
          UserObjectTreeNode tn = (UserObjectTreeNode) tree.getLastSelectedPathComponent();
          NameValueNode vmn = (NameValueNode) tn.getUserObject();
//          System.err.println("The name of the object is: " + vmn.getMemberName());
          // craft a selection event, for this port path
          RT_Port n = (RT_Port)Model.getRoot();
//        GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(thisPanel, n, tn));
          // DO NOTHING instead
        }
      }
    });

    tree.addMouseListener(new MouseAdapter()
    {
      public void mousePressed(MouseEvent e)
      {
//        System.err.println("Mouse event 0");
        if(isTreeNodeSelected(e))
        {
          // do nothing
//         System.err.println("deliberately not showing a popup at this location");
        }
        else if(e.isPopupTrigger())
        {
//          System.err.println("Mouse event 2");
        }
      }
    });

    add(tree, BorderLayout.CENTER);
  }
  
  protected boolean isTreeNodeSelected(MouseEvent e)
  {
    // return true if the mouse was over ANY tree node when clicked
      TreePath path = tree.getPathForLocation(e.getX(), e.getY());
      tree.setSelectionPath(path);
      UserObjectTreeNode tn = (UserObjectTreeNode) tree.getLastSelectedPathComponent();
      if (tn != null)
      {
          Rectangle pathBounds = tree.getUI().getPathBounds(tree, path);

          if (pathBounds != null && pathBounds.contains(e.getX(), e.getY()))
            return true;
      }
    return false;
  }
  public static void main(String[] args) throws Exception
  {
  }

  public RT_PortTreeModel getTreeModel()
  {
    return Model;
  }

  public static RT_PortTreeModel getTreeModel(RT_Port port, OpenSmMonitorService OMS, RT_Table table)
  {
    return getTreeModel(port, OMS.getFabric(), table);
  }

  public static RT_PortTreeModel getTreeModel(RT_Port port, OSM_Fabric fabric, RT_Table table)
  {
    return new RT_PortTreeModel(port, table, fabric);
  }

  public void setTreeModel(RT_PortTreeModel model)
  {
    // this is how the model gets into the panel.
    //  afterward, it may get updated (see below)
    this.Model = model;
    UserObjectTreeNode root = (UserObjectTreeNode) this.Model.getRootNode();

    this.setTreeRootNode(root);
  }

  public void updateTreeModel(RT_PortTreeModel model)
  {
    // assumes model already exists
  }

  @Override
  public void valueChanged(IB_GraphSelectionUpdater source, IB_GraphSelectionEvent event)
  {
    System.err.println("Value changed");

  }

  @Override
  public void osmFabricUpdate(OSM_Fabric osmFabric) throws Exception
  {
    // TODO Auto-generated method stub

  }

  protected int getHistorySize()
  {
    return HistorySize;
  }

  @Override
  public void osmServiceUpdate(OMS_Updater updater, OpenSmMonitorService osmService)
      throws Exception
  {
   }

}
