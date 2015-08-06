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
 *        file: SubnetStatusScreen.java
 *
 *  Created on: Mar 21, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command.console;

import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_UpdateProvider;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Updater;
import gov.llnl.lc.infiniband.opensm.plugin.data.SBN_Manager;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_UpdateProvider.UpdaterType;
import gov.llnl.lc.smt.props.SmtProperty;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

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
public class SubnetStatusScreen extends SmtConsoleScreen
{
  public boolean paintForeground() throws Exception
  {
    paintBorderForeground();
    
    // data from the service (may need to pad these)
    int column = 40;
    int row    = 6;
    
    boolean stale = (Fabric == null) ? true : Fabric.isStale();
    String staleString = stale ? "(stale)": "       ";
    
    /* keep in order, top down.  See row value increment */

    if(SysInfo != null)
    {
      Toolkit.printString(SysInfo.SM_State,       column, row++, FrgndTxtColor);
      Toolkit.printString(Integer.toString(SysInfo.SM_Priority),    column, row++, FrgndTxtColor);
      Toolkit.printString(SysInfo.SA_State,       column, row++, FrgndTxtColor);
      Toolkit.printString(SysInfo.RoutingEngine,  column, row++, FrgndTxtColor);
      Toolkit.printString("unknown",       column, row++, FrgndTxtColor);
      Toolkit.printString(Arrays.toString(SysInfo.EventPlugins), column, row++, FrgndTxtColor);
      row++;
      Toolkit.printString(SysInfo.PM_State + "/" + SysInfo.PM_SweepState,       column, row, FrgndTxtColor);
      Toolkit.printString(staleString,             column+18, row++, ErrorTxtColor);
      Toolkit.printString(Integer.toString(SysInfo.PM_SweepTime),       column, row++, FrgndTxtColor);
    }
    else
    {
      logger.warning("The SysInfo seems to be null");      
    }
    if(Stats != null)
    {
      row+=2;
      Toolkit.printString(Long.toString(Stats.qp0_mads_outstanding),       column, row++, FrgndTxtColor);
      Toolkit.printString(Long.toString(Stats.qp0_mads_outstanding_on_wire),       column, row++, FrgndTxtColor);
      Toolkit.printString(Long.toString(Stats.qp0_mads_rcvd),       column, row++, FrgndTxtColor);
      Toolkit.printString(Long.toString(Stats.qp0_mads_sent),       column, row++, FrgndTxtColor);
      Toolkit.printString(Long.toString(Stats.qp0_unicasts_sent),       column, row++, FrgndTxtColor);
      Toolkit.printString(Long.toString(Stats.qp0_mads_rcvd_unknown),       column, row++, FrgndTxtColor);
      Toolkit.printString(Long.toString(Stats.sa_mads_outstanding),       column, row++, FrgndTxtColor);
      Toolkit.printString(Long.toString(Stats.sa_mads_rcvd),       column, row++, FrgndTxtColor);
      Toolkit.printString(Long.toString(Stats.sa_mads_sent),       column, row++, FrgndTxtColor);
      Toolkit.printString(Long.toString(Stats.sa_mads_rcvd_unknown),       column, row++, FrgndTxtColor);
      Toolkit.printString(Long.toString(Stats.sa_mads_ignored),       column, row++, FrgndTxtColor);
    }
    else
    {
      logger.warning("The Stats seems to be null");      
    }
    if(Subnet != null)
    {
      row+=2;
      Toolkit.printString(Boolean.toString(Subnet.sweeping_enabled),       column, row++, FrgndTxtColor);
      Toolkit.printString(Integer.toString(Subnet.Options.sweep_interval),       column, row++, FrgndTxtColor);
      Toolkit.printString(Boolean.toString(Subnet.ignore_existing_lfts),       column, row++, FrgndTxtColor);
      Toolkit.printString(Boolean.toString(Subnet.subnet_initialization_error),       column, row++, FrgndTxtColor);
      Toolkit.printString(Boolean.toString(Subnet.in_sweep_hop_0),       column, row++, FrgndTxtColor);
      Toolkit.printString(Boolean.toString(Subnet.first_time_master_sweep),       column, row++, FrgndTxtColor);
      Toolkit.printString(Boolean.toString(Subnet.set_client_rereg_on_sweep),       column, row++, FrgndTxtColor);
      Toolkit.printString(Boolean.toString(Subnet.coming_out_of_standby),       column, row++, FrgndTxtColor);
      
      /* iterate through the managers, hopefully 2 or less */
      column = 1;
      row+=4;
      for(SBN_Manager m: Subnet.Managers)
      {
        Toolkit.printString(new IB_Guid(m.guid).toColonString(), column, row, FrgndTxtColor);
        Toolkit.printString(m.State, column+25, row, FrgndTxtColor);
        Toolkit.printString(Short.toString(m.pri_state), column+40, row++, FrgndTxtColor);
      }
    }
    else
    {
      logger.warning("The Subnet seems to be null");      
    }
    return true;
  }
  
  public boolean paintBackground() throws Exception
  {
    paintBorderBackground();
    // static graphics and text
    int column = 1;
    int row    = 4;
    
    printTitleString("Subnet Status", row++);

    row++;    
    Toolkit.printString("SM State:", column, row++, BkgndTxtColor);
    Toolkit.printString("SM Priority:", column, row++, BkgndTxtColor);
    Toolkit.printString("SA State:", column, row++, BkgndTxtColor);
    Toolkit.printString("Routing Engine:", column, row++, BkgndTxtColor);
    Toolkit.printString("AR Routing:", column, row++, BkgndTxtColor);
    Toolkit.printString("Loaded event plugins:", column, row++, BkgndTxtColor);
    row++;
    Toolkit.printString("PerfMgr state/sweep state:", column, row++, BkgndTxtColor);
    Toolkit.printString("PerfMgr sweep time (seconds):", column, row++, BkgndTxtColor);
    row++;
    Toolkit.drawHorizontalLine(column, row, 30, ErrorTxtColor);
    Toolkit.printString("MAD stats", column+2, row++, BkgndTxtColor);
    Toolkit.printString("QP0 MADs outstanding", column, row++, BkgndTxtColor);
    Toolkit.printString("QP0 MADs outstanding (on wire)", column, row++, BkgndTxtColor);
    Toolkit.printString("QP0 MADs rcvd", column, row++, BkgndTxtColor);
    Toolkit.printString("QP0 MADs sent", column, row++, BkgndTxtColor);
    Toolkit.printString("QP0 unicasts sent", column, row++, BkgndTxtColor);
    Toolkit.printString("QP0 unknown MADs rcvd", column, row++, BkgndTxtColor);
    Toolkit.printString("SA MADs outstanding", column, row++, BkgndTxtColor);
    Toolkit.printString("SA MADs rcvd", column, row++, BkgndTxtColor);
    Toolkit.printString("SA MADs sent", column, row++, BkgndTxtColor);
    Toolkit.printString("SA unknown MADs rcvd", column, row++, BkgndTxtColor);
    Toolkit.printString("SA MADs ignored", column, row++, BkgndTxtColor);
    row++;
    Toolkit.drawHorizontalLine(column, row, 30, ErrorTxtColor);
    Toolkit.printString("Subnet flags", column+2, row++, BkgndTxtColor);
    Toolkit.printString("Sweeping enabled", column, row++, BkgndTxtColor);
    Toolkit.printString("Sweep interval (seconds)", column, row++, BkgndTxtColor);
    Toolkit.printString("Ignore existing lfts", column, row++, BkgndTxtColor);
    Toolkit.printString("Subnet Init errors", column, row++, BkgndTxtColor);
    Toolkit.printString("In sweep hop 0", column, row++, BkgndTxtColor);
    Toolkit.printString("First time master sweep", column, row++, BkgndTxtColor);
    Toolkit.printString("Set client rereg on sweep", column, row++, BkgndTxtColor);
    Toolkit.printString("Coming out of standby", column, row++, BkgndTxtColor);
    row++;
    Toolkit.drawHorizontalLine(column, row, 30, ErrorTxtColor);
    Toolkit.printString("Known SMs", column+2, row++, BkgndTxtColor);
    Toolkit.printString("Port GUID", column, row, BkgndTxtColor);
    Toolkit.printString("SM State", column+25, row, BkgndTxtColor);
    Toolkit.printString("Priority", column+40, row++, BkgndTxtColor);
    Toolkit.drawHorizontalLine(column, row, column+20, ErrorTxtColor);
    Toolkit.drawHorizontalLine(column+25, row, column+34, ErrorTxtColor);
    Toolkit.drawHorizontalLine(column+40, row++, column+49, ErrorTxtColor);

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
    OMS_Updater uService = OMS_UpdateProvider.getInstance().getUpdater(UpdaterType.FILE_BASED_UPDATER);
    Properties serviceProps = uService.getProperties();
    if(serviceProps == null)
      serviceProps = new Properties();
    serviceProps.setProperty(SmtProperty.SMT_PORT.name(), "10011");
    serviceProps.setProperty(SmtProperty.SMT_HOST.name(), "localhost");
    serviceProps.setProperty(SmtProperty.SMT_UPDATE_PERIOD.name(), "1");
    serviceProps.setProperty(SmtProperty.SMT_OMS_FILE.name(), "/home/meier3/.smt/BigOMS.cache");
    uService.init(serviceProps);

    if(uService != null)
    {
      SmtConsoleScreen screen = new SubnetStatusScreen();
      // start the service
      screen.setServiceUpdater(uService, true);  // this will add itself to the Services listener list, and start the service if its not already running
      TimeUnit.SECONDS.sleep(15);           // wait a little, to allow the screen to be updated with fresh data
      
      // the screen should now have data
       screen.paintBackground();
       screen.paintForeground();
       TimeUnit.SECONDS.sleep(5);           // wait a little, to allow the screen to be updated with fresh data
       System.err.println("first one");
       
       // the screen should now have data
        screen.paintBackground();
        screen.paintForeground();
        TimeUnit.SECONDS.sleep(15);           // wait a little, to allow the screen to be updated with fresh data
        System.err.println("first second");
        
        // the screen should now have data
        screen.paintBackground();
        screen.paintForeground();
        TimeUnit.SECONDS.sleep(15);           // wait a little, to allow the screen to be updated with fresh data
        System.err.println("first thrid");
        
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
