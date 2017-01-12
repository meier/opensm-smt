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
 *        file: SmtFilter.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import gov.llnl.lc.smt.props.SmtProperty;
import gov.llnl.lc.util.filter.WhiteAndBlackListFilter;

/**********************************************************************
 * A simple string filter, that utilizes a white list (allowed) and
 * black list (denied) to implement the filter.  Additionally, a list
 * of files can be used to describe the white and black lists.
 * 
 * Once constructed, the primary way to use the filter is by the test
 * isFiltered(String).
 * 
 * <p>
 * @see  WhiteAndBlackListFilter
 *
 * @author meier3
 * 
 * @version Sep 18, 2013 1:25:05 PM
 **********************************************************************/
public class SmtFilter extends gov.llnl.lc.util.filter.WhiteAndBlackListFilter
{
  
  String FilterFileName = "unknown";
  String Description    = "unknown";
 
  /************************************************************
   * Method Name:
   *  getFilterFileName
  **/
  /**
   * Returns the value of filterFileName
   *
   * @return the filterFileName
   *
   ***********************************************************/
  
  public String getFilterFileName()
  {
    return FilterFileName;
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

  /************************************************************
   * Method Name:
   *  setDescription
  **/
  /**
   * Sets the value of description
   *
   * @param description the description to set
   *
   ***********************************************************/
  public void setDescription(String description)
  {
    Description = description;
  }

  /************************************************************
   * Method Name:
   *  SmtFilter
  **/
  /**
   * An empty filter.  Everything should pass through this filter.
   *
   * @see     describe related java objects
   *
   * @param filterFileName
   ***********************************************************/
  public SmtFilter()
  {
    super();
  }

  /************************************************************
   * Method Name:
   *  SmtFilter
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param filterFileName
   * @throws IOException 
   ***********************************************************/
  public SmtFilter(String filterFileName) throws IOException
  {
    super(filterFileName);
    String fName = convertSpecialFileName(filterFileName);
    if(fName != null)
      FilterFileName = fName;
  }
  
  /************************************************************
   * Method Name:
   *  SmtFilter
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param configMap
   * @throws IOException 
   ***********************************************************/
  public SmtFilter(Map<String, String> configMap) throws IOException
  {
    super(configMap, SmtProperty.SMT_FILTER_FILE.getName());
  }

  /************************************************************
   * Method Name:
   *  SmtFilter
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param whiteList
   * @param blackList
   ***********************************************************/
  public SmtFilter(String name, ArrayList<String> whiteList, ArrayList<String> blackList, ArrayList<String> fileList)
  {
    super(name, whiteList, blackList, fileList);
  }


  /************************************************************
   * Method Name:
   *  main
   **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   * @param args
   * @throws IOException 
   ***********************************************************/
  public static void main(String[] args) throws IOException
  {
    if((args != null) && (args.length > 0))
    {
      System.out.println("This is argument 0: " + args[0]);
      SmtFilter filter = new SmtFilter(args[0]);
      
      System.out.println("The white list is: " + filter.getWhiteList().size());
      System.out.println("The black list is: " + filter.getBlackList().size());
      System.out.println("The file list is: " + filter.getFileList().size());
      System.out.println("\nThe filter name is: " + filter.getFilterName());
      System.out.println("The filter ID is: " + filter.getFilterID().toString());
    }
  }

  /************************************************************
   * Method Name:
   *  createFilterFromCollection
  **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   * @param string
   * @param picked
   * @return
   ***********************************************************/
  public static SmtFilter createFilterFromCollection(String name, Collection picked)
  {
    // the collection is strings that represent nodes, selected from a graph
    // normally in the form of "name = guid"
    // use the guid, so parse everything after the = 
    //  by default, this is a white list
    //
    // the optional name is the filters name
    // if empty, include all results.  if not empty, only include results that contain a string in this list
    java.util.ArrayList<String> WhiteList     = new java.util.ArrayList<String>();
    String firstVertex = "unknown";
    for (Object v : picked)
    {
      String val = v.toString();
      String guid = val;
      int ndex  = val.indexOf("=");
      if(ndex > 0)
      {
        firstVertex = val.substring(0, ndex).trim();
        guid = val.substring(ndex+1).trim();
        WhiteList.add(guid);
      }
    }
    
    // make sure the filter has a name
    if((name == null) || (name.length() < 1))
      name = firstVertex;
    
    return new SmtFilter(name, WhiteList, null, null);
  }

}
