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
 *        file: SmtEventObject.java
 *
 *  Created on: Sep 30, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.event;

/**********************************************************************
 * Describe purpose and responsibility of SmtEventObject
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Sep 30, 2013 11:32:41 AM
 **********************************************************************/
public class SmtEventObject
{
  public SmtEvent Event;
  public Object   Source;
  public Object   Context;
  public Object   UserObject;
  /************************************************************
   * Method Name:
   *  SmtEventObject
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param event
   * @param source
   * @param context
   * @param userObject
   ***********************************************************/
  public SmtEventObject(SmtEvent event, Object source, Object context, Object userObject)
  {
    super();
    Event = event;
    Source = source;
    Context = context;
    UserObject = userObject;
  }
  /************************************************************
   * Method Name:
   *  getEvent
   **/
  /**
   * Returns the value of event
   *
   * @return the event
   *
   ***********************************************************/
  
  public SmtEvent getEvent()
  {
    return Event;
  }
  /************************************************************
   * Method Name:
   *  getSource
   **/
  /**
   * Returns the value of source
   *
   * @return the source
   *
   ***********************************************************/
  
  public Object getSource()
  {
    return Source;
  }
  /************************************************************
   * Method Name:
   *  getContext
   **/
  /**
   * Returns the value of context
   *
   * @return the context
   *
   ***********************************************************/
  
  public Object getContext()
  {
    return Context;
  }
  /************************************************************
   * Method Name:
   *  getUserObject
   **/
  /**
   * Returns the value of userObject
   *
   * @return the userObject
   *
   ***********************************************************/
  
  public Object getUserObject()
  {
    return UserObject;
  }
  
  // make some abstract methods here that must be implemented
  
}
