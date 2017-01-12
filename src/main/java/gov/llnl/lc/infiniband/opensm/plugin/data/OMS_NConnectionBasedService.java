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
 *        file: OMS_NConnectionBasedService.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.data;

import java.util.concurrent.TimeUnit;

import gov.llnl.lc.infiniband.opensm.plugin.net.OsmServiceManager;
import gov.llnl.lc.smt.props.SmtProperty;

public class OMS_NConnectionBasedService extends OMS_AbstractUpdateService
{
  /** the one and only <code>OMS_NConnectionBasedService</code> Singleton **/
  private volatile static OMS_NConnectionBasedService globalUpdateService  = null;


  private OMS_NConnectionBasedService()
  {
     createThread();  // doesn't start thread, see init() in AbstractService
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

  public static OMS_NConnectionBasedService getInstance()
  {
    synchronized( OMS_NConnectionBasedService.semaphore )
    {
      if ( globalUpdateService == null )
      {
        globalUpdateService = new OMS_NConnectionBasedService( );
      }
      return globalUpdateService;
    }
  }
  /*-----------------------------------------------------------------------*/

  
  @Override
  public synchronized long getUpdatePeriod()
  {
    String sVal = serviceProps.getProperty(SmtProperty.SMT_UPDATE_PERIOD.name());
    if(sVal == null)
      return 300L;
    Long lVal = new Long(sVal);
    return lVal.longValue();
  }

  @Override
  public void run()
  {
    Thread_Running = true;
    
    // check the Thread Termination Flag, and continue if Okay
    while(Continue_Thread)
    {
      try
      {
        updateAllListeners();
        TimeUnit.SECONDS.sleep(getUpdatePeriod());
       }
      catch (Exception e)
      {
        // nothing to do yet
        logger.severe("Crap, couldn't update listener with new OMS, check if listener died");
        logger.severe(e.getMessage());
       }
    }
    
    if((isConnectionReused()) && (ParentSession != null))
    {
      try
      {
        OsmServiceManager.getInstance().closeSession(ParentSession);
      }
      catch (Exception e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    Thread_Running = false;
      
    logger.info("Terminating the " + Listener_Thread.getName() + " Thread");
    /* fall through, must be done! */
  }

  @Override
  protected boolean startThread()
  {
    logger.info("Starting the " + Listener_Thread.getName() + " Thread");
    Listener_Thread.start();
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
  @Override
  protected synchronized void updateAllListeners() throws Exception
   {
     if(isConnectionReused())
     {
       if(ParentSession == null)
       {
         /* the one and only OsmServiceManager */
         OsmServiceManager OsmService = OsmServiceManager.getInstance();

         try
         {
           logger.info("OMS_NCBS: Opening the OMS Session");
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
         setOMS( OpenSmMonitorService.getOpenSmMonitorService(ParentSession));
         if((omsList == null) || (omsList.size() < 2))
         {
           // update the List directly from the service
           setOMS_List(OpenSmMonitorService.getOMS_List(ParentSession));
         }
         else
         {
           // update the local List from osmService I just obtained
           omsList.putCurrentOMS(getOMS());
         }
         for(OSM_ServiceChangeListener listener: Service_Listeners)
         {
           if(listener != null)
             listener.osmServiceUpdate(this, osmService);
         }
         
         // the config is relatively static, so just update/create it once
         if(osmConfig == null)
           setOsmConfig(OSM_Configuration.getOsmConfig(ParentSession));
         
       }
       else
       {
         logger.severe("Could not establish an OMS session");
       }
     }
     else
     {
       if((omsList == null) || (omsList.size() < 2))
       {
         // update the List directly from the service
         setOMS_List(OpenSmMonitorService.getOMS_List(getHost(), getPort()));
         setOMS(omsList.getCurrentOMS());
       }
       else
       {
         // update the local List from osmService I just obtained
         setOMS(OpenSmMonitorService.getOpenSmMonitorService(getHost(), getPort()));
         omsList.putCurrentOMS(getOMS());
       }
       for(OSM_ServiceChangeListener listener: Service_Listeners)
       {
         if(listener != null)
           listener.osmServiceUpdate(this, osmService);
       }

       
       // the config is relatively static, so just update/create it once
       if(osmConfig == null)
         setOsmConfig(OSM_Configuration.getOsmConfig(getHost(), getPort()));

     }
   }
   /*-----------------------------------------------------------------------*/


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

}
