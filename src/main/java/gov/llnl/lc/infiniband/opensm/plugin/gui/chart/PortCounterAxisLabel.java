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
 *        file: PortCounterAxisLabel.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.chart;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum PortCounterAxisLabel
{
  COUNTS(      0,             "counts",      "absolute value",  "register value"),    
  DELTA(       1,             "delta",       "change/period",   "difference between samples"),    
  RCV_DELTA(   2,             "rcv delta",   "change/period",   "difference between samples"),    
  XMT_DELTA(   3,             "xmit delta",  "change/period",   "difference between samples"),    
  RATE(        4,             "rate",        "MB/s", "transfer speed"),    
  UTILIZATION( 5,             "utilization", "% Max", "percentage of maximum rate"),    
  UTIL_AVE(    6,             "ave",         "% of full BW", "percentage of maximum rate"),    
  UTIL_STD_DEV(7,             "std dev",     "% of full BW", "percentage of maximum rate"),    
  UTIL_MIN(    8,             "min",         "% of full BW", "percentage of maximum rate"),    
  UTIL_MAX(    9,             "max",         "% of full BW", "percentage of maximum rate");    

  public static final EnumSet<PortCounterAxisLabel> PFM_ALL_COUNTER_LABELS = EnumSet.allOf(PortCounterAxisLabel.class);
  
  private static final Map<Integer,PortCounterAxisLabel> lookup = new HashMap<Integer,PortCounterAxisLabel>();

  static 
  {
    for(PortCounterAxisLabel s : PFM_ALL_COUNTER_LABELS)
         lookup.put(s.getIndex(), s);
  }
  
  // the enum index
  private int Index;
  
  // the short name of the counter unit
  private String Name;
  
  // the scaling value of the unit
  private String Units;
  
  // a description or a long name of the counter unit
  private String Description;
  
  private PortCounterAxisLabel(int index, String name, String units, String description)
  {
    Index = index;
    Units = units;
    Name = name;
    Description = description;
  }
  
  public static PortCounterAxisLabel getByName(String name)
  {
    PortCounterAxisLabel t = null;
    
    // return the first property with an exact name match
    for(PortCounterAxisLabel s : PFM_ALL_COUNTER_LABELS)
    {
      if(s.getName().equals(name))
        return s;
    }
    return t;
  }

  public static PortCounterAxisLabel getByIndex(int index)
  {
    PortCounterAxisLabel t = null;
    
    // return the first property with an exact name match
    for(PortCounterAxisLabel s : PFM_ALL_COUNTER_LABELS)
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


  public String getUnits()
  {
    return Units;
  }

  public String getName()
  {
    return Name;
  }


  public String getDescription()
  {
    return Description;
  }

}
