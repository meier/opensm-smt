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
 *        file: OSM_NodeActivity.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Set;

import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_Port.PortCounterName;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Edge;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;

/**********************************************************************
 * The OSM_NodeActivity object represents the dynamic behavior and state
 * of a node.  This normally means that it must be constructed from two
 * different snapshots (in time) of the node, so differences or changes
 * can be obtained.
 * 
 * The OSM_NodeActivity object contains records that represent traffic
 * or errors on the various ports of the node.
 * 
 * This object may be empty (not null, just empty) if there is no activity
 * in the node between the two snapshots.
 * 
 * <p>
 * This object is typically created by first getting an instance of a
 * FabricDelta, from which everything else can be obtained.  The constructor
 * needs an IB_Vertex (obtained from a vertexMap obtained using a fabric),
 * and a map of all the port changes, obtained from the fabric delta. 
 * <p>
 * @see  OSM_ActivityType
 * @see  OSM_FabricDelta#getOSM_FabricDelta(String, String)
 * @see  OSM_FabricDelta#getPortChangesFromNode(OSM_Node)
 * @see  IB_Vertex#createVertexMap(OSM_Fabric)
 * 
 *
 * @author meier3
 * 
 * @version Jun 14, 2013 8:01:48 AM
 **********************************************************************/
public class OSM_NodeActivity implements gov.llnl.lc.logging.CommonLogger
{
  /** The node, which may or may not have activity **/
  IB_Vertex Vertex;
  
  /** A (sortable) map of name/value pairs specific to the top level node, switch, vertex **/
  LinkedHashMap <String, String>             NodeInfo;
  
  /** A list of (sortable) maps of name/value pairs specific to the edges or links **/
  ArrayList <LinkedHashMap <String, String>> LinkInfo;
  
  /** The Activity, defined as changes that have occurred on a (child) port of this node,
   * typically activity is either traffic, or errors
   *  **/
  LinkedHashMap<String, PFM_PortChange>      PortChangeInfo;
  
 
  /************************************************************
   * Method Name:
   *  OSM_NodeActivity
  **/
  /**
   * A node (IB_Vertex), and the changes that have occurred on its
   * ports (PFM_PortChange) over time.  Typically this object is
   * created from an instance of an OSM_FabricDelta object.
   *
   * @see     describe related java objects
   *
   * @param Vertex
   ***********************************************************/
  public OSM_NodeActivity(IB_Vertex Vertex, LinkedHashMap<String, PFM_PortChange> pChanges)
  {
    super();
    this.Vertex = Vertex;
    this.PortChangeInfo = pChanges;
    
    if((Vertex != null) && (pChanges != null) && (pChanges.size() != 0))
      generateMaps();
  }
  
  public OSM_NodeActivity(IB_Vertex Vertex, OSM_FabricDelta fabricDelta)
  {
    super();
     
    if((Vertex != null) && (fabricDelta != null))
    {
      OSM_Fabric fabric2 = fabricDelta.getFabric2();
      if(fabric2 != null)
      {
        PortChangeInfo = fabricDelta.getPortChangesFromNode(Vertex.getNode());
        this.Vertex = Vertex;
        generateMaps();
      }

    }
   }
  
  private boolean generateMaps()
  {
    // this method only gets called if data from the constructor is valid data
    
    // SAVE (put) anything and everything worth saving in the hash maps.  The top
    // level map is for the node, and the array of maps is for the nodes links,
    // and ports.
    // 
    
    NodeInfo  = new LinkedHashMap <String, String>();
    ArrayList <IB_Edge> el = Vertex.getEdges();
    
    // from the PortChanges (activity), find the edges that "contain" the active ports
    ArrayList <IB_Edge> ael = new ArrayList <IB_Edge>();
    String timestamp = "unknown";
    int ndex = 0;
    for(IB_Edge e: el)
    {
      if(ndex == 0)
        timestamp = e.getEndPort1().pfmPort.getErrorTimeStamp().toString();
      
      // does this edge have activity?
      String key = PFM_PortChange.getPFM_PortChangeKey(e.Endpoint1.getGuid(), e.getEndPort1().getPortNumber());
      PFM_PortChange pc1 = PortChangeInfo.get(key);
      key = PFM_PortChange.getPFM_PortChangeKey(e.Endpoint2.getGuid(), e.getEndPort2().getPortNumber());
      PFM_PortChange pc2 = PortChangeInfo.get(key);
      
      // if either side of this link has a port with "activity", then the PortChange object will NOT be null
      
      if((pc1 != null) || (pc2 != null))
      {
        // must have activity, so add it
         ael.add(e);
      }
    }
    
    // the switch info map just contains some basic info about the node
    // node name, guid, #ports, #ports with errors, #links, # links with errors, timestamp
    NodeInfo.put(OSM_ActivityType.OAT_NAME.name(), Vertex.getNode().sbnNode.description);
    NodeInfo.put(OSM_ActivityType.OAT_GUID.name(), Vertex.getGuid().toColonString());
    NodeInfo.put(OSM_ActivityType.OAT_NUM_PORTS.name(), Short.toString(Vertex.getNode().sbnNode.num_ports));
    NodeInfo.put(OSM_ActivityType.OAT_NUM_ACTIVE_PORTS.name(), Integer.toString(ael.size()));
    NodeInfo.put(OSM_ActivityType.OAT_NUM_LINKS.name(), Integer.toString(el.size()));
    NodeInfo.put(OSM_ActivityType.OAT_NUM_ACTIVE_LINKS.name(), Integer.toString(ael.size()));
    NodeInfo.put(OSM_ActivityType.OAT_NODE_LEVEL.name(), Integer.toString(Vertex.getDepth()));
    NodeInfo.put(OSM_ActivityType.OAT_TIMESTAMP.name(), timestamp);
    
    
    // now go get the link info map for this switch
    generateLinkMap(ael);
    
     return true;
  }
  
  private boolean generateLinkMap(ArrayList <IB_Edge> edgeActiveList)
  {
    // only represent the activity that occur on the nodes side of the link
    
    // the link info map will be an ordered (by port number) list which contains
    // port number (internal or external?), lid,  link info (width, speed, state), remote info (guid, port num, lid, name)
    
    if((edgeActiveList == null) || (edgeActiveList.size() < 1))
      return false;
    
    LinkInfo    = new ArrayList <LinkedHashMap <String, String>> ();
    for(IB_Edge e: edgeActiveList)
    {
      LinkedHashMap <String, String> linkInfo = new LinkedHashMap <String, String>();
      
      // find the OSM_Port associated with this vertex (1 or 2)
      
      OSM_Port p1 = e.getEndPort1();
      OSM_Port p2 = e.getEndPort2();
      IB_Vertex remote = e.getEndpoint2();
      if(Vertex.getGuid().equals(new IB_Guid(p1.pfmPort.node_guid)))
      {
        // use 1
      }
      else
      {
        // use 2
        p1 = e.getEndPort2();
        p2 = e.getEndPort1();
        remote = e.getEndpoint1();
      }
      
      
      // p1 is the port for this vertex, and it should have an error
      if(p1.hasError())
      {
        // port number (internal or external?), lid,  link info (width, speed, state), remote info (guid, port num, lid, name)
           linkInfo.put(OSM_ActivityType.OAT_LINK_PORT_NUM.name(), new String(Integer.toString(p1.getPortNumber())));
           linkInfo.put(OSM_ActivityType.OAT_LID.name(), "lid not yet available");
           linkInfo.put(OSM_ActivityType.OAT_LINK_LEVEL.name(), Integer.toString(e.getDepth()));
          linkInfo.put(OSM_ActivityType.OAT_LINK_INFO.name(), "link info (speed, width, state) not yet available");
           
           // build a single string to represent the remote side
           String remoteInfo = remote.toString() + " port: " + p2.getPortNumber();
           linkInfo.put(OSM_ActivityType.OAT_LINK_REMOTE_INFO.name(), remoteInfo);
           
           // build a single string to represent the traffic on this link
           linkInfo.put(OSM_ActivityType.OAT_TRAFFIC_INFO.name(), generateTrafficString(p1));
           linkInfo.put(OSM_ActivityType.OAT_TRAFFIC_DELTA_INFO.name(), generateTrafficDeltaString(p1));
          
           // build a single string to represent the errors on this link
           linkInfo.put(OSM_ActivityType.OAT_ERROR_INFO.name(), generateErrorString(p1));
           linkInfo.put(OSM_ActivityType.OAT_ERROR_DELTA_INFO.name(), generateErrorDeltaString(p1));
           
           // all done, so add it to the List
           LinkInfo.add(linkInfo);
           
           // 
      }
      else
        logger.severe("Logic error in OSM_NodeError for links");
    }
    Collections.sort(LinkInfo, new SortLinkByPort());
   return true;
  }
  
  private String generateTrafficDeltaString(OSM_Port port)
  {
    if(port == null)
      return null;
    
    // find port changes that correspond to this port (the key is 
    String key = PFM_PortChange.getPFM_PortChangeKey(Vertex.getGuid(), port.getPortNumber());
    PFM_PortChange pChange = PortChangeInfo.get(key);
    
    if(pChange == null)
      return null;
    
    StringBuffer buff = new StringBuffer();
    // return just the four big counters
    PortCounterName pc = PortCounterName.xmit_data;
    buff.append("[" + pc.name() + "=" + Long.toString(pChange.getDelta_port_counter(pc)) + "]");
    pc = PortCounterName.rcv_data;
    buff.append(" [" + pc.name() + "=" + Long.toString(pChange.getDelta_port_counter(pc)) + "]");
    pc = PortCounterName.xmit_pkts;
    buff.append(" [" + pc.name() + "=" + Long.toString(pChange.getDelta_port_counter(pc)) + "]");
    pc = PortCounterName.rcv_pkts;
    buff.append(" [" + pc.name() + "=" + Long.toString(pChange.getDelta_port_counter(pc)) + "]");
    
    return buff.toString();
  }
  
  private String generateTrafficString(OSM_Port port)
  {
    if(port == null)
      return null;
    
    StringBuffer buff = new StringBuffer();
    // return just the four big counters
    PortCounterName pc = PortCounterName.xmit_data;
    buff.append("[" + pc.name() + "=" + Long.toString(port.pfmPort.port_counters[pc.ordinal()]) + "]");
    pc = PortCounterName.rcv_data;
    buff.append(" [" + pc.name() + "=" + Long.toString(port.pfmPort.port_counters[pc.ordinal()]) + "]");
    pc = PortCounterName.xmit_pkts;
    buff.append(" [" + pc.name() + "=" + Long.toString(port.pfmPort.port_counters[pc.ordinal()]) + "]");
    pc = PortCounterName.rcv_pkts;
    buff.append(" [" + pc.name() + "=" + Long.toString(port.pfmPort.port_counters[pc.ordinal()]) + "]");
    
    return buff.toString();
  }
  
  private String generateErrorString(OSM_Port port)
  {
    if(port == null)
      return null;
    
    StringBuffer buff = new StringBuffer();
    
    // return just the four big counters
    int ndex = 0;
    for(PFM_Port.PortCounterName pc : PortCounterName.PFM_ERROR_COUNTERS)
    {
      if(port.pfmPort.port_counters[pc.ordinal()] != 0)
      {
        if(ndex != 0)
          buff.append(" ");
        buff.append("[" + pc.name() + "=" + Long.toString(port.pfmPort.port_counters[pc.ordinal()]) + "]");
        ndex++;
      }
     }
    
    return buff.toString();
  }
  
  private String generateErrorDeltaString(OSM_Port port)
  {
    if(port == null)
      return null;
    
    StringBuffer buff = new StringBuffer();
    // find port changes that correspond to this port (the key is 
    String key = PFM_PortChange.getPFM_PortChangeKey(Vertex.getGuid(), port.getPortNumber());
    PFM_PortChange pChange = PortChangeInfo.get(key);
    
    // return just the four big counters
    int ndex = 0;
    for(PFM_Port.PortCounterName pc : PortCounterName.PFM_ERROR_COUNTERS)
    {
      if(pChange != null)
      {
        if(ndex != 0)
          buff.append(" ");
        buff.append("[" + pc.name() + "=" + Long.toString(pChange.getDelta_port_counter(pc)) + "]");
        ndex++;
      }
     }
    
    return buff.toString();
  }
  
  public Set<String> getNodeInfoKeySet()
  {
    if(NodeInfo == null)
      return null;
    return NodeInfo.keySet();
  }
  
  public Set<String> getLinkInfoKeySet()
  {
    if((LinkInfo == null) || (LinkInfo.size() < 1))
      return null;
    return LinkInfo.get(0).keySet();
  }
  
  public int getNumLinks()
  {
    if(LinkInfo == null)
      return 0;
    return LinkInfo.size();
  }
  
  public String getNodeInfo(String key)
  {
    // the SwitchInfo object gets created in generateMaps()
    if((key == null) || (NodeInfo == null))
      return null;
    return NodeInfo.get(key);
  }
  
  public class SortLinkByPort implements Comparator <LinkedHashMap<String, String>>
  {
    // sort on the port number.  The two objects are supposed to be elements from
    // the LinkInfo array list.
    @Override
    public int compare(LinkedHashMap<String, String> o1, LinkedHashMap<String, String> o2)
    {
      int rtnval = -1; //default return value
      try
      {
        String pn1 = o1.get(OSM_ActivityType.OAT_LINK_PORT_NUM.name());
        String pn2 = o2.get(OSM_ActivityType.OAT_LINK_PORT_NUM.name());
        int v1 = Integer.parseInt(pn1);
        int v2 = Integer.parseInt(pn2);
        rtnval =  v1-v2;
      }
      catch (Exception e)
      {
        // probably a number format exception or null
        // just catch and continue, sort is impossible
      }
      
      return rtnval;
    }
  }

  /************************************************************
   * Method Name:
   *  getLinkInfo
   **/
  /**
   * returns a sorted version of this list, based on the port
   * number
   *
   * @return the linkInfo
   *
   ***********************************************************/
  
  public ArrayList<LinkedHashMap<String, String>> getLinkInfo()
  {
    return LinkInfo;
  }

  /************************************************************
   * Method Name:
   *  isActive
   **/
  /**
   * Returns true if the object contains some sort of activity.
   * An idle node may cause this to be false.  
   *
   * @return true if there is port change information
   *
   ***********************************************************/
  
  public boolean isActive()
  {
    if((PortChangeInfo == null) || (PortChangeInfo.size() == 0))
      return false;
    return true;
  }

}
