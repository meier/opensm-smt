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
 *        file: SMT_SearchResult.java
 *
 *  Created on: Apr 29, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command.search;

import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;

/**********************************************************************
 * Describe purpose and responsibility of SMT_SearchResult
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Apr 29, 2015 2:52:14 PM
 **********************************************************************/
public class SMT_SearchResult
{
  private SMT_SearchResultType Type;
  
  private String               SearchString;
  
  /** this is the search context, the OMS **/
  private OpenSmMonitorService OMS;
  
  private String               Name;

  private IB_Guid              Guid;
  
  private int                  IntVal;
  
  private long                 LongVal;
  
  private Object               ResultObject;

  /************************************************************
   * Method Name:
   *  SMT_SearchResult
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param type
   * @param searchString
   * @param oms 
   * @param name
   * @param guid
   * @param intVal
   * @param longVal
   ***********************************************************/
  public SMT_SearchResult(SMT_SearchResultType type, String searchString, Object object, OpenSmMonitorService oms)
  {
    super();
    Type         = type;
    SearchString = searchString;
    ResultObject = object;
    OMS          = oms;
  }

  /************************************************************
   * Method Name:
   *  getType
   **/
  /**
   * Returns the value of type
   *
   * @return the type
   *
   ***********************************************************/
  
  public SMT_SearchResultType getType()
  {
    return Type;
  }

  /************************************************************
   * Method Name:
   *  getSearchString
   **/
  /**
   * Returns the value of searchString
   *
   * @return the searchString
   *
   ***********************************************************/
  
  public String getSearchString()
  {
    return SearchString;
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
   *  getGuid
   **/
  /**
   * Returns the value of guid
   *
   * @return the guid
   *
   ***********************************************************/
  
  public IB_Guid getGuid()
  {
    return Guid;
  }

  /************************************************************
   * Method Name:
   *  getIntVal
   **/
  /**
   * Returns the value of intVal
   *
   * @return the intVal
   *
   ***********************************************************/
  
  public int getIntVal()
  {
    return IntVal;
  }

  /************************************************************
   * Method Name:
   *  getLongVal
   **/
  /**
   * Returns the value of longVal
   *
   * @return the longVal
   *
   ***********************************************************/
  
  public long getLongVal()
  {
    return LongVal;
  }

  /************************************************************
   * Method Name:
   *  getResultObject
   **/
  /**
   * Returns the value of resultObject
   *
   * @return the resultObject
   *
   ***********************************************************/
  
  public Object getResultObject()
  {
    return ResultObject;
  }

  /************************************************************
   * Method Name:
   *  getOMS
   **/
  /**
   * Returns the value of oMS
   *
   * @return the oMS
   *
   ***********************************************************/
  
  public OpenSmMonitorService getOMS()
  {
    return OMS;
  }
  
  


}
