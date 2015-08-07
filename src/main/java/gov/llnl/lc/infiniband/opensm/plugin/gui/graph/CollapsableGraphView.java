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
 *        file: CollapsableGraphView.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.graph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.commons.collections15.Predicate;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout2;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.DefaultVisualizationModel;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.subLayout.GraphCollapser;
import edu.uci.ics.jung.visualization.util.PredicatedParallelEdgeIndexFunction;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphFactory;
import gov.llnl.lc.infiniband.opensm.plugin.graph.decorators.ClusterVertexShapeTransformer;
import gov.llnl.lc.infiniband.opensm.plugin.graph.decorators.IB_TransformerFactory;

/**
 * @author meier3
 * 
 * 
 * @deprecated
 * @see SimpleCollapsableGraph
 *
 */
public class CollapsableGraphView extends JApplet 
{
  String              Instructions = "<html>Use the mouse to select multiple vertices"
      + "<p>either by dragging a region, or by shift-clicking"
      + "<p>on multiple vertices."
      + "<p>After you select vertices, use the Collapse button"
      + "<p>to combine them into a single vertex."
      + "<p>Select a 'collapsed' vertex and use the Expand button"
      + "<p>to restore the collapsed vertices."
      + "<p>The Reset button will restore the original graph."
      + "<p>If you select 2 (and only 2) vertices, then press"
      + "<p>the Compress Edges button, parallel edges between"
      + "<p>those two vertices will no longer be expanded."
      + "<p>If you select 2 (and only 2) vertices, then press"
      + "<p>the Expand Edges button, parallel edges between"
      + "<p>those two vertices will be expanded."
      + "<p>You can drag the vertices with the mouse."
      + "<p>Use the 'Picking'/'Transforming' combo-box to switch"
      + "<p>between picking and transforming mode.</html>";
  
  final Set Exclusions = new HashSet();

  public String getInstructions()
  {
    return Instructions;
  }


  public Set getExclusions()
  {
    return Exclusions;
  }


  /**
   * the graph
   */
  Graph               graph;
  public Graph getGraph()
  {
    return graph;
  }


  public void setGraph(Graph graph)
  {
    this.graph = graph;
    collapsedGraph = graph;
    collapser = new GraphCollapser(graph);

    layout = new FRLayout(graph);

  }


  Graph               collapsedGraph;

  public Layout              layout;

  GraphCollapser      collapser;
  
  /**
   * the visual component and renderer for the graph
   */
  VisualizationViewer VisViewer;

  public Graph getCollapsedGraph()
  {
    return collapsedGraph;
  }


  public void setCollapsedGraph(Graph collapsedGraph)
  {
    this.collapsedGraph = collapsedGraph;
  }


  public VisualizationViewer getVisViewer()
  {
    return VisViewer;
  }


  public void setVisViewer(VisualizationViewer visViewer)
  {
    VisViewer = visViewer;
  }



  public void setLayout(Layout layout)
  {
    this.layout = layout;
  }


  public GraphCollapser getCollapser()
  {
    return collapser;
  }


  public void setCollapser(GraphCollapser collapser)
  {
    this.collapser = collapser;
  }

  @SuppressWarnings("unchecked")
  private Class<? extends Layout>[] getCombos()
  {
    List<Class<? extends Layout>> layouts = new ArrayList<Class<? extends Layout>>();
    layouts.add(KKLayout.class);
    layouts.add(FRLayout.class);
    layouts.add(CircleLayout.class);
    layouts.add(SpringLayout.class);
    layouts.add(SpringLayout2.class);
    layouts.add(ISOMLayout.class);
    return layouts.toArray(new Class[0]);
  }


	public CollapsableGraphView(Graph graph, boolean val) throws HeadlessException
  {
    super();
    setGraph(graph);

    layout = new FRLayout(graph);
    Dimension preferredSize = new Dimension(400, 400);
    final VisualizationModel visualizationModel = new DefaultVisualizationModel(layout, preferredSize);
    VisualizationViewer vv = new VisualizationViewer(visualizationModel, preferredSize);

    vv.addGraphMouseListener(new CollapsableGraphMouseListener<Number>());

    vv.getRenderContext().setVertexShapeTransformer(new ClusterVertexShapeTransformer());

    PickedState<Integer> picked_state = vv.getPickedVertexState();

    // create decorators
    vv.getRenderContext().setVertexFillPaintTransformer(IB_TransformerFactory.getDefaultPaintTransformer(vv));
    
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
    
    // add a listener for ToolTips

    vv.setVertexToolTipTransformer(new ToStringLabeller()
    {

      /*
       * (non-Javadoc)
       * 
       * @see edu.uci.ics.jung.visualization.decorators.DefaultToolTipFunction#
       * getToolTipText(java.lang.Object)
       */
      @Override
      public String transform(Object v)
      {
        if (v instanceof Graph)
        {
          return ((Graph) v).getVertices().toString();
        }
        return super.transform(v);
      }
    });

    /**
     * the regular graph mouse for the normal view
     */
    final DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();

    vv.setGraphMouse(graphMouse);

    Container content = getContentPane();
    GraphZoomScrollPane gzsp = new GraphZoomScrollPane(vv);
    content.add(gzsp);

    JComboBox modeBox = graphMouse.getModeComboBox();
    modeBox.addItemListener(graphMouse.getModeListener());
    graphMouse.setMode(ModalGraphMouse.Mode.PICKING);

    final ScalingControl scaler = new CrossoverScalingControl();

    JButton plus = new JButton("+");
    plus.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        VisualizationViewer vv = getVisViewer();
        scaler.scale(vv, 1.1f, vv.getCenter());
      }
    });
    JButton minus = new JButton("-");
    minus.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        VisualizationViewer vv = getVisViewer();
        scaler.scale(vv, 1 / 1.1f, vv.getCenter());
      }
    });

    JButton collapse = new JButton("Collapse");
    collapse.addActionListener(new ActionListener()
    {

      public void actionPerformed(ActionEvent e)
      {
        System.out.println("Collapsing the graph");
        
        // Pick all port zeros, and their IMMEDIATE links
//        PickManager.getInstance().pickAllSwitches(vv);
        
        VisualizationViewer vv = getVisViewer();
        Collection picked = new HashSet(vv.getPickedVertexState().getPicked());
        if (picked.size() > 1)
        {
          System.out.println("The number picked is: " + picked.size());
          Graph inGraph = layout.getGraph();
          Graph clusterGraph = collapser.getClusterGraph(inGraph, picked);

          Graph g = collapser.collapse(layout.getGraph(), clusterGraph);
          collapsedGraph = g;
          double sumx = 0;
          double sumy = 0;
          for (Object v : picked)
          {
            Point2D p = (Point2D) layout.transform(v);
            sumx += p.getX();
            sumy += p.getY();
          }
          Point2D cp = new Point2D.Double(sumx / picked.size(), sumy / picked.size());
          vv.getRenderContext().getParallelEdgeIndexFunction().reset();
          layout.setGraph(g);
          layout.setLocation(clusterGraph, cp);
          vv.getPickedVertexState().clear();
          vv.repaint();
        }

        // Collection picked = new
        // HashSet(vv.getPickedVertexState().getPicked());
        // if (picked.size() > 1)
        // {
        // Graph inGraph = layout.getGraph();
        // Graph clusterGraph = collapser.getClusterGraph(inGraph, picked);
        //
        // Graph g = collapser.collapse(layout.getGraph(), clusterGraph);
        // collapsedGraph = g;
        // double sumx = 0;
        // double sumy = 0;
        // for (Object v : picked)
        // {
        // Point2D p = (Point2D) layout.transform(v);
        // sumx += p.getX();
        // sumy += p.getY();
        // }
        // Point2D cp = new Point2D.Double(sumx / picked.size(), sumy /
        // picked.size());
        // vv.getRenderContext().getParallelEdgeIndexFunction().reset();
        // layout.setGraph(g);
        // layout.setLocation(clusterGraph, cp);
        // vv.getPickedVertexState().clear();
        // vv.repaint();
        // }

      }

    });

    JButton compressEdges = new JButton("Compress Edges");
    compressEdges.addActionListener(new ActionListener()
    {

      public void actionPerformed(ActionEvent e)
      {
        VisualizationViewer vv = getVisViewer();
        Collection picked = vv.getPickedVertexState().getPicked();
        if (picked.size() == 2)
        {
          Pair pair = new Pair(picked);
          Graph graph = layout.getGraph();
          Collection edges = new HashSet(graph.getIncidentEdges(pair.getFirst()));
          edges.retainAll(graph.getIncidentEdges(pair.getSecond()));
          getExclusions().addAll(edges);
          vv.repaint();
        }

      }
    });

    JButton expandEdges = new JButton("Expand Edges");
    expandEdges.addActionListener(new ActionListener()
    {

      public void actionPerformed(ActionEvent e)
      {
        VisualizationViewer vv = getVisViewer();
        Collection picked = vv.getPickedVertexState().getPicked();
        if (picked.size() == 2)
        {
          Pair pair = new Pair(picked);
          Graph graph = layout.getGraph();
          Collection edges = new HashSet(graph.getIncidentEdges(pair.getFirst()));
          edges.retainAll(graph.getIncidentEdges(pair.getSecond()));
          getExclusions().removeAll(edges);
          vv.repaint();
        }

      }
    });

    JButton expand = new JButton("Expand");
    expand.addActionListener(new ActionListener()
    {

      public void actionPerformed(ActionEvent e)
      {
        VisualizationViewer vv = getVisViewer();
        Collection picked = new HashSet(vv.getPickedVertexState().getPicked());
        for (Object v : picked)
        {
          if (v instanceof Graph)
          {

            Graph g = collapser.expand(layout.getGraph(), (Graph) v);
            vv.getRenderContext().getParallelEdgeIndexFunction().reset();
            layout.setGraph(g);
          }
          vv.getPickedVertexState().clear();
          vv.repaint();
        }
      }
    });

    JButton reset = new JButton("Reset");
    reset.addActionListener(new ActionListener()
    {

      public void actionPerformed(ActionEvent e)
      {
        VisualizationViewer vv = getVisViewer();
        Graph g = getGraph();
        layout.setGraph(g);
        getExclusions().clear();
        vv.repaint();
      }
    });

    JButton help = new JButton("Help");
    help.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        JOptionPane.showMessageDialog((JComponent) e.getSource(), getInstructions(), "Help",
            JOptionPane.PLAIN_MESSAGE);
      }
    });
    Class[] combos = getCombos();
    final JComboBox jcb = new JComboBox(combos);
    // use a renderer to shorten the layout name presentation
    jcb.setRenderer(new DefaultListCellRenderer()
    {
      public Component getListCellRendererComponent(JList list, Object value, int index,
          boolean isSelected, boolean cellHasFocus)
      {
        String valueString = value.toString();
        valueString = valueString.substring(valueString.lastIndexOf('.') + 1);
        return super.getListCellRendererComponent(list, valueString, index, isSelected,
            cellHasFocus);
      }
    });
    jcb.addActionListener(new LayoutChooser(jcb, vv, this));
    jcb.setSelectedItem(FRLayout.class);

    JPanel controls = new JPanel();
    JPanel zoomControls = new JPanel(new GridLayout(2, 1));
    zoomControls.setBorder(BorderFactory.createTitledBorder("Zoom"));
    zoomControls.add(plus);
    zoomControls.add(minus);
    controls.add(zoomControls);
    JPanel collapseControls = new JPanel(new GridLayout(3, 1));
    collapseControls.setBorder(BorderFactory.createTitledBorder("Picked"));
    collapseControls.add(collapse);
    collapseControls.add(expand);
    collapseControls.add(compressEdges);
    collapseControls.add(expandEdges);
    collapseControls.add(reset);
    controls.add(collapseControls);
    controls.add(modeBox);
    controls.add(help);
    controls.add(jcb);
    content.add(controls, BorderLayout.SOUTH);
  }

  static class CollapsableGraphMouseListener<V> implements GraphMouseListener<V>
  {

    public void graphClicked(V v, MouseEvent me)
    {
      System.err.println("Vertex " + v + " was clicked at (" + me.getX() + "," + me.getY() + ")");
      
    }

    public void graphPressed(V v, MouseEvent me)
    {
      System.err.println("Vertex " + v + " was pressed at (" + me.getX() + "," + me.getY() + ")");
    }

    public void graphReleased(V v, MouseEvent me)
    {
      System.err.println("Vertex " + v + " was released at (" + me.getX() + "," + me.getY() + ")");
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
	  
    if(args.length == 2)
    {
      if("-f".compareToIgnoreCase(args[0])==0)
      {
        fileData = true;
      }
      dataSource = args[1];
    }
    Graph graph = null;      
    if(fileData)
    {
      graph = IB_GraphFactory.getGraph(dataSource);
    }
    else
    {
      graph = IB_GraphFactory.getGraph("localhost", dataSource);
     }
    
    
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    CollapsableGraphView gv = new CollapsableGraphView(graph, true);

    f.getContentPane().add(gv);

    gv.setGraph(graph);
    
    f.pack();
    f.setVisible(true);
	}

}
