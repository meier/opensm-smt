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
 *        file: SmtRecord.java
 *
 *  Created on: May 31, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command.record;

import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Collection;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Configuration;
import gov.llnl.lc.smt.SmtConstants;
import gov.llnl.lc.smt.command.SmtCommand;
import gov.llnl.lc.smt.command.config.SmtConfig;
import gov.llnl.lc.smt.props.SmtProperty;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;

/**********************************************************************
 * Describe purpose and responsibility of SmtTop
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version May 31, 2013 2:56:01 PM
 **********************************************************************/
public class SmtRecord extends SmtCommand
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
    // this is the record command
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
    String hostNam = map.get(SmtProperty.SMT_HOST.getName());
    String portNum = map.get(SmtProperty.SMT_PORT.getName());
     
//    String file = convertSpecialFileName(map.get(SmtProperty.SMT_WRITE_OMS.getName()));    
//    if(file != null)
//    {
//      try
//      {
//        OpenSmMonitorService.writeOMS(file, OMService);
//        map.put(SmtProperty.SMT_OMS_FILE.getName(), file);      
//        System.out.println(OMService.toInfo());
//      }
//      catch (IOException e)
//      {
//        logger.severe("Unable to save OMS file: (" + e.getMessage() + ")");
//      }
//    }
//    
//    file = convertSpecialFileName(map.get(SmtProperty.SMT_WRITE_FABRIC.getName()));    
//    if(file != null)
//    {
//      try
//      {
//        OSM_Fabric.writeFabric(file, OMService.getFabric());
//        map.put(SmtProperty.SMT_FABRIC_FILE.getName(), file);
//        System.out.println(OMService.getFabric().toInfo());
//      }
//      catch (IOException e)
//      {
//        logger.severe("Unable to save Fabric file: (" + e.getMessage() + ")");
//      }
//    }
//    
//    file = convertSpecialFileName(map.get(SmtProperty.SMT_WRITE_DELTA.getName()));    
//    if(file != null)
//    {
//      try
//      {
//        OSM_FabricDelta fd = getOSM_FabricDeltaOld(false);
//        OSM_FabricDelta.writeFabricDelta(file, fd);
//        map.put(SmtProperty.SMT_FABRIC_DELTA_FILE.getName(), file);
//        System.out.println(fd.toInfo());
//      }
//      catch (IOException e)
//      {
//        logger.severe("Unable to save FabricDelta file: (" + e.getMessage() + ")");
//      }
//    }
//    
//    file = convertSpecialFileName(map.get(SmtProperty.SMT_WRITE_DELTA_HISTORY.getName()));    
//    if(file != null)
//    {
//      // this is the flight recorder, I need to know how long
//      String argType = map.get(SmtProperty.SMT_SUBCOMMAND_ARG.getName());
//      if(argType != null)
//      {
//        int duration = Integer.parseInt(map.get(argType));
//        TimeUnit tUnit = null;
//        if(argType.equals(SmtProperty.SMT_HISTORY_MINUTES.getName()))
//          tUnit = TimeUnit.MINUTES;
//        if(argType.equals(SmtProperty.SMT_HISTORY_HOURS.getName()))
//          tUnit = TimeUnit.HOURS;
//        
//        // always save the OSM_FabricDeltaCollection
//        OSM_FabricDeltaCollection.recordHistory(hostNam, portNum, duration, tUnit, file, true);
//      }
//      else
//      {
//        System.err.println("You must supply a duration (-nr | -nh | -nm) in order to record a History");
//      }
//    }
    
    String file = convertSpecialFileName(map.get(SmtProperty.SMT_WRITE_OMS_HISTORY.getName()));    
    if(file != null)
    {
      // this is the flight recorder, I need to know how long
      String argType = map.get(SmtProperty.SMT_SUBCOMMAND_ARG.getName());
      if(argType != null)
      {
        int duration = Integer.parseInt(map.get(argType));
        TimeUnit tUnit = null;
        if(argType.equals(SmtProperty.SMT_HISTORY_MINUTES.getName()))
          tUnit = TimeUnit.MINUTES;
        if(argType.equals(SmtProperty.SMT_HISTORY_HOURS.getName()))
          tUnit = TimeUnit.HOURS;
        
        // always save the OSM_Collection
        OMS_Collection.recordHistory(hostNam, portNum, duration, tUnit, file, true);
      }
      else
      {
        System.err.println("You must supply a duration (-nr | -nh | -nm) in order to record a History");
      }
    }
    
    file = convertSpecialFileName(map.get(SmtProperty.SMT_WRITE_CONFIG.getName()));    
    if(file != null)
    {
      // this is for the Fabric Configuration file
      if(OMService != null)
      {
        OSM_Configuration cfg = getOsmConfig(true);
        if((cfg != null) && (cfg.getFabricConfig() != null))
        {
          // save this configuration (cache it as well)
          OSM_Configuration.writeConfig(file, cfg);
          OSM_Configuration.cacheOSM_Configuration(OMService.getFabricName(), cfg);
        }
      }
      else
        System.err.println("You must supply connection information to obtain and save the Fabric Configuration.");
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
    USAGE = "[-h=<host url>] [-pn=<port num>] [-rC=<filename>] [-wH=<filename> [-nH=<# hours>]] ";
    HEADER = "smt-record - records one or more instances of OMS data (aka \"flight recorder\", requires a connection)";
    EXAMPLE ="examples:" + SmtConstants.NEW_LINE +
        "> smt-record -pn 10011 -nh 3 -wH history.his - saves 3 hours of data to history.his" + SmtConstants.NEW_LINE + 
        "> smt-record -pn 10013 -nr 50 -wH FRdata.his - saves 50 snapshots of data to FRdata.his" + SmtConstants.NEW_LINE + 
        "> smt-record -pn 10011 -wH short.his -nm 45  - saves 45 minutes of data to short.his" + SmtConstants.NEW_LINE  + ".";  // terminate with nl

    // create and initialize the common options for this command
    this.initMinimumOptions();
    this.initConnectionOptions();
//    this.initWriteFileOptions();
    this.initWriteHistoryFileOptions();
    this.initWriteConfigFileOptions();
//    this.initMultiWriteFileOptions();

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
    
    // set the command and sub-command (always do this)
    config.put(SmtProperty.SMT_COMMAND.getName(), this.getClass().getName());
    
    // if the host and port is supplied on the command line, assume we are making
    // a connection to the service - no matter what, for WRITING
    //
    // if this is a read operation, then ignore connection stuff
    //     read may be for getting information about the nature of the file
    //     read may be for converting one file to a different type
    //     read may be for extracting information.
    // 
    //  if host and port is NOT supplied AND there is persistent host and port configured
    //     AND this is NOT a read operation, then its for WRITING
    //
    //  if host and port is NOT supplied AND there is no persistent host and port configured
    //    then its for READING or for nothing
    //
    // parse (only) the command specific options
    
    
    SmtProperty sp = SmtProperty.SMT_WRITE_CONFIG;
//    if(line.hasOption(sp.getName()))
//    {
//      // the file name may have special characters, do I want to save them?
//      config.put(sp.getName(), line.getOptionValue(sp.getName()));
//      config.put(SmtProperty.SMT_SUBCOMMAND.getName(), sp.getName());
//    }
//    
//    sp = SmtProperty.SMT_WRITE_FABRIC;
//    if(line.hasOption(sp.getName()))
//    {
//      config.put(sp.getName(), line.getOptionValue(sp.getName()));
//      config.put(SmtProperty.SMT_SUBCOMMAND.getName(), sp.getName());
//    }
//    
//    sp = SmtProperty.SMT_WRITE_DELTA;
//    if(line.hasOption(sp.getName()))
//    {
//      // the file name may have special characters, do I want to save them?
//      config.put(sp.getName(), line.getOptionValue(sp.getName()));
//      config.put(SmtProperty.SMT_SUBCOMMAND.getName(), sp.getName());
//    }
//    
    
    // handle the HISTORY commands (can only be one of these types)
    SmtProperty sp2 = SmtProperty.SMT_WRITE_OMS_HISTORY;

    if(line.hasOption(SmtProperty.SMT_WRITE_OMS_HISTORY.getName()))
    {
      config.put(sp2.getName(), line.getOptionValue(sp2.getName()));
      config.put(SmtProperty.SMT_SUBCOMMAND.getName(), sp2.getName());
      boolean duration = false;
      
      // look for write history arguments  (check for SMT_SUBCOMMAND_ARG !!)
      sp = SmtProperty.SMT_HISTORY_RECORDS;
      if(line.hasOption(sp.getName()))
      {
        config.put(sp.getName(), line.getOptionValue(sp.getName()));
        config.put(SmtProperty.SMT_SUBCOMMAND_ARG.getName(), sp.getName());
        duration = true;
      }
      
      sp = SmtProperty.SMT_HISTORY_MINUTES;
      if(line.hasOption(sp.getName()))
      {
        config.put(sp.getName(), line.getOptionValue(sp.getName()));
        config.put(SmtProperty.SMT_SUBCOMMAND_ARG.getName(), sp.getName());
        duration = true;
      }
      
      sp = SmtProperty.SMT_HISTORY_HOURS;
      if(line.hasOption(sp.getName()))
      {
        config.put(sp.getName(), line.getOptionValue(sp.getName()));
        config.put(SmtProperty.SMT_SUBCOMMAND_ARG.getName(), sp.getName());
        duration = true;
      }
      
      if(!duration)
      {
        System.err.println("You must supply a duration (-nr | -nh | -nm) in order to record a History");
        logger.severe("You must supply a duration (-nr | -nh | -nm) in order to record a History");
        status = false;
      }
    }
    else if (line.hasOption(SmtProperty.SMT_WRITE_CONFIG.getName()))
    {
      // write the configuration file
      config.put(sp.getName(), line.getOptionValue(sp.getName()));
      config.put(SmtProperty.SMT_SUBCOMMAND.getName(), sp.getName());
    }
     return status;
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
    System.exit((new SmtRecord().execute(args)) ? 0: -1);
  }

}
