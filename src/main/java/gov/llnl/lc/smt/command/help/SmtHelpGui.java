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
 *        file: SmtHelpGui.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command.help;

import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.prefs.SmtGuiPreferences;
import gov.llnl.lc.smt.swing.SmtGuiApplication;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.JHelp;
import javax.swing.JFrame;

/**********************************************************************
 * Describe purpose and responsibility of SmtHelp
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Oct 22, 2014 9:47:32 AM
 **********************************************************************/
public class SmtHelpGui extends JFrame implements CommonLogger
{
  private static final String HelpSetName = "main_en_US.hs";
  
   /************************************************************
   * Method Name:
   *  SmtHelpGui
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @throws HeadlessException
   ***********************************************************/
  public SmtHelpGui() throws HeadlessException
  {
    super();
    initialize();
  }

  // Main HelpSet & Broker
  private HelpSet mainHS = null;
  private HelpBroker mainHB;
  private JHelp jhelp;

  /**
   * Initialize the contents of the frame.
   * 
   * @wbp.parser.entryPoint
   */
  private void initialize()
  {
    this.setTitle("smt-help");
    
    Dimension maxD = SmtGuiApplication.getAveAvailableScreenDimension();
    
    // load persistent data here, this should come with good defaults if no prior value
    Rectangle bounds = SmtGuiPreferences.getHelpBounds();

    this.setBounds(bounds);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.addWindowListener( new WindowAdapter()
    {
      public void windowClosing(WindowEvent e)
      {
        logger.severe("Closing Gracefully");
        destroy("Done!");
      }
    });
    
    this.getContentPane().setLayout(new BorderLayout(0, 0));
        
    // try to initialize the help system
    try
    {
      ClassLoader cl = SmtHelpGui.class.getClassLoader();
      URL url = HelpSet.findHelpSet(cl, HelpSetName);
      mainHS = new HelpSet(cl, url);
      mainHB = mainHS.createHelpBroker();
    } 
    catch (Exception ee)
    {
      System.err.println ("Help Set "+HelpSetName+" not found");
      System.exit(-1);
    }
    catch (ExceptionInInitializerError ex)
    {
      System.err.println("initialization error:");
      ex.getException().printStackTrace();
    }

    jhelp = new JHelp(mainHS);
    this.getContentPane().add(jhelp);
    
  }
  
  private void savePersistentData()
  {
    // save the size and location of the application
    logger.info("saving persistent data");    

    Rectangle r = this.getBounds();
    SmtGuiPreferences.setHelpBounds(r);
  }
  
  /************************************************************
   * Method Name:
   *  destroy
  **/
  /**
   * Releases resources, and exists gracefully.  This is the normal
   * controlled shutdown mechanism, which basically is supposed to
   * be the counterpart to the initialize() and initializeFramework()
   * methods.
   *
   * @see     describe related java objects
   *
   * @param msg
   ***********************************************************/
  public void destroy(String msg)
  {
    logger.info("Terminating SmtHelp");
    savePersistentData();
    logger.info(msg);
    
    logger.severe("Ending now");
    System.err.println(msg);
    System.exit(0);
  }

}
