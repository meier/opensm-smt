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
 *        file: SmtPrivileged.java
 *
 *  Created on: Jun 3, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command.privileged;

import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import gov.llnl.lc.infiniband.core.IB_Address;
import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.opensm.plugin.OsmNativeCommand;
import gov.llnl.lc.infiniband.opensm.plugin.net.OsmAdminApi;
import gov.llnl.lc.infiniband.opensm.plugin.net.OsmServiceManager;
import gov.llnl.lc.infiniband.opensm.plugin.net.OsmSession;
import gov.llnl.lc.smt.SmtConstants;
import gov.llnl.lc.smt.command.SmtCommand;
import gov.llnl.lc.smt.command.config.SmtConfig;
import gov.llnl.lc.smt.props.SmtProperty;
import gov.llnl.lc.system.CommandLineArguments;
import gov.llnl.lc.system.CommandLineResults;
import gov.llnl.lc.system.Console;

/**********************************************************************
 * The SmtPrivileged command is a convenient tool to invoke
 * commands that cause opensm to take an action or alter its behavior
 * that may directly or indirectly disturb the fabric.  This kind of
 * action requires elevated privileges.
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Jan 5, 2015 7:46:43 AM
 **********************************************************************/
public class SmtPrivileged extends SmtCommand
{
  private static final String NODE_ID = "nodeid";
  private static final String PORT_ID = "portid";
  private static final String ARG_ID  = "cmdArgs";
  
  private OsmSession ParentSession = null;
  
  /* the one and only OsmServiceManager */
  private volatile OsmServiceManager OsmService = null;

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
    OsmService                 = OsmServiceManager.getInstance();
    OsmAdminApi adminInterface = null;
    String subCommand          = null;
    CommandLineResults results = null;

    if (config != null)
    {
      // open a session for the command
      Map<String, String> map = config.getConfigMap();
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

      subCommand = map.get(SmtProperty.SMT_SUBCOMMAND.getName());
      if (subCommand == null)
        subCommand = SmtProperty.SMT_HELP.getName();

      // there should only be one subcommand
      if (subCommand.equalsIgnoreCase(SmtProperty.SMT_PRIV_EXT.getName()))
      {
        String cmdArgs = getCommandArgs(map);
        System.err.println("The command (" + cmdArgs + ")");
        results = adminInterface.invokePrivilegedCommand(new CommandLineArguments(cmdArgs));
      }
      else if (subCommand.equalsIgnoreCase(SmtProperty.SMT_PRIV_ENABLE.getName()))
      {
        String portid = getPortIdentification(map);
        results = ibportstate(portid + " enable");
      }
      else if (subCommand.equalsIgnoreCase(SmtProperty.SMT_PRIV_DISABLE.getName()))
      {
        String portid = getPortIdentification(map);
        results = ibportstate(portid + " disable");
      }
      else if (subCommand.equalsIgnoreCase(SmtProperty.SMT_PRIV_QUERY_PORT.getName()))
      {
        String portid = getPortIdentification(map);
        results = ibportstate(portid);
      }
      else if (subCommand.equalsIgnoreCase(SmtProperty.SMT_PRIV_UPDATE_DESC.getName()))
      {
        results = adminInterface.invokePrivilegedCommand(new CommandLineArguments(OsmNativeCommand.OSM_NATIVE_UPDATE_DESC.getCommandName()));
      }
      else if (subCommand.equalsIgnoreCase(SmtProperty.SMT_PRIV_REROUTE.getName()))
      {
        results = adminInterface.invokePrivilegedCommand(new CommandLineArguments(OsmNativeCommand.OSM_NATIVE_REROUTE.getCommandName()));
      }
      else if (subCommand.equalsIgnoreCase(SmtProperty.SMT_PRIV_PM_CLEAR.getName()))
      {
         results = adminInterface.invokePrivilegedCommand(new CommandLineArguments(OsmNativeCommand.OSM_NATIVE_PCLEAR.getCommandName()));
      }
      else if (subCommand.equalsIgnoreCase(SmtProperty.SMT_PRIV_LT_SWEEP.getName()))
      {
        results = adminInterface.invokePrivilegedCommand(new CommandLineArguments(OsmNativeCommand.OSM_NATIVE_LSWEEP.getCommandName()));
      }
      else if (subCommand.equalsIgnoreCase(SmtProperty.SMT_PRIV_HV_SWEEP.getName()))
      {
        results = adminInterface.invokePrivilegedCommand(new CommandLineArguments(OsmNativeCommand.OSM_NATIVE_HSWEEP.getCommandName()));
      }
      else if (subCommand.equalsIgnoreCase(SmtProperty.SMT_PRIV_PM_SWEEP.getName()))
      {
        results = adminInterface.invokePrivilegedCommand(new CommandLineArguments(OsmNativeCommand.OSM_NATIVE_PSWEEP.getCommandName()));
      }
      else if (subCommand.equalsIgnoreCase(SmtProperty.SMT_PRIV_OSM_LOG_LEVEL.getName()))
      {
        SmtProperty sc = SmtProperty.getByName(subCommand);
        
        // now use that property to find the sub commands argument
        if(sc != null)
        {
          String subCommandArg = map.get(sc.getName());
          if(subCommandArg != null)
          {
            // needs to be in hex format, with 0x out front
            results = adminInterface.invokePrivilegedCommand(new CommandLineArguments(OsmNativeCommand.OSM_NATIVE_LOGLEVEL.getCommandName() + " " + subCommandArg));
          }
          System.err.println("The subcommand and args are: " + subCommand + ", " + subCommandArg);
        }
      }
      else if (subCommand.equalsIgnoreCase(SmtProperty.SMT_PRIV_PM_SWEEP_PERIOD.getName()))
      {
        SmtProperty sc = SmtProperty.getByName(subCommand);
        
        // now use that property to find the sub commands argument
        if(sc != null)
        {
          String subCommandArg = map.get(sc.getName());
          if(subCommandArg != null)
          {
            // needs to be in seconds
            results = adminInterface.invokePrivilegedCommand(new CommandLineArguments(OsmNativeCommand.OSM_NATIVE_PPERIOD.getCommandName() + " " + subCommandArg));
          }
          System.err.println("The subcommand and args are: " + subCommand + ", " + subCommandArg);
        }
      }
      else
      {
        System.err.println("A priv command: " + subCommand + " was NOT handled");
      }
      
      if(results == null)
      {
        System.out.println("Privileged command denied");
        return false;
      }

      // show the results (color the System.err RED)
      if ((results.getOutput() != null) && (results.getOutput().length() > 1))
        System.out.println(results.getOutput());
      if ((results.getError() != null) && (results.getError().length() > 1))
      {
        Console.textNormal();
        Console.textColor(Console.ConsoleColor.red);
        System.out.println(results.getError());
      }
      Console.textNormal();
      Console.backgroundNormal();
      return true;
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
    USAGE = "[-v] [-?]  ";
    HEADER = "smt-priv - invokes a privileged command (requires a connection)";
    EXAMPLE ="examples:" + SmtConstants.NEW_LINE +
        "> smt-priv -pn 10011 -X \"hostname\"  - invoke the hostname command on the OMS node, and return the results" + SmtConstants.NEW_LINE + 
        "> smt-priv -pn 10013 -dP 14 3       - disable port 3 of node with lid 14" + SmtConstants.NEW_LINE + 
        "> smt-priv -pn 10011 -rt            - re-routes the fabric" + SmtConstants.NEW_LINE + 
        "> smt-priv -pn 10013 -uD            - updates the node descriptions" + SmtConstants.NEW_LINE  + ".";  // terminate with nl

    // create and initialize the common options for this command
    initConnectionOptions();
    
    // initialize the command specific options
    SmtProperty sp = SmtProperty.SMT_PRIV_EXT;
    Option ext     = OptionBuilder.hasArg(true).hasArgs().withArgName( sp.getArgName() ).withValueSeparator('=').withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );

    sp = SmtProperty.SMT_PRIV_ENABLE;
    Option enable  = OptionBuilder.hasArg(true).hasArgs(2).withArgName( sp.getArgName() ).withValueSeparator('=').withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );
    sp = SmtProperty.SMT_PRIV_DISABLE;
    Option disable = OptionBuilder.hasArg(true).hasArgs(2).withArgName( sp.getArgName() ).withValueSeparator('=').withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );
    sp = SmtProperty.SMT_PRIV_QUERY_PORT;
    Option query  = OptionBuilder.hasArg(true).hasArgs(2).withArgName( sp.getArgName() ).withValueSeparator('=').withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );

    sp = SmtProperty.SMT_PRIV_OSM_LOG_LEVEL;
    Option logLevel = OptionBuilder.hasArg(true).hasArgs(1).withArgName( sp.getArgName() ).withValueSeparator('=').withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );
    sp = SmtProperty.SMT_PRIV_PM_SWEEP_PERIOD;
    Option sPeriod = OptionBuilder.hasArg(true).hasArgs(1).withArgName( sp.getArgName() ).withValueSeparator('=').withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );

    sp = SmtProperty.SMT_PRIV_UPDATE_DESC;
    Option update  = OptionBuilder.hasArg(false).withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );
    sp = SmtProperty.SMT_PRIV_REROUTE;
    Option reroute  = OptionBuilder.hasArg(false).withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );
    sp = SmtProperty.SMT_PRIV_PM_CLEAR;
    Option clear  = OptionBuilder.hasArg(false).withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );
    sp = SmtProperty.SMT_PRIV_LT_SWEEP;
    Option lsweep  = OptionBuilder.hasArg(false).withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );
    sp = SmtProperty.SMT_PRIV_HV_SWEEP;
    Option hsweep  = OptionBuilder.hasArg(false).withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );
    sp = SmtProperty.SMT_PRIV_PM_SWEEP;
    Option psweep  = OptionBuilder.hasArg(false).withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );

    options.addOption( ext );
    options.addOption( enable );
    options.addOption( disable );
    options.addOption( query );
    
    options.addOption( logLevel );
    options.addOption( sPeriod );
    
    options.addOption( update );
    options.addOption( reroute );
    options.addOption( clear );
    options.addOption( lsweep );
    options.addOption( hsweep );
    options.addOption( psweep );
    
    return true;
  }

  /************************************************************
   * Method Name:
   *  parseCommands
   **/
  /**
  * Every command gets its command line options parsed and saved by an instance
   * of SmtConfig. After normal parsing, additional command specific parsing can
   * occur in this method. It is typically used to persist additional command
   * line arguments in the config map for future use.
   * 
   * @see gov.llnl.lc.smt.command.SmtConfig#parseCommandLineOptions
   * @see gov.llnl.lc.smt.command.SmtCommandInterface#parseCommands(java.util.Map,
   *      org.apache.commons.cli.CommandLine)
   * 
   * @param config
   * @param line
   * @return
   ***********************************************************/

  @Override
  public boolean parseCommands(Map<String, String> config, CommandLine line)
  {
    // set the command and sub-command
    config.put(SmtProperty.SMT_COMMAND.getName(), this.getClass().getName());
    
    // parse (only) the command specific options (see init() )
    SmtProperty sp = SmtProperty.SMT_PRIV_EXT;
    saveCommandArgs(sp.getName(), config, line);
    
    // these commands take exactly two arguments (last one wins)
    sp = SmtProperty.SMT_PRIV_ENABLE;
    savePortIdentification(sp.getName(), config, line);
    sp = SmtProperty.SMT_PRIV_DISABLE;
    savePortIdentification(sp.getName(), config, line);
    sp = SmtProperty.SMT_PRIV_QUERY_PORT;
    savePortIdentification(sp.getName(), config, line);
    
    
    // these commands take exactly one argument
    sp = SmtProperty.SMT_PRIV_OSM_LOG_LEVEL;
    if(line.hasOption(sp.getName()))
    {
      config.put(SmtProperty.SMT_SUBCOMMAND.getName(), sp.getName());
      config.put(sp.getName(), line.getOptionValue(sp.getName()));
    }
    sp = SmtProperty.SMT_PRIV_PM_SWEEP_PERIOD;
    if(line.hasOption(sp.getName()))
    {
      config.put(SmtProperty.SMT_SUBCOMMAND.getName(), sp.getName());
      config.put(sp.getName(), line.getOptionValue(sp.getName()));
    }
    
    // the remainder of the commands don't take arguments
    saveSubCommand(SmtProperty.SMT_PRIV_UPDATE_DESC.getName(), config, line);
    saveSubCommand(SmtProperty.SMT_PRIV_REROUTE.getName(), config, line);
    saveSubCommand(SmtProperty.SMT_PRIV_PM_CLEAR.getName(), config, line);
    saveSubCommand(SmtProperty.SMT_PRIV_LT_SWEEP.getName(), config, line);
    saveSubCommand(SmtProperty.SMT_PRIV_HV_SWEEP.getName(), config, line);
    saveSubCommand(SmtProperty.SMT_PRIV_PM_SWEEP.getName(), config, line);

    return true;
  }
  
  private boolean savePortIdentification(String cName, Map<String, String> config, CommandLine line)
  {
    if(line.hasOption(cName))
    {
    // there should be a guid or lid, followed by a port number
    String[] args = line.getOptionValues(cName);
    if(args.length != 2)
    {
      // log an error, and exit
      System.err.println("command requires a two arguments");
      return false;
    }
    // save the nodeid and port number
    config.put(NODE_ID, args[0]);
    config.put(PORT_ID, args[1]);
   
    config.put(SmtProperty.SMT_SUBCOMMAND.getName(), cName);
    return true;
    }
    return false;
  }
  
  private boolean saveCommandArgs(String cName, Map<String, String> config, CommandLine line)
  {
    if(line.hasOption(cName))
    {
      config.put(SmtProperty.SMT_SUBCOMMAND.getName(), cName);
      config.put(ARG_ID, "");


    // there may be command arguments, get them all, and concatenate them
    String[] args = line.getOptionValues(cName);
    if((args == null || args.length < 0))
    {
      // log an error, and exit
      System.err.println("argument error");
      return false;
    }
    // save all the arguments in a single parameter
    StringBuffer cmdArgs = new StringBuffer();
    for(String arg: args)
    {
      cmdArgs.append(arg + " ");
    }
    config.put(ARG_ID, cmdArgs.toString().trim());
    config.put(cName, cmdArgs.toString().trim());
    return true;
    }
    return false;
  }
  
  private boolean saveSubCommand(String cName, Map<String, String> config, CommandLine line)
  {
    if(line.hasOption(cName))
    {
      config.put(SmtProperty.SMT_SUBCOMMAND.getName(), cName);
      return true;
    }
    return false;
  }
  
  private String getCommandArgs(Map<String, String> map)
  {
    String cmdArgs = map.get(ARG_ID);
    
    // don't allow multiple commands
    int ndex = cmdArgs.indexOf(';');
    if(ndex > -1)
      return cmdArgs.substring(0, ndex).trim();
    return cmdArgs.trim();
  }
  
  private String getPortIdentification(Map<String, String> map)
  {
    String nodeid = map.get(NODE_ID);
    String portid = map.get(PORT_ID);
    IB_Guid g = null;
    int nodeLid = 0;
    int portN = 0;
    // arg will be a guid, string, or long
    logger.severe("Attempting to query a node: " + nodeid);
    // if this starts with an 0x, 0X, then assume it is hex
    // if this is a very long number, assume it is a long
    // if this is a short number, assume it is a lid
    if(nodeid != null)
    {
      if(nodeid.length() < 10)
      {
        // must be a lid
        nodeLid = IB_Address.toLidValue(nodeid);
      }
      else
      {
        // an IB_Guid or a long
        g = new IB_Guid(nodeid);
      }
      if(g != null)
        logger.severe("The guid is: " + g.toColonString());
      else
        logger.severe("The lid is: " + nodeLid);

    }
    else
    {
      // shouldn't be here, print message and bail
      System.err.println("Some form of node identification is required for this command");
      System.exit(0);
    }
    if(portid != null)
    {
      portN = Integer.parseInt(portid);
    }
    else
    {
      // shouldn't be here, print message and bail
      System.err.println("A port number is required for this command");
      System.exit(0);
    }
    StringBuffer cmdArgs = new StringBuffer();
    if(g != null)
      cmdArgs.append("-G 0x" + g.toString());
    else
      cmdArgs.append(nodeLid);
    
    cmdArgs.append(" " + portN);
    return cmdArgs.toString();
  }
  
  private CommandLineResults ibportstate(String args) throws Exception
  {
    OsmAdminApi adminInterface = ParentSession.getAdminApi();
    return adminInterface.invokePrivilegedCommand(new CommandLineArguments("ibportstate " + args));
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
    System.exit((new SmtPrivileged().execute(args)) ? 0: -1);
  }
}
