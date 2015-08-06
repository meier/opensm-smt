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
 *        file: SmtFilter.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.filter;

import gov.llnl.lc.smt.command.SmtCommand;
import gov.llnl.lc.smt.props.SmtProperty;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;

/**********************************************************************
 * Describe purpose and responsibility of WhiteAndBlackListFilter
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Sep 18, 2013 1:25:05 PM
 **********************************************************************/
public class SmtFilter implements gov.llnl.lc.logging.CommonLogger
{
  // if empty, include all results.  if not empty, only include results that contain a string in this list
  protected java.util.ArrayList<String> WhiteList     = new java.util.ArrayList<String>();
  // if empty, include all results.  if not empty, only reject results that contain a string in this list
  protected java.util.ArrayList<String> BlackList     = new java.util.ArrayList<String>();
  
  // the files used to create the Filter
  protected java.util.ArrayList<String> FileList     = new java.util.ArrayList<String>();

  

  /************************************************************
   * Method Name:
   *  WhiteAndBlackListFilter
  **/
  /**
   * An empty filter.  Everything should pass through this filter.
   *
   * @see     describe related java objects
   *
   * @param filterFileName
   ***********************************************************/
  public SmtFilter()
  {
    super();
  }

  /************************************************************
   * Method Name:
   *  WhiteAndBlackListFilter
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param filterFileName
   * @throws IOException 
   ***********************************************************/
  public SmtFilter(String filterFileName) throws IOException
  {
    super();
    
    // initialize the filter via the file
    SmtFilter f = readFilter(filterFileName);
    if(f != null)
    {
      setWhiteList(f.getWhiteList());
      setBlackList(f.getBlackList());
      setFileList(f.getFileList());
    }
  }

  
  /************************************************************
   * Method Name:
   *  WhiteAndBlackListFilter
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param configMap
   * @throws IOException 
   ***********************************************************/
  public SmtFilter(Map<String, String> configMap) throws IOException
  {
    super();
    
    // initialize the filter via the file
    SmtFilter f = initFilter(configMap);
    if(f != null)
    {
      setWhiteList(f.getWhiteList());
      setBlackList(f.getBlackList());
      setFileList(f.getFileList());
    }
  }

  /************************************************************
   * Method Name:
   *  WhiteAndBlackListFilter
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param whiteList
   * @param blackList
   ***********************************************************/
  public SmtFilter(ArrayList<String> whiteList, ArrayList<String> blackList, ArrayList<String> fileList)
  {
    super();
    WhiteList = whiteList;
    BlackList = blackList;
    FileList  = fileList;
    int wls = WhiteList == null ? 0: WhiteList.size();
    int bls = BlackList == null ? 0: BlackList.size();
    int fls = FileList  == null ? 0: FileList.size();
    logger.severe("Filter - WL:" + wls + ", BL:" + bls + ", FL:" + fls);
  }

  protected static SmtFilter initFilter(Map<String,String> map) throws IOException
  {
    logger.severe("reading a config");
    // check to see if anything needs to be initialized
    if(map == null)
      return null;

    String filterFile = map.get(SmtProperty.SMT_FILTER_FILE.getName());
    
    if(filterFile != null)
      return readFilter(filterFile);
    return null;
  }
  
  /************************************************************
   * Method Name:
   *  getWhiteList
   **/
  /**
   * Returns the value of whiteList
   *
   * @return the whiteList
   *
   ***********************************************************/
  
  public java.util.ArrayList<String> getWhiteList()
  {
    return WhiteList;
  }

  /************************************************************
   * Method Name:
   *  setWhiteList
   **/
  /**
   * Sets the value of whiteList
   *
   * @param whiteList the whiteList to set
   *
   ***********************************************************/
  public void setWhiteList(java.util.ArrayList<String> whiteList)
  {
    WhiteList     = new java.util.ArrayList<String>();
    addWhiteList(whiteList);
  }

  /************************************************************
   * Method Name:
   *  getBlackList
   **/
  /**
   * Returns the value of blackList
   *
   * @return the blackList
   *
   ***********************************************************/
  
  public java.util.ArrayList<String> getBlackList()
  {
    return BlackList;
  }

  /************************************************************
   * Method Name:
   *  setBlackList
   **/
  /**
   * Sets the value of blackList
   *
   * @param blackList the blackList to set
   *
   ***********************************************************/
  public void setBlackList(java.util.ArrayList<String> blackList)
  {
    BlackList     = new java.util.ArrayList<String>();
    addBlackList(blackList);
  }
  
  /************************************************************
   * Method Name:
   *  getFileList
   **/
  /**
   * Returns the value of FileList
   *
   * @return the FileList
   *
   ***********************************************************/
  
  public java.util.ArrayList<String> getFileList()
  {
    return FileList;
  }

  /************************************************************
   * Method Name:
   *  setFileList
   **/
  /**
   * Sets the value of fileList
   *
   * @param fileList the fileList to set
   *
   ***********************************************************/
  public void setFileList(java.util.ArrayList<String> fileList)
  {
    FileList     = new java.util.ArrayList<String>();
    addFileList(fileList);
  }
  

  
  protected boolean passListCheck(String test)
  {
    return (SmtFilter.passListCheck(test, WhiteList, BlackList));
  }

  public boolean isFiltered(String test)
  {
    return SmtFilter.isFiltered(test, WhiteList, BlackList);
  }

  protected static boolean passListCheck(String test, java.util.ArrayList<String> whiteList, java.util.ArrayList<String> blackList)
  {
    // will this String pass both list checks
    //    return true only if
    //  IS  in the WhiteList and
    //  NOT in the BlackList
    return (SmtFilter.isInWhiteList(test, whiteList) && !SmtFilter.isInBlackList(test, blackList));
  }

  public static boolean isFiltered(String test, java.util.ArrayList<String> whiteList, java.util.ArrayList<String> blackList)
  {
    // will this String pass both list checks
    //    return true only if
    //  IS  in the WhiteList and
    //  NOT in the BlackList
    return !(SmtFilter.passListCheck(test, whiteList, blackList));
  }

  public boolean isInWhiteList(String test)
  {
    return isInWhiteList(test, WhiteList);
  }

  public static boolean isInWhiteList(String test, java.util.ArrayList<String> whiteList)
  {
    // true if the WhiteList is empty
    if((whiteList == null) || (whiteList.size() == 0))
      return true;

    return isInStringList(test, whiteList);
  }

  public boolean isInBlackList(String test)
  {
    return isInBlackList(test, BlackList);
  }

  public static boolean isInBlackList(String test, java.util.ArrayList<String> blackList)
  {
    // false if the BlackList is empty
    if((blackList == null) || (blackList.size() == 0))
      return false;

    return isInStringList(test, blackList);
  }

  protected static boolean isInStringList(String string, java.util.ArrayList<String> stringList)
  {
    // return true if the test string contains
    // any string in the stringList
    if((string == null) || (string.length() == 0))
      return false;
    
    // iterate through the list, and return true ASAP
    for(String s: stringList)
      if(string.indexOf(s) > -1)
        return true;

    return false;
  }
  
  protected static SmtFilter readFilter(String fileName) throws IOException
  {
    logger.severe("reading a file");
    String fName = SmtCommand.convertSpecialFileName(fileName);
    File inFile = new File(fName);
    if (!inFile.exists()) 
    {
      logger.severe("Could not find file: (" + fName +") for reading");
      return null;
    }
    
    java.util.ArrayList<String> WL     = new java.util.ArrayList<String>();
    java.util.ArrayList<String> BL     = new java.util.ArrayList<String>();
    java.util.ArrayList<String> FL     = new java.util.ArrayList<String>();
    FL.add(fName);

    BufferedReader br = new BufferedReader( new FileReader( inFile )) ;
    String readString = null;
    String fname = null;
    boolean isWhiteListType = true;   // the default
    
    // don't include leading and trailing white space
    while(( readString = br.readLine())  != null)
    {
      String str = readString.trim();
      // skip empty lines
      if(str.length() > 1)
      {
        switch( str.charAt(0))
        {
          case '*':
            // change list type
            if(str.indexOf("White") > -1)
              isWhiteListType = true;
            if(str.indexOf("Black") > -1)
              isWhiteListType = false;
            break;
            
          case '@':
            // is a name of a file??
            fname = str.substring(1);
            // don't allow direct recursion (can't stop circular recursion)
            if(fName.equalsIgnoreCase(fname) || fileName.equalsIgnoreCase(fname))
            {
              logger.severe("file recursion not allowed");
              break;
            }
            SmtFilter ff = new SmtFilter(fname);
            if(ff != null)
            {
              // add these lists to our own
              WL.addAll(ff.getWhiteList());
              BL.addAll(ff.getBlackList());
              FL.addAll(ff.getFileList());
            }
             break;
            
          case '#':
            // do nothing with comments
            logger.info(str);
            break;
            
          default:
            // put this in one of the two lists
            if(isWhiteListType)
              WL.add(str);
            else
              BL.add(str);
            break;
         }
      }
     }
    br.close(  ) ;
    return new SmtFilter(WL, BL, FL);
   }
  
  public SmtFilter addWhiteList(ArrayList<String> list)
  {
    if(list != null)
      WhiteList.addAll(list);
    return this;
  }
  
  public SmtFilter addBlackList(ArrayList<String> list)
  {
    if(list != null)
      BlackList.addAll(list);
    return this;
  }
  
  public SmtFilter addFileList(ArrayList<String> list)
  {
    if(list != null)
      FileList.addAll(list);
    return this;
  }
  
  public SmtFilter addFilter(SmtFilter filter)
  {
    if(filter != null)
    {
      addWhiteList(filter.getWhiteList());
      addBlackList(filter.getBlackList());
      addFileList(filter.getFileList());
    }
    return this;
  }
  
  protected static void writeStrings(String fileName, java.util.ArrayList<String> stringList) throws IOException
  {
    if((stringList == null) || (stringList.size() == 0))
      return;
    
    String fName = SmtCommand.convertSpecialFileName(fileName);
    File outFile = new File(fName);
    
      if (!outFile.exists()) 
      {
        outFile.createNewFile();
      }
      PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outFile)));
      
      // iterate through the list
      for(String string: stringList)
      {
        out.println(string);
      }
      out.close();
    return;
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
   * @throws IOException 
   ***********************************************************/
  public static void main(String[] args) throws IOException
  {
    if((args != null) && (args.length > 0))
    {
      System.out.println("This is argument 0: " + args[0]);
      SmtFilter filter = new SmtFilter(args[0]);
      
      System.out.println("The white list is: " + filter.getWhiteList().size());
      System.out.println("The black list is: " + filter.getBlackList().size());
      System.out.println("The file list is: " + filter.getFileList().size());
    }
  }

}
