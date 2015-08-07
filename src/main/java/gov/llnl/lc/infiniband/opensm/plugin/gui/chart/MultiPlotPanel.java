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
 *        file: MultiPlotPanel.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.chart;

import java.awt.LayoutManager;
import java.util.List;

import gov.llnl.lc.logging.CommonLogger;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

/**********************************************************************
 * Describe purpose and responsibility of PortHeatMapPlotPanel
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Mar 26, 2015 3:02:00 PM
 **********************************************************************/
public class MultiPlotPanel extends ChartPanel implements CommonLogger
{
  /** this panel can hold more than one chart **/
  private List charts;
  private String FrameTitle;

  public MultiPlotPanel()
  {
    // this is the normal constructor
    super(null);
  }
    
  public MultiPlotPanel(String title)
  {
    this();
    // this is the normal constructor
    FrameTitle = title;
  }
  

  /**
   * Creates a new demo panel with the specified layout manager.
   *
   * @param layout
   *          the layout manager.
   */
  public MultiPlotPanel(LayoutManager layout)
  {
    this();
    this.setLayout(layout);
    this.charts = new java.util.ArrayList();
  }

  /**
   * Records a chart as belonging to this panel. It will subsequently be
   * returned by the getCharts() method.
   *
   * @param chart
   *          the chart.
   */
  public void addChart(JFreeChart chart)
  {
    this.charts.add(chart);
  }

  /**
   * Returns an array containing the charts within this panel.
   *
   * @return The charts.
   */
  public JFreeChart[] getCharts()
  {
    int chartCount = this.charts.size();
    JFreeChart[] charts = new JFreeChart[chartCount];
    for (int i = 0; i < chartCount; i++)
    {
      charts[i] = (JFreeChart) this.charts.get(i);
    }
    return charts;
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

}
