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
 *        file: SimpleXY_ChartPanel.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.chart;

import gov.llnl.lc.infiniband.opensm.plugin.data.MAD_Counter;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Collection;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_FabricDelta;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_FabricDeltaAnalyzer;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_FabricDeltaCollection;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Node;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_NodeType;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Stats;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_Port.PortCounterName;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_PortChange;
import gov.llnl.lc.infiniband.opensm.plugin.event.OSM_EventStats;
import gov.llnl.lc.infiniband.opensm.plugin.event.OsmEvent;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.command.fabric.SmtFabricStructure;
import gov.llnl.lc.smt.data.SMT_UpdateService;
import gov.llnl.lc.smt.event.SmtMessage;
import gov.llnl.lc.smt.event.SmtMessageType;
import gov.llnl.lc.smt.manager.MessageManager;
import gov.llnl.lc.smt.manager.SMT_AnalysisType;
import gov.llnl.lc.time.TimeStamp;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.swing.SwingWorker;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.Range;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

public class SimpleXY_ChartPanel extends ChartPanel implements CommonLogger
{
  private TimeSeries TSeries = new TimeSeries("counts");  
  private String FrameTitle;
  private String ChartTitle;
  private OSM_Port Port = null;
  private String NodeName = "unknown";
  private PortCounterName PortCounter = null;
  private OSM_Stats MAD_Stats = null;
  private MAD_Counter MADCounter = null;
  private OSM_EventStats EventStats = null;
  private OsmEvent EventType = null;
  
  private OMS_Collection History;
  private SMT_AnalysisType AnalysisType;
  private boolean Utilization = false;

  public SimpleXY_ChartPanel(JFreeChart chart)
  {
    super(chart);
    // TODO Auto-generated constructor stub
  }

  public SimpleXY_ChartPanel()
  {
    super(null);
    // TODO Auto-generated constructor stub
  }
  
  
  public SimpleXY_ChartPanel(XY_PlotType type, Object userObject, Object userElement)
  {
    // type is the type of graph or plot to produce, and should contain enough info
    //
    // osm contains the data, or the initial data anyway
    //
    // userObject contains "type" specific data, like the Port or the OSM_Stats
    // userElement contains "type" specific data, like the PortCounterName, or MAD_Counter
    //
    this();
    
    // this is the normal constructor
    FrameTitle = "unknown frame title";
    ChartTitle = "unknown chart title";
    
    // resolve the user objects for later use
    if(userObject instanceof OSM_Port)
      Port = (OSM_Port)userObject;
    if(userElement instanceof PortCounterName)
      PortCounter = (PortCounterName)userElement;
      
    if(userObject instanceof OSM_Stats)
      MAD_Stats = (OSM_Stats)userObject;
    if(userElement instanceof MAD_Counter)
      MADCounter = (MAD_Counter)userElement;
    
    if(userObject instanceof OSM_EventStats)
      EventStats = (OSM_EventStats)userObject;
    if(userElement instanceof OsmEvent)
      EventType = (OsmEvent)userElement;
    
    
    // is this plot one of the port utilization plots
    if(userObject instanceof OMS_Collection)
      History = (OMS_Collection)userObject;
    if(userElement instanceof SMT_AnalysisType)
      AnalysisType = (SMT_AnalysisType)userElement;
    Utilization = type.isUtilizationType();
    
    // create the first simple plot of the absolute counter values, quick
    JFreeChart chart = createChart(type, userObject, userElement);
    
    if(chart == null)
    {
      // need at least two datapoints to this chart to make sense
      logger.severe("Chart unavailable, cannot create the chart within SimpleXY_ChartPanel");
      return;
    }
    
    this.setChart(chart);
    this.setPreferredSize(new java.awt.Dimension(600, 270));
    this.setDomainZoomable(true);
    this.setRangeZoomable(true);
    
    this.setMouseWheelEnabled(true);
    
    // now fire off a worker thread to add the other axis (1 to 3 more) which
    // can take noticeable time
    
    SimplePlotWorker worker = new SimplePlotWorker(chart);
    worker.execute();
  }
  
  
  public SMT_AnalysisType getAnalysisType()
  {
    return AnalysisType;
  }
  
  public boolean isUtilizationType()
  {
    return Utilization;
  }
  
  public boolean isEventType()
  {
    // assume already determined
    return EventType != null;
  }
  
  private boolean isCompare()
  {
    return false;
  }
  
 private boolean isErrorType()
 {
   return false;
 }
  
  /************************************************************
   * Method Name:
   *  getFrameTitle
   **/
  /**
   * Returns the value of frameTitle
   *
   * @return the frameTitle
   *
   ***********************************************************/
  
 public String getFrameTitle()
 {
   return FrameTitle;
 }

 public String getChartTitle()
 {
   return ChartTitle;
 }

  private JFreeChart createChart(XY_PlotType type, Object userObject, Object userElement)
  {
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
    XYDataset dataset1 = createDataset(history, userObject, userElement);
    
    // most axis are in counts (and delta counts) but utilization is in percentages
    String axisLabel = isUtilizationType() ? PortCounterAxisLabel.UTIL_AVE.getName(): PortCounterAxisLabel.COUNTS.getName();
    
    // setup the chart for the desired counter
    JFreeChart chart = ChartFactory.createTimeSeriesChart(
          getChartTitle(),
          "Time of Day",
          axisLabel,
          dataset1,
          true,
          true,
          false
      );
    
    if(isUtilizationType())
    {
      chart.addSubtitle(new TextTitle(AnalysisType.getAnalysisName() + " (" + deltaPeriod + " sec. delta)"));
//      chart.addSubtitle(new TextTitle("number of ports"));      
    }
    else
      chart.addSubtitle(new TextTitle(type.getName() + " Activity (" + deltaPeriod + " sec. delta)"));

      XYPlot plot = (XYPlot) chart.getPlot();
      plot.setOrientation(PlotOrientation.VERTICAL);
      plot.setDomainPannable(true);
      plot.setRangePannable(true);
      plot.getRangeAxis().setFixedDimension(15.0);
      
      return chart;
  }
  
  private  boolean initTitles(OpenSmMonitorService osm)
  {
    if(Port != null)
    {
      NodeName = "I still don't know";
      if(osm != null)
      {
        String nodeKey = OSM_Fabric.getOSM_NodeKey(Port);
        OSM_Node n = osm.getFabric().getOSM_Node(nodeKey);
        NodeName = n.pfmNode.getNode_name();
      }
      ChartTitle = PortCounter.getName() + "   [" + NodeName + " port " + Port.getPortNumber() + "]";
      FrameTitle = PortCounter.getName() + "   [" + NodeName + "   (" + Port.getOSM_PortKey() + ")]";
     }
    
    if(MADCounter != null)
    {
      ChartTitle = MADCounter.getName() + " [" + osm.getFabricName() + "]";
      FrameTitle = ChartTitle;
    }
    
    String axisLabel = isUtilizationType() ? PortCounterAxisLabel.UTIL_AVE.getName(): PortCounterAxisLabel.COUNTS.getName();
    TSeries.setKey(axisLabel);
    
    if(isUtilizationType())
    {
      ChartTitle = "Port Bandwidth Utilization" + " [" + osm.getFabricName() + "]";
      FrameTitle = ChartTitle;
    }
    
    if(EventType != null)
    {
      ChartTitle = EventType.getEventName() + " [" + osm.getFabricName() + "]";
      FrameTitle = ChartTitle;
    }
    

    return true;
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
    private  XYDataset createDataset(OMS_Collection history, Object userObject, Object userElement)
    {
      // need to set the Frame and Chart titles here
      initTitles(history.getOMS(0));
      
      // the dataset is a timeseries collection
      double dValue = 0.0;
      long lValue = 0;
      TimeStamp ts = null;
      RegularTimePeriod ms = null;
      
      // iterate through the deltas (if rate based), or OMS's if counter based
      if(isUtilizationType())
      {
        // what type of analysis are we doing; SW-SW, ALL, or SW-CA??
        OSM_NodeType aType = OSM_NodeType.UNKNOWN;
        
        // create junk data, so the plot will appear quickly, while the worker works...

        OSM_FabricDeltaCollection deltaHistory = history.getOSM_FabricDeltaCollection();
        for(int j = 0; j < deltaHistory.getSize(); j++)
        {
          OSM_FabricDelta delta = deltaHistory.getOSM_FabricDelta(j);
          dValue = 4.0;
//          OSM_FabricDeltaAnalyzer DeltaAnalysis = new OSM_FabricDeltaAnalyzer(delta, aType);
          
//          dValue = DeltaAnalysis.getFabricRateUtilizationMean();
          ts = delta.getTimeStamp();
//          ts = DeltaAnalysis.getDeltaTimeStamp();

          ms = new FixedMillisecond(ts.getTimeInMillis());
          TSeries.addOrUpdate(ms, dValue);
         }
      }
      else
      {
        // iterate through the collection, and build up a time series
        for(int j = 0; j < history.getSize(); j++)
        {
          OpenSmMonitorService osm = history.getOMS(j);
          
          if(Port != null)
          {
          // find the desired port counter, in this instance
          OSM_Port p = osm.getFabric().getOSM_Ports().get(OSM_Port.getOSM_PortKey(Port));
          lValue = p.pfmPort.getCounter(PortCounter);
          ts = p.pfmPort.getCounterTimeStamp();
          
          }
          else if(MADCounter != null)
          {
            // find the desired MAD counter, in this instance
             lValue = MADCounter.getCounterValue(osm.getFabric().getOsmStats());
             ts = osm.getFabric().getTimeStamp();
//            ts = osm.getTimeStamp();
            
           }
          else if(EventType != null)
          {
            // find the desired Event counter, in this instance
            lValue = osm.getFabric().getOsmEventStats().getCounter(EventType);
            ts = osm.getFabric().getTimeStamp();
//          ts = osm.getTimeStamp();
            
           }
          else
            continue;
          ms = new FixedMillisecond(ts.getTimeInMillis());
          
          TSeries.addOrUpdate(ms, (double)lValue);
        }
      }
    
    TimeSeriesCollection dataset = new TimeSeriesCollection();
    dataset.addSeries(TSeries);

    return dataset;
    }
    
    private XYDataset createDeltaDataset(OSM_FabricDeltaCollection deltaHistory,  Object userElement, String seriesName)
    {
//      PortCounterName portCounter = null;
//      MAD_Counter madCounter = null;
//      OsmEvent osmEvent = null;
//      
//      if((Port != null) && (userElement instanceof PortCounterName))
//        portCounter = (PortCounterName)userElement;
//
//      if((MAD_Stats != null) && (userElement instanceof MAD_Counter))
//        madCounter = (MAD_Counter)userElement;
//
//      if((EventStats != null) && (userElement instanceof OsmEvent))
//        osmEvent = (OsmEvent)userElement;
//      
      TimeSeries series = new TimeSeries(seriesName);

      // iterate through the collection, and build up a time series
      for (int j = 0; j < deltaHistory.getSize(); j++)
      {
        OSM_FabricDelta delta = deltaHistory.getOSM_FabricDelta(j);

        // the dataset is a timeseries collection
        long lValue = 0;
        TimeStamp ts = null;
        RegularTimePeriod ms = null;
        
        if(Port != null)
        {
        // find the desired port counter, in this instance
        LinkedHashMap<String, PFM_PortChange> pcL = delta.getPortChanges();
        PFM_PortChange pC = pcL.get(OSM_Port.getOSM_PortKey(Port));
        lValue = pC.getDelta_port_counter(PortCounter);
        ts = pC.getCounterTimeStamp();
        
        // correct for missing time periods
        int deltaSeconds = delta.getDeltaSeconds();
        long sweepPeriod = delta.getFabric2().getPerfMgrSweepSecs();
        if(sweepPeriod < deltaSeconds)
        {
          // graph is reported as counts per period, so if the period is too long, interpolate
          lValue *= sweepPeriod;
          lValue /= deltaSeconds;
         }
        }
        else if(MADCounter != null)
        {
          // find the desired MAD counter, in this instance
          OSM_Stats mStats = delta.getStatChanges();
          lValue = MADCounter.getCounterValue(mStats);
          ts = delta.getTimeStamp();
          
          // correct for missing time periods
          int deltaSeconds = delta.getDeltaSeconds();
          long sweepPeriod = delta.getFabric2().getPerfMgrSweepSecs();
          if(sweepPeriod < deltaSeconds)
          {
            // graph is reported as counts per period, so if the period is too long, interpolate
            lValue *= sweepPeriod;
            lValue /= deltaSeconds;
           }
         }
        else if(EventType != null)
        {
          // find the desired Event counter, in this instance
          OSM_EventStats eStats = delta.getEventChanges();
          lValue = eStats.getCounter(EventType);
          ts = delta.getTimeStamp();
          
          // correct for missing time periods
          int deltaSeconds = delta.getDeltaSeconds();
          long sweepPeriod = delta.getFabric2().getPerfMgrSweepSecs();
          if(sweepPeriod < deltaSeconds)
          {
            // graph is reported as counts per period, so if the period is too long, interpolate
            lValue *= sweepPeriod;
            lValue /= deltaSeconds;
           }
         }
        else
          continue;

        ms = new FixedMillisecond(ts.getTimeInMillis());
        series.add(ms, (double) lValue);
      }
      TimeSeriesCollection dataset = new TimeSeriesCollection();
      dataset.addSeries(series);

      return dataset;
    }


  private class SimplePlotWorker extends SwingWorker<Void, Void>
  {
    JFreeChart Chart = null;
    ArrayList <SummaryStatistics> uStats = new ArrayList<SummaryStatistics>();
    ArrayList <TimeStamp>         uTimes = new ArrayList<TimeStamp>();
    long NumPorts = 0;
    long NumActivePorts = 0;
    
    public SimplePlotWorker(JFreeChart chart)
    {
      super();
      Chart = chart;
     }

    public SimplePlotWorker()
    {
      this(null);
    }

    @Override
    protected Void doInBackground() throws Exception
    {
      // 1.  counter value (already done, just add following to this)
      // 2.  delta/period
      //
      //    -- if error counter --
      // 3.  include xmit and rcv traffic deltas? (own scale)
      //
      //
      //    -- if traffic counter --
      // 3.  include rate and utilization values?
      //
      //    -- if utilization plot
      // 1.  ave utiliation
      // 2.  std deviation
      // 3.  max utilization
      // 4.  min utilization
      
      // this is a SwingWorker thread from its pool, give it a recognizable name
      Thread.currentThread().setName("SimplePlotWorker");
     
      logger.info( "Worker Building Plot");
      SMT_UpdateService updateService = SMT_UpdateService.getInstance();
      OMS_Collection history = updateService.getCollection();
      OSM_FabricDeltaCollection deltaHistory = history.getOSM_FabricDeltaCollection();

      XYPlot plot = (XYPlot) Chart.getPlot();
      
      ChartUtilities.applyCurrentTheme(Chart);
      
      Color c1 = Color.black;
      Color c2 = Color.blue;
      
      Color c3 = Color.green;
      Color c4 = Color.magenta;
      
      Color ce = Color.red;
 
      // AXIS 2 - the change, or delta value of the desired counter
      if(isUtilizationType())
      {
        // what type of analysis are we doing; SW-SW, ALL, or SW-CA??
        createUtilizationStats(deltaHistory);

        int ndex = 0;
        XYDataset dataset1 = createUtilDataset(uStats, uTimes, PortCounterAxisLabel.UTIL_AVE.getName());
        plot.setDataset(ndex, dataset1);
        NumberAxis axis1 = (NumberAxis)plot.getRangeAxis(ndex);
        axis1.setAutoRangeIncludesZero(true);
        
        ndex++;
        NumberAxis axis2 = new NumberAxis(PortCounterAxisLabel.UTIL_STD_DEV.getName());
        axis2.setFixedDimension(10.0);
        axis2.setAutoRangeIncludesZero(true);
        plot.setRangeAxis(ndex, axis2);
        
        XYDataset dataset2 = createUtilDataset(uStats, uTimes, PortCounterAxisLabel.UTIL_STD_DEV.getName());
        plot.setDataset(ndex, dataset2);
        plot.mapDatasetToRangeAxis(ndex, ndex);
        XYItemRenderer renderer2 = new StandardXYItemRenderer();
        plot.setRenderer(ndex, renderer2);
        
        ndex++;
        NumberAxis axis3 = new NumberAxis(PortCounterAxisLabel.UTIL_MAX.getName());
        axis3.setFixedDimension(10.0);
        axis3.setAutoRangeIncludesZero(true);
        plot.setRangeAxis(ndex, axis3);
        
        XYDataset dataset3 = createUtilDataset(uStats, uTimes, PortCounterAxisLabel.UTIL_MAX.getName());
        plot.setDataset(ndex, dataset3);
        plot.mapDatasetToRangeAxis(ndex, ndex);
        Range r = plot.getDataRange(axis3);
        double uBound = r.getUpperBound();

        XYItemRenderer renderer3 = new StandardXYItemRenderer();
        plot.setRenderer(ndex, renderer3);
        
        ndex++;
        NumberAxis axis4 = new NumberAxis(PortCounterAxisLabel.UTIL_MIN.getName());
        axis4.setFixedDimension(10.0);
        axis4.setAutoRangeIncludesZero(true);
        axis4.setRange(0.0, 1.0);
        plot.setRangeAxis(ndex, axis4);
        
        XYDataset dataset4 = createUtilDataset(uStats, uTimes, PortCounterAxisLabel.UTIL_MIN.getName());
        plot.setDataset(ndex, dataset4);
        plot.mapDatasetToRangeAxis(ndex, ndex);
        XYItemRenderer renderer4 = new StandardXYItemRenderer();
        plot.setRenderer(ndex, renderer4);
        
        // use the same axis scale for all
        axis1.setRange(0.0, uBound);
        axis2.setRange(0.0, uBound);
        axis3.setRange(0.0, uBound);
        axis4.setRange(0.0, uBound);

         // change the series and axis colours after the theme has
        // been applied...
        plot.getRenderer().setSeriesPaint(0, c1);

        renderer2.setSeriesPaint(0, c2);
        axis2.setLabelPaint(c2);
        axis2.setTickLabelPaint(c2);
        
        renderer3.setSeriesPaint(0, ce);
        axis3.setLabelPaint(ce);
        axis3.setTickLabelPaint(ce);
        
        renderer4.setSeriesPaint(0, c4);
        axis4.setLabelPaint(c4);
        axis4.setTickLabelPaint(c4);
        
        }
      else
      {
        // mad counters and event counters (most of this code is universal, only support 2 types now)
        
      NumberAxis axis2 = new NumberAxis(PortCounterAxisLabel.DELTA.getName());
      axis2.setFixedDimension(10.0);
      axis2.setAutoRangeIncludesZero(false);
      plot.setRangeAxis(1, axis2);
      
      XYDataset dataset2 = null;
      
      if(MADCounter != null)
      {
        dataset2 = createDeltaDataset(deltaHistory, MADCounter, PortCounterAxisLabel.DELTA.getName());
      }
      else if (EventType != null)
      {
        dataset2 = createDeltaDataset(deltaHistory, EventType, PortCounterAxisLabel.DELTA.getName());
      }
      
      plot.setDataset(1, dataset2);
      plot.mapDatasetToRangeAxis(1, 1);
      XYItemRenderer renderer2 = new StandardXYItemRenderer();
      plot.setRenderer(1, renderer2);
      
        // the other two axis are optional, and vary depending on the
        // type of counter
        NumberAxis axis3 = null;
        XYDataset dataset3 = null;
        XYItemRenderer renderer3 = null;
        NumberAxis axis4 = null;
        XYDataset dataset4 = null;
        XYItemRenderer renderer4 = null;

        
        if(isCompare())
        {
          if(isErrorType())
          {
            // add rcv deltas
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
          }
          else
          {
            // add rate
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
          }
        }
       
             
        if(isErrorType())
          c2 = ce;
        
        // change the series and axis colours after the theme has
        // been applied...
        plot.getRenderer().setSeriesPaint(0, c1);

        renderer2.setSeriesPaint(0, c2);
        axis2.setLabelPaint(c2);
        axis2.setTickLabelPaint(c2);
        
        if(isCompare())
        {
          renderer3.setSeriesPaint(0, c3);
          axis3.setLabelPaint(c3);
          axis3.setTickLabelPaint(c3);
          
          renderer4.setSeriesPaint(0, c4);
          axis4.setLabelPaint(c4);
          axis4.setTickLabelPaint(c4);
        }
      }

      return null;
    }
    
    /************************************************************
     * Method Name:
     *  createUtilDataset
    **/
    /**
     * Describe the method here
     *
     * @see     describe related java objects
     *
     * @param uStats2
     * @param uTimes2
     * @param name
     * @return
     ***********************************************************/
    private XYDataset createUtilDataset(ArrayList<SummaryStatistics> uStats2, ArrayList<TimeStamp> uTimes2, String seriesName)
    {
      // TODO Auto-generated method stub
      if((uStats2 != null) && (uTimes2 != null))
      {
        // they must be the same size
        TimeStamp[]         tA = uTimes2.toArray(new TimeStamp[uTimes2.size()]);
        SummaryStatistics[] sA = uStats2.toArray(new SummaryStatistics[uStats2.size()]);

        if(sA.length != tA.length)
        {
          System.err.println("Arrays are of different length, not possible! Utilization Array in SimpleXY_Chart");
          System.exit(0);
        }
        
        TimeSeries series = new TimeSeries(seriesName);
        double dValue = 0.0;
        TimeStamp ts = null;
        RegularTimePeriod ms = null;

        // loop through the arrays, and create the desired dataset
        for(int i = 0; i < tA.length; i++)
        {
          if(PortCounterAxisLabel.UTIL_STD_DEV.getName().equals(seriesName))
            dValue = sA[i].getStandardDeviation();
          else if(PortCounterAxisLabel.UTIL_MAX.getName().equals(seriesName))
            dValue = sA[i].getMax();
          else if(PortCounterAxisLabel.UTIL_MIN.getName().equals(seriesName))
            dValue = sA[i].getMin();
          else if(PortCounterAxisLabel.UTIL_AVE.getName().equals(seriesName))
            dValue = sA[i].getMean();
          
          ts = tA[i];
          ms = new FixedMillisecond(ts.getTimeInMillis());
          series.addOrUpdate(ms, dValue);
        }
        
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(series);
        return dataset;
      }
      System.err.println("Null Data");
      return null;
    }

    /************************************************************
     * Method Name:
     *  createUtilizationStats
    **/
    /**
     * Describe the method here
     *
     * @see     describe related java objects
     *
     * @param deltaHistory
     ***********************************************************/
    private void createUtilizationStats(OSM_FabricDeltaCollection deltaHistory)
    {
      OSM_NodeType aType = OSM_NodeType.UNKNOWN;  // all nodes
      aType = OSM_NodeType.CA_NODE;               // only compute nodes
      aType = OSM_NodeType.SW_NODE;               // only switch nodes
      
      if(SMT_AnalysisType.SMT_UTIL_ALL_PORTS.equals(getAnalysisType()))
        aType = OSM_NodeType.UNKNOWN;
      if(SMT_AnalysisType.SMT_UTIL_SW_PORTS.equals(getAnalysisType()))
        aType = OSM_NodeType.SW_NODE;
      if(SMT_AnalysisType.SMT_UTIL_CA_PORTS.equals(getAnalysisType()))
        aType = OSM_NodeType.CA_NODE;
      


      OSM_FabricDelta d = deltaHistory.getOSM_FabricDelta(0);
      SmtFabricStructure fabStruct = new SmtFabricStructure(d.getFabric1());
      NumActivePorts = fabStruct.getNumActivePorts();

      MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INFO, " building utilization statistics, please be patient..."));
      for(int j = 0; j < deltaHistory.getSize(); j++)
      {
        OSM_FabricDelta delta = deltaHistory.getOSM_FabricDelta(j);
        OSM_FabricDeltaAnalyzer DeltaAnalysis = new OSM_FabricDeltaAnalyzer(delta, aType);
        
        SummaryStatistics stats = DeltaAnalysis.getFabricUtilizationStats();
        uStats.add(stats);
        NumPorts = stats.getN();
        uTimes.add(DeltaAnalysis.getDeltaTimeStamp());
       }
    }

    @Override
    public void done()
    {
      if(isUtilizationType())
      {
      Chart.addSubtitle(new TextTitle(NumPorts + " of " + NumActivePorts + " active ports"));
      }
      // completion notification
      logger.info( "Worker Done Building Plot");
      MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INFO, "Worker Done Building Plot"));
     }
  }

}
