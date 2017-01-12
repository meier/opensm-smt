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
 *        file: LinkStatisticsScreen.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command.console;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import gov.llnl.lc.infiniband.core.IB_Link;
import gov.llnl.lc.infiniband.core.IB_LinkType;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_UpdateProvider;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_UpdateProvider.UpdaterType;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Updater;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_LinkRate;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_LinkSpeed;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_LinkState;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_LinkWidth;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_NodeType;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Nodes;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Ports;
import gov.llnl.lc.infiniband.opensm.plugin.data.SBN_Node;
import gov.llnl.lc.smt.props.SmtProperty;
import gov.llnl.lc.util.BinList;
import jcurses.system.CharColor;
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
public class LinkStatisticsScreen extends SmtConsoleScreen
{
  // "ALL" IB_Links
  private  ArrayList <IB_Link> ibla = null;
  
  private  int[] Lnum_nodes  = new int[4];
  private  int[] Lnum_ports  = new int[4];
  
  // Separate the links into the different types
  private  ArrayList <IB_Link> ibls = new ArrayList<IB_Link>();
  private  ArrayList <IB_Link> iblc = new ArrayList<IB_Link>();
  private  ArrayList <IB_Link> iblQ = new ArrayList<IB_Link>();  // unknown
 
  // put the various attribute counts in bins
  private  BinList <IB_Link> aLinkBins = new BinList <IB_Link>();
  private  BinList <IB_Link> sLinkBins = new BinList <IB_Link>();
  private  BinList <IB_Link> cLinkBins = new BinList <IB_Link>();
  private  BinList <IB_Link> QLinkBins = new BinList <IB_Link>(); // unknown

  /************************************************************
   * Method Name:
   *  updateForegroundData
  **/
  /**
   * Overrides the NOP abstract method to provide derived LINK
   * data based on an atomic Fabric object.
   *
   * @see     #osmFabricUpdate(OSM_Fabric)
   *
   * @return
   * @throws Exception
   ***********************************************************/
  protected boolean updateForegroundData() throws Exception
  {
    /* this method creates CLASS data from an instance of the Fabric
     * so it should be atomic.  Since the Fabric is being automatically
     * updated within a different thread, this needs to be synchronized
     * so the Fabric doesn't change while within this method.
     */
    boolean status = false;
    
    OSM_Nodes AllNodes = (Fabric == null) ? null : Fabric.getOsmNodes();
    OSM_Ports AllPorts = (Fabric == null) ? null : Fabric.getOsmPorts();

    if ((AllPorts != null) && (AllNodes != null))
    {
      // some link attributes depend on perfmgr data, so wait until available
      if ((AllNodes.getSubnNodes() != null) && (AllNodes.getPerfMgrNodes() != null))
      {
        /*
         * init flag - set true
         */
        status = true;

        // clear the counters
        for (int d = 0; d < 4; d++)
        {
          Lnum_nodes[d] = 0;
          Lnum_ports[d] = 0;
        }
        ArrayList<SBN_Node> sbna = new ArrayList<SBN_Node>(Arrays.asList(AllNodes.getSubnNodes()));
        for (SBN_Node sn : sbna)
        {
          if (OSM_NodeType.get(sn) == OSM_NodeType.SW_NODE)
          {
            Lnum_nodes[0] += 1;
            Lnum_ports[0] += sn.num_ports;
          }
          else if (OSM_NodeType.get(sn) == OSM_NodeType.CA_NODE)
          {
            Lnum_nodes[1] += 1;
            Lnum_ports[1] += sn.num_ports;
          }
          else
          {
            Lnum_nodes[2] += 1;
            Lnum_ports[2] += sn.num_ports;
          }
        }

        // create IB_Links
        ibla = AllPorts.createIB_Links(AllNodes);  // all links

        // clear all other data structures (arrays and binLists)
        ibls = new ArrayList<IB_Link>();    // switch links
        iblc = new ArrayList<IB_Link>();    // channel adapter links
        iblQ = new ArrayList<IB_Link>();    // unknown links (should be empty)
        aLinkBins = new BinList<IB_Link>(); // all
        sLinkBins = new BinList<IB_Link>(); // switch
        cLinkBins = new BinList<IB_Link>(); // channel adapter
        QLinkBins = new BinList<IB_Link>(); // unknown

        for (IB_Link link : ibla)
        {
          // create a list of switch and edge links
          if (link.getLinkType() == IB_LinkType.SW_LINK)
            ibls.add(link);
          else if (link.getLinkType() == IB_LinkType.CA_LINK)
            iblc.add(link);
          else
            iblQ.add(link);

          // bin up the types for ALL links
          if (link.hasTraffic())
            aLinkBins.add(link, "Traffic:");

          if (link.hasErrors())
            aLinkBins.add(link, "Errors:");

          aLinkBins.add(link, "State: " + link.getState().getStateName());
          aLinkBins.add(link, "Speed: " + link.getSpeed().getSpeedName());
          aLinkBins.add(link, "Width: " + link.getWidth().getWidthName());
          aLinkBins.add(link, "Rate: " + link.getRate().getRateName());
        }

        for (IB_Link link : ibls)
        {
          // bin up the types for SW links
          if (link.hasTraffic())
            sLinkBins.add(link, "Traffic:");

          if (link.hasErrors())
            sLinkBins.add(link, "Errors:");

          sLinkBins.add(link, "State: " + link.getState().getStateName());
          sLinkBins.add(link, "Speed: " + link.getSpeed().getSpeedName());
          sLinkBins.add(link, "Width: " + link.getWidth().getWidthName());
          sLinkBins.add(link, "Rate: " + link.getRate().getRateName());
        }

        for (IB_Link link : iblc)
        {
          // bin up the types for CA links
          if (link.hasTraffic())
            cLinkBins.add(link, "Traffic:");

          if (link.hasErrors())
            cLinkBins.add(link, "Errors:");

          cLinkBins.add(link, "State: " + link.getState().getStateName());
          cLinkBins.add(link, "Speed: " + link.getSpeed().getSpeedName());
          cLinkBins.add(link, "Width: " + link.getWidth().getWidthName());
          cLinkBins.add(link, "Rate: " + link.getRate().getRateName());
        }

        for (IB_Link link : iblQ)
        {
          // bin up the types for CA links
          if (link.hasTraffic())
            QLinkBins.add(link, "Traffic:");

          if (link.hasErrors())
            QLinkBins.add(link, "Errors:");

          QLinkBins.add(link, "State: " + link.getState().getStateName());
          QLinkBins.add(link, "Speed: " + link.getSpeed().getSpeedName());
          QLinkBins.add(link, "Width: " + link.getWidth().getWidthName());
          QLinkBins.add(link, "Rate: " + link.getRate().getRateName());
        }
      }
      else
        logger.warning("UD: PerfMgr data is not available... yet");

      // done processing, get ready for painting
      Lnum_nodes[3] = AllNodes.SubnNodes.length;
      Lnum_ports[3] = AllPorts.SubnPorts.length;
  }
  else
  {
    logger.warning("UD: The Node and Port info seems to be unavailable");
  }

    return status;
  }
  
  public boolean paintForeground() throws Exception
  {
    paintBorderForeground();

    // data from the service (may need to pad these)
    ArrayList<Integer> c_array = new ArrayList<Integer>();
    CharColor cc = FrgndTxtColor;
    int[] num_links = new int[4];
    int[] num_active = new int[4];
    int[] num_down = new int[4];
    int[] num_traffic = new int[4];
    int[] num_errors = new int[4];

    int row = 7;
    int rw = 0;
    int cl = 0;

    // attribute column is 20 wide, last 4 get remainder
    int attribute_width = 20;
    int col_offset = (ScreenCols - attribute_width) / 4;

    OSM_Nodes AllNodes = (Fabric == null) ? null : Fabric.getOsmNodes();
    OSM_Ports AllPorts = (Fabric == null) ? null : Fabric.getOsmPorts();

    if ((AllPorts != null) && (AllNodes != null))
    {
      // some link attributes depend on perfmgr data, so wait until available
      if ((AllNodes.getSubnNodes() != null) && (AllNodes.getPerfMgrNodes() != null) && (ibla != null))
      {
        num_links[0] = ibls.size();
        num_links[1] = iblc.size();
        num_links[2] = iblQ.size();
        num_links[3] = ibla.size();

        // the various Bins may not exist (if there was no element to add the
        // bin doesn't get created) so protect against null
        num_active[0] = sLinkBins.getBin("State: " + OSM_LinkState.ACTIVE.getStateName()) == null ? 0
            : sLinkBins.getBin("State: " + OSM_LinkState.ACTIVE.getStateName()).size();
        num_active[1] = cLinkBins.getBin("State: " + OSM_LinkState.ACTIVE.getStateName()) == null ? 0
            : cLinkBins.getBin("State: " + OSM_LinkState.ACTIVE.getStateName()).size();
        num_active[2] = QLinkBins.getBin("State: " + OSM_LinkState.ACTIVE.getStateName()) == null ? 0
            : QLinkBins.getBin("State: " + OSM_LinkState.ACTIVE.getStateName()).size();
        num_active[3] = aLinkBins.getBin("State: " + OSM_LinkState.ACTIVE.getStateName()) == null ? 0
            : aLinkBins.getBin("State: " + OSM_LinkState.ACTIVE.getStateName()).size();

        num_down[0] = sLinkBins.getBin("State: " + OSM_LinkState.DOWN.getStateName()) == null ? 0
            : sLinkBins.getBin("State: " + OSM_LinkState.DOWN.getStateName()).size();
        num_down[1] = cLinkBins.getBin("State: " + OSM_LinkState.DOWN.getStateName()) == null ? 0
            : cLinkBins.getBin("State: " + OSM_LinkState.DOWN.getStateName()).size();
        num_down[2] = QLinkBins.getBin("State: " + OSM_LinkState.DOWN.getStateName()) == null ? 0
            : QLinkBins.getBin("State: " + OSM_LinkState.DOWN.getStateName()).size();
        num_down[3] = aLinkBins.getBin("State: " + OSM_LinkState.DOWN.getStateName()) == null ? 0
            : aLinkBins.getBin("State: " + OSM_LinkState.DOWN.getStateName()).size();

        num_traffic[0] = sLinkBins.getBin("Traffic:") == null ? 0 : sLinkBins.getBin("Traffic:")
            .size();
        num_traffic[1] = cLinkBins.getBin("Traffic:") == null ? 0 : cLinkBins.getBin("Traffic:")
            .size();
        num_traffic[2] = QLinkBins.getBin("Traffic:") == null ? 0 : QLinkBins.getBin("Traffic:")
            .size();
        num_traffic[3] = aLinkBins.getBin("Traffic:") == null ? 0 : aLinkBins.getBin("Traffic:")
            .size();

        num_errors[0] = sLinkBins.getBin("Errors:") == null ? 0 : sLinkBins.getBin("Errors:")
            .size();
        num_errors[1] = cLinkBins.getBin("Errors:") == null ? 0 : cLinkBins.getBin("Errors:")
            .size();
        num_errors[2] = QLinkBins.getBin("Errors:") == null ? 0 : QLinkBins.getBin("Errors:")
            .size();
        num_errors[3] = aLinkBins.getBin("Errors:") == null ? 0 : aLinkBins.getBin("Errors:")
            .size();

        c_array.add(0, col_offset + 1);
        c_array.add(1, col_offset * 2 + 1);
        c_array.add(2, col_offset * 3 + 1);
        c_array.add(3, col_offset * 4 + 1);

        int column = 1;
        for (int n = 0; n < 4; n++)
        {
          rw = row;
          cl = c_array.get(n);
          // total nodes, ports and links, broken down by type
          Toolkit.printString(Long.toString(Lnum_nodes[n]), cl, rw++, FrgndTxtColor);
          Toolkit.printString(Long.toString(Lnum_ports[n]), cl, rw++, FrgndTxtColor);
          Toolkit.printString(Long.toString(num_links[n]), cl, rw++, FrgndTxtColor);
          rw++;
          // active and down links, broken down by type
          Toolkit.printString(Long.toString(num_active[n]), cl, rw++, FrgndTxtColor);
          Toolkit.printString(Long.toString(num_down[n]), cl, rw++, FrgndTxtColor);
          rw++;

          // links with traffic and errors, broken down by type
          Toolkit.printString("traffic:", column, rw, BkgndTxtColor);
          Toolkit.printString(Long.toString(num_traffic[n]), cl, rw++, FrgndTxtColor);
          Toolkit.printString("errors:", column, rw, BkgndTxtColor);
          if (num_errors[n] == 0)
            cc = FrgndTxtColor;
          else
            cc = ErrorTxtColor;
          Toolkit.printString(Long.toString(num_errors[n]), cl, rw++, cc);
        }
        // from here down, paint the foreground and background, because I don't
        // know which attributes
        // are non-zero.
        // width, speed and rate, broken down by type
        // use the "ALL" binlist to determine the number of rows for each width,
        // speed, and rate type

        // how many unique widths (must be at least one)?
        row = rw + 1;
        Toolkit.drawHorizontalLine(column, row, 7, ErrorTxtColor);
        Toolkit.printString("width", column + 1, row++, BkgndTxtColor);

        for (OSM_LinkWidth lw : OSM_LinkWidth.OSMLINK_ALL_WIDTHS)
        {
          ArrayList<IB_Link> la = aLinkBins.getBin("Width: " + lw.getWidthName());
          // place a background if necessary
          if (la != null)
          {
            // there is at least one of these, so loop through all types
            Toolkit.printString(lw.getWidthName(), column, row, BkgndTxtColor);
            num_links[0] = sLinkBins.getBin("Width: " + lw.getWidthName()) == null ? 0 : sLinkBins
                .getBin("Width: " + lw.getWidthName()).size();
            num_links[1] = cLinkBins.getBin("Width: " + lw.getWidthName()) == null ? 0 : cLinkBins
                .getBin("Width: " + lw.getWidthName()).size();
            num_links[2] = QLinkBins.getBin("Width: " + lw.getWidthName()) == null ? 0 : QLinkBins
                .getBin("Width: " + lw.getWidthName()).size();
            num_links[3] = aLinkBins.getBin("Width: " + lw.getWidthName()) == null ? 0 : aLinkBins
                .getBin("Width: " + lw.getWidthName()).size();

            for (int n = 0; n < 4; n++)
            {
              rw = row;
              cl = c_array.get(n);
              Toolkit.printString(Long.toString(num_links[n]), cl, rw++, FrgndTxtColor);
            }
            row = rw;
          }
        }

        // how many unique speeds (must be at least one)?
        row = rw + 1;
        Toolkit.drawHorizontalLine(column, row, 7, ErrorTxtColor);
        Toolkit.printString("speed", column + 1, row++, BkgndTxtColor);

        for (OSM_LinkSpeed ls : OSM_LinkSpeed.OSMLINK_ALL_SPEEDS)
        {
          ArrayList<IB_Link> la = aLinkBins.getBin("Speed: " + ls.getSpeedName());
          // place a background if necessary
          if (la != null)
          {
            // there is at least one of these, so loop through all types
            Toolkit.printString(ls.getSpeedName(), column, row, BkgndTxtColor);
            num_links[0] = sLinkBins.getBin("Speed: " + ls.getSpeedName()) == null ? 0 : sLinkBins
                .getBin("Speed: " + ls.getSpeedName()).size();
            num_links[1] = cLinkBins.getBin("Speed: " + ls.getSpeedName()) == null ? 0 : cLinkBins
                .getBin("Speed: " + ls.getSpeedName()).size();
            num_links[2] = QLinkBins.getBin("Speed: " + ls.getSpeedName()) == null ? 0 : QLinkBins
                .getBin("Speed: " + ls.getSpeedName()).size();
            num_links[3] = aLinkBins.getBin("Speed: " + ls.getSpeedName()) == null ? 0 : aLinkBins
                .getBin("Speed: " + ls.getSpeedName()).size();

            for (int n = 0; n < 4; n++)
            {
              rw = row;
              cl = c_array.get(n);
              Toolkit.printString(Long.toString(num_links[n]), cl, rw++, FrgndTxtColor);
            }
            row = rw;
          }
        }

        // how many unique rates (must be at least one)?
        row = rw + 1;
        Toolkit.drawHorizontalLine(column, row, 7, ErrorTxtColor);
        Toolkit.printString("rate", column + 1, row++, BkgndTxtColor);

        for (OSM_LinkRate lw : OSM_LinkRate.OSMLINK_UNIQUE_RATES)
        {
          ArrayList<IB_Link> la = aLinkBins.getBin("Rate: " + lw.getRateName());
          // place a background if necessary
          if (la != null)
          {
            // there is at least one of these, so loop through all types
            Toolkit.printString(lw.getRateName(), column, row, BkgndTxtColor);
            num_links[0] = sLinkBins.getBin("Rate: " + lw.getRateName()) == null ? 0 : sLinkBins
                .getBin("Rate: " + lw.getRateName()).size();
            num_links[1] = cLinkBins.getBin("Rate: " + lw.getRateName()) == null ? 0 : cLinkBins
                .getBin("Rate: " + lw.getRateName()).size();
            num_links[2] = QLinkBins.getBin("Rate: " + lw.getRateName()) == null ? 0 : QLinkBins
                .getBin("Rate: " + lw.getRateName()).size();
            num_links[3] = aLinkBins.getBin("Rate: " + lw.getRateName()) == null ? 0 : aLinkBins
                .getBin("Rate: " + lw.getRateName()).size();

            for (int n = 0; n < 4; n++)
            {
              rw = row;
              cl = c_array.get(n);
              Toolkit.printString(Long.toString(num_links[n]), cl, rw++, FrgndTxtColor);
            }
            row = rw;
          }
        }
      }
      else
        logger.warning("PF: PerfMgr data is not available... yet");
    }
    else
    {
      logger.warning("PF: The Node and Port info seems to be unavailable");
    }
    return true;
  }
  
  public boolean paintBackground() throws Exception
  {
    paintBorderBackground();
    // static graphics and text
    int column = 1;
    int row    = 4;
    
    // attribute column is 20 wide, last 4 get remainder
    int attribute_width = 20;
    int col_offset = (ScreenCols -attribute_width)/4;
    int col = column;
    
    printTitleString("Links", row++);
    row++;
    Toolkit.drawHorizontalLine(column, row, ScreenCols-4, ErrorTxtColor);

    Toolkit.printString("attribute", col+2, row, BkgndTxtColor);
    
    Toolkit.printString(OSM_NodeType.SW_NODE.getAbrevName(), col+=col_offset, row, BkgndTxtColor);
    Toolkit.printString(OSM_NodeType.CA_NODE.getAbrevName(), col+=col_offset, row, BkgndTxtColor);
    Toolkit.printString(OSM_NodeType.UNKNOWN.getAbrevName(), col+=col_offset, row, BkgndTxtColor);
    Toolkit.printString("All", col+=col_offset, row++, BkgndTxtColor);

    Toolkit.printString("Total Nodes:", column, row++, BkgndTxtColor);
    Toolkit.printString("Total Ports:", column, row++, BkgndTxtColor);
    Toolkit.printString("Total Links:", column, row++, BkgndTxtColor);
    row++;
    Toolkit.printString("Active:", column, row++, BkgndTxtColor);
    Toolkit.printString("Down:", column, row++, BkgndTxtColor);
    row++;
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
      SmtConsoleScreen screen = new LinkStatisticsScreen();
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
