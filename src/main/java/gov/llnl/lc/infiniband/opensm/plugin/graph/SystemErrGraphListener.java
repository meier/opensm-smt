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
 *        file: SystemErrGraphListener.java
 *
 *  Created on: Aug 21, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.graph;

import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;

import gov.llnl.lc.infiniband.opensm.plugin.gui.tree.FabricTreePanel;
import gov.llnl.lc.smt.manager.SMT_AnalysisType;


/**********************************************************************
 * Describe purpose and responsibility of SystemErrGraphListener
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Aug 21, 2013 12:31:56 PM
 **********************************************************************/
public class SystemErrGraphListener implements IB_GraphSelectionListener
{

  /************************************************************
   * Method Name:
   *  valueChanged
   **/
  /**
   * Describe the method here
   *
   * @see gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionListener#valueChanged(gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionEvent)
   *
   * @param event
   ***********************************************************/

  @Override
  public void valueChanged(IB_GraphSelectionUpdater source, IB_GraphSelectionEvent event)
  {
    StringBuffer buff = new StringBuffer();
    buff.append("Selection Updater: " + source.getName() + "\n");
     buff.append("Selection Event from: " + event.getSource().getClass().getName() + "\n");
     
     if(event.getSource() instanceof FabricTreePanel)
       buff.append("This event came from a FabricTreePanel\n");

     if (event.getContextObject() instanceof SMT_AnalysisType)
     {
       // typically these events happen within a PopupMenu
       SMT_AnalysisType sType = (SMT_AnalysisType) event.getContextObject();
       
       buff.append("The Analysis name is: " + sType.getAnalysisName() + "\n");
      }
     
     if(event.getSource() instanceof JMenuItem)
     {
       buff.append("This event came from a JMenuItem\n");
       if(event.getSelectedObject() instanceof ActionEvent)
       {
         ActionEvent e = (ActionEvent)event.getSelectedObject();
         buff.append("The action command is: " + e.getActionCommand() + "\n");
       }
     }

     
    if(event.getSelectedObject() instanceof IB_Vertex)
    {
      IB_Vertex v = (IB_Vertex) event.getSelectedObject();
      
      buff.append(v.getName() + "; Level " + v.getDepth() + "; GUID=" + v.getGuid().toColonString() + "; Num Links: " + v.getEdgeMap().size());
     }
    System.err.println(buff.toString());

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
