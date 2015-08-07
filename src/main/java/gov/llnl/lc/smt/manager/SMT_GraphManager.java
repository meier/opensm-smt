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
 *        file: GraphSelectionManager.java
 *
 *  Created on: Aug 21, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.manager;

import edu.uci.ics.jung.visualization.VisualizationViewer;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Edge;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionEvent;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;
import gov.llnl.lc.infiniband.opensm.plugin.gui.graph.SimpleCollapsableGraph;
import gov.llnl.lc.infiniband.opensm.plugin.gui.graph.SimpleGraphControlPanel;
import gov.llnl.lc.infiniband.opensm.plugin.gui.graph.SimpleGraphPopupMenu;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.swing.SMT_FabricGraphPanel;

import java.util.Collection;

/**********************************************************************
 * The SMT_GraphManager is a singleton (and therefore global) object that
 * manages the SMT_FabricGraph.  Normally, there is zero or one fabric
 * graphs at any one time.  Typically the graph communicates between other
 * components through the GraphSelection mechanism.  This object handles
 * some communication that doesn't fit the "selection event" model, but
 * is mostly responsible for creation and destruction of the graph itself.
 * <p>
 * @see  GraphSelectionManager
 * @see  IB_GraphSelectionEvent
 *
 * @author meier3
 * 
 * @version Jun 4, 2014 11:19:24 AM
 **********************************************************************/
public class SMT_GraphManager implements CommonLogger
{
  /** the one and only <code>SMT_GraphManager</code> Singleton **/
  private volatile static SMT_GraphManager gGraphMgr   = null;

  /** the synchronization object **/
  private static Boolean                   semaphore   = new Boolean(true);

  /** logger for the class **/
  private final java.util.logging.Logger   classLogger = java.util.logging.Logger.getLogger(getClass().getName());

  private static SimpleGraphPopupMenu      graphPopup  = new SimpleGraphPopupMenu();
  
  private static SMT_FabricGraphPanel  graphPanel;
  
  private static SimpleGraphControlPanel graphControlPanel;

  /************************************************************
   * Method Name: SMT_RouteManager
   **/
  /**
   * Describe the constructor here
   * 
   * @see describe related java objects
   * 
   ***********************************************************/
  private SMT_GraphManager()
  {
    super();
    initManager();
  }

  /**************************************************************************
   *** Method Name: getInstance
   **/
  /**
   *** Get the singleton SmtConsoleManager. This can be used if the application
   * wants to share one manager across the whole JVM. Currently I am not sure
   * how this ought to be used.
   *** <p>
   *** 
   *** @return the GLOBAL (or shared) SmtConsoleManager
   **************************************************************************/

  public static SMT_GraphManager getInstance()
  {
    synchronized (SMT_GraphManager.semaphore)
    {
      if (gGraphMgr == null)
      {
        gGraphMgr = new SMT_GraphManager();
      }
      return gGraphMgr;
    }
  }

  /*-----------------------------------------------------------------------*/

  public Object clone() throws CloneNotSupportedException
  {
    throw new CloneNotSupportedException();
  }

  protected boolean initManager()
  {
    // need to obtain a graph
    
    // restore persistent things here (like graph attributes, and such)

    return true;
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
  public static void main(String[] args)
  {
    // TODO Auto-generated method stub

  }

  public String getName()
  {
    return this.getClass().getSimpleName();
  }

  public SimpleGraphPopupMenu getSimpleGraphPopupMenu(IB_Vertex iv)
  {
    graphPopup.setVertex(iv);
    return graphPopup;
  }

  public void removeGraphPanel(SMT_FabricGraphPanel fgp)
  {
    if((fgp != null) && (graphPanel != null) && (fgp == graphPanel))
    {
      graphPanel = null;
      graphControlPanel = null;  // this cannot exist without a graph to control
    }
   }

  public void setGraphPanel(SMT_FabricGraphPanel fgp)
  {
    graphPanel = fgp;
    
  }

  public SMT_FabricGraphPanel getGraphPanel()
  {
    return graphPanel;
  }
  
  /* convenience methods for getting common objects from the graph */
  
  public SimpleCollapsableGraph getFabricGraph()
  {
    return getGraphPanel().getFabricGraph();
  }

  public VisualizationViewer<IB_Vertex,IB_Edge> getVisViewer()
  {
    return getFabricGraph().getVisViewer();
  }

  public void setGraphControlPanel(SimpleGraphControlPanel gcp)
  {
    graphControlPanel = gcp;
  }

  public SimpleGraphControlPanel getGraphControlPanel()
  {
    return graphControlPanel;
  }

  public void refreshGraph()
  {
    getFabricGraph().repaint();
    graphPanel.repaint();
  }

  public Collection<IB_Edge> getEdges()
  {
    return getFabricGraph().getCollapsedGraph().getEdges();
  }

  public Collection<IB_Vertex> getVertices()
  {
    return getFabricGraph().getCollapsedGraph().getVertices();
  }

}
