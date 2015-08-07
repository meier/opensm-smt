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
 *        file: VertexActivityTreeTablePanel.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/

package gov.llnl.lc.infiniband.opensm.plugin.gui.tree;

import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_ActivityType;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_FabricDelta;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_NodeActivity;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_PortChange;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;

/**
  *
  */
public class VertexActivityTreeTablePanel extends JPanel
{

  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = -6394575017697046499L;
  
  private final String[]        headings = { "timestamp", "attribute 1", "value 1", "attribute 2", "value 2", "other"};
  private VertexNode                  root;
  private DefaultTreeTableModel model;
  private JXTreeTable           table;
  
  
  String[]        dHeadings;
  
  OSM_FabricDelta fabricDelta;
  
  String fabricName = "fabricName";
  String timeStamp  = "timeStampDynamic";
  LinkedHashMap <String, IB_Vertex> vertexMap;
  int numActiveSwitches;
  
  LinkedHashMap<String, PFM_PortChange> activePorts;
  
  LinkedHashMap <String, IB_Vertex> sortedVertexMap;
 
  
  public String getTimeStamp()
  {
    return timeStamp;
  }
  

  public String[] getHeadings()
  {
    // copy headings over, except replace a few things
    List <String> sl = Arrays.asList(headings);
    dHeadings = new String[sl.size()];
    int ndex = 0;
    
    for(String s: sl)
    {
      if(ndex == 0)
        dHeadings[ndex] = getTimeStamp();
      else
        dHeadings[ndex] = s;
      
      ndex++;
    }
    
    return dHeadings;
  }


  public void setTimeStamp(String timeStamp)
  {
    this.timeStamp = timeStamp;
  }


  public String getFabricName()
  {
    return fabricName;
  }


  public VertexActivityTreeTablePanel()
  {
    super(new BorderLayout());
    table = getTreeTable();
    JScrollPane sp = new JScrollPane(table);
    add(sp);
  }


  // ============================================================= treetable

  public JXTreeTable getTreeTable()
  {
    loadData("localhost", "10011");
    Object[] data = new Object[] { getFabricName(), "# Switches", getNumSwitches(), "# Active Switches", getNumActiveSwitches() };

    root = new RootNode(data);
  
    numActiveSwitches = addSwitches();  // Hierarchical, adds the links and ports
    System.err.println("Numb active switches = " + getNumActiveSwitches());
    root.setValueAt(4, getNumActiveSwitches());
    model = new DefaultTreeTableModel(root, Arrays.asList(getHeadings()))
    {
      
      @Override
      public Class getColumnClass(int column)
      {
          return String.class;
      }
    };
    
    JXTreeTable table = new JXTreeTable(model);
    table.setShowGrid(true, true);
    table.setRootVisible(true);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setColumnControlVisible(true);
    table.setHorizontalScrollEnabled(true);
    table.setFillsViewportHeight(false);

    table.expandRow(0);
    table.packAll();
    return table;
  }

  private String getNumActiveSwitches()
  {
    return Integer.toString(numActiveSwitches);
  }


  private String getNumSwitches()
  {
    return Integer.toString(vertexMap.size());
  }

  private int addSwitches()
  {
    int num = 0;
    for (Entry<String, IB_Vertex> entry : sortedVertexMap.entrySet())
    {
      IB_Vertex v = entry.getValue();

      LinkedHashMap<String, PFM_PortChange> pcMap = fabricDelta.getPortChangesFromNode(v.getNode());
      if (pcMap.size() > 0)
      {
        OSM_NodeActivity ne = new OSM_NodeActivity(v, pcMap);
        String depth = ne.getNodeInfo(OSM_ActivityType.OAT_NODE_LEVEL.name());

        if (ne.isActive())
        {
          num++;
          setTimeStamp(ne.getNodeInfo("timestamp"));

          // name, guid name, guid value, num links, num link value
          Object[] data1 = new Object[] {
              ne.getNodeInfo(OSM_ActivityType.OAT_NAME.name()),
              ne.getNodeInfo(OSM_ActivityType.OAT_GUID.name()),
              "active ports/total ports = " + ne.getNodeInfo(OSM_ActivityType.OAT_NUM_ACTIVE_PORTS.name()) + "/"
                  + ne.getNodeInfo(OSM_ActivityType.OAT_NUM_PORTS.name()),
              "active links/total links = " + ne.getNodeInfo(OSM_ActivityType.OAT_NUM_ACTIVE_LINKS.name()) + "/"
                  + ne.getNodeInfo(OSM_ActivityType.OAT_NUM_LINKS.name()),
                  depth
                                   
          };

          SwitchNode child = new SwitchNode(data1);
          root.add(child);
          addLinks(child, ne);
        }
      }

    }
    return num;
  }

  private void addLinks(SwitchNode parent, OSM_NodeActivity ne)
  {
    ArrayList<LinkedHashMap<String, String>> al = ne.getLinkInfo();
    for(LinkedHashMap<String, String> link: al)
    {
      // this should be sorted, so just add it
      String pNum = link.get(OSM_ActivityType.OAT_LINK_PORT_NUM.name());
      String depth = link.get(OSM_ActivityType.OAT_LINK_LEVEL.name());
      
    Object[] data = new Object[] { "link " + pNum,
        "port # " + pNum + " [" + depth + "]",
 //       link.get("lid"),
        "->",
        link.get(OSM_ActivityType.OAT_LINK_INFO.name()),
        "->",
        link.get(OSM_ActivityType.OAT_LINK_REMOTE_INFO.name()) };
    
LinkNode child = new LinkNode(data);
parent.add(child);
addCounters(child, link);
  }
  }

    private void addCounters(LinkNode parent, LinkedHashMap<String, String> link)
    {
          Object[] data = new Object[] { "port counters",
              link.get(OSM_ActivityType.OAT_ERROR_INFO.name()),
              link.get(OSM_ActivityType.OAT_ERROR_DELTA_INFO.name()),
               link.get(OSM_ActivityType.OAT_TRAFFIC_INFO.name()),
          link.get(OSM_ActivityType.OAT_TRAFFIC_DELTA_INFO.name())};
          CounterNode child = new CounterNode(data);
          parent.add(child);
  }
    
  
  private void loadData(String host, String port)
  {
    // create arrays of strings to populate the table
    
    // switch data
    // link data
    // counter data
    
    /* I need the fabric name and timestamp*/
    fabricName = "unknown";
    timeStamp  = "unknown";
    
    // get a FabricDelta, on or off line
    fabricDelta = OSM_FabricDelta.getOSM_FabricDelta(host,  port);
    OSM_Fabric fabric2 = fabricDelta.getFabric2();
    
    this.fabricName = fabricDelta.getFabricName();
    
    // create a vertexMap from the most recent fabric
    sortedVertexMap = IB_Vertex.createVertexMap(fabric2);
    vertexMap = sortedVertexMap;
    
    // get a map of only the active ports (with change)
    activePorts = fabricDelta.getPortsWithChange();
    System.err.println("There are " + activePorts.size() + " ports that are changing");
    
  }

  // ========================================================== node classes

  private abstract class VertexNode extends AbstractMutableTreeTableNode
  {

    public VertexNode(Object[] data)
    {
      super(data);
      if (data == null)
      {
        throw new IllegalArgumentException("Node data cannot be null");
      }
    }

//    public abstract String getIdentity();

    /*
     * Inherited
     */
    @Override
    public int getColumnCount()
    {
      return getData().length;
    }

    /*
     * Inherited
     */
    @Override
    public Object getValueAt(int column)
    {
      return getData()[column];
    }

    public Object setValueAt(int column, Object userObject)
    {
      Object[] oArray = (Object[]) getUserObject();
      oArray[column] = userObject;
     return userObject;
    }

    public Object[] getData()
    {
      return (Object[]) getUserObject();
    }

  }

  private class RootNode extends VertexNode
  {

    public RootNode(Object[] data)
    {
      super(data);
    }

   public RootNode(String key)
    {
      super(new Object[] { key });
    }

    public String getIdentity()
    {
      return "";
    }
  }

  private class SwitchNode extends VertexNode
  {

    public SwitchNode(Object[] data)
    {
      super(data);
    }

//    public String getIdentity()
//    {
//      // this should be the guid, or name, or both
//      return (String) getData()[0];
//    }
//
//    public boolean isIdentity(String id)
//    {
//      return getIdentity().equals(id);
//    }
//
  }

  private class LinkNode extends VertexNode
  {

    public LinkNode(Object[] data)
    {
      super(data);
    }

//    public String getIdentity()
//    {
//      return ((VertexNode) getParent()).getIdentity();
//    }
//
//    public String getLink()
//    {
//      return getIdentity() + (String) getData()[1];
//    }
//
//    public boolean isLink(String link)
//    {
//      return getLink().equals(link);
//    }


  }

  private class CounterNode extends VertexNode
  {

    public CounterNode(Object[] data)
    {
      super(data);
    }

//    public String getIdentity()
//    {
//      return ((VertexNode) getParent()).getIdentity();
//    }


  }

  // =================================================================== main

  /**
   * @param args
   */
  public static void main(String[] args)
  {
//    try
//    {
//      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//    }
//    catch (Exception e)
//    {
//      e.printStackTrace();
//    }
    // get the data model
    
    VertexActivityTreeTablePanel vttp = new VertexActivityTreeTablePanel();
//    vttp.loadData("localhost", "10011");
    
    JFrame f = new JFrame("VertexTreeTable Panel");
    f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    f.setMinimumSize(new Dimension(700, 400));
    f.setLocationRelativeTo(null);
    f.setContentPane(vttp);
    f.pack();
    f.setVisible(true);

  }

}
