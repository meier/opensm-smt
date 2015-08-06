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
 *        file: SmtGuiRunner.java
 *
 *  Created on: Oct 2, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.command.gui;

import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Updater;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.command.SmtCommand;
import gov.llnl.lc.smt.event.SmtMessage;
import gov.llnl.lc.smt.event.SmtMessageType;
import gov.llnl.lc.smt.manager.MessageManager;
import gov.llnl.lc.smt.swing.SmtGuiApplication;

import java.util.concurrent.TimeUnit;

/**********************************************************************
 * Describe purpose and responsibility of SmtGuiRunner
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Oct 2, 2013 8:15:09 AM
 **********************************************************************/
public class SmtGuiRunner implements Runnable, CommonLogger
{
  OMS_Updater UpdateService = null;
  OpenSmMonitorService OMS = null;
  SmtCommand Parent = null;

  /************************************************************
   * Method Name:
   *  SmtRunner
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param enableFramework
   ***********************************************************/
  public SmtGuiRunner(OMS_Updater updater, OpenSmMonitorService initialOMS, SmtCommand parent)
  {
    super();
    OMS = initialOMS;
    UpdateService = updater;
    Parent = parent;
  }

  @Override
  public void run()
  {
    logger.severe("Running from within SmtGuiRunner");
    // only start if the splash frame is still up, it could have stopped
    try
    {
      SmtGuiApplication app = new SmtGuiApplication();
      
      // if the splash closes, make sure the app closes too
      Parent.getSplash().setParentObject(app);

      if(OMS != null)
      {
        MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "initializing the application framework"));
        app.initializeFramework( OMS);
        MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "initializing the framework with the services"));
       app.initUpdateService(UpdateService);
       MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "done initializing the application"));
       // give everything a little time to finish up initialization
       //  - seems to help slower clients (laptops and such) make sure threads are done
       TimeUnit.MILLISECONDS.sleep(300);
      }
      else
        logger.severe("The initial OMS is null, so didn't create the application");
      
      // show the application, and remove the splash screen
      app.setVisible(true);
      if(Parent != null)
      {
        Parent.closeSplash();
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}
