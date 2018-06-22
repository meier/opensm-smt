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
 *        file: SmtPort.java
 *
 *  Created on: Feb 27, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command.port;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.core.IB_Link;
import gov.llnl.lc.infiniband.core.IB_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.MLX_ExtPortInfo;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_FabricDelta;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_FabricDeltaAnalyzer;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_LinkSpeed;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Node;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_NodeType;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_SysInfo;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_Port.PortCounterName;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_PortChange;
import gov.llnl.lc.infiniband.opensm.plugin.data.RT_Table;
import gov.llnl.lc.infiniband.opensm.plugin.data.SBN_PortInfo;
import gov.llnl.lc.infiniband.opensm.plugin.net.OsmServerStatus;
import gov.llnl.lc.smt.SmtConstants;
import gov.llnl.lc.smt.command.SmtCommand;
import gov.llnl.lc.smt.command.config.SmtConfig;
import gov.llnl.lc.smt.command.link.SmtLink;
import gov.llnl.lc.smt.command.node.SmtNode;
import gov.llnl.lc.smt.command.route.SmtRoute;
import gov.llnl.lc.smt.command.search.SmtIdentification;
import gov.llnl.lc.smt.props.SmtProperty;
import gov.llnl.lc.util.BinList;

/**********************************************************************
 * Describe purpose and responsibility of SmtPort
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Feb 27, 2013 2:31:53 PM
 **********************************************************************/
public class SmtPort extends SmtCommand
{

  /************************************************************
   * Method Name:
   *  parseCommands
   **/
  /**
   * Describe the method here
   *
   * @see gov.llnl.lc.smt.command.SmtCommandInterface#parseCommands(java.util.Map, org.apache.commons.cli.CommandLine)
   *
   * @param config
   * @param line
   * @return
   ***********************************************************/

  @Override
  public boolean parseCommands(Map<String, String> config, CommandLine line)
  {
    // set the command, args, and sub-command
    config.put(SmtProperty.SMT_COMMAND.getName(), this.getClass().getName());
    
    // hopefully the node description is here, so save it
    saveCommandArgs(line.getArgs(), config);
    
    SmtProperty sp = SmtProperty.SMT_READ_OMS_HISTORY;
    if(line.hasOption(sp.getName()))
    {
      putHistoryProperty(config, line.getOptionValue(sp.getName()));
      config.put(SmtProperty.SMT_SUBCOMMAND.getName(), sp.getName());
    }
    
    // parse (only) the command specific options
    sp = SmtProperty.SMT_QUERY_TYPE;
    if(line.hasOption(sp.getName()))
    {
      config.put(SmtProperty.SMT_SUBCOMMAND.getName(), sp.getName());
      config.put(sp.getName(), line.getOptionValue(sp.getName()));
    }
    
    sp = SmtProperty.SMT_QUERY_LIST;
    if(line.hasOption(sp.getName()))
    {
      config.put(SmtProperty.SMT_SUBCOMMAND.getName(), SmtProperty.SMT_QUERY_TYPE.getName());
      config.put(SmtProperty.SMT_QUERY_TYPE.getName(), SmtProperty.SMT_LIST.getName());
    }

    sp = SmtProperty.SMT_STATUS;
    if(line.hasOption(sp.getName()))
    {
      config.put(SmtProperty.SMT_SUBCOMMAND.getName(), sp.getName());
      config.put(sp.getName(), line.getOptionValue(sp.getName()));
    }
    
    sp = SmtProperty.SMT_DUMP;
    if(line.hasOption(sp.getName()))
    {
      config.put(SmtProperty.SMT_SUBCOMMAND.getName(), sp.getName());
      config.put(sp.getName(), line.getOptionValue(sp.getName()));
    }
    
    return true;
  }

  /************************************************************
   * Method Name:
   *  doCommand
   **/
  /**
   * Describe the method here
   *
   * @see gov.llnl.lc.smt.command.SmtCommand#doCommand(gov.llnl.lc.smt.command.config.SmtConfig)
   *
   * @param config
   * @return
   * @throws Exception
   ***********************************************************/

  @Override
  public boolean doCommand(SmtConfig config) throws Exception
  {
    // this is a PORT command, and it can take a subcommand and an argument
    String subCommand    = null;
    Map<String,String> map = smtConfig.getConfigMap();
    
    IB_Guid g = getNodeGuidFromConfig(config);
    int pNum  = getPortNumberFromConfig(config);
    
    if(config != null)
    {
//      config.printConfig();
      map = config.getConfigMap();
      subCommand = map.get(SmtProperty.SMT_SUBCOMMAND.getName());
      if(subCommand != null)
        logger.info(subCommand);
      
      // check to see if the subCommand takes any arguments or values
      if(subCommand == null)
      {
        subCommand = SmtProperty.SMT_HELP.getName();
      }
     }
    
    // attempt to identify the port
    OSM_Fabric                    fabric = null;
    OSM_Port                           p = null;
    if((OMService != null) && (g != null))
    {
      fabric = OMService.getFabric();
      p = fabric.getOSM_Port(OSM_Port.getOSM_PortKey(g.getGuid(), (short)pNum));
    }
    
    // there should only be one subcommand (use big if statement)
    String qName = SmtProperty.SMT_QUERY_TYPE.getName();
    if(subCommand.equalsIgnoreCase(qName))
    {
      PortQuery qType = PortQuery.getByName(map.get(qName));
      
      if(qType == null)
      {
        logger.severe("Invalid SmtPort query option (" + map.get(qName) + ")");
        subCommand = SmtProperty.SMT_HELP.getName();
        return false;
      }
      
      OSM_FabricDelta                   fd = null;
      OSM_FabricDeltaAnalyzer fda          = null;
      OSM_Node                           n = null;
      LinkedHashMap<String, IB_Link> links = null;        
      if((OMService != null) && (g != null))
      {
        fd = getOSM_FabricDelta(false);
        if(fd == null)
        {
          // this should not happen, but if so, can't go further
          logger.severe("Could not produce FabricDelta's, suspect corrupt file.  Try -dump");
          System.out.println("Could not produce FabricDelta's, suspect corrupt file.  Try -dump");
          System.exit(0);
        }
        
        fabric = fd.getFabric2();
        fda          = new OSM_FabricDeltaAnalyzer(fd);
        n = fabric.getOSM_Node(g);
        p = fabric.getOSM_Port(OSM_Port.getOSM_PortKey(g.getGuid(), (short)pNum));
        links = fabric.getIB_Links(g);        
      }

      switch (qType)
      {
        case PORT_LIST:
          System.out.println(PortQuery.describeAllQueryTypes());
          System.exit(0);
          break;
          
        case PORT_STATUS:
          System.out.println(SmtPort.getPortSummary(OMService, g, (short)pNum));
          System.exit(0);
          break;
          
        case PORT_COUNTERS:
          System.out.println(SmtPort.getPortIdString(p, fd, true) + SmtConstants.NEW_LINE );
          System.out.println(SmtPort.getCounterSet(PortCounterName.PFM_ALL_COUNTERS, p, fd, ""));
          System.exit(0);
          break;
          
        case PORT_LINK:
          System.out.println(SmtPort.getPortLinkSummary(fd, p, links));
          System.exit(0);
          break;
          
        case PORT_DETAILS:
          System.out.println(SmtPort.getPortIdString(p, fd, false) + SmtConstants.NEW_LINE );
          System.out.println(SmtPort.getPortDetailsString(p));
          System.exit(0);
          break;
          
        case PORT_TRAFFIC_CNT:
          System.out.println(SmtPort.getPortIdString(p, fd, true) + SmtConstants.NEW_LINE );
          System.out.println(SmtPort.getCounterSet(PortCounterName.PFM_DATA_COUNTERS, p, fd, ""));
          System.exit(0);
          break;
          
        case PORT_ERROR_CNTS:
          System.out.println(SmtPort.getPortIdString(p, fd, true) + SmtConstants.NEW_LINE );
          System.out.println(SmtPort.getCounterSet(PortCounterName.PFM_ERROR_COUNTERS, p, fd, ""));
          System.exit(0);
          break;
          
        case PORT_ROUTE:
          System.out.println(SmtPort.getPortRoutes(OMService, g, (short)pNum));
          System.exit(0);
          break;
          
        case PORT_SPEED:
          OSM_LinkSpeed ls = getLinkSpeed(config);
          dumpAllPorts(ls);
          System.exit(0);
          break;
          
          default:
            System.err.println("That's not an option");
            break;
       }
      if((g == null) || (pNum < 1))
      {
        System.err.println("Could not resolve port identification string (" + map.get(SmtProperty.SMT_COMMAND_ARGS.getName()) + ")");
        System.exit(0);
      }
    }
    else if (subCommand.equalsIgnoreCase(SmtProperty.SMT_STATUS.getName()))
    {
//      System.out.println(SmtPort.getPortSummary(OMService, g, (short)pNum));
      System.out.println(SmtPort.getStatus(OMService, true));
      return true;
    }
    else if (subCommand.equalsIgnoreCase(SmtProperty.SMT_DUMP.getName()))
    {
      if(p == null)
        dumpAllPorts();
      else
        dumpPort(p);
      return true;
    }
    else if((g == null) && (OMService != null))
    {
      System.out.println(SmtPort.getStatus(OMService, false));
      return true;
    }
    
    if((g == null) || (pNum < 1))
    {
      System.err.println("Could not resolve port identification string (" + map.get(SmtProperty.SMT_COMMAND_ARGS.getName()) + ")");
      System.exit(0);
    }
    else
    {
      OSM_FabricDelta fd = getOSM_FabricDelta(false);

      System.out.println(SmtPort.getPortIdString(p, fd, true) + SmtConstants.NEW_LINE );
      System.out.println(SmtPort.getCounterSet(PortCounterName.PFM_ALL_COUNTERS, p, fd, ""));
    }
   return true;
 }

  private static String getPortDetailsString(OSM_Port p)
  {
    StringBuffer buff = new StringBuffer();
    String format = "%22s:  %s";
    SBN_PortInfo pi = p.sbnPort.port_info;
    
    buff.append(String.format(format,"local_port_num", pi.local_port_num)+ SmtConstants.NEW_LINE);
    
    buff.append(String.format(format,"link_width_enabled", pi.link_width_enabled)+ SmtConstants.NEW_LINE);
    buff.append(String.format(format,"link_width_supported", pi.link_width_supported)+ SmtConstants.NEW_LINE);
    buff.append(String.format(format,"link_width_active", pi.link_width_active)+ SmtConstants.NEW_LINE);
    buff.append(String.format(format,"state_info1", pi.state_info1)+ SmtConstants.NEW_LINE);
    buff.append(String.format(format,"state_info2", pi.state_info2)+ SmtConstants.NEW_LINE);
    buff.append(String.format(format,"mkey_lmc", pi.mkey_lmc)+ SmtConstants.NEW_LINE);
    buff.append(String.format(format,"link_speed", pi.link_speed)+ SmtConstants.NEW_LINE);
    buff.append(String.format(format,"link_speed_ext", pi.link_speed_ext)+ SmtConstants.NEW_LINE);
    buff.append(String.format(format,"link_speed_ext_enabled", pi.link_speed_ext_enabled)+ SmtConstants.NEW_LINE);
    buff.append(String.format(format,"mtu_smsl", pi.mtu_smsl)+ SmtConstants.NEW_LINE);
    buff.append(String.format(format,"vl_cap", pi.vl_cap)+ SmtConstants.NEW_LINE);
    buff.append(String.format(format,"vl_high_limit", pi.vl_high_limit)+ SmtConstants.NEW_LINE);
    buff.append(String.format(format,"vl_arb_high_cap", pi.vl_arb_high_cap)+ SmtConstants.NEW_LINE);
    buff.append(String.format(format,"vl_arb_low_cap", pi.vl_arb_low_cap)+ SmtConstants.NEW_LINE);
    buff.append(String.format(format,"mtu_cap", pi.mtu_cap)+ SmtConstants.NEW_LINE);
    buff.append(String.format(format,"vl_stall_life", pi.vl_stall_life)+ SmtConstants.NEW_LINE);
    buff.append(String.format(format,"vl_enforce", pi.vl_enforce)+ SmtConstants.NEW_LINE);
    buff.append(String.format(format,"guid_cap", pi.guid_cap)+ SmtConstants.NEW_LINE);
    buff.append(String.format(format,"subnet_timeout", pi.subnet_timeout)+ SmtConstants.NEW_LINE);
    buff.append(String.format(format,"resp_time_value", pi.resp_time_value)+ SmtConstants.NEW_LINE);
    buff.append(String.format(format,"error_threshold", pi.error_threshold)+ SmtConstants.NEW_LINE);
    buff.append(String.format(format,"base_lid", pi.base_lid)+ SmtConstants.NEW_LINE);
    buff.append(String.format(format,"master_sm_base_lid", pi.master_sm_base_lid)+ SmtConstants.NEW_LINE);
    buff.append(String.format(format,"capability_mask", "0x" + Integer.toHexString(pi.capability_mask))+ SmtConstants.NEW_LINE);
    buff.append(String.format(format,"capability_mask2", "0x" + Integer.toHexString(pi.capability_mask2))+ SmtConstants.NEW_LINE);
    buff.append(String.format(format,"diag_code", pi.diag_code)+ SmtConstants.NEW_LINE);
    buff.append(String.format(format,"m_key_lease_period", pi.m_key_lease_period)+ SmtConstants.NEW_LINE);
    buff.append(String.format(format,"m_key_violations", pi.m_key_violations)+ SmtConstants.NEW_LINE);
    buff.append(String.format(format,"p_key_violations", pi.p_key_violations)+ SmtConstants.NEW_LINE);
    buff.append(String.format(format,"q_key_violations", pi.q_key_violations)+ SmtConstants.NEW_LINE);
    buff.append(String.format(format,"max_credit_hint", pi.max_credit_hint)+ SmtConstants.NEW_LINE);
    buff.append(String.format(format,"link_rt_latency", pi.link_rt_latency)+ SmtConstants.NEW_LINE);
    buff.append(String.format(format,"m_key", pi.m_key)+ SmtConstants.NEW_LINE);
    buff.append(String.format(format,"subnet_prefix", new IB_Guid(pi.subnet_prefix).toColonString())+ SmtConstants.NEW_LINE);
    
    // add the extended port info as well
    MLX_ExtPortInfo pix = p.sbnPort.ext_port_info;
    if(pix != null)
    {
      // add the MLX_ExtPortInfo here
      
      buff.append(String.format(format,"link_speed_active", pix.link_speed_active)+ SmtConstants.NEW_LINE);
      buff.append(String.format(format,"link_speed_enabled", pix.link_speed_enabled)+ SmtConstants.NEW_LINE);
      buff.append(String.format(format,"link_speed_supported", pix.link_speed_supported)+ SmtConstants.NEW_LINE);
      buff.append(String.format(format,"state_change_enable", pix.state_change_enable)+ SmtConstants.NEW_LINE);
    }
    return buff.toString();
  }

  private static String getPortIdString(OSM_Port p, OSM_FabricDelta fd, boolean includeTimeStamps)
  {
    // Port x of node y, t0: t1:
    StringBuffer buff = new StringBuffer();
    
    if((p != null) && (fd != null))
    {
      String format = "Port %2d, Node: %s";
      String formatT = " (t0: %s  ->  t1: %s)";
      OSM_Fabric fab = fd.getFabric2();
      if(p == null)
        System.err.println("Crap, the port is null");
      String name = fab.getNameFromGuid(p.getNodeGuid());
      int lid     = fab.getLidFromGuid(p.getNodeGuid());
      
      String nodeId = name + " (" + p.getNodeGuid().toColonString() + " lid: " + lid + ")";
      buff.append(String.format(format, p.getPortNumber(), nodeId));
      if(includeTimeStamps)
        buff.append(SmtConstants.NEW_LINE + String.format(formatT,fd.getFabric1().getTimeStamp(), fd.getFabric2().getTimeStamp()));      
    }
    return buff.toString();
  }

  /************************************************************
   * Method Name:
   *  getOSM_PortByString
  **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   * @param subCommandArg
   * @return
   ***********************************************************/
  private OSM_Port getOSM_PortByString(String subCommandArg)
  {
    // return the first match
    HashMap<String, OSM_Port> ports    = getOSM_PortsByString(subCommandArg);
    if(ports.isEmpty())
      return null;
    
    return ports.values().iterator().next();
  }

  /************************************************************
   * Method Name:
   *  getOSM_PortByString
  **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   * @param subCommandArg
   * @return
   ***********************************************************/
  private HashMap<String, OSM_Port> getOSM_PortsByString(String subCommandArg)
  {
    // get all the ports, and return the ones with a matching string
    // in the node description, guid, etc.
    HashMap<String, OSM_Port> ports    = getOSM_Ports();
    HashMap<String, OSM_Port> matchPorts = null;

    if((ports != null) && ports.size() > 0)
    {
      matchPorts = new HashMap<String, OSM_Port> ();

      for(OSM_Port port: ports.values())
      {
        // if this port has this string anywhere, then add it to the map
        if(port.hasError())
        {
          matchPorts.put(OSM_Fabric.getOSM_PortKey(port), port);
        }
      }
    }
    return matchPorts;
  }

  /************************************************************
   * Method Name:
   *  dumpPort
  **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   * @param osm_Port
   ***********************************************************/
  private void dumpPort(OSM_Port osm_Port)
  {
     System.out.println(osm_Port.toVerboseString());
  }

  /************************************************************
   * Method Name:
   *  dumpAllPorts
  **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   ***********************************************************/
  private void dumpAllPorts()
  {
    HashMap<String, OSM_Port> ports = getOSM_Ports();
    for (OSM_Port p : ports.values())
      System.out.println(p.toVerboseString());
  }
  
  /************************************************************
   * Method Name:
   *  dumpAllPorts
  **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   ***********************************************************/
  private void dumpAllPorts(OSM_LinkSpeed lspeed)
  {
    HashMap<String, OSM_Port> ports = getOSM_Ports();
    for (OSM_Port p : ports.values())
    {
      if((p.isActive() && lspeed != null))
        if(lspeed == OSM_LinkSpeed.get(p))
          System.out.println(getPortSummary(OMService, p));
    }
  }
  
  /************************************************************
   * Method Name:
   *  init
   **/
  /**
   * Describe the method here
   *
   * @see gov.llnl.lc.smt.command.SmtCommand#init()
   *
   * @return
   ***********************************************************/

  @Override
  public boolean init()
  {
    USAGE = "[-h=<host url>] [-pn=<port num>]  <node: guid, lid, or name> <port #>";
    HEADER = "smt-port - Get port information";
    EXAMPLE ="examples:" + SmtConstants.NEW_LINE +
        "> smt-port -pn 10011                   - provide a summary of ports in the fabric" + SmtConstants.NEW_LINE + 
        "> smt-port -pn 10013 -q status 14 3    - show the attributes of port 3 of node with lid 14" + SmtConstants.NEW_LINE + 
        "> smt-port -pn 10011 14 3              - show port counters for the lid 14 and port number 3" + SmtConstants.NEW_LINE + 
        "> smt-port -q counters -pn 10013 14 3  - same as above" + SmtConstants.NEW_LINE + 
        "> smt-port -pn 10013 14 3 -q errors    - similar to above, but only show error counters" + SmtConstants.NEW_LINE + 
        "> smt-port -q route ibcore1 L113 24    - using the switches name, show the routes through port 24" + SmtConstants.NEW_LINE + 
        "> smt-port -q speed EDR                - show all of the EDR ports" + SmtConstants.NEW_LINE + 
        "> smt-port -rH surface3h.his -dump     - dump all information about all the ports" + SmtConstants.NEW_LINE  + ".";  // terminate with nl

    // create and initialize the common options for this command
    initCommonOptions();
    
    // initialize the command specific options
    SmtProperty sp = SmtProperty.SMT_QUERY_TYPE;
    Option qType     = OptionBuilder.hasArg(true).hasArgs(1).withArgName( sp.getArgName() ).withValueSeparator('=').withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );

    sp = SmtProperty.SMT_QUERY_LIST;
    Option qList = new Option( sp.getShortName(), sp.getName(), false, sp.getDescription() );    

    sp = SmtProperty.SMT_STATUS;
    Option status  = OptionBuilder.hasArg(false).withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );
    
    sp = SmtProperty.SMT_DUMP;
    Option dump  = OptionBuilder.hasArg(false).withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );
    
    options.addOption( qType );
    options.addOption( qList );
    options.addOption( status );
    options.addOption( dump );
    
    return true;
  }

  public static String getPortSummary(OpenSmMonitorService oms, IB_Guid g, short pNum)
  {
    if(oms != null)
    {
      OSM_Fabric fabric = oms.getFabric();
      if(fabric != null)
      {
        OSM_Port p = fabric.getOSM_Port(OSM_Port.getOSM_PortKey(g.getGuid(), (short)pNum));
        if(p != null)
          return getPortSummary(oms, p);
      }
    }
    return "Could not find port";
  }

  public static String getPortSummary(OpenSmMonitorService oms, OSM_Port p)
  {
    String formatString = "%17s:  %s";

    StringBuffer buff = new StringBuffer();
    if(oms != null)
    {
      OSM_Fabric fabric = oms.getFabric();
      if(fabric != null)
      {
        if(p != null)
        {
          IB_Guid g  = p.getNodeGuid();
          OSM_Node n = fabric.getOSM_Node(g);

          short pNum = (short)p.getPortNumber();
          
          LinkedHashMap<String, IB_Link> links = fabric.getIB_Links(g);
          IB_Link l = OSM_Fabric.getIB_Link(g.getGuid(), (short)pNum, links);
          boolean isSwitch = OSM_Fabric.isSwitch(g, fabric);

          buff.append(String.format(formatString, "Parent Node name", n.pfmNode.node_name + SmtConstants.NEW_LINE));
          buff.append(String.format(formatString, "guid", g.toColonString() + SmtConstants.NEW_LINE));
          buff.append(String.format(formatString, "lid", p.getAddress().getLocalIdHexString() + " (" + p.getAddress().getLocalId() + ")" + SmtConstants.NEW_LINE));
          buff.append(String.format(formatString, "port #", pNum + SmtConstants.NEW_LINE));
          buff.append(String.format(formatString, "state", p.getStateString() + SmtConstants.NEW_LINE));
          buff.append(String.format(formatString, "rate", p.getRateString() + SmtConstants.NEW_LINE));
          buff.append(String.format(formatString, "speed", p.getSpeedString() + SmtConstants.NEW_LINE));
          buff.append(String.format(formatString, "width", p.getWidthString() + SmtConstants.NEW_LINE));
          buff.append(String.format(formatString, "errors", p.hasError() + SmtConstants.NEW_LINE));
          buff.append(String.format(formatString, "port address", OSM_Port.getOSM_PortKey(p) + SmtConstants.NEW_LINE));
          String linkAddress = "none";
          if(l != null)
            linkAddress = OSM_Port.getOSM_PortKey(l.getRemoteEndpoint(p));
          buff.append(String.format(formatString, "link port address", linkAddress + SmtConstants.NEW_LINE)); 
          if(isSwitch)
          {
            RT_Table rTable = RT_Table.buildRT_Table(fabric);
            String pRouteCounts = SmtRoute.getPortRouteCountString(fabric, rTable, g, pNum);
            buff.append(String.format(formatString, "routes", pRouteCounts + SmtConstants.NEW_LINE));            
          }
        }
      }
    }
    return buff.toString();
  }

  public static String getPortRoutes(OpenSmMonitorService oms, IB_Guid g, short pNum)
  {
    return getPortRoutes(null, oms, g, pNum);
  }

  public static String getPortRoutes(RT_Table rTable, OpenSmMonitorService oms, IB_Guid g, short pNum)
  {
    StringBuffer buff = new StringBuffer();
    if((oms != null) && (g != null))
    {
      OSM_Fabric fabric = oms.getFabric();
      if(fabric != null)
      {
        if(OSM_Fabric.isSwitch(g, fabric))
        {
          OSM_Node n = fabric.getOSM_Node(g);
          if(n != null)
          {
            // conditionally build the table
            if(rTable == null)
              rTable = RT_Table.buildRT_Table(fabric);
            if(pNum == 0)
            {
              for(int p =1; p <= n.sbnNode.num_ports; p++)
                buff.append(SmtRoute.getPortTableSummary(oms, rTable, g, p) + SmtConstants.NEW_LINE );
            }
            else
              buff.append(SmtRoute.getPortTableSummary(oms, rTable, g, pNum));
           }
        }
        else
        {
          System.err.println("Not part of a switch, no routes");
        }
       }
    }
    return buff.toString();
  }

  public static String getPortsHeader(OSM_Fabric fabric, IB_Guid g)
  {
    OSM_Node n = fabric.getOSM_Node(g);
    return("(" + n.sbnNode.num_ports +") Ports for Node: " + n.pfmNode.node_name + "  (" + g.toColonString() + "  lid: " + fabric.getLidFromGuid(g) + ")" + SmtConstants.NEW_LINE);
  }

  public static String getDownPortSummary(OSM_FabricDelta fabricDelta, OSM_Node n, LinkedHashMap<String, IB_Link> links)
  {
    OSM_FabricDeltaAnalyzer fda = new OSM_FabricDeltaAnalyzer(fabricDelta);
    OSM_Fabric fabric = fabricDelta.getFabric2();
    StringBuffer buff = new StringBuffer();
    int num_ports = n.sbnNode.num_ports;
    int num_links = links.size();
    int num_down = num_ports - num_links;
    if (num_down > 0)
    {
      buff.append("(" + num_down + ") Down ports" + SmtConstants.NEW_LINE);
      // loop through the nodes port numbers, and print out the downed ports
      for (int pn = 0; pn < n.sbnNode.num_ports; pn++)
      {
        String pKey = OSM_Fabric.getOSM_PortKey(n.getNodeGuid().getGuid(), (short) (pn + 1));
        OSM_Port p = fabric.getOSM_Port(pKey);
        String errStr = fda.getPortErrorState(n.getNodeGuid(), pn+1);

        IB_Link l = null;
        boolean down = true;
        // is this one of the active links? If not, count it as down
        for (Map.Entry<String, IB_Link> entry : links.entrySet())
        {
          l = entry.getValue();
          if (l.contains(p))
          {
            // not one of the down ones
            down = false;
            break;
          }
        }
        if (down)
          buff.append(SmtNode.getDownPortLine(p, l, errStr) + SmtConstants.NEW_LINE);
      }
    }
    return buff.toString();
  }

  public static String getPortLinkSummary(OSM_FabricDelta fabricDelta, OSM_Port p, LinkedHashMap<String, IB_Link> links)
  {
    StringBuffer buff = new StringBuffer();
    if((p != null) && (fabricDelta != null))
    {
      OSM_FabricDeltaAnalyzer fda = new OSM_FabricDeltaAnalyzer(fabricDelta);
      OSM_Fabric fabric = fabricDelta.getFabric2();
      String formatString = "%13s: %-20s  %s:%2d ";
      String nName = fabric.getNameFromGuid(p.getNodeGuid());
      
      // find the link associated with this port
      IB_Link l = OSM_Fabric.getIB_Link(p.getNodeGuid().getGuid(), (short)p.getPortNumber(), links);
      String errStr = fda.getLinkErrorState(l);
      buff.append(SmtNode.getLinkLine(p, l, fabric, String.format(formatString, errStr, nName, p.getNodeGuid().toColonString(), p.getPortNumber())) + SmtConstants.NEW_LINE);      
    }
    return buff.toString();
  }

  public static String getCounterLine(PFM_Port.PortCounterName n, OSM_Port p, OSM_FabricDelta fd, String prePend)
  {
    StringBuffer buff = new StringBuffer();
    String formatString  = "%s%20s: %14d    (%12d)";
    String formatString2 = "%s%20s:   suppressed";
    // name: value (diff)
    if((p != null) && (fd != null) && (n != null))
    {
      PFM_Port pp = p.pfmPort;
      boolean suppressed = false;
      // active traffic, active errors?, num counters, excluded
      EnumSet<PortCounterName> sc = pp.getSuppressed_Counters();
      if((sc != null) && (sc.size() > 0))
        suppressed = sc.contains(n);
      
      if(suppressed)
        buff.append(String.format(formatString2, prePend, n.getName()));
      else
      {
      pp.getCounter(n);
      PFM_PortChange pc = fd.getPortChange(p);
      long c = pc.getDelta_port_counter(n);
      
      buff.append(String.format(formatString, prePend, n.getName(), pp.getCounter(n), c));
      }
    }
    return buff.toString();
  }

  public static String getCounterSet(EnumSet<PortCounterName> sc, OSM_Port p, OSM_FabricDelta fd, String prePend)
  {
    StringBuffer buff = new StringBuffer();
    String formatString = "%s%20s: %14s    (%12s)";

    buff.append(String.format(formatString, prePend, "counter   ", "value   ", "delta   ")+ SmtConstants.NEW_LINE);
   for(PFM_Port.PortCounterName n : sc)
     buff.append(SmtPort.getCounterLine(n, p, fd, prePend) + SmtConstants.NEW_LINE);
    return buff.toString();
  }

  public static String getDownPortLine(OSM_Port p, IB_Link l,String prePend)
  {
    // xx: error state <- (Down) -> ?
    StringBuffer buff = new StringBuffer();
    String formatString = "  %2d: %13s <- (Down) -> ?";
    
    buff.append(String.format(formatString, p.getPortNumber(), prePend));
    return buff.toString();
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
  public static void main(String[] args) throws Exception
  {
    System.exit((new SmtPort().execute(args)) ? 0: -1);
  }
  
  
  private void saveCommandArgs(String[] args, Map<String, String> config)
  {
    // stash the command line arguments away, because we will use them later
    // see getNodeGuid()
    if((args != null && args.length > 0))
    {
      // save all the arguments in a single parameter
      StringBuffer cmdArgs = new StringBuffer();
      for(String arg: args)
      {
        cmdArgs.append(arg + " ");
      }
      config.put(SmtProperty.SMT_COMMAND_ARGS.getName(), cmdArgs.toString().trim());
    }
  }
  
  private IB_Guid getNodeGuid(SmtConfig config)
  {
    // if there are any arguments, they normally reference a port identifier
    // return null, indicating couldn't be found, or nothing specified
    if(config != null)
    {
      Map<String,String> map = config.getConfigMap();
      String portid = map.get(SmtProperty.SMT_COMMAND_ARGS.getName());
      if(portid != null)
      {
        // should be at least one word
        //  if more than one, use everything except the last word
        //  to support guid, lid, and name
        //  if only one word, the treat it as a guid string
        String[] args = portid.split(" ");
        int num = args.length;
        int argNum = 0;
        StringBuffer nodeid = new StringBuffer();
        if(num == 1)
          nodeid.append(portid);
        
        // all but the last arg
        for(String arg: args)
        {
          if(++argNum < num)
            nodeid.append(arg + " ");
        }
        return getNodeGuid(nodeid.toString().trim());
      }
    }
     return null;
  }

//  private int getPortNumber2(SmtConfig config)
//  {
//    // FIXME - See equivalent method in OSM_Fabric, eliminate here
//    
//    // if there are any arguments, they normally reference a port identifier
//    // return 1, for the default, indicating couldn't be found, or nothing specified
//    if(config != null)
//    {
//      Map<String,String> map = config.getConfigMap();
//      String portid = map.get(SmtProperty.SMT_COMMAND_ARGS.getName());
//      if(portid != null)
//      {
//        // should be at least two words
//        //  the very last word, is supposed to be the port number
//        //  if only one word, then check to see if there are 4 colons, if so, port number is after that
//        String[] args = portid.split(" ");
//        if((args != null) && (args.length > 0))
//        {
//          int p = 1;
//          if(args.length == 1)
//          {
//            // see if a port number is tagged on as the last value of a colon delimited guid+port string
//            String[] octets = portid.split(":");
//            if(octets.length > 4)
//              p = Integer.parseInt(octets[octets.length -1]);
//           }
//          else
//            p = Integer.parseInt(args[args.length -1]);
//          return p;
//        }
//       }
//    }
//     return 1;
//  }

  private static OSM_LinkSpeed getLinkSpeed(SmtConfig config)
  {
    // the query argument should represent the desired link speed
    if(config != null)
    {
      Map<String,String> map = config.getConfigMap();
      String lSpeed = map.get(SmtProperty.SMT_COMMAND_ARGS.getName());
      if(lSpeed != null)
        return OSM_LinkSpeed.getByName(lSpeed, true);
    }
    return null;
  }
  
  private static String getAbbreviatedType(String s)
  {
    if(s != null)
    {
      if(s.startsWith("S"))
        return OSM_NodeType.SW_NODE.getAbrevName();
      if(s.startsWith("C"))
        return OSM_NodeType.CA_NODE.getAbrevName();
      if(s.startsWith("R"))
        return OSM_NodeType.RT_NODE.getAbrevName();
    }
    return OSM_NodeType.UNKNOWN.getAbrevName();
  }

  protected static String getProblemLines(String type, IB_Port[] pArray)
  {
    StringBuffer buff = new StringBuffer();
    
      // show the ports with problems
      if((pArray != null) && (pArray.length > 0))
      {
        // collect all these ports into bins, organized by port guids
        BinList <IB_Port> pbL = new BinList <IB_Port>();
        for(IB_Port p: pArray)
        {
          pbL.add(p, p.guid.toColonString());
        }
        
        // there should be at least one bin
        int n = pbL.size();
        for(int j = 0; j < n; j++)
        {
          ArrayList<IB_Port> pL = pbL.getBin(j);
          IB_Port p0 = pL.get(0);
          String pDesc = ("guid=" + p0.guid + " desc=" + p0.Description);
          StringBuffer sbuff = new StringBuffer();
          for(IB_Port p: pL)
            sbuff.append(p.portNumber + ", ");
          
          // strip off the trailing 
          sbuff.setLength((sbuff.length()-2));
          String pNum  = sbuff.toString();

          buff.append(getAbbreviatedType(type) + "--" + pDesc + SmtConstants.NEW_LINE);
          buff.append("port(s)=" + pNum + SmtConstants.NEW_LINE);
        }
      }
      return buff.toString();
  }
  
  private static String getStatLine(String label, long sw, long ca)
  {
    String stringFormat1  = "%-20s %4d  %5d  %5d";
    
    return String.format(stringFormat1, label+":", sw, ca, sw+ca);
  }
  
  public static String getStatus(OpenSmMonitorService OMService, boolean includeProblems)
  {
    // return a string representation of the link statistics, similar to the smt-console
    if(OMService == null)
    {
      logger.severe("Can't get status from a null OMS object");
      return "Can't get status from a null OMS object";
    }
    
    OSM_Fabric Fabric = OMService.getFabric();
    OSM_SysInfo SI = Fabric.getOsmSysInfo();
    StringBuffer buff = new StringBuffer();

    if(SI != null)
    {
      OsmServerStatus RStatus = OMService.getRemoteServerStatus();

      buff.append(String.format("                Port Status\n"));
      buff.append(SmtConstants.NEW_LINE);
      buff.append(String.format("Fabric Name:        %20s\n", Fabric.getFabricName()));
      if(RStatus != null)
        buff.append(String.format("Up since:           %20s\n", OMService.getRemoteServerStatus().Server.getStartTime().toString() ));
      buff.append(String.format("timestamp:          %20s\n", Fabric.getTimeStamp().toString() ));
      buff.append(SmtConstants.NEW_LINE);
      
      buff.append("--attribute------------SW-----CA--total" + SmtConstants.NEW_LINE);
 
      buff.append(getStatLine("Total Nodes",     SI.SW_PortStatus.total_nodes,           SI.CA_PortStatus.total_nodes) + SmtConstants.NEW_LINE);
      buff.append(getStatLine("Total Ports",     SI.SW_PortStatus.total_ports,           SI.CA_PortStatus.total_ports) + SmtConstants.NEW_LINE);

      buff.append(getStatLine("Active",          SI.SW_PortStatus.ports_active,          SI.CA_PortStatus.ports_active) + SmtConstants.NEW_LINE);
      buff.append(getStatLine("Down",            SI.SW_PortStatus.ports_down,            SI.CA_PortStatus.ports_down) + SmtConstants.NEW_LINE);
      buff.append(getStatLine("Disabled",        SI.SW_PortStatus.ports_disabled,        SI.CA_PortStatus.ports_disabled) + SmtConstants.NEW_LINE);
      buff.append(SmtConstants.NEW_LINE);
      
      buff.append(getStatLine("1X",              SI.SW_PortStatus.ports_1X,              SI.CA_PortStatus.ports_1X) + SmtConstants.NEW_LINE);
      buff.append(getStatLine("4X",              SI.SW_PortStatus.ports_4X,              SI.CA_PortStatus.ports_4X) + SmtConstants.NEW_LINE);
      buff.append(getStatLine("8X",              SI.SW_PortStatus.ports_8X,              SI.CA_PortStatus.ports_8X) + SmtConstants.NEW_LINE);
      buff.append(getStatLine("12X",             SI.SW_PortStatus.ports_12X,             SI.CA_PortStatus.ports_12X) + SmtConstants.NEW_LINE);
      buff.append(getStatLine("reduced width",   SI.SW_PortStatus.ports_reduced_width,   SI.CA_PortStatus.ports_reduced_width) + SmtConstants.NEW_LINE);
      buff.append(getStatLine("unknown width",   SI.SW_PortStatus.ports_unknown_width,   SI.CA_PortStatus.ports_unknown_width) + SmtConstants.NEW_LINE);
      buff.append(getStatLine("unenabled width", SI.SW_PortStatus.ports_unenabled_width, SI.CA_PortStatus.ports_unenabled_width) + SmtConstants.NEW_LINE);
      buff.append(SmtConstants.NEW_LINE);

      buff.append(getStatLine("SDR",             SI.SW_PortStatus.ports_sdr,             SI.CA_PortStatus.ports_sdr) + SmtConstants.NEW_LINE);
      buff.append(getStatLine("DDR",             SI.SW_PortStatus.ports_ddr,             SI.CA_PortStatus.ports_ddr) + SmtConstants.NEW_LINE);
      buff.append(getStatLine("QDR",             SI.SW_PortStatus.ports_qdr,             SI.CA_PortStatus.ports_qdr) + SmtConstants.NEW_LINE);
      buff.append(getStatLine("FDR10",           SI.SW_PortStatus.ports_fdr10,           SI.CA_PortStatus.ports_fdr10) + SmtConstants.NEW_LINE);
      buff.append(getStatLine("FDR",             SI.SW_PortStatus.ports_fdr,             SI.CA_PortStatus.ports_fdr) + SmtConstants.NEW_LINE);
      buff.append(getStatLine("EDR",             SI.SW_PortStatus.ports_edr,             SI.CA_PortStatus.ports_edr) + SmtConstants.NEW_LINE);
      buff.append(getStatLine("reduced speed",   SI.SW_PortStatus.ports_reduced_speed,   SI.CA_PortStatus.ports_reduced_speed) + SmtConstants.NEW_LINE);
      buff.append(getStatLine("unknown speed",   SI.SW_PortStatus.ports_unknown_speed,   SI.CA_PortStatus.ports_unknown_speed) + SmtConstants.NEW_LINE);
      buff.append(getStatLine("unenabled speed", SI.SW_PortStatus.ports_unenabled_speed, SI.CA_PortStatus.ports_unenabled_speed) + SmtConstants.NEW_LINE);

      if(includeProblems)
      {
      // show the ports with problems
      if(SI.SW_PortStatus.disabled_ports.length > 0 || SI.CA_PortStatus.disabled_ports.length > 0)
      {
        buff.append(SmtConstants.NEW_LINE + "--Disabled Ports--" + SmtConstants.NEW_LINE);
        buff.append(getProblemLines(SI.SW_PortStatus.NodeType, SI.SW_PortStatus.disabled_ports) + SmtConstants.NEW_LINE);
        buff.append(getProblemLines(SI.CA_PortStatus.NodeType, SI.CA_PortStatus.disabled_ports) + SmtConstants.NEW_LINE);        
      }

      if(SI.SW_PortStatus.reduced_width_ports.length > 0 || SI.CA_PortStatus.reduced_width_ports.length > 0)
      {
      buff.append(SmtConstants.NEW_LINE + "--Reduced Width--" + SmtConstants.NEW_LINE);
      buff.append(getProblemLines(SI.SW_PortStatus.NodeType, SI.SW_PortStatus.reduced_width_ports) + SmtConstants.NEW_LINE);
      buff.append(getProblemLines(SI.CA_PortStatus.NodeType, SI.CA_PortStatus.reduced_width_ports) + SmtConstants.NEW_LINE);
      }

      if(SI.SW_PortStatus.unenabled_width_ports.length > 0 || SI.CA_PortStatus.unenabled_width_ports.length > 0)
      {
      buff.append(SmtConstants.NEW_LINE + "--Unenabled Width--" + SmtConstants.NEW_LINE);
      buff.append(getProblemLines(SI.SW_PortStatus.NodeType, SI.SW_PortStatus.unenabled_width_ports) + SmtConstants.NEW_LINE);
      buff.append(getProblemLines(SI.CA_PortStatus.NodeType, SI.CA_PortStatus.unenabled_width_ports) + SmtConstants.NEW_LINE);
      }

      if(SI.SW_PortStatus.reduced_speed_ports.length > 0 || SI.CA_PortStatus.reduced_speed_ports.length > 0)
      {
      buff.append(SmtConstants.NEW_LINE + "--Reduced Speed--" + SmtConstants.NEW_LINE);
      buff.append(getProblemLines(SI.SW_PortStatus.NodeType, SI.SW_PortStatus.reduced_speed_ports) + SmtConstants.NEW_LINE);
      buff.append(getProblemLines(SI.CA_PortStatus.NodeType, SI.CA_PortStatus.reduced_speed_ports) + SmtConstants.NEW_LINE);
      }

      if(SI.SW_PortStatus.unenabled_speed_ports.length > 0 || SI.CA_PortStatus.unenabled_speed_ports.length > 0)
      {
      buff.append(SmtConstants.NEW_LINE + "--Unenabled Speed--" + SmtConstants.NEW_LINE);
      buff.append(getProblemLines(SI.SW_PortStatus.NodeType, SI.SW_PortStatus.unenabled_speed_ports) + SmtConstants.NEW_LINE);
      buff.append(getProblemLines(SI.CA_PortStatus.NodeType, SI.CA_PortStatus.unenabled_speed_ports) + SmtConstants.NEW_LINE);
      }
    }
    }
    else
    {
      logger.warning("The SysInfo seems to be null");      
    }
   
    
  return buff.toString();
  }
  

}
