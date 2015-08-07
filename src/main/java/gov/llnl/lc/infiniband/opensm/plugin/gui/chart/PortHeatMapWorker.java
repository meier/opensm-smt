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
 *        file: PortHeatMapWorker.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.chart;

import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Collection;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Depth;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.command.SmtCommand;
import gov.llnl.lc.smt.data.SMT_UpdateService;
import gov.llnl.lc.smt.event.SmtMessage;
import gov.llnl.lc.smt.event.SmtMessageType;
import gov.llnl.lc.smt.manager.MessageManager;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.EnumSet;

import javax.swing.SwingWorker;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYDataImageAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.data.Range;
import org.jfree.data.general.HeatMapUtilities;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

public class PortHeatMapWorker extends SwingWorker<Void, Void> implements CommonLogger
{
  private PortHeatMapPlotPanel PlotPanel;
  private EnumSet<IB_Depth> IncludedDepths;
  private OMS_Collection History;
  private boolean UseService = false;        // if true, use the SmtService, if false, use default file

  public PortHeatMapWorker(PortHeatMapPlotPanel portHeatMapPlotPanel)
  {
    this(portHeatMapPlotPanel, null, false);
  }

  public PortHeatMapWorker(PortHeatMapPlotPanel portHeatMapPlotPanel, EnumSet<IB_Depth> includedDepths, boolean useService)
  {
    super();
    PlotPanel      = portHeatMapPlotPanel;
    IncludedDepths = includedDepths;
    UseService     = useService;
    History        = null;
  }

  public PortHeatMapWorker(PortHeatMapPlotPanel portHeatMapPlotPanel, EnumSet<IB_Depth> includedDepths, OMS_Collection history)
  {
    super();
    PlotPanel      = portHeatMapPlotPanel;
    IncludedDepths = includedDepths;
    UseService     = false;
    History        = history;
  }

  protected Void doInBackground() throws Exception
  {
    // this is a SwingWorker thread from its pool, give it a recognizable name
    Thread.currentThread().setName("PortHeatMapWorker");

    JFreeChart Chart = PlotPanel.getHeatChart();

    logger.info("Worker Building HeatMapPlot");
    MessageManager.getInstance().postMessage(
        new SmtMessage(SmtMessageType.SMT_MSG_INFO, "Worker Building HeatMapPlot"));
    
    PortHeatMapDataSet pHeatMap = null;
    if(UseService)
    {
       SMT_UpdateService updateService = SMT_UpdateService.getInstance();
       History = updateService.getCollection();
       pHeatMap = new PortHeatMapDataSet(History, IncludedDepths);
    }
    else if(History != null)
    {
        pHeatMap = new PortHeatMapDataSet(History, IncludedDepths);
    }
    else
    {
      // FIXME - eliminate this, for test purposes only
      pHeatMap = new PortHeatMapDataSet(SmtCommand.convertSpecialFileName("%h/scripts/OsmScripts/SmtScripts/sierra3H.his"), IncludedDepths);
    }
//    logger.fine("Finished creating dataset");
//    logger.fine("Max X: " + pHeatMap.getMaximumXValue());
//    logger.fine("Max Y: " + pHeatMap.getMaximumYValue());
//    logger.fine("Max Z: " + pHeatMap.getMaximumZValue());
//    
    // if any of these "maximum" values are illegal, stop here and return null
    if(!pHeatMap.isValid())
    {
      logger.severe("Invalid HeatMap, check OMS Collection or Depth filter (empty or null)");
      MessageManager.getInstance().postMessage(
          new SmtMessage(SmtMessageType.SMT_MSG_SEVERE, "Invalid HeatMap, check OMS Collection or Depth filter (empty or null)"));
      PlotPanel.setHeatMapDataSet(null);
      return null;
    }

    PlotPanel.setHeatMapDataSet(pHeatMap);

    Range fixedXRange = new Range(0, pHeatMap.getMaximumXValue()); // time #
    Range fixedYRange = new Range(0, pHeatMap.getMaximumYValue()); // port #
    Range fixedZRange = new Range(0, pHeatMap.getMaximumZValue()); // % Util

    // there are 3 valid paint scales, 0, 1, & 2
    LookupPaintScale paintScale = PaintScaleFactory.getLookupPaintScale(1, 0,
        fixedZRange.getUpperBound(), fixedZRange.getUpperBound());
    ValueAxis paintAxis = PaintScaleFactory.getPaintScaleAxis(0, fixedZRange.getUpperBound(),
        PortHeatMapPlotPanel.UtilizationAxisLabel);

    BufferedImage image = HeatMapUtilities.createHeatMapImage(pHeatMap, paintScale);
    XYDataImageAnnotation ann = new XYDataImageAnnotation(image, fixedXRange.getLowerBound(),
        fixedYRange.getLowerBound(), fixedXRange.getUpperBound(), fixedYRange.getUpperBound(), true);
    XYPlot plot = (XYPlot) Chart.getPlot();
    plot.getRenderer().addAnnotation(ann, Layer.BACKGROUND);

    // finally, show the heatmap
    PaintScaleLegend psLegend = new PaintScaleLegend(paintScale, paintAxis);
    psLegend.setMargin(new RectangleInsets(3, 40, 3, 10));
    psLegend.setPosition(RectangleEdge.TOP); // location (within NORTH) of
                                             // heatmap legend
    psLegend.setAxisOffset(4.0);
    psLegend.setFrame(new BlockBorder(Color.GRAY));
    Chart.addSubtitle(psLegend);

    // fix the sliders ranges, and set them for the middle
    if ((pHeatMap != null) && (PlotPanel != null))
    {
      PlotPanel.getTimeSlider().setMinimum((int) fixedXRange.getLowerBound());
      PlotPanel.getTimeSlider().setMaximum((int) fixedXRange.getUpperBound());
      PlotPanel.getTimeSlider().setValue((int) fixedXRange.getCentralValue());

      PlotPanel.getPortSlider().setMinimum((int) fixedYRange.getLowerBound());
      PlotPanel.getPortSlider().setMaximum((int) fixedYRange.getUpperBound());
      PlotPanel.getPortSlider().setValue((int) fixedYRange.getCentralValue());
      
      HeatMapDepthPanel hmdp = new HeatMapDepthPanel(pHeatMap);
      PlotPanel.replaceDepthPanel(hmdp);
    }
    return null;
  }

  @Override
  public void done()
  {
    // completion notification
    logger.severe("Worker Done Building HeatMapPlot");

    // refresh the chart, probably not necessary
    PlotPanel.chartChanged(null);
    PlotPanel.repaint();

    MessageManager.getInstance().postMessage(
        new SmtMessage(SmtMessageType.SMT_MSG_INFO, "Worker Done Building HeatMapPlot"));
  }

}
