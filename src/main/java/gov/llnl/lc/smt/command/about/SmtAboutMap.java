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
 *        file: SmtAboutMap.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command.about;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**********************************************************************
 * This enum maps keys from a manifest file to keys associated with a
 * generic "about" type.  The keys in a manifest file can be almost anything
 * and this enum attempts to map well known keys to a smaller set of
 * uniform keys, for the purpose of providing simple "about" information.
 * <p>
 * NOTE: The enum map order should be least preferred to most.  Since several
 * attributes map to the same type, they will be overwritten if a subsequent
 * attribute with the same mapping occurs.
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Sep 30, 2013 11:28:30 AM
 **********************************************************************/
public enum SmtAboutMap
{
  SMT_CLASS_PATH(        0, "Class-Path",              SmtAboutType.SMT_ABOUT_NAME),    
  SMT_MAIN_CLASS(        1, "Main-Class",              SmtAboutType.SMT_ABOUT_NAME),    
  SMT_BUILT_BY(          2, "Built-By",                SmtAboutType.SMT_ABOUT_VENDOR),    
  SMT_BUILT_DATE(        3, "Built-Date",              SmtAboutType.SMT_ABOUT_DATE),
  SMT_IMPL_NAME(         4, "Implementation-Title",    SmtAboutType.SMT_ABOUT_NAME),    
  SMT_IMPL_VENDOR(       5, "Implementation-Vendor",   SmtAboutType.SMT_ABOUT_VENDOR),    
  SMT_IMPL_VERSION(      6, "Implementation-Version",  SmtAboutType.SMT_ABOUT_VERSION),    
  SMT_BNDL_NAME(         7, "Bundle-Name",             SmtAboutType.SMT_ABOUT_NAME),    
  SMT_BNDL_VENDOR(       8, "Bundle-Vendor",           SmtAboutType.SMT_ABOUT_VENDOR),    
  SMT_BNDL_VERSION(      9, "Bundle-Version",          SmtAboutType.SMT_ABOUT_VERSION),    
  SMT_BNDL_LICENSE(     10, "Bundle-License",          SmtAboutType.SMT_ABOUT_LICENSE),    
  SMT_BNDL_SUMMARY(     11, "Bundle-Description",      SmtAboutType.SMT_ABOUT_SUMMARY),
  SMT_PACKAGE(          12, "Package",                 SmtAboutType.SMT_ABOUT_NAME),    
  SMT_EXTENSION_NAME(   13, "Extension-Name",          SmtAboutType.SMT_ABOUT_NAME),    
  SMT_SPEC_VERSION(     14, "Specification-Version",   SmtAboutType.SMT_ABOUT_VERSION),    
  SMT_SPEC_TITLE(       15, "Specification-Title",     SmtAboutType.SMT_ABOUT_NAME),    
  SMT_SPEC_VENDOR(      16, "Specification-Vendor",    SmtAboutType.SMT_ABOUT_VENDOR);
  
  public static final EnumSet<SmtAboutMap> SMT_ALL_MAPPINGS = EnumSet.allOf(SmtAboutMap.class);
  public static final EnumSet<SmtAboutMap> SMT_ALL_TITLES = EnumSet.of(SMT_IMPL_NAME, SMT_SPEC_TITLE  );
  public static final EnumSet<SmtAboutMap> SMT_ALL_DATES = EnumSet.of(SMT_BUILT_DATE  );
  public static final EnumSet<SmtAboutMap> SMT_ALL_VERSIONS = EnumSet.of(SMT_IMPL_VERSION, SMT_BNDL_VERSION, SMT_SPEC_VERSION );
  
  private static final Map<Integer,SmtAboutMap> lookup = new HashMap<Integer,SmtAboutMap>();

  static 
  {
    for(SmtAboutMap s : SMT_ALL_MAPPINGS)
         lookup.put(s.getMapID(), s);
  }

  private int MapNum;
  private String MapName;
  private SmtAboutType AboutType;

private SmtAboutMap(int MapNum, String Name, SmtAboutType AboutType)
{
    this.MapNum = MapNum;
    this.MapName = Name;
    this.AboutType = AboutType;
}

public int getMapID()
{
  return MapNum;
  }

public String getMapName()
{
  return MapName;
  }

public SmtAboutType getAboutType()
{
  return AboutType;
}

public static SmtAboutMap get(int MapNum)
{ 
    return lookup.get(MapNum); 
}

public static SmtAboutMap getByName(String Name)
{
  SmtAboutMap p = null;
  
  // return the first property with an exact name match
  for(SmtAboutMap s : SMT_ALL_MAPPINGS)
  {
    if(s.getMapName().equals(Name))
      return s;
  }
  return p;
}


}
