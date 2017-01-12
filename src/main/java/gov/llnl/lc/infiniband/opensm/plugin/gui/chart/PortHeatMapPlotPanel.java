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
 *        file: PortHeatMapPlotPanel.java
 *
 *  Created on: Mar 26, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.chart;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.EnumSet;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.event.ChartChangeEvent;
import org.jfree.chart.event.ChartChangeListener;
import org.jfree.chart.panel.CrosshairOverlay;
import org.jfree.chart.plot.Crosshair;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.general.HeatMapUtilities;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RefineryUtilities;

import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Collection;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Depth;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.time.TimeStamp;

/**********************************************************************
 * Describe purpose and responsibility of PortHeatMapPlotPanel
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Mar 26, 2015 3:02:00 PM
 **********************************************************************/
public class PortHeatMapPlotPanel extends MultiPlotPanel implements CommonLogger, ChangeListener, ChartChangeListener
{
  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = -2906701417391455581L;
  
  private PortHeatMapWorker worker;
  private PortHeatMapDataSet pHeatMap;
  
  private static final String TimeAxisLabel        = "Time";
  private static final String PortAxisLabel        = "Port";
  public static final String UtilizationAxisLabel = "% Max";

  private ChartPanel     heatPanel;  // contains the chart, overlay for crosshairs, and the paintScale
  private JFreeChart     heatChart;
  private Crosshair      TimeXhair;
  private Crosshair      PortXhair;
  private Paint          TimeSliceColor   = Color.red;
  private Paint          PortSliceColor = Color.yellow; 
  private Dimension      overallDimension;
  private boolean        initial = true;

  private JFreeChart     SingleTimeChart;
  private JFreeChart     SinglePortChart;
  private JSlider        PortSlider;
  private JSlider        TimeSlider;
  private JPanel         TimeSliderPanel;

  private HeatMapDepthPanel DepthPanel;

  public PortHeatMapPlotPanel()
  {
    this(null, IB_Depth.IBD_COMPUTE_NODES);
  }

  public PortHeatMapPlotPanel(OMS_Collection history, EnumSet<IB_Depth> includedDepths)
  {
    this(history, null, includedDepths);
  }

  public PortHeatMapPlotPanel(OMS_Collection history, ArrayList<IB_Vertex> includedNodes)
  {
    this(history, includedNodes, null);
  }

  private PortHeatMapPlotPanel(OMS_Collection history, ArrayList<IB_Vertex> includedNodes, EnumSet<IB_Depth> includedDepths)
  {
    super(new BorderLayout());
    
    // creates the main chart panel, which creates the PortHeatMapDataSet
    // (so all the data has been initialized here, available for all subsequent actions)
    overallDimension = new java.awt.Dimension(900, 500);
    heatPanel = (ChartPanel) createHeatPanel(history, includedNodes, includedDepths);
    heatPanel.setPreferredSize(overallDimension);
    
    // add the vertical and horizontal crosshairs
    CrosshairOverlay overlay = new CrosshairOverlay();
    TimeXhair = new Crosshair(0);
    TimeXhair.setPaint(TimeSliceColor);   // vertical slice (single timestamp) for dataset1, SingleTimeChart plot 1, slider 2, etc
    PortXhair = new Crosshair(0);
    PortXhair.setPaint(PortSliceColor);  // horizontal slice (single port) for dataset2, SinglePortChart, slider 1
    overlay.addDomainCrosshair(TimeXhair);
    overlay.addRangeCrosshair(PortXhair);
    heatPanel.addOverlay(overlay);
    TimeXhair.setLabelVisible(true);
    TimeXhair.setLabelAnchor(RectangleAnchor.BOTTOM_RIGHT);
    TimeXhair.setLabelBackgroundPaint(new Color(255, 255, 0, 100));
    PortXhair.setLabelVisible(true);
    PortXhair.setLabelBackgroundPaint(new Color(255, 255, 0, 100));

    add(heatPanel);

    JPanel PortSliderPanel = new JPanel(new BorderLayout());

    // all ports, single timestamp (right vertical plot) - uses TimeXhair and slider 2 from the horizontal panel (TimeSliderPanel)
    XYSeriesCollection dataset1 = new XYSeriesCollection();
    SingleTimeChart = ChartFactory.createXYLineChart("Vertical Cross-section (all ports single timestamp)", PortAxisLabel, UtilizationAxisLabel, dataset1, PlotOrientation.HORIZONTAL, false, false, false);
    XYPlot plot1 = (XYPlot) SingleTimeChart.getPlot();
    plot1.getDomainAxis().setLowerMargin(0.0);
    plot1.getDomainAxis().setUpperMargin(0.0);
    plot1.setDomainAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
    plot1.getRenderer().setSeriesPaint(0, TimeSliceColor);

    // this is the right
    ChartPanel SingleTimeChartPanel = new ChartPanel(SingleTimeChart);
    SingleTimeChartPanel.setMinimumDrawWidth(0);
    SingleTimeChartPanel.setMinimumDrawHeight(0);

    SingleTimeChartPanel.setPreferredSize(new Dimension(200, (int)(overallDimension.getWidth()/3)));
    ((XYPlot)SingleTimeChart.getPlot()).getRangeAxis().setRange(new Range(0, 1));
    ((XYPlot)SingleTimeChart.getPlot()).getDomainAxis().setRange(new Range(0, 1));

    // this slider panel holds the slider and the Single Time Snapshot of all ports, on the right or EAST (for use with plot2)
    PortSlider = new JSlider(0, 1, 0);
    PortSlider.addChangeListener(this);
    PortSlider.setOrientation(JSlider.VERTICAL);

    PortSliderPanel.add(SingleTimeChartPanel);
    PortSliderPanel.add(PortSlider, BorderLayout.WEST);

    TimeSliderPanel = new JPanel(new BorderLayout());
    
    // single port, all timestamps (lower horizontal plot) - uses PortXhair and slider 1 from the vertical panel (PortSliderPanel)
    XYSeriesCollection dataset2 = new XYSeriesCollection();
    SinglePortChart = ChartFactory.createXYLineChart("Horizontal Cross-section (single port over time)", TimeAxisLabel, UtilizationAxisLabel, dataset2, PlotOrientation.VERTICAL, false, false, false);
    XYPlot plot2 = (XYPlot) SinglePortChart.getPlot();
    plot2.getDomainAxis().setLowerMargin(0.0);
    plot2.getDomainAxis().setUpperMargin(0.0);
    plot2.getRenderer().setSeriesPaint(0, PortSliceColor);

    ChartPanel SinglePortChartPanel = new ChartPanel(SinglePortChart);
    SinglePortChartPanel.setMinimumDrawWidth(0);
    SinglePortChartPanel.setMinimumDrawHeight(0);
    
    SinglePortChartPanel.setPreferredSize(new Dimension(200, (int)(overallDimension.getHeight()/3)));
    ((XYPlot)SinglePortChart.getPlot()).getRangeAxis().setRange(new Range(0, 1));
    ((XYPlot)SinglePortChart.getPlot()).getDomainAxis().setRange(new Range(0, 1));

    DepthPanel = new HeatMapDepthPanel(null);
    DepthPanel.setPreferredSize(new Dimension((int)(overallDimension.getWidth()/4), (int)(overallDimension.getHeight()/3)));
    TimeSliderPanel.add(DepthPanel, BorderLayout.EAST);

    // this slider panel holds the slider and the Single Port for all times, in the BOTTOM (for use with plot1)
    TimeSlider = new JSlider(0, 1, 0);
    TimeSlider.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 200));
    TimeSlider.addChangeListener(this);
    
    TimeSliderPanel.add(SinglePortChartPanel);
    TimeSliderPanel.add(TimeSlider, BorderLayout.NORTH);
    add(PortSliderPanel, BorderLayout.EAST);
    add(TimeSliderPanel, BorderLayout.SOUTH);
    heatChart.setNotify(true);    
  }

  /**
   * Creates a panel for heat map (make it zoomable)
   *
   * @return A panel.
   */
  public JPanel createHeatPanel(OMS_Collection history, ArrayList<IB_Vertex> includedNodes, EnumSet<IB_Depth> includedDepths)
  {
    // this will return quickly, it uses a swing worker to
    // develop the dataset, which can take considerable time
    // so a dummy chart is returned, to be fixed later
    createHeatChart(new XYSeriesCollection(), history, includedNodes, includedDepths);
    heatChart.addChangeListener(this);
    ChartPanel panel = new ChartPanel(heatChart);
    panel.setFillZoomRectangle(true);
    panel.setMouseWheelEnabled(true);
    return panel;
  }

  public void stateChanged(ChangeEvent e)
  {
    // handles both slider events
    
    if (e.getSource() == PortSlider)
    {
      //System.err.println("Port Slider is changing " + PortSlider.getValue());
      // this is the vertical slider (moves the horizontal xhair), which moves through ports on vertical axis
      PortXhair.setValue(PortSlider.getValue());
      int xIndex = PortSlider.getValue() - PortSlider.getMinimum();
      if(pHeatMap != null)
      {
        // extract this slice from the heatmap, and display it
        XYDataset d = HeatMapUtilities.extractRowFromHeatMapDataset(pHeatMap, xIndex, "Y1");
        SinglePortChart.getXYPlot().setDataset(d);
        SinglePortChart.setTitle(pHeatMap.getPortId(xIndex) + " (port " + xIndex +")");        
      }
    }
    else if (e.getSource() == TimeSlider)
    {
      //System.err.println("Time Slider is changing " + TimeSlider.getValue());
      // this is the horizontal slider (moves vertical xhair), which moves through time on the horizontal axis
      TimeXhair.setValue(TimeSlider.getValue());
      int xIndex = TimeSlider.getValue() - TimeSlider.getMinimum();
      if(pHeatMap != null)
      {
        // extract this slice from the heatmap, and display it
        XYDataset d = HeatMapUtilities.extractColumnFromHeatMapDataset(pHeatMap, xIndex, "Y2");
        SingleTimeChart.getXYPlot().setDataset(d);
        SingleTimeChart.setTitle(new TimeStamp(pHeatMap.getXTimeValue(xIndex)).toString());        
      }
    }
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
    
    logger.info("The port heat map chart changed");

    XYPlot plot2 = (XYPlot) SinglePortChart.getPlot();
    XYPlot plot1 = (XYPlot) SingleTimeChart.getPlot();
    XYPlot plot  = (XYPlot) heatChart.getPlot();

    if (pHeatMap != null)
    {
      // X - Domain - time
      // Y - Range  - port
      // Z -        - % Util
      Range fixedXRange = new Range(0, pHeatMap.getMaximumXValue());
      Range fixedYRange = new Range(0, pHeatMap.getMaximumYValue());
      Range fixedZRange = new Range(0, pHeatMap.getMaximumZValue());
      
      // vertical, all ports, single time
      plot1.getRangeAxis().setRange(fixedZRange);
      plot1.getDomainAxis().setRange(fixedYRange);
      
      // horizontal, single port, all times
      plot2.getRangeAxis().setRange(fixedZRange);
      plot2.getDomainAxis().setRange(fixedXRange);
      
      // main chart
      if(initial)
      {
        initial = false;  // only necessary the first time
        plot.getRangeAxis().setRange(fixedYRange);
        plot.getDomainAxis().setRange(fixedXRange);
      }

      // the main chart has to be handled different, cause its zoomable
      if (!plot.getDomainAxis().getRange().equals(fixedXRange))
      {
        fixedXRange = plot.getDomainAxis().getRange();
        // single port over time, lower horizontal plot
        plot2.getDomainAxis().setRange(fixedXRange);
        // plot2.getRangeAxis().setRange(fixedZRange);
      }
      if (!plot.getRangeAxis().getRange().equals(fixedYRange))
      {
        fixedYRange = plot.getRangeAxis().getRange();
        // all ports over single time, right vertical plot
        plot1.getDomainAxis().setRange(fixedYRange);
        // plot1.getRangeAxis().setRange(fixedZRange);
      }
    }
  }
  
  /**
   * Creates a chart and the heat map dataset.
   *
   * @param dataset
   *          a dataset.
   *
   * @return A chart.
   */
  private JFreeChart createHeatChart(XYDataset dataset2, OMS_Collection history, ArrayList<IB_Vertex> includedNodes, EnumSet<IB_Depth> includedDepths)
  {
    heatChart = ChartFactory.createScatterPlot("Port Activity (heat map)", TimeAxisLabel, PortAxisLabel, dataset2, PlotOrientation.VERTICAL, true, false, false);

//    worker = new PortHeatMapWorker(this, IB_Depth.IBD_COMPUTE_NODES, false);
    worker = new PortHeatMapWorker(this, includedNodes, includedDepths, history);
//    worker = new PortHeatMapWorker(this, IB_Depth.IBD_SWITCH_NODES, false);
//    worker = new PortHeatMapWorker(this, EnumSet.range(IB_Depth.IBD_SW3, IB_Depth.IBD_SW4), false);
    worker.execute();
    
    // use the following, if you don't want to use a swingworker, but remember
    // that makes it in-line, or serial, so the stuff that would normally happen
    // in the background, and probably later, happens right now, or first
//    try
//    {
//      worker.doInBackground();
//      worker.done();
//    }
//    catch (Exception e)
//    {
//      e.printStackTrace();
//    }
    return heatChart;
  }

  /**
   * Starting point for the demonstration application.
   *
   * @param args
   *          ignored.
   */
  public static void main(String[] args)
  {
    JFrame demo = new JFrame("HeatMap");
    JPanel content = new PortHeatMapPlotPanel();
    demo.setContentPane(content);
    demo.pack();
    RefineryUtilities.centerFrameOnScreen(demo);
    demo.setVisible(true);
  }

  public JSlider getPortSlider()
  {
    return PortSlider;
  }

  public JSlider getTimeSlider()
  {
    return TimeSlider;
  }
  
  public void setHeatMapDataSet(PortHeatMapDataSet dataSet)
  {
    pHeatMap = dataSet;
  }

  public ChartPanel getHeatPanel()
  {
    return heatPanel;
  }

  public JFreeChart getHeatChart()
  {
    return heatChart;
  }

  public void replaceDepthPanel(HeatMapDepthPanel panel)
  {
    TimeSliderPanel.remove(DepthPanel);
    DepthPanel = panel;
    DepthPanel.setPreferredSize(new Dimension((int)(overallDimension.getWidth()/4), (int)(overallDimension.getHeight()/3)));
    TimeSliderPanel.add(DepthPanel, BorderLayout.EAST);
    TimeSliderPanel.validate();
    validate();
  }

}
