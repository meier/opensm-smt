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
 *        file: TimeSliderPanel.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.time;

import gov.llnl.lc.logging.CommonLogger;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.concurrent.TimeUnit;

import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

public class TimeSliderPanel extends JPanel implements Runnable, CommonLogger
{
  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = -5541701390956802825L;
  
  private JFormattedTextField multiplier;
  private JCheckBox wrapRadio;
  private static JSlider slider;
  private static JLabel currentTimeSnapshotLabel;
  private JToggleButton playPauseToggle;
  
  private static TimeStamp initialTime;
  private static TimeStamp finalTime;
  
  private int elapsedSeconds;
  private boolean AutoUpdateTimeStamp;
  
  public static final int DEFAULT_MULTIPLIER =  20;
  public static final int MINIMUM_MULTIPLIER =   1;
  public static final int MAXIMUM_MULTIPLIER = 120;
  
  /** boolean specifying whether the thread should continue **/
  private volatile static boolean Continue_Thread = true;
  
  /** boolean specifying whether the thread has been created **/
  protected static boolean Thread_Exists = false;

  /** boolean specifying whether the thread is running **/
  protected static boolean Thread_Running = false;

  /** thread responsible for playing (moving) the slider **/
  private static java.lang.Thread SliderPlay_Thread;
    
  /** a list of Listeners, interested in knowing when slider activity occurs **/
  private static java.util.ArrayList <TimeSliderListener> Time_Listeners = new java.util.ArrayList<TimeSliderListener>();
  
   /**
   * Create the panel.
   */
  public TimeSliderPanel()
  {
    NumberFormat nDisplayFormat = NumberFormat.getIntegerInstance();
    NumberFormat nEditFormat    = NumberFormat.getIntegerInstance();
    nDisplayFormat.setMaximumIntegerDigits(3);
    nDisplayFormat.setMinimumIntegerDigits(2);
    nEditFormat.setMaximumIntegerDigits(3);
    nEditFormat.setMinimumIntegerDigits(2);
    
    DefaultFormatterFactory nFormat = new DefaultFormatterFactory(new NumberFormatter(nDisplayFormat), new NumberFormatter(nDisplayFormat), new NumberFormatter(nEditFormat));
    
    setLayout(new BorderLayout(0, 0));
    
    JPanel NPanel = new JPanel();
    add(NPanel, BorderLayout.NORTH);
    
    JPanel WPanel = new JPanel();
    add(WPanel, BorderLayout.WEST);
    
    JPanel SPanel = new JPanel();
    SPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
    add(SPanel, BorderLayout.SOUTH);
    SPanel.setLayout(new BorderLayout(0, 0));
    
    JPanel panel = new JPanel();
    SPanel.add(panel, BorderLayout.NORTH);
    panel.setLayout(new BorderLayout(0, 0));
    
    wrapRadio = new JCheckBox("Wrap");
    wrapRadio.setMnemonic('W');
    panel.add(wrapRadio, BorderLayout.EAST);
    
    JPanel SCPanel = new JPanel();
    panel.add(SCPanel, BorderLayout.CENTER);
    SCPanel.setLayout(new BorderLayout(0, 0));
    
    currentTimeSnapshotLabel = new JLabel("current / end time");
    currentTimeSnapshotLabel.setHorizontalAlignment(SwingConstants.CENTER);
    SCPanel.add(currentTimeSnapshotLabel, BorderLayout.CENTER);
    
    JPanel mxPanel = new JPanel();
    SCPanel.add(mxPanel, BorderLayout.EAST);
    
    multiplier = new JFormattedTextField(nFormat);
    
    // listens for the multiplier value to change
    multiplier.addPropertyChangeListener("value", new PropertyChangeListener()
    {
      public void propertyChange(PropertyChangeEvent e)
      {
        Object source = e.getSource();
        if(source == multiplier)
        {
          int val = getValidMultiplier();
          if(getMultiplier() != val)
           {
            setMultiplier(val);
          }
        }
      }
    });
    multiplier.setHorizontalAlignment(SwingConstants.CENTER);
    mxPanel.add(multiplier);
    multiplier.setToolTipText("valid multiplier range (" + MINIMUM_MULTIPLIER + "->" + MAXIMUM_MULTIPLIER + ")");
    
    setMultiplier(DEFAULT_MULTIPLIER);
    
    multiplier.setColumns(4);
    
    JLabel lblNewLabel = new JLabel("x Normal Speed");
    mxPanel.add(lblNewLabel);
    
    playPauseToggle = new JToggleButton("Play");
    playPauseToggle.setPreferredSize(new Dimension(78, 25));
    
    // listens for the button to be pushed (selected)
    playPauseToggle.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent e)
      {
        Object source = e.getItemSelectable();
        if(source == playPauseToggle)
          if(e.getStateChange() == ItemEvent.DESELECTED)
          {
            // The mode is now PAUSED, change the label and pause
             playPauseToggle.setText("Play");
             pauseThread();
          }
          else
          {
            // The mode is now PLAYING, so change the label and play
             playPauseToggle.setText("Pause");
            playThread();
          }
      }
    });
    playPauseToggle.setMnemonic('P');
    panel.add(playPauseToggle, BorderLayout.WEST);
    
    JPanel EPanel = new JPanel();
    add(EPanel, BorderLayout.EAST);
    
    JPanel CPanel = new JPanel();
    add(CPanel, BorderLayout.CENTER);
    CPanel.setLayout(new BorderLayout(0, 0));
    
    slider = new JSlider();
    slider.setToolTipText("drag or play through time");
    slider.setValue(0);   // this will cause an event (no one listening yet)
    slider.setPaintTicks(true);
    
    // listens for the slider value to change (drag, arrows, or by setSliderValue()
    slider.addChangeListener(new ChangeListener()
    {
      boolean readyForNextChange = true;
      long counter = 0L;
      long modCount = 3L;

      public void stateChanged(ChangeEvent e)
      {
        // make less responsive, don't want so many change notices
        counter++;
        if ((counter % modCount) == 0)
        {
          counter++;
          if (readyForNextChange)
          {
            readyForNextChange = false;

            if (!Thread_Running)
            {
              // only necessary if the play thread is not running, this handles
              // manual changes, someone dragging the slider

              // generate the new label based on where the slider is
              if(AutoUpdateTimeStamp)
              {
                TimeStamp ts = calculateSliderTime(slider.getValue(), elapsedSeconds);
                setCurrentTime(ts);
                repaint();
              }
            }

            try
            {
              TimeUnit.MILLISECONDS.sleep(50);
            }
            catch (InterruptedException e1)
            {
              e1.printStackTrace();
            }
//            repaint();
            readyForNextChange = true;
          }
        }

      }
    });    
    
    CPanel.add(slider);

    slider.setMaximum(3600);
    slider.setMinorTickSpacing(120);
    slider.setMajorTickSpacing(360);
    
    createThread();
  }
  
/**  
  * @wbp.parser.entryPoint
*/
  public static void main(String[] args)
  {
    JFrame frame = new JFrame("Time Slider Example");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    TimeSliderPanel tsp = new TimeSliderPanel();
    
    TimeStamp ts1 = new TimeStamp();
    TimeStamp ts2 = new TimeStamp();
    ts2.addHours(2);
    
    tsp.setTimeRange(ts1, ts2, 60, 120, true);
    tsp.setWrap(true);
    tsp.setMultiplier(45);
    tsp.setPlay(false);

    
    frame.setContentPane(tsp);
    frame.pack();
    frame.setVisible(true);
  }

  private int getValidMultiplier(int i)
  {
    i = i < MINIMUM_MULTIPLIER ? MINIMUM_MULTIPLIER: i;
    i = i > MAXIMUM_MULTIPLIER ? MAXIMUM_MULTIPLIER: i;
    return i;
  }

  private int getValidMultiplier()
  {
    return getValidMultiplier(getMultiplier());
  }

  public int setMultiplier(int i)
  {
    multiplier.setValue(getValidMultiplier(i));
    try
    {
      multiplier.commitEdit();
    }
    catch (ParseException e)
    {
      logger.severe("problem setting the multiplier");
    }
    return i;
  }

  public int getMultiplier()
  {
    if((multiplier == null) || (multiplier.getValue() == null))
      return DEFAULT_MULTIPLIER;
    return ((Number)multiplier.getValue()).intValue();
  }

  public int getSliderValue()
  {
    return slider.getValue();
  }

  public void setSliderValue(int value)
  {
    // this is going to generate a change event
    if(value != getSliderValue())
      slider.setValue(value);
  }

  public boolean isPlaying()
  {
    return playPauseToggle.isSelected();
  }

  public void setPlay(boolean b)
  {
    playPauseToggle.setSelected(b);
  }

  public void setWrap(boolean b)
  {
    wrapRadio.setSelected(b);
  }

  /************************************************************
   * Method Name:
   *  setTimeRange
  **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   * @param ts1  The initial or starting time
   * @param ts2  The final or ending time
   * @param numTimeStamps  The size of the time series
   * @param intervalSecs   The nominal* time between time stamps (secs)
   * @param autoUpdateTimeStamp  true, if you want the timestamp labels on the slider
   *                             automatically generated for you.  Good if the dataset is
   *                             rate-monotonic.  Otherwise set false, and update manually
   ***********************************************************/
  public void setTimeRange(TimeStamp ts1, TimeStamp ts2, int numTimeStamps, int intervalSecs, boolean autoUpdateTimeStamp)
  {
    // just during initialization, change the label
    AutoUpdateTimeStamp = autoUpdateTimeStamp;
    initialTime = ts1;
    finalTime   = ts2;
    // make a clone of the initial time
    setCurrentTime(initialTime);
    elapsedSeconds = intervalSecs;
    slider.setMaximum(numTimeStamps -1);
    int tic = numTimeStamps/30;
    tic = tic < 1 ? 1: tic;
    slider.setMinorTickSpacing(tic);
    tic = numTimeStamps/10;
    tic = tic < 1 ? 1: tic;
    slider.setMajorTickSpacing(tic);
  }

  public void resetTimeRange(TimeStamp cts, TimeStamp ts2, int numTimeStamps)
  {
    // typically used for growing the time, use the original slider settings, and extend the final time
    finalTime   = ts2;
    // make a clone of the initial time
    slider.setMaximum(numTimeStamps -1);
    int tic = numTimeStamps/30;
    tic = tic < 1 ? 1: tic;
    slider.setMinorTickSpacing(tic);
    tic = numTimeStamps/10;
    tic = tic < 1 ? 1: tic;
    slider.setMajorTickSpacing(tic);
    setCurrentTime(cts);
  }

  public void setCurrentTime(TimeStamp ct)
  {
    // clone this timestamp
    TimeStamp ts = new TimeStamp();
    ts.setTimeInSeconds(ct.getTimeInSeconds());
    setSliderTimeLabel(ts);
  }

  private void setSliderTimeLabel(TimeStamp ts)
  {
    currentTimeSnapshotLabel.setText("(" + ts.toString() + ") / (" + finalTime.toString()+ ")");
  }

  private TimeStamp calculateSliderTime(int index, int interval_secs)
  {
    TimeStamp ts = new TimeStamp();
    ts.setTimeInSeconds(initialTime.getTimeInSeconds());
    ts.addSeconds((int)interval_secs * index);
    return ts;
  }

   public synchronized void addListener(TimeSliderListener listener)
  {
    // add the listener, and its set of events
    if(listener != null)
    {
      Time_Listeners.add(listener);  // keep track
      
      slider.addChangeListener(listener);
      playPauseToggle.addItemListener(listener);
      multiplier.addPropertyChangeListener(listener);
      wrapRadio.addItemListener(listener);
    }
  }

  public synchronized boolean removeListener(TimeSliderListener listener)
  {
    if (Time_Listeners.remove(listener))
    {
      slider.removeChangeListener(listener);
      playPauseToggle.removeItemListener(listener);
      multiplier.removePropertyChangeListener(listener);
      wrapRadio.removeItemListener(listener);
     }
    return true;
  }
  
  private void createThread()
  {
    if (!Thread_Exists)
    {
      // set up the thread to listen
      SliderPlay_Thread = new Thread(this);
      SliderPlay_Thread.setDaemon(true);
      SliderPlay_Thread.setName("TimeSliderPlayer");
      
      logger.info("Creating the " + SliderPlay_Thread.getName() + " Thread");

      Thread_Exists = true;
      
      logger.info("Starting the " + SliderPlay_Thread.getName() + " Thread");
      SliderPlay_Thread.start();
    }
  }
  
  
  private boolean playThread()
  {
    logger.info("Playing the " + SliderPlay_Thread.getName() + " Thread");
     return Thread_Running = true;
  }
   private void stopThread()
  {
    Continue_Thread = false;
  }

  private boolean pauseThread()
  {
    logger.info("Pausing the " + SliderPlay_Thread.getName() + " Thread");
    return Thread_Running = false;
  }

  public void run()
  {
    // check the Thread Termination Flag, and continue if Okay
    while (Continue_Thread)
    {
      int max = slider.getMaximum();
      int timeDivision = 10; // break up the sleep interval, to check for
                             // Multiplier changes more responsively
      int loopCounter = 0;
      try
      {
        TimeUnit.MILLISECONDS.sleep(25);

        // this loop runs when the PLAY button is selected
        while (Thread_Running)
        {
          // the time between each snapshot during playback
          loopCounter = 0;
          while (loopCounter < timeDivision)
          {
            loopCounter++;
            long sleep_interval_ms = (1000L * elapsedSeconds) / (getMultiplier() * timeDivision);
            try
            {
              TimeUnit.MILLISECONDS.sleep(sleep_interval_ms);
            }
            catch (Exception e)
            {
              // nothing to do yet
            }
          }

          // the sliders range goes from 0 to max, move it accordingly
          // time has passed, increment the slider
          int val = slider.getValue() + 1;
          max = slider.getMaximum();

          // start over, or end?
          if (val > max)
          {
            if (wrapRadio.isSelected())
            {
              val = 0;
              setCurrentTime(initialTime);
            }
            else
            {
              // pause the thread - reached the end - no change event
              val = max;
              setPlay(false);
            }
          }
          slider.setValue(val); // this will generate a change event

          // generate the new label based on where the slider is
          if (AutoUpdateTimeStamp)
          {
            TimeStamp ts = calculateSliderTime(val, elapsedSeconds);
            setCurrentTime(ts);
          }

        }// end of the infinite PLAY loop

      }
      catch (Exception e)
      {
        // nothing to do yet
      }
    }
    /* fall through, must be done! */
  }
}
