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
 *        file: SystemTreePanel.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.tree;

import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Updater;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_ServiceChangeListener;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionEvent;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionListener;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionUpdater;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.manager.GraphSelectionManager;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class SystemTreePanel extends JPanel implements OSM_ServiceChangeListener, CommonLogger, IB_GraphSelectionListener
{
/**  describe serialVersionUID here **/
  private static final long serialVersionUID = 4208373232474907424L;

  //  private static final SMTUserObjectTreeCellRenderer SystemCellRenderer = new SMTUserObjectTreeCellRenderer(true);
  private static final SMTFabricTreeCellRenderer SystemCellRenderer = new SMTFabricTreeCellRenderer(true);
  
  /** this is the actual system image guid model **/
  private SystemTreeModel Model;
	
  /** this is the tree used by the panel, for visualization **/
  private JTree             tree;
  
  /** save a copy of myself for reference **/
private SystemTreePanel thisPanel = this;

public JTree getTree()
{
  return tree;
}

public void setTree(JTree tree)
{
}

public void setTreeRootNode(UserObjectTreeNode root)
{
  // creates the "tree" model, using the root node for the SystemTreeModel
  DefaultTreeModel tm = new DefaultTreeModel(root);

  if (tm != null)
  {
    // Do I have to recreate the tree, and put it here?
    initTree();
    tree.setModel(tm);
  }
  else
    logger.severe("The DefaultTreeModel for the SystemTreePanel is null");
}

public UserObjectTreeNode getTreeRootNode()
{
  return UserObjectTreeNode.getTreeRootNode(tree);
}

	
	public SystemTreePanel()
	{
	  setBorder(new TitledBorder(null, "System Switch Tree", TitledBorder.LEADING, TitledBorder.TOP, null, null));
	  setLayout(new BorderLayout(0, 0));
	  
//    // put the graph in the panel, and tell the manager I need to listen for events
	  /******************************redundant remove (see initTree) **********************************************************/   
    // create a brand new tree, and set it all up
    tree = new JTree();
    
    tree.setToolTipText("Explore System assemblies (core switches).");    
    tree.setCellRenderer(SystemCellRenderer);
    tree.addTreeSelectionListener(new TreeSelectionListener()
    {
      public void valueChanged(TreeSelectionEvent arg0)
      {
        // arg is the tree, and lastSelectedPathComponent is the
        // UserObjectTreeNode
        if (tree.getLastSelectedPathComponent() instanceof UserObjectTreeNode)
        {
          UserObjectTreeNode tn = (UserObjectTreeNode) tree.getLastSelectedPathComponent();
          IB_Vertex v = (IB_Vertex) tn.getUserObject();
          logger.info("the initial time for a SystemTreePanel()");
        }
      }
    });
    add(tree, BorderLayout.CENTER);
    
    /******************************redundant remove (see initTree) **********************************************************/   
	}
  private void initTree()
  {
    // only called from setTreeRootNode()
    
    // create the tree and add it to the panel, delete any old tree first
    if (tree != null)
    {
      // check to see if its contained by the panel, and remove it if necessary
      this.remove(tree);
    }

      // create a brand new tree, and set it all up
      tree = new JTree();
      final SystemTreePanel thisPanel = this;

      tree.setToolTipText("Explore System assemblies (core switches).");    
      tree.setCellRenderer(SystemCellRenderer);
      
      tree.addTreeSelectionListener(new TreeSelectionListener()
      {
        public void valueChanged(TreeSelectionEvent arg0)
        {
          // arg is the tree, and lastSelectedPathComponent is the
          // UserObjectTreeNode
          if (tree.getLastSelectedPathComponent() instanceof UserObjectTreeNode)
          {
            UserObjectTreeNode tn = (UserObjectTreeNode) tree.getLastSelectedPathComponent();
            NameValueNode vmn = (NameValueNode) tn.getUserObject();
            vmn.getMemberObject();
            // craft a selection event, for this vertex
            IB_Vertex v = Model.getRootVertex();
            GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(thisPanel, v, tn));
          }
        }
      });
      
      tree.addMouseListener ( new MouseAdapter ()
    {
      public void mousePressed(MouseEvent e)
      {
//        if(isCounterSelected(e))
//        {
//          // show the counter popup
//           showPortCounterMenu(e);
//        }
//        else
        if(isTreeNodeSelected(e))
        {
          // do nothing
          System.err.println("SystemTreePanel: deliberately not showing a popup at this location");
        }
        else if(e.isPopupTrigger())
        {
          // out in the open, so okay to show general purpose popup for the heatmap or utilize
           showSystemMenu(e);
        }
      }
    });
      
      add(tree, BorderLayout.CENTER);
    }
  
//  protected boolean isCounterSelected(MouseEvent e)
//  {
//    // is the selected component in the panel, a port counter?
//    if (e.isPopupTrigger())
//    {
//      TreePath path = tree.getPathForLocation(e.getX(), e.getY());
//      tree.setSelectionPath(path);
//      UserObjectTreeNode tn = (UserObjectTreeNode) tree.getLastSelectedPathComponent();
//      if (tn != null)
//      {
//        NameValueNode vmn = (NameValueNode) tn.getUserObject();
//        PortCounterName pcn = PortCounterName.getByName(vmn.getMemberName());
//        if ((pcn != null) && PortCounterName.PFM_ALL_COUNTERS.contains(pcn))
//        {
//          Rectangle pathBounds = tree.getUI().getPathBounds(tree, path);
//
//          if (pathBounds != null && pathBounds.contains(e.getX(), e.getY()))
//            return true;
//        }
//      }
//    }
//    return false;
//  }
  
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

  // this is simple, comapre, advanced, advanced compare
  
//  protected void showPortCounterMenu(MouseEvent e)
//  {
//    // this is private, all checks already passed (see isCounterSelected())
//    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
//    tree.setSelectionPath(path);
//    UserObjectTreeNode tn = (UserObjectTreeNode) tree.getLastSelectedPathComponent();
//    NameValueNode vmn = (NameValueNode) tn.getUserObject();
//    PortCounterName pcn = PortCounterName.getByName(vmn.getMemberName());
//    Rectangle pathBounds = tree.getUI().getPathBounds(tree, path);
//    PortCounterPopupMenu menu = new PortCounterPopupMenu(Model, pcn, getHistorySize());
//    menu.show(tree, pathBounds.x, pathBounds.y + pathBounds.height);
//  }
// 
  
  // this is just utilization
  
  protected void showSystemMenu(MouseEvent e)
  {
    // this is private, all checks already passed (see isCounterSelected())
    SystemTreePopupMenu menu = new SystemTreePopupMenu();
    
    // give the popup menu a copy of the Model, just in case
    menu.setSystemTreeModel(Model);
    menu.show(tree, e.getX(), e.getY());
  }
  
    public static void main(String[] args) throws Exception
    {
    }

    public SystemTreeModel getTreeModel()
    {
      return Model;
     }
    
    public void setTreeModel(SystemTreeModel model)
    {
      Model = model;
      setTreeRootNode((UserObjectTreeNode)model.getRoot());
    }

  @Override
  public void valueChanged(IB_GraphSelectionUpdater source, IB_GraphSelectionEvent event)
  {
    if (event.getSource() instanceof IB_Vertex)
    {
      IB_Vertex myVertex = (IB_Vertex) event.getSource();
      
      // do I want multiple frames, or reuse an existing one?

      JFrame frame = new JFrame();
      frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setLocation(100, 50);

      if (myVertex == null)
      {
        // not found, oops
        System.err.println("Could not find that vertex");
      }

      VertexTreeModel treeModel = new VertexTreeModel(myVertex, null);

      UserObjectTreeNode root = (UserObjectTreeNode) treeModel.getRoot();
      if (root != null)
      {
        System.err.println("The root has " + root.getChildCount() + " children");
      }

//      setTreeRootNode(root);
      
      System.err.println("PTP: The vertex for this port is: " + myVertex.getName());

      Container container = this.getParent();
      if(container == null)
      {
        System.err.println("No container yet");
      }
      else
      {
        if(container instanceof JFrame)
          System.err.println("the container is a jframe");
        if(container instanceof Container)
          System.err.println("the container is a container");
        if(container instanceof Panel)
          System.err.println("the container is a panel");
        if(container instanceof JComponent)
          System.err.println("the container is a jcomponent");
        if(container instanceof JRootPane)
          System.err.println("the container is a jrootpane");
        if(container instanceof JPanel)
          System.err.println("the container is a jpanel");
        if(container instanceof JInternalFrame)
          System.err.println("the container is a jinternalframe");
      }


      JScrollPane scroller = new JScrollPane(this);
      frame.getContentPane().add(scroller, BorderLayout.CENTER);
      frame.pack();
      frame.setVisible(true);
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
    SystemTreeModel model = null;
    IB_Guid sys_guid = null;
    OSM_Fabric f = osmService.getFabric();

    ArrayList<IB_Guid> guidList = null;

    if (Model != null)
    {
      sys_guid = Model.getSystemGuid();
      guidList = f.getNodeGuidsForSystemGuid(sys_guid);

      // Build the Tree Model for System Image Guid, put it in a Tree Panel, put
      // that in a Scroll Pane (in center) and hook it up as a listener

      LinkedHashMap<String, IB_Vertex> vertexMap = IB_Vertex.createVertexMap(f, guidList);
      model = new SystemTreeModel(f, sys_guid);
      setTreeModel(model);

      UserObjectTreeNode root = (UserObjectTreeNode) model.getRoot();
      /** TODO - consider updateTreeNode (refer to other panels) to avoid NULL paint problems **/
      setTreeRootNode(root);  // update will leave the state of the tree alone, while a set repaints (closes)

    }
    else
      logger.severe("The System Tree Model is null");
  }

}
