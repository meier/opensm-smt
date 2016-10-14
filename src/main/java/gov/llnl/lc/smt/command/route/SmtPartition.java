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
 *        file: SmtPartition.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command.route;

import gov.llnl.lc.infiniband.core.IB_Address;
import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Subnet;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.infiniband.opensm.plugin.data.SBN_PartitionKey;
import gov.llnl.lc.infiniband.opensm.plugin.net.OsmServerStatus;
import gov.llnl.lc.smt.SmtConstants;
import gov.llnl.lc.smt.command.SmtCommand;
import gov.llnl.lc.smt.command.config.SmtConfig;
import gov.llnl.lc.smt.props.SmtProperty;

import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

/**********************************************************************
 * Describe purpose and responsibility of SmtPartition
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Mar 20, 2013 12:08:04 PM
 **********************************************************************/
public class SmtPartition extends SmtCommand
{
  /************************************************************
   * Method Name:
   *  parseCommands
   **/
  /**
   * Parse the command line options here.  If any of these need to be persistent
   * then they need to be "put" into the config map.  Otherwise the command line
   * options need to set "command" flags or variables, which will potentially be
   * used later, typically by the "doCommand()".
   *
   *
   * @see gov.llnl.lc.smt.command.SmtCommandInterface#parseCommands(java.util.Map, org.apache.commons.cli.CommandLine)
   *
   * @param config
   * @param line
   * @return
   ***********************************************************/

  @Override
  public boolean parseCommands(Map<String, String> config, CommandLine line)
  {
    boolean status = true;
    
    // set the command and sub-command (always do this)
    config.put(SmtProperty.SMT_COMMAND.getName(), this.getClass().getName());
    
    
    // parse (only) the command specific options
 
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

    sp = SmtProperty.SMT_DUMP;
    if(line.hasOption(sp.getName()))
    {
      config.put(SmtProperty.SMT_SUBCOMMAND.getName(), sp.getName());
      config.put(sp.getName(), line.getOptionValue(sp.getName()));
    }

    sp = SmtProperty.SMT_STATUS;
    if(line.hasOption(sp.getName()))
    {
      config.put(SmtProperty.SMT_SUBCOMMAND.getName(), sp.getName());
      config.put(sp.getName(), line.getOptionValue(sp.getName()));
    }
    
    return status;
   }
  
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
    
    Map<String,String> map = smtConfig.getConfigMap();
    
    String subCommand = map.get(SmtProperty.SMT_SUBCOMMAND.getName());
    if (subCommand == null)
      subCommand = SmtProperty.SMT_HELP.getName();

    // this is the Partition command, and it can take a subcommand and an argument
    PartitionQuery qType = null;
    
    if(subCommand.equalsIgnoreCase(SmtProperty.SMT_QUERY_TYPE.getName()))
      qType = PartitionQuery.getByName(map.get(SmtProperty.SMT_QUERY_TYPE.getName()));
    
    // optional, only needed for some commands
    int      pkey  = getPKey(config, (PartitionQuery.PART_PKEY.equals(qType)));
    IB_Guid guid   = getGuid(config, (PartitionQuery.PART_PKEY.equals(qType)));
    
    // there should only be one subcommand (use big if statement)
    if(qType != null)
    {
      switch (qType)
      {
        case PART_LIST:
          System.out.println(PartitionQuery.describeAllQueryTypes());
          break;
          
        case PART_STATUS:
          System.out.println(SmtPartition.getStatus(OMService));

          break;
          
        case PART_PKEY:
          if(pkey > 0)
          {
            showPartition(pkey);
            System.exit(0);
          }
          else
          {
            System.out.println("Supply a valid pKey (see -q parts)");
            System.exit(0);              
          }
          break;
          
        case PART_MEMBER:
          if(guid != null)
          {
            showPartitionsWithMember(guid);
            System.exit(0);
          }
          else
          {
            System.out.println("Supply a valid node lid, guid, or name");
            System.exit(0);              
          }
          break;
          
        case PART_PARTS:
          showPartitions();
          break;
          
          default:
            System.out.println("That's not an option");
            break;
       }
    }
    else if (subCommand.equalsIgnoreCase(SmtProperty.SMT_STATUS.getName()))
    {
      System.out.println(SmtPartition.getStatus(OMService));
    }
    else if (subCommand.equalsIgnoreCase(SmtProperty.SMT_DUMP.getName()))
    {
      showAllPartitionInfo();
    }
    else if (subCommand.equalsIgnoreCase(SmtProperty.SMT_LIST.getName()))
    {
      System.out.println(PartitionQuery.describeAllQueryTypes());
    }
    else if(pkey > 0)
    {
      // handle short-cuts.  
      showPartition(pkey);
    }
    else if (OMService != null)
    {
      System.out.println(SmtPartition.getStatus(OMService));
    }
     
    return true;
  }
  
  private void showAllPartitionInfo()
  {
    SBN_PartitionKey[] pkA = OMService.getFabric().getOsmSubnet().PKeys;
    
    System.out.println(SmtPartition.getStatus(OMService));

    for(SBN_PartitionKey pk: pkA)
      showPartition(pk.pkey);
  }

  private void showPartitions()
  {
    SmtPartition.showPartitions(OMService.getFabric().getOsmSubnet().PKeys);
  }
  
  public static void showPartitions(SBN_PartitionKey[] pkA)
  {
    int n = 1;
    for(SBN_PartitionKey pk: pkA)
      System.out.println(toPartitionString(pk, String.format("%3d pKey: ", n++)));
  }
  
  public static String toPartitionString(SBN_PartitionKey pk, String prepend)
  {
    // a one liner
    StringBuffer buff = new StringBuffer();
    String wk = pk.well_known ? "well known": "";
    String format = "%s0x%4s (%4d), mlid: %4d, name: %-20s %10s # full members: %4d, # partial members: %4d";
    
    // not really a group, if there is only one (or fewer) members
    buff.append(String.format(format, prepend, Integer.toHexString(pk.pkey), pk.pkey, pk.mlid, pk.Name, wk, pk.full_member_guids.length, pk.partial_member_guids.length));
    return buff.toString();
  }

  private void showPartitionsWithMember(IB_Guid guid)
  {
    // given this guid, return the partitions that contain it
    SBN_PartitionKey[] pkA = getPartitionsWithMember(OMService.getFabric().getOsmSubnet().PKeys, guid);
    if(pkA.length < 1)
      System.out.println("Guid " + guid.toColonString() + " was not found in any Partition");
    else
    {
      System.out.println("Guid " + guid.toColonString() + " was found in " + pkA.length + " Partitions");
      SmtPartition.showPartitions(pkA);
    }   
   }

  public static SBN_PartitionKey getPartition(int pkey, OpenSmMonitorService oms)
  {
    if((oms != null) && (oms.getFabric() != null) && (oms.getFabric().getOsmSubnet() != null))
    {
    SBN_PartitionKey[] pkA = oms.getFabric().getOsmSubnet().PKeys;

    for(SBN_PartitionKey pk: pkA)
      if(pk.pkey == pkey)
        return pk;
    }
    return null;
   }

  private void showPartition(int pkey)
  {
    SBN_PartitionKey pk = getPartition(pkey, OMService);
    if(pk != null)
      if((pk != null) && (pk.pkey == pkey))
      {
        System.out.print(pk.toPartitionKeyString());
        System.out.println("-------------------------------------------------------------");
        System.out.println(pk.toFullMemberString(OMService.getFabric(), ""));
        return;
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

    OsmServerStatus RStatus = OMService.getRemoteServerStatus();
     StringBuffer buff = new StringBuffer();
    
    buff.append(String.format("                Fabric Partitions\n"));
    buff.append(SmtConstants.NEW_LINE);
    buff.append(String.format("Fabric Name:               %20s\n", Fabric.getFabricName()));
    if(RStatus != null)
      buff.append(String.format("Up since:                  %20s\n", RStatus.Server.getStartTime().toString() ));
    buff.append(String.format("timestamp:                 %20s\n", Fabric.getTimeStamp().toString()));
    buff.append(SmtConstants.NEW_LINE);
     
    OSM_Subnet Subnet  = Fabric.getOsmSubnet();

    if(Subnet != null)
    {
    if((Subnet.PKeys != null) && (Subnet.PKeys.length > 0))
    {
      String format = "%28s:    %4d" + SmtConstants.NEW_LINE;
      buff.append(String.format(format, "total partitions", Subnet.PKeys.length));
      buff.append(String.format(format, "well known partitions", (SmtPartition.getWellKnownPartitions(Subnet.PKeys, true)).length));
      buff.append(String.format(format, "unknown partitions", (SmtPartition.getWellKnownPartitions(Subnet.PKeys, false)).length));
    }
    }
    return buff.toString();
  }
  
  public static SBN_PartitionKey[] getWellKnownPartitions(SBN_PartitionKey[] pKeys, boolean wellKnown)
  {
    // if wellKnown is true, only return the well_known groups
    // if wellKnown is false, only return the non-well_known groups
    
    java.util.ArrayList<SBN_PartitionKey> pkeys = new java.util.ArrayList<SBN_PartitionKey>();

    for(SBN_PartitionKey g: pKeys)
    {
      // if this is a well known group, return it
      if(!(g.well_known ^ wellKnown))
        pkeys.add(g);
     }
    SBN_PartitionKey list[] = new SBN_PartitionKey[pkeys.size()];
    return pkeys.toArray(list);
  }

  public static SBN_PartitionKey[] getPartitionsWithMember(SBN_PartitionKey[] pKeys, IB_Guid guid)
  {
    if(guid == null)
      return null;
    
    // look through the partition, and build a new list which contains this guid as a member
    java.util.ArrayList<SBN_PartitionKey> pkeys = new java.util.ArrayList<SBN_PartitionKey>();

    for(SBN_PartitionKey g: pKeys)
    {
      // if this contains this guid (full or partial), then add it to the return list
      if(g.isMember(guid.getGuid(), false))
        pkeys.add(g);
     }
    SBN_PartitionKey list[] = new SBN_PartitionKey[pkeys.size()];
    return pkeys.toArray(list);
  }
  
  public static String getPartitionSummary(OSM_Fabric fab, String prePend)
  {
    OSM_Subnet Subnet = fab.getOsmSubnet();

    if (Subnet != null)
    {
      if ((Subnet.PKeys != null) && (Subnet.PKeys.length > 0))
      {
        System.out.println("There are " + Subnet.PKeys.length + " PartitionKeys in the fabric");
        for (SBN_PartitionKey pk : Subnet.PKeys)
        {
          System.out.println(pk.toPartitionKeyString());
          System.out.println(pk.toFullMemberString(fab, prePend));
        }
      }
    }
    return "done";
  }

  /************************************************************
   * Method Name:
   *  init
   **/
  /**
   * Describe the method here
   *
   * @see gov.llnl.lc.smt.command.SmtCommand#init()
   * @see gov.llnl.lc.smt.command.SmtCommand#initCommonOptions()
   *
   * @return
   ***********************************************************/

  @Override
  public boolean init()
  {
    USAGE = "[-h=<host url>] [-pn=<port num>] ";
    HEADER = "smt-partition - examine partition attributes";
    EXAMPLE ="examples:" + SmtConstants.NEW_LINE +
        "> smt-partition -pn 10011 -q status" + SmtConstants.NEW_LINE + 
        "> smt-partition -pn 10013 -q pkey 0x7fff" + SmtConstants.NEW_LINE + 
        "> smt-partition -rH surface3h.his -q member 0xf452:1403:0034:2ea1" + SmtConstants.NEW_LINE  + ".";  // terminate with nl

    // create and initialize the common options for this command
    initCommonOptions();
    
    // add non-common options
    initMulitReadFileOptions();  // read the remainder of the file types

    SmtProperty sp = SmtProperty.SMT_QUERY_TYPE;
    Option qType     = OptionBuilder.hasArg(true).hasArgs(1).withArgName( sp.getArgName() ).withValueSeparator('=').withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );

    sp = SmtProperty.SMT_QUERY_LIST;
    Option qList = new Option( sp.getShortName(), sp.getName(), false, sp.getDescription() );
    
    sp = SmtProperty.SMT_STATUS;
    Option status  = OptionBuilder.hasArg(false).withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );
    
    sp = SmtProperty.SMT_DUMP;
    Option dump  = OptionBuilder.hasArg(false).withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );
    
    options.addOption( status );
    options.addOption( qType );
    options.addOption( qList );
    options.addOption( dump );    
    
    return true;
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
   * @throws Exception 
   ***********************************************************/
  public static void main(String[] args) throws Exception
  {
    System.exit((new SmtPartition().execute(args)) ? 0: -1);
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
 
  private IB_Guid getGuid(SmtConfig config, boolean isPKeyCmd)
  {
    // if there are any arguments, they normally reference a node identifier
    // return null, indicating couldn't be found, or nothing specified
    //
    // a node identifier is a name, guid, or lid
    if((config != null) && (!isPKeyCmd))
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

  private int getPKey(SmtConfig config, boolean isPKeyCmd)
  {
    // if there are any arguments, and the mlid flag is true, try
    // to decode the first argument as an pkey
    //
    // return -1, if the value could not be decoded, or if the pkey
    // is NOT in the partition list
    if(config != null)
    {
      Map<String,String> map = config.getConfigMap();
      String cargs = map.get(SmtProperty.SMT_COMMAND_ARGS.getName());
      if(cargs != null)
      {
        if(isPKeyCmd)
        {
        // should be at least one word
        //  if more than one, ignore the rest
        String[] args = cargs.split(" ");
        
        // should be a lid
        try
        {
          SBN_PartitionKey[] pkA = OMService.getFabric().getOsmSubnet().PKeys;

          int pKey = IB_Address.toLidValue(args[0]);
          // check to see if this pkey is in the partition, if so return it
          for(SBN_PartitionKey pk: pkA)
            if(pk.pkey == pKey)
              return pKey;
        }
        catch(NumberFormatException nfe)
        {
          logger.severe("Couldn't convert (" + args[0] + ") to an pkey");
        }
        }
      }
    }
     return -1;
  }


  
  
}
