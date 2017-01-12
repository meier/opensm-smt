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
 *        file: SmtRoute.java
 *
 *  Created on: Sep 11, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command.route;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Node;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Nodes;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Ports;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.infiniband.opensm.plugin.data.RT_Balance;
import gov.llnl.lc.infiniband.opensm.plugin.data.RT_Node;
import gov.llnl.lc.infiniband.opensm.plugin.data.RT_NodeBalance;
import gov.llnl.lc.infiniband.opensm.plugin.data.RT_Path;
import gov.llnl.lc.infiniband.opensm.plugin.data.RT_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.RT_Table;
import gov.llnl.lc.infiniband.opensm.plugin.event.OSM_EventStats;
import gov.llnl.lc.infiniband.opensm.plugin.event.OsmEvent;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Edge;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;
import gov.llnl.lc.infiniband.opensm.plugin.net.OsmServerStatus;
import gov.llnl.lc.smt.SmtConstants;
import gov.llnl.lc.smt.command.SmtCommand;
import gov.llnl.lc.smt.command.config.SmtConfig;
import gov.llnl.lc.smt.command.port.SmtPort;
import gov.llnl.lc.smt.props.SmtProperty;
import gov.llnl.lc.util.BinList;

/**********************************************************************
 * SmtRoute provides the basic functionality of ibtracert and ibroute
 * 
 * By default, the command is in an ibroute mode.  It can be specified
 * by the -route command line option
 * 
 * The command can be placed in an ibtracert mode by specifying the
 * -path command line option.  Paths require two arguments, either two
 * guids or two lids.  A mixture is not allowed.
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Sep 11, 2013 8:13:06 AM
 **********************************************************************/
public class SmtRoute extends SmtCommand
{
  private RT_Table RoutingTable;

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
    // this is a Link command, and it can take a subcommand and an argument
    String subCommand    = null;
    Map<String,String> map = smtConfig.getConfigMap();
    
    if(config != null)
    {
      //config.printConfig();
      
      map = config.getConfigMap();
      subCommand = map.get(SmtProperty.SMT_SUBCOMMAND.getName());
      logger.severe(subCommand);
      
      // check to see if the subCommand takes any arguments or values
      if(subCommand == null)
      {
        subCommand = SmtProperty.SMT_HELP.getName();
      }
     }
    
    if(OMService == null)
      logger.severe("The service is null");

      // this is the ROUTE command, and it can take a subcommand and an argument
      String subCommandArg = null;
      subCommandArg = map.get(subCommand);
      
      // optional, only needed for some commands
      int      pN = getPortNumber(config);
      IB_Guid dst = getDestinationGuid(config);
      IB_Guid src = getSourceGuid(config);
      
      if(subCommandArg == null)
        subCommandArg = RT_Table.getCacheFileName(OMService.getFabricName());

      RoutingTable = buildTable(OMService);

      // there should only be one subcommand (use big if statement)
      if(subCommand.equalsIgnoreCase(SmtProperty.SMT_QUERY_TYPE.getName()))
      {
        RouteQuery qType = RouteQuery.getByName(map.get(SmtProperty.SMT_QUERY_TYPE.getName()));
        
        if(qType == null)
        {
          logger.severe("Invalid SmtRoute query option");
          subCommand = SmtProperty.SMT_HELP.getName();
          return false;
        }
        
        switch (qType)
        {
          case ROUTE_LIST:
            System.out.println(RouteQuery.describeAllQueryTypes());
            break;
            
          case ROUTE_STATUS:
            System.out.println(SmtRoute.getRouteTableSummary(OMService, RoutingTable));

            break;
            
          case ROUTE_TABLE:
            if(src == null)
            {
              System.out.println("Supply a guid or lid for the desired switch table");
              System.exit(0);
            }
            System.out.println(SmtRoute.getSwitchTableSummary(OMService, RoutingTable, src));
            break;
            
          case ROUTE_ROUTES:
            if((src == null) || (pN < 0))
            {
              if(src == null)
              {
                // all switches??
                System.out.println(getRouteTableDump(OMService, RoutingTable));
              }
              else
                System.out.println("Supply a valid guid or lid and a port number for the desired set of routes through a port");
              System.exit(0);
            }
            else
              // same as SmtPort with a guid and port (need guid and port num)
              System.out.println(SmtPort.getPortRoutes(RoutingTable, OMService, src, (short)pN));
            break;
            
          case ROUTE_PATH:
            if((src == null) || (dst == null))
            {
              System.out.println("Supply a source and destination guid or lid to obtain the directed path through the fabric");
              System.exit(0);
            }
            showPath(src, dst);
            break;
            
          case ROUTE_SWITCHES:
            showSwitches();
//            System.out.println(SmtRoute.getSwitchSummary(OMService, RoutingTable));
            break;
            
          case ROUTE_HOPS:
            if(src == null)
            {
              System.out.println("Supply a source guid or lid, so a hop count can be determined");
              System.exit(0);
            }
            System.out.println(SmtRoute.getHopCountSummary(OMService.getFabric(), RoutingTable, src));
            break;
            
          case ROUTE_HOP_DESTINATION:
            if(src == null)
            {
              System.out.println("Supply a source guid or lid, so a destination list can be determined");
              System.exit(0);
            }
            // requires a src guid, and a hop number (re-purpose the portNumber parsing for this)
            System.out.println(getDestinationHopList(OMService.getFabric(), RoutingTable, src, pN));
            break;
            
          case ROUTE_BALANCE:
            System.out.println(SmtRoute.getRouteBalanceReport(OMService, RoutingTable, src));
            break;
            
            default:
              System.out.println("That's not an option");
              break;
         }
      }
      else if (subCommand.equalsIgnoreCase(SmtProperty.SMT_STATUS.getName()))
      {
        System.out.println(SmtRoute.getStatus(OMService));
      }
      else if (subCommand.equalsIgnoreCase(SmtProperty.SMT_DUMP.getName()))
      {
        System.out.println(RoutingTable.toStringVerbose());
      }
      else if (subCommand.equalsIgnoreCase(SmtProperty.SMT_LIST.getName()))
      {
        System.out.println(RouteQuery.describeAllQueryTypes());
      }
      else if(src != null)
      {
        // handle short-cuts.  if one nodeid is supplied, show the switch table
        //                     if two nodeids are supplied, do a tracert
        if(dst != null)
        {
          // do a tracert
          showPath(src, dst);
        }
        else
        {
          // show the switch table
          System.out.println(SmtRoute.getSwitchTableSummary(OMService, RoutingTable, src));
        }
      }
      else if (RoutingTable != null)
      {
        System.out.println(SmtRoute.getStatus(OMService));
      }
    return false;
  }

  public static String getRouteBalanceReport(OpenSmMonitorService oms, RT_Table table, IB_Guid g)
  {
    StringBuffer buff = new StringBuffer();
    
    if((oms != null) && (table != null))
    {
      // need the levels determined via the IB_Edge and IB_Vertex objects
      OSM_Fabric fab = oms.getFabric();
      
      OSM_Nodes AllNodes = (fab == null) ? null : fab.getOsmNodes();
      OSM_Ports AllPorts = (fab == null) ? null : fab.getOsmPorts();

      HashMap<String, OSM_Node> nMap = OSM_Nodes.createOSM_NodeMap(AllNodes);
      HashMap<String, OSM_Port> pMap = OSM_Ports.createOSM_PortMap(AllNodes, AllPorts);

      LinkedHashMap<String, IB_Edge> edgeMap = IB_Edge.createEdgeMap(nMap, pMap);
      // from this edge map, create the vertex map (this sets the levels too)
      LinkedHashMap<String, IB_Vertex> vertexMap = IB_Vertex.createVertexMap(edgeMap, fab);
      IB_Vertex.setSBN_Switches(vertexMap, fab);

      
      // the whole fabric, or just a single switch?
      if(g == null)
      {
        // the whole fabric
        buff.append(getTableBalanceReport(table, fab, vertexMap));
      }
      else
      {
        // just a single switch
        RT_Node node = table.getRT_Node(g);
        if(node != null)
        {
           buff.append(getSwitchBalanceReport(node, table, fab, vertexMap));
        }
        else
          System.err.println("Unable to show switch balance for (" + g.toColonString() + "), not a switch");
      }
    }
    else
      System.err.println("Unable to show route balance for null objects");
    
    return buff.toString();
  }

  public static String getHopCountSummary(OSM_Fabric fab, RT_Table table, IB_Guid src)
  {
    StringBuffer buff = new StringBuffer();
    
    if((fab != null) && (table != null))
    {
      // using this source, and only CA's as destinations, count up the number of hops
      // it takes to get to there, from here
      
      buff.append("source:"+ SmtConstants.NEW_LINE);
      buff.append(" " + fab.getNodeIdString(src) + SmtConstants.NEW_LINE);
      
      // now bin up these lists into the number of legs or hops each one has
      // the object to return, should be the specified number of bins
      BinList<RT_Path> hopBins = table.getPathHopBins(src, fab);
      
      // these aren't in any order, but I know the # hops can't be more than 8
      // so pull them out of the bins in order and display them
      
      int k=0;
      for(k=0; k < 9; k++)
      {
        String key = Integer.toString(k);
        ArrayList <RT_Path> pList = hopBins.getBin(key);
        if(pList != null)
        {
          buff.append(key + " hops away (" + pList.size() +" destinations)" + SmtConstants.NEW_LINE);
        }
       }
     }
    else
      System.err.println("Unable to show hop count for null objects");
    
    return buff.toString();
  }

  public static String getDestinationHopList(OSM_Fabric fab, RT_Table table, IB_Guid src, int numHops)
  {
    StringBuffer buff = new StringBuffer();
    
    if((fab != null) && (table != null) && (numHops > 0))
    {
      buff.append("source:"+ SmtConstants.NEW_LINE);
      buff.append(" " + fab.getNodeIdString(src)+ SmtConstants.NEW_LINE);
      
      // using this source, return only CA's destinations that are the specified
      // number of hops away
      
       // get the HopBins (key is # hops, value is array list of RT_Paths
      BinList<RT_Path> hopBins = table.getPathHopBins(src, fab);

      ArrayList <RT_Path> pL = table.getRT_PathListFromHopBins(hopBins, numHops);
      buff.append(numHops + " hops away (" + pL.size() +" destinations): " + SmtConstants.NEW_LINE);
      for(RT_Path p: pL)
      {
        buff.append(" " + fab.getNodeIdString(p.getDestination())+ SmtConstants.NEW_LINE);
      }
    }
    else
      logger.severe("Unable to return hop count list for null objects");
    
    return buff.toString();
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
    USAGE = "[-h=<host url>] [-pn=<port num>] ";
    HEADER = "smt-route - a tool for obtaining routing information (similar to ibroute and ibtracert)";
    EXAMPLE ="examples:" + SmtConstants.NEW_LINE +
        "> smt-route -pn 10011 -sr                    - provide routing attributes of the fabric" + SmtConstants.NEW_LINE + 
        "> smt-route -pn 10013 -q switches            - list all of the switches that contain port routes" + SmtConstants.NEW_LINE + 
        "> smt-route -q table 0006:6a00:ec00:2d00     - show the switches (specified by guid) routing table by port #" + SmtConstants.NEW_LINE + 
        "> smt-route -q routes 0006:6a00:ec00:2d00 24 - show the routes through port 24 of that switch" + SmtConstants.NEW_LINE + 
        "> smt-route -pn 10013 -q path 374 112        - show the path (trace route) from lid 374 to lid 112" + SmtConstants.NEW_LINE + 
        "> smt-route -rH surface3h.his -dump          - dump all information about all the routes" + SmtConstants.NEW_LINE  + ".";  // terminate with nl

    // create and initialize the common options for this command
//    initConnectionOptions();
    initCommonOptions();
    
    /*
     * for most of these commands, a OSM_Fabric and an RT_Table is needed
     * 
     * -rR  read the routing table from a file
     * -pn  read the OMS, and therefore the fabric from a connection
     * 
     * -path | -route  do the path or route (path has preference)
     * 
     * -G  guids will be specified (one for route, src & dst for path)
     * -L  lids will be specified  (one for route, src & dst for path)
     * 
     * -wR  write the routing table to a file
     *         (generates the table, and saves it to a file)
     *         
     * -i  info about the routing table
     */
    
//    // add non-common options
//
    
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

  /************************************************************
   * Method Name:
   *  parseCommands
   **/
  /**
   * Parse the command line options and set everything up for "doing the command" in doCommand();
   *
   * @see gov.llnl.lc.smt.command.SmtCommand#parseCommands(java.util.Map, org.apache.commons.cli.CommandLine)
   *
   * @param config
   * @param line
   * @return
   ***********************************************************/

  @Override
  public boolean parseCommands(Map<String, String> config, CommandLine line)
  {
    boolean status = true;
    
    // set the command, args, and sub-command
    config.put(SmtProperty.SMT_COMMAND.getName(), this.getClass().getName());
    
    // hopefully the node description is here, so save it
    saveCommandArgs(line.getArgs(), config);
    
    SmtProperty sp = SmtProperty.SMT_READ_OMS_HISTORY;
    if(line.hasOption(sp.getName()))
    {
       status = putHistoryProperty(config, line.getOptionValue(sp.getName()));
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

    sp = SmtProperty.SMT_LIST;
    if(line.hasOption(sp.getName()))
    {
      config.put(sp.getName(), line.getOptionValue(sp.getName()));
      config.put(SmtProperty.SMT_SUBCOMMAND.getName(), sp.getName());
    }
    
    sp = SmtProperty.SMT_QUERY_LEVEL;
    if(line.hasOption(sp.getName()))
    {
      config.put(sp.getName(), line.getOptionValue(sp.getName()));
      config.put(SmtProperty.SMT_SUBCOMMAND.getName(), sp.getName());
    }
    
    return status;
  }
 
  private void readTable(String fileName)
  {
    String file = convertSpecialFileName(fileName);
    if (file != null)
    {
      try
      {
        logger.severe("Reading the RT_Table file: " + fileName);
        RoutingTable = RT_Table.readRT_Table(file);
      }
      catch (Exception e)
      {
        logger.severe("Tried, but couldn't convert file to RT_Table");
        e.printStackTrace();
      }
    }
    else
    {
      logger.severe("Have to give the file name, dummy");
    }
  }

  private static RT_Table buildTable(OpenSmMonitorService oms)
  {
     if(oms != null)
    {
      OSM_Fabric fab = oms.getFabric();
      if(fab != null)
      {
        logger.severe("Building the RT_Table: " + fab.getTimeStamp());
        return RT_Table.buildRT_Table(fab);
      }
    }
    logger.severe("Could not build Routing Tables from null objects");
    return null;
  }

  private void showPath(IB_Guid srcGuid, IB_Guid dstGuid)
  {
    if((srcGuid != null) && (dstGuid != null) && (OMService != null) && (RoutingTable != null))
    {
      RT_Path path = RoutingTable.getRT_Path(srcGuid, dstGuid, OMService.getFabric());
      if(path != null)
        System.out.println(path.toIB_TraceRtString());
      else
        System.err.println("Could not find a path for " + RT_Path.getRT_PathKey(srcGuid, dstGuid));
    }
    else
      System.err.println("Unable to find a path for (" + srcGuid + "->" + dstGuid + "), null objects");
  }

  private void showSwitches()
  {
    OSM_Fabric fab = null;
    if(OMService != null)
      fab = OMService.getFabric();
    if(this.RoutingTable != null)
      System.out.println(RoutingTable.toSwitchTableString(fab));
  }

  public static String getStatus(OpenSmMonitorService OMService)
  {
    // return a string representation of the link statistics, similar to the smt-console
    if(OMService == null)
    {
      logger.severe("Can't get status from a null OMS object");
      return "Can't get status from a null OMS object";
    }
    OSM_Fabric Fabric = OMService.getFabric();
    RT_Table rt = buildTable(OMService);
    OsmServerStatus RStatus = OMService.getRemoteServerStatus();
    OSM_EventStats EventStats  = Fabric.getOsmEventStats();
     StringBuffer buff = new StringBuffer();
    
    buff.append(String.format("              Routing Table\n"));
    buff.append(SmtConstants.NEW_LINE);
    buff.append(String.format("Fabric Name:             %20s\n", Fabric.getFabricName()));
    if(RStatus != null)
      buff.append(String.format("Up since:                %20s\n", OMService.getRemoteServerStatus().Server.getStartTime().toString() ));
    buff.append(String.format("timestamp:               %20s\n", rt.toTimeString()));
    buff.append(SmtConstants.NEW_LINE);
    
    buff.append("routing engine:                  " + rt.getRouteEngine() + SmtConstants.NEW_LINE);
    buff.append("tabletype:                       " + rt.getTableType().getTypeName() + SmtConstants.NEW_LINE);
    buff.append("# switches:                      " + rt.getNumSwitches() + SmtConstants.NEW_LINE);
    buff.append("# channel adapters:              " + rt.getNumChannelAdapters() + SmtConstants.NEW_LINE);
    buff.append("# routes:                        " + rt.getNumRoutes() + SmtConstants.NEW_LINE);
    buff.append("# lids:                          " + rt.getLidGuidMap().size() + SmtConstants.NEW_LINE);
    buff.append("# min lid:                       " + rt.getMinLid() + SmtConstants.NEW_LINE);
    buff.append("# max lid:                       " + rt.getMaxLid()+ SmtConstants.NEW_LINE);
    
    // add some event info, relative to routing
      buff.append("# route complete events:         " + EventStats.getCounter(OsmEvent.OSM_EVENT_UCAST_ROUTING_DONE) + SmtConstants.NEW_LINE);
      buff.append("# lft change events:             " + EventStats.getCounter(OsmEvent.OSM_EVEMT_LFT_CHANGE) + SmtConstants.NEW_LINE);
  
    return buff.toString();
  }
  
  public static String getSwitchTableSummary(OpenSmMonitorService oms, RT_Table table, IB_Guid g)
  {
    StringBuffer buff = new StringBuffer();
    
    if((g != null) && (oms != null) && (table != null))
    {
      RT_Node node = table.getRT_Node(g);
      if(node != null)
      {
 //       buff.append(node.toIB_RouteString(table, oms.getFabric()));
        buff.append(SmtRoute.getNodeHeader("", node, table, oms.getFabric()) + SmtConstants.NEW_LINE);
        buff.append(SmtRoute.getPortSummaryString(node, table, oms.getFabric()));
      }
      else
        System.err.println("Unable to show switch table for (" + g.toColonString() + "), not a switch");
    }
    else
      System.err.println("Unable to show switch table for (" + g + "), null objects");
    
    return buff.toString();
  }

  public static String getPortTableSummary(OpenSmMonitorService oms, RT_Table table, IB_Guid g, int portNum)
  {
    StringBuffer buff = new StringBuffer();
    
    if((g != null) && (oms != null) && (table != null))
    {
      RT_Node node = table.getRT_Node(g);
      RT_Port port = table.getRTPort(g, portNum);

      if((node != null) && (port != null))
      {
        buff.append(SmtRoute.getPortHeader("", port, node, table, oms.getFabric()) + SmtConstants.NEW_LINE);
        String formatString1 = "%20s, %s";
        // dump the table
        for(Map.Entry<String, Integer> entry: port.getPortGuidMap().entrySet())
        {
          String name = RT_Port.getDestinationName(entry, oms.getFabric());
          String other = port.toPortRouteTreeString(entry, table, oms.getFabric());
          buff.append(String.format(formatString1, name, other) + SmtConstants.NEW_LINE);
          }
       }
      else
        System.err.println("Unable to show port table for (" + g.toColonString() + "), table is empty (not a switch?)");
    }
    else
      System.err.println("Unable to show port table for (" + g + "), null objects");
    
    return buff.toString();
  }

  public static String getPortSummaryString(RT_Node node, RT_Table table, OSM_Fabric fabric)
  {
    // report number of ports with routes, and then each port summary
    StringBuffer buff = new StringBuffer();
    LinkedHashMap <String, RT_Port> PortRouteMap = RT_Node.sortPortRouteTable(node.getPortRouteMap(), true);
    buff.append("(" + PortRouteMap.size()+ ") Ports with routes  [total routes=" + node.getNumRoutes() + "]\n");
    if(PortRouteMap != null)
    {
      for(Map.Entry<String, RT_Port> entry: PortRouteMap.entrySet())
      {
        RT_Port rp = entry.getValue();
        int portNum = rp.getPortNumber();
        IB_Guid g =   node.getGuid();
        buff.append(getPortRouteLine(fabric, table, g, portNum));
      }
    }

    return buff.toString();
  }

  public static String getSwitchBalanceReport(RT_Node node, RT_Table table, OSM_Fabric fabric, LinkedHashMap<String, IB_Vertex> vertexMap)
  {
    // report number of ports with routes, and then each port summary
    RT_NodeBalance swBalance = new RT_NodeBalance(node, table, fabric, vertexMap);
    return swBalance.getBalanceReport();
  }

  
  public static String getTableBalanceReport(RT_Table table, OSM_Fabric fabric, LinkedHashMap<String, IB_Vertex> vertexMap)
  {
    // report number of ports with routes, and then each port summary
    RT_Balance balance = new RT_Balance(table, fabric, vertexMap);
    return balance.getBalanceReport();
  }

  public static String getNodeHeader(String prepend, RT_Node node, RT_Table table, OSM_Fabric fabric)
  {

    StringBuffer buff = new StringBuffer();
    String formatString = "%s(%4d) Routes for Node: %20s (%19s lid: %3d)";
    buff.append(String.format(formatString, prepend, node.getNumRoutes(), node.getName(fabric), node.getGuid().toColonString(), node.getLid()) + SmtConstants.NEW_LINE); 
    String formatString1 = "%s       average routes: %2d (min routes: %2d, max routes: %3d)";
    buff.append(String.format(formatString1, prepend, node.getAveRoutes(), node.getMinRoutes(), node.getMaxRoutes()) + SmtConstants.NEW_LINE); 
    return buff.toString();
  }

  public static String getPortHeader(String prepend, RT_Port port, RT_Node node, RT_Table table, OSM_Fabric fabric)
  {

    StringBuffer buff = new StringBuffer();
    String formatString = "%s(%4d) Routes for Port: %2d of Node: %20s (%19s lid: %3d)";
    buff.append(String.format(formatString, prepend, port.getNumRoutes(), port.getPortNumber(), node.getName(fabric), node.getGuid().toColonString(), node.getLid()) + SmtConstants.NEW_LINE); 
    buff.append("     " + SmtRoute.getPortRouteCountString(fabric, table, node.getGuid(), (short)port.getPortNumber()) + SmtConstants.NEW_LINE); 
    return buff.toString();
  }

  public static String getPortRouteLine(OSM_Fabric fabric, RT_Table table, IB_Guid g, int portNum)
  {
    StringBuffer buff = new StringBuffer();
    // xx:   xx CA routes, xx SW routes, total = xx
    String formatString = "  %2d: %3d CA routes, %3d SW routes, total=%4d";
    
    if((g != null) && (fabric != null) && (table != null))
    {
      RT_Node node = table.getRT_Node(g);
      RT_Port port = node.getRT_Port(portNum);
      if(port != null)
      {
        buff.append(String.format(formatString, port.getPortNumber(), port.getNumCaRoutes(table), port.getNumSwRoutes(table), port.getNumRoutes()));
        buff.append(SmtConstants.NEW_LINE);
      }
      else
        System.err.println("Unable to show switch table for (" + g.toColonString() + "), not a switch");
    }
    else
      System.err.println("Unable to show switch table for (" + g + "), null objects");
    
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
    System.exit((new SmtRoute().execute(args)) ? 0: -1);
  }

  public static String getRouteTableSummary(OpenSmMonitorService oms, RT_Table table)
  {
    StringBuffer buff = new StringBuffer();
    
    if((oms != null) && (table != null))
    {
      // (2822) Unicast Routes for fabricname
      //        timestamp:
      //        num lids: xx (min lid: xx, max lid: xx)
      //        num SW: xx, num CA: xx
      //
      // (xx) Switches with routing tables  [total routes=xxxx]
      // lid   guid   name  table size
      buff.append("(" + table.getNumRoutes() + ") " + table.getTableType().getTypeName() + " Routes for " + oms.getFabricName() + SmtConstants.NEW_LINE);
      buff.append("       routing engine: " + table.getRouteEngine() + SmtConstants.NEW_LINE);
      buff.append("       timestamp: " + oms.getTimeStamp() + SmtConstants.NEW_LINE);
      buff.append("       num lids: " + table.getGuidLidMap().size() + " (min lid: " + table.getMinLid() + ", max lid: " + table.getMaxLid() + ")" + SmtConstants.NEW_LINE);
      buff.append("       num SW: " + table.getNumSwitches() + ", num CA: " + table.getNumChannelAdapters() + SmtConstants.NEW_LINE + SmtConstants.NEW_LINE);
      
      buff.append(SmtRoute.getSwitchSummary(oms, table));      
    }
    else
      System.err.println("Unable to show routing table for null objects");
    
    return buff.toString();
  }

  public static String getRouteTableDump(OpenSmMonitorService oms, RT_Table table)
  {
    StringBuffer buff = new StringBuffer();
    
    if((oms != null) && (table != null))
    {
      // iterate through the switches, and dump their port routes
     HashMap <String, RT_Node> NodeRouteMap =table.getSwitchGuidMap();
      
      if(NodeRouteMap != null)
      {
        for(Map.Entry<String, RT_Node> entry: NodeRouteMap.entrySet())
        {
          RT_Node rn = entry.getValue();
          buff.append(SmtPort.getPortRoutes(table, oms, rn.getGuid(), (short)0));
        }
      }
     }
    else
      System.err.println("Unable to show routing table for null objects");
    
    return buff.toString();
  }

  public static String getSwitchSummary(OpenSmMonitorService oms, RT_Table table)
  {
    StringBuffer buff = new StringBuffer();
    
    if((oms != null) && (table != null))
    {
      HashMap <String, RT_Node> NodeRouteMap =table.getSwitchGuidMap();
      
      if(NodeRouteMap != null)
      {
        buff.append("(" + NodeRouteMap.size() + ") Switches with routing tables  [total routes=" + table.getNumRoutes() + "]" + SmtConstants.NEW_LINE);
        
        for(Map.Entry<String, RT_Node> entry: NodeRouteMap.entrySet())
        {
          RT_Node rn = entry.getValue();
          buff.append(SmtRoute.getNodeHeader(" ", rn, table, oms.getFabric()));
        }
      }
    }
    else
      System.err.println("Unable to show routing table for null objects");
    
    return buff.toString();
  }

  public static String getPortRouteCountString(OSM_Fabric fabric, RT_Table rTable, IB_Guid g, short pNum)
  {
    RT_Port pTable = rTable.getRTPort(g, pNum);
    if(pTable != null)
      return pTable.toNumRouteString(rTable);
    return "none";
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
 
  private IB_Guid getSourceGuid(SmtConfig config)
  {
    // if there are any arguments, they normally reference a node identifier
    // return null, indicating couldn't be found, or nothing specified
    //
    // a node identifier is a name, guid, or lid
    if(config != null)
    {
      Map<String,String> map = config.getConfigMap();
      String nodeid = map.get(SmtProperty.SMT_COMMAND_ARGS.getName());
      if(nodeid != null)
      {
        // should be at least one word
        //  if more than one, assume the rest is the destination
        String[] args = nodeid.split(" ");
        return getNodeGuid(args[0].trim());
      }
    }
     return null;
  }

  private IB_Guid getDestinationGuid(SmtConfig config)
  {
    // if there are any arguments, they normally reference a node identifier
    // return null, indicating couldn't be found, or nothing specified
    //
    // a node identifier is a name, guid, or lid
    if(config != null)
    {
      Map<String,String> map = config.getConfigMap();
      String nodeid = map.get(SmtProperty.SMT_COMMAND_ARGS.getName());
      if(nodeid != null)
      {
        // must be at least two words
        //  assume everyting past the first word is the destination
        String[] args = nodeid.split(" ");
        if(args.length < 2)
          return null;
        return getNodeGuid(nodeid.substring(nodeid.indexOf(" ")).trim());
      }
    }
     return null;
  }

  private int getPortNumber(SmtConfig config)
  {
    // if there are any arguments, they normally reference a port identifier
    // return 0, indicating couldn't be found, or nothing specified
    if(config != null)
    {
      Map<String,String> map = config.getConfigMap();
      String portid = map.get(SmtProperty.SMT_COMMAND_ARGS.getName());
      if(portid != null)
      {
        // should be at least two words
        //  the very last word, is supposed to be the port number
        //  if only one word, then check to see if there are 4 colons, if so, port number is after that
        String[] args = portid.split(" ");
        if((args != null) && (args.length > 0))
        {
          int p = 0;
          if(args.length == 1)
          {
            // see if a port number is tagged on as the last value of a colon delimited guid+port string
            String[] octets = portid.split(":");
            if(octets.length > 4)
              p = Integer.parseInt(octets[octets.length -1]);
           }
          else
          {
            try
            {
              p = Integer.parseInt(args[args.length -1]);
            }
            catch(NumberFormatException nfe)
            {
              // this must not be a port number, so return 0
              p=0;
            }
          }
          return p;
        }
       }
    }
     return 0;
  }
 }
