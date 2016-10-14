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
 *        file: SmtTop.java
 *
 *  Created on: May 31, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command.top;

import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Updater;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_ServiceChangeListener;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.smt.SmtConstants;
import gov.llnl.lc.smt.command.SmtCommand;
import gov.llnl.lc.smt.command.config.SmtConfig;
import gov.llnl.lc.smt.props.SmtProperty;

import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;


/**********************************************************************
 * Describe purpose and responsibility of SmtTop
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version May 31, 2013 2:56:01 PM
 **********************************************************************/
public class SmtTop extends SmtCommand
{
  
  TopAnalyzer TopA = TopAnalyzer.getInstance();

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
    // support single shot or continuous
    //
    //  online & single       (reqr: two snapshots to form a Delta)
    //  online & continuous   (ctrl-c to stop)
    //  offline & single      (reqr: OMS History, OSM_DeltaFabric)
    //  offline & continuous  (reqr: OMS History file)
    //
    //
    // this is the top command, and ultimately we want to be able
    // to operate off an instance of the the service (OFFLINE) or
    // dynamically with changing data
    
    // ALSO:  a mode where a connection is established and used or
    // reused until done.  As opposed to a new connection each time
    // new data is desired or refreshed.  Both have pros/cons, so
    // provide both.
    
    initServiceUpdater(config);
    
    Map<String,String> map = smtConfig.getConfigMap();
    String sNumTop = map.get(SmtProperty.SMT_TOP_NUMBER.getName());
    String sTopType = map.get(SmtProperty.SMT_SUBCOMMAND.getName());
    String sOnce    = map.get(SmtProperty.SMT_SINGLE_SHOT.getName());
    if(sOnce == null)
      sOnce = "false";
    
    boolean once = sOnce.toLowerCase().startsWith("t") || sOnce.toLowerCase().startsWith("y");;
    int numTop     = 20; // by default, show the top 20
    
    if(sNumTop != null)
    {
      try
      {
        numTop = Integer.parseInt(sNumTop);
      }
      catch(Exception e)
      {
        // I don't care why its here, just use the default
        numTop = 20;
      }
    }
    
    // check for white or black list, and init
    initFilter(smtConfig.getConfigMap());
          
     SmtProperty type = SmtProperty.getByName(sTopType);
     if((type == null) || !(SmtProperty.SMT_TOP_TYPES.contains(type)))
     {
       logger.warning("Top Type not specified, using default");
       type = SmtProperty.SMT_NODE_TRAFFIC;
     }
     
     // nodes, links, or ports
     TopA.setMode(numTop, type, false, getFilter());
     
     // normally run this continuously, but allow for a single shot
     if(once)
     {
       TopA.analyzeOnce();
       TopA.displayTop(type);
       
       // not really necessary, will fall through to end here anyway
       TopA.stopAnalysis();
     }
     else
     {
//       osmServiceUpdate(UpdateService, OMService);
       TopA.analyzeContinuously(numTop, type, getFilter());
     }
    
    return true;
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
    USAGE = "[-h=<host url>] [-pn=<port num>] [-rC=<filename>] [-wH=<filename> [-pX=<playback multipliers>]] ";
    HEADER = "\nsmt-top - display active nodes, ports, links\n\n";
    EXAMPLE ="examples:" + SmtConstants.NEW_LINE +
        "> smt-top -pn 10013                        - shows the top 20 traffic nodes" + SmtConstants.NEW_LINE + 
        "> smt-top -pn 10013 -pE 15                 - shows the top 15 error ports" + SmtConstants.NEW_LINE + 
        "> smt-top -pn 10013 -lT 10                 - show the top 10 busiest links" + SmtConstants.NEW_LINE + 
        "> smt-top -rH HypeFR.his -pControl -pT 20  - using a flight recorder file, show the top 20 ports and provide a time slider" + SmtConstants.NEW_LINE + ".";  // terminate with nl 

    // create and initialize the common options for this command
    initMinimumOptions();
    initConnectionOptions();
    initMulitReadFileOptions();
    initPlayableOptions();
    
    // initialize the command specific options
    SmtProperty sp = SmtProperty.SMT_NODE_TRAFFIC;
    Option nT  = OptionBuilder.hasArg(true).hasArgs(1).withArgName( sp.getArgName() ).withValueSeparator('=').withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );

    sp = SmtProperty.SMT_NODE_ERRORS;
    Option nE  = OptionBuilder.hasArg(true).hasArgs(1).withArgName( sp.getArgName() ).withValueSeparator('=').withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );

    sp = SmtProperty.SMT_LINK_TRAFFIC;
    Option lT  = OptionBuilder.hasArg(true).hasArgs(1).withArgName( sp.getArgName() ).withValueSeparator('=').withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );

    sp = SmtProperty.SMT_LINK_ERRORS;
    Option lE  = OptionBuilder.hasArg(true).hasArgs(1).withArgName( sp.getArgName() ).withValueSeparator('=').withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );

    sp = SmtProperty.SMT_PORT_TRAFFIC;
    Option pT  = OptionBuilder.hasArg(true).hasArgs(1).withArgName( sp.getArgName() ).withValueSeparator('=').withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );

    sp = SmtProperty.SMT_PORT_ERRORS;
    Option pE  = OptionBuilder.hasArg(true).hasArgs(1).withArgName( sp.getArgName() ).withValueSeparator('=').withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );

    sp = SmtProperty.SMT_FILTER_FILE;
    Option fF  = OptionBuilder.hasArg(true).hasArgs(1).withArgName( sp.getArgName() ).withValueSeparator('=').withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );

    options.addOption( nT );
    options.addOption( nE );
    options.addOption( lT );
    options.addOption( lE );
    options.addOption( pT );
    options.addOption( pE );
    options.addOption( fF );
    
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
    // set the command and sub-command
    config.put(SmtProperty.SMT_COMMAND.getName(), this.getClass().getName());
    
    // parse (only) the command specific options
    SmtProperty sp = SmtProperty.SMT_REUSE;
    if(line.hasOption(sp.getName()))
    {
      config.put(sp.getName(), line.getOptionValue(sp.getName()));
    }
    
    sp = SmtProperty.SMT_UPDATE_PERIOD;
    if(line.hasOption(sp.getName()))
    {
      config.put(sp.getName(), line.getOptionValue(sp.getName()));
    }
    
    sp = SmtProperty.SMT_UPDATE_MULTIPLIER;
    if(line.hasOption(sp.getName()))
    {
      config.put(sp.getName(), line.getOptionValue(sp.getName()));
    }
    
    sp = SmtProperty.SMT_WRAP_DATA;
    if(line.hasOption(sp.getName()))
    {
      config.put(sp.getName(), line.getOptionValue(sp.getName()));
    }
    
    sp = SmtProperty.SMT_SINGLE_SHOT;
    if(line.hasOption(sp.getName()))
    {
      config.put(sp.getName(), "true");
    }
    
    sp = SmtProperty.SMT_PLAY_CONTROL;
    if(line.hasOption(sp.getName()))
    {
      config.put(sp.getName(), "true");
    }
    
    sp = SmtProperty.SMT_FILTER_FILE;
    if(line.hasOption(sp.getName()))
    {
      config.put(sp.getName(), line.getOptionValue(sp.getName()));
    }
    
    sp = SmtProperty.SMT_READ_OMS_HISTORY;
    if(line.hasOption(sp.getName()))
    {
      boolean status = putHistoryProperty(config, line.getOptionValue(sp.getName()));
      if(status)
      {
        config.put(SmtProperty.SMT_FILE_NAME.getName(), convertSpecialFileName(line.getOptionValue(sp.getName())));
      }
      config.put(SmtProperty.SMT_SUBCOMMAND.getName(), sp.getName());
      // save the argument, which should be the filename
      
    }
    
    // do the 6 top types
     for(SmtProperty type: SmtProperty.SMT_TOP_TYPES)
    {
      if(line.hasOption(type.getName()))
      {
        config.put(type.getName(), line.getOptionValue(type.getName()));
        config.put(SmtProperty.SMT_SUBCOMMAND.getName(), type.getName());
        // save the argument, which should be number of records to display
       
        config.put(SmtProperty.SMT_TOP_NUMBER.getName(), line.getOptionValue(type.getName()));      
      }
    }
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
   ***********************************************************/
  public static void main(String[] args) throws Exception
  {
    System.exit((new SmtTop().execute(args)) ? 0: -1);
  }

  public OSM_ServiceChangeListener getUpdateListener()
  {
    return TopA;
  }
  
}
