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
 *        file: SmtConstants.java
 *
 *  Created on: Jan 11, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt;

import gov.llnl.lc.util.SystemConstants;

/**********************************************************************
 * Describe purpose and responsibility of SmtConstants
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Jan 11, 2013 2:31:01 PM
 **********************************************************************/
public interface SmtConstants extends SystemConstants
{
  static final String COPYRIGHT =  "Copyright (C) 2015, Lawrence Livermore National Security, LLC";
  static final long PERF_MGR_STALE_PERIOD =  1200L; // definitely stale if older than 20 minutes
  
  static final long PACKET_SIZE =      512;
  static final long KILOBYTES =       1024;
  static final long MEGABYTES =    1048576;
  static final long GIGABYTES = 1073741824;
  
  static final String SMT_DEFAULT_DIR = System.getProperty("user.home") + System.getProperty("file.separator") + ".smt" + System.getProperty("file.separator");
  static final String SMT_CACHE_DIR   = "cache" + System.getProperty("file.separator");
  static final String SMT_NO_FILE     = "NO FILE";

/********************** common html tags ********************************/  
public static final String XX_SMALL_FONT = "<font size=\"0\">";
public static final String X_SMALL_FONT  = "<font size=\"1\">";
public static final String SMALL_FONT    = "<font size=\"2\">";
public static final String MEDIUM_FONT   = "<font size=\"3\">";
public static final String LARGE_FONT    = "<font size=\"4\">";
public static final String X_LARGE_FONT  = "<font size=\"5\">";
public static final String XX_LARGE_FONT = "<font size=\"6\">";

public static final String RED_FONT        = "<font  color=\"red\">";
public static final String BLUE_FONT       = "<font  color=\"blue\">";
public static final String GREEN_FONT      = "<font  color=\"green\">";
public static final String BLACK_FONT      = "<font  color=\"black\">";
public static final String WHITE_FONT      = "<font  color=\"white\">";
public static final String ORANGE_FONT     = "<font  color=\"#FFA000\">";
public static final String YELLOW_FONT     = "<font  color=\"yellow\">";
public static final String NAVY_FONT       = "<font  color=\"navy\">";
public static final String LIME_FONT       = "<font  color=\"lime\">";
public static final String PURPLE_FONT     = "<font  color=\"purple\">";
public static final String TEAL_FONT       = "<font  color=\"teal\">";
public static final String BROWN_FONT      = "<font  color=\"#A02820\">";
public static final String OLIVE_FONT      = "<font  color=\"olive\">";
public static final String FUCHSIA_FONT    = "<font  color=\"fuchsia\">";
public static final String MAROON_FONT     = "<font  color=\"maroon\">";
public static final String VIOLET_FONT     = "<font  color=\"#F080F0\">";
public static final String CHARTREUSE_FONT = "<font  color=\"#80FF00\">";
public static final String SILVER_FONT     = "<font  color=\"silver\">";
public static final String AQUA_FONT       = "<font  color=\"aqua\">";


public static final String H_LINE     = "<hr>";
public static final String H_LINE_FAT = "<hr size=6 width=50%>";
public static final String LINE_BREAK      = "<br>";
public static final String PARAGRAPH_BREAK = "<p>";

public static final String NO_FORMAT_START = "<pre>";
public static final String NO_FORMAT_END   = "</pre>";

public static final String INDENT_START = "<blockquote>";
public static final String INDENT_END   = "</blockquote>";

public static final String CENTER_START = "<div align=\"center\">";
public static final String CENTER_END = "</div>";

public static final String LEFT_START = "<div align=\"left\">";
public static final String LEFT_END = "</div>";

public static final String RIGHT_START = "<div align=\"right\">";
public static final String RIGNT_END = "</div>";

public static final String BOLD_START = "<strong>";
public static final String BOLD_END = "</strong>";

public static final String SPACE = "&nbsp;";

public static final String END_FONT      = "</font>";
}
