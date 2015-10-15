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
 *        file: XY_PlotType.java
 *
 *  Created on: Mar 25, 2014
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.chart;

import gov.llnl.lc.infiniband.opensm.plugin.gui.tree.Event_CounterPopupMenu;
import gov.llnl.lc.infiniband.opensm.plugin.gui.tree.MAD_CounterPopupMenu;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**********************************************************************
 * Describe purpose and responsibility of XY_PlotType
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Mar 25, 2014 11:22:40 AM
 **********************************************************************/
public enum XY_PlotType
{
  PORT_COUNTER(             0, "Port Counter",          "Port Counter"),    
  PORT_COUNTER_PLUS(        1, "Port Counter",          "Port Counter Comparison"),    
  ADV_PORT_COUNTER(         2, "Port Counter",          "Port Counter with Table"),    
  ADV_PORT_COUNTER_PLUS(    3, "Port Counter",          "Port Counter Comparison with Table"),    
  MAD_COUNTER(              4, "MAD Counter",           "MAD Counter"),    
  MAD_COUNTER_PLUS(         5, "MAD Counter",           "MAD Counter Comparison"),    
  ADV_MAD_COUNTER(          6, "MAD Counter",           "MAD Counter with Table"),    
  ADV_MAD_COUNTER_PLUS(     7, "MAD Counter",           "MAD Counter Comparison with Table"),    
  EVENT_COUNTER(            8, "Event Counter",         "Event Counter"),    
  EVENT_COUNTER_PLUS(       9, "Event Counter",         "Event Counter Comparison"),    
  ADV_EVENT_COUNTER(       10, "Event Counter",         "Event Counter with Table"),    
  ADV_EVENT_COUNTER_PLUS(  11, "Event Counter",         "Event Counter Comparison with Table"),    
  PORT_UTIL(               12, "Port Utilization",      "Port Utilization"),    
  PORT_UTIL_PLUS(          13, "Port Utilization",      "Port Utilization Comparison"),    
  ADV_PORT_UTIL(           14, "Port Utilization",      "Port Utilization with Table"),    
  ADV_PORT_UTIL_PLUS(      15, "Port Utilization",      "Port Utilization Comparison with Table");    

    public static final EnumSet<XY_PlotType> XYPLOT_ALL_TYPES  = EnumSet.allOf(XY_PlotType.class);
    public static final EnumSet<XY_PlotType> XYPLOT_MAD_TYPES  = EnumSet.range(MAD_COUNTER, ADV_MAD_COUNTER_PLUS);
    public static final EnumSet<XY_PlotType> XYPLOT_PORT_TYPES = EnumSet.range(PORT_COUNTER, ADV_PORT_COUNTER_PLUS);
    public static final EnumSet<XY_PlotType> XYPLOT_EVENT_TYPES = EnumSet.range(EVENT_COUNTER, ADV_EVENT_COUNTER_PLUS);
    public static final EnumSet<XY_PlotType> XYPLOT_ADV_TYPES  = EnumSet.of(ADV_PORT_COUNTER, ADV_PORT_COUNTER_PLUS, ADV_MAD_COUNTER, ADV_MAD_COUNTER_PLUS,  ADV_EVENT_COUNTER, ADV_EVENT_COUNTER_PLUS);
    public static final EnumSet<XY_PlotType> XYPLOT_PLUS_TYPES = EnumSet.of(PORT_COUNTER_PLUS, ADV_PORT_COUNTER_PLUS, MAD_COUNTER_PLUS, ADV_MAD_COUNTER_PLUS, EVENT_COUNTER_PLUS, ADV_EVENT_COUNTER_PLUS);
    public static final EnumSet<XY_PlotType> XYPLOT_UTIL_TYPES  = EnumSet.range(PORT_UTIL, ADV_PORT_UTIL_PLUS);
    
    private static final Map<Integer,XY_PlotType> lookup = new HashMap<Integer,XY_PlotType>();

    static 
    {
      for(XY_PlotType s : XYPLOT_ALL_TYPES)
           lookup.put(s.getIndex(), s);
    }
    
  // the index that matches the native peer array
    private int Index;
    
    // the name of the counter
    private String Name;
    
    // a description of the counter
    private String Description;
    
    private XY_PlotType(int index, String name, String description)
    {
      Index = index;
      Name = name;
      Description = description;
    }
    
    public static XY_PlotType getByName(String name)
    {
      XY_PlotType t = null;
      
      // return the first property with an exact name match
      for(XY_PlotType s : XYPLOT_ALL_TYPES)
      {
        if(s.getName().equals(name))
          return s;
      }
      return t;
    }

    public static XY_PlotType getByIndex(int index)
    {
      XY_PlotType t = null;
      
      // return the first property with an exact name match
      for(XY_PlotType s : XYPLOT_ALL_TYPES)
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
     
     public boolean isAdvanced()
     {
       return XY_PlotType.XYPLOT_ADV_TYPES.contains(this);
     }

     public boolean isCompare()
     {
       return XY_PlotType.XYPLOT_PLUS_TYPES.contains(this);
     }

     public boolean isPortCounter()
     {
       return XY_PlotType.XYPLOT_PORT_TYPES.contains(this);
     }

     public boolean isMADCounter()
     {
       return XY_PlotType.XYPLOT_MAD_TYPES.contains(this);
     }
     
     public boolean isEventCounter()
     {
       return XY_PlotType.XYPLOT_EVENT_TYPES.contains(this);
     }
     
     public boolean isUtilizationType()
     {
       return XY_PlotType.XYPLOT_UTIL_TYPES.contains(this);
     }
     
     public String getMenuLabel()
     {
       if(isAdvanced())
       {
         if(isCompare())
           return "Advanced Compare";
         return "Advanced";
       }
       if(isCompare())
         return "Compare";
       return "Simple";
     }

     /************************************************************
     * Method Name:
     *  getPlotTypeFromMenuLabel
    **/
    /**
     * Attempt to return the type of plot based on the menuLabel, and an object
     * which represents the source of the query (Usually the type of PopUpMenu).
     * The menuLabel is normally either advanced, advanced compare,
     * or simple and simple compare.
     * 
     * The Object indicates if this is a PortCounter, EventCounter, or a MadCounter.
     *
     * @see     describe related java objects
     *
     * @param source
     * @param menuLabel
     * @return
     ***********************************************************/
    public static XY_PlotType getPlotTypeFromMenuLabel(Object source, String menuLabel)
     {
      boolean isMadCounter = (source instanceof MAD_CounterPopupMenu);
      boolean isPortCounter = !(source instanceof MAD_CounterPopupMenu);
      boolean isEventCounter = (source instanceof Event_CounterPopupMenu);
       if((menuLabel != null) && (menuLabel.length() > 5))
       {
         if(isMadCounter)
         {
           // restrict myself to the MAD Types
           if(menuLabel.contains("Advanced"))
           {
             if(menuLabel.contains("Compare"))
               return XY_PlotType.ADV_MAD_COUNTER_PLUS;
             return XY_PlotType.ADV_MAD_COUNTER;
           }
           if(menuLabel.contains("Compare"))
             return XY_PlotType.MAD_COUNTER_PLUS;
           return XY_PlotType.MAD_COUNTER;
         }
         else if(isEventCounter)
         {
           // restrict myself to the Event Types
           if(menuLabel.contains("Advanced"))
           {
             if(menuLabel.contains("Compare"))
               return XY_PlotType.ADV_EVENT_COUNTER_PLUS;
             return XY_PlotType.ADV_EVENT_COUNTER;
           }
           if(menuLabel.contains("Compare"))
             return XY_PlotType.EVENT_COUNTER_PLUS;
           return XY_PlotType.EVENT_COUNTER;
         }
         else if(isPortCounter)
         {
           // restrict myself to the PortTypes
           if(menuLabel.contains("Advanced"))
           {
             if(menuLabel.contains("Compare"))
               return XY_PlotType.ADV_PORT_COUNTER_PLUS;
             return XY_PlotType.ADV_PORT_COUNTER;
           }
           if(menuLabel.contains("Compare"))
             return XY_PlotType.PORT_COUNTER_PLUS;
           return XY_PlotType.PORT_COUNTER;
         }
       }
       return null;
      }

    public String getDescription()
    {
      return Description;
    }

}
