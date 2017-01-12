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
 *        file: SmtAbout.java
 *
 *  Created on: Jun 3, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command.about;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.jar.Manifest;

import org.apache.commons.cli.CommandLine;

import gov.llnl.lc.smt.SmtConstants;
import gov.llnl.lc.smt.command.SmtCommand;
import gov.llnl.lc.smt.command.config.SmtConfig;
import gov.llnl.lc.smt.props.SmtProperty;

/**********************************************************************
 * Describe purpose and responsibility of SmtAbout
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Jun 3, 2013 7:46:43 AM
 **********************************************************************/
public class SmtAbout extends SmtCommand
{
  // a list of all the information gathered from the manifest
  private ArrayList<SmtAboutRecord> Records = new ArrayList<SmtAboutRecord>();


  private boolean gatherRecordsFromManifest()
  {
     Records = getRecordsFromManifest(this);
     return true;
  }
  
  protected static ArrayList<SmtAboutRecord> getRecordsFromFile(Object obj, String fileName)
  {
    ArrayList<SmtAboutRecord> records = new ArrayList<SmtAboutRecord>();

    try
    {
      Enumeration<URL> resources = obj.getClass().getClassLoader().getResources(fileName);
      while (resources.hasMoreElements())
      {
        Manifest man = new Manifest(resources.nextElement().openStream());
        SmtAboutRecord r = new SmtAboutRecord(man);
        if ((r != null) && r.isValid())
          records.add(r);
      }
    }
    catch (IOException E)
    {
      // handle
    }
    return records;
  }
  
  public static ArrayList<SmtAboutRecord> getRecordsFromManifest(Object obj)
  {
    ArrayList<SmtAboutRecord> records = new ArrayList<SmtAboutRecord>();
    records.addAll(getRecordsFromFile(obj,"META-INF/MANIFEST.MF" ));
    records.addAll(getRecordsFromFile(obj,"META-INF/Manifest.MF" ));

    return records;
  }
  
  public boolean printRecords()
  {
    printRecords(Records);
    printSMT_Record(true);
    return printOMS_Record(false);
  }
  
  public static boolean printRecords(ArrayList<SmtAboutRecord> records)
  {
    for(SmtAboutRecord record: records)
    {
      System.out.println(record.toString());
    }
    return true;
  }
  
  public boolean printOMS_Record(boolean verbose)
  {
    return printRecord(Records, "OsmClientServer", verbose);
  }

  public boolean printSMT_Record(boolean verbose)
  {
    return printRecord(Records, "SubnetMonitorTool", verbose);
  }

  
  public static boolean printRecord(ArrayList<SmtAboutRecord> records, String title, boolean verbose)
  {
    SmtAboutRecord rcd = getAboutRecord(records, title);
    if(rcd != null)
    {
      if(verbose)
        System.out.println(rcd.toString());
      else
      {
        System.out.println(title);
        System.out.println(rcd.getVersion());
        System.out.println(rcd.getDate());
      }
    }
    return true;
  }
  
  public static SmtAboutRecord getAboutRecord(ArrayList<SmtAboutRecord> records, String title)
  {
    // given one or more records, return the first one that matches the title provided
    for(SmtAboutRecord record: records)
    {
      String rTitle = record.getTitle();
      if(title.compareToIgnoreCase(rTitle) == 0)
        return record;
    }
    return null;
  }
  
  public SmtAboutRecord getAboutRecord(String title)
  {
    return getAboutRecord(Records, title);
  }
  
  /************************************************************
   * Method Name:
   *  doCommand
   **/
  /**
   * Describe the method here
   *
   * @see gov.llnl.lc.smt.command.SmtCommand#doCommand(gov.llnl.lc.smt.command.config.SmtConfig)
   *
   * @param config
   * @return
   * @throws Exception
   ***********************************************************/

  @Override
  public boolean doCommand(SmtConfig config) throws Exception
  {
    gatherRecordsFromManifest();
    
    // ALL, SMT, or OMS ?
    printRecords();
    return true;
  }

  /************************************************************
   * Method Name:
   *  init
   **/
  /**
   * Describe the method here
   *
   * @see gov.llnl.lc.smt.command.SmtCommand#init()
   *
   * @return
   ***********************************************************/

  @Override
  public boolean init()
  {
    USAGE = "[-v] [-?]  ";
    HEADER = "smt-about - provides JAR package information";
    EXAMPLE ="examples:" + SmtConstants.NEW_LINE +
        "> smt-about" + SmtConstants.NEW_LINE + 
        "> smt-about -?" + SmtConstants.NEW_LINE  + ".";  // terminate with nl

    // create and initialize the common options for this command
    this.initMinimumOptions();
    
    return true;
  }

  /************************************************************
   * Method Name:
   *  parseCommands
   **/
  /**
   * Describe the method here
   *
   * @see gov.llnl.lc.smt.command.SmtCommand#parseCommands(java.util.Map, org.apache.commons.cli.CommandLine)
   *
   * @param config
   * @param line
   * @return
   ***********************************************************/

  @Override
  public boolean parseCommands(Map<String, String> config, CommandLine line)
  {
    
    // set the command, args, and sub-command
    config.put(SmtProperty.SMT_COMMAND.getName(), this.getClass().getName());
    SmtProperty sp = SmtProperty.SMT_ABOUT_COMMAND;
    config.put(SmtProperty.SMT_SUBCOMMAND.getName(), sp.getName());
        
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
    System.exit((new SmtAbout().execute(args)) ? 0: -1);
  }

}
