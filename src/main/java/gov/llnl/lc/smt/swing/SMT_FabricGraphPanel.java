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
 *        file: SMT_FabricGraphPanel.java
 *
 *  Created on: Nov 26, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.swing;

import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Updater;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_FabricDeltaAnalyzer;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Node;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_ServiceChangeListener;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Edge;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphFactory;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;
import gov.llnl.lc.infiniband.opensm.plugin.gui.graph.SimpleCollapsableGraph;
import gov.llnl.lc.infiniband.opensm.plugin.gui.graph.SimpleGraphControlPanel;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.data.SMT_AnalysisChangeListener;
import gov.llnl.lc.smt.event.SmtMessage;
import gov.llnl.lc.smt.event.SmtMessageType;
import gov.llnl.lc.smt.manager.MessageManager;
import gov.llnl.lc.smt.manager.SMT_AnalysisManager;
import gov.llnl.lc.smt.manager.SMT_AnalysisType;
import gov.llnl.lc.smt.manager.SMT_AnalysisUpdater;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.LinkedHashMap;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

/**********************************************************************
 * Describe purpose and responsibility of SMT_FabricGraphPanel
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Nov 26, 2013 9:01:23 AM
 **********************************************************************/
public class SMT_FabricGraphPanel extends JPanel implements OSM_ServiceChangeListener,
                                                 SMT_AnalysisChangeListener, CommonLogger
{
  
  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = -7962274693346742282L;
  
  SMT_AnalysisType GraphType = SMT_AnalysisType.SMT_FABRIC_GRAPH;
  SimpleCollapsableGraph FabricGraph;
  OSM_Node SubnetManager;

  /************************************************************
   * Method Name:
   *  getGraphType
   **/
  /**
   * Returns the value of graphType
   *
   * @return the graphType
   *
   ***********************************************************/
  
  public SMT_AnalysisType getType()
  {
    return GraphType;
  }

  /************************************************************
   * Method Name:
   *  getFabricGraph
   **/
  /**
   * Returns the value of fabricGraph
   *
   * @return the fabricGraph
   *
   ***********************************************************/
  
  public SimpleCollapsableGraph getFabricGraph()
  {
    return FabricGraph;
  }



  /************************************************************
   * Method Name:
   *  SMT_FabricGraphPanel
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   ***********************************************************/
  public SMT_FabricGraphPanel()
  {
    super();
  }



  /************************************************************
   * Method Name:
   *  SMT_FabricGraphPanel
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param layout
   ***********************************************************/
  public SMT_FabricGraphPanel(UndirectedSparseMultigraph<IB_Vertex,IB_Edge> graph)
  {
    this(graph, null, null);
  }

  public SMT_FabricGraphPanel(UndirectedSparseMultigraph<IB_Vertex,IB_Edge> graph, OSM_Node mgr, Dimension preferredSize)
  {
    super();
    SubnetManager = mgr;
    FabricGraph = new SimpleCollapsableGraph(graph, mgr, preferredSize);
    this.setLayout(new BorderLayout(0, 0));
    this.add(FabricGraph, BorderLayout.CENTER);
  }

  public SMT_FabricGraphPanel(String filename) throws Exception
  {
    this(IB_GraphFactory.getGraph(filename));
  }

  public SMT_FabricGraphPanel(String hostName, String portNum)
  {
    this(IB_GraphFactory.getGraph(hostName, portNum));
  }

  public SMT_FabricGraphPanel(OSM_Fabric fabric, Dimension preferredSize)
  {
    this(IB_GraphFactory.getGraph(fabric), fabric.getManagementNode(), preferredSize);
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
    f.getContentPane().add(fg, BorderLayout.CENTER);

    // conditionally attache a control panel so I can play with it
    SimpleGraphControlPanel gcp = new SimpleGraphControlPanel(fg.getFabricGraph());
    f.getContentPane().add(gcp, BorderLayout.SOUTH);
    
    f.pack();
    f.setVisible(true);
  }


  @Override
  public void osmFabricUpdate(OSM_Fabric osmFabric) throws Exception
  {
    logger.info("The FabricGraphPanel got a Fabric update - do nothing, waiting for analysis");
  }


  @Override
  public void osmServiceUpdate(OMS_Updater updater, OpenSmMonitorService osmService) throws Exception
  {
    SubnetManager = osmService.getFabric().getManagementNode();
    logger.info("The FabricGraphPanel got an OMS update - do nothing, waiting for analysis");    
  }

  @Override
  public void smtAnalysisUpdate(SMT_AnalysisUpdater updater) throws Exception
  {
    logger.info("The FabricGraphPanel got an Analysis update, redoing graph (mostly decoration)");
    
    // at a minimum, walk the graph and decorate the vertices and edges based on traffic and errors
    if(updater instanceof SMT_AnalysisManager)
    {
      SMT_AnalysisManager sam = (SMT_AnalysisManager)updater;
      OSM_FabricDeltaAnalyzer ofa = sam.getDeltaAnalysis();
      
      // currently only update the things with changing errors, not traffic yet
      LinkedHashMap<String, IB_Vertex> vMap   = ofa.getDynamicErrorVertexMap();
      LinkedHashMap<String, IB_Edge>   eMap   = ofa.getDynamicErrorEdgeMap();
      
      if((vMap != null) && (vMap.size() > 1))        
      {
        if(true)
        {
          SMT_FabricGraphWorker worker = new SMT_FabricGraphWorker(vMap, eMap, SubnetManager);
          worker.execute();
        }
        else
        {
          // push the changed vertices and edges into the graph, and tweak the decorator
//          UndirectedSparseMultigraph<IB_Vertex,IB_Edge> g = getFabricGraph().getGraph();
          UndirectedSparseMultigraph<IB_Vertex,IB_Edge> g = getFabricGraph().getGraph();
          g = IB_GraphFactory.updateGraph(g, vMap, eMap, SubnetManager);
                 
          // force a repaint
          getFabricGraph().repaint();
          
        }
      }
      else
        logger.warning("Could not find Error Vertices (nothing to decorate?  Undecorate??");
    }
  }
  
  private class SMT_FabricGraphWorker extends SwingWorker<Void, Void>
  {
    LinkedHashMap<String, IB_Vertex> vertexMap;
    LinkedHashMap<String, IB_Edge> edgeMap;
    OSM_Node SM;


    public SMT_FabricGraphWorker(LinkedHashMap<String, IB_Vertex> vMap, LinkedHashMap<String, IB_Edge> eMap, OSM_Node mgr)
    {
      vertexMap = vMap;
      edgeMap   = eMap;
      SM        = mgr;
     }
    
    @Override
    protected Void doInBackground() throws Exception
    {
      // this is a SwingWorker thread from its pool, give it a recognizable name
      Thread.currentThread().setName("SMT_FabricGraphWorker");

      // push the changed vertices and edges into the graph, and tweak the decorator
      UndirectedSparseMultigraph<IB_Vertex,IB_Edge> g = getFabricGraph().getGraph();

      g = IB_GraphFactory.updateGraph(g, vertexMap, edgeMap, SM);
             
      return null;
    }
    @Override
    public void done()
    {
      // completion notification
      getFabricGraph().repaint();
      MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INFO, "Worker Done Updating Graph"));
     }

  }
}

  
