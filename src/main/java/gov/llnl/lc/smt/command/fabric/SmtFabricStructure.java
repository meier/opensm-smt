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
 *        file: SmtFabricStructure.java
 *
 *  Created on: Oct 3, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command.fabric;

import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.core.IB_Link;
import gov.llnl.lc.infiniband.core.IB_LinkType;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_LinkRate;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_LinkSpeed;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_LinkWidth;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Node;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_NodeType;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Nodes;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Ports;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Stats;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_SysInfo;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.infiniband.opensm.plugin.data.SBN_Node;
import gov.llnl.lc.infiniband.opensm.plugin.data.SBN_NodePortStatus;
import gov.llnl.lc.infiniband.opensm.plugin.data.SBN_Switch;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Edge;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;
import gov.llnl.lc.infiniband.opensm.plugin.net.OsmServerStatus;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.net.ObjectSession;
import gov.llnl.lc.smt.SmtConstants;
import gov.llnl.lc.time.TimeStamp;
import gov.llnl.lc.util.BinList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**********************************************************************
 * Describe purpose and responsibility of SmtFabricStructure
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Oct 3, 2013 8:15:55 AM
 **********************************************************************/
public class SmtFabricStructure implements CommonLogger, SmtConstants
{

  private     OpenSmMonitorService OMS = null;
  private     OSM_Fabric Fabric = null;
  
  public SmtAttributeStructure Nodes;
  public SmtAttributeStructure Ports;
  public SmtAttributeStructure Links;
  
  public ArrayList<SmtAttributeStructure> LinkWidths = new ArrayList<SmtAttributeStructure>();
  public ArrayList<SmtAttributeStructure> LinkSpeeds = new ArrayList<SmtAttributeStructure>();
  public ArrayList<SmtAttributeStructure> LinkRates  = new ArrayList<SmtAttributeStructure>();

  public ArrayList<SmtAttributeStructure> PortWidths = new ArrayList<SmtAttributeStructure>();
  public ArrayList<SmtAttributeStructure> PortSpeeds = new ArrayList<SmtAttributeStructure>();
  public ArrayList<SmtAttributeStructure> PortState  = new ArrayList<SmtAttributeStructure>();

  public class SmtAttributeStructure implements Comparable<SmtAttributeStructure>
  {
    /************************************************************
     * Method Name:
     *  SmtAttributeStructure
    **/
    /**
     * Describe the constructor here
     *
     * @see     describe related java objects
     *
     * @param category
     * @param name
     * @param numTotal
     * @param numSwitches
     * @param numChannelAdapters
     * @param numOther
     ***********************************************************/
    public SmtAttributeStructure(String category, String name,
        int numTotal,
        int numSwitches,
        int numChannelAdapters,
        int numOther)
    {
      super();
      Category = category;
      Name = name;
      NumTotal = numTotal;
      NumSwitches = numSwitches;
      NumChannelAdapters = numChannelAdapters;
      NumOther = numOther;
    }
    
    
    public String Category;
    public String Name;
    public int NumTotal;
    public int NumSwitches;
    public int NumChannelAdapters;
    public int NumOther;
    
    
    static final  String sFormat = "         sw: %5d, ca: %5d,   ?: %4d";
    static final  String ssFormat  = "%7s  all: %5d, sw: %5d, ca: %5d";
    static final String ss2Format = "%7s  all: %5d - (sw: %5d, ca: %5d)";
    static final String ss3Format = "%7s: %5d - (sw: %5d, ca: %5d)";
    
    
    @Override
    public String toString()
    {
      return String.format(ss2Format, Name, NumTotal, NumSwitches, NumChannelAdapters);
    }

    public String toString(String format)
    {
      return String.format(format, Name, NumTotal, NumSwitches, NumChannelAdapters);
    }

    public String toStringAlternate()
    {
      return toString(ss3Format);
    }


    @Override
    public int compareTo(SmtAttributeStructure o)
    {
      if((o == null) || (this.Name == null))
        throw new NullPointerException();
      
      if(!(o instanceof SmtAttributeStructure))
        return -1;
      
      SmtAttributeStructure sas = (SmtAttributeStructure) o;
      
      // the names must match or -1
      if(!this.Name.equals(sas.Name))
        return -1;
      
      // compare the NumTotal values
       return this.NumTotal - sas.NumTotal;
    }

    public boolean equals(Object obj)
    {
      return ((obj != null) && (obj instanceof SmtAttributeStructure) && (this.compareTo((SmtAttributeStructure)obj)==0));
    }

  }
  
  private boolean determineLinkStructure(ArrayList<IB_Link> ibla)
  {
    if ((ibla == null) || (ibla.size() == 0))
    {
      logger.severe("NULL Error in determining IB_Links (in SmtFabricStrucure)");
      return false;
    }

    // Separate the links into the different types
    ArrayList<IB_Link> ibls = new ArrayList<IB_Link>();
    ArrayList<IB_Link> iblc = new ArrayList<IB_Link>();

    // put the various attribute counts in bins
    BinList<IB_Link> aLinkBins = new BinList<IB_Link>();
    BinList<IB_Link> sLinkBins = new BinList<IB_Link>();
    BinList<IB_Link> cLinkBins = new BinList<IB_Link>();

    for (IB_Link link : ibla)
    {
      // create a list of switch and edge links
      if (link.getLinkType() == IB_LinkType.SW_LINK)
        ibls.add(link);
      else if (link.getLinkType() == IB_LinkType.CA_LINK)
        iblc.add(link);
      else
      {
        logger.severe("UNKNOWN or UNDETERMINED link type: " + link.toLinkInfo());
      }

      // bin up the types for ALL links
      if (link.hasTraffic())
        aLinkBins.add(link, "Traffic:");

      if (link.hasErrors())
        aLinkBins.add(link, "Errors:");

      aLinkBins.add(link, "State: " + link.getState().getStateName());
      aLinkBins.add(link, "Speed: " + link.getSpeed().getSpeedName());
      aLinkBins.add(link, "Width: " + link.getWidth().getWidthName());
      if(link.getRate() != null)
        aLinkBins.add(link, "Rate: " + link.getRate().getRateName());
      else
    	System.err.println("(ALL) The link rate is null");
    }

    // now I have the links separated by type, so determine "type" breakdown
    Links = new SmtAttributeStructure("Summary", "Links", ibla.size(), ibls.size(), iblc.size(), 0);

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
//      sLinkBins.add(link, "Rate: " + link.getRate().getRateName());
      if(link.getRate() != null)
          sLinkBins.add(link, "Rate: " + link.getRate().getRateName());
        else
      	System.err.println("(SW) The link rate is null");
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
//      cLinkBins.add(link, "Rate: " + link.getRate().getRateName());
      if(link.getRate() != null)
          cLinkBins.add(link, "Rate: " + link.getRate().getRateName());
        else
      	System.err.println("(CA) The link rate is null");
    }

    // save the link stats, in this order... width, speed rate

    // iterate through all the widths, see if there are any
    int total = 0;
    for (OSM_LinkWidth s : OSM_LinkWidth.OSMLINK_ALL_WIDTHS)
    {
      ArrayList<IB_Link> ls = sLinkBins.getBin("Width: " + s.getWidthName());
      if ((ls != null) && (ls.size() > 0))
        total += ls.size();
      ArrayList<IB_Link> lc = cLinkBins.getBin("Width: " + s.getWidthName());
      if ((lc != null) && (lc.size() > 0))
        total += lc.size();
      if ((total > 0) && ((ls != null) || (lc != null)))
      {
        int sSize = ls == null ? 0: ls.size();
        int cSize = lc == null ? 0: lc.size();
        LinkWidths.add(new SmtAttributeStructure("Link Widths", s.getWidthName(),  total, sSize, cSize, 0));
      }
    }

    total = 0;
    for (OSM_LinkSpeed s : OSM_LinkSpeed.OSMLINK_ALL_SPEEDS)
    {
      ArrayList<IB_Link> ls = sLinkBins.getBin("Speed: " + s.getSpeedName());
      if ((ls != null) && (ls.size() > 0))
        total += ls.size();
      ArrayList<IB_Link> lc = cLinkBins.getBin("Speed: " + s.getSpeedName());
      if ((lc != null) && (lc.size() > 0))
        total += lc.size();
      if ((total > 0) && ((ls != null) || (lc != null)))
      {
        int sSize = ls == null ? 0: ls.size();
        int cSize = lc == null ? 0: lc.size();
        LinkSpeeds.add(new SmtAttributeStructure("Link Speeds", s.getSpeedName(), total, sSize, cSize, 0));
      }
    }

    total = 0;
    for (OSM_LinkRate s : OSM_LinkRate.OSMLINK_UNIQUE_RATES)
    {
      ArrayList<IB_Link> ls = sLinkBins.getBin("Rate: " + s.getRateName());
      if ((ls != null) && (ls.size() > 0))
        total += ls.size();
      ArrayList<IB_Link> lc = cLinkBins.getBin("Rate: " + s.getRateName());
      if ((lc != null) && (lc.size() > 0))
        total += lc.size();
      if ((total > 0) && ((ls != null) || (lc != null)))
      {
        int sSize = ls == null ? 0: ls.size();
        int cSize = lc == null ? 0: lc.size();
        LinkRates.add(new SmtAttributeStructure("Link Rates", s.getRateName(),  total, sSize, cSize, 0));
      }
    }
    return true;
  }
  

  private boolean determinePortStructure(OSM_Fabric Fabric)
  {
    OSM_SysInfo SysInfo = Fabric.getOsmSysInfo();

    SBN_NodePortStatus tots = new SBN_NodePortStatus();
    SBN_NodePortStatus nps = null;
    
    /* keep in order, top down.  See row value increment */
    // data from the service (may need to pad these)
    ArrayList<SBN_NodePortStatus> nps_array  = new ArrayList<SBN_NodePortStatus>();
    
    if(SysInfo != null)
    {
      // There are 6 port widths
      // There are 5 port speeds
      // There are 3 port states
      
       nps_array.add(0, SysInfo.SW_PortStatus);
       nps_array.add(1, SysInfo.CA_PortStatus);
       nps_array.add(2, SysInfo.RT_PortStatus);
       nps_array.add(3, tots);
 
      // loop through the four types, SW, CA, RT, and then Total
      for (int n = 0; n < 3; n++)
      {
        // 0 is switch
        // 1 is CA
        // 2 is other
        // 3 is total
        nps = nps_array.get(n);
        
        tots.add(nps);
      }
      
      // Port State
      if(tots.ports_disabled > 0)
        PortState.add(new SmtAttributeStructure("Port State", "Disabled", (int)tots.ports_disabled, (int)SysInfo.SW_PortStatus.ports_disabled, (int)SysInfo.CA_PortStatus.ports_disabled, (int)SysInfo.RT_PortStatus.ports_disabled));
      if(tots.ports_active > 0)
        PortState.add(new SmtAttributeStructure("Port State", "Active", (int)tots.ports_active, (int)SysInfo.SW_PortStatus.ports_active, (int)SysInfo.CA_PortStatus.ports_active, (int)SysInfo.RT_PortStatus.ports_active));
      if(tots.ports_down > 0)
        PortState.add(new SmtAttributeStructure("Port State", "Down", (int)tots.ports_down, (int)SysInfo.SW_PortStatus.ports_down, (int)SysInfo.CA_PortStatus.ports_down, (int)SysInfo.RT_PortStatus.ports_down));
       
      // Port Speed
      if(tots.ports_sdr > 0)
        PortSpeeds.add(new SmtAttributeStructure("Port Speed", "SDR", (int)tots.ports_sdr, (int)SysInfo.SW_PortStatus.ports_sdr, (int)SysInfo.CA_PortStatus.ports_sdr, (int)SysInfo.RT_PortStatus.ports_sdr));
      if(tots.ports_ddr > 0)
        PortSpeeds.add(new SmtAttributeStructure("Port Speed", "DDR", (int)tots.ports_ddr, (int)SysInfo.SW_PortStatus.ports_ddr, (int)SysInfo.CA_PortStatus.ports_ddr, (int)SysInfo.RT_PortStatus.ports_ddr));
      if(tots.ports_qdr > 0)
        PortSpeeds.add(new SmtAttributeStructure("Port Speed", "QDR", (int)tots.ports_qdr, (int)SysInfo.SW_PortStatus.ports_qdr, (int)SysInfo.CA_PortStatus.ports_qdr, (int)SysInfo.RT_PortStatus.ports_qdr));
      if(tots.ports_fdr10 > 0)
        PortSpeeds.add(new SmtAttributeStructure("Port Speed", "FDR10", (int)tots.ports_fdr10, (int)SysInfo.SW_PortStatus.ports_fdr10, (int)SysInfo.CA_PortStatus.ports_fdr10, (int)SysInfo.RT_PortStatus.ports_fdr10));
      if(tots.ports_fdr > 0)
        PortSpeeds.add(new SmtAttributeStructure("Port Speed", "FDR", (int)tots.ports_qdr, (int)SysInfo.SW_PortStatus.ports_fdr, (int)SysInfo.CA_PortStatus.ports_fdr, (int)SysInfo.RT_PortStatus.ports_fdr));
      if(tots.ports_edr > 0)
        PortSpeeds.add(new SmtAttributeStructure("Port Speed", "EDR", (int)tots.ports_edr, (int)SysInfo.SW_PortStatus.ports_edr, (int)SysInfo.CA_PortStatus.ports_edr, (int)SysInfo.RT_PortStatus.ports_edr));
      if(tots.ports_reduced_speed > 0)
        PortSpeeds.add(new SmtAttributeStructure("Port Speed", "reduced", (int)tots.ports_reduced_speed, (int)SysInfo.SW_PortStatus.ports_reduced_speed, (int)SysInfo.CA_PortStatus.ports_reduced_speed, (int)SysInfo.RT_PortStatus.ports_reduced_speed));
      if(tots.ports_unenabled_speed > 0)
        PortSpeeds.add(new SmtAttributeStructure("Port Speed", "unenabled", (int)tots.ports_unenabled_speed, (int)SysInfo.SW_PortStatus.ports_unenabled_speed, (int)SysInfo.CA_PortStatus.ports_unenabled_speed, (int)SysInfo.RT_PortStatus.ports_unenabled_speed));
      if(tots.ports_unknown_speed > 0)
        PortSpeeds.add(new SmtAttributeStructure("Port Speed", "unknown", (int)tots.ports_unknown_speed, (int)SysInfo.SW_PortStatus.ports_unknown_speed, (int)SysInfo.CA_PortStatus.ports_unknown_speed, (int)SysInfo.RT_PortStatus.ports_unknown_speed));
       
      // Port Width
      if(tots.ports_1X > 0)
        PortWidths.add(new SmtAttributeStructure("Port Width", "1x", (int)tots.ports_1X, (int)SysInfo.SW_PortStatus.ports_1X, (int)SysInfo.CA_PortStatus.ports_1X, (int)SysInfo.RT_PortStatus.ports_1X));
      if(tots.ports_4X > 0)
        PortWidths.add(new SmtAttributeStructure("Port Width", "4x", (int)tots.ports_4X, (int)SysInfo.SW_PortStatus.ports_4X, (int)SysInfo.CA_PortStatus.ports_4X, (int)SysInfo.RT_PortStatus.ports_4X));
      if(tots.ports_8X > 0)
        PortWidths.add(new SmtAttributeStructure("Port Width", "8x", (int)tots.ports_8X, (int)SysInfo.SW_PortStatus.ports_8X, (int)SysInfo.CA_PortStatus.ports_8X, (int)SysInfo.RT_PortStatus.ports_8X));
      if(tots.ports_12X > 0)
        PortWidths.add(new SmtAttributeStructure("Port Width", "12x", (int)tots.ports_12X, (int)SysInfo.SW_PortStatus.ports_12X, (int)SysInfo.CA_PortStatus.ports_12X, (int)SysInfo.RT_PortStatus.ports_12X));
      if(tots.ports_reduced_width > 0)
        PortWidths.add(new SmtAttributeStructure("Port Width", "reduced", (int)tots.ports_reduced_width, (int)SysInfo.SW_PortStatus.ports_reduced_width, (int)SysInfo.CA_PortStatus.ports_reduced_width, (int)SysInfo.RT_PortStatus.ports_reduced_width));
      if(tots.ports_unknown_speed > 0)
        PortWidths.add(new SmtAttributeStructure("Port Width", "unknown", (int)tots.ports_unknown_speed, (int)SysInfo.SW_PortStatus.ports_unknown_speed, (int)SysInfo.CA_PortStatus.ports_unknown_speed, (int)SysInfo.RT_PortStatus.ports_unknown_speed));
       
    }
    else
    {
      logger.warning("The SysInfo seems to be null");
      return false;
    }
    return true;
  }

  

  private boolean determineFabricStructure(OSM_Fabric Fabric)
  {
    OSM_Nodes AllNodes = (Fabric == null) ? null : Fabric.getOsmNodes();
    OSM_Ports AllPorts = (Fabric == null) ? null : Fabric.getOsmPorts();

    HashMap<String, OSM_Node> nMap = OSM_Nodes.createOSM_NodeMap(AllNodes);
    HashMap<String, OSM_Port> pMap = OSM_Ports.createOSM_PortMap(AllNodes, AllPorts);

    LinkedHashMap<String, IB_Edge> edgeMap = IB_Edge.createEdgeMap(nMap, pMap);
    // from this edge map, create the vertex map (this sets the levels too)
    LinkedHashMap<String, IB_Vertex> vertexMap = IB_Vertex.createVertexMap(edgeMap, Fabric);
    IB_Vertex.setSBN_Switches(vertexMap, Fabric);

    if ((AllPorts == null) || (AllNodes == null))
    {
      logger.severe("NULL Error in determining Fabric strucure");
      return false;
    }

    // "ALL" IB_Links
    ArrayList<IB_Link> ibla = AllPorts.createIB_Links(AllNodes);

    if ((ibla == null) || (ibla.size() == 0))
    {
      logger.severe("NULL Error in determining IB_Links (in SmtFabricStrucure)");
      return false;
    }
    
    determineLinkStructure(ibla);

    int[] Lnum_nodes = new int[4];
    int[] Lnum_ports = new int[4];

    if ((AllPorts != null) && (AllNodes != null))
    {
      Lnum_nodes[3] = AllNodes.SubnNodes.length;
      Lnum_ports[3] = AllPorts.SubnPorts.length;

      // some link attributes depend on perfmgr data, so wait until available
      if ((AllNodes.getSubnNodes() != null) && (AllNodes.getPerfMgrNodes() != null))
      {
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

        // save in attribute structure
        Lnum_nodes[3] = AllNodes.SubnNodes.length;
        Lnum_ports[3] = AllPorts.SubnPorts.length;

        Nodes = new SmtAttributeStructure("Summary", "Nodes", Lnum_nodes[3], Lnum_nodes[0],
            Lnum_nodes[1], Lnum_nodes[2]);
        Ports = new SmtAttributeStructure("Summary", "Ports", Lnum_ports[3], Lnum_ports[0],
            Lnum_ports[1], Lnum_ports[2]);
      }
      else
        logger.warning("UD: PerfMgr data is not available... yet");
    }
    else
      logger.warning("UD: The Node and Port info seems to be unavailable");
    return true;
  }
  
  public String toSwitchString()
  {
    StringBuffer buff = new StringBuffer();
    
    // lid  guid   name   num ports
    buff.append(" lid            guid               name/description         #ports" + SmtConstants.NEW_LINE);
    
    for(SBN_Switch s: Fabric.getOsmSubnet().Switches)
    {
      IB_Guid g = new IB_Guid(s.guid);
      String name = Fabric.getNameFromGuid(g);
      int lid = Fabric.getLidFromGuid(g);
      String format = "%5d  %20s  %30s  %3d";
      
      buff.append(String.format(format, lid, g.toColonString(), name, s.num_ports) + SmtConstants.NEW_LINE);
     }
    return buff.toString();
  }

  public String toHostString()
  {
    StringBuffer buff = new StringBuffer();
    
    // lid  guid   name   num ports
    buff.append(" lid            guid               name/description         #ports" + SmtConstants.NEW_LINE);

    OSM_Nodes AllNodes = (Fabric == null) ? null : Fabric.getOsmNodes();
    ArrayList<SBN_Node> sbna = new ArrayList<SBN_Node>(Arrays.asList(AllNodes.getSubnNodes()));
    
    for (SBN_Node sn : sbna)
    {
      if (OSM_NodeType.get(sn) == OSM_NodeType.CA_NODE)
      {
        IB_Guid g = new IB_Guid(sn.node_guid);
        String name = Fabric.getNameFromGuid(g);
        int lid = Fabric.getLidFromGuid(g);
        String format = "%5d  %20s  %30s  %3d";
        
        buff.append(String.format(format, lid, g.toColonString(), name, sn.num_ports) + SmtConstants.NEW_LINE);
      }
    }
    return buff.toString();
  }

  
  public String toServiceString()
  {
    OSM_SysInfo SysInfo = Fabric.getOsmSysInfo();
    OSM_Stats Stats     = Fabric.getOsmStats();
    ObjectSession ParentSessionStatus  = null;
    long time = 0;
    OsmServerStatus RemoteServerStatus = null;
    
    /** from the admin interface **/
    if(OMS != null)
    {
      ParentSessionStatus  = OMS.getParentSessionStatus();
      time = OMS.getRemoteServerStatus().Server.getStartTime().getTimeInMillis();
      RemoteServerStatus = OMS.getRemoteServerStatus();
    }
    
//    System.err.println(new TimeStamp(time).toString());

    StringBuffer buff = new StringBuffer();
    
    if(SysInfo != null)
      buff.append(SysInfo.OsmJpi_Version + ", built for (" + SysInfo.OpenSM_Version + ")" + SmtConstants.NEW_LINE);
    else
      buff.append("The system information from the OMS was unavailable");

    if(RemoteServerStatus != null)
    {
      TimeStamp tss = RemoteServerStatus.Server.getStartTime();
      TimeStamp ts  = RemoteServerStatus.getServerTime();
      buff.append(RemoteServerStatus.Server.getHost() + ", Service up since: " + tss + SmtConstants.NEW_LINE);
      long diffMillis = RemoteServerStatus.getServerTimeDiffFromNowInMillis();
      long diffDays = diffMillis/(1000 * 60 * 60 * 24);
      long diffHours = diffMillis/(1000 * 60 *60) - (diffDays * 24);
      long diffMins  = diffMillis/(1000 * 60) - ((diffDays * 24 * 60) + (diffHours * 60));
      long diffSecs  = diffMillis/1000 - ((diffDays * 24 * 60 * 60) + (diffHours * 60 * 60) + (diffMins * 60));
      if(diffMillis > 5000L)
      {
        if(diffDays > 0)
          buff.append("  Servers time differs from the Client by " + diffDays + " days, " + diffHours + " hours, " + diffMins + " mins, and "+ diffSecs + " secs" + SmtConstants.NEW_LINE);
        else if(diffHours > 0)
          buff.append("  Servers time differs from the Client by " + diffHours + " hours, " + diffMins + " mins, and "+ diffSecs + " secs" + SmtConstants.NEW_LINE);
        else if(diffMins > 0)
          buff.append("  Servers time differs from the Client by " + diffMins + " mins, and "+ diffSecs + " secs" + SmtConstants.NEW_LINE);
        else
          buff.append("  Servers time differs from the Client by " + diffSecs + " seconds" + SmtConstants.NEW_LINE);
          
        buff.append("  Servers time: " +  ts + SmtConstants.NEW_LINE);
        buff.append("  Client time:  " +  new TimeStamp().toString() + SmtConstants.NEW_LINE);
      }
    }
    else
      buff.append("The status of the remote server was unavailable" + SmtConstants.NEW_LINE);
   
    TimeStamp tsf = Fabric.getTimeStamp();
    if(tsf != null)
    {
      buff.append("The most recent timestamp for the fabric data is: " + tsf + SmtConstants.NEW_LINE);
      if((RemoteServerStatus != null) && (SysInfo != null))
      {
        long secsToNew = tsf.getTimeInSeconds() + SysInfo.PM_SweepTime;
        long deltaS    = secsToNew - (RemoteServerStatus.TimeInMillis/1000);
        buff.append(" (seconds to next update: " + deltaS + ")" + SmtConstants.NEW_LINE);
      }
    }
    else
      buff.append("The timestamp for the fabric data was unavailable" + SmtConstants.NEW_LINE);
    
    return buff.toString();
  }

  public String toNodeContent()
  {
    int numTotal = Nodes == null ? 0: Nodes.NumTotal;
    int numSW    = Nodes == null ? 0: Nodes.NumSwitches;
    int numCA    = Nodes == null ? 0: Nodes.NumChannelAdapters;
    
    StringBuffer stringValue = new StringBuffer();
    stringValue.append("<h3># Nodes: " + numTotal + "</h3>");
    stringValue.append("<blockquote>");
    stringValue.append(MEDIUM_FONT);
    stringValue.append("    switches nodes: " + numSW); 
    stringValue.append("<br>");
    stringValue.append("  leaf nodes: " + numCA);
    stringValue.append("</blockquote>");
    stringValue.append("<br>");
    return stringValue.toString();
  }
  
  public long getNumActivePorts()
  {
    if (PortState != null)
    {
      for (SmtAttributeStructure lw : PortState)
      {
        if(lw.Name.equals("Active"))
        {
          // add them up
          return (long)(lw.NumSwitches + lw.NumChannelAdapters);
        }
      }
    }
    return -1;
  }

  public long getNumTotalPorts()
  {
    return (long)( Ports == null ? -1: Ports.NumTotal);
  }

  public String toPortContent()
  {
    int numTotal = Ports == null ? 0: Ports.NumTotal;
    StringBuffer stringValue = new StringBuffer();
    stringValue.append("<h3># Ports: " + numTotal + "</h3>");
    
    if (PortState != null)
    {
      stringValue.append("<blockquote>");
      stringValue.append(MEDIUM_FONT);
      stringValue.append("<b>State</b>");
      stringValue.append("<blockquote>");
      for (SmtAttributeStructure lw : PortState)
      {
        stringValue.append(MEDIUM_FONT);
        stringValue.append("<b>" + lw.Name + ":</b>    switch ports: " + lw.NumSwitches
            + "    leaf ports: " + lw.NumChannelAdapters);
        stringValue.append("<br>");
      }
      stringValue.append("</blockquote>");
    }

    if(PortWidths != null)
    {
      stringValue.append(MEDIUM_FONT);
      stringValue.append("<b>Width</b>"); 
      stringValue.append("<blockquote>");
      for(SmtAttributeStructure lw: PortWidths)
      {
        stringValue.append(MEDIUM_FONT);
        stringValue.append("<b>" + lw.Name + ":</b>    switch ports: " + lw.NumSwitches + "    leaf ports: " + lw.NumChannelAdapters);
        stringValue.append("<br>");
      }
      stringValue.append("</blockquote>");      
    }

    if(PortSpeeds != null)
    {
      stringValue.append(MEDIUM_FONT);
      stringValue.append("<b>Speed</b>"); 
      stringValue.append("<blockquote>");
      for(SmtAttributeStructure lw: PortSpeeds)
      {
        stringValue.append(MEDIUM_FONT);
        stringValue.append("<b>" + lw.Name + ":</b>    switch ports: " + lw.NumSwitches + "    leaf ports: " + lw.NumChannelAdapters);
        stringValue.append("<br>");
      }
      stringValue.append("</blockquote>");      
    }

    stringValue.append(END_FONT);
    stringValue.append("</blockquote>");
    stringValue.append("<br>");
    
    return stringValue.toString();
  }

  public String toLinkContent()
  {
    int numTotal = Links == null ? 0: Links.NumTotal;

    StringBuffer stringValue = new StringBuffer();
    stringValue.append("<h3># Links: " + numTotal + "</h3>");
    stringValue.append("<blockquote>");
    stringValue.append(MEDIUM_FONT);
    stringValue.append("<b>Width</b>"); 
    stringValue.append("<blockquote>");
    
    if(LinkWidths != null)
    {
      for(SmtAttributeStructure lw: LinkWidths)
      {
        stringValue.append(MEDIUM_FONT);
        stringValue.append("<b>" + lw.Name + ":</b>    switch links: " + lw.NumSwitches + "    leaf links: " + lw.NumChannelAdapters);
        stringValue.append("<br>");
      }
      stringValue.append("</blockquote>");      
    }

    if(LinkSpeeds != null)
    {
      stringValue.append(MEDIUM_FONT);
      stringValue.append("<b>Speed</b>"); 
      stringValue.append("<blockquote>");
      for(SmtAttributeStructure lw: LinkSpeeds)
      {
        stringValue.append(MEDIUM_FONT);
        stringValue.append("<b>" + lw.Name + ":</b>    switch links: " + lw.NumSwitches + "    leaf links: " + lw.NumChannelAdapters);
        stringValue.append("<br>");
      }
      stringValue.append("</blockquote>");
    }

    if(LinkRates != null)
    {
       stringValue.append(MEDIUM_FONT);
      stringValue.append("<b>Rate</b>"); 
      stringValue.append("<blockquote>");
      for(SmtAttributeStructure lw: LinkRates)
      {
        stringValue.append(MEDIUM_FONT);
        stringValue.append("<b>" + lw.Name + ":</b>    switch links: " + lw.NumSwitches + "    leaf links: " + lw.NumChannelAdapters);
        stringValue.append("<br>");
      }
      stringValue.append("</blockquote>");
   }
 
    stringValue.append(END_FONT);
    stringValue.append("</blockquote>");
    stringValue.append("<br>");
    
    return stringValue.toString();
  }

  public String toContent()
  {
    StringBuffer stringValue = new StringBuffer();
    stringValue.append(toNodeContent());
    stringValue.append(toPortContent());
    stringValue.append(toLinkContent());

    return stringValue.toString();
  }

  
  @Override
  public String toString()
  {
    StringBuffer stringValue = new StringBuffer();
    
    stringValue.append(Nodes.toString() + SmtConstants.NEW_LINE + Ports.toString() + SmtConstants.NEW_LINE);
    
    stringValue.append(SmtConstants.NEW_LINE + "Port State" + SmtConstants.NEW_LINE);
    for(SmtAttributeStructure lw: PortState)
      stringValue.append(lw.toString()+ SmtConstants.NEW_LINE);
     
    stringValue.append(SmtConstants.NEW_LINE + "Port Width" + SmtConstants.NEW_LINE);
    for(SmtAttributeStructure lw: PortWidths)
      stringValue.append(lw.toString()+ SmtConstants.NEW_LINE);
     
    stringValue.append(SmtConstants.NEW_LINE + "Port Speed" + SmtConstants.NEW_LINE);
    for(SmtAttributeStructure lw: PortSpeeds)
      stringValue.append(lw.toString()+ SmtConstants.NEW_LINE);
     
    stringValue.append( SmtConstants.NEW_LINE + Links.toString() + SmtConstants.NEW_LINE);
    
    stringValue.append(SmtConstants.NEW_LINE + "Link Width" + SmtConstants.NEW_LINE);
    for(SmtAttributeStructure lw: LinkWidths)
      stringValue.append(lw.toString()+ SmtConstants.NEW_LINE);
 
    stringValue.append(SmtConstants.NEW_LINE + "Link Speed" + SmtConstants.NEW_LINE);
    for(SmtAttributeStructure lw: LinkSpeeds)
      stringValue.append(lw.toString()+ SmtConstants.NEW_LINE);
 
    stringValue.append(SmtConstants.NEW_LINE + "Link Rate" + SmtConstants.NEW_LINE);
    for(SmtAttributeStructure lw: LinkRates)
      stringValue.append(lw.toString()+ SmtConstants.NEW_LINE);
 
    return stringValue.toString();
  }
  
  public String toStringAlternate()
  {
    return toString(SmtAttributeStructure.ss3Format);
  }
  
  public String toString(String format)
  {
    StringBuffer stringValue = new StringBuffer();
    
    stringValue.append("Fabric Composition" + SmtConstants.NEW_LINE);
    stringValue.append(Nodes.toString(format) + SmtConstants.NEW_LINE + Ports.toString(format) + SmtConstants.NEW_LINE);
    stringValue.append(Links.toString(format) + SmtConstants.NEW_LINE);
    
    stringValue.append(SmtConstants.NEW_LINE + "Port State" + SmtConstants.NEW_LINE);
    for(SmtAttributeStructure lw: PortState)
      stringValue.append(lw.toString(format)+ SmtConstants.NEW_LINE);
     
    stringValue.append(SmtConstants.NEW_LINE + "Port Width" + SmtConstants.NEW_LINE);
    for(SmtAttributeStructure lw: PortWidths)
      stringValue.append(lw.toString(format)+ SmtConstants.NEW_LINE);
     
    stringValue.append(SmtConstants.NEW_LINE + "Port Speed" + SmtConstants.NEW_LINE);
    for(SmtAttributeStructure lw: PortSpeeds)
      stringValue.append(lw.toString(format)+ SmtConstants.NEW_LINE);
     
    stringValue.append(SmtConstants.NEW_LINE + "Link Width" + SmtConstants.NEW_LINE);
    for(SmtAttributeStructure lw: LinkWidths)
      stringValue.append(lw.toString(format)+ SmtConstants.NEW_LINE);
 
    stringValue.append(SmtConstants.NEW_LINE + "Link Speed" + SmtConstants.NEW_LINE);
    for(SmtAttributeStructure lw: LinkSpeeds)
      stringValue.append(lw.toString(format)+ SmtConstants.NEW_LINE);
 
    stringValue.append(SmtConstants.NEW_LINE + "Link Rate" + SmtConstants.NEW_LINE);
    for(SmtAttributeStructure lw: LinkRates)
      stringValue.append(lw.toString(format)+ SmtConstants.NEW_LINE);
 
    return stringValue.toString();
  }
  


  /************************************************************
   * Method Name:
   *  SmtFabricStructure
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param oMS
   ***********************************************************/
  public SmtFabricStructure(OpenSmMonitorService oMS)
  {
    this(oMS.getFabric());
    OMS = oMS;
  }

  public SmtFabricStructure(OSM_Fabric fabric)
  {
    super();
    Fabric = fabric;
    if(determineFabricStructure(Fabric))
      determinePortStructure(Fabric);
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
