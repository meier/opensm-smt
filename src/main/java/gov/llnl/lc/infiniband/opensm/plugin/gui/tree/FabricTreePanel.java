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
 *        file: FabricTreePanel.java
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
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;
import gov.llnl.lc.infiniband.opensm.plugin.graph.SystemErrGraphListener;
import gov.llnl.lc.infiniband.opensm.plugin.gui.text.FabricRootNodePopupMenu;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.data.SMT_UpdateService;
import gov.llnl.lc.smt.event.SmtMessage;
import gov.llnl.lc.smt.event.SmtMessageType;
import gov.llnl.lc.smt.manager.GraphSelectionManager;
import gov.llnl.lc.smt.manager.MessageManager;
import gov.llnl.lc.smt.manager.SMT_AnalysisType;
import gov.llnl.lc.util.BinList;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class FabricTreePanel extends JPanel implements OSM_ServiceChangeListener, ActionListener, TreeSelectionListener, CommonLogger
{
  /** describe serialVersionUID here **/
  private static final long serialVersionUID = -5560944667279679964L;
  private static final SMTFabricTreeCellRenderer NodeCellRenderer = new SMTFabricTreeCellRenderer(true);

  private JTree             tree;
//  private FabricTreeModel Model;
  private FabricTreePanel thisPanel = this;
  
  private FabricRootNodePopupMenu rootPopup = new FabricRootNodePopupMenu();

  private static final String FabOverview    = "Fabric Overview";
  private static final String FabGraph       = "Fabric Graph";
  private static final String FabUtilization = "Fabric " + SMT_AnalysisType.SMT_UTILIZATION.getAnalysisName();
  
  private OpenSmMonitorService OMS = null;
  
  public JTree getTree()
  {
    return tree;
  }

  public void setTree(JTree tree)
  {
  }
  

  public FabricTreePanel()
  {
    setBorder(new TitledBorder(null, "Fabric Tree", TitledBorder.LEADING, TitledBorder.TOP, null,null));
    setLayout(new BorderLayout(0, 0));
    
 /******************************redundant remove (see initTree) **********************************************************/   
    // create a brand new tree, and set it all up
    tree = new JTree();
    
    tree.setToolTipText("Explore the hierarchy of the fabrics links and nodes.");
    tree.addTreeSelectionListener(new TreeSelectionListener()
    {
      public void valueChanged(TreeSelectionEvent arg0)
      {
         if (tree.getLastSelectedPathComponent() instanceof UserObjectTreeNode)
        {
          UserObjectTreeNode tn = (UserObjectTreeNode) tree.getLastSelectedPathComponent();
          IB_Vertex v = (IB_Vertex) tn.getUserObject();
          logger.info("first one one");
        }
         System.err.println("Something from tree action listener valueChanged()");

      }
    });
    add(tree, BorderLayout.CENTER);
    
    /******************************redundant remove (see initTree) **********************************************************/   
  }

  public void setTreeRootNode(UserObjectTreeNode root)
  {
    DefaultTreeModel tm = new DefaultTreeModel(root);

    if (tm != null)
    {
      // Do I have to recreate the tree, and put it here?
      initTree();
      this.tree.setModel(tm);
    }
    else
      logger.severe("The DefaultTreeModel for the NodeTreePanel is null");
  }
  
  public UserObjectTreeNode getTreeRootNode()
  {
    return UserObjectTreeNode.getTreeRootNode(tree);
  }
  

  private void initTree()
  {
    // create the tree and add it to the panel, delete any old tree first
    if (tree != null)
    {
      // check to see if its contained by the panel, and remove it if necessary
      this.remove(tree);
    }

    final FabricTreePanel thisPanel = this;

    // create a brand new tree, and set it all up
    tree = new JTree();
    tree.setToolTipText("Explore the nature and structure of the fabric.");
    tree.setCellRenderer(NodeCellRenderer);

    tree.addTreeSelectionListener(thisPanel);
    
    tree.addMouseListener ( new MouseAdapter ()
   {
     public void mousePressed(MouseEvent e)
     {
       // only pop up a menu for specific components in the tree
       if (e.isPopupTrigger())
       {
         TreePath path = tree.getPathForLocation(e.getX(), e.getY());
         tree.setSelectionPath(path);
         UserObjectTreeNode tn = (UserObjectTreeNode) tree.getLastSelectedPathComponent();
         if (tn != null)
         {
           NameValueNode nvn = (NameValueNode) tn.getUserObject();
           IB_Vertex v = (IB_Vertex) nvn.getMemberObject();
           
           nvn = (NameValueNode) getTreeRootNode().getUserObject();
           IB_Vertex r = (IB_Vertex) nvn.getMemberObject();
           
           if (v.equals(r))
           {
             Rectangle pathBounds = tree.getUI().getPathBounds(tree, path);
             if (pathBounds != null && pathBounds.contains(e.getX(), e.getY()))
             {
               rootPopup.show(tree, pathBounds.x, pathBounds.y + pathBounds.height);
             }
           }
         }

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

  protected boolean hasMatchingSystemGuid(IB_Vertex v)
  {
    // return true if this vertex's node guid, system guid match, and match a valid system guid
    if(hasSystemGuid(v))
    {
      IB_Guid sg = new IB_Guid(v.getNode().sbnNode.sys_guid);
      if(v.getGuid().equals(sg))
        return true; 
      
      // the node guid and system guid are different, but may still want to return true, if this is the top
      // singleton node
      NameValueNode nvn = (NameValueNode) this.getTreeRootNode().getUserObject();
      IB_Vertex rt = (IB_Vertex) nvn.getMemberObject();
      
      if((rt.getDepth() -1) == v.getDepth())
        return true;
    }
    return false;
  }
  
  protected boolean hasSystemGuid(IB_Vertex v)
  {
    // return true if this vertex's system guid match a valid system guid
    if((OMS == null) || (v == null) || (v.getGuid() == null))
    {
      System.err.println("Can't determine SystemGuid ness");
      if(OMS == null)
        System.err.println("OMS is null");
      if(v == null)
        System.err.println("v is null");
      else if(v.getGuid() == null)
        System.err.println("vs guid is null");
      return false;
    }
    OSM_Fabric fabric = OMS.getFabric();
    fabric.createSystemGuidBins(false);

    BinList <IB_Guid> guidBins = fabric.getSystemGuidBins();
    
    if(guidBins.size() < 1)
      return false;
    
    // we have at least a single system guid, so we can continue
    
    // does this vertex system guid, match the KNOWN system guids?
    IB_Guid sg = new IB_Guid(v.getNode().sbnNode.sys_guid);
    int k = 0;
    for(ArrayList <IB_Guid> gList: guidBins)
    {
      // get the bins key, which is the guid string
      String sGuid = guidBins.getKey(k);
      IB_Guid sysGuid = new IB_Guid(sGuid);
      
      if(sg.equals(sysGuid))
        return true;
      k++;
    }
    return false;
  }
  
  protected boolean isSystemWithSingleTop(IB_Vertex v)
  {
    // return true if this vertex's node guid, system guid match, and match a valid system guid
    if(hasSystemGuid(v))
    {
      // the node guid and system guid are different, but may still want to return true, if this is the top
      // singleton node
      
      IB_Guid sg = new IB_Guid(v.getNode().sbnNode.sys_guid);
      if(sg.equals(v.getGuid()))   // Cant be the same, if this is a real switch
        return false;
      
      NameValueNode nvn = (NameValueNode) this.getTreeRootNode().getUserObject();
      IB_Vertex rt = (IB_Vertex) nvn.getMemberObject();
      
      if((rt.getDepth() -1) == v.getDepth())
        return true;
    }
    return false;
  }
  
  /************************************************************
   * Method Name: main
   **/
  /**
   * Describe the method here
   * 
   * @see describe related java objects
   * 
   * @param args
   ***********************************************************/
  public static void main(String[] args) throws Exception
  {
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLocation(100, 50);

    // get the data model from OMS, connection, or file
    OpenSmMonitorService OMS = OpenSmMonitorService.getOpenSmMonitorService("localhost", "10011");
    if(OMS == null)
      System.exit(-1);
    
    System.err.println(OMS.getFabricName());

    LinkedHashMap<String, IB_Vertex> vertexMap = IB_Vertex.createVertexMap(OMS.getFabric());
    if (vertexMap == null)
      System.exit(-1);
    System.err.println("There are " + vertexMap.size() + " vertices");
    FabricTreeModel treeModel = new FabricTreeModel(vertexMap, OMS.getFabric());

    UserObjectTreeNode root = (UserObjectTreeNode) treeModel.getRoot();
    if (root != null)
    {
      System.err.println("The root has " + root.getChildCount() + " children");
    }

    FabricTreePanel vtp = new FabricTreePanel();
    vtp.setTreeRootNode(root);
    
    SystemErrGraphListener listener = new SystemErrGraphListener();
    VertexTreePanel listener2 = new VertexTreePanel();
    
    GraphSelectionManager.getInstance().addIB_GraphSelectionListener(listener);
    GraphSelectionManager.getInstance().addIB_GraphSelectionListener(listener2);

    JScrollPane scroller = new JScrollPane(vtp);  
    frame.getContentPane().add(scroller, BorderLayout.CENTER); 
    frame.pack();
    frame.setVisible(true);
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
    FabricTreeModel model = null;
    setOMS(osmService);
    
    // if the updater is an SMT_Updater, skip analysis, already done
    if(updater instanceof SMT_UpdateService)
       model = new FabricTreeModel(((SMT_UpdateService)updater).getVertexMap(), osmService.getFabric());
    else
    {
      LinkedHashMap<String, IB_Vertex> vertexMap = IB_Vertex.createVertexMap(osmService.getFabric());
      if (vertexMap == null)
        System.exit(-1);
      logger.info("There are " + vertexMap.size() + " vertices");
      model = new FabricTreeModel(vertexMap, osmService.getFabric());
    }
    
    UserObjectTreeNode root = (UserObjectTreeNode) model.getRoot();
    if (root != null)
    {
      logger.info("The root has " + root.getChildCount() + " children");
    }

    setTreeRootNode(root);
   }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    // this handles right mouse clicks from menu items
    if (e.getActionCommand().equals(FabUtilization))
    {
      if (e.getSource() instanceof JMenuItem)
      {
        JMenuItem src = (JMenuItem) e.getSource();
        Container cont = src.getParent();
        
      MessageManager.getInstance().postMessage(
      new SmtMessage(SmtMessageType.SMT_MSG_INFO, "Fabric Utilization )"));
       
      }
    }
    
    if (e.getActionCommand().equals(FabOverview))
    {
      if (e.getSource() instanceof JMenuItem)
      {
        JMenuItem src = (JMenuItem) e.getSource();
        Container cont = src.getParent();

        MessageManager.getInstance().postMessage(
            new SmtMessage(SmtMessageType.SMT_MSG_INFO, "Fabric Overview )"));
      }
    }

    if (e.getActionCommand().equals(FabGraph))
    {
      if (e.getSource() instanceof JMenuItem)
      {
        JMenuItem src = (JMenuItem) e.getSource();
        Container cont = src.getParent();

        MessageManager.getInstance().postMessage(
            new SmtMessage(SmtMessageType.SMT_MSG_INFO, "Fabric Graph )"));
      }
    }
    System.err.println("Something from actionPerformed");

  }

  @Override
  public void valueChanged(TreeSelectionEvent e)
  {
  if (tree.getLastSelectedPathComponent() instanceof UserObjectTreeNode)
  {
    UserObjectTreeNode tn = (UserObjectTreeNode) tree.getLastSelectedPathComponent();
    NameValueNode nvn = (NameValueNode) tn.getUserObject();
    IB_Vertex v = (IB_Vertex) nvn.getMemberObject();
    
    // only send out if this is NOT the root vertex, representing the fabric
    nvn = (NameValueNode) getTreeRootNode().getUserObject();
    IB_Vertex r = (IB_Vertex) nvn.getMemberObject();
    
    // is this vertex a system guid node? (a fake one, where the guid and system guid match??)
    boolean isFakeSystemNode      = hasMatchingSystemGuid(v);
    boolean isRealTopSystemSwitch = isSystemWithSingleTop(v);
    
    if(isFakeSystemNode || isRealTopSystemSwitch)
    {
      //generate a sys_guid event (show the SYSTEM panel)
      IB_Guid sg = new IB_Guid(v.getNode().sbnNode.sys_guid);
      NameValueNode snvn = new NameValueNode("sys_guid", sg.toColonString());
      UserObjectTreeNode stn = new UserObjectTreeNode(snvn, false);
      
      GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(thisPanel, v, stn));
    }
    
    // show the SWITCH panel (if a valid/real switch)
    if((!(v.equals(r)) && !isFakeSystemNode) || isRealTopSystemSwitch )
       GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(thisPanel, v, v));
    else
      logger.info("This looks like the dummy root node, do nothing");
  }
}

  public void setRootNodePopup(FabricRootNodePopupMenu rootNodePopupMenu)
  {
    rootPopup = rootNodePopupMenu;
  }

  /************************************************************
   * Method Name:
   *  setOMS
  **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   * @param oMS2
   ***********************************************************/
  public void setOMS(OpenSmMonitorService oms)
  {
    OMS = oms;
  }

}
