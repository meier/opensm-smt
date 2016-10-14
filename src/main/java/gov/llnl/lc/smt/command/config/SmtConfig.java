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
 *        file: SmtConfig.java
 *
 *  Created on: Jan 9, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command.config;

import gov.llnl.lc.smt.SmtConstants;
import gov.llnl.lc.smt.command.SmtCommand;
import gov.llnl.lc.smt.props.CommandProperties;
import gov.llnl.lc.smt.props.SmtProperties;
import gov.llnl.lc.smt.props.SmtProperty;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**********************************************************************
 * This is the SMT Command responsible for handling command configuration.
 * It is important to note that the actual configuration of commands
 * is specified as a (persistent) preference, or on the command line.
 * Configuration via the command line always takes precedence over
 * preferences.
 *
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Jan 9, 2013 8:44:21 AM
 **********************************************************************/
public class SmtConfig  extends SmtCommand
{
  /************************************************************
   * Method Name:
   *  SmtConfig
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   ***********************************************************/
  public SmtConfig()
  {
    super();
    
    // initialize the map with the default crap (over write as needed later)
    SmtProperties sProp = new SmtProperties();
    
    // put these in the map
    sConfigMap.put(SmtProperty.SMT_HOST.getName(), sProp.gProp.nProp.getHostName());
    sConfigMap.put(SmtProperty.SMT_PORT.getName(), Integer.toString(sProp.gProp.nProp.getPortNumber()));
    sConfigMap.put(SmtProperty.SMT_LOG_FILE.getName(), sProp.gProp.nProp.getProperty(SmtProperty.SMT_LOG_FILE.getPropertyName()));
    sConfigMap.put(SmtProperty.SMT_LOG_LEVEL.getName(), sProp.gProp.nProp.getProperty(SmtProperty.SMT_LOG_LEVEL.getPropertyName()));
    createDefaultDir();
    
    // save the number of arguments
    sConfigMap.put(CommandProperties.SCMD_NUM_ARGS, Integer.toString(0));
    sConfigMap.put(CommandProperties.SCMD_FILE_SPECIFIED, Boolean.toString(false));
    sConfigMap.put(CommandProperties.SCMD_HOST_SPECIFIED, Boolean.toString(false));
    sConfigMap.put(CommandProperties.SCMD_PORT_SPECIFIED, Boolean.toString(false));
  }
  
  /************************************************************
   * Method Name:
   *  isOmsSpecified
  **/
  /**
   * True is a method to obtain the OMS is specified.  Generally, this
   * means either a file name is given with the -rH option, or a host and
   * port number is given with the -h and -pn options.
   * 
   * If neither of these were specified, this function will return false.
   * 
   * Either a persisted value can be used, or a default value can be used
   * to obtain the service.
   *
   * @see     describe related java objects
   *
   * @return
   ***********************************************************/
  public boolean isOmsSpecified()
  {
    boolean bf = Boolean.valueOf(sConfigMap.get(CommandProperties.SCMD_FILE_SPECIFIED));
    boolean bh = Boolean.valueOf(sConfigMap.get(CommandProperties.SCMD_HOST_SPECIFIED));
    boolean bp = Boolean.valueOf(sConfigMap.get(CommandProperties.SCMD_PORT_SPECIFIED));
    // return true if host or port is specified on the command line
    
    return bf || bh || bp;
  }

  private Map<String,String> sConfigMap = new HashMap<String,String>();

  /************************************************************
   * Method Name:
   *  getsConfigMap
   **/
  /**
   * Returns the value of sConfigMap
   *
   * @return the sConfigMap
   *
   ***********************************************************/
  
  public Map<String, String> getConfigMap()
  {
    return sConfigMap;
  }

  public static SmtConfig readConfig(String fileName) throws FileNotFoundException
  {
    SmtConfig c = new SmtConfig();
    c.sConfigMap = SmtConfig.readConfigFile(fileName);
    return c;
  }

  
  public static Map<String,String> readConfigFile(String fileName) throws FileNotFoundException
  {
    // return null if can't be read, for any reason
    logger.info("Reading config from: " + fileName);
    Map<String,String> config = null;
    
    if(fileName != null)
    {
      // Create input stream.
      FileInputStream fis;
      try
      {
        fis = new FileInputStream(fileName);
        // Create XML decoder.
        XMLDecoder xdec = new XMLDecoder(fis);

        // Read object.
        config = (HashMap<String,String>) xdec.readObject();
        
        // override this config file, with the actual filename (may already be correct)
        config.put(SmtProperty.SMT_READ_CONFIG.getName(), fileName);

      }
      catch (FileNotFoundException e)
      {
        logger.severe("Unable to read config from: " + fileName);
        throw(e);
      }
    }
    return config;
  }
  
  public static boolean writeConfigFile(String fileName, Map<String,String> config)
  {
    boolean success = false;
    logger.info("Writing config to: " + fileName);
    if((fileName != null) && (config != null))
    {
      FileOutputStream fos;
      try
      {
        fos = new FileOutputStream(fileName);
        XMLEncoder xenc = new XMLEncoder(fos);
        
        // save this file name within the config
        config.put(SmtProperty.SMT_WRITE_CONFIG.getName(), fileName);
        
        // Write object.
       xenc.writeObject(config);
       xenc.close();
       success = true;
      }
      catch (FileNotFoundException e)
      {
        logger.severe("Unable to write to: " + fileName);
        // TODO Auto-generated catch block
        e.printStackTrace();
      }      
    }
    else
    {
      
    }
    return success;
  }
    
  public Map<String,String> parseCommandFile(String[] args, SmtCommand command)
  {
    // assume the command line arguments have already been read
    SmtProperty sp = SmtProperty.SMT_READ_CONFIG;
    Map<String,String> config = null;

    String fileName = sConfigMap.get(sp.getName());
    if(fileName != null)
    {
      String fName = convertSpecialFileName(fileName);
      try
      {
        config = readConfigFile(fName);
      }
      catch (FileNotFoundException e)
      {
        // TODO Auto-generated catch block
        logger.severe("Config file not found: " + fName);
      }
    }
    return config;
  }
  
  /************************************************************
   * Method Name:
   *  parseCommandLineOptions
  **/
  /**
   * The primary object of this method is to parse the options
   * to construct the sConfigMap map object, which contains the
   * configuration name/value pairs.
   *
   * @see     describe related java objects
   *
   * @param args
   * @param options
   * @return true if there are any valid options specified
   ***********************************************************/
  public boolean parseCommandLineOptions(String[] args, SmtCommand command)
  {
    boolean status = false;
 
    Options options = command.getOptions();
    CommandLine line = null;
    
    // save the number of arguments
    sConfigMap.put(CommandProperties.SCMD_NUM_ARGS, Integer.toString(args.length));
    
    if((args.length < 1) || (options == null))
    {
      System.err.println("No args or options, ending");
      return status;
    }
    
    CommandLineParser parser = new GnuParser();
    try 
    {
        // parse the command line arguments
        line = parser.parse( options, args, false );
    }
    catch( ParseException exp )
    {
        // oops, something went wrong
        System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
        //return false;
    }
    
    if(line == null)
    {
      System.err.println("Parsed line is null");
      return false;
    }
    
    // do the global things first
    SmtProperty sp = SmtProperty.SMT_HELP;
    if((args.length < 1) || line.hasOption(sp.getName()))
    {
//      System.err.println("Parsed line is for help");
      return false;
    }
    
    sp = SmtProperty.SMT_VERSION;
    if(line.hasOption(sp.getName()))
    {
      sConfigMap.put(sp.getName(), line.getOptionValue(sp.getName()));
      sConfigMap.put(SmtProperty.SMT_COMMAND.getName(), sp.getName());
      return true;
    }
    
    sp = SmtProperty.SMT_ABOUT_COMMAND;
    if(line.hasOption(sp.getName()))
    {
      sConfigMap.put(sp.getName(), line.getOptionValue(sp.getName()));
      sConfigMap.put(SmtProperty.SMT_COMMAND.getName(), sp.getName());
      return true;
    }
    
    sp = SmtProperty.SMT_READ_CONFIG;
    if(line.hasOption(sp.getName()))
    {
      status = true;  // a valid argument
      sConfigMap.put(sp.getName(), line.getOptionValue(sp.getName()));
      
      // if a config file is provided, then its values should NOT override
      // arguments provided on the command line.  So handle it this way;
      //
      // 1.  process the config file first
      // 2.  use command line args to over-write or add to the config
      Map<String,String> config = null;

      config = parseCommandFile(args, command);
      
      // if config exists and not empty, then copy or replace the existing config
      if((config != null) && (!config.isEmpty() ))
      {
        // just over-write anything that might already exist
        for (Map.Entry<String, String> entry : config.entrySet())
        {
          sConfigMap.put(entry.getKey(), entry.getValue());
        }
      }
    }
        
    sp = SmtProperty.SMT_HOST;
    if(line.hasOption(sp.getName()))
    {
      status = true;  // a valid argument

      sConfigMap.put(sp.getName(), line.getOptionValue(sp.getName()));
      sConfigMap.put(CommandProperties.SCMD_HOST_SPECIFIED, Boolean.toString(true));
    }

    sp = SmtProperty.SMT_PORT;
    if(line.hasOption(sp.getName()))
    {
      status = true;  // a valid argument
      sConfigMap.put(sp.getName(), line.getOptionValue(sp.getName()));
      sConfigMap.put(CommandProperties.SCMD_PORT_SPECIFIED, Boolean.toString(true));
    }

    sp = SmtProperty.SMT_READ_OMS_HISTORY;
    if(line.hasOption(sp.getName()))
    {
      // save this, only if its a valid file
      status = putHistoryProperty(sConfigMap, line.getOptionValue(sp.getName()));
      if(status)
      {
        sConfigMap.put(CommandProperties.SCMD_FILE_SPECIFIED, Boolean.toString(true));        
      }
    }

    sp = SmtProperty.SMT_REUSE;
    if(line.hasOption(sp.getName()))
    {
      status = true;  // a valid argument
      sConfigMap.put(sp.getName(), line.getOptionValue(sp.getName()));
    }

    sp = SmtProperty.SMT_LOG_FILE;
    if(line.hasOption(sp.getName()))
    {
      status = true;  // a valid argument
      sConfigMap.put(sp.getName(), line.getOptionValue(sp.getName()));
    }

    sp = SmtProperty.SMT_LOG_LEVEL;
    if(line.hasOption(sp.getName()))
    {
      status = true;  // a valid argument
      sConfigMap.put(sp.getName(), line.getOptionValue(sp.getName()));
    }

    // fix any changes to the logging system (configLogging must be first, or may be skipped)
    status = configLogging() && status;
    
    // finally, any command specific things (parseCommands must be first, or may be skipped)
    status = command.parseCommands(sConfigMap, line) && status;
    
    return status;
  }
  
  public static boolean createDefaultDir()
  {
    // the messages won't work if the logger is not yet initialized
    File dDir = new File(SmtConstants.SMT_DEFAULT_DIR);
    if(dDir.mkdirs())
      logger.info("Created the default SMT directory (" + SmtConstants.SMT_DEFAULT_DIR + ")");
    return createCacheDir();
  }
  
  public static boolean createCacheDir()
  {
    // the messages won't work if the logger is not yet initialized
    String cNam = SmtConstants.SMT_DEFAULT_DIR + System.getProperty("file.separator") + SmtConstants.SMT_CACHE_DIR;
    File cDir = new File(cNam);
    if(cDir.mkdirs())
      logger.info("Created the cache SMT directory (" + cNam + ")");
    return true;
  }
  
  
  /************************************************************
   * Method Name:
   *  printUsage
  **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   * @param options
   ***********************************************************/
  private boolean configLogging()
  {
    boolean status = false;
    // fix any changes to the logging system
    if (sConfigMap.containsKey(SmtProperty.SMT_LOG_FILE.getName()))
    {
      status = true;  // a valid argument
      String pattern = sConfigMap.get(SmtProperty.SMT_LOG_FILE.getName());

      java.util.logging.Handler hpcHandlerF = null;
      try
      {
        // set the file pattern
        hpcHandlerF = new java.util.logging.FileHandler(pattern, true);
      }
      catch (SecurityException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      catch (IOException e)
      {
        // TODO Auto-generated catch block, probably because path to logging is not set up
        // a typical error is;
        // java.io.IOException: Couldn't get lock for %h/.smt/smt-console%u.log
        e.printStackTrace();
      }
      logger.addHandler(hpcHandlerF);
      Handler[] lhand = logger.getHandlers();

      if (lhand.length > 1)
      {
        // changing to a new handler, get rid of the original one
        lhand[0].close();
        logger.removeHandler(lhand[0]);
        logger.info("Replaced the original log handler");
      }
      // Done, may want to clean up previous log file,if any??

    }

    if (sConfigMap.containsKey(SmtProperty.SMT_LOG_LEVEL.getName()))
    {
      status = true;  // a valid argument
      Level level = logger.getLevel(); // just keep the default
      try
      {
        Level l = Level.parse(sConfigMap.get(SmtProperty.SMT_LOG_LEVEL.getName()));
        level = l;
      }
      catch (IllegalArgumentException iae)
      {

      }
      logger.info("Setting the log level to: " + level.toString());
      logger.setLevel(level);
    }
    return status;
  }

  public boolean saveConfig(String fileName)
  {
    if(fileName != null)
    {
      String fName = convertSpecialFileName(fileName);
      return writeConfigFile(fName, sConfigMap);
    }
    return false;
  }

  public boolean printConfig()
  {
    return printConfig(sConfigMap);
  }

  public static boolean printConfig(Map<String, String> config)
  {
    for (Map.Entry<String, String> entry : config.entrySet())
    {
        System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
    }
    return true;
  }
  
  public String getSubCommand(String command)
  {
    for (Map.Entry<String, String> entry : sConfigMap.entrySet())
    {
      if(entry.getKey().equals(command))
        return entry.getValue();
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
    System.exit((new SmtConfig().execute(args)) ? 0: -1);
  }

  @Override
  public boolean parseCommands(Map<String, String> config, CommandLine line)
  {
    boolean status = true;
    
    // set the command and sub-command (always do this)
    config.put(SmtProperty.SMT_COMMAND.getName(), this.getClass().getName());
    
    // now set or override anything that might be in the config file, with command line args
    SmtProperty sp = SmtProperty.SMT_LIST;
    if(line.hasOption(sp.getName()))
    {
      // allow multiple commands, list can be done with others
      config.put(sp.getName(), SmtProperty.SMT_SUBCOMMAND.getName());
      config.put(SmtProperty.SMT_SUBCOMMAND.getName(), sp.getName());
    }

    sp = SmtProperty.SMT_WRITE_CONFIG;
    if(line.hasOption(sp.getName()))
    {
      config.put(SmtProperty.SMT_SUBCOMMAND.getName(), sp.getName());
      config.put(sp.getName(), line.getOptionValue(sp.getName()));
    }
    return status;
  }

  @Override
  public boolean doCommand(SmtConfig config) throws Exception
  {
    boolean success = true;
    
    Map<String,String> map = smtConfig.getConfigMap();
    
    SmtProperty sp = SmtProperty.SMT_LIST;   
    if(map.get(sp.getName()) != null)
    {
      success = success && smtConfig.printConfig();
    }

    sp = SmtProperty.SMT_WRITE_CONFIG;   
    if(map.get(sp.getName()) != null)
    {
      String fname = map.get(SmtProperty.SMT_WRITE_CONFIG.getName());
      success = success && smtConfig.saveConfig(fname);
    }
    return success;
  }

  @Override
  public boolean init()
  {
    USAGE = "smt-config [-rC <filename>] [-wC <filename>] [-lp]";
    HEADER = "smt-config - a tool for reading, writing, and parsing SMT Configuration settings";
    EXAMPLE ="examples:" + SmtConstants.NEW_LINE +
        "> smt-config -rC %h/.smt/config.file -lp    - read and list the contents of config.file" + SmtConstants.NEW_LINE + 
        "> smt-config -wC newConfig.file             - write current configuration to newConfig.file" + SmtConstants.NEW_LINE  + ".";  // terminate with nl

    // create and initialize the minimum options for this command
//    initCommonOptions();
    initMinimumOptions();
    
    SmtProperty sp = SmtProperty.SMT_LIST;
    Option list = new Option( sp.getShortName(), sp.getName(), false, sp.getDescription() );    
 
    sp = SmtProperty.SMT_WRITE_CONFIG;
    Option wFile     = OptionBuilder.hasArg(true).hasArgs(1).withArgName( sp.getArgName() ).withValueSeparator('=').withDescription(  sp.getDescription() ).withLongOpt(sp.getName()).create( sp.getShortName() );

    options.addOption( wFile );
    options.addOption( list );
    
    return false;
  }
  
  /************************************************************
   * Method Name:
   *  toInfo
  **/
  /**
   * Describe the method here
   *
   * @see java.lang.Object#toString()
   *
   * @return
   ***********************************************************/
  
  public String toInfo()
  {
    StringBuffer bf = new StringBuffer();
    bf.append(SmtConfig.class.getSimpleName() + "\n");

    for (Map.Entry<String, String> entry : sConfigMap.entrySet())
    {
      bf.append(entry.getKey() + ": " + entry.getValue() + "\n");
    }
    return bf.toString();
  }
  
  /************************************************************
   * Method Name:
   *  toTimeString
  **/
  /**
   * Returns a list of TimeStamps for this object
   *
   * @see     describe related java objects
   *
   * @return
   ***********************************************************/
  public String toTimeString()
  {
    return "No Time Info\n";
  }

}
