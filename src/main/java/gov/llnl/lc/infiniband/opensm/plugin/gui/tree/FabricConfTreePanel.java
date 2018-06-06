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
 *        file: FabricConfTreePanel.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.tree;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Panel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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

import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Configuration;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionEvent;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionListener;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionUpdater;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;
import gov.llnl.lc.infiniband.opensm.plugin.graph.SystemErrGraphListener;
import gov.llnl.lc.infiniband.opensm.plugin.net.OsmClientApi;
import gov.llnl.lc.infiniband.opensm.plugin.net.OsmServiceManager;
import gov.llnl.lc.infiniband.opensm.plugin.net.OsmSession;
import gov.llnl.lc.infiniband.opensm.xml.IB_FabricConf;
import gov.llnl.lc.logging.CommonLogger;

public class FabricConfTreePanel extends JPanel implements CommonLogger, IB_GraphSelectionListener
{

	/**  describe serialVersionUID here **/
  private static final long serialVersionUID = 8453949270669592560L;
  private static final SMTUserObjectTreeCellRenderer PortCellRenderer = new SMTUserObjectTreeCellRenderer(true);
  /**
	 * Create the panel.
	 */
	private JTree tree;
  private FabricConfTreeModel Model;
  private TitledBorder Title;
	
	public FabricConfTreePanel()
	{
	  Title = new TitledBorder(null, "IB_FabricConf", TitledBorder.LEADING, TitledBorder.TOP, null, null);
	  setBorder(Title);
	  setLayout(new BorderLayout(0, 0));
	}
	
  private void setTreeRootNode(UserObjectTreeNode root)
  {
    DefaultTreeModel tm = new DefaultTreeModel(root);
    if(tm != null)
    {
      // Do I have to recreate the tree, and put it here?
      logger.info("Setting the Port Tree model here");
      initTree();
      this.tree.setModel(tm);
      
      // I want the border title to be the filename
//      Title.setTitle(getTreeModel().getRootConf().getFileName());
    }
    else
      logger.info("The DefaultTreeModel for the IB_FabricConf is null");
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
      final FabricConfTreePanel thisPanel = this;
      
      tree.setToolTipText("Explore the Ideal Fabric Configuration.");
 
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
//            IB_Vertex v = Model.getRootVertex();
//            GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(thisPanel, v, tn));
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
//        else if(isTreeNodeSelected(e))
//        {
//          // do nothing
////          System.err.println("deliberately not showing a popup at this location");
//        }
//        else if(e.isPopupTrigger())
//        {
//          // out in the open, so okay to show general purpose popup
//          showPortMenu(e);
//        }
      }
    });
      
      add(tree, BorderLayout.CENTER);
    }
  
    public FabricConfTreeModel getTreeModel()
    {
      return Model;
     }
    
    public void setTreeModel(FabricConfTreeModel model)
    {
    this.Model = model;
    UserObjectTreeNode root = (UserObjectTreeNode) this.Model.getRootNode();

    this.setTreeRootNode(root);
    }

    public void updateTreeModel(FabricConfTreeModel model)
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

  public static void main(String[] args) throws Exception
  {

    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLocation(100, 50);

            IB_FabricConf fc    = new IB_FabricConf("/home/meier3/IB_DIAGS_OUT/ibfabricconf.xml");

            FabricConfTreeModel model = new FabricConfTreeModel(fc);
            FabricConfTreePanel etp = new FabricConfTreePanel();
            etp.setTreeModel(model);
            
            SystemErrGraphListener listener = new SystemErrGraphListener();
   
            JScrollPane scroller = new JScrollPane(etp);  
            frame.getContentPane().add(scroller, BorderLayout.CENTER); 
            frame.pack();
            frame.setVisible(true);

  }

  public static void mainOrig(String[] args) throws Exception
  {
    OSM_Configuration Config   = null;
    
    /** from the client interface **/
    OsmClientApi clientInterface = null;

    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLocation(100, 50);

    OsmSession ParentSession = null;

    /* the one and only OsmServiceManager */
    OsmServiceManager OsmService = OsmServiceManager.getInstance();
    String hostName = "localhost";
    String portNum  = "10014";

    try
    {
      ParentSession = OsmService.openSession(hostName, portNum, null, null);
    }
    catch (Exception e)
    {
      System.err.println("Could not open a session to: " + hostName + ":" + portNum);
      System.exit(0);
    }

    if (ParentSession != null)
    {
      clientInterface = ParentSession.getClientApi();
      
      if (clientInterface != null)
      {
        Config  = clientInterface.getOsmConfig();
        if(Config != null)
        {

          
          if(Config.getFabricConfig() != null)
          {
            FabricConfTreeModel model = new FabricConfTreeModel(Config.getFabricConfig());
            FabricConfTreePanel etp = new FabricConfTreePanel();
            etp.setTreeModel(model);
            
            SystemErrGraphListener listener = new SystemErrGraphListener();
   
            JScrollPane scroller = new JScrollPane(etp);  
            frame.getContentPane().add(scroller, BorderLayout.CENTER); 
            frame.pack();
            frame.setVisible(true);

          }
       }
     }
    }
  }


}
