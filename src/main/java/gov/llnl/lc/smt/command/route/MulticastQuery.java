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
 *        file: MulticastQuery.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command.route;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**********************************************************************
 * Describe purpose and responsibility of MulticastQuery
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Jan 12, 2015 4:50:55 PM
 **********************************************************************/
public enum MulticastQuery
{
  MCAST_LIST(              0, "list",             "l",    "lists the available query options"),    
  MCAST_STATUS(            2, "status",           "stat", "provides multicast stats"),    
  MCAST_GROUPS(            3, "groups",            "G",    "lists each group on a single line"),    
  MCAST_MEMBER(            4, "member",            "M",    "list the groups this port is a member <lid, guid, or name>"),    
  MCAST_MLID(              5, "mlid",              "m",   "show mcast group <mlid>"),    
  MCAST_LAST_QUERY(      999, "QueryEnd",         "end",  "always the end of the property list");
  
  /*
   *   This enum needs to change to something that supports command line options, such as
   *   int, PropertyName, shortName, longName, Description, ArgName
   */
  public static final EnumSet<MulticastQuery> MCAST_ALL_QUERIES = EnumSet.allOf(MulticastQuery.class);
  
  
  private static final Map<Integer,MulticastQuery> lookup = new HashMap<Integer,MulticastQuery>();

  static 
  {
    for(MulticastQuery q : MCAST_ALL_QUERIES)
         lookup.put(q.getQNum(), q);
  }
  
  private static class AlphaCompare implements Comparator<MulticastQuery>
  {
    @Override
    public int compare(MulticastQuery q1, MulticastQuery q2) 
    {
        return q1.getName().compareTo(q2.getName());
    }
  }

  private int QNum;
  
  // the normal full name, suitable for the long command line
  private String Name;
  
  // a short name, perhaps a single letter, suitable for the short command line
  private String ShortName;
  
  // a description of the property, normally just a single line, suitable for "usage"
  private String Description;

  private MulticastQuery(int QNum, String Name, String ShortName, String Description)
  {
      this.QNum         = QNum;
      this.Name         = Name;
      this.ShortName    = ShortName;
      this.Description  = Description;
  }

  /************************************************************
   * Method Name:
   *  getQNum
   **/
  /**
   * Returns the value of qNum
   *
   * @return the qNum
   *
   ***********************************************************/
  
  public int getQNum()
  {
    return QNum;
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

  public static MulticastQuery get(int qNum)
  { 
      return lookup.get(qNum); 
  }


  public static MulticastQuery getByName(String Name)
  {
    // return the first query with an exact name match
    for(MulticastQuery q : MCAST_ALL_QUERIES)
    {
      if(q.getName().equals(Name))
        return q;
    }
    return null;
  }

  public static String describeAllQueryTypes()
  {
    return describeAllQueryTypes(MCAST_ALL_QUERIES);
  }

  public static String describeAllQueryTypes(EnumSet<MulticastQuery> eSet)
  {
    // put in alphabetical order
    TreeSet<MulticastQuery> ts = new TreeSet<MulticastQuery>(new AlphaCompare());
    for(MulticastQuery q : eSet)
    {
      if(q.equals(MCAST_LAST_QUERY))
        break;
      ts.add(q);
    }

    int maxNameLength = getMaxNameLength(eSet);
    String formatString = "   %-" + maxNameLength + "s   %s\n";
    StringBuffer buff = new StringBuffer();
    // build a string describing the set
    buff.append("usage: smt-multicast [-h=<host url>] [-pn=<port num>] <mlid> -q <option>\n");
    buff.append(" some query options require an mlid, and perhaps a guid\n");
    buff.append("  options include:\n");
    
    for(MulticastQuery q : ts)
    {
      buff.append(String.format(formatString, q.getName(), q.getDescription()));
    }
    return buff.toString();
  }

  private static int getMaxNameLength(EnumSet<MulticastQuery> eSet)
  {
    int len = 0;
    for(MulticastQuery q : eSet)
    {
      if(q.equals(MCAST_LAST_QUERY))
        break;
      
      len = len > q.getName().length() ? len: q.getName().length();
    }
    return len;
  }

  
}
