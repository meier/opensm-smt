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
 *        file: OptionMapTreePanel.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.tree;

import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Updater;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_ServiceChangeListener;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionEvent;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionListener;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionUpdater;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphUpdater;
import gov.llnl.lc.infiniband.opensm.plugin.graph.SystemErrGraphListener;
import gov.llnl.lc.infiniband.opensm.plugin.gui.text.FabricRootNodePopupMenu;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.data.SMT_UpdateService;
import gov.llnl.lc.smt.manager.GraphSelectionManager;

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

public class OptionMapTreePanel extends JPanel implements OSM_ServiceChangeListener, CommonLogger, IB_GraphSelectionListener
{
  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = -4226619244786432197L;
  /**
   * Create the panel.
   */
  private JTree           tree;
  private IB_GraphUpdater GraphUpdater;
  private OptionMapTreeModel Model;
  private int HistorySize = 0;


  private FabricRootNodePopupMenu rootPopup = new FabricRootNodePopupMenu();
  
  public OptionMapTreePanel()
  {
    setBorder(new TitledBorder(null, "Fabric Configuration (options)", TitledBorder.LEADING, TitledBorder.TOP, null,
        null));
    setLayout(new BorderLayout(0, 0));

    GraphUpdater = new IB_GraphUpdater();
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
      logger.info("The DefaultTreeModel for the OptionMapTreePanel is null");
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
    final OptionMapTreePanel thisPanel = this;

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
          vmn.getMemberObject();
          // craft a selection event, for this vertex
          OpenSmMonitorService osm = Model.getRoot();
          GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(thisPanel, osm, tn));
        }
      }
    });

    tree.addMouseListener(new MouseAdapter()
    {
      public void mousePressed(MouseEvent e)
      {
        if(isTreeNodeSelected(e))
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
  

  protected void showRootPopupMenu(MouseEvent e)
  {
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

  public static OptionMapTreeModel getTreeModel(OpenSmMonitorService OMS)
  {
    return new OptionMapTreeModel(OMS);
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

    OptionMapTreeModel model = getTreeModel(OMS);
    OptionMapTreePanel etp = new OptionMapTreePanel();
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

  public void setTreeModel(OptionMapTreeModel model)
  {
    this.Model = model;
    UserObjectTreeNode root = (UserObjectTreeNode) this.Model.getRootNode();

    this.setTreeRootNode(root);
  }

  public void updateTreeModel(OptionMapTreeModel model)
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
    OptionMapTreeModel model = null;
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
