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
 *        file: SmtIdentification.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command.search;

import gov.llnl.lc.infiniband.core.IB_Address;
import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Node;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_NodeType;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.SBN_MulticastGroup;
import gov.llnl.lc.infiniband.opensm.plugin.data.SBN_PartitionKey;
import gov.llnl.lc.smt.SmtConstants;
import gov.llnl.lc.smt.command.SmtCommand;
import gov.llnl.lc.smt.command.config.SmtConfig;
import gov.llnl.lc.smt.command.route.SmtMulticast;
import gov.llnl.lc.smt.command.route.SmtPartition;
import gov.llnl.lc.smt.props.SmtProperty;

import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

/**********************************************************************
 * Describe purpose and responsibility of SmtIdentification
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Mar 20, 2013 12:08:04 PM
 **********************************************************************/
public class SmtIdentification extends SmtCommand
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
    
    // if the host and port is supplied on the command line, assume we are making
    // a connection to the service - no matter what
    //
    // if an OMS_FILE or FABRIC_FILE is supplied, it could be for reading or writing
    // but NOT BOTH.  We need to determine which based on;
    // 
    //  if host and port is supplied, then its for WRITING
    //
    //  if host and port is NOT supplied AND there is persistent host and port configured
    //     AND the -R flag is NOT provided on the command line, then its for WRITING
    //
    //  if host and port is NOT supplied AND there is no persistent host and port configured
    //    then its for READING
    //
    //  if the -R flag is provided on the command line, then its for READING
    
    // parse (only) the command specific options
    
    
    // hopefully the node description is here, so save it
    saveCommandArgs(line.getArgs(), config);
    
    SmtProperty sp = SmtProperty.SMT_READ_OMS_HISTORY;
    if(line.hasOption(sp.getName()))
    {
      config.put(sp.getName(), line.getOptionValue(sp.getName()));
      config.put(SmtProperty.SMT_SUBCOMMAND.getName(), sp.getName());
    }
    
    sp = SmtProperty.SMT_ID_PORT;
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
    // support obtaining the fabric on-line, or from an OMS or Fabric
    // file.  Only one at a time....
    
    // which is all done by default within the execute() command of the
    // parent superclass smt-command
    
    // only one way of obtaining fabric data should be specified, but IF more
    // than one is, prefer;
    //
    //  on-line (if host or port is specified)
    //  OMS file
    //  Fabric file
    //  on-line using localhost and port 10011
    
    // this is a ID command, and it can take a subcommand and an argument
    String subCommand    = null;
    Map<String,String> map = smtConfig.getConfigMap();
    
    IB_Guid g = getNodeGuid(config);
    int  pNum = getPortNumber(config);
    int  pKey = getPartitionKey(config);
    int  mLid = getMulticastLid(config);
        
    if(config != null)
    {
      map = config.getConfigMap();
      subCommand = map.get(SmtProperty.SMT_SUBCOMMAND.getName());
       if(subCommand == null)
      {
        subCommand = SmtProperty.SMT_HELP.getName();
      }
     }

    // attempt to identify the node
    OSM_Fabric                    fabric = null;
    OSM_Node                           n = null;
    if((OMService != null) && (g != null))
    {
      fabric = OMService.getFabric();
      n = fabric.getOSM_Node(g);
    }

    // there should only be one subcommand
    if (subCommand.equalsIgnoreCase(SmtProperty.SMT_ID_PORT.getName()))
    {
      String portNum = map.get(SmtProperty.SMT_ID_PORT.getName());
      if (portNum != null)
        try
        {
          pNum = Integer.parseInt(portNum);
        }
        catch (Exception e)
        {
          pNum = 0;
        }
    }
    
    if(n != null)
      System.out.println(SmtIdentification.getIdentication(fabric, n, pNum));
    
    if(pKey >= 0)
       System.out.println(SmtPartition.getPartition(pKey, OMService).toPartitionKeyString());
      
    if(mLid >= 0)
      System.out.println((SmtMulticast.getMulticastGroup(mLid, OMService.getFabric().getOsmSubnet().MCGroups)).toMulticastGroupString());
    
    if((n == null) && (pKey < 0) && (mLid < 0))
      System.err.println("Could not identify object: (" + map.get(SmtProperty.SMT_COMMAND_ARGS.getName()) + ")");
     
    return true;
  }
  
  private int getMulticastLid(SmtConfig config)
  {
    // return a valid mlid, or -1
    if(config != null)
    {
      Map<String,String> map = config.getConfigMap();
      int mLid = getLid( map.get(SmtProperty.SMT_COMMAND_ARGS.getName()));
      if(mLid >= 0)
      {
        // does this mLid exist in the fabric??
        SBN_MulticastGroup mg = SmtMulticast.getMulticastGroup(mLid, OMService.getFabric().getOsmSubnet().MCGroups);
        if(mg != null)
          return mLid;
       }
    }
    return -1;
  }
  
  private int getLid(String idString)
  {
    // return -1 if this doesn't work
    if(idString != null)
    {
      // this shouldn't be a long string
      if(idString.length() < 8)
      {
        // could be a lid
        try
        {
          int lidVal = IB_Address.toLidValue(idString);
          return lidVal;
        }
        catch(NumberFormatException nfe)
        {
          // perhaps a small name??
          logger.severe("couldn't convert " + idString + " to a lid");
        }
      }
     }
    return -1;
   }

  private int getPartitionKey(SmtConfig config)
  {
    // return a valid pKey, or -1
    if(config != null)
    {
      Map<String,String> map = config.getConfigMap();
      int pKey = getLid( map.get(SmtProperty.SMT_COMMAND_ARGS.getName()));
      if(pKey >= 0)
      {
        // does this pKey exist in the fabric??
        SBN_PartitionKey[] pkA = OMService.getFabric().getOsmSubnet().PKeys;

        // check to see if this pkey is in the partition, if so return it
        for(SBN_PartitionKey pk: pkA)
          if(pk.pkey == pKey)
            return pKey;
       }
    }
    return -1;
  }

  public static String getIdentication(OSM_Fabric f, OSM_Node n, int pNum)
  {
    // return a node or port identifier
    String formatString = "%12s:  %s";

    StringBuffer buff = new StringBuffer();
    if ((f != null) && (n != null))
    {
      IB_Guid g = n.getNodeGuid();
      int lid = f.getLidFromGuid(g);
      boolean bPguid = f.isUniquePortGuid(g);
      IB_Guid pg = f.getParentGuid(g);
      boolean bSw = OSM_NodeType.isSwitchNode(n.sbnNode);
      
      // if the supplied node is a channel adapter, show port info

      buff.append(String.format(formatString, "Node name", n.pfmNode.node_name + SmtConstants.NEW_LINE));
      buff.append(String.format(formatString, "description", n.sbnNode.description + SmtConstants.NEW_LINE));
      buff.append(String.format(formatString, "guid", g.toColonString() + SmtConstants.NEW_LINE));
      buff.append(String.format(formatString, "lid", lid + " (0x" + Integer.toHexString(lid) + ")" + SmtConstants.NEW_LINE));
      buff.append(String.format(formatString, "type", OSM_NodeType.get(n).getFullName() + SmtConstants.NEW_LINE));
      if(!bSw)
      {
        pNum = 1;
        OSM_Port p = f.getOSM_Port(OSM_Port.getOSM_PortKey(n.getNodeGuid().getGuid(), (short)pNum));
        if(p != null)
          g = new IB_Guid(p.sbnPort.port_guid);
        else
        {
          g = new IB_Guid(n.getNodeGuid().getGuid() + 1);
        }
        buff.append(String.format(formatString, "port guid", g.toColonString() + SmtConstants.NEW_LINE));
        buff.append(String.format(formatString, "port num", pNum + SmtConstants.NEW_LINE));        
      }
      else
      {
        if(pNum > 0)
        {
          // add port info for this node
          OSM_Port p = f.getOSM_Port(OSM_Port.getOSM_PortKey(n.getNodeGuid().getGuid(), (short)pNum));
          if(p != null)
            g = new IB_Guid(p.sbnPort.port_guid);
          buff.append(String.format(formatString, "port guid", g.toColonString() + SmtConstants.NEW_LINE));
          buff.append(String.format(formatString, "port num", pNum + SmtConstants.NEW_LINE));        
        }
      }
    }
    return buff.toString();
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
    USAGE = "[-h=<host url>] [-pn=<port num>] <ID string> [-p=<node port #>]";
    HEADER = "smt-id - an identification tool.  Provide a guid, lid, pkey, mlid, or name as an ID string";
    EXAMPLE ="examples:" + SmtConstants.NEW_LINE +
        "> smt-id -pn 10011 374                  - identifies and describes lid 374" + SmtConstants.NEW_LINE + 
        "> smt-id 0006:6a01:e800:1313 -pn 10013  - identifies and describes the guid" + SmtConstants.NEW_LINE + 
        "> smt-id -pn 10013 0xc003               - identifies and describes the mlid" + SmtConstants.NEW_LINE + 
        "> smt-id -rH surface3h.his 0x7fff       - identifies and describes the pkey" + SmtConstants.NEW_LINE  + ".";  // terminate with nl

    // create and initialize the common options for this command
    initCommonOptions();
    
    SmtProperty sp = SmtProperty.SMT_ID_PORT;
    Option port  = OptionBuilder.hasArg(true).hasArgs(1).withArgName( sp.getArgName() ).withValueSeparator('=').withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );
    
    options.addOption( port );
    return true;
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
  
  private IB_Guid getNodeGuid(SmtConfig config)
  {
    // if there are any arguments, they normally reference a node identifier
    // return null, indicating couldn't be found, or nothing specified
    if(config != null)
    {
      Map<String,String> map = config.getConfigMap();
      String nodeid = map.get(SmtProperty.SMT_COMMAND_ARGS.getName());
      if(nodeid != null)
      {
        // the id may be a name, lid, guid, or a port-guid (not same as node guid)
        return getPortsNodeGuid(nodeid);
       }
    }
     return null;
  }


  private int getPortNumber(SmtConfig config)
  {
    // if there are any arguments, they normally reference a port identifier
    // return 0, indicating couldn't be found, or nothing specified
    if(config != null)
    {
      Map<String,String> map = config.getConfigMap();
      String portid = map.get(SmtProperty.SMT_COMMAND_ARGS.getName());
      if(portid != null)
      {
        // should be at least two words
        //  the very last word, is supposed to be the port number
        //  if only one word, then check to see if there are 4 colons, if so, port number is after that
        String[] args = portid.split(" ");
        if((args != null) && (args.length > 0))
        {
          int p = 0;
          if(args.length == 1)
          {
            // see if a port number is tagged on as the last value of a colon delimited guid+port string
            String[] octets = portid.split(":");
            if(octets.length > 4)
              p = Integer.parseInt(octets[octets.length -1]);
           }
          else
          {
            // multiple words, only look at the last one
            String arg = args[args.length -1];
            try
            {
              p = Integer.parseInt(arg);
            }
            catch(Exception e)
            {
              // not a pure number, but perhaps colon delimited
              String[] octets = arg.split(":");
              p = 0;
              if(octets.length > 1)
                p = Integer.parseInt(octets[octets.length -1]);
            }
          }
          return p;
        }
       }
    }
     return 0;
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
//    System.out.println(new CurrentClassName().getClassName());
    System.exit((new SmtIdentification().execute(args)) ? 0: -1);
  }

}
