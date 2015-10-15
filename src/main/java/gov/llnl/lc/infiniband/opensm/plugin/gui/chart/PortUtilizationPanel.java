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
 *        file: PortUtilizationPanel.java
 *
 *  Created on: Mar 26, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.chart;

import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Collection;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.manager.SMT_AnalysisType;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.event.ChartChangeEvent;
import org.jfree.chart.event.ChartChangeListener;
import org.jfree.ui.RefineryUtilities;

/**********************************************************************
 * A parent Panel container for the elements that show port utilization
 * information.  Normally this is intended to display some sort of graph
 * covering a collection or History of OMS snapshots.
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Mar 26, 2015 3:02:00 PM
 **********************************************************************/
public class PortUtilizationPanel extends JPanel implements CommonLogger, ChangeListener, ChartChangeListener
{
  XY_PlotPanel thePanel;
  XY_PlotType  plotType = XY_PlotType.ADV_PORT_UTIL_PLUS;
  SMT_AnalysisType analysisType = SMT_AnalysisType.SMT_UTIL_ALL_PORTS;

  private boolean        initial = true;

  public PortUtilizationPanel()
  {
    this(null);
  }

  public PortUtilizationPanel(OMS_Collection history)
  {
    super(new BorderLayout());
    
    thePanel = createUtilizationPanel(history, plotType, analysisType);
    add(thePanel);
  }

  public PortUtilizationPanel(OMS_Collection history, SMT_AnalysisType aType)
  {
    super(new BorderLayout());
    
    thePanel = createUtilizationPanel(history, plotType, aType);
    add(thePanel);
  }

  public PortUtilizationPanel(OMS_Collection history, XY_PlotType pType, SMT_AnalysisType aType)
  {
    super(new BorderLayout());
    
    thePanel = createUtilizationPanel(history, pType, aType);
    add(thePanel);
  }

  /**
   * Creates a panel for heat map (make it zoomable)
   *
   * @return A panel.
   */
  public XY_PlotPanel createUtilizationPanel(OMS_Collection history, XY_PlotType pType, SMT_AnalysisType aType)
  {
    plotType = pType;
    analysisType = aType;
    XY_PlotPanel panel =  new AdvancedXY_PlotPanel(plotType, history, aType);
    return panel;
  }

  public void stateChanged(ChangeEvent e)
  {
  }

  /**
   * See if the axis ranges have changed in the main chart and, if so, update
   * the subcharts.
   *
   * @param event
   */
  public void chartChanged(ChartChangeEvent event)
  {
    // this happens when zooming, or resetting the axis scales
    
    logger.info("The port utilization chart changed");

      // main chart
      if(initial)
      {
        initial = false;  // only necessary the first time
      }
  }
  /**
   * Starting point for the demonstration application.
   *
   * @param args
   *          ignored.
   */
  public static void main(String[] args)
  {
    JFrame demo = new JFrame("Port Utilization");
    JPanel content = new PortUtilizationPanel();
    demo.setContentPane(content);
    demo.pack();
    RefineryUtilities.centerFrameOnScreen(demo);
    demo.setVisible(true);
  }

  public XY_PlotPanel getPlotPanel()
  {
    return thePanel;
  }

  public XY_PlotType getPlotType()
  {
    return plotType;
  }

  public SMT_AnalysisType getAnalysisType()
  {
    return analysisType;
  }

 }
