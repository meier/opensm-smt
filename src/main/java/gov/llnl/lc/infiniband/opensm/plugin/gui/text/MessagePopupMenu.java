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
 *        file: MessagePopupMenu.java
 *
 *  Created on: Oct 31, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.text;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import gov.llnl.lc.smt.event.SmtMessage;
import gov.llnl.lc.smt.event.SmtMessageType;
import gov.llnl.lc.smt.manager.MessageManager;

/**********************************************************************
 * Describe purpose and responsibility of MessagePopupMenu
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Oct 31, 2013 9:52:24 AM
 **********************************************************************/
public class MessagePopupMenu extends JPopupMenu implements ActionListener
{
  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = -1765491444730530138L;
  
  static final String MsgFlush  = "Flush";
  static final String TopBottom = "Most recent at top";
  static final String IncludeTS = "Include timestamp";
  static final String IncludeType = "Include message type";
  JMenuItem FlushItem;
  JCheckBoxMenuItem TopToBottomCB;
  JCheckBoxMenuItem TimeStampCB;
  private JCheckBoxMenuItem MessageTypeCB;
  
  public MessagePopupMenu()
  {
      
      TopToBottomCB = new JCheckBoxMenuItem(TopBottom);
      TopToBottomCB.setSelected(true);
      add(TopToBottomCB);
      
      TimeStampCB = new JCheckBoxMenuItem(IncludeTS);
      TimeStampCB.setSelected(true);
      add(TimeStampCB);
      
      MessageTypeCB = new JCheckBoxMenuItem(IncludeType);
      MessageTypeCB.setSelected(true);
      add(MessageTypeCB);
      
      JSeparator separator = new JSeparator();
      add(separator);
      FlushItem = new JMenuItem(MsgFlush);
      add(FlushItem);
      
      
      TopToBottomCB.addActionListener(this);
      TimeStampCB.addActionListener(this);
      MessageTypeCB.addActionListener(this);
      FlushItem.addActionListener(this);
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
    if(e.getActionCommand().equals(TopBottom))
    {
      MessageManager.getInstance().setRecentMsgOnTop(TopToBottomCB.isSelected());
      MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INFO, "Changing Top to Bottom order (top first=" + TopToBottomCB.isSelected() + ")"));
    }
    else if(e.getActionCommand().equals(IncludeTS))
    {
      MessageManager.getInstance().includeTimeStamps(TimeStampCB.isSelected());
      MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INFO, "Changing Timestamps to: " + TimeStampCB.isSelected()));
    }
    else if(e.getActionCommand().equals(IncludeType))
    {
      MessageManager.getInstance().includeMsgType(MessageTypeCB.isSelected());
      MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INFO, "Changing Message Types to: " + MessageTypeCB.isSelected()));
    }
    else if(e.getActionCommand().equals(MsgFlush))
    {
      MessageManager.getInstance().flushMessages();
      MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INFO, "flushed!"));
    }
  }

}
