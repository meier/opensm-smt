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
 *        file: HeartBeatManager.java
 *
 *  Created on: Sep 30, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.manager;

import java.util.HashMap;

import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Updater;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_ServiceChangeListener;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.data.SMT_AbstractUpdateService;
import gov.llnl.lc.smt.event.SmtHeartBeat;
import gov.llnl.lc.smt.event.SmtHeartBeatListener;
import gov.llnl.lc.smt.event.SmtMessage;
import gov.llnl.lc.smt.event.SmtMessageType;
import gov.llnl.lc.time.TimeStamp;

/**********************************************************************
 * Describe purpose and responsibility of HeartBeatManager
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Sep 30, 2013 12:09:58 PM
 **********************************************************************/
public class HeartBeatManager implements SmtHeartBeatUpdater, OSM_ServiceChangeListener, CommonLogger
{

  /** the one and only <code>HeartBeatManager</code> Singleton **/
  private volatile static HeartBeatManager                 gHeartBeatMgr       = null;

  /** the synchronization object **/
  private static Boolean                                 semaphore         = new Boolean(true);

  /** the previous HeartBeat object, if any **/
  private static SmtHeartBeat                                 prevHB       = null;
  
  private long prevPerfMgrTimeInSecs = 0L;

  /** a list of Listeners, interested in knowing when Message Events happened **/
  private static java.util.ArrayList<SmtHeartBeatListener> HeartBeatListener = new java.util.ArrayList<SmtHeartBeatListener>();

  /** logger for the class **/
  private final java.util.logging.Logger                 classLogger       = java.util.logging.Logger.getLogger(getClass().getName());

  /************************************************************
   * Method Name: HeartBeatManager
   **/
  /**
   * Describe the constructor here
   * 
   * @see describe related java objects
   * 
   ***********************************************************/
  private HeartBeatManager()
  {
    super();
    initManager();
  }

  /**************************************************************************
   *** Method Name: getInstance
   **/
  /**
   *** Get the singleton MessageManager. This can be used if the application wants
   * to share one manager across the whole JVM. Currently I am not sure how this
   * ought to be used.
   *** <p>
   *** 
   *** @return the GLOBAL (or shared) MessageManager
   **************************************************************************/

  public static HeartBeatManager getInstance()
  {
    synchronized (HeartBeatManager.semaphore)
    {
      if (gHeartBeatMgr == null)
      {
        gHeartBeatMgr = new HeartBeatManager();
      }
      return gHeartBeatMgr;
    }
  }

  /*-----------------------------------------------------------------------*/

  public Object clone() throws CloneNotSupportedException
  {
    throw new CloneNotSupportedException();
  }

  protected boolean initManager()
  {
    return true;
  }

  public String getName()
  {
    return this.getClass().getSimpleName();
  }

  public synchronized int getNumListeners()
  {
    return HeartBeatListener.size();
  }
   
  @Override
  public synchronized boolean addHeartBeatListener(SmtHeartBeatListener listener)
  {
    classLogger.info("adding message listener");
    if (listener != null)
    {
      HeartBeatListener.add(listener);
    }
    return true;
  }

  @Override
  public synchronized boolean removeHeartBeatListener(SmtHeartBeatListener listener)
  {
    classLogger.info("removing message listener");
    if (HeartBeatListener.remove(listener))
    {
    }
    return true;
  }

  @Override
  public synchronized void updateAllListeners(SmtHeartBeat heartBeat)
  {
    for (int i = 0; i < HeartBeatListener.size(); i++)
    {
      SmtHeartBeatListener listener = (SmtHeartBeatListener) HeartBeatListener.get(i);
      if(listener != null)
        listener.heartBeatUpdate(this, heartBeat);
    }
    prevHB = heartBeat;
  }

  @Override
  public void timeUpdate(TimeStamp time)
  {
    // new time has arrived, so create a new event and broadcast it
    SmtHeartBeat hb = null;
    if(prevHB == null)
    {
      hb = new SmtHeartBeat(time, 30, 180);
    }
    else
    {
      // secDiff should almost always be 1, but calculate it just in case
      long secDiff = time.getTimeInSeconds() - prevHB.getTime().getTimeInSeconds();
      long numOMS = prevHB.getNumSecsToOMS() - secDiff;
      long numPFM = prevHB.getNumSecsToPerfMgr() - secDiff;
      hb = new SmtHeartBeat(time, (int)numOMS, (int)numPFM);
    }
    updateAllListeners(hb);
  }

  @Override
  public void osmFabricUpdate(OSM_Fabric osmFabric) throws Exception
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void osmServiceUpdate(OMS_Updater updater, OpenSmMonitorService osmService) throws Exception
  {
    // new OMS has arrived, so get the vital pieces of info, and construct an HB
    TimeStamp time = new TimeStamp();
    
    // some defaults, in case I can't figure things out
    int pSweep = 180;
    int oSweep = 30;
    boolean isFileMode = false;   // reading from a file, or a connection?
    
    if((osmService != null) && (osmService.getFabric() != null) && (osmService.getFabric().getOptions() != null))
    {
      HashMap<String, String> OptionsMap = osmService.getFabric().getOptions();
      String pVal = OptionsMap.get("perfmgr_sweep_time_s");
      if(pVal != null)
        pSweep = Integer.parseInt(pVal);
      
      if(updater != null)
      {
        updater.getName();
        if(updater instanceof SMT_AbstractUpdateService)
        {
          isFileMode = ((SMT_AbstractUpdateService)updater).isFileMode();
        }
        oSweep = (int)updater.getUpdatePeriod();
      } 
    }
    else
    {
      // something is wrong, post a severe message
      MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_SEVERE, "Did NOT get a snapshot of the Fabric, check connection or the service", this));
    }
    
    // new time has arrived, so create a new event and broadcast it
    SmtHeartBeat hb = null;
    if(prevHB == null)
    {
      hb = new SmtHeartBeat(time, oSweep, pSweep);
      this.prevPerfMgrTimeInSecs = time.getTimeInSeconds();
    }
    else
    {
      // assume I just updated from the Service, so reset OMS counter to full remaining time
      long numOMS = oSweep;
      
      // time remaining before perf mgr updates
      long numPFM = prevHB.getNumSecsToPerfMgr();

      if(this.prevPerfMgrTimeInSecs != osmService.getTimeStamp().getTimeInSeconds())
      {
        // looks like a new Perf update, so reset PerfMgr counter to full remaining time
        numPFM = pSweep;
        this.prevPerfMgrTimeInSecs = osmService.getTimeStamp().getTimeInSeconds();
      }
      long secDiff = time.getTimeInSeconds() - osmService.getTimeStamp().getTimeInSeconds();
      
      // if the data is stale, and not from a file, post an error message
      if((secDiff > 360) && !isFileMode)
      {
        MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_SEVERE, "Stale Perf Manager time, see Hearbeat Watchdog", this));
      }
      hb = new SmtHeartBeat(time, (int)numOMS, (int)numPFM);
    }
    updateAllListeners(hb);
    
  }


}
