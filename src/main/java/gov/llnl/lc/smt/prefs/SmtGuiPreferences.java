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
 *        file: SmtGuiPreferences.java
 *
 *  Created on: Oct 31, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.prefs;

import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.command.SmtCommand;

import java.awt.Rectangle;

import javax.swing.JCheckBoxMenuItem;


/**********************************************************************
 * Describe purpose and responsibility of SmtGuiPreferences
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Oct 31, 2013 4:47:40 PM
 **********************************************************************/
public class SmtGuiPreferences implements CommonLogger
{
//these are the normal System Properties, which I may want convenience methods

public static final String JVM_VERSION         = "java.vm.version";
public static final String JVM_VENDOR          = "java.vm.vendor";
public static final String PATH_SEPERATOR      = "path.separator";
public static final String USER_COUNTRY        = "user.country";
public static final String OS_PATCH_LEVEL      = "sun.os.patch.level";
public static final String OS_ENDIAN_TYPE      = "sun.cpu.endian";
public static final String JRE_VERSION         = "java.runtime.version";
public static final String OS_ARCHITECTURE     = "os.arch";
public static final String OS_NAME             = "os.name";
public static final String JAVA_CLASS_VERSION  = "java.class.version";
public static final String OS_VERSION          = "os.version";
public static final String JAVA_SPEC_VERSION   = "java.specification.version";
public static final String USER_LANGUAGE       = "user.language";
public static final String USER_DIR            = "user.dir";
public static final String USER_NAME           = "user.name";
public static final String JAVA_VERSION        = "java.version";
public static final String FILE_SEPERATOR      = "file.separator";
public static final String LINE_SEPERATOR      = "line.separator";

/** default value for the installation directory **/
/** this is where the other directories (persist,config,cache) go **/
public static final String DEFAULT_INSTALL_PATH_LINUX = "%h/.smt";

/** default value for the installation directory **/
public static final String DEFAULT_INSTALL_PATH_WIN32 = "C:\\smt";

/** default value for installation directory **/
private static final String DEFAULT_DATA_DIR = java.io.File.separator;

/** string for the installation directory node name **/
private static final String DATA_DIR_NODE_NAME = "DataDir";

/** default value (relative) for the persistence directory **/
private static final String DEFAULT_PERSIST_PATH = "persist";

/** default value (relative) for configuration directory **/
private static final String DEFAULT_CONFIG_PATH = "config";

/** default value (relative) for the cache directory **/
private static final String DEFAULT_CACHE_PATH = "cache";

/** string for the persistence directory node name **/
private static final String PERSIST_DIR_NODE_NAME = "PersistDir";
                                                  
/** string for the cache directory node name **/
private static final String CACHE_DIR_NODE_NAME = "CacheDir";

/** string for the configuration directory node name **/
private static final String CONFIG_NODE_NAME = "Config";

/** string for the configuration directory node name **/
private static final String CONFIG_DIR_NAME = "ConfigDir";

/** string for the Application Thread node name **/
private static final String SMT_PROCESS_NODE_NAME  = "Process";
                                                  
/** string for the process id parameter **/
private static final String SMT_PROCESS_ID  = "ProcessId";
                                                  
/** string for the user id parameter **/
private static final String SMT_USER_ID     = "UserId";
                                                  
/** string for the user name parameter **/
private static final String SMT_USER_NAME        = "UserName";
                                                  
/** string for the default system preferences information node **/
private static final String SMT_SYSTEM_NODE_NAME    = "SystemPrefs";

/** string for the Max Memory preference **/
private static final String SMT_MAX_MEMORY  = "MaxMemory";                                                  

/** string representing previous OMS types **/
private static final String SMT_GUI_HIST_1  = "SmtGuiHistory1";                                                  
private static final String SMT_GUI_HIST_2  = "SmtGuiHistory2";                                                  
private static final String SMT_GUI_HIST_3  = "SmtGuiHistory3";                                                  
private static final String SMT_GUI_HIST_4  = "SmtGuiHistory4";                                                  
private static final String SMT_GUI_HIST_5  = "SmtGuiHistory5";                                                  

private static final String NEW_LINE = System.getProperty(LINE_SEPERATOR);

/** the root preference node for the system **/
  private static java.util.prefs.Preferences  mySystemRoot =
      java.util.prefs.Preferences.systemRoot();

//the userNode stuff goes in ~/.java/.userPrefs for the class
  private static java.util.prefs.Preferences  mySmtSystem =
  java.util.prefs.Preferences.userNodeForPackage
  (
      gov.llnl.lc.smt.prefs.SmtGuiPreferences.class
  );

  private static java.util.prefs.Preferences  mySmtRoot =
      java.util.prefs.Preferences.userNodeForPackage
      (gov.llnl.lc.smt.command.gui.SmtGui.class);

  /** preference node for parameter DataDir **/
  private static java.util.prefs.Preferences  myDataDir =
      mySmtSystem.node(DATA_DIR_NODE_NAME);

  /** preference node for parameter ConfigDir **/
  private static java.util.prefs.Preferences  myConfig =
      mySmtSystem.node(CONFIG_NODE_NAME);

  /** preference node for parameter TempDir **/
  private static java.util.prefs.Preferences  myCacheDir =
      mySmtSystem.node(CACHE_DIR_NODE_NAME);

  /** preference node for parameter PersistDir **/
  private static java.util.prefs.Preferences  myPersistDir =
      mySmtSystem.node(PERSIST_DIR_NODE_NAME);

    /** preference node for the process or thread parameters **/
    private static java.util.prefs.Preferences  SMT_Instance =
        mySmtSystem.node(SMT_PROCESS_NODE_NAME);

    /** preference node for the System Preferences **/
    private static java.util.prefs.Preferences  myPreferences =
        mySmtSystem.node(SMT_SYSTEM_NODE_NAME);

    static
    {
      // if not windows, then assume it is Linux
      if (SmtGuiPreferences.isOsWindows())
      {
        SmtGuiPreferences.setDirectories(SmtGuiPreferences.DEFAULT_INSTALL_PATH_WIN32);
      }
      else
      {
        SmtGuiPreferences.setDirectories(SmtGuiPreferences.DEFAULT_INSTALL_PATH_LINUX);
      }
    }
    
    /**
    * Private constructor because this class is not meant to be constructed.
    * Only class (static) methods are provided.
    **/
    private SmtGuiPreferences()
    {
    }
    
    static public String getDataDirName()
    {
        return SmtCommand.convertSpecialFileName(myDataDir.get(DATA_DIR_NODE_NAME, DEFAULT_DATA_DIR));
    }
 
    static public java.io.File getDataDir()
    {
        return new java.io.File( getDataDirName() );
    }
 
    static public boolean setDataDir(java.io.File dir)
    {
        boolean status = false;     // return status
        if ( dir.isDirectory() )
        {
            myDataDir.put( DATA_DIR_NODE_NAME, dir.toString() );
            status = true;
        }
        return status;
    }

    static public boolean setDataDir(String dirName)
    {
         return setDataDir( new java.io.File(dirName) );
    }
 
    static public boolean resetDataDir(String dirName)
    {
      boolean success = false;
      
      if ( setDataDir(new java.io.File(dirName)) )
      {
        // clear these guys out so the new defaults will be used
        myConfig.remove(CONFIG_DIR_NAME);
        myPersistDir.remove(PERSIST_DIR_NODE_NAME);
        myCacheDir.remove(CACHE_DIR_NODE_NAME);
        
        success = true;
      }
      return success;
    }
    
    static public String getConfigDirName()
    {
        return myConfig.get(
            CONFIG_DIR_NAME,
            getDataDirName() + java.io.File.separator + DEFAULT_CONFIG_PATH
            );
    }

    static public java.io.File getConfigDir()
    {
        return new java.io.File( getConfigDirName() );
    }

    static public boolean setConfigDir(java.io.File dir)
    {
        boolean status = false;     // return status

        if ( dir.isDirectory() )
        {
            myConfig.put( CONFIG_DIR_NAME, dir.toString() );
            status = true;
        }
        return status;
    }

    static public boolean setConfigDir(String dirName)
    {
        return setConfigDir( new java.io.File(dirName) );
    }
 
    static public String getCacheDirName()
    {
        return myCacheDir.get(
            CACHE_DIR_NODE_NAME,
            getDataDirName() + java.io.File.separator + DEFAULT_CACHE_PATH
            );
    }

    static public java.io.File getCacheDir()
    {
        return new java.io.File( getCacheDirName() );
    }

    static public boolean setCacheDir(java.io.File dir)
    {
        boolean status = false;     // return status

        if ( dir.isDirectory() )
        {
            myCacheDir.put( CACHE_DIR_NODE_NAME, dir.toString() );
            status = true;
        }
        return status;
    }

    static public boolean setCacheDir(String dirName)
    {
         return setCacheDir( new java.io.File(dirName) );
    }
    
    static public String getPersistDirName()
    {
        return myPersistDir.get(
            PERSIST_DIR_NODE_NAME,
            getDataDirName() + java.io.File.separator + DEFAULT_PERSIST_PATH );
    }
     static public java.io.File getPersistDir()
    {
        return new java.io.File( getPersistDirName() );
    }

    static public boolean setPersistDir(java.io.File dir)
    {
        boolean status = false;     // return status

        if ( dir.isDirectory() )
        {
            myPersistDir.put( PERSIST_DIR_NODE_NAME, dir.toString() );
            status = true;
        }
        return status;
    }
     static public boolean setPersistDir(String dirName)
    {
         return setPersistDir( new java.io.File(dirName) );
    }
     
     static public int getNumberOfProcessors()
     {
       return (Runtime.getRuntime().availableProcessors());
     }
     
     static public long getMaximumMemoryAvailable()
     {
        java.lang.Long value = new java.lang.Long(myPreferences.get(SMT_MAX_MEMORY, "200000000"));
          return value.longValue();
     }
 
     static public boolean setMaximumMemoryAvailable(long value)
     {
         myPreferences.put( SMT_MAX_MEMORY, (new Long(value)).toString() );
         return true;
     }

     static public boolean isOsWindows()
     {
       return System.getProperty(OS_NAME).startsWith("Window");
     }

     static public long getProcessId()
     {
        java.lang.Double id = new java.lang.Double(SMT_Instance.get(SMT_PROCESS_ID, "0"));
         return id.longValue();
     }
     static public boolean setProcessId(long id)
    {
        SMT_Instance.put( SMT_PROCESS_ID, (new Long(id)).toString() );
        return true;
    }

    static public boolean setHist_1(String currentHistory)
    {
      // get the current value at history 1, if empty or null, replace it with this
      //
      // if not null, push prior history 1, to history 2, and then set history 1 to
      // current value
      
      // The value is the ActionCommand, specifically the filename -OR- host and port number
      String actionCommand = getHist_1();
      if((" ".equals(actionCommand)) || (actionCommand == null))
      {
        SMT_Instance.put( SMT_GUI_HIST_1, currentHistory );
        return true;
      }
      
      // do nothing if the current history is the same as history 1
      if((actionCommand != null) && actionCommand.equals(currentHistory))
        return true;
      
      // do a LIFO type push, move 4 to 5, 3 to 4, 2 to 3, 1 to 2, then save this one
      SMT_Instance.put( SMT_GUI_HIST_5, getHist_4() );
      SMT_Instance.put( SMT_GUI_HIST_4, getHist_3() );
      SMT_Instance.put( SMT_GUI_HIST_3, getHist_2() );
      SMT_Instance.put( SMT_GUI_HIST_2, getHist_1() );
        
      SMT_Instance.put( SMT_GUI_HIST_1, currentHistory );
      return true;
    }

    static public String getHist_1()
    {
       return SMT_Instance.get( SMT_GUI_HIST_1, " " );
    }

    static public String getHist_2()
    {
       return SMT_Instance.get( SMT_GUI_HIST_2, " " );
    }

    static public String getHist_3()
    {
       return SMT_Instance.get( SMT_GUI_HIST_3, " " );
    }

    static public String getHist_4()
    {
       return SMT_Instance.get( SMT_GUI_HIST_4, " " );
    }

    static public String getHist_5()
    {
       return SMT_Instance.get( SMT_GUI_HIST_5, " " );
    }

     static public boolean setBounds(Rectangle r)
    {
       SMT_Instance.put( "x", (new Integer((int)r.getX())).toString() );
       SMT_Instance.put( "y", (new Integer((int)r.getY())).toString() );
       SMT_Instance.put( "width", (new Integer((int)r.getWidth())).toString() );
       SMT_Instance.put( "height", (new Integer((int)r.getHeight())).toString() );
       return true;
    }

     static public Rectangle getBounds()
    {
       Integer x      = new Integer(SMT_Instance.get( "x", "100" ));
       Integer y      = new Integer(SMT_Instance.get( "y", "100" ));
       Integer width  = new Integer(SMT_Instance.get( "width", "1750" ));
       Integer height = new Integer(SMT_Instance.get( "height", "1000" ));
       return new Rectangle(x, y, width, height);
    }

     static public boolean setHelpBounds(Rectangle r)
    {
       SMT_Instance.put( "hx", (new Integer((int)r.getX())).toString() );
       SMT_Instance.put( "hy", (new Integer((int)r.getY())).toString() );
       SMT_Instance.put( "hwidth", (new Integer((int)r.getWidth())).toString() );
       SMT_Instance.put( "hheight", (new Integer((int)r.getHeight())).toString() );
       return true;
    }

     static public Rectangle getHelpBounds()
    {
       Integer x      = new Integer(SMT_Instance.get( "hx", "100" ));
       Integer y      = new Integer(SMT_Instance.get( "hy", "100" ));
       Integer width  = new Integer(SMT_Instance.get( "hwidth", "1750" ));
       Integer height = new Integer(SMT_Instance.get( "hheight", "1000" ));
       return new Rectangle(x, y, width, height);
    }

     static public long getUserId()
     {
        java.lang.Double id = new java.lang.Double(SMT_Instance.get(SMT_USER_ID, "-1"));
          return id.longValue();
     }

     static public boolean setUserId(long id)
     {
         SMT_Instance.put( SMT_USER_ID, (new Long(id)).toString() );
         return true;
     }

     static public JCheckBoxMenuItem getCheckBox(JCheckBoxMenuItem cbmi)
     {
       // look for the corresponding checkbox, and change its state, then return it
       if(cbmi != null)
       {
         Boolean selected = new Boolean(SMT_Instance.get( cbmi.getActionCommand(), "true" ));
         cbmi.setSelected(selected.booleanValue());
        }
       return cbmi;
     }

     static public boolean setCheckBox(JCheckBoxMenuItem cbmi)
     {
       if(cbmi == null)
         return false;
       SMT_Instance.put( cbmi.getActionCommand(), Boolean.toString(cbmi.isSelected()));
       return true;
     }

     static public String getUserName()
     {
        return System.getProperty(SmtGuiPreferences.USER_NAME);
     }

     static public boolean setUserName()
     {
         SMT_Instance.put( SMT_USER_NAME, System.getProperty(SmtGuiPreferences.USER_NAME) );
         return true;
     }

     static public boolean isOsLinux()
     {
       // assume Linux, if not windows
       return !isOsWindows();
     }

     /**************************************************************************
      *** Method Name:
      ***     setDirectories
      **/
      /**
      *** Using the supplied name for the installation (data) directory, all necessary
      *** directories are created if necessary.  Also (dependent) subdirectories
      *** are redefined.  Previous settings are checked to see if this is an new
      *** installation, or just moving an old one.  Conditionally, old settings can
      *** be moved from the old location to the new one (Not Yet Implemented).
      *** <p>
      ***
      *** @see          SmtGuiPreferences#setDataDir
      ***
      *** @param        dataDir  the new root directory for SmtGui
      ***
      *** @return       true, if the operation completes successfully
      **************************************************************************/

      public static boolean setDirectories(String newDataDir)
      {
        boolean newInstallation = true;
        boolean success         = true;

      // does the new installation directory exist?
      // is there an old installation directory in persistant storage?

        String origDataDir = SmtCommand.convertSpecialFileName(SmtGuiPreferences.getDataDirName());
        String dataDir     = SmtCommand.convertSpecialFileName(newDataDir);

        java.io.File nid = new java.io.File(dataDir);
        java.io.File oid = null;
        
        if(origDataDir != null)
          oid = new java.io.File(origDataDir);
       
        // first, check to see if the supplied install dir is already our current value
        newInstallation = (oid == null) ? true: !(nid.getAbsolutePath().equalsIgnoreCase(oid.getAbsolutePath()));
        
        if (newInstallation)
        {
          if ( (oid != null) && (oid.isDirectory()) )
          {
            // it appears like we had an old installation, do we want to save some
            // data from the previous environment or just
            // start fresh?
            logger.warning("\n*** SmtGui has detected a previous installation in" +
                "\n*** the (" + origDataDir + ") directory, but is NOT" +
                "\n*** taking any action to copy over old configuration or" +
                "\n*** other persistent data.\n");
                
            logger.warning("\n*** Using " + dataDir + ") directory for all configuration" + 
                "\n*** and cluster data.\n");
                
            // set a flag to let us know that this is not a virgin system
            newInstallation = false;                    
          }
          
          // okay set up the system as if it is brand spankin new
          if (nid.mkdirs())
          {
            logger.severe("Created directory (" + dataDir + ")");
          }
          else
          {
            logger.severe("Could not create directory (" + dataDir + ")");
          }
          
          if (SmtGuiPreferences.resetDataDir(dataDir))
          {
            // get the new default values and create some dirs
            success = true;
            
            // create the new dirs, and okay to redefine them, must copy over utils
            java.io.File d = SmtGuiPreferences.getConfigDir();
            if (d.mkdirs())
            {
              logger.info("Created directory (" + d.getName() + ")");
            }
            SmtGuiPreferences.setConfigDir(d);
          
            d = SmtGuiPreferences.getPersistDir();
            if (d.mkdirs())
            {
              logger.info("Created directory (" + d.getName() + ")");
            }
            SmtGuiPreferences.setPersistDir(d);
          
          
            d = SmtGuiPreferences.getCacheDir();
            if (d.mkdirs())
            {
              logger.info("Created directory (" + d.getName() + ")");
            }
            SmtGuiPreferences.setCacheDir(d);
          }
        }
        else
        {
          success = true;
          
          // no change, so take no action except to provide a message
          logger.config("\n*** The supplied installation directory (" + dataDir + ")" +
                         "\n*** is the same as the current setting.  You do not need to" +
                         "\n*** specify the installation directory at each invocation, " +
                         "\n*** it is only necessary the first time, or when making a change.\n");
        }

        return success;
      }
      /*-----------------------------------------------------------------------*/

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
    System.out.println(SmtGuiPreferences.toStaticString());
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
  
  public static String toStaticString()
  {
    StringBuffer buff = new StringBuffer();
    
    buff.append("DataDirName     = " + SmtGuiPreferences.getDataDirName());
    buff.append(NEW_LINE);
    buff.append("DataDir         = " + SmtGuiPreferences.getDataDir());
    buff.append(NEW_LINE);
    buff.append("PersistDirName  = " + SmtGuiPreferences.getPersistDirName());
    buff.append(NEW_LINE);
    buff.append("PersistDir      = " + SmtGuiPreferences.getPersistDir());
    buff.append(NEW_LINE);
    buff.append("CacheDirName    = " + SmtGuiPreferences.getCacheDirName());
    buff.append(NEW_LINE);
    buff.append("CacheDir        = " + SmtGuiPreferences.getCacheDir());
    buff.append(NEW_LINE);
    buff.append("ConfigDirName   = " + SmtGuiPreferences.getConfigDirName());
    buff.append(NEW_LINE);
    buff.append("ConfigDir       = " + SmtGuiPreferences.getConfigDir());
    buff.append(NEW_LINE);
    buff.append("NumProcessors   = " + SmtGuiPreferences.getNumberOfProcessors());
    buff.append(NEW_LINE);
    buff.append("Linux?          = " + Boolean.toString(SmtGuiPreferences.isOsLinux()));
    buff.append(NEW_LINE);
    buff.append("MaxMemAvailable = " + SmtGuiPreferences.getMaximumMemoryAvailable());
    buff.append(NEW_LINE);
    buff.append("ProcessId       = " + SmtGuiPreferences.getProcessId());
    buff.append(NEW_LINE);
    buff.append("UserId          = " + SmtGuiPreferences.getUserId());
    buff.append(NEW_LINE);
    buff.append("UserName        = " + SmtGuiPreferences.getUserName());
    buff.append(NEW_LINE);
    
    return buff.toString();
  }

}
