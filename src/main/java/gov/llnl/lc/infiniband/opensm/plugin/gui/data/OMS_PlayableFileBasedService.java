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
 *        file: OMS_PlayableFileBasedService.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.data;

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

import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_AbstractUpdateService;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Collection;
import gov.llnl.lc.smt.command.SmtCommand;
import gov.llnl.lc.smt.command.SmtCommandType;
import gov.llnl.lc.smt.command.console.SmtConsoleManager;
import gov.llnl.lc.time.TimeSliderListener;
import gov.llnl.lc.time.TimeSliderPanel;

public class OMS_PlayableFileBasedService extends OMS_AbstractUpdateService implements TimeSliderListener
{
  private static TimeSliderPanel TimePlayer;
  
  private volatile static boolean ReadyToUpdate = false;
  
  private volatile static boolean DetachedFrame = false;
  
  private volatile static long updateCount = 0;

  /** the one and only <code>OMS_PlayableFileBasedService</code> Singleton **/
  private volatile static OMS_PlayableFileBasedService globalUpdateService  = null;

  private static volatile OMS_Collection omsHistory = null;

  private OMS_PlayableFileBasedService()
  {
     createThread();
  }
  
  /**************************************************************************
 *** Method Name:
 ***     getInstance
 **/
 /**
 *** Get the singleton OMS_ConnectionBasedService. This can be used if the application wants
 *** to share one manager across the whole JVM.  Currently I am not sure
 *** how this ought to be used.
 *** <p>
 ***
 *** @return       the GLOBAL (or shared) OMS_ConnectionBasedService
 **************************************************************************/

 public static OMS_PlayableFileBasedService getInstance()
 {
   synchronized( OMS_PlayableFileBasedService.semaphore )
   {
     if ( globalUpdateService == null )
     {
       globalUpdateService = new OMS_PlayableFileBasedService( );
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
    
    // open the file if possible
    String fn = SmtCommand.convertSpecialFileName(getFile());
    try
   {
     omsHistory = OMS_Collection.readOMS_Collection(fn);
   }
   catch (FileNotFoundException e)
   {
     System.err.println("Couldn't open the file");
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
    logger.severe("Done reading the file: " + fn);

    
    // don't start the thread until after the file has been read
    Listener_Thread.start();
    return true;
  }
  /*-----------------------------------------------------------------------*/

  protected boolean initPlayer()
  {
    // the history should be in memory
    osmService = omsHistory.getOMS(0);
    omsList    = omsHistory.getOldestOMS_List();

    // create the slider,initialize it, and listen
    TimePlayer = new TimeSliderPanel();
    
    if(osmService == null)
      return false;
    
    // now set up the file specific stuff for the slider
    TimePlayer.setTimeRange(omsHistory.getOldestOMS().getTimeStamp(),
        omsHistory.getCurrentOMS().getTimeStamp(), omsHistory.getSize(),
        osmService.getFabric().getPerfMgrSweepSecs(), false);
    
    TimePlayer.setWrap(isWrapData());
    TimePlayer.setPlay(true);
    TimePlayer.setMultiplier((int)getUpdateMultiplier());
    
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
   if(!initPlayer())
   {
     logger.severe("Couldn't get a new service instance, or initialize the player");
     Continue_Thread = false;
   }   
   
   Thread_Running = true;
   
   // check the Thread Termination Flag, and continue if Okay
    while(Continue_Thread)
   {
     try
     {
       // provide the first two OMS data sets (for delta calcs), then pause
       if(updateCount == 1)
       {
         updateCount++;  // increment so we don't hit this again
         TimePlayer.setPlay(false);
       }
        if(isReadyToUpdate())
       {
         updateAllListeners();
         TimePlayer.setCurrentTime(osmService.getTimeStamp());

         setReadyToUpdate(false);
        }
       TimeUnit.MILLISECONDS.sleep(25);
      }
     catch (Exception e)
     {
       // nothing to do yet
       logger.severe("Crap, couldn't get a new service instance");
     }
   }
     
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

/************************************************************
 * Method Name:
 *  stateChanged
**/
/**
 * Listen for the slider to change.  Once started, the slider acts as the
 * event timer, so use it to get the data from the history, and update everything.
 * The slider will generate this event if;
 *     time period has elapsed
 *     manually dragged
 * The slider will stop generating if paused.
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
    if(!isReadyToUpdate())
    {
      setOMS(omsHistory.getOMS(TimePlayer.getSliderValue()));
      setReadyToUpdate(true);
     }
    
    // if not playing, show the timestamp that corresponds to the slider
    // so it will keep up
    if(!TimePlayer.isPlaying())
    {
      TimePlayer.setCurrentTime(omsHistory.getOMS(TimePlayer.getSliderValue()).getTimeStamp());
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
