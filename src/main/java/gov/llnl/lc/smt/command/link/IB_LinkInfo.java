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
 *        file: IB_LinkInfo.java
 *
 *  Created on: Sep 12, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command.link;

import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.core.IB_Link;
import gov.llnl.lc.infiniband.core.IB_LinkType;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_FabricDelta;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_FabricDeltaAnalyzer;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_LinkRate;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_LinkSpeed;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_LinkState;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_LinkWidth;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Node;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_NodeType;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Nodes;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_PortState;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Ports;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.infiniband.opensm.plugin.data.SBN_Node;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Edge;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;
import gov.llnl.lc.infiniband.opensm.plugin.net.OsmServerStatus;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.SmtConstants;
import gov.llnl.lc.util.BinList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**********************************************************************
 * Describe purpose and responsibility of IB_LinkInfo
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Sep 12, 2013 9:18:52 AM
 **********************************************************************/
public class IB_LinkInfo implements CommonLogger
{
  private IB_Edge Edge;      // the link of interest
  private IB_Vertex Vertex;  // the node of interest (must be one side of the link)
  
  private static final String ibli_format  = "  %3s %2d[%2s] ==(%32s)==> %3s %2d[%2s] %20s (%s)";
  private static final String ibli_format2 = " %20s %18s:%2d ==(%32s)==> %18s:%2d %20s (%s)";
  
  private String Warning;  // an error or message string for the link (optional)

  /************************************************************
   * Method Name:
   *  IB_LinkInfo
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param edge
   ***********************************************************/
  public IB_LinkInfo(IB_Edge edge)
  {
    this(edge, null);
  }
  
  public IB_LinkInfo(IB_Vertex vertex)
  {
    this(null, vertex);
  }
  
  public IB_LinkInfo(IB_Edge edge, IB_Vertex vertex)
  {
    super();
    Edge   = edge;
    // TODO, only set Vertex if one side of the Edge (must be Endpoint1 or 2)
    // TODO, what if Edge doesn't exist (see constructor above)
    Vertex = vertex;
    Warning = "";
  }
  
  
  /************************************************************
   * Method Name:
   *  getWarning
   **/
  /**
   * Returns the value of warning
   *
   * @return the warning
   *
   ***********************************************************/
  
  public String getWarning()
  {
    return Warning;
  }

  /************************************************************
   * Method Name:
   *  setWarning
   **/
  /**
   * Sets the value of warning
   *
   * @param warning the warning to set
   *
   ***********************************************************/
  public void setWarning(String warning)
  {
    Warning = warning;
  }

  public int getPortNumber()
  {
    int rtnval = -1;
    
    // if there is a vertex associated with this Edge, then return the port Number
    if((Vertex != null) && (Edge != null))
    {
      OSM_Port p = Edge.getEndPort(Vertex);
      if(p != null)
        rtnval = p.getPortNumber();
    }
    
    return rtnval;
  }
  
  public IB_Guid getGuid()
  {
    IB_Guid rtnval = null;
    
    // if there is a vertex associated with this Edge, then return its guid
    if(Vertex != null)
      rtnval = Vertex.getGuid();
    
    return rtnval;
  }
  
  public String getNodeInfo()
  {
    if(Vertex == null)
      return null;
    return Vertex.getNodeType() + " " + Vertex.getGuid().toColonString() + " " + Vertex.getName();
  }
  
  public static String getMissingLinkInfo(int portNum, String info)
  {
    return String.format(ibli_format, "   ", portNum, "  ", info, "  ",-1, "  ", "unknown", "");
  }
  
  public String getMissingLinkInfo(int portNum)
  {
    return getMissingLinkInfo(portNum, getMissingLinkKey(portNum) );
  }
  
  public String getMissingLinkKey(int portNum)
  {
//    return getLinkKey() + "m" + portNum;
    return Vertex.getGuid().toColonString() + ":" + portNum;
  }
  
  private String getLinkInfoLine(OpenSmMonitorService oms)
  {
    // get the name and lids
    if((Vertex == null) || (Edge == null))
      return "invalid or missing link";
    
    OSM_Port l_port = Edge.getEndPort(Vertex);
    OSM_Port r_port = Edge.getEndPort1();
    
    IB_Guid lg = l_port.getAddress().getGuid();
    if(lg != null)
    {
      if(lg.equals(Edge.getEndPort1().getAddress().getGuid()))
        r_port = Edge.getEndPort2();
    }
    IB_Guid rg = r_port.getAddress().getGuid();
    IB_Vertex r_Vertex = Edge.getEndpoint(r_port);
    
    int l_LID = oms.getFabric().getLidFromGuid(lg);
    int l_pn  = l_port.getPortNumber();
    String l_epn = "  ";
    String l_info = Edge.getIB_Link().toLinkInfo();
    int r_LID = oms.getFabric().getLidFromGuid(rg);
    int r_pn = r_port.getPortNumber();
    String r_epn = "  ";
    String l_name = Vertex.getName();
    String r_name = r_Vertex.getName();
    String warning = this.getWarning();
    String sIbli = String.format(ibli_format2, l_name, lg.toColonString(), l_pn, l_info, rg.toColonString(), r_pn, r_name, warning);
    
    return sIbli;
  }
  
  public String getLinkInfo()
  {
    // the link has two sides, only one of which corresponds to the vertex
    if((Vertex == null) || (Edge == null))
      return "invalid or missing link";
    
    OSM_Port l_port = Edge.getEndPort(Vertex);
    OSM_Port r_port = Edge.getEndPort1();
    
    IB_Guid lg = l_port.getAddress().getGuid();
    if(lg != null)
    {
      if(lg.equals(Edge.getEndPort1().getAddress().getGuid()))
        r_port = Edge.getEndPort2();
    }

    
    /*    
             56    1[  ] ==( 4X 10.0 Gbps Active/  LinkUp)==>      44    1[  ] "woprjr0" ( )
  
                56    1[  ] ==( 4X 10.0 Gbps Active/  LinkUp)==>      44    1[  ] "woprjr0" ( )
              ^^^  ^^^^^^     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^         ^^^   ^^^^^^  ^^^^^^^  ^^^^
              LID  Port       Link Info                            LID   Port     Name    Opt Warn!
                   [External port]                                 REMOTE node info

   Going from left to right:

   LID       : lid for the node
   Port      : port that had errors
   [  ]      : If there is a number here it is the EXTERNAL port.  (Remember
         that Voltaire switches have a weird internal to external
         mapping.)
   Link Info : information about the link such as width/speed and state.
   LID       : LID of the REMOTE node
   Port      : Port of the REMOTE node
   [  ]      : Again an Optional external port
   Name      : name of the REMOTE node
   Opt Warn  : A Optional warning that this particular link is running at a
               reduced state of some sort.

    
*/
    
    // local port for this vertex is l_port, and remote port is r_port
    
    IB_Vertex r_Vertex = Edge.getEndpoint(r_port);
    
    String l_LID = "   ";
    int l_pn  = l_port.getPortNumber();
    String l_epn = "  ";
    String l_info = Edge.getIB_Link().toLinkInfo();
    String r_LID = "   ";
    int r_pn = r_port.getPortNumber();
    String r_epn = "  ";
    String r_name = r_Vertex.getName();
    String warning = this.getWarning();
    String sIbli = String.format(ibli_format, l_LID, l_pn, l_epn, l_info, r_LID, r_pn, r_epn, r_name, warning);
    
    return sIbli;
  }

  public String getLinkKey()
  {
    // this should be the same as the port key, specifically guid:portnum
    // need both
    if((Edge == null) || (Vertex == null))
      return null;
    
    OSM_Port port = Edge.getEndPort(Vertex);
    int portNum = port != null ? port.getPortNumber(): -6;
    // the link has two sides, only one of which corresponds to the vertex
    return Vertex.getGuid().toColonString() + ":" + portNum;
  }

  public String getNodeKey()
  {
    if(Vertex == null)
      return null;
    return Vertex.getGuid().toColonString() + ":" + "-1";
  }

  public static LinkedHashMap<String, String> getLinkInfoRecordsByPort(OSM_Port p, OpenSmMonitorService oms, OSM_FabricDelta fd)
  {
    LinkedHashMap <String, String> map = new LinkedHashMap<String, String>();
    
    if( p == null)
      return map;
    
    OSM_Fabric Fabric = oms.getFabric();
    
    // from this edge map, create the vertex map (this sets the levels too)
    LinkedHashMap<String, IB_Vertex> vertexMap = IB_Vertex.createVertexMap(Fabric);
    LinkedHashMap <String, IB_Edge> edgeMap = IB_Vertex.createEdgeMap(IB_Vertex.createVertexMap(Fabric));
    
    // print the record for the edge.  Include both sides, because I don't have a vertex
    IB_Vertex vertex = IB_Vertex.getVertex(p, vertexMap);
    IB_Edge edge     = IB_Edge.getEdge(p, edgeMap);
    IB_LinkInfo ili = new IB_LinkInfo(edge, vertex);

    if((fd != null) && (edge != null))
    {
      OSM_FabricDeltaAnalyzer fda = new OSM_FabricDeltaAnalyzer(fd);
      String errStr = fda.getLinkErrorState(edge);

      // find the matching delta port counters, and construct an error string for the LinkInfo
      ili.setWarning(errStr);
    }
    
    map.put(ili.getLinkKey(), ili.getLinkInfoLine(oms));
    return map;
  }
  
  private static LinkedHashMap<String, String> getLinkInfoRecords(IB_Edge edge, LinkedHashMap<String, IB_Vertex> vertexMap, OpenSmMonitorService oms, OSM_FabricDelta fd)
  {
    LinkedHashMap <String, String> map = new LinkedHashMap<String, String>();
    
    if( edge == null)
      return map;
    
    // print the record for the edge.  Include both sides, because I don't have a vertex
    OSM_Port p = edge.getEndPort1();
    IB_Vertex vertex = IB_Vertex.getVertex(p, vertexMap);
    IB_LinkInfo ili = null;

    ili = new IB_LinkInfo(edge, vertex);
    
    OSM_FabricDeltaAnalyzer fda = (fd == null) ? null: new OSM_FabricDeltaAnalyzer(fd);
      
    // attempt to add the warning info
    if(fda != null)
      ili.setWarning(fda.getLinkErrorState(edge));
     
    map.put(ili.getLinkKey(), ili.getLinkInfoLine(oms));
    return map;
  }
  
  private static LinkedHashMap<String, String> getLinkInfoRecords(IB_Vertex vertex, HashMap <String, OSM_Port> pMap, OSM_FabricDeltaAnalyzer fda, boolean includeGoodLinks, boolean includeMissingLinks)
  {
    LinkedHashMap <String, String> map = new LinkedHashMap<String, String>();
    
    // both can't be false
    if((!includeGoodLinks && !includeMissingLinks) || vertex == null)
      return map;
    
    
    // includeGoodLinks    include missingLinks        result
    //========================================================
    //        false                false                empty   (non-sense)
    //        false                true                 only missing links
    //        true                 false                only good links
    //        true                 true                 all links (default)
    
    // iterate through the edges in the vertex, and build the map
    // use the guid and port # for keys, and node port is -1
    ArrayList <IB_Edge> el = vertex.getEdges();

    // sort these based on this vertex's port number values
    if (el.size() > 1)
    {
      // sort the list of active error ports
      try
      {
        Collections.sort(el, new IB_EdgeVertexPortComparator(vertex));
      }
      catch (Exception e)
      {
        logger.severe("Caught an exception while sorting");
      }
//      Collections.reverse(eList);
    }
    
    // TODO this is the header, so only put this if there are links (check include flags)
    IB_LinkInfo ilh = new IB_LinkInfo(vertex);
    IB_LinkInfo ili = null;
    
    // if the resultant size is fewer than the vertex's number of ports
    // then we could be missing information, be verbose
    int nPorts = vertex.getNode().pfmNode.num_ports;
    int nEdges = el.size();
    IB_Edge e = null;
    
    // never attempt to include ESP0
    if(vertex.getNode().pfmNode.isEsp0())
      nPorts--;
    
     boolean init = false;
    
    int pn = 1;
    for(int i = 0; (i < nEdges) || (pn <= nPorts); i++, pn++)
    {
      if(i < nEdges)
        e = el.get(i);
      
      ili = new IB_LinkInfo(e, vertex);
      
      // attempt to add the warning info
      if(fda != null)
        ili.setWarning(fda.getLinkErrorState(e));
      
      // does this port number match the index
      if(pn == ili.getPortNumber())
      {
        // consider this a good link, do we include it?
        if(includeGoodLinks)
        {
          if(!init)
          {
            // the node line (header)
            map.put(ilh.getNodeKey(), ilh.getNodeInfo());
            init = true;
          }
          // the link line
          map.put(ili.getLinkKey(), ili.getLinkInfo());
        }
      }
      else
      {
        // consider this a missing link, do we include it
        if(includeMissingLinks)
        {
          if(!init)
          {
            // the node line (header)
            map.put(ilh.getNodeKey(), ilh.getNodeInfo());
            init = true;
          }
          // the link line (use the ports state, if I can get it)
          OSM_Port p = pMap.get(ili.getMissingLinkKey(pn));
          if(p == null)
            map.put(ili.getMissingLinkKey(pn), ili.getMissingLinkInfo(pn));
          else
            map.put(ili.getMissingLinkKey(pn), getMissingLinkInfo(pn, OSM_PortState.get(p).getStateName()));
        }
        i--;
      }
    }
    return map;
  }
  
  public static LinkedHashMap<String, String> getCALinkInfoRecords(OpenSmMonitorService oms, OSM_FabricDelta fd, boolean includeGoodLinks, boolean includeMissingLinks)
  {
    return getLinkInfoRecords(oms, fd, 0, true, includeGoodLinks, includeMissingLinks);
  }

  public static LinkedHashMap<String, String> getSWLinkInfoRecords(OpenSmMonitorService oms, OSM_FabricDelta fd, boolean includeGoodLinks, boolean includeMissingLinks)
  {
    return getLinkInfoRecords(oms, fd, 0, false, includeGoodLinks, includeMissingLinks);
  }

  public static LinkedHashMap<String, String> getLinkInfoRecords(OpenSmMonitorService oms, OSM_FabricDelta fd,boolean includeGoodLinks, boolean includeMissingLinks)
  {
    return getLinkInfoRecords(oms, fd, 100, false, includeGoodLinks, includeMissingLinks);
  }

  public static LinkedHashMap<String, String> getDownPorts(OpenSmMonitorService oms)
  {
    LinkedHashMap <String, String> map = new LinkedHashMap<String, String>();
    OSM_Fabric Fabric = oms.getFabric();
    
    OSM_Nodes AllNodes = (Fabric == null) ? null : Fabric.getOsmNodes();
    OSM_Ports AllPorts = (Fabric == null) ? null : Fabric.getOsmPorts();
    
    HashMap <String, OSM_Node> nMap = OSM_Nodes.createOSM_NodeMap(AllNodes);
    HashMap <String, OSM_Port> pMap = OSM_Ports.createOSM_PortMap(AllNodes, AllPorts);
    
    LinkedHashMap <String, IB_Edge> edgeMap = IB_Edge.createEdgeMap(nMap, pMap);
    // from this edge map, create the vertex map (this sets the levels too)
    LinkedHashMap<String, IB_Vertex> vertexMap = IB_Vertex.createVertexMap(edgeMap, Fabric);
    IB_Vertex.setSBN_Switches(vertexMap, Fabric);

    for (Entry<String, OSM_Port> entry : pMap.entrySet())
    {
      OSM_Port p = entry.getValue();
      if((p != null) && (p.getState() == OSM_PortState.DOWN))
        System.err.println("This port (" + p.getOSM_PortKey() + ") is DOWN");
    }
    
return map;
  }

  public static LinkedHashMap<String, String> getLinkInfoRecordsByDepth(OpenSmMonitorService oms, OSM_FabricDelta fd, int Depth)
  {
    LinkedHashMap <String, String> map = new LinkedHashMap<String, String>();
    OSM_Fabric Fabric = oms.getFabric();
    
    // from this edge map, create the vertex map (this sets the levels too)
    LinkedHashMap<String, IB_Vertex> vertexMap = IB_Vertex.createVertexMap(Fabric);
    LinkedHashMap <String, IB_Edge> edgeMap = IB_Vertex.createEdgeMap(IB_Vertex.createVertexMap(Fabric));

     for (Entry<String, IB_Edge> entry : edgeMap.entrySet())
    {
      IB_Edge e = entry.getValue();
      boolean includeThis =  e.getDepth() == Depth;
      if(includeThis)
        map.putAll(IB_LinkInfo.getLinkInfoRecords(e, vertexMap, oms, fd));      
    }     
     return map;
  }

  public static LinkedHashMap<String, String> getLinkInfoRecordsByGuid(IB_Guid guid, OpenSmMonitorService oms, OSM_FabricDelta fd, boolean includeGoodLinks, boolean includeMissingLinks)
  {
    OSM_Fabric Fabric = oms.getFabric();
    
    OSM_Nodes AllNodes = (Fabric == null) ? null : Fabric.getOsmNodes();
    OSM_Ports AllPorts = (Fabric == null) ? null : Fabric.getOsmPorts();
    
    HashMap <String, OSM_Node> nMap = OSM_Nodes.createOSM_NodeMap(AllNodes);
    HashMap <String, OSM_Port> pMap = OSM_Ports.createOSM_PortMap(AllNodes, AllPorts);
    
    LinkedHashMap <String, IB_Edge> edgeMap = IB_Edge.createEdgeMap(nMap, pMap);
    // from this edge map, create the vertex map (this sets the levels too)
    LinkedHashMap<String, IB_Vertex> vertexMap = IB_Vertex.createVertexMap(edgeMap, Fabric);

    OSM_FabricDeltaAnalyzer fda = (fd == null) ? null: new OSM_FabricDeltaAnalyzer(fd);

    // find the Vertex that owns this guid
    String key       = IB_Vertex.getVertexKey(guid);
    IB_Vertex vertex = IB_Vertex.getVertex(key, vertexMap);

    return getLinkInfoRecords(vertex, pMap, fda, includeGoodLinks, includeMissingLinks);
  }

  public static String getStatus(OpenSmMonitorService OMService)
  {
    /* TODO create a HASHMAP or some sort of name value pair thing, that can be formatted later */
    
    // return a string representation of the link statistics, similar to the smt-console
    if(OMService == null)
    {
      logger.severe("Crap, its null!");
      return "Can't get status from a null object";
    }
    
    // "ALL" IB_Links
    ArrayList <IB_Link> ibla = null;
    
    int[] Lnum_nodes  = new int[4];
    int[] Lnum_ports  = new int[4];
    
    // Separate the links into the different types
    ArrayList <IB_Link> ibls = new ArrayList<IB_Link>();
    ArrayList <IB_Link> iblc = new ArrayList<IB_Link>();
    ArrayList <IB_Link> iblQ = new ArrayList<IB_Link>();  // unknown
   
    // put the various attribute counts in bins
    BinList <IB_Link> aLinkBins = new BinList <IB_Link>();
    BinList <IB_Link> sLinkBins = new BinList <IB_Link>();
    BinList <IB_Link> cLinkBins = new BinList <IB_Link>();
    BinList <IB_Link> QLinkBins = new BinList <IB_Link>(); // unknown

    
    OSM_Fabric Fabric = OMService.getFabric();

    OSM_Nodes AllNodes = (Fabric == null) ? null : Fabric.getOsmNodes();
    OSM_Ports AllPorts = (Fabric == null) ? null : Fabric.getOsmPorts();

    if ((AllPorts != null) && (AllNodes != null))
    {
      // some link attributes depend on perfmgr data, so wait until available
      if ((AllNodes.getSubnNodes() != null) && (AllNodes.getPerfMgrNodes() != null))
      {
        /*
         * init flag - set true
         */
 
        // clear the counters
        for (int d = 0; d < 4; d++)
        {
          Lnum_nodes[d] = 0;
          Lnum_ports[d] = 0;
        }
        ArrayList<SBN_Node> sbna = new ArrayList<SBN_Node>(Arrays.asList(AllNodes.getSubnNodes()));
        for (SBN_Node sn : sbna)
        {
          if (OSM_NodeType.get(sn) == OSM_NodeType.SW_NODE)
          {
            Lnum_nodes[0] += 1;
            Lnum_ports[0] += sn.num_ports;
          }
          else if (OSM_NodeType.get(sn) == OSM_NodeType.CA_NODE)
          {
            Lnum_nodes[1] += 1;
            Lnum_ports[1] += sn.num_ports;
          }
          else
          {
            Lnum_nodes[2] += 1;
            Lnum_ports[2] += sn.num_ports;
          }
        }

        // create IB_Links
        ibla = AllPorts.createIB_Links(AllNodes);

        // clear all other data structures (arrays and binLists)
        ibls = new ArrayList<IB_Link>();
        iblc = new ArrayList<IB_Link>();
        iblQ = new ArrayList<IB_Link>(); // unknown
        aLinkBins = new BinList<IB_Link>();
        sLinkBins = new BinList<IB_Link>();
        cLinkBins = new BinList<IB_Link>();
        QLinkBins = new BinList<IB_Link>(); // unknown

        for (IB_Link link : ibla)
        {
          // create a list of switch and edge links
          if (link.getLinkType() == IB_LinkType.SW_LINK)
            ibls.add(link);
          else if (link.getLinkType() == IB_LinkType.CA_LINK)
            iblc.add(link);
          else
            iblQ.add(link);

          // bin up the types for ALL links
          if (link.hasTraffic())
            aLinkBins.add(link, "Traffic:");

          if (link.hasErrors())
            aLinkBins.add(link, "Errors:");

          aLinkBins.add(link, "State: " + link.getState().getStateName());
          aLinkBins.add(link, "Speed: " + link.getSpeed().getSpeedName());
          aLinkBins.add(link, "Width: " + link.getWidth().getWidthName());
          aLinkBins.add(link, "Rate: " + link.getRate().getRateName());
        }

        for (IB_Link link : ibls)
        {
          // bin up the types for SW links
          if (link.hasTraffic())
            sLinkBins.add(link, "Traffic:");

          if (link.hasErrors())
            sLinkBins.add(link, "Errors:");

          sLinkBins.add(link, "State: " + link.getState().getStateName());
          sLinkBins.add(link, "Speed: " + link.getSpeed().getSpeedName());
          sLinkBins.add(link, "Width: " + link.getWidth().getWidthName());
          sLinkBins.add(link, "Rate: " + link.getRate().getRateName());
        }

        for (IB_Link link : iblc)
        {
          // bin up the types for CA links
          if (link.hasTraffic())
            cLinkBins.add(link, "Traffic:");

          if (link.hasErrors())
            cLinkBins.add(link, "Errors:");

          cLinkBins.add(link, "State: " + link.getState().getStateName());
          cLinkBins.add(link, "Speed: " + link.getSpeed().getSpeedName());
          cLinkBins.add(link, "Width: " + link.getWidth().getWidthName());
          cLinkBins.add(link, "Rate: " + link.getRate().getRateName());
        }

        for (IB_Link link : iblQ)
        {
          // bin up the types for CA links
          if (link.hasTraffic())
            QLinkBins.add(link, "Traffic:");

          if (link.hasErrors())
            QLinkBins.add(link, "Errors:");

          QLinkBins.add(link, "State: " + link.getState().getStateName());
          QLinkBins.add(link, "Speed: " + link.getSpeed().getSpeedName());
          QLinkBins.add(link, "Width: " + link.getWidth().getWidthName());
          QLinkBins.add(link, "Rate: " + link.getRate().getRateName());
        }
      }
      else
        logger.warning("UD: PerfMgr data is not available... yet");

      // done processing, get ready for painting
      Lnum_nodes[3] = AllNodes.SubnNodes.length;
      Lnum_ports[3] = AllPorts.SubnPorts.length;
  }
  else
  {
    logger.warning("UD: The Node and Port info seems to be unavailable");
  }


/*****************************************************************************/
// all done calculating, now format to look like page 6
    StringBuffer buff = new StringBuffer();
    
    int[] num_links = new int[4];
    int[] num_active = new int[4];
    int[] num_down = new int[4];
    int[] num_traffic = new int[4];
    int[] num_errors = new int[4];

    if ((AllPorts != null) && (AllNodes != null))
    {
      // some link attributes depend on perfmgr data, so wait until available
      if ((AllNodes.getSubnNodes() != null) && (AllNodes.getPerfMgrNodes() != null) && (ibla != null))
      {
        num_links[0] = ibls.size();
        num_links[1] = iblc.size();
        num_links[2] = iblQ.size();
        num_links[3] = ibla.size();

        // the various Bins may not exist (if there was no element to add the
        // bin doesn't get created) so protect against null
        num_active[0] = sLinkBins.getBin("State: " + OSM_LinkState.ACTIVE.getStateName()) == null ? 0
            : sLinkBins.getBin("State: " + OSM_LinkState.ACTIVE.getStateName()).size();
        num_active[1] = cLinkBins.getBin("State: " + OSM_LinkState.ACTIVE.getStateName()) == null ? 0
            : cLinkBins.getBin("State: " + OSM_LinkState.ACTIVE.getStateName()).size();
        num_active[2] = QLinkBins.getBin("State: " + OSM_LinkState.ACTIVE.getStateName()) == null ? 0
            : QLinkBins.getBin("State: " + OSM_LinkState.ACTIVE.getStateName()).size();
        num_active[3] = aLinkBins.getBin("State: " + OSM_LinkState.ACTIVE.getStateName()) == null ? 0
            : aLinkBins.getBin("State: " + OSM_LinkState.ACTIVE.getStateName()).size();

        num_down[0] = sLinkBins.getBin("State: " + OSM_LinkState.DOWN.getStateName()) == null ? 0
            : sLinkBins.getBin("State: " + OSM_LinkState.DOWN.getStateName()).size();
        num_down[1] = cLinkBins.getBin("State: " + OSM_LinkState.DOWN.getStateName()) == null ? 0
            : cLinkBins.getBin("State: " + OSM_LinkState.DOWN.getStateName()).size();
        num_down[2] = QLinkBins.getBin("State: " + OSM_LinkState.DOWN.getStateName()) == null ? 0
            : QLinkBins.getBin("State: " + OSM_LinkState.DOWN.getStateName()).size();
        num_down[3] = aLinkBins.getBin("State: " + OSM_LinkState.DOWN.getStateName()) == null ? 0
            : aLinkBins.getBin("State: " + OSM_LinkState.DOWN.getStateName()).size();

        num_traffic[0] = sLinkBins.getBin("Traffic:") == null ? 0 : sLinkBins.getBin("Traffic:")
            .size();
        num_traffic[1] = cLinkBins.getBin("Traffic:") == null ? 0 : cLinkBins.getBin("Traffic:")
            .size();
        num_traffic[2] = QLinkBins.getBin("Traffic:") == null ? 0 : QLinkBins.getBin("Traffic:")
            .size();
        num_traffic[3] = aLinkBins.getBin("Traffic:") == null ? 0 : aLinkBins.getBin("Traffic:")
            .size();

        num_errors[0] = sLinkBins.getBin("Errors:") == null ? 0 : sLinkBins.getBin("Errors:")
            .size();
        num_errors[1] = cLinkBins.getBin("Errors:") == null ? 0 : cLinkBins.getBin("Errors:")
            .size();
        num_errors[2] = QLinkBins.getBin("Errors:") == null ? 0 : QLinkBins.getBin("Errors:")
            .size();
        num_errors[3] = aLinkBins.getBin("Errors:") == null ? 0 : aLinkBins.getBin("Errors:")
            .size();

        // total nodes, ports and links, broken down by type
        OsmServerStatus RStatus = OMService.getRemoteServerStatus();

        buff.append(String.format("                      Link Status\n"));
        buff.append(String.format("\n"));
        buff.append(String.format("Fabric Name:                     %20s\n", Fabric.getFabricName()));
        if(RStatus != null)
          buff.append(String.format("Up since:                        %20s\n", OMService.getRemoteServerStatus().Server.getStartTime().toString() ));
        buff.append(String.format("timestamp:                       %20s\n", Fabric.getTimeStamp().toString() ));
        buff.append(SmtConstants.NEW_LINE);

        buff.append(String.format("--attribute--------SW--------CA---------?--------All--\n"));
          buff.append(String.format("Total Nodes:     %4d      %4d      %4d      %5d\n", Lnum_nodes[0], Lnum_nodes[1], Lnum_nodes[2], Lnum_nodes[3])); 
          buff.append(String.format("Total Ports:     %4d      %4d      %4d      %5d\n", Lnum_ports[0], Lnum_ports[1], Lnum_ports[2], Lnum_ports[3])); 
          buff.append(String.format("Total Links:     %4d      %4d      %4d      %5d\n", num_links[0], num_links[1], num_links[2], num_links[3])); 
          buff.append(String.format("\n")); 
          buff.append(String.format("Active:          %4d      %4d      %4d      %5d\n", num_active[0], num_active[1], num_active[2], num_active[3])); 
          buff.append(String.format("Down:            %4d      %4d      %4d      %5d\n", num_down[0], num_down[1], num_down[2], num_down[3])); 
          buff.append(String.format("\n")); 
          buff.append(String.format("traffic:         %4d      %4d      %4d      %5d\n", num_traffic[0], num_traffic[1], num_traffic[2], num_traffic[3])); 
          buff.append(String.format("errors:          %4d      %4d      %4d      %5d\n", num_errors[0], num_errors[1], num_errors[2], num_errors[3])); 
 
        // how many unique widths (must be at least one)?
        buff.append(String.format("\n--width--\n"));
        
        for (OSM_LinkWidth lw : OSM_LinkWidth.OSMLINK_ALL_WIDTHS)
        {
          ArrayList<IB_Link> la = aLinkBins.getBin("Width: " + lw.getWidthName());
          // place a background if necessary
          if (la != null)
          {
            // there is at least one of these, so loop through all types
            num_links[0] = sLinkBins.getBin("Width: " + lw.getWidthName()) == null ? 0 : sLinkBins
                .getBin("Width: " + lw.getWidthName()).size();
            num_links[1] = cLinkBins.getBin("Width: " + lw.getWidthName()) == null ? 0 : cLinkBins
                .getBin("Width: " + lw.getWidthName()).size();
            num_links[2] = QLinkBins.getBin("Width: " + lw.getWidthName()) == null ? 0 : QLinkBins
                .getBin("Width: " + lw.getWidthName()).size();
            num_links[3] = aLinkBins.getBin("Width: " + lw.getWidthName()) == null ? 0 : aLinkBins
                .getBin("Width: " + lw.getWidthName()).size();

            buff.append(String.format("%7s          %4d      %4d      %4d      %5d\n", lw.getWidthName(), num_links[0], num_links[1], num_links[2], num_links[3])); 
          }
        }

        // how many unique speeds (must be at least one)?
        buff.append(String.format("\n--speed--\n"));
 
        for (OSM_LinkSpeed ls : OSM_LinkSpeed.OSMLINK_ALL_SPEEDS)
        {
          ArrayList<IB_Link> la = aLinkBins.getBin("Speed: " + ls.getSpeedName());
          // place a background if necessary
          if (la != null)
          {
            // there is at least one of these, so loop through all types
            num_links[0] = sLinkBins.getBin("Speed: " + ls.getSpeedName()) == null ? 0 : sLinkBins
                .getBin("Speed: " + ls.getSpeedName()).size();
            num_links[1] = cLinkBins.getBin("Speed: " + ls.getSpeedName()) == null ? 0 : cLinkBins
                .getBin("Speed: " + ls.getSpeedName()).size();
            num_links[2] = QLinkBins.getBin("Speed: " + ls.getSpeedName()) == null ? 0 : QLinkBins
                .getBin("Speed: " + ls.getSpeedName()).size();
            num_links[3] = aLinkBins.getBin("Speed: " + ls.getSpeedName()) == null ? 0 : aLinkBins
                .getBin("Speed: " + ls.getSpeedName()).size();

            buff.append(String.format("%7s          %4d      %4d      %4d      %5d\n", ls.getSpeedName(), num_links[0], num_links[1], num_links[2], num_links[3])); 
          }
        }

        // how many unique rates (must be at least one)?
        buff.append(String.format("\n--rate---\n"));

        for (OSM_LinkRate lw : OSM_LinkRate.OSMLINK_UNIQUE_RATES)
        {
          ArrayList<IB_Link> la = aLinkBins.getBin("Rate: " + lw.getRateName());
          // place a background if necessary
          if (la != null)
          {
            // there is at least one of these, so loop through all types
            num_links[0] = sLinkBins.getBin("Rate: " + lw.getRateName()) == null ? 0 : sLinkBins
                .getBin("Rate: " + lw.getRateName()).size();
            num_links[1] = cLinkBins.getBin("Rate: " + lw.getRateName()) == null ? 0 : cLinkBins
                .getBin("Rate: " + lw.getRateName()).size();
            num_links[2] = QLinkBins.getBin("Rate: " + lw.getRateName()) == null ? 0 : QLinkBins
                .getBin("Rate: " + lw.getRateName()).size();
            num_links[3] = aLinkBins.getBin("Rate: " + lw.getRateName()) == null ? 0 : aLinkBins
                .getBin("Rate: " + lw.getRateName()).size();

            buff.append(String.format("%7s          %4d      %4d      %4d      %5d\n", lw.getRateName(), num_links[0], num_links[1], num_links[2], num_links[3])); 
          }
        }
      }
      else
        logger.warning("PF: PerfMgr data is not available... yet");
    }
    else
    {
      logger.warning("PF: The Node and Port info seems to be unavailable");
    }
    return buff.toString();
  }
  

  private static LinkedHashMap<String, String> getLinkInfoRecords(OpenSmMonitorService oms, OSM_FabricDelta fd, int Depth, boolean matchDepth, boolean includeGood, boolean includeMissing)
  {
    LinkedHashMap <String, String> map = new LinkedHashMap<String, String>();
    OSM_Fabric Fabric = oms.getFabric();
    
    OSM_Nodes AllNodes = (Fabric == null) ? null : Fabric.getOsmNodes();
    OSM_Ports AllPorts = (Fabric == null) ? null : Fabric.getOsmPorts();
    
    HashMap <String, OSM_Port> pMap = OSM_Ports.createOSM_PortMap(AllNodes, AllPorts);
    
    OSM_FabricDeltaAnalyzer fda = (fd == null) ? null: new OSM_FabricDeltaAnalyzer(fd);

    // from this edge map, create the vertex map (this sets the levels too)
    LinkedHashMap<String, IB_Vertex> vertexMap = IB_Vertex.createVertexMap(Fabric);

    for (Entry<String, IB_Vertex> entry : vertexMap.entrySet())
    {
      IB_Vertex v = entry.getValue();
      boolean includeThis = matchDepth ? v.getDepth() == Depth: v.getDepth() != Depth;
      if(includeThis)
        map.putAll(IB_LinkInfo.getLinkInfoRecords(v, pMap, fda, includeGood, includeMissing));      
    }
     return map;
  }

  public static LinkedHashMap<String, String> getErrorLinkInfoRecords(OpenSmMonitorService oms, OSM_FabricDelta fd)
  {
    LinkedHashMap <String, String> map = new LinkedHashMap<String, String>();
    OSM_Fabric Fabric = oms.getFabric();
    
    // from this fabric, get all the verticies
    LinkedHashMap<String, IB_Vertex> vertexMap = IB_Vertex.createVertexMap(Fabric);
    
    OSM_FabricDeltaAnalyzer fda = (fd == null) ? null: new OSM_FabricDeltaAnalyzer(fd);
    
    // from this data analyzer, get all the links with dynamic errors
    LinkedHashMap<String, IB_Edge> edgeMap = fda.getDynamicErrorEdgeMap();
    
    // return only good links, with dynamic errors
    for (Entry<String, IB_Edge> entry : edgeMap.entrySet())
    {
      IB_Edge e = entry.getValue();
      map.putAll(IB_LinkInfo.getLinkInfoRecords(e, vertexMap, oms, fd));      
    }
     return map;
  }

}
