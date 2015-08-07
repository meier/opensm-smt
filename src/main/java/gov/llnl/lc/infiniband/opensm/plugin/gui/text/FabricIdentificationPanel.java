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
 *        file: FabricIdentificationPanel.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.text;

import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.command.fabric.SmtFabricStructure;
import gov.llnl.lc.smt.command.fabric.SmtFabricStructure.SmtAttributeStructure;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;

public class FabricIdentificationPanel extends JPanel implements CommonLogger
{
  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = 2432745409866252013L;
  
  private final String[]           headings   = { "timestamp", "", "", "SW", "CA", "TOT" };
  private VertexNode               rootNode;
  private DefaultTreeTableModel    TTmodel;
  protected JXTreeTable              TTable;
  private String                   HostName   = "localhost";
  private String                   PortNum    = "10013";

  private String[]                         dHeadings;

  private OpenSmMonitorService             OMS        = null;
  private SmtFabricStructure               FabStruct  = null;

  public String getTimeStamp()
  {
    return OMS.getTimeStamp().toString();
  }

  private String[] getHeadings()
  {
    // copy headings over, except replace a few things
    List<String> sl = Arrays.asList(headings);
    dHeadings = new String[sl.size()];
    int ndex = 0;

    for (String s : sl)
    {
      if (ndex == 0)
        dHeadings[ndex] = getTimeStamp();
      else
        dHeadings[ndex] = s;

      ndex++;
    }

    return dHeadings;
  }

  public String getFabricName()
  {
    return OMS.getFabricName();
  }

  public void expandAll()
  {
    TTable.expandAll();
  }

  public void collapseAll()
  {
    TTable.collapseAll();
  }

  public FabricIdentificationPanel()
  {
    super(new BorderLayout());
  }

  public void init(String hostName, String portNum) throws IOException
  {
    JXTreeTable ttable = getTreeTable(hostName, portNum);
    JScrollPane sp = new JScrollPane(ttable);

    if (getComponentCount() > 0)
      removeAll();

    add(sp);
  }

  public JXTreeTable getTreeTable() throws IOException
  {
    // get a fresh copy
    return getTreeTable(HostName, PortNum);
  }

  private JXTreeTable getTreeTable(String host, String port) throws IOException
  {
    loadData(host, port);

    Object[] data = new Object[] { getFabricName(), "service port: " + PortNum,
        "up time: " + OMS.getRemoteServerStatus().Server.getStartTime().toString() };
    rootNode = new RootNode(data);

    addNodes(rootNode);
    addPorts(rootNode);
    addLinks(rootNode);

    TTmodel = new DefaultTreeTableModel(rootNode, Arrays.asList(getHeadings()))
    {
      @Override
      public Class getColumnClass(int column)
      {
        return String.class;
      }
    };

    JXTreeTable TTable = new JXTreeTable(TTmodel);
    TTable.setShowGrid(true, true);
    TTable.setRootVisible(true);
    TTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    TTable.setColumnControlVisible(true);
    TTable.setHorizontalScrollEnabled(true);
    TTable.setPreferredScrollableViewportSize(TTable.getPreferredSize());
    TTable.setFillsViewportHeight(false);
    
    TTable.addMouseListener(new MouseAdapter()
    {
      public void mousePressed(MouseEvent e)
      {
//        System.err.println("The Moused was pressed on this JXTreeTable");
      }
    });

    // the first row is the root, it needs to be expanded to show the other 3 rows
    TTable.expandRow(0);
    // start from the bottom and working up because "expand" will change the row numbering
    TTable.expandRow(3);  // Links
    TTable.expandRow(2);  // Ports
    TTable.expandRow(1);  // Nodes - currently does not have anything to expand
    TTable.packAll();
    return TTable;
  }

  private void addNodes(VertexNode parent)
  {
    // the order will be name, blank, blank, sw, ca, total
    AttributeNode child = addAttribute(parent, FabStruct.Nodes);
    parent.add(child);
  }

  private void addPorts(VertexNode parent)
  {
    // the order will be name, blank, blank, sw, ca, total
    AttributeNode child = addAttribute(parent, FabStruct.Ports);
    parent.add(child);
    addNamedAttributeList(child, "State", FabStruct.PortState);
    addNamedAttributeList(child, "Width", FabStruct.PortWidths);
    addNamedAttributeList(child, "Speed", FabStruct.PortSpeeds);
  }

  private void addNamedAttributeList(VertexNode parent, String name,
      ArrayList<SmtAttributeStructure> attrList)
  {
    // the order will be name, blank, blank, sw, ca, total
    Object[] data = new Object[] { name, " ", " ", " ", " ", " " };
    AttributeNode thisParent = new AttributeNode(data);
    parent.add(thisParent);

    // now check to see the various attributes that exist
    if ((attrList == null) || (attrList.size() < 1))
      return;
    
    SmtAttributeStructure maxAttr = null;

    for (SmtAttributeStructure attr : attrList)
    {
      if(maxAttr == null)
        maxAttr = attr;
      else
      {
        if(attr.compareTo(maxAttr) > 0)
          maxAttr = attr;
      }
      addAttribute(thisParent, attr);
    }
    
    // insert the maximum into the list node parent
    thisParent.setValueAt(2, maxAttr.Name);
    thisParent.setValueAt(5, Integer.toString(maxAttr.NumTotal));
   
  }

  private void addLinks(VertexNode parent)
  {
    // the order will be name, blank, blank, sw, ca, total
    AttributeNode child = addAttribute(parent, FabStruct.Links);
    parent.add(child);
    addNamedAttributeList(child, "Width", FabStruct.LinkWidths);
    addNamedAttributeList(child, "Speed", FabStruct.LinkSpeeds);
    addNamedAttributeList(child, "Rate", FabStruct.LinkRates);
  }

  private AttributeNode addAttribute(VertexNode parent, SmtAttributeStructure attr)
  {
    // the order will be name, blank, blank, sw, ca, total
    String name = attr.Name;
    int sw = attr.NumSwitches;
    int ca = attr.NumChannelAdapters;
    int tot = attr.NumTotal;

    Object[] data = new Object[] { name, " ", " ", sw, ca, tot };
    AttributeNode child = new AttributeNode(data);
    parent.add(child);
    return child;
  }

  private void loadData(String host, String port) throws IOException
  {
    // create arrays of strings to populate the table

    // switch data
    // link data
    // counter data

     // get a FabricDelta, on or off line
    OMS = null;
    try
    {
      OMS = OpenSmMonitorService.getOpenSmMonitorService(host, port);
      HostName = host;
      PortNum = port;
      FabStruct = new SmtFabricStructure(OMS);
    }
    catch (IOException e)
    {
      logger.severe("Couldn't get OMS for determining the fabric strucutre");
       throw e;
    }
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

  }

  private class AttributeNode extends VertexNode
  {

    public AttributeNode(Object[] data)
    {
      super(data);
    }
  }

  /**
   * @param args
   * @throws InterruptedException
   */
  public static void main(String[] args) throws Exception
  {
    FabricIdentificationPanel vttp = new FabricIdentificationPanel();

    JFrame f = new JFrame("FabricDiscoveryPanel Panel");
    f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    f.setMinimumSize(new Dimension(700, 400));
    f.setLocationRelativeTo(null);
    f.setContentPane(vttp);
    f.pack();
    f.setVisible(true);

    TimeUnit.SECONDS.sleep(3);
    vttp.init("localhost", "10011");
    f.setVisible(true);

    TimeUnit.SECONDS.sleep(200);
    vttp.init("localhost", "10011");
    f.setVisible(true);
    System.err.println("doo'd it");
  }

}
