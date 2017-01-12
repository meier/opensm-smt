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
 *        file: SubnetTreeModel.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.tree;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import gov.llnl.lc.infiniband.core.IB_Address;
import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.opensm.plugin.data.MAD_Counter;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Updater;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Stats;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Subnet;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_SysInfo;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.infiniband.opensm.plugin.data.SBN_Manager;
import gov.llnl.lc.infiniband.opensm.plugin.event.OSM_EventStats;
import gov.llnl.lc.infiniband.opensm.plugin.event.OsmEvent;
import gov.llnl.lc.infiniband.opensm.plugin.net.OsmServerStatus;
import gov.llnl.lc.net.ObjectSession;
import gov.llnl.lc.smt.command.SmtCommandType;
import gov.llnl.lc.smt.command.about.SmtAbout;
import gov.llnl.lc.smt.command.about.SmtAboutRecord;
import gov.llnl.lc.smt.data.SMT_UpdateService;
import gov.llnl.lc.smt.prefs.SmtGuiPreferences;

public class SubnetTreeModel extends DefaultTreeModel
{  
  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = -7873686995461929626L;
  
  private UserObjectTreeNode rootNode;
  private OpenSmMonitorService OSM;

  public UserObjectTreeNode getRootNode()
  {
    return rootNode;
  }
  
  public OpenSmMonitorService getRoot()
  {
    return OSM;
  }
  
  public OSM_Stats getMAD_Stats()
  {
    return OSM.getFabric().getOsmStats();
  }
  
  public OSM_EventStats getEvent_Stats()
  {
    return OSM.getFabric().getOsmEventStats();
  }
  

  /************************************************************
   * Method Name:
   *  PortTreeModel
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param root
   * @param asksAllowsChildren
   ***********************************************************/
  public SubnetTreeModel(TreeNode root)
  {
    super(root, true);
    // TODO Auto-generated constructor stub
  }

  public SubnetTreeModel(OpenSmMonitorService osm)
  {
    super(null, true);
    // assume it is fully constructed
    
    // this is the normal preferred way to construct the tree model
    
    // the tree is made up of VertexMemberTreeNodes, which contains a name/value pair
    //    the name is just type, and the value is the object
    //
    //  the root is an OSM_Port, determined by an IB_Edge and an IB_Vertex
     
    // go no further, we should have both parts
    if(osm == null)
      return;
    
    this.OSM = osm;
    NameValueNode      vmn = new NameValueNode("subnet name", OSM.getFabricName());
    UserObjectTreeNode vmtn = new UserObjectTreeNode(vmn, true);
    rootNode = vmtn;
    
    addSubnetManagers(rootNode);
    addSubnetAdministrators(rootNode);
    addPerformanceManagers(rootNode);
    addEventCounters(rootNode);
    addMadStatistics(rootNode);
    addOpenSmMonitorSerivce(rootNode);
    addOptionsMap(rootNode);
  }
  
  public static NameValueNode getNameValueNode(UserObjectTreeNode node, String name)
  {
    // given a parent node, look for the child node with the given name
    //     must match exactly or return null
    for (Enumeration <UserObjectTreeNode> c = node.children(); c.hasMoreElements() ;)
    {
      UserObjectTreeNode uotn = (UserObjectTreeNode)c.nextElement();
      NameValueNode tst = (NameValueNode)uotn.getUserObject();
      if(name.equals(tst.getMemberName()))
        return tst;
     }
    return null;
  }
  
  public boolean updateModel(SubnetTreeModel model)
  {
    if(model == null)
      return false;
    
    // walk the model, and update its values
    updateNode(this.getRootNode(), model.getRootNode());
    this.reload(this.getRootNode());
    return true;
  }
  
  public boolean updateNode(UserObjectTreeNode origNode, UserObjectTreeNode newNode)
  {
    if((origNode == null) || (newNode == null))
      return false;
    
    // walk the children of this node
    if(origNode.getChildCount() != newNode.getChildCount())
    {
      System.err.println("new and old subnet nodes have different number of children");
//      origNode = newNode;
      return false;
    }
    
    // update this node, and then its children
    NameValueNode ovmn = (NameValueNode) origNode.getUserObject();
    NameValueNode nvmn = (NameValueNode) newNode.getUserObject();
    
    // change just the Object portion
    ovmn.setMemberValue(nvmn.getMemberObject());
    
    // do the children
    for(int index = 0; index < origNode.getChildCount(); index++)
    {
      UserObjectTreeNode origChild = (UserObjectTreeNode) origNode.getChildAt(index);
      UserObjectTreeNode newChild  = (UserObjectTreeNode) newNode.getChildAt(index);
      updateNode(origChild, newChild);
    }
    return true;
  }
  
  
  private boolean addSubnetManagers(UserObjectTreeNode vmtn)
  {
      OSM_Fabric      Fabric               = OSM.getFabric();      
      OSM_SysInfo SysInfo                  = Fabric.getOsmSysInfo();
      OSM_Subnet Subnet                    = Fabric.getOsmSubnet();
      HashMap<String, String> OptionsMap   = Fabric.getOptions();

      NameValueNode     n  = new NameValueNode("Subnet Managers", Subnet.Managers.length);
      UserObjectTreeNode tn = new UserObjectTreeNode(n, true);
      vmtn.add(tn);
      
      NameValueNode      m  = new NameValueNode("name", OSM.getFabricName(true));
      UserObjectTreeNode mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("port guid", new IB_Guid(Subnet.sm_port_guid).toColonString());
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("state", SysInfo.SM_State);
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);

      m  = new NameValueNode("sweep interval", Subnet.Options.sweep_interval);
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);

      m  = new NameValueNode("priority", SysInfo.SM_Priority);
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);

      m  = new NameValueNode("routing engine", SysInfo.RoutingEngine);
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);

      addPlugins(tn);
      addSubnetFlags(tn);
      
      String key = "sm_key";
      String val = OptionsMap.get(key);
      if(val != null)
      {
        IB_Guid vg = new IB_Guid(val);
        m  = new NameValueNode(key, vg.toColonString() + " (" + val + ")");
        mn = new UserObjectTreeNode(m, false);
        tn.add(mn);
      }
      
      key = "subnet_prefix";
      val = OptionsMap.get(key);
      if(val != null)
      {
        IB_Guid vg = new IB_Guid(val);
        m  = new NameValueNode(key, vg.toColonString() + " (" + val + ")");
        mn = new UserObjectTreeNode(m, false);
        tn.add(mn);
      }
      
      addMoreSubnet(tn);
      
     
      if(Subnet.Managers.length > 1)
      {
        m  = new NameValueNode("Other Managers", Subnet.Managers.length);
        mn = new UserObjectTreeNode(m, true);
        tn.add(mn);

        for(SBN_Manager mgr: Subnet.Managers)
        {
          NameValueNode     mm  = new NameValueNode("state", mgr.State);
          UserObjectTreeNode mmn = new UserObjectTreeNode(mm, false);
          mn.add(mmn);
          
          mm  = new NameValueNode("priority", mgr.pri_state);
          mmn = new UserObjectTreeNode(mm, false);
          mn.add(mmn);

          mm  = new NameValueNode("port guid", new IB_Guid(mgr.guid).toColonString());
          mmn = new UserObjectTreeNode(mm, false);
          mn.add(mmn);
         }
      }
      return true;
  }
  
  private boolean addMoreSubnet(UserObjectTreeNode vmtn)
  {
//    ObjectSession   ParentSessionStatus  = OSM.getParentSessionStatus();
//    OsmServerStatus ServerStatus         = OSM.getRemoteServerStatus();
    OSM_Fabric      Fabric               = OSM.getFabric();
    
//    OSM_SysInfo SysInfo                  = Fabric.getOsmSysInfo();
//    OSM_Stats Stats                      = Fabric.getOsmStats();
    OSM_Subnet Subnet                    = Fabric.getOsmSubnet();
//    HashMap<String, String> OptionsMap   = Fabric.getOptions();


      NameValueNode     n  = new NameValueNode("more", 1);
      UserObjectTreeNode tn = new UserObjectTreeNode(n, true);
      vmtn.add(tn);
      
      NameValueNode      m  = new NameValueNode("num switches", Subnet.Switches.length);
      UserObjectTreeNode mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("need update", Subnet.need_update);
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("max lid", IB_Address.toLidHexString(Subnet.max_ucast_lid_ho) + " (" + Subnet.max_ucast_lid_ho + ")");
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      n.setMemberValue(tn.getChildCount());
      return true;
  }
  
  private boolean addSubnetFlags(UserObjectTreeNode vmtn)
  {
//    ObjectSession   ParentSessionStatus  = OSM.getParentSessionStatus();
//    OsmServerStatus ServerStatus         = OSM.getRemoteServerStatus();
    OSM_Fabric      Fabric               = OSM.getFabric();
    
//    OSM_SysInfo SysInfo                  = Fabric.getOsmSysInfo();
//    OSM_Stats Stats                      = Fabric.getOsmStats();
    OSM_Subnet Subnet                    = Fabric.getOsmSubnet();
    HashMap<String, String> OptionsMap   = Fabric.getOptions();

      NameValueNode     n  = new NameValueNode("Subnet Flags", 1);
      UserObjectTreeNode tn = new UserObjectTreeNode(n, true);
      vmtn.add(tn);
      
      NameValueNode      m  = new NameValueNode("sweeping enabled", Boolean.toString(Subnet.sweeping_enabled));
      UserObjectTreeNode mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("sweep interval (seconds)", Subnet.Options.sweep_interval);
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);

      m  = new NameValueNode("ignore existing lfts", Boolean.toString(Subnet.ignore_existing_lfts));
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("subnet init errors", Boolean.toString(Subnet.subnet_initialization_error));
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("in sweep hop 0", Boolean.toString(Subnet.in_sweep_hop_0));
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("first time master sweep", Boolean.toString(Subnet.first_time_master_sweep));
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("coming out of standby", Boolean.toString(Subnet.coming_out_of_standby));
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      String key = "qos";
      String val = OptionsMap.get(key);
      
      if(val != null)
      {
        m  = new NameValueNode(key, val);
        mn = new UserObjectTreeNode(m, false);
        tn.add(mn);
      }
      
      n.setMemberValue(vmtn.getChildCount());
      return true;
  }
  
  private boolean addOptionsMap1(UserObjectTreeNode vmtn)
  {
//    ObjectSession   ParentSessionStatus  = OSM.getParentSessionStatus();
//    OsmServerStatus ServerStatus         = OSM.getRemoteServerStatus();
    OSM_Fabric      Fabric               = OSM.getFabric();
    
//    OSM_SysInfo SysInfo                  = Fabric.getOsmSysInfo();
//    OSM_Stats Stats                      = Fabric.getOsmStats();
//    OSM_Subnet Subnet                    = Fabric.getOsmSubnet();
    HashMap<String, String> OptionsMap   = Fabric.getOptions();

      NameValueNode     n  = new NameValueNode("Configuration", 1);
      UserObjectTreeNode tn = new UserObjectTreeNode(n, true);
      vmtn.add(tn);
      
      NameValueNode      m;
      UserObjectTreeNode mn;
      
       for (Map.Entry<String, String> entry: OptionsMap.entrySet())
      {
        m  = new NameValueNode(entry.getKey(), entry.getValue());
        mn = new UserObjectTreeNode(m, false);
        tn.add(mn);
      }
      n.setMemberValue(tn.getChildCount());
      return true;
  }
  
  private boolean addOptionsMap(UserObjectTreeNode vmtn)
  {
    OptionMapTreeModel omtm = new OptionMapTreeModel(OSM);
    
    vmtn.add(omtm.getRootNode());
      return true;
  }
  
  private boolean addPlugins(UserObjectTreeNode vmtn)
  {
//    ObjectSession   ParentSessionStatus  = OSM.getParentSessionStatus();
//    OsmServerStatus ServerStatus         = OSM.getRemoteServerStatus();
    OSM_Fabric      Fabric               = OSM.getFabric();
    
    OSM_SysInfo SysInfo                  = Fabric.getOsmSysInfo();
//    OSM_Stats Stats                      = Fabric.getOsmStats();
//    OSM_Subnet Subnet                    = Fabric.getOsmSubnet();
//    HashMap<String, String> OptionsMap   = Fabric.getOptions();

      // normally there is only a single plugin, but there could be more!
      
      // if single, just add and return
      // if multiple, create a parent node for plugins, add it, and then add
      // the plugins to it.
      UserObjectTreeNode pNode = vmtn;
      
      if(SysInfo.EventPlugins.length > 1)
      {
        // create a parent, and add to that
        NameValueNode     pi  = new NameValueNode("loaded event plugins", SysInfo.EventPlugins.length);
        UserObjectTreeNode pitn = new UserObjectTreeNode(pi, true);
        vmtn.add(pitn);
        pNode = pitn;
      }
 
      for(int i = 0; i < SysInfo.EventPlugins.length; i++)
      {
        NameValueNode     n  = new NameValueNode("plugin name ["+i+"]", SysInfo.EventPlugins[i]);
        //
        if(SysInfo.EventPlugins.length == 1)
          n  = new NameValueNode("plugin name", SysInfo.EventPlugins[i]);
        
        UserObjectTreeNode tn = new UserObjectTreeNode(n, true);
        pNode.add(tn);
      }
       return true;
  }
  
  private boolean addSubnetAdministrators(UserObjectTreeNode vmtn)
  {
    OSM_Fabric      Fabric               = OSM.getFabric();
    OSM_SysInfo SysInfo                  = Fabric.getOsmSysInfo();
    HashMap<String, String> OptionsMap   = Fabric.getOptions();

      NameValueNode     n  = new NameValueNode("Subnet Administrators", 1);
      UserObjectTreeNode tn = new UserObjectTreeNode(n, true);
      vmtn.add(tn);
      
      NameValueNode      m  = new NameValueNode("name", OSM.getFabricName(true));
      UserObjectTreeNode mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("state", SysInfo.SA_State);
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      String key = "sa_key";
      String val = OptionsMap.get(key);
      if(val != null)
      {
        IB_Guid vg = new IB_Guid(val);
        m  = new NameValueNode(key, vg.toColonString() + " (" + val + ")");
        mn = new UserObjectTreeNode(m, false);
        tn.add(mn);
      }
      
       return true;
  }
  
  
  private boolean addMadStatistics(UserObjectTreeNode vmtn)
  {
    OSM_Fabric      Fabric               = OSM.getFabric();
    OSM_Stats Stats                      = Fabric.getOsmStats();

      NameValueNode     n  = new NameValueNode("MAD Counters", 1);
      UserObjectTreeNode tn = new UserObjectTreeNode(n, true);
      vmtn.add(tn);
      
      NameValueNode      m  = new NameValueNode(MAD_Counter.qp0_mads_outstanding.getName(), Stats.qp0_mads_outstanding);
      UserObjectTreeNode mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode(MAD_Counter.qp0_mads_outstanding_on_wire.getName(), Stats.qp0_mads_outstanding_on_wire);
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode(MAD_Counter.qp0_mads_rcvd.getName(), Stats.qp0_mads_rcvd);
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode(MAD_Counter.qp0_mads_sent.getName(), Stats.qp0_mads_sent);
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode(MAD_Counter.qp0_unicasts_sent.getName(), Stats.qp0_unicasts_sent);
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode(MAD_Counter.qp0_mads_rcvd_unknown.getName(), Stats.qp0_mads_rcvd_unknown);
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode(MAD_Counter.sa_mads_outstanding.getName(), Stats.sa_mads_outstanding);
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode(MAD_Counter.sa_mads_rcvd.getName(), Stats.sa_mads_rcvd);
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode(MAD_Counter.sa_mads_sent.getName(), Stats.sa_mads_sent);
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode(MAD_Counter.sa_mads_rcvd_unknown.getName(), Stats.sa_mads_rcvd_unknown);
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode(MAD_Counter.sa_mads_ignored.getName(), Stats.sa_mads_ignored);
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      
      n.setMemberValue(tn.getChildCount());
      return true;
  }
  
  private boolean addEventCounters(UserObjectTreeNode vmtn)
  {
      OSM_Fabric Fabric = OSM.getFabric();
      
      OSM_EventStats EventStats  = Fabric.getOsmEventStats();

      NameValueNode     n  = new NameValueNode("Event Counters", 1);
      UserObjectTreeNode tn = new UserObjectTreeNode(n, true);
      vmtn.add(tn);
      
      for(OsmEvent s : OsmEvent.OSM_STAT_EVENTS)
      {
        NameValueNode      m  = new NameValueNode(s.getEventName(), EventStats.getCounter(s));
        UserObjectTreeNode mn = new UserObjectTreeNode(m, false);
        tn.add(mn);
       }
      
      n.setMemberValue(tn.getChildCount());
      return true;
  }
  
  private boolean addPerformanceManagers(UserObjectTreeNode vmtn)
  {
      OSM_Fabric      Fabric               = null;
      
      OSM_SysInfo SysInfo = null;
       HashMap<String, String> OptionsMap = null;

      Fabric = OSM.getFabric();
      
      SysInfo     = Fabric.getOsmSysInfo();
      OptionsMap  = Fabric.getOptions();


      NameValueNode     n  = new NameValueNode("Performance Managers", 1);
      UserObjectTreeNode tn = new UserObjectTreeNode(n, true);
      vmtn.add(tn);
      
      NameValueNode      m  = new NameValueNode("name", OSM.getFabricName(true));
      UserObjectTreeNode mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("state", SysInfo.PM_State);
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);

      m  = new NameValueNode("sweep state", SysInfo.PM_SweepState);
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);

      String key = "perfmgr_sweep_time_s";
      String val = Integer.toString(SysInfo.PM_SweepTime);
//      String val = OptionsMap.get(key);
      if(val == null)
         val = "180";      
      m  = new NameValueNode("sweep period", val);
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);

      boolean stale = (Fabric == null) ? true : Fabric.isStale();
      String staleString = stale ? "stale": "current";
      
      m  = new NameValueNode("sweep data", staleString);
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);

      key = "perfmgr_max_outstanding_queries";
      val = Integer.toString(SysInfo.PM_OutstandingQueries);
//      val = OptionsMap.get(key);
      if(val != null)
      {
        m  = new NameValueNode("max outstanding queries", val);
        mn = new UserObjectTreeNode(m, false);
        tn.add(mn);
      }
      
      key = "perfmgr_redir";
      val = OptionsMap.get(key);
      if(val != null)
      {
        m  = new NameValueNode("redirection", val);
        mn = new UserObjectTreeNode(m, false);
        tn.add(mn);
      }
        return true;
  }
  
  private boolean addOpenSmMonitorSerivce(UserObjectTreeNode vmtn)
  {
//      OMS_Updater     ServiceUpdater       = null;  // this screen may be updated by an OMS service
//      ObjectSession   ParentSessionStatus  = null;
//      OsmServerStatus ServerStatus         = null;
//      OSM_Fabric      Fabric               = null;
//      
//      OSM_SysInfo SysInfo = null;
//      OSM_Stats Stats     = null;
//      OSM_Subnet Subnet   = null;
//      HashMap<String, String> OptionsMap = null;
//
//      ParentSessionStatus = OSM.getParentSessionStatus();
//      ServerStatus = OSM.getRemoteServerStatus();
      OSM_Fabric Fabric = OSM.getFabric();
      
      OSM_SysInfo SysInfo     = Fabric.getOsmSysInfo();
//      Stats       = Fabric.getOsmStats();
//      Subnet      = Fabric.getOsmSubnet();
//      OptionsMap  = Fabric.getOptions();


      NameValueNode     n  = new NameValueNode("OpenSM Monitoring Service", 1);
      UserObjectTreeNode tn = new UserObjectTreeNode(n, true);
      vmtn.add(tn);
      
      NameValueNode      m  = new NameValueNode("name", OSM.getFabricName(true));
      UserObjectTreeNode mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("state", SysInfo.PM_State);
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);

      addNativePlugin(tn);
      addServer(tn);
      addClient(tn);
      return true;
  }
  
  private boolean addNativePlugin(UserObjectTreeNode vmtn)
  {
      OsmServerStatus ServerStatus = OSM.getRemoteServerStatus();
      OSM_Fabric Fabric            = OSM.getFabric();
      OSM_SysInfo SysInfo          = Fabric.getOsmSysInfo();

      NameValueNode     n  = new NameValueNode("Java Native Plugin", 1);
      UserObjectTreeNode tn = new UserObjectTreeNode(n, true);
      vmtn.add(tn);
      
      NameValueNode      m  = new NameValueNode("name", "OsmJniPi");
      UserObjectTreeNode mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      // strip out the name and version and build date
      String nm = "OSM_JNI_Plugin ";
      int sndx = SysInfo.OsmJpi_Version.indexOf("(");
      int endx = SysInfo.OsmJpi_Version.lastIndexOf(")");
      String ver = SysInfo.OsmJpi_Version.substring(nm.length(), sndx);
      String da = SysInfo.OsmJpi_Version.substring(sndx + 1, endx-1);
      
      m  = new NameValueNode("version", ver);
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("build date", da);
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("OpenSM version", SysInfo.OpenSM_Version.substring("OpenSM ".length()));
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("refresh period (seconds)", Integer.toString(ServerStatus.NativeUpdatePeriodSecs));
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("report period (seconds)", Integer.toString(ServerStatus.NativeReportPeriodSecs));
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("refresh count", Long.toString(ServerStatus.NativeHeartbeatCount));
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("event timeout (milliseconds)", Integer.toString(ServerStatus.NativeEventTimeoutMsecs));
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("event count", Long.toString(ServerStatus.NativeEventCount));
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      n.setMemberValue(tn.getChildCount());
      return true;
  }
  
  private boolean addServer(UserObjectTreeNode vmtn)
  {
      ObjectSession ParentSessionStatus = OSM.getParentSessionStatus();
      OsmServerStatus ServerStatus = OSM.getRemoteServerStatus();

      // currently, this is the only way to get version and build numbers
      String version   = ServerStatus.Version;
      String buildDate = ServerStatus.BuildDate;

      NameValueNode     n  = new NameValueNode("Server", 1);
      UserObjectTreeNode tn = new UserObjectTreeNode(n, true);
      vmtn.add(tn);
      
      NameValueNode      m  = new NameValueNode("name", "OpenSM Monitoring Service");
      UserObjectTreeNode mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("class name", ServerStatus.Server.getServerName());
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("OMS version", version);
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("OMS build date", buildDate);
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("up since", ServerStatus.Server.getStartTime().toString());
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("host", ServerStatus.Server.getHost());
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("port", Integer.toString(ServerStatus.Server.getPortNum()));
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("thread ID", Long.toString(ServerStatus.Server.getThreadId()));
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("Authenticator class", ParentSessionStatus.getAuthenticator());
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("localhost allowed?", Boolean.toString(ServerStatus.AllowLocalHost));
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("Protocol class", ParentSessionStatus.getProtocol());
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("active clients", Integer.toString(ServerStatus.Server.getCurrent_Sessions().size()));
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("cumulative clients", Integer.toString(ServerStatus.Server.getHistorical_Sessions().size()));
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("max parent sessions", Integer.toString(ServerStatus.MaxParentSessions));
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("max child sessions (per parent)", Integer.toString(ServerStatus.MaxChildSessions));
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("refresh period (seconds)", Integer.toString(ServerStatus.ServerUpdatePeriodSecs));
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("refresh count", Long.toString(ServerStatus.ServerHeartbeatCount));
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      n.setMemberValue(tn.getChildCount());
      return true;
  }
  
  private boolean addClient(UserObjectTreeNode vmtn)
  {
    ObjectSession   ParentSessionStatus  = OSM.getParentSessionStatus();
    
      // currently, this is the only way to get version and build numbers
      String version   = "version";
      String buildDate = "buildDate";
      ArrayList<SmtAboutRecord> records = SmtAbout.getRecordsFromManifest(this);
      SmtAboutRecord record = SmtAbout.getAboutRecord(records, "SubnetMonitorTool");
      if(record != null)
      {
        version = record.getVersion();
        buildDate = record.getDate();
      }

      NameValueNode     n  = new NameValueNode("Client", 1);
      UserObjectTreeNode tn = new UserObjectTreeNode(n, true);
      vmtn.add(tn);
      
      NameValueNode      m  = new NameValueNode("name", "smt-gui");
      UserObjectTreeNode mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("class name", SmtCommandType.SMT_GUI_APP.getCommandName());
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("version", version);
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("build date", buildDate);
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("up since", SMT_UpdateService.getInstance().getUpTime().toString());
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      addClientConnection(tn);
      
      // the client side of the OMS
      record = SmtAbout.getAboutRecord(records, "OsmClientServer");
      if(record != null)
      {
        version = record.getVersion();
        buildDate = record.getDate();
        
        m  = new NameValueNode("OMS version", version);
        mn = new UserObjectTreeNode(m, false);
        tn.add(mn);
        
        m  = new NameValueNode("OMS build date", buildDate);
        mn = new UserObjectTreeNode(m, false);
        tn.add(mn);
      }
      
      m  = new NameValueNode("Authenticator class", ParentSessionStatus.getAuthenticator());
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("Protocol class", ParentSessionStatus.getClientProtocol());
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("user name", SmtGuiPreferences.getUserName());
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
            
      n.setMemberValue(tn.getChildCount());
      return true;
  }
  
  private boolean addClientConnection(UserObjectTreeNode vmtn)
  {
    OMS_Updater     ServiceUpdater       = SMT_UpdateService.getInstance();
    ObjectSession   ParentSessionStatus  = OSM.getParentSessionStatus();

      NameValueNode     n  = new NameValueNode("Connection", 1);
      UserObjectTreeNode tn = new UserObjectTreeNode(n, true);
      vmtn.add(tn);
      
      NameValueNode      m  = new NameValueNode("remote host", ParentSessionStatus.getHost());
      UserObjectTreeNode mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("port", Integer.toString(ParentSessionStatus.getPort()));
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("name", ParentSessionStatus.getSessionName());
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("thread ID", Long.toString(ParentSessionStatus.getThreadId()));
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("user ID", ParentSessionStatus.getUser());
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("connected since", ParentSessionStatus.getOpenTime().toString());
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      m  = new NameValueNode("connection reused?", Boolean.toString(ServiceUpdater.isConnectionReused()));
      mn = new UserObjectTreeNode(m, false);
      tn.add(mn);
      
      n.setMemberValue(tn.getChildCount());
      return true;
  }
  
  


  @Override
  public Object getChild(Object parent, int index)
  {
    Object [] ca = null;
    if (index >= 0)
    {
      ca = getChildSet(parent).toArray();
      if (index < ca.length)
      {
        return ca[index];
      }
    }
    System.err.println("Array out of bounds: num children(" + ca.length + ") and index is (" + index+ ")");

    return null;
  }


  @Override
  public void valueForPathChanged(TreePath path, Object newValue)
  {
    // TODO Auto-generated method stub

  }

  @Override
  public int getIndexOfChild(Object parent, Object child)
  {
    Object [] ca = getChildSet(parent).toArray();
    
    // iterate until found
    for(int index = 0; index < ca.length; index++)
    {
       if(child.equals(ca[index]))
         return index;
    }
    System.err.println("Match Not Found: node is not a child of parent");
    return -1;
  }
  
  public static boolean isMAD_Counter(UserObjectTreeNode node)
  {
    return (getMAD_Counter(node) != null) ? true: false;
  }

  public static boolean isEvent_Counter(UserObjectTreeNode node)
  {
    return (getEvent_Counter(node) != null) ? true: false;
  }

  public static OsmEvent getEvent_Counter(UserObjectTreeNode node)
  {
    if (node != null)
    {
      NameValueNode vmn = (NameValueNode) node.getUserObject();
      String eName = vmn.getMemberName();
      
      // find the event, or return null
      for(OsmEvent s : OsmEvent.OSM_STAT_EVENTS)
      {
        if(s.getEventName().equals(eName))
          return s;
       }
    }
    return null;
  }

  public static MAD_Counter getMAD_Counter(UserObjectTreeNode node)
  {
    if (node != null)
    {
      NameValueNode vmn = (NameValueNode) node.getUserObject();
      MAD_Counter mcn = MAD_Counter.getByName(vmn.getMemberName());
      if ((mcn != null) && MAD_Counter.MAD_ALL_COUNTERS.contains(mcn))
        return mcn;
    }
    return null;
  }

  @Override
  public void addTreeModelListener(TreeModelListener l)
  {
    // TODO Auto-generated method stub

  }

  @Override
  public void removeTreeModelListener(TreeModelListener l)
  {
    // TODO Auto-generated method stub

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
    // TODO Auto-generated method stub

  }

  @Override
  public boolean isLeaf(Object node)
  {
    return getChildCount(node) < 1 ? true: false;
  }

  @Override
  public int getChildCount(Object parent)
  {
    return getChildSet(parent).size();
  }
  
  private Set <UserObjectTreeNode> getChildSet(Object parentNode)
  {
    UserObjectTreeNode parent = null;
    
    if(parentNode instanceof UserObjectTreeNode)
    {
      parent = (UserObjectTreeNode) parentNode;
    }
     
    if(parent == null)
      return null;

       Set <UserObjectTreeNode> childSet = new HashSet <UserObjectTreeNode> ();
      
      for (Enumeration <UserObjectTreeNode> c = parent.children(); c.hasMoreElements() ;)
      {
        childSet.add(c.nextElement());
      }
      System.err.println("NumChildren: " + childSet.size());    
      return childSet;
  }

}
