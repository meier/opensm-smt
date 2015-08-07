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
 *        file: OSM_TimeSeriesCollection.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.utils;

import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_Port.PortCounterName;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_PortChange;
import gov.llnl.lc.time.TimeStamp;
import gov.llnl.lc.util.BinList;
import gov.llnl.lc.util.BinValueKeys;

import java.util.ArrayList;

public class OSM_TimeSeriesCollection
{
  
  private ArrayList <BinList<PFM_PortChange>> TimeSeries;
  
  private TimeStamp InitialTime;
  
  private TimeStamp FinalTime;
  
  private String FabricName;
  
  private PortCounterName CounterName;
  
  private Long MaxBinSize;
  
  private Long MinBinSize;
  
  private BinValueKeys binValuesAndKeys;  // a set (hashMap) of bin value ranges, for the time series
  
  

  /************************************************************
   * Method Name:
   *  getTimeSeries
   **/
  /**
   * Returns the value of timeSeries
   *
   * @return the timeSeries
   *
   ***********************************************************/
  
  public ArrayList<BinList<PFM_PortChange>> getTimeSeries()
  {
    return TimeSeries;
  }



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
  
  public TimeStamp getInitialTime()
  {
    return InitialTime;
  }



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
  
  public TimeStamp getFinalTime()
  {
    return FinalTime;
  }



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
  
  public String getFabricName()
  {
    return FabricName;
  }



  /************************************************************
   * Method Name:
   *  getCounterName
   **/
  /**
   * Returns the value of counterName
   *
   * @return the counterName
   *
   ***********************************************************/
  
  public PortCounterName getCounterName()
  {
    return CounterName;
  }



  /************************************************************
   * Method Name:
   *  getMaxBinSize
   **/
  /**
   * Returns the value of maxBinSize
   *
   * @return the maxBinSize
   *
   ***********************************************************/
  
  public Long getMaxBinSize()
  {
    return MaxBinSize;
  }



  /************************************************************
   * Method Name:
   *  getMinBinSize
   **/
  /**
   * Returns the value of minBinSize
   *
   * @return the minBinSize
   *
   ***********************************************************/
  
  public Long getMinBinSize()
  {
    return MinBinSize;
  }



  /************************************************************
   * Method Name:
   *  getMaxNumBins
   **/
  /**
   * Returns the value of maxNumBins
   *
   * @return the maxNumBins
   *
   ***********************************************************/
  
  public BinValueKeys getBinValueKeys()
  {
    return binValuesAndKeys;
  }



  /************************************************************
   * Method Name:
   *  OSM_TimeSeriesCollection
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param timeSeries
   * @param initialTime
   * @param finalTime
   * @param fabricName
   * @param counterName
   * @param maxBinSize
   * @param minBinSize
   * @param maxNumBins
   ***********************************************************/
  public OSM_TimeSeriesCollection(ArrayList<BinList<PFM_PortChange>> timeSeries,
      TimeStamp initialTime, TimeStamp finalTime, String fabricName, PortCounterName counterName,
      Long maxBinSize, Long minBinSize, BinValueKeys binValueKeys)
  {
    super();
    TimeSeries = timeSeries;
    InitialTime = initialTime;
    FinalTime = finalTime;
    FabricName = fabricName;
    CounterName = counterName;
    MaxBinSize = maxBinSize;
    MinBinSize = minBinSize;
    binValuesAndKeys = binValueKeys;
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
