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
 *        file: MessageListenerPanel.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.text;

import gov.llnl.lc.smt.event.SmtMessage;
import gov.llnl.lc.smt.event.SmtMessageListener;
import gov.llnl.lc.smt.manager.MessageManager;
import gov.llnl.lc.smt.manager.SmtMessageUpdater;

import java.awt.BorderLayout;

import javax.swing.JEditorPane;
import javax.swing.JPanel;

public class MessageListenerPanel extends JPanel implements SmtMessageListener
{
  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = 7730574837142859312L;

  private JEditorPane editorPane;
  
  /** TopDown is true, if you want to start at the top, and work your way down, or
   * in other words, the most recent message is last or at the bottom **/
  private boolean TopDown = false;

  private boolean HTML = false;

  /************************************************************
   * Method Name:
   *  isTopDown
   **/
  /**
   * Returns the value of topDown
   *
   * @return the topDown
   *
   ***********************************************************/
  
  public boolean isTopDown()
  {
    return TopDown;
  }

  /************************************************************
   * Method Name:
   *  setTopDown
   **/
  /**
   * Sets the value of topDown
   *
   * @param topDown the topDown to set
   *
   ***********************************************************/
  public void setTopDown(boolean topDown)
  {
    TopDown = topDown;
  }

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

  /**
   * Create the panel.
   */
  public MessageListenerPanel()
  {
    this(true, true);
  }
  
  public MessageListenerPanel(boolean useHTML, boolean topDown)
  {
    setLayout(new BorderLayout(0, 0));
    
    editorPane = new JEditorPane();
    editorPane.setEditable(false);
    setHTML(useHTML);
    setTopDown(topDown);
    add(editorPane, BorderLayout.CENTER);
    
   // message context menu, sends events to the message manager
    editorPane.setComponentPopupMenu(new MessagePopupMenu());
    
    // by definition, this is a listener (from the message manager)
    MessageManager.getInstance().addMessageListener(this);
  }
  

  @Override
  public void messageUpdate(SmtMessageUpdater msgMgr, SmtMessage msg)
  {
    // check the MsgManager for settings changes, then repaint all
    boolean timeStamped  = MessageManager.getInstance().isTimeStampsIncluded();
    boolean typeIncluded = MessageManager.getInstance().isMsgTypeIncluded();
    boolean topToBottom  = MessageManager.getInstance().isRecentMsgOnTop();
    // the message manager holds the messages, and distributes them to all listeners
    if(isHTML())
     editorPane.setText(SmtMessage.getContent(msgMgr.getMessages(), timeStamped, typeIncluded, topToBottom));
    else
      editorPane.setText(SmtMessage.toStringList(msgMgr.getMessages(), timeStamped, typeIncluded, topToBottom));
  }

}
