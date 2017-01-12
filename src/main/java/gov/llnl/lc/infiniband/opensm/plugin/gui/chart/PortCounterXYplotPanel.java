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
 *        file: PortCounterXYplotPanel.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.chart;

import java.util.LinkedHashMap;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RefineryUtilities;

import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Collection;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_FabricDelta;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_FabricDeltaCollection;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_Port.PortCounterName;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_PortChange;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_PortRate;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_PortRate.PortCounterUnits;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.data.SMT_UpdateService;
import gov.llnl.lc.smt.event.SmtMessage;
import gov.llnl.lc.smt.event.SmtMessageType;
import gov.llnl.lc.smt.manager.MessageManager;
import gov.llnl.lc.smt.manager.SMT_AnalysisManager;
import gov.llnl.lc.time.TimeStamp;

public class PortCounterXYplotPanel extends ChartPanel implements CommonLogger
{
  private IB_Vertex Vertex;
  private OSM_Port Port;
  private PortCounterName PortCounter;
  private boolean AddExtra;
  private boolean isError;
  private TimeSeries TSeries = new TimeSeries("counts");
  
  private String FrameTitle;
  
  private long     prevVal = 0;
  private TimeStamp prevTS = null;


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
   */
  
  
  public PortCounterXYplotPanel()
  {
    // this is the normal constructor
    super(null);
  }
    
  public PortCounterXYplotPanel(String title)
  {
    this();
    // this is the normal constructor
    FrameTitle = title;
  }

  /************************************************************
   * Method Name:
   *  getTSeries
   **/
  /**
   * Returns the value of tSeries
   *
   * @return the tSeries
   *
   ***********************************************************/
  
  public TimeSeries getTSeries()
  {
    return TSeries;
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
  public PortCounterXYplotPanel(IB_Vertex vertex, OSM_Port port, PortCounterName portCounter, boolean includeExtra)
  {
    // this is the normal constructor
    this(portCounter.getName() + "   [" + vertex.getName() + "   (" + port.getOSM_PortKey() + ")]");
    
    this.Vertex = vertex;
    this.Port   = port;
    this.PortCounter = portCounter;
    this.AddExtra   = includeExtra;
    isError = PortCounterName.PFM_ERROR_COUNTERS.contains(portCounter);
    
    // create the first simple plot of the absolute counter values, quick
    JFreeChart chart = createChart();
    
    if(chart == null)
    {
      // need at least two datapoints to this chart to make sense
      logger.severe("Chart unavailable, cannot create the XY Panel");
      return;
    }
    
    this.setChart(chart);
    this.setPreferredSize(new java.awt.Dimension(600, 270));
    this.setDomainZoomable(true);
    this.setRangeZoomable(true);
    
    this.setMouseWheelEnabled(true);
    
    // now fire off a worker thread to add the other axis (1 to 3 more) which
    // can take noticeable time
    
    // TODO Needs more work and testing, plots and tables seem to be missing things and colored wrong
        PortCounterPlotWorker worker1 = new PortCounterPlotWorker(chart, port, portCounter, includeExtra, isError);

      worker1.execute();
  }
  
//  private class PortCounterPlotWorkerOrig extends SwingWorker<Void, Void>
//  {
//    JFreeChart Chart = null;
//    
//    public PortCounterPlotWorkerOrig(JFreeChart chart)
//    {
//      super();
//      Chart = chart;
//     }
//
//    @Override
//    protected Void doInBackground() throws Exception
//    {
//      // 1.  counter value (already done, just add following to this)
//      // 2.  delta/period
//      //
//      //    -- if error counter --
//      // 3.  include xmit and rcv traffic deltas? (own scale)
//      //
//      //
//      //    -- if traffic counter --
//      // 3.  include rate and utilization values?
//      //
//      
//      // this is a SwingWorker thread from its pool, give it a recognizable name
//     Thread.currentThread().setName("PortCounterPlotWorkerOrig");
//      
//      logger.info( "Worker Building Plot");
//      SMT_UpdateService updateService = SMT_UpdateService.getInstance();
//      OMS_Collection history = updateService.getCollection();
//      OSM_FabricDeltaCollection deltaHistory = history.getOSM_FabricDeltaCollection();
//
//      XYPlot plot = (XYPlot) Chart.getPlot();
// 
//      // AXIS 2 - the change, or delta value of the desired counter
//      NumberAxis axis2 = new NumberAxis(PortCounterAxisLabel.DELTA.getName());
//      axis2.setFixedDimension(10.0);
//      axis2.setAutoRangeIncludesZero(false);
//      plot.setRangeAxis(1, axis2);
//      
//      XYDataset dataset2 = createDeltaDataset(deltaHistory, PortCounter, PortCounterAxisLabel.DELTA.getName());
//        plot.setDataset(1, dataset2);
//        plot.mapDatasetToRangeAxis(1, 1);
//        XYItemRenderer renderer2 = new StandardXYItemRenderer();
//        plot.setRenderer(1, renderer2);
//        
//        // the other two axis are optional, and vary depending on the
//        // type of counter
//        NumberAxis axis3 = null;
//        XYDataset dataset3 = null;
//        XYItemRenderer renderer3 = null;
//        NumberAxis axis4 = null;
//        XYDataset dataset4 = null;
//        XYItemRenderer renderer4 = null;
//
//        
//        if(AddExtra)
//        {
//          if(isError)
//          {
//            
//            // add rcv deltas
//            PortCounterName pcr = PortCounterName.rcv_data;
//            axis3 = new NumberAxis(PortCounterAxisLabel.RCV_DELTA.getName());
//            axis3.setFixedDimension(10.0);
//            axis3.setAutoRangeIncludesZero(false);
//            plot.setRangeAxis(2, axis3);
//
//            dataset3 = createDeltaDataset(deltaHistory, pcr, pcr.getName());
//            plot.setDataset(2, dataset3);
//            plot.mapDatasetToRangeAxis(2, 2);
//            renderer3 = new StandardXYItemRenderer();
//            plot.setRenderer(2, renderer3);
//            
//            // add xmit deltas
//            pcr = PortCounterName.xmit_data;
//            axis4 = new NumberAxis(PortCounterAxisLabel.XMT_DELTA.getName());
//            axis4.setFixedDimension(10.0);
//            axis4.setAutoRangeIncludesZero(false);
//            plot.setRangeAxis(3, axis4);
//
//            dataset4 = createDeltaDataset(deltaHistory, pcr, pcr.getName());
//            plot.setDataset(3, dataset4);
//            plot.mapDatasetToRangeAxis(3, 3);
//            renderer4 = new StandardXYItemRenderer();
//            plot.setRenderer(3, renderer4);
//            
//            // use a common scale for both xmit and rcv counters
//            double minRange = axis3.getLowerBound() < axis4.getLowerBound() ? axis3.getLowerBound(): axis4.getLowerBound();
//            double maxRange = axis3.getUpperBound() < axis4.getUpperBound() ? axis4.getUpperBound(): axis3.getUpperBound();
//            axis3.setAutoRange(false);
//            axis4.setAutoRange(false);
//            axis3.setRange(minRange, maxRange);
//            axis4.setRange(minRange, maxRange);
//          }
//          else
//          {
//            // add rate
//            PortCounterName pcr = PortCounter;
//            axis3 = new NumberAxis(pcr.getName() + " " + PortCounterAxisLabel.RATE.getUnits());
//            axis3.setFixedDimension(10.0);
//            axis3.setAutoRangeIncludesZero(true);
//            plot.setRangeAxis(2, axis3);
//
//            dataset3 = createRateDataset(deltaHistory, pcr, PortCounterAxisLabel.RATE.getName());
//            plot.setDataset(2, dataset3);
//            plot.mapDatasetToRangeAxis(2, 2);
//            renderer3 = new StandardXYItemRenderer();
//            plot.setRenderer(2, renderer3);
//            
//            // add utilization
//            axis4 = new NumberAxis(pcr.getName() + " " + PortCounterAxisLabel.UTILIZATION.getUnits());
//            axis4.setFixedDimension(10.0);
//   //         axis4.setAutoRangeIncludesZero(true);
//            axis4.setRange(0.0, 100.0);
//            plot.setRangeAxis(3, axis4);
//
//            dataset4 = createUtilizationDataset(deltaHistory, pcr, PortCounterAxisLabel.UTILIZATION.getName());
//            plot.setDataset(3, dataset4);
//            plot.mapDatasetToRangeAxis(3, 3);
//            renderer4 = new StandardXYItemRenderer();
//            plot.setRenderer(3, renderer4);
//          }
//        }
//        ChartUtilities.applyCurrentTheme(Chart);
//        
//        Color c1 = Color.black;
//        Color c2 = Color.blue;
//        
//        Color c3 = Color.green;
//        Color c4 = Color.magenta;
//        
//        Color ce = Color.red;
//             
//        if(isError)
//          c2 = ce;
//        
//        // change the series and axis colours after the theme has
//        // been applied...
//        plot.getRenderer().setSeriesPaint(0, c1);
//
//        renderer2.setSeriesPaint(0, c2);
//        axis2.setLabelPaint(c2);
//        axis2.setTickLabelPaint(c2);
//        
//        if(AddExtra)
//        {
//          renderer3.setSeriesPaint(0, c3);
//          axis3.setLabelPaint(c3);
//          axis3.setTickLabelPaint(c3);
//          
//          renderer4.setSeriesPaint(0, c4);
//          axis4.setLabelPaint(c4);
//          axis4.setTickLabelPaint(c4);
//        }
//
//      return null;
//    }
//    
//    @Override
//    public void done()
//    {
//      // completion notification
//      logger.info( "Worker Done Building Plot");
//      MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INFO, "Worker Done Building Plot"));
//     }
//  }

  private  JFreeChart createChart()
  {
    // 1.  counter value
    //
    // -- see worker thread for following axis --
    //
    // 2.  delta/period
    //
    //    -- if error counter --
    // 3.  include xmit and rcv traffic deltas? (own scale)
    //
    //
    //    -- if traffic counter --
    // 3.  include rate and utilization values?
    //
    
    SMT_UpdateService updateService = SMT_UpdateService.getInstance();
    OMS_Collection history = updateService.getCollection();
    
    if((history == null) || (history.getSize() < 2))
    {
      // need at least two datapoints to this chart to make sense
      logger.severe("OMS Delta unavailable, cannot createChart(), must wait for more historical snapshots");
      MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_SEVERE, "Cannot build chart without historical data"));
      return null;
    }
    
    MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INFO, "Worker Building Plot"));
    long deltaPeriod = history.getAveDeltaSeconds();

    // the primary dataset of the desired counter which will be axis1  
    XYDataset dataset1 = createDataset(history);
    
    // setup the chart for the desired counter
      JFreeChart chart = ChartFactory.createTimeSeriesChart(
          PortCounter.getName() + "   [" + Vertex.getName() + " port " + Port.getPortNumber() + "]",
          "Time of Day",
          PortCounterAxisLabel.COUNTS.getName(),
          dataset1,
          true,
          true,
          false
      );

      chart.addSubtitle(new TextTitle("Port Counter Activity (" + deltaPeriod + " sec. delta)"));
      XYPlot plot = (XYPlot) chart.getPlot();
      plot.setOrientation(PlotOrientation.VERTICAL);
      plot.setDomainPannable(true);
      plot.setRangePannable(true);
      plot.getRangeAxis().setFixedDimension(15.0);
      
      return chart;
  }

  /**
   * Creates a sample dataset.
   *
   * @param name  the dataset name.
   * @param base  the starting value.
   * @param start  the starting period.
   * @param count  the number of values to generate.
   *
   * @return The dataset.
   */
    private  XYDataset createDataset(OMS_Collection history)
    {
    // iterate through the collection, and build up a time series
    for(int j = 0; j < history.getSize(); j++)
    {
      OpenSmMonitorService osm = history.getOMS(j);
      
      // find the desired port counter, in this instance
      LinkedHashMap<String, OSM_Port> pL = osm.getFabric().getOSM_Ports();
      OSM_Port p = pL.get(OSM_Port.getOSM_PortKey(Port));
      long lValue = prevVal;
      TimeStamp ts = prevTS;
      if((p != null) && (p.pfmPort != null))
      {
        lValue = p.pfmPort.getCounter(PortCounter);
        ts = p.pfmPort.getCounterTimeStamp();
        prevVal = lValue;
        prevTS = ts;
      }
      
      RegularTimePeriod ms = new FixedMillisecond(ts.getTimeInMillis());
//      TSeries.add(ms, (double)lValue);
      TSeries.addOrUpdate(ms, (double)lValue);
    }
    TimeSeriesCollection dataset = new TimeSeriesCollection();
    dataset.addSeries(TSeries);

    return dataset;
    }

    private XYDataset createDeltaDataset(OSM_FabricDeltaCollection deltaHistory, PortCounterName pcn, String seriesName)
    {
      TimeSeries series = new TimeSeries(seriesName);

      // iterate through the collection, and build up a time series
      for (int j = 0; j < deltaHistory.getSize(); j++)
      {
        OSM_FabricDelta delta = deltaHistory.getOSM_FabricDelta(j);

        // find the desired port counter, in this instance
        LinkedHashMap<String, PFM_PortChange> pcL = delta.getPortChanges();
        PFM_PortChange pC = pcL.get(OSM_Port.getOSM_PortKey(Port));
        long lValue = pC.getDelta_port_counter(pcn);
        
        // correct for missing time periods
        int deltaSeconds = delta.getDeltaSeconds();
        long sweepPeriod = delta.getFabric2().getPerfMgrSweepSecs();
        if(sweepPeriod < deltaSeconds)
        {
          // graph is reported as counts per period, so if the period is too long, interpolate
          lValue *= sweepPeriod;
          lValue /= deltaSeconds;
         }
        TimeStamp ts = pC.getCounterTimeStamp();

        RegularTimePeriod ms = new FixedMillisecond(ts.getTimeInMillis());
        series.add(ms, (double) lValue);
      }
      TimeSeriesCollection dataset = new TimeSeriesCollection();
      dataset.addSeries(series);

      return dataset;
    }

    private XYDataset createRateDataset(OSM_FabricDeltaCollection deltaHistory, PortCounterName pcn, String seriesName)
    {
      TimeSeries series = new TimeSeries(seriesName);

      // iterate through the collection, and build up a time series
      for (int j = 0; j < deltaHistory.getSize(); j++)
      {
        OSM_FabricDelta delta = deltaHistory.getOSM_FabricDelta(j);

        // find the desired port counter, in this instance
        PFM_PortChange pC = delta.getPortChange(Port);
        long lValue = PFM_PortRate.getChangeRateLong(pC, pcn, PortCounterUnits.MEGABYTES);
        TimeStamp ts = pC.getCounterTimeStamp();

        RegularTimePeriod ms = new FixedMillisecond(ts.getTimeInMillis());
        series.add(ms, (double) lValue);
      }
      TimeSeriesCollection dataset = new TimeSeriesCollection();
      dataset.addSeries(series);

      return dataset;
    }

     private XYDataset createUtilizationDataset(OSM_FabricDeltaCollection deltaHistory, PortCounterName pcn, String seriesName)
    {
      TimeSeries series = new TimeSeries(seriesName);

      // iterate through the collection, and build up a time series
      for (int j = 0; j < deltaHistory.getSize(); j++)
      {
        OSM_FabricDelta delta = deltaHistory.getOSM_FabricDelta(j);

        // find the desired port counter, in this instance
        LinkedHashMap<String, PFM_PortChange> pcL = delta.getPortChanges();
        PFM_PortChange pC = pcL.get(OSM_Port.getOSM_PortKey(Port));
        PFM_PortRate pR = new PFM_PortRate(pC);
        TimeStamp ts = pC.getCounterTimeStamp();
        
        // convert rate to utilization
         double lValue = SMT_AnalysisManager.getInstance().getDeltaAnalysis().getPortUtilization(pR, pcn);


        RegularTimePeriod ms = new FixedMillisecond(ts.getTimeInMillis());
        series.add(ms, lValue);
      }
      TimeSeriesCollection dataset = new TimeSeriesCollection();
      dataset.addSeries(series);

      return dataset;
    }

  /**
   * Creates a panel for the XY Plot
   *
   * @return A panel.
   */
  public JPanel createPlotPanel()
  {
      JFreeChart chart = createChart();
      ChartPanel panel = new ChartPanel(chart);
      panel.setMouseWheelEnabled(true);
      return panel;
  }

  /**
   * Starting point for the demonstration application.
   *
   * @param args  ignored.
   */
  public static void main(String[] args) 
  {
    JFrame jf = new JFrame();
    
      PortCounterXYplotPanel demo = new PortCounterXYplotPanel("JFreeChart: PortCounterXYplot.java");
      jf.getContentPane().add(demo);
      jf.pack();
      RefineryUtilities.centerFrameOnScreen(jf);
      jf.setVisible(true);
  }

}
