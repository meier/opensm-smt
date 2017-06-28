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
 *        file: NodeList.java
 *
 *  Created on: Jan 19, 2017
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.util;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**********************************************************************
 * Describe purpose and responsibility of NodeList
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Jan 19, 2017 2:35:38 PM
 **********************************************************************/
public class NodeList
{
  private String NodeListString;
  private String NodeName;
  private ArrayList<Integer> NodeNumbers;

  /************************************************************
   * Method Name:
   *  NodeList
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   ***********************************************************/
  public NodeList()
  {
    // TODO Auto-generated constructor stub
    /*
     * catalyst[1-66,68-72,74-76,78-106,108-113,115-133]
     * catalyst[17,23-324]
     * catalyst[1-16,18-22]
     * 
     */
  }

  public NodeList(String nodeList)
  {
    // TODO Auto-generated constructor stub
    /*
     * catalyst[1-66,68-72,74-76,78-106,108-113,115-133]
     * up:   303: catalyst[17,23-324]
     * down:  21: catalyst[1-16,18-22]
     * 
     */
    NodeListString = nodeList;
    initList();
    
  }

  /************************************************************
   * Method Name:
   *  initList
  **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   ***********************************************************/
  private void initList()
  {
    // from NodeListString, create the rest
    
    /*
     * catalyst[1-66,68-72,74-76,78-106,108-113,115-133]
     * catalyst[17,23-324]
     * catalyst[1-16,18-22]
     * 
     */
    
    // first portion is the NodeName
    // the remainder is contained in the brackets
    
    int bN = this.NodeListString.indexOf("[");
    int eN = this.NodeListString.indexOf("]");
    
    if((bN >= 0) && (eN > bN))
    {
      this.NodeName = this.NodeListString.substring(0, bN++);
      
      String intRange = this.NodeListString.substring(bN, eN);
      if(isValidIntRangeInput(intRange))
        this.NodeNumbers = getIntsInRange(intRange);
      else
        System.err.println("Invalid node list range for: " + NodeListString);
    }
    else
      System.err.println("Invalid node list string: " + NodeListString);
  }

  /************************************************************
   * Method Name:
   *  getNodeListString
  **/
  /**
   * Returns the value of nodeListString
   *
   * @return the nodeListString
   *
   ***********************************************************/
  
  public String getNodeListString()
  {
    return NodeListString;
  }
  
  public ArrayList<String> getListOfNodes()
  {
    java.util.ArrayList<String> nodes     = new java.util.ArrayList<String>();
    
    if((NodeNumbers != null) && (NodeName != null))    
      for(Integer I: NodeNumbers)
        nodes.add(NodeName + I.intValue());
    return nodes;
  }

  /************************************************************
   * Method Name:
   *  getNodeName
  **/
  /**
   * Returns the value of nodeName
   *
   * @return the nodeName
   *
   ***********************************************************/
  
  public String getNodeName()
  {
    return NodeName;
  }

  /************************************************************
   * Method Name:
   *  getNodeNumbers
  **/
  /**
   * Returns the value of nodeNumbers
   *
   * @return the nodeNumbers
   *
   ***********************************************************/
  
  public ArrayList<Integer> getNodeNumbers()
  {
    return NodeNumbers;
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
    StringBuffer buffer = new StringBuffer();
    
    if((NodeNumbers != null) && (NodeName != null))    
      for(Integer I: NodeNumbers)
        buffer.append(NodeName + I.intValue() + "\n");
    return buffer.toString();
  }

  public static Boolean isValidIntRangeInput(String text) {
    Pattern re_valid = Pattern.compile(
        "# Validate comma separated integers/integer ranges.\n" +
        "^             # Anchor to start of string.         \n" +
        "[0-9]+        # Integer of 1st value (required).   \n" +
        "(?:           # Range for 1st value (optional).    \n" +
        "  -           # Dash separates range integer.      \n" +
        "  [0-9]+      # Range integer of 1st value.        \n" +
        ")?            # Range for 1st value (optional).    \n" +
        "(?:           # Zero or more additional values.    \n" +
        "  ,           # Comma separates additional values. \n" +
        "  [0-9]+      # Integer of extra value (required). \n" +
        "  (?:         # Range for extra value (optional).  \n" +
        "    -         # Dash separates range integer.      \n" +
        "    [0-9]+    # Range integer of extra value.      \n" +
        "  )?          # Range for extra value (optional).  \n" +
        ")*            # Zero or more additional values.    \n" +
        "$             # Anchor to end of string.           ", 
        Pattern.COMMENTS);
    Matcher m = re_valid.matcher(text);
    if (m.matches())    return true;
    else                return false;
}
  public static ArrayList<Integer> getIntsInRange(String text) 
  {
    java.util.ArrayList<Integer> intsInRange     = new java.util.ArrayList<Integer>();
    
    Pattern re_next_val = Pattern.compile(
          "# extract next integers/integer range value.    \n" +
          "([0-9]+)      # $1: 1st integer (Base).         \n" +
          "(?:           # Range for value (optional).     \n" +
          "  -           # Dash separates range integer.   \n" +
          "  ([0-9]+)    # $2: 2nd integer (Range)         \n" +
          ")?            # Range for value (optional). \n" +
          "(?:,|$)       # End on comma or string end.", 
          Pattern.COMMENTS);
      Matcher m = re_next_val.matcher(text);
      while (m.find())
      {
        int start = Integer.parseInt(m.group(1));
          intsInRange.add(new Integer(start++));
          
          if (m.group(2) != null)
          {
            int range = Integer.parseInt(m.group(2));
            for(int i=start; i <= range; i++)
              intsInRange.add(new Integer(i));
          };
      }
      return intsInRange;
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
    String[] arr = new String[]
        { // Valid inputs:
        "1",
        "1,2,3",
        "1-9",
        "1-9,10-19,20-199",
        "1-8,9,10-18,19,20-199",
        // Invalid inputs:
        "A",
        "1,2,",
        "1 - 9",
        " "
        , "" };
    // Loop through all test input strings:
    int i = 0;
    for (String s : arr)
    {
      String msg = "String[" + ++i + "] = \"" + s + "\" is ";
      if (isValidIntRangeInput(s))
      {
        // Valid input line
        System.out.println(msg + "valid input. Parsing...");
      }
      else
      {
        // Match attempt failed
        System.out.println(msg + "NOT valid input.");
      }
    }
    
    // if any arguments, loop through them
    int j = 0;
    for (String st : args)
    {
      String msg = "String[" + ++j + "] = \"" + st + "\" is ";
      if (isValidIntRangeInput(st))
      {
        // Valid input line
        System.out.println(msg + "valid input. Parsing...");
      }
      else
      {
        // Match attempt failed
        System.out.println(msg + "NOT valid input.");
      }
      
      NodeList nl2 = new NodeList(st);
      System.out.println("The node list is:");
      System.out.println(nl2.toString());

    }
    NodeList nl = new NodeList("catalyst[17,23-324]");
    System.out.println("The node list is:");
    System.out.println(nl.toString());
  }
  

}
