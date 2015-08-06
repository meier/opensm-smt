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
 *        file: SmtConsoleScreen.java
 *
 *  Created on: Mar 21, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command.console;

import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Updater;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Stats;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Subnet;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_SysInfo;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.infiniband.opensm.plugin.net.OsmServerStatus;
import gov.llnl.lc.net.ObjectSession;
import gov.llnl.lc.time.TimeStamp;

import java.util.HashMap;

import jcurses.system.CharColor;
import jcurses.system.InputChar;
import jcurses.system.Toolkit;
import jcurses.util.Rectangle;

/**********************************************************************
 * Describe purpose and responsibility of SmtConsoleScreen
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Mar 21, 2013 2:49:53 PM
 **********************************************************************/
public abstract class SmtConsoleScreen implements ConsoleScreen
{
  OMS_Updater     ServiceUpdater       = null;  // this screen may be updated by an OMS service
  ObjectSession   ParentSessionStatus  = null;
  OsmServerStatus ServerStatus         = null;
  OSM_Fabric      Fabric               = null;
  
  OSM_SysInfo SysInfo = null;
  OSM_Stats Stats     = null;
  OSM_Subnet Subnet   = null;
  HashMap<String, String> OptionsMap = null;
  
  /** the synchronization object (used primarily to ensure atomic Fabric operations) **/
  protected static Boolean semaphore            = new Boolean( true );
    
  private static SmtScreenType startFooterScreen = SmtScreenType.SCN_CONTENTS;
  private boolean repaintScreen = false;
  private boolean useCurrentTime = false;  // by default (false), show the timestamp from the fabric data
  protected boolean clearList = false;
  
  protected volatile static long timeLoopCounter = 0;
  protected volatile static int updateTimer = 0;

  SmtScreenType ScreenType = null;

  
  TimeStamp CurrentTime = new TimeStamp();
//  long refreshPeriod   = 30;

  int printCenteredString(String string, int row, CharColor color)
  {
    // Centered with title color
    return printColoredString(string, (ScreenCols - string.length())/2, row, color);
  }
  
  int printColoredString(String string, int col, int row, CharColor color)
  {
    // at specified coordinates with specified color
    Toolkit.printString(string, col, row, color);
    return col + string.length();
  }
  
  int printTitleString(String title, int row)
  {
    // Centered with title color
    return printCenteredString(title, row,  ScrnTitleColor);
  }
  
  void printString(String text, int x, int y, CharColor color)
  {
    // forground printing that automatically pads at the end
    // only usefull for test that CHANGES LENGTH
    Toolkit.printString(text + "     ", x, y, color);
  }
  
  int printDataTimeStamp(TimeStamp timestamp, int row, boolean showStaleState)
  {
    Toolkit.printString("as of:",                        ScreenCols -28, row, BkgndTxtColor);
    Toolkit.printString(timestamp.toString(), ScreenCols -21, row, FrgndTxtColor);
    
    if(showStaleState)
    {
    // is this stale, and if so print it out
    row++;
    int staleCol = 36;
    boolean stale = (Fabric == null) ? true : Fabric.isStale();
    if(stale)
    {
      Toolkit.printString("(counter values are stale)",  staleCol, row, ErrorTxtColor);
    }
    else
    {
      Toolkit.printString("                          ",  staleCol, row, FrgndTxtColor);
    }
    }
    return row;
  }
  
  public boolean useCurrentTime(boolean useCurrent)
  {
    // if true, display the current time of day
    // if false, display the time from the data set
    return useCurrentTime = useCurrent;
  }
  
  public boolean clearScreen()
  {
    Toolkit.clearScreen(Scrn0BTxtColor);
    return true;
  }
  
  boolean clearList(boolean forceClear, int startRow, int numRows)
  {
    int row = startRow;
    if(clearList || forceClear)
    {
       String format = "%" + (ScreenCols -4) + "s";
      for(int i = 0; i < numRows; i++)
        Toolkit.printString(String.format(format, " "), 2, row++, FrgndTxtColor);
      
      clearList = false;
    }
    return clearList;
  }
  
  boolean clearList(boolean forceClear)
  {
    return clearList(forceClear, 8, 38);
  }

  public boolean getRepaint()
  {
    return repaintScreen;
  }

  public boolean refreshScreen(boolean clearFirst) throws Exception
  {
    if(clearFirst)
      clearScreen();
    paintBackground();
    return paintForeground();
  }

  public  boolean paintBorderForeground() throws Exception
  {
    // data from the service for this screen
    if(SysInfo != null)
    {
      Toolkit.printString(SysInfo.OpenSM_Version, 1, 1, BorderFTxtColor);
      Toolkit.printString(SysInfo.OsmJpi_Version, 1, 2, BorderFTxtColor);
    }
    if(CurrentTime != null)
      Toolkit.printString(CurrentTime.toString(), ScreenCols -21, 1, ScrnHeaderColor);
    if(ServerStatus != null)
    {
      printCenteredString(ServerStatus.Server.getHost(), 0, ScrnHeaderColor);
      Toolkit.printString(ServerStatus.Server.getStartTime().toString(), ScreenCols -21, 2, BorderFTxtColor);
    }
      return true;
  }
  
  public boolean paintBorderBackground() throws Exception
  {
    // static graphics and text
    Rectangle rect1 = new Rectangle(0,0,ScreenCols,ScreenRows);
    Toolkit.drawBorder(rect1, new CharColor(CharColor.WHITE, CharColor.RED));  // outline
    Toolkit.drawHorizontalLine(1, 3, ScreenCols -2, ErrorTxtColor);
    Toolkit.drawHorizontalLine(1, ScreenRows-3, ScreenCols -2, ErrorTxtColor);
    // top area
    Toolkit.printString("up since:", ScreenCols -31, 2, BorderBTxtColor);
    Toolkit.printString("current:", ScreenCols -30, 1, BorderBTxtColor);
    
    // bottom area
    return paintBorderFooter();
  }
  public boolean nextBorderFooter() throws Exception
  {
    // advance the initial shortcut by a specified number of screens
    startFooterScreen = SmtScreenType.getNext(startFooterScreen);
    return true;
  }
  
  /************************************************************
   * Method Name:
   *  paintBorderFooter
  **/
  /**
   * Generate and paint a single line of information at the bottom
   * in the form of a "menu cheat sheet", showing the keyboard
   * short-cuts.  Mostly these allow individual screens to be
   * randomly accessed.
   * 
   * The SmtScreenType is used to construct this line.  If there
   * are more screens than can be displayed on a single line, then
   * additional shortcuts can be accessed via the nextBorderFooter()
   * method.
   * Only 8 screens can be displayed at a time, and the first short-cut
   * is always the Esc key.
   *
   * @see     describe related java objects
   *
   * @return
   * @throws Exception
   ***********************************************************/
  public boolean paintBorderFooter() throws Exception
  {
    int row=ScreenRows-2;
    int column=1;
    
    int keySpace   = 28;
    int labelSpace = 48;
    int numKeys    = 9;
    int maxMenu    = 7;
    int colPad = (ScreenCols - (keySpace+labelSpace))/(numKeys-1);
    if(colPad < 1)
      colPad = 1;
    
    SmtScreenType s1 = SmtScreenType.SCN_QUIT;
    Toolkit.printString(BLANKLINE, column, row, Scrn0BTxtColor);
    Toolkit.printString(s1.getKeyString()+"-", column, row, BorderBTxtColor);
    Toolkit.printString(s1.getShortScreenName(), column+=4, row, ErrorTxtColor);
    column+=3;
    
    // iterate through the screen types, maximum of 7
    SmtScreenType s = startFooterScreen;
    
    for(int i = 0; i < maxMenu; i++)
    {
      Toolkit.printString(s.getKeyString()+"-", column+=colPad, row, BorderBTxtColor);
      column = printColoredString(s.getShortScreenName(), column+=3, row, ScrnHeaderColor);
      
      s = SmtScreenType.getNext(s);
      
      // don't repeat the first one
      if(s == startFooterScreen)
      {
        break;
      }
    }
    Toolkit.printString("...", column+=colPad, row, BorderBTxtColor);
    return true;
  }
  
  public boolean paintBorderFooterOld() throws Exception
  {
    int row=ScreenRows-2;
    int column=1;
    
    int keySpace   = 28;
    int labelSpace = 48;
    int numKeys    = 9;
    int colPad = (ScreenCols - (keySpace+labelSpace))/(numKeys-1);
    if(colPad < 1)
      colPad = 1;
    
    Toolkit.printString("Esc-", column, row, BorderBTxtColor);
    Toolkit.printString("quit", column+=4, row, ErrorTxtColor);
    
    Toolkit.printString("F2-", column+=5, row, BorderBTxtColor);
    column = printColoredString("Status", column+=3, row, ScrnHeaderColor);
    
    Toolkit.printString("F3-", column+=colPad, row, BorderBTxtColor);
    column = printColoredString("Nodes", column+=3, row, ScrnHeaderColor);
    
    Toolkit.printString("F4-", column+=colPad, row, BorderBTxtColor);
    column = printColoredString("Ports", column+=3, row, ScrnHeaderColor);
    
    Toolkit.printString("F5-", column+=colPad, row, BorderBTxtColor);
    column = printColoredString("PerfMgr", column+=3, row, ScrnHeaderColor);
    
    Toolkit.printString("F6-", column+=colPad, row, BorderBTxtColor);
    column = printColoredString("Links", column+=3, row, ScrnHeaderColor);
    
    Toolkit.printString("F7-", column+=colPad, row, BorderBTxtColor);
    column = printColoredString("Events", column+=3, row, ScrnHeaderColor);
    
    Toolkit.printString("F8-", column+=colPad, row, BorderBTxtColor);
    column = printColoredString("Config", column+=3, row, ScrnHeaderColor);
    
    Toolkit.printString("F9-", column+=colPad, row, BorderBTxtColor);
    column = printColoredString("Srvc", column+=3, row, ScrnHeaderColor);
    return true;
  }
  

  /************************************************************
   * Method Name:
   *  setServiceUpdater
  **/
  /**
   * If set, will add this screen to the updaters listener list
   * and if this is the first listener for the service, the
   * service will start.  The frequency or period of updates
   * must be set through the updater.
   *
   * @see     describe related java objects
   *
   * @param serviceUpdater
   ***********************************************************/
  
  
  /************************************************************
   * Method Name:
   *  setServiceUpdater
  **/
  /**
   * Sets the screens OMS_Updater instance.  It conditionally adds
   * this screen to the Updaters listener list, so that this
   * screen gets its <code>osmServiceUpdate()</code> method called.
   * 
   *
   * @see     main
   *
   * @param serviceUpdater
   * @param add
   ***********************************************************/
  public void setServiceUpdater(OMS_Updater serviceUpdater, boolean add)
  {
    ServiceUpdater = serviceUpdater;
    // automatically add myself to the list
    if((ServiceUpdater != null) && add)
      ServiceUpdater.addListener(this);
  }

  @Override
  public void osmServiceUpdate(OMS_Updater updater, OpenSmMonitorService osmService) throws Exception
  {
    /** this needs to be atomic, don't let any other screen operation use the data until we are done **/
    synchronized (SmtConsoleScreen.semaphore)
    {
      if (osmService != null)
      {
        if(osmService.getParentSessionStatus() != null)
          ParentSessionStatus = osmService.getParentSessionStatus();
        if(osmService.getRemoteServerStatus() != null)
          ServerStatus = osmService.getRemoteServerStatus();
        if(osmService.getFabric() != null)
        {
          Fabric = osmService.getFabric();
          osmFabricUpdate(Fabric);          
          logger.info("Time from service is: " + Fabric.getTimeStamp().toString());
        }
      }
    }
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
    return status;
  }

  /************************************************************
   * Method Name:
   *  osmFabricUpdate
   **/
  /**
   * Describe the method here
   *
   * @see gov.llnl.lc.infiniband.opensm.plugin.data.OSM_FabricChangeListener#osmFabricUpdate(gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric)
   *
   * @param osmFabric
   * @throws Exception
   ***********************************************************/

  public synchronized void osmFabricUpdate(OSM_Fabric osmFabric) throws Exception
  {
      if(osmFabric != null)
      {
        Fabric      = osmFabric;
        SysInfo     = Fabric.getOsmSysInfo();
        Stats       = Fabric.getOsmStats();
        Subnet      = Fabric.getOsmSubnet();
        OptionsMap  = Fabric.getOptions();
        updateTimer = 0;
      }
      
      /* got new data, so update the foreground */
      try
      {
        updateForegroundData();
        paintForeground();
      }
      catch (Exception e)
      {
        logger.severe("Couldn't paint foreground for " + this.getClass().getSimpleName());
        e.printStackTrace();
      }

  }

  @Override
  public void timeUpdate(TimeStamp time)
  {
    // this should get called once per second
    updateTimer++;
    timeLoopCounter++;
    if((useCurrentTime) || (Fabric == null))
      CurrentTime = time;
    else
      CurrentTime = Fabric.getTimeStamp();
    
    try
    {
      paintForeground();  // anything that changes once per second
    }
    catch (Exception e)
    {
      logger.severe("Couldn't paint foreground for " + this.getClass().getSimpleName());
      e.printStackTrace();
    }    
  }
  /************************************************************
   * Method Name:
   *  defaultReadInput
  **/
  /**
   * Given the input from the keyboard, process it as if from a normal
   * screen, which only handles screen navigation and menu actions.
   * 
   * It also handles the normal termination methods.
   *
   * @see gov.llnl.lc.smt.command.console.ConsoleScreen#readInput()
   *
   * @return
   * @throws Exception
   ***********************************************************/
  public SmtScreenType defaultReadInput(InputChar c) throws Exception
  {
    SmtScreenType st = ScreenType;
    
    int code    = c.getCode();
    
    // HOME, and ?, should return the TOC page, or same as F2
    // END, ESC, q, and Q should end the program, or same as ESC
    // SPACE should advance the footer shortcut menu (circularly)
    // PageUp & PageDown should go to the next or previous screen
    // RightArrow & LeftArrow should go to the next or previous screen
    // j & J should go to the next screen
    // k & K should go to the previous screen
    
    
    // handle all special cases
    if(c.isSpecialCode() || code==27 || code==9 || code==10 || code==63 || code== 81 || code==113 || code==32 || code==106 || code==107 || code==74 || code==75)
    {
      // map these to QUIT, and then immediately return
      if(code==InputChar.KEY_END || code==27 || code==113 || code==81)
        return SmtScreenType.SCN_QUIT;
      
      // Map these to the TOC
      if(code==InputChar.KEY_HOME || code==63)
        code = InputChar.KEY_F2;
      
      // PageDown, ->, j, J should go to the next screen, or wrap
      if(code==InputChar.KEY_NPAGE || code==74 || code==106 || code==InputChar.KEY_RIGHT )
      {
        st = SmtScreenType.getNext(st);
        if(st != null)
          return st;
      }
      
      // PageUp, <-, k, K should go to the previous screen, or wrap
      if(code==InputChar.KEY_PPAGE || code==75 || code==107 || code==InputChar.KEY_LEFT )
      {
        st = SmtScreenType.getPrev(st);
        if(st != null)
          return st;
      }
      
      // handle the secret function keys
      if(code==323)
        repaintScreen = true;
      
      // handle the space key, don't change screens
      if(code==32)
        nextBorderFooter();
      
    // display the key hit, for future development
    logger.severe("The special character was: " + code);
    
    // change the screen based on the code  
    st = SmtScreenType.getByKeyCode(code);
    }
    else
      logger.severe("The normal character was: " + code);
      
    
    return st;
      
  }

  /************************************************************
   * Method Name:
   *  readInput
  **/
  /**
   * Read the keyboard input, and take action.  This is a BLOCKING
   * function.  Normally, a special character is typed, and needs
   * to be interpreted here.  The return code needs to be an instance
   * of the desired ScreenType, so it needs to remain the same or change
   * to a new screen.  If the special ESC key is pressed, then this
   * returns the QUIT type.
   *
   * @see gov.llnl.lc.smt.command.console.SmtConsoleScreen#defaultReadInput()
   * @see gov.llnl.lc.smt.command.console.ConsoleScreen#readInput()
   *
   * @return
   * @throws Exception
   ***********************************************************/
  @Override
  public SmtScreenType readInput() throws Exception
  {
    // wait for any key to be pressed
    InputChar c = Toolkit.readCharacter();
    
    return defaultReadInput( c);
  }

  /************************************************************
   * Method Name:
   *  main
   **/
  /**
   * This is used primarily for unit testing.
   *
   * @see     describe related java objects
   *
   * @param args
   ***********************************************************/
  public static void main(String[] args) throws Exception
  {
    Toolkit.init();
    Toolkit.clearScreen(BorderBTxtColor);
    OpenSmMonitorService service = OpenSmMonitorService.getOpenSmMonitorService("localhost", "10011"); 
    if(service != null)
    {
      SmtConsoleScreen screen = new SubnetStatusScreen();
      screen.osmServiceUpdate(null, service);
      screen.paintBackground();
      screen.paintForeground();
    }
    
    // wait for any key to be pressed
    InputChar c = Toolkit.readCharacter();
    Toolkit.clearScreen(BorderBTxtColor);
    Toolkit.shutdown();
  }

}
