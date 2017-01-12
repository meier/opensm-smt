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
 *        file: SubnetTreePanel.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.tree;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import gov.llnl.lc.infiniband.opensm.plugin.data.MAD_Counter;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Updater;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_ServiceChangeListener;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.infiniband.opensm.plugin.event.OsmEvent;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionEvent;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionListener;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionUpdater;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphUpdater;
import gov.llnl.lc.infiniband.opensm.plugin.graph.SystemErrGraphListener;
import gov.llnl.lc.infiniband.opensm.plugin.gui.text.FabricRootNodePopupMenu;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.data.SMT_UpdateService;
import gov.llnl.lc.smt.manager.GraphSelectionManager;

/**********************************************************************
 * A JPanel that contains a tree and its model, representing information
 * common to the "Subnet".  Usually considered "Detailed" information,
 * this panel contains subtrees for the;
 *   Subnet Manager
 *   Subnet Administrator
 *   Performance Manager
 *   Event Counters
 *   MAD Counters
 *   OpenSM Monitoring Service
 *   Configuration (options)
 * <p>
 * @see  gov.llnl.lc.infiniband.opensm.plugin.gui.tree.SubnetTreeModel
 *
 * @author meier3
 * 
 * @version Oct 8, 2015 3:21:51 PM
 **********************************************************************/
public class SubnetTreePanel extends JPanel implements OSM_ServiceChangeListener, CommonLogger, IB_GraphSelectionListener
{
  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = 4095131148248618086L;
  /**
   * Create the panel.
   */
  private JTree           tree;
  private IB_GraphUpdater GraphUpdater;
  private SubnetTreeModel Model;
  private int HistorySize = 0;

  private FabricRootNodePopupMenu rootPopup = new FabricRootNodePopupMenu();
  
  public SubnetTreePanel()
  {
    setBorder(new TitledBorder(null, "Fabric Details", TitledBorder.LEADING, TitledBorder.TOP, null,
        null));
    setLayout(new BorderLayout(0, 0));

    GraphUpdater = new IB_GraphUpdater();

    // // put the graph in the panel, and tell the manager I need to listen for
    // events
    // globalFabricManager =
    // gov.llnl.lc.infiniband.opensm.client.browser.manager.FabricManager.getInstance();
    // initTree();

  }

  private void setTreeRootNode(UserObjectTreeNode root)
  {
    DefaultTreeModel tm = new DefaultTreeModel(root);
    if (tm != null)
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
    final SubnetTreePanel thisPanel = this;

    tree.setCellRenderer(new SMTUserObjectTreeCellRenderer(true));

    // this tree is selectable,through the graph listener mechanism
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
          OpenSmMonitorService osm = Model.getRoot();
          // System.err.println("The name of the vertex is: " + v.getName());
          GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(thisPanel, osm, tn));
        }
      }
    });

    tree.addMouseListener(new MouseAdapter()
    {
      public void mousePressed(MouseEvent e)
      {
        if (isCounterSelected(e))
        {
          showMadCounterMenu(e);
        }
        else if(isEventSelected(e))
        {
          showEventCounterMenu(e);
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
  


  protected boolean isCounterSelected(MouseEvent e)
  {
    // is the selected component in the panel, a MAD counter?
    if (e.isPopupTrigger())
    {
      TreePath path = tree.getPathForLocation(e.getX(), e.getY());
      tree.setSelectionPath(path);
      UserObjectTreeNode tn = (UserObjectTreeNode) tree.getLastSelectedPathComponent();
       if (tn != null)
      {
        if(SubnetTreeModel.isMAD_Counter(tn))
          return true;
      }
    }
    return false;
  }

  protected boolean isEventSelected(MouseEvent e)
  {
    // is the selected component in the panel, an Event counter?
    if (e.isPopupTrigger())
    {
      TreePath path = tree.getPathForLocation(e.getX(), e.getY());
      tree.setSelectionPath(path);
      UserObjectTreeNode tn = (UserObjectTreeNode) tree.getLastSelectedPathComponent();
       if (tn != null)
      {
        if(SubnetTreeModel.isEvent_Counter(tn))
          return true;
      }
    }
    return false;
  }

  protected OsmEvent getSelectedEvent(MouseEvent e)
  {
    // is the selected component in the panel, an Event counter?
    if (e.isPopupTrigger())
    {
      TreePath path = tree.getPathForLocation(e.getX(), e.getY());
      tree.setSelectionPath(path);
      UserObjectTreeNode tn = (UserObjectTreeNode) tree.getLastSelectedPathComponent();
      return SubnetTreeModel.getEvent_Counter(tn);
    }
    return null;
  }

  protected MAD_Counter getSelectedCounter(MouseEvent e)
  {
    // is the selected component in the panel, a MAD counter?
    if (e.isPopupTrigger())
    {
      TreePath path = tree.getPathForLocation(e.getX(), e.getY());
      tree.setSelectionPath(path);
      UserObjectTreeNode tn = (UserObjectTreeNode) tree.getLastSelectedPathComponent();
      return SubnetTreeModel.getMAD_Counter(tn);
    }
    return null;
  }

  protected void showMadCounterMenu(MouseEvent e)
  {
    // this is private, all checks already passed (see isCounterSelected())
    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
    tree.setSelectionPath(path);
    Rectangle pathBounds = tree.getUI().getPathBounds(tree, path);
    MAD_CounterPopupMenu menu = new MAD_CounterPopupMenu(Model, Model.getMAD_Stats(), getSelectedCounter(e), getHistorySize());
    menu.show(tree, pathBounds.x, pathBounds.y + pathBounds.height);
  }

  protected void showEventCounterMenu(MouseEvent e)
  {
    // this is private, all checks already passed (see isEventSelected())
    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
    tree.setSelectionPath(path);
    Rectangle pathBounds = tree.getUI().getPathBounds(tree, path);
    Event_CounterPopupMenu menu = new Event_CounterPopupMenu(Model, Model.getEvent_Stats(), getSelectedEvent(e), getHistorySize());
    menu.show(tree, pathBounds.x, pathBounds.y + pathBounds.height);
  }

  protected void showVertexMenu(MouseEvent e)
  {
    // this is private, all checks already passed (see isCounterSelected())
    // VertexTreePopupMenu menu = new VertexTreePopupMenu();
    // TODO: only show if on screen, do a check for these dimentions and
    // visibility
    //
    // ?? sometimes get;
    // java.awt.IllegalComponentStateException: component must be showing on the
    // screen to determine its location
    try
    {
      if (e.getComponent().isVisible())
      {
        VertexTreePopupMenu menu = new VertexTreePopupMenu();

        menu.show(tree, e.getX(), e.getY());
      }
    }
    catch (Exception ex)
    {
      logger.severe("Can't show VertexTreePopupMenu() (" + ex.getMessage() + ")");
    }
  }

  protected void showRootPopupMenu(MouseEvent e)
  {
    // this is private, all checks already passed (see isCounterSelected())
    // VertexTreePopupMenu menu = new VertexTreePopupMenu();
    // TODO: only show if on screen, do a check for these dimentions and
    // visibility
    //
    // ?? sometimes get;
    // java.awt.IllegalComponentStateException: component must be showing on the
    // screen to determine its location
    try
    {
      if (e.getComponent().isVisible())
      {
        rootPopup.show(tree, e.getX(), e.getY());
      }
    }
    catch (Exception ex)
    {
      logger.severe("Can't show RootPopupPopupMenu() (" + ex.getMessage() + ")");
    }
  }

  public static SubnetTreeModel getTreeModel(OpenSmMonitorService OMS)
  {
    return new SubnetTreeModel(OMS);
  }

  public static void main(String[] args) throws Exception
  {
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLocation(100, 50);

    // get the data model from OMS, connection, or file
    OpenSmMonitorService OMS = OpenSmMonitorService.getOpenSmMonitorService("localhost", "10011");
    if (OMS == null)
      System.exit(-1);

    System.err.println(OMS.getFabricName());

    SubnetTreeModel model = getTreeModel(OMS);
    SubnetTreePanel etp = new SubnetTreePanel();
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
    OMS = OpenSmMonitorService.getOpenSmMonitorService("localhost", "10011");
    model = getTreeModel(OMS);
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

  }

  protected int getHistorySize()
  {
    return HistorySize;
  }

  public void setTreeModel(SubnetTreeModel model)
  {
    this.Model = model;
    UserObjectTreeNode root = (UserObjectTreeNode) this.Model.getRootNode();

    this.setTreeRootNode(root);
  }

  public void updateTreeModel(SubnetTreeModel model)
  {
    // assumes model already exists
    // if this is a completely different modle, just replace it
    this.Model.updateModel(model);
    this.repaint();
  }
  
  public void setRootNodePopup(FabricRootNodePopupMenu rootNodePopupMenu)
  {
    rootPopup = rootNodePopupMenu;
//    tree.setComponentPopupMenu(rootPopup);
  }

  @Override
  public void valueChanged(IB_GraphSelectionUpdater source, IB_GraphSelectionEvent event)
  {
  }

  @Override
  public void osmFabricUpdate(OSM_Fabric osmFabric) throws Exception
  {
    // TODO Auto-generated method stub

  }

  @Override
  public void osmServiceUpdate(OMS_Updater updater, OpenSmMonitorService osmService) throws Exception
  {
    SubnetTreeModel model = null;
    // if the updater is an SMT_Updater, skip analysis, probably already done
    if (updater instanceof SMT_UpdateService)
    {
     SMT_UpdateService sus = ((SMT_UpdateService)updater);
     if (osmService != null)
        model = getTreeModel(osmService);
     
     if(sus.getCollection() != null)
       HistorySize = sus.getCollection().getSize();
     
    }
    
 
    // just update the data without changing the structure
    if (model != null)
      updateTreeModel(model);
  }

}
