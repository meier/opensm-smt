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
 *        file: SmtScreenType.java
 *
 *  Created on: Mar 21, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command.console;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import jcurses.system.InputChar;

/**********************************************************************
 * Used to control the screen page navigation, and also the table of
 * contents and footer information.  The Key Character specifies how to
 * get to the page described by the class.
 * <p>
 * The order below is how it will show up in the TOC and the footer,
 * So put it in the order desired, probably by key.
 * <p>
 * If re-ordering, be mindful of the EnumSet definitions.
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Mar 21, 2013 4:13:52 PM
 **********************************************************************/
public enum SmtScreenType
{
  SCN_QUIT(       0, "Quit", "Quit", "no class", InputChar.KEY_END, "Esc", "Gracefully exit the application"),    
  SCN_CONTENTS(   1, "Contents", "Menu", "gov.llnl.lc.smt.command.console.SmtConsoleTOC_Screen", InputChar.KEY_F2, "F2", "This screen, a list of other screens"),    
  SCN_STATUS(     2, "Subnet Status", "Status", "gov.llnl.lc.smt.command.console.SubnetStatusScreen", InputChar.KEY_F3, "F3", "Show the status of the subnet"),    
  SCN_PORT_COUNT( 8, "PerfMgr Port Counters", "PerfMgr", "gov.llnl.lc.smt.command.console.PerfMgrPortCounterScreen", InputChar.KEY_F4, "F4", "PerfMgr Port Counters in aggregated form"),    
  SCN_PORT_STAT(  6, "OpenSM Port Stats", "Ports", "gov.llnl.lc.smt.command.console.PortStatisticsScreen", InputChar.KEY_F5, "F5", "Show the number and types of Ports in the fabric"),    
  SCN_LINK_STAT(  7, "OpenSM Link Stats", "Links", "gov.llnl.lc.smt.command.console.LinkStatisticsScreen", InputChar.KEY_F6, "F6", "Show the number and types of Links in the fabric"),    
  SCN_NODE_STAT(  5, "OpenSM Node Stats", "Nodes", "gov.llnl.lc.smt.command.console.NodeStatisticsScreen", InputChar.KEY_F7, "F7", "Show the number and types of Nodes in the fabric"),    
  SCN_CONFIG(     3, "Configuration", "Config", "gov.llnl.lc.smt.command.console.SubnetConfigScreen", InputChar.KEY_F8, "F8", "Show a list of subnet options"),
  SCN_SERVICE(    4, "Monitor Service", "Srvc", "gov.llnl.lc.smt.command.console.OpenSmMonitorServiceScreen", InputChar.KEY_F9, "F9", "Observe and administer the Monitor Service"),    
  SCN_TOP_PORT_T(16, "Top Port Traffic", "TopPT", "gov.llnl.lc.smt.command.console.TopPortTrafficScreen", 293, "^F5", "Top Port Traffic"),    
  SCN_TOP_LINK_T(17, "Top Link Traffic", "TopLT", "gov.llnl.lc.smt.command.console.TopLinkTrafficScreen",294, "^F6", "Top Link Traffic"),    
  SCN_TOP_NODE_T(18, "Top Node Traffic", "TopNT", "gov.llnl.lc.smt.command.console.TopNodeTrafficScreen", 295, "^F7", "Top Node Traffic"),    
  SCN_TOP_PORT_E(19, "Top Port Errors", "TopPE", "gov.llnl.lc.smt.command.console.TopPortErrorScreen", 281, "sF5", "Top Port Errors"),    
  SCN_TOP_LINK_E(20, "Top Link Errors", "TopLE", "gov.llnl.lc.smt.command.console.TopLinkErrorScreen",282, "sF6", "Top Link Errors"),    
  SCN_TOP_NODE_E(21, "Top Node Errors", "TopNE", "gov.llnl.lc.smt.command.console.TopNodeErrorScreen", 283, "sF7", "Top Node Errors"),    
  SCN_ENDTYPE(  999, "EndOfType", "END", "no class", 0, "unknown", "invalid: end of list");    
  
  
  public static final EnumSet<SmtScreenType> SCN_ALL_SCREENS = EnumSet.allOf(SmtScreenType.class);
  /** this is all the "displayable" screens, so the range needs to start with the first one, and end with the last **/
  public static final EnumSet<SmtScreenType> SCN_SCREENS = EnumSet.range(SCN_CONTENTS, SCN_TOP_NODE_E);
  
  private static final Map<Integer,SmtScreenType> lookup = new HashMap<Integer,SmtScreenType>();

  static 
  {
    for(SmtScreenType s : SCN_ALL_SCREENS)
         lookup.put(s.getScreenNum(), s);
  }
  
// a random meaningless number that must be unique
  private int ScreenNum;
  
  // the name of the screen
  private String ScreenName;
  
  // an abbreviated version of above
  private String ShortScreenName;
  
  // the class that implements the screen
  private String ClassName;
  
  // the jcurses keycode used to invoke the screen
  private int keyCode;
  
  // a string representation of the key(s) that generate the above keyCode
  private String keyString;

  // a description of the screen and its purpose (can be long)
  private String Description;


private SmtScreenType(int screenNum, String screenName, String shortScreenName, String className,
      int keyCode, String keyString, String description)
  {
    ScreenNum = screenNum;
    ScreenName = screenName;
    ShortScreenName = shortScreenName;
    ClassName = className;
    this.keyCode = keyCode;
    this.keyString = keyString;
    Description = description;
  }


public int getScreenNum()
{
  return ScreenNum;
}


public String getScreenName()
{
  return ScreenName;
}


public String getShortScreenName()
{
  return ShortScreenName;
}


public String getClassName()
{
  return ClassName;
}


public int getKeyCode()
{
  return keyCode;
}


public String getKeyString()
{
  return keyString;
}


public String getDescription()
{
  return Description;
}


public static SmtScreenType get(int ScreenNum)
{ 
    return lookup.get(ScreenNum); 
}

public static SmtScreenType getNext(SmtScreenType type)
{
  // return the next type in the order, or wrap if out of range
  if(type==null)
    return null;
  int ndex = type.ordinal() + 1;
  if(SmtScreenType.SCN_ENDTYPE.ordinal() <= ndex )
    ndex = SmtScreenType.SCN_QUIT.ordinal()+1;
  
  return SmtScreenType.values()[ndex];
}

public static SmtScreenType getPrev(SmtScreenType type)
{
  // return the next type in the order, or wrap if out of range
  if(type==null)
    return null;
  int ndex = type.ordinal() - 1;
  if(SmtScreenType.SCN_QUIT.ordinal() >= ndex )
    ndex = SmtScreenType.SCN_ENDTYPE.ordinal()-1;
  
  return SmtScreenType.values()[ndex];
}

public static SmtScreenType getByName(String Name)
{
  SmtScreenType t = null;
  
  // return the first property with an exact name match
  for(SmtScreenType s : SCN_ALL_SCREENS)
  {
    if(s.getScreenName().equals(Name))
      return s;
  }
  return t;
}

public static SmtScreenType getByKeyCode(int code)
{
  SmtScreenType t = null;
  
  // return the first property with an exact name match
  for(SmtScreenType s : SCN_ALL_SCREENS)
  {
    if(s.getKeyCode() == code)
      return s;
  }
  return t;
}




}
