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
 *        file: SimpleCollapsableGraph.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/

package gov.llnl.lc.infiniband.opensm.plugin.gui.graph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.commons.collections15.Predicate;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.visualization.DefaultVisualizationModel;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.subLayout.GraphCollapser;
import edu.uci.ics.jung.visualization.util.PredicatedParallelEdgeIndexFunction;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Node;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Edge;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphFactory;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionEvent;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;
import gov.llnl.lc.infiniband.opensm.plugin.graph.decorators.IB_TransformerFactory;
import gov.llnl.lc.smt.event.SmtMessage;
import gov.llnl.lc.smt.event.SmtMessageType;
import gov.llnl.lc.smt.manager.GraphSelectionManager;
import gov.llnl.lc.smt.manager.MessageManager;

public class SimpleCollapsableGraph extends JPanel
{
  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = -1630681531811624050L;
  
  final Set                                      Exclusions = new HashSet();
  UndirectedSparseMultigraph<IB_Vertex, IB_Edge> collapsedGraph;
  public Layout<IB_Vertex, IB_Edge>              layout;
  GraphCollapser                                 collapser;

  UndirectedSparseMultigraph<IB_Vertex, IB_Edge> graph;
  VisualizationViewer<IB_Vertex, IB_Edge>        VisViewer;

  public Set getExclusions()
  {
    return Exclusions;
  }

  public UndirectedSparseMultigraph<IB_Vertex, IB_Edge> getGraph()
  {
    return graph;
  }

  public void setGraph(UndirectedSparseMultigraph<IB_Vertex, IB_Edge> graph)
  {
    this.graph = graph;
    collapsedGraph = graph;
    collapser = new GraphCollapser(graph);

    layout = new FRLayout<IB_Vertex, IB_Edge>(graph);
  }

  public UndirectedSparseMultigraph<IB_Vertex, IB_Edge> getCollapsedGraph()
  {
    return collapsedGraph;
  }

  public void setCollapsedGraph(UndirectedSparseMultigraph<IB_Vertex, IB_Edge> collapsedGraph)
  {
    this.collapsedGraph = collapsedGraph;
  }

  public VisualizationViewer<IB_Vertex, IB_Edge> getVisViewer()
  {
    return VisViewer;
  }

  public void setVisViewer(VisualizationViewer<IB_Vertex, IB_Edge> visViewer)
  {
    VisViewer = visViewer;
  }

  public void setGraphLayout(Layout<IB_Vertex, IB_Edge> layout)
  {
    this.layout = layout;
  }

  public Layout<IB_Vertex, IB_Edge> getGraphLayout()
  {
    return this.layout;
  }

  public GraphCollapser getCollapser()
  {
    return collapser;
  }

  public void setCollapser(GraphCollapser collapser)
  {
    this.collapser = collapser;
  }

  public SimpleCollapsableGraph(UndirectedSparseMultigraph<IB_Vertex, IB_Edge> graph, OSM_Node subnetManager) throws HeadlessException
  {
    this(graph, subnetManager, null);
  }

  public SimpleCollapsableGraph(UndirectedSparseMultigraph<IB_Vertex, IB_Edge> graph, OSM_Node subnetManager, Dimension preferredSize) throws HeadlessException
  {
    super();
    setGraph(graph);

    layout = new FRLayout<IB_Vertex, IB_Edge>(graph);

    if (preferredSize == null)
      preferredSize = new Dimension(600, 600);
    else
    {
      // trim 10% off the supplied size
      preferredSize = new Dimension((preferredSize.width * 9) / 10, (preferredSize.height * 9) / 10);
    }

    final VisualizationModel<IB_Vertex, IB_Edge> visualizationModel = new DefaultVisualizationModel<IB_Vertex, IB_Edge>(layout, preferredSize);
    VisualizationViewer<IB_Vertex, IB_Edge> vv = new VisualizationViewer<IB_Vertex, IB_Edge>(visualizationModel, preferredSize);

    vv.addGraphMouseListener(new SimpleGraphMouseListener<IB_Vertex>(this));

    final PickedState<IB_Vertex> picked_v_state = vv.getPickedVertexState();
    PickedState<IB_Edge> picked_e_state = vv.getPickedEdgeState();

    picked_e_state.addItemListener(new ItemListener()
    {
      @Override
      public void itemStateChanged(ItemEvent e)
      {
        Object subject = e.getItem();

        if (subject instanceof IB_Edge)
        {
          IB_Edge edge = (IB_Edge) subject;

          if (e.getStateChange() == ItemEvent.SELECTED)
          {
            // craft a selection event, for this edge
            GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(this, subject, edge));
            System.err.println("SCG - edge selected: Color that peckerdoodle!");
          }
          else
          {
            // this is deselected, do nothing
            System.err.println("SCG - edge not selected: The edge itemEvent is: " + e.getStateChange());
          }
        }
        else
        {
          System.err.println("SCG - not edge??: The edge subject picked is: " + subject.getClass().getCanonicalName());
        }
      }
    });

    // create decorators
    vv.getRenderContext().setVertexLabelTransformer(IB_TransformerFactory.getVertexLabelTransformer(vv));
    vv.getRenderContext().setVertexShapeTransformer(IB_TransformerFactory.getVertexShapeTransformer(vv));
    vv.getRenderContext().setVertexFillPaintTransformer(IB_TransformerFactory.getVertexFillPaintTransformer(vv, subnetManager));
    vv.getRenderContext().setEdgeLabelTransformer(IB_TransformerFactory.getEdgeLabelTransformer(vv));
    vv.getRenderContext().setEdgeFillPaintTransformer(IB_TransformerFactory.getEdgeFillPaintTransformer(vv));
    vv.getRenderContext().setEdgeDrawPaintTransformer(IB_TransformerFactory.getEdgeDrawPaintTransformer(vv));

    // add a listener for ToolTips
    vv.setVertexToolTipTransformer(IB_TransformerFactory.getVertexToolTipTransformer(vv));
    vv.setEdgeToolTipTransformer(IB_TransformerFactory.getEdgeToolTipTransformer(vv));

    setVisViewer(vv);

    final PredicatedParallelEdgeIndexFunction eif = PredicatedParallelEdgeIndexFunction.getInstance();
    final Set exclusions = new HashSet();
    eif.setPredicate(new Predicate()
    {

      public boolean evaluate(Object e)
      {
        return exclusions.contains(e);
      }
    });

    vv.getRenderContext().setParallelEdgeIndexFunction(eif);

    vv.setBackground(Color.white);

    /**
     * the regular graph mouse for the normal view
     */
    final DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();

    vv.setGraphMouse(graphMouse);

    // Container content = getContentPane(); // for JApplet or JFrame
    Container content = this; // for JPanel
    content.setLayout(new BorderLayout());
    GraphZoomScrollPane gzsp = new GraphZoomScrollPane(vv);
    content.add(gzsp, BorderLayout.CENTER);
  }

  static class CollapsableGraphMouseListener<V> implements GraphMouseListener<V>
  {
    // Button0 is my upper right button
    // Button1 is left button, or selection
    // Button2 is my upper left button
    // Button3 is right button, or for popup

    public void graphClicked(V v, MouseEvent me)
    {
      // if this is a vertex and a right click, or selected, notify the
      // selection manager
      if (v instanceof IB_Vertex)
      {
        // selected??
        if (me.getButton() == MouseEvent.BUTTON1)
        {
          // generate a selection message (handled elsewhere)
          if (me.getSource() instanceof VisualizationViewer)
          {
            // VisualizationViewer vv = (VisualizationViewer) me.getSource();
            GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(this, me.getSource(), v));
          }
        }
      }
      else
      {
        // selected??
        if (me.getButton() == MouseEvent.BUTTON1)
        {
          MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INFO, "UNKNOWN (" + v.getClass().getCanonicalName() + ") was selected"));
        }
      }
    }

    public void graphPressed(V v, MouseEvent me)
    {
      // popup?
      if ((v instanceof IB_Vertex) && (me.getButton() == MouseEvent.BUTTON3))
      {
        // generate a selection message
        if (me.getSource() instanceof VisualizationViewer)
        {
          // VisualizationViewer vv = (VisualizationViewer) me.getSource();
          MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INFO, "Vertex (" + ((IB_Vertex) v).toString() + ") PopUp"));
          System.err.println("SCG Popup for vertex " + v);
        }
      }
      else if (me.getButton() == MouseEvent.BUTTON3)
      {
        MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INFO, "UNDOWND ("+ v.getClass().getCanonicalName() + ") PopUp"));
        System.err.println("SCG Popup for edge " + v);
      }
    }

    public void graphReleased(V v, MouseEvent me)
    {
      // System.err.println("IB Vertex " + v + " was released at (" + me.getX()
      // + "," + me.getY() + ")");
    }
  }

  /**
   * @param args
   */
  public static void main(String[] args) throws Exception
  {
    // dataSource will be the 2nd arg, either a filename or portnumber
    String dataSource = "10011";
    boolean fileData = false;

    if (args.length == 2)
    {
      if ("-f".compareToIgnoreCase(args[0]) == 0)
      {
        fileData = true;
      }
      dataSource = args[1];
    }
    UndirectedSparseMultigraph<IB_Vertex, IB_Edge> graph = null;
    if (fileData)
    {
      graph = IB_GraphFactory.getGraph(dataSource);
    }
    else
    {
      graph = IB_GraphFactory.getGraph("localhost", dataSource);
    }

    JFrame f = new JFrame();
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    SimpleCollapsableGraph gv = new SimpleCollapsableGraph(graph, null, null);

    f.getContentPane().add(gv);
    gv.setGraph(graph);
    f.pack();
    f.setVisible(true);
  }
}
