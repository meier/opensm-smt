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
 *        file: SmtMessageType.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.event;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**********************************************************************
 * Describe purpose and responsibility of SmtMessageType
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Sep 30, 2013 11:28:30 AM
 **********************************************************************/
public enum SmtMessageType
{
  SMT_MSG_SEVERE(          0, "SEVERE"),    
  SMT_MSG_WARNING(         1, "WARNING"),    
  SMT_MSG_INFO(            2, "INFO"),    
  SMT_MSG_DEBUG(           3, "DEBUG"),    
  SMT_MSG_INIT(            9, "INIT"),    
  SMT_MSG_MAX(            10, "final message");
  
  public static final EnumSet<SmtMessageType> SMT_ALL_MESSAGES = EnumSet.allOf(SmtMessageType.class);
  
  private static final Map<Integer,SmtMessageType> lookup = new HashMap<Integer,SmtMessageType>();

  static 
  {
    for(SmtMessageType s : SMT_ALL_MESSAGES)
         lookup.put(s.getMessageID(), s);
  }

  private int MsgNum;
  private String MsgName;

private SmtMessageType(int MsgNum, String Name)
{
    this.MsgNum = MsgNum;
    this.MsgName = Name;
}

public int getMessageID()
{
  return MsgNum;
  }

public String getMessageName()
{
  return MsgName;
  }

public static SmtMessageType get(int MsgNum)
{ 
    return lookup.get(MsgNum); 
}


}
