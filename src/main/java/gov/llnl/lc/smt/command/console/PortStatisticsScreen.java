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
 *        file: PortStatisticsScreen.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command.console;

import gov.llnl.lc.infiniband.core.IB_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_UpdateProvider;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Updater;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_NodeType;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_UpdateProvider.UpdaterType;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_Port.PortCounterName;
import gov.llnl.lc.infiniband.opensm.plugin.data.SBN_NodePortStatus;
import gov.llnl.lc.smt.props.SmtProperty;
import gov.llnl.lc.util.BinList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

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
public class PortStatisticsScreen extends SmtConsoleScreen
{
  public boolean paintForeground() throws Exception
  {
    paintBorderForeground();
    
    // data from the service (may need to pad these)
    ArrayList<SBN_NodePortStatus> nps_array  = new ArrayList<SBN_NodePortStatus>();
    ArrayList <Integer> c_array  = new ArrayList<Integer>();
    
    CharColor err_color = null;
    
    int row    = 7;
    
    // attribute column is 20 wide, last 4 get remainder
    int attribute_width = 20;
    int col_offset = (ScreenCols -attribute_width)/4;
    
    
    SBN_NodePortStatus tots = new SBN_NodePortStatus();
    SBN_NodePortStatus nps = null;
    int cl= 0, rw=0;
    
    /* keep in order, top down.  See row value increment */

    if(SysInfo != null)
    {
      nps_array.add(0, SysInfo.SW_PortStatus);
      c_array.add(0, col_offset + 1);
      nps_array.add(1, SysInfo.CA_PortStatus);
      c_array.add(1, col_offset*2 +1);
      nps_array.add(2, SysInfo.RT_PortStatus);
      c_array.add(2, col_offset*3 +1);
      nps_array.add(3, tots);
      c_array.add(3, col_offset*4 +1);

      for(int n = 0; n < 4; n++)
      {
        rw = row;
        cl = c_array.get(n);
        nps = nps_array.get(n);
        
        Toolkit.printString(Long.toString(nps.total_nodes),          cl, rw++, FrgndTxtColor);
        tots.total_nodes += nps.total_nodes;
        Toolkit.printString(Long.toString(nps.total_ports),      cl, rw++, FrgndTxtColor);
        tots.total_ports += nps.total_ports;
        rw++;
      Toolkit.printString(Long.toString(nps.ports_active),        cl, rw++, FrgndTxtColor);
      tots.ports_active += nps.ports_active;
      Toolkit.printString(Long.toString(nps.ports_down),          cl, rw++, FrgndTxtColor);
      
      tots.ports_down += nps.ports_down;
      err_color = nps.ports_disabled != 0? ErrorTxtColor: FrgndTxtColor;
      Toolkit.printString(Long.toString(nps.ports_disabled),      cl, rw++, err_color);
      tots.ports_disabled += nps.ports_disabled;
      rw++;
      Toolkit.printString(Long.toString(nps.ports_1X),            cl, rw++, FrgndTxtColor);
      tots.ports_1X += nps.ports_1X;
      Toolkit.printString(Long.toString(nps.ports_4X),            cl, rw++, FrgndTxtColor);
      tots.ports_4X += nps.ports_4X;
      Toolkit.printString(Long.toString(nps.ports_8X),            cl, rw++, FrgndTxtColor);
      tots.ports_8X += nps.ports_8X;
      Toolkit.printString(Long.toString(nps.ports_12X),           cl, rw++, FrgndTxtColor);
      tots.ports_12X += nps.ports_12X;
      err_color = nps.ports_reduced_width != 0? ErrorTxtColor: FrgndTxtColor;
      Toolkit.printString(Long.toString(nps.ports_reduced_width), cl, rw++, err_color);
      tots.ports_reduced_width += nps.ports_reduced_width;
      Toolkit.printString(Long.toString(nps.ports_unknown_width), cl, rw++, FrgndTxtColor);
      tots.ports_unknown_width += nps.ports_unknown_width;
      Toolkit.printString(Long.toString(nps.ports_unenabled_width), cl, rw++, FrgndTxtColor);
      tots.ports_unenabled_width += nps.ports_unenabled_width;
      rw++;
      Toolkit.printString(Long.toString(nps.ports_sdr),           cl, rw++, FrgndTxtColor);
      tots.ports_sdr += nps.ports_sdr;
      Toolkit.printString(Long.toString(nps.ports_ddr),           cl, rw++, FrgndTxtColor);
      tots.ports_ddr += nps.ports_ddr;
      Toolkit.printString(Long.toString(nps.ports_qdr),           cl, rw++, FrgndTxtColor);
      tots.ports_qdr += nps.ports_qdr;
      Toolkit.printString(Long.toString(nps.ports_fdr10),           cl, rw++, FrgndTxtColor);
      tots.ports_fdr10 += nps.ports_fdr10;
      Toolkit.printString(Long.toString(nps.ports_fdr),           cl, rw++, FrgndTxtColor);
      tots.ports_fdr += nps.ports_fdr;
      Toolkit.printString(Long.toString(nps.ports_edr),           cl, rw++, FrgndTxtColor);
      tots.ports_edr += nps.ports_edr;
      err_color = nps.ports_reduced_speed != 0? ErrorTxtColor: FrgndTxtColor;
      Toolkit.printString(Long.toString(nps.ports_reduced_speed), cl, rw++, err_color);
      tots.ports_reduced_speed += nps.ports_reduced_speed;
      Toolkit.printString(Long.toString(nps.ports_unknown_speed), cl, rw++, FrgndTxtColor);
      tots.ports_unknown_speed += nps.ports_unknown_speed;
      Toolkit.printString(Long.toString(nps.ports_unenabled_speed), cl, rw++, FrgndTxtColor);
      tots.ports_unenabled_speed += nps.ports_unenabled_speed;
      }
      // show the ports with problems
      row = rw + 1;
      int column = 1;
      Toolkit.drawHorizontalLine(column, row, 30, ErrorTxtColor);
      Toolkit.printString("Disabled Ports", column+2, row++, BkgndTxtColor);
      for(SBN_NodePortStatus ps: nps_array)
        row = paintPortProblem(column, row, ps.NodeType, ps.disabled_ports);
      
      row++;
      Toolkit.drawHorizontalLine(column, row, 30, ErrorTxtColor);
      Toolkit.printString("Reduced Speed", column+2, row++, BkgndTxtColor);
      for(SBN_NodePortStatus ps: nps_array)
        row = paintPortProblem(column, row, ps.NodeType, ps.reduced_speed_ports);
      
      row++;
      Toolkit.drawHorizontalLine(column, row, 30, ErrorTxtColor);
      Toolkit.printString("Reduced Width", column+2, row++, BkgndTxtColor);
      for(SBN_NodePortStatus ps: nps_array)
        row = paintPortProblem(column, row, ps.NodeType, ps.reduced_width_ports);
      
    }
    else
    {
      logger.warning("The SysInfo seems to be null");      
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
    
    printTitleString("Port Status", row++);
    row++;
    Toolkit.drawHorizontalLine(column, row, ScreenCols-4, ErrorTxtColor);

    Toolkit.printString("attribute", col+2, row, BkgndTxtColor);
    
    Toolkit.printString("SW", col+=col_offset, row, BkgndTxtColor);
    Toolkit.printString("CA", col+=col_offset, row, BkgndTxtColor);
    Toolkit.printString("RT", col+=col_offset, row, BkgndTxtColor);
    Toolkit.printString("All", col+=col_offset, row++, BkgndTxtColor);

    Toolkit.printString("Total Nodes:", column, row++, BkgndTxtColor);
    Toolkit.printString("Total Ports:", column, row++, BkgndTxtColor);
    row++;
    Toolkit.printString("Active:", column, row++, BkgndTxtColor);
    Toolkit.printString("Down:", column, row++, BkgndTxtColor);
    Toolkit.printString("Disabled:", column, row++, BkgndTxtColor);
    row++;
    Toolkit.printString("1X:", column, row++, BkgndTxtColor);
    Toolkit.printString("4X:", column, row++, BkgndTxtColor);
    Toolkit.printString("8X:", column, row++, BkgndTxtColor);
    Toolkit.printString("12X:", column, row++, BkgndTxtColor);
    Toolkit.printString("reduced width:", column, row++, BkgndTxtColor);
    Toolkit.printString("unknown width:", column, row++, BkgndTxtColor);
    Toolkit.printString("unenabled width:", column, row++, BkgndTxtColor);
    row++;
    Toolkit.printString("SDR:", column, row++, BkgndTxtColor);
    Toolkit.printString("DDR:", column, row++, BkgndTxtColor);
    Toolkit.printString("QDR:", column, row++, BkgndTxtColor);
    Toolkit.printString("FDR10:", column, row++, BkgndTxtColor);
    Toolkit.printString("FDR:", column, row++, BkgndTxtColor);
    Toolkit.printString("EDR:", column, row++, BkgndTxtColor);
    Toolkit.printString("reduced speed:", column, row++, BkgndTxtColor);
    Toolkit.printString("unknown speed:", column, row++, BkgndTxtColor);
    Toolkit.printString("unenabled speed:", column, row++, BkgndTxtColor);
    return true;
    }

  private String getAbbreviatedType(String s)
  {
    if(s != null)
    {
      if(s.startsWith("S"))
        return OSM_NodeType.SW_NODE.getAbrevName();
      if(s.startsWith("C"))
        return OSM_NodeType.CA_NODE.getAbrevName();
      if(s.startsWith("R"))
        return OSM_NodeType.RT_NODE.getAbrevName();
    }
    return OSM_NodeType.UNKNOWN.getAbrevName();
  }

  protected int paintPortProblem(int column, int row, String type, IB_Port[] pArray)
  {
    // show the ports with problems
    if((pArray != null) && (pArray.length > 0))
    {
      // collect all these ports into bins, organized by port guids
      BinList <IB_Port> pbL = new BinList <IB_Port>();
      for(IB_Port p: pArray)
      {
        pbL.add(p, p.guid.toColonString());
      }
      
      // there should be at least one bin
      int n = pbL.size();
      for(int j = 0; j < n; j++)
      {
        ArrayList<IB_Port> pL = pbL.getBin(j);
        IB_Port p0 = pL.get(0);
        String pDesc = ("guid=" + p0.guid + " desc=" + p0.Description);
        StringBuffer sbuff = new StringBuffer();
        for(IB_Port p: pL)
          sbuff.append(p.portNumber + ", ");
        
        // strip off the trailing 
        sbuff.setLength((sbuff.length()-2));
        String pNum  = sbuff.toString();

        Toolkit.printString(getAbbreviatedType(type) + "--" + pDesc, column, row++, FrgndTxtColor);
        Toolkit.printString("port(s)=" + pNum, column+4, row++, FrgndTxtColor);
      }
    }
    return row;
  }
  

  protected int paintPortErrors4(int column, int row, ArrayList<PFM_Port> pmpa)
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
      SmtConsoleScreen screen = new PortStatisticsScreen();
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
