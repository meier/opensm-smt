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
 *        file: MAD_Counter.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.data;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum MAD_Counter
{
  /**********************************************************************
   * The <code>MAD_Counter</code> enum is a convenience representing the
   * stats.
   * <p>
   *
   * @author meier3
   * 
   * @version Aug 26, 2014 4:40:35 PM
   **********************************************************************/
  
  qp0_mads_outstanding(             0, "QP0 MADs outstanding",          "sa_mads_ignored"),    
  qp0_mads_outstanding_on_wire(     1, "QP0 MADs outstanding (on wire)", "description"),    
  qp0_mads_rcvd(                    2, "QP0 MADs rcvd",                 "description"),    
  qp0_mads_sent(                    3, "QP0 MADs sent",                 "description"),    
  qp0_unicasts_sent(                4, "QP0 unicasts sent",             "description"),    
  qp0_mads_rcvd_unknown(            5, "QP0 unknown MADs rcvd",         "description"),    
  sa_mads_outstanding(              6, "SA MADs outstanding",           "description"),    
  sa_mads_rcvd(                     7, "SA MADs rcvd",                  "description"),    
  sa_mads_sent(                     8, "SA MADs sent",                  "description"),    
  sa_mads_rcvd_unknown(             9, "SA unknown MADs rcvd",          "description"),    
  sa_mads_ignored(                 10, "SA MADs ignored",               "description");    

    public static final EnumSet<MAD_Counter> MAD_ALL_COUNTERS = EnumSet.allOf(MAD_Counter.class);
    
    public static final EnumSet<MAD_Counter> MAD_QP0_COUNTERS  = EnumSet.range(MAD_Counter.qp0_mads_outstanding, MAD_Counter.qp0_mads_rcvd_unknown);
    public static final EnumSet<MAD_Counter> MAD_SA_COUNTERS   = EnumSet.range(MAD_Counter.sa_mads_outstanding, MAD_Counter.sa_mads_ignored);

    private static final Map<Integer,MAD_Counter> lookup = new HashMap<Integer,MAD_Counter>();

    static 
    {
      for(MAD_Counter s : MAD_ALL_COUNTERS)
           lookup.put(s.getIndex(), s);
    }
    
  // the index that matches the native peer array
    private final int Index;
    
    // the name of the counter
    private String Name;
    
    // a description of the counter
    private String Description;
    
    private MAD_Counter(int index, String name, String description)
    {
      Index = index;
      Name = name;
      Description = description;
    }
    
    public static MAD_Counter getByName(String name)
    {
      MAD_Counter t = null;
      
      // return the first property with an exact name match
      for(MAD_Counter s : MAD_ALL_COUNTERS)
      {
        if(s.getName().equals(name))
          return s;
      }
      return t;
    }

    public static MAD_Counter getByIndex(int index)
    {
      MAD_Counter t = null;
      
      // return the first property with an exact name match
      for(MAD_Counter s : MAD_ALL_COUNTERS)
      {
        if(s.getIndex() == index)
          return s;
      }
      return t;
    }

    public int getIndex()
    {
      return Index;
    }

     public String getName()
    {
      return Name;
    }


    public String getDescription()
    {
      return Description;
    }

    public long getCounterValue(OSM_Stats mad_stats)
    {
      long val = -1L;
      // TODO Move this functionality into OSM_Stats
      
      switch (Index) 
      {
        case 1:
          val = mad_stats.qp0_mads_outstanding;
          break;
          
        case 2:
          val = mad_stats.qp0_mads_rcvd;
          break;
          
        case 3:
          val = mad_stats.qp0_mads_sent;
          break;
          
        case 4:
          val = mad_stats.qp0_unicasts_sent;
          break;
          
         case 5:
           val = mad_stats.qp0_mads_rcvd_unknown;
           break;
           
         case 6:
           val = mad_stats.sa_mads_outstanding;
           break;
           
         case 7:
           val = mad_stats.sa_mads_rcvd;
           break;
           
         case 8:
           val = mad_stats.sa_mads_sent;
           break;
           
         case 9:
           val = mad_stats.sa_mads_rcvd_unknown;
           break;
           
         case 10:
           val = mad_stats.sa_mads_ignored;
           break;
          
      }
      return val;
    }

}
