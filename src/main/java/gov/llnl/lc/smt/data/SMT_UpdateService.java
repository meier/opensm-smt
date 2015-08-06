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
 *        file: SMT_UpdateService.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.data;

import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Collection;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_List;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Configuration;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.infiniband.opensm.plugin.net.OsmServiceManager;
import gov.llnl.lc.smt.command.SmtCommand;
import gov.llnl.lc.smt.command.SmtCommandType;
import gov.llnl.lc.smt.command.console.SmtConsoleManager;
import gov.llnl.lc.smt.event.SmtMessage;
import gov.llnl.lc.smt.event.SmtMessageType;
import gov.llnl.lc.smt.manager.MessageManager;
import gov.llnl.lc.time.TimeSliderListener;
import gov.llnl.lc.time.TimeSliderPanel;

import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;

/**********************************************************************
 * The SMT_UpdateService is intended to be used with the SmtGui.  It
 * provides a "playable" (gui slider) update service.  The SMT data always
 * comes from a file.  A previously saved file can be used, or a file can
 * be concurrently created via a connection to the OMS.  The later is
 * the default.
 * 
 * Obviously, a previously saved file has a fixed size.  A concurrently saved
 * file grows in size indefinitely.  It is considered a temporary cache file,
 * and is named appropriately.
 * 
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Oct 21, 2013 9:32:49 AM
 **********************************************************************/
public class SMT_UpdateService extends SMT_AbstractUpdateService implements TimeSliderListener
{
  // the control, normally needs to be provided
  private static TimeSliderPanel TimePlayer;
  
  // if true, a new OMS instance is available to be broadcast to all listeners
  private volatile static boolean ReadyToUpdate = false;
  
  private volatile static boolean DetachedFrame = false;
  
  // the number of times the listeners have been updated with an OMS instance
  private volatile static long updateCount = 0;
  
  /** the most current OMS instance from a connection - used only for building the history**/
  private static volatile OpenSmMonitorService newService = null;
  
  /** the one and only <code>SMT_UpdateService</code> Singleton **/
  private volatile static SMT_UpdateService globalUpdateService  = null;

  private SMT_UpdateService()
  {
    createThread();
  }
  
  /**************************************************************************
 *** Method Name:
 ***     getInstance
 **/
 /**
 *** Get the singleton SMT_UpdateService. This can be used if the application wants
 *** to share one manager across the whole JVM.  Currently I am not sure
 *** how this ought to be used.
 *** <p>
 ***
 *** @return       the GLOBAL (or shared) SMT_UpdateService
 **************************************************************************/

 public static SMT_UpdateService getInstance()
 {
   synchronized( SMT_UpdateService.semaphore )
   {
     if ( globalUpdateService == null )
     {
       globalUpdateService = new SMT_UpdateService( );
     }
     return globalUpdateService;
   }
 }
 /*-----------------------------------------------------------------------*/

 /**************************************************************************
  *** Method Name:
  ***     startThread
  **/
  /**
  *** Summary_Description_Of_What_startThread_Does.
  *** <p>
  ***
  *** @see          Method_related_to_this_method
  ***
  *** @param        Parameter_name  Description_of_method_parameter__Delete_if_none
  ***
  *** @return       Description_of_method_return_value__Delete_if_none
  ***
  *** @throws       Class_name  Description_of_exception_thrown__Delete_if_none
  **************************************************************************/

  protected boolean startThread()
  {
    logger.info("Starting the " + Listener_Thread.getName() + " Thread");
    
    OpenSmMonitorService oms = null;

    // the default file name will be
    String fn = "smt-gui file";
    
    String mst = "  Already have the OMS History?? (" + Boolean.toString(this.omsHistory != null) + ")";
    MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, mst));

    // open the file if possible
    if(isFileMode())
    {
      if(omsHistory == null)
      {
        fn = SmtCommand.convertSpecialFileName(getFile());
        try
       {
         MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "  Reading the OMS History file (" + fn + ")"));
         omsHistory = OMS_Collection.readOMS_Collection(fn);
       }
       catch (FileNotFoundException e)
       {
         System.err.println("Couldn't open the file (" + getFile() + ")");
         e.printStackTrace();
       }      
       catch (IOException e)
       {
         e.printStackTrace();
       }
       catch (ClassNotFoundException e)
       {
         e.printStackTrace();
       }
        MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "  Done reading the OMS History file (" + fn + ")"));
        logger.severe("Done reading the file: " + fn);
        
        oms = omsHistory.getCurrentOMS();
        
      }
      else
        MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "Already have the collection in memory"));

      // now try to read the configuration file if it exists
      if((osmConfig == null) && (oms != null))
      {
        // try a specified config file
        fn = SmtCommand.convertSpecialFileName(getConfigFile());
        if(fn == null)
        {
          oms.getFabricName();
          fn  = OSM_Configuration.getCacheFileName(oms.getFabricName());
          logger.info("Trying cache file (" + fn + ")");
        }
        
        if(fn != null)
        {
          try
          {
            MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "  Reading the OSM Configuration file (" + fn + ")"));
            osmConfig = OSM_Configuration.readConfig(fn);
          }
          catch (FileNotFoundException e)
          {
            System.err.println("Couldn't open the file (" + getFile() + ")");
            e.printStackTrace();
          }      
          catch (IOException e)
          {
            System.err.println("Couldn't open the file (" + getFile() + ")");
            e.printStackTrace();
          }
          catch (ClassNotFoundException e)
          {
            e.printStackTrace();
          }
           MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "  Done reading the OSM Configuration file (" + fn + ")"));
           logger.severe("Done reading the file: " + fn);
        }
        else
        {
          System.err.println("The configuration file is NULL???");
          MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_SEVERE, "  Could not find the OSM Configuration file (" + fn + ")"));
          logger.severe("Could not find the OSM Configuration file (" + fn + ")");
        }
      }
      else
        MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "Already have the configuration in memory"));

    }
    else
    {
      // this is a live connection, so get the initial instances of the OMS and Config, create a collection
      // and then save them into a cache file.
      try
      {
        // get a brand new OMS from the connection
        MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "  Getting an OMS snapshot from the connection"));
        oms = getNewOMS();
      }
      catch (IOException e)
      {
        // TODO Auto-generated catch block
        logger.severe("Could not get a new OMS via the connection");
      }
      MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "  Received OMS snapshot from the connection"));
      // create a history with this one in it, and then save it at the end
      MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "  Creating a temporary OMS History cache from this snapshot"));
      if(oms != null)
        omsHistory = OMS_Collection.cacheOMS_Collection(oms, omsHistory);
      else
        logger.severe("Initial OMS was not obtained from getNewOMS()");
    }
    
    // don't start the thread until after the file has been read
    MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "Starting the SMT_UpdateService thread"));
    Listener_Thread.start();
    return true;
  }
  /*-----------------------------------------------------------------------*/

  protected boolean initPlayer()
  {
    // the history should be in memory
    if((omsHistory == null) || (omsHistory.getSize() < 1))
      return false;
    
    osmService = omsHistory.getOMS(0);
    
    if(osmService == null)
      return false;

    // create the slider,initialize it, and listen
    TimePlayer = new TimeSliderPanel();
    
    // now set up the file specific stuff for the slider
    TimePlayer.setTimeRange(omsHistory.getOldestOMS().getTimeStamp(),
        omsHistory.getCurrentOMS().getTimeStamp(), omsHistory.getSize(),
        osmService.getFabric().getPerfMgrSweepSecs(), false);
    
    TimePlayer.setWrap(isWrapData());
    TimePlayer.setPlay(true);
    TimePlayer.setMultiplier((int)getUpdateMultiplier());
    
    if(!isFileMode())
      TimePlayer.setMultiplier(1);
    
    // add myself as a listener, get all change types
    TimePlayer.addListener(this);
    
    // if there is no parent frame, create our own and fill it
    if(DetachedFrame)
    {
      // closing the player frame should cause a graceful exit.
      // Detect it, and do the right thing for the specific command
      JFrame frame = new JFrame(getFrameTitle());
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.addWindowListener( new WindowAdapter()
      {
        public void windowClosing(WindowEvent e)
        {
          // if I am a ConsoleManager, make sure it cleans up
          if(getToolName().equalsIgnoreCase(SmtCommandType.SMT_CONSOLE_CMD.getToolName()))
            SmtConsoleManager.getInstance().destroy("Done Playing Around");
          // TODO  any other special command specific close cleanup
        }
      });

      frame.setContentPane(TimePlayer);
      frame.pack();
      frame.setVisible(true);      
    }
    return true;
  }
  
  protected void updatePlayer(boolean grown)
  {
     // re-jigger the player with the new collection ranges
    
    // where is the slider right now?
    int index = TimePlayer.getSliderValue();
    OpenSmMonitorService osm = omsHistory.getOMS(index);    
    
    TimePlayer.resetTimeRange(osm.getTimeStamp(), omsHistory.getCurrentOMS().getTimeStamp(), omsHistory.getSize());
  
 //   System.err.println("3The player state is: " + TimePlayer.isPlaying() + ", and value is: " + index + ", and size is: " + omsHistory.getSize());
  }
  
  protected String getFrameTitle()
  {
    // should be something like;
    //         "smt-command for (cluster) using [filename]"
    String command = getToolName();
    String fabric = "unknown fabric";
    if(osmService != null)
      fabric = osmService.getFabricName();
    
    return command + " for (" + fabric + ") using [" + getFile() + "]";
  }

 @Override
 public void run()
 {
   // the history should be in memory, so okay to initialize
   MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "initializing the TimeSliderPlayer"));
   if(!initPlayer())
   {
       logger.severe("Couldn't get a new service instance, or initialize the player");
       Continue_Thread = false;
   }
   
   // the following only used for connected mode (not FileMode), using the player controls
   long sliderDwellMS  = 25;
   long updateSecs     = getUpdatePeriod();
   long updateTrigger  = (updateSecs * 1000)/sliderDwellMS;
   long triggerCounter  = 0L;
   
   
   Thread_Running = true;
   
   MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "Entering continuous loop of SMT_UpdateService thread (run())"));
   // check the Thread Termination Flag, and continue if Okay
    while(Continue_Thread)
   {
      // this loop is basically spinning, waiting for the osmService to be
      // updated by activity in the player.  The player changes either
      // automatically, or manually (someone moving the slider)
      // either way, we are listening and responding here as if it came
     try
     {
       // attempt to update the collection if running in "connected" mode
       if((triggerCounter > updateTrigger) && !isFileMode())
       {
         // reset the loop (dwell) counter for the next trigger interval
         triggerCounter = 0L;
                 
         // this is a live connection, so an instance the OMS and grow the collection
         // and then save it into a cache file.
         try
         {
           newService = getNewOMS();
         }
         catch (IOException e)
         {
           // TODO Auto-generated catch block
           logger.severe("Could not get a new OMS via the connection");
         }
         // grow the history with this one in it, and then save it
         if(newService != null)
         {
           int origSize = omsHistory.getSize();
           omsHistory = OMS_Collection.cacheOMS_Collection(newService, omsHistory);
           int newSize = omsHistory.getSize();
           // tweak the slider, so it knows it has more data
           if(origSize != newSize)
           {
             // the collection has grown, so the slider/player needs to be updated
              updatePlayer(true);
           }
         }
         else
         {
           // the returned service was null, connection closed or dropped??
           logger.severe("Did not obtain a new OMS, connection dropped?  Try again? Dwell...");
           TimeUnit.SECONDS.sleep(1);
         }
       }

       // provide the first two OMS data sets (for delta calcs), then pause if in file mode
       if(updateCount == 1)
       {
         updateCount++;  // increment so we don't hit this again
         if(isFileMode())
           TimePlayer.setPlay(false);  // by default, stop the player after two data points for file based stuff
       }
       
       if(isReadyToUpdate())
       {
         updateAllListeners();
         // where is this comming from?
         TimePlayer.setCurrentTime(osmService.getTimeStamp());

         setReadyToUpdate(false);
        }
       
        // dwell a tiny bit, just so we don't spin like crazy (40hz)
       TimeUnit.MILLISECONDS.sleep(sliderDwellMS);
       triggerCounter++;
      }
     catch (Exception e)
     {
       // nothing to do yet
       logger.severe("Crap, couldn't get a new service instance");
       logger.severe(e.getMessage());
     }
   }
     
    MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "Terminating the continuous loop of SMT_UpdateService thread (run())"));
   logger.info("Terminating the " + Listener_Thread.getName() + " Thread");
   
   /* fall through, must be done! */
   Thread_Running = false;
 }
 
 private static synchronized boolean isFirstTwoUpdates()
 {
   return updateCount < 2;
 }
 
 /************************************************************
 * Method Name:
 *  isReadyToUpdate
 **/
/**
 * Returns the value of readyToUpdate
 *
 * @return the readyToUpdate
 *
 ***********************************************************/

private static synchronized boolean isReadyToUpdate()
{
  return ReadyToUpdate;
}

/************************************************************
 * Method Name:
 *  setReadyToUpdate
 **/
/**
 * Sets the value of readyToUpdate
 *
 * @param readyToUpdate the readyToUpdate to set
 *
 ***********************************************************/
private static synchronized void setReadyToUpdate(boolean readyToUpdate)
{
  if(readyToUpdate)
    updateCount++;
  ReadyToUpdate = readyToUpdate;
}

 
///////////////////////////////////////////// slider listeners ///////////////////////////////
 
/************************************************************
 * Method Name:
 *  isDetachedFrame
 **/
/**
 * Returns the value of detachedFrame
 *
 * @return the detachedFrame
 *
 ***********************************************************/

public synchronized boolean isDetachedFrame()
{
  return DetachedFrame;
}

/************************************************************
 * Method Name:
 *  setDetachedFrame
 **/
/**
 * Sets the value of detachedFrame
 *
 * @param detachedFrame the detachedFrame to set
 *
 ***********************************************************/
public synchronized void setDetachedFrame(boolean detachedFrame)
{
  DetachedFrame = detachedFrame;
}

/************************************************************
 * Method Name:
 *  getTimePlayer
 **/
/**
 * Returns the value of timePlayer
 *
 * @return the timePlayer
 *
 ***********************************************************/

public synchronized TimeSliderPanel getTimePlayer()
{
  return TimePlayer;
}

private OSM_Configuration getNewConfig() throws IOException
{
  // this is only used when I need to force a "new" or immediate
  // OSM_Configuration acquisition, because I should already have one
  
  OSM_Configuration config = null;
  if(isConnectionReused())
  {
    if(ParentSession == null)
    {
      /* the one and only OsmServiceManager */
      OsmServiceManager OsmService = OsmServiceManager.getInstance();

      try
      {
        logger.info("Opening the OMS Session for re-use (keep open)");
        ParentSession = OsmService.openSession(getHost(), getPort(), null, null);
      }
      catch (Exception e)
      {
        logger.severe(e.getStackTrace().toString());
        System.exit(-1);
      }         
    }

    if (ParentSession != null)
    {
      logger.info("Re-using the OMS Session to obtain an OSM_Configuration object)");
      config = OSM_Configuration.getOsmConfig(ParentSession);
    }
    else
    {
      logger.severe("Could not establish an OMS session");
    }
  }
  else
  {
    logger.info("Obtaining an OSM_Configuration object via a single use OMS Session)");
    config = OSM_Configuration.getOsmConfig(getHost(), getPort());
  }
return config;
}

private OpenSmMonitorService getNewOMSV2() throws IOException
{
  OMS_List omsList = getOMS_List();

  if((omsList != null)&&(omsList.size() > 0))
    return omsList.getCurrentOMS();
  return null;
}

private OpenSmMonitorService getNewOMS() throws IOException
{
  // this is only used when I need to force a "new" or immediate
  // OMS acquisition, because I am already getting it periodically
  
  OpenSmMonitorService oms = null;
  if(isConnectionReused())
  {
    if(ParentSession == null)
    {
      /* the one and only OsmServiceManager */
      OsmServiceManager OsmService = OsmServiceManager.getInstance();

      try
      {
        logger.info("Opening the OMS Session for re-use (keep open)");
        ParentSession = OsmService.openSession(getHost(), getPort(), null, null);
      }
      catch (Exception e)
      {
        logger.severe(e.getStackTrace().toString());
        System.exit(-1);
      }         
    }

    if (ParentSession != null)
    {
      logger.info("Re-using the OMS Session to obtain an OMS snapshot)");
      oms = OpenSmMonitorService.getOpenSmMonitorService(ParentSession);         
    }
    else
    {
      logger.severe("Could not establish an OMS session");
    }
  }
  else
  {
    logger.info("Obtaining an OMS snapshot via a single use OMS Session)");
    oms = OpenSmMonitorService.getOpenSmMonitorService(getHost(), getPort());
  }
  
return oms;
}

private OMS_List getNewOMS_List() throws IOException
{
  // this is only used when I need to force a "new" or immediate
  // OMS_List acquisition, because I am already getting it periodically
  //
  // it ALSO gets the Servers version of the List, instead of the locally
  // maintained version this Updater builds.  They "should be the same,
  // but this just goes and gets it.
  
  OMS_List omsList = null;
  if(isConnectionReused())
  {
    if(ParentSession == null)
    {
      /* the one and only OsmServiceManager */
      OsmServiceManager OsmService = OsmServiceManager.getInstance();

      try
      {
        logger.info("Opening the OMS Session for re-use (keep open)");
        ParentSession = OsmService.openSession(getHost(), getPort(), null, null);
      }
      catch (Exception e)
      {
        logger.severe(e.getStackTrace().toString());
        System.exit(-1);
      }         
    }

    if (ParentSession != null)
    {
      logger.info("Re-using the OMS Session to obtain an OMS_List cache)");
      omsList = OpenSmMonitorService.getOMS_List(ParentSession);         
    }
    else
    {
      logger.severe("Could not establish an OMS session");
    }
  }
  else
  {
    logger.info("Obtaining an OMS_List cache via a single use OMS Session)");
    omsList = OpenSmMonitorService.getOMS_List(getHost(), getPort());
  }
  
return omsList;
}

/************************************************************
 * Method Name:
 *  stateChanged
**/
/**
 * Listen for the slider to change.  Once started, the slider acts as the
 * event timer, so use it to get the data from the history, and update everything.
 * 
 * The slider will generate this event if;
 *     time period has elapsed
 *     manually dragged
 *     the TimePlayer.setSliderValue() is called
 * The slider will stop generating periodic events if paused, but all else should
 * still work.
 *
 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
 *
 * @param evt
 ***********************************************************/
  public void stateChanged(ChangeEvent evt)
  {
    // make less responsive, don't want so many change notices
    logger.info("History Player Slider stateChange");

    // change to this instance only if previous was processed
    //  ** the ReadyToUpdate state is supposed to indicate if
    //  ** we had time to finish the previous change, so we
    //  ** don't try to update too often (dragging, or ???)
    if (!isReadyToUpdate())
    {
      //  false means all listeners got the previous update, its okay to
      //   overwrite with another from the history based on the slider value
      setOMS(omsHistory.getOMS(TimePlayer.getSliderValue()));
      
      // use getOMS() from now on
      TimePlayer.setCurrentTime(getOMS().getTimeStamp());
      setReadyToUpdate(true);
    }

    // if not playing, show the timestamp that corresponds to the slider
    // so it will keep up, even if we are not done processing (not ReadyToUpdate)
    if (!TimePlayer.isPlaying())
    {
      // FIXME - is this correct?  what behavior do I want?  always show the real OMS TS, or
      // FIXME - just reflect what the slider position is?
      
//      TimePlayer.setCurrentTime(omsHistory.getOMS(TimePlayer.getSliderValue()).getTimeStamp());
    }

    // reset flag, start timer
    try
    {
      TimeUnit.MILLISECONDS.sleep(25);
    }
    catch (InterruptedException e1)
    {
      e1.printStackTrace();
    }
  }

  /************************************************************
   * Method Name:
   *  propertyChange
  **/
  /**
   * Listen for the multiplier to change values.
   * Currently don't specialize this event, as the TimeSliderPanel does
   * everything necessary.   *
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   *
   * @param evt
   ***********************************************************/
  public void propertyChange(PropertyChangeEvent evt)
  {
    // the multiplier value
    Object source = evt.getSource();
    if (source instanceof JFormattedTextField)
    {
      // do nothing, the TimeSliderPanel is doing all the work
      logger.info("History Player Slider multiplier changed: " + TimePlayer.getMultiplier());
    }
  }

  /************************************************************
   * Method Name:
   *  itemStateChanged
  **/
  /**
   * Listen for the wrap checkbox and the play/pause button.
   * Currently don't specialize this event, as the TimeSliderPanel does
   * everything necessary.
   *
   * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
   *
   * @param evt
   ***********************************************************/
  public void itemStateChanged(ItemEvent evt)
  {
    Object source = evt.getItemSelectable();

    // the play/pause button
    if ((source instanceof JToggleButton) && !(source instanceof JCheckBox))
      if (evt.getStateChange() == ItemEvent.DESELECTED)
        logger.info("Play button was deselected");
      else
        logger.info("Play button was selected");

    // the wrap checkbox
    if (source instanceof JCheckBox)
      if (evt.getStateChange() == ItemEvent.DESELECTED)
        logger.info("Wrap button was deselected");
      else
        logger.info("Wrap button was selected");
  }

}
