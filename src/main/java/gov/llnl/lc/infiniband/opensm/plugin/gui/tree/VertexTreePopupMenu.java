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
 *        file: VertexTreePopupMenu.java
 *
 *  Created on: Oct 31, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.tree;

import gov.llnl.lc.infiniband.opensm.plugin.data.RT_Path;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionEvent;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;
import gov.llnl.lc.smt.event.SmtMessage;
import gov.llnl.lc.smt.event.SmtMessageType;
import gov.llnl.lc.smt.manager.GraphSelectionManager;
import gov.llnl.lc.smt.manager.MessageManager;
import gov.llnl.lc.smt.manager.SMT_AnalysisType;
import gov.llnl.lc.smt.manager.SMT_RouteManager;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultTreeModel;

/**********************************************************************
 * Describe purpose and responsibility of VertexTreePopupMenu
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Oct 31, 2013 9:52:24 AM
 **********************************************************************/
public class VertexTreePopupMenu extends JPopupMenu implements ActionListener
{
  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = -8335712498809772638L;
  
  static final String MsgUtilize = SMT_AnalysisType.SMT_UTILIZATION.getAnalysisName();
  JMenuItem anItem;
  
  private RT_Path Path;
  private IB_Vertex selectedVertex;
  private static IB_Vertex srcVertex;
  private static IB_Vertex dstVertex;
  
  private final JLabel sourceLabel = new JLabel("src:");
  private final JLabel destLabel = new JLabel("dst:");

  public VertexTreePopupMenu(IB_Vertex iv)
 {
   this();
   this.setVertex(iv);
 }

   
  public VertexTreePopupMenu()
  {
    super();
    setLabel("");
      
      JMenu mnPath = new JMenu("Path");
      add(mnPath);
      
      JMenuItem mntmSetAsSource = new JMenuItem("set as Source");
      mnPath.add(mntmSetAsSource);
      
      
      mntmSetAsSource.addActionListener(new ActionListener() 
      {
        public void actionPerformed(ActionEvent e)
        {
          setSource(selectedVertex);
       }
      });
      
      mnPath.add(mntmSetAsSource);
      
      JMenuItem mntmSetAsDestination = new JMenuItem("set as Destination");
      mnPath.add(mntmSetAsDestination);
      mntmSetAsDestination.addActionListener(new ActionListener() 
      {
        public void actionPerformed(ActionEvent e) 
        {
          setDestination(selectedVertex);
        }

      });
      
      mnPath.add(mntmSetAsDestination);

      
       
      JMenuItem mntmSwapSourceAnd = new JMenuItem("swap Source and Destination");
      mnPath.add(mntmSwapSourceAnd);
      mntmSwapSourceAnd.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          swapSrcDst();
        }

       });
      
      JSeparator separator = new JSeparator();
      mnPath.add(separator);
      sourceLabel.setForeground(Color.BLUE);
      sourceLabel.setHorizontalAlignment(SwingConstants.CENTER);

      mnPath.add(sourceLabel);
      destLabel.setForeground(Color.BLUE);
      destLabel.setHorizontalAlignment(SwingConstants.CENTER);
      mnPath.add(destLabel);
      
      JSeparator separator_1 = new JSeparator();
      mnPath.add(separator_1);
      
      JMenuItem mntmShowPathTree = new JMenuItem("show Path Tree");
      mntmShowPathTree.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          // tell the graph manager to "set" the scratch values as the
          // current path, and trigger the display of the tree node
          if(SMT_RouteManager.getInstance().makeScratchCurrent())
            SMT_RouteManager.getInstance().showPathTree();
       }
      });
      mnPath.add(mntmShowPathTree);
      
      anItem = new JMenuItem(MsgUtilize);
      add(anItem);
      anItem.addActionListener(this);
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
  
  /************************************************************
   * Method Name:
   *  getPath
   **/
  /**
   * Returns the value of path
   *
   * @return the path
   *
   ***********************************************************/
  
  public RT_Path getPath()
  {
    return Path;
  }
  

  /************************************************************
   * Method Name:
   *  getVertex
   **/
  /**
   * Returns the value of vertex
   *
   * @return the vertex
   *
   ***********************************************************/
  
  public IB_Vertex getVertex()
  {
    return selectedVertex;
  }


  /************************************************************
   * Method Name:
   *  setVertex
   **/
  /**
   * Sets the value of vertex
   *
   * @param vertex the vertex to set
   *
   ***********************************************************/
  public void setVertex(IB_Vertex vertex)
  {
    this.selectedVertex = vertex;
  }

  /************************************************************
   * Method Name:
   *  setPath
   **/
  /**
   * Sets the value of path
   *
   * @param path the path to set
   *
   ***********************************************************/
  public void setPath(RT_Path path)
  {
    Path = path;
  }


  
  public static String getGuidString(UserObjectTreeNode node)
  {
    NameValueNode nvn = getNameValueNode(node, "guid");
    if(nvn != null)
    {
      // strip the last colon, which is the port number
      return (String)nvn.getMemberObject();
     }
    return null;
  }

  public static NameValueNode getNameValueNode(UserObjectTreeNode node, String name)
  {
    // given a parent node, look for the child node with the given name
    //     must match exactly or return null
    for (Enumeration <UserObjectTreeNode> c = node.children(); c.hasMoreElements() ;)
    {
      UserObjectTreeNode uotn = (UserObjectTreeNode)c.nextElement();
      NameValueNode tst = (NameValueNode)uotn.getUserObject();
      if(name.equals(tst.getMemberName()))
        return tst;
     }
    return null;
  }
  
  private void setSource(IB_Vertex v)
  {
    // send this vertex to the RouteManager
    if(v != null)
    {
      SMT_RouteManager.getInstance().setScratchSource(v.getGuid());
      srcVertex = v;
      sourceLabel.setText("src: " + srcVertex.getName());
    }
  }
  
  private void swapSrcDst()
  {
    // swap source and destination
    IB_Vertex tmp = dstVertex;
    setDestination(srcVertex);
    setSource(tmp);
  }


  private void setDestination(IB_Vertex v)
  {
    // send this vertex to the RouteManager
    if(v != null)
    {
      SMT_RouteManager.getInstance().setScratchDestination(v.getGuid());
      dstVertex = v;
      destLabel.setText("dst: " + dstVertex.getName());
    }
  }


  @Override
  public void actionPerformed(ActionEvent e)
  {
    if (e.getActionCommand().equals(MsgUtilize))
    {
      if (e.getSource() instanceof JMenuItem)
      {
        JMenuItem src = (JMenuItem) e.getSource();
        Container cont = src.getParent();
        if (cont instanceof VertexTreePopupMenu)
        {
          VertexTreePopupMenu ptpm = (VertexTreePopupMenu) cont;
          Component comp = ptpm.getInvoker();
          if (comp instanceof JTree)
          {
            JTree tree = (JTree) comp;

            // I think the Model is of type, PortTreeModel, check for that
            if (tree.getModel() instanceof DefaultTreeModel)
            {
              DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel();

              if (dtm.getRoot() instanceof UserObjectTreeNode)
              {
                UserObjectTreeNode tn = (UserObjectTreeNode) dtm.getRoot();
                System.err.println("Guid is: " + getGuidString(tn));

                MessageManager.getInstance().postMessage(
                    new SmtMessage(SmtMessageType.SMT_MSG_INFO, "Popup a panel for showing the Node Utilization (" + getGuidString(tn)+ ")"));
                
                // craft a selection event that contains necessary info for this utilization event
                GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(this, SMT_AnalysisType.SMT_NODE_UTILIZATION, getGuidString(tn)));
              }
            }
          }
        }
      }
    }
  }

}
