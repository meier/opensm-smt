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
 *        file: SimpleGraphMouseListener.java
 *
 *  Created on: May 27, 2014
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.graph;

import java.awt.Dimension;
import java.awt.event.MouseEvent;

import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionEvent;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;
import gov.llnl.lc.smt.event.SmtMessage;
import gov.llnl.lc.smt.event.SmtMessageType;
import gov.llnl.lc.smt.manager.GraphSelectionManager;
import gov.llnl.lc.smt.manager.MessageManager;
import gov.llnl.lc.smt.manager.SMT_RouteManager;

/**********************************************************************
 * Describe purpose and responsibility of SimpleGraphMouseListener
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version May 27, 2014 1:47:28 PM
 **********************************************************************/
public class SimpleGraphMouseListener<V> implements GraphMouseListener<V>
{
  
  SimpleCollapsableGraph parent;
  
  // What is the difference between pressed, clicked, and released??
  
  // clicked is pressed and released, or simply a quick selection
  //
  // pressed is a down, which may or may not be followed by a release
  //   this is typically useful for multiple selections or popups
  //
  // release is the other half of pressed, and can be used to initiate the action
  //   defined by the press (delayed reaction)

//Button0 is my upper right button
//Button1 is left button, or selection
//Button2 is my upper left button
//Button3 is right button, or for popup


  public SimpleGraphMouseListener(SimpleCollapsableGraph simpleCollapsableGraph)
  {
    parent = simpleCollapsableGraph;
  }

  @Override
  public void graphClicked(V v, MouseEvent me)
  {
    // if this is a vertex and a right click, or selected, notify the selection manager
    if(v instanceof IB_Vertex)
    {
      // selected??
      if(me.getButton() == MouseEvent.BUTTON1)
      {
        // generate a selection message (handled elsewhere)
        if(me.getSource() instanceof VisualizationViewer)
        {
          if(!me.isShiftDown())
          // if the shift key is down, then allow multiple selection, but if just a single vertex, trigger a graph selection
           GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(this, me.getSource(), v));
        }
      }
    }
    else
    {
      // selected??
      if(me.getButton() == MouseEvent.BUTTON1)
      {
          MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INFO, "UNKNOWN (" + v.getClass().getCanonicalName() + ") was selected"));
       }
      
    }
  }

  @Override
  public void graphPressed(V v, MouseEvent me)
  {
    // press and release, which button am I reacting to?
    
    if((v instanceof IB_Vertex) && (me.getButton() == MouseEvent.BUTTON3))
    {
      // generate a selection message
      if(me.getSource() instanceof VisualizationViewer)
      {
        VisualizationViewer<VisualizationModel, Dimension> vv = (VisualizationViewer<VisualizationModel, Dimension>) me.getSource();
        IB_Vertex iv = (IB_Vertex)v;
        MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INFO, "Vertex (" + iv.toString() + ") PopUp pressed"));
//        System.err.println("SGM-P Popup for vertex " + v);
         
         
         // put a popup menu near the vertex
//        SimpleGraphPopupMenu pum = new SimpleGraphPopupMenu();
        SimpleGraphPopupMenu pum = SMT_RouteManager.getInstance().getSimpleGraphPopupMenu(iv);
         
         if(pum.getComponentCount() > 0) 
         {
//           pum.show(parent, me.getX(), me.getY());
           pum.setVertex(iv);
           pum.show(vv, me.getX(), me.getY());
           MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INFO, "Showing the SimpleGraphPopupMenu (from RouteManager)"));
//           System.err.println("The popup attempted to show");
         }
         else
         {
           MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_WARNING, "Could not show the SimpleGraphPopupMenu (empty?? No routes?)"));

 //          System.err.println("The popup count appears to be empty");
         }
       }
    }
    else if(me.getButton() == MouseEvent.BUTTON3)
    {
           MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INFO, "UNKNOWN (" + v.getClass().getCanonicalName() + ") PopUp"));
//          System.err.println("SGM-P Popup for edge " + v);
      }
  }

  @Override
  public void graphReleased(V v, MouseEvent me)
  {
//    MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INFO, "Vertex (" + ((IB_Vertex)v).toString() + ") PopUp release"));
//    System.err.println("SGM-R Popup for vertex " + v);
//  System.err.println("IB Vertex " + v + " was released at (" + me.getX() + "," + me.getY() + ")");
    
  }
  

}
