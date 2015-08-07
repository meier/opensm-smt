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
 *        file: PFM_PortRate.java
 *
 *  Created on: Jul 10, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.data;

import java.math.BigInteger;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**********************************************************************
 * Describe purpose and responsibility of PFM_PortRate
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Jul 10, 2013 9:15:36 AM
 **********************************************************************/
public class PFM_PortRate
{
  
  private PFM_PortChange PortChange;
  private  HashMap<String,Long> Rates = new HashMap<String,Long>();
  
  /************************************************************
   * Method Name:
   *  PFM_PortRate
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param portChange
   ***********************************************************/
  public PFM_PortRate(PFM_PortChange portChange)
  {
    super();
    PortChange = portChange;
    
    // precalculate everything, because BigIntegers are expensive
  }
  
  public PFM_PortChange getPortChange()
  {
    return PortChange;
  }


  /************************************************************
   * Method Name:
   *  getChangeRate
  **/
  /**
   * Calculates the rate of change of the named counter.  No scaling.
   *
   * @see     describe related java objects
   * @param   describe the parameters
   *
   * @return  describe the value returned
   * @param portChange
   * @param name
   * @return
   ***********************************************************/
  public long getChangeRate(PFM_Port.PortCounterName name)
  {
    // check to see if we already calculated this "named" counter
    Long Val = Rates.get(name.getName());
    long val = 0L;
    if(Val == null)
    {
      // calculate and save here, scale by one
      val = PFM_PortRate.getChangeRateLong(this.PortChange, name, PortCounterUnits.COUNTS);
      Rates.put(name.getName(), new Long(val));
    }
    else
      val = Val.longValue();
    return val;
  }

  /************************************************************
   * Method Name:
   *  getChangeRate
  **/
  /**
   * Calculates the rate of change of the named counter.
   *
   * @see     describe related java objects
   * @param   describe the parameters
   *
   * @return  describe the value returned
   * @param portChange
   * @param name
   * @return
   ***********************************************************/
  public static long getChangeRate(PFM_PortChange portChange, PFM_Port.PortCounterName name)
  {
    return getChangeRateLong(portChange, name, PortCounterUnits.COUNTS);
  }

  /************************************************************
   * Method Name:
   *  getChangeRate
  **/
  /**
   * Calculates the rate of change of the named counter.  Divides by the
   * provided scale.
   *
   * @see     describe related java objects
   * @param   describe the parameters
   *
   * @return  describe the value returned
   * @param portChange
   * @param name
   * @return
   ***********************************************************/
  public static long getChangeRateLong(PFM_PortChange portChange, PFM_Port.PortCounterName name, PortCounterUnits scale)
  {
    return (long)getChangeRateDouble(portChange, name, scale);
  }

  /************************************************************
   * Method Name:
   *  getChangeRate
  **/
  /**
   * Calculates the rate of change of the named counter.  Divides by the
   * provided scale.
   *
   * @see     describe related java objects
   * @param   describe the parameters
   *
   * @return  describe the value returned
   * @param portChange
   * @param name
   * @return
   ***********************************************************/
  public static long getChangeRateLongOld(PFM_PortChange portChange, PFM_Port.PortCounterName name, PortCounterUnits scale)
  {
    /*** NOTE: BigInteger usage is expensive, minimize use as much as possible ***/
    if((portChange != null) && (portChange.getDelta_counter_ts() > 0) && (scale != null))
    {
      // this rate is the difference counter divided by the timestamp divided by the scale
      
      // use BigIntegers, because divides can become problematic, but only where absolutely necessary
      BigInteger pc = null;
      if(name.getScale() == 1)
        pc = PFM_Port.convertUnsignedLongLongToBigInteger(portChange.getDelta_port_counter(name));
      else
        pc = PFM_Port.convertUnsignedLongLongToBigInteger(portChange.getDelta_port_counter(name) * name.getScale());

      // only use BigIntegers when absolutely necessary
      long rntVal = 0L;
      if(scale.getValue() == 1)
        rntVal = pc.divide(BigInteger.valueOf(portChange.getDelta_counter_ts())).longValue();
      else
        rntVal = pc.divide(BigInteger.valueOf(portChange.getDelta_counter_ts() * scale.getValue())).longValue();
      
      return rntVal;
    }
    return 0L;
  }

  /************************************************************
   * Method Name:
   *  getChangeRate
  **/
  /**
   * Calculates the rate of change of the named counter.  Divides by the
   * provided scale.
   *
   * @see     describe related java objects
   * @param   describe the parameters
   *
   * @return  describe the value returned
   * @param portChange
   * @param name
   * @return
   ***********************************************************/
  private static double getChangeRateDouble(PFM_PortChange portChange, PFM_Port.PortCounterName name, PortCounterUnits scale)
  {
    /*** NOTE: BigInteger usage is expensive, minimize use as much as possible ***/
    if((portChange != null) && (portChange.getDelta_counter_ts() > 0) && (scale != null) && (portChange.hasChange()))
    {
      // this rate is the difference counter divided by the timestamp divided by the scale
      
      // use BigIntegers, because divides can become problematic, but only where absolutely necessary
      BigInteger pc = PFM_Port.convertUnsignedLongLongToBigInteger(portChange.getDelta_port_counter(name));
      if(name.getScale() != 1)
        pc = pc.multiply(BigInteger.valueOf(name.getScale()));

      // only use BigIntegers when absolutely necessary
      double rntVal = 0;
      double numer = Double.parseDouble(pc.toString());
      double denom = (double)portChange.getDelta_counter_ts();

      if(scale.getValue() != 1)
        denom = (double)(portChange.getDelta_counter_ts() * scale.getValue());
      rntVal = numer/denom;
      
      return rntVal;
    }
    return 0;
  }

  /************************************************************
   * Method Name:
   *  getTransmitRate
  **/
  /**
   * Calculates the data transfer rate of the "xmit_data"
   *
   * @see     describe related java objects
   * @param n1
   * @param n2
   * @return
   ***********************************************************/
  public static long getTransmitRateMBs(PFM_PortChange portChange)
  {
    return PFM_PortRate.getChangeRateLong(portChange, PFM_Port.PortCounterName.xmit_data, PortCounterUnits.MEGABYTES);
  }

  /************************************************************
   * Method Name:
   *  getReceiveRate
  **/
  /**
   * Calculates the data transfer rate of the "xmit_data"
   *
   * @see     describe related java objects
   * @param n1
   * @param n2
   * @return
   ***********************************************************/
  public static long getReceiveRateMBs(PFM_PortChange portChange)
  {
    // this is the number of octets divided by 4 (bytes/4) so multiply to normalize
    return PFM_PortRate.getChangeRateLong(portChange, PFM_Port.PortCounterName.rcv_data, PortCounterUnits.MEGABYTES);
  }

  public static String toTransmitRateMBString(PFM_PortChange portChange)
  {
    return String.format("%6d", PFM_PortRate.getTransmitRateMBs(portChange));
  }
  
  public static String toReceiveRateMBString(PFM_PortChange portChange)
  {
    return String.format("%6d", PFM_PortRate.getReceiveRateMBs(portChange));
  }
  
 /************************************************************
   * Method Name:
   *  toVerboseDiagnosticString
  **/
  /**
   * Return a string that shows everything about xmit and rcv data
   *
   * @see java.lang.Object#toString()
  
   * @param   describe the parameters
   *
   * @return
   ***********************************************************/
  
  public static String toVerboseDiagnosticString(PFM_PortChange portChange)
  {
    StringBuffer sbuff = new StringBuffer();
    
    if(portChange == null)
      return "The PFM_PortChange object was null";
    
    sbuff.append("\n Receive  Rate = " + PFM_PortRate.getReceiveRateMBs(portChange) + " MB/sec");
    sbuff.append("\n Transmit Rate = " + PFM_PortRate.getTransmitRateMBs(portChange) + " MB/sec");
    sbuff.append("\n");

    return sbuff.toString();
  }

  public enum PortCounterUnits
  {
    PACKET_SIZE(  0,             1024L, "pkt", "units per packet"),    
    ONE(          1,                1L, "",   ""),    
    COUNTS(       2,                1L, "",   "counts"),    
    BYTES(        3,                1L, "B",  "bytes"),    
    KILOBYTES(    4,             1024L, "KB", "kilobytes"),    
    MEGABYTES(    5,          1048576L, "MB", "megabytes"),    
    GIGABYTES(    6,       1073741824L, "GB", "gigabytes"),    
    TERABYTE(     7,    1099511627776L, "TB", "terabytes"),    
    PETABYTE(     8, 1125899906842624L, "PB", "petabytes");   

    public static final EnumSet<PortCounterUnits> PFM_ALL_COUNTER_UNITS = EnumSet.allOf(PortCounterUnits.class);
    
    private static final Map<Integer,PortCounterUnits> lookup = new HashMap<Integer,PortCounterUnits>();

    static 
    {
      for(PortCounterUnits s : PFM_ALL_COUNTER_UNITS)
           lookup.put(s.getIndex(), s);
    }
    
    // the enum index
    private int Index;
    
    // the scaling value of the unit
    private long Value;
    
    // the short name of the counter unit
    private String Name;
    
    // a description or a long name of the counter unit
    private String Description;
    
    private PortCounterUnits(int index, long value, String name, String description)
    {
      Index = index;
      Value = value;
      Name = name;
      Description = description;
    }
    
    public static PortCounterUnits getByName(String name)
    {
      PortCounterUnits t = null;
      
      // return the first property with an exact name match
      for(PortCounterUnits s : PFM_ALL_COUNTER_UNITS)
      {
        if(s.getName().equals(name))
          return s;
      }
      return t;
    }

    public static PortCounterUnits getByIndex(int index)
    {
      PortCounterUnits t = null;
      
      // return the first property with an exact name match
      for(PortCounterUnits s : PFM_ALL_COUNTER_UNITS)
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


    public long getValue()
    {
      return Value;
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

}
