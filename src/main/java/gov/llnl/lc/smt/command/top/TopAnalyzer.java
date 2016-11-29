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
 *        file: TopAnalyzer.java
 *
 *  Created on: Jul 2, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command.top;

import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_AbstractUpdateService;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_List;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Updater;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_FabricDelta;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_ServiceChangeListener;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_Port.PortCounterName;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_PortChange;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_PortChangeComparator;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_PortRate;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Edge;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.filter.SmtFilter;
import gov.llnl.lc.smt.props.SmtProperty;
import gov.llnl.lc.system.Console;
import gov.llnl.lc.system.Console.ConsoleColor;
import gov.llnl.lc.system.Console.ConsoleControl;
import gov.llnl.lc.time.TimeStamp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**********************************************************************
 * Describe purpose and responsibility of TopAnalyzer
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Jul 2, 2013 4:15:06 PM
 **********************************************************************/
public class TopAnalyzer implements CommonLogger, OSM_ServiceChangeListener
{
  private static OpenSmMonitorService osmService = null;
  private static OSM_Fabric prevFabric = null;
  private static OSM_Fabric currFabric = null;
  private static TimeStamp analyzedTime = new TimeStamp();
    
  /************************************************************
   * Method Name:
   *  getAnalyzedTime
   **/
  /**
   * Returns the value of analyzedTime
   *
   * @return the analyzedTime
   *
   ***********************************************************/
  
  public static synchronized TimeStamp getAnalyzedTime()
  {
    return analyzedTime;
  }

  public static synchronized boolean stopAnalysis()
  {
    return Continue_Thread = false;
  }

  private static LinkedHashMap <String, IB_Vertex> VertexMap = null;
  private static LinkedHashMap <String, IB_Edge>   EdgeMap   = null;
  
  private static LinkedHashMap<String, PFM_PortChange> AllPortChanges = null;
  private static LinkedHashMap<String, IB_Edge>        AllLinkChanges = null;
  private static LinkedHashMap<String, IB_Vertex>      AllNodeChanges = null;

  private static LinkedHashMap<String, PFM_PortChange> AllPortErrors = null;
  private static LinkedHashMap<String, IB_Edge>        AllLinkErrors = null;
  private static LinkedHashMap<String, IB_Vertex>      AllNodeErrors = null;

  private static LinkedHashMap<String, PFM_PortChange> TopPortChanges = null;
  private static LinkedHashMap<String, IB_Edge>        TopLinkChanges = null;
  private static LinkedHashMap<String, IB_Vertex>      TopNodeChanges = null;

  // an empty filter
  protected static SmtFilter smtFilter     = new SmtFilter();

  /** the one and only <code>TopAnalyzer</code> Singleton **/
  private volatile static TopAnalyzer Top_Analyzer  = null;

  /** the synchronization object **/
  private static Boolean semaphore            = new Boolean( true );
  static boolean  Continue_Thread = true;
  static boolean  Continuous = false;
    
  private static int NumberOfTop = 20;
  private static SmtProperty TopType = SmtProperty.SMT_NODE_TRAFFIC;
  
  private TopAnalyzer()
  {
    // must be set from external source if used
//    setWhiteList(initWhite());
  }
  

  /**************************************************************************
   *** Method Name:
   ***     getInstance
   **/
   /**
   *** Get the singleton TopAnalyzer. This can be used if the application wants
   *** to share one manager across the whole JVM.  Currently I am not sure
   *** how this ought to be used.
   *** <p>
   ***
   *** @return       the GLOBAL (or shared) TopAnalyzer
   **************************************************************************/

   public static TopAnalyzer getInstance()
   {
     synchronized( TopAnalyzer.semaphore )
     {
       if ( Top_Analyzer == null )
       {
         Top_Analyzer = new TopAnalyzer( );
       }
       return Top_Analyzer;
     }
   }
   /*-----------------------------------------------------------------------*/

   public Object clone() throws CloneNotSupportedException
   {
     throw new CloneNotSupportedException(); 
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

  @Override
  public void osmFabricUpdate(OSM_Fabric osmFabric) throws Exception
  {
    // given a new fabric, replace the previous and current
    if((prevFabric == null) && (osmFabric != null))
    {
       prevFabric = osmFabric;
       return;
    }
    
    if((currFabric == null) && (osmFabric != null))
    {
       currFabric = osmFabric;
       // if this timestamp is different than previous, calculate
       if(isReadyToAnalyze() && Continuous)
       {
         OSM_FabricDelta delta = new OSM_FabricDelta(prevFabric, currFabric);
         calculateTops(delta, this.getTopType(), this.getNumberOfTop());
         // assumes this is only used in SmtTop
         displayTop(TopType);
       }
       return;
     }
     
    if((currFabric != null) && (osmFabric != null))
    {
      logger.severe("Got a valid fabric: " + osmFabric.getTimeStamp().toString());
      //replace only if it has a different timestamp (this protects the initial time through)
      if(!(currFabric.getTimeStamp().toString().equals(osmFabric.getTimeStamp().toString())))
      {
        logger.severe(currFabric.getTimeStamp().toString() + " vs: " + osmFabric.getTimeStamp().toString());
        // this MIGHT be a previous timestamp, so check it against the current
        if(osmFabric.getTimeStamp().before(currFabric.getTimeStamp()))
        {
          logger.severe("Time out of order, fixing it up");
          logger.severe("got a PREVIOUS fabric");
          prevFabric = osmFabric;
        }
        else
        {
          prevFabric = currFabric;
          currFabric = osmFabric;
          logger.severe("got the NEXT fabric" + currFabric.getTimeStamp().toString());
        }
        logger.severe(prevFabric.getTimeStamp().toString() + ", and then " + currFabric.getTimeStamp().toString());
       
       if(isReadyToAnalyze())
        {
          OSM_FabricDelta delta = new OSM_FabricDelta(prevFabric, currFabric);
          calculateTops(delta, this.getTopType(), this.getNumberOfTop());
          // assumes this is only used in SmtTop
          displayTop(TopType);
        }
      }
      logger.severe("The fabric has the same timestamp as current: " + osmFabric.getTimeStamp().toString());
    }
  }

  public void osmFabricUpdateOrig(OSM_Fabric osmFabric) throws Exception
  {
    // given a new fabric, replace the previous and current
    if((currFabric != null) && (osmFabric != null))
    {
      logger.severe("Got a valid fabric: " + osmFabric.getTimeStamp().toString());
      //replace only if it has a different timestamp
      if(!(currFabric.getTimeStamp().toString().equals(osmFabric.getTimeStamp().toString())))
      {
        logger.severe(currFabric.getTimeStamp().toString() + " vs: " + osmFabric.getTimeStamp().toString());
        // this MIGHT be a previous timestamp, so check it against the current
        if(osmFabric.getTimeStamp().before(currFabric.getTimeStamp()))
        {
          logger.severe("Time out of order, fixing it up");
          logger.severe("got a PREVIOUS fabric");
          prevFabric = osmFabric;
        }
        else
        {
          prevFabric = currFabric;
          currFabric = osmFabric;
          logger.severe("got the NEXT fabric" + currFabric.getTimeStamp().toString());
        }
        logger.severe(prevFabric.getTimeStamp().toString() + ", and then " + currFabric.getTimeStamp().toString());
       
        if(isReadyToAnalyze())
        {
         logger.info("Ready to Calculate Tops");
          OSM_FabricDelta delta = new OSM_FabricDelta(prevFabric, currFabric);
          calculateTops(delta, this.getTopType(), this.getNumberOfTop());
          // assumes this is only used in SmtTop
          displayTop(TopType);
        }
      }
      logger.severe("The fabric has the same timestamp as current: " + osmFabric.getTimeStamp().toString());
    }
    else
    {
      currFabric = osmFabric;
      if(currFabric != null)
      {
        logger.severe("got the FIRST fabric taofuo: " + currFabric.getTimeStamp().toString());
      }
    }
  }

//  private void calculateTops(OSM_FabricDelta delta, SmtProperty type, int maxTop, boolean continuous)
  private void calculateTops(OSM_FabricDelta delta, SmtProperty type, int maxTop)
  {
    LinkedHashMap<String, PFM_PortChange> activePorts        = delta.getPortsWithChange();
    LinkedHashMap<String, PFM_PortChange> activeErrorPorts   = delta.getPortsWithErrorChange();
    LinkedHashMap<String, PFM_PortChange> activeTrafficPorts = delta.getPortsWithTrafficChange();
    
    LinkedHashMap <String, IB_Vertex> vertexMap              = IB_Vertex.createVertexMap(delta.getFabric2());
    LinkedHashMap <String, IB_Edge> edgeMap                  = IB_Vertex.createEdgeMap(vertexMap);
    
    setVertexMap(vertexMap);
    setEdgeMap(edgeMap);
    
    logger.severe("Calculating Tops for: " + delta.getFabricName() + ": " + delta.getFabric1().getTimeStamp().toString() + " and " + delta.getFabric2().getTimeStamp().toString());
    logger.severe("Calculating " + type.getName() + ", returning " + maxTop + " results");
    
    analyzedTime = delta.getTimeStamp();
        
    // collect, sort, and trim the active ports (the results are used in successive queries - so get more)
    LinkedHashMap<String, PFM_PortChange> aep = calculateActiveErrorPorts(maxTop, activeErrorPorts);
    LinkedHashMap<String, PFM_PortChange> atp = calculateActiveTrafficPorts(-1, activeTrafficPorts);
    LinkedHashMap <String, IB_Edge> ael = calculateActiveErrorLinks(-1, activeErrorPorts, edgeMap);
    LinkedHashMap <String, IB_Vertex> aen = calculateActiveErrorNodes(-1, activeErrorPorts, vertexMap);
    LinkedHashMap <String, IB_Edge> atl = calculateActiveTrafficLinks(-1, atp, edgeMap);
    LinkedHashMap <String, IB_Vertex> atn = calculateActiveTrafficNodes(-1, atp, vertexMap);
    
    AllPortErrors = activeErrorPorts;
    AllLinkErrors = ael;
    AllNodeErrors = aen;

    AllPortChanges = atp;
    AllLinkChanges = atl;
    AllNodeChanges = atn;
    
    int numResults = 0;
    int maxNameLen = 60;  // used for white/black list check
    
    switch(type)
    {
      case SMT_NODE_ERRORS:
        LinkedHashMap <String, IB_Vertex> aEn = calculateActiveErrorNodes(maxTop, aep, vertexMap);
        // filtered
        LinkedHashMap<String, IB_Vertex> fenMap = new LinkedHashMap<String, IB_Vertex>();
        numResults = 0;
        if(aEn != null)
        for (Map.Entry<String, IB_Vertex> eMapEntry : aEn.entrySet())
        {
          IB_Vertex tv = eMapEntry.getValue();
          // add only if it isn't filtered
          if(!smtFilter.isFiltered(getNodeErrorString(tv, maxNameLen)))
          {
            numResults++;
            fenMap.put(eMapEntry.getKey(), eMapEntry.getValue());
          }
          if(numResults >= maxTop)
            break;
        }
        setTopNodeChanges(fenMap);
       break;
       
      case SMT_NODE_TRAFFIC:
        // already obtained the vertex traffic, only return # requested
        LinkedHashMap<String, IB_Vertex> tMap = new LinkedHashMap<String, IB_Vertex>();
        numResults = 0;
        if(atn != null)
        for (Map.Entry<String, IB_Vertex> eMapEntry : atn.entrySet())
        {
          IB_Vertex tv = eMapEntry.getValue();
          // add only if it isn't filtered
          if(!smtFilter.isFiltered(tv.toVertexIdString(maxNameLen)))
          {
            numResults++;
            tMap.put(eMapEntry.getKey(), eMapEntry.getValue());
          }
          if(numResults >= maxTop)
            break;
        }
        setTopNodeChanges(tMap);
       break;
       
      case SMT_LINK_ERRORS:
        LinkedHashMap <String, IB_Edge> aEl = calculateActiveErrorLinks(maxTop, aep, edgeMap);
        // filtered
        LinkedHashMap <String, IB_Edge> fael = new LinkedHashMap<String, IB_Edge>();
        if(aEl != null)
        for (Map.Entry<String, IB_Edge> eMapEntry : aEl.entrySet())
        {
          IB_Edge te = eMapEntry.getValue();
          
          // add only if in white list, and not in black list
          if(!smtFilter.isFiltered(getLinkErrorString(te, maxNameLen)))
          {
            numResults++;
            fael.put(eMapEntry.getKey(), eMapEntry.getValue());
          }
          if(numResults >= maxTop)
            break;
        }

        setTopLinkChanges(fael);
        break;
        
      case SMT_LINK_TRAFFIC:
        // already obtained the vertex traffic, only return # requested
        LinkedHashMap<String, IB_Edge> teMap = new LinkedHashMap<String, IB_Edge>();
        numResults = 0;
         
        if(atl != null)
        for (Map.Entry<String, IB_Edge> eMapEntry : atl.entrySet())
        {
          IB_Edge te = eMapEntry.getValue();
          
          // add only if it isn't filtered
          if(!smtFilter.isFiltered(te.toEdgeIdStringVerbose(maxNameLen)))
          {
            numResults++;
            teMap.put(eMapEntry.getKey(), eMapEntry.getValue());
          }
          if(numResults >= maxTop)
            break;
        }
        setTopLinkChanges(teMap);
        break;
       
      case SMT_PORT_ERRORS:
        LinkedHashMap<String, PFM_PortChange> pcMap = new LinkedHashMap<String, PFM_PortChange>();
        numResults = 0;
        if(aep != null)
        for (Map.Entry<String, PFM_PortChange> eMapEntry : aep.entrySet())
        {
          PFM_PortChange pc = eMapEntry.getValue();
          
          // add only if it isn't filtered
          if(!smtFilter.isFiltered(getPortErrorString(pc, maxNameLen)))
          {
            numResults++;
            pcMap.put(eMapEntry.getKey(), eMapEntry.getValue());
          }
          if(numResults >= maxTop)
            break;
        }
        setTopPortChanges(pcMap);
        break;
        
      case SMT_PORT_TRAFFIC:
        // already obtained the port traffic, only return # requested
        LinkedHashMap<String, PFM_PortChange> pcMap1 = new LinkedHashMap<String, PFM_PortChange>();
        numResults = 0;
        if(atp != null)
        for (Map.Entry<String, PFM_PortChange> eMapEntry : atp.entrySet())
        {
           PFM_PortChange pc = eMapEntry.getValue();
          
          // add only if it isn't filtered
          if(!smtFilter.isFiltered(getPortIdString(pc, maxNameLen)))
          {
            numResults++;
            pcMap1.put(eMapEntry.getKey(), eMapEntry.getValue());
          }
          if(numResults >= maxTop)
            break;
        }
        setTopPortChanges(pcMap1);
        break;
        
      default:
        logger.severe("Invalid Top Type (" + type.name() + ")");
        break;
    }
    logger.info("Done calculating TOP");
   }
  
  public void displayTop(SmtProperty type)
  {
    int maxNameLen = 24;
    
    LinkedHashMap<String, IB_Vertex>      tvMap = getTopNodeChanges();
    LinkedHashMap<String, IB_Edge>        teMap = getTopLinkChanges();
    LinkedHashMap<String, PFM_PortChange> pcMap = getTopPortChanges();
    
     // if here, then I should have results ready
    Console.clearScreen();
    Console.homeCursor();
    Console.bold();
    switch(type)
    {
      case SMT_NODE_ERRORS:
        // already obtained the node traffic, only return # requested
        if((tvMap != null) && (tvMap.size() > 0))
        {
         // show the counter names that are suppressed
          EnumSet<PortCounterName> sc = PortCounterName.PFM_SUPPRESS_COUNTERS;
         Map.Entry<String, IB_Vertex> me = tvMap.entrySet().iterator().next();
         sc = me.getValue().getTopPortChange().getPort1().getSuppressed_Counters();
         System.out.println(this.getNodeErrorHeader(maxNameLen, sc));
         Console.reset();
         System.out.println(getTopNodeErrorString(maxNameLen));
      }
       else
       {
         System.out.println(this.getNodeErrorHeader(maxNameLen, null));
         Console.reset();
         System.out.println("  No dynamic errors for (" + getAnalyzedTime().toString() + ")"); 
       }
        break;
       
      case SMT_NODE_TRAFFIC:
        // already obtained the node traffic, only return # requested
        String hdr = getNodeTrafficHeader(maxNameLen);
        System.out.println(hdr);
        Console.reset();
        if((tvMap != null) && (tvMap.size() > 0))
        {
          String bdy = getTopNodeTrafficString(maxNameLen);
          System.out.println(bdy);
        }
        else
        {
          System.out.println("  No dynamic traffic for (" + getAnalyzedTime().toString() + ")");
        }
         break;
       
      case SMT_LINK_ERRORS:
        // already obtained the link traffic, only return # requested
        if((teMap != null) && (teMap.size() > 0))
        {
          // show the counter names that are suppressed
          EnumSet<PortCounterName> sc = PortCounterName.PFM_SUPPRESS_COUNTERS;
          Map.Entry<String, IB_Edge> me = teMap.entrySet().iterator().next();
          sc = me.getValue().getEndPort1().getPfmPort().getSuppressed_Counters();
          hdr = getLinkErrorHeader(maxNameLen, sc);
          String bdy = getTopLinkErrorString(maxNameLen);
           System.out.println(hdr);
          Console.reset();
          System.out.println(bdy);
         }
        else
        {
          System.out.println(getLinkErrorHeader(maxNameLen, null));
          Console.reset();
          System.out.println("  No dynamic errors for (" + getAnalyzedTime().toString() + ")");
        }
         break;
        
      case SMT_LINK_TRAFFIC:
        // already obtained the link traffic, only return # requested
        System.out.println(getLinkTrafficHeader(maxNameLen));
        Console.reset();
        if((teMap != null) && (teMap.size() > 0))
        {
          String bdy = getTopLinkTrafficString(maxNameLen);
          System.out.println(bdy);
        }
        else
        {
          System.out.println("  No dynamic traffic for (" + getAnalyzedTime().toString() + ")");
        }
         break;
       
      case SMT_PORT_ERRORS:
           // already obtained the port traffic, only return # requested
          if((pcMap != null) && (pcMap.size() > 0))
          {
            // show the counter names that are suppressed
            Map.Entry<String, PFM_PortChange> me = pcMap.entrySet().iterator().next();
            EnumSet<PortCounterName> sc = me.getValue().getPort1().getSuppressed_Counters();
            System.out.println(this.getPortErrorHeader(maxNameLen, sc));
            Console.reset();
            System.out.println(getTopPortErrorString(maxNameLen));
         }
          else
          {
            System.out.println(this.getPortErrorHeader(maxNameLen, null));
            Console.reset();

            System.out.println("  No dynamic errors for (" + getAnalyzedTime().toString() + ")"); 
          }
           break;
        
      case SMT_PORT_TRAFFIC:
        // already obtained the port traffic, only return # requested
        System.out.println(getPortTrafficHeader(maxNameLen));
        Console.reset();
        if((pcMap != null) && (pcMap.size() > 0))
        {
           String bdy = getTopPortTrafficString(maxNameLen);
           System.out.println(bdy);
        }
        else
          System.out.println("  No dynamic traffic for (" + getAnalyzedTime().toString() + ")"); 
         break;
        
      default:
        logger.severe("Invalid Top Type (" + type.name() + ")");
        break;
    }
  }


  private String getLinkErrorHeader(int maxNameLen, EnumSet<PortCounterName> enumSet)
  {
    StringBuffer sbuff = new StringBuffer();
    
    String surpressed = "none";
    if(enumSet != null)
      surpressed = Arrays.asList(enumSet.toArray()).toString();

    
    // name, guid portnum, err list
    String lvl = "level";
    String nm = "node name";
    String guid = "guid";
    String pn = "p#";
    String el = " delta errs/period";
    int namePad = (maxNameLen)/2;
    
    sbuff.append("                Top Link Errors (" + getAnalyzedTime().toString() + ")   suppressed: " + surpressed + "\n");
    
    String format = "%s %" + (namePad+2) + "s %" + (namePad-6) + "s %11s %10s %12s %10s %" + (namePad+4) +"s %" + (namePad-6) + "s  %s";
    
    sbuff.append(String.format(format, lvl, nm, " ", guid, pn,        guid, pn, nm, " ", el));
     return sbuff.toString();
  }
  
  private String getLinkTrafficHeader(int maxNameLen)
  {
    StringBuffer sbuff = new StringBuffer();
    
    // name, guid portnum, xmit, rcv
    String lvl = "level";
    String nm = "node name";
    String guid = "guid";
    String pn = "p#";
    String xmt = "xmt MB/s";
    String rcv = "rcv MB/s";
    int namePad = (maxNameLen)/2;
    
    sbuff.append("                       Top Link Traffic     (" + getAnalyzedTime().toString() + ")\n");
    
    String format = "%s %" + (namePad+2) + "s %" + (namePad-6) + "s %11s %10s %12s %10s %" + (namePad+4) +"s %" + (namePad-6) + "s  %s %s";
    
    sbuff.append(String.format(format, lvl, nm, " ", guid, pn,        guid, pn, nm, " ", xmt, rcv));
     return sbuff.toString();
  }
  
  private String getPortErrorHeader(int maxNameLen, EnumSet<PortCounterName> enumSet)
  {
    StringBuffer sbuff = new StringBuffer();
    
    // name, guid portnum, xmit, rcv
    String lvl = "level";
    String nm = "node name";
    String guid = "guid         ";
    String pn = "p#";
    String el = " delta errs/period    (suppressed:";
    int namePad = (maxNameLen)/2;
    
    sbuff.append("         Top Port Errors (" + getAnalyzedTime().toString() + ")\n");
    
    String format = "%s%" + (namePad+2) + "s %" + (namePad-5) + "s %19s %s %s %s)";
    String surpressed = "none";
    if(enumSet != null)
      surpressed = Arrays.asList(enumSet.toArray()).toString();
    
    sbuff.append(String.format(format, lvl, nm, " ", guid, pn, el, surpressed));
    return sbuff.toString();
  }
  
  private String getPortTrafficHeader(int maxNameLen)
  {
    StringBuffer sbuff = new StringBuffer();
    
    // name, guid portnum, xmit, rcv
    String lvl = "level";
    String nm = "node name";
    String guid = "guid         ";
    String pn = "p#";
    String xmt = "xmt MB/s";
    String rcv = "rcv MB/s";
    int namePad = (maxNameLen)/2;
    
    sbuff.append("         Top Port Traffic (" + getAnalyzedTime().toString() + ")\n");
    
    String format = "%s%" + (namePad+2) + "s %" + (namePad-5) + "s %19s %s %s %s";
    
    sbuff.append(String.format(format, lvl, nm, " ", guid, pn, xmt, rcv));
     return sbuff.toString();
  }
  
  private String getNodeErrorHeader(int maxNameLen, EnumSet<PortCounterName> enumSet)
  {
    StringBuffer sbuff = new StringBuffer();
    
    // name, guid portnum, xmit, rcv
    String lvl = "level";
    String nm = "node name";
    String guid = "guid         ";
    String el = " error:port list    (suppressed:";
    int namePad = (maxNameLen)/2;
    
    sbuff.append("         Top Node Errors (" + getAnalyzedTime().toString() + ")\n");
    
    String format = "%s%" + (namePad+2) + "s %" + (namePad-5) + "s %19s %s %s)";
    String surpressed = "none";
    if(enumSet != null)
      surpressed = Arrays.asList(enumSet.toArray()).toString();
    
    sbuff.append(String.format(format, lvl, nm, " ", guid, el, surpressed));
    return sbuff.toString();
  }
  
  private String getNodeTrafficHeader(int maxNameLen)
  {
    StringBuffer sbuff = new StringBuffer();
    
    // name, guid portnum, xmit, rcv
    String lvl = "level";
    String nm = "node name";
    String guid = "guid         ";
    String pn = "p#";
    String xmt = "xmt MB/s";
    String rcv = "rcv MB/s";
    int namePad = (maxNameLen)/2;
    
    sbuff.append("         Top Node Traffic (" + getAnalyzedTime().toString() + ")\n");
    
    String format = "%s%" + (namePad+2) + "s %" + (namePad-5) + "s %19s %s %s %s";
    
    sbuff.append(String.format(format, lvl, nm, " ", guid, pn, xmt, rcv));
     return sbuff.toString();
  }

  private String getPortErrorString(PFM_PortChange pc, int maxNameLen)
  {
    StringBuffer sbuff = new StringBuffer();
    LinkedHashMap<String, IB_Vertex>      avl = getVertexMap();
    PFM_Port p = pc.getPort1();
    String name  = "";
    
    // get the edge and vertex that owns this port (need for missing data)
    IB_Vertex v = IB_Vertex.getVertex(IB_Vertex.getVertexKey(p.getNodeGuid()), avl);
    if(v != null)
        name  = v.getNode().sbnNode.description;
    
   sbuff.append(p.toPortIdString(name, maxNameLen) + "  ");
   sbuff.append(pc.toShortErrorString());

    return sbuff.toString();
 }
  
  private String getTopPortErrorString(int maxNameLen)
  {
    // **** lifted from TopPortErrorScreen ***
    StringBuffer sbuff = new StringBuffer();
     
    // data from the top analyzer is in order
    LinkedHashMap<String, PFM_PortChange> atp = getTopPortChanges();
    LinkedHashMap<String, IB_Vertex>      avl = getVertexMap();
    
    int num = getNumberOfTop();
    if((atp != null) && !(atp.isEmpty()))
      num = atp.size();
    else
      num = 0;
     
    if((atp != null) && !(atp.isEmpty()))
    {
       int count = 0;
         
       for (Map.Entry<String, PFM_PortChange> eMapEntry : atp.entrySet())
      {
         PFM_PortChange pc = eMapEntry.getValue();
         PFM_Port p = pc.getPort1();
         int depth    = -1;
         String name  = "";
         
         // get the edge and vertex that owns this port (need for missing data)
         IB_Vertex v = IB_Vertex.getVertex(IB_Vertex.getVertexKey(p.getNodeGuid()), avl);
         if(v != null)
         {
             depth    = v.getDepth();
             name  = v.getNode().sbnNode.description;
         }
         
        sbuff.append(String.format("%3d", depth));
        sbuff.append(p.toPortIdString(name, maxNameLen) + "  ");
        sbuff.append(Console.getTextColorString(ConsoleColor.n_red));
        sbuff.append(pc.toShortErrorString() + "\n");
        sbuff.append(ConsoleControl.default_text_color.getControlString());
        
        if(++count >= getNumberOfTop())
         break;
       }
     }
    else
    {
      if((atp != null) && (atp.isEmpty()))
      {
        // wipe the screen clear, display an empty message - no errors!
        logger.warning("The TopPortErrors seems to be empty");
      }
      logger.warning("The TopPortErrors seems to be null");      
    }
    return sbuff.toString();
  }
  
  private String getLinkErrorString(IB_Edge te, int maxNameLen)
  {
    StringBuffer sbuff = new StringBuffer();
    sbuff.append(te.toEdgeIdStringVerbose(maxNameLen) + "  ");
    sbuff.append(te.toShortErrorString());
   
    return sbuff.toString();
  }

  
  private String getTopLinkErrorString(int maxNameLen)
  {
    // **** lifted from TopLinkErrorScreen ***
    StringBuffer sbuff = new StringBuffer();
    
    // data from the top analyzer is in order
    LinkedHashMap<String, PFM_PortChange> atp = getTopPortChanges();
    LinkedHashMap<String, IB_Vertex>      avl = getVertexMap();
    LinkedHashMap<String, IB_Edge>        atl = getTopLinkChanges();
    
    int num = getNumberOfTop();
    if((atl != null) && !(atl.isEmpty()))
      num = atl.size();
    else
      num = 0;
     
    if((atl != null) && !(atl.isEmpty()))
    {
       int count = 0;
       EnumSet<PortCounterName> sc = PortCounterName.PFM_SUPPRESS_COUNTERS;
          // show the counter names that are suppressed
       Map.Entry<String, IB_Edge> me = atl.entrySet().iterator().next();
       sc = me.getValue().getEndPort1().getPfmPort().getSuppressed_Counters();
         String suppressed = "none";
         if(sc != null)
           suppressed = Arrays.asList(sc.toArray()).toString();
         
        // fill up the area with link errors
         for (Map.Entry<String, IB_Edge> eMapEntry : atl.entrySet())
         {
           IB_Edge te = eMapEntry.getValue();
           sbuff.append(String.format("%3d", te.getDepth()));
           sbuff.append(te.toEdgeIdStringVerbose(maxNameLen) + "  ");
           sbuff.append(Console.getTextColorString(ConsoleColor.n_red));
           sbuff.append(te.toShortErrorString() + "\n");
           sbuff.append(ConsoleControl.default_text_color.getControlString());
           
           if(++count >= getNumberOfTop())
            break;
         }
     }
    else
    {
      if((atl != null) && (atl.isEmpty()))
      {
        // wipe the screen clear, display an empty message - no errors!
        logger.warning("The TopLinkErrors seems to be empty");
      }
      logger.warning("The TopLinkErrors seems to be null");      
    }
    return sbuff.toString();
  }
  
  private String getNodeErrorString(IB_Vertex v, int maxNameLen)
  {
    StringBuffer sbuff = new StringBuffer();
    
    sbuff.append(v.toVertexIdString(maxNameLen) + " ");
    sbuff.append(v.toVertexErrorString());
    return sbuff.toString();
  }

  
  private String getTopNodeErrorString(int maxNameLen)
  {
    // **** lifted from TopPortErrorScreen ***
    StringBuffer sbuff = new StringBuffer();
     
    // data from the top analyzer is in order
    LinkedHashMap<String, IB_Vertex> atn = getTopNodeChanges();
        
    int num = getNumberOfTop();
    if((atn != null) && !(atn.isEmpty()))
      num = atn.size();
    else
      num = 0;
    if((atn != null) && !(atn.isEmpty()))
    {
       int count = 0;

       for (Map.Entry<String, IB_Vertex> eMapEntry : atn.entrySet())
      {
         IB_Vertex v = eMapEntry.getValue();
         if(v != null)
         {
             sbuff.append(String.format("%3d", v.getDepth()));
             sbuff.append(v.toVertexIdString(maxNameLen) + "  ");
             sbuff.append(Console.getTextColorString(ConsoleColor.n_red));
             sbuff.append(v.toVertexErrorString() + "\n");
             sbuff.append(ConsoleControl.default_text_color.getControlString());
          }
       
         if(++count >= getNumberOfTop())
          break;
       }
     }
    else
    {
      if((atn != null) && (atn.isEmpty()))
      {
        // wipe the screen clear, display an empty message - no errors!
        logger.warning("The TopNodeErrors seems to be empty");
      }
      logger.warning("The TopNodeErrors seems to be null");      
    }
    return sbuff.toString();
  }
  
  private String getTopNodeTrafficString(int maxNameLen)
  {
    // **** lifted from TopNodeTrafficScreen ***
    StringBuffer sbuff = new StringBuffer();
     
    // data from the top analyzer is in order
    LinkedHashMap<String, IB_Vertex> atn = getTopNodeChanges();
        
    if(atn != null)
    {
      int count = 0;
       for (Map.Entry<String, IB_Vertex> eMapEntry : atn.entrySet())
      {
        IB_Vertex tn = eMapEntry.getValue();
        sbuff.append(String.format("%3d", tn.getDepth()));
        sbuff.append(tn.toTopPortIdString(maxNameLen) + "  ");
        sbuff.append(tn.toVertexXmitString() + "    ");
        sbuff.append(tn.toVertexRcvString() + "\n");
        
        if(++count >= getNumberOfTop())
         break;
       }
    }
    else
    {
      logger.warning("The TopNodes seems to be null");      
    }
    return sbuff.toString();
  }
  
  private String getTopLinkTrafficString(int maxNameLen)
  {
    // **** lifted from TopPortTrafficScreen ***
    StringBuffer sbuff = new StringBuffer();
     
    // data from the top analyzer is in order (and wl/bl filtered)
    LinkedHashMap<String, IB_Edge> atl = getTopLinkChanges();
        
    if(atl != null)
    {
      logger.info("Number of TopLinkChanges is: " + atl.size());
      int count = 0;
       for (Map.Entry<String, IB_Edge> eMapEntry : atl.entrySet())
      {
        IB_Edge te = eMapEntry.getValue();
          sbuff.append(String.format("%3d", te.getDepth()));
          sbuff.append(te.toEdgeIdStringVerbose(maxNameLen) + "  ");
          sbuff.append(te.toEdgeXmitString() + "    ");
          sbuff.append(te.toEdgeRcvString() + "\n");
          
          if(++count >= getNumberOfTop())
           break;          
       }
    }
    else
    {
      logger.warning("The TopLinks seems to be null");      
    }
    return sbuff.toString();
  }
  
  private String getPortIdString(PFM_PortChange pc, int maxNameLen)
  {
    // get an id string from this portchange
    StringBuffer sbuff = new StringBuffer();
    LinkedHashMap<String, IB_Edge>        atl = getEdgeMap();
   
    PFM_Port p = pc.getPort1();
    String name  = "";
    
    // get the edge and vertex that owns this port (need for missing data)
    IB_Edge e    = IB_Edge.getEdge( p, atl);
    if(e != null)
    {
      // fill in the missing data
      IB_Vertex v  = e.getEndpoint1().hasPort(p)? e.getEndpoint1(): e.getEndpoint2();
      if(v != null)
        name  = v.getNode().sbnNode.description;
     }
     sbuff.append(p.toPortIdString(name, maxNameLen));
    return sbuff.toString();

  }
  
  private String getTopPortTrafficString(int maxNameLen)
  {
    // **** lifted from TopPortTrafficScreen ***
    StringBuffer sbuff = new StringBuffer();
     
    // data from the top analyzer is in order
    LinkedHashMap<String, PFM_PortChange> atp = getTopPortChanges();
    LinkedHashMap<String, IB_Edge>        atl = getEdgeMap();
    
    if(atp != null)
    {
       int count = 0;
       for (Map.Entry<String, PFM_PortChange> eMapEntry : atp.entrySet())
      {
         PFM_PortChange pc = eMapEntry.getValue();
         PFM_Port p = pc.getPort1();
         int depth    = -1;
         String name  = "";
         
         // get the edge and vertex that owns this port (need for missing data)
         IB_Edge e    = IB_Edge.getEdge( p, atl);
         if(e != null)
         {
           // fill in the missing data
           IB_Vertex v  = e.getEndpoint1().hasPort(p)? e.getEndpoint1(): e.getEndpoint2();
           if(v != null)
           {
             depth    = v.getDepth();
             name  = v.getNode().sbnNode.description;
           }
         }
         sbuff.append(String.format("%3d", depth));
         sbuff.append(p.toPortIdString(name, maxNameLen) + "  ");
         sbuff.append(PFM_PortRate.toTransmitRateMBString(pc) + "    ");
         sbuff.append(PFM_PortRate.toReceiveRateMBString(pc) + "\n");
         
         if(++count >= getNumberOfTop())
          break;
       }
    }
    else
    {
      logger.warning("The TopPorts seems to be null");      
    }
    return sbuff.toString();
  }
  
  private int getNodeLevelFromGuid(IB_Guid guid)
  {
    // Find the Vertex that belongs to this guid, then return its depth
    int depth = -1;
    IB_Vertex v = IB_Vertex.getVertex(IB_Vertex.getVertexKey(guid), this.getVertexMap());
    if(v != null)
      depth = v.getDepth();
    return depth;
  }
  
  private String getNodeNameFromGuid(IB_Guid guid)
  {
    // Find the Vertex that belongs to this guid, then return its name
    String name = "unknown";
    IB_Vertex v = IB_Vertex.getVertex(IB_Vertex.getVertexKey(guid), this.getVertexMap());
    if(v != null)
      name = v.getNode().sbnNode.description;
    return name;
  }


  public static LinkedHashMap<String, IB_Vertex> calculateActiveTrafficNodes(int topSize, LinkedHashMap<String, PFM_PortChange> activeTrafficPorts, LinkedHashMap <String, IB_Vertex> vertexMap)
  {
    // the provided activeTrafficPorts should already be sorted, from most active to least
    
    // special case is if topSize == -1, calculate and sort all ports
    if (((topSize == -1) || (topSize > 0)) && (activeTrafficPorts != null))
      if (activeTrafficPorts.size() > 0)
      {
        LinkedHashMap <String, IB_Vertex> topVertexMap = new LinkedHashMap <String, IB_Vertex>();

        int rtnSize = 0;

        for (Map.Entry<String, PFM_PortChange> pcMapEntry : activeTrafficPorts.entrySet())
        {
          PFM_PortChange ppc = pcMapEntry.getValue();

          // find the node associated with this port
          PFM_Port p = ppc.getPort1();
          IB_Vertex v = IB_Vertex.getVertex(p, vertexMap);
          if(v != null)
          {
            // if this already exists in the map, don't replace, because the existing one should be more active
            if(!topVertexMap.containsKey(v.getKey()))
              topVertexMap.put(v.getKey(), v);
            rtnSize = topVertexMap.size();
          }
          if ((topSize > 0) && (rtnSize >= topSize))
            break;
        }
        return topVertexMap;
      }
    return null;
  }


  public static LinkedHashMap<String, IB_Vertex> calculateActiveErrorNodes(int topSize, LinkedHashMap<String, PFM_PortChange> activeErrorPorts, LinkedHashMap <String, IB_Vertex> vertexMap)
  {
    // the provided activeTrafficPorts should already be sorted, from most active to least
    
    // special case is if topSize == -1, calculate and sort all ports
    if (((topSize == -1) || (topSize > 0)) && (activeErrorPorts != null))
      if (activeErrorPorts.size() > 0)
      {
//        System.err.println("There are " + activeErrorPorts.size() + " ports with errors");
        LinkedHashMap <String, IB_Vertex> topVertexMap = new LinkedHashMap <String, IB_Vertex>();

        int rtnSize = 0;

        for (Map.Entry<String, PFM_PortChange> pcMapEntry : activeErrorPorts.entrySet())
        {
          PFM_PortChange ppc = pcMapEntry.getValue();

          // find the node associated with this port
          PFM_Port p = ppc.getPort1();
          IB_Vertex v = IB_Vertex.getVertex(p, vertexMap);
          if(v != null)
          {
            // if this already exists in the map, don't replace, because the existing one should be more active
            if(!topVertexMap.containsKey(v.getKey()))
              topVertexMap.put(v.getKey(), v);
            rtnSize = topVertexMap.size();
          }
          if ((topSize > 0) && (rtnSize >= topSize))
            break;
        }
        return topVertexMap;
      }
    return null;
  }

  public static LinkedHashMap<String, IB_Edge> calculateActiveTrafficLinks(int topSize, LinkedHashMap<String, PFM_PortChange> activeTrafficPorts, LinkedHashMap <String, IB_Edge> edgeMap)
  {
    // special case is if topSize == -1, calculate and sort all ports
    if (((topSize == -1) || (topSize > 0)) && (activeTrafficPorts != null))
      if (activeTrafficPorts.size() > 0)
      {
        LinkedHashMap<String, IB_Edge> topEdgeMap = new LinkedHashMap<String, IB_Edge>();

        int rtnSize = 0;

        // the tops are all calculated, find the links associated with these
        // ports
        // and return the top unique links

        for (Map.Entry<String, PFM_PortChange> pcMapEntry : activeTrafficPorts.entrySet())
        {
          PFM_PortChange ppc = pcMapEntry.getValue();
          // find the link associated with this port
          PFM_Port p = ppc.getPort1();
          IB_Edge e = IB_Edge.getEdge(p, edgeMap);
          if (e != null)
          {
            // put the change on the right end of the link
            if (e.getEndPort1().hasPort(ppc.getPort1()))
              e.setPortChange1(ppc);
            else
              e.setPortChange2(ppc);

            topEdgeMap.put(e.getKey(), e);
            rtnSize = topEdgeMap.size();
          }
          if ((topSize > 0) && (rtnSize >= topSize))
            break;
        }
        return topEdgeMap;
      }
    return null;
  }

  public static LinkedHashMap<String, IB_Edge> calculateActiveErrorLinks(int topSize, LinkedHashMap<String, PFM_PortChange> activeErrorPorts, LinkedHashMap <String, IB_Edge> edgeMap)
  {
    // create a link from the list of error ports
    
    return calculateActiveTrafficLinks(topSize, activeErrorPorts, edgeMap);
  }


  public static LinkedHashMap<String, PFM_PortChange> calculateActiveTrafficPorts(int topSize, LinkedHashMap<String, PFM_PortChange> activeTrafficPorts)
  {
    // special case is if topSize == -1, calculate and sort all ports
    if (((topSize == -1) || (topSize > 0)) && (activeTrafficPorts != null))
      if (activeTrafficPorts.size() > 0)
      {
        int rtnSize = 0;

        // sort the list of active traffic ports
        PFM_PortChangeComparator pcCompare = new PFM_PortChangeComparator(PFM_Port.PortCounterName.PFM_DATA_COUNTERS);
        ArrayList<PFM_PortChange> pcList = new ArrayList<PFM_PortChange>();

        for (Map.Entry<String, PFM_PortChange> cMapEntry : activeTrafficPorts.entrySet())
        {
          PFM_PortChange tpc = cMapEntry.getValue();
          pcList.add(tpc);
        }
        
        // sort this from most active to least (sort, then reverse)
         try
        {
        Collections.sort(pcList, pcCompare);
        }
        catch (Exception e)
        {
          logger.warning("Caught an exception while sorting");
        }
        Collections.reverse(pcList);
        
//        System.err.println("Showing only the top " + topSize + " ports");
        LinkedHashMap<String, PFM_PortChange> activePorts = new LinkedHashMap<String, PFM_PortChange>();
        for (PFM_PortChange ppc : pcList)
        {
//          System.err.println(ppc.getOSM_PortKey() + ", " + ppc.toTrafficString());
//          System.err.println(PFM_PortRate.toVerboseDiagnosticString(ppc));
          activePorts.put(PFM_PortChange.getOSM_PortKey(ppc), ppc);
          rtnSize = activePorts.size();
          if ((topSize > 0) && (rtnSize >= topSize))
            break;
        }
        return activePorts;
      }
      else
        logger.warning("No active port traffic, unable to create top list");

    return null;
  }

  public static LinkedHashMap<String, PFM_PortChange> calculateActiveErrorPorts(int topSize, LinkedHashMap<String, PFM_PortChange> activeErrorPorts)
  {
    // special case is if topSize == -1, calculate and sort all ports
    if (((topSize == -1) || (topSize > 0)) && (activeErrorPorts != null))
      if (activeErrorPorts.size() > 0)
      {
        if(topSize < 0)
          topSize = activeErrorPorts.size();
        
        LinkedHashMap<String, PFM_PortChange> activePorts = new LinkedHashMap<String, PFM_PortChange>();

        int rtnSize = 0;

        for (Map.Entry<String, PFM_PortChange> entry : activeErrorPorts.entrySet())
        {
          String key = entry.getKey();
          PFM_PortChange pc = entry.getValue();

//          System.err.println(key + ", " + pc.toErrorString());
          activePorts.put(key, pc);
          rtnSize = activePorts.size();
          if (rtnSize >= topSize)
            break;
        }
        return activePorts;
      }
      else
        logger.warning("No active port errors, unable to create top list");

    return null;
  }

  public boolean setMode(int numTop, SmtProperty topType, boolean continuous, SmtFilter filter)
  {
    Continuous = continuous;
    NumberOfTop = numTop;
    TopType = topType;
    smtFilter = filter;
    return true;
  }

  private void destroy(String msg)
  {
    logger.info("Terminating SmtTop");
    logger.info(msg);
    
    logger.severe("Ending now");
    System.err.println(msg);
  }

  public boolean analyzeOnce()
  {
    // dwell here if I don't already have the top results
    // calculated and ready
    
    while(!isReadyToAnalyze())
      try
      {
        TimeUnit.SECONDS.sleep(2L);
      }
      catch (InterruptedException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      OSM_FabricDelta delta = new OSM_FabricDelta(prevFabric, currFabric);
      calculateTops(delta, this.getTopType(), this.getNumberOfTop());
    
    return true;
  }


   public boolean isReadyToAnalyze()
   {
     // must have two different fabrics
     if((prevFabric == null) || (currFabric == null))
       return false;
     
     return true;
   }



  /************************************************************
   * Method Name:
   *  getTopPortChanges
   **/
  /**
   * Returns the value of topPortChanges
   *
   * @return the topPortChanges
   *
   ***********************************************************/
  
  public synchronized LinkedHashMap<String, PFM_PortChange> getTopPortChanges()
  {
    return TopPortChanges;
  }


  /************************************************************
   * Method Name:
   *  getTopLinkChanges
   **/
  /**
   * Returns the value of topLinkChanges
   *
   * @return the topLinkChanges
   *
   ***********************************************************/
  
  public synchronized LinkedHashMap<String, IB_Edge> getTopLinkChanges()
  {
    return TopLinkChanges;
  }


  /************************************************************
   * Method Name:
   *  getTopNodeChanges
   **/
  /**
   * Returns the value of topNodeChanges
   *
   * @return the topNodeChanges
   *
   ***********************************************************/
  
  public synchronized LinkedHashMap<String, IB_Vertex> getTopNodeChanges()
  {
    return TopNodeChanges;
  }


  /************************************************************
   * Method Name:
   *  getNumberOfTop
   **/
  /**
   * Returns the value of numberOfTop
   *
   * @return the numberOfTop
   *
   ***********************************************************/
  
  public int getNumberOfTop()
  { 
    return NumberOfTop;
  }


  /************************************************************
   * Method Name:
   *  getTopType
   **/
  /**
   * Returns the value of topType
   *
   * @return the topType
   *
   ***********************************************************/
  
  public SmtProperty getTopType()
  {
    return TopType;
  }


  /************************************************************
   * Method Name:
   *  setTopPortChanges
   **/
  /**
   * Sets the value of topPortChanges
   *
   * @param topPortChanges the topPortChanges to set
   *
   ***********************************************************/
  private static synchronized void setTopPortChanges(LinkedHashMap<String, PFM_PortChange> topPortChanges)
  {
    TopPortChanges = topPortChanges;
  }


  /************************************************************
   * Method Name:
   *  setVertexMap
   **/
  /**
   * Sets the value of vertexMap
   *
   * @param vertexMap the vertexMap to set
   *
   ***********************************************************/
  private static synchronized void setVertexMap(LinkedHashMap<String, IB_Vertex> vertexMap)
  {
    VertexMap = vertexMap;
  }


  /************************************************************
   * Method Name:
   *  setEdgeMap
   **/
  /**
   * Sets the value of edgeMap
   *
   * @param edgeMap the edgeMap to set
   *
   ***********************************************************/
  private static synchronized void setEdgeMap(LinkedHashMap<String, IB_Edge> edgeMap)
  {
    EdgeMap = edgeMap;
  }


  /************************************************************
   * Method Name:
   *  getVertexMap
   **/
  /**
   * Returns the value of vertexMap
   *
   * @return the vertexMap
   *
   ***********************************************************/
  
  public synchronized LinkedHashMap<String, IB_Vertex> getVertexMap()
  {
    return VertexMap;
  }


  /************************************************************
   * Method Name:
   *  getEdgeMap
   **/
  /**
   * Returns the value of edgeMap
   *
   * @return the edgeMap
   *
   ***********************************************************/
  
  public synchronized LinkedHashMap<String, IB_Edge> getEdgeMap()
  {
    return EdgeMap;
  }


  public OSM_FabricDelta getFabricDelta()
  {
    return new OSM_FabricDelta(prevFabric, currFabric);
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
  
  public synchronized SmtFilter getFilter()
  {
    return smtFilter;
  }
  
   /************************************************************
   * Method Name:
   *  setTopLinkChanges
   **/
  /**
   * Sets the value of topLinkChanges
   *
   * @param topLinkChanges the topLinkChanges to set
   *
   ***********************************************************/
  private synchronized void setTopLinkChanges(LinkedHashMap<String, IB_Edge> topLinkChanges)
  {
    TopLinkChanges = topLinkChanges;
  }

  /************************************************************
   * Method Name:
   *  setTopNodeChanges
   **/
  /**
   * Sets the value of topNodeChanges
   *
   * @param topNodeChanges the topNodeChanges to set
   *
   ***********************************************************/
  private synchronized void setTopNodeChanges(LinkedHashMap<String, IB_Vertex> topNodeChanges)
  {
    TopNodeChanges = topNodeChanges;
  }


  public boolean analyzeContinuously(int num, SmtProperty topType, SmtFilter filter)
  {
    setMode(num, topType, true, filter);
//    InputChar c = null;
    int code    = 26;
    String exitMessage = "done";
    
    // this is just a big blocking section, and the real work happens when one
    // of the two "Update" methods gets called.  It is expected that this analyzer
    // is setup as a listener for these updates, and that an Updater is feeding it
    // snapshots of OMS objects to analyze.
    //
    // continues until the ESC key is pressed.
    
    while(Continue_Thread)
      try
      {
        TimeUnit.SECONDS.sleep(2L);
        Continue_Thread = !(code == 27);
      }
      catch (Exception e)
      {
        e.printStackTrace();
        exitMessage = "Unexpected Exception: "+ e.getMessage();
        logger.severe(exitMessage);
        
        /* this should break the interactive screen program out of its infinite loop, and end the thread */
        Continue_Thread = false;
        
      }
    Continuous = false;
    // this should clean things up
    destroy(exitMessage);

    logger.severe("Done analyzing Tops");
    return true;
  }

  @Override
  public void osmServiceUpdate(OMS_Updater updater, OpenSmMonitorService osmService) throws Exception
  {
    // normally just get the current fabric, and pass it through as Fabric update, however...
    
    // handle the special initial condition of the Analyzer not having a previous and current
    // Fabric, but the updater DOES.  If so, get it from the updater, and signal the Analyser
    // with two Fabrics.
    
    // SMT_UpdateService
    
    if(prevFabric == null)
      logger.severe("Previous Fabric is Null");
    if(currFabric == null)
      logger.severe("Current Fabric is Null");
//    if(updater instanceof OMS_AbstractUpdateService)
//      logger.severe("updater is an instance of OMS_AbstractUpdateService");
//    else
//      logger.severe("updater is NOT an instance of OMS_AbstractUpdateService");

    
    if((prevFabric == null) && (currFabric == null) && (updater instanceof OMS_AbstractUpdateService))
    {
      OMS_AbstractUpdateService newUpdater = (OMS_AbstractUpdateService)updater;
      if(kickstartTop(newUpdater))
        return;
      
      logger.severe("Attempted to Kickstart TOP via a file or history from the Service, but was unsuccessful");
      System.err.println("Attempted to Kickstart TOP via a file or history from the Service, but was unsuccessful");
      System.err.println("If using a file, it may be corrupt, or may have only a single snapshot (minimum of 2 required)");

      System.exit(0);
    }
    
    // given a new osmService object, calculate the new tops
    if(osmService != null)
    {
      this.osmService = osmService;
      logger.severe("DEFAULT FABRIC UPDATE METHOD taosu: " + osmService.getTimeStamp().toString());
      osmFabricUpdateOrig(osmService.getFabric());
    }
  }

  private boolean kickstartTop(OMS_AbstractUpdateService newUpdater)
  {
    // return true, if able to kickstart Top with two Fabrics
    boolean success = false;
    
    logger.severe("Kickstarting the TopAnalyzer");
    logger.severe("KST: " + newUpdater.getClass().getCanonicalName());
      OMS_List oList = newUpdater.getOMS_List();
      
      logger.severe("The list size is: " + (oList != null? oList.size(): 0));
     // the list may be empty, or have one or two values in it.  Do the right thing.
      if((oList == null) || (oList.size() < 2))
        return success;
      
      // since we are here, we have a previous and current in the list
      
     OpenSmMonitorService[] oA = oList.getRecentOMSs(2);
     if(oA.length != 2)
       return success;
     
     for(OpenSmMonitorService o: oA)
      {
        logger.severe("TS is: " + o.getTimeStamp());
      }
     
     prevFabric = oA[0].getFabric();
     if(prevFabric == null)
       logger.severe("The Previous Fabric is still null, how can that be? " + oA[0].getTimeStamp());
     currFabric = oA[1].getFabric();
     if(currFabric == null)
       logger.severe("The Current Fabric is still null, how can that be? " + oA[1].getTimeStamp());
     
     logger.severe("ready     : " + isReadyToAnalyze());
    
     this.osmService = oA[1];
     
     if(isReadyToAnalyze())
     {
       logger.info("Ready to Calculate Tops");
       OSM_FabricDelta delta = new OSM_FabricDelta(prevFabric, currFabric);
       calculateTops(delta, this.getTopType(), this.getNumberOfTop());
       // assumes this is only used in SmtTop
       displayTop(TopType);
       success = true;
     }
     return success;
  }

  public void osmServiceUpdateOrig(OMS_Updater updater, OpenSmMonitorService osmService) throws Exception
  {
    // normally just get the current fabric, and pass it through as Fabric update, however...
    
    // handle the special initial condition of the Analyzer not having a previous and current
    // Fabric, but the updater DOES.  If so, get it from the updater, and signal the Analyser
    // with two Fabrics.
    
    // SMT_UpdateService
    
    if(prevFabric == null)
      logger.severe("Previous Fabric is Null");
    if(currFabric == null)
      logger.severe("Current Fabric is Null");
//    if(updater instanceof OMS_AbstractUpdateService)
//      logger.severe("updater is an instance of OMS_AbstractUpdateService");
//    else
//      logger.severe("updater is NOT an instance of OMS_AbstractUpdateService");

    
    if(((prevFabric == null) || (currFabric == null)) && (updater instanceof OMS_AbstractUpdateService))
    {
      logger.severe("Kickstarting the TopAnalyzer");
      OMS_AbstractUpdateService newUpdater = (OMS_AbstractUpdateService)updater;
      OMS_List oList = newUpdater.getOMS_List();
      
      logger.severe("The list size is: " + (oList != null? oList.size(): 0));
     // the list may be empty, or have one or two values in it.  Do the right thing.
      if((oList == null) || (oList.isEmpty()))
        return;
      
     OpenSmMonitorService[] oA = oList.getRecentOMSs(2);
     for(OpenSmMonitorService o: oA)
      {
        logger.severe("TS is: " + o.getTimeStamp());
      }
      
      if(oList.size() == 1)
      {
        // only has one, so make it the current, and previous will remain null
        this.osmService = oList.getCurrentOMS();
        osmFabricUpdate(osmService.getFabric());
        return;
      }
      
      // if here, the list contains two, set the previous one if necessary
      
      logger.severe("1b) Previous is: " + oList.getPreviousOMS().getTimeStamp().toString());
      logger.severe("1b) Current is : " + oList.getCurrentOMS().getTimeStamp().toString());
      
      logger.severe("2b) Previous is: " + oA[0].getTimeStamp().toString()); //
      logger.severe("2b) Current is : " + oA[1].getTimeStamp().toString()); //

      if(prevFabric == null)
      {
        this.osmService = oA[0];
        osmService = oA[0];
//        this.osmService = oList.getPreviousOMS();
        logger.severe("Setting the previous: " + osmService.getTimeStamp().toString());
        osmFabricUpdate(osmService.getFabric());
        TimeUnit.SECONDS.sleep(5);
      }
      
      osmService = oList.getCurrentOMS();
      logger.severe("Setting the current: " + osmService.getTimeStamp().toString());
    }
    
    // given a new osmService object, calculate the new tops
    if(osmService != null)
    {
      this.osmService = osmService;
      logger.severe("DEFAULT FABRIC UPDATE METHOD taosuo: " + osmService.getTimeStamp().toString());
      osmFabricUpdate(osmService.getFabric());
    }
  }

}
