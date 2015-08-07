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
 *        file: OSM_ActivityType.java
 *
 *  Created on: Jun 19, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.data;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**********************************************************************
 * This enum is simply a container for fixed or constant name/value pair
 * that are associated with the keys in an OSM_NodeActivity object.
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Jun 19, 2013 1:15:40 PM
 **********************************************************************/
public enum OSM_ActivityType implements Serializable
{
  OAT_NAME(                    0, "name",             "the name of node or port"),    
  OAT_GUID(                    1, "guid",             "the guid of node or port"),    
  OAT_TIMESTAMP(               2, "timestamp",        "the time associated with this data"),    
  OAT_NUM_NODES(               3, "num nodes",        "the number of ports"),    
  OAT_NUM_ACTIVITY_NODES(      4, "num active nodes", "the number of ports"),    
  OAT_NUM_PORTS(               5, "num ports",        "the number of ports"),    
  OAT_NUM_ACTIVE_PORTS(        6, "num active ports", "the number of ports"),    
  OAT_NUM_LINKS(               7, "num links",         "the number of ports"),    
  OAT_NUM_ACTIVE_LINKS(        8, "num active links", "the number of ports"),    
  OAT_NODE_LEVEL(              9, "node level", "the number of ports"),    
  OAT_NODE_INFO(              10, "node info", "the number of ports"),    
  OAT_LINK_PORT_NUM(          11, "port num", "the number of ports"),    
  OAT_LID(                    12, "lid", "the number of ports"),    
  OAT_LINK_SPEED(             13, "link speed", "the number of ports"),    
  OAT_LINK_RATE(              14, "link rate", "the number of ports"),    
  OAT_LINK_WIDTH(             15, "link width", "the number of ports"),    
  OAT_LINK_STATE(             16, "link state", "the number of ports"),    
  OAT_LINK_LEVEL(             17, "link level", "the number of ports"),    
  OAT_LINK_INFO(              18, "link info", "the number of ports"),    
  OAT_LINK_REMOTE_INFO(       19, "remote link info", "the number of ports"),    
  OAT_LINK_REMOTE_PORT_NUM(   20, "remote port num", "the number of ports"),    
  OAT_TRAFFIC_INFO(           21, "traffic info", "the number of ports"),    
  OAT_TRAFFIC_DELTA_INFO(     22, "traffic delta info", "the number of ports"),    
  OAT_ERROR_INFO(             23, "error info", "the number of ports"),    
  OAT_ERROR_DELTA_INFO(       24, "error delta info", "the number of ports"),    
  OAT_END(                   200, "end", "end");    
  
  
  public static final EnumSet<OSM_ActivityType> OSM_ALL_ACTIVITIES = EnumSet.allOf(OSM_ActivityType.class);
  
  /**  the commands that never need an active connection to the service **/
  public static final EnumSet<OSM_ActivityType> OSM_SWITCH_ACTIVITY_TYPES = EnumSet.range(OAT_NAME, OAT_NUM_PORTS);

  private static final Map<Integer,OSM_ActivityType> lookup = new HashMap<Integer,OSM_ActivityType>();

  static 
  {
    for(OSM_ActivityType s : OSM_ALL_ACTIVITIES)
         lookup.put(s.getActivityNum(), s);
  }

  private int ActivityNum;
  
  // suitable for a property file
  private String ActivityName;
  
  // a description of the activity, normally just a single line, suitable for tooltips
  private String Description;

  private OSM_ActivityType(int ActivityNum, String ActivityName, String Description)
  {
      this.ActivityNum  = ActivityNum;
      this.ActivityName = ActivityName;
      this.Description  = Description;
  }


  public int getActivityNum()
  {
    return ActivityNum;
    }

  public String getActivityName()
  {
    return ActivityName;
    }


  public static OSM_ActivityType get(int ActivityNum)
  { 
      return lookup.get(ActivityNum); 
  }
  
  public String getDescription()
  {
    return Description;
  }

}
