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
 *        file: SmtProperty.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.props;

import gov.llnl.lc.net.NetworkConstants;

import java.io.Serializable;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**********************************************************************
 * An SmtPropertyEnum supports smt command configuration.  This includes
 * command line parsing, as well as configuration files.
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Jan 11, 2013 2:48:46 PM
 **********************************************************************/
public enum SmtProperty implements Serializable, NetworkConstants
{
  SMT_HOST(                     0, NET_SERVICE_HOSTNAME_KEY, "host",        "h",        "host url", "the url of the host with the OpenSM Monitoring Service"),    
  SMT_PORT(                     1, NET_SERVICE_PORTNUM_KEY,  "portn",       "pn",       "port #",   "the number associated with the OpenSM Monitoring Service"),
  SMT_REUSE(                    2, "reuse connection",       "reuse",       "rc",       "t/f",      "boolean indicating if the connection should be maintained (t) and reused each time, or discarded (default)"),
  SMT_WRAP_DATA(                3, "wrap data",              "wrap",        "wrap",     "t/f",      "boolean indicating if reading & playback of data from file should wrap around and continue, or stop when end is reached"),
  SMT_SINGLE_SHOT(              4, "single snapshot",        "once",        "once",     "",          "use a single snapshot of the fabric data"),
  SMT_PLAY_CONTROL(             5, "play controls",          "playControl", "pControl", "",          "use gui controls for playback"),
  SMT_DUMP(                     6, "raw dump",               "dump",        "dump",     "",          "raw dump"),
  SMT_ID_PORT(                  7, "port id",                "nodePort",    "p",     "port number",  "return port specific results"),
  SMT_QUERY_LIST(             100, "SmtQuery.list",          "queryList",   "ql",        "query option",      "list the available query options"),
  SMT_QUERY_TYPE(             101, "SmtQuery.type",          "query",       "q",        "query option",      "the type of query to perform"),
  SMT_QUERY_ALL(              102, "SmtQuery.all",           "queryAll", "qa", "", "searches everything"),
  SMT_QUERY_NAME(             103, "SmtQuery.name",          "queryName", "qn", "name", "searches for a matching name or description"),
  SMT_QUERY_GUID(             104, "SmtQuery.quid",          "queryGuid", "qg", "guid", "searches for a matching guid"),
  SMT_QUERY_LID(              100, "SmtQuery.lid",           "queryLid", "ql", "lid", "searches for a matching lid"),
  SMT_QUERY_PORT(             105, "SmtQuery.port",          "queryPort", "qp", "guid:port", "searches for a matching guid:port or port guid"),
  SMT_QUERY_STRING(           106, "SmtQuery.string",        "queryString", "qs", "string", "searches everything for a matching string"),
  SMT_QUERY_SWITCH(           107, "SmtQuery.switch",        "switches", "SW", " ", "selects all switches"),
  SMT_QUERY_CA(               108, "SmtQuery.ca",            "channelAdapter", "CA", " ", "selects all channel adapters"),
  SMT_QUERY_LEVEL(            109, "SmtQuery.ca",            "level", "L", "level value", "selects links at this level (0=CA, 1=leaf SW, 2=SW...)"),
  SMT_ONLY_MISSING(           110, "SmtQuery.omissing",      "onlyMissing", "oM", "t/f", "boolean that selects only the Missing elements if t, and only the Good ones if f (both if option is not specified - default)"),
  SMT_INCLUDE_MISSING(        111, "SmtQuery.imissing",      "includeMissing", "iM", "t/f", "includes Missing or Unknown elements if true"),
  SMT_STATUS(                 118, "SmtCommand.status",      "status", "sr", "", "provides a status report"),
  SMT_LIST(                   119, "SmtCommand.list",        "list", "lp", "", "lists or prints the object"),
  SMT_COMMAND(                120, "SmtCommand",             "command", "cmd", "", "the last smt command"),
  SMT_COMMAND_ARGS(            99, "SmtCcommand.args",       "commandArgs", "args", "args", "an argument for the command"),
  SMT_SUBCOMMAND(             121, "SmtSubcommand",          "subcommand", "sub", "arg", "the last smt sub-command"),
  SMT_SUBCOMMAND_ARG(         122, "SmtSubcommand.arg",      "subcommandArg", "arg", "arg", "an argument for the sub-command"),
  SMT_SUBCOMMAND_VAL(         123, "SmtSubcommand.val",      "subcommandVal", "val", "val", "the value of the argument"),
  SMT_CONFIG_COMMAND(         124, "gov.llnl.lc.smt.command.config.SmtConfig", "subcommand", "sub", "arg", "the last smt sub-command"),
  SMT_FILE_COMMAND(           125, "gov.llnl.lc.smt.command.file.SmtFile",     "subcommand", "sub", "arg", "the last smt sub-command"),
  SMT_CONSOLE_COMMAND(        126, "gov.llnl.lc.smt.command.console.SmtConsole","subcommand", "sub", "arg", "the last smt sub-command"),
  SMT_GUI_COMMAND(            127, "gov.llnl.lc.smt.command.gui.SmtGui",    "SmtGui", "sub", "arg", "the last smt sub-command"),
  SMT_VERSION(                128, "version",                 "version", "v", "", "print the version"),
  SMT_ABOUT_COMMAND(          129, "gov.llnl.lc.smt.command.about.SmtAbout",   "subcommand", "sub", "arg", "the last smt sub-command"),
  SMT_READ_CONFIG(            130, "SmtConfig.read",          "readConfig", "rC", "filename", "reads the specified configuration file"),
  SMT_WRITE_CONFIG(           131, "SmtConfig.write",         "writeConfig", "wC", "filename", "writes the configuration to the specified file"),
  SMT_READ_OMS(               132, "SmtOMS.read",             "readOMS", "rO", "filename", "reads the specified OMS data file"),
  SMT_WRITE_OMS(              133, "SmtOMS.write",            "writeOMS", "wO", "filename", "writes the OMS data to the specified file"),
  SMT_READ_FABRIC(            134, "SmtFabric.read",          "readFabric", "rF", "filename", "reads the specified Fabric data file"),
  SMT_WRITE_FABRIC(           135, "SmtFabric.write",         "writeFabric", "wF", "filename", "writes the Fabric data to the specified file"),
  SMT_READ_DELTA(             136, "SmtDelta.read",           "readDelta", "rD", "filename", "reads the specified Fabric Delta data file"),
  SMT_WRITE_DELTA(            137, "SmtDelta.write",          "writeDelta", "wD", "filename", "writes the Fabric Delta data to the specified file"),
  SMT_READ_DELTA_HISTORY(     138, "SmtHistory.read",         "readDeltaHistory", "rDH", "filename", "reads the specified Delta History data file"),
  SMT_WRITE_DELTA_HISTORY(    139, "SmtHistory.write",        "writeDeltaHistory", "wDH", "filename", "writes the Delta History data (fabric delta collection) to the specified file (requires -nr|-nh|-nm)"),
  SMT_READ_OMS_HISTORY(       140, "SmtHistory.read",         "readOMSHistory", "rH", "filename", "reads the specified OMS History (flight recorder) data file"),
  SMT_WRITE_OMS_HISTORY(      141, "SmtHistory.write",        "writeOMSHistory", "wH", "filename", "writes the OMS History data (flight recorder) to the specified file (requires -nr|-nh|-nm)"),
  SMT_HISTORY_RECORDS(        142, "SmtHistory.records",      "numRecords", "nr", "# to record", "specifies the number of Fabrics instances to record in the History"),
  SMT_HISTORY_MINUTES(        143, "TimeUnit.MINUTES",        "numMinutes", "nm", "# minutes to record", "specifies the length of time, in minutes, to record the Fabric History"),
  SMT_HISTORY_HOURS(          144, "TimeUnit.HOURS",          "numHours", "nh", "# hours to record", "specifies the length of time, in hours, to record the Fabric History"),
  SMT_READ_RT_TABLE(          150, "SmtRoute.read",           "readRoute", "rR", "filename", "reads the specified Routing Table file"),
  SMT_WRITE_RT_TABLE(         151, "SmtRoute.write",          "writeRoute", "wR", "filename", "writes the Routing Table to the specified file"),
  SMT_FILE_OP(                190,  "SmtFabric.file.operation", "fop", "fo", "r or w", "the value of the operation, either read, write, or error"),
  SMT_FILE_TYPE(              191,  "SmtFile.type",           "type",  "t",  "filename", "discovers the type of the specified file"),
  SMT_FILE_NAME(              192,  "SmtFile.name",           "fname", "fn", "filename", "the name of the file in question"),
  SMT_FILE_INFO(              193,  "SmtFile.info",           "info",  "i",  "filename", "provides a summary of the contents of the file"),
  SMT_FILTER_FILE(            194,  "SmtFile.info",           "filter",  "filter",  "filename", "a text file containing filter strings (* White/Black list)"),
  SMT_LIST_TIMESTAMP(         195,  "SmtFile.time",           "listTimes", "lts", "", "lists or prints the timestamps"),
  SMT_FILE_COMPRESS(          196,  "SmtFile.compress",       "compress",  "c", "factor> <out filename", "writes a compressed file, by skipping snapshots (2 is 1/2 size, 4 is 1/4, 10 is 1/10, etc.)"),
  SMT_FILE_NUM_SKIPS(         197,  "SmtFile.compress",       "skips   ",  "c", "# to skip", "writes a compressed file, by skipping snapshots"),
  SMT_FILE_EXTRACT(           198,  "SmtFile.extract",        "extract ",  "x", "t1> <t2> <out filename", "extracts a subset of snapshots from the file, specified by the initial (t1) and final (t2) times"),
  SMT_FILE_TSTAMP_1(          199,  "SmtFile.timestamp1",     "ts1   ",  "t1", "# to skip", "writes a compressed file, by skipping snapshots"),
  SMT_FILE_TSTAMP_2(          200,  "SmtFile.timestamp2",     "ts2   ",  "t2", "# to skip", "writes a compressed file, by skipping snapshots"),
  SMT_GRAPH_ANIMATED_BAR_PCC( 400,  "Animated.bar.graph",     "graph",  "gb",  "filename", "provides a bar graph"),
  SMT_OUT_TYPE(               300, "output type",             "output", "o", "type", "the desired output format (text, xml, jason)"),
  SMT_PRIV_COMMAND(           900, "gov.llnl.lc.smt.command.privileged.SmtPrivileged",   "subcommand", "sub", "arg", "the last smt sub-command"),
  SMT_PRIV_EXT(               901, "SmtPrivileged.Ext",       "rCommand",      "X",  "command with args", "invokes a command on the mgmt node"),
  SMT_PRIV_ENABLE(            902, "SmtPrivileged.enable",    "enablePort",    "eP", "guid or lid> <portNum", "enables the specified port"),
  SMT_PRIV_DISABLE(           903, "SmtPrivileged.disable",   "disablePort",   "dP", "guid or lid> <portNum", "disables the specified port"),
  SMT_PRIV_QUERY_PORT(        911, "SmtPrivileged.query",     "queryPort",     "qP", "guid or lid> <portNum", "queries the specified port"),
  SMT_PRIV_UPDATE_DESC(       904, "SmtPrivileged.updateDesc","updateDesc",    "uD", "arg", "updates the node descriptions"),
  SMT_PRIV_REROUTE(           905, "SmtPrivileged.reroute",   "re-route",      "rt", "arg", "re-routes the fabric"),
  SMT_PRIV_LT_SWEEP(          906, "SmtPrivileged.lt_sweep",  "liteSweep",     "lS", "arg", "forces a light sweep of the fabric"),
  SMT_PRIV_HV_SWEEP(          907, "SmtPrivileged.hv_sweep",  "heavySweep",    "hS", "arg", "forces a heavy sweep of the fabric"),
  SMT_PRIV_PM_SWEEP(          908, "SmtPrivileged.pm_sweep",  "pfmgrSweep",    "pS", "arg", "forces the performance manager to sweep the fabric"),
  SMT_PRIV_PM_CLEAR(          909, "SmtPrivileged.pm_clear",  "clearCounters", "cC", "arg", "clears all the port counters"),
  SMT_PRIV_NAME(              910, "SmtPrivileged.Name",      "privname",    "pN", "arg", "the last smt sub-command"),
  SMT_PRIV_PM_SWEEP_PERIOD(   911, "SmtPrivileged.pm_sw_period",  "pfmgrSweepPeriod",    "pSS", "seconds", "sets the performance manager to sweep period in secs."),
  SMT_PRIV_OSM_LOG_LEVEL(     912, "SmtPrivileged.osm_log_level",  "osmLogLevel",    "oLL", "level value", "sets the opensm.log verbosity level"),
  SMT_HELP_COMMAND(           997, "gov.llnl.lc.smt.command.help.SmtHelp",   "subcommand", "sub", "arg", "the last smt sub-command"),
  SMT_HELP(                   998, "Help", "Help", "?", "", "print this message"),
  SMT_LOG_FILE(               200, "java.util.logging.FileHandler.pattern", "logFile", "lf", "file name", "the file name or pattern to use for log files"),
  SMT_LOG_LEVEL(              201, "java.util.logging.ConsoleHandler.level", "logLevel", "ll", "log level", "the verbosity level for log files"),
  SMT_FABRIC_DELTA_COLLECTION_FILE(210, "gov.llnl.lc.infiniband.opensm.plugin.data.OSM_FabricDeltaCollection", "readFabricDeltaCollectionOnly", "toInfo", "toTimeString", "the file of the Fabric cache"),
  SMT_OMS_COLLECTION_FILE(    202, "gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Collection",            "readOMS_Collection",        "toInfo", "toTimeString", "the file of the OMS collection cache"),
  SMT_FABRIC_COLLECTION_FILE( 205, "gov.llnl.lc.infiniband.opensm.plugin.data.OSM_FabricCollection",      "readFabricCollection",      "toInfo", "toTimeString", "the file of the Fabric cache"),
  SMT_OMS_FILE(               207, "gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService",      "readOMS",                   "toInfo", "toTimeString", "the file name of the OMS cache"),
  SMT_FABRIC_FILE(            208, "gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric",                "readFabric",                "toInfo", "toTimeString", "the file of the Fabric cache"),
  SMT_FABRIC_DELTA_FILE(      209, "gov.llnl.lc.infiniband.opensm.plugin.data.OSM_FabricDelta",           "readFabricDelta",           "toInfo", "toTimeString", "the file of the Fabric cache"),
  SMT_ROUTE_TABLE_FILE(       204, "gov.llnl.lc.infiniband.opensm.plugin.data.RT_Table",                  "readRT_Table",                "toInfo", "toTimeString", "the file of the routing table"),
  SMT_CONFIG_FILE(            203, "gov.llnl.lc.smt.command.config.SmtConfig",                            "readConfig",                "toInfo", "toTimeString", "the file of the configuration file"),
  SMT_FABRIC_CONFIG_FILE(     206, "gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Configuration",         "readConfig",                "toInfo", "toTimeString", "the file of the configuration file"),
  SMT_FABRIC_CONFIG_CMD(      212, "SmtFabric.ibFabricConfig",                                            "fabricConfig",             "fC", "toTimeString", "displays the ibfabricconf.xml"),
  SMT_NODE_MAP_CMD(           213, "SmtFabric.nodeNameMap",                                               "nodeNameMap",              "nM", "toTimeString", "displays the ib-node-name-map"),
  SMT_UTILIZE_COMMAND(        214, "gov.llnl.lc.smt.command.utilize.SmtUtilize",    "utilize",               "u",      "file name", "the file of the configuration file"),
  SMT_TOP_COMMAND(            220, "gov.llnl.lc.smt.command.top.SmtTop",            "top",                       "t",      "file name", "the file of the configuration file"),
  SMT_TOP_NUMBER(             221, "SmtTop.number",                                 "topNum",                "tN", "# top lines", "the number of top result lines"),
  SMT_NODE_TRAFFIC(           222, "SmtTop.nodeTraffic",                            "nodeTraffic",           "nT", "# top lines", "the number of top result lines"),
  SMT_NODE_ERRORS(            223, "SmtTop.nodeErrors",                            "nodeErrors",             "nE", "# top lines", "the number of top result lines"),
  SMT_LINK_TRAFFIC(           224, "SmtTop.linkTraffic",                            "linkTraffic",           "lT", "# top lines", "the number of top result lines"),
  SMT_LINK_ERRORS(            225, "SmtTop.linkErrors",                            "linkErrors",             "lE", "# top lines", "the number of top result lines"),
  SMT_PORT_TRAFFIC(           226, "SmtTop.portTraffic",                            "portTraffic",           "pT", "# top lines", "the number of top result lines"),
  SMT_PORT_ERRORS(            227, "SmtTop.portErrors",                            "portErrors",             "pE", "# top lines", "the number of top result lines"),
  SMT_ROUTE_PATH(             230, "gov.llnl.lc.infiniband.opensm.plugin.data.RT_Path", "path",              "P", "toTimeString", "a Route, or Path through the fabric"),
  SMT_ROUTE_SWITCH_INFO(      230, "gov.llnl.lc.infiniband.opensm.plugin.data.RT_Path", "switchInfo",        "sI", "toTimeString", "information about the switch tables"),
  SMT_ROUTE_TABLE_INFO(       230, "gov.llnl.lc.infiniband.opensm.plugin.data.RT_Path", "tableInfo",         "tI", "toTimeString", "information about the overall routing table"),
  SMT_FABRIC_DISCOVER(        300, "discover",                                      "discover",               "dF", "# attempts", "attempt to discover OMS/SMT ports (on-line only)"),
  SMT_UPDATE_PERIOD(          800, "Online.update.period",                          "update",                "uS", "# secs", "the interval (in secs) between data updates from the service"),
  SMT_UPDATE_MULTIPLIER(      801, "Offline.playback.multiplier",                    "playX",                "pX", "# times faster", "play back history this # times faster than normal"),
  SMT_TIMESTAMP(              810, "gov.llnl.lc.time.TimeStamp",                    "timeStamp",             "ts", "Aug 01 16:10:32 2013", "the time of the OMS data within the OMS History file"),
  SMT_USE_DEFAULT(            998, "DEFAULT",                    "Use Default",             "default", "", "a directive to use the default if necessary"),
  SMT_LAST_PROPERTY(          999, "PropertyEnd",                            "EndOfList",                "end", "", "always the end of the property list");
  
  
  /*
   *   This enum needs to change to something that supports commnand line options, such as
   *   int, PropertyName, shortName, longName, Description, ArgName
   */
  public static final EnumSet<SmtProperty> SMT_ALL_PROPERTIES = EnumSet.allOf(SmtProperty.class);
  
  /**  the commands that should never get an initial instance of the OMS **/
  public static final EnumSet<SmtProperty> SMT_NO_OMS_INIT_COMMANDS = EnumSet.of(SMT_CONFIG_COMMAND, SMT_FILE_COMMAND, SMT_FABRIC_DISCOVER, SMT_ABOUT_COMMAND, SMT_PRIV_COMMAND, SMT_HELP_COMMAND, SMT_HELP, SMT_TOP_COMMAND);
  
  /**  the various types of files supported by SMT (ordered from big file, to small file for optimization purposes **/
  public static final EnumSet<SmtProperty> SMT_FILE_TYPES = EnumSet.range(SMT_FABRIC_DELTA_COLLECTION_FILE, SMT_FABRIC_CONFIG_FILE);
  
  /**  the various types of files supported by SMT (ordered from big file, to small file for optimization purposes **/
  public static final EnumSet<SmtProperty> SMT_TOP_TYPES = EnumSet.range(SMT_NODE_TRAFFIC, SMT_PORT_ERRORS);
  
  private static final Map<Integer,SmtProperty> lookup = new HashMap<Integer,SmtProperty>();

  static 
  {
    for(SmtProperty s : SMT_ALL_PROPERTIES)
         lookup.put(s.getProperty(), s);
  }

  private static class PropertyCompare implements Comparator<SmtProperty>
  {
    @Override
    public int compare(SmtProperty q1, SmtProperty q2) 
    {
        return (q1.getProperty() - q2.getProperty());
    }
  }
  

  private int Property;
  
  // suitable for a property file
  private String PropertyName;
  
  // the normal full name, suitable for the long command line
  private String Name;
  
  // a short name, perhaps a single letter, suitable for the short command line
  private String ShortName;
  
  // if the name takes an argument, this would describe the argument
  private String ArgName;
  
  // a description of the property, normally just a single line, suitable for "usage"
  private String Description;

  private SmtProperty(int Property, String PropertyName, String Name, String ShortName, String ArgName, String Description)
  {
      this.Property     = Property;
      this.PropertyName = PropertyName;
      this.Name         = Name;
      this.ShortName    = ShortName;
      this.ArgName      = ArgName;
      this.Description  = Description;
  }

public int getProperty()
{
  return Property;
  }

public String getPropertyName()
{
  return PropertyName;
  }

public String getName()
{
  return Name;
  }

public static SmtProperty get(int Property)
{ 
    return lookup.get(Property); 
}

public static boolean isSkipOMSCommand(String cmdName)
{
  // return true if this is a command that doesn't want an initial oms
  for(SmtProperty s : SMT_NO_OMS_INIT_COMMANDS)
  {
    if(s.getPropertyName().equals(cmdName))
      return true;
  }
  return false;
}



public static SmtProperty getByName(String Name)
{
  SmtProperty p = null;
  
  // return the first property with an exact name match
  for(SmtProperty s : SMT_ALL_PROPERTIES)
  {
    if(s.getName().equals(Name))
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

/************************************************************
 * Method Name:
 *  sortPropertySet
**/
/**
 * Describe the method here
 *
 * @see     describe related java objects
 *
 * @param eSet
 * @return
 ***********************************************************/
public static TreeSet<SmtProperty> sortPropertySet(EnumSet<SmtProperty> eSet)
{
  // put in numerical order
  TreeSet<SmtProperty> ts = new TreeSet<SmtProperty>(new PropertyCompare());
  for(SmtProperty q : eSet)
  {
    if(q.equals(SMT_LAST_PROPERTY))
      break;
    ts.add(q);
  }
  return ts;
}

}
