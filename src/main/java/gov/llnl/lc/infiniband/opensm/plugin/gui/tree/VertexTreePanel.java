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
 *        file: VertexTreePanel.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.tree;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

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
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphUpdater;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;
import gov.llnl.lc.infiniband.opensm.plugin.graph.SystemErrGraphListener;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.data.SMT_UpdateService;
import gov.llnl.lc.smt.manager.GraphSelectionManager;
import gov.llnl.lc.smt.manager.SMT_RouteManager;

public class VertexTreePanel extends JPanel implements OSM_ServiceChangeListener, CommonLogger, IB_GraphSelectionListener
{
	/**
   * 
   */
  private static final long serialVersionUID = -1408208398857881002L;
  private static final SMTUserObjectTreeCellRenderer VertexCellRenderer = new SMTUserObjectTreeCellRenderer(true);

  /**
	 * 
	 */

	/**
	 * Create the panel.
	 */
	private JTree tree;
  private IB_GraphUpdater   GraphUpdater;
  private VertexTreeModel Model;
  
  private int HistorySize = 0;
	
	
	public VertexTreePanel()
	{
	  setBorder(new TitledBorder(null, "Node Tree", TitledBorder.LEADING, TitledBorder.TOP, null, null));
	  setLayout(new BorderLayout(0, 0));
	  
    GraphUpdater = new IB_GraphUpdater();

//    // put the graph in the panel, and tell the manager I need to listen for events
//    globalFabricManager = gov.llnl.lc.infiniband.opensm.client.browser.manager.FabricManager.getInstance();
//    initTree();

	}
	
	private void setTreeRootNode(UserObjectTreeNode root)
	{
	  DefaultTreeModel tm = new DefaultTreeModel(root);
	  if(tm != null)
	  {
	    // Do I have to recreate the tree, and put it here?
	    initTree();
      this.tree.setModel(tm);
	  }
	  else
	    logger.info("The DefaultTreeModel for the VertexTreePanel is null");
	}

  
  public IB_GraphSelectionUpdater getGraphUpdater()
  {
    return GraphUpdater;
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
      final VertexTreePanel thisPanel = this;

     tree.setToolTipText("Explore the elements and attributes of a node.");
      tree.setCellRenderer(VertexCellRenderer);
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
          showPortCounterMenu(e);
        }
        else if(isTreeNodeSelected(e))
        {
          // do nothing
//          System.err.println("deliberately not showing a popup at this location");
        }
        else if(e.isPopupTrigger())
        {
          showVertexMenu(e);
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
  
  protected void showPortCounterMenu(MouseEvent e)
  {
    // this is private, all checks already passed (see isCounterSelected())
    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
    tree.setSelectionPath(path);
    UserObjectTreeNode tn = (UserObjectTreeNode) tree.getLastSelectedPathComponent();
    
    // I know for a fact this is a counter, and I need to get the OSM_Port
    // this nodes parent is "counters" and its Parent is "port #" with the port value
    UserObjectTreeNode cn = (UserObjectTreeNode)tn.getParent();
    UserObjectTreeNode pn = (UserObjectTreeNode)cn.getParent();
    NameValueNode pmn = (NameValueNode) pn.getUserObject();
    OSM_Port port = null;
    if(pmn.getMemberObject() instanceof OSM_Port)
      port = (OSM_Port)pmn.getMemberObject();
    
    NameValueNode vmn = (NameValueNode) tn.getUserObject();
    PortCounterName pcn = PortCounterName.getByName(vmn.getMemberName());
    Rectangle pathBounds = tree.getUI().getPathBounds(tree, path);
    PortCounterPopupMenu menu = new PortCounterPopupMenu(Model, port, pcn, getHistorySize());
    menu.show(tree, pathBounds.x, pathBounds.y + pathBounds.height);
  }
  
  protected void showVertexMenu(MouseEvent e)
  {
    // this is private, all checks already passed (see isCounterSelected())
//    VertexTreePopupMenu menu = new VertexTreePopupMenu();
    // TODO: only show if on screen, do a check for these dimentions and visibility
    //
    // ?? sometimes get;
    // java.awt.IllegalComponentStateException: component must be showing on the screen to determine its location
    try
    {
      if(e.getComponent().isVisible())
      {
        VertexTreePopupMenu menu = SMT_RouteManager.getInstance().getVertexTreePopupMenu(Model.getRootVertex());
 
        menu.show(tree, e.getX(), e.getY());
      }
    }
    catch(Exception ex)
    {
      logger.severe("Can't show VertexTreePopupMenu() (" + ex.getMessage() + ")");
    }
  }

  
  public static VertexTreeModel getTreeModel(OpenSmMonitorService OMS, String guidString)
  {
    LinkedHashMap<String, IB_Vertex> vertexMap = IB_Vertex.createVertexMap(OMS.getFabric());
    LinkedHashMap<String, OSM_Port> portMap = OMS.getFabric().getOSM_Ports();
    return getTreeModel(vertexMap, portMap, guidString);
   }
  
  public static VertexTreeModel getTreeModel(LinkedHashMap<String, IB_Vertex> vertexMap, LinkedHashMap<String, OSM_Port> portMap, String guidString)
  {
    if (vertexMap == null)
      System.exit(-1);
    
    ArrayList<OSM_Port> pList = null;
    
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
    else
    {
      // build a portMap for this node/vertex
      pList = myVertex.getNode().getOSM_Ports(portMap);
    }
    return new VertexTreeModel(myVertex, pList);
   }
  
  
    public static void main(String[] args) throws Exception
    {
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLocation(100, 50);

    // get the data model from OMS, connection, or file
    OpenSmMonitorService OMS = OpenSmMonitorService.getOpenSmMonitorService("localhost", "10013");
    if(OMS == null)
      System.exit(-1);
    
    System.err.println(OMS.getFabricName());
    String guidString = "0006:6a00:e300:440e";

    VertexTreeModel model = getTreeModel(OMS, guidString);
    VertexTreePanel etp = new VertexTreePanel();
    etp.setTreeModel(model);
    
    SystemErrGraphListener listener = new SystemErrGraphListener();
    etp.getGraphUpdater().addIB_GraphSelectionListener(listener);
    etp.getGraphUpdater().addIB_GraphSelectionListener(etp);

    JScrollPane scroller = new JScrollPane(etp);  
    frame.getContentPane().add(scroller, BorderLayout.CENTER); 
    frame.pack();
    frame.setVisible(true);
    
    // try same guid over, wait for new data.
    // don't close or collapse, just update info
    try
    {
      TimeUnit.MINUTES.sleep(1);
      System.err.println("1");
      TimeUnit.MINUTES.sleep(1);
      System.err.println("2");
      TimeUnit.MINUTES.sleep(1);
      System.err.println("3");
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
    // get new snapshot, for same node 
    OMS   = OpenSmMonitorService.getOpenSmMonitorService("localhost", "10011");
    model = getTreeModel(OMS, guidString);
    UserObjectTreeNode r = (UserObjectTreeNode)model.getRoot();
    // just update the data without changing the structure
    etp.updateTreeModel(model);
    
    // wait a little, before the next test
    try
    {
      TimeUnit.SECONDS.sleep(5);
      System.err.println("1");
      TimeUnit.SECONDS.sleep(5);
      System.err.println("2");
      TimeUnit.SECONDS.sleep(5);
      System.err.println("3");
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }

    // a different node, so I want a brand new model
    guidString = "0006:6a00:e300:4430";
    model = getTreeModel(OMS, guidString);
    r = (UserObjectTreeNode)model.getRoot();
    
    // this should wipe out the old one, and start new
    etp.updateTreeModel(model);

    // try this guid over again, wait for new data.
    // don't close or collapse, just update info
    try
    {
      TimeUnit.MINUTES.sleep(1);
      System.err.println("1");
      TimeUnit.MINUTES.sleep(1);
      System.err.println("2");
      TimeUnit.MINUTES.sleep(1);
      System.err.println("3");
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
    // get new snapshot, for same node 
    OMS   = OpenSmMonitorService.getOpenSmMonitorService("localhost", "10011");
    model = getTreeModel(OMS, guidString);
    r = (UserObjectTreeNode)model.getRoot();
    // just update the data without changing the structure
    etp.updateTreeModel(model);
  }

    public void setTreeModel(VertexTreeModel model)
    {
    this.Model = model;
    UserObjectTreeNode root = (UserObjectTreeNode) this.Model.getRoot();

    this.setTreeRootNode(root);
   }

    public void updateTreeModel(VertexTreeModel model)
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
  

  /************************************************************
   * Method Name:
   *  getHistorySize
   **/
  /**
   * Returns the value of historySize
   *
   * @return the historySize
   *
   ***********************************************************/
  
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
    VertexTreeModel model = null;
    
   // if the updater is an SMT_Updater, skip analysis, probably already done
    if(updater instanceof SMT_UpdateService)
    {
      // the updater may or may not have the model yet
      SMT_UpdateService sus = ((SMT_UpdateService)updater);
      if((sus == null) || (sus.getVertexMap() == null))
      {
        // do it old-school (model not ready)
      logger.severe("getting the treemodel for vertexTreePanel without the SMT_UpdateService (not ready?)");
       model = getTreeModel(osmService, guidString);
       if(sus.getCollection() != null)
         HistorySize = sus.getCollection().getSize();
      }
      else
      {
         model = getTreeModel(sus.getVertexMap(), sus.getOMS().getFabric().getOSM_Ports(), guidString);
         if(sus.getCollection() != null)
           HistorySize = sus.getCollection().getSize();
      }
    }
    else
      model = getTreeModel(osmService, guidString);

   // just update the data without changing the structure
    if(model != null)
      updateTreeModel(model);
  }

}
