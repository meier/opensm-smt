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
 *        file: SmtAboutRecord.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command.about;

import gov.llnl.lc.smt.SmtConstants;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;

public class SmtAboutRecord
{
  private  HashMap<SmtAboutMap,String> AboutMap   = new HashMap<SmtAboutMap,String>();
  public   String RcdFormat = "%-10s: %s";
  private boolean LLNL = false;



  /************************************************************
   * Method Name:
   *  SmtAboutRecord
  **/
  /**
   * Build a record from a manifest.
   *
   * @see     describe related java objects
   *
   * @param rates
   ***********************************************************/
  public SmtAboutRecord(Manifest manifest)
  {
    super();
      Attributes attr1 = manifest.getMainAttributes();
      for(Object str: attr1.keySet())
      {
        if(str instanceof Name)
        {
          String str1 = ((Name)str).toString();
          SmtAboutMap m = SmtAboutMap.getByName(str1);
          if(m != null)
          {
            String val = attr1.getValue(str1);
            AboutMap.put(m, attr1.getValue(str1));
            if(val.startsWith("Lawrence Livermore"))
              LLNL = true;
          }
        }
      }
   }
  
  public HashMap<SmtAboutMap,String> getSmtAboutMaps()
  {
    return AboutMap;
  }
  
  public HashMap<SmtAboutType,String> getSmtAboutTypes()
  {
    HashMap<SmtAboutType,String> aMap = new HashMap<SmtAboutType,String>();
    
    // create a non-redundant (prefer last) list of just types
    for(SmtAboutMap m : SmtAboutMap.SMT_ALL_MAPPINGS)
    {
      SmtAboutType t = m.getAboutType();
      String val     = AboutMap.get(m);
      if(val != null)
         aMap.put(t, val);
     }
    return aMap;
  }
  
  public String toContent()
  {
    return toString(true);
  }

  /************************************************************
   * Method Name:
   *  toString
  **/
  /**
   * Describe the method here
   *
   * @see java.lang.Object#toString()
   *
   * @return
   ***********************************************************/
  
  public String toString(boolean html)
  {
    HashMap<SmtAboutType,String> aMap = getSmtAboutTypes();
    StringBuffer stringValue = new StringBuffer();
    String bstr = html ? "<b>": "";
    String bend = html ? "</b>": "";
    String cr   = html ? "<br>": "\n";
    
    // typically I want it in order of SmtAboutType
    for(SmtAboutType s : SmtAboutType.SMT_ALL_ABOUTS)
    {
      String val = aMap.get(s);
      if((val != null) && (val.length() > 1))
      {
        // there is something to print
        //      if this is a name, do it in blue
        //      a date should be green
        //      a version should be red
        String val1 = (bstr+val+bend);
        
        if(html && LLNL && (s.getName().startsWith(SmtAboutType.SMT_ABOUT_NAME.getName())))
          val1 = (bstr+SmtConstants.BLUE_FONT+val+ SmtConstants.END_FONT +bend);
          
        if(html && LLNL && (s.getName().startsWith(SmtAboutType.SMT_ABOUT_DATE.getName())))
          val1 = (bstr+SmtConstants.GREEN_FONT+val+ SmtConstants.END_FONT +bend);
          
        if(html && LLNL && (s.getName().startsWith(SmtAboutType.SMT_ABOUT_VERSION.getName())))
          val1 = (bstr+SmtConstants.RED_FONT+val+ SmtConstants.END_FONT +bend);
          
        
        stringValue.append(String.format(RcdFormat, s.getName(), val1) + cr);
      }
    }
    return stringValue.toString();
  }
  /************************************************************
   * Method Name:
   *  toString
  **/
  /**
   * Describe the method here
   *
   * @see java.lang.Object#toString()
   *
   * @return
   ***********************************************************/
  
  @Override
  public String toString()
  {
    HashMap<SmtAboutType,String> aMap = getSmtAboutTypes();
    StringBuffer stringValue = new StringBuffer();
        
    // typically I want it in order of SmtAboutType
    for(SmtAboutType s : SmtAboutType.SMT_ALL_ABOUTS)
    {
      String val = aMap.get(s);
      if((val != null) && (val.length() > 1))
        stringValue.append(String.format(RcdFormat, s.getName(), val) + "\n");
    }
    return stringValue.toString();
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
  public static void main(String[] args)
  {
    // TODO Auto-generated method stub

  }

  public boolean isValid()
  {
    // do I care about this particular record?  If not, return false
    
    if ((AboutMap == null) || (AboutMap.size() < 2))
      return false;
    
    HashMap<SmtAboutType,String> aMap = getSmtAboutTypes();

    if("meier3".equalsIgnoreCase(aMap.get(SmtAboutType.SMT_ABOUT_VENDOR)))
      return false;

    if(aMap.get(SmtAboutType.SMT_ABOUT_NAME) == null)
      return false;
    
    return true;
  }

  public String getTitle()
  {
    return getEnumSetValue(SmtAboutMap.SMT_ALL_TITLES);
  }

  public String getVersion()
  {
    return getEnumSetValue(SmtAboutMap.SMT_ALL_VERSIONS);
  }

  public String getDate()
  {
    return getEnumSetValue(SmtAboutMap.SMT_ALL_DATES);
  }

  private String getEnumSetValue(EnumSet<SmtAboutMap> eSet)
  {
    // return the first value found in the map that cooresponds to this enumset
    
    // create a non-redundant (prefer last) list of just dates
    for(SmtAboutMap m : eSet)
    {
      SmtAboutType t = m.getAboutType();
      String val     = AboutMap.get(m);
      if(val != null)
         return val;
     }
    return "unknown";
  }

}
