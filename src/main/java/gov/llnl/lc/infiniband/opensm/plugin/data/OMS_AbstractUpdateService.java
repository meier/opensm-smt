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
 *        file: OMS_AbstractUpdateService.java
 *
 *  Created on: Aug 28, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.data;

import gov.llnl.lc.infiniband.opensm.plugin.net.OsmSession;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.SmtConstants;
import gov.llnl.lc.smt.command.SmtCommandType;
import gov.llnl.lc.smt.props.SmtProperty;
import gov.llnl.lc.time.TimeStamp;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**********************************************************************
 * Describe purpose and responsibility of OMS_AbstractUpdateService
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Aug 28, 2013 8:24:22 AM
 **********************************************************************/
public abstract class OMS_AbstractUpdateService implements OMS_Updater, Runnable, CommonLogger
{
  /** the synchronization object **/
  protected static Boolean semaphore            = new Boolean( true );

  /** the one and only <code>OMS_AbstractUpdateService</code> Singleton **/
  private volatile static OMS_AbstractUpdateService globalUpdateService  = null;

  /** boolean specifying whether the thread should continue **/
  protected volatile static boolean Continue_Thread = true;
  
  /** boolean specifying whether the thread has been created **/
  protected static boolean Thread_Exists = false;

  /** boolean specifying whether the thread is running **/
  protected static boolean Thread_Running = false;

  /** thread responsible for getting new server instances **/
  protected static java.lang.Thread Listener_Thread;
  
  /** the creation date/time of this Updater */
  private static TimeStamp UpTime = null;
  
  /** configuration or initialization properties */
  protected static Properties serviceProps = null;
  
  /** an OMS session for obtaining all the information **/
  protected static OsmSession ParentSession = null;
  
  /** the instance of OpenSmMonitoringService used for analysis (see setOMS() **/
  protected static volatile OpenSmMonitorService osmService = null;
  
  /** the instance of OpenSmMonitoringService cache (size of two) **/
  protected static volatile OMS_List omsList = null;
  
  /** the ibfabricconf and node-name-map files **/
  protected static volatile OSM_Configuration osmConfig = null;
  
  private static String Host = "localhost";
  
  private static String Port = "10011";
  
  private static boolean reuseConnection = false;
  

  /** a list of Listeners, interested in knowing when new service info available **/
  protected static java.util.ArrayList <OSM_ServiceChangeListener> Service_Listeners =
    new java.util.ArrayList<OSM_ServiceChangeListener>();

    /**************************************************************************
     *** Method Name:
     ***     createThread
     **/
     /**
     *** Summary_Description_Of_What_createThread_Does.
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

     protected void createThread()
     {
       if (!Thread_Exists)
       {
         UpTime = new TimeStamp();

         // set up the thread to listen
         Listener_Thread = new Thread(this);
         Listener_Thread.setDaemon(true);
         Listener_Thread.setName(this.getClass().getSimpleName());
         
         logger.info("Creating the " + Listener_Thread.getName() + " Thread");

         Thread_Exists = true;
       }
     }

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

     protected abstract boolean startThread();
//     {
//       logger.info("Starting the " + Listener_Thread.getName() + " Thread");
//       Listener_Thread.start();
//       return true;
//     }
     /*-----------------------------------------------------------------------*/

     /**************************************************************************
      *** Method Name:
      ***     stopThread
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

      private void stopThread()
      {
        logger.info("Stopping the " + Listener_Thread.getName() + " Thread");
        Continue_Thread = false;
      }
      /*-----------------------------------------------------------------------*/


      public void destroy()
      {
        logger.info("Destroying the " + Listener_Thread.getName() + " Thread");
        stopThread();
      }
      

    public Object clone() throws CloneNotSupportedException
    {
      throw new CloneNotSupportedException(); 
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
    protected synchronized void updateAllListeners() throws Exception
   {
     
     for(OSM_ServiceChangeListener listener: Service_Listeners)
     {
       if(listener != null)
        listener.osmServiceUpdate( this, osmService);
     }
   }
   /*-----------------------------------------------------------------------*/


  /************************************************************
   * Method Name:
   *  addListener
  **/
  /**
   * Add the provided listener to the list of subscribers interested
   * in being notified of time changes.  
   *
   * @param listener
   ***********************************************************/
  
  @Override
  public synchronized void addListener(OSM_ServiceChangeListener listener)
  {
    // add the listener, and its set of events
    if(listener != null)
    {
      Service_Listeners.add(listener);
      
      // immediately give this listener a service, so it doesn't have to wait
      // for the next update period, which can be considerable.
      try
      {
        if(osmService != null)
        {
          listener.osmServiceUpdate(this, osmService);
        }
        else
        {
          logger.severe("could not provide service update to newly added listener (because the service is null)");
        }
      }
      catch (Exception e)
      {
        // TODO Auto-generated catch bloc
        logger.severe("error attempting to provide service update to newly added listener");
      }
      
    }
  }

  /************************************************************
   * Method Name:
   *  removeListener
  **/
  /**
   * Describe the method here
   *
   * @see gov.llnl.lc.infiniband.opensm.plugin.event.OsmEventUpdater#removeListener(gov.llnl.lc.infiniband.opensm.plugin.event.OsmEventListener)
  
   * @param   describe the parameters
   *
   * @return  describe the value returned
   * @param listener
   * @return
   ***********************************************************/
  
  @Override
  public synchronized boolean removeListener(OSM_ServiceChangeListener listener)
  {
    if (Service_Listeners.remove(listener))
    {
     }
    return true;
  }

  public synchronized long getUpdateMultiplier()
  {
    String sVal = serviceProps.getProperty(SmtProperty.SMT_UPDATE_MULTIPLIER.name());
    if(sVal == null)
      return 300L;
    Long lVal = new Long(sVal);
    return lVal.longValue();
  }

  public synchronized long getUpdatePeriod()
  {
    // this will have a minimum value of 2
    //
    // for off-line, this value is the fabrics update period
    // divided by the multiplier
    if(osmService != null)
    {
      int period = osmService.getFabric().getPerfMgrSweepSecs();
      long m = getUpdateMultiplier();
      long val = period/m;
      return val > 1L ? val: 2L;
    }
    return 1L;
  }
  
  public synchronized int getNumListeners()
  {
    return Service_Listeners.size();
  }
   
  public synchronized OpenSmMonitorService getOMS()
  {
    return osmService;
  }
   
  public synchronized OMS_List getOMS_List()
  {
    return omsList;
  }
   
  public synchronized OSM_Configuration getOsmConfig()
  {
    return osmConfig;
  }
   
  public String getName()
  {
    return this.getClass().getSimpleName();
  }

  public synchronized String getCommand()
  {
    // returns the simple string known to users, not the command class name
    return serviceProps.getProperty(SmtProperty.SMT_COMMAND.name());
  }
  
  public synchronized String getToolName()
  {
    // returns the simple string known to users, not the command class name
    String command = serviceProps.getProperty(SmtProperty.SMT_COMMAND.name());
    String tool = "unknown tool";
    SmtCommandType type = null;
    if(command != null)
      type = SmtCommandType.getByCommandName(getCommand());
    if(type != null)
      tool = type.getToolName();
    return tool;
  }
  
  public synchronized String getHost()
  {
    return serviceProps.getProperty(SmtProperty.SMT_HOST.name());
  }
  
  public synchronized String getPort()
  {
    return serviceProps.getProperty(SmtProperty.SMT_PORT.name());
  }
  
  public synchronized String getFile()
  {
    return serviceProps.getProperty(SmtProperty.SMT_OMS_FILE.name());
  }
  
  public synchronized String getOMS_Source()
  {
    /** return the file name, or the port number **/
    String method = serviceProps.getProperty(SmtProperty.SMT_OMS_FILE.name());
    if(SmtConstants.SMT_NO_FILE.equals(method))
      method = getHost() + ":" + getPort();
    return method;
  }
  
  public synchronized String getConfigFile()
  {
    return serviceProps.getProperty(SmtProperty.SMT_OMS_FILE.name() + ".cfg");
  }
  
  public boolean isWrapData()
  {
    String sVal = serviceProps.getProperty(SmtProperty.SMT_WRAP_DATA.name());
    if(sVal == null)
      return false;
    Boolean bVal = new Boolean(sVal);
    return bVal.booleanValue();
  } 
  
  public synchronized void setHost(String host)
  {
    Host = host;
  }

  protected synchronized void setOsmConfig(OSM_Configuration config)
  {
    osmConfig = config;
  }

  protected synchronized void setOMS(OpenSmMonitorService oms)
  {
    osmService = oms;
  }

  public synchronized void setOMS_List(OMS_List oms)
  {
    omsList = oms;
    setOMS(oms.getCurrentOMS());
  }

  public synchronized void setPort(String port)
  {
    Port = port;
  }

  public void setReuseConnection(boolean reuse)
  {
    reuseConnection = reuse;
  }

  @Override
  public boolean isConnectionReused()
  {
    String sVal = serviceProps.getProperty(SmtProperty.SMT_REUSE.name());
    if(sVal == null)
      return false;
    Boolean bVal = new Boolean(sVal);
    return bVal.booleanValue();
  }

  @Override
  public TimeStamp getUpTime()
  {
    return UpTime;
  }

  @Override
  public synchronized boolean init(Properties properties)
  {
    if(properties != null)
    {
      // look for properties I may care about, and initialize myself
      serviceProps = properties;
      
      // read the file, and start the listener thread if not already running
      if(!Thread_Running)
        startThread();
      
      // TODO:  Move the file reading code
     }
    return false;
  }

  @Override
  public synchronized Properties getProperties()
  {
    return serviceProps;
  }

  @Override
  public boolean isReady()
  {
    return Thread_Running;
  }
  
  @Override
  public boolean waitUntilReady() throws InterruptedException
  {
    for(int count = 0; (!isReady() && count < 50); count++)
      TimeUnit.MILLISECONDS.sleep(100);

    return isReady();
  }
}
