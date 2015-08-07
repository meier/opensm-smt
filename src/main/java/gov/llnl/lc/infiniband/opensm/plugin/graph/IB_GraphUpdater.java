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
 *        file: IB_GraphUpdater.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.graph;

import gov.llnl.lc.logging.CommonLogger;


/**********************************************************************
 * Describe purpose and responsibility of GraphSelectionManager
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Aug 21, 2013 12:03:33 PM
 **********************************************************************/
public class IB_GraphUpdater implements IB_GraphSelectionUpdater, CommonLogger
{

  /************************************************************
   * Method Name:
   *  GraphSelectionManager
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   ***********************************************************/
  public IB_GraphUpdater()
  {
    super();
    // TODO Auto-generated constructor stub
  }
  /** a list of Listeners, interested in knowing when Graph Events happend **/
  private static java.util.ArrayList <IB_GraphSelectionListener> Graph_Listeners =
    new java.util.ArrayList<IB_GraphSelectionListener>();

    /** logger for the class **/
    private final java.util.logging.Logger classLogger =
        java.util.logging.Logger.getLogger( getClass().getName() );

    public synchronized int getNumListeners()
    {
      return Graph_Listeners.size();
    }
     
 /************************************************************
   * Method Name:
   *  addIB_GraphSelectionListener
   **/
  /**
   * Describe the method here
   *
   * @see gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionUpdater#addIB_GraphSelectionListener(gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionListener)
   *
   * @param listener
   * @return
   ***********************************************************/

  @Override
  public synchronized boolean addIB_GraphSelectionListener(IB_GraphSelectionListener listener)
  {
    // add the listener, and its set of events
    classLogger.info("adding selection listener");
    if(listener != null)
    {
      Graph_Listeners.add(listener);
    }
    return true;
  }

  /************************************************************
   * Method Name:
   *  removeIB_GraphSelectionListener
   **/
  /**
   * Describe the method here
   *
   * @see gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionUpdater#removeIB_GraphSelectionListener(gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionListener)
   *
   * @param listener
   * @return
   ***********************************************************/

  @Override
  public synchronized boolean removeIB_GraphSelectionListener(IB_GraphSelectionListener listener)
  {
    classLogger.info("removing selection listener");
    if (Graph_Listeners.remove(listener))
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
  public synchronized void updateAllListeners(IB_GraphSelectionEvent event)
  {
    for( int i = 0; i < Graph_Listeners.size(); i++ )
    {
      IB_GraphSelectionListener listener = (IB_GraphSelectionListener)Graph_Listeners.get( i );
      if(listener != null)
        listener.valueChanged(this, event);
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

}
