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
 *        file: OSM_NodeError.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_Port.PortCounterName;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Edge;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;

/**********************************************************************
 * Describe purpose and responsibility of OSM_NodeError
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Jun 14, 2013 8:01:48 AM
 **********************************************************************/
public class OSM_NodeError implements gov.llnl.lc.logging.CommonLogger
{
  /*
   * A record which represents the error(s) associated with a specific
   * OSM_Node.  A node has ports, usually linked to the ports of other
   * nodes.
   * 
   * A node has erros if there are errors on its ports, or if there is
   * some sort of problem with links.
   */
  
  IB_Vertex node;
  
  LinkedHashMap <String, String>             SwitchInfo;
  ArrayList <LinkedHashMap <String, String>> LinkInfo;
  HashMap<String, PFM_PortChange>            PortChangeInfo;
  
 
  /************************************************************
   * Method Name:
   *  OSM_NodeError
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param node
   ***********************************************************/
  public OSM_NodeError(IB_Vertex node, HashMap<String, PFM_PortChange> pChanges)
  {
    super();
    this.node = node;
    this.PortChangeInfo = pChanges;
    
    if(node != null)
      generateMaps();
  }
  
  private boolean generateMaps()
  {
    SwitchInfo  = new LinkedHashMap <String, String>();
    ArrayList <IB_Edge> el = node.getEdges();
    ArrayList <IB_Edge> eel = new ArrayList <IB_Edge>();
    String timestamp = "unknown";
    int ndex = 0;
    for(IB_Edge e: el)
    {
      if(ndex == 0)
        timestamp = e.getEndPort1().pfmPort.getErrorTimeStamp().toString();
      if(e.hasError(node))
      {
        eel.add(e);
      }
    }
    
    // the switch info map just contains some basic info about the node
    // node name, guid, #ports, #ports with errors, #links, # links with errors, timestamp
    SwitchInfo.put("name", node.getNode().sbnNode.description);
    SwitchInfo.put("guid", node.getGuid().toColonString());
    SwitchInfo.put("num_ports", Short.toString(node.getNode().sbnNode.num_ports));
    SwitchInfo.put("num_error_ports", Integer.toString(el.size()));
    SwitchInfo.put("num_links", Integer.toString(el.size()));
    SwitchInfo.put("num_error_links", Integer.toString(eel.size()));
    SwitchInfo.put("timestamp", timestamp);
    
    // now go get the link info map for this switch
    generateLinkMap(eel);
    
     return true;
  }
  
  private boolean generateLinkMap(ArrayList <IB_Edge> edgeErrorList)
  {
    // only represent the errors that occur on the nodes side of the link
    
    // the link info map will be an ordered (by port number) list which contains
    // port number (internal or external?), lid,  link info (width, speed, state), remote info (guid, port num, lid, name)
    if((edgeErrorList == null) || (edgeErrorList.size() < 1))
      return false;
    
    LinkInfo    = new ArrayList <LinkedHashMap <String, String>> ();
    for(IB_Edge e: edgeErrorList)
    {
      LinkedHashMap <String, String> linkInfo = new LinkedHashMap <String, String>();
      
      // find the OSM_Port associated with this vertex (1 or 2)
      OSM_Port p1 = e.getEndPort1();
      OSM_Port p2 = e.getEndPort2();
      IB_Vertex remote = e.getEndpoint2();
      if(node.getGuid().equals(new IB_Guid(p1.pfmPort.node_guid)))
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
           linkInfo.put("port number", new String(Integer.toString(p1.getPortNumber())));
           linkInfo.put("lid", "lid not yet available");
           linkInfo.put("link info", "link info (speed, width, state) not yet available");
           
           // build a single string to represent the remote side
           String remoteInfo = remote.toString() + " port: " + p2.getPortNumber();
           linkInfo.put("remote info", remoteInfo);
           
           // build a single string to represent the traffic on this link
           linkInfo.put("traffic info", generateTrafficString(p1));
           linkInfo.put("traffic delta info", generateTrafficDeltaString(p1));
          
           // build a single string to represent the errors on this link
           linkInfo.put("error info", generateErrorString(p1));
           linkInfo.put("error delta info", generateErrorDeltaString(p1));
           
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
    String key = PFM_PortChange.getPFM_PortChangeKey(node.getGuid(), port.getPortNumber());
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
    String key = PFM_PortChange.getPFM_PortChangeKey(node.getGuid(), port.getPortNumber());
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
  
  public Set<String> getSwitchInfoKeySet()
  {
    if(SwitchInfo == null)
      return null;
    return SwitchInfo.keySet();
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
  
  public String getSwitchInfo(String key)
  {
    if(key == null)
      return null;
    return SwitchInfo.get(key);
  }
  
  public class SortLinkByPort implements Comparator <LinkedHashMap<String, String>>
  {
    // sort on the port number

    @Override
    public int compare(LinkedHashMap<String, String> o1, LinkedHashMap<String, String> o2)
    {
      String pn1 = o1.get("port number");
      String pn2 = o2.get("port number");
      int v1 = Integer.parseInt(pn1);
      int v2 = Integer.parseInt(pn2);
      return v1-v2;
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
  
  

}
