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
 *        file: SmtSplashFrame.java
 *
 *  Created on: Sep 30, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.swing;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;

import gov.llnl.lc.infiniband.opensm.plugin.gui.data.SmtIconType;
import gov.llnl.lc.smt.event.SmtEvent;
import gov.llnl.lc.smt.event.SmtEventObject;
import gov.llnl.lc.smt.prefs.SmtGuiPreferences;

/**********************************************************************
 * Describe purpose and responsibility of SmtSplashFrame
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Sep 30, 2013 12:50:53 PM
 **********************************************************************/
public class SmtSplashFrame extends SmtFrame
{
  private String VersionString;
  private String TitleString;
  private SmtSplashPanel SplashPanel;
  
  /** The application or frame that contains me, and listening for my shutdown event **/
  private java.awt.event.ActionListener ParentObject;


  /************************************************************
   * Method Name:
   *  smtUpdate
   **/
  /**
   * Describe the method here
   *
   * @see gov.llnl.lc.smt.swing.SmtFrame#smtUpdate(gov.llnl.lc.smt.event.SmtEvent, gov.llnl.lc.smt.event.SmtEventObject)
   *
   * @param sEvent
   * @param sEventObject
   ***********************************************************/

  @Override
  public void smtUpdate(SmtEvent sEvent, SmtEventObject sEventObject)
  {
    // a special event comming from the main application will tell
    // this splash screen to close
    if(false)
      // simulate an button action event, so I can ask my parent to shut me down
      notifyParentToClose();

  }

  /************************************************************
   * Method Name:
   *  actionPerformed
   **/
  /**
   * Describe the method here
   *
   * @see gov.llnl.lc.smt.swing.SmtFrame#actionPerformed(java.awt.event.ActionEvent)
   *
   * @param e
   ***********************************************************/

  @Override
  public void actionPerformed(ActionEvent e)
  {
    // The Splash Panel was clicked, so kill it off, and perform original
    // behavior, which is to wait for the main application frame to appear.
    logger.severe("Button Pushed");
    notifyParentToClose();
  }

  public SmtSplashFrame()
  {
    this("SplashTitle", "1.0");
  }

  public SmtSplashFrame(String titleString, String versionString)
  {
    super();
    VersionString = versionString;
    TitleString = titleString;
    
    initComponents();
  }
  
  private void initComponents()
  {
    setTitle(TitleString);
    setName(TitleString);
    setIconImage(SmtIconType.SMT_FABRIC_ICON.getIcon().getImage());

    
    SplashPanel = new SmtSplashPanel((java.awt.event.ActionListener)this);
    SplashPanel.setTitle(TitleString);
    SplashPanel.setVersion(VersionString);
    
    // get persistent location, should have good default if not yet set
    Rectangle r = SmtGuiPreferences.getBounds();
    setLocation(r.getLocation());

  
    // add this one initially
    getContentPane().add(SplashPanel, java.awt.BorderLayout.CENTER);
    pack();
    setVisible(true);

    addWindowListener(new java.awt.event.WindowAdapter()
    {
      public void windowClosing(java.awt.event.WindowEvent evt)
      {
        exitForm(evt);
      }
    });

  }
  
  /**
   * @param parentObject the parentObject to set
   */
  public void setParentObject(java.awt.event.ActionListener parentObject)
  {
    // if there is a ParentObject, let it close me on a close event
    ParentObject = parentObject;
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
  }

  private void exitForm(java.awt.event.WindowEvent evt)
  {
    // closing the SplashFrame?  Same as fabric update and clicking image
    //  - let the Parent shut me down gracefully
    logger.severe("Exit Pushed");
    notifyParentToClose();
  }
  
  private void notifyParentToClose()
  {
    logger.severe("Notify parent to close: " + Boolean.toString(ParentObject==null));
    if(ParentObject != null)
      ParentObject.actionPerformed(new ActionEvent(this, 0, "close"));
  }
   

  /**
   * @param args
   */
  public static void main(String[] args)
  {
    SmtSplashFrame sf = new SmtSplashFrame("application name", "version string");
  }
}
