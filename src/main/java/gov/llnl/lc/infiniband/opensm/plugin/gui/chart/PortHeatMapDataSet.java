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
 *        file: PortHeatMapDataSet.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.chart;

import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Collection;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_FabricAnalyzer;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_FabricDeltaAnalyzer;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_PortChange;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_PortRate;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Depth;
import gov.llnl.lc.infiniband.opensm.plugin.gui.data.HM_Port;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.command.SmtCommand;
import gov.llnl.lc.time.TimeStamp;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jfree.data.general.HeatMapDataset;

public class PortHeatMapDataSet implements HeatMapDataset, CommonLogger
{
  /** the heat map is derived from an OMS_Collection **/
  OMS_Collection omsHistory = null;

  /** the file, where the collection came from **/
  String fileName           = SmtCommand.convertSpecialFileName("%h/scripts/OsmScripts/SmtScripts/sierra3H.his");
  
  public EnumSet<IB_Depth> IncludedDepths;

  /** the Y values **/
  public SortedSet<HM_Port> FilteredAndSortedPorts = new TreeSet<HM_Port>();
  
  /** the number of levels, and the size **/
  public int LevelSize [] = new int [IB_Depth.MAX_IB_DEPTH];
  
  /** the X values **/
  ArrayList<Long> timeInMillis = new ArrayList<Long>();
  
//  private OSM_FabricAnalyzer fabricAnalyzer = null;
  
  private double [][] ZValues;
  
  private double maxZvalue;
  
/** X Axis - timestamps.   Should be a timestamp for each snapshot, so the size of the collection
 *  Y Axis - port.         Should be ordered, based on node name, then port number (with and without switches??)
 *  Z Axis - %Utilization. Should be xmit_data.  Parameterize, just to solve the general case.
 */
  
  /************************************************************
   * Method Name:
   *  PortHeatMapDataSet
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   ***********************************************************/
  public PortHeatMapDataSet(String filename, EnumSet<IB_Depth> includedDepths)
  {
    OMS_Collection history = null;
    try
   {
     history = OMS_Collection.readOMS_Collection(fileName);
   }
   catch (Exception e)
   {
     logger.severe("Couldn't open the file: " + fileName);
     e.printStackTrace();
     System.exit(0);
   }
   logger.severe("Done reading the file: " + fileName);
   
   createMapDataSet(history, includedDepths);
  }

  public PortHeatMapDataSet(OMS_Collection history, EnumSet<IB_Depth> includedDepths)
  {
    createMapDataSet(history, includedDepths);
  }

  
  private void createMapDataSet(OMS_Collection history, EnumSet<IB_Depth> includedDepths)
  {
     omsHistory     = history;
     IncludedDepths = includedDepths;
     
     // this is time consuming, especially if you scan the ENTIRE history
     // normally, it is not necessary to scan the entire history, so safe to use FALSE
     SortedSet<HM_Port> sortedPorts = HM_Port.getUniquePortSet(history, false);

    calculateLevels(sortedPorts);

    // may only want a subset of ALL of the ports, filter out unwanted
    // ** puts in FilteredAndSortedPorts **
    filterPorts(sortedPorts);
    
    // z axis is the % utilization of the port
    calculateZvalues(FilteredAndSortedPorts);
  }
  
  private boolean filterPorts(SortedSet<HM_Port> sortedPorts)
  {
    Iterator<HM_Port> it = sortedPorts.iterator();
    while(it.hasNext())
    {
      HM_Port hmp = (HM_Port)it.next();

      // conditionally add this HM_Port, if it passes the filter
    if(include(hmp))
      FilteredAndSortedPorts.add(hmp);

    }
    return true;
  }

  private boolean include(HM_Port hmp)
  {
    // return true, if this HM_Port should be included
    // in the sorted list (NOT_FILTERED)
    //
    // return false, if this HM_Port would be filtered out
    
    if(IncludedDepths != null)
      return IncludedDepths.contains(hmp.getIB_Depth());
    
    return true;
  }

  private boolean calculateZvalues(SortedSet<HM_Port> sortedPorts)
  {
    logger.info("Calculating Z values now : " + new TimeStamp());
    logger.info("History size for Z values is : " + omsHistory.getSize());
    
    // step through each timestamp, and pre-calculate these
    ZValues = new double[omsHistory.getSize() -1][sortedPorts.size()];
    maxZvalue = -1;
    
    // iterate over ALL snapshots, just to make sure we have ALL the ports
    OpenSmMonitorService oms0 = omsHistory.getOMS(0);  // previous
    OpenSmMonitorService oms1 = omsHistory.getOMS(0);  // current
    
    // just do this once, need to know maximum BW for %Utilization calculation
    // now for each port in the OMS, add it to the tree
    OSM_Fabric fab = oms1.getFabric();
    OSM_FabricAnalyzer fabricAnalyzer = new OSM_FabricAnalyzer(fab);

    for(int X = 1; X < omsHistory.getSize(); X++)
    {
      oms0 = oms1;
      oms1 = omsHistory.getOMS(X);
      
      // by definition, the timestamps must be different
      if(oms0.getTimeStamp().equals(oms1.getTimeStamp()))
        logger.severe("The timestamps of successive OSMs are the same: " + oms0.getTimeStamp().toString());
      
      // the X axis is the set of timestamps
      timeInMillis.add(new Long(oms1.getTimeStamp().getTimeInMillis()));
      
      // now loop through all the ports, in order
      Iterator<HM_Port> it = sortedPorts.iterator();
      int Y = -1;
      while (it.hasNext())
      {
        Y++;
        HM_Port hmp = (HM_Port) it.next();
        // get the ports from these OMS instances
        OSM_Port port0 = oms0.getFabric().getOSM_Port(OSM_Port.getOSM_PortKey(hmp.getPort()));
        OSM_Port port1 = oms1.getFabric().getOSM_Port(OSM_Port.getOSM_PortKey(hmp.getPort()));

        double U = 0;

        if ((port0 != null) && (port1 != null))
        {
          // diff the counters for these ports
          PFM_PortChange pChange = new PFM_PortChange(port0.getPfmPort(), port1.getPfmPort());
          PFM_PortRate pr = new PFM_PortRate(pChange);

          // fabricAnalyzer knows how fast things can theoretically happen
          U = OSM_FabricDeltaAnalyzer.getPortUtilization(pr, PFM_Port.PortCounterName.xmit_data, fabricAnalyzer);
        }
        else
          System.err.println("Missing ports");
        ZValues[X-1][Y] = U;
        maxZvalue = maxZvalue > U ? maxZvalue: U;
      } 
    }


    logger.info("Done calculating Z values: " + new TimeStamp());
    return true;
  }

  private boolean calculateLevels(SortedSet<HM_Port> sortedPorts)
  {
    logger.info("Calculating Level values now : " + new TimeStamp());
    
      // now loop through all the ports, in order
      Iterator<HM_Port> it = sortedPorts.iterator();
      while (it.hasNext())
      {
        HM_Port hmp = (HM_Port) it.next();
        int d = hmp.getDepth();
        
        LevelSize[d]++;
      } 

    logger.info("Done calculating Level values: " + new TimeStamp());
    return true;
  }

  /************************************************************
   * Method Name:
   *  getMaximumXValue
   **/
  /**
   * Describe the method here
   *
   * @see org.jfree.data.general.HeatMapDataset#getMaximumXValue()
   *
   * @return
   ***********************************************************/

  @Override
  public double getMaximumXValue()
  {
    return getXValue(timeInMillis.size()-1);
  }

  /************************************************************
   * Method Name:
   *  getMaximumYValue
   **/
  /**
   * Describe the method here
   *
   * @see org.jfree.data.general.HeatMapDataset#getMaximumYValue()
   *
   * @return
   ***********************************************************/

  @Override
  public double getMaximumYValue()
  {
    return FilteredAndSortedPorts == null ? 0: (double)FilteredAndSortedPorts.size() -1;
  }

  /************************************************************
   * Method Name:
   *  getMaximumYValue
   **/
  /**
   * Describe the method here
   *
   * @see org.jfree.data.general.HeatMapDataset#getMaximumYValue()
   *
   * @return
   ***********************************************************/

  public double getMaximumZValue()
  {
    return maxZvalue;
  }

  /************************************************************
   * Method Name:
   *  getMinimumXValue
   **/
  /**
   * Describe the method here
   *
   * @see org.jfree.data.general.HeatMapDataset#getMinimumXValue()
   *
   * @return
   ***********************************************************/

  @Override
  public double getMinimumXValue()
  {
    return timeInMillis == null ? 0: (double)timeInMillis.get(0);
  }

  /************************************************************
   * Method Name:
   *  getMinimumYValue
   **/
  /**
   * Describe the method here
   *
   * @see org.jfree.data.general.HeatMapDataset#getMinimumYValue()
   *
   * @return
   ***********************************************************/

  @Override
  public double getMinimumYValue()
  {
    return 0;
  }

  /************************************************************
   * Method Name:
   *  getMinimumYValue
   **/
  /**
   * Describe the method here
   *
   * @see org.jfree.data.general.HeatMapDataset#getMinimumYValue()
   *
   * @return
   ***********************************************************/

  public double getMinimumZValue()
  {
    return 0;
  }

  /************************************************************
   * Method Name:
   *  getXSampleCount
   **/
  /**
   * Describe the method here
   *
   * @see org.jfree.data.general.HeatMapDataset#getXSampleCount()
   *
   * @return
   ***********************************************************/

  @Override
  public int getXSampleCount()
  {
    // the number of snapshots in the collection
    return timeInMillis == null ? 0: timeInMillis.size();
  }

  /************************************************************
   * Method Name:
   *  getXValue
   **/
  /**
   * Describe the method here
   *
   * @see org.jfree.data.general.HeatMapDataset#getXValue(int)
   *
   * @param arg0
   * @return
   ***********************************************************/

  @Override
  public double getXValue(int arg0)
  {
    if(timeInMillis != null)
    {
      return arg0 > timeInMillis.size() ? 0: (double)arg0;
    }
    return 0;
  }

  /************************************************************
   * Method Name:
   *  getYSampleCount
   **/
  /**
   * Describe the method here
   *
   * @see org.jfree.data.general.HeatMapDataset#getYSampleCount()
   *
   * @return
   ***********************************************************/

  @Override
  public int getYSampleCount()
  {
    // the number of ports
    return FilteredAndSortedPorts == null ? 0: FilteredAndSortedPorts.size();
  }

  /************************************************************
   * Method Name:
   *  getYValue
   **/
  /**
   * Describe the method here
   *
   * @see org.jfree.data.general.HeatMapDataset#getYValue(int)
   *
   * @param arg0
   * @return
   ***********************************************************/

  @Override
  public double getYValue(int arg0)
  {
    // the Y axis is the port id, so just use the index for now
    if(FilteredAndSortedPorts != null)
    {
      return arg0 > FilteredAndSortedPorts.size() ? 0: (double)arg0;
    }
    return 0;
  }

  /************************************************************
   * Method Name:
   *  getZ
   **/
  /**
   * Describe the method here
   *
   * @see org.jfree.data.general.HeatMapDataset#getZ(int, int)
   *
   * @param arg0
   * @param arg1
   * @return
   ***********************************************************/

  @Override
  public Number getZ(int arg0, int arg1)
  {
    // not used, but necessary
    return null;
  }

  /************************************************************
   * Method Name:
   *  getZValue
   **/
  /**
   * Describe the method here
   *
   * @see org.jfree.data.general.HeatMapDataset#getZValue(int, int)
   *
   * @param arg0
   * @param arg1
   * @return
   ***********************************************************/

  @Override
  public double getZValue(int xIndex, int yIndex)
  {
    // return the utilization value for these coordinates
    // xIndex, is the timestamp index
    // yIndex, is the node + port# index
    // step through each timestamp, and pre-calculate these
    
    // make sure the indecies are okay
    
    // step through each timestamp, and pre-calculate these
    if(xIndex > omsHistory.getSize() -1)
    {
      logger.severe("xindex out of bounds: " + xIndex);
      return 0;
    }
    else if (xIndex < 0)
    {
      logger.severe("xindex out of bounds: " + xIndex);
      return 0;
    }
    else if (yIndex >= FilteredAndSortedPorts.size())
    {
      logger.severe("yindex out of bounds: " + yIndex);
      return 0;
    }
    else if (yIndex < 0)
    {
      logger.severe("yindex out of bounds: " + yIndex);
      return 0;
    }
 
     return ZValues[xIndex][ yIndex];
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
    TimeStamp ts0 = new TimeStamp();
    System.err.println(ts0.toString());
    new PortHeatMapDataSet(SmtCommand.convertSpecialFileName("%h/scripts/OsmScripts/SmtScripts/sierra3H.his"), IB_Depth.IBD_COMPUTE_NODES);
    TimeStamp ts1 = new TimeStamp();
    System.err.println(ts1.toString());
    long delta = ts1.getTimeInSeconds() - ts0.getTimeInSeconds();
    System.err.println("Delta Time: " + delta + " seconds");
  }

  public long getXTimeValue(int xIndex)
  {
  if(timeInMillis != null)
  {
    return xIndex > timeInMillis.size() ? 0: timeInMillis.get(xIndex).longValue();
  }
  return 0;
  }

  public String getPortId(int xIndex)
  {
    // the Y axis is the port id, so just use the index for now
    if(FilteredAndSortedPorts != null)
    {
      HM_Port[] hmA = FilteredAndSortedPorts.toArray(new HM_Port[FilteredAndSortedPorts.size()]);
      if(xIndex < hmA.length)
        return xIndex > FilteredAndSortedPorts.size() ? "unknown": hmA[xIndex].getCompareString();
    }
    return "null";
  }

  public boolean isValid()
  {
    // The collection has to be non-zero in size
    // The max values should be bigger than the minimum
    if((timeInMillis.size() < 1) || (FilteredAndSortedPorts.size() < 1) ||
        !(this.getMaximumXValue() > this.getMinimumYValue()) ||
            !(this.getMaximumZValue() > this.getMinimumZValue()) )
            return false;
    return true;
  }

}
