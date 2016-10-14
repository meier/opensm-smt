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
 *        file: SmtGui.java
 *
 *  Created on: Oct 1, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command.gui;

import gov.llnl.lc.smt.SmtConstants;
import gov.llnl.lc.smt.command.SmtCommand;
import gov.llnl.lc.smt.command.config.SmtConfig;
import gov.llnl.lc.smt.event.SmtMessage;
import gov.llnl.lc.smt.event.SmtMessageType;
import gov.llnl.lc.smt.manager.MessageManager;
import gov.llnl.lc.smt.props.SmtProperty;
import gov.llnl.lc.smt.swing.SmtGuiApplication;
import gov.llnl.lc.time.TimeStamp;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

/**********************************************************************
 * Describe purpose and responsibility of SmtGui
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Oct 1, 2013 4:39:40 PM
 **********************************************************************/
public class SmtGui extends SmtCommand
{

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
    System.exit((new SmtGui().execute(args)) ? 0: -1);
  }

  @Override
  public boolean doCommand(SmtConfig config) throws Exception
  {
    boolean exit = false;
    
    MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "** Initializing the ServiceUpdater"));
    initServiceUpdater(config);
    MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "** Finished ServiceUpdater initialization"));

    if(OMService == null)
    {
      System.err.println("The service is null");
    }
    else
    {
      // this is the GUI command, and it can take a subcommand and an argument
      String subCommand    = null;
      String subCommandArg = null;
       
      if(config != null)
      {
        Map<String,String> map = config.getConfigMap();
        subCommand = map.get(SmtProperty.SMT_SUBCOMMAND.getName());
        subCommandArg = map.get(subCommand);
      }
      
       
      // start all managers
      /* refer to SmtConsole, may need a manager to do this more cleanly */
      exit = (UpdateService == null) || (OMService == null);
      if(!exit)
      {
        logger.severe("Starting the SMT GUI Application Now");
        MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "ready to start the graphical interface"));
        SmtGuiApplication.go(UpdateService, OMService, this);
      }
      else
        logger.severe("Could not start the SMT GUI Application, check service updater");
      
     // infinite wait loop
      while(!exit)
        TimeUnit.MINUTES.sleep(5);
     }
    return true;
  }


  @Override
  public boolean init()
  {
    USAGE = "[-h=<host url>] [-pn=<port num>] ";
    HEADER = "smt-gui - a graphical tool for investigation, discovery, and exploring Infiniband fabrics";
    EXAMPLE ="examples:" + SmtConstants.NEW_LINE +
        "> smt-gui -pn 10011          - invoke the gui using a connection to the OMS" + SmtConstants.NEW_LINE + 
        "> smt-gui -rH surface3h.his  - invoke the gui using a flight recorder file" + SmtConstants.NEW_LINE + ".";  // terminate with nl

    // create and initialize the common options for this command
    initMinimumOptions();
    initConnectionOptions();
    initPlayableOptions();  

    SmtProperty sp = SmtProperty.SMT_READ_OMS_HISTORY;
    Option rHist  = OptionBuilder.hasArg(true).hasArgs(1).withArgName( sp.getArgName() ).withValueSeparator('=').withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );

    options.addOption( rHist );
    
    return true;
  }

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
    
    sp = SmtProperty.SMT_FILTER_FILE;
    if(line.hasOption(sp.getName()))
    {
      config.put(sp.getName(), line.getOptionValue(sp.getName()));
    }

     return status;
  }

}
