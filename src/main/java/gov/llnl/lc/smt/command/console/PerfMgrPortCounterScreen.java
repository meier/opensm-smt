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
 *        file: PerfMgrPortCounterScreen.java
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
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_NodeType;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Nodes;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Ports;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_Node;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_NodePortStatus;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_Port.PortCounterName;
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
public class PerfMgrPortCounterScreen extends SmtConsoleScreen
{
  private static ArrayList<PFM_NodePortStatus> nps_array  = new ArrayList<PFM_NodePortStatus>();
  private static ArrayList<PFM_Node> pmna = new ArrayList<PFM_Node>();
  private static ArrayList<PFM_Port> pmpa = new ArrayList<PFM_Port>();
  
  /** all the node ports **/
  private static PFM_NodePortStatus pnpsa = null;
  
  /** only the nodes with more than 2 ports, and without esp0 */
  private static PFM_NodePortStatus pnpss = null;
  
  /** only the nodes with more than 2 ports, and with esp0 */      
  private static PFM_NodePortStatus pnpse = null;
  
  /** everything that doesn't look like a switch */
  private static PFM_NodePortStatus pnpsc = null;
  
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
    /*
     * this method creates CLASS data from an instance of the Fabric so it
     * should be atomic. Since the Fabric is being automatically updated within
     * a different thread, this needs to be synchronized so the Fabric doesn't
     * change while within this method.
     */
    boolean status = false;

    OSM_Nodes AllNodes = (Fabric == null) ? null : Fabric.getOsmNodes();
    OSM_Ports AllPorts = (Fabric == null) ? null : Fabric.getOsmPorts();

    if ((AllPorts != null) && (AllNodes != null))
    {
      // some attributes depend on perfmgr data, so wait until available
      if ((AllNodes.getSubnNodes() != null) && (AllNodes.getPerfMgrNodes() != null))
      {
        /*
         * init flag - set true
         */
        status = true;

        nps_array  = new ArrayList<PFM_NodePortStatus>();
        PFM_Node[] pna = AllNodes.getPerfMgrNodes();
        PFM_Port[] ppa = AllPorts.getPerfMgrPorts();
        // the perfmgr may not have returned data due to start-up delay, check
        if((pna != null) && (ppa != null))
        {
        pmna = new ArrayList<PFM_Node>(Arrays.asList(pna));
        pmpa = new ArrayList<PFM_Port>(Arrays.asList(ppa));
        
        logger.warning("Calculating NodePortStatus");
        pnpsa = new PFM_NodePortStatus(pmna, pmpa, true);
        
        /* only the nodes with more than 2 ports, and without esp0 */
        pnpss = PFM_NodePortStatus.getSwitchNPS(pmna, pmpa, false);
        
        /* only the nodes with more than 2 ports, and with esp0 */      
        pnpse = PFM_NodePortStatus.getSwitchNPS(pmna, pmpa, true);
        
        /* everything that doesn't look like a switch */
        pnpsc = PFM_NodePortStatus.getChannelAdapterNPS(pmna, pmpa);
        
        }
        else
          logger.warning("The data from the PerfManager is not available... yet");
      
      }
      else
        logger.warning("UD: PerfMgr data is not available... yet");

      // done processing, get ready for painting
    }
    else
    {
      logger.warning("UD: The Node and Port info seems to be unavailable");
    }

    return status;
  }  
  

  protected int paintPortErrors(int column, int row, ArrayList<PFM_Port> pmpa)
  {
    int NUM_TOP_ERRORS = 5;
    long total_top_errors = 0L;
    
    // show the top ports with with errors, organized by node
    if((pmpa != null) && (pmpa.size() > 0))
    {
      // collect all the ports with errors into bins, organized by port guids
      // organize the ports by guid
      BinList <PFM_Port> pbL  = new BinList <PFM_Port>();
      BinList <PFM_Port> tpbL = new BinList <PFM_Port>();
      
      for(PFM_Port p: pmpa)
      {
        /* if this port has an error, add it */
        if(p.hasError())
        {
          pbL.add(p, p.getNodeGuid().toColonString());
        }
      }
      
      // there should be at least one bin, determine a bin error count
      int n = pbL.size();
      int[] errorArray = new int[n];
      int[] used_ndex  = new int[n];
      int num_added = 0;
      
      for(int j = 0; j < n; j++)
      {
        for(PFM_Port pp: pbL.getBin(j))
        {
          errorArray[j] += pp.getTotalErrors();
        }
      }
      
      // now sort this errorArray, worst to best
      int [] sortedArray = Arrays.copyOf(errorArray, errorArray.length);
      Arrays.sort(sortedArray);
      
      // construct a new BinList, sorted by total errors, and limited in size to NUM_TOP_ERRORS
      for(int j = n-1; (j >= 0) && (num_added < NUM_TOP_ERRORS); j--)
      {
        // get the bin that matches the Reversed sorted error counts
        for(int ndex = 0; ndex < n; ndex++)
        {
          if(sortedArray[j] == errorArray[ndex])
          {
            // found a match using ndex, check to see if we have found this already
            //  is ndex in the used_ndex array?  if so break;
            boolean skip = false;
            for(int k = 0; k < num_added; k++)
              if(ndex == used_ndex[k])
              {
                skip = true;
                break;
              }
            if(skip)
              break;
            
            tpbL.addBin(pbL.getBin(ndex));
            total_top_errors += sortedArray[j];
            
            // mark this ndex, or Bin, used, so we don't add it again
            used_ndex[num_added] = ndex;
            num_added++;
            break;
          }
        }
      }
      
      // print out only the top errors, or until I run out of lines
      
      int c = 14;
      String tte = Long.toString(total_top_errors);
      Toolkit.printString("(", c, row-1, BkgndTxtColor);
      Toolkit.printString(tte, c+1, row-1, FrgndTxtColor);
      Toolkit.printString(")", c+1+tte.length(), row-1, BkgndTxtColor);

      int numErrorBins = tpbL.size();
      String ErrorString = null;
      String PortDescString = null;
      int maxStringLength = ScreenCols - (column+4);
      int maxNumLines = ScreenRows - (row+3);
      int numLines = 0;
      
      for(int j = 0; (j < numErrorBins) && (numLines < maxNumLines); j++)
      {
        ArrayList<PFM_Port> pL = tpbL.getBin(j);
        PortDescString = PFM_Port.getPortDescription(pL);
        Toolkit.printString(PortDescString, column, row++, FrgndTxtColor);
        numLines++;
        for (PortCounterName counter : PortCounterName.PFM_ERROR_COUNTERS)
        {
          // error_name (num): port_num=value, port_num = value, ...
          StringBuffer sbuff = new StringBuffer();
            int num_errs = 0;
            for(PFM_Port p : pL)
              if(p.getCounter(counter) != 0L)
                num_errs++;

            // skip this error, if all ports are zero
            if(num_errs > 0)
            {
            boolean newError = true;
            for(PFM_Port p : pL)
            {
              if(p.getCounter(counter) != 0L)
              {
                // if this is the first one, include the name
                if(newError)
                {
                  sbuff.append(counter.name() + "(" + num_errs + "): " + p.port_num + "=" + p.getCounter(counter));
                  newError = false;
                }
                else
                {
                  sbuff.append(", " + p.port_num + "=" + p.getCounter(counter));
                }
              }
            }
            }
            ErrorString = sbuff.toString();
        // don't exceed ScreenCols in length or go out of the box
        if((ErrorString != null) && (ErrorString.length() > 0) && (numLines < maxNumLines) && (row < (ScreenRows - 3)))
        {
          numLines++;
          if(ErrorString.length() > maxStringLength)
            Toolkit.printString(ErrorString.substring(0, maxStringLength-4) + "...", column+4, row++, FrgndTxtColor);
          else
            Toolkit.printString(ErrorString, column+4, row++, FrgndTxtColor);
        }
        }
      }
    }
    return row;
  }
  public boolean paintForeground() throws Exception
  {
    paintBorderForeground();
    
    ArrayList <Integer> c_array  = new ArrayList<Integer>();
    CharColor val_color = null;
    
    int row    = 7;
    
    // attribute column is 20 wide, last 4 get remainder
    int attribute_width = 20;
    int col_offset = (ScreenCols -attribute_width)/4;
    int cl= 0, rw=0;
 
    OSM_Nodes AllNodes = (Fabric == null) ? null : Fabric.getOsmNodes();
    OSM_Ports AllPorts = (Fabric == null) ? null : Fabric.getOsmPorts();
    
    if((Fabric != null) && (Fabric.getTimeStamp() != null))
      printDataTimeStamp(Fabric.getTimeStamp(), 4, true);

//      Toolkit.printString("(" + Fabric.getTimeStamp().toString() + ")",  38, 5, FrgndTxtColor);
//
//    boolean stale = (Fabric == null) ? true : Fabric.isStale();
//    if(stale)
//    {
//      Toolkit.printString("  counter values are stale",  59, 5, ErrorTxtColor);
//      Toolkit.printString(")",  85, 5, FrgndTxtColor);
//    }
//    else
//    {
//      Toolkit.printString(")                         ",  59, 5, FrgndTxtColor);
//      Toolkit.printString(" ",  85, 5, FrgndTxtColor);
//    }

    if((AllNodes != null) && (AllPorts != null))
    {
      // the four arrays need to exist, or go no further
      if(nps_array == null || pmna == null || pmpa == null || pnpsa == null)
      {
        logger.severe("Painting Forground 2,have bad arrays, sup?");      
        return false;
      }
      
      c_array.add(0, col_offset + 7);
      nps_array.add(0, pnpss);  // just the switches (without eps0)
      c_array.add(1, col_offset*2 +3);
      nps_array.add(1, pnpse);  // just the eps ports
      c_array.add(2, col_offset*3 +1);
      nps_array.add(2, pnpsc);  // just the CA nodes & ports
      c_array.add(3, col_offset*4 -1);
      nps_array.add(3, pnpsa);  // this should be everything

      for(int n = 0; n < 4; n++)
      {
        rw = row;
        cl = c_array.get(n);
        PFM_NodePortStatus nps = nps_array.get(n);
        
        Toolkit.printString(Long.toString(nps.total_nodes),          cl, rw++, FrgndTxtColor);
        Toolkit.printString(Long.toString(nps.total_ports),          cl, rw++, FrgndTxtColor);
        rw++;

        long val = 0L;
        if(nps != null)
        for (PortCounterName counter : PortCounterName.PFM_ALL_COUNTERS)
        {
          /* show the count values, and color non-zero error counters red */
       
          val = nps.port_counters[counter.ordinal()];
          val_color = (PortCounterName.PFM_ERROR_COUNTERS.contains(counter) && (val != 0)) ? ErrorTxtColor: FrgndTxtColor;
          Toolkit.printString(Long.toString(val),  cl, rw++, val_color);
          
          /* separate errors from other counters with a blank line*/
          if(counter.equals(PFM_Port.PortCounterName.vl15_dropped))
            rw++;
        }
      }
      row = paintPortErrors(1, rw+2, pmpa);
    }
    else
    {
      logger.warning("The PerfMgr Objects seems to be null");      
    }
    return true;
  }
  
  public boolean paintBackground() throws Exception
  {
    paintBorderBackground();
    // static graphics and text
    int column = 1;
    int row    = 4;
    
    // counter (attribute) column is 20 wide, last 4 get remainder
    int attribute_width = 20;
    int col_offset = (ScreenCols -attribute_width)/4;
    int col = column;
    
    printTitleString("Performance Manager", row++);
    row++;
    Toolkit.drawHorizontalLine(column, row, ScreenCols-4, ErrorTxtColor);

    Toolkit.printString("counter", col+2, row, BkgndTxtColor);
    
    Toolkit.printString(OSM_NodeType.SW_NODE.getAbrevName(), col+=col_offset+6, row, BkgndTxtColor);
    Toolkit.printString(OSM_NodeType.SW_NODE.getAbrevName()+"(esp0)", col+=col_offset-4, row, BkgndTxtColor);
    Toolkit.printString(OSM_NodeType.CA_NODE.getAbrevName(), col+=col_offset-2, row, BkgndTxtColor);
    Toolkit.printString("All", col+=col_offset-2, row++, BkgndTxtColor);

    Toolkit.printString("Total Nodes:", column, row++, BkgndTxtColor);
    Toolkit.printString("Total Ports:", column, row++, BkgndTxtColor);
    row++;

    for(PFM_Port.PortCounterName n : PortCounterName.PFM_ALL_COUNTERS)
    {
      Toolkit.printString(n + ":", column, row++, BkgndTxtColor);
      
      /* separate errors from other counters */
      if(n.equals(PFM_Port.PortCounterName.vl15_dropped))
        row++;
    }
    
    row++;
    Toolkit.drawHorizontalLine(column, row, 30, ErrorTxtColor);
     Toolkit.printString("top errors", column+2, row++, BkgndTxtColor);
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
      SmtConsoleScreen screen = new PerfMgrPortCounterScreen();
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
