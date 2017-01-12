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
 *        file: SMT_RouteManager.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.manager;

import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Updater;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_ServiceChangeListener;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.infiniband.opensm.plugin.data.RT_Path;
import gov.llnl.lc.infiniband.opensm.plugin.data.RT_Table;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionEvent;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;
import gov.llnl.lc.infiniband.opensm.plugin.gui.graph.SimpleGraphPopupMenu;
import gov.llnl.lc.infiniband.opensm.plugin.gui.tree.VertexTreePopupMenu;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.event.SmtMessage;
import gov.llnl.lc.smt.event.SmtMessageType;


/**********************************************************************
 * The SMT_RouteManager
 * 
 * This is a singleton, which means it is a globally shared object, easily
 * obtainable through the getInstance() method.
 * 
 * Currently, routing information is not widely available.  The route and 
 * path functionality throughout the SMT commands will be accessed and
 * managed through this global manager.  Its responsibilities include;
 * 
 * maintaining the routing table
 * maintaining one instance of the fabric (for name mapping and such)
 * maintaining a global popup for constructing a path
 * maintaining current, scratch, and previous paths
 * providing decoration instructions for current path
 * 
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Aug 21, 2013 12:03:33 PM
 **********************************************************************/
public class SMT_RouteManager implements CommonLogger, OSM_ServiceChangeListener
{
  
  /** the one and only <code>SMT_RouteManager</code> Singleton **/
  private volatile static SMT_RouteManager gRouteMgr  = null;

  /** the synchronization object **/
  private static Boolean semaphore            = new Boolean( true );
    
    /** logger for the class **/
    private final java.util.logging.Logger classLogger =
        java.util.logging.Logger.getLogger( getClass().getName() );
    

    private static SimpleGraphPopupMenu routePopup = new SimpleGraphPopupMenu();
    private static VertexTreePopupMenu vertexTreePopup = new VertexTreePopupMenu();

    private RT_Path currentPath;
    private RT_Path previousPath;
    private IB_Guid scratchSource;
    private IB_Guid scratchDestination;
    
    /** a collection of switch route tables (optionally exists) **/
    private RT_Table Table = null;
    
    private OSM_Fabric Fabric;
    private boolean showNodes = true;
    
  /************************************************************
   * Method Name:
   *  SMT_RouteManager
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   ***********************************************************/
  private SMT_RouteManager()
  {
    super();
    initManager();
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

     public static SMT_RouteManager getInstance()
     {
       synchronized( SMT_RouteManager.semaphore )
       {
         if ( gRouteMgr == null )
         {
           gRouteMgr = new SMT_RouteManager( );
         }
         return gRouteMgr;
       }
     }
     /*-----------------------------------------------------------------------*/

     public Object clone() throws CloneNotSupportedException
     {
       throw new CloneNotSupportedException(); 
     }
     
     protected boolean initManager()
     {
       // need to obtain a table and fabric
       
       
      return true;     
     }
     
     
     public boolean setOMS(OpenSmMonitorService oms)
     {
       boolean success = false;
       if(oms != null)
       {
         Fabric = oms.getFabric();
         Table = getNewRT_Table(oms);
         success = true;
       }
       return success;
     }

     private RT_Table getNewRT_Table(OpenSmMonitorService oms)
     {
       if(oms != null)
       {
         OSM_Fabric fab = oms.getFabric();
         if(fab != null)
           return RT_Table.buildRT_Table(fab);
       }
       logger.severe("Could not build Routing Tables from null objects");
       MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_SEVERE, "Could not build Routing Tables from null objects"));
       return null;
     }

     private RT_Table getNewRT_Table1(OpenSmMonitorService oms)
     {
       // this uses a routing table file, which is basically a file that contains all of the switches routing tables
       // this is how it used to be done, before the LFTs were included in the SBN_Switch object.
       //
       // keep this around, because reading and writing to a file may be useful
       //
       String filePath = RT_Table.getCacheFileName(oms.getFabricName());
       MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "  Attempting to read the routing tables (" + filePath + ")"));

       RT_Table table = null;

       // currently, just read it from a file, whos name and location are
       // predetermined
       try
       {
         table = RT_Table.readRT_Table(filePath);
       }
       catch (Exception e)
       {
         logger.severe("Read Table exception: " + e.getMessage());
         MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_SEVERE, "Read Table exception: " + e.getMessage()));
       }
       MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "  Done reading the routing tables"));

       return table;
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

  public String getName()
  {
    return this.getClass().getSimpleName();
  }

  public void setScratchSource(IB_Guid guid)
  {
    if(guid != null)
      scratchSource = guid;
  }

  public SimpleGraphPopupMenu getSimpleGraphPopupMenu(IB_Vertex iv)
  {
    routePopup.setVertex(iv);
    return routePopup;
  }

  public VertexTreePopupMenu getVertexTreePopupMenu(IB_Vertex iv)
  {
    vertexTreePopup.setVertex(iv);
    return vertexTreePopup;
  }

  public void setScratchDestination(IB_Guid guid)
  {
    if(guid != null)
      scratchDestination = guid;
  }

  public boolean showPathTree()
  {
    // signal the application to add an RT_PathTree to the gui
    //System.err.println("Attempting to display the tree for path: " + currentPath.toString());
    // this came from me, so I can handle it
   //  craft a selection event that contains necessary info for this routing event
    GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(this, SMT_AnalysisType.SMT_ROUTE_PATH, currentPath));
    return false;
  }

  public boolean makeScratchCurrent()
  {
    // if legal, make a path out of scratch
    // then move current into previous
    // and scratch into current
    //   leave scratch, as scratch
    boolean success = false;
    
    if((scratchSource != null) && (scratchDestination != null))
    {
      // they also must be different
      if(scratchSource.equals(scratchDestination))
      {
        logger.severe("Source and destination ports must be different");
        MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_SEVERE, "Source and destination ports must be different"));
        return false;
      }
      RT_Path tmpPath = new RT_Path(scratchSource, scratchDestination, Table, Fabric);
      if(tmpPath != null)
      {
        previousPath = currentPath;
        currentPath  = tmpPath;
        success = true;
      }
    }
    return success;
  }

  public RT_Table getRouteTable()
  {
    return Table;
  }

  /************************************************************
   * Method Name:
   *  getCurrentPath
   **/
  /**
   * Returns the value of currentPath
   *
   * @return the currentPath
   *
   ***********************************************************/
  
  public RT_Path getCurrentPath()
  {
    return currentPath;
  }

  /************************************************************
   * Method Name:
   *  getPreviousPath
   **/
  /**
   * Returns the value of previousPath
   *
   * @return the previousPath
   *
   ***********************************************************/
  
  public RT_Path getPreviousPath()
  {
    return previousPath;
  }

  /************************************************************
   * Method Name:
   *  getFabric
   **/
  /**
   * Returns the value of fabric
   *
   * @return the fabric
   *
   ***********************************************************/
  
  public OSM_Fabric getFabric()
  {
    return Fabric;
  }
  
  public boolean isEnabled()
  {
    // true only if there is a Table and a Fabric
    if((Table != null) && (Fabric != null))
      return true;
    return false;
  }

  @Override
  public void osmFabricUpdate(OSM_Fabric osmFabric) throws Exception
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void osmServiceUpdate(OMS_Updater updater, OpenSmMonitorService osmService)
      throws Exception
  {
    // new OMS has arrived, so get the vital pieces of info, and update the routing table
     if((osmService != null) && (osmService.getFabric() != null))
    {
       setOMS(osmService);
     }
    else
    {
      // something is wrong, post a severe message
      MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_SEVERE, "Did NOT get a snapshot of the Fabric, check connection or the service", this));
    }
      
  }

}
