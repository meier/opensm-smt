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
 *        file: OpenSmMonitorServiceScreen.java
 *
 *  Created on: Mar 22, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command.console;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_UpdateProvider;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_UpdateProvider.UpdaterType;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Updater;
import gov.llnl.lc.smt.props.SmtProperty;
import jcurses.system.InputChar;
import jcurses.system.Toolkit;

/**********************************************************************
 * Describe purpose and responsibility of OpenSmMonitorServiceScreen
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Mar 22, 2013 8:17:03 AM
 **********************************************************************/
public class OpenSmMonitorServiceScreen extends SmtConsoleScreen
{

  /************************************************************
   * Method Name:
   *  paintBackground
   **/
  /**
   * Describe the method here
   *
   * @see gov.llnl.lc.smt.command.console.ConsoleScreen#paintBackground()
   *
   * @return
   * @throws Exception
   ***********************************************************/

  @Override
  public boolean paintBackground() throws Exception
  {
    paintBorderBackground();
    // static graphics and text
    int column = 1;
    int row    = 4;
    
    printTitleString("Monitor Service", row++);

    Toolkit.drawHorizontalLine(column, row, 30, ErrorTxtColor);
    Toolkit.printString("Remote Service", column+2, row++, BkgndTxtColor);
    Toolkit.printString("Host:", column, row++, BkgndTxtColor);
    Toolkit.printString("Port:", column, row++, BkgndTxtColor);
    Toolkit.printString("Name:", column, row++, BkgndTxtColor);
    Toolkit.printString("Thread ID:", column, row++, BkgndTxtColor);
    row++;    
    Toolkit.printString("Authenticator:", column, row++, BkgndTxtColor);
    Toolkit.printString(" (localhost allowed?):", column, row++, BkgndTxtColor);
    Toolkit.printString("Protocol:", column, row++, BkgndTxtColor);
    row++;    
    Toolkit.printString("Plugin refresh period (seconds):", column, row++, BkgndTxtColor);
    Toolkit.printString("Plugin report period (seconds):", column, row++, BkgndTxtColor);
    Toolkit.printString("Plugin refresh count:", column, row++, BkgndTxtColor);
    Toolkit.printString("Server refresh period (seconds):", column, row++, BkgndTxtColor);
    Toolkit.printString("Server refresh count:", column, row++, BkgndTxtColor);
    Toolkit.printString("Event timeout (milliseconds):", column, row++, BkgndTxtColor);
    Toolkit.printString("Event count:", column, row++, BkgndTxtColor);
    row++;    
    Toolkit.printString("Active Clients:", column, row++, BkgndTxtColor);
    Toolkit.printString("Cumulative Clients:", column, row++, BkgndTxtColor);
    Toolkit.printString("Max Parent Sessions:", column, row++, BkgndTxtColor);
    Toolkit.printString("Max Child Sessions (per parent):", column, row++, BkgndTxtColor);
    row++;
    Toolkit.drawHorizontalLine(column, row, 30, ErrorTxtColor);
    Toolkit.printString("SmtConsole Client (this)", column+2, row++, BkgndTxtColor);
    Toolkit.printString("up since:", column, row++, BkgndTxtColor);
    Toolkit.printString("Connection:", column, row++, BkgndTxtColor);
    Toolkit.printString(" Remote Host:", column, row++, BkgndTxtColor);
    Toolkit.printString(" Port:", column, row++, BkgndTxtColor);
    Toolkit.printString(" Name:", column, row++, BkgndTxtColor);
    Toolkit.printString(" Thread ID:", column, row++, BkgndTxtColor);
    Toolkit.printString(" User ID:", column, row++, BkgndTxtColor);
    Toolkit.printString(" connected since:", column, row++, BkgndTxtColor);
    Toolkit.printString(" (connection reused):", column, row++, BkgndTxtColor);
    row++;    
    Toolkit.printString("Authenticator:", column, row++, BkgndTxtColor);
    Toolkit.printString("Protocol:", column, row++, BkgndTxtColor);
    row++;    
    Toolkit.printString("Console refresh period (seconds):", column, row++, BkgndTxtColor);
    return true;
  }

  /************************************************************
   * Method Name:
   *  paintForeground
   **/
  /**
   * Describe the method here
   *
   * @see gov.llnl.lc.smt.command.console.ConsoleScreen#paintForeground()
   *
   * @return
   * @throws Exception
   ***********************************************************/

  @Override
  public boolean paintForeground() throws Exception
  {
    paintBorderForeground();
    // data from the service (may need to pad these)
    int column = 40;
    int row    = 6;
    
    /* keep in order, top down.  See row value increment */

    if(ServerStatus != null)
    {
      Toolkit.printString(ServerStatus.Server.getHost(),                 column, row++, FrgndTxtColor);
      Toolkit.printString(Integer.toString(ServerStatus.Server.getPortNum()),                 column, row++, FrgndTxtColor);
      Toolkit.printString(ServerStatus.Server.getServerName(),                 column, row++, FrgndTxtColor);
      Toolkit.printString(Long.toString(ServerStatus.Server.getThreadId()),                 column, row++, FrgndTxtColor);
      row++;    
      if(ParentSessionStatus != null)
      {
        Toolkit.printString(ParentSessionStatus.getAuthenticator(),       column, row++, FrgndTxtColor);
        Toolkit.printString(Boolean.toString(ServerStatus.AllowLocalHost), column, row++, FrgndTxtColor);
        Toolkit.printString(ParentSessionStatus.getProtocol(),       column, row++, FrgndTxtColor);
      }
      row++;    
      printString(Integer.toString(ServerStatus.NativeUpdatePeriodSecs), column, row++, FrgndTxtColor);
      printString(Integer.toString(ServerStatus.NativeReportPeriodSecs), column, row++, FrgndTxtColor);
      printString(Long.toString(ServerStatus.NativeHeartbeatCount), column, row++, FrgndTxtColor);
      printString(Integer.toString(ServerStatus.ServerUpdatePeriodSecs), column, row++, FrgndTxtColor);
      printString(Long.toString(ServerStatus.ServerHeartbeatCount), column, row++, FrgndTxtColor);
      printString(Integer.toString(ServerStatus.NativeEventTimeoutMsecs), column, row++, FrgndTxtColor);
      printString(Long.toString(ServerStatus.NativeEventCount), column, row++, FrgndTxtColor);
      row++;    
      printString(Integer.toString(ServerStatus.Server.getCurrent_Sessions().size()), column, row++, FrgndTxtColor);
      printString(Integer.toString(ServerStatus.Server.getHistorical_Sessions().size()), column, row++, FrgndTxtColor);
      printString(Integer.toString(ServerStatus.MaxParentSessions), column, row++, FrgndTxtColor);
      printString(Integer.toString(ServerStatus.MaxChildSessions), column, row++, FrgndTxtColor);
    }
    else
    {
      logger.warning("The ServerStatus (remote) seems to be null");      
    }
    
    row+=2;
    if(this.ServiceUpdater == null)
      logger.warning("The ServiceUpdater is null");
    else
      Toolkit.printString(this.ServiceUpdater.getUpTime().toString(),       column, row, FrgndTxtColor);
    
    row+=2;
    if(ParentSessionStatus != null)
    {
      Toolkit.printString(ParentSessionStatus.getHost(),       column, row++, FrgndTxtColor);
      Toolkit.printString(Integer.toString(ParentSessionStatus.getPort()),       column, row++, FrgndTxtColor);
      Toolkit.printString(ParentSessionStatus.getSessionName(),       column, row++, FrgndTxtColor);
      Toolkit.printString(Long.toString(ParentSessionStatus.getThreadId()),       column, row++, FrgndTxtColor);
      Toolkit.printString(ParentSessionStatus.getUser(),       column, row++, FrgndTxtColor);
      Toolkit.printString(ParentSessionStatus.getOpenTime().toString(),       column, row++, FrgndTxtColor);
      if(this.ServiceUpdater == null)
        logger.warning("The ServiceUpdater is null");
      else
        Toolkit.printString(Boolean.toString(this.ServiceUpdater.isConnectionReused()),       column, row, FrgndTxtColor);
      row+=2;
      Toolkit.printString(ParentSessionStatus.getAuthenticator(),       column, row++, FrgndTxtColor);
      Toolkit.printString(ParentSessionStatus.getClientProtocol(),       column, row++, FrgndTxtColor);
    }
    else
    {
      logger.warning("The ParentSessionStatus seems to be null");      
    }
        
    row++;
    int timeToUpdate = 0;
    String updateRate = "unknown";
    if(ServiceUpdater != null)
    {
      // only happens if running from main, otherwise SmtConsoleManager
      updateRate = Long.toString(ServiceUpdater.getUpdatePeriod()) + " ";
      timeToUpdate = (int)(ServiceUpdater.getUpdatePeriod() - updateTimer);
    }
    else
    {
      // see if being updated by the SmtConsoleManager
      SmtConsoleManager man = SmtConsoleManager.getInstance();
      if(man != null)
      {
        updateRate = Long.toString(man.getUpdatePeriod());
        timeToUpdate = (int)(man.getUpdatePeriod() - updateTimer);
      }
    }
    Toolkit.printString(updateRate,                           column,   row, FrgndTxtColor);
    Toolkit.printString(String.format("(%2d)", timeToUpdate), column+4, row++, ErrorTxtColor);
    
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
      SmtConsoleScreen screen = new OpenSmMonitorServiceScreen();
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
