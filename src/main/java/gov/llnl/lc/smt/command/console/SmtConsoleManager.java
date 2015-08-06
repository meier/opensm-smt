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
 *        file: SmtConsoleManager.java
 *
 *  Created on: Mar 22, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command.console;

import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_NConnectionBasedService;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_UpdateProvider;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_UpdateProvider.UpdaterType;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Updater;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_ServiceChangeListener;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.infiniband.opensm.plugin.gui.data.OMS_PlayableFileBasedService;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.SmtConstants;
import gov.llnl.lc.smt.command.SmtCommand;
import gov.llnl.lc.smt.props.SmtProperty;
import gov.llnl.lc.time.TimeListener;
import gov.llnl.lc.time.TimeService;
import gov.llnl.lc.time.TimeStamp;

import java.util.Properties;

import jcurses.system.Toolkit;

/**********************************************************************
 * The SmtConsoleManager provides the environment for obtaining OMS
 * data, keyboard input, and other dynamic events (current time), and
 * for displaying the desired Screens.  
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Mar 22, 2013 11:47:49 AM
 **********************************************************************/
public class SmtConsoleManager implements CommonLogger, OSM_ServiceChangeListener, TimeListener
{
  /** the one and only <code>SmtConsoleManager</code> Singleton **/
  private volatile static SmtConsoleManager Smt_Console  = null;

  /** the synchronization object **/
  private static Boolean semaphore            = new Boolean( true );
    
  private volatile static TimeService Tserv = TimeService.getInstance();
  private volatile static long timeLoopCounter = 0;
  private static long refreshPeriod   = 30;
  boolean Continue_Thread = true;
  
  private static OMS_Updater uService = null;
  
  private static SmtCommand smtCommand = null;   // the command using this Singleton (if any)
  private  SmtConsoleScreen currentScreen = null;
  
  /* local copies of the current data that gets passed to the screens */
  private OpenSmMonitorService currentOSM = null;
  private OSM_Fabric currentFabric = null;
  private TimeStamp currentTime = new TimeStamp();


  private SmtConsoleManager()
  {
    Toolkit.init();
    Toolkit.clearScreen(ConsoleScreen.BorderBTxtColor);
 }
  

  /**************************************************************************
   *** Method Name:
   ***     getInstance
   **/
   /**
   *** Get the singleton SmtConsoleManager. This can be used if the application wants
   *** to share one manager across the whole JVM.  Currently I am not sure
   *** how this ought to be used.
   *** <p>
   ***
   *** @return       the GLOBAL (or shared) SmtConsoleManager
   **************************************************************************/

   public static SmtConsoleManager getInstance()
   {
     synchronized( SmtConsoleManager.semaphore )
     {
       if ( Smt_Console == null )
       {
         Smt_Console = new SmtConsoleManager( );
       }
       return Smt_Console;
     }
   }
   /*-----------------------------------------------------------------------*/

   public Object clone() throws CloneNotSupportedException
   {
     throw new CloneNotSupportedException(); 
   }
   
   protected boolean initManager(OMS_Updater updater, SmtScreenType initialScreen, SmtCommand command) throws Exception
   {
     // assume the service updater is fully initialized
     uService = updater;
     smtCommand = command;
     
     Class theClass  = Class.forName(initialScreen.getClassName());
     currentScreen = (SmtConsoleScreen)theClass.newInstance();
     
     boolean realTime = (uService instanceof OMS_NConnectionBasedService) ? true: false;
     String exitMessage = "done";

     
     // for online, always show the current time, rather than the data's time
     currentScreen.useCurrentTime(true);

     
     if(!uService.waitUntilReady())
     {
       destroy("service failed");
       return false;
     }

   // do these last, when everything is ready to receive data
    Tserv.addListener(this);
    uService.addListener(this);
    currentScreen.clearScreen();
    
    
    while(Continue_Thread && currentScreen != null)
      try
      {
        currentScreen.useCurrentTime(realTime);
        Continue_Thread = paintScreen();  // blocks on readCharacter
      }
      catch (Exception e)
      {
        e.printStackTrace();
        exitMessage = "Unexpected Exception: "+ e.getMessage();
        logger.severe(exitMessage);
        
        /* this should break the interactive screen program out of its infinite loop, and end the thread */
        Continue_Thread = false;
        
      }
    // this should clean things up
    destroy(exitMessage);

    return true;     
   }
   
  private boolean paintScreen() throws Exception
  {
    boolean status = currentScreen.refreshScreen(false);
    
    SmtScreenType nextScreen = currentScreen.readInput();
    boolean repaintScreen = currentScreen.getRepaint();
    
    if(repaintScreen)
      logger.info("Repaint Request");
    
    if(nextScreen == SmtScreenType.SCN_QUIT)
      return false;
    
    SmtConsoleScreen nScreen = currentScreen;
    
    // repaint the current screen
    if((currentScreen != null) && repaintScreen)
    {
      // clear the screen, and then repaint
      currentScreen.clearScreen();
      logger.info("repainting");
      if(currentTime != null)
        currentScreen.timeUpdate(currentTime);
      if(currentOSM != null)
        currentScreen.osmServiceUpdate(uService, currentOSM);       
      repaintScreen = false;  
    }
    
    // conditionally change screens
    if((nextScreen != null) && (currentScreen.ScreenType != nextScreen))
    {
      // clear the screen, and then change to the new one
      currentScreen.clearScreen();
      Class theClass  = Class.forName(nextScreen.getClassName());
      nScreen = (SmtConsoleScreen)theClass.newInstance();
      
      //TODO - this should happen automatically, a classes screentype needs to be fixed
      if(nScreen != null)
      {
        // force an unscheduled data update, so new screen will be populated with data
        nScreen.ScreenType = nextScreen;
        currentScreen = nScreen;

        if(currentTime != null)
          currentScreen.timeUpdate(currentTime);
        if(currentOSM != null)
          currentScreen.osmServiceUpdate(uService, currentOSM);       
      }
    }
  return true;
  }
  
  public void destroy(String msg)
  {
    logger.info("Terminating the SmtConsole");
    logger.info(msg);
    
    Tserv.removeListener(this);
    Tserv.destroy();
    uService.removeListener(this);
    uService.destroy();
    currentScreen.clearScreen();
    logger.severe("Ending now");
    Toolkit.shutdown();
    System.err.println(msg);
  }

  public synchronized long getUpdatePeriod()
  {
    return refreshPeriod;
  }

  public synchronized SmtCommand getParentSmtCommand()
  {
    return smtCommand;
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
  public static void main(String[] args)
  {
    OMS_Updater UpdateService = null;
    UpdaterType Utype = UpdaterType.CONNECTION_BASED_UPDATER;  // the default
    UpdateService = OMS_UpdateProvider.getInstance().getUpdater(Utype);

    if(UpdateService instanceof OMS_PlayableFileBasedService)
      ((OMS_PlayableFileBasedService) UpdateService).setDetachedFrame(true);

    Properties serviceProps = UpdateService.getProperties();
     if(serviceProps == null)
       serviceProps = new Properties();
     
     // setup the updater service properties
     serviceProps.setProperty(SmtProperty.SMT_PORT.name(), "10011");
     serviceProps.setProperty(SmtProperty.SMT_HOST.name(), "localhost");
     serviceProps.setProperty(SmtProperty.SMT_UPDATE_PERIOD.name(), "30");
     serviceProps.setProperty(SmtProperty.SMT_REUSE.name(), Boolean.toString(true));
     serviceProps.setProperty(SmtProperty.SMT_UPDATE_MULTIPLIER.name(), "20");
     serviceProps.setProperty(SmtProperty.SMT_WRAP_DATA.name(), Boolean.toString(true));
     serviceProps.setProperty(SmtProperty.SMT_OMS_FILE.name(), SmtConstants.SMT_NO_FILE);
     UpdateService.init(serviceProps);
    
    SmtConsoleManager mgr = SmtConsoleManager.getInstance();

    try
    {
      // give the manager the updater, so it can initialize itself, then done!
      mgr.initManager(UpdateService, SmtScreenType.SCN_CONTENTS, null);
    }
    catch (Exception e)
    {
      logger.severe("Could not initialize the SmtConsoleManager with the update service");
      e.printStackTrace();
    }
  }


  @Override
  public void osmFabricUpdate(OSM_Fabric osmFabric) throws Exception
  {
    // just pass this on to the current screen (should cause foreground to update too!)
    if(currentScreen != null)
    {
      // the SmtConsoleManager is not technically an OMS_UdateService, but
      // is essentially a proxy for that service.  It receives the update,
      // then forwards it out to the current screen.  Only the current
      // screen gets they update, not all the invisible screens.
      // If all the individual screens were listening to the OMS_ConnectionBasedService,
      // then they would all receive all the updates, even when they are not
      // visible.  Using the proxy method like this is an optimization.
      currentScreen.osmFabricUpdate(osmFabric);
      
      // give the screen access to the updater, if it needs it
      currentScreen.setServiceUpdater(uService, false);
    }
    currentFabric = osmFabric;
  }


  @Override
  public void timeUpdate(TimeStamp Time)
  {
    // this should get called once per second
    timeLoopCounter++;

    // just pass this on to the current screen (should cause foreground to update too!)
    if((currentScreen != null) && (Time != null))
    {
       currentScreen.timeUpdate(Time);
    }
    currentTime = Time;
  }


  @Override
  public void osmServiceUpdate(OMS_Updater updater, OpenSmMonitorService osmService) throws Exception
  {
    // just pass this on to the current screen (should cause foreground to update too!)
    if(currentScreen != null)
    {
      // the SmtConsoleManager is not technically an OMS_UdateService, but
      // is essentially a proxy for that service.  It receives the update,
      // then forwards it out to the current screen.  Only the current
      // screen gets the update, not all the invisible screens.
      // If all the individual screens were listening to the OMS_ConnectionBasedService,
      // then they would all receive all the updates, even when they are not
      // visible.  Using the proxy method like this is an optimization.
      currentScreen.osmServiceUpdate(updater, osmService);
      
      // give the screen access to the updater, if it needs it
      currentScreen.setServiceUpdater(uService, false);
    }
    currentOSM = osmService;
  }

}
