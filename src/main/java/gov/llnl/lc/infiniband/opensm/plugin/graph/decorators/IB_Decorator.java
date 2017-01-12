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
 *        file: IB_Decorator.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.graph.decorators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Node;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.util.BinList;

public class IB_Decorator implements CommonLogger
{
  // see IB_LevelTransformer for how the levels are colored
  
  public static final String LEVEL="Level";
  private String Name;
  private int Number;
  public IB_Decorator(String name, int number)
  {
    super();
    Name = name;      // a unique name
    Number = number;  // a unique positive number (from zero up)
  }
  public String getName()
  {
    return Name;
  }
  public void setName(String name)
  {
    Name = name;
  }
  public int getNumber()
  {
    return Number;
  }
  public void setNumber(int number)
  {
    Number = number;
  }
  
  public String getKey()
  {
    return IB_Decorator.getKey(this.Name);
  }
  
  public static String getKey(String name)
  {
    return "Name=" + name;
  }
  
  public static void setAllNameDecorators(HashMap<String, IB_Vertex> vMap, int nChars)
  {
    if((vMap != null) && (vMap.size() > 1))
    {
      // each bin is for similarly named vectors (so gets the same decorator)
      BinList <IB_Vertex> nVertexBins = new BinList <IB_Vertex>();
      
      int num     = 0;
      for (Entry<String, IB_Vertex> entry : vMap.entrySet())
      {
        IB_Vertex v = entry.getValue();
        if(v != null)
        {
          // bin up the names using only the first three letters
          nVertexBins.add(v, v.getNode().pfmNode.node_name.substring(0, nChars).toLowerCase());
        }
      }
      // iterate through the bins, and create a single decorator for each vector in the bin
      int k=0;
      for(ArrayList <IB_Vertex> vn: nVertexBins)
      {
        IB_Decorator td = new IB_Decorator(nVertexBins.getKey(k), k);
        logger.info("The Vertices tagged (" + td.getName() + ") has " + vn.size() + " elements in it");
        // decorate all those vertices the same
        for(IB_Vertex v: vn)
        {
          v.setDecorator(td);
        }
        k++;
      }
    }
  }
  
  public static void setManagerDecorator(HashMap<String, IB_Vertex> vMap, OSM_Node mgr)
  {
    // find the vertex in the map that matches this node, and decorate it in a special way
    if(mgr != null)
    {
      IB_Vertex vm = vMap.get(IB_Vertex.getVertexKey(mgr.getNodeGuid()));
      if(vm != null)
        vm.setDecorator(new IB_Decorator("Manager", IB_LevelTransformer.getManagerNumber()));
    }
    else
    {
      logger.warning("Could not decorate a null manager node IBD");
    }
  }
  
  public static void setAllNameDecorators(ArrayList<IB_Vertex> vL, int nChars)
  {
    IB_Decorator.setAllNameDecorators(IB_Vertex.createVertexMap(vL), nChars);
  }
  
  private static int setLevelDecorators(ArrayList<IB_Vertex> vL, int levelNumber)
  {
    // walk through the vertices, and set all leaf vertex (with only a single edge) to
    // LEVEL 1
    // walk through the vertieses again, skip LEVEL 1, set all vertex that touch LEVEL 1
    // vertex to LEVEL 2
    // walk through the verteces again, skip LEVEL 1, 2, set all vertex that touch LEVEL 2
    // vertex to LEVEL 3
    // repeat until done.
    
    int num = 0;
    String LevelName = IB_Decorator.LEVEL + levelNumber; 
    if((vL != null) && (vL.size() > 0))
    {
      // now loop through the vertices, check their edges to see if they touch a lower level edge
      for(IB_Vertex tv: vL)
      {
        // Special case for level 1
        if(levelNumber == 1)
        {
          // TODO - check if the node in this Vertex thinks its a switch
          //       Don't make a Switch Level 1
          //       Some HCAs or leaf nodes (level 1) may have 2 ports, or edges
          if(tv.getEdgeMap().size() <= 1)
          {
            tv.setDecorator(new IB_Decorator(LevelName, levelNumber));
            num++;
            if(tv.getEdgeMap().size() == 0)
              logger.warning("No edges for this vertex");
          }          
        }
        else
        {
        // skip vertices already assigned a level
        if((tv.getDecorator() != null) && (tv.getDecorator().getName() != null) && (tv.getDecorator().getName().startsWith(IB_Decorator.LEVEL)))
          continue;
        
        // assign only this level
        if(tv.isTouching(levelNumber -1))
        {
          tv.setDecorator(new IB_Decorator(LevelName, levelNumber));
          num++;        
        }
      }
    }
//      if(num > 0)
//        logger.info("There are " + num + " " + LevelName + " vertices");          
    }
    if((num <=0) && (levelNumber ==1))
    {
      logger.severe("Error, there must be at least one Level One vertex!");
      System.exit(-1);
    }
    
    return num;   
  }  
  

  public static void setAllLevelDecorators(ArrayList<IB_Vertex> vL)
  {
    // Level 1 is the leaf node level, and should only have a single edge or link
    // to an edge switch (Level 2)
    
    // Work your way up from level 1, assign levels, and determine the next level based
    // on previous work.
    
    // Stop when no vertices are assigned a level - must be done!
    
    int levelNumber  = 1;
    int num = 1;  // just to get it started
    
    while(num > 0)
      num = setLevelDecorators(vL, levelNumber++);  // do this level, go to next
  }

  public static void setAllLevelDecorators(HashMap<String, IB_Vertex> vMap)
  {
    if(vMap != null)
      IB_Decorator.setAllLevelDecorators(new ArrayList<IB_Vertex>(vMap.values()));
  }


  @Override
  public String toString()
  {
    return "IB_Decorator [Name=" + Name + ", Number=" + Number + "]";
  }

}
