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
 *        file: SimpleGraphPopupMenu.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.graph;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import edu.uci.ics.jung.visualization.picking.MultiPickedState;
import gov.llnl.lc.infiniband.opensm.plugin.data.RT_Path;
import gov.llnl.lc.infiniband.opensm.plugin.data.RT_PathLeg;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Edge;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;
import gov.llnl.lc.infiniband.opensm.plugin.gui.tree.RT_PathTreeModel;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.event.SmtMessage;
import gov.llnl.lc.smt.event.SmtMessageType;
import gov.llnl.lc.smt.manager.MessageManager;
import gov.llnl.lc.smt.manager.SMT_GraphManager;
import gov.llnl.lc.smt.manager.SMT_RouteManager;

public class SimpleGraphPopupMenu extends JPopupMenu implements ActionListener, CommonLogger
{
  private JLabel VertexNameLabel = new JLabel("vertex name");
  private final JSeparator separator = new JSeparator();
  private SimpleGraphPopupMenu thisMenu = this;

  private IB_Vertex selectedVertex;
  private static IB_Vertex srcVertex;
  private static IB_Vertex dstVertex;
  
  private final JLabel sourceLabel = new JLabel("src:");
  private final JLabel destLabel = new JLabel("dst:");

  private RT_PathTreeModel Pmodel;
  private RT_Path Path;
  private final JMenuItem Show = new JMenuItem("Node Tree");
  
  private final JSeparator separator_1 = new JSeparator();
  private final JMenu mnPath = new JMenu("Path");
  private final JMenuItem mntmSetAsSource = new JMenuItem("set as Source");
  private final JMenuItem mntmSetAsDestination = new JMenuItem("set as Destination");
  private final JSeparator separator_2 = new JSeparator();
  private final JMenuItem mntmSwapSourceAnd = new JMenuItem("swap Source and Destination");
  private final JSeparator separator_3 = new JSeparator();
  private final JMenuItem mntmShowPathTree = new JMenuItem("show Path Tree");
  private final JMenuItem mntmDecorateGraphWith = new JMenuItem("decorate Graph with Path");


  
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
    this.setVertexName(this.selectedVertex.getName());

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

  /************************************************************
   * Method Name:
   *  getSwap
   **/
  /**
   * Returns the value of swap
   *
   * @return the swap
   *
   ***********************************************************/
  
  public boolean getSwap()
  {
    return Show.isSelected();
  }

  /************************************************************
   * Method Name:
   *  getPathTreeCB
   **/
  /**
   * Returns the value of pathTreeCB
   *
   * @return the pathTreeCB
   *
   ***********************************************************/
  
  /************************************************************
   * Method Name:
   *  SimpleGraphPopupMenu
  **/
  /**
   * This popup menu is used by the simple graph, when a vertex is
   * selected.  
   * 
   *
   * @see     describe related java objects
   *
   ***********************************************************/
  public SimpleGraphPopupMenu()
  {
    super();
    setLabel("");
    VertexNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
    
    add(VertexNameLabel);
    
    add(separator);
    
    add(mnPath);
    mntmSetAsSource.addActionListener(new ActionListener() 
    {
      public void actionPerformed(ActionEvent e)
      {
        setSource(selectedVertex);
     }
    });
    
    mnPath.add(mntmSetAsSource);
    mntmSetAsDestination.addActionListener(new ActionListener() 
    {
      public void actionPerformed(ActionEvent e) 
      {
        setDestination(selectedVertex);
      }

    });
    
    mnPath.add(mntmSetAsDestination);
    mntmSwapSourceAnd.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        swapSrcDst();
      }

     });
    
    mnPath.add(mntmSwapSourceAnd);
    
    mnPath.add(separator_2);
    sourceLabel.setForeground(Color.BLUE);
    sourceLabel.setHorizontalAlignment(SwingConstants.CENTER);
    
    mnPath.add(sourceLabel);
    destLabel.setForeground(Color.BLUE);
    destLabel.setHorizontalAlignment(SwingConstants.CENTER);
    
    mnPath.add(destLabel);
    
    mnPath.add(separator_3);
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
    mntmDecorateGraphWith.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) 
      {
        MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INFO, "Using the current path to decorate the graph"));
        System.err.println("Use the current path to decorate the graph");
        /*
         * from the visualizat5ion viewer, get the picked state for the edges
         * and then "pick" all the edges assoaciated with this path.
         */
        if(SMT_RouteManager.getInstance().makeScratchCurrent())
        {
          RT_Path path = SMT_RouteManager.getInstance().getCurrentPath();
          
          MultiPickedState<IB_Edge> pickedEdges = (MultiPickedState<IB_Edge>) SMT_GraphManager.getInstance().getVisViewer().getPickedEdgeState();
          Collection <IB_Edge> EdgeSet = SMT_GraphManager.getInstance().getEdges();
          Collection <IB_Vertex> VertexSet = SMT_GraphManager.getInstance().getVertices();
          
          // iterate through the paths, and decorate the verticies and edges
          for(RT_PathLeg leg: path.getLegs())
          {
            IB_Vertex v = IB_Vertex.getFromVertex(leg, VertexSet);
            if(v != null)
               v.Decorator.ForwardPath = true;
            v = IB_Vertex.getToVertex(leg, VertexSet);
            if(v != null)
               v.Decorator.ForwardPath = true;
             IB_Edge edge = IB_Edge.getEdge(leg,  EdgeSet);
            if(edge != null)
            {
              System.err.println(edge.toEdgeIdStringVerbose(48));
              pickedEdges.pick(edge, true);
              edge.Decorator.ForwardPath = true;
             }
          }
          for(RT_PathLeg leg: path.getReturnPath().getLegs())
          {
            IB_Vertex v = IB_Vertex.getFromVertex(leg, VertexSet);
            if(v != null)
               v.Decorator.ReturnPath = true;
            v = IB_Vertex.getToVertex(leg, VertexSet);
            if(v != null)
               v.Decorator.ReturnPath = true;
           IB_Edge edge = IB_Edge.getEdge(leg,  EdgeSet);
            if(edge != null)
            {
              System.err.println(edge.toEdgeIdStringVerbose(48));
              edge.Decorator.ReturnPath  = true;
             }
          }
        }
       }
    });
    
    mnPath.add(mntmDecorateGraphWith);
    add(Show);
    
    add(separator_1);
   
    // refer to the actionPerformed method for JMenuItems
//    Swap.addActionListener(this);
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


  /************************************************************
   * Method Name:
   *  PortCounterPopupMenu
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param label
   ***********************************************************/
  public SimpleGraphPopupMenu(String label)
  {
    this();
    this.setVertexName(label);
  }
  
  public SimpleGraphPopupMenu(RT_PathTreeModel Model, RT_Path path)
  {
    this(path.getPathIdString());
    this.Pmodel = Model;
    this.Path = path;
  }
  
  
   public SimpleGraphPopupMenu(IB_Vertex iv)
  {
    this();
    this.setVertex(iv);
  }

  public void setVertexName(String name)
  {
    VertexNameLabel.setText(name);
  }
 
  @Override
  public void actionPerformed(ActionEvent e)
  {
    
  }

}
