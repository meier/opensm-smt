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
 *        file: PortCounterCrossHairXYplot.java
 *
 *  Created on: Feb 3, 2014
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.chart;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.text.SimpleDateFormat;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.DateCellRenderer;
import org.jfree.ui.NumberCellRenderer;
import org.jfree.ui.RefineryUtilities;

import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_Port.PortCounterName;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;

/**********************************************************************
 * This Plot contains a normal PortCounterXYplotPanel AND a PortCounterTable.
 * There is a slider that controls a vertical crosshair, which in turn
 * populates the values in the Table.
 * <p>
 * 
 * @see related classes and interfaces
 * 
 * @author meier3
 * 
 * @version Feb 3, 2014 11:19:29 AM
 **********************************************************************/
public class PortCounterCrossHairXYplot extends JFrame
{
  private class PortCounterPlotPanel extends JPanel implements ChangeListener, ChartProgressListener
  {
    static final protected int MAX_DATASETS = 4;
    static final protected int MAX_DS_SIZE  = 102;

    private int NumDataSets = MAX_DATASETS/2;

    private ChartPanel            chartPanel;

    private PortCounterTableModel model;

    private JFreeChart            chart;

    private JSlider               slider;
    
    private int CrossHairDomainIndex;

    /************************************************************
     * Method Name:
     *  getCrossHairDomainIndex
     **/
    /**
     * Returns the value of crossHairDomainIndex
     *
     * @return the crossHairDomainIndex
     *
     ***********************************************************/
    
    protected int getCrossHairDomainIndex()
    {
      return CrossHairDomainIndex;
    }

    /************************************************************
     * Method Name:
     *  setCrossHairDomainIndex
     **/
    /**
     * Sets the value of crossHairDomainIndex
     *
     * @param crossHairDomainIndex the crossHairDomainIndex to set
     *
     ***********************************************************/
    protected void setCrossHairDomainIndex()
    {
      int value = this.slider.getValue();

      if (this.chartPanel != null)
      {
        JFreeChart c = this.chart;
        if (c != null)
        {
          XYPlot plot = (XYPlot) c.getPlot();
          XYDataset dataset = plot.getDataset();
          int itemCount = dataset.getItemCount(0);
          double ndexD = (double)itemCount * ((double)value / 100);
          int ndex = (int)ndexD;
           
          // the index goes from 0 to (itemCount -1)
          this.CrossHairDomainIndex = ndex >= itemCount ? itemCount-1: ndex;
        }
      }
    }

    /**
     * Creates a new demo panel.
     */
    public PortCounterPlotPanel()
    {
      super(new BorderLayout());
//      initChart();
    }
    
    public PortCounterPlotPanel(IB_Vertex vertex, OSM_Port port, PortCounterName portCounter, boolean includeExtra)
    {
      super(new BorderLayout());
      initChart(vertex, port, portCounter, includeExtra);
    }
    

    private void initChart(IB_Vertex vertex, OSM_Port port, PortCounterName portCounter, boolean includeExtra)
    {
      PortCounterXYplotPanel plot1 = new PortCounterXYplotPanel(vertex, port, portCounter, includeExtra);
      
      // normally just counts and delta counts, but can include more
      int numRows = includeExtra ? MAX_DATASETS: MAX_DATASETS/2;
      int rowSize = includeExtra ? MAX_DS_SIZE: MAX_DS_SIZE/2 + 19;  // extra for padding
      NumDataSets = numRows;

      this.chartPanel = plot1;
      this.chart      = plot1.getChart();
      
       // see "chartProgress()" method
      this.chart.addProgressListener(this);

      this.chartPanel.setPreferredSize(new java.awt.Dimension(750, 300));
      this.chartPanel.setDomainZoomable(true);
      this.chartPanel.setRangeZoomable(true);
      Border border = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4), BorderFactory.createEtchedBorder());
      this.chartPanel.setBorder(border);
      add(this.chartPanel);

      JPanel dashboard = new JPanel(new BorderLayout());
      dashboard.setPreferredSize(new Dimension(400, rowSize));
      dashboard.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 4));
      
      this.model = new PortCounterTableModel(numRows);
      
      // initialize the model, and table, here
//      this.model.setValueAt("name", 0, 1);
      this.model.setValueAt(new Double("0.00"), 0, 1);
      this.model.setValueAt(new Double("0.00"), 0, 2);
//      this.model.setValueAt("units", 0, 3);
      JTable table = new JTable(this.model);
      
      // the columns are name, time, value, units.  both name and units are strings
      // so need special renderers for time and value
      
      TableCellRenderer renderer1 = new DateCellRenderer(new SimpleDateFormat("HH:mm:ss"));
      TableCellRenderer renderer2 = new NumberCellRenderer();
      table.getColumnModel().getColumn(1).setCellRenderer(renderer1);
      table.getColumnModel().getColumn(2).setCellRenderer(renderer2);
      JScrollPane scroller = new JScrollPane(table);
      dashboard.add(scroller);

      this.slider = new JSlider(0, 100, 10);
      this.slider.addChangeListener(this);
      dashboard.add(this.slider, BorderLayout.SOUTH);
      add(dashboard, BorderLayout.SOUTH);
      
       XYPlot plot = (XYPlot) this.chart.getPlot();
      
    plot.setDomainCrosshairLockedOnData(true);
    plot.setRangeCrosshairVisible(false);
    plot.setDomainCrosshairVisible(true);
    }

 
    /**
     * Handles a state change event. This comes from the slider, and it moves
     * the crosshair.
     * 
     * @param event
     *          the event.
     */
    public void stateChanged(ChangeEvent event)
    {
      int value = this.slider.getValue();
      XYPlot plot = (XYPlot) this.chart.getPlot();

      ValueAxis domainAxis = plot.getDomainAxis();
      Range range = domainAxis.getRange();
      double c = domainAxis.getLowerBound() + (value / 100.0) * range.getLength();

      // displays the vertical crosshair in the desired position
      plot.setDomainCrosshairValue(c);
      
      // the cross hair is keyed to the horizontal slider - for the time domain axis
      setCrossHairDomainIndex();
    }

    /**
     * Handles a chart progress event.
     * 
     * @param event
     *          the event.
     */
    public void chartProgress(ChartProgressEvent event)
    {
      if (event.getType() != ChartProgressEvent.DRAWING_FINISHED)
      {
        return;
      }
      
      // update the table model
      
      if (this.chartPanel != null)
      {
        JFreeChart c = this.chart;
        if (c != null)
        {
          XYPlot plot = (XYPlot) c.getPlot();
          
          int ndex = this.getCrossHairDomainIndex();
 
          // update the table from the value at the crosshair
          
          for(int pnum = 0; pnum < NumDataSets; pnum++)
          {
            XYDataset dataset = plot.getDataset(pnum);
            String seriesName = "Unknown";
            if((dataset != null) && (dataset.getSeriesKey(0) != null))
            {
              int ds_size = dataset.getItemCount(0);
              if(dataset.getSeriesKey(0) instanceof String)
                seriesName = (String)dataset.getSeriesKey(0);
              
              // the name
              this.model.setValueAt(seriesName, pnum, 0);
              
              // the deltas are one smaller than the counters, so make sure the
              // crosshair index is valid
              if(ndex < ds_size)
              {
                // the time
                this.model.setValueAt(dataset.getXValue(0, ndex), pnum, 1);
                
                // the value
                this.model.setValueAt(dataset.getYValue(0, ndex), pnum, 2);               
              }
               
              // the units (key off the series name)
              PortCounterAxisLabel label = PortCounterAxisLabel.getByName(seriesName);
              if(label != null)
                this.model.setValueAt(label.getUnits(), pnum, 3);
              else
                this.model.setValueAt(PortCounterAxisLabel.DELTA.getUnits(), pnum, 3);
            }
           }
         }
        else
          System.err.println("Its NULL, Jim!");
      }
    }

  
  
  }

  /**
   * A demonstration application showing how to control a crosshair using an
   * external UI component.
   * 
   * @param title
   *          the frame title.
   */
  public PortCounterCrossHairXYplot(String title)
  {
    super(title);
    setContentPane(new PortCounterPlotPanel());
  }
  
  public PortCounterCrossHairXYplot(IB_Vertex vertex, OSM_Port port, PortCounterName portCounter, boolean includeExtra)
  {
    // this is the normal constructor
    this(portCounter.getName() + "   [" + vertex.getName() + "   (" + port.getOSM_PortKey() + ")]");
    
    setContentPane(new PortCounterPlotPanel(vertex, port, portCounter, includeExtra));
  }


//  /**
//   * Creates a panel for the demo (used by SuperDemo.java).
//   * 
//   * @return A panel.
//   */
//  public JPanel createPlotPanel()
//  {
//    return new PortCounterPlotPanel();
//  }

  /**
   * Starting point for the demonstration application.
   * 
   * @param args
   *          ignored.
   */
  public static void main(String[] args)
  {
    PortCounterCrossHairXYplot plot = new PortCounterCrossHairXYplot("PortCounterCrossHairXYplot");
    plot.pack();
    RefineryUtilities.centerFrameOnScreen(plot);
    plot.setVisible(true);
  }

  /**
   * A PortCounter table model.
   */
  class PortCounterTableModel extends AbstractTableModel implements TableModel
  {
    private int MAX_COLS = 4;
    private int NumRows   = 4;
    
    private Object[][] data;

    /**
     * Creates a table model 4 columns by specified rows (1 + header)
     * 
     * @param rows
     *          the row count.
     */
    public PortCounterTableModel(int rows)
    {
      // how many rows depends on how many series
      // the first (or zeroth) row is for labels
      NumRows = rows < PortCounterPlotPanel.MAX_DATASETS ? rows: PortCounterPlotPanel.MAX_DATASETS;
      this.data = new Object[NumRows][MAX_COLS];
    }

    /**
     * Returns the number of columns.
     * 
     * @return MAX_COLS.
     */
    public int getColumnCount()
    {
      return MAX_COLS;
    }

    /**
     * Returns the row count.
     * 
     * @return NumRows.
     */
    public int getRowCount()
    {
       return NumRows;
    }

    /**
     * Returns the value at the specified cell in the table.
     * 
     * @param row
     *          the row index.
     * @param column
     *          the column index.
     * 
     * @return The value.
     */
    public Object getValueAt(int row, int column)
    {
      return this.data[row][column];
    }

    /**
     * Sets the value at the specified cell.
     * 
     * @param value
     *          the value.
     * @param row
     *          the row index.
     * @param column
     *          the column index.
     */
    public void setValueAt(Object value, int row, int column)
    {
      this.data[row][column] = value;
      fireTableDataChanged();
    }

    /**
     * Returns the column name.
     * 
     * @param column
     *          the column index.
     * 
     * @return The column name.
     */
    public String getColumnName(int column)
    {
      switch (column)
      {
        case 0:
          return "name";
        case 3:
          return "units";
        case 1:
          return "time";
        case 2:
          return "value";
      }
      return null;
    }
  }
}
