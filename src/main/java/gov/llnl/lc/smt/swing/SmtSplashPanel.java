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
 *        file: SmtSplashPanel.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.swing;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.event.SmtMessage;
import gov.llnl.lc.smt.event.SmtMessageListener;
import gov.llnl.lc.smt.event.SmtMessageType;
import gov.llnl.lc.smt.manager.MessageManager;
import gov.llnl.lc.smt.manager.SmtMessageUpdater;
import gov.llnl.lc.smt.props.SmtProperties;

public class SmtSplashPanel extends JPanel implements CommonLogger, SmtMessageListener
{
  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = -2035512649438056606L;

  /** The application or frame that contains me, and listening for my shutdown (button click) event **/
  private java.awt.event.ActionListener ParentObject;

  private JLabel lblVersion;
  private JLabel lblMessage;
  private String TitleString;
  private JButton SplashButton;
  private static ImageIcon SplashIcon;

  /**
   * Create the panel.
   */
  
  private void initComponents()
  {
    setLayout(new BorderLayout(0, 0));
    
    SplashButton = new JButton("");
    SplashButton.setIcon(SplashIcon);
    SplashButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        SplashButtonActionPerformed(evt);
      }
    });

    add(SplashButton);
    
    lblVersion = new JLabel("Put Verions Here");
    add(lblVersion, BorderLayout.NORTH);
    
    lblMessage = new JLabel("Put Messages Here");
    add(lblMessage, BorderLayout.SOUTH);

    // by definition, this is a listener
    MessageManager.getInstance().addMessageListener(this);
  }
  
  private void SplashButtonActionPerformed(java.awt.event.ActionEvent evt)
  {
  // tell my Parent object, I am ready to die
    ParentObject.actionPerformed(evt);
  }
  
  public SmtSplashPanel()
  {
    this(null);
  }
  /*-----------------------------------------------------------------------*/

  public SmtSplashPanel(java.awt.event.ActionListener parent)
  {
      String imgDir = new SmtProperties().getImageDir()+ "SplashSMT.png";
      SplashIcon    = new ImageIcon(SmtSplashPanel.class.getResource(imgDir));
      this.initComponents();
      this.init(parent);
  }

  private void init(java.awt.event.ActionListener parent)
  {
      this.ParentObject = parent;
  }
  /*-----------------------------------------------------------------------*/



  @Override
  public void messageUpdate(SmtMessageUpdater msgMgr, SmtMessage msg)
  {
    // only display messages intended for this label
    if(msg.getType() == SmtMessageType.SMT_MSG_INIT)
    {
      logger.info(msg.getMessage());
      setMessage(msg.getMessage());      
    }
  }

  public String getMessage() {
    return lblMessage.getText();
  }
  public void setMessage(String text) {
    lblMessage.setText(text);
  }
  public String getVersion() {
    return lblVersion.getText();
  }
  public void setVersion(String text_1) {
    lblVersion.setText(text_1);
  }
  public void setTitle(String text_1) {
    TitleString = text_1;
  }
}
