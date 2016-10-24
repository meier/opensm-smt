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
 *        file: SmtSystem.java
 *
 *  Created on: Sept 30, 2016
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command.system;

import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_System;
import gov.llnl.lc.infiniband.opensm.plugin.data.SBN_Node;
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
 * Describe purpose and responsibility of SmtSystem
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Mar 20, 2013 12:08:04 PM
 **********************************************************************/
public class SmtSystem extends SmtCommand
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
    
    // hopefully the node description is here, so save it
    saveCommandArgs(line.getArgs(), config);
    
    // if the host and port is supplied on the command line, assume we are making
    // a connection to the service - no matter what
    //
    // if an OMS_FILE or FABRIC_FILE is supplied, it could be for reading or writing
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
      // save this, only if its a valid file
      status = putHistoryProperty(config, line.getOptionValue(sp.getName()));
      if(status)
      {
        config.put(SmtProperty.SMT_OMS_COLLECTION_FILE.getName(), convertSpecialFileName(line.getOptionValue(sp.getName())));       
      }
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
      // the dump option is intended to process a file, and display the results as soon as possible
      //  - playback speed does not apply - instead process next delta as soon as previous finishes
      //  - wrap does not apply - display from beginning to end, then stop
      //  - connection issues do not apply, this is file based
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
    
    initServiceUpdater(config);
    
    Map<String,String> map = smtConfig.getConfigMap();
    
    String subCommand = map.get(SmtProperty.SMT_SUBCOMMAND.getName());
    if (subCommand == null)
      subCommand = SmtProperty.SMT_HELP.getName();

    // attempt to identify the systems
    ArrayList <OSM_System> sysArray = getOSM_Systems();
    IB_Guid g = getSystemGuid(config, sysArray);
    
    // there should only be one subcommand
    if (subCommand.equalsIgnoreCase(SmtProperty.SMT_QUERY_TYPE.getName()))
    {
      SystemQuery qType = SystemQuery.getByName(map.get(SmtProperty.SMT_QUERY_TYPE.getName()));
      
      if(qType == null)
      {
        logger.severe("Invalid SmtSystem query option");
       subCommand = SmtProperty.SMT_HELP.getName();
       return false;
      }
      
      switch (qType)
      {
        case SYS_LIST:
          System.out.println(SystemQuery.describeAllQueryTypes());
          break;
          
        case SYS_SWITCHES:
          showSwitches(sysArray, g);
         break;
         
        case SYS_LEVELS:
          showLevels(sysArray, g);
          break;
         
        case SYS_PORTS:
          showPorts(sysArray, g);
         break;
         
        case SYS_STATUS:
          showStatus(sysArray, g);
           break;
          
          default:
            System.out.println("That's not an option");
            break;
       }
    }
    else if (subCommand.equalsIgnoreCase(SmtProperty.SMT_STATUS.getName()))
    {
      showStatus(sysArray, g);
    }
    else if (subCommand.equalsIgnoreCase(SmtProperty.SMT_DUMP.getName()))
    {
      if(g == null)
        dumpAllSystems();
      else
        dumpSystem(OSM_System.getOSM_System(sysArray, g));
    }
    else if (OMService != null)
      showStatus(sysArray, g);
     
    return true;
  }
  
  public String toSwitchString(OSM_System sys, boolean includePortDetails)
  {
    OSM_Fabric  Fabric  = OMService.getFabric();
    StringBuffer buff = new StringBuffer();
    String shortHeader  = " lid            guid               name/description         #ports" + SmtConstants.NEW_LINE;
    String shortFormat  = "%5d  %20s  %30s  %3d";

    String detailHeader = " lid            guid               name/description          lvl  up  down total" + SmtConstants.NEW_LINE;
    String detailFormat = "%5d  %20s  %30s  %2d  %3d  %3d  %3d";
    
    // lid  guid   name   num ports
    buff.append(includePortDetails ? detailHeader: shortHeader);
    
    if(includePortDetails)
    {
      LinkedHashMap<String, IB_Vertex> vMap = sys.getVertexMap();
      int maxDepth = sys.getMaxDepth();
      int minDepth = sys.getMinDepth();
      
      for(int d = maxDepth; d>= minDepth; d--)
      {
        LinkedHashMap <String, IB_Vertex> level = IB_Vertex.getVertexMapAtDepth(vMap, d);
        for (Entry<String, IB_Vertex> entry : level.entrySet())
        {
          IB_Vertex v = entry.getValue();
          ArrayList<IB_Edge> el = v.getEdges();
          int upPorts   = 0;
          int downPorts = 0;
          
          // if associated with an edge, then it is active (add up the ones at the lowest which will be external)
          for(IB_Edge e: el)
          {
             if(e.getDepth() < v.getDepth())
               downPorts++;
             else
               upPorts++;
          }
          IB_Guid g = v.getGuid();
          SBN_Node s = Fabric.getOSM_Node(g).sbnNode;
          String name = Fabric.getNameFromGuid(g);
          int lid = Fabric.getLidFromGuid(g);

          buff.append(String.format(detailFormat, lid, g.toColonString(), name, v.getDepth(), upPorts, downPorts, v.getNumPorts()) + SmtConstants.NEW_LINE);
       }    
      }
    }
    else
      for(IB_Guid g: sys.getGuidList())
      {
        SBN_Node s = Fabric.getOSM_Node(g).sbnNode;
        String name = Fabric.getNameFromGuid(g);
        int lid = Fabric.getLidFromGuid(g);
        
        buff.append(String.format(shortFormat, lid, g.toColonString(), name, s.num_ports) + SmtConstants.NEW_LINE);
       }
    return buff.toString();
  }

  public String toSwitchString(OSM_System sys)
  {
    return toSwitchString(sys, false);
  }

  public String toPortString(OSM_System sys)
  {
    StringBuffer buff = new StringBuffer();
     
    // active/inactive and internal/external ports
    buff.append("  port type       active      inactive      total" + SmtConstants.NEW_LINE);
    String format = "   %8s       %5d       %5d        %6d";
    buff.append(String.format(format, "internal", sys.getActiveInternalPorts(), sys.getInactiveInternalPorts(), sys.getTotalInternalPorts()) + SmtConstants.NEW_LINE);
    buff.append(String.format(format, "external", sys.getActiveExternalPorts(), sys.getInactiveExternalPorts(), sys.getTotalExternalPorts()) + SmtConstants.NEW_LINE);
    buff.append(String.format(format, "total",    sys.getTotalActivePorts(),    sys.getTotalInactivePorts(),    sys.getTotalPorts()) + SmtConstants.NEW_LINE);

    return buff.toString();
  }

  public static String toPortStatusString(OSM_System sys)
  {
    StringBuffer buff = new StringBuffer();
     
    // active/inactive and internal/external ports
    buff.append("  inactive ports: " + sys.getTotalInactivePorts() + ", externally available ports: " + sys.getActiveExternalPorts());
    return buff.toString();
  }

  public static String getStatus(ArrayList <OSM_System> sysArray, IB_Guid sysGuid)
  {
    if((sysArray == null) || (sysArray.size() < 1))
    {
      logger.severe("Can't get status from a null object");
      return "Can't get status from a null object";
    }
     // show the status of one or more systems
    OSM_System sys = OSM_System.getOSM_System(sysArray, sysGuid);
    if(sys == null)
      return "System guid: " + sysGuid.toColonString() + " not found";
    
    StringBuffer buff = new StringBuffer();
    buff.append(getStatus(sys)+ SmtConstants.NEW_LINE);
    buff.append(toPortStatusString(sys));
    return buff.toString();
  }
  
  public static String getStatus(OSM_System sys)
  {
    if(sys == null)
      return null;
    
    StringBuffer buff = new StringBuffer();
    buff.append("System " + sys.getSysGuid().toColonString() + " - " + sys.getTotalSwitches() + " switches, " + sys.getTotalPorts() + " ports (" + sys.getTotalPorts()/sys.getTotalSwitches() +"/switch)");
    return buff.toString();
  }
  
  protected ArrayList <OSM_System> getOSM_Systems()
  {
    OSM_Fabric                    fabric = null;
    ArrayList <OSM_System> sysArray = null;

    if(OMService != null)
    {
      fabric = OMService.getFabric();
      if((fabric == null) || (!fabric.isInitialized()))
        return null;
      
      sysArray = OSM_System.getArrayOfSystems(fabric);
    }
    return sysArray;
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
  private void dumpSystem(OSM_System sys)
  {
    if(sys == null)
      return;
    
    StringBuffer buff = new StringBuffer();
    buff.append(getStatus(sys) + SmtConstants.NEW_LINE);
    buff.append(toSwitchString(sys, true));
    
    System.out.println(buff.toString());
    return;
    
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
  private void dumpAllSystems()
  {
    ArrayList <OSM_System> sysArray = getOSM_Systems();
   
    if(sysArray != null)
    {
      for(OSM_System sys: sysArray)
      {
        dumpSystem(sys);
      }
    }
  }

  private void showLevels(ArrayList <OSM_System> sysArray, IB_Guid sysGuid)
  {
    OSM_System sys = OSM_System.getOSM_System(sysArray, sysGuid);
    
    // find the OSM_System, and show the levels
    if(sys != null)
    {
      System.out.println(getStatus(sys));
      System.out.println(sys.toLevels());
    }
   }
  
  private void showSwitches(ArrayList <OSM_System> sysArray, IB_Guid sysGuid)
  {
    OSM_System sys = OSM_System.getOSM_System(sysArray, sysGuid);
    
    // find the OSM_System, and show the levels
    if(sys != null)
    {
      System.out.println(getStatus(sys));
      System.out.println(toSwitchString(sys));
    }
   }
  
  private void showPorts(ArrayList <OSM_System> sysArray, IB_Guid sysGuid)
  {
    OSM_System sys = OSM_System.getOSM_System(sysArray, sysGuid);
    
    // find the OSM_System, and show the levels
    if(sys != null)
    {
      System.out.println(getStatus(sys));
      System.out.println(toPortString(sys));
    }
   }
  
  private void showStatus(ArrayList <OSM_System> sysArray, IB_Guid sysGuid)
  {
    if((sysArray == null) || (sysArray.size() < 1))
    {
      System.out.println("No Systems detected in this fabric");
      return;
    }
    // show the status of one or more systems
    OSM_System sys = OSM_System.getOSM_System(sysArray, sysGuid);
    
    // find the OSM_System, and show the levels
    if(sys != null)
    {
      // print just the single system guid
      System.out.println(getStatus(sysArray, sys.getSysGuid()));
    }
    else if(sysGuid == null)
    {
      // want a summary of all systems, if a null was passed in
        for(OSM_System s: sysArray)
        {
          System.out.println(getStatus(sysArray, s.getSysGuid()));
        }
    }
    else
      System.out.println("System: " + sysGuid.toColonString() + " is not a valid system guid in this fabric.");
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
    USAGE = "[-h=<host url>] [-pn=<port num>] [<sys guid>] -sr";
    HEADER = "smt-system - a tool for obtaining high level system information";
    EXAMPLE ="examples:" + SmtConstants.NEW_LINE +
        "> smt-system -pn 10011                 - using the default port, list any & all systems" + SmtConstants.NEW_LINE + 
        "> smt-system -ql                       - list the query options" + SmtConstants.NEW_LINE + 
        "> smt-system -pn 10011 0006:6a00:e900:131d -q switches\n"
        + ".                                      - list the switches associated with the system" + SmtConstants.NEW_LINE + 
        "> smt-system  0006:6a00:e900:131e -q ports\n"
        + ".                                      - show the port arrangement for the system" + SmtConstants.NEW_LINE + 
        "> smt-system -pn 10013 -dump           - show the systems (if any) in a verbose way" + SmtConstants.NEW_LINE  + ".";  // terminate with nl

    // create and initialize the common options for this command
    initCommonOptions();
    
    SmtProperty sp = SmtProperty.SMT_QUERY_TYPE;
    Option qType     = OptionBuilder.hasArg(true).hasArgs(1).withArgName( sp.getArgName() ).withValueSeparator('=').withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );

    sp = SmtProperty.SMT_QUERY_LIST;
    Option qList = new Option( sp.getShortName(), sp.getName(), false, sp.getDescription() );    

    sp = SmtProperty.SMT_DUMP;
    Option dump  = OptionBuilder.hasOptionalArg().withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );
    
    sp = SmtProperty.SMT_STATUS;
    Option status  = OptionBuilder.hasArg(false).withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );
    
    options.addOption( status );
    options.addOption( qType );
    options.addOption( qList );
    options.addOption( dump );
    
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
  
  private IB_Guid getSystemGuid(SmtConfig config, ArrayList <OSM_System> systems)
  {
    // if there are any arguments, they normally reference a system guid
    // return null, indicating couldn't be found, or nothing specified
    if(config != null)
    {
      Map<String,String> map = config.getConfigMap();
      String guidId = map.get(SmtProperty.SMT_COMMAND_ARGS.getName());
      
      if(guidId != null)
      {
        // should be at least one word
        //  if more than one, use everything except the last word
        //  to support guid, lid, and name
        //  if only one word, the treat it as a guid string
        String[] args = guidId.split(" ");
        int num = args.length;
        int argNum = 0;
        StringBuffer nodeid = new StringBuffer();
        if(num == 1)
          nodeid.append(guidId);
        
        // all but the last arg
        for(String arg: args)
        {
          if(++argNum < num)
            nodeid.append(arg + " ");
        }
        return getSystemGuid(nodeid.toString().trim(), systems);
      }
    }
     return null;
  }
  
  private IB_Guid getSystemGuid(String systemId, ArrayList <OSM_System> systems)
  {
    // if there are any arguments, they normally reference a system guid
    // return null, indicating couldn't be found, or nothing specified
    if((systemId != null) && (systems != null) && (systems.size() > 0))
    {
      IB_Guid sysGuid = null;

      try
      {
        sysGuid = new IB_Guid(systemId);
      }
      catch (Exception e)
      {
        // don't care
      }

      OSM_System sys = OSM_System.getOSM_System(systems, sysGuid);
      if(sys != null)
        return sys.getSysGuid();
    }
     return null;
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
    System.exit((new SmtSystem().execute(args)) ? 0: -1);
  }

}
