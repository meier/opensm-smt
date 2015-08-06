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
 *        file: SmtAboutType.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command.about;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**********************************************************************
 * Describe purpose and responsibility of SmtAboutType
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Sep 30, 2013 11:28:30 AM
 **********************************************************************/
public enum SmtAboutType
{
  SMT_ABOUT_NAME(          0, "Name"),    
  SMT_ABOUT_VERSION(       1, "Version"),    
  SMT_ABOUT_DATE(          2, "Date"),    
  SMT_ABOUT_VENDOR(        3, "Vendor"),    
  SMT_ABOUT_LICENSE(       9, "License"),    
  SMT_ABOUT_SUMMARY(      10, "Summary");
  
  public static final EnumSet<SmtAboutType> SMT_ALL_ABOUTS = EnumSet.allOf(SmtAboutType.class);
  
  private static final Map<Integer,SmtAboutType> lookup = new HashMap<Integer,SmtAboutType>();

  static 
  {
    for(SmtAboutType s : SMT_ALL_ABOUTS)
         lookup.put(s.getAboutID(), s);
  }

  private int AboutNum;
  private String AboutName;

private SmtAboutType(int AboutNum, String Name)
{
    this.AboutNum = AboutNum;
    this.AboutName = Name;
}

public int getAboutID()
{
  return AboutNum;
  }

public String getName()
{
  return AboutName;
  }

public static SmtAboutType get(int AboutNum)
{ 
    return lookup.get(AboutNum); 
}


}
