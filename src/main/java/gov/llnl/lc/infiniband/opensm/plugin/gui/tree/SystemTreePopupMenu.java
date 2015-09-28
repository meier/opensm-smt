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
 *        file: SystemTreePopupMenu.java
 *
 *  Created on: Sep 31, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.tree;

import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionEvent;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.event.SmtMessage;
import gov.llnl.lc.smt.event.SmtMessageType;
import gov.llnl.lc.smt.manager.GraphSelectionManager;
import gov.llnl.lc.smt.manager.MessageManager;
import gov.llnl.lc.smt.manager.SMT_AnalysisType;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;

/**********************************************************************
 * Describe purpose and responsibility of SystemTreePopupMenu
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Oct 31, 2013 9:52:24 AM
 **********************************************************************/
public class SystemTreePopupMenu extends JPopupMenu implements ActionListener, CommonLogger
{
  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = 3837174107559442026L;
  
  private SystemTreeModel Model;
  
//  static final String MsgUtilize = "UTILIZE";
//  static final String MsgHeatMap = "HeatMap";
  
static final String MsgUtilize = SMT_AnalysisType.SMT_UTILIZATION.getAnalysisName();
static final String MsgHeatMap = SMT_AnalysisType.SMT_HEAT_MAP.getAnalysisName();
  JMenuItem anItem;
  JMenuItem hmItem;
  
  public SystemTreePopupMenu()
  {
    anItem = new JMenuItem(MsgUtilize);
    add(anItem);
    anItem.addActionListener(this);
    
    hmItem = new JMenuItem(MsgHeatMap);
    add(hmItem);
    hmItem.addActionListener(this);
    
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

  @Override
  public void actionPerformed(ActionEvent e)
  {
    if (e.getActionCommand().equals(MsgHeatMap))
    {
      // send the array list of vertices for this system guid
      if (e.getSource() instanceof JMenuItem)
      {
        JMenuItem src = (JMenuItem) e.getSource();
        Container cont = src.getParent();
        if (cont instanceof SystemTreePopupMenu)
        {
          SystemTreePopupMenu ptpm = (SystemTreePopupMenu) cont;
          Component comp = ptpm.getInvoker();
          if (comp instanceof JTree)
          {
            JTree tree = (JTree) comp;

            // I think the Model is of type, SystemTreeModel, check for that
            if (tree.getModel() instanceof DefaultTreeModel)
            {
              DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel();

              if (dtm.getRoot() instanceof UserObjectTreeNode)
              {
                UserObjectTreeNode tn = (UserObjectTreeNode) dtm.getRoot();
                String sysGuid = SystemTreeModel.getSystemGuidString(tn);
//                System.err.println("The system guid is: (" + sysGuid + ")");
//                String sysName = SystemTreeModel.getSystemNameString(tn);
//                System.err.println("The system name is: (" + sysName + ")");
                if(Model != null)
                {
                  MessageManager.getInstance().postMessage(
                      new SmtMessage(SmtMessageType.SMT_MSG_INFO, "Popup a panel for showing the System Guid Heat Map"));
                  
                  // craft a selection event that contains necessary info for this utilization event
                  GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(this, SMT_AnalysisType.SMT_HMAP_SYS_PORTS, Model));
               }

               }
            }
          }
        }
      }

    }
    
    if (e.getActionCommand().equals(MsgUtilize))
    {
      if (e.getSource() instanceof JMenuItem)
      {
        System.err.println("Selected utilize");
        JMenuItem src = (JMenuItem) e.getSource();
        Container cont = src.getParent();
        if (cont instanceof SystemTreePopupMenu)
        {
          SystemTreePopupMenu ptpm = (SystemTreePopupMenu) cont;
          Component comp = ptpm.getInvoker();
          if (comp instanceof JTree)
          {
            JTree tree = (JTree) comp;

            // I think the Model is of type, SystemTreeModel, check for that
            if (tree.getModel() instanceof DefaultTreeModel)
            {
              DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel();

              if (dtm.getRoot() instanceof UserObjectTreeNode)
              {
                UserObjectTreeNode tn = (UserObjectTreeNode) dtm.getRoot();
                String sysGuid = SystemTreeModel.getSystemGuidString(tn);
//                System.err.println("The system guid is: (" + sysGuid + ")");
//                String sysName = SystemTreeModel.getSystemNameString(tn);
//                System.err.println("The system name is: (" + sysName + ")");
                if(Model != null)
                {
                  // provide a "utilization" analysis for this core system switch assembly
                  
//                MessageManager.getInstance().postMessage(
//                new SmtMessage(SmtMessageType.SMT_MSG_INFO,
//                    "Popup a panel for showing the System Utilization (" + SystemTreeModel.getName(tn)
//                        + ") [" + SystemTreeModel.getSystemGuidString(tn) + "]"));
//            
//            // craft a selection event that contains necessary info for this utilization event
//            GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(this, SMT_AnalysisType.SMT_PORT_UTILIZATION, PortTreeModel.getPortAddressString(tn)));
               }

               }
            }
          }
        }
      }
    }
  }

  /************************************************************
   * Method Name:
   *  setSystemTreeModel
  **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   * @param model
   ***********************************************************/
  public void setSystemTreeModel(SystemTreeModel model)
  {
    Model = model;
    
  }

}
