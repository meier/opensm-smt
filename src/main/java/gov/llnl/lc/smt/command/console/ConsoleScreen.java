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
 *        file: ConsoleScreen.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command.console;

import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_ServiceChangeListener;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.time.TimeListener;
import jcurses.system.CharColor;

public interface ConsoleScreen extends OSM_ServiceChangeListener,  TimeListener, CommonLogger
{
  /* the geometry for the curses console screens */
  static final int ScreenRows = 50;
  static final int ScreenCols = 100;
 
  static final CharColor BorderBTxtColor =  new CharColor(CharColor.WHITE, CharColor.BLACK, CharColor.NORMAL, CharColor.NORMAL);
  static final CharColor BorderFTxtColor =  new CharColor(CharColor.WHITE, CharColor.BLACK, CharColor.BOLD, CharColor.BOLD);
  static final CharColor BkgndTxtColor   =  new CharColor(CharColor.WHITE, CharColor.BLACK, CharColor.NORMAL, CharColor.NORMAL);
  static final CharColor ScrnHeaderColor =  new CharColor(CharColor.WHITE, CharColor.BLUE, CharColor.BOLD, CharColor.BOLD);
  static final CharColor ScrnTitleColor  =  new CharColor(CharColor.WHITE, CharColor.MAGENTA, CharColor.BOLD, CharColor.BOLD);
  static final CharColor FrgndTxtColor   =  new CharColor(CharColor.WHITE, CharColor.BLUE, CharColor.NORMAL, CharColor.NORMAL);
  static final CharColor ErrorTxtColor   =  new CharColor(CharColor.WHITE, CharColor.RED, CharColor.NORMAL, CharColor.NORMAL);
  static final CharColor Scrn0BTxtColor  =  new CharColor(CharColor.WHITE, CharColor.BLACK, CharColor.NORMAL, CharColor.NORMAL);
  static final CharColor Scrn0FTxtColor  =  new CharColor(CharColor.WHITE, CharColor.BLACK, CharColor.BOLD, CharColor.BOLD);
  
  static final String BLANKLINE = "                                                                                                  ";    


  /* needs to be able to display the static (unchanging) background text */
  public boolean paintBackground() throws Exception;
  
  /* needs to be able to display the dynamic (changing) foreground text */
  public boolean paintForeground() throws Exception;
  
  /* return the "next" screen, or same if unchanged */ 
  public SmtScreenType readInput() throws Exception;
  
  /* returns true, if the screen should be FORCED to repaint */ 
  public boolean getRepaint();
  
  public boolean clearScreen();


}
