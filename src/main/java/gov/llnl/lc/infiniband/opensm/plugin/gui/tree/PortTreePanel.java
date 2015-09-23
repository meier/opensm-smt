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
 *        file: PortTreePanel.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.tree;

import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Updater;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_ServiceChangeListener;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_Port.PortCounterName;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionEvent;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionListener;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionUpdater;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.data.SMT_UpdateService;
import gov.llnl.lc.smt.manager.GraphSelectionManager;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

public class PortTreePanel extends JPanel implements OSM_ServiceChangeListener, CommonLogger, IB_GraphSelectionListener
{
	/**  describe serialVersionUID here **/
  private static final long serialVersionUID = -4569629228409327770L;
  private static final SMTUserObjectTreeCellRenderer PortCellRenderer = new SMTUserObjectTreeCellRenderer(true);
  /**
	 * Create the panel.
	 */
	private JTree tree;
  private AbstractPortTreeModel Model;
	
  private int HistorySize = 0;
  
	
	public PortTreePanel()
	{
	  setBorder(new TitledBorder(null, "Port Tree", TitledBorder.LEADING, TitledBorder.TOP, null, null));
	  setLayout(new BorderLayout(0, 0));
	  
//    // put the graph in the panel, and tell the manager I need to listen for events
//    globalFabricManager = gov.llnl.lc.infiniband.opensm.client.browser.manager.FabricManager.getInstance();
//    initTree();
	}
	
  private void setTreeRootNode(UserObjectTreeNode root)
  {
//    PortTreeModel tm = new PortTreeModel(root);
    
    // creates the "tree" model, using the root node for the PortModel
    DefaultTreeModel tm = new DefaultTreeModel(root);
    if(tm != null)
    {
      // Do I have to recreate the tree, and put it here?
      logger.info("Setting the Port Tree model here");
      initTree();
      this.tree.setModel(tm);
    }
    else
      logger.info("The DefaultTreeModel for the PortTreePanel is null");
  }

//  private void setTreeRootNode1(UserObjectTreeNode root)
//  {
//    DefaultTreeModel tm = new DefaultTreeModel(root);
//    if(tm != null)
//    {
//      // Do I have to recreate the tree, and put it here?
//      logger.info("Setting the Port Tree model here");
//      initTree();
//      if(Model != null)
//        this.tree.setModel(Model);
//      else
//        this.tree.setModel(tm);
//    }
//    else
//      logger.info("The DefaultTreeModel for the PortTreePanel is null");
//  }
//
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
      final PortTreePanel thisPanel = this;

      tree.setToolTipText("Explore the elements and attributes of a port.");
 
      tree.setCellRenderer(PortCellRenderer);
      tree.addTreeSelectionListener(new TreeSelectionListener()
      {
        public void valueChanged(TreeSelectionEvent arg0)
        {
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
        if(isCounterSelected(e))
        {
          // show the counter popup
           showPortCounterMenu(e);
        }
        else if(isTreeNodeSelected(e))
        {
          // do nothing
//          System.err.println("deliberately not showing a popup at this location");
        }
        else if(e.isPopupTrigger())
        {
          // out in the open, so okay to show general purpose popup
          showPortMenu(e);
        }
      }
    });
      
      add(tree, BorderLayout.CENTER);
    }
  
  protected boolean isCounterSelected(MouseEvent e)
  {
    // is the selected component in the panel, a port counter?
    if (e.isPopupTrigger())
    {
      TreePath path = tree.getPathForLocation(e.getX(), e.getY());
      tree.setSelectionPath(path);
      UserObjectTreeNode tn = (UserObjectTreeNode) tree.getLastSelectedPathComponent();
      if (tn != null)
      {
        NameValueNode vmn = (NameValueNode) tn.getUserObject();
        PortCounterName pcn = PortCounterName.getByName(vmn.getMemberName());
        if ((pcn != null) && PortCounterName.PFM_ALL_COUNTERS.contains(pcn))
        {
          Rectangle pathBounds = tree.getUI().getPathBounds(tree, path);

          if (pathBounds != null && pathBounds.contains(e.getX(), e.getY()))
            return true;
        }
      }
    }
    return false;
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
  
  protected void showPortCounterMenu(MouseEvent e)
  {
    // this is private, all checks already passed (see isCounterSelected())
    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
    tree.setSelectionPath(path);
    UserObjectTreeNode tn = (UserObjectTreeNode) tree.getLastSelectedPathComponent();
    NameValueNode vmn = (NameValueNode) tn.getUserObject();
    PortCounterName pcn = PortCounterName.getByName(vmn.getMemberName());
    Rectangle pathBounds = tree.getUI().getPathBounds(tree, path);
    PortCounterPopupMenu menu = new PortCounterPopupMenu(Model, pcn, getHistorySize());
    menu.show(tree, pathBounds.x, pathBounds.y + pathBounds.height);
  }
  
  protected void showPortMenu(MouseEvent e)
  {
    // this is private, all checks already passed (see isCounterSelected())
    PortTreePopupMenu menu = new PortTreePopupMenu();
    menu.show(tree, e.getX(), e.getY());
  }
  
    public static void main(String[] args) throws Exception
    {
    }

    public AbstractPortTreeModel getTreeModel()
    {
      return Model;
     }
    
    public static AbstractPortTreeModel getTreeModel(OpenSmMonitorService OMS, String guidString, int portNum)
    {
      LinkedHashMap<String, IB_Vertex> vertexMap = IB_Vertex.createVertexMap(OMS.getFabric());
      return getTreeModel(vertexMap, guidString, portNum);
     }
    
    public static AbstractPortTreeModel getTreeModel(LinkedHashMap<String, IB_Vertex> vertexMap, String guidString, int portNum)
    {
      if (vertexMap == null)
        System.exit(-1);
      
      IB_Guid guid = new IB_Guid(guidString);
      // find a vertex in the map
      String key = IB_Vertex.getVertexKey(guid);
      IB_Vertex myVertex = vertexMap.get(key);
      if(myVertex == null)
      {
        // not found, oops
        logger.severe("Could not find that vertex (" + guidString + ")");
        return null;
      }
      return new PortTreeModel(myVertex, portNum);
     }
    
    public void setTreeModel(AbstractPortTreeModel model)
    {
    this.Model = model;
    UserObjectTreeNode root = (UserObjectTreeNode) this.Model.getRootNode();

    this.setTreeRootNode(root);
    }

    public void updateTreeModel(AbstractPortTreeModel model)
    {
      // assumes model already exists
      //if this is a completely different modle, just replace it
      if(this.Model.containsSameRoot(model))
      {     
        this.Model.updateModel(model);
        this.repaint();
      }
      else
        setTreeModel(model);
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

      setTreeRootNode(root);
      
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

  protected int getHistorySize()
  {
    return HistorySize;
  }

  @Override
  public void osmServiceUpdate(OMS_Updater updater, OpenSmMonitorService osmService)
      throws Exception
  {
    // need a vertex and port number
    String guidString = Model.getRootVertex().getGuid().toColonString();
    int portNum = ((OSM_Port)(Model.getRoot())).getPortNumber();
    AbstractPortTreeModel model = null;
    
    // if the updater is an SMT_Updater, skip analysis, probably already done
    if(updater instanceof SMT_UpdateService)
    {
      // the updater may or may not have the model yet
      SMT_UpdateService sus = ((SMT_UpdateService)updater);
      if((sus == null) || (sus.getVertexMap() == null))
      {
        // do it old-school (model not ready)
      logger.severe("getting the treemodel for PortTreePanel without the SMT_UpdateService (not ready?)");
       model = getTreeModel(osmService, guidString, portNum);
      }
      else
      {
        model = getTreeModel((sus).getVertexMap(), guidString, portNum);
      if(sus.getCollection() != null)
        HistorySize = sus.getCollection().getSize();
      }
    }
    else
      model = getTreeModel(osmService, guidString, portNum);

    // just update the data without changing the structure
    if(model != null)
      updateTreeModel(model);
  }

}
