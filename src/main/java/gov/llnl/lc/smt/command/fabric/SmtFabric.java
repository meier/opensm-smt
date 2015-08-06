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
 *        file: SmtFabric.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command.fabric;

import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.core.IB_Link;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_WhatsUpInfo;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Configuration;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_FabricDelta;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_FabricDeltaAnalyzer;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Node;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Nodes;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Stats;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Subnet;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_SysInfo;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.infiniband.opensm.plugin.data.RT_Table;
import gov.llnl.lc.infiniband.opensm.plugin.data.SBN_Manager;
import gov.llnl.lc.infiniband.opensm.plugin.net.OsmAdminApi;
import gov.llnl.lc.infiniband.opensm.plugin.net.OsmServerStatus;
import gov.llnl.lc.infiniband.opensm.plugin.net.OsmServiceManager;
import gov.llnl.lc.infiniband.opensm.plugin.net.OsmSession;
import gov.llnl.lc.smt.SmtConstants;
import gov.llnl.lc.smt.command.SmtCommand;
import gov.llnl.lc.smt.command.config.SmtConfig;
import gov.llnl.lc.smt.command.event.SmtEvent;
import gov.llnl.lc.smt.command.link.IB_LinkInfo;
import gov.llnl.lc.smt.command.node.SmtNode;
import gov.llnl.lc.smt.command.route.SmtRoute;
import gov.llnl.lc.smt.props.SmtProperty;
import gov.llnl.lc.system.whatsup.WhatsUpInfo;
import gov.llnl.lc.util.SystemConstants;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

/**********************************************************************
 * Describe purpose and responsibility of SmtFabric
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Mar 20, 2013 12:08:04 PM
 **********************************************************************/
public class SmtFabric extends SmtCommand
{
  /************************************************************
   * Method Name:
   *  parseCommands
   **/
  /**
   * Parse the command line options here.  If any of these need to be persistent
   * then they need to be "put" into the config map.  Otherwise the command line
   * options need to set "command" flags or variables, which will potentially be
   * used later, typically by the "doCommand()".
   *
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
    boolean status = true;
    
    // set the command and sub-command (always do this)
    config.put(SmtProperty.SMT_COMMAND.getName(), this.getClass().getName());
    
    // if the host and port is supplied on the command line, assume we are making
    // a connection to the service - no matter what
    //
    // if an OMS_HISTORY FILE is supplied, it could be for reading or writing
    // but NOT BOTH.  We need to determine which based on;
    // 
    //  if host and port is supplied, then its for WRITING
    //
    //  if host and port is NOT supplied AND there is persistent host and port configured
    //     AND the -R flag is NOT provided on the command line, then its for WRITING
    //
    //  if host and port is NOT supplied AND there is no persistent host and port configured
    //    then its for READING
    //
    //  if the -R flag is provided on the command line, then its for READING
    
    // parse (only) the command specific options
    
    SmtProperty sp = SmtProperty.SMT_READ_OMS_HISTORY;
    if(line.hasOption(sp.getName()))
    {
      config.put(sp.getName(), line.getOptionValue(sp.getName()));
      config.put(SmtProperty.SMT_SUBCOMMAND.getName(), sp.getName());
    }
    
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

    sp = SmtProperty.SMT_FABRIC_CONFIG_CMD;
    if(line.hasOption(sp.getName()))
    {
      config.put(SmtProperty.SMT_SUBCOMMAND.getName(), SmtProperty.SMT_FABRIC_CONFIG_CMD.getName());
    }

    sp = SmtProperty.SMT_NODE_MAP_CMD;
    if(line.hasOption(sp.getName()))
    {
      config.put(SmtProperty.SMT_SUBCOMMAND.getName(), SmtProperty.SMT_NODE_MAP_CMD.getName());
    }

    sp = SmtProperty.SMT_FABRIC_DISCOVER;
    if(line.hasOption(sp.getName()))
    {
      config.put(sp.getName(), line.getOptionValue(sp.getName()));
      config.put(SmtProperty.SMT_SUBCOMMAND.getName(), sp.getName());
      config.put(SmtProperty.SMT_COMMAND.getName(), sp.getName());
    }

    /* just save this, if it exists in the map then do it */
    sp = SmtProperty.SMT_STATUS;
    if(line.hasOption(sp.getName()))
    {
      config.put(SmtProperty.SMT_SUBCOMMAND.getName(), sp.getName());
      config.put(sp.getName(), line.getOptionValue(sp.getName()));
    }
    
    return status;
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
    // this is the fabric command
    // support obtaining the fabric on-line, or from an OMS or Fabric
    // file.  Only one at a time....
    
    // which is all done by default within the execute() command of the
    // parent superclass smt-command
    
    // only one way of obtaining fabric data should be specified, but IF more
    // than one is, prefer;
    //
    //  on-line (if host or port is specified)
    //  OMS file
    //  Fabric file
    //  on-line using localhost and port 10011
    
    Map<String,String> map = smtConfig.getConfigMap();
    OSM_Configuration cfg  = null;
    
    
    String subCommand = map.get(SmtProperty.SMT_SUBCOMMAND.getName());
    if (subCommand == null)
      subCommand = SmtProperty.SMT_HELP.getName();
    
    // there should only be one subcommand
    if (subCommand.equalsIgnoreCase(SmtProperty.SMT_FABRIC_DISCOVER.getName()))
    {
      showDiscoveredFabrics(map);
      return true;
    }
    else if(subCommand.equalsIgnoreCase(SmtProperty.SMT_FABRIC_CONFIG_CMD.getName()))
    {
      cfg = getOsmConfig(true);
      if((cfg != null) && (cfg.getFabricConfig() != null) && (cfg.getFabricConfig().getFabricName() != null))
      {
        // save this configuration and then perform a check
        OSM_Configuration.cacheOSM_Configuration(OMService.getFabricName(), cfg);
        System.out.println(cfg.getFabricConfig().toContent());
      }
      else
      {
        logger.severe("Couldn't obtain Fabric configuration, check service connection.");
        System.err.println("Couldn't obtain Fabric configuration, check service connection.");
      }
      return true;
    }
    else if (subCommand.equalsIgnoreCase(SmtProperty.SMT_STATUS.getName()))
    {
      System.out.println(getStatus(OMService));
    }
    else if(subCommand.equalsIgnoreCase(SmtProperty.SMT_NODE_MAP_CMD.getName()))
    {
      cfg = getOsmConfig(true);
      if((cfg != null) && (cfg.getFabricConfig() != null) && (cfg.getFabricConfig().getFabricName() != null))
      {
        // save this configuration and then perform a check
        OSM_Configuration.cacheOSM_Configuration(OMService.getFabricName(), cfg);
        System.out.println(cfg.getNodeNameMap().toContent());
      }
      return true;
    }
    else if (subCommand.equalsIgnoreCase(SmtProperty.SMT_QUERY_TYPE.getName()))
    {
      FabricQuery qType = FabricQuery.getByName(map.get(SmtProperty.SMT_QUERY_TYPE.getName()));
      
      if(qType == null)
      {
        logger.severe("Invalid SmtFabric query option");
        System.err.println("Invalid SmtFabric query option");
        subCommand = SmtProperty.SMT_HELP.getName();
       return false;
      }
      
      SmtFabricStructure fs = null;
      if(OMService != null)
        fs = new SmtFabricStructure(OMService);
      else
        System.err.println("The OMService is null, baby");
      
      switch (qType)
      {
        case FAB_LIST:
          System.out.println(FabricQuery.describeAllQueryTypes());
          break;
          
        case FAB_STATUS:
          System.out.println(getStatus(OMService));
          break;
          
        case FAB_STRUCTURE:
          System.out.println(fs.toStringAlternate());
          break;
          
        case FAB_SWITCHES:
        System.out.println(fs.toSwitchString());
        break;
        
        case FAB_HOSTS:
        System.out.println(fs.toHostString());
        break;
        
        case FAB_SERVICE:
          System.out.println(fs.toServiceString());
          break;
          
        case FAB_CHECK:
          // check for dynamic link errors AND configuration errors
          System.out.println("Checking for Link errors...");
          LinkedHashMap<String, String> errMap = IB_LinkInfo.getErrorLinkInfoRecords(OMService, getOSM_FabricDelta(false));
          for (Map.Entry<String, String> mapEntry : errMap.entrySet())
            System.out.println(mapEntry.getValue());
          System.out.println();
 
          cfg = getOsmConfig(true);
          if((cfg != null) && (cfg.getFabricConfig() != null) && (cfg.getFabricConfig().getFabricName() != null))
          {
            // save this configuration and then perform a check
            OSM_Configuration.cacheOSM_Configuration(OMService.getFabricName(), cfg);
            OMService.getFabric().checkFabricStructure(cfg.getFabricConfig(), true);
          }
          break;
          
        case FAB_CONFIG:
          cfg = getOsmConfig(true);
          if((cfg != null) && (cfg.getFabricConfig() != null) && (cfg.getFabricConfig().getFabricName() != null))
          {
            // save this configuration and then perform a check
            OSM_Configuration.cacheOSM_Configuration(OMService.getFabricName(), cfg);
            
            System.out.println(cfg.toInfo());
          }
          break;
          
        case FAB_WHATSUP:
          showWhatsUp(map);
           break;
          
        case FAB_ERRORS:
          OSM_FabricDelta                   fd = getOSM_FabricDelta(false);
          if(fd == null)
          {
            System.err.println("FabricDelta is null.  Check service connection, or perhaps just wait for another snapshot");
            System.exit(0);
          }
          OSM_Fabric                    fabric = fd.getFabric2();
          OSM_FabricDeltaAnalyzer          fda = new OSM_FabricDeltaAnalyzer(fd);

          LinkedHashMap<String, IB_Link> links = fabric.getIB_Links();
          
          System.out.println(getErrorNodeSummary(fda, links, false));
          break;
          
        case FAB_EVENTS:
          if(OMService != null)
            System.out.println(SmtEvent.getEventSummary(getOSM_FabricDelta(false), ""));
         else
           System.err.println("An OMS instance is required (connection or file)");
         break;
          
        case FAB_ROUTE:
          if(OMService != null)
          {
            RT_Table RoutingTable = RT_Table.buildRT_Table(OMService.getFabric());
            System.out.println(SmtRoute.getRouteTableSummary(OMService, RoutingTable));
          }
          break;
          
          default:
            // should never get here, because it will be trapped above
            System.err.println("Invalid SmtFabric query option, again");
            break;
       }
    }
    else if(OMService != null)
    {
      System.out.println(getStatus(OMService));
    }

    return true;
  }

  public static String getStatus(OpenSmMonitorService OMService)
  {
    /* TODO create a HASHMAP or some sort of name value pair thing, that can be formatted later */
    
    // return a string representation of the subnet status, similar to the opensm console
    // or smt-console
    if(OMService == null)
    {
      logger.severe("Can't get status from a null object");
      return "Can't get status from a null object";
    }
    OSM_Fabric  Fabric  = OMService.getFabric();
    OSM_SysInfo SysInfo = Fabric.getOsmSysInfo();
    OSM_Stats   Stats   = Fabric.getOsmStats();
    OSM_Subnet  Subnet  = Fabric.getOsmSubnet();
    OsmServerStatus RStatus = OMService.getRemoteServerStatus();
    boolean stale = (Fabric == null) ? true : Fabric.isStale();
    String staleString = stale ? "(stale)": "      ";

    
    StringBuffer buff = new StringBuffer();
    buff.append(String.format("                      Fabric Status\n"));
    buff.append(String.format("\n"));
    buff.append(String.format("Fabric Name:                                %20s\n", Fabric.getFabricName()));
    if(RStatus != null)
      buff.append(String.format("Up since:                                   %20s\n", OMService.getRemoteServerStatus().Server.getStartTime().toString() ));
    buff.append(String.format("timestamp:                                  %20s\n", Fabric.getTimeStamp().toString() ));
    buff.append(SmtConstants.NEW_LINE);

        buff.append(String.format("SM State:                                   %s\n", SysInfo.SM_State));
    buff.append(String.format("SM Priority:                                %s\n", Integer.toString(SysInfo.SM_Priority)));
    buff.append(String.format("SA State:                                   %s\n", SysInfo.SA_State));
    buff.append(String.format("Routing Engine:                             %s\n", SysInfo.RoutingEngine));
    buff.append(String.format("AR Routing:                                 %s\n", "unknown"));
    buff.append(String.format("Loaded event plugins:                       %s\n", Arrays.toString(SysInfo.EventPlugins)));
    buff.append(String.format("\n"));
    buff.append(String.format("PerfMgr state/sweep state:                  %s   %s\n", SysInfo.PM_State + "/" + SysInfo.PM_SweepState, staleString));
    buff.append(String.format("PerfMgr sweep time (seconds):               %s\n", Integer.toString(SysInfo.PM_SweepTime)));
    buff.append(String.format("\n"));
    buff.append(String.format("--MAD stats -----------------------\n"));
    buff.append(String.format("QP0 MADs outstanding                        %s\n", Long.toString(Stats.qp0_mads_outstanding)));
    buff.append(String.format("QP0 MADs outstanding (on wire)              %s\n", Long.toString(Stats.qp0_mads_outstanding_on_wire)));
    buff.append(String.format("QP0 MADs rcvd                               %s\n", Long.toString(Stats.qp0_mads_rcvd)));
    buff.append(String.format("QP0 MADs sent                               %s\n", Long.toString(Stats.qp0_mads_sent)));
    buff.append(String.format("QP0 unicasts sent                           %s\n", Long.toString(Stats.qp0_unicasts_sent)));
    buff.append(String.format("QP0 unknown MADs rcvd                       %s\n", Long.toString(Stats.qp0_mads_rcvd_unknown)));
    buff.append(String.format("SA MADs outstanding                         %s\n", Long.toString(Stats.sa_mads_outstanding)));
    buff.append(String.format("SA MADs rcvd                                %s\n", Long.toString(Stats.sa_mads_rcvd)));
    buff.append(String.format("SA MADs sent                                %s\n", Long.toString(Stats.sa_mads_sent)));
    buff.append(String.format("SA unknown MADs rcvd                        %s\n", Long.toString(Stats.sa_mads_rcvd_unknown)));
    buff.append(String.format("SA MADs ignored                             %s\n", Long.toString(Stats.sa_mads_ignored)));
    buff.append(String.format("\n"));
    buff.append(String.format("--Subnet flags --------------------\n"));
    buff.append(String.format("Sweeping enabled                            %s\n", Boolean.toString(Subnet.sweeping_enabled)));
    buff.append(String.format("Sweep interval (seconds)                    %s\n", Integer.toString(Subnet.Options.sweep_interval)));
    buff.append(String.format("Ignore existing lfts                        %s\n", Boolean.toString(Subnet.ignore_existing_lfts)));
    buff.append(String.format("Subnet Init errors                          %s\n", Boolean.toString(Subnet.subnet_initialization_error)));
    buff.append(String.format("In sweep hop 0                              %s\n", Boolean.toString(Subnet.in_sweep_hop_0)));
    buff.append(String.format("First time master sweep                     %s\n", Boolean.toString(Subnet.first_time_master_sweep)));
    buff.append(String.format("Coming out of standby                       %s\n", Boolean.toString(Subnet.coming_out_of_standby)));
    buff.append(String.format("\n"));
    buff.append(String.format("--Known SMs -----------------------\n"));
    buff.append(String.format("Port GUID                    SM State       Priority\n"));
    buff.append(String.format("-----------------------      -----------    -----------\n"));
    
    for(SBN_Manager m: Subnet.Managers)
    {
      buff.append(String.format("%s          %s    %s\n", new IB_Guid(m.guid).toColonString(), m.State, Short.toString(m.pri_state) ));
    }
    return buff.toString();
  }
  
  private void showDiscoveredFabrics(Map<String,String> map)
  {
    String hostNam  = map.get(SmtProperty.SMT_HOST.getName());
    String portNum  = map.get(SmtProperty.SMT_PORT.getName());
    String numPorts = map.get(SmtProperty.SMT_FABRIC_DISCOVER.getName());
    
    // this will do the actual discovery
    LinkedHashMap<String, OpenSmMonitorService> omsFabrics = OpenSmMonitorService.discoverOpenSmMonitorService(hostNam, portNum, numPorts);
   
    OpenSmMonitorService OMS = null;
    String portString = null;
    
    for (Map.Entry<String, OpenSmMonitorService> entry : omsFabrics.entrySet())
    {
      OMS        = entry.getValue();
      portString = entry.getKey();
      
      if(OMS != null)
      {
        logger.info("The OMS timestamp is: " + OMS.getTimeStamp());
        StringBuffer s = new StringBuffer();
        s.append("OMS port    :   " + portString + SystemConstants.NEW_LINE);
        s.append("Fabric name :   " + OMS.getFabricName() + SystemConstants.NEW_LINE);
        s.append(SystemConstants.NEW_LINE);

        s.append(new SmtFabricStructure(OMS).toString());
        s.append("/***************************************************/");
        System.out.println(s.toString());
     }
    }
   }

  private void showWhatsUp(Map<String,String> map) throws Exception
  {
    OsmSession ParentSession = null;
    OsmServiceManager OsmService  = OsmServiceManager.getInstance();
    OsmAdminApi adminInterface = null;

    String hostNam = map.get(SmtProperty.SMT_HOST.getName());
    String portNum = map.get(SmtProperty.SMT_PORT.getName());
    try
    {
      ParentSession  = OsmService.openSession(hostNam, portNum, null, null);
      adminInterface = ParentSession.getAdminApi();
    }
    catch (Exception e)
    {
      logger.severe(e.getStackTrace().toString());
      System.exit(0);
    }
    if (adminInterface != null)
    {
      WhatsUpInfo whatsUp = adminInterface.getWhatsUpInfo();
      OSM_Nodes nodes = OMService.getFabric().getOsmNodes();
      if(whatsUp != null)
      {
         OMS_WhatsUpInfo owu = new OMS_WhatsUpInfo(whatsUp, nodes);
         OSM_Fabric  Fabric  = OMService.getFabric();
         System.out.println("Whats up on " + Fabric.getFabricName() + "?");
         System.out.println(owu.toInfo(" "));
      }
     }
   }
  
  public static String getErrorNodeSummary(OSM_FabricDeltaAnalyzer fda, LinkedHashMap<String, IB_Link> links, boolean includeStaticErrors)
  {
    OSM_Fabric fabric = fda.getDelta().getFabric2();
    StringBuffer buff = new StringBuffer();
    LinkedHashMap <String, OSM_Node> allNodes = fabric.getOSM_Nodes();
    OSM_Node n = null;
    for (Map.Entry<String, OSM_Node> entry : allNodes.entrySet())
    {
      n = entry.getValue();
      
      // are there any dynamic errors on this node?
      if(SmtNode.getNumPortErrors(fda, n, includeStaticErrors) > 0)
      {
        buff.append(SmtNode.getPortsHeader(fabric, n.getNodeGuid()));
        buff.append(" " + SmtNode.getErrorPortSummary(fda, n, links, includeStaticErrors) + SmtConstants.NEW_LINE);        
      }
     }
    return buff.toString() + SmtConstants.NEW_LINE;
  }



  /************************************************************
   * Method Name:
   *  init
   **/
  /**
   * Describe the method here
   *
   * @see gov.llnl.lc.smt.command.SmtCommand#init()
   * @see gov.llnl.lc.smt.command.SmtCommand#initCommonOptions()
   *
   * @return
   ***********************************************************/

  @Override
  public boolean init()
  {
    USAGE = "[-h=<host url>] [-pn=<port num>] -sr";
    HEADER = "smt-fabric - a tool for obtaining high level fabric information";
    EXAMPLE ="examples:" + SmtConstants.NEW_LINE +
        "> smt-fabric -ql                       - list the query options" + SmtConstants.NEW_LINE + 
        "> smt-fabric -dF 3                     - starting at port 10011, and for the next 3 ports, attempt to find an OMS and report" + SmtConstants.NEW_LINE + 
        "> smt-fabric -pn 10011 -q switches     - list all the switches in the fabric" + SmtConstants.NEW_LINE + 
        "> smt-fabric -rH surface3.his -q check - using the history file, perform a fabric check" + SmtConstants.NEW_LINE + 
        "> smt-fabric -pn 10013 -sr             - provide a status report for the fabric on port 10013" + SmtConstants.NEW_LINE  + ".";  // terminate with nl

    // create and initialize the common options for this command
    initCommonOptions();
    
    SmtProperty sp = SmtProperty.SMT_QUERY_TYPE;
    Option qType     = OptionBuilder.hasArg(true).hasArgs(1).withArgName( sp.getArgName() ).withValueSeparator('=').withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );

    sp = SmtProperty.SMT_QUERY_LIST;
    Option qList = new Option( sp.getShortName(), sp.getName(), false, sp.getDescription() );    

    sp = SmtProperty.SMT_FABRIC_CONFIG_CMD;
    Option fConfig = new Option( sp.getShortName(), sp.getName(), false, sp.getDescription() );    

    sp = SmtProperty.SMT_NODE_MAP_CMD;
    Option nMap = new Option( sp.getShortName(), sp.getName(), false, sp.getDescription() );    

    sp = SmtProperty.SMT_FABRIC_DISCOVER;
    Option discover  = OptionBuilder.hasArg(true).hasArgs(1).withArgName( sp.getArgName() ).withValueSeparator('=').withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );

    sp = SmtProperty.SMT_STATUS;
    Option status  = OptionBuilder.hasArg(false).withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );

    options.addOption( status );
    options.addOption( qType );
    options.addOption( qList );
    options.addOption( discover );
    options.addOption( fConfig );
    options.addOption( nMap );
    
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
   * @throws Exception 
   ***********************************************************/
  public static void main(String[] args) throws Exception
  {
    System.exit((new SmtFabric().execute(args)) ? 0: -1);
  }

}
