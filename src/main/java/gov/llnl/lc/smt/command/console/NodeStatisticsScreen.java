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
 *        file: NodeStatisticsScreen.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command.console;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_UpdateProvider;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_UpdateProvider.UpdaterType;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Updater;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_NodeType;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Nodes;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_Node;
import gov.llnl.lc.infiniband.opensm.plugin.data.SBN_Node;
import gov.llnl.lc.smt.props.SmtProperty;
import gov.llnl.lc.util.BinList;
import jcurses.system.InputChar;
import jcurses.system.Toolkit;


/**********************************************************************
 * Describe purpose and responsibility of SubnetStatusScreen
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Mar 21, 2013 10:31:41 AM
 **********************************************************************/
public class NodeStatisticsScreen extends SmtConsoleScreen
{
  private int[] NodeColumns = null;  // gets initialized by paintBackground (caution - generally happens 2nd!)

  public boolean paintForeground() throws Exception
  {
    paintBorderForeground();
    
    // data from the service (may need to pad these)
    int column = 40;
    int row    = 20;
    OSM_Nodes AllNodes  = (Fabric == null) ? null: Fabric.getOsmNodes();

    BinList <SBN_Node> UniqueNodes = new BinList <SBN_Node>();
    BinList <PFM_Node> pNodes = new BinList <PFM_Node>();
    ArrayList<PFM_Node> pmna = new ArrayList<PFM_Node>();
    ArrayList<SBN_Node> sbna = new ArrayList<SBN_Node>();
    int totalSW = 0;
    int totalSWp = 0;
    int totalCA = 0;
    int totalCAp = 0;
    int totalNodes = 0;
    int totalPorts = 0;

    if(AllNodes != null)
    {
      PFM_Node[] pna = AllNodes.getPerfMgrNodes();
      SBN_Node[] sna = AllNodes.getSubnNodes();
      
      // the perfmgr may not have returned data due to start-up delay, check
      if((pna != null) && (sna != null))
      {
      pmna = new ArrayList<PFM_Node>(Arrays.asList(pna));
      sbna = new ArrayList<SBN_Node>(Arrays.asList(sna));
    
      // build up bins of unique nodes based on these criteria
      String key = null;
      for(SBN_Node sn: sbna)
      {
        key = Short.toString(sn.node_type) + "-" + Short.toString(sn.num_ports) + "-" +Integer.toString(sn.device_id) + Integer.toString(sn.partition_cap) + Integer.toString(sn.port_num_vendor_id) + Integer.toString(sn.revision) + Short.toString(sn.base_version) + Short.toString(sn.class_version);
        UniqueNodes.add(sn, key);
      }
      
      for(PFM_Node pn: pmna)
      {
        key = Short.toString(pn.getNum_ports()) + Boolean.toString(pn.isEsp0());
        pNodes.add(pn, key);
      }
      
      if(NodeColumns != null)
      {
      for(ArrayList<SBN_Node> sn: UniqueNodes)
      {
        // each node in the bin looks identical, so just use the first one
        SBN_Node s = sn.get(0);
        // each column should match the attribute from the background screen
        if(row < (ScreenRows - 3))
        {
        Toolkit.printString(Integer.toString(sn.size()),               NodeColumns[0], row, FrgndTxtColor);
        Toolkit.printString(OSM_NodeType.get(s).getAbrevName(),        NodeColumns[1], row, FrgndTxtColor);
        Toolkit.printString(Short.toString(s.num_ports),               NodeColumns[2], row, FrgndTxtColor);
        Toolkit.printString(Integer.toHexString(s.device_id),          NodeColumns[3], row, FrgndTxtColor);
        Toolkit.printString(Integer.toHexString(s.port_num_vendor_id), NodeColumns[4], row, FrgndTxtColor);
        Toolkit.printString(Integer.toHexString(s.revision),           NodeColumns[5], row, FrgndTxtColor);
        Toolkit.printString(Short.toString(s.base_version),            NodeColumns[6], row, FrgndTxtColor);
        Toolkit.printString(Short.toString(s.class_version),           NodeColumns[7], row, FrgndTxtColor);
        Toolkit.printString(Integer.toHexString(s.partition_cap),      NodeColumns[8], row, FrgndTxtColor);
        }
        /* calculate totals here */
        if(s.node_type == 1)
        {
          // CA totals
          totalCA  += sn.size();
          totalCAp += (s.num_ports * sn.size());
        }
        if(s.node_type == 2)
        {
          // SW totals
          totalSW  += sn.size();
          totalSWp += (s.num_ports * sn.size());
        }
        row++;
      }
      
      totalNodes = totalCA  + totalSW;
      totalPorts = totalCAp + totalSWp;
      
      // overall totals
      row = 7;
      Toolkit.printString(Integer.toString(totalCA),               NodeColumns[0], row, FrgndTxtColor);
      Toolkit.printString(OSM_NodeType.CA_NODE.getAbrevName(),     NodeColumns[1], row, FrgndTxtColor);
      Toolkit.printString(Integer.toString(totalCAp),              NodeColumns[2], row, FrgndTxtColor);
      row++;
      Toolkit.printString(Integer.toString(totalSW),               NodeColumns[0], row, FrgndTxtColor);
      Toolkit.printString(OSM_NodeType.SW_NODE.getAbrevName(),     NodeColumns[1], row, FrgndTxtColor);
      Toolkit.printString(Integer.toString(totalSWp),              NodeColumns[2], row, FrgndTxtColor);
      row++;
      Toolkit.printString(Integer.toString(totalNodes),             NodeColumns[0], row, FrgndTxtColor);
      Toolkit.printString("ALL",                                     NodeColumns[1], row, FrgndTxtColor);
      Toolkit.printString(Integer.toString(totalPorts),              NodeColumns[2], row, FrgndTxtColor);
      row+=3;

      // totals per type
      int pn_type = 0;
      String esp = "";
      for(ArrayList<PFM_Node> pn: pNodes)
      {
        // each node in the bin looks identical, so just use the first one
        PFM_Node p = pn.get(0);
        pn_type = p.getNum_ports() > 2 ? 2: 1;
        esp = p.isEsp0() ? "  Y": " ";
        
        // each column should match the attribute from the background screen
        Toolkit.printString(Integer.toString(pn.size()),               NodeColumns[0], row, FrgndTxtColor);
        Toolkit.printString(OSM_NodeType.get(pn_type).getAbrevName(),  NodeColumns[1], row, FrgndTxtColor);
        Toolkit.printString(Short.toString(p.num_ports),               NodeColumns[2], row, FrgndTxtColor);
        Toolkit.printString(esp,                                       NodeColumns[3], row, FrgndTxtColor);
        row++;
      }
      row++;
      }
      else
      {
        logger.warning("NodeColumns array needs to be initialized");
        return false;        
      }
      }
      else
      {
        logger.warning("The data from the PerfManager is not available... yet");
        return false;
      }
    }
    else
    {
      logger.warning("The OSM_Nodes seems to be null"); 
      return false;
    }
    return true;
  }
  
  public boolean paintBackground() throws Exception
  {
    paintBorderBackground();
    // static graphics and text
    int column = 1;
    int col_offset = 1;
    int row    = 4;
    int stringSizes = 0;
    String[] heads = { "QTY", "Type", "# ports", "device_id", "pnv_id", "revision", "base_v", "class_v", "prt_cap" };
    
    printTitleString("OpenSM Nodes", row++);

    row    = 19;
    Toolkit.drawHorizontalLine(column, row, ScreenCols-3, ErrorTxtColor);
    
    // evenly space the columns
    int j = 0;
    for(String s: heads)
      stringSizes += s.length();
    
    col_offset = (ScreenCols - (stringSizes + 2))/(heads.length);
    int c_pos = 3;
    int prev_string_size = 0;
    NodeColumns = new int[heads.length];
    
   // these are the columns, which need to be used for the foreground too
    for(String s: heads)
    {
      if(j == 0)
        c_pos = 3;
      else
        c_pos += col_offset + prev_string_size;
      prev_string_size = s.length();
      NodeColumns[j++] = c_pos;
      Toolkit.printString(s, c_pos, row, BkgndTxtColor);
    }
    
    row=6;
    j = 0;
    Toolkit.drawHorizontalLine(column, row, ScreenCols/2, ErrorTxtColor);
    for(String s: heads)
    {
      Toolkit.printString(s, NodeColumns[j++], row, BkgndTxtColor);
      if(j > 1)
        break;
    }
    Toolkit.printString("T ports", NodeColumns[j++], row, BkgndTxtColor);

    row=11;
    j = 0;
    Toolkit.drawHorizontalLine(column, row, ScreenCols/2, ErrorTxtColor);
    for(String s: heads)
    {
      Toolkit.printString(s, NodeColumns[j++], row, BkgndTxtColor);
      if(j > 2)
        break;
    }
    Toolkit.printString("esp0", NodeColumns[j++], row, BkgndTxtColor);
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
   ***********************************************************/
  public static void main(String[] args) throws Exception
  {
    Toolkit.init();
    Toolkit.clearScreen(BorderBTxtColor);
    OMS_Updater uService = OMS_UpdateProvider.getInstance().getUpdater(UpdaterType.CONNECTION_BASED_UPDATER);
    Properties serviceProps = uService.getProperties();
    if(serviceProps == null)
      serviceProps = new Properties();
    serviceProps.setProperty(SmtProperty.SMT_PORT.name(), "10011");
    serviceProps.setProperty(SmtProperty.SMT_HOST.name(), "localhost");
    serviceProps.setProperty(SmtProperty.SMT_UPDATE_PERIOD.name(), "30");
    uService.init(serviceProps);
    if(uService != null)
    {
      SmtConsoleScreen screen = new NodeStatisticsScreen();
      // start the service
      screen.setServiceUpdater(uService, true);  // this will add itself to the Services listener list, and start the service if its not already running
      TimeUnit.SECONDS.sleep(5);           // wait a little, to allow the screen to be updated with fresh data
      
     // the screen should now have data
      screen.paintBackground();
      screen.paintForeground();
      uService.removeListener(screen);
      
      // stop the service
      uService.destroy();
    }
    
    // wait for any key to be pressed
    InputChar c = Toolkit.readCharacter();
    Toolkit.clearScreen(BorderBTxtColor);
    Toolkit.shutdown();
  }

}
