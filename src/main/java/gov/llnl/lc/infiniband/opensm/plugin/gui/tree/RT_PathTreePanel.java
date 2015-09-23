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
 *        file: RT_PathTreePanel.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.tree;

import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Updater;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_ServiceChangeListener;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.infiniband.opensm.plugin.data.RT_Path;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionEvent;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionListener;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionUpdater;
import gov.llnl.lc.logging.CommonLogger;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class RT_PathTreePanel extends JPanel implements OSM_ServiceChangeListener, CommonLogger, IB_GraphSelectionListener
{
  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = 1340344966520653784L;
  private static final SMTUserObjectTreeCellRenderer RT_TableCellRenderer = new SMTUserObjectTreeCellRenderer(true);
  /**
   * Create the panel.
   */
  private JTree                                      tree;
  private RT_PathTreeModel                           Model;
  private RT_PathTreePanel     thisPanel = this;
  
  private RT_PathPopupMenu rootPopup = null;

  public RT_PathTreePanel()
  {
    setBorder(new TitledBorder(null, "Path Tree (trace route)", TitledBorder.LEADING, TitledBorder.TOP, null, null));
    setLayout(new BorderLayout(0, 0));
  }

  private void setTreeRootNode(UserObjectTreeNode root)
  {
    // RT_PathTreeModel tm = new RT_PathTreeModel(root);
    DefaultTreeModel tm = new DefaultTreeModel(root);
    if (tm != null)
    {
      // Do I have to recreate the tree, and put it here?
      logger.info("Setting the RT_Path Tree model here");
      initTree();
      this.tree.setModel(tm);
    }
    else
      logger.info("The DefaultTreeModel for the RT_PathTreePanel is null");
  }

  private void initTree()
  {
    // create the tree and add it to the panel, delete any old tree first
    if (tree != null)
    {
      // check to see if its contained by the panel, and remove it if necessary
      this.remove(tree);
      tree.removeAll();
    }

    // create a brand new tree, and set it all up
    tree = new JTree();

//    tree.setToolTipText("Explore the elements and attributes of a node route.");

//    tree.setCellRenderer(RT_TableCellRenderer);
    tree.addTreeSelectionListener(new TreeSelectionListener()
    {
      public void valueChanged(TreeSelectionEvent arg0)
      {
        if (tree.getLastSelectedPathComponent() instanceof UserObjectTreeNode)
        {
          UserObjectTreeNode tn = (UserObjectTreeNode) tree.getLastSelectedPathComponent();
          NameValueNode vmn = (NameValueNode) tn.getUserObject();
          // vmn.getMemberName());
          vmn.getMemberObject();
          // craft a selection event, for this vertex
//          IB_Vertex v = Model.getRootVertex();
//          GraphSelectionManager.getInstance().updateAllListeners(
//              new IB_GraphSelectionEvent(thisPanel, v, tn));
        }
      }
    });

    tree.addMouseListener(new MouseAdapter()
    {
      public void mousePressed(MouseEvent e)
      {
        if(false)
        {
          System.err.println("Mouse event 1");
        }
        else if(isTreeNodeSelected(e))
        {
          // do nothing
//          System.err.println("deliberately not showing a popup at this location");
        }
        else if(e.isPopupTrigger())
        {
          showRootPopupMenu(e);
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

  public RT_PathTreeModel getTreeModel()
  {
    return Model;
  }


  public void setTreeModel(RT_PathTreeModel model)
  {
    // this is how the model gets into the panel.
    //  afterward, it may get updated (see below)
    UserObjectTreeNode root = (UserObjectTreeNode) model.getRootNode();
//
    this.setTreeRootNode(root);
    this.Model = model;
  }

  public void updateTreeModel(RT_PathTreeModel model)
  {
    // assumes model already exists
    // if this is a completely different modle, just replace it
    if (this.Model.containsSameRoot(model) && (this.Model.includesNodes() == model.includesNodes()))
      this.Model.updateModel(model, model.includesNodes());
    else
      setTreeModel(model);
    this.repaint();
  }

  @Override
  public void valueChanged(IB_GraphSelectionUpdater source, IB_GraphSelectionEvent event)
  {
    if (event.getSource() instanceof RT_PathPopupMenu)
    {
      RT_PathPopupMenu myMenu = (RT_PathPopupMenu) event.getSource();

      // is this MY model and path?
      if(event.getContextObject() instanceof RT_PathTreeModel)
      {
        RT_PathTreeModel model = (RT_PathTreeModel)event.getContextObject();
        if(model.containsSameRoot(Model))
        {
          if(event.getSelectedObject() instanceof RT_Path)
          {
            RT_Path path = (RT_Path)event.getSelectedObject();
            if(path.compareTo(Model.getRootPath()) == 0)
            {
              System.err.println("The path matches!");
              
              // what action?
              
            }
          }
          if (event.getSelectedObject() instanceof JCheckBox)
          {
            JCheckBox cb = (JCheckBox) event.getSelectedObject();
            if ((cb != null) && (cb.getText() != null))
            {
              if (cb.getText().startsWith("Hide"))
              {
                // hide or reveal the nodes
                RT_PathTreeModel m = new RT_PathTreeModel(Model.getRootPath(), !cb.isSelected());
                updateTreeModel(m);
              }
            }
          }
          if (event.getSelectedObject() instanceof JMenuItem)
          {
            JMenuItem mi = (JMenuItem) event.getSelectedObject();
            if ((mi != null) && (mi.getText() != null))
            {
              if (mi.getText().startsWith("Swap"))
              {
                // swap action
                RT_Path rtn = Model.getRootPath().getReturnPath();
                RT_PathTreeModel m = new RT_PathTreeModel(rtn, Model.includesNodes());
                setTreeModel(m);
              }
            }
          }
        }
      }
     }
    else
    {
      // I probably don't care about an event from somewhere else
//      System.err.println("From RT_PathTreePanel: Unhandled event of context  type (" + event.getContextObject().getClass().getSimpleName() + ")");
//      System.err.println("From RT_PathTreePanel: Unhandled event of selected type (" + event.getSelectedObject().getClass().getSimpleName() + ")");
    }

  }

  @Override
  public void osmFabricUpdate(OSM_Fabric osmFabric) throws Exception
  {
    // TODO Auto-generated method stub

  }

  @Override
  public void osmServiceUpdate(OMS_Updater updater, OpenSmMonitorService osmService)
      throws Exception
  {
    // need a vertex and port number
//    String guidString = Model.getRootRtNode().getGuid().toColonString();
      RT_PathTreeModel model = null;

    // if the updater is an SMT_Updater, skip analysis, probably already done
//    if (updater instanceof SMT_UpdateService)
//    {
//      // the updater may or may not have the model yet
//      SMT_UpdateService sus = ((SMT_UpdateService) updater);
//      if ((sus == null) || (sus.getVertexMap() == null))
//      {
//        // do it old-school (model not ready)
//        logger.severe("getting the treemodel for PortTreePanel without the SMT_UpdateService (not ready?)");
//        model = getTreeModel(osmService, guidString, portNum);
//      }
//      else
//      {
//        model = getTreeModel((sus).getVertexMap(), guidString, portNum);
//        if (sus.getCollection() != null)
//          HistorySize = sus.getCollection().getSize();
//      }
//    }
//    else
//      model = getTreeModel(osmService, guidString, portNum);

    // just update the data without changing the structure
    if (model != null)
      updateTreeModel(model);
  }
  
  protected void showRootPopupMenu(MouseEvent e)
  {
     try
    {
      if (e.getComponent().isVisible())
      {
        RT_Path rtPath = Model.getRootPath();
        if(rtPath == null)
          logger.severe("Hmm, the path seems to be null");
        if(rootPopup == null)
          rootPopup = new RT_PathPopupMenu(Model, rtPath);
        else
          rootPopup.setModelAndPath(Model, rtPath);

        if(tree == null)
          logger.severe("The tree is NULL, Jim");
        
        rootPopup.show(tree, e.getX(), e.getY());
      }
    }
    catch (Exception ex)
    {
      logger.severe("Can't show RootPopupPopupMenu() (" + ex.getMessage() + ")");
    }
  }
}
