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
 *        file: ValueRange.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.util;

/**********************************************************************
 * An object that represents a range of long values.  The is an upper and
 * a lower limit, and this object can be used to test if another value
 * is within this range or outside of it.
 * 
 * An optional scaling value can be associated with this range which is
 * used solely for labeling purposes.
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version May 30, 2013 8:41:51 AM
 **********************************************************************/
public class ValueRange
{
  long Max;
  long Min;
  
  /** a scale value (should also include units??) only used for the label
   * 
   */
  long Scale = 1L;
  
  

  /************************************************************
   * Method Name:
   *  ValueRange
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param max
   * @param min
   ***********************************************************/
//  public ValueRange(long max, long min)
//  {
//    this(max, min, 1L);
//  }
    /************************************************************
     * Method Name:
     *  ValueRange
    **/
    /**
     * Describe the constructor here
     *
     * @see     describe related java objects
     *
     * @param max
     * @param min
     ***********************************************************/
    public ValueRange(long max, long min, long scale)
    {
    super();
    Max = max;
    Min = min;
    Scale = Math.abs(scale);
    
    if(max < min)
    {
      Max = min;
      Min = max;
    }
  }
  
  public boolean inRange(long value)
  {
    // return true if the value is between the min and max
    if(value <= Max)
      if(value >= Min)
        return true;
    return false;
  }

  /************************************************************
   * Method Name:
   *  getMax
   **/
  /**
   * Returns the value of max
   *
   * @return the max
   *
   ***********************************************************/
  
  public long getMax()
  {
    return Max;
  }

  /************************************************************
   * Method Name:
   *  getMin
   **/
  /**
   * Returns the value of min
   *
   * @return the min
   *
   ***********************************************************/
  
  public long getMin()
  {
    return Min;
  }

  /************************************************************
   * Method Name:
   *  getScale
   **/
  /**
   * Returns the value of scale
   *
   * @return the scale
   *
   ***********************************************************/
  
  public long getScale()
  {
    return Scale;
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
  
  public String getKey()
  {
    return  (String)(Min + " to " + Max);
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
  
   public String getLabel()
  {
     // same as get key, except scaled
     long min = Min/Scale;
     long max = Max/Scale;
     
     return  (String)(min + " to " + max);
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
    return "ValueRange [Max=" + Max + ", Min=" + Min + "]";
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
