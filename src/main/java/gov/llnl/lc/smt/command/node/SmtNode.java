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
 *        file: SmtNode.java
 *
 *  Created on: Jan 9, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command.node;

import gov.llnl.lc.infiniband.core.IB_Address;
import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.core.IB_Link;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Configuration;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_FabricDelta;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_FabricDeltaAnalyzer;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Node;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_NodeType;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Nodes;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_Node;
import gov.llnl.lc.infiniband.opensm.plugin.data.RT_Table;
import gov.llnl.lc.infiniband.opensm.plugin.data.SBN_Node;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;
import gov.llnl.lc.infiniband.opensm.plugin.net.OsmServerStatus;
import gov.llnl.lc.smt.SmtConstants;
import gov.llnl.lc.smt.command.SmtCommand;
import gov.llnl.lc.smt.command.config.SmtConfig;
import gov.llnl.lc.smt.command.port.SmtPort;
import gov.llnl.lc.smt.command.route.SmtRoute;
import gov.llnl.lc.smt.props.SmtProperty;
import gov.llnl.lc.system.Console;
import gov.llnl.lc.system.Console.ConsoleColor;
import gov.llnl.lc.util.BinList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

/**********************************************************************
 * Describe purpose and responsibility of SmtNode
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Jan 9, 2013 11:36:04 AM
 **********************************************************************/
public class SmtNode extends SmtCommand
{
  
  private static final String portFormatString = "  %2d: %13s ";

  
  /************************************************************
   * Method Name:
   *  init
  **/
  /**
   * Initializes the resources for the Command, primarily the Option
   * object.
   *
   * @see gov.llnl.lc.smt.command.SmtCommand#init()
   *
   * @return  true
   ***********************************************************/
  public boolean init()
  {
    USAGE = "[-h=<host url>] [-pn=<port num>]  <node: guid, lid, or name>";
    HEADER = "smt-node - Get node information (most commands require some form of node identification)";
    EXAMPLE ="examples:" + SmtConstants.NEW_LINE +
        "> smt-node -pn 10011 -sr                  - provide a summary of nodes in the fabric" + SmtConstants.NEW_LINE + 
        "> smt-node 0006:6a01:e800:1313 -q status  - provide node status using a guid" + SmtConstants.NEW_LINE + 
        "> smt-node -pn 10011 184 -q errors        - show the errors on the node with lid 184" + SmtConstants.NEW_LINE + 
        "> smt-node ibcore1 L113 -q route          - using the switches name, show its routing table" + SmtConstants.NEW_LINE + 
        "> smt-node -rH surface3h.his -dump        - dump all information about all the nodes" + SmtConstants.NEW_LINE  + ".";  // terminate with nl

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
  
  public boolean doCommand(SmtConfig config) throws Exception
  {
    // this is a NODE command, and it can take a subcommand and an argument
    String subCommand    = null;
    Map<String,String> map = smtConfig.getConfigMap();
    
    IB_Guid g = getNodeGuid(config);
    int pNum  = getPortNumber(config);
    
    if(config != null)
    {
//      config.printConfig();
      map = config.getConfigMap();
      subCommand = map.get(SmtProperty.SMT_SUBCOMMAND.getName());
      logger.severe(subCommand);
      
      // check to see if the subCommand takes any arguments or values
      if(subCommand == null)
      {
        subCommand = SmtProperty.SMT_HELP.getName();
      }
     }

    // attempt to identify the node
    OSM_Fabric                    fabric = null;
    OSM_Node                           n = null;
    OSM_Port                           p = null;
     if((OMService != null) && (g != null))
    {
      fabric = OMService.getFabric();
      n = fabric.getOSM_Node(g);
    }

    // there should only be one subcommand (use big if statement)
    if(subCommand.equalsIgnoreCase(SmtProperty.SMT_QUERY_TYPE.getName()))
    {
      NodeQuery qType = NodeQuery.getByName(map.get(SmtProperty.SMT_QUERY_TYPE.getName()));
      
      if(qType == null)
      {
        logger.severe("Invalid SmtNode query option");
       subCommand = SmtProperty.SMT_HELP.getName();
       return false;
      }
      
      OSM_FabricDelta                   fd = null;
      OSM_FabricDeltaAnalyzer fda          = null;
      LinkedHashMap<String, IB_Link> links = null;        
      if((OMService != null) && (g != null))
      {
        fd = getOSM_FabricDelta(false);
        fabric = fd.getFabric2();
        fda          = new OSM_FabricDeltaAnalyzer(fd);
        n = fabric.getOSM_Node(g);
        links = fabric.getIB_Links(g);
        p = fabric.getOSM_Port(OSM_Port.getOSM_PortKey(g.getGuid(), (short)pNum));
      }

      switch (qType)
      {
        case NODE_LIST:
          System.out.println(NodeQuery.describeAllQueryTypes());
          break;
          
        case NODE_STATUS:
          if(g != null)
          System.out.println(SmtNode.getNodeSummary(OMService, g));
          else
            System.err.println("Could not resolve node identification string (" + map.get(SmtProperty.SMT_COMMAND_ARGS.getName()) + ")");

          break;
          
        case NODE_PORTS:
          if(g != null)
          System.out.println(SmtNode.getPortSummary(fd, g));
          else
            System.err.println("Could not resolve node identification string (" + map.get(SmtProperty.SMT_COMMAND_ARGS.getName()) + ")");
          break;
          
        case NODE_ACTIVE:
          if(g != null)
          {
          System.out.println(getPortsHeader(fabric, g));
          System.out.println(getActivePortSummary(fd, n, links));
          }
          else
            System.err.println("Could not resolve node identification string (" + map.get(SmtProperty.SMT_COMMAND_ARGS.getName()) + ")");
        break;
        
        case NODE_DOWN:
          if(g != null)
          {
          System.out.println(getPortsHeader(fabric, g));
          System.out.println(getDownPortSummary(fd, n, links));
          }
          else
            System.err.println("Could not resolve node identification string (" + map.get(SmtProperty.SMT_COMMAND_ARGS.getName()) + ")");
        break;
        
        case NODE_ERRORS:
          if(g != null)
          {
          System.out.println(getPortsHeader(fabric, g));
          System.out.println(getErrorPortSummary(fda, n, links, true));
          }
          else
            System.err.println("Could not resolve node identification string (" + map.get(SmtProperty.SMT_COMMAND_ARGS.getName()) + ")");
          break;
          
        case NODE_CHECK:
          OSM_Configuration cfg = this.getOsmConfig(true);
          if((cfg != null) && (cfg.getFabricConfig() != null) && (cfg.getFabricConfig().getFabricName() != null))
          {
            // save this configuration and then perform a check
            OSM_Configuration.cacheOSM_Configuration(OMService.getFabricName(), cfg);
            if(OMService.getFabric().checkNodeStructure(cfg.getFabricConfig(), n, true))
              System.out.println("Node OK");
          }
          break;
          
        case NODE_CONFIG:
          cfg = getOsmConfig(true);
          if((cfg != null) && (cfg.getFabricConfig() != null) && (cfg.getFabricConfig().getFabricName() != null))
          {
            // save this configuration and then perform a check
            OSM_Configuration.cacheOSM_Configuration(OMService.getFabricName(), cfg);
            
            System.out.println(cfg.toInfo(n));
          }
          break;
          
        case NODE_ROUTE:
          if(g != null)
          {
            // if the pNum is within the range of valid ports, dump the port routes
            // otherwise, dump the node route summary table
            if((pNum > 0) && (pNum <= n.sbnNode.num_ports))
            {
              System.out.println(SmtPort.getPortRoutes(OMService, g, (short)pNum));
            }
            else
            {
              RT_Table RoutingTable = RT_Table.buildRT_Table(fabric);
              System.out.println(SmtRoute.getSwitchTableSummary(OMService, RoutingTable, g));              
            }
           }
          else
            System.err.println("Could not resolve node identification string (" + map.get(SmtProperty.SMT_COMMAND_ARGS.getName()) + ")");
          break;
          
          default:
            System.out.println("That's not an option");
            break;
       }
      System.exit(0);
    }
  else if (subCommand.equalsIgnoreCase(SmtProperty.SMT_STATUS.getName()))
  {
    // arg will be a guid, string, or long
 //   System.out.println(SmtNode.getNodeSummary(OMService, g));
    System.out.println(SmtNode.getStatus(OMService));
  }
  else if (subCommand.equalsIgnoreCase(SmtProperty.SMT_DUMP.getName()))
  {
    if(n == null)
      dumpAllNodes();
    else
      dumpNode(n);
    return true;
  }
    else if (OMService != null)
    {
      if (g == null)
      {
        System.out.println(SmtNode.getStatus(OMService));
      }
      else
      {
        System.out.println(SmtNode.getNodeSummary(OMService, g));
      }
    }
    //
//    
// 
//
//    if((g == null))
//    {
//      System.err.println("Could not resolve node identification string (" + map.get(SmtProperty.SMT_COMMAND_ARGS.getName()) + ")");
//      System.exit(0);
//    }
//    else
//    {
//      System.out.println(SmtNode.getNodeSummary(OMService, g));
//    }
   
    return true;
  }

  public static String getNodeSummary(OpenSmMonitorService oms, IB_Guid g)
  {
    String formatString = "%12s:  %s";

    StringBuffer buff = new StringBuffer();
    if((oms != null) && (g != null))
    {
      OSM_Fabric fabric = oms.getFabric();
      if(fabric != null)
      {
        OSM_Node n = fabric.getOSM_Node(g);
        boolean mgr = fabric.isManagementNode(n);
        String mString = mgr ? Console.getTextColorString(ConsoleColor.red) + " (Subnet Manager)" + Console.getTextNormalString() : "";
        if(n != null)
        {
          int num_ports = n.sbnNode.num_ports;
          boolean isSwitch = OSM_Fabric.isSwitch(g, fabric);
          
          LinkedHashMap<String, IB_Link> links = fabric.getIB_Links(g);
          int num_links = links.size();
          int num_down  = num_ports - num_links;
          int lid = fabric.getLidFromGuid(g);

          buff.append(String.format(formatString, "Node name", n.pfmNode.node_name + SmtConstants.NEW_LINE));
          buff.append(String.format(formatString, "description", n.sbnNode.description + mString + SmtConstants.NEW_LINE));
          buff.append(String.format(formatString, "guid", g.toColonString() + SmtConstants.NEW_LINE));
          buff.append(String.format(formatString, "lid", lid + " (" + IB_Address.toLidHexString(lid) + ")" + SmtConstants.NEW_LINE));
          buff.append(String.format(formatString, "type", OSM_NodeType.get(n).getFullName() + SmtConstants.NEW_LINE));
          buff.append(String.format(formatString, "active", n.pfmNode.active + SmtConstants.NEW_LINE));
          if(isSwitch)
          buff.append(String.format(formatString, "esp0", n.pfmNode.esp0 + SmtConstants.NEW_LINE));
          buff.append(String.format(formatString, "total ports", n.sbnNode.num_ports + SmtConstants.NEW_LINE));
          buff.append(String.format(formatString, "active ports", num_links + SmtConstants.NEW_LINE));
          buff.append(String.format(formatString, "down ports", num_down + SmtConstants.NEW_LINE));
          if(isSwitch)
          {
            LinkedHashMap<String, IB_Vertex> vertexMap = IB_Vertex.createVertexMap(fabric);
            IB_Vertex v = IB_Vertex.getVertex(IB_Vertex.getVertexKey(g), vertexMap);
            int ul = v.getUpLinkNumbers().size();
            int dl = v.getDownLinkNumbers().size();
            buff.append(String.format(formatString, "up links", ul + SmtConstants.NEW_LINE));
            buff.append(String.format(formatString, "down links", dl + SmtConstants.NEW_LINE));
          }
        }
      }
    }
    return buff.toString();
  }

  public static String getPortSummary(OSM_FabricDelta fd, IB_Guid g)
  {
    OSM_Fabric fabric = fd.getFabric2();
    StringBuffer buff = new StringBuffer();
      if(fabric != null)
      {
        OSM_Node n = fabric.getOSM_Node(g);
        if(n != null)
        {
          LinkedHashMap<String, IB_Link> links = fabric.getIB_Links(g);
          
          buff.append(getPortsHeader(fabric, g));
          buff.append(" " + getDownPortSummary(fd, n, links));
          buff.append(" " + getActivePortSummary(fd, n, links));
//          buff.append(" " + getErrorPortSummary(fd, n, links));
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

  public static String getActivePortSummary(OSM_FabricDelta fabricDelta, OSM_Node n, LinkedHashMap<String, IB_Link> links)
  {
    OSM_FabricDeltaAnalyzer fda = new OSM_FabricDeltaAnalyzer(fabricDelta);
    OSM_Fabric fabric = fabricDelta.getFabric2();
    StringBuffer buff = new StringBuffer();
    
    LinkedHashMap<String, IB_Vertex> vertexMap = IB_Vertex.createVertexMap(fabric);
    IB_Vertex v = IB_Vertex.getVertex(IB_Vertex.getVertexKey(n.getNodeGuid()), vertexMap);
    
    int num_links = links.size();
    buff.append("(" + num_links  + ") Active ports" + SmtConstants.NEW_LINE);
    // loop through the nodes port numbers, and print out the active ports
    for(int pn = 0; pn < n.sbnNode.num_ports; pn++)
    {
      String pKey = OSM_Fabric.getOSM_PortKey(n.getNodeGuid().getGuid(), (short)(pn+1));
      OSM_Port p = fabric.getOSM_Port(pKey);
      String errStr = fda.getPortErrorState(n.getNodeGuid(), pn+1);
      
      // if the error string is blank, provide the up/down direction of the port
      if((errStr == null) || (errStr.length() < 1))
        errStr = v.getPortDirection(pn+1) + " link";
 
      // is this one of the active links?  If so, display it
      for(Map.Entry<String, IB_Link> entry: links.entrySet())
      {
        IB_Link l = entry.getValue();
        if(l.contains(p))
        {
          // not one of the down ones
          buff.append(SmtNode.getLinkLine(p, l, fabric, String.format(portFormatString, p.getPortNumber(), errStr)) + SmtConstants.NEW_LINE);
          break;
        }
      }
    }
    return buff.toString();
  }

  public static String getErrorPortSummary(OSM_FabricDeltaAnalyzer fda, OSM_Node n, LinkedHashMap<String, IB_Link> links, boolean includeStaticErrors)
  {
    OSM_Fabric fabric = fda.getDelta().getFabric2();
    StringBuffer buff = new StringBuffer();
    int num_errs = 0;
    // loop through the nodes port numbers, and print out the ones with errors ports
    for(int pn = 0; pn < n.sbnNode.num_ports; pn++)
    {
      String pKey = OSM_Fabric.getOSM_PortKey(n.getNodeGuid().getGuid(), (short)(pn+1));
      OSM_Port p = fabric.getOSM_Port(pKey);
      String errStr = fda.getPortErrorState(n.getNodeGuid(), pn+1);
      
      // if this is a port with an error, continue
      if((errStr.length() > 1) && (includeStaticErrors || errStr.equals(OSM_FabricDeltaAnalyzer.DYNAMIC_ERROR)))
      {
        num_errs++;
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
        else
          buff.append(SmtNode.getLinkLine(p, l, fabric, String.format(portFormatString, p.getPortNumber(), errStr)) + SmtConstants.NEW_LINE);
      }
    }
    return "(" + num_errs  + ") Error ports" + SmtConstants.NEW_LINE + buff.toString();
  }

  public static int getNumPortErrors(OSM_FabricDeltaAnalyzer fda, OSM_Node n, boolean includeStaticErrors)
  {
    int num_errs = 0;
    // loop through the nodes port numbers, and count up the errors
    for(int pn = 0; pn < n.sbnNode.num_ports; pn++)
    {
      String errStr = fda.getPortErrorState(n.getNodeGuid(), pn+1);
      
      // if this is a port with an error, continue
      if(errStr.length() > 1)
        if(includeStaticErrors || errStr.equals(OSM_FabricDeltaAnalyzer.DYNAMIC_ERROR))
        num_errs++;
     }
    return num_errs;
  }

  public static String getLinkLine(OSM_Port p, IB_Link l, OSM_Fabric fabric, String prePend)
  {
    // <prepend><- (link info) -> guid:portnum name
    StringBuffer buff = new StringBuffer();
    String formatString = "%s<- (%22s) -> %s:%2d  %s";
    OSM_Port rp = l.getRemoteEndpoint(p);
    buff.append(String.format(formatString, prePend, l.toLinkInfo(), rp.getNodeGuid().toColonString(), rp.getPortNumber(), fabric.getNameFromGuid(rp.getNodeGuid())));
    return buff.toString();
  }

  public static String getDownPortLine(OSM_Port p, IB_Link l,String prePend)
  {
    // xx: error state <- (Down) -> ?
    StringBuffer buff = new StringBuffer();
    String formatString = "  %2d: %13s <- (Down) -> ?";
    
//    OSM_Port rp = l.getRemoteEndpoint(p);
    
    buff.append(String.format(formatString, p.getPortNumber(), prePend));
    return buff.toString();
  }
  
  /************************************************************
   * Method Name:
   *  dumpAllNodes
  **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   ***********************************************************/
  private void dumpAllNodes()
  {
    HashMap<String, OSM_Node> nodes = getOSM_Nodes();
    for (OSM_Node n : nodes.values())
      System.out.println(n.toVerboseString());
  }
  
  private void dumpNode(OSM_Node n)
  {
     System.out.println(n.toVerboseString());
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
    System.exit((new SmtNode().execute(args)) ? 0: -1);
  }

  /************************************************************
   * Method Name:
   *  parseCommands
  **/
  /**
   * Parse the command line options here.  If any of these need to be persistent
   * then they need to be "put" into the config map.  Otherwise the command line
   * options need to set "command" flags or variables, which will potentially be
   * used later, typically the "doCommand()".
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
      config.put(sp.getName(), line.getOptionValue(sp.getName()));
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
    // if there are any arguments, they normally reference a node identifier
    // return null, indicating couldn't be found, or nothing specified
    if(config != null)
    {
      Map<String,String> map = config.getConfigMap();
      String nodeid = map.get(SmtProperty.SMT_COMMAND_ARGS.getName());
      if(nodeid != null)
      {
        return getNodeGuid(nodeid);
      }
    }
     return null;
  }
  
  private int getPortNumber(SmtConfig config)
  {
    // FIXME - See equivalent method in OSM_Fabric, eliminate here
    
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
            p = Integer.parseInt(args[args.length -1]);
          return p;
        }
       }
    }
     return 0;
  }
  

  
  private static String getStatLine(String label, long sw, long ca, long total)
  {
    String stringFormat1  = "%-17s %7s  %7s  %9s";
    
    return String.format(stringFormat1, label+":", sw, ca, total);
  }
  
  public static String getStatus(OpenSmMonitorService OMService)
  {
    // return a string representation of the node statistics, similar to the smt-console
    if(OMService == null)
    {
      logger.severe("Crap, its null!");
      return "Can't get status from a null object";
    }
    OSM_Fabric Fabric = OMService.getFabric();
    StringBuffer buff = new StringBuffer();
    OSM_Nodes AllNodes  = (Fabric == null) ? null: Fabric.getOsmNodes();

    BinList <PFM_Node> pNodes = new BinList <PFM_Node>();
    ArrayList<PFM_Node> pmna = new ArrayList<PFM_Node>();
    int totalSW = 0;
    int totalSWp = 0;
    int totalCA = 0;
    int totalCAp = 0;
    int totalNodes = 0;
    int cumPorts = 0;
    int totalPorts = 0;

    if(AllNodes != null)
    {
      PFM_Node[] pna = AllNodes.getPerfMgrNodes();
      SBN_Node[] sna = AllNodes.getSubnNodes();
      
      // the perfmgr may not have returned data due to start-up delay, check
      if((pna != null) && (sna != null))
      {
      pmna = new ArrayList<PFM_Node>(Arrays.asList(pna));
    
      String key = null;
      for(PFM_Node pn: pmna)
      {
        key = Short.toString(pn.getNum_ports()) + Boolean.toString(pn.isEsp0());
        pNodes.add(pn, key);
      }
      
      totalNodes = totalCA  + totalSW;
      totalPorts = totalCAp + totalSWp;

      // total nodes, ports and links, broken down by type
      OsmServerStatus RStatus = OMService.getRemoteServerStatus();

      buff.append(String.format("                 Node Status\n"));
      buff.append(SmtConstants.NEW_LINE);
      buff.append(String.format("Fabric Name:                %20s\n", Fabric.getFabricName()));
      if(RStatus != null)
        buff.append(String.format("Up since:                   %20s\n", OMService.getRemoteServerStatus().Server.getStartTime().toString() ));
      buff.append(String.format("timestamp:                  %20s\n", Fabric.getTimeStamp().toString() ));
      buff.append(SmtConstants.NEW_LINE);
      
      buff.append("---node type--------# nodes--# ports---cum ports" + SmtConstants.NEW_LINE);
      // totals per type
      int pn_type = 0;
      int nports = 0;
      String esp = "";
      for(ArrayList<PFM_Node> pn: pNodes)
      {
        // each node in the bin looks identical, so just use the first one
        PFM_Node p = pn.get(0);
        pn_type = p.getNum_ports() > 2 ? 2: 1;
        esp = p.isEsp0() ? " (w. esp0)": "";
        
        cumPorts = pn.size() * p.num_ports;
        nports = p.isEsp0() ? p.num_ports -1: p.num_ports;
        cumPorts = pn.size() * nports;
 
        buff.append(getStatLine( OSM_NodeType.get(pn_type).getFullName() + esp, pn.size(), p.num_ports, cumPorts) + SmtConstants.NEW_LINE);
        
        totalNodes += pn.size();
        totalPorts += cumPorts;
      }
      buff.append(SmtConstants.NEW_LINE);
      String stringFormat1 = "%-17s %7s  %7s  %9s";
      buff.append(String.format(stringFormat1, "all (excl. esp0):", Integer.toString(totalNodes), "", Integer.toString(totalPorts)) + SmtConstants.NEW_LINE);
  }
    }
      else
      {
        logger.warning("The OSM_Nodes seems to be null"); 
      }
      return buff.toString();
  }


}
