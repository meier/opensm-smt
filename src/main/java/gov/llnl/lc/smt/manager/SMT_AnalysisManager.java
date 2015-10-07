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
 *        file: SMT_AnalysisManager.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.manager;

import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Updater;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_FabricDelta;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_FabricDeltaAnalyzer;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_NodeType;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.infiniband.opensm.plugin.gui.text.OMS_UpdateListenerPanel;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.data.SMT_AnalysisChangeListener;
import gov.llnl.lc.smt.data.SMT_UpdateService;


/**********************************************************************
 * The SMT_AnalysisManager is a one to many analysis event broadcaster.
 * 
 * The analysis events are generated here, based on new OMS updates.  They
 * are then broadcast to all registered listeners, indicating the analysis
 * results are complete.
 * 
 * To use:  Register this Instance as an OMS_ServiceChangeListener with
 *          an SMT_Updater
 *          
 *          Then it will do the analysis for each OMS, and signal the
 *          results are ready to each of IT'S registered analysis listeners.
 * 
 * This is a singleton, which means it is a globally shared object, easily
 * obtainable through the getInstance() method.
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Aug 21, 2013 12:03:33 PM
 **********************************************************************/
public class SMT_AnalysisManager implements SMT_AnalysisUpdater, Runnable, CommonLogger
{
  
  /** the one and only <code>SMT_AnalysisManager</code> Singleton **/
  private volatile static SMT_AnalysisManager gAnalysisMgr  = null;

  /** the synchronization object **/
  private static Boolean semaphore            = new Boolean( true );
  
  /** thread responsible for updating the analysis data **/
  private static java.lang.Thread Update_Analysis_Thread;
  
  /** boolean specifying whether the thread has been created **/
  protected static boolean Thread_Exists = false;

  /** boolean specifying whether the thread should continue **/
  private volatile static boolean Continue_Thread = true;

  /** boolean specifying whether the thread is running **/
  protected static boolean Thread_Running = false;

  /** the most recent delta, from which to analyze **/
  private static OSM_FabricDelta osmFabricDelta = null;
  
  /** the previous Fabric **/
  private static OSM_Fabric oldOsmFabric = null;
  
  /** analyze ALL, Switch to Switch, or just CA ports **/
  private OSM_NodeType IncludedTypes = OSM_NodeType.UNKNOWN;                 // by default, include all ports
  
  /** the analyzer that contains the results **/
  private static OSM_FabricDeltaAnalyzer DeltaAnalysis = null;
  
  /** diagnostic update thread counter **/
  private static int update_counter = 0;
  
  private static SMT_UpdateService smt_UpdateService = null;
  private static OpenSmMonitorService osmService = null;

    
  /************************************************************
   * Method Name:
   *  getDeltaAnalysis
   **/
  /**
   * Returns the value of deltaAnalysis
   *
   * @return the deltaAnalysis
   *
   ***********************************************************/
  
  public synchronized OSM_FabricDeltaAnalyzer getDeltaAnalysis()
  {
    return DeltaAnalysis;
  }

  /** a list of Listeners, interested in knowing when Analysis is complete **/
  private static java.util.ArrayList <SMT_AnalysisChangeListener> Analysis_Listeners =
    new java.util.ArrayList<SMT_AnalysisChangeListener>();

    /** logger for the class **/
    private final java.util.logging.Logger classLogger =
        java.util.logging.Logger.getLogger( getClass().getName() );

 
  /************************************************************
   * Method Name:
   *  SMT_AnalysisManager
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   ***********************************************************/
  private SMT_AnalysisManager()
  {
    super();
    initManager();
  }

    /**************************************************************************
     *** Method Name:
     ***     getInstance
     **/
     /**
     *** Get the singleton SMT_AnalysisManager. This can be used if the application wants
     *** to share one manager across the whole JVM.  Currently I am not sure
     *** how this ought to be used.
     *** <p>
     ***
     *** @return       the GLOBAL (or shared) SMT_AnalysisManager
     **************************************************************************/

     public static SMT_AnalysisManager getInstance()
     {
       synchronized( SMT_AnalysisManager.semaphore )
       {
         if ( gAnalysisMgr == null )
         {
           gAnalysisMgr = new SMT_AnalysisManager( );
         }
         return gAnalysisMgr;
       }
     }
     /*-----------------------------------------------------------------------*/

     public Object clone() throws CloneNotSupportedException
     {
       throw new CloneNotSupportedException(); 
     }
     
     protected boolean initManager()
     {
      createThread();
      startThread();
      return true;     
     }
     
     protected void createThread()
     {
       if (!Thread_Exists)
       {
         update_counter++;
          // set up the thread to do the analysis
         Update_Analysis_Thread = new Thread(this);
         Update_Analysis_Thread.setDaemon(true);
         Update_Analysis_Thread.setName(this.getClass().getSimpleName());
         
         logger.info("Creating the " + Update_Analysis_Thread.getName() + " Thread ");

         Thread_Exists = true;
       }
     }
    
     /************************************************************
     * Method Name:
     *  startThread
    **/
    /**
     * Conditionally creates the analysis thread and starts it.  It won't
     * create a new thread if a previous one is still running.  Normally,
     * this won't happen because the analysis (even on big fabrics) takes
     * only a second or so.
     * 
     * If a new thread does not start because the previous one has not yet
     * finished, it may be due to the TimeSlider being dragged, or the speed
     * multiplier set too high.
     *
     * @see     describe related java objects
     *
     * @return
     ***********************************************************/
    private boolean startThread()
     {
       logger.info("Starting the " + Update_Analysis_Thread.getName() + " Thread");
       Update_Analysis_Thread.start();
       return true;
     }
    
    private void stopThread()
    {
      logger.info("Stopping the " + Update_Analysis_Thread.getName() + " Thread");
      Continue_Thread = false;
    }
    /*-----------------------------------------------------------------------*/


    public void destroy()
    {
      logger.info("Terminating the SMT_AnalysisManager");
      stopThread();
    }

     private boolean isBusy()
     {
       if(Update_Analysis_Thread == null)
         return false;
       return Update_Analysis_Thread.isAlive();
     }
     
    public synchronized int getNumListeners()
     {
       return Analysis_Listeners.size();
     }
      
   /************************************************************
     * Method Name:
     *  getIncludedTypes
     **/
    /**
     * Returns the value of includedTypes
     *
     * @return the includedTypes
     *
     ***********************************************************/
    
    public synchronized OSM_NodeType getIncludedTypes()
    {
      return IncludedTypes;
    }

    /************************************************************
     * Method Name:
     *  setIncludedTypes
     **/
    /**
     * Sets the value of includedTypes which can be either UNKNOWN,
     * meaning analyze ALL ports, SW_NODE meaning only switch to
     * switch ports, or CA_NODE which means only port to or from a
     * channel adapter.
     *
     * @param includedTypes the includedTypes to set
     *
     ***********************************************************/
    public synchronized void setIncludedTypes(OSM_NodeType includedTypes)
    {
      IncludedTypes = includedTypes;
    }

  /************************************************************
   * Method Name:
   *  addSMT_AnalysisChangeListener
   **/
  /**
   * Describe the method here
   *
   * @see gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionUpdater#addSMT_AnalysisChangeListener(gov.llnl.lc.infiniband.opensm.plugin.graph.SMT_AnalysisChangeListener)
   *
   * @param listener
   * @return
   ***********************************************************/

  @Override
  public synchronized boolean addSMT_AnalysisChangeListener(SMT_AnalysisChangeListener listener)
  {
    // add the listener, and its set of events
    classLogger.info("adding analysis listener");
    if(listener != null)
    {
      Analysis_Listeners.add(listener);
      
      // attempt to provide the listener with its first set of data
      if(DeltaAnalysis != null)
      {
        try
        {
          listener.smtAnalysisUpdate(this);
        }
        catch (Exception e)
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      else
        logger.severe("could not provide delta analysis to newly added listener");

    }
    return true;
  }

  /************************************************************
   * Method Name:
   *  removeSMT_AnalysisChangeListener
   **/
  /**
   * Describe the method here
   *
   * @see gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionUpdater#removeSMT_AnalysisChangeListener(gov.llnl.lc.infiniband.opensm.plugin.graph.SMT_AnalysisChangeListener)
   *
   * @param listener
   * @return
   ***********************************************************/

  @Override
  public synchronized boolean removeSMT_AnalysisChangeListener(SMT_AnalysisChangeListener listener)
  {
    classLogger.info("removing analysis listener");
    if (Analysis_Listeners.remove(listener))
    {
     }
    return true;
  }

  /**************************************************************************
  *** Method Name:
  ***     updateAllListeners
  ***
  **/
  /**
  *** Notifies all listeners that some event has occurred.
  *** <p>
  ***
  **************************************************************************/
  public synchronized void updateAllListeners()
  {
    logger.severe("Updating " + getNumListeners() + " Listeners NOW!");

    for( int i = 0; i < Analysis_Listeners.size(); i++ )
    {
      SMT_AnalysisChangeListener listener = (SMT_AnalysisChangeListener)Analysis_Listeners.get( i );
      try
      {
        if(listener != null)
        {
          logger.severe("Updating  SMT Analysis Listener: " + listener.getClass().getCanonicalName());
          listener.smtAnalysisUpdate(this);
          logger.severe("Done Updating Analysis Listener: " + listener.getClass().getCanonicalName());          
        }
      }
      catch (Exception e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
  /**************************************************************************
  *** Method Name:
  ***     notifyAllListeners
  ***
  **/
  /**
  *** Notifies all listeners that some event has occurred.
  *** <p>
   * @throws InterruptedException 
  ***
  **************************************************************************/
  private synchronized void notifyAllListeners() throws InterruptedException
  {
    // block here, until released by notify() in osmServiceUpdate()
    wait();

    if (doAnalysis())
    {
      if (getNumListeners() > 0)
      {
        // skip analysis if the only listener is the watchdog panel
        if ((DeltaAnalysis == null) || (getNumListeners() != 1) || !(Analysis_Listeners.get(0) instanceof OMS_UpdateListenerPanel))
        {
          DeltaAnalysis = new OSM_FabricDeltaAnalyzer(osmFabricDelta, IncludedTypes);
        }
        // always notify the listeners, (even if I skipped the analysis)
        updateAllListeners();
      }
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
  public static void main(String[] args)
  {
    // TODO Auto-generated method stub

  }

  @Override
  public String getName()
  {
    return this.getClass().getSimpleName();
  }

  @Override
  public synchronized void osmServiceUpdate(OMS_Updater updater, OpenSmMonitorService osmService) throws Exception
  {
    // This Manager should be registered as a listener for ServiceUpdates ( the SMT_UpdateService)
    //
    // do the analysis here, then send out updates to all Analysis listeners
    
        // if the updater is an SMT_Updater, skip analysis, already done
        if(updater instanceof SMT_UpdateService)
        {
          // save the updater and the OMS, then trigger the worker thread
           SMT_AnalysisManager.osmService = osmService;
           SMT_AnalysisManager.smt_UpdateService = ((SMT_UpdateService)updater);
           // this triggers the waiting thread, telling it to do the analysis and broadcast results
           notify();
        }
        else
        {
          logger.severe("The updater for the analysis is not correct, can't inform listeners");
        }
  }

  /************************************************************
   * Method Name:
   *  doAnalysis
  **/
  /**
   * Returns true, if a new analysis can be performed, and false
   * otherwise.  This is just a test, it does NOT do the analysis.
   * 
   * An analysis can only be performed if there is a fresh OSM_FabricDelta,
   * which means that the new fabric (obtained from the service) is different
   * from the old fabric, so that a new OSM_FabricDelta can be created.
   *
   * @see     describe related java objects
   *
   * @param smt_UpdateService
   * @param osmService
   * @return
   ***********************************************************/
  private boolean doAnalysis()
  {
    logger.severe("doing SMT Analysis now");
    if(osmService != null)
    {
      OSM_Fabric fab = osmService.getFabric();
      
      // is this different than what I already have?
      if((oldOsmFabric == null) || (osmFabricDelta == null))
      {
        logger.severe("History still has null objects");
        
        // starting fresh, build up my local history
        if(osmFabricDelta == null)
        {
          logger.severe("The Delta is null");
          if(oldOsmFabric == null)
          {
            oldOsmFabric = fab;
            logger.severe("Got the Initial Fabric");
            return false;
          }
          
          // is this different from the existing Fabric
          if(oldOsmFabric.getTimeStamp().equals(fab.getTimeStamp()))
          {
            logger.severe("This Fabric seems to have the same timestamp as the old one");
             return false;
          }
          
          osmFabricDelta = new OSM_FabricDelta(oldOsmFabric, fab);
          logger.severe("Created an initial Delta for analysis");
            
        }
        else
          logger.severe("Have a Delta, but not a Fabric?  This should NEVER happen");
      }
      else
      {
        // I have some existing data, is this new snapshot different?
        if(osmFabricDelta.getFabric2().getTimeStamp().equals(fab.getTimeStamp()))
        {
          logger.severe("The new timestamp matches that on the old delta, nothing to do");
         return false;
        }
        
        oldOsmFabric    = osmFabricDelta.getFabric1();
        OSM_Fabric fab1 = osmFabricDelta.getFabric2();
        osmFabricDelta  = new OSM_FabricDelta(fab1, fab);
        logger.severe("Created new (replacement) Delta for analysis");
       }
      
      // if I have fallen through to here, I should have a new delta
      if(osmFabricDelta != null)
        return true;
      else
      {
        logger.severe("Expected a new Delta!");
        return false;
      }
    }
    else
      logger.severe("Analyzer updated with NULL data");
    return false;
   }

  @Override
  public void osmFabricUpdate(OSM_Fabric osmFabric) throws Exception
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public synchronized void run()
  {
    // check the Thread Termination Flag, and continue if Okay
    while(Continue_Thread)
    {
        try
        {
          // this blocks until a new osmServiceUpdate occurs
          notifyAllListeners();
        }
        catch (InterruptedException e)
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
    }
    logger.info("Exiting the " + Update_Analysis_Thread.getName() + " Thread");
    Thread_Exists = false;
  }


}
