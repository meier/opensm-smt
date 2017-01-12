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
 *        file: RT_PathPopupMenu.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.tree;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import gov.llnl.lc.infiniband.opensm.plugin.data.RT_Path;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionEvent;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.event.SmtMessage;
import gov.llnl.lc.smt.event.SmtMessageType;
import gov.llnl.lc.smt.manager.GraphSelectionManager;
import gov.llnl.lc.smt.manager.MessageManager;
import gov.llnl.lc.smt.manager.SMT_AnalysisType;

public class RT_PathPopupMenu extends JPopupMenu implements ActionListener, CommonLogger
{
  static final String MsgUtilize = SMT_AnalysisType.SMT_ROUTE_UTILIZATION.getAnalysisName();
//  static final String MsgUtilize = "Path Utilization";

  private JLabel PathNameLabel = new JLabel("path name");
  private final JSeparator separator = new JSeparator();
  private RT_PathPopupMenu thisMenu = this;

  
  private RT_PathTreeModel Pmodel;
  private RT_Path Path;
  private final JMenuItem Swap = new JMenuItem("Swap Source and Destination");
  
  private int HistorySize = 0;
  private final JSeparator separator_1 = new JSeparator();
  private final JCheckBox pathTreeCB = new JCheckBox("Path Tree");
  private final JCheckBox pathUtilizationCB = new JCheckBox(MsgUtilize);
  private final JCheckBox HideNodesCB = new JCheckBox("Hide Nodes");

  /************************************************************
   * Method Name:
   *  getPath
   **/
  /**
   * Returns the value of path
   *
   * @return the path
   *
   ***********************************************************/
  
  public RT_Path getPath()
  {
    return Path;
  }

  /************************************************************
   * Method Name:
   *  setPath
   **/
  /**
   * Sets the value of path
   *
   * @param path the path to set
   *
   ***********************************************************/
  public void setPath(RT_Path path)
  {
    Path = path;
    setPathName(path.getPathIdString());
  }

  /************************************************************
   * Method Name:
   *  getSwap
   **/
  /**
   * Returns the value of swap
   *
   * @return the swap
   *
   ***********************************************************/
  
  public boolean getSwap()
  {
    return Swap.isSelected();
  }

  /************************************************************
   * Method Name:
   *  getPathTreeCB
   **/
  /**
   * Returns the value of pathTreeCB
   *
   * @return the pathTreeCB
   *
   ***********************************************************/
  
  public boolean getHideNodes()
  {
    return HideNodesCB.isSelected();
  }

  public void setHideNodes(boolean hide)
  {
    HideNodesCB.setSelected(hide);
  }

  public boolean getPathTree()
  {
    return pathTreeCB.isSelected();
  }

  public void setPathTree(boolean pathTree)
  {
    pathTreeCB.setSelected(pathTree);
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
   ***********************************************************/
  public RT_PathPopupMenu()
  {
    super();
    setLabel("");
    PathNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
    
    add(PathNameLabel);
    
    add(separator);
    add(Swap);
    
    add(separator_1);
    
    add(pathTreeCB);
    HideNodesCB.addActionListener(new ActionListener() 
    {
      /* tell the tree to fix the model to include/exclude the nodes
       * and then redraw.  Links will always be shown
       */
      public void actionPerformed(ActionEvent e)
      {
        GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(thisMenu, Pmodel, HideNodesCB));

      }
    });
    
    add(HideNodesCB);
    
    pathUtilizationCB.addActionListener(new ActionListener() 
    {
      public void actionPerformed(ActionEvent e) 
      {
        if(pathUtilizationCB.isSelected())
           MessageManager.getInstance().postMessage(
            new SmtMessage(SmtMessageType.SMT_MSG_INFO,
                "Popup a panel for showing the Path Utilization (" + Path.getPathIdString() + ") [" + Path.getRT_PathKey() + "]"));
        else
          MessageManager.getInstance().postMessage(
              new SmtMessage(SmtMessageType.SMT_MSG_INFO,
                  "Destroy the Path Utilization Panel, if up (" + Path.getPathIdString() + ") [" + Path.getRT_PathKey() + "]"));

        // craft a selection event that contains necessary info for this utilization event
        GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(thisMenu, SMT_AnalysisType.SMT_ROUTE_UTILIZATION, Path));
        
        // dismiss
        thisMenu.setVisible(false);
      }
    });
    
    add(pathUtilizationCB);
   
    // refer to the actionPerformed method for JMenuItems
//    Swap.addActionListener(this);
    Swap.addActionListener(new ActionListener() 
    {
      /* tell the tree to change the model by swapping source and destination.
       */
      public void actionPerformed(ActionEvent e)
      {
        GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(thisMenu, Pmodel, Swap));

      }
    });
    

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
  public RT_PathPopupMenu(String label)
  {
    this();
    this.setPathName(label);
  }
  
  public RT_PathPopupMenu(RT_PathTreeModel Model, RT_Path path)
  {
    this(path.getPathIdString());
    this.Pmodel = Model;
    this.Path = path;
    setHideNodes(!Model.includesNodes());
  }
  
  
   public void setPathName(String name)
  {
    PathNameLabel.setText(name);
  }
 
  @Override
  public void actionPerformed(ActionEvent e)
  {
    if(HistorySize < 2)
    {
      String msg = "Historical collection of OMS snapshots unavailable, cannot create a plot.  Wait for more snapshots.";
      logger.severe(msg);
      MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_SEVERE, msg));
      return;
    }
    
  }

  public void setModelAndPath(RT_PathTreeModel Model, RT_Path path)
  {
    this.Pmodel = Model;
    setPath(path);
  }

}
