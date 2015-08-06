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
 *        file: TopPortErrorScreen.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command.console;

import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_UpdateProvider;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_UpdateProvider.UpdaterType;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Updater;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_Port.PortCounterName;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_PortChange;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;
import gov.llnl.lc.smt.command.SmtCommand;
import gov.llnl.lc.smt.command.top.TopAnalyzer;
import gov.llnl.lc.smt.filter.SmtFilter;
import gov.llnl.lc.smt.props.SmtProperty;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
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
public class TopPortErrorScreen extends SmtConsoleScreen
{
  // use a top analyzer to do the calculations
  TopAnalyzer TA = TopAnalyzer.getInstance();
  
  
  int lvlCol = 2;
  int idCol    = 30;
  int sCol     = ScreenCols - 54;
  int eCol     = ScreenCols - 54;
  
  public boolean paintForeground() throws Exception
  {
    paintBorderForeground();
    
    int maxNameLen = 16;
    int titleRow   = 4;
    int listRow    = 8;
    
    // data from the top analyzer is in order
    LinkedHashMap<String, PFM_PortChange> atp = TA.getTopPortChanges();
    LinkedHashMap<String, IB_Vertex>      avl = TA.getVertexMap();
    
    int num = TA.getNumberOfTop();
    if((atp != null) && !(atp.isEmpty()))
      num = atp.size();
    else
      num = 0;
     
    printTitleString(String.format("Top %2d Active Error Ports", num), titleRow);
    
    if((atp != null) && !(atp.isEmpty()))
    {
      printDataTimeStamp(TA.getAnalyzedTime(), titleRow, false);
       int count = 0;
       EnumSet<PortCounterName> sc = PortCounterName.PFM_SUPPRESS_COUNTERS;
          // show the counter names that are suppressed
         Map.Entry<String, PFM_PortChange> me = atp.entrySet().iterator().next();
         sc = me.getValue().getPort1().getSuppressed_Counters();
         String suppressed = "none";
         if(sc != null)
           suppressed = Arrays.asList(sc.toArray()).toString();
         
         Toolkit.printString(suppressed, sCol+12, 5, FrgndTxtColor);
         
         clearList(false);

       for (Map.Entry<String, PFM_PortChange> eMapEntry : atp.entrySet())
      {
         PFM_PortChange pc = eMapEntry.getValue();
         PFM_Port p = pc.getPort1();
         int depth    = -1;
         String name  = "";
         
         // get the edge and vertex that owns this port (need for missing data)
         IB_Vertex v = IB_Vertex.getVertex(IB_Vertex.getVertexKey(p.getNodeGuid()), avl);
         if(v != null)
         {
             depth    = v.getDepth();
             name  = v.getNode().sbnNode.description;
         }
         
        Toolkit.printString(Integer.toString(depth),                 lvlCol+1, listRow, FrgndTxtColor);
        Toolkit.printString(p.toPortIdString(name, maxNameLen),      lvlCol+3, listRow, FrgndTxtColor);
        Toolkit.printString(pc.toShortErrorString(),                 eCol, listRow, ErrorTxtColor);
       
        listRow++;
         if(++count >= TA.getNumberOfTop())
          break;
       }
     }
    else
    {
      if((atp != null) && (atp.isEmpty()))
      {
        // wipe the screen clear, display an empty message - no errors!
        logger.warning("The TopPortErrors seems to be empty");
        clearList(true);
      }
      logger.warning("The TopPortErrors seems to be null");      
    }
    return true;
  }
  
  public boolean paintBackground() throws Exception
  {
    paintBorderBackground();
    // static graphics and text
    int column = 1;
    int row    = 4;
    
    printTitleString(String.format("Top %2d Active Error Ports", TA.getNumberOfTop()), row++);
    Toolkit.printString("suppressed:",       sCol,    row++, BkgndTxtColor);
    Toolkit.drawHorizontalLine(              column,  row, ScreenCols-3, ErrorTxtColor);
    Toolkit.printString("level",             lvlCol,  row, BkgndTxtColor);
    Toolkit.printString("identity",          idCol,   row, BkgndTxtColor);
    Toolkit.printString("delta errs/period", eCol+16, row, BkgndTxtColor);

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
      SmtConsoleScreen screen = new TopPortErrorScreen();
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
      TA.setMode(numTop, SmtProperty.SMT_PORT_ERRORS, false, cmd.getFilter());
    else
      TA.setMode(numTop, SmtProperty.SMT_PORT_ERRORS, false, new SmtFilter());

   
    // I have a new Fabric, so calculate data
    TA.osmFabricUpdate(Fabric);   
    if(TA.isReadyToAnalyze())
      TA.analyzeOnce();
    
    clearList = true;
     
    return status;
  }


}
