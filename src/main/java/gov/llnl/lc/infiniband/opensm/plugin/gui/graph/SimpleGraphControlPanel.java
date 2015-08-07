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
 *        file: SimpleGraphControlPanel.java
 *
 *  Created on: Nov 26, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.graph;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout2;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.subLayout.GraphCollapser;
import gov.llnl.lc.smt.swing.SMT_FabricGraphPanel;

/**********************************************************************
 * Describe purpose and responsibility of SimpleGraphControlPanel
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Nov 26, 2013 9:18:42 AM
 **********************************************************************/
public class SimpleGraphControlPanel extends JPanel
{
  
  SimpleCollapsableGraph SimpleGraph;


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


 
  
  /************************************************************
   * Method Name:
   *  SimpleGraphControlPanel
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   ***********************************************************/
  public SimpleGraphControlPanel(SimpleCollapsableGraph simpleGraph)
  {
    super();
    
    if(simpleGraph == null)
      return;
    
    final DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();
    
    SimpleGraph = simpleGraph;
    
    VisualizationViewer vv = SimpleGraph.getVisViewer();

    vv.setGraphMouse(graphMouse);
    
    JComboBox modeBox = graphMouse.getModeComboBox();
    modeBox.addItemListener(graphMouse.getModeListener());
    graphMouse.setMode(ModalGraphMouse.Mode.PICKING);

    final ScalingControl scaler = new CrossoverScalingControl();

    JButton plus = new JButton("+");
    plus.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        VisualizationViewer vv = SimpleGraph.getVisViewer();
        scaler.scale(vv, 1.1f, vv.getCenter());
      }
    });
    JButton minus = new JButton("-");
    minus.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        VisualizationViewer vv = SimpleGraph.getVisViewer();
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
        
        VisualizationViewer vv = SimpleGraph.getVisViewer();
        Collection picked = new HashSet(vv.getPickedVertexState().getPicked());
        if (picked.size() > 1)
        {
          System.out.println("The number picked is: " + picked.size());
          Layout layout = SimpleGraph.getGraphLayout();
          GraphCollapser collapser = SimpleGraph.getCollapser();

          Graph inGraph = layout.getGraph();
          Graph clusterGraph = collapser.getClusterGraph(inGraph, picked);

          Graph g = collapser.collapse(layout.getGraph(), clusterGraph);
          if(g instanceof UndirectedSparseMultigraph)
            SimpleGraph.setCollapsedGraph((UndirectedSparseMultigraph)g);
 //         collapsedGraph = g;
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
        VisualizationViewer vv = SimpleGraph.getVisViewer();
        Collection picked = vv.getPickedVertexState().getPicked();
        if (picked.size() == 2)
        {
          Layout layout = SimpleGraph.getGraphLayout();
          Pair pair = new Pair(picked);
          Graph graph = layout.getGraph();
          Collection edges = new HashSet(graph.getIncidentEdges(pair.getFirst()));
          edges.retainAll(graph.getIncidentEdges(pair.getSecond()));
          SimpleGraph.getExclusions().addAll(edges);
          vv.repaint();
        }

      }
    });

    JButton expandEdges = new JButton("Expand Edges");
    expandEdges.addActionListener(new ActionListener()
    {

      public void actionPerformed(ActionEvent e)
      {
        VisualizationViewer vv = SimpleGraph.getVisViewer();
        Collection picked = vv.getPickedVertexState().getPicked();
        if (picked.size() == 2)
        {
          Pair pair = new Pair(picked);
          Graph graph = SimpleGraph.getGraphLayout().getGraph();
          Collection edges = new HashSet(graph.getIncidentEdges(pair.getFirst()));
          edges.retainAll(graph.getIncidentEdges(pair.getSecond()));
          SimpleGraph.getExclusions().removeAll(edges);
          vv.repaint();
        }

      }
    });

    JButton expand = new JButton("Expand");
    expand.addActionListener(new ActionListener()
    {

      public void actionPerformed(ActionEvent e)
      {
        VisualizationViewer vv = SimpleGraph.getVisViewer();
        Collection picked = new HashSet(vv.getPickedVertexState().getPicked());
        for (Object v : picked)
        {
          if (v instanceof Graph)
          {

            Graph g = SimpleGraph.getCollapser().expand(SimpleGraph.getGraphLayout().getGraph(), (Graph) v);
            vv.getRenderContext().getParallelEdgeIndexFunction().reset();
            SimpleGraph.getGraphLayout().setGraph(g);
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
        VisualizationViewer vv = SimpleGraph.getVisViewer();
        Graph g = SimpleGraph.getGraph();
        SimpleGraph.getGraphLayout().setGraph(g);
        SimpleGraph.getExclusions().clear();
        vv.repaint();
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
    jcb.addActionListener(new GraphLayoutChooser(jcb, vv, SimpleGraph));
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
    controls.add(jcb);
    add(controls, BorderLayout.SOUTH);
  }

  /************************************************************
   * Method Name:
   *  SimpleGraphControlPanel
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param isDoubleBuffered
   ***********************************************************/
  public SimpleGraphControlPanel(boolean isDoubleBuffered)
  {
    super(isDoubleBuffered);
    // TODO Auto-generated constructor stub
  }

  /************************************************************
   * Method Name:
   *  SimpleGraphControlPanel
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param layout
   * @param isDoubleBuffered
   ***********************************************************/
  public SimpleGraphControlPanel(LayoutManager layout, boolean isDoubleBuffered)
  {
    super(layout, isDoubleBuffered);
    // TODO Auto-generated constructor stub
  }

  /************************************************************
   * Method Name:
   *  SimpleGraphControlPanel
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param layout
   ***********************************************************/
  public SimpleGraphControlPanel(LayoutManager layout)
  {
    super(layout);
    // TODO Auto-generated constructor stub
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
   * @throws Exception 
   ***********************************************************/
  public static void main(String[] args) throws Exception
  {
    // just test the controls, but need a graph to interact with, so
    //
    // get the OMS, create a graph, put it in a frame, and then
    // connect the controls to ito
    
    SMT_FabricGraphPanel fg = null;
    
    String dataSource = "10011";
    boolean fileData = false;
    
    if(args.length == 2)
    {
      if("-f".compareToIgnoreCase(args[0])==0)
        fileData = true;
      // this should be either the file name (OMS) or a port number
      dataSource = args[1];
    }
    
    if(fileData)
      fg = new SMT_FabricGraphPanel(dataSource);
    else
       fg = new SMT_FabricGraphPanel("localhost", dataSource);

    JFrame f = new JFrame();
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);    
    f.getContentPane().setLayout(new BorderLayout(0, 0));    
    // conditionally attach a graph panel so I can play with it
//    f.getContentPane().add(fg, BorderLayout.CENTER);

    SimpleGraphControlPanel gcp = new SimpleGraphControlPanel(fg.getFabricGraph());
    f.getContentPane().add(gcp, BorderLayout.SOUTH);
    
    f.pack();
    f.setVisible(true);
  }

}
