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
 *        file: SmtEvent.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command.event;

import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_FabricDelta;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.infiniband.opensm.plugin.event.OSM_EventStats;
import gov.llnl.lc.infiniband.opensm.plugin.event.OsmEvent;
import gov.llnl.lc.infiniband.opensm.plugin.net.OsmServerStatus;
import gov.llnl.lc.smt.SmtConstants;
import gov.llnl.lc.smt.command.SmtCommand;
import gov.llnl.lc.smt.command.config.SmtConfig;
import gov.llnl.lc.smt.props.SmtProperty;

import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

/**********************************************************************
 * Describe purpose and responsibility of SmtEvent
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Mar 20, 2013 12:08:04 PM
 **********************************************************************/
public class SmtEvent extends SmtCommand
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
    
    String subCommand = map.get(SmtProperty.SMT_SUBCOMMAND.getName());
    if (subCommand == null)
      subCommand = SmtProperty.SMT_HELP.getName();

    // there should only be one subcommand
    if (subCommand.equalsIgnoreCase(SmtProperty.SMT_QUERY_TYPE.getName()))
    {
      EventQuery qType = EventQuery.getByName(map.get(SmtProperty.SMT_QUERY_TYPE.getName()));
      
      if(qType == null)
      {
        logger.severe("Invalid SmtEvent query option");
       subCommand = SmtProperty.SMT_HELP.getName();
       return false;
      }
      
      switch (qType)
      {
        case EVENT_LIST:
          System.out.println(EventQuery.describeAllQueryTypes());
          break;
          
          
        case EVENT_STATUS:
          if(OMService != null)
             System.out.println(getEventSummary(getOSM_FabricDelta(false), ""));
          else
            System.err.println("An OMS instance is required (connection or file)");
          break;
          
          default:
            System.out.println("That's not an option");
            break;
       }
    }
    else if (subCommand.equalsIgnoreCase(SmtProperty.SMT_STATUS.getName()))
    {
      System.out.println(getEventSummary(getOSM_FabricDelta(false), ""));
    }
    else if (OMService != null)
      System.out.println(getStatus(OMService));
     
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
      return "Can't get status from a null OMS object";
    }
    OSM_Fabric Fabric = OMService.getFabric();
    StringBuffer buff = new StringBuffer();
    
    // total nodes, ports and links, broken down by type
    OsmServerStatus RStatus = OMService.getRemoteServerStatus();

    buff.append(String.format("              Event Status\n"));
    buff.append(SmtConstants.NEW_LINE);
    buff.append(String.format("Fabric Name:         %20s\n", Fabric.getFabricName()));
    if(RStatus != null)
      buff.append(String.format("Up since:            %20s\n", OMService.getRemoteServerStatus().Server.getStartTime().toString() ));
    buff.append(String.format("timestamp:           %20s\n", Fabric.getTimeStamp().toString() ));
    buff.append(SmtConstants.NEW_LINE);
    
    String formatString = "%-19s      %12d";
    
    buff.append("---event type---------------------count--" + SmtConstants.NEW_LINE);
    OSM_EventStats EventStats  = Fabric.getOsmEventStats();
    
    for(OsmEvent s : OsmEvent.OSM_STAT_EVENTS)
      buff.append(String.format(formatString, s.getEventName()+":", EventStats.getCounter(s)) + SmtConstants.NEW_LINE);
    
    return buff.toString();
  }
  
  
  public static String getEventSummary(OSM_FabricDelta fd, String prePend)
  {
    // xx: error state <- (link info) -> guid:portnum name
    StringBuffer buff = new StringBuffer();
    String formatString = "%s %19s: %7d,  (%5d)";
    
    if(fd != null)
    {
    OSM_Fabric fabric = fd.getFabric2();
//    System.err.println(fd.getFabric1().getTimeStamp());
//    System.err.println(fd.getFabric2().getTimeStamp());
//    System.err.println(fd.getAgeDifference(TimeUnit.MINUTES));
    OSM_EventStats DiffStats  = fd.getEventChanges();
    OSM_EventStats EventStats  = fabric.getOsmEventStats();
    buff.append("                 Event Summary"+ SmtConstants.NEW_LINE);
    buff.append(" " + fabric.getTimeStamp() + "         (delta: " + fd.getDeltaSeconds() + "s)"+ SmtConstants.NEW_LINE);
    buff.append("                       count  (delta count)"+ SmtConstants.NEW_LINE);
    
    for(OsmEvent s : OsmEvent.OSM_STAT_EVENTS)
      buff.append(String.format(formatString, prePend, s.getEventName(), EventStats.getCounter(s), DiffStats.getCounter(s)) + SmtConstants.NEW_LINE);
    }
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
   * @see gov.llnl.lc.smt.command.SmtCommand#initCommonOptions()
   *
   * @return
   ***********************************************************/

  @Override
  public boolean init()
  {
    USAGE = "[-h=<host url>] [-pn=<port num>] -sr";
    HEADER = "smt-event - a tool for obtaining event information";
    EXAMPLE ="examples:" + SmtConstants.NEW_LINE +
        "> smt-event -pn 10013" + SmtConstants.NEW_LINE + 
        "> smt-event -pn 10013 -sr" + SmtConstants.NEW_LINE + 
        "> smt-event -rH surface3h.his -q status" + SmtConstants.NEW_LINE  + ".";  // terminate with nl

    // create and initialize the common options for this command
    initCommonOptions();
    
    // add non-common options
    initMulitReadFileOptions();  // read the remainder of the file types

    SmtProperty sp = SmtProperty.SMT_QUERY_TYPE;
    Option qType     = OptionBuilder.hasArg(true).hasArgs(1).withArgName( sp.getArgName() ).withValueSeparator('=').withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );

    sp = SmtProperty.SMT_QUERY_LIST;
    Option qList = new Option( sp.getShortName(), sp.getName(), false, sp.getDescription() );
    
    sp = SmtProperty.SMT_STATUS;
    Option status  = OptionBuilder.hasArg(false).withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );
    
    options.addOption( status );
    options.addOption( qType );
    options.addOption( qList );
    
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
    System.exit((new SmtEvent().execute(args)) ? 0: -1);
  }

}
