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

import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.core.IB_Link;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_FabricDelta;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Nodes;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Ports;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Edge;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;
import gov.llnl.lc.smt.SmtConstants;
import gov.llnl.lc.smt.command.SmtCommand;
import gov.llnl.lc.smt.command.config.SmtConfig;
import gov.llnl.lc.smt.props.SmtProperty;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

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
    
    int pNum  = getPortNumber(config);
    IB_Guid g = getNodeGuid(config, pNum);
    
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
    
    if(OMService == null)
      logger.severe("The service is null");

    // this is the LINK command, and it can take a subcommand and an argument
      String subCommandArg = null;
      boolean onlyMissing   = false;
      boolean includeMissing = true;
      
      subCommandArg = map.get(subCommand);
        
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
        
        OSM_FabricDelta fd = null;
        if(OMService != null)
          fd = getOSM_FabricDelta(false);

        switch (qType)
        {
          case LINK_LIST:
            System.out.println(LinkQuery.describeAllQueryTypes());
            break;
            
          case LINK_STATUS:
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
      
      if((g != null) || (pNum > 0))
      {
        OSM_Port                           p = null;
        if((OMService != null) && (g != null))
          p = OMService.getFabric().getOSM_Port(OSM_Port.getOSM_PortKey(g.getGuid(), (short)pNum));
        if(pNum < 1)
          printStringMap(IB_LinkInfo.getLinkInfoRecordsByGuid(g, OMService, getOSM_FabricDelta(false), !onlyMissing, includeMissing));
        else
          printStringMap(IB_LinkInfo.getLinkInfoRecordsByPort(p, OMService, getOSM_FabricDelta(false)));
      }
    return false;
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
        "> smt-link -pn 10013 -q switches -oM T   - show only the down links between switches" + SmtConstants.NEW_LINE + 
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
    
    sp = SmtProperty.SMT_QUERY_LEVEL;
    Option level  = OptionBuilder.hasArg(true).hasArgs(1).withArgName( sp.getArgName() ).withValueSeparator('=').withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );

    sp = SmtProperty.SMT_ONLY_MISSING;
    Option oMissing  = OptionBuilder.hasArg(true).hasArgs(1).withArgName( sp.getArgName() ).withValueSeparator('=').withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );

    options.addOption( qType );
    options.addOption( qList );
    options.addOption( status );
    options.addOption( dump );

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

    config.put(SmtProperty.SMT_INCLUDE_MISSING.getName(), "true");
    config.put(SmtProperty.SMT_ONLY_MISSING.getName(),    "false");
    
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
    ArrayList <IB_Link> ibla = null;
    OSM_Fabric Fabric = OMService.getFabric();
    OSM_Nodes AllNodes = (Fabric == null) ? null : Fabric.getOsmNodes();
    OSM_Ports AllPorts = (Fabric == null) ? null : Fabric.getOsmPorts();

    if ((AllPorts != null) && (AllNodes != null))
    {
        // create IB_Links
        ibla = AllPorts.createIB_Links(AllNodes);
        
        for(IB_Link l: ibla)
        {
          System.out.println(l.toContent());
        }
    }
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
  
  private IB_Guid getNodeGuid(SmtConfig config, int pNum)
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
