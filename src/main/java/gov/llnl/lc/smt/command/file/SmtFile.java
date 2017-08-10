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
 *        file: SmtFile.java
 *
 *  Created on: Jun 3, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command.file;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_AnonymizedCollection;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Collection;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_FilteredCollection;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.smt.SmtConstants;
import gov.llnl.lc.smt.command.SmtCommand;
import gov.llnl.lc.smt.command.config.SmtConfig;
import gov.llnl.lc.smt.filter.SmtAnonymizer;
import gov.llnl.lc.smt.filter.SmtFilter;
import gov.llnl.lc.smt.props.SmtProperty;
import gov.llnl.lc.time.TimeStamp;

/**********************************************************************
 * Describe purpose and responsibility of SmtFile
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Jun 3, 2013 7:47:21 AM
 **********************************************************************/
public class SmtFile extends SmtCommand
{
  
//  Consumer <String> consumer = SmtFile::printFileInfo;

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
    // this is the FILE command, and it can take a subcommand and an argument
    String subCommand    = null;
    String subCommandArg = null;
    boolean includeTS    = false;
    
    if(config != null)
    {
      Map<String,String> map = config.getConfigMap();
      subCommand = map.get(SmtProperty.SMT_SUBCOMMAND.getName());
      subCommandArg = map.get(SmtProperty.SMT_FILE_NAME.getName());
      String ts = map.get(SmtProperty.SMT_LIST_TIMESTAMP.getName());
      if(ts != null)
        includeTS = true;
      else
        logger.info("NOT including timestamps");
       
      // check to see if the subCommand takes any arguments or values
      if(subCommand == null)
      {
        // if no subCommand specified, assume file type
        subCommand = SmtProperty.SMT_FILE_TYPE.getName();
      }
    }
    
    // there should only be one subcommand    
    if(subCommand.equalsIgnoreCase(SmtProperty.SMT_FILE_TYPE.getName()))
    {
      System.out.println(getFileType(subCommandArg));
    }
    else if (subCommand.equalsIgnoreCase(SmtProperty.SMT_FILE_INFO.getName()))
    {
      System.out.println(getFileInfo(subCommandArg, includeTS));
    }
    else if (subCommand.equalsIgnoreCase(SmtProperty.SMT_CONCAT_HISTORY.getName()))
    {
      System.out.println("Concatinating from input file: " + subCommandArg);
      String fileName = concatFile(subCommandArg, config);
      System.out.println("\nConcatinating to output file: " + fileName);
//      System.out.println(getFileInfo(fileName, includeTS));
    }
    else if (subCommand.equalsIgnoreCase(SmtProperty.SMT_FILE_EXTRACT.getName()))
    {
      System.out.println("Extracting from input file: " + subCommandArg);
      System.out.println(getFileInfo(subCommandArg, includeTS));
      String fileName = extractFile(subCommandArg, config);
      System.out.println("\nExtracting to output file: " + fileName);
//      System.out.println(getFileInfo(fileName, includeTS));
    }
    else if (subCommand.equalsIgnoreCase(SmtProperty.SMT_FILE_COMPRESS.getName()))
    {
      System.out.println("Compressing input file: " + subCommandArg);
      System.out.println(getFileInfo(subCommandArg, includeTS));
      String fileName = compressFile(subCommandArg, config);
      System.out.println("\nCompressed output file: " + fileName);
 //     System.out.println(getFileInfo(fileName, includeTS));
      
    }
    else if (subCommand.equalsIgnoreCase(SmtProperty.SMT_FILE_ANONYMIZE.getName()))
    {
      System.out.println("Anonymizing input file: " + subCommandArg);
      String fileName = anonymizeFile(subCommandArg, config);
      System.out.println("\nAnonymized output file: " + fileName);
//      System.out.println(getFileInfo(fileName, includeTS));      
    }
    else if (subCommand.equalsIgnoreCase(SmtProperty.SMT_FILE_FILTER.getName()))
    {
      System.out.println("Filtering input file: " + subCommandArg);
      String fileName = filterFile(subCommandArg, config);
      System.out.println("\nFiltered output file: " + fileName);
//      System.out.println(getFileInfo(fileName, includeTS));    
    }
    
   return true;
  }
  
  /************************************************************
   * Method Name:
   *  getFileType
  **/
  /**
   * Iterates through the "known" filetypes, and checks to see
   * if the provided name maps to one of these.  If so, return
   * the first success.
   *
   * @see     describe related java objects
   *
   * @param fileName
   * @return
   ***********************************************************/
  private static String getFileType(String fileName)
  { 
    logger.severe("getting file type");
    
    // sort the various types based on the the priority (or probability)
    TreeSet<SmtProperty> ft = SmtProperty.sortPropertySet(SmtProperty.SMT_FILE_TYPES);

    for(SmtProperty s : ft)
    {
      if(isFileType(fileName, s))
        return s.getPropertyName();
    }
    return "Unknown File Type";
  }
  
  /************************************************************
   * Method Name:
   *  isFileType
  **/
  /**
   * if the provided name maps to the provided type, return true,
   * otherwise return false;
   *
   * @see     describe related java objects
   *
   * @param fileName
   * @return
   ***********************************************************/
  public static boolean isFileType(String fileName, SmtProperty fileType)
  {
    if ((fileName == null) || !(SmtProperty.SMT_FILE_TYPES.contains(fileType)))
      return false;
    
    logger.severe("is file: " + fileName + " of type: " + fileType.getPropertyName());

    String fn = convertSpecialFileName(fileName);

    // parameter types for all read file (just a filename
    Class[] partypes = new Class[] { String.class };

    // Arguments to be passed into method
    Object[] arglist = new String[] { fn };

    String className  = fileType.getPropertyName();
    String methodName = fileType.getName();

    // attempt to open this file, using this class
    Class<?> smtClass = null;
    try
    {
      smtClass = Class.forName(className);
      Method m = smtClass.getDeclaredMethod(methodName, partypes);
      Object o = m.invoke(null, arglist);
      
      // previous should throw an exception if not the correct type, but...
      // final sanity check
      
      String type = o.getClass().getCanonicalName();
      if(className.equals(type))
        return true;
 
      // if I am here, then the read method didn't throw an exception, but also didn't match class names????
      return false;
    }
    catch (Exception e)
    {
      logger.info("File NOT: " + className);
    }
    return false;
  }
  
  /************************************************************
   * Method Name:
   *  getFileType
  **/
  /**
   * Iterates through the "known" filetypes, and checks to see
   * if the provided name maps to one of these.  If so, return
   * the first success.
   *
   * @see     describe related java objects
   *
   * @param fileName
   * @return
   ***********************************************************/
  private String getSupportedFileTypes()
  {
    StringBuffer bs = new StringBuffer();
    
    // sort the various types based on the the priority (or probability)
    TreeSet<SmtProperty> ft = SmtProperty.sortPropertySet(SmtProperty.SMT_FILE_TYPES);

      for(SmtProperty s : ft)
      {
        bs.append(s.getPropertyName() + "\n");
       }
    return bs.toString();
  }
  
  private static void printFileInfo(String fileName)
  {
    System.out.println(fileName);
    System.out.println(getFileInfo(fileName, false));
  }
    
  private static String getFileInfo(String fileName, boolean includeTimeStamps)
  {
    String fn = convertSpecialFileName(fileName);
    
  //parameter types for all read file (just a filename)
    Class[] partypes = new Class[]{String.class};
    
    //Arguments to be passed into method
    Object[] arglist = new String[]{fn};
    
    // sort the various types based on the the priority (or probability)
    TreeSet<SmtProperty> ft = SmtProperty.sortPropertySet(SmtProperty.SMT_FILE_TYPES);
    
      for(SmtProperty s : ft)
      {
        /* TODO move this to a different enum, this is a hack */
        String className    = s.getPropertyName();
        String methodRead   = s.getName();
        String methodInfo   = s.getShortName();
        String methodListTS = s.getArgName();
        
        // attempt to open this file, using this class
        Class<?> smtClass = null;
        try
        {
          String timeString = "";
          smtClass = Class.forName(className);
          Method r = smtClass.getDeclaredMethod(methodRead, partypes);
          Object o = r.invoke(null, arglist);
          
          // previous should throw an exception if not the correct type, but...
          // final sanity check
          
          String type = o.getClass().getCanonicalName();
          if(!className.equals(type))
          {
            logger.info("File NOT: " + className + " (" + type + ")");
            continue;
//            
//            return "Could not get File Info";
//            return "Could not get File Info";
          }
           
          // if I am here, then it worked, so I know the type of file it is
          // now invoke the other method to return the info, or the timelist string
          Method i = smtClass.getDeclaredMethod(methodInfo, (java.lang.Class<?>[])null);
          Object str = i.invoke(o, (java.lang.Object[])null);
          
          // conditionally get the timelist too
        if (includeTimeStamps)
        {
          Method t = smtClass.getDeclaredMethod(methodListTS, (java.lang.Class<?>[]) null);
          Object strT = t.invoke(o, (java.lang.Object[]) null);
          timeString = "\ntimestamp list:\n" + strT.toString();
       }
           
          // I know this is a string
          return str.toString() + timeString;
        }
        catch (Exception e)
        {
          logger.info("File is NOT: " + className);
        }
      }
    return "Could not obtain File Info";
  }
  
  public static Object getFileObject(String fileName)
  {
    String fn = convertSpecialFileName(fileName);
    
  //parameter types for all read file (just a filename)
    Class[] partypes = new Class[]{String.class};
    
    //Arguments to be passed into method
    Object[] arglist = new String[]{fn};
    
    // sort the various types based on the the priority (or probability)
    TreeSet<SmtProperty> ft = SmtProperty.sortPropertySet(SmtProperty.SMT_FILE_TYPES);

      for(SmtProperty s : ft)
      {
        String className = s.getPropertyName();
        String methodRead = s.getName();
        
        // attempt to open this file, using this class
        Class<?> smtClass = null;
        try
        {
          smtClass = Class.forName(className);

          Method r = smtClass.getDeclaredMethod(methodRead, partypes);
           Object o = r.invoke(null, arglist);
          
          // if I am here, then it didn't throw an exception, but did I get anything?
          if(o != null)
            return o;
        }
        catch (Exception e)
        {
          logger.severe("File NOT: " + className);
        }
      }
    return "Could not obtain an object from the file File";
  }

  /************************************************************
   * Method Name:
   *  init
   **/
  /**
   * Initializes the resources for the Command, primarily the Option
   * object.
   *
   * @see gov.llnl.lc.smt.command.SmtCommand#init()
   *
   * @return
   ***********************************************************/

  @Override
  public boolean init()
  {
    USAGE = "filename [-t] [-i] [-c <# skip> <out filename>]";
    HEADER = "smt-file - SMT file interrogation and manipulation";
    EXAMPLE ="examples:" + SmtConstants.NEW_LINE +
        "> smt-file -i surface3h.his                  - show surface3h.his details" + SmtConstants.NEW_LINE + 
        "> smt-file -i HypeFR.his -lts                - show HypeFR.his details, including timestamps" + SmtConstants.NEW_LINE + 
        "> smt-file surface3h.his -c 4 compress.his   - compress surface3h.his 4x and write to compress.his" + SmtConstants.NEW_LINE  + 
        "> smt-file sierra3H.his -x Feb 25 12:35:08 2015 Feb 25 13:52:38 2015 sierraSmall.his" + SmtConstants.NEW_LINE  +
        ".                                            - extract snapshots from sierra3H.his (t1 to t2) and write to sierraSmall.his" + SmtConstants.NEW_LINE  + ".";  // terminate with nl

    // create and initialize the common options for this command
    initMinimumOptions();
    
    // initialize the command specific options
    SmtProperty sp = SmtProperty.SMT_FILE_TYPE;
    Option fType     = new Option( sp.getShortName(), sp.getName(), false, sp.getDescription() );
    sp = SmtProperty.SMT_FILE_INFO;
    Option info     = new Option( sp.getShortName(), sp.getName(), false, sp.getDescription() );
    sp = SmtProperty.SMT_FILE_COMPRESS;
    Option compress = OptionBuilder.hasArg(true).hasArgs(2).withArgName( sp.getArgName() ).withValueSeparator('=').withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );
    sp = SmtProperty.SMT_CONCAT_HISTORY;
    Option concat = OptionBuilder.hasArg(true).hasArgs(1).withArgName( sp.getArgName() ).withValueSeparator('=').withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );
    sp = SmtProperty.SMT_FILE_EXTRACT;
    Option extract = OptionBuilder.hasArg(true).hasArgs(9).withArgName( sp.getArgName() ).withValueSeparator('=').withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );
    sp = SmtProperty.SMT_LIST_TIMESTAMP;
    Option lTimeStamps = new Option( sp.getShortName(), sp.getName(), false, sp.getDescription() );    
    sp = SmtProperty.SMT_FILE_ANONYMIZE;
    Option fA  = OptionBuilder.hasArg(true).hasArgs(2).withArgName( sp.getArgName() ).withValueSeparator('=').withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );
    sp = SmtProperty.SMT_FILE_FILTER;
    Option fF  = OptionBuilder.hasArg(true).hasArgs(2).withArgName( sp.getArgName() ).withValueSeparator('=').withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );

    
    options.addOption( fType );
    options.addOption( info );
    options.addOption( compress );
    options.addOption( concat );
    options.addOption( extract );
    options.addOption( lTimeStamps );
    options.addOption( fA );
    options.addOption( fF );
    
    return true;
  }

  /************************************************************
   * Method Name:
   *  parseCommands
   **/
  /**
   * Parse the command line options here.  If any of these need to be persistent
   * then they need to be "put" into the config map.  Otherwise the command line
   * options need to set "command" flags or variables, which will potentially be
   * used later, typically the "doCommand()".
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
    // set the command and sub-command
    config.put(SmtProperty.SMT_COMMAND.getName(), this.getClass().getName());
    
    // parse (only) the command specific options (see init() )
    
    // the command needs a filename as an argument
    saveCommandArgs(line.getArgs(), config);

    // only one of these will work at a time.  Since the INFO command is parsed
    // second, it will always take precedence if both are specified on the command
    // line.  (Subcommand and Values are over written).
    SmtProperty sp = SmtProperty.SMT_FILE_TYPE;
    if(line.hasOption(sp.getName()))
    {
      config.put(SmtProperty.SMT_SUBCOMMAND.getName(), sp.getName());
      config.put(sp.getName(), line.getOptionValue(sp.getName()));
    }

    sp = SmtProperty.SMT_FILE_INFO;
    if(line.hasOption(sp.getName()))
    {
      config.put(SmtProperty.SMT_SUBCOMMAND.getName(), sp.getName());
      config.put(sp.getName(), line.getOptionValue(sp.getName()));
    }
    
    sp = SmtProperty.SMT_FILE_COMPRESS;
    if(line.hasOption(sp.getName()))
    {
      // exactly two arguments, first must be #, second is output filename
      config.put(SmtProperty.SMT_SUBCOMMAND.getName(), sp.getName());
      saveCompressionArgs(line.getOptionValues(sp.getName()), config);
    }
    
    sp = SmtProperty.SMT_CONCAT_HISTORY;
    if(line.hasOption(sp.getName()))
    {
      config.put(SmtProperty.SMT_SUBCOMMAND.getName(), sp.getName());
      saveConcatinationArgs(line.getOptionValues(sp.getName()), config);
    }
    
    sp = SmtProperty.SMT_FILE_ANONYMIZE;
    if(line.hasOption(sp.getName()))
    {
      // must be two arguments, first is anonymize filename,  final is output filename
      
      config.put(sp.getName(), line.getOptionValue(sp.getName()));
      config.put(SmtProperty.SMT_SUBCOMMAND.getName(), sp.getName());
      saveAnonymizeArgs(line.getOptionValues(sp.getName()), config);
    }
    
    sp = SmtProperty.SMT_FILE_FILTER;
    if(line.hasOption(sp.getName()))
    {
      // must be two arguments, first is filter filename,  final is output filename
      
      config.put(sp.getName(), line.getOptionValue(sp.getName()));
      config.put(SmtProperty.SMT_SUBCOMMAND.getName(), sp.getName());
      saveFilterArgs(line.getOptionValues(sp.getName()), config);
    }
    
    sp = SmtProperty.SMT_FILE_EXTRACT;
    if(line.hasOption(sp.getName()))
    {
      // must be two or three arguments, should be one or two timestamps, final is output filename
      
      config.put(SmtProperty.SMT_SUBCOMMAND.getName(), sp.getName());
      saveExtractionArgs(line.getOptionValues(sp.getName()), config);
    }
    
    sp = SmtProperty.SMT_LIST_TIMESTAMP;
    if(line.hasOption(sp.getName()))
    {
      config.put(sp.getName(), sp.getName());
    }
    return true;
  }
  
  private void saveCommandArgs(String[] args, Map<String, String> config)
  {
    // stash the command line arguments away, because we will use them later
    // see getFileName()
    
    if((args != null && args.length > 0))
    {
      // save all the arguments in a single parameter
      StringBuffer cmdArgs = new StringBuffer();
      for(String arg: args)
      {
        cmdArgs.append(arg + " ");
      }
      config.put(SmtProperty.SMT_FILE_NAME.getName(), convertSpecialFileName(args[0].trim()));      
      config.put(SmtProperty.SMT_COMMAND_ARGS.getName(), cmdArgs.toString().trim());
    }
  }
  
  private void saveCompressionArgs(String[] args, Map<String, String> config)
  {
    // stash the compression arguments away, because we will use them later
    // see getFileName()
    
    if((args != null && args.length > 0))
    {
      config.put(SmtProperty.SMT_FILE_NUM_SKIPS.getName(), args[0]);
      if(args.length > 1)
        config.put(SmtProperty.SMT_WRITE_OMS_HISTORY.getName(), args[1]);
    }
  }
  
  private void saveConcatinationArgs(String[] args, Map<String, String> config)
  {
    // stash the concatination arguments away, because we will use them later
    // see getFileName()
    
    if((args != null && args.length > 0))
    {
      // save all the arguments in a single parameter
      StringBuffer cmdArgs = new StringBuffer();
      for(String arg: args)
      {
        cmdArgs.append(arg + " ");
      }
      // this should be the output file name
      config.put(SmtProperty.SMT_WRITE_OMS_HISTORY.getName(), args[0]);
    }
  }
  
  private void saveExtractionArgs(String[] args, Map<String, String> config)
  {
    // stash the extraction arguments away, because we will use them later
    // see getFileName()
    
    if((args != null && args.length > 8))
    {
      // save all the arguments in a single parameter
      int argNum = 0;
      StringBuffer cmdArgs = new StringBuffer();
      StringBuffer t1 = new StringBuffer();
      StringBuffer t2 = new StringBuffer();
      for(String arg: args)
      {
        cmdArgs.append(arg + " ");
        if(argNum < 4)
          t1.append(arg + " ");
        else if(argNum < 8)
          t2.append(arg + " ");
          
        argNum++;
      }
      int fileNdex = args.length -1;
      // timestamps look like; Feb 25 13:32:38 2015
      // they are 4 words, Month, day, time, and year
      
      config.put(SmtProperty.SMT_FILE_TSTAMP_1.getName(), t1.toString().trim());
      config.put(SmtProperty.SMT_FILE_TSTAMP_2.getName(), t2.toString().trim());
      config.put(SmtProperty.SMT_WRITE_OMS_HISTORY.getName(), args[fileNdex]);
    }
  }
  
  private void saveAnonymizeArgs(String[] args, Map<String, String> config)
  {
    if((args != null && args.length > 0))
    {
      config.put(SmtProperty.SMT_ANONYMIZE_FILE.getName(), args[0]);
      if(args.length > 1)
        config.put(SmtProperty.SMT_WRITE_OMS_HISTORY.getName(), args[1]);
    }
  }
  
  private void saveFilterArgs(String[] args, Map<String, String> config)
  {
    if((args != null && args.length > 0))
    {
      config.put(SmtProperty.SMT_FILTER_FILE.getName(), args[0]);
      if(args.length > 1)
        config.put(SmtProperty.SMT_WRITE_OMS_HISTORY.getName(), args[1]);
    }
  }
  
  private String getFileName(SmtConfig config)
  {
    Map<String,String> map = config.getConfigMap();
    return convertSpecialFileName(map.get(SmtProperty.SMT_WRITE_OMS_HISTORY.getName()));    
  }
  
  private int getNumToSkip(SmtConfig config)
  {
    Map<String,String> map = config.getConfigMap();
    String val = map.get(SmtProperty.SMT_FILE_NUM_SKIPS.getName());
    return val == null ? 0: Integer.parseInt(val) -1;
  }
  
  private String compressFile(String inFile, SmtConfig config)
  {
    String inputFileName = convertSpecialFileName(inFile);
    int skipNum = getNumToSkip(config);
    String fName  = getFileName(config);
    
    OMS_Collection origHistory;
    try
    {
      origHistory = OMS_Collection.readOMS_Collection(inputFileName);
      
      // create a new, smaller, collection
      int skipCount = 0;
      OMS_Collection newHistory = new OMS_Collection();
      for(OpenSmMonitorService oms: origHistory.getOSM_History().values())
      {
        // put the original
        if(skipCount++ >= skipNum)
        {
          newHistory.put(oms);
          skipCount = 0;
        }
      }
      OMS_Collection.writeOMS_Collection(fName, newHistory);
    }
    catch (Exception e)
    {
      System.err.println("Could not compress file: " + inputFileName + ", to " + fName);
      System.err.println("Exception: " + e.getMessage());
      e.printStackTrace();
    }
    
    return fName;    
  }
  
  private String anonymizeFile(String inFile, SmtConfig config)
  {
    String inputFileName = convertSpecialFileName(inFile);
    OMS_Collection origHistory;
    try
    {
      origHistory = OMS_Collection.readOMS_Collection(inputFileName);
    }
    catch (Exception e1)
    {
      e1.printStackTrace();
      return null;
    }

    OpenSmMonitorService OMS = origHistory.getCurrentOMS();
    SmtAnonymizer anonymizer = getAnonymizer(config, OMS);
    String fName = getFileName(config);

    if ((anonymizer != null) && !anonymizer.hasEmptyAnonymizer())
    {
      try
      {
        // create a new, anonymized, collection
        OMS_AnonymizedCollection newHistory = new OMS_AnonymizedCollection();
        newHistory.setAnonymizerFileName(anonymizer.aProp.getAnonymizerFileName());
        newHistory.setHistoryFileName(inputFileName);
        for (OpenSmMonitorService oms : origHistory.getOSM_History().values())
        {
          // anonymize the original, and add to new History
          //  let the anonymizer do the work
          newHistory.put(SmtAnonymizer.getOpenSmMonitorService(oms, anonymizer));
        }
        OMS_AnonymizedCollection.writeOMS_Collection(fName, newHistory);
      }
      catch (Exception e)
      {
        System.err.println("Could not anonymize file: " + inputFileName + ", to " + fName);
        System.err.println("Exception: " + e.getMessage());
        e.printStackTrace();
      }
    }
    else
      System.err.println("Invalid anonymizer file specification");

    return fName;
  }  

  private String filterFile(String inFile, SmtConfig config)
  {
    String inputFileName = convertSpecialFileName(inFile);
    OMS_Collection origHistory;
    try
    {
      origHistory = OMS_Collection.readOMS_Collection(inputFileName);
    }
    catch (Exception e1)
    {
      e1.printStackTrace();
      return null;
    }

    OpenSmMonitorService OMS = origHistory.getCurrentOMS();
    SmtFilter filter = getFilter(config, OMS);
    String fName = getFileName(config);

    if ((filter != null) && !filter.hasEmptyFilter())
    {
      try
      {
        // create a new, filtered, collection
        OMS_FilteredCollection newHistory = new OMS_FilteredCollection();
        newHistory.setFilterFileName(filter.getFilterFileName());
        newHistory.setHistoryFileName(inputFileName);
        for (OpenSmMonitorService oms : origHistory.getOSM_History().values())
        {
          // filter the original, and add to new History
          newHistory.put(OpenSmMonitorService.getOpenSmMonitorService(oms, filter));
        }
        OMS_FilteredCollection.writeOMS_Collection(fName, newHistory);
      }
      catch (Exception e)
      {
        System.err.println("Could not filter file: " + inputFileName + ", to " + fName);
        System.err.println("Exception: " + e.getMessage());
        e.printStackTrace();
      }
    }
    else
      System.err.println("Invalid filter file specification");

    return fName;
  }  

  /************************************************************
   * Method Name:
   *  getAnonymizer
  **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   * @param config
   * @return
   ***********************************************************/
  private SmtAnonymizer getAnonymizer(SmtConfig config, OpenSmMonitorService OMS)
  {
    Map<String,String> map = config.getConfigMap();
    String val = map.get(SmtProperty.SMT_ANONYMIZE_FILE.getName());
    SmtAnonymizer anonymizer = null;
    
    try
    {
      anonymizer = new SmtAnonymizer(val, OMS);
      if(!anonymizer.hasEmptyAnonymizer())
        System.err.println("Read in an anonymizer file from: " + val);
      else
      {
        System.err.println("Could not read the anonymizer file");
        return null;
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    return anonymizer;
  }

  /************************************************************
   * Method Name:
   *  getFilter
  **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   * @param config
   * @return
   ***********************************************************/
  private SmtFilter getFilter(SmtConfig config, OpenSmMonitorService OMS)
  {
    Map<String,String> map = config.getConfigMap();
    String val = map.get(SmtProperty.SMT_FILTER_FILE.getName());
    SmtFilter filter = null;
    
    try
    {
      filter = new SmtFilter(val, OMS);
      if(!filter.hasEmptyFilter())
        System.err.println("Read in a filter file from: " + val);
      else
      {
        System.err.println("Could not read the filter file");
        return null;
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    return filter;
  }

  private String extractFile(String inFile, SmtConfig config)
  {
    String inputFileName = convertSpecialFileName(inFile);
    
    // getTimeStamp can return null, so be careful
    TimeStamp ts0  = getTimeStamp(0, config);
    TimeStamp ts1  = getTimeStamp(1, config);
    String outFile  = getFileName(config);
    
    OMS_Collection origHistory;
    try
    {
      origHistory = OMS_Collection.readOMS_Collection(inputFileName);
      
      // create a new, smaller, collection, based on a subset of indecies
      OMS_Collection newHistory = new OMS_Collection();
      for(OpenSmMonitorService oms: origHistory.getOSM_History().values())
      {
        TimeStamp t = oms.getTimeStamp();
        // if either of the timestamps are not specified, use the beginning and/or final timestamp
        if(((ts0 == null) || (t.compareTo(ts0) >=0)) && ((ts1 == null) || (t.compareTo(ts1) <= 0)))
          newHistory.put(oms);
      }
      OMS_Collection.writeOMS_Collection(outFile, newHistory);
    }
    catch (Exception e)
    {
      System.err.println("Could not extract file: " + inputFileName + ", to " + outFile);
      System.err.println("Exception: " + e.getMessage());
      e.printStackTrace();
    }
    
    return outFile;    
  }

  private String concatFile(String inFile, SmtConfig config)
  {
    String inputFileName = convertSpecialFileName(inFile);
    int skipNum = getNumToSkip(config);
    String outFile  = getFileName(config);
    
    System.err.println("The skip number is: " + skipNum);
    
    OMS_Collection origHistory;
    
    // skip 0 means don't skip any
    //      1 means skip every other one, or return 1/2 the data
    // skip 9 means skip 9 before returning one, or return 1/10th the data
    
    /* ideally, the input file is just a list of OMS History files,
     * one per line, in time order.  First file is the earliest (oldest),
     * and last file is the latest (most recent).
     * 
     * the output will be one big file (if it can fit)
     * 
     */
    
    // read the file list, and make sure they are all there
    List<String> OMS_Files = getFileList(inputFileName);
    
//    OMS_Files.forEach(SmtFile::printFileInfo);
    
    // create a new, smaller, collection, based on a subset of indecies
    OMS_Collection newHistory = new OMS_Collection();
    for (String fname : OMS_Files)
    {
      String fn = convertSpecialFileName(fname);
      System.out.println(fn);
      
    try
    {
      origHistory = OMS_Collection.readOMS_Collection(fn);
      int num = origHistory.getSize();
      boolean initial = true;
      TimeStamp t = null;
      int skipCount = 0;

      
      for(OpenSmMonitorService oms: origHistory.getOSM_History().values())
      {
        if(initial)
        {
          t = oms.getTimeStamp();
          initial = false;
        }
        if(skipCount++ >= skipNum)
        {
          newHistory.put(oms);
          skipCount = 0;
        }
      }
      System.out.println("  timestamp:   " + t.toString());
      System.out.println("  num records: " + num);
    }
    catch (Exception e)
    {
      System.err.println("Could open file for concatenation: " + inputFileName + ", to " + outFile);
      System.err.println("Exception: " + e.getMessage());
      e.printStackTrace();
    }
    
    }
    
    /* have the new collection, lets try to save it */
    try
    {
      OMS_Collection.writeOMS_Collection(outFile, newHistory);
    }
    catch (IOException e)
    {
      System.err.println("Could not concatenate file: " + inputFileName + ", to " + outFile);
      System.err.println("Exception: " + e.getMessage());
      e.printStackTrace();
    }
    return outFile;    
  }
  
  private List<String> getFileList(String fileName)
  {
    List<String> fileList = new ArrayList<String>();

    try
    {
      BufferedReader br = Files.newBufferedReader(Paths.get(fileName));
      //br returns as stream and convert it into a List
      fileList = br.lines().collect(Collectors.toList());
      
      // trim empty lines from list
      fileList.removeIf(s -> s.isEmpty());

      // trim comment lines from list
      fileList.removeIf(s -> s.startsWith("#"));
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    return fileList;
  }

  private TimeStamp getTimeStamp(int i, SmtConfig config)
  {
    Map<String,String> map = config.getConfigMap();

    // only allow 0 or 1
    if((i < 0) || (i > 1))
      return null;
      
    // always get timestamp 1
    String val = map.get(SmtProperty.SMT_FILE_TSTAMP_1.getName());
    if(val != null)
    {
      TimeStamp ts1 = new TimeStamp(val);
        
      if(i == 0)
        return ts1;
      
      // asking for second timestamp
      String val2 = map.get(SmtProperty.SMT_FILE_TSTAMP_2.getName());
      if(val.equalsIgnoreCase(val2) || (val2 == null))
        return null;
      
      return new TimeStamp(val2);
     }
    return null;
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
    System.exit((new SmtFile().execute(args)) ? 0: -1);
  }

}
