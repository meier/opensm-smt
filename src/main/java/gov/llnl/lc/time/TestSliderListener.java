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
 *        file: TestSliderListener.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.time;

import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.util.concurrent.TimeUnit;

import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;

public class TestSliderListener implements TimeSliderListener
{
  public TimeSliderPanel tsp;

  public TestSliderListener()
  {
    
    // create a panel, and a frame for it
    super();
    JFrame frame = new JFrame("Time Slider Listener");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    tsp = new TimeSliderPanel();
    
    // initialize everything for testing

    TimeStamp ts1 = new TimeStamp();
    TimeStamp ts2 = new TimeStamp();
    ts2.addHours(2);
    int numTimeStamps = 60;

    tsp.setTimeRange(ts1, ts2, numTimeStamps, 120, true);
    tsp.setWrap(true);
    tsp.setPlay(false);
    tsp.setMultiplier(45);
    
    
    // add myself as a listener, get all change types
    tsp.addListener(this);

    frame.setContentPane(tsp);
    frame.pack();
    frame.setVisible(true);

  }

  /************************************************************
   * Method Name: main
   **/
  /**
   * Describe the method here
   * 
   * @see describe related java objects
   * 
   * @param args
   ***********************************************************/
  public static void main(String[] args)
  {
    TestSliderListener tsl = new TestSliderListener();
    
    // initialize everything for testing

    TimeStamp ts1 = new TimeStamp();
    TimeStamp ts2 = new TimeStamp();
    ts2.addHours(3);
    int numTimeStamps = 1800;

    tsl.tsp.setTimeRange(ts1, ts2, numTimeStamps, 60, true);
    tsl.tsp.setWrap(true);
    tsl.tsp.setPlay(false);
    tsl.tsp.setMultiplier(360);
    

    // do some programmatic changes before manual tests
    try
    {
      TimeUnit.SECONDS.sleep(5);
      tsl.tsp.setPlay(true);
      TimeUnit.SECONDS.sleep(5);
      tsl.tsp.setPlay(false);
    }
    catch (InterruptedException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  public void stateChanged(ChangeEvent e)
  {
    // make less responsive, don't want so many change notices
    System.err.println("Test Time Slider Change");
    System.err.println("value: " + tsp.getSliderValue());
    // reset flag, start timer
    try
    {
      TimeUnit.MILLISECONDS.sleep(50);
    }
    catch (InterruptedException e1)
    {
      e1.printStackTrace();
    }
  }

  public void propertyChange(PropertyChangeEvent e)
  {
    // the multiplier value
    Object source = e.getSource();
    if (source instanceof JFormattedTextField)
    {
      System.err.println("TSL the multiplier changed");
      System.err.println("value: " + tsp.getMultiplier());
    }
  }

  // this is the pause/play button or the wrap checkbox
  // just get the state of both
  public void itemStateChanged(ItemEvent e)
  {
    Object source = e.getItemSelectable();

    // the play/pause button
    if ((source instanceof JToggleButton) && !(source instanceof JCheckBox))
      if (e.getStateChange() == ItemEvent.DESELECTED)
        System.err.println("TSL play button was deselected");
      else
        System.err.println("TSL play button was selected");

    // the wrap checkbox
    if (source instanceof JCheckBox)
      if (e.getStateChange() == ItemEvent.DESELECTED)
        System.err.println("TSL wrap button was deselected");
      else
        System.err.println("TSL wrap button was selected");
  }
}
