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
 *        file: IB_RouteParser.java
 *
 *  Created on: May 5, 2014
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.opensm.plugin.data.RT_Node;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.parser.Parser;
import gov.llnl.lc.time.TimeStamp;

/**********************************************************************
 * Describe purpose and responsibility of IB_RouteParser
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version May 5, 2014 9:25:18 AM
 **********************************************************************/
public class IB_RouteParser implements Parser, CommonLogger
{
  private static final String NEW_LINE = System.getProperty("line.separator");
  static final String COLON              = ":";
  static final String SPACE              = " ";
  
  static final int NORMAL_STATE  = 0;
  static final int HEADER_STATE  = 1;
  static final int IGNORE_STATE  = 2;
  static final int NULL_STATE = 13;

  static final String HEX_PREFIX        = "0x";
  static final String UNICAST_PREFIX     = "Unicast";
  static final String LID_PREFIX     = "Lid";
  static final String GUID_PREFIX = "guid";
  static final String PORT_GUID_PREFIX = "portguid";

  protected String FileName;
  protected TimeStamp FileTimeStamp;
  protected int linesParsed;
  protected RT_Node RT_node;
  
  protected void initParser()
  {
    // get rid of instance data
    FileName = "";
    linesParsed = 0;
  }

 /************************************************************
   * Method Name:
   *  parse
   **/
  /**
   * Describe the method here
   *
   * @see gov.llnl.lc.parser.Parser#parse(java.io.BufferedReader)
   *
   * @param in
   * @throws IOException
   ***********************************************************/

  @Override
  public void parse(BufferedReader in) throws IOException
  {
    int state = NULL_STATE;
    linesParsed = 0;
    String line;  // get one line at a time
    
    RT_Node swNode = null;
    
    while ((line = in.readLine()) != null)
    {
      // parse the input, line by line
      linesParsed++;
      if ( logger.isLoggable(java.util.logging.Level.FINEST) )
      {
        logger.finest(line);
      }
      
      if (line.length() == 0) 
      {
        state = NULL_STATE;
      }
      else if (line.startsWith(UNICAST_PREFIX) )
      {
        // stay in this state until the first hex
        state = HEADER_STATE;
      }
      else if (line.startsWith(HEX_PREFIX))
        state = NORMAL_STATE;
      else
        state = IGNORE_STATE;

      switch (state) 
      {
        case NULL_STATE:
          break;
          
        case IGNORE_STATE:
          if(swNode != null)
          {
            RT_node = swNode;
          }
           break;
          
        case NORMAL_STATE:
          if(line.startsWith(HEX_PREFIX))
          {
            /* line is in the form;
             * 
             * 0x0016 029 : (Channel Adapter portguid 0x001175000077f91e: 'hype356 qib0')
             * 
             * The first value is the lid
             * The second value is the port number
             * And the only other value I care about is the portguid
             * 
             */
            /* get the value after the colon, up to the next white space */
            String[] values = line.split(COLON);
            if(values.length > 1)
            {
              /* the lid is at 2 to 5, and the portnumber is 7 to 9 */
              String lidStr = values[0].substring(2, 6);
              String pnStr  = values[0].substring(7, 10);
              int lid = Integer.parseInt(lidStr, 16);
              int pn  = Integer.parseInt(pnStr);
              
              /* the port guid is just after the prefix */
              int gndex = values[1].indexOf(PORT_GUID_PREFIX) + PORT_GUID_PREFIX.length() + 1;
              String gStr = values[1].substring(gndex);
              IB_Guid guid = new IB_Guid(gStr);
              if(swNode != null)
                swNode.add(pn, lid, guid);
             }
          }
           break;
          
        case HEADER_STATE:
          if(line.startsWith(UNICAST_PREFIX))
          {
            /* create the switch, pull out the lid and guid from the line in the form;
             * 
             * Unicast lids [0x0-0x16f] of switch Lid 17 guid 0x00066a00ec002eec (ibcore1 L109):
             * 
             * The Lid is just after "Lid"
             * The guid is just after "guid"
             * 
             */
            int lndex = line.indexOf(LID_PREFIX) + LID_PREFIX.length() + 1;
            String lString = line.substring(lndex);
            String[] values = lString.split(SPACE);
            if(values.length > 3)
            {
              /* the first value is the lid (not hex)
               * the third value is the guid
               */
              int lid = Integer.parseInt(values[0]);
              String gStr = values[2];
              IB_Guid guid = new IB_Guid(gStr);
              
              // this should only happen once, in the header
              swNode = new RT_Node(lid, guid);
            }
          }
           break;
          
          default:
            break;
      }
     }
  }

  /************************************************************
   * Method Name:
   *  parseFile
   **/
  /**
   * Describe the method here
   *
   * @see gov.llnl.lc.parser.Parser#parseFile(java.lang.String)
   *
   * @param filename
   * @throws IOException
   ***********************************************************/

  public void parseFile(File file) throws IOException
  {
    BufferedReader fr = new BufferedReader(new FileReader(file));
    setFileName(file.getAbsolutePath());
    FileTimeStamp = new TimeStamp(file.lastModified());
    
    if ( logger.isLoggable(java.util.logging.Level.INFO) )
    {
      logger.info("Parsing File: " + this.getFileName());
    }
    parse(fr);
  }

  
  @Override
  public void parseFile(String filename) throws IOException
  {
      parseFile(new File(filename));
  }
  
  

    /************************************************************
   * Method Name:
   *  getRT_node
   **/
  /**
   * Returns the value of rT_node
   *
   * @return the rT_node
   *
   ***********************************************************/
  
  public RT_Node getRT_node()
  {
    return RT_node;
  }

    public String getSummary() {
       StringBuffer stringValue = new StringBuffer();
      
       stringValue.append(this.getClass().getName() + NEW_LINE);
       stringValue.append(this.getFileName() + NEW_LINE);
       stringValue.append("Date: " + this.getFileTimeStamp() + NEW_LINE);
       stringValue.append("Lines: " +this.getLinesParsed() + NEW_LINE);
       
       if(RT_node != null)
       {
         stringValue.append(RT_node.toString());
       }
          
      return stringValue.toString();
  }
  
  public TimeStamp getFileTimeStamp()
  {
    return FileTimeStamp;
  }

  public void setFileTimeStamp(TimeStamp fileTimeStamp)
  {
    FileTimeStamp = fileTimeStamp;
  }



  /************************************************************
   * Method Name:
   *  parseString
   **/
  /**
   * Parses the entire output of the ibroute command, for a single
   * switch node.
   *
   * @see gov.llnl.lc.parser.Parser#parseString(java.lang.String)
   *
   * @param routeOutput  the entire output of the ibroute command
   * @throws IOException
   ***********************************************************/

  @Override
  public void parseString(String routeOutput) throws IOException
  {
    StringReader sr = new StringReader(routeOutput);
    BufferedReader br = new BufferedReader(sr);
    
    FileName = "StringReader";
    FileTimeStamp = new TimeStamp();

    
    if ( logger.isLoggable(java.util.logging.Level.FINEST) )
    {
      logger.finest("Parsing String");
    }
    parse(br);
  }

  /************************************************************
   * Method Name:
   *  getFileName
   **/
  /**
   * Describe the method here
   *
   * @see gov.llnl.lc.parser.Parser#getFileName()
   *
   * @return
   ***********************************************************/

  /************************************************************
   * Method Name:
   *  getFileName
   **/
  /**
   * Describe the method here
   *
   * @see gov.llnl.lc.parser.Parser#getFileName()

   * @param   describe the parameters
   *
   * @return  describe the value returned
   * @return
   ***********************************************************/

  @Override
  public String getFileName()
  {
    return FileName;
  }

  /************************************************************
   * Method Name:
   *  setFileName
   **/
  /**
   * Describe the method here
   *
   * @see gov.llnl.lc.parser.Parser#setFileName(java.lang.String)

   * @param   describe the parameters
   *
   * @return  describe the value returned
   * @param fileName
   ***********************************************************/

  @Override
  public void setFileName(String fileName)
  {
    // clear previous, prepare for a new file
    initParser();
    FileName = fileName;
  }

  /************************************************************
   * Method Name:
   *  getLinesParsed
   **/
  /**
   * Describe the method here
   *
   * @see gov.llnl.lc.parser.Parser#getLinesParsed()

   * @param   describe the parameters
   *
   * @return  describe the value returned
   * @return
   ***********************************************************/

  @Override
  public int getLinesParsed()
  {
    return linesParsed;
  }

  /************************************************************
   * Method Name:
   *  main
   **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects

   * @param   describe the parameters
   *
   * @return  describe the value returned
   * @param args
   ***********************************************************/
  public static void main(String[] args)
  {
    IB_RouteParser parser = new IB_RouteParser();
    String fileName = "/home/meier3/.smt/cache/routes/route_results.txt";
    
    try
    {
      parser.parseFile(new File(fileName));
    }
    catch (IOException e)
    {
      System.out.println("Parse exception: " + e.getMessage());
    }
    
    System.out.println(parser.toString());    
  }

}
