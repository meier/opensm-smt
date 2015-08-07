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
 *        file: IB_Depth.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.graph;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**********************************************************************
 * This enum is simply a container for fixed or constant name/value pair
 * that are associated with this Depth object.
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Jun 19, 2013 1:15:40 PM
 **********************************************************************/
public enum IB_Depth implements Serializable
{
  IBD_CA(         0, "CA",             "Channel Adapter"),    
  IBD_SW1(        1, "SW-1",           "Leaf Switch"),    
  IBD_SW2(        2, "SW-2",           "Switch level 2"),    
  IBD_SW3(        3, "SW-3",           "Switch level 3"),    
  IBD_SW4(        4, "SW-4",           "Switch level 4"),    
  IBD_SW5(        5, "SW-5",           "Switch level 5"),    
  IBD_SW6(        6, "SW-6",           "Switch level 6"),    
  IBD_SW7(        7, "SW-7",           "Switch level 7"),    
  IBD_SW8(        8, "SW-8",           "Switch level 8"),    
  IBD_END(        9, "end",            "end");    
  
  
  public static final int MAX_IB_DEPTH = IBD_END.getDepth();
  
  public static final EnumSet<IB_Depth> IBD_ALL_LEVELS = EnumSet.allOf(IB_Depth.class);
  
  /**  the commands that never need an active connection to the service **/
  public static final EnumSet<IB_Depth> IBD_COMPUTE_NODES = EnumSet.range(IBD_CA, IBD_CA);

  /**  the commands that never need an active connection to the service **/
  public static final EnumSet<IB_Depth> IBD_SWITCH_NODES = EnumSet.range(IBD_SW1, IBD_SW8);

  private static final Map<Integer,IB_Depth> lookup = new HashMap<Integer,IB_Depth>();

  static 
  {
    for(IB_Depth s : IBD_ALL_LEVELS)
         lookup.put(s.getDepth(), s);
  }

  private int Depth;
  
  // suitable for a property file
  private String DepthName;
  
  // a description of the activity, normally just a single line, suitable for tooltips
  private String Description;

  private IB_Depth(int Depth, String DepthName, String Description)
  {
      this.Depth        = Depth;
      this.DepthName    = DepthName;
      this.Description  = Description;
  }


  public int getDepth()
  {
    return Depth;
  }

  public String getDepthName()
  {
    return DepthName;
    }


  public static IB_Depth get(int Depth)
  { 
      return lookup.get(Depth); 
  }
  
  public String getDescription()
  {
    return Description;
  }


  public static EnumSet<IB_Depth> getLowerDepths(IB_Depth ib_Depth)
  {
    return EnumSet.range(IBD_CA, ib_Depth);
  }
  
  public static EnumSet<IB_Depth> getHigherDepths(IB_Depth ib_Depth)
  {
    return EnumSet.range(ib_Depth, IBD_SW8);
  }

}
