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
 *        file: BinValueKeys.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

/**********************************************************************
 * An object that constructs and contains a set of keys for Bins.
 * Given the desired number of bins, and the range of values of
 * all the bins combined, this object will create a set of keys
 * for all of the bins.
 * <p>
 * @see  gov.llnl.lc.infiniband.opensm.plugin.utils.OSM_TimeSeriesCollection
 *
 * @author meier3
 * 
 * @version May 30, 2013 8:41:02 AM
 **********************************************************************/
public class BinValueKeys
{
  public static final String ZERO_BIN_KEY = "0 to ";
  
  private ValueRange range;  // this bin holds this range of values
  
  private int NumBins;       // the number of bins
  private LinkedHashMap <String, ValueRange> binMap;  // the set of ranges, organized in a map, with keys
  
  
  public Set<String> keySet()
  {
    return binMap.keySet();
  }
  
  public Collection<ValueRange> values()
  {
    return binMap.values();
  }
  
  /************************************************************
   * Method Name:
   *  getRange
   **/
  /**
   * Returns the value of range
   *
   * @return the range
   *
   ***********************************************************/
  
  public ValueRange getRange()
  {
    return range;
  }

  /************************************************************
   * Method Name:
   *  getNumBins
   **/
  /**
   * Returns the value of numBins
   *
   * @return the numBins
   *
   ***********************************************************/
  
  public int getNumBins()
  {
    return NumBins;
  }

  /************************************************************
   * Method Name:
   *  getBinMap
   **/
  /**
   * Returns the value of binMap
   *
   * @return the binMap
   *
   ***********************************************************/
  
  public HashMap<String, ValueRange> getBinMap()
  {
    return binMap;
  }

  public ValueRange getValueRange(long value)
  {
    for(Entry<String, ValueRange> mapEntry: binMap.entrySet())
    {
      ValueRange r = mapEntry.getValue();
      if(r.inRange(value))
        return r;
    }
    return null;
  }
  
  public String getKey(long value)
  {
    for(Entry<String, ValueRange> mapEntry: binMap.entrySet())
    {
      ValueRange r = mapEntry.getValue();
      if(r.inRange(value))
        return mapEntry.getKey();
    }
    return null;
  }

  /************************************************************
   * Method Name:
   *  BinValueKeys
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param max
   * @param min
   * @param numBins
   ***********************************************************/
  public BinValueKeys(long max, long min, long scale, int numBins, boolean useLabels, boolean forceZeroBin)
  {
    this(new ValueRange(max, min, scale), numBins, useLabels, forceZeroBin);
  }

  public BinValueKeys(ValueRange range, int numBins, boolean useLabels, boolean forceZeroBin)
  {
    if((range.Max == range.Min) || (numBins < 1))
      return;
    
    NumBins = numBins;
    this.range = range;
    Long scale = range.Scale;
    
    binMap = new LinkedHashMap <String, ValueRange>();
    
    // if including a zero bin, then adjust the bins to accommodate
    long min = forceZeroBin ? 0L: range.Min;
    
    long binSize = ((range.Max - min)/NumBins);
    
    long from = min;
    long to   = from + binSize -1;
    
    for(int i=0; i<numBins;i++)
    {
      ValueRange r = new ValueRange(to, from, scale);
      if(useLabels)
        binMap.put(r.getLabel(), r);
      else
        binMap.put(r.getKey(), r);
      
      from = to +1;
      to   = from + binSize -1;
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
   ***********************************************************/
  public static void main(String[] args)
  {
    // TODO Auto-generated method stub

  }

}
