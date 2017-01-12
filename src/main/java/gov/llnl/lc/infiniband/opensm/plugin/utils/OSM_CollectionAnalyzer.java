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
 *        file: OMS_CollectionAnalyzer.java
 *
 *  Created on: May 14, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_FabricDelta;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_FabricDeltaCollection;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_Port.PortCounterName;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_PortChange;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_PortChangeComparator;


/**********************************************************************
 * A helper class that provides methods that operate on OSM_FabricCollection
 * and OSM_FabricDeltaCollection objects.
 * 
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version May 14, 2013 8:44:16 AM
 **********************************************************************/
public class OSM_CollectionAnalyzer implements gov.llnl.lc.logging.CommonLogger
{
  
   public static ArrayList<PFM_PortChange> getTopChangingPortCounter(OSM_FabricDeltaCollection history, PortCounterName countername, int numToReturn)
  {
    LinkedHashMap<String, OSM_FabricDelta> fabricsAll = history.getOSM_FabricDeltas();

    HashMap<String, PFM_PortChange> portChangers = new HashMap<String, PFM_PortChange>();

    // iterate through the collection, and examine just the named port counter
    for (Map.Entry<String, OSM_FabricDelta> deltaMapEntry : fabricsAll.entrySet())
    {
      // only look through the ports with change
      HashMap<String, PFM_PortChange> portChanges = deltaMapEntry.getValue().getPortsWithChange();
      for (Map.Entry<String, PFM_PortChange> changeMapEntry : portChanges.entrySet())
      {
        // just looking for a specific port counter
        PFM_PortChange pc = changeMapEntry.getValue();
        PFM_Port p = pc.getPort1();

        // identify the ports with the highest changes
        long value = pc.getDelta_port_counter(countername);
        String portKey = p.toPFM_ID_String() + " " + countername;

        if (portChangers.get(portKey) == null)
          portChangers.put(portKey, pc);
        else
        {
          // replace only if the value is larger
          PFM_PortChange maxC = portChangers.get(portKey);
          long maxVal = maxC.getDelta_port_counter(countername);
          if (value > maxVal)
            portChangers.put(portKey, pc);
        }
      }
    }

    // now build a list with only the top ones (sort by
    // PFM_PortChange.getDelta_port_counter(countername)
    ArrayList<PFM_PortChange> pcList = new ArrayList<PFM_PortChange>();

    for (Map.Entry<String, PFM_PortChange> cMapEntry : portChangers.entrySet())
    {
      PFM_PortChange tpc = cMapEntry.getValue();
      pcList.add(tpc);

    }
    PFM_PortChangeComparator pcCompare = new PFM_PortChangeComparator(countername);

    Collections.sort(pcList, pcCompare);
    Collections.reverse(pcList);

    // assume they are sorted, copy the desired amount
    ArrayList<PFM_PortChange> rtnList = new ArrayList<PFM_PortChange>();

    int c = 0;
    for (PFM_PortChange ppc : pcList)
    {
      c++;
      rtnList.add(ppc);
      if (c >= numToReturn)
        break;
    }
    return rtnList;
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
   * @throws  
   * @throws IOException 
   * @throws FileNotFoundException 
   ***********************************************************/
  public static void main(String[] args) throws FileNotFoundException, IOException, Exception
  {
    OSM_FabricDeltaCollection fabricHistory = OSM_FabricDeltaCollection.readFabricDeltaCollection("/home/meier3/omsRepo/vrelic/vrelic627-2.hst");
    System.err.println(fabricHistory.toString());
    
    PortCounterName name = PortCounterName.xmit_data;
    int numToReturn = 20;
    ArrayList <PFM_PortChange>pcList = OSM_CollectionAnalyzer.getTopChangingPortCounter(fabricHistory, name, numToReturn);
    
    int c = 0;
    for(PFM_PortChange ppc: pcList)
    {
      PFM_Port p = ppc.getPort1();
      
      // identify the ports with the highest changes
      long value = ppc.getDelta_port_counter(name);
      String portKey = p.toPFM_ID_String()+ " " + name;
      System.err.println(c++ + " " + portKey + ", value: " + value);
    }
  }

}
