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
 *        file: SMT_AnalysisType.java
 *
 *  Created on: Nov 11, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.manager;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**********************************************************************
 * Describe purpose and responsibility of SMT_AnalysisType
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Nov 11, 2013 2:13:03 PM
 **********************************************************************/
public enum SMT_AnalysisType
{
  SMT_FABRIC_UTILIZATION(    1, "Fabric Utilization",                   "Fabric",                   "play back history this # times faster than normal"),
  SMT_NODE_UTILIZATION(      2, "Node Utilization",                     "Node",                "the time of the OMS data within the OMS History file"),
  SMT_PORT_UTILIZATION(      3, "Port Utilization",                     "Port",              "a directive to use the default if necessary"),
  SMT_LINK_UTILIZATION(      4, "Link Utilization",                     "Link",              "a directive to use the default if necessary"),
  SMT_ROUTE_UTILIZATION(     5, "Path Utilization",                      "Utilization",              "a directive to use the default if necessary"),
  SMT_FABRIC_GRAPH(         10, "Fabric Graph",                      "FabGraph",              "a directive to use the default if necessary"),
  SMT_FABRIC_OVERVIEW(      20, "Fabric Overview",                      "FabOver",              "a directive to use the default if necessary"),
  SMT_FABRIC_ROUTING(       25, "Fabric Routing",                      "FabRoute",              "a directive to use the default if necessary"),
  SMT_ROUTE_PATH(           26, "Route Path",                      "RoutePath",              "a directive to use the default if necessary"),
  SMT_FABRIC_DETAILS(       30, "Fabric Details",                      "FabDetails",              "a directive to use the default if necessary"),
  SMT_GRAPH(               996, "Graph",                             "Graph",              "a directive to use the default if necessary"),
  SMT_OVERVIEW(            997, "Overview",                             "Summary",              "a directive to use the default if necessary"),
  SMT_UTILIZATION(         998, "Utilization",                          "",              "a directive to use the default if necessary"),
  SMT_ROUTING(             994, "Routing",                          "General",              "a directive to use the default if necessary"),
  SMT_DETAILS(             995, "Details",                          "General",              "a directive to use the default if necessary"),
  SMT_HEAT_MAP(            996, "Heat Map",                          "General",              "a directive to use the default if necessary"),
  SMT_FABRIC_TOP_NODES(     30, "Top Traffic Nodes",                 "Top Nodes",              "a directive to use the default if necessary"),
  SMT_FABRIC_TOP_LINKS(     31, "Top Traffic Links",                 "Top Links",              "a directive to use the default if necessary"),
  SMT_FABRIC_TOP_PORTS(     32, "Top Traffic Ports",                 "Top Ports",              "a directive to use the default if necessary"),
  SMT_FABRIC_ERROR_NODES(     35, "Top Error Nodes",                 "Error Nodes",              "a directive to use the default if necessary"),
  SMT_FABRIC_ERROR_LINKS(     36, "Top Error Links",                 "Error Links",              "a directive to use the default if necessary"),
  SMT_FABRIC_ERROR_PORTS(     37, "Top Error Ports",                 "Error Ports",              "a directive to use the default if necessary"),
  SMT_HMAP_CA_PORTS(          38, "Heat Map for CA Ports",                 "HM:CA Ports",              "a directive to use the default if necessary"),
  SMT_HMAP_SW_PORTS(          39, "Heat Map for SW Ports",                 "HM:SW Ports",              "a directive to use the default if necessary"),
  SMT_HMAP_ALL_PORTS(         40, "Heat Map for ALL Ports",                "HM:All Ports",              "a directive to use the default if necessary"),
  SMT_HMAP_SYS_PORTS(         41, "Heat Map for System Ports",             "HM:System Ports",              "a directive to use the default if necessary"),
  SMT_UTIL_CA_PORTS(          45, "Utilization Plot for SW - CA Ports",    "U:CA Ports",              "a directive to use the default if necessary"),
  SMT_UTIL_SW_PORTS(          46, "Utilization Plot for SW - SW Ports",    "U:SW Ports",              "a directive to use the default if necessary"),
  SMT_UTIL_ALL_PORTS(         47, "Utilization Plot for ALL Ports",        "U:All Ports",              "a directive to use the default if necessary"),
  SMT_LAST_ANALYSIS(       999, "AnalysisEnd",                          "EndOfList",                "always the end of the property list");

  /*
   *   This enum needs to change to something that supports commnand line options, such as
   *   int, PropertyName, shortName, longName, Description, ArgName
   */
  public static final EnumSet<SMT_AnalysisType> SMT_ALL_ANALYSIS_TYPES = EnumSet.allOf(SMT_AnalysisType.class);
  public static final EnumSet<SMT_AnalysisType> SMT_UTILIZATION_TYPES = EnumSet.range(SMT_FABRIC_UTILIZATION, SMT_ROUTE_UTILIZATION);
  public static final EnumSet<SMT_AnalysisType> SMT_TOP_TYPES = EnumSet.range(SMT_FABRIC_TOP_NODES, SMT_FABRIC_ERROR_PORTS);
  public static final EnumSet<SMT_AnalysisType> SMT_HMAP_TYPES = EnumSet.range(SMT_HMAP_CA_PORTS, SMT_HMAP_SYS_PORTS);
  public static final EnumSet<SMT_AnalysisType> SMT_UTIL_PLOT_TYPES = EnumSet.range(SMT_UTIL_CA_PORTS, SMT_UTIL_ALL_PORTS);
  public static final EnumSet<SMT_AnalysisType> SMT_TRAFFIC_TYPES = EnumSet.range(SMT_FABRIC_TOP_NODES, SMT_FABRIC_TOP_PORTS);
  public static final EnumSet<SMT_AnalysisType> SMT_ERROR_TYPES = EnumSet.range(SMT_FABRIC_ERROR_NODES, SMT_FABRIC_ERROR_PORTS);
   
  private static final Map<Integer,SMT_AnalysisType> lookup = new HashMap<Integer,SMT_AnalysisType>();

  static 
  {
    for(SMT_AnalysisType s : SMT_ALL_ANALYSIS_TYPES)
      lookup.put(s.getAnalysisType(), s);
  }

  private int AnalysisType;
  
  // suitable for a property file
  private String AnalysisName;
  
  // the normal full name, suitable for the long command line
  private String Name;
  
  // a description of the property, normally just a single line, suitable for "usage"
  private String Description;

  private SMT_AnalysisType(int AnalysisType, String AnalysisName, String Name, String Description)
  {
      this.AnalysisType     = AnalysisType;
      this.AnalysisName = AnalysisName;
      this.Name         = Name;
      this.Description  = Description;
  }

public int getAnalysisType()
{
  return AnalysisType;
  }

public String getAnalysisName()
{
  return AnalysisName;
  }

public String getName()
{
  return Name;
  }

public static SMT_AnalysisType get(int AnalysisType)
{ 
    return lookup.get(AnalysisType); 
}


public static SMT_AnalysisType getByName(String Name)
{
  SMT_AnalysisType p = null;
  
  // return the first property with an exact name match
  for(SMT_AnalysisType s : SMT_ALL_ANALYSIS_TYPES)
  {
    if(s.getName().equals(Name))
      return s;
  }
  return p;
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

}
