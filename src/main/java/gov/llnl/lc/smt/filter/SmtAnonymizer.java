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
 *        file: SmtAnonymizer.java
 *
 *  Created on: Aug 4, 2017
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.filter;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Nodes;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Ports;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Subnet;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_SysInfo;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_Node;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.SBN_Manager;
import gov.llnl.lc.infiniband.opensm.plugin.data.SBN_MulticastGroup;
import gov.llnl.lc.infiniband.opensm.plugin.data.SBN_Node;
import gov.llnl.lc.infiniband.opensm.plugin.data.SBN_PartitionKey;
import gov.llnl.lc.infiniband.opensm.plugin.data.SBN_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.SBN_Router;
import gov.llnl.lc.infiniband.opensm.plugin.data.SBN_Switch;
import gov.llnl.lc.infiniband.opensm.plugin.event.OSM_EventStats;
import gov.llnl.lc.infiniband.opensm.plugin.net.OsmServerStatus;
import gov.llnl.lc.net.MultiSSLServerStatus;
import gov.llnl.lc.net.ObjectSession;
import gov.llnl.lc.smt.command.SmtCommand;
import gov.llnl.lc.smt.props.AnonymizeProperties;

/**********************************************************************
 * The SmtAnonymizer can be used to convert identifying information in
 * OMS records into something else.  Typically this is used to strip out
 * data that may be considered sensitive or private, while maintaining
 * the integrity of the OMS data set.
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Aug 4, 2017 10:52:15 AM
 **********************************************************************/
public class SmtAnonymizer implements Serializable, gov.llnl.lc.logging.CommonLogger
{
  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = 1L;
  
  public AnonymizeProperties aProp;

  protected String serverAnonymous     = "AnonymousServer";
  protected String userAnonymous       = "AnonymousUser";
  protected String hostAnonymous       = "AnonymousHost";

  protected String Description         = "unknown";
  protected String FabricName          = "anonymized";
  protected String ObjectNameAnonymize = "anonymized";
  protected long   guidAnonymize       = 0xff;
  protected long   subnetAnonymize     = 0xff;
  protected long   keyAnonymize        = 0xff;
 
  /************************************************************
   * Method Name:
   *  SmtAnonymizer
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   ***********************************************************/
  public SmtAnonymizer()
  {
    super();
  }
  
  /************************************************************
   * Method Name:
   *  SmtAnonymizer
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param anonymizerFileName
   * @throws IOException 
   ***********************************************************/
  public SmtAnonymizer(String anonymizerFileName) throws IOException
  {
    this(anonymizerFileName, null);
  }
  
  /************************************************************
   * Method Name:
   *  SmtAnonymizer
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param val
   * @param oMService
   ***********************************************************/
  public SmtAnonymizer(String anonymizerFileName, OpenSmMonitorService OMS) throws IOException
  {
    super();
    String fName = SmtCommand.convertSpecialFileName(anonymizerFileName);
    if(fName != null)
      aProp = new AnonymizeProperties(fName);

    if(aProp != null)
    {
      Description         = aProp.getDescription();
      FabricName          = aProp.getFabricName();
      ObjectNameAnonymize = aProp.getName();
      guidAnonymize       = aProp.getGuidOffset();
      subnetAnonymize     = aProp.getSubnetOffset();
      keyAnonymize        = aProp.getKeyOffset();
      serverAnonymous     = aProp.getServer();
      userAnonymous       = aProp.getUser();
      hostAnonymous       = aProp.getHost();
    }
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
  public static void main(String[] args)
  {
  }

  /************************************************************
   * Method Name:
   *  hasEmptyAnonymizer
  **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   * @return
   ***********************************************************/
  public boolean hasEmptyAnonymizer()
  {
    return aProp == null;
  }

  /************************************************************
   * Method Name:
   *  getOpenSmMonitorService
  **/
  /**
   * Anonymize the provided OMS using the rules in the provided anonymizer.
   *
   * @see     describe related java objects
   *
   * @param oms
   * @param anonymizer
   * @return
   ***********************************************************/
  public static OpenSmMonitorService getOpenSmMonitorService(OpenSmMonitorService oms, SmtAnonymizer anonymizer)
  {
    // anonymize the original, and add to new History

    // take apart the OMS, and anonymize each piece, before putting it back together
    
    ObjectSession session  = anonymizer.anonymize(oms.getParentSessionStatus());
    OsmServerStatus server = anonymizer.anonymize(oms.getRemoteServerStatus());
    OSM_Fabric fabric      = anonymizer.anonymize(oms.getFabric());
    
    return new OpenSmMonitorService(session, server, fabric);
  }

  /************************************************************
   * Method Name:
   *  anonymize
  **/
  /**
   * Remove any private, sensitive, or identifiable information from
   * this object, while making sure it is still viable.
   *
   * @see     describe related java objects
   *
   * @param fabric
   * @return
   ***********************************************************/
  private OSM_Fabric anonymize(OSM_Fabric fabric)
  {
    // the easiest way to anonymize the fabric, is to anonymize
    // each component, and then create a new fabric from those parts
    
    OSM_Nodes aNodes       = anonymize(fabric.getOsmNodes());
    OSM_Ports aPorts       = anonymize(fabric.getOsmPorts());
    OSM_Subnet aSubnet     = anonymize(fabric.getOsmSubnet());
    OSM_SysInfo aSysInfo   = anonymize(fabric.getOsmSysInfo());
    OSM_EventStats aEstats = anonymize(fabric.getOsmEventStats());
    
    return new OSM_Fabric(FabricName, aNodes, aPorts, fabric.getOsmStats(), aSubnet, aSysInfo, aEstats);
  }

  /************************************************************
   * Method Name:
   *  anonymize
  **/
  /**
   * Remove any private, sensitive, or identifiable information from
   * this object, while making sure it is still viable.
   *
   * @see     describe related java objects
   *
   * @param osmEventStats
   * @return
   ***********************************************************/
  private OSM_EventStats anonymize(OSM_EventStats osmEventStats)
  {
    // these are just counters, nothing to anonymize
    return osmEventStats;
  }

  /************************************************************
   * Method Name:
   *  anonymize
  **/
  /**
   * Remove any private, sensitive, or identifiable information from
   * this object, while making sure it is still viable.
   *
   * @see     describe related java objects
   *
   * @param osmSysInfo
   * @return
   ***********************************************************/
  private OSM_SysInfo anonymize(OSM_SysInfo osmSysInfo)
  {
    // these are just counters, nothing to anonymize
    return osmSysInfo;
  }

  /************************************************************
   * Method Name:
   *  anonymize
  **/
  /**
   * Remove any private, sensitive, or identifiable information from
   * this object, while making sure it is still viable.
   *
   * @see     describe related java objects
   *
   * @param osmSubnet
   * @return
   ***********************************************************/
  private OSM_Subnet anonymize(OSM_Subnet osmSubnet)
  {
    // these are all public members, just replace the ones that are
    // necessary, and leave the rest
    
    osmSubnet.Options.ca_name       = ObjectNameAnonymize + " Channel Adapter";
    osmSubnet.Options.guid          += guidAnonymize;
    osmSubnet.Options.m_key         += keyAnonymize;
    osmSubnet.Options.sm_key        += keyAnonymize;
    osmSubnet.Options.sa_key        += keyAnonymize;
    osmSubnet.Options.cc_key        += keyAnonymize;
    osmSubnet.Options.subnet_prefix += subnetAnonymize;
        
    SBN_Manager []        Managers = anonymize(osmSubnet.Managers);
    SBN_Router []          Routers = anonymize(osmSubnet.Routers);
    SBN_Switch []         Switches = anonymize(osmSubnet.Switches);
    SBN_PartitionKey []      PKeys = anonymize(osmSubnet.PKeys);
    SBN_MulticastGroup [] MCGroups = anonymize(osmSubnet.MCGroups);
    
    osmSubnet.Managers = Managers;
    osmSubnet.Routers  = Routers;
    osmSubnet.Switches = Switches;
    osmSubnet.PKeys    = PKeys;
    osmSubnet.MCGroups = MCGroups;
    
    return osmSubnet;
  }

  /************************************************************
   * Method Name:
   *  anonymize
  **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   * @param mCGroups
   * @return
   ***********************************************************/
  private SBN_MulticastGroup[] anonymize(SBN_MulticastGroup[] mCGroups)
  {
    for(SBN_MulticastGroup mg: mCGroups)
    {
      // replace guids
      for(int ndex = 0; ndex < mg.port_guids.length; ndex++)
        mg.port_guids[ndex] += guidAnonymize;
    }
    return mCGroups;
  }

  /************************************************************
   * Method Name:
   *  anonymize
  **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   * @param pKeys
   * @return
   ***********************************************************/
  private SBN_PartitionKey[] anonymize(SBN_PartitionKey[] pKeys)
  {
    for(SBN_PartitionKey pK: pKeys)
    {
      // replace keys and guid
      pK.pkey += keyAnonymize;
      
      // replace guids
      for(int ndex = 0; ndex < pK.full_members; ndex++)
        pK.full_member_guids[ndex] += guidAnonymize;
      
      for(int ndex = 0; ndex < pK.full_member_guids.length; ndex++)
        pK.full_member_guids[ndex] += guidAnonymize;
    }
    return pKeys;
  }

  /************************************************************
   * Method Name:
   *  anonymize
  **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   * @param switches
   * @return
   ***********************************************************/
  private SBN_Switch[] anonymize(SBN_Switch[] switches)
  {
    for(SBN_Switch s: switches)
      // replace names and guid
      s.guid += guidAnonymize;

    return switches;
  }

  /************************************************************
   * Method Name:
   *  anonymize
  **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   * @param routers
   * @return
   ***********************************************************/
  private SBN_Router[] anonymize(SBN_Router[] routers)
  {
    for(SBN_Router r: routers)
      r.guid += guidAnonymize;

    return routers;
  }

  /************************************************************
   * Method Name:
   *  anonymize
  **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   * @param managers
   * @return
   ***********************************************************/
  private SBN_Manager[] anonymize(SBN_Manager[] managers)
  {
    for(SBN_Manager m: managers)
    {
      // replace names and guid
      m.guid += guidAnonymize;
      m.sm_key += keyAnonymize;
    }
    return managers;
  }

  /************************************************************
   * Method Name:
   *  anonymize
  **/
  /**
   * Remove any private, sensitive, or identifiable information from
   * this object, while making sure it is still viable.
   *
   * @see     describe related java objects
   *
   * @param osmPorts
   * @return
   ***********************************************************/
  private OSM_Ports anonymize(OSM_Ports osmPorts)
  {
    // create new Ports from the PerfMgr and Subnet ports
    
    PFM_Port[] perfMgrPorts = anonymize(osmPorts.PerfMgrPorts);
    SBN_Port[] subnPorts    = anonymize(osmPorts.SubnPorts);
    
    return new OSM_Ports(perfMgrPorts, subnPorts);
  }

  /************************************************************
   * Method Name:
   *  anonymize
  **/
  /**
   * Remove any private, sensitive, or identifiable information from
   * this object, while making sure it is still viable.
   *
   * @see     describe related java objects
   *
   * @param subnPorts
   * @return
   ***********************************************************/
  private SBN_Port[] anonymize(SBN_Port[] subnPorts)
  {
    for(SBN_Port p: subnPorts)
    {
      // replace names and guid
      p.node_guid               += guidAnonymize;
      p.port_guid               += guidAnonymize;
      p.linked_node_guid        += guidAnonymize;
      p.linked_port_guid        += guidAnonymize;
      
      p.port_info.subnet_prefix += subnetAnonymize;
      
      // nothing in extended port info, yet...
    }
    return subnPorts;
  }

  /************************************************************
   * Method Name:
   *  anonymize
  **/
  /**
   * Remove any private, sensitive, or identifiable information from
   * this object, while making sure it is still viable.
   *
   * @see     describe related java objects
   *
   * @param perfMgrPorts
   * @return
   ***********************************************************/
  private PFM_Port[] anonymize(PFM_Port[] perfMgrPorts)
  {
    for(PFM_Port p: perfMgrPorts)
      // replace names and guid
      p.node_guid += guidAnonymize;

    return perfMgrPorts;
  }

  /************************************************************
   * Method Name:
   *  anonymize
  **/
  /**
   * Remove any private, sensitive, or identifiable information from
   * this object, while making sure it is still viable.
   *
   * @see     describe related java objects
   *
   * @param osmNodes
   * @return
   ***********************************************************/
  private OSM_Nodes anonymize(OSM_Nodes osmNodes)
  {
    // create new Nodes from the PerfMgr and Subnet nodes
    PFM_Node[] perfMgrNodes = anonymize(osmNodes.PerfMgrNodes);
    SBN_Node[] subnNodes    = anonymize(osmNodes.SubnNodes);
    
    return new OSM_Nodes(perfMgrNodes, subnNodes);
  }

  /************************************************************
   * Method Name:
   *  anonymize
  **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   * @param subnNodes
   * @return
   ***********************************************************/
  private SBN_Node[] anonymize(SBN_Node[] subnNodes)
  {
    for(SBN_Node n: subnNodes)
    {
      // replace names and guid
      n.node_guid += guidAnonymize;
      n.port_guid += guidAnonymize;
      n.sys_guid  += guidAnonymize;
      n.description = n.getNodeGuid().toColonString() + " " + ObjectNameAnonymize;
    }
    return subnNodes;
  }

  /************************************************************
   * Method Name:
   *  anonymize
  **/
  /**
   * Remove any private, sensitive, or identifiable information from
   * this object, while making sure it is still viable.
   *
   *
   * @see     describe related java objects
   *
   * @param perfMgrNodes
   * @return
   ***********************************************************/
  private PFM_Node[] anonymize(PFM_Node[] perfMgrNodes)
  {
    for(PFM_Node n: perfMgrNodes)
    {
      // replace names and guid
      n.node_guid += guidAnonymize;
      n.node_name = n.getNodeGuid().toColonString() + " " + ObjectNameAnonymize;
    }
    return perfMgrNodes;
  }

  /************************************************************
   * Method Name:
   *  anonymize
  **/
  /**
   * Remove any private, sensitive, or identifiable information from
   * this object, while making sure it is still viable.
   *
   * @see     describe related java objects
   *
   * @param remoteServerStatus
   * @return
   ***********************************************************/
  private OsmServerStatus anonymize(OsmServerStatus remoteServerStatus)
  {
    MultiSSLServerStatus server = remoteServerStatus.Server;
    server.setHost(serverAnonymous);
    
    java.util.ArrayList <ObjectSession> current_Sessions    = anonymize(server.getCurrent_Sessions());
    java.util.ArrayList <ObjectSession> historical_Sessions = anonymize(server.getHistorical_Sessions());

    server.setCurrent_Sessions(current_Sessions);
    server.setHistorical_Sessions(historical_Sessions);
    
    remoteServerStatus.Server = server;
    return remoteServerStatus;
  }
  
  /************************************************************
   * Method Name:
   *  anonymize
  **/
  /**
   * Remove any private, sensitive, or identifiable information from
   * this object, while making sure it is still viable.
   *
   * @see     describe related java objects
   *
   * @param current_Sessions
   * @return
   ***********************************************************/
  private ArrayList<ObjectSession> anonymize(ArrayList<ObjectSession> sessions)
  {
    if(sessions == null)
      return sessions;
    
    for(ObjectSession o: sessions)
      o = anonymize(o);
    return sessions;
  }

  /************************************************************
   * Method Name:
   *  anonymize
  **/
  /**
   * Remove any private, sensitive, or identifiable information from
   * this object, while making sure it is still viable.
   *
   * @see     describe related java objects
   *
   * @param parentSessionStatus
   * @return
   ***********************************************************/
  private ObjectSession anonymize(ObjectSession parentSessionStatus)
  {
    parentSessionStatus.setUser(userAnonymous);
    parentSessionStatus.setHost(hostAnonymous);

    return parentSessionStatus;
  }

}
