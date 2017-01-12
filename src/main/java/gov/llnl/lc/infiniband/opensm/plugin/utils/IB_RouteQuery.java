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
 *        file: IB_RouteQuery.java
 *
 *  Created on: Feb 11, 2014
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.utils;

import java.io.File;
import java.io.FileWriter;

import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.opensm.plugin.data.RT_Node;
import gov.llnl.lc.system.CommandLineArguments;
import gov.llnl.lc.system.CommandLineExecutor;
import gov.llnl.lc.system.CommandLineResults;

/**********************************************************************
 * Describe purpose and responsibility of IB_RouteQuery
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Feb 11, 2014 10:14:59 AM
 **********************************************************************/
public class IB_RouteQuery
{
  private String QueryCommand;
  private CommandLineResults Results;
  static final String GUID_QUERY          = "ibroute -G";

  /************************************************************
   * Method Name:
   *  getResults
   **/
  /**
   * Returns the value of results
   *
   * @return the results
   *
   ***********************************************************/
  
  public CommandLineResults getResults()
  {
    return Results;
  }

  public int queryGuid(IB_Guid swGuid) throws Exception
  {
    return query(GUID_QUERY + " 0x" + swGuid.toString());
  }
  
  public int query(String queryCommand) throws Exception
  {
    QueryCommand = queryCommand;
    CommandLineExecutor cmdExecutor = new CommandLineExecutor(new CommandLineArguments(queryCommand));    
    cmdExecutor.runCommand();
    Results = cmdExecutor.getResults();
    return Results.getReturnCode();    
  }
  
  /************************************************************
   * Method Name:
   *  main
   **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects

   * @param   describe the parameters
   *
   * @return  describe the value returned
   * @param args
   * @throws Exception 
   ***********************************************************/
  public static void main(String[] args)
  {
    IB_RouteQuery query = new IB_RouteQuery();
    int exitStatus = 0;
    try
    {
      IB_Guid swGuid = new IB_Guid("0x66a02e8001313");
      exitStatus = query.queryGuid(swGuid);
      System.out.println(exitStatus);
      System.out.println(query.Results.getReturnCode());
      System.out.println(query.Results.getError());
      System.out.println(query.Results.getOutput());
      
      // if the return code or exit status is zero, write to a file
      if(query.Results.getReturnCode() == 0)
      {
        File file = new File("/tmp/ib/switch.txt");
        file.getParentFile().mkdirs();
        FileWriter fw = new FileWriter(file);
        fw.write(query.Results.getOutput());
        fw.close();
        
        // test the parser on the string
        IB_RouteParser parser = new IB_RouteParser();
        parser.parseString(query.Results.getOutput());
        RT_Node node = parser.getRT_node();
        System.out.println("Parser Summary:(" + parser.getSummary() + ")");
        System.out.println("Node toString():(" + node.toString() + ")");
        System.out.println("Node routes:(" + node.getNumRoutes() + ")");

      }
      return;
    }
    catch (Exception ioe)
    {
      System.out.println("Query exception: " + ioe.getMessage());
    }
    
    System.out.println(query.toString());    
    return;
  }

  /************************************************************
   * Method Name:
   *  toString
  **/
  /**
   * Describe the method here
   *
   * @see java.lang.Object#toString()
   *
   * @return
   ***********************************************************/
  
  @Override
  public String toString()
  {
    return "IB_RouteQuery [QueryCommand=" + QueryCommand + ", Results=" + Results + "]";
  }
}
