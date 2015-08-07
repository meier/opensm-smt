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
 *        file: SmtIconType.java
 *
 *  Created on: Jan 16, 2014
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.data;

import gov.llnl.lc.infiniband.opensm.plugin.gui.tree.SMTUserObjectTreeCellRenderer;
import gov.llnl.lc.smt.props.SmtProperties;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

/**********************************************************************
 * Describe purpose and responsibility of SmtIconType
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Jan 16, 2014 8:30:31 AM
 **********************************************************************/
public enum SmtIconType
{
  SMT_FABRIC_ICON(        202, "fabric",                 "chart_organisation.png",       "fabric",                 "a normal fabric"),
  SMT_FABRIC_DERR_ICON(   202, "fabric-dynamic-err",     "chart_organisation_error.png", "dynamic fabric error",   "errors are occuring on this fabric now"),
  SMT_LEAF_ICON(          200, "leaf",                   "leaf.png",                     "leaf",                   "a leaf node in the fabric"),
  SMT_LEAF_ERR_ICON(      201, "leaf-error",             "leaf_error.png",               "leaf with error",        "a leaf node with an error"),
  SMT_LEAF_TOP_ICON(      200, "leaf-top",                   "leaf_top.png",                 "top leaf",               "a top leaf node in the fabric"),
  SMT_LEAF_BOTH_ICON(     201, "leaf-top-error",             "leaf_both.png",                "top leaf with error",    "a top leaf node with an error"),
  SMT_SWITCH_ICON(          200, "switch",                   "switch.png",                     "leaf",                   "a leaf node in the fabric"),
  SMT_SWITCH_ERR_ICON(      201, "switch-error",             "switch_error.png",               "leaf with error",        "a leaf node with an error"),
  SMT_SWITCH_TOP_ICON(      200, "switch-top",                   "switch_top.png",                 "top leaf",               "a top leaf node in the fabric"),
  SMT_SWITCH_BOTH_ICON(     201, "switch-top-error",             "switch_both.png",                "top leaf with error",    "a top leaf node with an error"),
  SMT_LINK_ICON(          202, "link",                   "link.png",                     "link",                   "a normal link between two ports"),
  SMT_LINK_TOP_ICON(      202, "link-top",               "link_top.png",                 "top link",               "one of the most active links"),
  SMT_LINK_ERR_ICON(      202, "link-error",             "link_error.png",               "link error",             "this link has experienced errors"),
  SMT_LINK_DERR_ICON(     202, "link-dynamic-err",       "link_derror.png",              "dynamic link error",     "errors are occuring on this link now"),
  SMT_LINK_BOTH_ICON(    202, "link-both",             "link_both.png",               "top link with error",            "a normal link between two ports"),
  SMT_LINK_BREAK_ICON(    202, "link-break",             "link_break.png",               "broken link",            "a normal link between two ports"),
  SMT_PORT_ICON(          202, "port",                   "bullet_black.png",                     "link",                   "a normal link between two ports"),
  SMT_PORT_TOP_ICON(          202, "port",                   "bullet_blue.png",                     "link",                   "a normal link between two ports"),
  SMT_PORT_ERR_ICON(          202, "port",                   "bullet_yellow.png",                     "link",                   "a normal link between two ports"),
  SMT_PORT_DERR_ICON(          202, "port",                   "bullet_red.png",                     "link",                   "a normal link between two ports"),
  SMT_PORT_BOTH_ICON(          202, "port",                   "bullet_purple.png",                     "link",                   "a normal link between two ports"),
  SMT_PORT_DOWN_ICON(          202, "down-port",              "link_break.png",                     "unlinked port",                   "a port that is not part of a link, probably down"),
  SMT_COUNTER_ICON(       202, "counter",                "counters.png",           "prfmgr counter",         "a counter value maintained by the performance cuonter"),
  SMT_COUNTER_ERR_ICON(   202, "counter-error",          "counters_error.png",                 "a counter has an error", "a counter has an error"),
  SMT_COUNTER_DERR_ICON(  202, "counter-dynamic-error",  "counters_derror.png",                   "a dynamic error",        "a counter has an active or dynamic error"),
  SMT_COUNTER_TOP_ICON(  202, "counter-top",  "counters_top.png",                   "a dynamic error",        "a counter has an active or dynamic error"),
  SMT_COUNTER_BOTH_ICON(  202, "counter-both",  "counters_both.png",                   "a dynamic error",        "a counter has an active or dynamic error"),
  SMT_CNT_VAL_ICON(       202, "counter-value",                "CounterDefault.png",           "specific prfmgr counter",         "a counter value maintained by the performance cuonter"),
  SMT_CNT_VAL_DERR_ICON(       202, "dynamic-error",                "exclamation.png",           "prfmgr counter",         "a counter value maintained by the performance cuonter"),
  SMT_CNT_VAL_ERR_ICON(       202, "static-error",                "error.png",           "prfmgr counter",         "a counter value maintained by the performance cuonter"),
  SMT_CNT_VAL_TOP_ICON(       202, "top-traffic",                "flag_blue.png",           "prfmgr counter",         "a counter value maintained by the performance cuonter"),
  SMT_INFORMATION_ICON(       202, "information",                "information.png",           "information",         "a counter value maintained by the performance cuonter"),
  SMT_MORE_ICON(       202, "more",                "add.png",           "more",         "extra, more, or additional information"),
  SMT_HELP_ICON(       202, "help",                "help.png",           "help",         "a counter value maintained by the performance cuonter"),
  SMT_SEARCH_ICON(       202, "search",                "SearchSmall.png",           "search",         "a magnifying glass"),
  SMT_FINAL_ICON(         400, "end",                    "bullet_black.png",             "end",                    "the last icon");

  /*
   *   This enum needs to change to something that supports commnand line options, such as
   *   int, IconName, shortName, longName, Description, ArgName
   */
  public static final EnumSet<SmtIconType> SMT_ALL_ICONS = EnumSet.allOf(SmtIconType.class);
  
  private static final Map<Integer,SmtIconType> lookup = new HashMap<Integer,SmtIconType>();

  static 
  {
    for(SmtIconType s : SMT_ALL_ICONS)
         lookup.put(s.getIconNum(), s);
  }

  private int IconNum;
  
  // the simple name of the icon
  private String IconName;
  
  // the name of the icon file, within the image directory
  private String FileName;
  
  // the actual Icon, constructed from the image file
  private ImageIcon Icon;
  
  // a short description of the icon, for use in a legend
  private String ShortDescription;

  // a description of the icon, describing its full meaning
  private String Description;

  private SmtIconType(int IconNum, String IconName, String FileName, String ShortDescription, String Description)
  {
      String imgDir = new SmtProperties().getImageDir(false);
      this.IconNum     = IconNum;
      this.IconName = IconName;
      this.FileName         = FileName;
      this.ShortDescription    = ShortDescription;
      this.Description  = Description;
      this.Icon   = new ImageIcon(SMTUserObjectTreeCellRenderer.class.getResource(imgDir + FileName));
  }

public int getIconNum()
{
  return IconNum;
  }

public ImageIcon getIcon()
{
  return Icon;
  }

public String getIconName()
{
  return IconName;
  }

public String getFileName()
{
  return FileName;
  }

public static SmtIconType get(int IconNum)
{ 
    return lookup.get(IconNum); 
}

public static SmtIconType getByIconName(String IconName)
{
  SmtIconType p = null;
  
  // return the first property with an exact name match
  for(SmtIconType s : SMT_ALL_ICONS)
  {
    if(s.getIconName().equals(IconName))
      return s;
  }
  return p;
}

/************************************************************
 * Method Name:
 *  getShortName
 **/
/**
 * Returns the value of shortName
 *
 * @return the shortName
 *
 ***********************************************************/

public String getShortDescription()
{
  return ShortDescription;
}

/************************************************************
 * Method Name:
 *  getDescription
 **/
/**
 * Returns the value of description
 *
 * @return the description
 *
 ***********************************************************/

public String getDescription()
{
  return Description;
}
}
