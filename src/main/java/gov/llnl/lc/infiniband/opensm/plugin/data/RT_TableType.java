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
 *        file: RT_TableType.java
 *
 *  Created on: May 5, 2014
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.data;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**********************************************************************
 * Describe purpose and responsibility of RT_TableType
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version May 5, 2014 1:36:03 PM
 **********************************************************************/
public enum RT_TableType
{
  RT_UNICAST(                  0, "Unicast"),    
  RT_MULTICAST(                1, "Multicast");   
  
  
  public static final EnumSet<RT_TableType> RT_ALL_TYPES = EnumSet.allOf(RT_TableType.class);

  private static final Map<Integer,RT_TableType> lookup = new HashMap<Integer,RT_TableType>();

  static 
  {
    for(RT_TableType s : RT_ALL_TYPES)
         lookup.put(s.getTypeNum(), s);
  }

  private int TypeNum;
  
  // suitable for a property file
  private String TypeName;
  
  private RT_TableType(int TypeNum, String TypeName)
  {
      this.TypeNum  = TypeNum;
      this.TypeName = TypeName;
  }
  
  public int getTypeNum()
  {
    return TypeNum;
    }

  public String getTypeName()
  {
    return TypeName;
    }


  public static RT_TableType get(int TypeNum)
  { 
      return lookup.get(TypeNum); 
  }

}
