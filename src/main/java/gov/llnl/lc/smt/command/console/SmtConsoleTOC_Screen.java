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
 *        file: SmtConsoleTOC_Screen.java
 *
 *  Created on: Mar 22, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command.console;

import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import jcurses.system.InputChar;
import jcurses.system.Toolkit;

/**********************************************************************
 * Describe purpose and responsibility of SmtConsoleTOC_Screen
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Mar 22, 2013 8:28:07 AM
 **********************************************************************/
public class SmtConsoleTOC_Screen extends SmtConsoleScreen
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
    // the static part
    int kCol = 4;         // Key Column
    int sCol = kCol + 3;  // dash Column
    int nCol = sCol + 3;  // Name Column
    int dCol = nCol + 23; // Description Column
    int row    = 5;
    
    printTitleString("SMT Console Table of Contents", row++);
    row++;

    for(SmtScreenType s : SmtScreenType.SCN_SCREENS)
    {
      Toolkit.printString(s.getKeyString(), kCol, row, BorderBTxtColor);
      Toolkit.printString(" - ", sCol, row, FrgndTxtColor);
      Toolkit.printString(s.getScreenName() + ":", nCol, row, FrgndTxtColor);
      Toolkit.printString(s.getDescription(), dCol, row++, FrgndTxtColor);
    }
    row++;
    Toolkit.printString("aF11", kCol-1, row, BorderBTxtColor);
    Toolkit.printString(" - ", sCol, row, FrgndTxtColor);
    Toolkit.printString("Refresh:", nCol, row, FrgndTxtColor);
    Toolkit.printString("Refresh the Current Screen (clear junk)", dCol, row++, FrgndTxtColor);
    Toolkit.printString("Esc", kCol, row, ErrorTxtColor);
    Toolkit.printString(" - ", sCol, row, FrgndTxtColor);
    Toolkit.printString("Quit:", nCol, row, FrgndTxtColor);
    Toolkit.printString("Gracefully exit the program", dCol, row++, FrgndTxtColor);
    return false;
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
    // the dynamic or changing part
    
    return false;
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
      SmtConsoleScreen screen = new SmtConsoleTOC_Screen();
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
