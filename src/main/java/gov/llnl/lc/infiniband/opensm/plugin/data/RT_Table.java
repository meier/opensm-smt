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
 *        file: RT_Table.java
 *
 *  Created on: May 5, 2014
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.data;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.opensm.plugin.gui.tree.RT_PathTreeModel;
import gov.llnl.lc.infiniband.opensm.plugin.gui.tree.RT_PathTreePanel;
import gov.llnl.lc.infiniband.opensm.plugin.utils.IB_RouteParser;
import gov.llnl.lc.infiniband.opensm.plugin.utils.IB_RouteQuery;
import gov.llnl.lc.smt.SmtConstants;
import gov.llnl.lc.smt.command.SmtCommand;
import gov.llnl.lc.smt.manager.GraphSelectionManager;
import gov.llnl.lc.time.TimeStamp;
import gov.llnl.lc.util.BinList;

/**********************************************************************
 * Describe purpose and responsibility of RT_Table
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version May 5, 2014 1:30:27 PM
 **********************************************************************/
public class RT_Table implements Serializable, gov.llnl.lc.logging.CommonLogger, Comparable<RT_Table>
{
  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = -373306659394101046L;
  String FabricName;
  String RouteEngine;
  RT_TableType TableType;
  TimeStamp TableAge;
  
  /* a switch guid and switch table lookup map
   *   the key will be the switch guid, and the value is the RT_Node
   *   
   *   */
  HashMap<String, RT_Node> SwitchGuidMap = new HashMap<String, RT_Node>();
  
  /** compress the object when serializing it to a file? **/
  private static boolean useCompression = true;
  
//  private IB_Guid guid;
  
  /************************************************************
   * Method Name:
   *  RT_Table
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param fabricName
   * @param tableType
   * @param tableAge
   ***********************************************************/
  public RT_Table(String fabricName, String routeEngine, RT_TableType tableType, TimeStamp tableAge)
  {
    super();
    FabricName = fabricName;
    RouteEngine = routeEngine;
    TableType = tableType;
    TableAge = tableAge;
  }

  public RT_Table(String fabricName, String routeEngine)
  {
    super();
    FabricName = fabricName;
    RouteEngine = routeEngine;
    TableType = RT_TableType.RT_UNICAST;
    TableAge = new TimeStamp();
  }

  
  /************************************************************
   * Method Name:
   *  getLidGuidMap
   **/
  /**
   * Returns the value of lidGuidMap
   *
   * @return the lidGuidMap
   *
   ***********************************************************/
  
  public HashMap<String, Integer> getLidGuidMap()
  {
    /* every guid has a lid, but the lid can change, only the guid is guaranteed to remain constant
     *   the key will be the guid, and the value is the lid
     */
    HashMap<String, Integer> LidGuidMap = new HashMap<String, Integer>();

    if(SwitchGuidMap != null)
    {
      for(Map.Entry<String, RT_Node> entry: SwitchGuidMap.entrySet())
      {
        RT_Node rn = entry.getValue();
        LidGuidMap.putAll(rn.getLidGuidMap());
      }
    }
    return LidGuidMap;
  }

  public HashMap<Integer, IB_Guid> getGuidLidMap()
  {
    /* every guid has at least one lid, but can have multiple if LMC is non-zero
     * 
     * this is in fact, more correct than getLidGuidMap(), which assumes a one-to-one
     * match of GUID to LID
     */
    HashMap<Integer, IB_Guid> GuidLidMap = new HashMap<Integer, IB_Guid>();

    if(SwitchGuidMap != null)
    {
      for(Map.Entry<String, RT_Node> entry: SwitchGuidMap.entrySet())
      {
        RT_Node rn = entry.getValue();
        GuidLidMap.putAll(rn.getGuidLidMap());
      }
    }
    return GuidLidMap;
  }


  protected static java.util.ArrayList<String> readNodeFileList(String fileName, String path) throws IOException
  {
    String fName = SmtCommand.convertSpecialFileName(fileName);
    File inFile = new File(fName);
    if (!inFile.exists()) 
    {
      logger.severe("Could not find file: (" + fName +") for reading");
      return null;
    }
    
    java.util.ArrayList<String> FL     = new java.util.ArrayList<String>();

    BufferedReader br = new BufferedReader( new FileReader( inFile )) ;
    String readString = null;
    
    // don't include leading and trailing white space
    while(( readString = br.readLine())  != null)
    {
      String str = readString.trim();
      // skip empty lines and lines that start with #
      if((str.length() > 2) && (!str.startsWith("#")))
      {
        if((path != null) && (path.trim().length() > 1))
          FL.add(path.trim() + str);
        else
          FL.add(str);
       }
    }
    br.close(  ) ;
    return FL;
   }
  
  public int getNumChannelAdapters()
  {
    return getLidGuidMap().size() - getNumSwitches();
  }
  
  public int getNumSwitches()
  {
    return (SwitchGuidMap == null) ? 0: SwitchGuidMap.size();
  }
  
  public int getNumRoutes()
  {
    int total = 0;
    if(SwitchGuidMap != null)
    {
      for(Map.Entry<String, RT_Node> entry: SwitchGuidMap.entrySet())
      {
        RT_Node rn = entry.getValue();
        total += rn.getNumRoutes();
      }
    }
    return total;
  }
  
  public int getNumCaRoutes()
  {
    int total = 0;
    if(SwitchGuidMap != null)
    {
      for(Map.Entry<String, RT_Node> entry: SwitchGuidMap.entrySet())
      {
        RT_Node rn = entry.getValue();
        total += rn.getNumCaRoutes(this);
      }
    }
    return total;
  }
  
  public int getNumSwRoutes()
  {
    int total = 0;
    if(SwitchGuidMap != null)
    {
      for(Map.Entry<String, RT_Node> entry: SwitchGuidMap.entrySet())
      {
        RT_Node rn = entry.getValue();
        total += rn.getNumSwRoutes(this);
      }
    }
    return total;
  }
  
  public void add(RT_Node rtNode)
  {
    if((rtNode == null) || (rtNode.getGuid() == null))
    {
      logger.severe("RT_Node has null guid");
      return;
    }
    
    SwitchGuidMap.put(rtNode.getGuid().toColonString(), rtNode);
  }

  public static RT_Table readRT_Table(String fileName) throws FileNotFoundException, IOException, ClassNotFoundException
  {
    FileInputStream fileInput = new FileInputStream(fileName);
    ObjectInputStream objectInputStream = null;
    GZIPInputStream in = null;
    
    if(useCompression)
    {
      in = new GZIPInputStream(fileInput);
      objectInputStream = new ObjectInputStream(in);
    }
    else
      objectInputStream = new ObjectInputStream(fileInput);
    
    RT_Table obj = null;
    Object unknownObject = objectInputStream.readObject();
    if(unknownObject instanceof RT_Table)
      obj = (RT_Table) unknownObject;

    objectInputStream.close();
    if(useCompression)
      in.close();
    fileInput.close();
    return obj;
  }
  
  public static void writeRT_Table(String fileName, RT_Table table) throws IOException
  {
    if((fileName == null) || (table == null))
    {
      logger.severe("Can't write the routing table without a destination file, or the table (" + fileName + ")");
      if(table == null)
        logger.severe("  the table is null");
      return;
    }
    File file = new File(fileName);
    file.getParentFile().mkdirs();
    FileOutputStream fileOutput = new FileOutputStream(file);
    ObjectOutputStream objectOutput = null;
    GZIPOutputStream out = null;
    
    if(useCompression)
    {
      out =  new GZIPOutputStream(fileOutput);
      objectOutput = new ObjectOutputStream(out);
    }
    else
      objectOutput = new ObjectOutputStream(fileOutput);
    
    objectOutput.writeObject(table);
    objectOutput.flush();
    objectOutput.close();
    if(useCompression)
      out.close();
    fileOutput.close();
    return;
  }
  
  /************************************************************
   * Method Name:
   *  isUseCompression
   **/
  /**
   * Returns the value of useCompression
   *
   * @return the useCompression
   *
   ***********************************************************/
  
  public static boolean isUseCompression()
  {
    return useCompression;
  }

  /************************************************************
   * Method Name:
   *  setUseCompression
   **/
  /**
   * Sets the value of useCompression
   *
   * @param useCompression the useCompression to set
   *
   ***********************************************************/
  public static void setUseCompression(boolean useCompression)
  {
    RT_Table.useCompression = useCompression;
  }

  /************************************************************
   * Method Name:
   *  getGuid
   **/
  /**
   * Returns the value of guid
   *
   * @return the guid
   *
   ***********************************************************/
  
  public IB_Guid getGuid(int lid)
  {
    return getGuidLidMap().get(new Integer(lid));
  }

  /************************************************************
   * Method Name:
   *  getFabricName
   **/
  /**
   * Returns the value of fabricName
   *
   * @return the fabricName
   *
   ***********************************************************/
  
  public String getFabricName()
  {
    return FabricName;
  }
    
  public String getRouteEngine()
  {
    return RouteEngine;
  }
    
  public String getCacheFileName()
  {
    return getCacheFileName(this);
  }
  
  public static String getCacheFileName(RT_Table table)
  {
    if((table != null) && (table.getFabricName() != null))
      return getCacheFileName(table.getFabricName());
    return null;
  }
  
  public static String getCacheFileName(String fabricName)
  {
    if(fabricName != null)
    {
      // the name should be a combination of the cache location and the fabric name
      String fNam = fabricName + ".rt";
      String cNam = SmtConstants.SMT_DEFAULT_DIR + SmtConstants.SMT_CACHE_DIR;
      return cNam + fNam;
     
//      return "/home/meier3/.smt/cache/fabric.rt";      
    }
    return null;
  }
  

  
  
  
  

  /************************************************************
   * Method Name:
   *  getTableType
   **/
  /**
   * Returns the value of tableType
   *
   * @return the tableType
   *
   ***********************************************************/
  
  public RT_TableType getTableType()
  {
    return TableType;
  }

  /************************************************************
   * Method Name:
   *  getTableAge
   **/
  /**
   * Returns the value of tableAge
   *
   * @return the tableAge
   *
   ***********************************************************/
  
  public TimeStamp getTableAge()
  {
    return TableAge;
  }

  /************************************************************
   * Method Name:
   *  getSwitchGuidMap
   **/
  /**
   * Returns the value of switchGuidMap
   *
   * @return the switchGuidMap
   *
   ***********************************************************/
  
  public HashMap<String, RT_Node> getSwitchGuidMap()
  {
    return SwitchGuidMap;
  }

  /************************************************************
   * Method Name:
   *  getRT_Node
  **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   * @param swGuid
   * @return
   ***********************************************************/
  public RT_Node getRT_Node(IB_Guid swGuid)
  {
    if(swGuid != null)
      return SwitchGuidMap.get(swGuid.toColonString());
    return null;
  }

  public RT_Node getRT_Node(int lid)
  {
    return getRT_Node(getGuid(lid));
  }

  public RT_Port getRTPort(IB_Guid swGuid, int pNum)
  {
    if(swGuid != null)
    {
      // if I can't find an RT_Node, then there is no switch table
      //  it either doesn't exist yet, or this guid doesn't lead to a switch
      RT_Node nt = getRT_Node(swGuid);
      if(nt == null)
        return null;
      
      return nt.getRT_Port(pNum);
    }
    return null;
  }


  /************************************************************
   * Method Name:
   *  getLid
  **/
  /**
   * Get the first lid for this guid, or the value of zero, if
   * nothing can be found.
   *
   * @see     describe related java objects
   *
   * @param swGuid
   * @return
   ***********************************************************/
  public int getLid(IB_Guid guid)
  {
    ArrayList<Integer> lidArray = getLids(guid);
    if((lidArray != null) && (lidArray.size() > 0))
      return lidArray.get(0).intValue();
    
    return 0;
  }

  public ArrayList<Integer> getLids(IB_Guid guid)
  {
    // return all the lids for this guid (no duplicates)
    HashSet<Integer> lidSet = new HashSet<Integer>();
    if(guid != null)
    {
      // either not in the table, or it could be a CA
      for(Map.Entry<String, RT_Node> entry: SwitchGuidMap.entrySet())
      {
        RT_Node rn = entry.getValue();
        if(rn.contains(guid))
          lidSet.addAll(rn.getLids(guid));
      }
    }
    return new ArrayList<Integer>(Arrays.asList(lidSet.toArray(new Integer[0])));
  }
  
  public ArrayList<RT_Path> getRT_PathListFromHopBins(BinList<RT_Path> hopBins, int hops)
  {
    ArrayList<RT_Path> paths = new ArrayList<RT_Path>();
    
    int k=0;
    for(ArrayList <RT_Path> pList: hopBins)
    {
      String hopString = hopBins.getKey(k);
      int hopNum = Integer.parseInt(hopString);
      if(hopNum == hops)
        return pList;
      k++;
    }
    return paths;  // return empty, or null?
  }
  
  public ArrayList<RT_Path> getRT_PathsToCAs(IB_Guid source, OSM_Fabric fabric)
  {
    // given a guids, return a list of paths to ALL channel adapters
    ArrayList<RT_Path> paths = new ArrayList<RT_Path>();
    
    if(fabric != null)
    {
      // get all the nodes, filter on the type
      HashMap<String, OSM_Node> nodes = fabric.getOSM_Nodes();
        
      for(OSM_Node n: nodes.values())
      {
        if(OSM_NodeType.get(n) == OSM_NodeType.CA_NODE)
        {
          // this is a Channel Adapter, so get a path to it from this source
          RT_Path p = getRT_Path(source, n.getNodeGuid(), fabric);
          paths.add(p);
        }
      }
     }
    else
      logger.severe("Unable to build the hop list with a null fabric object");
    return paths;
  }
  
  public BinList<RT_Path> getPathHopBins(IB_Guid src, OSM_Fabric fab)
  {
    // using this source, and only CA's as destinations, bin up all the paths
    // from source to destinations, using #hops (legs) as the key
    BinList<RT_Path> hopBins = new BinList<RT_Path>();
    
    if(fab != null)
    {
      // using this source, and only CA's as destinations, count up the number of hops
      ArrayList<RT_Path> paths = getRT_PathsToCAs(src, fab );
      
      for(RT_Path p: paths)
      {
        int hops = p.getLegs().size();
        hopBins.add(p, Integer.toString(hops));
      }
    }
    else
      System.err.println("Unable to develop path bins for null objects");
    
    return hopBins;
  }


    
  public RT_Path getRT_Path(IB_Guid source, IB_Guid destination, OSM_Fabric fabric)
  {
    // given two guids, return the trace route
    return new RT_Path(source, destination, this, fabric);
  }
    
  public RT_Path getRT_Path(int source, int destination, OSM_Fabric fabric)
  {
    // given two lids, return the trace route
    return new RT_Path(getGuid(source), getGuid(destination), this, fabric);
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
    StringBuffer buff = new StringBuffer();
    
    buff.append("Name: " + FabricName + ", and Type: " + TableType.getTypeName() + ", TS: " + TableAge + ", num Switches: "+ SwitchGuidMap.size() + ", num routes: " + getNumRoutes());
    return buff.toString();
  }
  
  /************************************************************
   * Method Name:
   *  toStringVerbose
  **/
  /**
   * Describe the method here
   *
   * @see java.lang.Object#toString()
   *
   * @return
   ***********************************************************/
  
  public String toStringVerbose()
  {
    String smString = RT_Node.getMapString(SwitchGuidMap);
    return "RT_Table [FabricName=" + FabricName + ", RouteEngine=" + RouteEngine + ", TableType="
        + TableType + ", TableAge=" + TableAge + ", SwitchGuidMap=" + SwitchGuidMap.size() + "]\n"
    
        + smString + "\n";
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
  
  public String toInfo()
  {
    StringBuffer stringValue = new StringBuffer();
    stringValue.append(RT_Table.class.getSimpleName() + "\n");
    
    stringValue.append("fabric name:             " + this.getFabricName() + "\n");
    stringValue.append("timestamp:               " + this.toTimeString() + "\n");
    stringValue.append("routing engine:          " + this.getRouteEngine() + "\n");
    stringValue.append("tabletype:               " + this.getTableType().getTypeName() + "\n");
    stringValue.append("# switches:              " + this.getNumSwitches() + "\n");
    stringValue.append("# channel adapters:      " + this.getNumChannelAdapters() + "\n");
    stringValue.append("# routes:                " + this.getNumRoutes() + "\n");
    stringValue.append("# lids:                  " + this.getLidGuidMap().size() + "\n");
    stringValue.append("# min lid:               " + this.getMinLid() + "\n");
    stringValue.append("# max lid:               " + this.getMaxLid());
  
    return stringValue.toString();
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
    return getTableAge().toString();
  }

  public String toSwitchTableString(OSM_Fabric fabric)
  {
    StringBuffer buff = new StringBuffer();
    
     buff.append(RT_Node.getHeaderString(fabric) + "\n");

     // iterate through the switch nodes, and get their info
    if(SwitchGuidMap != null)
    {
      for(Map.Entry<String, RT_Node> entry: SwitchGuidMap.entrySet())
      {
        RT_Node rn = entry.getValue();
        buff.append(rn.toString(fabric) + "\n");
       }
      buff.append(SwitchGuidMap.size() + " valid switches dumped\n");
    }
     return buff.toString();
 }
  
  public String toIB_RouteString(IB_Guid swGuid, OSM_Fabric fabric)
  {
    RT_Node rn = getRT_Node(swGuid);
    if(rn != null)
      return rn.toIB_RouteString(this, fabric);
    return toString();
 }
  
  public String toIB_RouteString(int lid, OSM_Fabric fabric)
  {
    return toIB_RouteString(getGuid(lid), fabric);
 }
  
  /************************************************************
   * Method Name:
   *  buildRT_Table
  **/
  /**
   * Builds up the overall routing table for a fabric based on a collection
   * of files.  
   *
   * @see     describe related java objects
   *
   * @param tableName
   * @param fileList
   * @param resultPath
   * @return
   ***********************************************************/
  public static RT_Table buildRT_Table(String tableName, String fileList, String filePath)
  {
    RT_Table rtable = new RT_Table(tableName, "unknown");
    
    try
    {
      java.util.ArrayList<String> FL = RT_Table.readNodeFileList(fileList, filePath);
      
      for(String file: FL)
      {
        RT_Node node = new RT_Node(file);
        rtable.add(node);
        node.setParentTable(rtable);
//        System.out.println(node.toLongString());
      }
     }
    catch (Exception e)
    {
      logger.severe("Parse exception: " + e.getMessage());
    }
//    System.err.println(rtable.toString());
    
    return rtable;
  }
  
  public static RT_Table buildRT_Table(OSM_Fabric fabric)
  {
    // build the table using using the linear forwarding table in
    // the SBN_Switch object in the fabric.
    
    RT_Table rtable = null;
    
    if(fabric != null)
    {
      rtable = new RT_Table(fabric.getFabricName(true), fabric.getOsmSysInfo().RoutingEngine, RT_TableType.RT_UNICAST, fabric.getTimeStamp());

      OSM_Subnet Subnet = fabric.getOsmSubnet();
      
      if ((Subnet.Switches != null) && (Subnet.Switches.length > 0))
      {
        for(SBN_Switch s: Subnet.Switches)
        {
          RT_Node node = new RT_Node(s, fabric);
          if(node != null)
          {
            rtable.add(node);
            node.setParentTable(rtable);
          }
          else
            logger.severe("Couldn't construct an RT_Node from the SBN_Switch: " + s.toString());
        }
      }
     }
    else
      logger.severe("Unable to build the routing table with a null fabric object");

    return rtable;
  }
  
  public static RT_Table buildIB_RouteTable(OSM_Fabric fabric)
  {
    // build the table, if this command is running on the local box and as root
    // use ibroute -G to obtain all of the switch routing tables.
    
    logger.severe("Entering the Table Build Section");
    RT_Table rtable = null;
    
    if(fabric != null)
    {
      rtable = new RT_Table(fabric.getFabricName(true), fabric.getOsmSysInfo().RoutingEngine);

      // get all the nodes, filter on the type
      HashMap<String, OSM_Node> nodes = fabric.getOSM_Nodes();
        
      for(OSM_Node n: nodes.values())
      {
        if(OSM_NodeType.get(n) == OSM_NodeType.SW_NODE)
        {
          // this is a switch, so get its (switch) table, and add
          // it to the overall table
          IB_RouteQuery query = new IB_RouteQuery();
          int exitStatus = 0;
          try
          {
            exitStatus = query.queryGuid(n.getNodeGuid());
            
            // if the return code or exit status is zero, attempt to parse the results
            if(exitStatus == 0)
            {
              // use the specific command parser to build an RT_Node
              IB_RouteParser parser = new IB_RouteParser();
              parser.parseString(query.getResults().getOutput());
              RT_Node node = parser.getRT_node();
              if(node != null)
                rtable.add(node);
              else
                logger.severe("Couldn't construct a node from the parsed ibroute output for switch: " + n.toString());
            }
            else
            {
              logger.severe("The query results return code was: " + query.getResults().getReturnCode());
              logger.severe("The query results error was: " + query.getResults().getError());
            }
          }
            catch (Exception ioe)
            {
              logger.severe("Query exception: " + ioe.getMessage());
            }
        }
      }
     }
    else
      logger.severe("Unable to build the routing table with a null fabric object");

    logger.severe("Leaving the Table Build Section");
    return rtable;
  }

  public int getMinLid()
  {
    int min = 60000;
    if(SwitchGuidMap != null)
    {
      for(Map.Entry<String, RT_Node> entry: SwitchGuidMap.entrySet())
      {
        RT_Node rp = entry.getValue();
        int pMin = rp.getMinLid();
        min = pMin > min ? min: pMin;
      }
    }
    return min;
  }

  public int getMaxLid()
  {
    int max = 0;
    if(SwitchGuidMap != null)
    {
      for(Map.Entry<String, RT_Node> entry: SwitchGuidMap.entrySet())
      {
        RT_Node rp = entry.getValue();
        int pMax = rp.getMaxLid();
        max = pMax < max ? max: pMax;
      }
    }
    return max;
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
    String fileDir = "/home/meier3/.smt/cache/";
    String fileName = fileDir + "route-filelist.txt";
    
//    RT_Table rtable = RT_Table.buildRT_Table("cab", fileName, fileDir);
//    
//    try
//    {
//      RT_Table.writeRT_Table(fileDir + rtable.getFabricName() + "Table.rt", rtable);
//    }
//    catch (IOException e)
//    {
//      logger.severe("Write Table exception: " + e.getMessage());
//    }

    RT_Table ntable = null; 
    try
    {
      ntable = RT_Table.readRT_Table(fileDir + "sierra7.llnl.gov.rt");
      System.out.println(ntable.toString());
    }
    catch (Exception e)
    {
      logger.severe("Read Table exception: " + e.getMessage());
    }
    
    IB_Guid src = new IB_Guid("0x001175000077ef9e");
    IB_Guid dst = new IB_Guid("0x001175000077a00e");
    
    OSM_Fabric fabric = null;
    fileDir = "/home/meier3/scripts/OsmScripts/SmtScripts/";

    try
    {
      fabric = OSM_Fabric.readFabric(fileDir + "sierra.fab");
    }
    catch (Exception e)
    {
      logger.severe("Read Fabric exception: " + e.getMessage());
    }
     
    RT_Path path = ntable.getRT_Path(1209, 1466, fabric);
    System.err.println(path.toIB_TraceRtString());
    
    src = new IB_Guid("0x01175000079911e");
    dst = new IB_Guid(" 0x0011750000798c72");
    path = new RT_Path(src, dst);
    path.updateRoutingTable(ntable, fabric);
    System.err.println(path.toIB_TraceRtString());
////    
////    System.out.println(" ");
////    System.out.println(ntable.toIB_RouteString(src, fabric));
//   ////////////////////////////////////////////////////////////////////////////// 
//    
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLocation(100, 50);

    // get the data model from OMS, connection, or file

//    RT_NodeTreeModel model = new RT_NodeTreeModel(src, ntable, fabric);
//    RT_NodeTreePanel vtp = new RT_NodeTreePanel();
//    vtp.setTreeModel(model);
//    
    RT_PathTreeModel model = new RT_PathTreeModel(path, true);
    RT_PathTreePanel vtp = new RT_PathTreePanel();
    GraphSelectionManager.getInstance().addIB_GraphSelectionListener(vtp);

    
//    RT_TableTreeModel model = new RT_TableTreeModel(ntable, fabric);
//    RT_TableTreePanel vtp = new RT_TableTreePanel();

    vtp.setTreeModel(model);
    
 
    JScrollPane scroller = new JScrollPane(vtp);  
    frame.getContentPane().add(scroller, BorderLayout.CENTER); 
    frame.pack();
    frame.setVisible(true);
//    
//    System.err.println("The size of the LidGuidMap is: " + rtable.getLidGuidMap().size());
//    System.err.println("The size of the GuidLidMap is: " + rtable.getGuidLidMap().size());
//    
//
//    
//    
//    
    
    
    
    
    
  }

  public boolean contains(IB_Guid source)
  {
    // return true if this guid is anywhere in the switch guid Map
    if(SwitchGuidMap != null)
    {
      // before going further, check to see if this is one of the switches
      if(isSwitch(source))
        return true;
      
      for(Map.Entry<String, RT_Node> entry: SwitchGuidMap.entrySet())
      {
        RT_Node rn = entry.getValue();
        if(rn.contains(source))
          return true;
      }
    }
    return false;
  }
  
  public boolean contains(int lid)
  {
    // return true if this lid is anywhere in the switch guid  Map
    if(SwitchGuidMap != null)
    {
      for(Map.Entry<String, RT_Node> entry: SwitchGuidMap.entrySet())
      {
        RT_Node rn = entry.getValue();
        
        if(rn.getLid() == lid)
          return true;
        
        if(rn.contains(lid))
          return true;
      }
    }
    return false;
  }

  public boolean isSwitch(IB_Guid source)
  {
    // return true if this guid matches one of the RT_Node
    if((source != null) && (SwitchGuidMap != null) && (SwitchGuidMap.containsKey(source.toColonString())))
      return true;
    return false;
  }

  public boolean isSwitch(int lid)
  {
    // return true if this lid matches one of the RT_Nodes
    if(SwitchGuidMap != null)
    {
      for(Map.Entry<String, RT_Node> entry: SwitchGuidMap.entrySet())
      {
        RT_Node rn = entry.getValue();
        
        if(rn.getLid() == lid)
          return true;
      }
    }
    return false;
  }

  /************************************************************
   * Method Name:
   *  compareTo
  **/
  /**
   * RT_Ports are considered to be the same, if their parent guids
   * and port numbers match.
   *
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   * @param   describe the parameters
   *
   * @return  describe the value returned
   ***********************************************************/
  
  @Override
  public int compareTo(RT_Table table)
  {    
    // the guid is the only thing that MUST be unique
        //
    // both object must exist (and of the same class)
    // and should be consistent with equals
    //
    // -1 if less than
    // 0 if the same
    // 1 if greater than
    //
    if(table == null)
            return -1;
    
    if(table.getFabricName() == null)
      return -1;
    
    int result = this.getFabricName().compareTo(table.getFabricName());

    // if the names are the same, compare the number of routes
    if(result == 0)
      result = this.getNumRoutes() - table.getNumRoutes();
    return result;
  }

}
