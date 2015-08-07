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
 *        file: BarGraphDataSeries.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.bargraph;

import java.util.ArrayList;

import gov.llnl.lc.time.TimeStamp;
import gov.llnl.lc.util.ValueRange;

import org.jfree.data.category.DefaultCategoryDataset;

public interface BarGraphDataSeries
{
  /************************************************************
   * Method Name:
   *  isValid
   **/
  /**
   * The data series gets constructed from a variety of pieces of
   * information, and any number of things can go wrong in the
   * constructor, resulting in a partially constructed object.
   * The object will exist, just may not be fully built.
   * 
   * Returns true, if this data series is valid.
   *
   * @return the valid
   *
   ***********************************************************/
  
  public boolean isValid();
 
  /************************************************************
   * Method Name:
   *  getGraphTitle
   **/
  /**
   * Returns the value of title that goes in the Chart or outter
   * most panel that contains the graph (window)
   *
   * @return the title
   *
   ***********************************************************/
  
  public String getGraphTitle();

  /************************************************************
   * Method Name:
   *  getTitle
   **/
  /**
   * Returns the value of title just above the plot or graph
   *
   * @return the title
   *
   ***********************************************************/
  
  public String getTitle();

  /************************************************************
   * Method Name:
   *  getSeriesLabel
  **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   * @param seriesIndex
   * @return
   ***********************************************************/
  public String getSeriesLabel(int seriesIndex);

  /************************************************************
   * Method Name:
   *  getDomainLabel
   **/
  /**
   * Returns the value of domainLabel
   *
   * @return the domainLabel
   *
   ***********************************************************/
  
  public String getDomainLabel();

  /************************************************************
   * Method Name:
   *  getRangeLabel
   **/
  /**
   * Returns the value of rangeLabel
   *
   * @return the rangeLabel
   *
   ***********************************************************/
  
  public String getRangeLabel();

  public ArrayList <ValueRange> getRangeValueRanges();
  
  public ArrayList <ValueRange> getDomainValueRanges();
  
  /************************************************************
   * Method Name:
   *  getNumInSeries
   **/
  /**
   * Returns the value of numInSeries
   *
   * @return the numInSeries
   *
   ***********************************************************/
  
  public int getNumInSeries();
 
  /************************************************************
   * Method Name:
   *  getInitialTime
   **/
  /**
   * Returns the value of initialTime
   *
   * @return the initialTime
   *
   ***********************************************************/
  
  public TimeStamp getInitialTime();

  /************************************************************
   * Method Name:
   *  getFinalTime
   **/
  /**
   * Returns the value of finalTime
   *
   * @return the finalTime
   *
   ***********************************************************/
  
  public TimeStamp getFinalTime();
 
  /************************************************************
   * Method Name:
   *  getDeltaSeconds
   **/
  /**
   * Returns the (nominal) number of seconds between data points.
   * The data in the DataSet is expected to be collected in a
   * periodic fashion, so this value represents the period between
   * data samples.
   *
   * @return the deltaSeconds
   *
   ***********************************************************/
  
  public int getDeltaSeconds();
 
  /************************************************************
   * Method Name:
   *  getDataSet
   **/
  /**
   * Returns the specified Dataset.  When the Object is created
   * many global, structural attributes are determined, set, and
   * remain static.  The Index is a sequential index, usually based
   * on time.
   *
   * @return the dataSet
   *
   ***********************************************************/
  
  public DefaultCategoryDataset getDataSet(int Index);

  /************************************************************
   * Method Name:
   *  getFabricName
   **/
  /**
   * Returns the value of fabricName
   *
   * @return the fabricName
   *
   ***********************************************************/
  
  public String getFabricName();
  
  public boolean copyDataset(DefaultCategoryDataset to, int fromIndex);


}
