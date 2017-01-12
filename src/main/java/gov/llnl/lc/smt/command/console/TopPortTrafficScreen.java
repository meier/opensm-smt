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
 *        file: TopPortTrafficScreen.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command.console;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_UpdateProvider;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_UpdateProvider.UpdaterType;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Updater;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_PortChange;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_PortRate;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Edge;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;
import gov.llnl.lc.smt.command.SmtCommand;
import gov.llnl.lc.smt.command.top.TopAnalyzer;
import gov.llnl.lc.smt.filter.SmtFilter;
import gov.llnl.lc.smt.props.SmtProperty;
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
public class TopPortTrafficScreen extends SmtConsoleScreen
{
  // use a top analyzer to do the calculations
  TopAnalyzer TA = TopAnalyzer.getInstance();
  
  
  int lvlCol = 2;
  int idCol    = 30;
  int rCol     = ScreenCols - 13;
  int xCol     = rCol - 13;
  
  public boolean paintForeground() throws Exception
  {
    paintBorderForeground();
    
    int row    = 8;
    int maxNameLen = 24;
    int titleRow   = 4;
    int listRow    = 8;
    
    // data from the top analyzer is in order
    LinkedHashMap<String, PFM_PortChange> atp = TA.getTopPortChanges();
    LinkedHashMap<String, IB_Edge>        atl = TA.getEdgeMap();
    
    if(atp != null)
    {
      printDataTimeStamp(TA.getAnalyzedTime(), titleRow, false);
       int count = 0;
       for (Map.Entry<String, PFM_PortChange> eMapEntry : atp.entrySet())
      {
         PFM_PortChange pc = eMapEntry.getValue();
         PFM_Port p = pc.getPort1();
         int depth    = -1;
         String name  = "";
         
         // get the edge and vertex that owns this port (need for missing data)
         IB_Edge e    = IB_Edge.getEdge( p, atl);
         if(e != null)
         {
           // fill in the missing data
           IB_Vertex v  = e.getEndpoint1().hasPort(p)? e.getEndpoint1(): e.getEndpoint2();
           if(v != null)
           {
             depth    = v.getDepth();
             name  = v.getNode().sbnNode.description;
           }
         }
         
        Toolkit.printString(Integer.toString(depth),                 lvlCol+1, row, FrgndTxtColor);
        Toolkit.printString(p.toPortIdString(name, maxNameLen),      lvlCol+3, row, FrgndTxtColor);
        Toolkit.printString(PFM_PortRate.toTransmitRateMBString(pc),   xCol+3, row, FrgndTxtColor);
        Toolkit.printString(PFM_PortRate.toReceiveRateMBString(pc),    rCol+3, row, FrgndTxtColor);
        
        row++;
         if(++count >= TA.getNumberOfTop())
          break;
       }
    }
    else
    {
      logger.warning("The TopLinks seems to be null");      
    }
    return true;
  }
  
  public boolean paintBackground() throws Exception
  {
    paintBorderBackground();
    // static graphics and text
    int column = 1;
    int row    = 4;
    
    printTitleString("Top " + Integer.toString(TA.getNumberOfTop()) +" Active Traffic Ports", row++);
    row++;
    Toolkit.drawHorizontalLine(column, row, ScreenCols-3, ErrorTxtColor);
    Toolkit.printString("level",       lvlCol, row, BkgndTxtColor);
    Toolkit.printString("identity",    idCol,  row, BkgndTxtColor);
    Toolkit.printString("xmit MB/sec", xCol,   row, BkgndTxtColor);
    Toolkit.printString("rcv MB/sec",  rCol,   row, BkgndTxtColor);

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
      SmtConsoleScreen screen = new TopPortTrafficScreen();
      // start the service
      screen.setServiceUpdater(uService, true);  // this will add itself to the Services listener list, and start the service if its not already running
      TimeUnit.SECONDS.sleep(5);           // wait a little, to allow the screen to be updated with fresh data
      
     // the screen should now have data
      screen.paintBackground();
      screen.paintForeground();
      TimeUnit.SECONDS.sleep(200);           // wait a little, to allow the screen to be updated with fresh data
      screen.paintBackground();
      screen.paintForeground();
      TimeUnit.SECONDS.sleep(200);           // wait a little, to allow the screen to be updated with fresh data
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
  /************************************************************
   * Method Name:
   *  updateForegroundData
  **/
  /**
   * Any screen than needs to do substantial analysis on the Service
   * or Fabric data MUST override this method and do it here to
   * create derived data.  Since the Service and Fabric are being
   * constantly (asynchronously) updated, they can change while
   * analysis is occurring.  You can either make a private copy, or
   * synchronize access to the object.  This method is invoked
   * within the synchronized block that gets the new Service and
   * Fabric, so guarantees atomic objects.
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
    int numTop = ScreenRows - 12;
    
    SmtCommand cmd = SmtConsoleManager.getInstance().getParentSmtCommand();
    if(cmd != null)
      TA.setMode(numTop, SmtProperty.SMT_PORT_TRAFFIC, false, cmd.getFilter());
    else
      TA.setMode(numTop, SmtProperty.SMT_PORT_TRAFFIC, false, new SmtFilter());

   
    // I have a new Fabric, so calculate data
    TA.osmFabricUpdate(Fabric);   
    if(TA.isReadyToAnalyze())
      TA.analyzeOnce();
     
    return status;
  }


}
