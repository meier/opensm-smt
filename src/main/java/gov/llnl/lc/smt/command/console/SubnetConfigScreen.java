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
 *        file: SubnetConfigScreen.java
 *
 *  Created on: Mar 21, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command.console;

import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;

import java.util.HashMap;
import java.util.Map;

import jcurses.system.InputChar;
import jcurses.system.Toolkit;

/**********************************************************************
 * Describe purpose and responsibility of SubnetConfigScreen
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Mar 21, 2013 3:34:21 PM
 **********************************************************************/
public class SubnetConfigScreen extends SmtConsoleScreen
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
    String title = Subnet == null ? "configuration" : Subnet.Options.config_file;
    
    printTitleString("OpenSM Configuration", row++);

    Toolkit.drawHorizontalLine(column, row, 30, ErrorTxtColor);
    Toolkit.printString(title, column+2, row++, BkgndTxtColor);
    
    // see forgreound for remainder
    return true;
  }

  private int  paintOptionMap(HashMap<String, String> map, int row, int col)
  {
//    int midPoint = 46;
    int midPoint = (ScreenCols-col)/2;
    int maxKeySize = midPoint/2;
    int column = col;
    String key;
    int endOfLine;
    
// the maximum "name" length is "maxKeySize"
// from the beginning of the line (supplied col value), if key plus "value" lenght
    //    is greater than "midPoint" then the NVP gets the whole line
    //    otherwise start a new NVP at "midPoint" if the next one will fit.
    // (if doesn't fit, start a new line)
    //  assume num columns 100
    for (Map.Entry<String, String> entry: map.entrySet())
    {
      // check to see if this key/value pair will fit on remainder of line
      endOfLine = column + maxKeySize + entry.getValue().length();
      if((endOfLine > (ScreenCols -(col + 2))) && (column != col))
      {
        column = col;
        row++;
      }
      
      // truncate the name (key) if necessary
      key = entry.getKey().length()< maxKeySize ? entry.getKey() : entry.getKey().substring(0, maxKeySize);
      Toolkit.printString(key + ":",  column, row, BkgndTxtColor);
      column += maxKeySize + col + 1;
      Toolkit.printString(entry.getValue(), column, row, FrgndTxtColor);
      
      // setup the row and column for the next, max two per line
      if((column + entry.getValue().length()) > midPoint)
      {
        column = col;
        row++;
      }
      else
      {
        column = midPoint;
      }
      // increment the row if column is over halfway
    }
    return row;
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
    int column = 1;
    int row    = 6;
    
    /* keep in order, top down.  See row value increment */

    if(Subnet != null)
    {
      row = paintOptionMap(OptionsMap, row, column);
    }
    else
    {
      logger.warning("The Subnet seems to be null");      
    }
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
    OpenSmMonitorService service = OpenSmMonitorService.getOpenSmMonitorService("localhost", "10011"); 
    if(service != null)
    {
      SmtConsoleScreen screen = new SubnetConfigScreen();
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
