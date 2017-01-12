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
 *        file: RT_NodeBalance.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;
import gov.llnl.lc.smt.SmtConstants;
import gov.llnl.lc.util.BinList;

/**********************************************************************
 * A Nodes routing table is considered to be balanced, if the following
 * conditions are satisfied;
 * 
 * The number of down links is approximately the same as the number of up links
 * (an exception is allowed for the top level switches)
 * 
 * The number of routes to CA nodes in each down link is roughly the same
 * 
 * Up links depend on the nodes depth.  TBD
 * 
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version May 18, 2015 10:07:50 AM
 **********************************************************************/
public class RT_NodeBalance
{
  RT_Node                    Node;
  IB_Vertex                  Vertex;
  RT_Table                   Table;
  OSM_Fabric                 Fabric;
  LinkedHashMap<String, IB_Vertex> VertexMap;
  int                        NumPortsTotal  = 0;
  int                        NumPortsRoutes = 0;
  int                        NumCaRoutes    = 0;

  /**
   * a List of Bins, containing ports with the same # of routes to a CA (sorted)
   **/
  BinList<RT_Port>           UpCA_Bins         = new BinList<RT_Port>();
  BinList<RT_Port>           DownCA_Bins       = new BinList<RT_Port>();
    
  ArrayList<Integer>         UnderSubscribedPorts = new ArrayList<Integer>();
  ArrayList<Integer>         OverSubscribedPorts  = new ArrayList<Integer>();
  
//  /**
//   * # ports that have a single route to a CA (leaf switch port) this should
//   * exactly match # CAs
//   **/
//  int                        NumSingleRoutes = 0;
//
  /**
   * stats for the number of routes to a CA for each port (excluding leaf switch
   * ports)
   **/
  SummaryStatistics          UpStats      = new SummaryStatistics();
  SummaryStatistics          DownStats    = new SummaryStatistics();

  /************************************************************
   * Method Name:
   *  RT_NodeBalance
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param node
   * @param table
   * @param fabric
   * @param vertexMap
   ***********************************************************/
  public RT_NodeBalance(RT_Node node, RT_Table table, OSM_Fabric fabric, LinkedHashMap<String, IB_Vertex> vertexMap)
  {
    super();
    Node     = node;
    Table    = table;
    Fabric   = fabric;
    VertexMap = vertexMap;
    initialize();
  }

  
  private SummaryStatistics calculateStatistics(BinList<RT_Port> caBins)
  {
    // sort these by the # of CA paths
    BinList<RT_Port>      CA_Bins        = RT_Balance.sortBinsByKeys(caBins);
    SummaryStatistics     RouteStats     = new SummaryStatistics();

    int k = 0;
    for (ArrayList<RT_Port> caList : CA_Bins)
    {
      if (caList != null)
      {
        String key = (CA_Bins.getKey(k)).trim();
        NumPortsRoutes += caList.size();
        
        for (int i = 0; i < caList.size(); i++)
        {
          RouteStats.addValue(Double.parseDouble(key));
        }


//        // excluding single paths (switch to leaf nodes), calculate the
//        // statistics
//        if ("1".equalsIgnoreCase(key))
//        {
//          // sanity check, these are ports connected directly to CAs, so
//          // should match # of CAs
//          NumSingleRoutes = caList.size();
//        }
//        else
//        {
//          // include these in the stats, the ports connected to compute
//          // (leaf or CA) nodes will throw off stats
//          for (int i = 0; i < caList.size(); i++)
//          {
//            RouteStats.addValue(Double.parseDouble(key));
//          }
//        }
      }
      k++;
    }
    return RouteStats;
  }
  
  private boolean getPortsExceedingSubscription(BinList<RT_Port> caBins, SummaryStatistics stats)
  {
    // create the normal boundaries, anything outside of these are considered "out of balance"
    double stdDev   = stats.getStandardDeviation() > RT_Balance.DEVIATION_LIMIT ? stats.getStandardDeviation(): RT_Balance.DEVIATION_LIMIT;
    double maxPaths = stats.getMean() + stdDev;
    double minPaths = stats.getMean() - stdDev;
    
    int k = 0;
    for (ArrayList<RT_Port> caList : caBins)
    {
      if (caList != null)
      {
        double nPaths = Double.parseDouble((caBins.getKey(k)).trim());
        if(nPaths > maxPaths)
        {
          // add all of these port numbers to the OverSubscribed list
          for (RT_Port p: caList)
            OverSubscribedPorts.add(p.getPortNumber());
        }
        else if(nPaths < minPaths)
        {
          // add all of these port numbers to the UnderSubscribed list
          for (RT_Port p: caList)
            UnderSubscribedPorts.add(p.getPortNumber());
        }
      }
      k++;
    }
    return true;
  }
  
  private boolean initialize()
  {
    boolean balanced = false;
    
    // if Node == null, then this balance is for the entire Table, not just a switch
    if ((Table != null) && (Fabric != null) && (VertexMap != null))
    {
      if(Node != null)
      {
        // find the matching IB_Vertex for this node
        Vertex = VertexMap.get(IB_Vertex.getVertexKey(Node.getGuid()));
        NumCaRoutes = Node.getNumCaRoutes(Table);
        
        LinkedHashMap<String, RT_Port> PortRouteMap = RT_Node.sortPortRouteTable(Node.getPortRouteMap(), true);
        if (PortRouteMap != null)
        {
          OSM_Node node = Fabric.getOSM_Node(Node.getGuid());
          NumPortsTotal += node.sbnNode.num_ports;
          
          // bin up the ports by the number of (non-zero) CA routes
          for (Map.Entry<String, RT_Port> entry : PortRouteMap.entrySet())
          {
            RT_Port rp = entry.getValue();
            int nCAs = rp.getNumCaRoutes(Table);
            if (nCAs > 0)
            {
              if(Vertex.isDownLink(rp.getPortNumber()))
                DownCA_Bins.add(rp, Integer.toString(nCAs));
              else if(Vertex.isUpLink(rp.getPortNumber()))
                UpCA_Bins.add(rp, Integer.toString(nCAs));
              else
              {
                System.err.println("Unknow type of Port (not up or down)");
                System.err.println(rp.getPortNumber());
                System.err.println(rp.toIB_RouteString(Table, Fabric));
              }
            }        
           }
          
          // now I have the up and down CA Bins, calculate their stats seperately
          UpStats   = calculateStatistics(UpCA_Bins);
          DownStats = calculateStatistics(DownCA_Bins);
          getPortsExceedingSubscription(UpCA_Bins, UpStats);
          getPortsExceedingSubscription(DownCA_Bins, DownStats);
         }
//
//        System.err.println("**************************************");
//        System.err.println("Levels: " + NumFabricLevels + ", this node level: " + NodeLevel);
//        System.err.println("U/D  Balanced: " + isUpDownBalanced());
//        System.err.println("Down Balanced: " + isDownLinksBalanced());
//        System.err.println("Up   Balanced: " + isUpLinksBalanced());
//        System.err.println("Up   average: " + UpStats.getMean() + ", and std dev: " + UpStats.getStandardDeviation());
//        System.err.println("Down average: " + DownStats.getMean() + ", and std dev: " + DownStats.getStandardDeviation());
//        System.err.println("Num Under Subscribed: " + UnderSubscribedPorts.size() + ", " + UnderSubscribedPorts.toString());
//        System.err.println("Num Over Subscribed: " + OverSubscribedPorts.size() + ", " + OverSubscribedPorts.toString());
//        System.err.println("Balanced: " + isBalanced());
//        System.err.println("**************************************");
//
       }
      
     }
//    System.err.println("Up Bins: " + UpCA_Bins.size() + ", balanced? " + isBinListBalanced(UpCA_Bins));
//    System.err.println("Down Bins: " + DownCA_Bins.size() + ", balanced? " + isBinListBalanced(DownCA_Bins));
//    
//
    return balanced;
  }
//
//  public static String getBalanceReport(RT_Table table, OSM_Fabric fabric)
//  {
//
//    // for all of the switches, bin up the CA routes
//    StringBuffer buff = new StringBuffer();
//    
//    // loop through all the nodes
//    // for all of the switches, bin up the CA routes
//    HashMap<String, RT_Node> NodeRouteMap = table.getSwitchGuidMap();
//
//    if (NodeRouteMap != null)
//    {
//      int numBalanced = 0;
//      buff.append(String.format("     Balance Report: All Switches to Channel Adapters")+ SmtConstants.NEW_LINE);
//      buff.append(SmtConstants.NEW_LINE);
//      buff.append(String.format("Fabric Name:                           %20s", fabric.getFabricName())+ SmtConstants.NEW_LINE);
//      buff.append(String.format("timestamp:                             %20s", table.toTimeString())+ SmtConstants.NEW_LINE);
//      buff.append(SmtConstants.NEW_LINE);
//      
//      buff.append(String.format("routing engine:                        %20s", table.getRouteEngine()) + SmtConstants.NEW_LINE);
//      buff.append(String.format("# switches:                                            %4d", table.getNumSwitches()) + SmtConstants.NEW_LINE);
//      buff.append(String.format("# channel adapters:                                  %6d", table.getNumChannelAdapters()) + SmtConstants.NEW_LINE);
//      buff.append(String.format("# routes:                                            %6d", table.getNumRoutes()) + SmtConstants.NEW_LINE);
//
//      int k = 0;
//      for (Map.Entry<String, RT_Node> entry : NodeRouteMap.entrySet())
//      {
//        RT_Node rn = entry.getValue();
//        
////        RT_NodeBalance swBalance = new RT_NodeBalance(rn, table, fabric);
//        
//        // keep track of the number of balanced and not balanced
//        buff.append(++k + ") -----------------------" + SmtConstants.NEW_LINE);
////        buff.append(swBalance.getShortBalanceReport());
////        if(swBalance.isBalanced())
//          numBalanced++;
//      }
//      buff.append("-------------------------" + SmtConstants.NEW_LINE);
//      buff.append("total balanced: " + numBalanced + ", total unbalanced: " + (k-numBalanced) + SmtConstants.NEW_LINE);
//
//    }
//    
//    return buff.toString();
//  }
//  
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
  
  protected boolean isUpDownBalanced()
  {
    // unless this is a top level switch, the up and down links need to
    // be similar
    if(getNodeLevel() == getMaxFabricLevel())
      return true;
    
      int ul = Vertex.getUpLinkNumbers().size();  // the # of up links for node "n"
      int dl = Vertex.getDownLinkNumbers().size();
      
      // allow same, or up to 1/2 of down
      int minUp = dl/2;
      int maxUp = dl + 3;
      if((ul > minUp) && (ul < maxUp))
        return true;

    return false;
  }
  
  protected boolean isLinksBalanced(ArrayList<Integer> porNumList)
  {
    // assume already initialized()
    
    // given these links, do any of these exist in the over/under
    // subscribed list?
    
    for(Integer pNum: porNumList)
    {
      // does this port number exist in either imbalance list?
      for(Integer oNum: OverSubscribedPorts)
        if(pNum == oNum)
          return false;
      for(Integer oNum: UnderSubscribedPorts)
        if(pNum == oNum)
          return false;
    }
    return true;
  }
  
  protected boolean isDownLinksBalanced()
  {
    return isLinksBalanced(Vertex.getDownLinkNumbers());
  }
  
  protected boolean isUpLinksBalanced()
  {
    return isLinksBalanced(Vertex.getUpLinkNumbers());
  }
  
  protected boolean isAllCAHaveRoutes()
  {
    // every switch (via all its ports) must have routes to all
    // Channel Adapters.
    if(Table.getNumChannelAdapters() == (UpStats.getSum() + DownStats.getSum()))
        return true;
    
    return false;
  }
  
  public boolean isBalanced()
  {
    // satisfy all conditions, or return false
    if(isAllCAHaveRoutes() && isUpDownBalanced() && isUpLinksBalanced() && isDownLinksBalanced())
      return true;
    return false;
  }
  
  
  
  /************************************************************
   * Method Name:
   *  getVertex
   **/
  /**
   * Returns the value of vertex
   *
   * @return the vertex
   *
   ***********************************************************/
  
  public IB_Vertex getVertex()
  {
    return Vertex;
  }

  public RT_Node getRT_Node()
  {
    return Node;
  }


  /************************************************************
   * Method Name:
   *  getUpCA_Bins
   **/
  /**
   * Returns the value of upCA_Bins
   *
   * @return the upCA_Bins
   *
   ***********************************************************/
  
  public BinList<RT_Port> getUpCA_Bins()
  {
    return UpCA_Bins;
  }


  /************************************************************
   * Method Name:
   *  getDownCA_Bins
   **/
  /**
   * Returns the value of downCA_Bins
   *
   * @return the downCA_Bins
   *
   ***********************************************************/
  
  public BinList<RT_Port> getDownCA_Bins()
  {
    return DownCA_Bins;
  }


  /************************************************************
   * Method Name:
   *  getUnderSubscribedPorts
   **/
  /**
   * Returns the value of underSubscribedPorts
   *
   * @return the underSubscribedPorts
   *
   ***********************************************************/
  
  public ArrayList<Integer> getUnderSubscribedPorts()
  {
    return UnderSubscribedPorts;
  }


  /************************************************************
   * Method Name:
   *  getOverSubscribedPorts
   **/
  /**
   * Returns the value of overSubscribedPorts
   *
   * @return the overSubscribedPorts
   *
   ***********************************************************/
  
  public ArrayList<Integer> getOverSubscribedPorts()
  {
    return OverSubscribedPorts;
  }


  /************************************************************
   * Method Name:
   *  getNodeLevel
   **/
  /**
   * Returns the value of nodeLevel
   *
   * @return the nodeLevel
   *
   ***********************************************************/
  
  public int getNumCaRoutes()
  {
    return NumCaRoutes;
  }

  public int getNodeLevel()
  {
    return Vertex == null ? -1: Vertex.getDepth();
  }

  public int getMaxFabricLevel()
  {
    return IB_Vertex.getMaxDepth(VertexMap);
  }


  /************************************************************
   * Method Name:
   *  getUpStats
   **/
  /**
   * Returns the value of upStats
   *
   * @return the upStats
   *
   ***********************************************************/
  
  public SummaryStatistics getUpStats()
  {
    return UpStats;
  }


  /************************************************************
   * Method Name:
   *  getDownStats
   **/
  /**
   * Returns the value of downStats
   *
   * @return the downStats
   *
   ***********************************************************/
  
  public SummaryStatistics getDownStats()
  {
    return DownStats;
  }


  public String getBalanceReport()
  {
    // report number of ports with routes, and then each port summary
    StringBuffer buff = new StringBuffer();
    
    if((Table != null) && (Node != null) && (Fabric != null))
    {
      buff.append(String.format("     Balance Report: Single Switch to Channel Adapters")+ SmtConstants.NEW_LINE);
      buff.append(SmtConstants.NEW_LINE);
      buff.append(String.format("Fabric Name:                           %20s", Fabric.getFabricName())+ SmtConstants.NEW_LINE);
      buff.append(String.format("timestamp:                             %20s", Table.toTimeString())+ SmtConstants.NEW_LINE);
      buff.append(SmtConstants.NEW_LINE);
      
      buff.append(String.format("routing engine:                        %20s", Table.getRouteEngine()) + SmtConstants.NEW_LINE);
      buff.append(String.format("switch:%20s (%19s lid: %4d)", Node.getName(Fabric), Node.getGuid().toColonString(), Node.getLid()) + SmtConstants.NEW_LINE);
      buff.append(String.format("switch depth:                                        %6d", getNodeLevel()) + SmtConstants.NEW_LINE);
      buff.append(String.format("fabric depth:                                        %6d", getMaxFabricLevel()) + SmtConstants.NEW_LINE);
      buff.append(String.format("# channel adapters:                                  %6d", Table.getNumChannelAdapters()) + SmtConstants.NEW_LINE);
      buff.append(String.format("# switch ports:                                      %6d", NumPortsTotal) + SmtConstants.NEW_LINE);
      buff.append(String.format("# switch ports with routes:                          %6d", NumPortsRoutes) + SmtConstants.NEW_LINE);
      buff.append(String.format("# routes:                                            %6d", Node.getNumRoutes()) + SmtConstants.NEW_LINE);
      buff.append(String.format("# CA routes:                                         %6d", (int)(UpStats.getSum() + DownStats.getSum())) + SmtConstants.NEW_LINE);

      // if the switch depth equals the fabric depth, then there shouldn't be any up links
      if(getNodeLevel() == getMaxFabricLevel())
      {
        buff.append("        ---------Top Level Switch, " + Vertex.getUpLinkNumbers().size() + " Up Links-------" + SmtConstants.NEW_LINE);
      }
      else
      {
        buff.append("        -----------------" + Vertex.getUpLinkNumbers().size() + " Up Links---------------" + SmtConstants.NEW_LINE);
        int k = 0;
        for(ArrayList <RT_Port> caList: UpCA_Bins)
        {
          if(caList != null)
          {
            String key = (UpCA_Bins.getKey(k)).trim();
            buff.append(String.format("# ports with %3s CA routes:                            %4d", key, caList.size()) + SmtConstants.NEW_LINE);
          }
          k++;
        }
        buff.append(String.format("up links balanced?:                                   %5s", Boolean.toString(isUpLinksBalanced())) + SmtConstants.NEW_LINE);
       }
      
      buff.append("        -----------------" + Vertex.getDownLinkNumbers().size() + " Down Links--------------" + SmtConstants.NEW_LINE);
      
      int k = 0;
      for(ArrayList <RT_Port> caList: DownCA_Bins)
      {
        if(caList != null)
        {
          String key = (DownCA_Bins.getKey(k)).trim();
          buff.append(String.format("# ports with %3s CA routes:                            %4d", key, caList.size()) + SmtConstants.NEW_LINE);
        }
        k++;
      }
      
//      buff.append("        ------- excluding single leaf routes ------" + SmtConstants.NEW_LINE);
//      buff.append(String.format("total # CA routes (this switch):                    %7.0f", RouteStats.getSum()+NumSingleRoutes) + SmtConstants.NEW_LINE);
//      buff.append(String.format("# CA routes (w/o singles):                          %7.0f", RouteStats.getSum()) + SmtConstants.NEW_LINE);
//      buff.append(String.format("mean # CA routes/port:                                %5.1f", RouteStats.getMean()) + SmtConstants.NEW_LINE);
//      buff.append(String.format("std deviation:                                        %5.1f", RouteStats.getStandardDeviation()) + SmtConstants.NEW_LINE);
//      buff.append(String.format("min:                                                  %5.1f", RouteStats.getMin()) + SmtConstants.NEW_LINE);
//      buff.append(String.format("max:                                                  %5.1f", RouteStats.getMax()) + SmtConstants.NEW_LINE);
      buff.append(String.format("down links balanced?:                                 %5s", Boolean.toString(isDownLinksBalanced())) + SmtConstants.NEW_LINE);
      buff.append("        --------------------------------------------" + SmtConstants.NEW_LINE);
      buff.append(String.format("switch routes balanced?:                              %5s", Boolean.toString(isBalanced())) + SmtConstants.NEW_LINE);
   }
    return buff.toString();
  }


}
