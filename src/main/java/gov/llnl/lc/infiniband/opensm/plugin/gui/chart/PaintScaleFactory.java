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
 *        file: PaintScaleFactory.java
 *
 *  Created on: Apr 3, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.chart;

import java.awt.Color;
import java.awt.Paint;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**********************************************************************
 * Describe purpose and responsibility of PaintScaleFactory
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Apr 3, 2015 10:39:36 AM
 **********************************************************************/
public class PaintScaleFactory
{
  static final int maxDivisions   = 100;
  static final double maxColor    = 255.8;
  static final Paint defaultColor = Color.white;
  
  public static LookupPaintScale getLookupPaintScale(int type, double lower, double upper,  double maxValue)
  {
    LookupPaintScale lps = new LookupPaintScale(lower, upper, defaultColor);
    double inc = maxColor / maxDivisions;
    double cinc = inc * 2.0;
    int half = maxDivisions / 2;
    double cdiv = maxValue / maxDivisions;
    
    lps.add(0, Color.black);

    if (type == 0)
    {
      Color[] gColors = generateColors(maxDivisions - 1);
      for (int i = 1; i < maxDivisions; i++)
      {
        double cval = (double)i * cdiv;
        lps.add(cval, gColors[i - 1]);
      }
    }
    else if(type == 1)
    {
      for (int i = 1; i < maxDivisions; i++)
      {
        //
        boolean firstHalf = i < half;
        int rval = firstHalf ? 0 : (int) ((i - half) * cinc);
        int gval = firstHalf ? (int) (i * cinc) : 510 - (int) (i * cinc);
        int bval = firstHalf ? 255 - (int) (i * cinc) : 0;
        double cval = (double)i * cdiv;
        lps.add(cval, new Color(rval, gval, bval));
//        System.err.println(i + ") V:" + cval + ", R:" + rval + ", G:" + gval + ", B:" + bval);
      }
    }
    else
    {
      for (int i = 1; i < maxDivisions; i++)
      {
        // red slowly ramps up, and blue slowly ramps down
        boolean firstHalf = i < half;
        int rval = (int)(i * inc);
        int gval = firstHalf ? (int) (i * cinc) : 510 - (int) (i * cinc);
        int bval = 255 - rval;
        double cval = (double)i * cdiv;
        lps.add(cval, new Color(rval, gval, bval));
//        System.err.println(i + ") V:" + cval + ", R:" + rval + ", G:" + gval + ", B:" + bval);
      }
    }
//    System.err.println("Color type: " + type);
    return lps;
  }
  
  public static Color[] generateColors(int n)
  {
    Color[] cols = new Color[n];
    for(int i = 0; i < n; i++)
    {
      cols[i] = Color.getHSBColor((float) i / (float) n, 0.85f, 1.0f);
    }
    return cols;
  }

  public static ValueAxis getPaintScaleAxis(double lower, double upper, String label)
  {
    // return a simple axis for the scale
    double inc = (upper - lower)/maxDivisions;
    
    final XYSeries s1 = new XYSeries("Series 1");

    for (int i = 0; i < maxDivisions; i++)
    {
        s1.add(i, i*inc);
    }

    XYSeriesCollection dataset = new XYSeriesCollection();
    dataset.addSeries(s1);
     
    JFreeChart chart = ChartFactory.createXYLineChart(
        "Axis Demo",          // chart title
        "Category",               // domain axis label
        "Value",                  // range axis label
        dataset,                  // data
        PlotOrientation.VERTICAL,
        true,                     // include legend
        true,
        false
    );

    final XYPlot plot = chart.getXYPlot();
    final NumberAxis domainAxis = new NumberAxis("xish");
    final NumberAxis rangeAxis = new NumberAxis(label);
    plot.setDomainAxis(domainAxis);
    plot.setRangeAxis(rangeAxis);
    return plot.getRangeAxis();
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
