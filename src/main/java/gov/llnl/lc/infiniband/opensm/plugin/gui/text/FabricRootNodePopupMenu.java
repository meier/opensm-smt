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
 *        file: FabricRootNodePopupMenu.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.text;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionEvent;
import gov.llnl.lc.smt.event.SmtMessage;
import gov.llnl.lc.smt.event.SmtMessageType;
import gov.llnl.lc.smt.manager.GraphSelectionManager;
import gov.llnl.lc.smt.manager.MessageManager;
import gov.llnl.lc.smt.manager.SMT_AnalysisType;

public class FabricRootNodePopupMenu extends JPopupMenu implements ActionListener
{
  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = -5709219934608238589L;
  
  private static final String FabOverview    = "Fabric " + SMT_AnalysisType.SMT_OVERVIEW.getAnalysisName();
  private static final String FabGraph       = "Fabric " + SMT_AnalysisType.SMT_GRAPH.getAnalysisName();
  private static final String FabUtilization = "Fabric " + SMT_AnalysisType.SMT_UTILIZATION.getAnalysisName();
  private static final String FabRouting     = "Fabric " + SMT_AnalysisType.SMT_ROUTING.getAnalysisName();
  private static final String FabDetails     = "Fabric " + SMT_AnalysisType.SMT_DETAILS.getAnalysisName();

  private JCheckBoxMenuItem Overview         = new JCheckBoxMenuItem(FabOverview);
  private JCheckBoxMenuItem Graph            = new JCheckBoxMenuItem(FabGraph);
  private JCheckBoxMenuItem Utilization      = new JCheckBoxMenuItem(FabUtilization);
  private JCheckBoxMenuItem Routing          = new JCheckBoxMenuItem(FabRouting);
  private JCheckBoxMenuItem Details          = new JCheckBoxMenuItem(FabDetails);

  /************************************************************
   * Method Name:
   *  FabricRootNodePopupMenu
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   ***********************************************************/
  public FabricRootNodePopupMenu()
  {
    this("");
   }

  /************************************************************
   * Method Name:
   *  FabricRootNodePopupMenu
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param label
   ***********************************************************/
  public FabricRootNodePopupMenu(String label)
  {
    super(label);
    add(Overview);
    add(Graph);
    add(Utilization);
    add(Routing);
    add(Details);
   
    // refer to the actionPerformed method for JMenuItems
    Overview.addActionListener(this);
    Graph.addActionListener(this);
    Utilization.addActionListener(this);
    Routing.addActionListener(this);
    Details.addActionListener(this);
  }
  
  

  /************************************************************
   * Method Name:
   *  getOverview
   **/
  /**
   * Returns the value of overview
   *
   * @return the overview
   *
   ***********************************************************/
  
  public JCheckBoxMenuItem getOverview()
  {
    return Overview;
  }

  /************************************************************
   * Method Name:
   *  getGraph
   **/
  /**
   * Returns the value of graph
   *
   * @return the graph
   *
   ***********************************************************/
  
  public JCheckBoxMenuItem getGraph()
  {
    return Graph;
  }

  /************************************************************
   * Method Name:
   *  getUtilization
   **/
  /**
   * Returns the value of utilization
   *
   * @return the utilization
   *
   ***********************************************************/
  
  public JCheckBoxMenuItem getUtilization()
  {
    return Utilization;
  }
  
  /************************************************************
   * Method Name:
   *  getUtilization
   **/
  /**
   * Returns the value of utilization
   *
   * @return the utilization
   *
   ***********************************************************/
  
  public JCheckBoxMenuItem getRouting()
  {
    return Routing;
  }
  
  /************************************************************
   * Method Name:
   *  getUtilization
   **/
  /**
   * Returns the value of utilization
   *
   * @return the utilization
   *
   ***********************************************************/
  
  public JCheckBoxMenuItem getDetails()
  {
    return Details;
  }
  

  /************************************************************
   * Method Name:
   *  setOverview
   **/
  /**
   * Sets the value of overview
   *
   * @param overview the overview to set
   *
   ***********************************************************/
  public void setOverviewSelected(boolean selected)
  {
    Overview.setSelected(selected);
  }

  /************************************************************
   * Method Name:
   *  setGraph
   **/
  /**
   * Sets the value of graph
   *
   * @param graph the graph to set
   *
   ***********************************************************/
  public void setGraphSelected(boolean selected)
  {
    Graph.setSelected(selected);
  }

  /************************************************************
   * Method Name:
   *  setUtilization
   **/
  /**
   * Sets the value of utilization
   *
   * @param utilization the utilization to set
   *
   ***********************************************************/
  public void setUtilizationSelected(boolean selected)
  {
    Utilization.setSelected(selected);
  }
  
  /************************************************************
   * Method Name:
   *  setUtilization
   **/
  /**
   * Sets the value of utilization
   *
   * @param utilization the utilization to set
   *
   ***********************************************************/
  public void setRoutingSelected(boolean selected)
  {
    Routing.setSelected(selected);
  }
  
  /************************************************************
   * Method Name:
   *  setUtilization
   **/
  /**
   * Sets the value of utilization
   *
   * @param utilization the utilization to set
   *
   ***********************************************************/
  public void setDetailsSelected(boolean selected)
  {
    Details.setSelected(selected);
  }
  
  

  @Override
  public void actionPerformed(ActionEvent e)
  {
    // synchronize this checkbox event with the others, so they all show the same thing
    
    // this handles right mouse clicks from menu items
    if (e.getActionCommand().equals(FabUtilization))
    {
      if (e.getSource() instanceof JMenuItem)
      {
        JMenuItem src = (JMenuItem) e.getSource();
        Container cont = src.getParent();
        
        MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INFO,"Fabric Utilization"));
        if(cont instanceof FabricRootNodePopupMenu)
        {
          // this came from me, so I can handle it
         //  craft a selection event that contains necessary info for this utilizaiton event
          GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(this, SMT_AnalysisType.SMT_FABRIC_UTILIZATION, new Boolean(src.isSelected())));
        }
      }
    }
    
    if (e.getActionCommand().equals(FabOverview))
    {
      if (e.getSource() instanceof JMenuItem)
      {
        JMenuItem src = (JMenuItem) e.getSource();
        Container cont = src.getParent();

        MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INFO, "Fabric Overview"));
        if(cont instanceof FabricRootNodePopupMenu)
        {
          // this came from me, so I can handle it
         //  craft a selection event that contains necessary info for this utilizaiton event
          GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(this, SMT_AnalysisType.SMT_FABRIC_OVERVIEW, new Boolean(src.isSelected())));
        }
      }
    }

    if (e.getActionCommand().equals(FabGraph))
    {
      if (e.getSource() instanceof JMenuItem)
      {
        JMenuItem src = (JMenuItem) e.getSource();
        Container cont = src.getParent();

        MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INFO, "Fabric Graph"));
        if(cont instanceof FabricRootNodePopupMenu)
        {
          // this came from me, so I can handle it
         //  craft a selection event that contains necessary info for this utilizaiton event
          GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(this, SMT_AnalysisType.SMT_FABRIC_GRAPH, new Boolean(src.isSelected())));
        }
      }
    }

    if (e.getActionCommand().equals(FabRouting))
    {
      if (e.getSource() instanceof JMenuItem)
      {
        JMenuItem src = (JMenuItem) e.getSource();
        Container cont = src.getParent();

        MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INFO, "Fabric Routing"));
        if(cont instanceof FabricRootNodePopupMenu)
        {
          // this came from me, so I can handle it
         //  craft a selection event that contains necessary info for this routing event
          GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(this, SMT_AnalysisType.SMT_FABRIC_ROUTING, new Boolean(src.isSelected())));
        }
      }
    }

    if (e.getActionCommand().equals(FabDetails))
    {
      if (e.getSource() instanceof JMenuItem)
      {
        JMenuItem src = (JMenuItem) e.getSource();
        Container cont = src.getParent();

        MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INFO, "Fabric Details"));
        if(cont instanceof FabricRootNodePopupMenu)
        {
          // this came from me, so I can handle it
         //  craft a selection event that contains necessary info for this utilizaiton event
          GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(this, SMT_AnalysisType.SMT_FABRIC_DETAILS, new Boolean(src.isSelected())));
        }
      }
    }

  }
}
