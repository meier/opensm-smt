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
 *        file: SmtMulticast.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command.route;

import gov.llnl.lc.infiniband.core.IB_Address;
import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.infiniband.opensm.plugin.data.SBN_MulticastGroup;
import gov.llnl.lc.infiniband.opensm.plugin.net.OsmServerStatus;
import gov.llnl.lc.smt.SmtConstants;
import gov.llnl.lc.smt.command.SmtCommand;
import gov.llnl.lc.smt.command.config.SmtConfig;
import gov.llnl.lc.smt.props.SmtProperty;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

/**********************************************************************
 * SmtMulticast provides primitive query operations for showing
 * one to many communication attributes.
 * 
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Sep 11, 2013 8:13:06 AM
 **********************************************************************/
public class SmtMulticast extends SmtCommand
{
  private LinkedHashMap<String, SBN_MulticastGroup> McastGroups;

  /************************************************************
   * Method Name:
   *  doCommand
   **/
  /**
   * Describe the method here
   *
   * @see gov.llnl.lc.smt.command.SmtCommand#doCommand(gov.llnl.lc.smt.command.config.SmtConfig)
   *
   * @param config
   * @return
   * @throws Exception
   ***********************************************************/

  @Override
  public boolean doCommand(SmtConfig config) throws Exception
  {
    // this is a Link command, and it can take a subcommand and an argument
    String subCommand    = null;
    Map<String,String> map = smtConfig.getConfigMap();
    
    if(config != null)
    {
      //config.printConfig();
      
      map = config.getConfigMap();
      subCommand = map.get(SmtProperty.SMT_SUBCOMMAND.getName());
      
      // check to see if the subCommand takes any arguments or values
      if(subCommand == null)
      {
        subCommand = SmtProperty.SMT_HELP.getName();
      }
     }
    
    if(OMService == null)
      logger.severe("The service is null");

      // this is the MULTICAST command, and it can take a subcommand and an argument
      McastGroups = preenMCastGroups(OMService);
      MulticastQuery qType = null;
      
      if(subCommand.equalsIgnoreCase(SmtProperty.SMT_QUERY_TYPE.getName()))
        qType = MulticastQuery.getByName(map.get(SmtProperty.SMT_QUERY_TYPE.getName()));
      
      // optional, only needed for some commands
      int      mlid  = getMLid(config, (MulticastQuery.MCAST_MLID.equals(qType)));
      IB_Guid guid   = getGuid(config, (MulticastQuery.MCAST_MLID.equals(qType)));
      
      
      // there should only be one subcommand (use big if statement)
        if(qType != null)
        {
        switch (qType)
        {
          case MCAST_LIST:
            System.out.println(MulticastQuery.describeAllQueryTypes());
            break;
            
          case MCAST_STATUS:
            System.out.println(SmtMulticast.getStatus(OMService));

            break;
            
          case MCAST_MLID:
            if(mlid > 0)
            {
              showMulticastGroup(mlid);
              System.exit(0);
            }
            else
            {
              System.out.println("Supply a valid mlid (see -q groups)");
              System.exit(0);              
            }
            break;
            
          case MCAST_MEMBER:
            if(guid != null)
            {
              showGroupsWithMember(guid);
              System.exit(0);
            }
            else
            {
              System.out.println("Supply a valid node lid, guid, or name");
              System.exit(0);              
            }
            break;
            
          case MCAST_GROUPS:
            showGroups();
            break;
            
            default:
              System.out.println("That's not an option");
              break;
         }
      }
      else if (subCommand.equalsIgnoreCase(SmtProperty.SMT_STATUS.getName()))
      {
        System.out.println(SmtMulticast.getStatus(OMService));
      }
      else if (subCommand.equalsIgnoreCase(SmtProperty.SMT_DUMP.getName()))
      {
        showAllMulticastInfo();
      }
      else if (subCommand.equalsIgnoreCase(SmtProperty.SMT_LIST.getName()))
      {
        System.out.println(MulticastQuery.describeAllQueryTypes());
      }
      else if(mlid > 0)
      {
        // handle short-cuts.  
        showMulticastGroup(mlid);
      }
      else if (OMService != null)
      {
        System.out.println(SmtMulticast.getStatus(OMService));
      }
    return false;
  }
  
  public static SBN_MulticastGroup[] preenMcastGroups(SBN_MulticastGroup[] MCGroups)
  {
    // the arrays seem to hold redundant junk, not sure why, but try to save
    // only unique entries
    LinkedHashMap<String, SBN_MulticastGroup> mcGroups = new LinkedHashMap<String, SBN_MulticastGroup>(500+1, .75F, false);
    
    if((MCGroups != null) && (MCGroups.length > 0))
    {
      for(SBN_MulticastGroup mg: MCGroups)
      {
        // add my mlid, they should be unique (this is a destructive write, later array entries win
        //  only if they have non-zero members
        String key = Integer.toString(mg.mlid).trim();
        if(!mcGroups.containsKey(key) || mg.port_guids.length > 0)  
          mcGroups.put(key, mg);
       }
    }     
    SBN_MulticastGroup list[] = new SBN_MulticastGroup[mcGroups.size()];
    return mcGroups.values().toArray(list);
    
  }
  
  public static LinkedHashMap<String, SBN_MulticastGroup> preenMCastGroups(OpenSmMonitorService oms)
  {
    // the arrays seem to hold redundant junk, not sure why, but try to save
    // only unique entries
    LinkedHashMap<String, SBN_MulticastGroup> mcGroups = new LinkedHashMap<String, SBN_MulticastGroup>(500+1, .75F, false);
    
    if((oms != null) && (oms.getFabric() != null) && (oms.getFabric().getOsmSubnet() != null))
    {
      SBN_MulticastGroup[] MCGroups = preenMcastGroups(oms.getFabric().getOsmSubnet().MCGroups);
      if((MCGroups != null) && (MCGroups.length > 0))
        for(SBN_MulticastGroup mg: MCGroups)
        {
          String key = Integer.toString(mg.mlid).trim();
          mcGroups.put(key, mg);
        }
    }
    return mcGroups;
  }
  
  
  /************************************************************
   * Method Name:
   *  init
   **/
  /**
   * Describe the method here
   *
   * @see gov.llnl.lc.smt.command.SmtCommand#init()
   *
   * @return
   ***********************************************************/

  @Override
  public boolean init()
  {
    USAGE = "[-h=<host url>] [-pn=<port num>] ";
    HEADER = "smt-multicast - examine one-to-many communication attributes";
    EXAMPLE ="examples:" + SmtConstants.NEW_LINE +
        "> smt-multicast -pn 10011 -q status" + SmtConstants.NEW_LINE + 
        "> smt-multicast -rH surface3h.his -q member 0xf452:1403:0034:2ea1" + SmtConstants.NEW_LINE  + ".";  // terminate with nl

    // create and initialize the common options for this command
    initCommonOptions();
    
    // add non-common options
    
    // initialize the command specific options
    SmtProperty sp = SmtProperty.SMT_QUERY_TYPE;
    Option qType     = OptionBuilder.hasArg(true).hasArgs(1).withArgName( sp.getArgName() ).withValueSeparator('=').withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );

    sp = SmtProperty.SMT_QUERY_LIST;
    Option qList = new Option( sp.getShortName(), sp.getName(), false, sp.getDescription() );    

    sp = SmtProperty.SMT_STATUS;
    Option status  = OptionBuilder.hasArg(false).withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );
    
    sp = SmtProperty.SMT_DUMP;
    Option dump  = OptionBuilder.hasArg(false).withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );
    
    options.addOption( qType );
    options.addOption( qList );
    options.addOption( status );
    options.addOption( dump );
    return true;
  }

  /************************************************************
   * Method Name:
   *  parseCommands
   **/
  /**
   * Parse the command line options and set everything up for "doing the command" in doCommand();
   *
   * @see gov.llnl.lc.smt.command.SmtCommand#parseCommands(java.util.Map, org.apache.commons.cli.CommandLine)
   *
   * @param config
   * @param line
   * @return
   ***********************************************************/

  @Override
  public boolean parseCommands(Map<String, String> config, CommandLine line)
  {
    boolean status = true;
    
    // set the command, args, and sub-command
    config.put(SmtProperty.SMT_COMMAND.getName(), this.getClass().getName());
    
    // hopefully the node description is here, so save it
    saveCommandArgs(line.getArgs(), config);
    
    SmtProperty sp = SmtProperty.SMT_READ_OMS_HISTORY;
    if(line.hasOption(sp.getName()))
    {
      status = putHistoryProperty(config, line.getOptionValue(sp.getName()));
      config.put(SmtProperty.SMT_SUBCOMMAND.getName(), sp.getName());
    }
    
    // parse (only) the command specific options
    sp = SmtProperty.SMT_QUERY_TYPE;
    if(line.hasOption(sp.getName()))
    {
      config.put(SmtProperty.SMT_SUBCOMMAND.getName(), sp.getName());
      config.put(sp.getName(), line.getOptionValue(sp.getName()));
    }
    
    sp = SmtProperty.SMT_QUERY_LIST;
    if(line.hasOption(sp.getName()))
    {
      config.put(SmtProperty.SMT_SUBCOMMAND.getName(), SmtProperty.SMT_QUERY_TYPE.getName());
      config.put(SmtProperty.SMT_QUERY_TYPE.getName(), SmtProperty.SMT_LIST.getName());
    }

    sp = SmtProperty.SMT_STATUS;
    if(line.hasOption(sp.getName()))
    {
      config.put(SmtProperty.SMT_SUBCOMMAND.getName(), sp.getName());
      config.put(sp.getName(), line.getOptionValue(sp.getName()));
    }

    sp = SmtProperty.SMT_DUMP;
    if(line.hasOption(sp.getName()))
    {
      config.put(SmtProperty.SMT_SUBCOMMAND.getName(), sp.getName());
      config.put(sp.getName(), line.getOptionValue(sp.getName()));
    }

    sp = SmtProperty.SMT_LIST;
    if(line.hasOption(sp.getName()))
    {
      config.put(sp.getName(), line.getOptionValue(sp.getName()));
      config.put(SmtProperty.SMT_SUBCOMMAND.getName(), sp.getName());
    }
    return status;
  }

  private void showGroups()
  {
    SBN_MulticastGroup list[] = new SBN_MulticastGroup[McastGroups.size()];
    SmtMulticast.showGroups(McastGroups.values().toArray(list));
  }
  
  public static void showGroups(SBN_MulticastGroup[] mGroups)
  {
    int n = 1;
    for(SBN_MulticastGroup g: mGroups)
      System.out.println(toMulticastGroupString(g, String.format("%3d mlid: ", n++)));
  }
  
  private void showGroupsWithMember(IB_Guid guid)
  {
    SBN_MulticastGroup[] mGroups = getGroupsWithMember(McastGroups, guid);
    if(mGroups.length < 1)
      System.out.println("Guid " + guid.toColonString() + " was not found in any Multicast Groups");
    else
    {
      System.out.println("Guid " + guid.toColonString() + " was found in " + mGroups.length + " Multicast Groups");
      SmtMulticast.showGroups(mGroups);
    }   
  }
  
  public static String toMulticastGroupString(SBN_MulticastGroup mg, String prepend)
  {
    // a one liner
    StringBuffer buff = new StringBuffer();
    String wk = mg.well_known ? "well known": "";
    String format = "%s%4d (0x%4s), %10s   # members: %4d";
    
    // not really a group, if there is only one (or fewer) members
    buff.append(String.format(format, prepend, mg.mlid, Integer.toHexString(mg.mlid), wk, mg.port_guids.length));
    return buff.toString();
  }
  
  public static SBN_MulticastGroup[] getGroupsWithMembers(LinkedHashMap<String, SBN_MulticastGroup> mcGroups, int minimumNumber)
  {
    java.util.ArrayList<SBN_MulticastGroup> gMems = new java.util.ArrayList<SBN_MulticastGroup>();

    for(SBN_MulticastGroup g: mcGroups.values())
    {
      // if this has the minimum number of members, then add it to the return list
      if(g.port_guids.length >= minimumNumber)
        gMems.add(g);
     }

    SBN_MulticastGroup list[] = new SBN_MulticastGroup[gMems.size()];
    return gMems.toArray(list);
  }

  public static SBN_MulticastGroup[] getGroupsWithMember(LinkedHashMap<String, SBN_MulticastGroup> mcGroups, IB_Guid guid)
  {
    if(guid == null)
      return null;
    
    // look through the groups, and build a new list which contains this guid as a member
    java.util.ArrayList<SBN_MulticastGroup> gMems = new java.util.ArrayList<SBN_MulticastGroup>();

    for(SBN_MulticastGroup g: mcGroups.values())
    {
      // if this contains this guid, then add it to the return list
      if(g.isMember(guid.getGuid()))
        gMems.add(g);
     }

    SBN_MulticastGroup list[] = new SBN_MulticastGroup[gMems.size()];
    return gMems.toArray(list);
  }

  public static SBN_MulticastGroup[] getWellKnownGroups(LinkedHashMap<String, SBN_MulticastGroup> mcGroups, boolean wellKnown)
  {
    // if wellKnown is true, only return the well_known groups
    // if wellKnown is false, only return the non-well_known groups
    
    java.util.ArrayList<SBN_MulticastGroup> gMems = new java.util.ArrayList<SBN_MulticastGroup>();

    for(SBN_MulticastGroup g: mcGroups.values())
    {
      // if this is a well known group, return it
      if(!(g.well_known ^ wellKnown))
        gMems.add(g);
     }
    SBN_MulticastGroup list[] = new SBN_MulticastGroup[gMems.size()];
    return gMems.toArray(list);
  }

  private void showAllMulticastInfo()
  {
    System.out.println(SmtMulticast.getStatus(OMService));

    for(SBN_MulticastGroup mg: McastGroups.values())
       showMulticastGroup(mg);
   }
  
  public static  SBN_MulticastGroup getMulticastGroup(int mlid, SBN_MulticastGroup[] mGroups)
  {
    SBN_MulticastGroup[] MCGroups = preenMcastGroups(mGroups);
    for(SBN_MulticastGroup g: MCGroups)
    {
      // if the mlids match, return it
      if(mlid == g.mlid)
        return g;
     }
    return null;
  }
  
  private void showMulticastGroup(int mlid)
  {
    showMulticastGroup(McastGroups.get(Integer.toString(mlid).trim()));
  }
  
  private void showMulticastGroup(SBN_MulticastGroup mg)
  {
    if(mg != null)
    {
      System.out.println(mg.toMulticastGroupString());
      System.out.println("----------------------------------------------------");
      System.out.println(mg.toMulticastTableString(OMService.getFabric(), ""));      
    }
  }
  
  public static String getStatus(OpenSmMonitorService OMService)
  {
    // return a string representation of the multicast statistics, similar to the smt-console
    if(OMService == null)
    {
      logger.severe("Can't get status from a null OMS object");
      return "Can't get status from a null OMS object";
    }
    OSM_Fabric Fabric = OMService.getFabric();
    LinkedHashMap<String, SBN_MulticastGroup> mcGroups = preenMCastGroups(OMService);

    OsmServerStatus RStatus = OMService.getRemoteServerStatus();
     StringBuffer buff = new StringBuffer();
    
    buff.append(String.format("                Multicast Groups\n"));
    buff.append(SmtConstants.NEW_LINE);
    buff.append(String.format("Fabric Name:               %20s\n", Fabric.getFabricName()));
    if(RStatus != null)
      buff.append(String.format("Up since:                  %20s\n", RStatus.Server.getStartTime().toString() ));
    buff.append(String.format("timestamp:                 %20s\n", Fabric.getTimeStamp().toString()));
    buff.append(SmtConstants.NEW_LINE);
    
    int numGroups = mcGroups.size();
    int numSingleGroups = getGroupsWithMembers(mcGroups, 1).length;
    int numActualGroups = getGroupsWithMembers(mcGroups, 2).length;
    int numEmptyGroups = numGroups - numSingleGroups;
    numSingleGroups -= numActualGroups;
    
    String format = "%38s:    %4d" + SmtConstants.NEW_LINE;
    buff.append(String.format(format, "total multicast groups", numGroups));
    buff.append(String.format(format, "empty multicast groups", numEmptyGroups));
    buff.append(String.format(format, "multicast groups with a single member", numSingleGroups));
    buff.append(String.format(format, "multicast groups with many members", numActualGroups));
    buff.append(String.format(format, "well known multicast groups", getWellKnownGroups(mcGroups, true).length));
    buff.append(String.format(format, "unknown multicast groups", getWellKnownGroups(mcGroups, false).length));
  
    return buff.toString();
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
   ***********************************************************/
  public static void main(String[] args) throws Exception
  {
    System.exit((new SmtMulticast().execute(args)) ? 0: -1);
  }
  
  private void saveCommandArgs(String[] args, Map<String, String> config)
  {
    // stash the command line arguments away, because we will use them later
    // see getNodeGuid()
    if((args != null && args.length > 0))
    {
      // save all the arguments in a single parameter
      StringBuffer cmdArgs = new StringBuffer();
      for(String arg: args)
      {
        cmdArgs.append(arg + " ");
      }
      config.put(SmtProperty.SMT_COMMAND_ARGS.getName(), cmdArgs.toString().trim());
    }
  }
 
  private IB_Guid getGuid(SmtConfig config, boolean isMlidCmd)
  {
    // if there are any arguments, they normally reference a node identifier
    // return null, indicating couldn't be found, or nothing specified
    //
    // a node identifier is a name, guid, or lid
    if((config != null) && (!isMlidCmd))
    {
      Map<String,String> map = config.getConfigMap();
      String nodeid = map.get(SmtProperty.SMT_COMMAND_ARGS.getName());
      if(nodeid != null)
      {
        return getNodeGuid(nodeid, true);
      }
    }
     return null;
  }

  private int getMLid(SmtConfig config, boolean isMlidCmd)
  {
    // if there are any arguments, and the mlid flag is true, try
    // to decode the first argument as an mlid
    //
    // return -1, if the value could not be decoded, or if the mlid
    // is NOT in the table
    if(config != null)
    {
      Map<String,String> map = config.getConfigMap();
      String cargs = map.get(SmtProperty.SMT_COMMAND_ARGS.getName());
      if(cargs != null)
      {
        if(isMlidCmd)
        {
        // should be at least one word
        //  if more than one, ignore the rest
        String[] args = cargs.split(" ");
        
        // should be a lid
        try
        {
          int mLid = IB_Address.toLidValue(args[0]);
          // check to see if this lid is in the table, if so return it
          if(McastGroups.containsKey(Integer.toString(mLid).trim()))
            return mLid;
        }
        catch(NumberFormatException nfe)
        {
          logger.severe("Couldn't convert (" + args[0] + ") to an mlid");
        }
        }
      }
    }
     return -1;
  }

 }
