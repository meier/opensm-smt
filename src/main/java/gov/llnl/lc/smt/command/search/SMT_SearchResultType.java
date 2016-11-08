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
 *        file: SMT_SearchResultType.java
 *
 *  Created on: Jan 12, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command.search;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**********************************************************************
 * Describe purpose and responsibility of SMT_SearchResultType
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Jan 12, 2015 4:50:55 PM
 **********************************************************************/
public enum SMT_SearchResultType
{
  SEARCH_SYSTEM(          11, "system",          "l",   "lists the available query options"),    
  SEARCH_NODE(             0, "node",            "l",   "lists the available query options"),    
  SEARCH_PORT(             1, "port",            "stat","provides a subnet status report"),    
  SEARCH_LINK(             2, "link",            "fab", "shows the current composition of the fabric"),    
  SEARCH_RTNODE(           3, "switch routes",   "sw",  "lists switches"),    
  SEARCH_RTPORT(           4, "port routes",     "hca", "lists host channel adapters"),    
  SEARCH_CONFIG(           5, "configuration",   "OMS", "provides an OMS status report"),    
  SEARCH_PARTITION(        6, "partition",       "ck",  "checks the fabric and reports issues"),    
  SEARCH_MCAST(            7, "multicast group", "cfg", "lists the ideal configuration"),    
  SEARCH_SA_KEY(           8, "SA key",          "up",  "show the nodes that are up (on-line only)"),    
  SEARCH_SUBNET_KEY(       9, "SM key",          "rt",  "show fabric routing table information"),    
  SEARCH_SUBNET_PREFIX(   10, "Subnet prefix",   "e",   "shows dynamic errors in the fabric"),    
  SEARCH_LAST_TYPE(      999, "QueryEnd",        "end", "always the end of the enum");
  
  /*
   *   This enum represents the type of result, contained in the SMT_SearchResult
   */
  public static final EnumSet<SMT_SearchResultType> SEARCH_ALL_TYPES = EnumSet.allOf(SMT_SearchResultType.class);
  
  public static final EnumSet<SMT_SearchResultType> SEARCH_NORMAL_TYPES = EnumSet.range(SEARCH_SYSTEM, SEARCH_SUBNET_PREFIX);
  
  
  private static final Map<Integer,SMT_SearchResultType> lookup = new HashMap<Integer,SMT_SearchResultType>();

  static 
  {
    for(SMT_SearchResultType s : SEARCH_ALL_TYPES)
         lookup.put(s.getTypeNum(), s);
  }
  
  private static class AlphaCompare implements Comparator<SMT_SearchResultType>
  {
    @Override
    public int compare(SMT_SearchResultType s1, SMT_SearchResultType s2) 
    {
        return s1.getName().compareTo(s2.getName());
    }
  }
  
  private int TypeNum;
  
  // the normal full name, suitable for the long command line
  private String Name;
  
  // a short name, perhaps a single letter, suitable for the short command line
  private String ShortName;
  
  // a description of the property, normally just a single line, suitable for "usage"
  private String Description;

  private SMT_SearchResultType(int TypeNum, String Name, String ShortName, String Description)
  {
      this.TypeNum      = TypeNum;
      this.Name         = Name;
      this.ShortName    = ShortName;
      this.Description  = Description;
  }

  /************************************************************
   * Method Name:
   *  getTypeNum
   **/
  /**
   * Returns the value of qNum
   *
   * @return the typeNum
   *
   ***********************************************************/
  
  public int getTypeNum()
  {
    return TypeNum;
  }

  /************************************************************
   * Method Name:
   *  getName
   **/
  /**
   * Returns the value of name
   *
   * @return the name
   *
   ***********************************************************/
  
  public String getName()
  {
    return Name;
  }

  /************************************************************
   * Method Name:
   *  getShortName
   **/
  /**
   * Returns the value of shortName
   *
   * @return the shortName
   *
   ***********************************************************/
  
  public String getShortName()
  {
    return ShortName;
  }

  /************************************************************
   * Method Name:
   *  getDescription
   **/
  /**
   * Returns the value of description
   *
   * @return the description
   *
   ***********************************************************/
  
  public String getDescription()
  {
    return Description;
  }

  public static SMT_SearchResultType get(int type)
  { 
      return lookup.get(type); 
  }


  public static SMT_SearchResultType getByName(String Name)
  {
    // return the first type with an exact name match
    for(SMT_SearchResultType s : SEARCH_ALL_TYPES)
    {
      if(s.getName().equals(Name))
        return s;
    }
    return null;
  }

  public static String describeAllSearchTypes()
  {
    return describeSearchTypes(SEARCH_ALL_TYPES);
  }

  public static String describeSearchTypes(EnumSet<SMT_SearchResultType> eSet)
  {
    // put in alphabetical order
    TreeSet<SMT_SearchResultType> ts = new TreeSet<SMT_SearchResultType>(new AlphaCompare());
    for(SMT_SearchResultType q : eSet)
    {
      if(q.equals(SEARCH_LAST_TYPE))
        break;
      ts.add(q);
    }

    int maxNameLength = getMaxNameLength(eSet);
    String formatString = "   %-" + maxNameLength + "s   %s\n";
    StringBuffer buff = new StringBuffer();
    // build a string describing the set
       
    for(SMT_SearchResultType q : ts)
      buff.append(String.format(formatString, q.getName(), q.getDescription()));
    return buff.toString();
  }

  private static int getMaxNameLength(EnumSet<SMT_SearchResultType> eSet)
  {
    int len = 0;
    for(SMT_SearchResultType q : eSet)
    {
      if(q.equals(SEARCH_LAST_TYPE))
        break;
      
      len = len > q.getName().length() ? len: q.getName().length();
    }
    return len;
  }
  
}
