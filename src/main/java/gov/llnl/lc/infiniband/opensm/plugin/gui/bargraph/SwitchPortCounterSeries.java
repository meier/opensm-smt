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
 *        file: SwitchPortCounterSeries.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.bargraph;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jfree.data.category.DefaultCategoryDataset;

import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_FabricDelta;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_FabricDeltaCollection;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Node;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_Port.PortCounterName;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_PortChange;
import gov.llnl.lc.smt.SmtConstants;
import gov.llnl.lc.time.TimeStamp;
import gov.llnl.lc.util.BinList;
import gov.llnl.lc.util.ValueRange;

/**********************************************************************
 * Describe purpose and responsibility of SwitchPortCounter
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version May 30, 2013 4:26:36 PM
 **********************************************************************/
public class SwitchPortCounterSeries implements BarGraphDataSeries
{

  private DefaultCategoryDataset DataSet;
  private OSM_Node SwitchNode;                      // the switch of interest
  private ArrayList <OSM_Port> PortList;            // the switches list of ports
  private ArrayList <BinList<PFM_PortChange>> DataSeries;
  private TimeStamp InitialTime;
  private TimeStamp FinalTime;
  private int DeltaSeconds;
  private ArrayList <ValueRange> CounterRangeList;
  private String FabricName;
  private ArrayList <PortCounterName> CounterNames;
  
  private boolean Valid = false;
  
  private boolean createDataSeries(OSM_FabricDeltaCollection history)
  {
    // initialize a set of maximums
    CounterRangeList = new ArrayList<ValueRange>();
    ArrayList <Long> maxCounterRanges = new ArrayList<Long>();
    for(PortCounterName pcn: CounterNames)
      maxCounterRanges.add(new Long(0));
 
    // this internal method is only called if all dependencies are satisfied
    
    // given the switch node and the list of its ports, create the DataSeries which
    // represents a collection (over time) of a collection (the ports) of PortChange Objects
    
    // for each time slice in the history, populate the bins
    LinkedHashMap<String, OSM_FabricDelta> fabricsAll = history.getOSM_FabricDeltas();
    
    // this is the output, create this and set it to DataSeries
    ArrayList <BinList<PFM_PortChange>> TimeSeriesPortBins = new ArrayList <BinList<PFM_PortChange>>();
    
    
    // iterate through the collection, and examine just the named port counters
    for (Map.Entry<String, OSM_FabricDelta> deltaMapEntry : fabricsAll.entrySet())
    {
      BinList<PFM_PortChange> portChangeBins = new BinList<PFM_PortChange>();

      // I have a timestamped instance of the (delta) fabric, get the port counters
      // for this instance, and for my switch node, and put them in the port bins
      
      // get the portchanges for just the desired switch node
      LinkedHashMap<String, PFM_PortChange> portChanges = deltaMapEntry.getValue().getPortChangesFromNode(SwitchNode);
      
      // each instance of portChanges, is for a single timestamp
      for (Map.Entry<String, PFM_PortChange> changeMapEntry : portChanges.entrySet())
      {
        // just looking for a specific port counter
        PFM_PortChange pc = changeMapEntry.getValue();
        
        // set max for each counter type
        int ndex = 0;
        for(PortCounterName pcn: CounterNames)
        {
          long val = pc.getDelta_port_counter(pcn);
          // update the maximum value if necessary
          if(maxCounterRanges.get(ndex) == null)
          {
            // this should never happen
            System.err.println("SwithchPortDataSeries null error");
            System.exit(-1);
          }
          if(maxCounterRanges.get(ndex).longValue() < val)
            maxCounterRanges.set(ndex, new Long(val));
          
          ndex++;
        }
        // put the portchange in the bin keyed off the portNumber + level?
 
        // determine which bin this goes in by its value
        short pn = pc.getPort1().port_num;
        String binKey = new Short(pn).toString();
        
        if(binKey != null)
          portChangeBins.add(pc, binKey);
      }
      // done bin'ing up for this timestamp
      
      TimeSeriesPortBins.add(portChangeBins);
    }
    for(Long max: maxCounterRanges)
    {
      CounterRangeList.add(new ValueRange(max.longValue(), 0L, 1L));
    }
    DataSeries = TimeSeriesPortBins;
    return true;
  }
 
  
  private boolean createDataset()
  {
    // create the initial dataset to fix the structure of the graph
    // so animations will be smooth
    
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    copyDataset(dataset, 0);
    this.DataSet = dataset;

    return true;
  }
  
  /************************************************************
   * Method Name:
   *  getGraphTitle
   **/
  /**
   * Describe the method here
   *
   * @see gov.llnl.lc.infiniband.graph.test.BarGraphDataSeries#getGraphTitle()
   *
   * @return
   ***********************************************************/

  @Override
  public String getGraphTitle()
  {
    
    return "OMS Switch Port Activity";
  }

  /************************************************************
   * Method Name:
   *  getTitle
   **/
  /**
   * Describe the method here
   *
   * @see gov.llnl.lc.infiniband.graph.test.BarGraphDataSeries#getTitle()
   *
   * @return
   ***********************************************************/

  @Override
  public String getTitle()
  {
    return SwitchNode.sbnNode.description;
  }

  /************************************************************
   * Method Name:
   *  getSeriesLabel
   **/
  /**
   * Describe the method here
   *
   * @see gov.llnl.lc.infiniband.graph.test.BarGraphDataSeries#getSeriesLabel(int)
   *
   * @param seriesIndex
   * @return
   ***********************************************************/

  @Override
  public String getSeriesLabel(int seriesIndex)
  {
    // in this case, the series label is simply the bins timestamp
    BinList<PFM_PortChange> pcBin = DataSeries.get(seriesIndex);
    
    /* is there data in this binlist ? */
    if(pcBin.size() == 0)
      return null;
     
    // get the timestamp from this set of bins, any one will do
    return pcBin.getBin(0).get(0).getCounterTimeStamp().toString();
  }

  /************************************************************
   * Method Name:
   *  getDomainLabel
   **/
  /**
   * Describe the method here
   *
   * @see gov.llnl.lc.infiniband.graph.test.BarGraphDataSeries#getDomainLabel()
   *
   * @return
   ***********************************************************/

  @Override
  public String getDomainLabel()
  {
    return "Port Number (level)";
  }

  /************************************************************
   * Method Name:
   *  getRangeLabel
   **/
  /**
   * Describe the method here
   *
   * @see gov.llnl.lc.infiniband.graph.test.BarGraphDataSeries#getRangeLabel()
   *
   * @return
   ***********************************************************/

  @Override
  public String getRangeLabel()
  {
    return "MB per sec";
  }
  
  public ArrayList <ValueRange> getRangeValueRanges()
  {
   return CounterRangeList; 
  }
  
  public ArrayList <ValueRange> getDomainValueRanges()
  {
    // return the min and max value for the port number in this series
    
    return null;
  }

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
   *  copyDataset
   **/
  /**
   * This is used in the animator, as a way to keep everything (labels, ranges, etc)
   * the same from the series, except the data, as you move through the series.
   *
   * @see gov.llnl.lc.infiniband.graph.test.BarGraphDataSeries#copyDataset(org.jfree.data.category.DefaultCategoryDataset, int)
   *
   * @param to
   * @param fromIndex
   * @return
   ***********************************************************/

  @Override
  public boolean copyDataset(DefaultCategoryDataset to, int fromIndex)
  {
    // just copy the values, the structure and names should already match exactly
    // replace the values
    
    BinList<PFM_PortChange> portChanges = DataSeries.get(fromIndex);
    
    /* is there data in this binlist ? */
    if((portChanges.size() == 0) || (to == null))
      return false;
    
    int numPorts = PortList.size();
    
    // iterate through the counters and fix the ValueRange (scale, etc)
    int ndex = 0;
    for(PortCounterName pcn: CounterNames)
    {
      String seriesName = pcn.name();
      
      // if this is a traffic counter, scale to MEGABYTES per Second
      //  otherwise just leave at Counts per Second
      long units = PortCounterName.PFM_DATA_COUNTERS.contains(pcn) ? SmtConstants.MEGABYTES: 1L;
      units = PortCounterName.PFM_PACKET_COUNTERS.contains(pcn) ? SmtConstants.MEGABYTES/SmtConstants.PACKET_SIZE: units;
      long scale = getDeltaSeconds() * units;
      if(scale < 1)
        scale = 1L;
      
      if(fromIndex == 0)
      {
        ValueRange vr = CounterRangeList.get(ndex);
        ValueRange nvr = null;
       
        if(vr == null)
        {
          nvr = new ValueRange(0,0,scale);
        }
        else
        {
          nvr = new ValueRange(vr.getMax(), vr.getMin(), scale);
        }
        // now replace it with the scaled version
        CounterRangeList.set(ndex, nvr);
        
      }
      
      // add the individual bars - from the bins, which are just the ports
      for(int portNum = 1; portNum <= numPorts; portNum++)
      {
        // the port number is the key
        String key = new Integer(portNum).toString();

        String label   = key;
        Number value = 0;
        
        // check to see if any data (in the first binlist) exists in this bin
        ArrayList <PFM_PortChange> aPC = portChanges.getBin(key);
        if((aPC != null) && (aPC.size() > 0))
        {
          // there should only be a single entry in this bin, but I don't care if there is more
          // just get the first one
          value = aPC.get(0).getDelta_port_counter(pcn)/CounterRangeList.get(ndex).getScale();
        }
        else
        {
          value = 0L;
        }
        to.setValue(value, seriesName, label);
      }
       ndex++; // finally increment the counter name index
    }
    
    return true;
  }

  
  public SwitchPortCounterSeries( OSM_FabricDeltaCollection history, IB_Guid nodeGuid, ArrayList <PortCounterName> counterNames)
  {
    OSM_FabricDelta oDelta = history.getOldestOSM_FabricDelta();
    OSM_FabricDelta cDelta = history.getCurrentOSM_FabricDelta();
    
    FabricName = cDelta.getFabricName();
    InitialTime = oDelta.getTimeStamp();
    FinalTime = cDelta.getTimeStamp();
    CounterNames = counterNames;
    DeltaSeconds = (int) oDelta.getAgeDifference(TimeUnit.SECONDS);

    // find a single switch in this fabric that matches the supplied nodeGuid
    // find out how many ports it has, and thats the number of bins I need
    // The name of the bin can be the switch guid + the port number, or just the port number
    //
    // The bin contains just the counter value, nothing else
    //
    // For each port number, find out if its an uplink port or a downlink port
    //   do this by assigning a number to all ports/links from the bottom up
    //   a particular port is an uplink, if the remote port has the same or greater value
    //   a particular port is a downlink, if the remote port has a lower value
    //   (this should be static, so just calculate it once, on the current delta)
    //
    
    OSM_Fabric f = cDelta.getFabric1();
    OSM_Node n   = f.getOSM_Nodes().get(OSM_Fabric.getOSM_NodeKey(nodeGuid.getGuid()));
    LinkedHashMap <String, OSM_Port> pMap = f.getOSM_Ports();
    
    if(n != null)
    {
      SwitchNode = n;
      System.err.println("node: " + n.toString());
    }
    else
      System.err.println("Couldn't find: " + nodeGuid.toColonString());
    
    // find all this nodes ports, in this fabric
    if((pMap != null) && (n != null))
    {
      // print out the ports of this switch
      ArrayList <OSM_Port> aP = n.getOSM_Ports(pMap);
      if((aP == null) || (aP.size() < 1))
      {
        System.err.println("The port array list is either null or empty");
        System.exit(0);
      }
      else
      {
        // have the ports for this node/switch
        PortList = aP;
        int i = 1;
        for(OSM_Port p: aP)
        {
          System.err.println("Port " + i++ + ": " + p);
        }
        
        // build the DataSeries
        if(createDataSeries(history))
        {
        // build the DataSet
        Valid = createDataset();
        }
      }
    }
    else
    {
      System.err.println("Could not get the ports in the fabric or the node for this switch");
    }    
  }


  
  
  /************************************************************
   * Method Name:
   *  isValid
   **/
  /**
   * Returns the value of valid
   *
   * @return the valid
   *
   ***********************************************************/
  
  public boolean isValid()
  {
    return Valid;
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


  @Override
  public int getDeltaSeconds()
  {
    // TODO Auto-generated method stub
    return DeltaSeconds;
  }
}
