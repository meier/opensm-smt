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
 *        file: FabricPopupMenu.java
 *
 *  Created on: Oct 31, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.text;

import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionEvent;
import gov.llnl.lc.smt.event.SmtMessage;
import gov.llnl.lc.smt.event.SmtMessageType;
import gov.llnl.lc.smt.manager.GraphSelectionManager;
import gov.llnl.lc.smt.manager.MessageManager;
import gov.llnl.lc.smt.manager.SMT_AnalysisType;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public class FabricPopupMenu extends JPopupMenu implements ActionListener
{
//  static final String MsgUtilize = "Utilization";
  JMenuItem anItem;
  
  public FabricPopupMenu()
  {
      anItem = new JMenuItem(SMT_AnalysisType.SMT_UTILIZATION.getAnalysisName());
      add(anItem);
      anItem.addActionListener(this);
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
  
  
  public static String getFabricName(JEditorPane ePane)
  {
    // the editor pane contains the parsable document
    return ePane.getName();
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    if (e.getActionCommand().equals(SMT_AnalysisType.SMT_UTILIZATION.getAnalysisName()))
    {
      if (e.getSource() instanceof JMenuItem)
      {
        JMenuItem src = (JMenuItem) e.getSource();
        Container cont = src.getParent();
        if (cont instanceof FabricPopupMenu)
        {
          FabricPopupMenu ptpm = (FabricPopupMenu) cont;
          Component comp = ptpm.getInvoker();
          
          if (comp instanceof JEditorPane)
          {
            JEditorPane ePane = (JEditorPane) comp;

                MessageManager.getInstance().postMessage(
                    new SmtMessage(SmtMessageType.SMT_MSG_INFO,
                        "Popup a panel for showing the Fabric Utilization (" + getFabricName(ePane)+ ")"));
                
                // craft a selection event that contains necessary info for this utilizaiton event
                GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(this, SMT_AnalysisType.SMT_FABRIC_UTILIZATION, getFabricName(ePane)));

          }
        }
      }
    }
  }

}
