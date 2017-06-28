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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JFileChooser;

import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;
import gov.llnl.lc.smt.command.fabric.SmtFabric;
import gov.llnl.lc.smt.event.SmtMessage;
import gov.llnl.lc.smt.event.SmtMessageType;
import gov.llnl.lc.smt.manager.MessageManager;
import gov.llnl.lc.smt.props.SmtProperty;
import gov.llnl.lc.util.NodeList;
import gov.llnl.lc.util.filter.WhiteAndBlackListFilter;

/**********************************************************************
 * A simple string filter, that utilizes a white list (allowed) and
 * black list (denied) to implement the filter.  Additionally, a list
 * of files can be used to describe the white and black lists.
 * 
 * Once constructed, the primary way to use the filter is by the test
 * isFiltered(String).
 * 
 * <p>
 * @see  WhiteAndBlackListFilter
 *
 * @author meier3
 * 
 * @version Sep 18, 2013 1:25:05 PM
 **********************************************************************/
public class SmtFilter extends gov.llnl.lc.util.filter.WhiteAndBlackListFilter
{
  static final String NeighborIntroducer = "* NBWhite";
  
  String FilterFileName = "unknown";
  String Description    = "unknown";
 
  /************************************************************
   * Method Name:
   *  getFilterFileName
  **/
  /**
   * Returns the value of filterFileName
   *
   * @return the filterFileName
   *
   ***********************************************************/
  
  public String getFilterFileName()
  {
    return FilterFileName;
  }

  /************************************************************
   * Method Name:
   *  getDescription
  **/
  /**
   * Returns the value of description
   *
   * @return the description
   *
   ***********************************************************/
  
  public String getDescription()
  {
    return Description;
  }

  /************************************************************
   * Method Name:
   *  setDescription
  **/
  /**
   * Sets the value of description
   *
   * @param description the description to set
   *
   ***********************************************************/
  public void setDescription(String description)
  {
    Description = description;
  }

  /************************************************************
   * Method Name:
   *  SmtFilter
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
   *  SmtFilter
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
    this(filterFileName, null);
  }
  
  /************************************************************
   * Method Name:
   *  SmtFilter
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
    super(configMap, SmtProperty.SMT_FILTER_FILE.getName());
    SmtFilter f = adjustForNodeLists(this, null);
    if(f != null)
    {
      setWhiteList(f.getWhiteList());
      setBlackList(f.getBlackList());
      setFileList(f.getFileList());
      setFilterName(f.getFilterName());
    }
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
    return "SmtFilter [FilterFileName=" + FilterFileName + ", Description=" + Description
        + ", WhiteList=" + WhiteList.size() + ", BlackList=" + BlackList.size() + ", FileList=" + FileList.size()
        + ", getFilterID()=" + getFilterID() + "]";
  }

  /************************************************************
   * Method Name:
   *  SmtFilter
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param whiteList
   * @param blackList
   ***********************************************************/
  public SmtFilter(String name, ArrayList<String> whiteList, ArrayList<String> blackList, ArrayList<String> fileList)
  {
    super(name, whiteList, blackList, fileList);
  }


  /************************************************************
   * Method Name:
   *  SmtFilter
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param val
   * @param oMService
   ***********************************************************/
  public SmtFilter(String filterFileName, OpenSmMonitorService OMS) throws IOException
  {
    super(filterFileName);
    String fName = convertSpecialFileName(filterFileName);
    if(fName != null)
      FilterFileName = fName;
    SmtFilter f = adjustForNodeLists(this, OMS);
    if(f != null)
    {
      setWhiteList(f.getWhiteList());
      setBlackList(f.getBlackList());
      setFileList(f.getFileList());
      setFilterName(f.getFilterName());
    }
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
      System.out.println("\nThe filter name is: " + filter.getFilterName());
      System.out.println("The filter ID is: " + filter.getFilterID().toString());
      
      NodeList nl = new NodeList("catalyst[1-16,18-22,55,98-100]");
      
      SmtFilter flter = SmtFilter.createFilterFromNodeList("Testing", nl, null, false);
      SmtFilter.saveToFile(flter, "Testing", true);
    }
  }

  /************************************************************
   * Method Name:
   *  createFilterFromCollection
  **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   * @param string
   * @param picked
   * @return
   ***********************************************************/
  public static SmtFilter createFilterFromCollection(String name, Collection picked)
  {
    // the collection is IB_Vertex that represent nodes, selected from a graph
    //  by default, this is a white list
    //
    // the optional name is the filters name
    // if empty, include all results.  if not empty, only include results that contain a string in this list
    return SmtFilter.createFilterFromCollection( name, picked, null, false);
  }

  public static SmtFilter createFilterFromCollection(String name, Collection picked, OpenSmMonitorService OMService, boolean includeNeighbors)
  {
    // the collection is IB_Vertex that represent nodes, selected from a graph
    //  by default, this is a white list
    //
    // the optional name is the filters name
    // if empty, include all results.  if not empty, only include results that contain a string in this list
    java.util.ArrayList<IB_Guid> guidList     = new java.util.ArrayList<IB_Guid>();
    String firstVertex = "unknown";
    
    for (Object pv : picked)
    {
      if (pv instanceof IB_Vertex)
      {
        IB_Vertex pV = (IB_Vertex)pv;
        guidList.add(pV.getGuid());
        firstVertex = pV.getName();
      }
    }
    
    // make sure the filter has a name
    if((name == null) || (name.length() < 1))
      name = firstVertex;
    
    return SmtFilter.createFilterFromGuidList( name, guidList, OMService, includeNeighbors);
  }

  /************************************************************
   * Method Name:
   *  createFilterFromCollection
  **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   * @param string
   * @param picked
   * @return
   ***********************************************************/
  public static SmtFilter createFilterFromGuidList(String name, ArrayList<IB_Guid> guidList, OpenSmMonitorService OMService, boolean includeNeighbors)
  {
    java.util.ArrayList<String> WhiteList = new java.util.ArrayList<String>();
    HashSet<String> NeighborSet = new HashSet<String>();
    LinkedHashMap<String, IB_Vertex> VertexMap = null;

    // make sure the filter has a name
    if ((name == null) || (name.length() < 1))
      name = "unknown";

    if ((OMService != null) && includeNeighbors)
      VertexMap = IB_Vertex.createVertexMap(OMService.getFabric());

    for (IB_Guid g : guidList)
    {
      WhiteList.add(g.toColonString());
      if ((OMService != null) && includeNeighbors)
      {
        // find neighbors and add them
        IB_Vertex thisV = VertexMap.get(g.toColonString());

        LinkedHashMap<String, IB_Vertex> myNeighbors = thisV.getNeighborMap();
        for (Entry<String, IB_Vertex> entry : myNeighbors.entrySet())
        {
          IB_Vertex nV = entry.getValue();
          NeighborSet.add(nV.getGuid().toColonString());
        }
      }
    }

    if (!NeighborSet.isEmpty())
    {
      WhiteList.add("#");
      WhiteList.add(SmtFilter.NeighborIntroducer);
      for (String ns : NeighborSet)
      {
        WhiteList.add(ns);
      }

    }
    return new SmtFilter(name, WhiteList, null, null);
  }

  public static SmtFilter createFilterFromNodeList(String name, String nodeList)
  {
    // the nodeList string represents the value obtained from an sqlog output
    // in the form;
    //     catalyst[1-66,68-72,74-76,78-106,108-113,115-133]
    //
    //  which can be parsed in the same way as WhatsUp.
    //
    // the optional name is the filters name
    //
    // this list represents a WhiteList of nodes, or HCA's to include
    // in the results
    
    return SmtFilter.createFilterFromNodeList(name, new NodeList(nodeList));
  }

  public static SmtFilter createFilterFromNodeList(String name, NodeList nodeList)
  {
    // the nodeList contains a list of compute node names.  hopefully, these
    // compute node names correspond to the IB names for the HCA's
    //
    // the optional name is the filters name
    //
    // this list represents a WhiteList of nodes, or HCA's to include
    // in the results
    
     return SmtFilter.createFilterFromNodeList(name, nodeList, null, false);
  }

  public static SmtFilter createFilterFromNodeList(String name, NodeList nodeList, OpenSmMonitorService OMService, boolean includeNeighbors)
  {
    // the NodeList represents the primary nodes of interest (and their interfaces)
    // if the OMService is supplied with a non-zero number of extra levels, then
    // a filter will be constructed including nodes, or interfaces, the specified
    // number of levels distant (up only).
    //
    // the optional name is the filters name
    //
    // this resultant list represents a WhiteList of guids to include
    //
    // example:
    //    * White
    //    f452:1403:0057:9b30
    //    f452:1403:0057:9440
    //    f452:1403:0057:ae00
    //    f452:1403:0057:9d70
    //    0002:c902:004b:0c68
    //    #
    //    L1
    //    f452:1403:0057:9470
    //    f452:1403:0057:94c0
    //    f452:1403:0057:94a0
    //    f452:1403:0057:95b0
    //    f452:1403:0057:96f0
    //    f452:1403:0057:9c40
    //    #
    //    L2
    //    f452:1403:0057:9630
    //    f452:1403:0057:91a0
    //    f452:1403:005d:fa30
    //
    //    
    // 
    
    // make sure the filter has a name
    if((name == null) || (name.length() < 1))
      // use the nodelists name
      name = nodeList.getNodeName();
    
    if(OMService != null)
    {
      ArrayList<IB_Guid> guidList = SmtFabric.getGuidsFromHostNameList(OMService, nodeList.getListOfNodes());
      return SmtFilter.createFilterFromGuidList( name, guidList, OMService, includeNeighbors);
    }
    return new SmtFilter(name, nodeList.getListOfNodes(), null, null);
  }
  
  public static void saveToFile(SmtFilter filter, String description, boolean includeNeighbors)
  {
    MessageManager Message_Mgr   = MessageManager.getInstance();
    SmtFilter flter = filter;
    if(!includeNeighbors)
      flter = SmtFilter.removeNeighbors(flter);

    try
    {
      flter.setDescription("fabric:    " + description);
      
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setDialogTitle("Save the Filter to a file");

      int userSelection = fileChooser.showSaveDialog(null);

      if (userSelection == JFileChooser.APPROVE_OPTION)
      {
        File fileToSave = fileChooser.getSelectedFile();
        try
        {
          Message_Mgr.postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INFO,
              "Save filter to file: " + fileToSave.getAbsolutePath()));
          SmtFilter.writeFilter(fileToSave.getAbsolutePath(), flter, flter.getDescription());
        }
        catch (IOException e1)
        {
          Message_Mgr.postMessage(new SmtMessage(SmtMessageType.SMT_MSG_SEVERE,
              "Error Saving filter to file: " + fileToSave.getAbsolutePath()));
        }
      }
    }
    catch (Exception e1)
    {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
  }

  /************************************************************
   * Method Name:
   *  adjustForNodeLists
  **/
  /**
   * Create a duplicate filter, and expand node lists, if they exist
   *
   * @see     describe related java objects
   *
   * @param flter
   * @return
   ***********************************************************/
  private static SmtFilter adjustForNodeLists(SmtFilter flter, OpenSmMonitorService OMS)
  {
    SmtFilter filter = adjustForNodeLists(flter, OMS, true);
    return adjustForNodeLists(filter, OMS, false);
  }
  
  private static SmtFilter adjustForNodeLists(SmtFilter flter, OpenSmMonitorService OMS, boolean whiteList)
  {
    // return a new filter, potentially modified, if there is a nodeList specification
    java.util.ArrayList<String> newList     = new java.util.ArrayList<String>();
    java.util.ArrayList<String> origList    =  whiteList ? flter.getWhiteList(): flter.getBlackList();
    
    for(String element: origList)
    {
      // this looks like a node list, so expand it if possible
      if(element.contains("[") && element.contains("]"))
      {
        // make an attempt to convert it to guids, otherwise just leave it as names
        NodeList nl = new NodeList(element);
        if(OMS != null)
        {
          SmtFilter tmp = createFilterFromNodeList("tmp", nl, OMS, true);
          ArrayList<String> guidList = tmp.getWhiteList();
          for(String node: guidList)
            newList.add(node);
        }
        else
        {
          java.util.ArrayList<String> nodeList = nl.getListOfNodes();
          for(String node: nodeList)
            newList.add(node);
        }
      }
      else
        newList.add(element);
    }
    String desc = flter.getDescription();
    java.util.ArrayList<String> wl          = whiteList ? newList: flter.getWhiteList();
    java.util.ArrayList<String> bl          = whiteList ? flter.getBlackList(): newList;

    SmtFilter f = new SmtFilter(flter.getFilterName(), wl, bl, flter.getFileList());
    
    return f; 
  }

  /************************************************************
   * Method Name:
   *  removeNeighbors
  **/
  /**
   * Create a duplicate filter, but leave out neighbor nodes if they exist
   *
   * @see     describe related java objects
   *
   * @param flter
   * @return
   ***********************************************************/
  private static SmtFilter removeNeighbors(SmtFilter flter)
  {
    // only modify the white list
    java.util.ArrayList<String> WhiteList     = new java.util.ArrayList<String>();
    java.util.ArrayList<String> origWL        = flter.getWhiteList();
    boolean removed = false;
    
    for(String white: origWL)
    {
      // stop when I encounter the neighbor delimiter, everything after can be ignored
      if(white.startsWith(SmtFilter.NeighborIntroducer))
      {
        removed = true;
        break;
      }
      WhiteList.add(white);
    }
    String desc = flter.getDescription();
    SmtFilter f = new SmtFilter(flter.getFilterName(), WhiteList, flter.getBlackList(), flter.getFileList());
    f.setDescription(removed? desc + " (neighbors removed)": desc);
    
    return f; 
  }
}
