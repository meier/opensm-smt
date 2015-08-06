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
 *        file: SmtCommand.java
 *
 *  Created on: Jan 15, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command;

import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.core.IB_Link;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Collection;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_List;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_NConnectionBasedService;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_UpdateProvider;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_UpdateProvider.UpdaterType;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Updater;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Configuration;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_FabricDelta;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Node;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_ServiceChangeListener;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.infiniband.opensm.plugin.gui.data.OMS_PlayableFileBasedService;
import gov.llnl.lc.infiniband.opensm.plugin.net.OsmServerStatus;
import gov.llnl.lc.infiniband.opensm.plugin.net.OsmServiceManager;
import gov.llnl.lc.infiniband.opensm.plugin.net.OsmSession;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.net.ObjectSession;
import gov.llnl.lc.smt.SmtConstants;
import gov.llnl.lc.smt.command.config.SmtConfig;
import gov.llnl.lc.smt.command.top.SmtTop;
import gov.llnl.lc.smt.data.SMT_UpdateService;
import gov.llnl.lc.smt.event.SmtMessage;
import gov.llnl.lc.smt.event.SmtMessageType;
import gov.llnl.lc.smt.filter.SmtFilter;
import gov.llnl.lc.smt.manager.MessageManager;
import gov.llnl.lc.smt.manager.SMT_SearchManager;
import gov.llnl.lc.smt.props.SmtProperties;
import gov.llnl.lc.smt.props.SmtProperty;
import gov.llnl.lc.smt.swing.SmtSplashFrame;
import gov.llnl.lc.system.Console;
import gov.llnl.lc.time.TimeStamp;

import java.awt.Dimension;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

/**********************************************************************
 * This abstract class should be extended for all SMT Commands. It servers to
 * provide default or common implementation, and to enforce the use of the
 * command interface.
 * <p>
 * 
 * @see #SmtNode
 * 
 * @author meier3
 * 
 * @version Jan 15, 2013 3:37:12 PM
 **********************************************************************/
public abstract class SmtCommand implements SmtCommandInterface, SmtConstants, CommonLogger
{
  protected OMS_Updater          UpdateService = null;

  // almost every command could use/re-use the fabric
  protected OpenSmMonitorService OMService     = null;
  
  // few commands need this, and it may be null, so test it
  protected OSM_Configuration osmConfig = null;

  // you must initialize legal options in the commands constructor);
  protected Options              options       = null;

  // an empty filter, by default
  protected SmtFilter            smtFilter     = new SmtFilter();
  
  // a splash screen only used by slow starting gui commands (see execute)
  protected SmtSplashFrame       Splash = null;

  /************************************************************
   * Method Name:
   *  getSplash
   **/
  /**
   * Returns the value of splash
   *
   * @return the splash
   *
   ***********************************************************/
  
  public SmtSplashFrame getSplash()
  {
    return Splash;
  }

  // get the default properties, then override if necessary
  protected SmtProperties        nProps        = null;
  protected SmtConfig            smtConfig     = null;

  protected String               USAGE         = "[-h=<host url>] [-p=<port num>] [-v] [-?]";
  protected String               HEADER        = "smt-command - Invoke a genereric smt command";
  protected String               EXAMPLE       = SmtConstants.NEW_LINE;
  protected String               FOOTER        = SmtConstants.COPYRIGHT;

  // true means the data is based on an active connection (online) to an
  // OMS(ervice)
  protected boolean              online        = true;
  
  // this typically does not exist, but if it does, can be used for optimization purposes (check for null)  
  protected OMS_Collection history = null;


  /************************************************************
   * Method Name: getOptions
   **/
  /**
   * Describe the method here
   * 
   * @see gov.llnl.lc.smt.command.SmtCommandInterface#getOptions()
   * 
   * @return
   ***********************************************************/

  @Override
  public Options getOptions()
  {
    return options;
  }

  /************************************************************
   * Method Name: initMinimumOptions
   **/
  /**
   * All the options that are common to all SMT commands. This method should be
   * included within every init() method, near the top.
   * 
   * @see describe related java objects
   * 
   * @return
   ***********************************************************/
  protected boolean initMinimumOptions()
  {
    // create and initialize the common options for most commands
    if (options == null)
      options = new Options();

    SmtProperty sp = SmtProperty.SMT_HELP;
    Option help = new Option(sp.getShortName(), sp.getName(), false, sp.getDescription());

    sp = SmtProperty.SMT_VERSION;
    Option version = new Option(sp.getShortName(), sp.getName(), false, sp.getDescription());

    sp = SmtProperty.SMT_READ_CONFIG;
    Option cFile = OptionBuilder.hasArg(true).hasArgs(1).withArgName(sp.getArgName())
        .withValueSeparator('=').withDescription(sp.getDescription()).withLongOpt(sp.getName())
        .create(sp.getShortName());

    sp = SmtProperty.SMT_LOG_FILE;
    Option log_file = OptionBuilder.hasArg(true).hasArgs(1).withArgName(sp.getArgName())
        .withValueSeparator('=').withDescription(sp.getDescription()).withLongOpt(sp.getName())
        .create(sp.getShortName());
    sp = SmtProperty.SMT_LOG_LEVEL;
    Option log_level = OptionBuilder.hasArg(true).hasArgs(1).withArgName(sp.getArgName())
        .withValueSeparator('=').withDescription(sp.getDescription()).withLongOpt(sp.getName())
        .create(sp.getShortName());

    options.addOption(cFile);
    options.addOption(log_file);
    options.addOption(log_level);
    options.addOption(help);
    options.addOption(version);

    return true;
  }

  /************************************************************
   * Method Name: initCommonOptions
   **/
  /**
   * All the options that are common to all SMT commands. This method should be
   * included within every init() method, near the top.
   * 
   * @see describe related java objects
   * 
   * @return
   ***********************************************************/
  protected boolean initCommonOptions()
  {
    // create and initialize the common options for most commands

    initMinimumOptions();
    initConnectionOptions();
//    initReadFileOptions();
    initReadHistoryFileOptions();

    return true;
  }

  /************************************************************
   * Method Name: initReadFileOptions
   **/
  /**
   * Set up the options for reading files that contain "single" instances of the
   * fabric. This is the most common type of reading.
   * 
   * @see describe related java objects
   * 
   * @return
   ***********************************************************/
  protected boolean initReadFileOptions()
  {
    /* support reading of both OMS and Fabric files */
    SmtProperty sp = SmtProperty.SMT_READ_OMS;
    Option roFile = OptionBuilder.hasArg(true).hasArgs(1).withArgName(sp.getArgName())
        .withValueSeparator('=').withDescription(sp.getDescription()).withLongOpt(sp.getName())
        .create(sp.getShortName());
//    sp = SmtProperty.SMT_READ_FABRIC;
//    Option rfFile = OptionBuilder.hasArg(true).hasArgs(1).withArgName(sp.getArgName())
//        .withValueSeparator('=').withDescription(sp.getDescription()).withLongOpt(sp.getName())
//        .create(sp.getShortName());

    options.addOption(roFile);
//    options.addOption(rfFile);

    return true;
  }

  /************************************************************
   * Method Name: initMulitReadFileOptions
   **/
  /**
   * Set up the options for reading files that contain "multiple" instances of
   * the fabric. Tools that read these types of files are less common.
   * 
   * @see describe related java objects
   * 
   * @return
   ***********************************************************/
  protected boolean initMulitReadFileOptions()
  {
    /* support reading of both OMS and Fabric files */
    SmtProperty sp = SmtProperty.SMT_READ_DELTA;
    Option rdFile = OptionBuilder.hasArg(true).hasArgs(1).withArgName(sp.getArgName())
        .withValueSeparator('=').withDescription(sp.getDescription()).withLongOpt(sp.getName())
        .create(sp.getShortName());
    sp = SmtProperty.SMT_READ_DELTA_HISTORY;
    Option rdhFile = OptionBuilder.hasArg(true).hasArgs(1).withArgName(sp.getArgName())
        .withValueSeparator('=').withDescription(sp.getDescription()).withLongOpt(sp.getName())
        .create(sp.getShortName());
    sp = SmtProperty.SMT_READ_OMS_HISTORY;
    Option roFile = OptionBuilder.hasArg(true).hasArgs(1).withArgName(sp.getArgName())
        .withValueSeparator('=').withDescription(sp.getDescription()).withLongOpt(sp.getName())
        .create(sp.getShortName());

//    options.addOption(rdFile);
//    options.addOption(rdhFile);
    options.addOption(roFile);

    return true;
  }

  /************************************************************
   * Method Name: initReadHistoryFileOptions
   **/
  /**
   * Set up the options for reading files that contain "multiple" instances of
   * the fabric. Tools that read these types of files are MOST common.
   * 
   * @see describe related java objects
   * 
   * @return
   ***********************************************************/
  protected boolean initReadHistoryFileOptions()
  {
    /* support reading of History files files */
    SmtProperty sp = SmtProperty.SMT_READ_OMS_HISTORY;
    Option roFile = OptionBuilder.hasArg(true).hasArgs(1).withArgName(sp.getArgName())
        .withValueSeparator('=').withDescription(sp.getDescription()).withLongOpt(sp.getName())
        .create(sp.getShortName());

    options.addOption(roFile);

    return true;
  }

  /************************************************************
   * Method Name: initWriteFileOptions
   **/
  /**
   * Set up the options for writing files that contain "single" instances of the
   * fabric. This is the most common type of writing.
   * 
   * @see describe related java objects
   * 
   * @return
   ***********************************************************/
  protected boolean initWriteFileOptions()
  {
    // initialize the command specific options
    SmtProperty sp = SmtProperty.SMT_WRITE_OMS;
    Option woFile = OptionBuilder.hasArg(true).hasArgs(1).withArgName(sp.getArgName())
        .withValueSeparator('=').withDescription(sp.getDescription()).withLongOpt(sp.getName())
        .create(sp.getShortName());

    /* support writing of both OMS and Fabric files */
//    sp = SmtProperty.SMT_WRITE_FABRIC;
//    Option wfFile = OptionBuilder.hasArg(true).hasArgs(1).withArgName(sp.getArgName())
//        .withValueSeparator('=').withDescription(sp.getDescription()).withLongOpt(sp.getName())
//        .create(sp.getShortName());

    options.addOption(woFile);
//    options.addOption(wfFile);

    return true;
  }

  /************************************************************
   * Method Name: initWriteConfigFileOptions
   **/
  /**
   * Set up the options for writing the Fabric Config files, currently
   * this means the ibfabricconf.xml and node-name-map files.
   * 
   * @see describe related java objects
   * 
   * @return
   ***********************************************************/
  protected boolean initWriteConfigFileOptions()
  {
    // initialize the command specific options
    SmtProperty sp = SmtProperty.SMT_WRITE_CONFIG;
    Option wcFile = OptionBuilder.hasArg(true).hasArgs(1).withArgName(sp.getArgName())
        .withValueSeparator('=').withDescription(sp.getDescription()).withLongOpt(sp.getName())
        .create(sp.getShortName());

    options.addOption(wcFile);

    return true;
  }

  /************************************************************
   * Method Name: initMultiWriteFileOptions
   **/
  /**
   * Set up the options for writing files that contain "multiple" instances of
   * the fabric. Tools that write these types of files are less common.
   * 
   * 
   * @see describe related java objects
   * 
   * @return
   ***********************************************************/
  protected boolean initMultiWriteFileOptions()
  {
    // initialize the command specific options
    SmtProperty sp = SmtProperty.SMT_WRITE_DELTA;
    Option wdFile = OptionBuilder.hasArg(true).hasArgs(1).withArgName(sp.getArgName())
        .withValueSeparator('=').withDescription(sp.getDescription()).withLongOpt(sp.getName())
        .create(sp.getShortName());

    /* support writing of both OMS and Fabric files */
    sp = SmtProperty.SMT_WRITE_DELTA_HISTORY;
    Option wdhFile = OptionBuilder.hasArg(true).hasArgs(1).withArgName(sp.getArgName())
        .withValueSeparator('=').withDescription(sp.getDescription()).withLongOpt(sp.getName())
        .create(sp.getShortName());

    /* support writing of both OMS and Fabric files */
    sp = SmtProperty.SMT_WRITE_OMS_HISTORY;
    Option woFile = OptionBuilder.hasArg(true).hasArgs(1).withArgName(sp.getArgName())
        .withValueSeparator('=').withDescription(sp.getDescription()).withLongOpt(sp.getName())
        .create(sp.getShortName());

    // the recording attributes (only applies when in the History writing mode)
    sp = SmtProperty.SMT_HISTORY_RECORDS;
    Option nRecords = OptionBuilder.hasArg(true).hasArgs(1).withArgName(sp.getArgName())
        .withValueSeparator('=').withDescription(sp.getDescription()).withLongOpt(sp.getName())
        .create(sp.getShortName());

    sp = SmtProperty.SMT_HISTORY_MINUTES;
    Option nMinutes = OptionBuilder.hasArg(true).hasArgs(1).withArgName(sp.getArgName())
        .withValueSeparator('=').withDescription(sp.getDescription()).withLongOpt(sp.getName())
        .create(sp.getShortName());

    sp = SmtProperty.SMT_HISTORY_HOURS;
    Option nHours = OptionBuilder.hasArg(true).hasArgs(1).withArgName(sp.getArgName())
        .withValueSeparator('=').withDescription(sp.getDescription()).withLongOpt(sp.getName())
        .create(sp.getShortName());

//    options.addOption(woFile);
//    options.addOption(wdFile);
    options.addOption(wdhFile);

    options.addOption(nRecords);
    options.addOption(nMinutes);
    options.addOption(nHours);

    return true;
  }

  /************************************************************
   * Method Name: initWriteHistoryFileOptions
   **/
  /**
   * Set up the options for writing files that contain "multiple" instances of
   * the OMS. Tools that write these types of files are MOST common.
   * 
   * 
   * @see describe related java objects
   * 
   * @return
   ***********************************************************/
  protected boolean initWriteHistoryFileOptions()
  {
    // initialize the command specific options

    SmtProperty sp = SmtProperty.SMT_WRITE_OMS_HISTORY;
    Option woFile = OptionBuilder.hasArg(true).hasArgs(1).withArgName(sp.getArgName())
        .withValueSeparator('=').withDescription(sp.getDescription()).withLongOpt(sp.getName())
        .create(sp.getShortName());

    // the recording attributes (only applies when in the History writing mode)
    sp = SmtProperty.SMT_HISTORY_RECORDS;
    Option nRecords = OptionBuilder.hasArg(true).hasArgs(1).withArgName(sp.getArgName())
        .withValueSeparator('=').withDescription(sp.getDescription()).withLongOpt(sp.getName())
        .create(sp.getShortName());

    sp = SmtProperty.SMT_HISTORY_MINUTES;
    Option nMinutes = OptionBuilder.hasArg(true).hasArgs(1).withArgName(sp.getArgName())
        .withValueSeparator('=').withDescription(sp.getDescription()).withLongOpt(sp.getName())
        .create(sp.getShortName());

    sp = SmtProperty.SMT_HISTORY_HOURS;
    Option nHours = OptionBuilder.hasArg(true).hasArgs(1).withArgName(sp.getArgName())
        .withValueSeparator('=').withDescription(sp.getDescription()).withLongOpt(sp.getName())
        .create(sp.getShortName());

    options.addOption(woFile);

    options.addOption(nRecords);
    options.addOption(nMinutes);
    options.addOption(nHours);

    return true;
  }

  /************************************************************
   * Method Name: initCommonOptions
   **/
  /**
   * All the options that are common to all SMT commands. This method should be
   * included within every init() method, near the top.
   * 
   * @see describe related java objects
   * 
   * @return
   ***********************************************************/
  protected boolean initConnectionOptions()
  {
    // create and initialize the common options for most commands
    initMinimumOptions();

    SmtProperty sp = SmtProperty.SMT_HOST;
    Option host_name = OptionBuilder.hasArg(true).hasArgs(1).withArgName(sp.getArgName())
        .withValueSeparator('=').withDescription(sp.getDescription()).withLongOpt(sp.getName())
        .create(sp.getShortName());
    sp = SmtProperty.SMT_PORT;
    Option port_num = OptionBuilder.hasArg(true).hasArgs(1).withArgName(sp.getArgName())
        .withValueSeparator('=').withDescription(sp.getDescription()).withLongOpt(sp.getName())
        .create(sp.getShortName());
    sp = SmtProperty.SMT_REUSE;
    Option re_use = OptionBuilder.hasArg(true).hasArgs(1).withArgName(sp.getArgName())
        .withValueSeparator('=').withDescription(sp.getDescription()).withLongOpt(sp.getName())
        .create(sp.getShortName());

    options.addOption(host_name);
    options.addOption(port_num);
    options.addOption(re_use);

    return true;
  }

  /************************************************************
   * Method Name: initPlayableOptions
   **/
  /**
   * All the options necessary for playback control (the time
   * slider)
   * 
   * @see describe related java objects
   * 
   * @return
   ***********************************************************/
  protected boolean initPlayableOptions()
  {
    // create and initialize the common options for playback
    SmtProperty sp = SmtProperty.SMT_UPDATE_PERIOD;
    Option uPeriod  = OptionBuilder.hasArg(true).hasArgs(1).withArgName( sp.getArgName() ).withValueSeparator('=').withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );

    sp = SmtProperty.SMT_UPDATE_MULTIPLIER;
    Option multi  = OptionBuilder.hasArg(true).hasArgs(1).withArgName( sp.getArgName() ).withValueSeparator('=').withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );

    sp = SmtProperty.SMT_WRAP_DATA;
    Option wrap  = OptionBuilder.hasArg(true).hasArgs(1).withArgName( sp.getArgName() ).withValueSeparator('=').withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );

    sp = SmtProperty.SMT_SINGLE_SHOT;
    Option once = new Option( sp.getShortName(), sp.getName(), false, sp.getDescription() );    

    sp = SmtProperty.SMT_PLAY_CONTROL;
    Option play = new Option( sp.getShortName(), sp.getName(), false, sp.getDescription() );    

    options.addOption( uPeriod );
    options.addOption( multi );
    options.addOption( wrap );
    options.addOption( once );
    options.addOption( play );

    return true;
  }

  /************************************************************
   * Method Name: printUsage
   **/
  /**
   * Override this method, if the overall SMT version is not desired for a
   * specific command.
   * 
   * @see describe related java objects
   * 
   * @param options
   ***********************************************************/
  public static void printVersion()
  {
    SubnetMonitorTool.printVersion();
  }

  /************************************************************
   * Method Name: printUsage
   **/
  /**
   * Describe the method here
   * 
   * @see describe related java objects
   * 
   * @param options
   ***********************************************************/
  public void printUsage()
  {
    if (options == null)
    {
      logger.severe("The options object was null, initialize the command!");
      System.err.println("No help or options for " + this.getClass().getName());
      return;
    }
    Dimension d = Console.getScreenDimension();
    int width = d.width < 32 ? 32: d.width -2;
    HelpFormatter helpFormatter = new HelpFormatter();
    helpFormatter.setWidth(width);
    
    // override this method, or just the static strings in your command
    helpFormatter.printHelp(USAGE, SmtConstants.NEW_LINE + HEADER + SmtConstants.NEW_LINE + ".", options, SmtConstants.NEW_LINE + EXAMPLE + SmtConstants.NEW_LINE + FOOTER);
  }

  @Override
  public void osmServiceUpdate(OMS_Updater updater, OpenSmMonitorService osmService)
      throws Exception
  {
    // a new service is available
    OMService = osmService;
  }

  @Override
  public void osmFabricUpdate(OSM_Fabric osmFabric) throws Exception
  {
    // TODO Auto-generated method stub

  }

  /************************************************************
   * Method Name: execute
   **/
  /**
   * The main entry point into the command. It is broken into 3 phases. 1.
   * init() (in the Objects Constructor) 1a. this execute() (from main()) 1b.
   * configure command line options (using an SmtConfig obj) 1b*.
   * parseCommands() - command specific (invoked within SmtCofnig) 1c. get an
   * OMS 2. doCommand() the specific command 3. destroy (release resources,
   * etc.)
   * 
   * @see describe related java objects
   * 
   * @param cmdLineArgs
   * @throws Exception
   ***********************************************************/
  public boolean execute(String[] cmdLineArgs) throws Exception
  {
    boolean success = true;

    // read in properties and preferences
    smtConfig = new SmtConfig();

    // read in command line options, which may override preferences
    // an option may be a config file, use it first, override with
    // remaining command line options
    
    if (smtConfig.parseCommandLineOptions(cmdLineArgs, this))
    {
      // get the OMS and the fabric, unless this is an Off-Line command
      Map<String, String> map = smtConfig.getConfigMap();
      String cmdName = map.get(SmtProperty.SMT_COMMAND.getName());
      
      // Special Case for gui commands that take a long time to start
      if(SmtProperty.SMT_GUI_COMMAND.getPropertyName().equals(cmdName))
      {
        // put up the splash panel (and have it live until the main window is ready - listen for ready event)
        Splash = new SmtSplashFrame(SmtCommandType.SMT_GUI_CMD.getToolName(), SubnetMonitorTool.getVersion());
//        Splash = new SmtSplashFrame(SmtProperty.SMT_GUI_COMMAND.getName(), SubnetMonitorTool.getVersion());
        if(!skipMessages())
          MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "Initial Splash Message"));
      }

      // some commands shouldn't attempt to connect to the service, but most do
      if (!SmtProperty.isSkipOMSCommand(map.get(SmtProperty.SMT_COMMAND.getName())) && online)
      {
        if(!skipMessages())
          MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "Getting an OMS for command: " + map.get(SmtProperty.SMT_COMMAND.getName())));
       logger.info("Getting an OMS for command: " + map.get(SmtProperty.SMT_COMMAND.getName()));
        // attempt to get an initial instance from a connection or a file
        OMService = this.getOpenSmMonitorService();
      }
      else
      {
        logger.info("An smt command that does not require an initial OMS was detected: " + map.get(SmtProperty.SMT_COMMAND.getName()));
      }

      // if asking for version info, provide it and stop
      if (SmtProperty.SMT_VERSION.getName().equalsIgnoreCase(map.get(SmtProperty.SMT_COMMAND.getName())))
      {
        // this will invoke the abstract CLASS method printVersion(). If you
        // want to have a different
        // version for your specific command, you must over-ride this method
        printVersion();
      }
      else
      {
        if(!skipMessages())
          MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "Starting command specific work"));
        doCommand(smtConfig);
      }
    }
    else
    {
      // if here, nothing valid on the command line, assume needs help
      logger.severe("returned false");
      printUsage();
    }

    // clean up - probably a NOP
    destroy();
    return success;
  }

  abstract public boolean doCommand(SmtConfig config) throws Exception;

  /************************************************************
   * Method Name: init
   **/
  /**
   * Initializes the resources for the Command, primarily the Option object.
   * 
   * @see gov.llnl.lc.smt.command.SmtCommand#init()
   * 
   * @return true
   ***********************************************************/
  abstract public boolean init();

  /************************************************************
   * Method Name: parseCommands
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
  abstract public boolean parseCommands(Map<String, String> config, CommandLine line);

  /************************************************************
   * Method Name: destroy
   **/
  /**
   * Releases the resources for the Command, this may do nothing at all, if the
   * doCommand() cleans up after itself. It is here for symmetry.
   * 
   * @see gov.llnl.lc.smt.command.SmtCommand#init()
   * 
   * @return true
   ***********************************************************/
  public boolean destroy()
  {
    return true;
  }

  /************************************************************
   * Method Name: openSession
   **/
  /**
   * Describe the method here
   * 
   * @throws Exception
   * 
   * @see describe related java objects
   * 
   ***********************************************************/
  protected OsmSession openSession()
  {
    // establish a connection
    OsmSession ParentSession = null;
    Map<String, String> map = smtConfig.getConfigMap();
    String hostNam = map.get(SmtProperty.SMT_HOST.getName());
    String portNum = map.get(SmtProperty.SMT_PORT.getName());

    /* the one and only OsmServiceManager */
    OsmServiceManager OsmService = OsmServiceManager.getInstance();

    try
    {
      ParentSession = OsmService.openSession(hostNam, portNum, null, null);
    }
    catch (Exception e)
    {
      logger.severe(e.getStackTrace().toString());
      System.exit(-1);
    }

    return ParentSession;
  }
  
  private boolean skipMessages()
  {
    // anyone listening to messages? if not skip
    return MessageManager.getInstance().getNumListeners() < 1;
  }

  protected void closeSession(OsmSession ParentSession)
  {
    OsmServiceManager OsmService = OsmServiceManager.getInstance();
    try
    {
      OsmService.closeSession(ParentSession);
    }
    catch (Exception e)
    {
      logger.severe(e.getStackTrace().toString());
    }
  }
  
  public void closeSplash()
  {
    logger.severe("\n*** Shutting down the SmtSplashFrame, and bringing up the normal SmtGui.\n");
    if(Splash != null)
    {
      // gracefully shut the splash screen down
      
      // this normally means
      //   flush the messages in the manager (intended for splash screen)
      //   removed the splash panel from the listener list
      //   post a final message
      //   make it invisible, and remove
//      MessageManager.getInstance().flushMessages();
      if(!skipMessages())
        MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INFO, "Ready"));
          
      Splash.setVisible(false);
      Splash.dispose();
    }
  }  


  public static String convertSpecialFileName(String fname)
  {
    if (fname == null)
      return null;

    String fileName = fname;

    if (fname.startsWith("%h"))
      fileName = System.getProperty("user.home") + fname.substring(2);
    if (fname.startsWith("%t"))
      fileName = System.getProperty("java.io.tmpdir") + fname.substring(2);
    
    // finally, if the file name is just the file name, assume path is current directory
    if(Character.isLetterOrDigit(fname.charAt(0)))
      fileName = System.getProperty("user.dir") + FILE_SEPARATOR + fname;
    return fileName;
  }

  protected IB_Guid getNodeGuid(String nodeStr)
  {
    return getNodeGuid(nodeStr, false);
  }

  protected IB_Guid getNodeGuid(String nodeStr, boolean portGuid)
  {
    return SMT_SearchManager.getNodeGuid(nodeStr, portGuid, OMService);
  }

  protected IB_Guid getPortsNodeGuid(String nodeStr)
  {
    return SMT_SearchManager.getNodeGuid(nodeStr, true, OMService);
  }

  protected HashMap<String, OSM_Node> getOSM_Nodes()
  {
    OSM_Fabric Fabric = OMService.getFabric();
    return ((Fabric == null) || (!Fabric.isInitialized())) ? null : Fabric.getOSM_Nodes();
  }

  protected OMS_Updater initServiceUpdater(SmtConfig config) throws Exception
  {
    // support online or offline (connected or file based)
    // support single shot or continuous
    // support playable offline (gui), or just free run
    //
    // online & single (reqr: two snapshots to form a Delta)
    // online & continuous (ctrl-c to stop)
    // offline & single (reqr: OMS History, OSM_DeltaFabric)
    // offline & continuous (reqr: OMS History file)
    //
    //
    // this is the top command, and ultimately we want to be able
    // to operate off an instance of the the service (OFFLINE) or
    // dynamically with changing data

    // ALSO: a mode where a connection is established and used or
    // reused until done. As opposed to a new connection each time
    // new data is desired or refreshed. Both have pros/cons, so
    // provide both.

    Map<String, String> map = config.getConfigMap();
    String hostNam = map.get(SmtProperty.SMT_HOST.getName());
    String portNum = map.get(SmtProperty.SMT_PORT.getName());
    String re_use = map.get(SmtProperty.SMT_REUSE.getName());
    String sWrap = map.get(SmtProperty.SMT_WRAP_DATA.getName());
    String sMult = map.get(SmtProperty.SMT_UPDATE_MULTIPLIER.getName());
    String sNumTop = map.get(SmtProperty.SMT_TOP_NUMBER.getName());
    String sTopType = map.get(SmtProperty.SMT_SUBCOMMAND.getName());
    String sOnce = map.get(SmtProperty.SMT_SINGLE_SHOT.getName());
    String sPlay = map.get(SmtProperty.SMT_PLAY_CONTROL.getName());

// the defaults, TODO make these persistent or at least define in a common place
    if (re_use == null)
      re_use = "false";
    if (sWrap == null)
      sWrap = "true";
    if (sMult == null)
      sMult = "20";
    if (sOnce == null)
      sOnce = "false";

    if (sPlay == null)
      sPlay = "false";

    boolean reuse = re_use.toLowerCase().startsWith("t") || re_use.toLowerCase().startsWith("y");
    boolean wrap = sWrap.toLowerCase().startsWith("t") || sWrap.toLowerCase().startsWith("y");
    boolean once = sOnce.toLowerCase().startsWith("t") || sOnce.toLowerCase().startsWith("y");
    boolean playable = sPlay.toLowerCase().startsWith("t") || sOnce.toLowerCase().startsWith("y");

    String file = convertSpecialFileName(map.get(SmtProperty.SMT_READ_OMS_HISTORY.getName()));
    if (file != null)
      online = false;
    else
      file = SmtConstants.SMT_NO_FILE;

    UpdaterType Utype = UpdaterType.CONNECTION_BASED_UPDATER; // the default

    if (!isOnLine())
      // using a file based service updater, which kind?
      Utype = playable ? UpdaterType.PLAYABLE_FILE_BASED_UPDATER : UpdaterType.FILE_BASED_UPDATER;

    if (usingSMT_Updater())
    {
      // using an SMT_Updater, currently only one kind, which is a hybrid (file, connection/file)
      Utype = UpdaterType.SMT_UPDATER;
    }

    if(!skipMessages())
      MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "getting the desired service updater from the OMS_UpdateProvider"));
    OMS_UpdateProvider up = OMS_UpdateProvider.getInstance();
    UpdateService = up.getUpdater(Utype);

    if (UpdateService instanceof OMS_PlayableFileBasedService)
    {
      ((OMS_PlayableFileBasedService) UpdateService).setDetachedFrame(true);
    }

    Properties serviceProps = UpdateService.getProperties();
    if (serviceProps == null)
      serviceProps = new Properties();

    // setup the updater service properties
    serviceProps.setProperty(SmtProperty.SMT_PORT.name(), portNum);
    serviceProps.setProperty(SmtProperty.SMT_HOST.name(), hostNam);
    serviceProps.setProperty(SmtProperty.SMT_UPDATE_PERIOD.name(), "30");
    serviceProps.setProperty(SmtProperty.SMT_REUSE.name(), Boolean.toString(reuse));
    serviceProps.setProperty(SmtProperty.SMT_UPDATE_MULTIPLIER.name(), sMult);
    serviceProps.setProperty(SmtProperty.SMT_WRAP_DATA.name(), Boolean.toString(wrap));
    serviceProps.setProperty(SmtProperty.SMT_OMS_FILE.name(), file);
    serviceProps.setProperty(SmtProperty.SMT_COMMAND.name(), this.getClass().getName());
    
    if ((UpdateService instanceof SMT_UpdateService) && (history != null))
    {
      // using an SMT_Updater, AND I have already read the file, so just set the collection
      SMT_UpdateService sus = (SMT_UpdateService)UpdateService;
      sus.setCollection(history);
    }

    if ((UpdateService instanceof OMS_NConnectionBasedService) && (history != null))
    {
      // using an Connection_Updater, AND I have already read the OMS_List so just update the service
      OMS_NConnectionBasedService cus = (OMS_NConnectionBasedService)UpdateService;
      cus.setOMS_List(new OMS_List(history));
    }

    if(!skipMessages())
     MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "initializing the " + UpdateService.getName() + " (via init())"));


    UpdateService.addListener(this);
    // optimization, if this is TOP, add its Analyser
    if(this instanceof SmtTop)
    {
      // TOP starts another thread for analysis, and it should get updates too
      SmtTop top = (SmtTop)this;
      OSM_ServiceChangeListener analyzer = top.getUpdateListener();
      UpdateService.addListener(analyzer);
    }
    
    // this will initialize it and start it, and get the first OMS instance.  Make sure
    // listeners are registered, or they will miss the first one and have to wait for the
    // next one
    UpdateService.init(serviceProps);
    return UpdateService;
  }

  protected OpenSmMonitorService getOpenSmMonitorService()
  {
    // TODO: Convert to use the service provider (refer to top or console)

    OpenSmMonitorService OMS = null;
    Map<String, String> map = smtConfig.getConfigMap();
    // get the an instance from an on-line connection or from a file
    // this needs to have been previously saved or -W -of filename
    //

    // 1. read the OMS from cache, if it exists
    // 1a. if it exists, check to see if its stale
    // 2. if directed to read from OMS cache via command line
    // do it, override any previous stuff, and done
    // 3. otherwise, if cache is not stale, done, but if stale
    // connect to the service and get a fresh copy
    //

    String oread = map.get(SmtProperty.SMT_READ_OMS.getName());
    String hread = map.get(SmtProperty.SMT_READ_OMS_HISTORY.getName());
    String fread = map.get(SmtProperty.SMT_READ_FABRIC.getName());
    String hostNam = map.get(SmtProperty.SMT_HOST.getName());
    String portNum = map.get(SmtProperty.SMT_PORT.getName());
    
    if(!skipMessages())
      MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "*** Inside getOpenSmMonitorService() to get a snapshot"));
    if ((oread != null) || (hread != null))
    {
      if (hread != null)
      {
        // grab a single snapshot out of this history file
        // if a timestamp was not specified, use the earliest one
        if(!skipMessages())
          MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "grabbing a single OMS snapshot out of the history file"));
        String fName = convertSpecialFileName(hread);

        String ts = map.get(SmtProperty.SMT_TIMESTAMP.getName());
        try
        {
          if(!skipMessages())
            MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "Reading OMS history file (" + fName + ")"));
          history = OMS_Collection.readOMS_Collection(fName);
          // use the current (most recent) by default
          OMS = history.getCurrentOMS();
          OMS_List lst = new OMS_List(history);
          String cts = OMS.getTimeStamp().toString();

          if(!skipMessages())
            MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "Done reading history file"));

          // if I can make a timestamp AND find it in the history, then use it
          if ((ts != null) && (!ts.equalsIgnoreCase(SmtProperty.SMT_USE_DEFAULT.getName())))
          {
            String name = OMS.getFabricName();
            TimeStamp tStamp = new TimeStamp(ts);
            logger.info("Looking for a specific instance of OMS");
            OpenSmMonitorService osmms = history.getOMS(name, tStamp);
            if (osmms != null)
            {
              OMS = osmms;
              logger.info("Found an OMS with timestamp (" + OMS.getTimeStamp().toString() + ")");
            }
          }
          online = false;         
          
          String msg = "Using an OMS from the OMS History file [" + fName + "] with timestamp (" + cts + "), size {" + history.getSize() +"}";
          logger.severe(msg);
          if(!skipMessages())
            MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, msg));
        }
        catch (Exception e)
        {
          logger.severe("Couldn't read the History file");
        }
      }
      else
      {
        if(!skipMessages())
          MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "grabbing a single OMS snapshot out of the OMS file"));

        try
        {
          // the file name may have special characters, so interpret them
          String fName = convertSpecialFileName(oread);
          logger.info("Getting a new OMS instance via the file: " + fName);
          OMS = OpenSmMonitorService.readOMS(fName);
          online = false;
        }
        catch (Exception e)
        {
          // TODO Auto-generated catch block
          logger.severe("Couldn't read the OMS file");
        }
      }
    }
    else if (fread != null)
    {
      String file = convertSpecialFileName(fread);
      if (file != null)
      {
        OSM_Fabric fabric = null;
        try
        {
          logger.info("Getting a new Fabric instance via the file: " + file);
          fabric = OSM_Fabric.readFabric(file);
        }
        catch (Exception e)
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }

        if (fabric != null)
        {
          ObjectSession ParentSession = null;
          OsmServerStatus RemoteStatus = null;

          if (OMService != null)
          {
            ParentSession = OMService.getParentSessionStatus();
            RemoteStatus = OMService.getRemoteServerStatus();
          }
          logger.info("Constructing a new OMS instance from the fabric");

          // have everything I need to construct a new OpenSmMonitorService
          // (which will include the fabric)
          OpenSmMonitorService service = null;
          service = new OpenSmMonitorService(ParentSession, RemoteStatus, fabric);
          if (service != null)
          {
            OMS = service;
            online = false;
          }
          else
          {
            logger.severe("Failed to construct an OMS instance, its null Jim!");
          }
        }
      }
    }
    else
    {
      // always get a new instance
      if(!skipMessages())
        MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "Getting a new OMS instance via the OMS"));
      try
      {
        OMS_List lst = OpenSmMonitorService.getOMS_List(hostNam, portNum);

        OMS = lst.getCurrentOMS();
        if(history == null)
        {
          history = new OMS_Collection();
          history.put(lst.getOldestOMS());
          history.put(OMS);
        }
      }
      catch (IOException e)
      {
        logger.severe("IOException: " + e.getMessage());
      }
      if (OMS != null)
      {
        if ((OMS.getFabric() != null) && (OMS.getFabric().isStale()))
          logger.severe("The Fabric information appears stale, make sure the PerfMgr is running");
      }
    }

    if(!skipMessages())
      MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "*** Done getting a new OMS instance"));
    return OMS;
  }

  protected OSM_Configuration getOsmConfig(boolean newInstance) throws FileNotFoundException, IOException, ClassNotFoundException
  {
    if (newInstance)
    {
      logger.info("Getting a new Configuration instance via the OMS");
      Map<String, String> map = smtConfig.getConfigMap();
      String hostNam = map.get(SmtProperty.SMT_HOST.getName());
      String portNum = map.get(SmtProperty.SMT_PORT.getName());
      OSM_Configuration cfg = null;
      try
      {
        cfg = OSM_Configuration.getOsmConfig(hostNam, portNum);
      }
      catch (IOException e)
      {
        logger.severe("Couldn't obtain OsmConfig via " + hostNam + " and port " + portNum + ". Trying cache file..");
        if(OMService != null)
        {
          OMService.getFabricName();
          String fname = OSM_Configuration.getCacheFileName(OMService.getFabricName());
          logger.info("Trying cache file (" + fname + ")");
          cfg = OSM_Configuration.readConfig(fname);
        }
      }
      if(cfg != null)
        osmConfig = cfg;
      return cfg;
    }
    return osmConfig;
  }

  protected OSM_Fabric getOSM_Fabric(boolean newInstance)
  {
    if (newInstance)
    {
      logger.info("Getting a new Fabric instance via the OMS");
      Map<String, String> map = smtConfig.getConfigMap();
      String hostNam = map.get(SmtProperty.SMT_HOST.getName());
      String portNum = map.get(SmtProperty.SMT_PORT.getName());
      return OSM_Fabric.getOSM_Fabric(hostNam, portNum);
    }
    return OMService.getFabric();
  }

  protected OSM_FabricDelta getOSM_FabricDelta(boolean newInstance)
  {
    // create a FabricDelta using the history, or getting new fabrics
    // if the history doesn't exits
    
    // how big is the history?
    if(history != null)
      return history.getCurrentOSM_FabricDelta();
    return getOSM_FabricDeltaOld(newInstance);
  }

  protected OSM_FabricDelta getOSM_FabricDeltaOld(boolean newInstance)
  {
    // create a FabricDelta using the existing fabric, and getting another one
    OSM_Fabric Fabric1 = getOSM_Fabric(newInstance);
    int sleepTime = Fabric1.getPerfMgrSweepSecs();

    // wait for the perfmanager to perform another sweep, and produce new data
    // (add a little extra time to make sure)
    try
    {
      TimeUnit.SECONDS.sleep(sleepTime + 5);
    }
    catch (InterruptedException e)
    {
      logger.severe("Simple sleep failed");
    }

    // get the second fabric
    OSM_Fabric Fabric2 = getOSM_Fabric(true);
    return new OSM_FabricDelta(Fabric1, Fabric2);
  }

  protected HashMap<String, IB_Link> getIB_Links()
  {
    OSM_Fabric Fabric = OMService.getFabric();
    return ((Fabric == null) || (!Fabric.isInitialized())) ? null : Fabric.getIB_Links();
  }

  protected HashMap<String, OSM_Port> getOSM_Ports()
  {
    OSM_Fabric Fabric = OMService.getFabric();
    return ((Fabric == null) || (!Fabric.isInitialized())) ? null : Fabric.getOSM_Ports();
  }

  protected OSM_Node getOSM_Node(long guid)
  {
    // OSM_Fabric Fabric = OMService.getFabric();
    return getOSM_Node(OSM_Fabric.getOSM_NodeKey(guid));
  }

  protected OSM_Node getOSM_Node(String guidHashKey)
  {
    // if this starts with an 0x, 0X, then assume it is hex
    // if this is a number, assume it is a long
    // if this is a
    return getOSM_Nodes().get(guidHashKey);
  }

  protected boolean isOnLine()
  {
    return online;
  }

  /************************************************************
   * Method Name:
   *  usingSMT_Updater
  **/
  /**
   * A few SMT Commands should always use the SMT_Updater, instead
   * of the more general forms.  This method checks to see if this
   * command should use the SMT_UpdateService, and returns true if so
   * and false otherwise.
   *
   * @return  true if this command should use the SMT_UpdateService
   ***********************************************************/
  protected boolean usingSMT_Updater()
  {
    boolean smt = false;
    // only return true if this is the SmtGui command
    if (SmtProperty.SMT_GUI_COMMAND.getPropertyName().compareTo(this.getClass().getName()) == 0)
      smt = true;
    return smt;
  }

  /************************************************************
   * Method Name:
   *  getOSM_Port
  **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   * @param guidPortHashKey
   * @return
   ***********************************************************/
  protected OSM_Port getOSM_Port(String guidPortHashKey)
  {
    // TODO Auto-generated method stub
    return getOSM_Ports().get(guidPortHashKey);
  }

  protected OSM_Port getOSM_Port(long guid, short port_num)
  {
    // OSM_Fabric Fabric = OMService.getFabric();
    return getOSM_Port(OSM_Fabric.getOSM_PortKey(guid, port_num));
  }

  protected OSM_Node getOSM_Node(IB_Guid guid, HashMap<String, OSM_Node> nodes)
  {
    // OSM_Fabric Fabric = OMService.getFabric();
    // find the first node that matches the supplied guid
    OSM_Node node = null;
    if ((guid != null) && (nodes != null) && (nodes.size() > 0))
    {
      node = nodes.get(OSM_Fabric.getOSM_NodeKey(guid.getGuid()));
    }
    return node;
  }

  protected OSM_Node getOSM_Node(IB_Guid guid)
  {
    return getOSM_Node(guid.getGuid());
  }

  protected OSM_Node getOSM_NodeByName(String name)
  {
    // find the first node that matches the supplied name
    OSM_Node node = null;
    if (name != null)
    {
      HashMap<String, OSM_Node> nodes = getOSM_Nodes();

      for (OSM_Node n : nodes.values())
      {
        if (n.pfmNode.getNode_name().startsWith(name))
        {
          node = n;
          break;
        }
      }
    }
    return node;
  }

  protected boolean initFilter(Map<String, String> map)
  {
    // check to see if anything needs to be initialized
    if (map == null)
      return false;

    try
    {
      smtFilter = new SmtFilter(map);
    }
    catch (IOException e)
    {
      logger.severe("Could not initialize filter from configuration map");
    }

    return true;
  }

  /************************************************************
   * Method Name: getFilter
   **/
  /**
   * Returns the value of smtFilter
   * 
   * @return the smtFilter
   * 
   ***********************************************************/

  public SmtFilter getFilter()
  {
    return smtFilter;
  }

  protected boolean isFiltered(String test)
  {
    if (smtFilter == null)
      return false;
    return (smtFilter.isFiltered(test));
  }

  /************************************************************
   * Method Name: SmtCommand
   **/
  /**
   * Describe the constructor here
   * 
   * @see describe related java objects
   * 
   ***********************************************************/
  public SmtCommand()
  {
    super();
    init();
  }

}
