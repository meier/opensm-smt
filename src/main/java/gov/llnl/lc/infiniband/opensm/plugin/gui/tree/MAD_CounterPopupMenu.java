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
 *        file: MAD_CounterPopupMenu.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.tree;

import gov.llnl.lc.infiniband.opensm.plugin.data.MAD_Counter;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Stats;
import gov.llnl.lc.infiniband.opensm.plugin.gui.chart.XY_PlotFrame;
import gov.llnl.lc.infiniband.opensm.plugin.gui.chart.XY_PlotType;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.event.SmtMessage;
import gov.llnl.lc.smt.event.SmtMessageType;
import gov.llnl.lc.smt.manager.MessageManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import org.jfree.ui.RefineryUtilities;

public class MAD_CounterPopupMenu extends JPopupMenu implements ActionListener, CommonLogger
{
  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = -5709219934608238589L;
  private JLabel CounterNameLabel = new JLabel("counter name");
  private final JSeparator separator = new JSeparator();
  
  
  private SubnetTreeModel Smodel;
  private OSM_Stats MAD_Stats;
  private MAD_Counter MadCounter;
  private final JMenuItem Simple = new JMenuItem(XY_PlotType.MAD_COUNTER.getMenuLabel());
  private final JMenuItem Advanced = new JMenuItem(XY_PlotType.ADV_MAD_COUNTER.getMenuLabel());
  
  private int HistorySize = 0;

  /************************************************************
   * Method Name:
   *  PortCounterPopupMenu
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   ***********************************************************/
  public MAD_CounterPopupMenu()
  {
    super();
    setLabel("");
    CounterNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
    
    add(CounterNameLabel);
    
    add(separator);
    add(Simple);
    add(Advanced);
   
    // refer to the actionPerformed method for JMenuItems
    Simple.addActionListener(this);
    Advanced.addActionListener(this);
  }

  /************************************************************
   * Method Name:
   *  PortCounterPopupMenu
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param label
   ***********************************************************/
  public MAD_CounterPopupMenu(String label)
  {
    this();
    this.setCounterName(label);
  }
  
  public MAD_CounterPopupMenu(SubnetTreeModel Model, OSM_Stats stats, MAD_Counter mcn, int historySize)
  {
    this(mcn.getName());
    this.Smodel = Model;
    this.MadCounter = mcn;
    this.MAD_Stats = stats;
    this.HistorySize = historySize;
  }
  
   public void setCounterName(String name)
  {
    CounterNameLabel.setText(name);
  }
 
  @Override
  public void actionPerformed(ActionEvent e)
  {
    if(HistorySize < 2)
    {
      String msg = "Historical collection of MAD Statistics unavailable, cannot create a plot.  Wait for more snapshots.";
      logger.severe(msg);
      MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_SEVERE, msg));
      return;
    }
    
    if((Smodel != null) && (MAD_Stats != null) && (HistorySize > 0))
    {
      String sourceText = ((JMenuItem)e.getSource()).getText();
      
      // get the plot type based on the Text
      XY_PlotType type = XY_PlotType.getPlotTypeFromMenuLabel(false, sourceText);
      
      JFrame plot = new XY_PlotFrame(type, MAD_Stats, MadCounter);
      
      if(plot != null)
      {
        plot.pack();
        RefineryUtilities.centerFrameOnScreen(plot);
        plot.setVisible(true);
      }
    }
  }
}
