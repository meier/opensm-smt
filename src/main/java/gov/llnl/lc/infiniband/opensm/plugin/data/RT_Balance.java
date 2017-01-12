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
 *        file: RT_Balance.java
 *
 *  Created on: Apr 16, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;
import gov.llnl.lc.smt.SmtConstants;
import gov.llnl.lc.util.BinList;

/**********************************************************************
 * Describe purpose and responsibility of RT_Balance
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Apr 16, 2015 8:17:19 AM
 **********************************************************************/
public class RT_Balance
{
  public static final Double DEVIATION_LIMIT = 1.8;

  RT_Table                   Table;
  OSM_Fabric                 Fabric;
  LinkedHashMap<String, IB_Vertex> VertexMap;
  int                        NumPortsTotal  = 0;
  int                        NumPortsRoutes = 0;
  int                        NumCaRoutes    = 0;
  
  ArrayList<RT_NodeBalance>         UnBalancedNodes = new ArrayList<RT_NodeBalance>();
  ArrayList<RT_Port>           UnderSubscribedPorts = new ArrayList<RT_Port>();
  ArrayList<RT_Port>            OverSubscribedPorts = new ArrayList<RT_Port>();

  /************************************************************
   * Method Name: RT_Balance
   **/
  /**
   * Describe the constructor here
   *
   * @see describe related java objects
   *
   * @param node
   * @param table
   * @param fabric
   ***********************************************************/
  public RT_Balance(RT_Table table, OSM_Fabric fabric, LinkedHashMap<String, IB_Vertex> vertexMap)
  {
    super();
    Table    = table;
    Fabric   = fabric;
    VertexMap = vertexMap;
    initialize();
  }

  public static BinList<RT_Port> sortBinsByKeys(BinList<RT_Port> caBins)
  {
    BinList<RT_Port> sortedBins = new BinList<RT_Port>();

    // get the keys, sort them, then build the new list
    java.util.Set<String> keySet = caBins.getKeys();

    // copy and sort the keys
    Integer[] kNumArray = new Integer[keySet.size()];

    int ndex = 0;
    for (Iterator<String> sKey = keySet.iterator(); sKey.hasNext();)
    {
      String key = sKey.next();
      kNumArray[ndex++] = Integer.parseInt(key);
    }
    Arrays.sort(kNumArray);

    // use the sorted key array
    for (Integer key : kNumArray)
    {
      String k = Integer.toString(key);
      ArrayList<RT_Port> pList = caBins.getBin(k);
      sortedBins.addBin(pList, k);
    }
    return sortedBins;
  }

  private boolean initialize()
  {
    if ((Table != null) && (Fabric != null))
    {
      // walk through all the nodes, and collect stats
        HashMap<String, RT_Node> NodeRouteMap = Table.getSwitchGuidMap();

        if (NodeRouteMap != null)
        {
          for (Map.Entry<String, RT_Node> entry : NodeRouteMap.entrySet())
          {
            RT_Node rn = entry.getValue();
            RT_NodeBalance nBal = new RT_NodeBalance(rn, Table, Fabric, VertexMap);
            
            ArrayList<Integer> portNums = null;
            if(!nBal.isBalanced())
            {
              // there must be under or over subscribed ports here??
             UnBalancedNodes.add(nBal);
              
             portNums = nBal.getOverSubscribedPorts();
             if(portNums.size() > 0)
               for(int pNum: portNums)
                 OverSubscribedPorts.add(rn.getRT_Port(pNum));
               
             portNums = nBal.getUnderSubscribedPorts();
             if(portNums.size() > 0)
               for(int pNum: portNums)
                 UnderSubscribedPorts.add(rn.getRT_Port(pNum));
            }
            
            
            OSM_Node node = Fabric.getOSM_Node(rn.getGuid());
            // total switch ports
            NumPortsTotal += node.sbnNode.num_ports;
            // total ports with routes
            NumPortsRoutes += rn.getPortRouteMap().size();
            // total CA routes
            NumCaRoutes += nBal.getNumCaRoutes();
          }
        }
        return true;
    }
    return false;
  }

  public String getBalanceReport()
  {
    // for all of the switches, bin up the CA routes
    StringBuffer buff = new StringBuffer();
    
    // loop through all the nodes
    if ((Table != null) && (Fabric != null))
    {
      int numUnBalanced = UnBalancedNodes.size();
      buff.append(String.format("     Balance Report: All Switches to Channel Adapters")+ SmtConstants.NEW_LINE);
      buff.append(SmtConstants.NEW_LINE);
      buff.append(String.format("Fabric Name:                           %20s", Fabric.getFabricName())+ SmtConstants.NEW_LINE);
      buff.append(String.format("timestamp:                             %20s", Table.toTimeString())+ SmtConstants.NEW_LINE);
      buff.append(SmtConstants.NEW_LINE);
      
      buff.append(String.format("routing engine:                        %20s", Table.getRouteEngine()) + SmtConstants.NEW_LINE);
      buff.append(String.format("# switches:                                            %4d", Table.getNumSwitches()) + SmtConstants.NEW_LINE);
      buff.append(String.format("# channel adapters:                                  %6d", Table.getNumChannelAdapters()) + SmtConstants.NEW_LINE);
      buff.append(String.format("average # CA routes/switch:                          %6d", (NumCaRoutes)/Table.getNumSwitches()) + SmtConstants.NEW_LINE);
      buff.append(String.format("# switch ports:                                      %6d", NumPortsTotal) + SmtConstants.NEW_LINE);
      buff.append(String.format("# switch ports with routes:                          %6d", NumPortsRoutes) + SmtConstants.NEW_LINE);
      buff.append(String.format("# routes:                                            %6d", Table.getNumRoutes()) + SmtConstants.NEW_LINE);
      buff.append(String.format("# routes to channel adapters:                        %6d", NumCaRoutes) + SmtConstants.NEW_LINE);
      buff.append("        -------------------------------------------" + SmtConstants.NEW_LINE);
      buff.append(String.format("# balanced switches:                                 %6d", (Table.getNumSwitches() - numUnBalanced)) + SmtConstants.NEW_LINE);
      buff.append(String.format("# unbalanced switches:                               %6d", numUnBalanced) + SmtConstants.NEW_LINE);
      buff.append(String.format("# oversubscribed ports:                              %6d", OverSubscribedPorts.size()) + SmtConstants.NEW_LINE);
      buff.append(String.format("# undersubscribed ports:                             %6d", UnderSubscribedPorts.size()) + SmtConstants.NEW_LINE);

      buff.append("        -----------unbalanced switches-------------" + SmtConstants.NEW_LINE);
      // show all the unbalanced nodes
      for(RT_NodeBalance nb: UnBalancedNodes)
      {
        RT_Node Node = nb.getRT_Node();
        buff.append(String.format(" %20s (%19s lid: %4d)", Node.getName(Fabric), Node.getGuid().toColonString(), Node.getLid()) + SmtConstants.NEW_LINE);
      }
      buff.append("        -------------------------------------------" + SmtConstants.NEW_LINE);
      buff.append(String.format("fabric balanced?:                                     %5s", Boolean.toString(isBalanced())) + SmtConstants.NEW_LINE);
    }
    return buff.toString();
  }

  /************************************************************
   * Method Name:
   *  isBalanced
   **/
  /**
   * Returns the value of balanced
   *
   * @return the balanced
   *
   ***********************************************************/
  
  public boolean isBalanced()
  {
    return (UnBalancedNodes.size() == 0);
  }

}
