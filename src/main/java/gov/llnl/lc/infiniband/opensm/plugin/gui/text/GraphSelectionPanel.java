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
 *        file: GraphSelectionPanel.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.text;

import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionEvent;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionListener;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionUpdater;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;
import gov.llnl.lc.infiniband.opensm.plugin.gui.tree.FabricTreePanel;
import gov.llnl.lc.infiniband.opensm.plugin.gui.tree.NameValueNode;
import gov.llnl.lc.infiniband.opensm.plugin.gui.tree.RT_PathTreeModel;
import gov.llnl.lc.infiniband.opensm.plugin.gui.tree.UserObjectTreeNode;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.SmtConstants;
import gov.llnl.lc.smt.manager.GraphSelectionManager;
import gov.llnl.lc.smt.manager.SMT_AnalysisType;
import gov.llnl.lc.smt.swing.JClosableTabbedPane;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.Iterator;

import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

public class GraphSelectionPanel extends JPanel implements IB_GraphSelectionListener, CommonLogger
{
  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = 963385255104603888L;
  
  private JEditorPane editorPane;
  
  /** A collection of Event Objects **/
  protected static java.util.LinkedList <IB_GraphSelectionEvent> EventList = new java.util.LinkedList<IB_GraphSelectionEvent>();

  private static IB_GraphSelectionEvent currentEvent;

  private boolean HTML = false;
  
 /************************************************************
   * Method Name:
   *  isHTML
   **/
  /**
   * Returns the value of hTML
   *
   * @return the hTML
   *
   ***********************************************************/
  
  public boolean isHTML()
  {
    return HTML;
  }

  /************************************************************
   * Method Name:
   *  setHTML
   **/
  /**
   * Sets the value of hTML
   *
   * @param hTML the hTML to set
   *
   ***********************************************************/
  public void setHTML(boolean hTML)
  {
    HTML = hTML;
    if(HTML)
    {
      editorPane.setContentType("text/html");
    }
    else
    {
      editorPane.setContentType("text/plain");
     }
  }

  public void flushEvents()
  {
    EventList.clear();
//    System.err.println("FLUSHING the events from the GraphSelectionPanel");
  }

  public void refreshEvents()
  {
//    System.err.println("REFRESHING the events from the GraphSelectionPanel");
  }

  public String getContent(java.util.LinkedList <IB_GraphSelectionEvent> EventList, boolean timeStamped, boolean topToBottom)
  {
    // return a string, properly formatted
    StringBuffer buff = new StringBuffer();
    if((EventList == null) || (EventList.isEmpty()))
    {
      buff.append("empty");
    }
    else
    {
      if(isHTML())
      {
        buff.append(SmtConstants.NO_FORMAT_START);
        buff.append(SmtConstants.MEDIUM_FONT);
      }
 
      // recent ones are added to the tail, therefore the first ones are the oldest
      // is this the order I want, or do I need to reverse it?
      
       // set Iterator as descending or not
      Iterator <IB_GraphSelectionEvent> x = topToBottom ? EventList.descendingIterator(): EventList.iterator();

      // print list with descending order
      while (x.hasNext())
      {
        IB_GraphSelectionEvent event = x.next();
        
        // build a string for this event
        buff.append(getEventText(GraphSelectionManager.getInstance(), event, timeStamped));
       }
      if(isHTML())
        buff.append(SmtConstants.NO_FORMAT_END);

    }
    return buff.toString();
  }

  /**
   * Create the panel.
   */
  public GraphSelectionPanel()
  {
    setLayout(new BorderLayout(0, 0));
    
    editorPane = new JEditorPane();
    editorPane.setEditable(false);
    
    // by default, this is a listener
    GraphSelectionManager.getInstance().addIB_GraphSelectionListener(this);
    add(editorPane);
    
    // message context menu, sends events to the message manager
    editorPane.setComponentPopupMenu(new GraphSelectionPopupMenu());
    
    setHTML(true);
  }
  
  static String getEventText(IB_GraphSelectionUpdater source, IB_GraphSelectionEvent event, boolean includeTimeStamp)
  {
    StringBuffer buff = new StringBuffer();
    
    if(includeTimeStamp)
      buff.append("=======================" + event.getEventTime() + "=======================\n");
    else
      buff.append("==================================================================\n");

    
    buff.append("Selection Updater   : " + source.getName() + " (# listeners: " + source.getNumListeners() + ")\n");
    buff.append("Selection Event from: " + event.getSource().getClass().getName() + "\n");
     
    if(event.getSource() instanceof JMenuItem)
    {
       if(event.getSelectedObject() instanceof ActionEvent)
      {
        ActionEvent e = (ActionEvent)event.getSelectedObject();
        buff.append("ActionCommand   : " + e.getActionCommand() + "\n");
      }
    }
    
    if((event.getSelectedObject() != null) && (event.getSelectedObject() instanceof String))
    {
      if("FLUSH".equalsIgnoreCase((String)event.getSelectedObject()))
        buff.append("ActionCommand   : " + "Flush Selection Events" + "\n");
      if("REFRESH".equalsIgnoreCase((String)event.getSelectedObject()))
        buff.append("ActionCommand   : " + "Refresh Selection Events" + "\n");
    }


    if(event.getSource() instanceof FabricTreePanel)
      buff.append("This event came from a FabricTreePanel\n");

     if(event.getSelectedObject() instanceof IB_Vertex)
     {
       IB_Vertex v = (IB_Vertex) event.getSelectedObject();
       
       buff.append(v.getName() + "; Level " + v.getDepth() + "; GUID=" + v.getGuid().toColonString() + "; Num Links: " + v.getEdgeMap().size()+ "\n");
     }
     
     if (event.getContextObject() instanceof SMT_AnalysisType)
     {
       // typically these events happen within a PopupMenu
       SMT_AnalysisType sType = (SMT_AnalysisType) event.getContextObject();
       
       buff.append("The SMT_Analysis name is: " + sType.getAnalysisName() + "\n");
      }
     
     if (event.getContextObject() instanceof RT_PathTreeModel)
     {
       if(event.getSelectedObject() instanceof JCheckBox)
       {
         JCheckBox cb = (JCheckBox) event.getSelectedObject();
         buff.append("Selection Event Context object is: " + event.getContextObject().getClass().getSimpleName() + "\n");
         buff.append("Selection is: " + cb.getText() + " (" + cb.isSelected() + ")\n");
       }
     }
     else if(event.getContextObject() != null)
     {
       buff.append("Selection Event Context object is: " + event.getContextObject().getClass().getSimpleName() + "\n");
     }
     
     if(event.getSelectedObject() instanceof UserObjectTreeNode)
     {
       UserObjectTreeNode tn = (UserObjectTreeNode) event.getSelectedObject();
       buff.append("A tree was selected! [" + tn.toString() + "]\n");
       buff.append("ChildCount [" + tn.getChildCount() + "]\n");
       
       NameValueNode vmn = (NameValueNode) tn.getUserObject();
       buff.append("The name of the object is: " + vmn.getMemberName() + "\n");
       Object obj = vmn.getMemberObject();
       // TODO, what kind of object is this, and what to do with it?
     }
     
     if(event.getSource() instanceof JClosableTabbedPane)
     {
       JClosableTabbedPane tp = (JClosableTabbedPane) event.getSource();
       Integer index = (Integer) event.getSelectedObject();
       if(index < tp.getComponentCount())
         buff.append("Closing tab: (" + (tp.getTitleAt(index)).trim() + ") at index: " + index + "\n");
      }
     
    return buff.toString();
  }

  @Override
  public void valueChanged(IB_GraphSelectionUpdater source, IB_GraphSelectionEvent event)
  {
    // the source will always be the global graph selection manager
    // the event.source will be the parent object where the event came from
    // the event.graphEvent is the object that was selected
    
    // keep a collection of events, with an arrival timestamp
    
    if((event.getSelectedObject() != null) && (event.getSelectedObject() instanceof String))
    {
      if("FLUSH".equalsIgnoreCase((String)event.getSelectedObject()))
        flushEvents();
    }
    StringBuffer buff = new StringBuffer();
    
    // check the MsgManager for settings changes, then repaint all
    currentEvent = event;
    EventList.add(currentEvent);
    
     buff.append(getContent(EventList, GraphSelectionManager.getInstance().isTimeStampsIncluded(), GraphSelectionManager.getInstance().isRecentMsgOnTop()));
    editorPane.setText(buff.toString());
  }

}
