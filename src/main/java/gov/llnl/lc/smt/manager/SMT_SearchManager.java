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
 *        file: SMT_SearchManager.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.manager;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import gov.llnl.lc.infiniband.core.IB_Address;
import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.core.IB_GuidType;
import gov.llnl.lc.infiniband.core.IB_Link;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Node;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_System;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.infiniband.opensm.plugin.data.RT_Node;
import gov.llnl.lc.infiniband.opensm.plugin.data.RT_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.RT_Table;
import gov.llnl.lc.infiniband.opensm.plugin.data.SBN_MulticastGroup;
import gov.llnl.lc.infiniband.opensm.plugin.data.SBN_PartitionKey;
import gov.llnl.lc.infiniband.opensm.plugin.data.SBN_Port;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.command.route.SmtMulticast;
import gov.llnl.lc.smt.command.search.SMT_SearchResult;
import gov.llnl.lc.smt.command.search.SMT_SearchResultType;

/**********************************************************************
 * The SMT_SearchManager is a singleton (and therefore global) object that
 * provides helper functions for finding objects in the fabric.
 *
 * @author meier3
 * 
 * @version Apr 29, 2015 11:19:24 AM
 **********************************************************************/
/**********************************************************************
 * Describe purpose and responsibility of SMT_SearchManager
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Oct 19, 2016 9:58:23 AM
 **********************************************************************/
/**********************************************************************
 * Describe purpose and responsibility of SMT_SearchManager
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Oct 19, 2016 9:59:27 AM
 **********************************************************************/
public class SMT_SearchManager implements CommonLogger
{
  /** the one and only <code>SMT_SearchManager</code> Singleton **/
  private volatile static SMT_SearchManager gSearchMgr   = null;

  /** the synchronization object **/
  private static Boolean                   semaphore   = new Boolean(true);

  /** logger for the class **/
  private final java.util.logging.Logger   classLogger = java.util.logging.Logger.getLogger(getClass().getName());
  
  private static java.util.ArrayList<String> SearchHistory;


  /************************************************************
   * Method Name: SMT_SearchManager
   **/
  /**
   * Describe the constructor here
   * 
   * @see describe related java objects
   * 
   ***********************************************************/
  private SMT_SearchManager()
  {
    super();
    initManager();
  }

  /**************************************************************************
   *** Method Name: getInstance
   **/
  /**
   *** Get the singleton SMT_SearchManager. This can be used if the application
   * wants to share one manager across the whole JVM. Currently I am not sure
   * how this ought to be used.
   *** <p>
   *** 
   *** @return the GLOBAL (or shared) SmtConsoleManager
   **************************************************************************/

  public static SMT_SearchManager getInstance()
  {
    synchronized (SMT_SearchManager.semaphore)
    {
      if (gSearchMgr == null)
      {
        gSearchMgr = new SMT_SearchManager();
      }
      return gSearchMgr;
    }
  }
  
  public static IB_Guid getPortsNodeGuid(IB_Guid portGuid, OpenSmMonitorService oms)
  {
    if((oms != null) && (portGuid != null))
    {
      // given a ports guid, return its parent guid
      
      // search through the ports addresses, and find a match
     //  if found, return the NODE guid, not the port guid
     OSM_Fabric Fabric = oms.getFabric();
     if(Fabric != null)
     {
       SBN_Port [] SubnPorts = Fabric.getOsmPorts().getSubnPorts();
       
       if((SubnPorts != null) && (SubnPorts.length > 0))
       {
         for(SBN_Port p: SubnPorts)
         {
           if((p.port_guid == portGuid.getGuid()))
             return new IB_Guid(p.node_guid);
         }
       }
     }      
    }
    return null;
  }
  
  public static java.util.ArrayList<SMT_SearchResult> getSearchResults(SMT_SearchResultType type, String searchString, OpenSmMonitorService oms)
  {
    java.util.ArrayList<SMT_SearchResult> results     = new java.util.ArrayList<SMT_SearchResult>();
    if(oms != null)
    {
    
    OSM_Fabric Fabric = oms.getFabric();
    int number         = getTrailingNumber(searchString, true);
    IB_Guid sg         = getGuid(searchString);

    IB_Guid SystemGuid = SMT_SearchManager.getGuidByType(searchString, IB_GuidType.SYSTEM_GUID, oms);
    IB_Guid NodeGuid   = SMT_SearchManager.getGuidByType(searchString, IB_GuidType.NODE_GUID, oms);
    IB_Guid PortGuid   = SMT_SearchManager.getGuidByType(searchString, IB_GuidType.PORT_GUID, oms);
    if(Fabric.isUniquePortGuid(PortGuid))
    {
      // this is a port guid, which normally means its a channel adapter
      // and the port number may or may not be specified
      number = number < 1 ? 1: number;
      PortGuid = getPortsNodeGuid(PortGuid, oms);  // always prefer the parent node guid + port number
    }
    
    // use these types, to determine
    switch (type)
    {
      case SEARCH_SYSTEM:
        // only return a system, that matches the string
        // if a switch guid is provided, return its system (if any)
        // if a port guid is provided, return its system (if any)
          if((SystemGuid != null) || (NodeGuid != null) || (PortGuid != null))
          {
            OSM_System sys = OSM_System.getOSM_System(Fabric, SystemGuid);
            if(sys != null)
              results.add(new SMT_SearchResult(type, searchString, sys, oms));
            else if (NodeGuid != null)
            {
              OSM_Node node = Fabric.getOSM_Node(NodeGuid);
              // didn't find a system guid, but perhaps this is a node, which has a system guid?
              if(node != null)
              {
                SystemGuid = node.sbnNode.getSysGuid();
                if(!NodeGuid.equals(SystemGuid))
                {
                  sys = OSM_System.getOSM_System(Fabric, SystemGuid);
                  if(sys != null)
                    results.add(new SMT_SearchResult(type, searchString, sys, oms));
                }
               }
             }
            }
        break;
        
      case SEARCH_NODE:
        // only return nodes that match the string
        // if a port guid is provided, return its parent node
           if((NodeGuid != null) || (PortGuid != null))
          {
            OSM_Node node = Fabric.getOSM_Node(NodeGuid);
            if(node == null)
              node = Fabric.getOSM_Node(PortGuid);
            // save the corresponding vertex
            if(node != null)
              results.add(new SMT_SearchResult(type, searchString, node, oms));
          }
        break;
        
      case SEARCH_PORT:
        // only return ports that match the string
          if((PortGuid != null) && (number > 0))
          {
            // make sure this guid actually has a port with this value, before adding it
            OSM_Port port = Fabric.getOSM_Port(OSM_Fabric.getOSM_PortKey(PortGuid.getGuid(), (short)number));
            if(port != null)
              results.add(new SMT_SearchResult(type, searchString, port, oms));
          }
        break;
        
      case SEARCH_LINK:
        // only return links that match the string
        if((PortGuid != null) && (number > 0))
        {
          // make sure this guid actually has a port with this value, before adding it
          OSM_Port port = Fabric.getOSM_Port(OSM_Fabric.getOSM_PortKey(PortGuid.getGuid(), (short)number));
          if(port != null)
          {
            // find the link associated with this port
            IB_Link link = OSM_Fabric.getIB_Link(port.getNodeGuid().getGuid(), (short)port.getPortNumber(), Fabric.getIB_Links());
            // save the corresponding edge
            if(link != null)
              results.add(new SMT_SearchResult(type, searchString, link, oms));
           }
        }
        break;
        
      case SEARCH_RTNODE:
        // only return nodes that match the string
          if((NodeGuid != null) || (PortGuid != null))
          {
            OSM_Node node = Fabric.getOSM_Node(NodeGuid);
            if(node == null)
              node = Fabric.getOSM_Node(PortGuid);
            if((node != null) && (node.isSwitch()))
            {
               // attempt to find the switch routing table for this node
               RT_Table rTable = SMT_RouteManager.getInstance().getRouteTable();
               RT_Node rNode = rTable.getRT_Node(NodeGuid);

              // save the corresponding rtnode
               if(rNode != null)
                 results.add(new SMT_SearchResult(type, searchString, rNode, oms));
            }
          }
        break;        
        
      case SEARCH_RTPORT:
        // only return ports that match the string
         if((PortGuid != null) && (number > 0))
          {
            OSM_Node node = Fabric.getOSM_Node(NodeGuid);
            if(node == null)
              node = Fabric.getOSM_Node(PortGuid);
            if((node != null) && (node.isSwitch()))
            {
               // attempt to find the switch routing table for this node
               RT_Table rTable = SMT_RouteManager.getInstance().getRouteTable();
               RT_Port rPort = rTable.getRTPort(NodeGuid, number);

              // save the corresponding rPort
               if(rPort != null)
                 results.add(new SMT_SearchResult(type, searchString, rPort, oms));
            }
          }
        break;        
        
      case SEARCH_CONFIG:
        // only return type that matches the string
        HashMap<String, String> OptionsMap   = Fabric.getOptions();
        String[] words = (searchString.trim()).split(" ");
        // do both a forward and reverse lookup on these keys
        for(String key: words)
        {
//          // in this case, the key is one of the search words
//          String val = OptionsMap.get(key);
//          if(val != null)
//          {
//            // an exact match
//            AbstractMap.SimpleEntry<String, String> pair = new AbstractMap.SimpleEntry<String, String>(key, val);
//            results.add(new SMT_SearchResult(type, searchString, pair, oms));
//           }
//          
//          {
            // a full or partial match??
            for(Entry<String, String> entry: OptionsMap.entrySet())
            {
              //does the value "contain" the key
              if(entry.getKey().toString().toLowerCase().trim().contains(key.toLowerCase().trim()))
              {
                // put it back in the correct order
                AbstractMap.SimpleEntry<String, String> pair = new AbstractMap.SimpleEntry<String, String>(entry.getKey().toString(), entry.getValue().toString());
                results.add(new SMT_SearchResult(type, searchString, pair, oms));
              }
//            }
          }
          
          // reverse is more tedious (don't care about partial match??)
          if(OptionsMap.containsValue(key))
          {
            // may contain more than one, so return all
            for(Entry<String, String> entry: OptionsMap.entrySet())
            {
              //does the key match the value??
              if(key.equalsIgnoreCase(entry.getValue().toString().trim()))
              {
                // put it back in the correct order
                AbstractMap.SimpleEntry<String, String> pair = new AbstractMap.SimpleEntry<String, String>(entry.getKey().toString(), entry.getValue().toString());
                results.add(new SMT_SearchResult(type, searchString, pair, oms));
              }
            }
          }
        }

        break;        
        
      case SEARCH_PARTITION:
        // only return type that matches the string
        number = number >= 0 ? number: getTrailingNumber(searchString, false);
        if(number >= 0)
        {
            // does this pKey exist in the fabric??
            SBN_PartitionKey[] pkA = Fabric.getOsmSubnet().PKeys;

            // check to see if this pkey is in the partition, if so return it
            for(SBN_PartitionKey pk: pkA)
              if(pk.pkey == number)
              {
                results.add(new SMT_SearchResult(type, searchString, pk, oms));
                break;
              }
        }
        break;        
        
      case SEARCH_MCAST:
        // only return type that matches the string
        number = number >= 0 ? number: getTrailingNumber(searchString, false);
        
        if(number >= 0)
        {
          // does this mLid exist in the fabric??
          SBN_MulticastGroup mg = SmtMulticast.getMulticastGroup(number, Fabric.getOsmSubnet().MCGroups);
          if(mg != null)
            results.add(new SMT_SearchResult(type, searchString, mg, oms));
         }
        break;        
        
      case SEARCH_SA_KEY:
        // only return type that matches the string, and an SA Key looks like a guid
        if(sg == null)
          sg = new IB_Guid(0);
        else
        {
          // is this guid the SA_KEY?
          OptionsMap   = Fabric.getOptions();
          String key = "sa_key";
          String val = OptionsMap.get(key);
          if(val != null)
          {
            IB_Guid vg = new IB_Guid(val);
            if(sg.equals(vg))
              results.add(new SMT_SearchResult(type, searchString, sg, oms));
           }
        }
        break;        
        
      case SEARCH_SUBNET_KEY:
        // only return type that matches the string
        if(sg == null)
          sg = new IB_Guid(0);
        else
        {
          // is this guid the SUBNET_KEY?
          OptionsMap   = Fabric.getOptions();
          String key = "sm_key";
          String val = OptionsMap.get(key);
          if(val != null)
          {
            IB_Guid vg = new IB_Guid(val);
            if(sg.equals(vg))
              results.add(new SMT_SearchResult(type, searchString, sg, oms));
           }
        }
        break;        
        
      case SEARCH_SUBNET_PREFIX:
        // only return type that matches the string
        if(sg == null)
          sg = new IB_Guid(0);
        else
        {
          // is this guid the Subnet Prefix?
          OptionsMap   = Fabric.getOptions();
          String key = "subnet_prefix";
          String val = OptionsMap.get(key);
          if(val != null)
          {
            IB_Guid vg = new IB_Guid(val);
            if(sg.equals(vg))
              results.add(new SMT_SearchResult(type, searchString, sg, oms));
           }
        }
        break;        
        
      default:
          System.err.println("That's not a supported search result type (yet): " + type.getTypeNum() + ", and " + type.getName());
          break;
     }
    
  }
    return results;
  }
   
  protected static IB_Guid getGuid(String searchString)
  {
    // does not try to validate, just convert
    IB_Guid g = null;
    String colonString = getColonString(searchString);
    String nameString = getNameString(searchString);

    // both cannot be valid, its either a name or a colonString
    if (colonString.length() > 0)
    {
      try
      {
        // this MUST work, but fall through if not
        g = new IB_Guid(colonString);
      }
      catch (Exception e)
      {
//        System.err.println("Could not convert colon string as expected");
      }
    }

    // a lid, long, hex (with and without leading 0x) and a description or name
    if ((g == null) && (nameString.length() > 0))
    {
      try
      {
        // this MUST work, but fall through if not
        g = new IB_Guid(nameString);
      }
      catch (Exception e)
      {
//        System.err.println("Could not convert name string as expected");
      }
    }
    return g;
  }

  /**************************************************************************
   *** Method Name: getGuidByType
   **/
  /**
   *** Get the Guid for the node associated with the provided search string.
   *   The search string can be the node name, a lid, a guid, or even a port guid.
   *   
   *   If the boolean is true, return the ports guid, instead of the nodes guid
   *   
   *** <p>
   *** 
   *** @return the GLOBAL (or shared) SmtConsoleManager
   **************************************************************************/
  public static IB_Guid getGuidByType(String searchString, IB_GuidType gType, OpenSmMonitorService oms)
  {
    IB_Guid g = null;
    if((oms != null) && (oms.getFabric() != null))
    {
      OSM_Fabric Fabric = oms.getFabric();
      
      String colonString = getColonString(searchString);
      String nameString  = getNameString(searchString);
    
    // both cannot be valid, its either a name or a colonString
    if(colonString.length() > 0)
    {
      try
      {
        // this MUST work, but fall through if not
        g = new IB_Guid(colonString);
      }
      catch(Exception e)
      {
 //       System.err.println("Could not convert colon string as expected");
      }
    }
    
    // a lid, long, hex (with and without leading 0x) and a description or name
    if((g == null) && (nameString.length() > 0))
    {
      // this can be a name, lid, or guid (try name last)
      if(nameString.length() < 8)
      {
        // could be a lid
        try
        {
          int nodeLid = IB_Address.toLidValue(nameString);
          g = Fabric.getGuidFromLid(nodeLid);
        }
        catch(NumberFormatException nfe)
        {
          // perhaps a small name
//          System.err.println("Could not convert name to a guid using lid");
        }
      }
      else try
      {
        // this should handle longs and hex (with and without leading 0x)
        g = new IB_Guid(nameString);
      }
      catch(Exception e)
      {
//        System.err.println("Could not convert name string, using longs and lids");
      }
      
      if(g == null)
      {
        // it might be the name, or description
        g = Fabric.getGuidFromName(nameString);
      }
    }
    
    // try to validate this guid, by finding a node
    if(g != null)
    {
      // it is either a port guid, or a node guid, or system guid
      // is this a node guid?
      OSM_Node on = Fabric.getOSM_Node(g);
      if(on != null)
      {
        // got a node, but is that what I was asking for, null other wise
        if(gType == IB_GuidType.NODE_GUID)
          return g;
        return null;
      }
      
      OSM_Port op = Fabric.getOSM_Port(g);
      if(op != null)
      {
        // got a port, but is that what I was asking for, null other wise
        if(gType == IB_GuidType.PORT_GUID)
          return g;
        return null;
      }
      
      // If I am here, I have a valid looking guid, and I must have asked for
      // a system guid
      OSM_System sys = OSM_System.getOSM_System(Fabric, g);
      if(sys != null)
      {
        // got a port, but is that what I was asking for, null other wise
        if(gType == IB_GuidType.SYSTEM_GUID)
          return g;
       }
      
      // went through all valid types, to no avail
      logger.warning("Looks like a valid guid (" + g.toColonString() + ") but not the desired type: " + gType.getGuidName());
      g = null;
    }
    }
    return g;
  }
  
  protected static boolean isClearSearchResults(String searchString)
  {
    boolean clear = true;
    
    if((searchString != null) && (searchString.length() > 0))
    {
      // the search string can be just about anything, but if its a single space, then
      // it means clear
      if((searchString.length() > 1) || !(searchString.equals(" ")))
        return false;
    }
    return clear;
  }

  public static java.util.ArrayList<SMT_SearchResult> getSearchResults(String searchString, OpenSmMonitorService oms)
  {
    java.util.ArrayList<SMT_SearchResult> results     = new java.util.ArrayList<SMT_SearchResult>();
    if(isClearSearchResults(searchString))
      return results;
    
    // iterate through each type, and try to find any matches, or results
    for(SMT_SearchResultType st : SMT_SearchResultType.SEARCH_NORMAL_TYPES)
    {
      results.addAll(getSearchResults(st, searchString.trim(), oms));
    }
    SMT_SearchManager.getInstance().addSearch(searchString);
     
    return results;
  }
  
  public synchronized boolean addSearch(String searchString)
  {
    return SearchHistory.add(searchString);
  }

  public synchronized boolean removeSearch(String searchString)
  {
    return SearchHistory.remove(searchString);
  }

   /************************************************************
   * Method Name:
   *  getSearchHistory
   **/
  /**
   * Returns the value of searchHistory
   *
   * @return the searchHistory
   *
   ***********************************************************/
  
  public synchronized java.util.ArrayList<String> getSearchHistory()
  {
    return SearchHistory;
  }

  public static int getTrailingNumber(String searchString, boolean excludeFirst)
  {
    // return -1 if the trailing string can not be converted into an int
    //
    // attempt to convert the last word, and return it if possible
    //
    // if only one word, and it contains colons, attempt to convert the part after
    // the last colon, and return it.
    
    // if excludeFirst is true, then return -1 if there is only a single word to decode
    int p = -1;
    if(searchString != null)
    {
        // only interested in working on the last word
        String[] args = searchString.split(" ");
        int minimumLength = excludeFirst ? 2: 1;
        if((args != null) && (args.length > 0))
        {
          String lastWord = args[args.length -1];
          boolean containsColon = lastWord.contains(":");
          
          // only continue if the minimum length, or if there is a colon delimited number
          if((args.length >= minimumLength) || (containsColon))
          {
           
            if(containsColon)
            {
              String[] octets = lastWord.split(":");
              lastWord = octets[octets.length -1];
            }
            
            try
            {
              p = IB_Address.toLidValue(lastWord);
            }
            catch(Exception e)
            {
//              System.err.println("Could not parse the last word [" + lastWord + "] into an int");
            }
          }
        }
    }
     return p;
  }

  public static String getColonString(String searchString)
  {
    // return a word that can be converted into a guid, or empty
    //
    String colonString = "";
    if((searchString != null) && (searchString.contains(":")))
    {
      // the colon string MUST be the first contiguous string, throw away trailing
      String[] octets = (((searchString.trim()).split(" "))[0]).split(":");
//      String[] octets = (searchString.trim()).split(":");
      
      int numColons = 3;
      
      // if there are at least 4 octets, then use them, and ignore any trailing crap (like a port number)
      if(octets.length > numColons)
      {
        StringBuffer buff = new StringBuffer();
        
        // use just the first 4 octets, even if there are more than 4
        for(int i=0; i <= numColons; i++)
        {
          buff.append(octets[i].trim());
          if(i < numColons)
            buff.append(":");
        }
        colonString = buff.toString();
       }
    }
     return colonString;
  }


  public static String getNameString(String searchString)
  {
    // return all of the words that may be part of a description
    // or name
    //
    // trim off trailing numbers, and if its a valid colon string
    //
    // return an empty string, if nothing left after "trimming"
    String nameString = "";
    if(searchString != null)
    {
      // can't have a name AND a colon string, so see if this is a colon string
      if(getColonString(searchString).length() < 1)
      {
        // it wasn't a colon string, so determine if a number is appended
        int num = getTrailingNumber(searchString, false);
        if(num > 0)
        {
          // there is a trailing number, so trim it off
          
          // the number is either an extra word, or appended via a colon (trim off)
          int lastSpace = searchString.lastIndexOf(" ");
          int lastColon = searchString.lastIndexOf(":");
          int end = lastSpace > lastColon ? lastSpace: lastColon;
          end = end > 0 ? end: searchString.length();
          nameString = searchString.substring(0, end);
         }
        else
          // just a clean name string, so use it
          nameString = searchString;
      }
    }
     return nameString.trim();
  }


  /*-----------------------------------------------------------------------*/

  public Object clone() throws CloneNotSupportedException
  {
    throw new CloneNotSupportedException();
  }

  protected boolean initManager()
  {
    // need to obtain an OMS
    
    SearchHistory = new ArrayList<String>();

    return true;
  }

  /************************************************************
   * Method Name: main
   **/
  /**
   * Describe the method here
   * 
   * @see describe related java objects
   * 
   * @param args
   ***********************************************************/
  public static void main(String[] args)
  {
    // TODO Auto-generated method stub

  }

  public String getName()
  {
    return this.getClass().getSimpleName();
  }

}
