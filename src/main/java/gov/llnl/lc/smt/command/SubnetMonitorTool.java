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
 *        file: SubnetMonitorTool.java
 *
 *  Created on: Jan 17, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command;

import gov.llnl.lc.smt.SmtConstants;
import gov.llnl.lc.smt.command.config.SmtConfig;
import gov.llnl.lc.smt.props.SmtProperties;
import gov.llnl.lc.smt.props.SmtProperty;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

/**********************************************************************
 * Describe purpose and responsibility of SubnetMonitorTool
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Jan 17, 2013 8:14:27 AM
 **********************************************************************/
public class SubnetMonitorTool extends SmtCommand
{

  /************************************************************
   * Method Name:
   *  printVersion
   **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   * @param args
   ***********************************************************/
  public static void printVersion()
  {
    System.out.println(getVersion());
  }
  
  public static String getVersion()
  {
    return "Subnet Monitor Tool v" + new SmtProperties().getVersion();
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
    System.exit((new SubnetMonitorTool().execute(args)) ? 0: -1);
  }

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
    boolean status = true;
    
    // set the command and sub-command (always do this)
    config.put(SmtProperty.SMT_COMMAND.getName(), this.getClass().getName());
    
    // everything is considered an argument that is not part of the subcommand
    // the first subcommand wins
    
    saveCommandArgs(line.getArgs(), config);

    for(SmtCommandType s : SmtCommandType.SMT_COMMON_CMDS)
    {
      if(line.hasOption(s.getName()))
      {
        config.put(SmtProperty.SMT_SUBCOMMAND.getName(), s.getName());
        config.put(s.getName(), line.getOptionValue(s.getName()));
        saveCommandArgs(line.getOptionValues(s.getName()), config);
        break;
      }
    }
    return status;
  }
  
  private void saveCommandArgs(String[] args, Map<String, String> config)
  {
    // stash the command line arguments away, because we will use them later
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
  
  private void printCommandArgs(Map<String, String> map)
  {
    if(map != null)
    {
      String args = map.get(SmtProperty.SMT_COMMAND_ARGS.getName());
      if(args != null)
      {
        System.err.println("ARGS (" + args + ")");
       }
    }
  }
  
  private String getHistoryFileName(Map<String, String> map, boolean usePersistedValue)
  {
    // return this filename, only if it exists
    String fn = getPropertyValue(SmtProperty.SMT_READ_OMS_HISTORY, map, usePersistedValue);
    if(fn != null)
    {
      File hf = new File(fn);
      if(hf.exists())
        return fn;      
    }

    return null;
  }
  
  private boolean isCommandHelpWanted(Map<String, String> map)
  {
    // returns true if the commands help should be displayed.  This should happen
    // only if an ? or Help shows up as arguments (without hyphens)
    if (map != null)
    {
      SmtProperty sp = SmtProperty.SMT_HELP;
      String args = map.get(SmtProperty.SMT_COMMAND_ARGS.getName());
      if ((args != null) && (args.contains(sp.getShortName()) || args.contains(sp.getName())))
      {
        String[] argA = args.split(" ");
        for (String a : argA)
        {
          if ((a.equals(sp.getShortName()) || args.equals(sp.getName())))
          {
            System.err.println("Command arguments       : " + args);
            System.err.println("Request for command help: " + a);
            return true;
          }
        }
      }
    }
    return false;
  }
  
  private String[] getCmdArgs(Map<String, String> map, SmtCommandType s)
  {
    // parse or decode the map, with respect to the subcommand, and return
    // a valid argument list (options + arguments) for that subcommand

    // default
    if(isCommandHelpWanted(map))
      return new String [] {"-?"};
    
    // get the connection args
    String[] omsSource = getOmsSourceArgs(map, s);
    
    // get the final resultant arguments
    String[] cmdArgs = getDefaultArgs(omsSource, map, s);
    
    return cmdArgs;
  }
  
  private String[] getOmsSourceArgs(Map<String, String> map, SmtCommandType s)
  {
    // return the method for retrieving an OMS snapshot (may be command specific)
    
    // preferential order
    //
    // use command line arguments if supplied
    // if NOT supplied
    //  use localhost and default port
    //  use persisted history file
    //  use persisted host and port
    //
    // first success wins!
    
    String args[] = new String [4];
    
    // default arguments and values
    args[0] = "--host";
    args[1] = "localhost";
    args[2] = "-pn";
    args[3] = "10011";

    // currently, support only rH filename, h hostnane, pn port#
    String F = getHistoryFileName(map, false);
    String H = getHostName(map, false);
    String P = getPortNumber(map, false);
    
    if(H != null)
    {
      args[1] = H;
    }
    if(P != null)
    {
      args[3] = P;
    }
    
    // F will take precedence over H & P
    if(F != null)
      args = new String [] {"-rH",F};

    return args;
  }
  
  private String[] getDefaultArgs(String[] omsSource, Map<String, String> map, SmtCommandType s)
  {
    // return the "normal" arguments for this type of command
    String[] newArgs = null;
    String args[] = new String [] {""};
    String line = map.get(SmtProperty.SMT_COMMAND_ARGS.getName());
    
    switch (s) 
    {
      case SMT_SERVICE_CMD:
      case SMT_FABRIC_CMD:
      case SMT_NODE_CMD:
      case SMT_PORT_CMD:
      case SMT_LINK_CMD:
      case SMT_EVENT_CMD:
      case SMT_ROUTE_CMD:
      case SMT_PART_CMD:
      case SMT_MCAST_CMD:
      case SMT_CONSOLE_CMD:
      case SMT_GUI_CMD:
        args = omsSource;
        break;
        
      case SMT_TOP_CMD:
      case SMT_UTILIZE_CMD:
        newArgs = new String[omsSource.length + 1];
        newArgs[0] = "-once";
        System.arraycopy(omsSource, 0, newArgs, 1, omsSource.length);
        args = newArgs;
        break;
        
      case SMT_ID_CMD:
        args = omsSource;
        // the id search needs an OMS SOURCE and one string
        // for searching.  Put the string first, followed by
        // OMS SOURCE args
        if((line != null) && (line.length() > 1))
        {
          String lineArgs[] = line.split(" ");
          int ndex = 0;
          for (ndex = 0; ndex < lineArgs.length; ndex++)
          {
            String a = lineArgs[ndex];
            if (a.equals("h") || a.equals("rH") || a.equals("pn"))
            {
              // ignore these, and skip the next argument too
              ndex++;
            }
            else
            {
              newArgs = new String[omsSource.length + 1];
              newArgs[0] = a;
              System.arraycopy(omsSource, 0, newArgs, 1, omsSource.length);
              args = newArgs;
              break;
            }
          }
        }
        break;
        
      case SMT_HELP_CMD:
      case SMT_ABOUT_CMD:
        args = new String [] {"-lf"," %h/.smt/smt-tool%u.log"};
        break;
        
      case SMT_FILE_CMD:
        // use the i option and its argument, by default, if provided
        // if NOT provided, try to use the persisted file if it exists
        if((line != null) && (line.length() > 1) && (line.contains("i")))
        {
          String lineArgs[] = line.split(" ");
          int ndex = 0;
          for (String a : lineArgs)
          {
            if (a.equals("i"))
            {
              // the file name is either before or after the i, which one??
              int fNdex = ndex == 0 ? 1: 0;
              args = new String [] {"-lf"," %h/.smt/smt-file%u.log", "-i",lineArgs[fNdex]};
              return args;
            }
            ndex++;
          }
        }
        
        String F = getHistoryFileName(map, true);
        if(F != null)
          args = new String [] {"-lf"," %h/.smt/smt-file%u.log", "-i",F};
        break;
        
      case SMT_RECORD_CMD:
        args = new String [] {"-lf"," %h/.smt/smt-record%u.log", "-?","record"};
        break;
        
      case SMT_CONFIG_CMD:
        args = new String [] {"-lf"," %h/.smt/smt-config%u.log", "-?","conf"};
        break;
        
      case SMT_PRIV_CMD:
        args = new String [] {"-lf"," %h/.smt/smt-priv%u.log", "-?","priv"};
        break;
        
      default:
        args = new String [] {"-lf"," %h/.smt/smt-tool%u.log"};
        break;
    }
    return args;
  }
  
  private String getHostName(Map<String, String> map, boolean usePersistedValue)
  {
    return getPropertyValue(SmtProperty.SMT_HOST, map, usePersistedValue);
  }
  
  private String getPropertyValue(SmtProperty sp, Map<String, String> map, boolean usePersistedValue)
  {
    if (map != null)
    {
      String args = map.get(SmtProperty.SMT_COMMAND_ARGS.getName());
      if ((args != null) && (args.contains(sp.getShortName()) || args.contains(sp.getName())))
      {
        String[] argA = args.split(" ");
        int ndex = 0;
        for (String a : argA)
        {
          ndex++;
          if ((a.equals(sp.getShortName()) || args.equals(sp.getName())))
          {
             // a match, return the next arg
            if (ndex < argA.length)
              return argA[ndex];
          }
        }
      }
      else
      {
        if(usePersistedValue)
          return map.get(sp.getName());
      }
    }
    return null;
  }
  
  private String getPortNumber(Map<String, String> map, boolean usePersistedValue)
  {
    return getPropertyValue(SmtProperty.SMT_PORT, map, usePersistedValue);
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
    boolean success = false;
    
    Map<String,String> map = smtConfig.getConfigMap();
    
    String subCommand = map.get(SmtProperty.SMT_SUBCOMMAND.getName());
    
    if (subCommand != null)
    {
      SmtCommandType s = SmtCommandType.getByName(subCommand);
      
      if(s == null)
        logger.severe("SubCommand not found by name");
      else
      {
        // attempt to invoke the command
        invokeSmtCommand(s, getCmdArgs(map, s));
        success = true;
      }      
    }
    else
      printUsage();
      
    return success;
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
    USAGE = "smt <command> [-?] [?] [<command args>]";
    HEADER = "\n This command provides access to some of the most commonly" +
             " used SMT commands.  Most commands should be invoked directly" +
             " using the form \"smt-<command>\", but can be invoked here" +
             " for convenience.";
    EXAMPLE ="examples:" + SmtConstants.NEW_LINE +
        "> smt -?                    - provides this help" + SmtConstants.NEW_LINE + 
        "> smt --node ?              - provides help for the node command (no dash for its args)" + SmtConstants.NEW_LINE + 
        "> smt --multicast pn 10013  - multicast status for service on port 10013" + SmtConstants.NEW_LINE  + ".";  // terminate with nl


    // create and initialize the common options for this command
    this.initMinimumOptions();
    
    // initialize the command specific options
     for(SmtCommandType s : SmtCommandType.SMT_COMMON_CMDS)
    {
      Option cCmd = new Option( s.getShortName(), s.getName(), false, s.getCmdDescription() );    
      options.addOption( cCmd );
    }
    return true;
  }

  /************************************************************
   * Method Name:
   *  destroy
  **/
  /**
   * Describe the method here
   *
   * @see gov.llnl.lc.smt.command.SmtCommand#destroy()
   *
   * @return
   ***********************************************************/
  @Override
  public boolean destroy()
  {
    // TODO Auto-generated method stub
    return false;
  }
  
  private boolean invokeSmtCommand(SmtCommandType s, String[] cmdArgs)
  {
    if(s != null)
    {
      String className    = s.getCommandName();
      
      // attempt to invoke this command, using this class
      Class<?> smtClass = null;
       try
      {
         smtClass = Class.forName(className);
        Class[] argTypes = new Class[] { String[].class };
        Method main = smtClass.getDeclaredMethod("main", argTypes);
        
        // don't "invoke" with a null arg
        if(cmdArgs == null)
           cmdArgs = new String[] {""};
        
//        System.out.format("invoking %s.main()%n", smtClass.getName());
//        System.out.println("with arguments " + Arrays.toString(cmdArgs));
        main.invoke(null, (Object)cmdArgs);
       }
      catch (Exception e)
      {
        logger.info("CMD NOT: " + className);
        System.err.println(className + " threw an exception while invoking main");
        System.err.println(e.getMessage());
        e.printStackTrace();
      }
    return true;
  }
    return false;
  }

}
