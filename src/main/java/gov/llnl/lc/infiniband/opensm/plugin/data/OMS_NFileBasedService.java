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
 *        file: OMS_NFileBasedService.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.data;

import gov.llnl.lc.smt.command.SmtCommand;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class OMS_NFileBasedService extends OMS_AbstractUpdateService
{
  /** the one and only <code>OMS_NFileBasedService</code> Singleton **/
  private volatile static OMS_NFileBasedService globalUpdateService  = null;

  private static volatile OMS_Collection omsHistory = null;

  private OMS_NFileBasedService()
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

 public static OMS_NFileBasedService getInstance()
 {
   synchronized( OMS_NFileBasedService.semaphore )
   {
     if ( globalUpdateService == null )
     {
       globalUpdateService = new OMS_NFileBasedService( );
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
    
    // open the OMS file if possible
    String fn = SmtCommand.convertSpecialFileName(getFile());
    try
   {
     omsHistory = OMS_Collection.readOMS_Collection(fn);
   }
   catch (FileNotFoundException e)
   {
     logger.severe("Couldn't open the Collection file: " + fn);
     System.err.println("Couldn't open the Collection file: " + fn);
     e.printStackTrace();
   }
   catch (IOException e)
   {
     logger.severe("IOException reading the Collection file: " + fn);
     e.printStackTrace();
   }
   catch (ClassNotFoundException e)
   {
     logger.severe("ClassNotFoundException reading the Collection file: " + fn);
     e.printStackTrace();
   }
    logger.info("Done reading the Collection file: " + fn);

    OpenSmMonitorService oms = null;
    if(omsHistory != null)
    {
      oms = omsHistory.getOldestOMS();
      this.setOMS(oms);
      
      // the list will be set-up in run()
     }

    // open the Config file if possible
    // try a specified config file
    String cfn = SmtCommand.convertSpecialFileName(getConfigFile());
    if(cfn == null)
    {
      logger.warning("Configuration file not specified");
      
       if((oms != null) && (oms.getFabricName() != null))
      {
        cfn  = OSM_Configuration.getCacheFileName(oms.getFabricName());
        logger.info("Trying cache file (" + cfn + ")");
      }
    }

   try
   {
     setOsmConfig(OSM_Configuration.readConfig(cfn));
   }
   catch (FileNotFoundException e)
   {
     logger.severe("Couldn't open the Configuration file: " + cfn);
     System.err.println("Couldn't open the Configuration file: " + cfn);
     e.printStackTrace();
   }
   catch (IOException e)
   {
     logger.severe("IOException reading the Configuration file: " + cfn);
     e.printStackTrace();
   }
    catch (ClassNotFoundException e)
    {
      logger.severe("ClassNotFoundException reading the Configuration file: " + cfn);
      e.printStackTrace();
    }
    catch (Exception e)
    {
      logger.severe("Exception reading the Configuration file: " + cfn);
      e.printStackTrace();
    }
    logger.info("Done reading the Configuration file: " + cfn);

    
    // don't start the thread until after the file has been read
    Listener_Thread.start();
    return true;
  }
  /*-----------------------------------------------------------------------*/


 @Override
 public void run()
 {
   // the history should be in memory
   int num = omsHistory.getSize();
   int ndex = 0;
   osmService = omsHistory.getOMS(ndex++);
   omsList    = omsHistory.getOldestOMS_List();
   logger.severe("updating listeners now");
   long updatePeriod = osmService.getFabric().getPerfMgrSweepSecs();
   updatePeriod = updatePeriod/getUpdateMultiplier();
   updatePeriod = updatePeriod > 1L ? updatePeriod: 2L;
   logger.severe("Perf Sweep Secs    : " + osmService.getFabric().getPerfMgrSweepSecs());
   logger.severe("Multiplier is      : " + getUpdateMultiplier());
   logger.severe("Update period  secs: " + updatePeriod);
   
   Thread_Running = true;
   
   // check the Thread Termination Flag, and continue if Okay
   while(Continue_Thread)
   {
     try
     {
       updateAllListeners();
       TimeUnit.SECONDS.sleep(updatePeriod);
      }
     catch (Exception e)
     {
       // nothing to do yet
       logger.severe("Crap, couldn't get a new service instance");
     }
     // advance to the next instance of the collection
     osmService = omsHistory.getOMS(ndex++);
     if(ndex >= omsHistory.getSize())
     {
       if(isWrapData())
         ndex = 0;
       else
         ndex = omsHistory.getSize() -1;
     }
   }
     
   logger.info("Terminating the " + Listener_Thread.getName() + " Thread");
   
   /* fall through, must be done! */
   Thread_Running = false;
 }

}
