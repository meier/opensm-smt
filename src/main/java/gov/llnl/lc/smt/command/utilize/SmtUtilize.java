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
package gov.llnl.lc.smt.command.utilize;

import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Collection;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_List;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_NodeType;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.smt.SmtConstants;
import gov.llnl.lc.smt.command.SmtCommand;
import gov.llnl.lc.smt.command.config.SmtConfig;
import gov.llnl.lc.smt.data.SMT_AnalysisChangeListener;
import gov.llnl.lc.smt.data.SMT_UpdateService;
import gov.llnl.lc.smt.manager.SMT_AnalysisManager;
import gov.llnl.lc.smt.manager.SMT_AnalysisUpdater;
import gov.llnl.lc.smt.props.SmtProperty;
import gov.llnl.lc.time.TimeStamp;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;


/**********************************************************************
 * Describe purpose and responsibility of SmtUtilize
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version May 31, 2013 2:56:01 PM
 **********************************************************************/
public class SmtUtilize extends SmtCommand implements SMT_AnalysisChangeListener
{
  
  SMT_AnalysisManager Analysis_Mgr  = SMT_AnalysisManager.getInstance();
  OSM_NodeType aType = OSM_NodeType.UNKNOWN;
  boolean longForm = true;


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
    // this is the utilize command, and ultimately we want to be able
    // to operate off an instance of the the service (OFFLINE) or
    // dynamically with changing data
    
    // ALSO:  a mode where a connection is established and used or
    // reused until done.  As opposed to a new connection each time
    // new data is desired or refreshed.  Both have pros/cons, so
    // provide both.
    
    boolean exit = false;
    
    initServiceUpdater(config);

    if(OMService == null)
    {
      System.err.println("The service is null");
    }
    else
    {
      // this is the GUI command, and it can take a subcommand and an argument
      String subCommand    = null;
      String subCommandArg = null;
      String sOnce         = null;
      String sPlay         = null;
       
      if(config != null)
      {
        Map<String,String> map = config.getConfigMap();
        subCommand             = map.get(SmtProperty.SMT_SUBCOMMAND.getName());
        subCommandArg          = map.get(subCommand);
        sOnce                  = map.get(SmtProperty.SMT_SINGLE_SHOT.getName());
        sPlay                  = map.get(SmtProperty.SMT_PLAY_CONTROL.getName());

      }
      
      // start all managers
      /* refer to SmtConsole, may need a manager to do this more cleanly */
      exit = (UpdateService == null) || (OMService == null);
      if(!exit)
      {
        // connect the analyzer to the service
        Analysis_Mgr.setIncludedTypes(aType);
        UpdateService.addListener(Analysis_Mgr);
        Analysis_Mgr.addSMT_AnalysisChangeListener(this);
        
        if (sPlay == null)
          sPlay = "false";
        boolean playable = sPlay.toLowerCase().startsWith("t") || sPlay.toLowerCase().startsWith("y");

        if(sOnce == null)
          sOnce = "false";
        boolean once = sOnce.toLowerCase().startsWith("t") || sOnce.toLowerCase().startsWith("y");
        
        longForm = !(SmtProperty.SMT_DUMP.getName().equalsIgnoreCase(subCommand));
        boolean pControl = once ? false: playable;
        
        /*
         * We are either onLine or offLine - isOnLine()
         * We want a single snaphot, or continuous
         *   if continuous; with or without playBar
         * We want a short or long form
         */

        // normally run this continuously, but allow for a single shot
        if(once)
        {
          dumpOneUtilization();
          System.exit(0);
        }
        
        // if here, display multiple - either from a file, or connection
        
        if((pControl) && (UpdateService instanceof SMT_UpdateService))
          // a floating time slider
          ((SMT_UpdateService) UpdateService).setDetachedFrame(true);

        if(!isOnLine() && !pControl)
        {
          // this is from a file and not using the play control, so just display everything
          dumpAllUtilization();
          System.exit(0);
        }
        
        // if here, we are displaying continuously from a file or from a connection
 
          // infinite wait loop
          while(!exit)
          {
            TimeUnit.MINUTES.sleep(5);
            // if this is connected to a file, check if we have reached the end, and wrap is not true
            // allow exit to be true, if we have reached the end
          }
      }
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
    USAGE = "[-h=<host url>] [-pn=<port num>] [-rH=<filename> [-pX=<playback multipliers>]] ";
    HEADER = "\nsmt-utilize - display % bandwidth utilized\n\n";
    EXAMPLE ="examples:" + SmtConstants.NEW_LINE +
        "> smt-utilize -pn 10013                           - continuously shows the bandwidth utilization of all ports via the service (long form)" + SmtConstants.NEW_LINE + 
        "> smt-utilize -rH HypeFR.his -pControl -dump -SW  - shows the raw switch to switch port utilization from the file using the play controls" + SmtConstants.NEW_LINE + 
        "> smt-utilize -pn 10011 -once -dump -CA           - shows the current bandwidth utilization of the ports connected to channel adapters" + SmtConstants.NEW_LINE + 
        "> smt-utilize -rH HypeFR.his -once                - shows the first bw utilization for all the ports from the flight recorder file" + SmtConstants.NEW_LINE + ".";  // terminate with nl 

    // create and initialize the common options for this command
    initMinimumOptions();
    initConnectionOptions();
    initPlayableOptions();  

    SmtProperty sp = SmtProperty.SMT_READ_OMS_HISTORY;
    Option rHist  = OptionBuilder.hasArg(true).hasArgs(1).withArgName( sp.getArgName() ).withValueSeparator('=').withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );

    sp = SmtProperty.SMT_DUMP;
    Option dump  = OptionBuilder.hasArg(false).withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );
    
    Option sw_type  = OptionBuilder.hasArg(false).withDescription(  "analyze only switch to switch ports" ).withLongOpt("switch2switch").create( "SW" );
    Option ca_type  = OptionBuilder.hasArg(false).withDescription(  "analyze ports to or from channel adapters" ).withLongOpt("edgePort").create( "CA" );
    
    options.addOption( rHist );
    options.addOption( dump );
    options.addOption( sw_type );
    options.addOption( ca_type );
    
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
     
      // see if there was a timestamp specified, and if so, does it make sense
      String ts = config.get(SmtProperty.SMT_TIMESTAMP.getName());

      // it could be null, or if it returns the current time, then its probably crap
      if((ts == null) || ((new TimeStamp()).compareTo(new TimeStamp(ts))) == 0)
      {
        logger.severe("The timestamp couldn't be created for: (" + ts + ")");
        // empty or couldn't convert to timestring (set special directive to use default value)
        config.put(SmtProperty.SMT_TIMESTAMP.getName(), SmtProperty.SMT_USE_DEFAULT.getName());
      }
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
    
    if(line.hasOption("SW"))
    {
      aType = OSM_NodeType.SW_NODE;
    }
    else if(line.hasOption("CA"))
    {
      aType = OSM_NodeType.CA_NODE;
    }
    
    // parse (only) the command specific options
    sp = SmtProperty.SMT_REUSE;
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

     return status;
  }
  
  private void dumpAllUtilization()
  {
    // iterate through the history, and display the analysis
    boolean exit = (UpdateService == null) || (OMService == null);
    if(!exit)
    {
      if(UpdateService instanceof SMT_UpdateService)
      {
        SMT_UpdateService sus = ((SMT_UpdateService) UpdateService);
        OMS_Collection collection = sus.getCollection();
        LinkedHashMap<String, OpenSmMonitorService> omsHistory = collection.getOSM_History();
        
        // loop through the history, and display the utilization (key is timestamp, value is OMS)
        for (Map.Entry<String, OpenSmMonitorService> entry : omsHistory.entrySet())
        {
          OpenSmMonitorService oms = entry.getValue();
          
          try
          {
            // trigger a new analysis
            Analysis_Mgr.osmServiceUpdate(sus, oms);
            
            // give the analyzer some time to complete, and signal the smtAnalysisUpdate, before continuing
            TimeUnit.SECONDS.sleep(1);
          }
          catch (Exception e)
          {
            System.err.println("Error doing the delta analysis, or time delay");
          }
        }
      }
      else
        System.err.println("Using the original OMS_UpdateService");
    }
  }
  
  private void dumpOneUtilization()
  {
     boolean exit = (UpdateService == null) || (OMService == null);
    if(!exit)
    {
      if(UpdateService instanceof SMT_UpdateService)
      {
        SMT_UpdateService sus = ((SMT_UpdateService) UpdateService);
        OMS_Collection collection = sus.getCollection();
        OMS_List list = collection.getOMS_List();
        
        if(list.size() > 1)
        {
          OpenSmMonitorService [] omsArray = list.getRecentOMSs(2);
          for (OpenSmMonitorService oms : omsArray)
          {
            try
            {
              // trigger a new analysis
              Analysis_Mgr.osmServiceUpdate(sus, oms);
              
              // give the analyzer some time to complete, and signal the smtAnalysisUpdate, before continuing
              TimeUnit.SECONDS.sleep(2);
            }
            catch (Exception e)
            {
              System.err.println("Error doing the delta analysis, or time delay");
            }
          }
        }
       }
      else
        System.err.println("Using the original OMS_UpdateService");
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
    System.exit((new SmtUtilize().execute(args)) ? 0: -1);
  }

  /************************************************************
   * Method Name:
   *  smtAnalysisUpdate
  **/
  /**
   * Describe the method here
   *
   * @see gov.llnl.lc.smt.data.SMT_AnalysisChangeListener#smtAnalysisUpdate(gov.llnl.lc.smt.manager.SMT_AnalysisUpdater)
   *
   * @param updater
   * @throws Exception
   ***********************************************************/
  
  @Override
  public void smtAnalysisUpdate(SMT_AnalysisUpdater updater) throws Exception
  {
    if(longForm)
      System.out.println(Analysis_Mgr.getDeltaAnalysis().getFabricRateUtilizationSummary());
    else
      System.out.println(Analysis_Mgr.getDeltaAnalysis().getDeltaTimeStamp() + ", " + Analysis_Mgr.getDeltaAnalysis().getFabricRateUtilizationShortSummary());
  }

}
