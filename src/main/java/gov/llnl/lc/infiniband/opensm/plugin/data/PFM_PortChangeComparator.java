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
 *        file: PFM_PortChangeComparator.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.data;

import java.util.Comparator;
import java.util.EnumSet;

import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_Port.PortCounterName;
import gov.llnl.lc.logging.CommonLogger;

public class PFM_PortChangeComparator implements Comparator<PFM_PortChange>, CommonLogger
{
  EnumSet<PFM_Port.PortCounterName> CountersToCompare;
  long counts = 0L;

  /************************************************************
   * Method Name:
   *  PFM_PortChangeComparator
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param name
   ***********************************************************/
  public PFM_PortChangeComparator(PortCounterName name)
  {
    super();
    this.CountersToCompare = EnumSet.of(name);
  }

  public PFM_PortChangeComparator(EnumSet<PFM_Port.PortCounterName> countersToCompare)
  {
    super();
    this.CountersToCompare = countersToCompare;
  }

  @Override
  public int compare(PFM_PortChange o1, PFM_PortChange o2)
  {
    // return 1, if NOT COMPARABLE
    // this will push the results to the top of the sort, and then by
    // reversing the sort, the bad results will be pushed to the bottom
    //
    // -1 if first is less than second
    //  1 if first is greater than second (or if not comparable)
    
    // not comparable
    if((o1 == null) || (o2 == null) || (CountersToCompare == null))
      return 1;
    
    // compare timestamps, should be same, if not return 1
    long tdiff = o1.getCounterTimeStamp().getTimeInSeconds() - o2.getCounterTimeStamp().getTimeInSeconds();
    
    if(tdiff != 0L)
    {
      return 1;
    }
    
    // all that remain should be comparable
    
    // compare max of all counter changes (FIXME use BigInteger ??)
    long maxDiff1 = 0L;
    long maxDiff2 = 0L;
    long count1   = 0L;
    long count2   = 0L;
    counts++;
    
    for (PortCounterName counter : CountersToCompare)
    {
//      if((counts % 30000) == 0)
//        logger.info("Comparing " + counter.name() + ", " + counts);
      count1 = o1.getDelta_port_counter(counter);
      count2 = o2.getDelta_port_counter(counter);
      maxDiff1 = maxDiff1 > count1 ? maxDiff1: count1;
      maxDiff2 = maxDiff2 > count2 ? maxDiff2: count2;
    }
    
    long diff = maxDiff1 - maxDiff2;
    if(diff == 0L)
      return 0;
    if(diff > 0L)
      return 1;
    return -1;
  }

}
