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
 *        file: SmtUtilize.java
 *
 *  Created on: May 31, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command.server;

import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Configuration;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Stats;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Subnet;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_SysInfo;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.infiniband.opensm.plugin.net.OsmServerStatus;
import gov.llnl.lc.net.ObjectSession;
import gov.llnl.lc.smt.SmtConstants;
import gov.llnl.lc.smt.command.SmtCommand;
import gov.llnl.lc.smt.command.config.SmtConfig;
import gov.llnl.lc.smt.props.SmtProperty;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;


/**********************************************************************
 * The SmtServer command is a client utility for obtaining and displaying
 * information about the OpenSM Monitoring Service (OMS).  It is useful
 * primarily to determine; 1 - if the service is up, and available
 *                         2 - the versions and build dates of components
 *                         3 - timestamps and heartbeats (activity)
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version May 31, 2016 2:56:01 PM
 **********************************************************************/
public class SmtServer extends SmtCommand
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
    // support online or offline (connected or file based)
    // support single shot
    //
    //  online & single       (reqr: single OMS)
    //  offline & single      (reqr: OMS History)
     //
    // this is the server command, and ultimately we want to be able
    // to operate off an instance of the the service (OFFLINE) or
    // with a direct connection with the service.
    
    boolean exit = false;
    
      Map<String,String> map = smtConfig.getConfigMap();
      OSM_Configuration cfg  = null;
      
      String subCommand    = map.get(SmtProperty.SMT_SUBCOMMAND.getName());
      String subCommandArg = null;
      
      if (subCommand == null)
        subCommand = SmtProperty.SMT_HELP.getName();
      
      // there should only be one subcommand
      if (subCommand.equalsIgnoreCase(SmtProperty.SMT_STATUS.getName()))
      {
        System.out.println(getStatus(OMService));
        exit = true;
      }
      else if (subCommand.equalsIgnoreCase(SmtProperty.SMT_QUERY_TYPE.getName()))
      {
        ServerQuery qType = ServerQuery.getByName(map.get(SmtProperty.SMT_QUERY_TYPE.getName()));
             
        if(qType == null)
        {
          logger.severe("Invalid SmtServer query option");
          System.err.println("Invalid SmtServer query option");
          subCommand = SmtProperty.SMT_HELP.getName();
         return false;
        }
        
        switch (qType)
        {
          case SERVER_LIST:
            System.out.println(ServerQuery.describeAllQueryTypes());
            break;
            
          case SERVER_STATUS:
            System.out.println(getStatus(OMService));
            break;
            
          case SERVER_CONNECT:
            System.out.println(getConnection(OMService));
            break;
            
            default:
              // should never get here, because it will be trapped above
              System.err.println("Invalid SmtServer query option, again");
              break;
         }
      }
      else if(OMService != null)
      {
        System.out.println(getStatus(OMService));
      }
    return exit;
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
    USAGE = "[-h=<host url>] [-pn=<port num>] [-rH=<filename>";
    HEADER = "\nsmt-server - shows the status of the OpenSM Monitoring Service\n\n";
    EXAMPLE ="examples:" + SmtConstants.NEW_LINE +
        "> smt-server -pn 10013          - shows the active status of the service via a direct connection" + SmtConstants.NEW_LINE + 
        "> smt-server -rH HypeFR.his     - shows the historical status of the service from the file" + SmtConstants.NEW_LINE + ".";  // terminate with nl 

    // create and initialize the common options for this command
    initCommonOptions();
    
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
   *  parseCommands
   **/
  /**
   * Parse the command line options here.  If any of these need to be persistent
   * then they need to be "put" into the config map.  Otherwise the command line
   * options need to set "command" flags or variables, which will potentially be
   * used later, typically by the "doCommand()".
   *
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
    
    // set the command and sub-command (always do this) to default values
    config.put(SmtProperty.SMT_COMMAND.getName(), this.getClass().getName());
//    config.put(SmtProperty.SMT_SUBCOMMAND.getName(), SmtProperty.SMT_QUERY_ALL.getName());
     
    SmtProperty sp = SmtProperty.SMT_READ_OMS_HISTORY;
    if(line.hasOption(sp.getName()))
    {
      // put the file in both places
      status = putHistoryProperty(config, line.getOptionValue(sp.getName()));
      if(status)
      {
      config.put(SmtProperty.SMT_FILE_NAME.getName(), convertSpecialFileName(line.getOptionValue(sp.getName())));
      config.put(SmtProperty.SMT_OMS_COLLECTION_FILE.getName(), convertSpecialFileName(line.getOptionValue(sp.getName())));
      }
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

    /* just save this, if it exists in the map then do it */
    sp = SmtProperty.SMT_STATUS;
    if(line.hasOption(sp.getName()))
    {
      config.put(SmtProperty.SMT_SUBCOMMAND.getName(), sp.getName());
      config.put(sp.getName(), line.getOptionValue(sp.getName()));
    }
        
     return status;
  }
  
  public static String getStatus(OpenSmMonitorService OMService)
  {
    if(OMService == null)
    {
      logger.severe("Can't get status from a null object");
      return "Can't get status from a null OMS object";
    }
    ObjectSession   ParentSessionStatus  = null;
    OsmServerStatus ServerStatus         = null;
    OSM_Fabric      Fabric               = null;
    
    OSM_SysInfo SysInfo = null;
    OSM_Stats Stats     = null;
    OSM_Subnet Subnet   = null;
    HashMap<String, String> OptionsMap = null;
    
    if(OMService.getParentSessionStatus() != null)
      ParentSessionStatus = OMService.getParentSessionStatus();
    if(OMService.getRemoteServerStatus() != null)
      ServerStatus = OMService.getRemoteServerStatus();
    if(OMService.getFabric() != null)
      Fabric = OMService.getFabric();
    
    if(Fabric != null)
    {
      SysInfo     = Fabric.getOsmSysInfo();
      Stats       = Fabric.getOsmStats();
      Subnet      = Fabric.getOsmSubnet();
      OptionsMap  = Fabric.getOptions();      
    }

    // Report versions, uptime, sample rate, etc. of service
    // See F9 of SmtConsole and also the Details page of the gui
    // under both the Server and the Native Plugin
    
    StringBuffer buff = new StringBuffer();
    
    buff.append(" Server host: " + ServerStatus.Server.getHost());
    buff.append(SmtConstants.NEW_LINE);
    if(ServerStatus != null)
    buff.append("    up since: " + OMService.getRemoteServerStatus().Server.getStartTime().toString() + SmtConstants.NEW_LINE);
    if(Fabric != null)
    {
    buff.append("   timestamp: " + Fabric.getTimeStamp().toString() );

    buff.append(SmtConstants.NEW_LINE);
    buff.append("-----------------------------------------" + SmtConstants.NEW_LINE);
    buff.append("Service name: " + "OpenSM Monitoring Service\n");
    buff.append("     version: " + ServerStatus.Version + "\n");
    buff.append("  build date: " + ServerStatus.BuildDate + "\n");
    buff.append("   heartbeat: " + Long.toString(ServerStatus.ServerHeartbeatCount) + "\n");

    // strip out the name and version and build date
    String nm = "OSM_JNI_Plugin ";
    int sndx = SysInfo.OsmJpi_Version.indexOf("(");
    int endx = SysInfo.OsmJpi_Version.lastIndexOf(")");
    String ver = SysInfo.OsmJpi_Version.substring(nm.length(), sndx);
    String da = SysInfo.OsmJpi_Version.substring(sndx + 1, endx-1);
    
    buff.append("-----------------------------------------" + SmtConstants.NEW_LINE);
    buff.append(" Plugin name: " + "OsmJniPi\n");
    buff.append("     version: " + ver + "\n");
    buff.append("  build date: " + da + "\n");
    buff.append("   built for: " + SysInfo.OpenSM_Version + "\n");
    
  }

    buff.append("   heartbeat: " + Long.toString(ServerStatus.NativeHeartbeatCount) + "\n");
    
    return buff.toString();
  }
  
  public static String getConnection(OpenSmMonitorService OMService)
  {
    if(OMService == null)
    {
      logger.severe("Can't get connection info from a null object");
      return "Can't get connection info from a null OMS object";
    }
    ObjectSession   ParentSessionStatus  = null;
    OsmServerStatus ServerStatus         = null;
    OSM_Fabric      Fabric               = null;
    
    OSM_SysInfo SysInfo = null;
    OSM_Stats Stats     = null;
    OSM_Subnet Subnet   = null;
    HashMap<String, String> OptionsMap = null;
    
    if(OMService.getParentSessionStatus() != null)
      ParentSessionStatus = OMService.getParentSessionStatus();
    if(OMService.getRemoteServerStatus() != null)
      ServerStatus = OMService.getRemoteServerStatus();
    if(OMService.getFabric() != null)
      Fabric = OMService.getFabric();
    
    if(Fabric != null)
    {
      SysInfo     = Fabric.getOsmSysInfo();
      Stats       = Fabric.getOsmStats();
      Subnet      = Fabric.getOsmSubnet();
      OptionsMap  = Fabric.getOptions();      
    }
    
    // Report versions, uptime, sample rate, etc. of service
    // See F9 of SmtConsole and also the Details page of the gui
    // under both the Server and the Native Plugin
    
    StringBuffer buff = new StringBuffer();
    
    buff.append("             Server host: " + ServerStatus.Server.getHost());
    buff.append(SmtConstants.NEW_LINE);
    if(ServerStatus != null)
    buff.append("                up since: " + OMService.getRemoteServerStatus().Server.getStartTime().toString() + SmtConstants.NEW_LINE);
    buff.append("               timestamp: " + Fabric.getTimeStamp().toString() );

    buff.append(SmtConstants.NEW_LINE);
    buff.append("-----------------------------------------" + SmtConstants.NEW_LINE);
    buff.append("            Service name: " + "OpenSM Monitoring Service\n");
    buff.append("                 version: " + ServerStatus.Version + "\n");
    buff.append("              build date: " + ServerStatus.BuildDate + "\n");
    buff.append("                    host: " + ServerStatus.Server.getHost() + SmtConstants.NEW_LINE);
    buff.append("                    port: " + Integer.toString(ServerStatus.Server.getPortNum()) + SmtConstants.NEW_LINE);
    buff.append("               thread id: " + Long.toString(ServerStatus.Server.getThreadId())+ SmtConstants.NEW_LINE);
    buff.append("refresh period (seconds): " + Integer.toString(ServerStatus.ServerUpdatePeriodSecs)+ SmtConstants.NEW_LINE);
    buff.append("      cumulative clients: " + Integer.toString(ServerStatus.Server.getHistorical_Sessions().size())+ SmtConstants.NEW_LINE);
    buff.append("          active clients: " + Integer.toString(ServerStatus.Server.getCurrent_Sessions().size())+ SmtConstants.NEW_LINE);
    buff.append("              class name: " + ServerStatus.Server.getServerName()+ SmtConstants.NEW_LINE);
    buff.append("     Authenticator class: " + ParentSessionStatus.getAuthenticator()+ SmtConstants.NEW_LINE);
    buff.append("          Protocol class: " + ParentSessionStatus.getProtocol()+ SmtConstants.NEW_LINE);
    buff.append("               heartbeat: " + Long.toString(ServerStatus.ServerHeartbeatCount) + "\n");

    // strip out the name and version and build date
    String nm = "OSM_JNI_Plugin ";
    int sndx = SysInfo.OsmJpi_Version.indexOf("(");
    int endx = SysInfo.OsmJpi_Version.lastIndexOf(")");
    String ver = SysInfo.OsmJpi_Version.substring(nm.length(), sndx);
    String da = SysInfo.OsmJpi_Version.substring(sndx + 1, endx-1);
    
    buff.append("-----------------------------------------" + SmtConstants.NEW_LINE);
    buff.append("             Plugin name: " + "OsmJniPi\n");
    buff.append("                 version: " + ver + "\n");
    buff.append("              build date: " + da + "\n");
    buff.append("               built for: " + SysInfo.OpenSM_Version + "\n");
    buff.append("refresh period (seconds): " + Integer.toString(ServerStatus.NativeUpdatePeriodSecs)+ SmtConstants.NEW_LINE);
    buff.append(" report period (seconds): " + Integer.toString(ServerStatus.NativeReportPeriodSecs)+ SmtConstants.NEW_LINE);
    buff.append("      event timeout (ms): " + Integer.toString(ServerStatus.NativeEventTimeoutMsecs)+ SmtConstants.NEW_LINE);
    buff.append("             event count: " + Long.toString(ServerStatus.NativeEventCount)+ SmtConstants.NEW_LINE);
    buff.append("               heartbeat: " + Long.toString(ServerStatus.NativeHeartbeatCount) + "\n");

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
    System.exit((new SmtServer().execute(args)) ? 0: -1);
  }

}
