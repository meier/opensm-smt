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
 *        file: XY_PlotFrame.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.chart;

import javax.swing.JFrame;

import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.RefineryUtilities;

public class XY_PlotFrame extends JFrame
{
  XY_PlotPanel thePanel;
  XY_PlotType  theType;

  /**
   * A JFrame for holding an XY_PlotPanel.  There are various forms of
   * XY_PlotPanels, but essentially it contains either just a chart
   * or a table and a chart.
   * 
    *  
   */
  
  public XY_PlotFrame(String title)
  {
      super(title);
  }

  public XY_PlotFrame(XY_PlotType type)
  {
      this(type.getName());
  }

  /************************************************************
   * Method Name:
   *  getThePanel
   **/
  /**
   * Returns the value of thePanel
   *
   * @return the thePanel
   *
   ***********************************************************/
  
  public XY_PlotPanel getPlotPanel()
  {
    return thePanel;
  }

  public XY_PlotType getPlotType()
  {
    return theType;
  }

  /************************************************************
   * Method Name:
   *  PortCounterXYplot
  **/
  /**
   * A time series plot of a single port counter.  This is a multiple axis
   * plot, showing the actual value, and the delta value.  Optionally two
   * additional data sets can be displayed for correlation purposes if the
   * "includeExtra" argument is set.
   * 
   *  In the case of an Error counter, the xmit and rcv delta counts are
   *  included.
   *  
   *  In the case of a Traffic counter, the rate and % utilization values are
   *  included
   *
   * @see     XYPlot
   *
   * @param vertex        the parent node of the port
   * @param port          the parent port of the counter
   * @param portCounter   the specific counter to be plotted
   * @param includeExtra  true if extra counter values should be plotted
   ***********************************************************/
  public XY_PlotFrame(XY_PlotType type, Object userObject, Object userElement)
  {
    // type is the type of graph or plot to produce, and should contain enough info
    //
    // osm contains the data, or the initial data anyway
    //
    // userObject contains "type" specific data, like the PortCounterName, or MAD_Counter
    //
    
    if(type.isAdvanced())
      thePanel = new AdvancedXY_PlotPanel(type, userObject, userElement);
    else
      thePanel = new SimpleXY_PlotPanel(type, userObject, userElement);

    this.setTitle(thePanel.getTitle());
    this.getContentPane().add(thePanel);
    this.pack();
    RefineryUtilities.centerFrameOnScreen(this);
    this.setVisible(true);
  }
  /**
   * Starting point for the demonstration application.
   *
   * @param args  ignored.
   */
  public static void main(String[] args) 
  {
      XY_PlotFrame demo = new XY_PlotFrame("JFreeChart: PortCounterXYplot.java");
      demo.pack();
      RefineryUtilities.centerFrameOnScreen(demo);
      demo.setVisible(true);
  }

}
