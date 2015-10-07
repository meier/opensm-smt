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
 *        file: SmtCommandType.java
 *
 *  Created on: Jan 17, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**********************************************************************
 * Describe purpose and responsibility of SmtCommandType
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Jan 17, 2013 1:22:50 PM
 **********************************************************************/
public enum SmtCommandType
{
  SMT_FABRIC_CMD(        200, "smt-fabric",  "gov.llnl.lc.smt.command.fabric.SmtFabric",          "fabric",  "f",   "cmd args", "provides fabric level information"),
  SMT_NODE_CMD(          201, "smt-node",    "gov.llnl.lc.smt.command.node.SmtNode",              "node",    "n",   "cmd args", "provides node level information"),
  SMT_PORT_CMD(          202, "smt-port",    "gov.llnl.lc.smt.command.port.SmtPort",              "port",    "p",   "cmd args", "provides port level information"),
  SMT_LINK_CMD(          203, "smt-link",    "gov.llnl.lc.smt.command.link.SmtLink",              "link",    "l",   "cmd args", "provides link level information"),
  SMT_CONSOLE_CMD(       204, "smt-console", "gov.llnl.lc.smt.command.console.SmtConsole",        "console", "con", "cmd args", "a curses application for viewing OMS information"),
  SMT_TOP_CMD(           205, "smt-top",     "gov.llnl.lc.smt.command.top.SmtTop",                "top",     "t",   "cmd args", "shows top errors and traffic"),
  SMT_FILE_CMD(          206, "smt-file",    "gov.llnl.lc.smt.command.file.SmtFile",              "file",    "fn",  "cmd args", "provides information about OMS files"),
  SMT_ABOUT_CMD(         207, "smt-about",   "gov.llnl.lc.smt.command.about.SmtAbout",            "about",   "abt", "cmd args", "software package information"),
  SMT_RECORD_CMD(        208, "smt-record",  "gov.llnl.lc.smt.command.record.SmtRecord",          "record",  "rcd", "cmd args", "saves OMS information (flight recorder)"),
  SMT_CONFIG_CMD(        209, "smt-config",  "gov.llnl.lc.smt.command.config.SmtConfig",          "config",  "c",   "cmd args", "checks or modifies the SMT configuration"),
  SMT_ROUTE_CMD(         210, "smt-route",   "gov.llnl.lc.smt.command.route.SmtRoute",            "route",   "r",   "cmd args", "routing table tools"),
  SMT_GUI_CMD(           211, "smt-gui",     "gov.llnl.lc.smt.command.gui.SmtGui",                "gui",     "gui", "cmd args", "a gui fabric exploration tool"),
  SMT_EVENT_CMD(         213, "smt-event",   "gov.llnl.lc.smt.command.event.SmtEvent",            "event",   "e",   "cmd args", "shows SM events, traps, and exceptions"),
  SMT_ID_CMD(            214, "smt-id",      "gov.llnl.lc.smt.command.search.SmtIdentification",  "id",      "id",  "cmd args", "an identificaton tool (name resolver)"),
  SMT_HELP_CMD(          215, "smt-help",    "gov.llnl.lc.smt.command.help.SmtHelp",              "help",     "h",  "cmd args", "a gui help tool"),
  SMT_MCAST_CMD(         216, "smt-multicast","gov.llnl.lc.smt.command.route.SmtMulticast",       "multicast","m",  "cmd args", "a multicast group tool"),
  SMT_PART_CMD(          217, "smt-partition","gov.llnl.lc.smt.command.route.SmtPartition",       "partition","part","cmd args","a partition tool"),
  SMT_UTILIZE_CMD(       218, "smt-utilize", "gov.llnl.lc.smt.command.utilize.SmtUtilize",        "utilize",  "util","cmd args","show bandwitdh utilization"),
  SMT_PRIV_CMD(          220, "smt-priv",    "gov.llnl.lc.smt.command.privileged.SmtPrivileged",  "priv",    "pv",  "cmd args", "a set of privileged commands"),
  SMT_ALIAS_CMD(         300, "smt-config",  "gov.llnl.lc.smt.command.config.SmtConfig",          "config",  "c",   "cmd args", "checks or modifies the SMT configuration"),
  SMT_CONSOLE_MGR(       301, "smt-console", "gov.llnl.lc.smt.command.console.SmtConsoleManager", "console", "con", "cmd args", "a curses application for viewing OMS information"),
  SMT_GUI_APP(           302, "smt-gui-app", "gov.llnl.lc.smt.swing.SmtGuiApplication",           "gui-app", "ga",  "cmd args", "the swing framework for smt-gui"),
  SMT_FINAL_CMD(         400, "smt-config",  "gov.llnl.lc.smt.command.config.SmtConfig",          "config",  "c",   "cmd args", "checks or modifies the SMT configuration");

  /*
   *   This enum needs to change to something that supports commnand line options, such as
   *   int, CommandName, shortName, longName, Description, ArgName
   */
  public static final EnumSet<SmtCommandType> SMT_COMMON_CMDS = EnumSet.range(SMT_FABRIC_CMD, SMT_PRIV_CMD);
  
  public static final EnumSet<SmtCommandType> SMT_ALL_CMDS = EnumSet.allOf(SmtCommandType.class);
  
  private static final Map<Integer,SmtCommandType> lookup = new HashMap<Integer,SmtCommandType>();

  static 
  {
    for(SmtCommandType s : SMT_ALL_CMDS)
         lookup.put(s.getCommandNum(), s);
  }

  private static class AlphaCompare implements Comparator<SmtCommandType>
  {
    @Override
    public int compare(SmtCommandType q1, SmtCommandType q2) 
    {
        return q1.getName().compareTo(q2.getName());
    }
  }
  
  public static String describeAllQueryTypes()
  {
    return describeQueryTypes(SMT_ALL_CMDS);
  }

  public static String describeQueryTypes(EnumSet<SmtCommandType> eSet)
  {
    // put in alphabetical order
    TreeSet<SmtCommandType> ts = new TreeSet<SmtCommandType>(new AlphaCompare());
    for(SmtCommandType q : eSet)
    {
      if(q.equals(SMT_FINAL_CMD))
        break;
      ts.add(q);
    }

    int maxNameLength = getMaxNameLength(eSet);
    String formatString = "   %-" + maxNameLength + "s   %s\n";
    StringBuffer buff = new StringBuffer();
    // build a string describing the set
    buff.append("usage: smt-port [-h=<host url>] [-pn=<port num>] <node: guid, lid, or name>  <port #> -q <option>\n");
    buff.append(" all query options require a port description [(guid, lid, or name) + port #]\n");
    buff.append("  options include:\n");
    
    for(SmtCommandType q : ts)
    {
      buff.append(String.format(formatString, q.getName(), q.getDescription()));
    }
    return buff.toString();
  }
  
  private static int getMaxNameLength(EnumSet<SmtCommandType> eSet)
  {
    int len = 0;
    for(SmtCommandType q : eSet)
    {
      if(q.equals(SMT_FINAL_CMD))
        break;
      
      len = len > q.getName().length() ? len: q.getName().length();
    }
    return len;
  }



  
  private int CmdNum;
  
  // suitable for a property file, class name
  private String CommandName;
  
  // this is the script name, that people will be familiar with
  private String ToolName;
  
  // the normal full name, suitable for the long command line
  private String Name;
  
  // a short name, perhaps a single letter, suitable for the short command line
  private String ShortName;
  
  // if the name takes an argument, this would describe the argument
  private String ArgName;
  
  // a description of the property, normally just a single line, suitable for "usage"
  private String Description;

  private SmtCommandType(int CmdNum, String ToolName, String CommandName, String Name, String ShortName, String ArgName, String Description)
  {
      this.CmdNum     = CmdNum;
      this.ToolName     = ToolName;
      this.CommandName = CommandName;
      this.Name         = Name;
      this.ShortName    = ShortName;
      this.ArgName      = ArgName;
      this.Description  = Description;
  }

public int getCommandNum()
{
  return CmdNum;
  }

public String getToolName()
{
  return ToolName;
  }

public String getCommandName()
{
  return CommandName;
  }

public String getName()
{
  return Name;
  }

public static SmtCommandType get(int CmdNum)
{ 
    return lookup.get(CmdNum); 
}

public static SmtCommandType getByName(String Name)
{
  SmtCommandType p = null;
  
  // return the first property with an exact name match
  for(SmtCommandType s : SMT_ALL_CMDS)
  {
    if(s.getName().equals(Name))
      return s;
  }
  return p;
}

public static SmtCommandType getByCommandName(String CommandName)
{
  SmtCommandType p = null;
  
  // return the first property with an exact name match
  for(SmtCommandType s : SMT_ALL_CMDS)
  {
    if(s.getCommandName().equals(CommandName))
      return s;
  }
  return p;
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
 *  getArgName
 **/
/**
 * Returns the value of argName
 *
 * @return the argName
 *
 ***********************************************************/

public String getArgName()
{
  return ArgName;
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

public String getCmdDescription()
{
  return String.format("%-14s - %s", getToolName()+",", getDescription());
}
}
