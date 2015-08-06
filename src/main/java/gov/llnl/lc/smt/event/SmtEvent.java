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
 *        file: SmtEvent.java
 *
 *  Created on: Sep 30, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.event;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**********************************************************************
 * Describe purpose and responsibility of SmtEvent
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Sep 30, 2013 11:28:30 AM
 **********************************************************************/
public enum SmtEvent
{
  SMT_EVENT_HEARTBEAT(          0, "one second heartbeat"),    
  SMT_EVENT_OMS_UPDATE(         1, "new OMS is available"),    
  SMT_EVENT_PERF_MGR_UPDATE(    2, "new port counters withing OMS is available"),    
  SMT_EVENT_SIGNAL_HUP(         3, "a HUP signal (linux only)"),    
  SMT_EVENT_SIGNAL_USR2(        4, "a USR2 signal (linux only)"),    
  SMT_EVENT_SELECTION(          5, "a gui object has been selected"),    
  SMT_EVENT_DECORATION(         6, "rendering of a gui object may need to change"),    
  SMT_EVENT_CONFIGURATION(      7, "configuration or preference has changed"),    
  SMT_EVENT_PROCESS_START(      8, "a process thread has started"),    
  SMT_EVENT_PROCESS_COMPLETE(   9, "a process thread has completed"),    
  SMT_EVENT_MESSAGE(           10, "a general purpose message"),    
  SMT_EVENT_TIMEOUT(           12, "event timeout"),    
  SMT_EVENT_MAX(               13, "final event");
  
  public static final EnumSet<SmtEvent> SMT_ALL_EVENTS = EnumSet.allOf(SmtEvent.class);
  
  private static final Map<Integer,SmtEvent> lookup = new HashMap<Integer,SmtEvent>();

  static 
  {
    for(SmtEvent s : SMT_ALL_EVENTS)
         lookup.put(s.getEvent(), s);
  }

  private int Event;
  private String EventName;

private SmtEvent(int Event, String Name)
{
    this.Event = Event;
    this.EventName = Name;
}

public int getEvent()
{
  return Event;
  }

public String getEventName()
{
  return EventName;
  }

public static SmtEvent get(int Event)
{ 
    return lookup.get(Event); 
}


}
