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
 *        file: SmtLink.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command.link;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.core.IB_Link;
import gov.llnl.lc.infiniband.opensm.json.IB_FabricJson;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Configuration;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_FabricDelta;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_LinkSpeed;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Node;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Nodes;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Ports;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Edge;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;
import gov.llnl.lc.smt.SmtConstants;
import gov.llnl.lc.smt.command.SmtCommand;
import gov.llnl.lc.smt.command.config.SmtConfig;
import gov.llnl.lc.smt.props.SmtProperty;

/**********************************************************************
 * Describe purpose and responsibility of SmtRoute
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Sep 11, 2013 8:13:06 AM
 **********************************************************************/
public class SmtLink extends SmtCommand
{

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
    
    int pNum  = getPortNumberFromConfig(config);
    IB_Guid g = getNodeGuid(config, pNum);
    
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
    if(OMService == null)
      logger.severe("The service is null");

    // this is the LINK command, and it can take a subcommand and an argument
      String subCommandArg = null;
      boolean onlyMissing   = false;
      boolean includeMissing = true;
      
      subCommandArg = map.get(subCommand);
        
      String delimString = map.get(SmtProperty.SMT_DELIMITER.getName());
       
      String oMstring = map.get(SmtProperty.SMT_ONLY_MISSING.getName());
      if((oMstring != null) && (oMstring.startsWith("t") || (oMstring.startsWith("T"))))
          onlyMissing = true;
       
        String iMstring = map.get(SmtProperty.SMT_INCLUDE_MISSING.getName());
        if((iMstring != null) && (iMstring.startsWith("f") || (iMstring.startsWith("F"))))
          includeMissing = false;
 
      // there should only be one subcommand (use big if statement)
      if(subCommand.equalsIgnoreCase(SmtProperty.SMT_QUERY_TYPE.getName()))
      {
        LinkQuery qType = LinkQuery.getByName(map.get(SmtProperty.SMT_QUERY_TYPE.getName()));
        
        if(qType == null)
        {
          logger.severe("Invalid SmtLink query option");
          subCommand = SmtProperty.SMT_HELP.getName();
          return false;
        }
        
        OSM_FabricDelta                   fd = null;
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
          links = fabric.getIB_Links(g);        
        }
        
        switch (qType)
        {
          case LINK_LIST:
            System.out.println(LinkQuery.describeAllQueryTypes());
            break;
            
          case LINK_CONFIG:
            OSM_Configuration cfg = getOsmConfig(true);
            if((cfg != null) && (cfg.getFabricConfig() != null) && (cfg.getFabricConfig().getFabricName() != null))
            {
              // save this configuration and then perform a check
              OSM_Configuration.cacheOSM_Configuration(OMService.getFabricName(), cfg);
              System.out.print(cfg.getFabricConfig().toLinkStrings(delimString));
            }
            else
            {
              logger.severe("Couldn't obtain Fabric configuration, check service connection and existance of config file.");
              System.err.println("Couldn't obtain Fabric configuration, check service connection and existance of config file.");
            }
            break;
            
          case LINK_CURRENT:
            // use the current fabric
            if(OMService.getFabric() != null)
            {
              IB_FabricJson fab = new IB_FabricJson(OMService.getFabric());
              System.out.print(fab.toLinkString(delimString));
            }
            else
            {
              logger.severe("Couldn't obtain Fabric, check service connection.");
              System.err.println("Couldn't obtain Fabric, check service connection.");
            }
            break;
            
          case LINK_STATUS:
            IB_Link link = IB_Link.getIB_Link(getAllLinks(), p);
            if(link != null)
              System.out.println(SmtLink.getLinkSummary(OMService, link));
            else
              showStatus();
            break;
            
          case LINK_LEVELS:
            showLevels();
            break;
            
          case LINK_ALL:
          printStringMap(IB_LinkInfo.getLinkInfoRecords(OMService, fd, true, true));
            break;
            
          case LINK_ACTIVE:
          printStringMap(IB_LinkInfo.getLinkInfoRecords(OMService, fd, true, false));
            break;
            
          case LINK_DOWN:
          printStringMap(IB_LinkInfo.getLinkInfoRecords(OMService, fd, false, true));
            break;
            
          case LINK_ERROR:
            printStringMap(IB_LinkInfo.getErrorLinkInfoRecords(OMService, fd));
            break;
            
          case LINK_SWITCHES:
            printStringMap(IB_LinkInfo.getSWLinkInfoRecords(OMService, fd, !onlyMissing, includeMissing));
            break;
            
          case LINK_HOSTS:
            printStringMap(IB_LinkInfo.getCALinkInfoRecords(OMService, fd, !onlyMissing, includeMissing));
            break;
            
          case LINK_SPEED:
            OSM_LinkSpeed ls = getLinkSpeed(config);
            dumpAllLinks(ls);
            System.exit(0);
            break;
            
            default:
              System.out.println("That's not an option");
              break;
         }
        System.exit(0);
      }
      else if (subCommand.equalsIgnoreCase(SmtProperty.SMT_STATUS.getName()))
      {
        showStatus();
      }
      else if (subCommand.equalsIgnoreCase(SmtProperty.SMT_DUMP.getName()))
      {
        dumpAllLinks();
      }
      else if (subCommand.equalsIgnoreCase(SmtProperty.SMT_LIST.getName()))
      {
        System.err.println("List");
      }
      else if(subCommand.equals(SmtProperty.SMT_QUERY_LEVEL.getName()) && subCommandArg != null)
      {
        Integer level = new Integer(subCommandArg);
        LinkedHashMap<String, String> lmap = IB_LinkInfo.getLinkInfoRecordsByDepth(OMService, getOSM_FabricDelta(false), level.intValue());
        System.out.println("Link Level " + level.intValue() + ": " + lmap.size() + " links");
        printStringMap(lmap);
      }
      else if(subCommand.equals(SmtProperty.SMT_STATUS.getName()))
      {
        showStatus();
      }
      else if(g == null)
      {
        showStatus();        
      }
      
      if((g != null) || (p != null))
      {
        if(pNum < 1)
          printStringMap(IB_LinkInfo.getLinkInfoRecordsByGuid(g, OMService, getOSM_FabricDelta(false), !onlyMissing, includeMissing));
        else
          printStringMap(IB_LinkInfo.getLinkInfoRecordsByPort(p, OMService, getOSM_FabricDelta(false)));
      }
    return true;
  }

  /************************************************************
   * Method Name:
   *  dumpAllLinks
  **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   * @param ls
   ***********************************************************/
  private void dumpAllLinks(OSM_LinkSpeed lspeed)
  {
    // "ALL" IB_Links
    ArrayList <IB_Link> ibla = getAllLinks();
    if((ibla != null) && (lspeed != null))
      for(IB_Link l: ibla)
      {
        if(lspeed == l.getSpeed())
          printLinkInfo(l);
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
    USAGE = "[-h=<host url>] [-pn=<port num>] ";
    HEADER = "smt-link - a tool for obtaining high level link information";
    EXAMPLE ="examples:" + SmtConstants.NEW_LINE +
        "> smt-link -pn 10011 -q status           - provide a link summary" + SmtConstants.NEW_LINE + 
        "> smt-link -pn 10013 -q errors           - show the links that are currently experiencing errors" + SmtConstants.NEW_LINE + 
        "> smt-link -q status 196 17              - show the link associated with lid 196 port 17" + SmtConstants.NEW_LINE + 
        "> smt-link -pn 10013 -q switches -oM T   - show only the down links between switches" + SmtConstants.NEW_LINE + 
        "> smt-link -q speed EDR                  - show all of the EDR links" + SmtConstants.NEW_LINE + 
        "> smt-link -rH surface3h.his -dump       - dump all the link information" + SmtConstants.NEW_LINE  + ".";  // terminate with nl

    // create and initialize the common options for this command
    initCommonOptions();
    
//    // add non-common options

    // initialize the command specific options
    SmtProperty sp = SmtProperty.SMT_QUERY_TYPE;
    Option qType     = OptionBuilder.hasArg(true).hasArgs(1).withArgName( sp.getArgName() ).withValueSeparator('=').withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );

    sp = SmtProperty.SMT_QUERY_LIST;
    Option qList = new Option( sp.getShortName(), sp.getName(), false, sp.getDescription() );    

    sp = SmtProperty.SMT_STATUS;
    Option status  = OptionBuilder.hasArg(false).withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );
    
    sp = SmtProperty.SMT_DUMP;
    Option dump  = OptionBuilder.hasArg(false).withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );
    
    sp = SmtProperty.SMT_DELIMITER;
    Option delim  = OptionBuilder.hasArg(true).hasArgs(1).withArgName( sp.getArgName() ).withValueSeparator('=').withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );
    
    sp = SmtProperty.SMT_QUERY_LEVEL;
    Option level  = OptionBuilder.hasArg(true).hasArgs(1).withArgName( sp.getArgName() ).withValueSeparator('=').withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );

    sp = SmtProperty.SMT_ONLY_MISSING;
    Option oMissing  = OptionBuilder.hasArg(true).hasArgs(1).withArgName( sp.getArgName() ).withValueSeparator('=').withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );

    options.addOption( qType );
    options.addOption( qList );
    options.addOption( status );
    options.addOption( dump );
    options.addOption( delim );

    options.addOption( level );
    options.addOption( oMissing );
    
    return true;
  }

  /************************************************************
   * Method Name:
   *  parseCommands
   **/
  /**
   * Describe the method here
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

    // save some defaults
    config.put(SmtProperty.SMT_DELIMITER.getName(), "   ");
    config.put(SmtProperty.SMT_INCLUDE_MISSING.getName(), "true");
    config.put(SmtProperty.SMT_ONLY_MISSING.getName(),    "false");
    
    sp = SmtProperty.SMT_DELIMITER;
    if(line.hasOption(sp.getName()))
    {
      config.put(sp.getName(), line.getOptionValue(sp.getName()));
      // this is an argument for the query configured subcommand
      // this is NOT a subcommand
    }    

    sp = SmtProperty.SMT_ONLY_MISSING;
    if(line.hasOption(sp.getName()))
    {
      // if true,  then include missing is true also
      // if false, then include missing is false also
      config.put(sp.getName(),                              line.getOptionValue(sp.getName()));
      config.put(SmtProperty.SMT_INCLUDE_MISSING.getName(), line.getOptionValue(sp.getName()));
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
  

  private int getNumAtLevel(LinkedHashMap <String, IB_Edge> eMap, int level)
  {
    // given a map of edges, return only those at the desired level (sort by guid then port number)
    int num = 0;
    
    // iterate through the map, and add only those with the desired depth or level
    for (Entry<String, IB_Edge> entry : eMap.entrySet())
    {
      IB_Edge e = entry.getValue();
      if(e.getDepth() == level)
        num++;
     }
    return num;
  }

  private int getNumLevels(LinkedHashMap <String, IB_Edge> eMap)
  {
    // given a map of edges, find the maximum depth, it must be the number of levels
    int maxLevel = 0;
    
    for (Entry<String, IB_Edge> entry : eMap.entrySet())
    {
      IB_Edge e = entry.getValue();
      maxLevel = maxLevel > e.getDepth() ? maxLevel: e.getDepth();
     }
    return maxLevel+1;
  }

  /************************************************************
   * Method Name:
   *  dumpAllLinks
  **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   ***********************************************************/
  private void dumpAllLinks()
  {
    // "ALL" IB_Links
    ArrayList <IB_Link> ibla = getAllLinks();
    for(IB_Link l: ibla)
    {
      System.out.println(l.toContent());
    }
  }
  
  /************************************************************
   * Method Name:
   *  dumpAllLinks
  **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   ***********************************************************/
  private ArrayList <IB_Link> getAllLinks()
  {
    // "ALL" IB_Links
    ArrayList <IB_Link> ibla = null;
    OSM_Fabric Fabric = OMService.getFabric();
    OSM_Nodes AllNodes = (Fabric == null) ? null : Fabric.getOsmNodes();
    OSM_Ports AllPorts = (Fabric == null) ? null : Fabric.getOsmPorts();

    if ((AllPorts != null) && (AllNodes != null))
    {
        // create IB_Links
        ibla = AllPorts.createIB_Links(AllNodes);
    }
    return ibla;
  }
  
  private void showStatus()
  {
    System.out.println(IB_LinkInfo.getStatus(OMService));
  }
  
  private void showLevels()
  {
    StringBuffer buff = new StringBuffer();
    String formatString = "%9s: %s";
    LinkedHashMap <String, IB_Edge> edgeMap = getEdgeMap();
    if(edgeMap != null)
    {
      buff.append(String.format(formatString, "total", edgeMap.size() + " links") + SmtConstants.NEW_LINE);
      
    int maxLevel = getNumLevels(edgeMap);
    for(int l = 0; l < maxLevel; l++)
    {
      buff.append(String.format(formatString, "level " + l, getNumAtLevel(edgeMap, l) + " links") + SmtConstants.NEW_LINE);
    }
    System.out.println(buff.toString());
      
    }
  }
  
  private LinkedHashMap <String, IB_Edge> getEdgeMap()
  {
    OSM_Fabric Fabric = OMService.getFabric();
    
    // vertex map  needs to be created first, so depths can be assigned
    return IB_Vertex.createEdgeMap(IB_Vertex.createVertexMap(Fabric));
  }
  
  private boolean printStringMap(LinkedHashMap <String, String> map)
  {
    // the tops are all calculated, find the links associated with these
    // ports
    // and return the top unique links

    for (Map.Entry<String, String> mapEntry : map.entrySet())
    {
      String s = mapEntry.getValue();
      System.out.println(s);
    }    
    return true;
  }
  
  private boolean printLinkInfo(IB_Link link)
  {
    IB_LinkInfo li = new IB_LinkInfo(link, OMService);
    System.out.println(li.getLinkInfoLine(OMService));
    return true;
  }
    
  public static String getLinkSummary(OpenSmMonitorService oms, IB_Link link)
  {
    String formatString = "%17s:  %s";

    StringBuffer buff = new StringBuffer();
    if(oms != null)
    {
      OSM_Fabric fabric = oms.getFabric();
      if(fabric != null)
      {
        if(link != null)
        {
          OSM_Port p = link.getEndpoint1();
          IB_Guid g  = p.getNodeGuid();
          OSM_Node n = fabric.getOSM_Node(g);
          short pNum = (short)p.getPortNumber();

          buff.append(String.format(formatString, "First Node name", n.pfmNode.node_name + SmtConstants.NEW_LINE));
          buff.append(String.format(formatString, "guid", g.toColonString() + SmtConstants.NEW_LINE));
          buff.append(String.format(formatString, "lid", p.getAddress().getLocalIdHexString() + " (" + p.getAddress().getLocalId() + ")" + SmtConstants.NEW_LINE));
          buff.append(String.format(formatString, "port #", pNum + SmtConstants.NEW_LINE));
          buff.append(String.format(formatString, "port address", OSM_Port.getOSM_PortKey(p) + SmtConstants.NEW_LINE));
          buff.append(String.format(formatString, "errors", p.hasError() + SmtConstants.NEW_LINE));
          buff.append(SmtConstants.NEW_LINE);

          buff.append(String.format(formatString, "state", link.getStateString() + SmtConstants.NEW_LINE));
          buff.append(String.format(formatString, "rate", link.getRate().getRateName() + SmtConstants.NEW_LINE));
          buff.append(String.format(formatString, "speed", link.getSpeed().getSpeedName() + SmtConstants.NEW_LINE));
          buff.append(String.format(formatString, "width", link.getWidth().getWidthName() + SmtConstants.NEW_LINE));
          buff.append(SmtConstants.NEW_LINE);
          
          p = link.getEndpoint2();
          g  = p.getNodeGuid();
          n = fabric.getOSM_Node(g);
          pNum = (short)p.getPortNumber();

          buff.append(String.format(formatString, "Second Node name", n.pfmNode.node_name + SmtConstants.NEW_LINE));
          buff.append(String.format(formatString, "guid", g.toColonString() + SmtConstants.NEW_LINE));
          buff.append(String.format(formatString, "lid", p.getAddress().getLocalIdHexString() + " (" + p.getAddress().getLocalId() + ")" + SmtConstants.NEW_LINE));
          buff.append(String.format(formatString, "port #", pNum + SmtConstants.NEW_LINE));
          buff.append(String.format(formatString, "port address", OSM_Port.getOSM_PortKey(p) + SmtConstants.NEW_LINE));
          buff.append(String.format(formatString, "errors", p.hasError() + SmtConstants.NEW_LINE));
        }
      }
    }
    return buff.toString();
  }
  
  protected IB_Guid getNodeGuid(SmtConfig config, int pNum)
  {
    // if there are any arguments, they normally reference a node or port identifier
    // return null, indicating couldn't be found, or nothing specified
    //
    // a node identifier is a name, guid, or lid
    // a port identifier is a node identifier, followed by an int
    if(config != null)
    {
      Map<String,String> map = config.getConfigMap();
      String portid = map.get(SmtProperty.SMT_COMMAND_ARGS.getName());
      if(portid != null)
      {
        // should be at least one word
        //  if more than one, check to see if the pNum arg
        //  passed in, is non-zero.
        // If so,
        //   then use everything except the last word for the guid
        // If not,
        //   then use everything to find the guid
        
        String[] args = portid.split(" ");
        int num = args.length;
        int argNum = 0;
        StringBuffer nodeid = new StringBuffer();
        if((num == 1) || (pNum == 0))
          return getNodeGuid(portid.trim());
        
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
    System.exit((new SmtLink().execute(args)) ? 0: -1);
  }
}
