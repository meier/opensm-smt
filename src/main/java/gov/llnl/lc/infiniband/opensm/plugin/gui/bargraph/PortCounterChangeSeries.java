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
 *        file: PortCounterChangeSeries.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.bargraph;

import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_FabricDelta;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_FabricDeltaCollection;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_Port.PortCounterName;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_PortChange;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_PortChangeRange;
import gov.llnl.lc.smt.SmtConstants;
import gov.llnl.lc.time.TimeStamp;
import gov.llnl.lc.util.BinList;
import gov.llnl.lc.util.BinValueKeys;
import gov.llnl.lc.util.ValueRange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.jfree.data.category.DefaultCategoryDataset;

public class PortCounterChangeSeries implements BarGraphDataSeries
{
  private DefaultCategoryDataset DataSet;
  private ArrayList <ArrayList <BinList<PFM_PortChange>>> DataSeries;
  private TimeStamp InitialTime;
  private TimeStamp FinalTime;
  private int DeltaSeconds;
  private String FabricName;
  private ArrayList <PortCounterName> CounterNames;
  
  private boolean Valid = false;
  

  
  private ValueRange BinSizeRange;
  private BinValueKeys binValuesAndKeys;  // a set (hashMap) of bin value ranges, for the time series (list is the data)


  /************************************************************
   * Method Name:
   *  copyDataset
  **/
  /**
   * This is used in the animator, as a way to keep everything (labels, ranges, etc)
   * the same from the series, except the data, as you move through the series.
    *
   * @see gov.llnl.lc.infiniband.opensm.plugin.gui.bargraph.BarGraphDataSeries#copyDataset(org.jfree.data.category.DefaultCategoryDataset, int)
   *
   * @param to
   * @param Index
   * @return
   ***********************************************************/
  public boolean copyDataset(DefaultCategoryDataset to, int Index)
  {
    // just copy the values, the structure and names should already match exactly
    // replace the values
    
    ArrayList <BinList<PFM_PortChange>> portChangeArray = DataSeries.get(Index);
    
    /* is there data in this binlist ? */
    if(portChangeArray.size() == 0)
      return false;
    
    BinValueKeys bvk = binValuesAndKeys;  // this contains information about the bins
//    int numFBins = bvk.getNumBins();
    HashMap <String, ValueRange> vrMap = bvk.getBinMap();
    
    int numCounters = to.getRowCount();
//    int numBins = to.getColumnCount();
    
//    int numFCounters = 1;
    
//    System.err.println("Index  : " + Index + ", and is portChange null? " + (portChangeArray == null));
//    System.err.println("To  :  " + numCounters + ", " + numBins);
//    System.err.println("From:  " + numFCounters + ", " + numFBins);
//    System.err.println("FBins: " + numFCounters + ", " + portChangeArray.size());
//       
    // create the dataset...
    for(int rw = 0; rw < numCounters; rw++)
    {
      int c = 0;
      for(Entry<String, ValueRange> mapEntry: vrMap.entrySet())
      {
//        ValueRange r = mapEntry.getValue();
        String key   = mapEntry.getKey();
        Number value = 0;
        Comparable rowKey    = to.getRowKey(rw);
        Comparable columnKey = to.getColumnKey(c);

        // check to see if any data (in the first binlist) exists in this bin
        BinList<PFM_PortChange> bl = portChangeArray.get(rw);
        if((bl != null) && (bl.size() > 0))
        {
          ArrayList <PFM_PortChange> aPC = bl.getBin(key);
          if(aPC != null)
            value = aPC.size();
        
//      value, series name, label
        to.setValue(value, rowKey, columnKey);
          
        }
        c++;
      }
    }
    return true;
  }
  
  public String getSeriesLabel(int Index)
  {
    // in this case, the series label is simply the bins timestamp
    ArrayList <BinList<PFM_PortChange>> pcBinArray = DataSeries.get(Index);
    
    /* is there data in this binlist ? */
    if(pcBinArray.size() == 0)
      return null;
     
    // get the timestamp from this set of bins, any one will do
    return pcBinArray.get(0).getBin(0).get(0).getCounterTimeStamp().toString();
//    return pcBin.getBin(0).get(0).getCounterTimeStamp().toString();
  }
  
 
 
  public PortCounterChangeSeries( OSM_FabricDeltaCollection history, ArrayList <PortCounterName> counterNames, int numBins, boolean excludeZeroBin)
  {
    OSM_FabricDelta oDelta = history.getOldestOSM_FabricDelta();
    OSM_FabricDelta cDelta = history.getCurrentOSM_FabricDelta();
    
    FabricName = cDelta.getFabricName();
    InitialTime = oDelta.getTimeStamp();
    FinalTime = cDelta.getTimeStamp();
    CounterNames = counterNames;
    DeltaSeconds = (int) oDelta.getAgeDifference(TimeUnit.SECONDS);

    createDataSeries(history, numBins, excludeZeroBin);
    
    Valid = createDataset();
   }
  
  

  private boolean createDataSeries( OSM_FabricDeltaCollection history, int numBins, boolean excludeZeroBin)
 {
   PFM_PortChangeRange pcr = history.getRangeOfChanges();
   
   // two different scales depending on counter type
   long scale        = SmtConstants.MEGABYTES * this.getDeltaSeconds();;
   
   Long allTimeMaxBinSize = 0L;
   long maxOfAll = 0L;
   for(PortCounterName pcn: CounterNames)
   {
     System.err.println("The maximum " + pcn + " is from "+ pcr.getMaxPortCounterPortDescription(pcn) + " = " + pcr.getMaxPortCounterValue(pcn));
     maxOfAll = pcr.getMaxPortCounterValue(pcn) > maxOfAll ? pcr.getMaxPortCounterValue(pcn): maxOfAll;
//     System.err.println("The maximum of all is = " + maxOfAll);
        }
   BinValueKeys bvk = new BinValueKeys(maxOfAll, 0, scale, numBins, true, true);
   
     // just show the bins, there must be at least one, or we are done!
   if(bvk.getBinMap() == null)
     return false;
          
 //  System.err.println("The BinMap keys (" + excludeZeroBin + ")");
   for(Entry<String, ValueRange> mapEntry: bvk.getBinMap().entrySet())
   {
   //  System.err.println(mapEntry.getKey());
        if(mapEntry.getKey().equalsIgnoreCase("0 to 0"))
         System.err.println("Invalid Bin key, FIXME");
    }
   
   // create the bins from these keys, and stuff the data!
   
   // for each time slice in the history, populate the bins
   LinkedHashMap<String, OSM_FabricDelta> fabricsAll = history.getOSM_FabricDeltas();
   
   
   ArrayList <ArrayList <BinList<PFM_PortChange>>> TimeSeriesPortBins = new ArrayList<ArrayList <BinList<PFM_PortChange>>>();
   /*
    * The outer list is the time series
    *   the next list is the port counter name
    *      the binList is an array of bins for the counter changes
    *         (the bins key is based on the rate of change of the counter)
    *         (so the size of the bin, is the number of counters changing at a similar rate)
    *         (each entry in the binlist is the portchange object)
    */
   
   
   // iterate through the collection, and examine the named port counters
   for (Map.Entry<String, OSM_FabricDelta> deltaMapEntry : fabricsAll.entrySet())
   {
     ArrayList <BinList<PFM_PortChange>> portCounterChangeBinList = new ArrayList <BinList<PFM_PortChange>>();
     for(PortCounterName pcn: CounterNames)
     {
       // initialize the bin list for each time stamp, and for each counter
       portCounterChangeBinList.add(new BinList<PFM_PortChange>());
     }

     // I have a timestamped instance of the (delta) fabric, get the port counters
     // for this instance, and put them in the prededermined bins
     
     // only look through the ports with change
     HashMap<String, PFM_PortChange> portChanges = deltaMapEntry.getValue().getPortsWithChange();
     
     // each instance of portChanges, is for a single timestamp
     boolean tsInitialized = false;
     for (Map.Entry<String, PFM_PortChange> changeMapEntry : portChanges.entrySet())
     {
       // just looking for a specific port counter
       PFM_PortChange pc = changeMapEntry.getValue();

       int ndex = 0;
       Long maxSize = 0L;
       for(PortCounterName pcn: CounterNames)
       {
         BinList<PFM_PortChange> portChangeBins = portCounterChangeBinList.get(ndex);
         
         // determine which bin this goes in by its value
         long value = pc.getDelta_port_counter(pcn);
         
         // scale this up, if the counter is packet based
         if(PortCounterName.PFM_PACKET_COUNTERS.contains(pcn))
           value *= SmtConstants.PACKET_SIZE;
             
         String binKey = bvk.getKey(value);
         
         // FIXME is this a valid bin key?
         if((binKey != null) && (!excludeZeroBin || !tsInitialized || !binKey.startsWith(BinValueKeys.ZERO_BIN_KEY)))
         {
             if(binKey.startsWith(BinValueKeys.ZERO_BIN_KEY) && excludeZeroBin && tsInitialized)
             {
               continue;
             }
             portChangeBins.add(pc, binKey);
             tsInitialized = true;
         }
         Long totalSize = portChangeBins.getTotalBinSizes();
         maxSize   = portChangeBins.getMaxBinSize();
         
//
//         System.err.println(portChangeBins.toString());
//         System.err.println(pcn.getName() +": Max Bin Size: " + maxSize + ", Total size of all bins = "  + totalSize);
//
//         
         ndex++;
         allTimeMaxBinSize = allTimeMaxBinSize < maxSize ? maxSize : allTimeMaxBinSize;
       }
       // done for this counter
       
     }
     // done bin'ing up for this timestamp
     // show the sizes of the bins
     
     TimeSeriesPortBins.add(portCounterChangeBinList);
   }
   System.err.println("Max All Time Bin Size: " + allTimeMaxBinSize );
   System.err.println("Time Series Size: " + TimeSeriesPortBins.size() );
   
   // all done, save the info
   this.DataSeries = TimeSeriesPortBins;
   this.BinSizeRange = new ValueRange(allTimeMaxBinSize, 0L, 1L);
   this.binValuesAndKeys = bvk;
   
   return true;
 }
 
  private boolean createDataset()
  {
    // from the BinList of PortChanges, create the DataSet
    
    ArrayList <BinList<PFM_PortChange>> portChangeArray = DataSeries.get(0);
    BinValueKeys bvk = binValuesAndKeys;
    
    // create the dataset...
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    HashMap <String, ValueRange> vrMap = bvk.getBinMap();
    
    int ndex = 0;
    for(PortCounterName pcn: CounterNames)
    {
      String seriesName = pcn.name();
      BinList<PFM_PortChange> portChanges = portChangeArray.get(ndex);
      
      for(Entry<String, ValueRange> mapEntry: vrMap.entrySet())
      {
        ValueRange r = mapEntry.getValue();
        String key   = mapEntry.getKey();
        String label   = r.getLabel();
        Number value = 0;
        
        // check to see if any data (in the first binlist) exists in this bin
        ArrayList <PFM_PortChange> aPC = portChanges.getBin(key);
        if(aPC != null)
        {
          value = aPC.size();
        }
        
        dataset.addValue(value, seriesName, label);
      }
      ndex++;
    }
    
    this.DataSet = dataset;
    return true;
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
  
  public DefaultCategoryDataset getDataSet(int Index)
  {
    return DataSet;
  }

  /************************************************************
   * Method Name:
   *  getTitle
   **/
  /**
   * Returns the value of title
   *
   * @return the title
   *
   ***********************************************************/
  
  public String getTitle()
  {
    return getFabricName();
  }

  /************************************************************
   * Method Name:
   *  getDomainLabel
   **/
  /**
   * Returns the value of domainLabel, which in this case is the
   * rate of change.
   *
   * @return the domainLabel
   *
   ***********************************************************/
  
  public String getDomainLabel()
  {
    return "MB per sec";
  }

  /************************************************************
   * Method Name:
   *  getRangeLabel
   **/
  /**
   * Returns the value of rangeLabel, which in this case is the
   * number of ports in each bin, or the bin size
   *
   * @return the rangeLabel
   *
   ***********************************************************/
  
  public String getRangeLabel()
  {
    return "number of ports";
  }

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
  
  public int getNumInSeries()
  {
    return DataSeries != null ? DataSeries.size(): 0;
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

  @Override
  public String getGraphTitle()
  {
    // TODO Auto-generated method stub
    return "OMS PortCounter";
  }

  @Override
  public int getDeltaSeconds()
  {
    return DeltaSeconds;
  }

  @Override
  public ArrayList <ValueRange> getRangeValueRanges()
  {
    ArrayList <ValueRange> rtn = new ArrayList <ValueRange>();
    rtn.add(BinSizeRange);
    return rtn;
  }

  @Override
  public ArrayList <ValueRange> getDomainValueRanges()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isValid()
  {
    return Valid;
  }

}
