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
 *        file: AnimatedBarGraph.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.bargraph;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.Timer;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.Range;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_Port.PortCounterName;
import gov.llnl.lc.infiniband.opensm.plugin.gui.OMS_DataSetFactory;
import gov.llnl.lc.util.ValueRange;

/**
 * A simple demonstration application showing how to create a dynamic bar chart.
 */
/**********************************************************************
 * A dynamic bar graph.  Given a (fixed) data set, with known boundaries,
 * this object will iterate through the data and display it, creating an
 * Animated view.
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version May 30, 2013 9:06:01 AM
 **********************************************************************/
public class AnimatedBarGraph extends ApplicationFrame
{
  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = 5720412569429774051L;

  public AnimatedBarGraph(BarGraphDataSeries dataSeries, int mSecs, boolean cycle)
  {
    super(dataSeries.getGraphTitle());
    
    JPanel chartPanel = createPanel(dataSeries, mSecs, cycle);
    chartPanel.setPreferredSize(new Dimension(800, 400));
    setContentPane(chartPanel);
  }

  private static JFreeChart createChart(BarGraphDataSeries dataSeries)
  {
    if(dataSeries == null)
      return null;
    
    BarGraphDataSeries ds = dataSeries;
    
    // create the chart...
    JFreeChart chart = ChartFactory.createBarChart(
        ds.getTitle(),       // chart title
        ds.getDomainLabel(), // domain axis label
        ds.getRangeLabel(), // range axis label
        ds.getDataSet(0), // data
        PlotOrientation.VERTICAL, // orientation
        true, // include legend
        true, // tooltips?
        false // URLs?
        );

    // get a reference to the plot for further customization...
    CategoryPlot plot = (CategoryPlot) chart.getPlot();
    plot.setDomainGridlinesVisible(true);

    // set the range axis to display integers only...
    NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
    rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

    // disable bar outlines...
    BarRenderer renderer = (BarRenderer) plot.getRenderer();
    renderer.setDrawBarOutline(false);

    // set up gradient paints for series...
    GradientPaint gp0 = new GradientPaint(0.0f, 0.0f, Color.blue, 0.0f, 0.0f, new Color(0, 0, 64));
    GradientPaint gp1 = new GradientPaint(0.0f, 0.0f, Color.green, 0.0f, 0.0f, new Color(0, 64, 0));
    GradientPaint gp2 = new GradientPaint(0.0f, 0.0f, Color.red, 0.0f, 0.0f, new Color(64, 0, 0));
    GradientPaint gp3 = new GradientPaint(0.0f, 0.0f, Color.yellow, 0.0f, 0.0f, new Color(64, 0, 0));
    renderer.setSeriesPaint(0, gp0);
    renderer.setSeriesPaint(1, gp1);
    renderer.setSeriesPaint(2, gp2);
    renderer.setSeriesPaint(3, gp3);

    CategoryAxis domainAxis = plot.getDomainAxis();
    domainAxis.setCategoryLabelPositions(CategoryLabelPositions
        .createUpRotationLabelPositions(Math.PI / 6.0));
    
    ValueRange vr = ds.getRangeValueRanges().get(0);
    if(vr != null)
    {
      Range range = new Range(vr.getMin()/vr.getScale(), vr.getMax()/vr.getScale());
      rangeAxis.setRange(range);      
    }
    return chart;

  }


  /**
   * Creates a panel for the bar graph, gets the data, and sets up the animator
   * 
   * @return A panel.
   */
  public static JPanel createPanel(BarGraphDataSeries dataSeries, int mSecs, boolean cycle)
  {
     // give this dataset to the chart and the animator
    JFreeChart chart  = createChart(dataSeries);
    BG_Animator animator = new BG_Animator(dataSeries, chart, mSecs, cycle);
    animator.start();  // all done, so start the data update process
    return new ChartPanel(chart);
  }

  /**
   * Starting point for the demonstration application.
   * 
   * @param args
   *          ignored.
   */
  public static void main(String[] args)
  {
    // take three arguments
    //
    // arg 1 is the filename
    // arg 2 is the portcounter 
    // arg 3, if it exists, is the node guid (if none supplied, assume entire fabric)
    
    
//    String fileName = "/home/meier3/.smt/vrelic.hst";
    String fileName = "/home/meier3/.smt/cabHist.his";
    PortCounterName pcName = PortCounterName.xmit_data;
//  PortCounterName pcName = PortCounterName.rcv_data;
//  PortCounterName pcName = PortCounterName.symbol_err_cnt;
//  PortCounterName pcName = PortCounterName.rcv_err;
//  PortCounterName pcName = PortCounterName.link_err_recover;
//    String fileName = "/home/meier3/.smt/BigFabricDelta.cache";
//    String fileName = "/home/meier3/.smt/DayFabricDeltaCollection.cache";
    int mSecs = 1500;
    boolean cycle = true;
//    IB_Guid swGuid = new IB_Guid("66a00ec003003");  // hype
//    IB_Guid swGuid = new IB_Guid("0002:c902:0048:b718");  // vrelic vulcan leaf switch
    IB_Guid swGuid = new IB_Guid("0006:6a00:e300:4414");  // cab   0006:6a00:e300:4414
    
    
//  /home/meier3/omsRepo/vrelic/vrelic627-2.hst rcv_data 0002:c902:0048:b718
//  /home/meier3/.smt/cabHist.his rcv_data 0006:6a00:e300:4414

    if((args.length > 0) && (args[0].length() > 1))
      fileName = args[0];

//    if((args.length > 1) && (args[1].length() > 1))
//    {
//      PortCounterName pc = PortCounterName.getByName(args[1]);
//      if(pc != null)
//         pcName = pc;
//    }
//
    int numBins = 15;
    boolean excludeZero = true;
    
    BarGraphDataSeries dataSeries = null;
    if((args.length > 1) && (args[1].length() > 1))
    {
      // convert the argument to guid
      swGuid = new IB_Guid(args[1]);  // vrelic vulcan leaf switch
      // just the ports of a single switch (pc rate vs port num)
      
      ArrayList <PortCounterName> counterNames = new ArrayList <PortCounterName> ();
      counterNames.add(PortCounterName.xmit_data);
      counterNames.add(PortCounterName.rcv_data);
      counterNames.add(PortCounterName.rcv_pkts);
      counterNames.add(PortCounterName.xmit_pkts);
      dataSeries = OMS_DataSetFactory.getSwitchPortCounterSeries(fileName, swGuid, counterNames);
    }
    else
    {
      // all ports in the system (num ports vs pc rate) <= y, x
      ArrayList <PortCounterName> counterNames = new ArrayList <PortCounterName> ();
      counterNames.add(PortCounterName.xmit_data);
      counterNames.add(PortCounterName.rcv_data);
      counterNames.add(PortCounterName.rcv_pkts);
      counterNames.add(PortCounterName.xmit_pkts);
      dataSeries = OMS_DataSetFactory.getPortCounterChangeSeries(fileName, counterNames, numBins, excludeZero);
    }
    
    //set up the basic bar graph
    if((dataSeries != null) && dataSeries.isValid())
    {
      AnimatedBarGraph barGraph = new AnimatedBarGraph(dataSeries, mSecs, cycle);
      barGraph.pack();
      RefineryUtilities.centerFrameOnScreen(barGraph);
      barGraph.setVisible(true);
      
    }
    else
      System.err.println("Could not fully construct the data series for the annimated bar graph");
  }

}

/**
 * The animator.
 */
class BG_Animator extends Timer implements ActionListener
{
  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = -6040308363751986130L;
  /** The plot. */
  private DefaultCategoryDataset dataset;
  private JFreeChart chart;
  private BarGraphDataSeries dataSeries;
  
  private int timeIndex;
  
  private boolean WrapAnimation;   // cycle

  /**
   * Constructor.
   * 
   * @param dataset
   *          the dataset.
   */
  BG_Animator(BarGraphDataSeries dataSeries, JFreeChart chart, int millisecondsPerDataSet, boolean cycle)
  {
    super(millisecondsPerDataSet, null); // trigger the listener at this rate (1 per second) 
    this.chart   = chart;
    this.dataSeries = dataSeries;
    if(dataSeries != null)
      this.dataset = dataSeries.getDataSet(0);
    this.timeIndex = 0;
    this.WrapAnimation = cycle;
    addActionListener(this);
  }

  public void actionPerformed(ActionEvent event)
  {
    // assumes everything has been initialized through the constructor
    if(dataSeries == null)
    {
      // something very wrong here, remove me from the listener list
      this.removeActionListener(this);
      
      return;
    }
    int size = this.dataSeries.getNumInSeries();
    if(timeIndex >= size)
    {
      // reached the end, conditionally start over
      if(WrapAnimation)
        timeIndex = 0;
      else
        timeIndex--;
    }
    // update the title, based on the timestamp
    String ts = this.dataSeries.getSeriesLabel(timeIndex);
    String title = this.dataSeries.getTitle() + "   (" + this.dataSeries.getFabricName() + " - " + ts + ")";
    if(this.dataSeries.getTitle().equals(this.dataSeries.getFabricName()))
      title = this.dataSeries.getTitle() + "   (" + ts + ")";
    chart.setTitle(title);

    // get the next dataSet
    this.dataSeries.copyDataset(dataset, timeIndex);
    
    timeIndex++;  // increment for the next one
  }

}
