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
 *        file: OSM_FabricDeltaAnalyzer.java
 *
 *  Created on: Nov 4, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.data;

import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.core.IB_Link;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_Port.PortCounterName;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_PortRate.PortCounterUnits;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Edge;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.SmtConstants;
import gov.llnl.lc.smt.command.top.TopAnalyzer;
import gov.llnl.lc.time.TimeStamp;
import gov.llnl.lc.util.BinList;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

/**********************************************************************
 * Describe purpose and responsibility of OSM_FabricDeltaAnalyzer
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Nov 4, 2013 9:41:02 AM
 **********************************************************************/
public class OSM_FabricDeltaAnalyzer implements CommonLogger
{
  private OSM_FabricDelta Delta;
  
  private LinkedHashMap <String, IB_Vertex> VertexMap;
  private LinkedHashMap <String, IB_Edge> EdgeMap;
  
  private LinkedHashMap<String, PFM_PortChange> AllPortErrors = null;
  private LinkedHashMap<String, IB_Edge>        AllLinkErrors = null;
  private LinkedHashMap<String, IB_Vertex>      AllNodeErrors = null;
  
  private OSM_FabricAnalyzer FabAnalizer;
  private LinkedHashMap<String, PFM_PortChange> ActiveTrafficPorts;
  private LinkedHashMap<String, IB_Edge>        ActiveTrafficLinks = null;
  private LinkedHashMap<String, IB_Vertex>      ActiveTrafficNodes = null;
  
  private LinkedHashMap<String, PFM_PortRate>   PortRates;
  
  private OSM_NodeType IncludedTypes;                 // all ports, only switch to switch ports, or only CA ports
  private BinList<PFM_PortRate> UtilizationRateBins;
  
  private SummaryStatistics RateStats;
  
  private static int DEFAULT_NUM_BINS = 10;
  private int BinSize = 20;
  
  public static final String STATIC_ERROR  = "Static Error";
  public static final String DYNAMIC_ERROR = "Dynamic Error";
  
  int MaxNameSize = 30;
  int MaxHopSize  = 8;

  
  private String FabHeader = "%-20s  # ports: %6d, max rate: %s      %s";
  private String SumFormat = "Utilization: ave=%5.2f%%, std dev=%5.2f%%, max=%6.2f%%, min=%5.2f%%";
  private String BinHeader = "  % max rate       # ports         % ports";
  private String BinFormat = "    %2d-%2d          %6d          %6.2f%%";
 
  /************************************************************
   * Method Name:
   *  OSM_FabricDeltaAnalyzer
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param delta
   ***********************************************************/
  public OSM_FabricDeltaAnalyzer(OSM_Fabric fabric1, OSM_Fabric fabric2)
  {
    this(new OSM_FabricDelta(fabric1, fabric2));
  }

  public OSM_FabricDeltaAnalyzer(OSM_Fabric fabric1, OSM_Fabric fabric2, int numBins)
  {
    this(new OSM_FabricDelta(fabric1, fabric2), numBins, OSM_NodeType.UNKNOWN);
  }

  /************************************************************
   * Method Name:
   *  OSM_FabricDeltaAnalyzer
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param delta
   ***********************************************************/
  public OSM_FabricDeltaAnalyzer(OSM_FabricDelta delta, int numBins, OSM_NodeType includeTypes)
  {
    super();
    Delta = delta;
    logger.severe("Initializing the Analyzer with " + numBins + " num bins");
    logger.severe("CREATING VERTEX MAP");
    VertexMap              = IB_Vertex.createVertexMap(Delta.getFabric2());
    logger.severe("CREATING EDGE MAP");
    EdgeMap                = IB_Vertex.createEdgeMap(VertexMap);

    logger.severe("CREATING FABRIC ANALYZER");
    FabAnalizer = new OSM_FabricAnalyzer(Delta.getFabric2());

    init(numBins, VertexMap, EdgeMap, FabAnalizer, includeTypes);
  }

  public OSM_FabricDeltaAnalyzer(OSM_FabricDelta delta, int numBins, LinkedHashMap <String, IB_Vertex> vertexMap, LinkedHashMap <String, IB_Edge> edgeMap, OSM_FabricAnalyzer fabAnalizer)
  {
    super();
    Delta = delta;
    init(numBins, vertexMap, edgeMap, fabAnalizer, OSM_NodeType.UNKNOWN);
  }

  /************************************************************
   * Method Name:
   *  OSM_FabricDeltaAnalyzer
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param delta
   ***********************************************************/
  public OSM_FabricDeltaAnalyzer(OSM_FabricDelta delta)
  {
     this(delta, OSM_NodeType.UNKNOWN);
   }

  public OSM_FabricDeltaAnalyzer(OSM_FabricDelta delta, OSM_NodeType includeTypes)
  {
     this(delta, DEFAULT_NUM_BINS, includeTypes);
   }


  /************************************************************
   * Method Name:
   *  getDelta
   **/
  /**
   * Returns the value of delta
   *
   * @return the delta
   *
   ***********************************************************/
  
  public OSM_FabricDelta getDelta()
  {
    return Delta;
  }
  
  private boolean init(int numBins, LinkedHashMap <String, IB_Vertex> vertexMap, LinkedHashMap <String, IB_Edge> edgeMap, OSM_FabricAnalyzer fabAnalizer, OSM_NodeType includeTypes)
  {
    VertexMap              = vertexMap;
    EdgeMap                = edgeMap;
    FabAnalizer            = fabAnalizer;
    IncludedTypes          = includeTypes;
    logger.warning("Initializing the Delta Analyzer with new instance");
    
    // use the methods from the TopAnalyzer to find differences (traffic and errors)
    
    ActiveTrafficPorts = TopAnalyzer.calculateActiveTrafficPorts(-1, Delta.getPortsWithTrafficChange());
    UtilizationRateBins = getFabricRateUtilizationBins(numBins, includeTypes);
    AllPortErrors = TopAnalyzer.calculateActiveErrorPorts(-1, Delta.getPortsWithErrorChange());
    
    AllLinkErrors = TopAnalyzer.calculateActiveErrorLinks(-1, AllPortErrors, edgeMap);
    AllNodeErrors = TopAnalyzer.calculateActiveErrorNodes(-1, AllPortErrors, vertexMap);
    ActiveTrafficLinks = TopAnalyzer.calculateActiveTrafficLinks(-1, ActiveTrafficPorts, edgeMap);
    ActiveTrafficNodes = TopAnalyzer.calculateActiveTrafficNodes(-1, ActiveTrafficPorts, vertexMap);
    
    // TODO:  anything for the traffic??
     return true;
  }
  
  public static String truncate(String value, int length)
  {
    if (value != null && value.length() > length)
      value = value.substring(0, length);
    return value;
  }
  
  public static boolean isSwitchPort(OSM_FabricDelta d, PFM_Port p)
  {
    if(d == null || d.getFabric2() == null || p == null)
      return false;
    
    OSM_Fabric f = d.getFabric2();
    OSM_Node n = f.getOSM_Node(p.getNodeGuid());
    return n == null ? false: n.isSwitch();
  }

  public static boolean includeThisPort(OSM_FabricDelta d, PFM_Port p, OSM_NodeType includeTypes)
  {
    // return true if includeTypes == OSM_NodeType.UNKNOWN
    if(OSM_NodeType.UNKNOWN.equals(includeTypes))
    {
//      System.out.println("Port");
      return true;      
    }
    
    // what types of ports are connected to each other
    OSM_Fabric f = d.getFabric2();
    String key = OSM_Fabric.getOSM_PortKey(p.node_guid, p.port_num);
    OSM_Port port = f.getOSM_Port(key);
    IB_Guid rguid = new IB_Guid(port.sbnPort.linked_node_guid);
    
    OSM_Node rn = f.getOSM_Node(rguid);
    
    boolean isLocalSwitchPort  = isSwitchPort(d, p);
    boolean isRemoteSwitchPort = rn == null? false: rn.isSwitch();

    // if includeTypes == OSM_NodeType.SW_NODE, return true only if this port is a switch port AND is
    //                                          connected to another switch
    if(OSM_NodeType.SW_NODE.equals(includeTypes) &&
        (isLocalSwitchPort && isRemoteSwitchPort))
    {
//      System.out.println("SW Port");
      return true;
    }

    // if includeTypes == OSM_NodeType.CA_NODE, return true if this port is a ca port OR is
    //                                          connected to another ca port
    if(OSM_NodeType.CA_NODE.equals(includeTypes) &&
        (!isLocalSwitchPort || !isRemoteSwitchPort))
    {
//      System.out.println("CA Port");
      return true;
    }
    return false;
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
    return Delta.getFabricName();
  }


  /************************************************************
   * Method Name:
   *  getDeltaTimeStamp
   **/
  /**
   * Returns the value of deltaTimeStamp
   *
   * @return the deltaTimeStamp
   *
   ***********************************************************/
  
  public TimeStamp getDeltaTimeStamp()
  {
    return Delta.getTimeStamp();
  }


  /************************************************************
   * Method Name:
   *  getDeltaSeconds
   **/
  /**
   * Returns the value of deltaSeconds
   *
   * @return the deltaSeconds
   *
   ***********************************************************/
  
  public int getDeltaSeconds()
  {
    return (int)(Delta.getAgeDifference(TimeUnit.SECONDS));
  }
  
  /************************************************************
   * Method Name:
   *  getPortRates
   **/
  /**
   * Returns the value of portRates
   *
   * @return the portRates
   *
   ***********************************************************/
  
  public LinkedHashMap<String, PFM_PortRate> getPortRates()
  {
    return PortRates;
  }

  /************************************************************
   * Method Name:
   *  getUtilizationRateBins
   **/
  /**
   * Returns the value of utilizationRateBins
   *
   * @return the utilizationRateBins
   *
   ***********************************************************/
  
  public BinList<PFM_PortRate> getUtilizationRateBins()
  {
    return UtilizationRateBins;
  }

  /************************************************************
   * Method Name:
   *  getRateStats
   **/
  /**
   * Returns the value of rateStats
   *
   * @return the rateStats
   *
   ***********************************************************/
  
  public SummaryStatistics getRateStats()
  {
    return RateStats;
  }
  
  public IB_Vertex getIB_Vertex(IB_Guid guid)
  {
    return VertexMap.get(IB_Vertex.getVertexKey(guid));
  }

  public OSM_Port getOSM_Port(IB_Guid guid, int portNum)
  {
    // this port may be up or down, active or idle, fine or with errors
    IB_Vertex v = getIB_Vertex(guid);
    if(v != null)
    {
      // see if the port is part of this vertex
      IB_Edge e = v.getEdge(portNum);
      if(e != null)
        return e.getEndPort(v);
     }
    return null;
   }

  public IB_Edge getIB_Edge(IB_Guid guid, int portNum)
  {
    String key = PFM_PortChange.getPFM_PortChangeKey(guid, portNum);
    if((key == null) || (ActiveTrafficPorts == null))
      return null;
    PFM_PortChange ppc = ActiveTrafficPorts.get(key);
    if(ppc == null)
      return null;
    PFM_Port port = ppc.getPort2();
    return IB_Edge.getEdge(port, EdgeMap);
  }

  public double getNodeUtilization(IB_Guid guid, PFM_Port.PortCounterName pcn)
  {
  // find all the ports for this node, and average up their utilization numbers
    IB_Vertex v = getIB_Vertex(guid);
    if(v == null)
      return 0.0;
    
    SummaryStatistics nodeStats = new SummaryStatistics();
    
     // loop through all the ports in this vertex
    int num_ports = v.getNode().sbnNode.num_ports;
    for(int pn = 1; pn <= num_ports; pn++)
    {
      nodeStats.addValue(getPortUtilization(guid, pn, pcn));
    }
    return nodeStats.getMean();
  }
  
  public double getLinkUtilization(IB_Guid guid, int portNum, PFM_Port.PortCounterName pcn)
  {
    String key = PFM_PortChange.getPFM_PortChangeKey(guid, portNum);
    if((key == null) || (ActiveTrafficPorts == null))
      return 0;
    PFM_PortChange ppc = ActiveTrafficPorts.get(key);
    PFM_Port port = ppc.getPort2();
    IB_Edge e = IB_Edge.getEdge(port, EdgeMap);
    
    return getLinkUtilization(e, pcn);
  }
  
  public String getPortUtilizationString(IB_Guid guid, int portNum)
  {
    double x = getPortUtilization(guid, portNum, PFM_Port.PortCounterName.xmit_data);
    double r = getPortUtilization(guid, portNum, PFM_Port.PortCounterName.rcv_data);
    PFM_Port.PortCounterName maxC = (x > r ? PFM_Port.PortCounterName.xmit_data:PFM_Port.PortCounterName.rcv_data);
    double maxN = (x > r ? x:r);

    return maxC.getName() + ": " + getDoubletUtilizationString(maxN);
  }
  
  public String getPathUtilizationTablesString(RT_Path path)
  {
    StringBuffer sbuff = new StringBuffer();
    sbuff.append(getPathUtilizationTableString(path, true));
    sbuff.append(getPathUtilizationTableString(path.getReturnPath(), false));
    return sbuff.toString();
  }

  
  public String getPathUtilizationTableString(RT_Path path, boolean toDestination)
  {
    StringBuffer sbuff = new StringBuffer();
    sbuff.append(SmtConstants.MEDIUM_FONT);

     if(toDestination)
     {
       String tsString = this.getDeltaTimeStamp().toString();
       sbuff.append("<p align=right>time stamp: " + SmtConstants.SPACE + "<b>" + tsString + "</b>");
       sbuff.append("<br>period (secs): " + SmtConstants.SPACE + "<b>" + this.getDeltaSeconds() + "</b>");
       sbuff.append("<h4>Transmit Path (to dst):</h4>");
     }
    else
      sbuff.append("<h4>Receive Path (from dst):</h4>");
     
    sbuff.append("<blockquote>");
    sbuff.append(SmtConstants.MEDIUM_FONT);
    sbuff.append("path id: " + SmtConstants.SPACE + "<b>" + path.getPathIdString() + "</b>");
    sbuff.append("</blockquote>");

    sbuff.append("<table class=\"tftable\" border=\"1\">");
    
    // the header, or title
    sbuff.append("<tr><th>hop</th><th>node</th><th>output port</th><th>xmit delta (counts)</th><th>xmit rate</th><th>units</th><th>% rate</th></tr>");
    
    // the guts of the table (start with the source, and finish with the destination)
    ArrayList<RT_PathLeg> legs = path.getLegs();
    
    // iterate through the legs
    int hopNum  = 0;
    int numHops = legs.size();
    for(RT_PathLeg leg : legs)
    {
    OSM_Port p1 = leg.getFromPort();
    OSM_Port p2 = leg.getToPort();
    String nodeName = truncate(getIB_Vertex(p1.getNodeGuid()).getName(), MaxNameSize);  
    String key = PFM_PortChange.getPFM_PortChangeKey(p1.getNodeGuid(), p1.getPortNumber());
    PFM_PortRate pr = PortRates.get(key);
    if(pr != null)
    {
      // the hop line
      sbuff.append(getPathRateUtilizationTableLine(leg.getFromNodeName(), leg, hopNum, false));
      
      if(++hopNum >= numHops)
      {
        // print the final destination line? (check PathRateUtilizationLine() for formatting clues
        nodeName = truncate(getIB_Vertex(p2.getNodeGuid()).getName(), MaxNameSize);
        sbuff.append(getPathRateUtilizationTableLine(path.getPathIdString(), leg, hopNum, true));

        
//        sbuff.append(String.format(dstFormat,
//            dstHop,
//            nodeName,
//            p2.getNodeGuid().toColonString() + "\n"));
        break;
      }
    }
    else
      sbuff.append("  please be patient, rates not yet available\n");

    }
    sbuff.append("</table>");
    return sbuff.toString();
  }
  
  public String getTopNodeTableString(int numTop)
  { 
    StringBuffer sbuff = new StringBuffer();
    String tsString = this.getDeltaTimeStamp().toString();
    sbuff.append(SmtConstants.MEDIUM_FONT);
    sbuff.append("<p align=right>time stamp: " + SmtConstants.SPACE + "<b>" + tsString + "</b>");
    sbuff.append("<br>period (secs): " + SmtConstants.SPACE + "<b>" + this.getDeltaSeconds() + "</b>");

    // this will end badly if there are no active traffic nodes
    if((ActiveTrafficNodes == null) || (ActiveTrafficNodes.entrySet() == null) || (ActiveTrafficNodes.entrySet().size() < 1))
      return sbuff.toString();

    sbuff.append("<table class=\"tftable\" border=\"1\">");
    sbuff.append("<tr><th>#</th><th>level</th><th>name</th><th>guid</th><th>port #</th><th>xmit MB/s</th><th>recv MB/s</th></tr>");
        
        // the guts of the table
    int topNum = 0;
    
    for (Map.Entry<String, IB_Vertex> eMapEntry : ActiveTrafficNodes.entrySet())
    {
      IB_Vertex v = eMapEntry.getValue();
      IB_Guid g = v.getGuid();
      int pNum  = v.getTopPortChange().getPortNumber();
      String name = v.getName();
      int depth   = v.getDepth();
      String ID   = "<a href=\"" + g.toColonString() + "\">" + ++topNum + "</a>";

      sbuff.append("<tr>");
      sbuff.append("<td>" + ID + "</td>");
      sbuff.append("<td>" + depth + "</td>");
      sbuff.append("<td>" + name + "</td>");
      sbuff.append("<td>" + g.toColonString() + "</td>");
      sbuff.append("<td>" + pNum + "</td>");
      sbuff.append("<td>" + v.toVertexXmitString() + "</td>");
      sbuff.append("<td>" + v.toVertexRcvString() + "</td>");
     
      // stop after the desired number of iterations, or when the end is reached
      if(topNum >= numTop)
        break;
    }
        
    sbuff.append("</table>");
    return sbuff.toString();
  }
  
  public String getTopLinkTableString(int numTop)
  { 
    StringBuffer sbuff = new StringBuffer();
    String tsString = this.getDeltaTimeStamp().toString();
    sbuff.append(SmtConstants.MEDIUM_FONT);
    sbuff.append("<p align=right>time stamp: " + SmtConstants.SPACE + "<b>" + tsString + "</b>");
    sbuff.append("<br>period (secs): " + SmtConstants.SPACE + "<b>" + this.getDeltaSeconds() + "</b>");

    // FIXME: this will end badly if there are no active traffic links
    if((ActiveTrafficLinks == null) || (ActiveTrafficLinks.entrySet() == null) || (ActiveTrafficLinks.entrySet().size() < 1))
      return sbuff.toString();

    sbuff.append("<table class=\"tftable\" border=\"1\">");
    sbuff.append("<tr><th>#</th><th>level</th><th>link identification</th><th>xmit MB/s</th><th>recv MB/s</th></tr>");
        
        // the guts of the table
    int topNum = 0;
    
    for (Map.Entry<String, IB_Edge> eMapEntry : ActiveTrafficLinks.entrySet())
    {
      IB_Edge te = eMapEntry.getValue();
      
      String name = te.toEdgeIdStringVerbose(54);
      int depth   = te.getDepth();
      IB_Guid g = te.getEndPort1().getNodeGuid();
      int pNum  = te.getEndPort1().getPortNumber();

      String ID   = "<a href=\"" + g.toColonString() + ":" + pNum +"\">" + ++topNum + "</a>";

      sbuff.append("<tr>");
      sbuff.append("<td>" + ID + "</td>");
      sbuff.append("<td>" + depth + "</td>");
      sbuff.append("<td>" + name + "</td>");
      sbuff.append("<td>" + te.toEdgeXmitString() + "</td>");
      sbuff.append("<td>" + te.toEdgeRcvString() + "</td>");
     
      // stop after the desired number of iterations, or when the end is reached
      if(topNum >= numTop)
        break;
    }
        
    sbuff.append("</table>");
    return sbuff.toString();
  }
  
  public String getTopPortTableString(int numTop)
  { 
    StringBuffer sbuff = new StringBuffer();
    String tsString = this.getDeltaTimeStamp().toString();
    sbuff.append(SmtConstants.MEDIUM_FONT);
    sbuff.append("<p align=right>time stamp: " + SmtConstants.SPACE + "<b>" + tsString + "</b>");
    sbuff.append("<br>period (secs): " + SmtConstants.SPACE + "<b>" + this.getDeltaSeconds() + "</b>");

    // this will end badly if there are no active traffic nodes
    if((ActiveTrafficPorts == null) || (ActiveTrafficPorts.entrySet() == null) || (ActiveTrafficPorts.entrySet().size() < 1))
      return sbuff.toString();

    sbuff.append("<table class=\"tftable\" border=\"1\">");
    sbuff.append("<tr><th>#</th><th>level</th><th>name</th><th>guid</th><th>port #</th><th>xmit MB/s</th><th>recv MB/s</th></tr>");
        
        // the guts of the table
    int topNum = 0;
    
    for (Map.Entry<String, PFM_PortChange> eMapEntry : ActiveTrafficPorts.entrySet())
    {
      PFM_PortChange pc = eMapEntry.getValue();
      IB_Guid g = pc.getAddress().getGuid();
      IB_Vertex v = getIB_Vertex(g);
      int pNum  = pc.getPortNumber();
      String name = v.getName();
      int depth   = v.getDepth();
      String ID   = "<a href=\"" + g.toColonString() + ":" + pNum +"\">" + ++topNum + "</a>";

      sbuff.append("<tr>");
      sbuff.append("<td>" + ID + "</td>");
      sbuff.append("<td>" + depth + "</td>");
      sbuff.append("<td>" + name + "</td>");
      sbuff.append("<td>" + g.toColonString() + "</td>");
      sbuff.append("<td>" + pNum + "</td>");
      sbuff.append("<td>" + PFM_PortRate.toTransmitRateMBString(pc) + "</td>");
      sbuff.append("<td>" + PFM_PortRate.toReceiveRateMBString(pc) + "</td>");
     
      // stop after the desired number of iterations, or when the end is reached
      if(topNum >= numTop)
        break;
    }
        
    sbuff.append("</table>");
    return sbuff.toString();
  }
  
  public String getNodeErrorTableString(int numErrs)
  {    
    StringBuffer sbuff = new StringBuffer();
    String tsString = this.getDeltaTimeStamp().toString();
    sbuff.append(SmtConstants.MEDIUM_FONT);
    sbuff.append("<p align=right>time stamp: " + SmtConstants.SPACE + "<b>" + tsString + "</b>");
    sbuff.append("<br>period (secs): " + SmtConstants.SPACE + "<b>" + this.getDeltaSeconds() + "</b>");
     
    StringBuffer supp = new StringBuffer();
    boolean first = true;
    for(PortCounterName sn: PortCounterName.PFM_SUPPRESS_COUNTERS)
    {
      if(!first)
        supp.append(", ");
      supp.append(sn.getName());
      first = false;
    }

    // this will end badly if there are no node errors
    if((AllNodeErrors == null) || (AllNodeErrors.entrySet() == null) || (AllNodeErrors.entrySet().size() < 1))
      return sbuff.toString();

    sbuff.append("<h4>Nodes with Errors: " + AllNodeErrors.size() + "</h4>");
    sbuff.append("<blockquote>");
    sbuff.append(SmtConstants.MEDIUM_FONT);
    sbuff.append("Supressed Errors: " + SmtConstants.SPACE + "<b>" + supp.toString() + "</b>");
    sbuff.append("</blockquote>");

 
    sbuff.append("<table class=\"tftable\" border=\"1\">");
    sbuff.append("<tr><th>#</th><th>level</th><th>name</th><th>guid</th><th>error:port list</th></tr>");
    
        
        // the guts of the table
    int errNum = 0;
    
    for (Map.Entry<String, IB_Vertex> eMapEntry : AllNodeErrors.entrySet())
    {
      IB_Vertex v = eMapEntry.getValue();
      String name = v.getName();
      IB_Guid g = v.getGuid();
      int depth   = v.getDepth();
      
            String errs = SmtConstants.RED_FONT + v.toVertexErrorString() + SmtConstants.END_FONT;
            String ID   = "<a href=\"" + g.toColonString() + "\">" + ++errNum + "</a>";

            sbuff.append("<tr>");
            sbuff.append("<td>" + ID + "</td>");
            sbuff.append("<td>" + depth + "</td>");
            sbuff.append("<td>" + name + "</td>");
            sbuff.append("<td>" + g.toColonString() + "</td>");
            sbuff.append("<td>" + errs + "</td>");

            // stop after the desired number of iterations, or when the end is reached
      if(errNum >= numErrs)
        break;
    }
        
    sbuff.append("</table>");
    return sbuff.toString();
  }
  
  public String getLinkErrorTableString(int numErrs)
  {    
    StringBuffer sbuff = new StringBuffer();
    String tsString = this.getDeltaTimeStamp().toString();
    sbuff.append(SmtConstants.MEDIUM_FONT);
    sbuff.append("<p align=right>time stamp: " + SmtConstants.SPACE + "<b>" + tsString + "</b>");
    sbuff.append("<br>period (secs): " + SmtConstants.SPACE + "<b>" + this.getDeltaSeconds() + "</b>");
     
    StringBuffer supp = new StringBuffer();
    boolean first = true;
    for(PortCounterName sn: PortCounterName.PFM_SUPPRESS_COUNTERS)
    {
      if(!first)
        supp.append(", ");
      supp.append(sn.getName());
      first = false;
    }

    // this will end badly if there are no link errors
    if((AllLinkErrors == null) || (AllLinkErrors.entrySet() == null) || (AllLinkErrors.entrySet().size() < 1))
      return sbuff.toString();

    sbuff.append("<h4>Links with Errors: " + AllLinkErrors.size() + "</h4>");
    sbuff.append("<blockquote>");
    sbuff.append(SmtConstants.MEDIUM_FONT);
    sbuff.append("Supressed Errors: " + SmtConstants.SPACE + "<b>" + supp.toString() + "</b>");
    sbuff.append("</blockquote>");

 
    sbuff.append("<table class=\"tftable\" border=\"1\">");
    sbuff.append("<tr><th>#</th><th>level</th><th>link identification</th><th>delta error/period=p1,p2</th></tr>");
    
        
        // the guts of the table
    int errNum = 0;
    
    for (Map.Entry<String, IB_Edge> eMapEntry : AllLinkErrors.entrySet())
    {
      IB_Edge te = eMapEntry.getValue();
      
      String name = te.toEdgeIdStringVerbose(54);
      int depth   = te.getDepth();
      String errs = SmtConstants.RED_FONT + te.toShortErrorString() + SmtConstants.END_FONT;
      
      IB_Guid g = te.getEndPort1().getNodeGuid();
      int pNum  = te.getEndPort1().getPortNumber();

      String ID   = "<a href=\"" + g.toColonString() + ":" + pNum +"\">" + ++errNum + "</a>";

      sbuff.append("<tr>");
      sbuff.append("<td>" + ID + "</td>");

            sbuff.append("<td>" + depth + "</td>");
            sbuff.append("<td>" + name + "</td>");
            sbuff.append("<td>" + errs + "</td>");
      // stop after the desired number of iterations, or when the end is reached
      if(errNum >= numErrs)
        break;
    }
        
    sbuff.append("</table>");
    return sbuff.toString();
  }
  
  public String getPortErrorTableString(int numErrs)
  {    
    StringBuffer sbuff = new StringBuffer();
    String tsString = this.getDeltaTimeStamp().toString();
    sbuff.append(SmtConstants.MEDIUM_FONT);
    sbuff.append("<p align=right>time stamp: " + SmtConstants.SPACE + "<b>" + tsString + "</b>");
    sbuff.append("<br>period (secs): " + SmtConstants.SPACE + "<b>" + this.getDeltaSeconds() + "</b>");
     
    StringBuffer supp = new StringBuffer();
    boolean first = true;
    for(PortCounterName sn: PortCounterName.PFM_SUPPRESS_COUNTERS)
    {
      if(!first)
        supp.append(", ");
      supp.append(sn.getName());
      first = false;
    }

    // this will end badly if there are no port errors
    if((AllPortErrors == null) || (AllPortErrors.entrySet() == null) || (AllPortErrors.entrySet().size() < 1))
      return sbuff.toString();

    sbuff.append("<h4>Ports with Errors: " + AllPortErrors.size() + "</h4>");
    sbuff.append("<blockquote>");
    sbuff.append(SmtConstants.MEDIUM_FONT);
    sbuff.append("Supressed Errors: " + SmtConstants.SPACE + "<b>" + supp.toString() + "</b>");
    sbuff.append("</blockquote>");

 
    sbuff.append("<table class=\"tftable\" border=\"1\">");
    sbuff.append("<tr><th>#</th><th>level</th><th>name</th><th>guid</th><th>port #</th><th>delta error/period</th></tr>");
    
        
        // the guts of the table
    int errNum = 0;
    
    for (Map.Entry<String, PFM_PortChange> eMapEntry : AllPortErrors.entrySet())
    {
      PFM_PortChange pc = eMapEntry.getValue();
      IB_Guid g = pc.getAddress().getGuid();
      IB_Vertex v = getIB_Vertex(g);
      int pNum  = pc.getPortNumber();
      String name = v.getName();
      int depth   = v.getDepth();
      
            String errs = SmtConstants.RED_FONT + pc.toShortErrorString() + SmtConstants.END_FONT;
            String ID   = "<a href=\"" + g.toColonString() + ":" + pNum +"\">" + ++errNum + "</a>";

            sbuff.append("<tr>");
            sbuff.append("<td>" + ID + "</td>");
            sbuff.append("<td>" + depth + "</td>");
            sbuff.append("<td>" + name + "</td>");
            sbuff.append("<td>" + g.toColonString() + "</td>");
            sbuff.append("<td>" + pNum + "</td>");
            sbuff.append("<td>" + errs + "</td>");
       
      // stop after the desired number of iterations, or when the end is reached
      if(errNum >= numErrs)
        break;
    }
        
    sbuff.append("</table>");
    return sbuff.toString();
  }
  
  public String getNodeUtilizationString(IB_Guid guid)
  {
    // get the "average" xmit and rcv, then return the max of the two
    double x = getNodeUtilization(guid, PFM_Port.PortCounterName.xmit_data);
    double r = getNodeUtilization(guid, PFM_Port.PortCounterName.rcv_data);
    PFM_Port.PortCounterName maxC = (x > r ? PFM_Port.PortCounterName.xmit_data:PFM_Port.PortCounterName.rcv_data);
    double maxN = (x > r ? x:r);

    return maxC.getName() + ": " + getDoubletUtilizationString(maxN);
  }
  
  public double getMaxPortUtilization(PFM_PortRate pr)
  {
    // return the maximum of xmit or rcv data
    double x = getPortUtilization(pr, PFM_Port.PortCounterName.xmit_data);
    double r = getPortUtilization(pr, PFM_Port.PortCounterName.rcv_data);
    return (x > r ? x:r);
   }
  
  public double getMaxPortUtilization(IB_Guid guid, int portNum)
  {
    // return the maximum of xmit or rcv data
    double x = getPortUtilization(guid, portNum, PFM_Port.PortCounterName.xmit_data);
    double r = getPortUtilization(guid, portNum, PFM_Port.PortCounterName.rcv_data);
    return (x > r ? x:r);
   }
  
  public String getFabrictUtilizationString()
  {
    return "ave: " + getDoubletUtilizationString(getFabricRateUtilizationMean());
  }
  
  public double getPortUtilization(IB_Guid guid, int portNum, PFM_Port.PortCounterName pcn)
  {
//    Look inside the PortRates for a port that matches this guid and PN
    String key = PFM_PortChange.getPFM_PortChangeKey(guid, portNum);
    PFM_PortRate pr = PortRates.get(key);
    return getPortUtilization(pr, pcn);
  }
  
  /************************************************************
   * Method Name:
   *  getPortUtilization
  **/
  /**
   * Return a percentage value, representing the rate of change of the
   * named counter, compared to the maximum rate.
   *
   * @see     describe related java objects
   *
   * @param pr
   * @param pcn
   * @return
   ***********************************************************/
  public double getPortUtilization(PFM_PortRate pr, PFM_Port.PortCounterName pcn)
  {
    return getPortUtilization(pr, pcn, FabAnalizer);
  }
  
  public static double getPortUtilization(PFM_PortRate pr, PFM_Port.PortCounterName pcn, OSM_FabricAnalyzer fAnalyzer)
  {
    // Utilization is between 0 and 100%
    //   1. find the maximum link rate for the fabric, and use that for all ports (even slower ones)
    //   2. find the counter units (different counters, count different sizes of things)
    //   3. find the change rate for this counter (change per unit time - like BW)
    //   4. utilization should be (change rate)/(max rate)  * 100%
    //
    if(pr == null)
      return 0;
    
    // what are the typical units for this counter?
  
    // assume data counters unless packets
    PortCounterUnits units = PortCounterUnits.COUNTS;
    if(PFM_Port.PortCounterName.PFM_PACKET_COUNTERS.contains(pcn))
      units = PortCounterUnits.PACKET_SIZE;
    
    // make sure everything is scaled properly for the type of counter
    OSM_LinkRate maxLinkRate = fAnalyzer.getMaxLinkRate();
    double maxRate = (double)maxLinkRate.getRateValue(units.getValue());
   
    double rate = (double)pr.getChangeRate(pcn);
    double U = (double)(rate * 100)/maxRate;
    if((U > 98) || (U < 0))
    {
      logger.severe(pr.getPortChange().getPort1().toPFM_ID_String());
      logger.severe("Arbitrarily setting utilization to zero, because it is incorrect: " + U);
      logger.severe("PortRate:" + PFM_PortRate.toVerboseDiagnosticString(pr.getPortChange()));
      U = 0;
    }
    return U;
  }
  
  public OSM_LinkRate getTheoreticalMaxRate()
  {
    return FabAnalizer.getMaxLinkRate();
  }
  
  /************************************************************
   * Method Name:
   *  getTheoreticalMaxRateString
  **/
  /**
   * Converts the theoretical max link rate (in Gbs) to the units
   * specified, and returns a string.  For example, converts from
   * 40 Gbs to a string "4000 MB/s", if supplied with MEGABYTES as
   * units.  This takes into account, bits vs bytes, giga vs mega,
   * but also removes the 20% overhead to show only "payload"
   * rate.  See OSM_LinkRate.getRateValue()
   *
   * @see     describe related java objects
   *
   * @param units
   * @return
   ***********************************************************/
  public String getTheoreticalMaxRateString(PortCounterUnits units)
  {
    OSM_LinkRate maxLinkRate = FabAnalizer.getMaxLinkRate();
    long maxRate = maxLinkRate.getRateValue(units.getValue());
    return Long.toString(maxRate) + " " + units.getName() + "/s";
  }
  
  public SummaryStatistics getFabricUtilizationStats()
  {
    return RateStats;
  }

  public double getFabricRateUtilizationMean()
  {
    // assume fully constructed, just return the total utilization number
    return RateStats.getMean();
  }
  
  public double getFabricRateUtilizationMax()
  {
    // assume fully constructed, just return the total utilization number
    return RateStats.getMax();
  }
  
  public double getFabricRateUtilizationMin()
  {
    // assume fully constructed, just return the total utilization number
    return RateStats.getMin();
  }
  
  public double getFabricRateUtilizationStdDev()
  {
    // assume fully constructed, just return the total utilization number
    return RateStats.getStandardDeviation();
  }
  
  public String getFabricRateUtilizationShortSummary()
  {
    return String.format(SumFormat, getFabricRateUtilizationMean(), getFabricRateUtilizationStdDev(), getFabricRateUtilizationMax(), getFabricRateUtilizationMin());
  }
  
  public BinList<PFM_PortRate>  getFabricRateUtilizationBins()
  {
    return UtilizationRateBins;
  }
  
  /************************************************************
   * Method Name:
   *  getFabricRateUtilizationSummary
  **/
  /**
   * Provides a (complex) "utilization" description of the overall
   * fabric based on the current FabricDelta.  The returned string
   * is suitable for printing to the screen or to an html document.
   *
   * @see     describe related java objects
   *
   * @return
   ***********************************************************/
  public String  getFabricRateUtilizationSummary()
  {
    StringBuffer sbuff = new StringBuffer();
    long totPorts = UtilizationRateBins.getTotalBinSizes();
    
    // the fabric header area
    sbuff.append(String.format(FabHeader, getFabricName(), totPorts, getTheoreticalMaxRate().getRateName(), getDeltaTimeStamp().toString()) + "\n");
    sbuff.append("  (" + getFabricRateUtilizationShortSummary() + ")\n\n");
    
    sbuff.append("  " + BinHeader + "\n");
    
    java.util.Set <String> keySet = UtilizationRateBins.getKeys();
    
    // copy and sort the keys
    String[] keyArray = new String[keySet.size()];
    int ndex = 0;
    for (Iterator<String> sKey = keySet.iterator(); sKey.hasNext();)
      keyArray[ndex++] = sKey.next();
    Arrays.sort(keyArray);
    
    // use the sorted key array
    ndex = 0;
    for (Iterator<String> sKey = keySet.iterator(); sKey.hasNext();)
    {
      String key = sKey.next();
      key = keyArray[ndex++];
      ArrayList <PFM_PortRate> prList = UtilizationRateBins.getBin(key);
      int num = prList.size();
      int val = Integer.parseInt(key);
      double percent = ((double)(num *100))/(double)totPorts;
      sbuff.append("  " + String.format(BinFormat, val-BinSize, val, prList.size(), percent) + "\n");
    }
     return sbuff.toString();
  }
  
  public String  getNodeRateUtilizationSummary(IB_Guid guid)
  {
    String NodeHeader = "%-18s         t1: %s   t0: %s       # links: %2d        max rate: %s (%s)";
    
    StringBuffer sbuff = new StringBuffer();
    IB_Vertex v = getIB_Vertex(guid);
    
    if(v == null)
    {
      return "Node (vertex) unavailable during this time period";
    }
    LinkedHashMap<String, IB_Edge> eMap = v.getEdgeMap();
    int numPorts = v.getNumPorts();
    int numLinks = eMap.size();
    boolean initialNode = true;
    boolean initialLink = true;
    
//  Look inside the PortRates for a port that matches this guid and PN
    
    // loop through the ports (in order) and try to find a matching link to display
    for(int portNum = 1; portNum <= numPorts; portNum++)
    {
      String key = PFM_PortChange.getPFM_PortChangeKey(guid, portNum);
      PFM_PortRate pr = PortRates.get(key);
      if(pr != null)
      {
         if(initialNode)
        {
          initialNode = false;
          PFM_Port port1 = pr.getPortChange().getPort1();
          PFM_Port port2 = pr.getPortChange().getPort2();
          sbuff.append(String.format(NodeHeader, v.getName(), port1.getCounterTimeStamp().toString(), port2.getCounterTimeStamp().toString(), numLinks, getTheoreticalMaxRate().getRateName(), this.getTheoreticalMaxRateString(PortCounterUnits.MEGABYTES)) + "\n\n");
         }
        sbuff.append(getNodeRateUtilizationLine(pr, portNum, initialLink));
        initialLink = false;
       }
      else
      {
        // its okay to have ports that don't belong to links, but we may want to keep track
        // TODO - dangling port list (ports without links)
        //logger.info("Link not found for that port number");
      }
    }
    if(initialNode || initialLink)
      sbuff.append("  please be patient, rates not yet available\n");
  return sbuff.toString();
  }
  
  public String  getPortRateUtilizationSummary(IB_Guid guid, int portNum)
  {
    String PortHeader = "%-24s  node: %-18s    port: %2d       max rate: %s (%s)";
    StringBuffer sbuff = new StringBuffer();
    
//  Look inside the PortRates for a port that matches this guid and PN
  String key = PFM_PortChange.getPFM_PortChangeKey(guid, portNum);
  PFM_PortRate pr = PortRates.get(key);
  if(pr != null)
  {
    IB_Vertex v = getIB_Vertex(guid);
    
      // the port header area
    sbuff.append(String.format(PortHeader, getFabricName(), v.getName(), portNum, getTheoreticalMaxRate().getRateName(), this.getTheoreticalMaxRateString(PortCounterUnits.MEGABYTES)) + "\n\n");

    sbuff.append(getPortRateUtilizationLine(pr, PFM_Port.PortCounterName.xmit_data, true));
    sbuff.append(getPortRateUtilizationLine(pr, PFM_Port.PortCounterName.rcv_data,  false));
    sbuff.append(getPortRateUtilizationLine(pr, PFM_Port.PortCounterName.xmit_pkts, false));
    sbuff.append(getPortRateUtilizationLine(pr, PFM_Port.PortCounterName.rcv_pkts,  false));
  }
  else
    sbuff.append("  please be patient, rates not yet available\n");
  return sbuff.toString();
  }
  
  /************************************************************
   * Method Name:
   *  getPortRateUtilizationLine
  **/
  /**
   * Generates a counter string similar to the one shown below.  Conditionally
   * generates a header string prepended to the counter string, also shown
   * below.
   * 
   * <pre>
   *  counter       Oct 22 11:15:04 2013     Oct 22 11:12:04 2013     delta (counts)    rate (units)   % max rate
   *xmit_data         324889040703834          324889040210045            493789            0  MB/s       0.00%
   *</pre>
   *
   * @see     describe related java objects
   *
   * @param pr                the rate of change for this port
   * @param pcn               the desired counter
   * @param includeLineHeader true, if you want the header prepended, normally only do this the first time
   * @return                  a rate string, in quasi table form, representing a counter for a specific port
   ***********************************************************/
  public String  getPortRateUtilizationLine(PFM_PortRate pr, PFM_Port.PortCounterName pcn, boolean includeLineHeader)
  {
    // assume data counters unless packets
    PortCounterUnits units = PortCounterUnits.MEGABYTES;
    if(PFM_Port.PortCounterName.PFM_PACKET_COUNTERS.contains(pcn))
      units = PortCounterUnits.PACKET_SIZE;
    
    String counterName = pcn.getName(); 
    String DeltaHeader = "  counter       %20s     %20s     delta (counts)     rate (units)   %% max rate";
    String DeltaFormat  = "%-12s    %17d        %17d        %12d     %6d %3s/s      %6.2f%%";
    String DeltaFormat1 = "%-12s    %17d        %17d        %12d     %6d  %3s/s";
    StringBuffer sbuff = new StringBuffer();
    
  if(pr != null)
  {
    PFM_PortChange pc = pr.getPortChange();
    PFM_Port port1 = pr.getPortChange().getPort1();
    PFM_Port port2 = pr.getPortChange().getPort2();
    
    if(includeLineHeader)
        sbuff.append(String.format(DeltaHeader, port1.getCounterTimeStamp().toString(), port2.getCounterTimeStamp().toString()) + "\n");
    
    if(PortCounterName.PFM_DATA_COUNTERS.contains(pcn))
     sbuff.append(String.format(DeltaFormat,
        counterName,
        port1.getCounter(pcn),
        port2.getCounter(pcn),
        pc.getDelta_port_counter(pcn),
        PFM_PortRate.getChangeRateLong(pc, pcn, units),
        units.getName(),
        getPortUtilization(pr, pcn)) + "\n");
    else
      sbuff.append(String.format(DeltaFormat1,
          counterName,
          port1.getCounter(pcn),
          port2.getCounter(pcn),
          pc.getDelta_port_counter(pcn),
          PFM_PortRate.getChangeRateLong(pc, pcn, units),
          units.getName()) + "\n");
  }
  return sbuff.toString();
  }
  
  /************************************************************
   * Method Name:
   *  getNodeRateUtilizationLine
  **/
  /**
   * Generates a counter string similar to the one shown below.  Conditionally
   * generates a header string prepended to the counter string, also shown
   * below.
   * 
   * <pre>
   *  counter       Oct 22 11:15:04 2013     Oct 22 11:12:04 2013     delta (counts)    rate (units)   % max rate
   *xmit_data         324889040703834          324889040210045            493789            0  MB/s       0.00%
   *</pre>
   *
   * @see     describe related java objects
   *
   * @param pr                the rate of change for this port
   * @param pcn               the desired counter
   * @param includeLineHeader true, if you want the header prepended, normally only do this the first time
   * @return                  a rate string, in quasi table form, representing a counter for a specific port
   ***********************************************************/
  public String  getNodeRateUtilizationLine(PFM_PortRate pr, int portNum, boolean includeLineHeader)
  {
    // assume data counters
    PFM_Port.PortCounterName xmit = PortCounterName.xmit_data;
    PFM_Port.PortCounterName rcv  = PortCounterName.rcv_data;
    
    PortCounterUnits units = PortCounterUnits.MEGABYTES;
    
    String DeltaHeader = "port #     xmit delta (counts)    xmit rate (units)   rcv delta (counts)  rcv rate (units)  % max rate";
    String DeltaFormat = "  %2d           %12d          %6d %3s/s        %12d       %6d %3s/s     %6.2f%%";
    StringBuffer sbuff = new StringBuffer();
    
  if(pr != null)
  {
    PFM_PortChange pc = pr.getPortChange();
    
    if(includeLineHeader)
        sbuff.append(DeltaHeader + "\n");
    
    sbuff.append(String.format(DeltaFormat,
        portNum,
        pc.getDelta_port_counter(xmit),
        PFM_PortRate.getChangeRateLong(pc, xmit, units),
        units.getName(),
        pc.getDelta_port_counter(rcv),
        PFM_PortRate.getChangeRateLong(pc, rcv, units),
        units.getName(),
        getMaxPortUtilization(pr)) + "\n");
  }
  return sbuff.toString();
  }
  
  /*******************************************************************************************************/  
  public String  getLinkRateUtilizationSummary(IB_Edge edge)
  {
    // A single link.  It has two ports, and each port has xmit and rcv
    
/*    guid:port  xmit delta (counts) xmit rate (units) rcv delta (counts) rcv rate (units) %max rate
 * 
 *    endport 1
 *    endport 2
 *    
 *    header is just the id string t1: t0: and max rate:
 *    
 *    refer to node utilization
 */
    
    String LinkHeader = "%-18s  link: %-36s  t1: %s   t0: %s   max rate: %s (%s)";
    
    StringBuffer sbuff = new StringBuffer();
    
//  Look inside the PortRates for a port that matches this guid and PN
    OSM_Port p1 = edge.getEndPort1();
    OSM_Port p2 = edge.getEndPort2();
  String key = PFM_PortChange.getPFM_PortChangeKey(p1.getNodeGuid(), p1.getPortNumber());
  PFM_PortRate pr = PortRates.get(key);
  if(pr != null)
  {
    // the page header
    PFM_Port port1 = pr.getPortChange().getPort1();
    PFM_Port port2 = pr.getPortChange().getPort2();
    sbuff.append(String.format(LinkHeader, getFabricName(), edge.toEdgeIdString(36), port1.getCounterTimeStamp().toString(), port2.getCounterTimeStamp().toString(), getTheoreticalMaxRate().getRateName(), this.getTheoreticalMaxRateString(PortCounterUnits.MEGABYTES)) + "\n\n");

    // the first line (endport1 with a header)
    sbuff.append(getLinkRateUtilizationLine(pr, key, true));
    
    // the second line (endport2)
    key = PFM_PortChange.getPFM_PortChangeKey(p2.getNodeGuid(), p2.getPortNumber());
    pr = PortRates.get(key);
    if(pr != null)
    {
      sbuff.append(getLinkRateUtilizationLine(pr, key, false));
    }
  }
  else
    sbuff.append("  please be patient, rates not yet available\n");
  return sbuff.toString();
  }
 
  /************************************************************
   * Method Name:
   *  getLinkRateUtilizationLine
  **/
  /**
   * Generates a counter string similar to the one shown below.  Conditionally
   * generates a header string prepended to the counter string, also shown
   * below.
   * 
   * <pre>
   *  counter       Oct 22 11:15:04 2013     Oct 22 11:12:04 2013     delta (counts)    rate (units)   % max rate
   *xmit_data         324889040703834          324889040210045            493789            0  MB/s       0.00%
   *</pre>
   *
   * @see     describe related java objects
   *
   * @param pr                the rate of change for this port
   * @param pcn               the desired counter
   * @param includeLineHeader true, if you want the header prepended, normally only do this the first time
   * @return                  a rate string, in quasi table form, representing a counter for a specific port
   ***********************************************************/
  public String getLinkRateUtilizationLine(PFM_PortRate pr, String portId, boolean includeLineHeader)
  {
    // assume data counters
    PFM_Port.PortCounterName xmit = PortCounterName.xmit_data;
    PFM_Port.PortCounterName rcv  = PortCounterName.rcv_data;
    
    PortCounterUnits units = PortCounterUnits.MEGABYTES;
    
    String DeltaHeader = "       end port              xmit delta (counts)    xmit rate (units)  rcv delta (counts)  rcv rate (units)  % max rate";
    String DeltaFormat = "%-22s          %12d           %6d %3s/s       %12d       %6d %3s/s     %6.2f%%";
    StringBuffer sbuff = new StringBuffer();
    
  if(pr != null)
  {
    PFM_PortChange pc = pr.getPortChange();
    
    if(includeLineHeader)
        sbuff.append(DeltaHeader + "\n");
    
    sbuff.append(String.format(DeltaFormat,
        portId,
        pc.getDelta_port_counter(xmit),
        PFM_PortRate.getChangeRateLong(pc, xmit, units),
        units.getName(),
        pc.getDelta_port_counter(rcv),
        PFM_PortRate.getChangeRateLong(pc, rcv, units),
        units.getName(),
        getMaxPortUtilization(pr)) + "\n");
  }
  return sbuff.toString();
  }
  

  
  public String  getLinkUtilizationString(IB_Edge edge)
  {
    double x = getLinkUtilization(edge, PFM_Port.PortCounterName.xmit_data);
    double r = getLinkUtilization(edge, PFM_Port.PortCounterName.rcv_data);
    PFM_Port.PortCounterName maxC = (x > r ? PFM_Port.PortCounterName.xmit_data:PFM_Port.PortCounterName.rcv_data);
    double maxN = (x > r ? x:r);

    return maxC.getName() + ": " + getDoubletUtilizationString(maxN);
  }
  public double getLinkUtilization(IB_Edge e, PFM_Port.PortCounterName pcn)
  {
    SummaryStatistics linkStats = new SummaryStatistics();
    
    // get both sides of the edge
    OSM_Port op1 = e.getEndPort1();
    OSM_Port op2 = e.getEndPort2();
    
    linkStats.addValue(this.getPortUtilization(op1.getNodeGuid(), op1.getPortNumber(), pcn));
    linkStats.addValue(this.getPortUtilization(op2.getNodeGuid(), op2.getPortNumber(), pcn));
    
    return linkStats.getMax();
  }
  
  /*******************************************************************************************************/  
  public String  getPathRateUtilizationSummary(RT_Path path, boolean toDestination)
  {
    // An ordered set of links.
    // Each has two ports (in and out), and each port has xmit and rcv
    
/*    guid:port  xmit delta (counts) xmit rate (units) rcv delta (counts) rcv rate (units) %max rate
 * 
 *    endport 1
 *    endport 2
 *    
 *    header is just the id string t1: t0: and max rate:
 *    
 *    refer to node utilization
 */
    
 //   String PathHeader = "path: %-36s  t1: %s   t0: %s     max rate: %s (%s)";
    StringBuffer sbuff = new StringBuffer();
    
    
    if(toDestination)
      sbuff.append("Transmit Path (to dst)\n");
    else
      sbuff.append("Receive Path (from dst)\n");
    ArrayList<RT_PathLeg> legs = path.getLegs();
    
    // iterate through the legs
    int hopNum  = 0;
    int numHops = legs.size();
    for(RT_PathLeg leg : legs)
    {
    OSM_Port p1 = leg.getFromPort();
    OSM_Port p2 = leg.getToPort();
    String nodeName = truncate(getIB_Vertex(p1.getNodeGuid()).getName(), MaxNameSize);  
    String key = PFM_PortChange.getPFM_PortChangeKey(p1.getNodeGuid(), p1.getPortNumber());
    PFM_PortRate pr = PortRates.get(key);
    if(pr != null)
    {
      // the hop line (if first one, include a header)
      sbuff.append(getPathRateUtilizationLine(path.getPathIdString(), leg, hopNum, hopNum==0));
      
      if(++hopNum >= numHops)
      {
        // print the final destination line? (check PathRateUtilizationLine() for formatting clues
        String dstFormat = "%" + MaxHopSize + "s  %-" + MaxNameSize + "s %-22s";
        String dstHop = "dst-" + Integer.toString(hopNum).trim();
        nodeName = truncate(getIB_Vertex(p2.getNodeGuid()).getName(), MaxNameSize);
        sbuff.append(String.format(dstFormat,
            dstHop,
            nodeName,
            p2.getNodeGuid().toColonString() + "\n"));
        break;
      }
    }
    else
      sbuff.append("  please be patient, rates not yet available\n");

    }
  return sbuff.toString();
  }
 
  /************************************************************
   * Method Name:
   *  getPathRateUtilizationLine
  **/
  /**
   * Generates a counter string similar to the one shown below.  Conditionally
   * generates a header string prepended to the counter string, also shown
   * below.
   * 
   * <pre>
   *  counter       Oct 22 11:15:04 2013     Oct 22 11:12:04 2013     delta (counts)    rate (units)   % max rate
   *xmit_data         324889040703834          324889040210045            493789            0  MB/s       0.00%
   *</pre>
   *
   * @see     describe related java objects
   *
   * @param pr                the rate of change for this port
   * @param pcn               the desired counter
   * @param includeLineHeader true, if you want the header prepended, normally only do this the first time
   * @return                  a rate string, in quasi table form, representing a counter for a specific port
   ***********************************************************/
  public String getPathRateUtilizationLine(String pathId, RT_PathLeg leg, int hopNum, boolean includeLineHeader)
  {
    // transmit data counter only (one direction - source to destination)
    PFM_Port.PortCounterName xmit = PortCounterName.xmit_data;
    PortCounterUnits units = PortCounterUnits.MEGABYTES;
    
    String PathHeader = " path: %-36s  t1: %s   t0: %s     max rate: %s (%s)";
    String DeltaHeader = " hop#               node                               output port                  xmit delta (counts)      xmit rate (units)    % rate";
    String DeltaFormat     = "%-" + MaxHopSize + "s  %-" + MaxNameSize + "s %-22s         %16d           %10d %4s/s     %6.2f%%";
    StringBuffer sbuff = new StringBuffer();
    
    OSM_Port p1 = leg.getFromPort();
    OSM_Port p2 = leg.getToPort();
    
    // make sure the name isn't too long
    String nodeName = truncate(getIB_Vertex(p1.getNodeGuid()).getName().trim(), MaxNameSize);
    
    String portId = PFM_PortChange.getPFM_PortChangeKey(p1.getNodeGuid(), p1.getPortNumber()).trim();
    PFM_PortRate pr = PortRates.get(portId);
    
  if(pr != null)
  {
    PFM_PortChange pc = pr.getPortChange();
    
    if(includeLineHeader)
    {
      // the page header
      PFM_Port port1 = pr.getPortChange().getPort1();
      PFM_Port port2 = pr.getPortChange().getPort2();
      sbuff.append(String.format(PathHeader, pathId, port1.getCounterTimeStamp().toString(), port2.getCounterTimeStamp().toString(), getTheoreticalMaxRate().getRateName(), this.getTheoreticalMaxRateString(PortCounterUnits.MEGABYTES)) + "\n\n");
      sbuff.append(DeltaHeader + "\n");
    }
    
    String hop = hopNum == 0 ? "src-0" : Integer.toString(hopNum).trim();
    
    sbuff.append(String.format(DeltaFormat,
        hop,
        nodeName,
        portId,
        pc.getDelta_port_counter(xmit),
        PFM_PortRate.getChangeRateLong(pc, xmit, units),
        units.getName(),
        getPortUtilization(pr, xmit)) + "\n");
  }
  return sbuff.toString();
  }
  

  public String getPathRateUtilizationTableLine(String pathId, RT_PathLeg leg, int hopNum, boolean destination)
  {
    // transmit data counter only (one direction - source to destination)
    PFM_Port.PortCounterName xmit = PortCounterName.xmit_data;
    PortCounterUnits units = PortCounterUnits.MEGABYTES;
    
    StringBuffer sbuff = new StringBuffer();
    
    OSM_Port p1 = leg.getFromPort();
    OSM_Port p2 = leg.getToPort();
    
    // make sure the name isn't too long
    String nodeName = truncate(getIB_Vertex(p1.getNodeGuid()).getName().trim(), MaxNameSize);
    
    String portId = PFM_PortChange.getPFM_PortChangeKey(p1.getNodeGuid(), p1.getPortNumber()).trim();
    PFM_PortRate pr = PortRates.get(portId);
    
  if(pr != null)
  {
    PFM_PortChange pc = pr.getPortChange();
    
    String hop = hopNum == 0 ? "src-0" : Integer.toString(hopNum).trim();
    String ID   = "<a href=\"" + portId                             + "\">" + hop + "</a>";
//  String ID   = "<a href=\"" + g.toColonString() + ":" + pNum +"\">" + ++errNum + "</a>";
//    ID = hop;

    
    if(destination)
    {
      ID = "dst-" + hop;
      nodeName = truncate(getIB_Vertex(p2.getNodeGuid()).getName(), MaxNameSize);
      portId = p2.getNodeGuid().toColonString();
      
      
      sbuff.append("<tr>");
      sbuff.append("<td align=\"center\">" + ID + "</td>");
      sbuff.append("<td align=\"left\">"   + nodeName + "</td>");
      sbuff.append("<td align=\"left\">"   + portId + "</td>");
      sbuff.append("<td align=\"right\">"  + " " + "</td>");
      sbuff.append("<td align=\"right\">"  + " " + "</td>");
      sbuff.append("<td align=\"center\">" + " " + "</td>");
      sbuff.append("<td align=\"right\">"  + " " + "</td>");
    }
    else
    {
      sbuff.append("<tr>");
      sbuff.append("<td align=\"center\">" + ID + "</td>");
      sbuff.append("<td align=\"left\">"   + nodeName + "</td>");
      sbuff.append("<td align=\"left\">"   + portId + "</td>");
      sbuff.append("<td align=\"right\">"  + pc.getDelta_port_counter(xmit) + "</td>");
      sbuff.append("<td align=\"right\">"  + PFM_PortRate.getChangeRateLong(pc, xmit, units) + "</td>");
      sbuff.append("<td align=\"center\">" + units.getName() + "/s" + "</td>");
      sbuff.append("<td align=\"right\">"  + String.format("%6.2f %%", getPortUtilization(pr, xmit)) + "</td>");

      
    }
    

  }
  return sbuff.toString();
  }
  

  
  public String  getPathUtilizationString(RT_Path path)
  {
    // paths are directional (unlike links) so only examine xmit data
    PFM_Port.PortCounterName maxC = PFM_Port.PortCounterName.xmit_data;
    double x = getPathUtilization(path, maxC);
    return maxC.getName() + ": " + getDoubletUtilizationString(x);
  }
  
  public double getPathUtilization(RT_Path path, PFM_Port.PortCounterName pcn)
  {
    // walk the path, and return the maximum utilization number for any leg
    SummaryStatistics linkStats = new SummaryStatistics();
    
    ArrayList<RT_PathLeg> legs = path.getLegs();
    
    // iterate through the legs
    for(RT_PathLeg leg : legs)
    {
      OSM_Port p1 = leg.getFromPort();
      String portId = PFM_PortChange.getPFM_PortChangeKey(p1.getNodeGuid(), p1.getPortNumber());
      PFM_PortRate pr = PortRates.get(portId);

      linkStats.addValue(getPortUtilization(pr, pcn));
    }
    return linkStats.getMax();
  }
  
  /*******************************************************************************************************/  

  
  public String getDoubletUtilizationString(double val)
  {
    return String.format("%6.2f%%", val);
  }
  
  
  
    private BinList<PFM_PortRate>  getFabricRateUtilizationBins(int numBins, OSM_NodeType includeTypes)
    {
    // assume I have a Delta, and I want to compute the BW's and put them
    // in a fixed number of bins.
      
      // only include the ports from the specified node types.  if unknown, include all
   
    // the object to return, should be the specified number of bins
    BinList<PFM_PortRate> UtilizationBins = new BinList<PFM_PortRate>();
    PortRates = new LinkedHashMap <String, PFM_PortRate>();
    
    // enforce min/max
    numBins = numBins <  1 ?  1: numBins;
    numBins = numBins > DEFAULT_NUM_BINS ? DEFAULT_NUM_BINS: numBins;
    
    // create the bin keys, from 0 to 100 %, linear, whole numbers

    // divide the max link rate up into the desired bins (0 - 100%)
    BinSize = 100/numBins;
    RateStats = new SummaryStatistics();
    
    if((ActiveTrafficPorts == null) || (ActiveTrafficPorts.entrySet() == null))
    {
      logger.severe("WHAT THE HECK!");
      if(ActiveTrafficPorts == null)
      {
        logger.severe("I have no idea why, but the active Traffic Ports from TOP seem to be null!");
       
      }
      else
      {
        logger.severe("Hmmm, the Active Traffic Ports is not null, but the entry set is, check out top!");
      }
      return UtilizationBins;
    }

   for (Map.Entry<String, PFM_PortChange> eMapEntry : ActiveTrafficPorts.entrySet())
   {
      PFM_PortChange pc = eMapEntry.getValue();
      String key = eMapEntry.getKey();
      // what type of node is this port from??
      boolean sPortType = isSwitchPort(getDelta(), pc.getPort1());
      
      // only add this value if the port change is from the desired type (or no desired type specified)
      if(includeThisPort(getDelta(), pc.getPort1(), includeTypes))
      {
        PFM_PortRate   pr = new PFM_PortRate(pc);
        PortRates.put(key, pr);
              
        // FIXME - decide which way is more correct
        
        // use xmit data by default or Max ??
        double U = getPortUtilization(pr, PFM_Port.PortCounterName.xmit_data);
        //double U = getMaxPortUtilization(pr);
        
        RateStats.addValue(U);
       
       // put this PortChange in the desired bin
       for(int k = BinSize; k < 100; k+= BinSize)
       {
         if(U < (double) k)
         {
           UtilizationBins.add(pr, Integer.toString(k));
           break;
         }
       }
      }
     }
    return UtilizationBins;
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
   * @throws ClassNotFoundException 
   * @throws IOException 
   * @throws FileNotFoundException 
   ***********************************************************/
  public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException
  {
    OSM_FabricDelta ofd = OSM_FabricDelta.readFabricDelta("/home/meier3/omsHistoryRepo/cab/201509251054.data.his");
    OSM_FabricDeltaAnalyzer ofda = new OSM_FabricDeltaAnalyzer(ofd);
    ofda.getFabricRateUtilizationBins();
    PortCounterName pcn = PFM_Port.PortCounterName.xmit_data;
    
    System.err.println(ofda.getFabricRateUtilizationSummary());
    System.err.println("Port utilization is: " + ofda.getPortUtilization(new IB_Guid("0006:6a00:e300:4436"), 14, pcn)+ "%");
    System.err.println("Link utilization is: " + ofda.getLinkUtilization(new IB_Guid("0006:6a00:e300:4436"), 14, pcn)+ "%");
    System.err.println("Node utilization is: " + ofda.getNodeUtilization(new IB_Guid("0006:6a00:e300:4436"), pcn)+ "%");
    System.err.println("Fabric utilization is: " + ofda.getFabricRateUtilizationShortSummary());
    System.err.println("MAX (" + ofda.getTheoreticalMaxRate().toString() + ")");
    System.err.println("MAX (" + ofda.getTheoreticalMaxRate().getRateName() + ")");
    System.err.println("MAX (" + ofda.getTheoreticalMaxRate().getRateValue(1) + ")");

  }

  public LinkedHashMap<String, IB_Vertex> getDynamicErrorVertexMap()
  {
    return AllNodeErrors;
  }

  public LinkedHashMap<String, IB_Edge> getDynamicErrorEdgeMap()
  {
    return AllLinkErrors;
  }

  public LinkedHashMap<String, IB_Vertex> getVertexMap()
  {
    return VertexMap;
  }

  public LinkedHashMap<String, IB_Edge> getEdgeMap()
  {
    return EdgeMap;
  }

  /************************************************************
   * Method Name:
   *  getActiveTrafficPorts
   **/
  /**
   * Returns the value of ActiveTrafficPorts
   *
   * @return the activeTrafficLinks
   *
   ***********************************************************/
  
  public LinkedHashMap<String, PFM_PortChange> getActiveTrafficPorts()
  {
    return ActiveTrafficPorts;
  }

  /************************************************************
   * Method Name:
   *  getActiveTrafficLinks
   **/
  /**
   * Returns the value of activeTrafficLinks
   *
   * @return the activeTrafficLinks
   *
   ***********************************************************/
  
  public LinkedHashMap<String, IB_Edge> getActiveTrafficLinks()
  {
    return ActiveTrafficLinks;
  }

  /************************************************************
   * Method Name:
   *  getActiveTrafficNodes
   **/
  /**
   * Returns the value of activeTrafficNodes
   *
   * @return the activeTrafficNodes
   *
   ***********************************************************/
  
  public LinkedHashMap<String, IB_Vertex> getActiveTrafficNodes()
  {
    return ActiveTrafficNodes;
  }

  public boolean hasDynamicError(IB_Guid guid, int portNum, PortCounterName name)
  {
    // return true, if the specified error counters value changed during
    // the last period.  otherwise return false
    
    if((AllPortErrors == null) || (AllPortErrors.entrySet() == null) || (AllPortErrors.entrySet().size() < 1))
      return false;

    String key = PFM_PortChange.getPFM_PortChangeKey(guid, portNum);
    PFM_PortChange pc = AllPortErrors.get(key);
    
     // look inside this port, if available, to see if the change happened
    // on the counter I care about
    if((pc != null) && (name != null))
    {
      // name must be contained in the ErrorCounter group
      if(PortCounterName.PFM_ERROR_COUNTERS.contains(name))
      {
      long change = pc.getDelta_port_counter(name);
      if(change > 0)
        return true;
      }
    }
    return false;
  }
  
  public boolean hasDynamicError(IB_Edge edge)
  {
    // return true if the guid from either endpoint is in the error list
    // otherwise return false
    
    if(edge == null)
      return false;
    
    if((hasDynamicError(edge.getEndPort1().getNodeGuid(), edge.getEndPort1().getPortNumber())) || (hasDynamicError(edge.getEndPort2().getNodeGuid(), edge.getEndPort2().getPortNumber())))
    {
      return true;
    }
    return false;
  }
  
  public boolean hasStaticError(IB_Guid guid, int portNum)
  {
    // return true, if any error counter value is non-zero
    OSM_Fabric f = getDelta().getFabric2();
    OSM_Port p = f.getOSM_Port(OSM_Fabric.getOSM_PortKey(guid.getGuid(), (short)portNum));
    if(p == null)
      return false;
    return p.hasError();
  }
  
  public boolean hasDynamicError(IB_Guid guid, int portNum)
  {
    // return true, if any error counter value changed during
    // the last period.  otherwise return false
    
    if((AllPortErrors == null) || (AllPortErrors.entrySet() == null) || (AllPortErrors.entrySet().size() < 1))
      return false;

    String key = PFM_PortChange.getPFM_PortChangeKey(guid, portNum);
    PFM_PortChange pc = AllPortErrors.get(key);

    // look inside this port, if available, to see if the change happened
    // on the counter I care about
    if(pc != null)
    {
      // if pc is not null, then something was found.  Is that enough?
      for(PortCounterName s : PortCounterName.PFM_ERROR_COUNTERS)
      {
        long change = pc.getDelta_port_counter(s);
        if(change > 0L)
         return true;
      }
    }
    return false;
  }
  
  public boolean hasDynamicError(IB_Guid guid)
  {
    // return true if this guid is in the error list
    // otherwise return false
    
    if((AllNodeErrors == null) || (AllNodeErrors.entrySet() == null) || (AllNodeErrors.entrySet().size() < 1))
      return false;
    
    for (Map.Entry<String, IB_Vertex> eMapEntry : AllNodeErrors.entrySet())
    {
      IB_Vertex v = eMapEntry.getValue();
      
      // if the guids match, return true immediately
      if(v.getGuid().equals(guid))
        return true;
    }
    return false;
  }

  public boolean isTopNode(IB_Guid guid, int topSize)
  {
    // return true if this guid is in the top traffic list
    // otherwise return false
    if((ActiveTrafficNodes == null) || (ActiveTrafficNodes.entrySet() == null) || (ActiveTrafficNodes.entrySet().size() < 1))
      return false;
    
    int topNum = 0;
    for (Map.Entry<String, IB_Vertex> eMapEntry : ActiveTrafficNodes.entrySet())
    {
      IB_Vertex v = eMapEntry.getValue();
      
      // if the guids match, return true immediately
      if(v.getGuid().equals(guid))
        return true;
      
      if(topNum++ > topSize)
        return false;
    }
    return false;
  }

  public boolean isTopPort(IB_Guid guid, int portNum, int topSize)
  {
    // return true if this guid is in the top traffic list
    // otherwise return false
    if((ActiveTrafficPorts == null) || (ActiveTrafficPorts.entrySet() == null) || (ActiveTrafficPorts.entrySet().size() < 1))
      return false;
    
    int topNum = 0;
    for (Map.Entry<String, PFM_PortChange> eMapEntry : ActiveTrafficPorts.entrySet())
    {
      PFM_PortChange pc = eMapEntry.getValue();
      IB_Guid g = pc.getAddress().getGuid();
      int pNum  = pc.getPortNumber();
      
      // if the supplied guid and portNum are in the list, print it out
      if((g != null) && (pNum > 0) && (g.equals(guid)) && (pNum == portNum))
        return true;
      // stop after the desired number of iterations, or when the end is reached
      if(topNum++ > topSize)
        return false;
    }
    return false;
  }

  public boolean isTopPort(OSM_Port port, int topSize)
  {
    // return true if this guid is in the top traffic list
    // otherwise return false
    if((ActiveTrafficPorts == null) || (ActiveTrafficPorts.entrySet() == null) || (ActiveTrafficPorts.entrySet().size() < 1))
      return false;
    
    if(port == null)
      return false;
    
    return isTopPort(port.getNodeGuid(), port.getPortNumber(), topSize);
  }

  public boolean isTopLink(IB_Edge edge, int topSize)
  {
    // return true if this guid is in the top traffic list
    // otherwise return false
    if((ActiveTrafficPorts == null) || (ActiveTrafficPorts.entrySet() == null) || (ActiveTrafficPorts.entrySet().size() < 1))
      return false;
    
    if(edge == null)
      return false;
    
    return (isTopPort(edge.getEndPort1(), topSize) || (isTopPort(edge.getEndPort2(), topSize)));
  }

  public boolean isTopLink(IB_Guid guid, int portNum, int topSize)
  {
    // find the TOP edge that has this guid and portnumber
    // otherwise return false
    if((ActiveTrafficPorts == null) || (ActiveTrafficPorts.entrySet() == null) || (ActiveTrafficPorts.entrySet().size() < 1))
      return false;
    
    if(guid == null)
      return false;
    
    return isTopLink(getIB_Edge(guid, portNum), topSize);
  }

  public String getPortErrorState(IB_Guid nodeGuid, int portNum)
  {
    // return one of: <empty>, Static Error, Dynamic Error
    String state = "";
    if(hasStaticError(nodeGuid, portNum))
      state = STATIC_ERROR;
    if(hasDynamicError(nodeGuid, portNum))
      state = DYNAMIC_ERROR;
    return state;
  }

  public String getLinkErrorState(IB_Edge e)
  {
    // return one of: <empty>, Static Error, Dynamic Error
    //
    // check both sides of the link, and return the HIGHEST error
    String state = "";
    if(e != null)
    {
      if(hasDynamicError(e.getEndPort1().getNodeGuid(), e.getEndPort1().getPortNumber()) ||
          hasDynamicError(e.getEndPort2().getNodeGuid(), e.getEndPort2().getPortNumber()))
            return DYNAMIC_ERROR;
       
      if(hasStaticError(e.getEndPort1().getNodeGuid(), e.getEndPort1().getPortNumber()) ||
          hasStaticError(e.getEndPort2().getNodeGuid(), e.getEndPort2().getPortNumber()))
            return STATIC_ERROR;
     }
    return state;
  }

  public String getLinkErrorState(IB_Link l)
  {
    // return one of: <empty>, Static Error, Dynamic Error
    //
    // check both sides of the link, and return the HIGHEST error
    String state = "";
    if(l != null)
    {
      if(hasDynamicError(l.getEndpoint1().getNodeGuid(), l.getEndpoint1().getPortNumber()) ||
          hasDynamicError(l.getEndpoint2().getNodeGuid(), l.getEndpoint2().getPortNumber()))
            return DYNAMIC_ERROR;
       
      if(hasStaticError(l.getEndpoint1().getNodeGuid(), l.getEndpoint1().getPortNumber()) ||
          hasStaticError(l.getEndpoint2().getNodeGuid(), l.getEndpoint2().getPortNumber()))
            return STATIC_ERROR;
     }
    return state;
  }

}
