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
 *        file: SMT_AbstractUpdateService.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.data;

import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_AbstractUpdateService;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Collection;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_ServiceChangeListener;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Edge;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;
import gov.llnl.lc.smt.SmtConstants;
import gov.llnl.lc.smt.props.SmtProperty;

import java.util.LinkedHashMap;

public abstract class SMT_AbstractUpdateService extends OMS_AbstractUpdateService implements SMT_Updater
{
  /** the one and only <code>SMT_AbstractUpdateService</code> Singleton **/
  private volatile static SMT_AbstractUpdateService globalUpdateService  = null;
  
  /** a collection of snapshots, obtained from a file, or by building up from instances via a connection **/
  protected static OMS_Collection omsHistory = null;
  
  protected static LinkedHashMap<String, IB_Edge> edgeMap = null;
  protected static LinkedHashMap<String, IB_Vertex> vertexMap = null;
  
  @Override
  public synchronized long getUpdatePeriod()
  {
    String sVal = serviceProps.getProperty(SmtProperty.SMT_UPDATE_PERIOD.name());
    if(sVal == null)
      return 300L;
    Long lVal = new Long(sVal);
    return lVal.longValue();
  }


  /************************************************************
   * Method Name:
   *  isFileMode
   **/
  /**
   * Returns the value of fileMode
   *
   * @return the fileMode
   *
   ***********************************************************/
  
  public boolean isFileMode()
  {
    // if a history file is provided, then we are file mode
    if(SmtConstants.SMT_NO_FILE.equals(getFile()))
      return false;
    
    // only support a History file
    return true;
  }
  
  public boolean isConnectionMode()
  {
    // normally the host and port always have default values,
    // but if we are definately using a file, then we are not
    // using a connection
    if(isFileMode())
      return false;
    
    // only true if I have a host and port number to use
    String host = getHost();
    String port = getPort();
    
    return (host != null) && (host.length() > 2) && (port != null) && (port.length() > 2);
  }
  
  public boolean isOmsSpecified()
  {
    // check to see if a file was specified or a host and port number
    return isFileMode() || isConnectionMode();
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
  public synchronized void refreshService()
   {
    
    // override as necessary, but include the edge and map creation, as well as other necessary
    // derived data
     
     // recalculate everything, before notifying the listeners
     LinkedHashMap<String, IB_Edge>   eMap = IB_Edge.createEdgeMap(osmService.getFabric().getOSM_Nodes(), osmService.getFabric().getOSM_Ports());
     LinkedHashMap<String, IB_Vertex> vMap = IB_Vertex.createVertexMap(eMap, osmService.getFabric());

     if (vMap == null)
       System.exit(-1);
     
     IB_Vertex.setSBN_Switches(vMap, osmService.getFabric());
     vertexMap = vMap;
     edgeMap   = eMap;
     
     if(vertexMap == null)
       System.err.println("The VERTEX MAP IS NULL");
   }
   /*-----------------------------------------------------------------------*/
  
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
  protected synchronized void updateAllListeners()
   {
    refreshService();
    
//    // override as necessary, but include the edge and map creation, as well as other necessary
//    // derived data
//     
//     // recalculate everything, before notifying the listeners
//     LinkedHashMap<String, IB_Edge>   eMap = IB_Edge.createEdgeMap(osmService.getFabric().getOSM_Nodes(), osmService.getFabric().getOSM_Ports());
//     LinkedHashMap<String, IB_Vertex> vMap = IB_Vertex.createVertexMap(eMap);
//
//     if (vMap == null)
//       System.exit(-1);
//     
//     IB_Vertex.setSBN_Switches(vMap, osmService.getFabric());
//     vertexMap = vMap;
//     edgeMap   = eMap;
     
     for(OSM_ServiceChangeListener listener: Service_Listeners)
     {
       try
      {
        listener.osmServiceUpdate(this, osmService);
      }
      catch (Exception e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
        logger.severe("Problem updating smt listeners");
        if(listener == null)
          logger.severe("the listener is null, clean up list");
        else
          logger.severe(listener.getClass().getName());

        if(osmService == null)
          logger.severe("the osmService is null, don't send out");

        logger.severe(e.getMessage());
      }
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

  @Override
  public synchronized LinkedHashMap<String, IB_Edge> getEdgeMap()
  {
    return edgeMap;
  }

  @Override
  public synchronized LinkedHashMap<String, IB_Vertex> getVertexMap()
  {
    return vertexMap;
  }


  @Override
  public synchronized OMS_Collection getCollection()
  {
    return omsHistory;
  }

  @Override
  public synchronized void setCollection(OMS_Collection history)
  {
    omsHistory = history;
  }

}
