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
 *        file: OMS_UpdateProvider.java
 *
 *  Created on: Jul 1, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.data;

import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;


/**********************************************************************
 * An OMS_UpdateProvider is a factory for different kinds of OMS_Updater
 * objects.  Currently there are three different forms (and two deprecated
 * ones), which are defined by the UpdaterType enum.
 * 
 * A connection based updater, provides current OMS snapshots via a live
 * connection to the OMS.
 * 
 * Two forms of file based updaters exist.  The basic kind plays back an
 * OMS_Collection file (history) at a fixed rate, and either stops or wraps
 * around when the end is reached.  A more advanced type of file updater
 * is controllable via a small slider based gui.  You can start & stop the
 * playback, adjust the speed, and specify the wrap, all dynamically.
 * <p>
 * @see  related classes and interfaces
 
 * @author meier3
 * 
 * @version Jul 1, 2013 11:13:40 AM
 **********************************************************************/
public class OMS_UpdateProvider
{
  /** the one and only <code>OMS_UpdateProvider</code> Singleton **/
  private volatile static OMS_UpdateProvider OMS_UpdateServiceProvider  = null;

  /** the synchronization object **/
  private static Boolean semaphore            = new Boolean( true );
  
  private static OMS_Updater Updater = null;
    
   public enum UpdaterType
  {
//     CONNECTION_BASED_UPDATER(       0, "gov.llnl.lc.infiniband.opensm.plugin.data.OMS_ConnectionBasedService",   "on-line"),    
     CONNECTION_BASED_UPDATER(       0, "gov.llnl.lc.infiniband.opensm.plugin.data.OMS_NConnectionBasedService",   "on-line"),    
//     FILE_BASED_UPDATER(             1, "gov.llnl.lc.infiniband.opensm.plugin.data.OMS_FileBasedService",        "OMS file"),    
     FILE_BASED_UPDATER(             1, "gov.llnl.lc.infiniband.opensm.plugin.data.OMS_NFileBasedService",        "OMS file"),    
//     FILE_BASED_UPDATER(             1, "gov.llnl.lc.infiniband.opensm.plugin.gui.data.OMS_PlayableFileBasedService",        "OMS file"),    
     PLAYABLE_FILE_BASED_UPDATER(    2, "gov.llnl.lc.infiniband.opensm.plugin.gui.data.OMS_PlayableFileBasedService", "OMS file with play controls"),    
     SMT_OLDUPDATER(                 3, "gov.llnl.lc.smt.data.SMT_OldUpdateService", "Basic OMS + SMT updater"),    
     SMT_UPDATER(                    4, "gov.llnl.lc.smt.data.SMT_UpdateService", "Basic OMS + SMT updater");    

    public static final EnumSet<UpdaterType> OMS_ALL_UPDATERS = EnumSet.allOf(UpdaterType.class);
     
    private static final Map<Integer,UpdaterType> lookup = new HashMap<Integer,UpdaterType>();

    static 
    {
      for(UpdaterType s : OMS_ALL_UPDATERS)
           lookup.put(s.getIndex(), s);
    }
    
  // the index that matches the native peer array
    private int Index;
    
    // the name of the counter
    private String Name;
    
    // a description of the counter
    private String Description;
    
    private UpdaterType(int index, String name, String description)
    {
      Index = index;
      Name = name;
      Description = description;
    }
    
    public static UpdaterType getByName(String name)
    {
      UpdaterType t = null;
      
      // return the first property with an exact name match
      for(UpdaterType s : OMS_ALL_UPDATERS)
      {
        if(s.getName().equals(name))
          return s;
      }
      return t;
    }

    public static UpdaterType getByIndex(int index)
    {
      UpdaterType t = null;
      
      // return the first property with an exact name match
      for(UpdaterType s : OMS_ALL_UPDATERS)
      {
        if(s.getIndex() == index)
          return s;
      }
      return t;
    }

    public int getIndex()
    {
      return Index;
    }

    public String getName()
    {
      return Name;
    }

    public String getDescription()
    {
      return Description;
    }
  };


  
  private OMS_UpdateProvider()
  {
  }
  

  /**************************************************************************
   *** Method Name:
   ***     getInstance
   **/
   /**
   *** Get the singleton SmtConsoleManager. This can be used if the application wants
   *** to share one manager across the whole JVM.  Currently I am not sure
   *** how this ought to be used.
   *** <p>
   ***
   *** @return       the GLOBAL (or shared) SmtConsoleManager
   **************************************************************************/

   public static OMS_UpdateProvider getInstance()
   {
     synchronized( OMS_UpdateProvider.semaphore )
     {
       if ( OMS_UpdateServiceProvider == null )
       {
         OMS_UpdateServiceProvider = new OMS_UpdateProvider( );
       }
       return OMS_UpdateServiceProvider;
     }
   }
   /*-----------------------------------------------------------------------*/

   public Object clone() throws CloneNotSupportedException
   {
     throw new CloneNotSupportedException(); 
   }

   public OMS_Updater getUpdater(UpdaterType updaterType)
   {
     // make sure it has been initialized
     getInstance();
     OMS_Updater updater = null;
     
     Class<?> upClass;
    try
    {
      upClass = Class.forName(updaterType.getName());
      Method method = upClass.getMethod("getInstance", new Class[0]);
      Object obj = method.invoke(upClass, new Object[0]);
      updater = (OMS_Updater)obj;
    }
    catch (Exception e)
    {
      // all or nothing, baby!
      e.printStackTrace();
    }
    Updater = updater;
     return Updater;
   }
   
   public OMS_Updater getUpdater()
   {
     return Updater;
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

}
