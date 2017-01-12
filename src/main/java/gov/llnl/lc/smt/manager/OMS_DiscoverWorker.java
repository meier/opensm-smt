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
 *        file: OMS_DiscoverWorker.java
 *
 *  Created on: Mar 20, 2014
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.manager;

import javax.swing.SwingWorker;

import gov.llnl.lc.infiniband.opensm.plugin.gui.text.FabricIdentificationPanel;
import gov.llnl.lc.infiniband.opensm.plugin.gui.tree.FabricDiscoveryPanel;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.event.SmtMessage;
import gov.llnl.lc.smt.event.SmtMessageType;

/**********************************************************************
 * Describe purpose and responsibility of OMS_DiscoverWorker
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Mar 20, 2014 11:19:52 AM
 **********************************************************************/
public class OMS_DiscoverWorker extends SwingWorker<Void, Void> implements CommonLogger
{
  int StartPort;
  int NumPorts;
  FabricDiscoveryPanel DiscoveryPanel = null;
  boolean found = false;

  /************************************************************
   * Method Name:
   *  OMS_DiscoverWorker
  **/
  /**
   * Describe the constructor here
   *
   * @see     describe related java objects
   *
   * @param startPort
   * @param numPorts
   ***********************************************************/
  public OMS_DiscoverWorker(int startPort, int numPorts, FabricDiscoveryPanel fd)
  {
    super();
    StartPort = startPort;
    NumPorts = numPorts;
    DiscoveryPanel = fd;
  }

  public OMS_DiscoverWorker()
  {
    this(10011, 7, new FabricDiscoveryPanel());
  }

  /************************************************************
   * Method Name:
   *  getDiscoveryPanel
   **/
  /**
   * Returns the value of discoveryPanel
   *
   * @return the discoveryPanel
   *
   ***********************************************************/
  
  public FabricDiscoveryPanel getDiscoveryPanel()
  {
    return DiscoveryPanel;
  }

  @Override
  protected Void doInBackground() throws Exception
  {
    // this is a SwingWorker thread from its pool, give it a recognizable name
    Thread.currentThread().setName("OMS_DiscoverWorker");

    for(int p = 0; p < NumPorts; p++)
    {
      String portNum = Integer.toString(StartPort + p);
      MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INFO, "Searching for OMS Service on port " + portNum));
      try
      {
        FabricIdentificationPanel fp = new FabricIdentificationPanel();
        fp.init("localhost", portNum);
        if(fp != null)
        {
          DiscoveryPanel.add(fp);
          DiscoveryPanel.updateBorderTitle();
          DiscoveryPanel.repaint();
          found = true;
          MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INFO, "Found OMS Service on port " + portNum));
       }
      }
      catch (Exception e)
      {
        MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_WARNING, "Could not find OMS Service on port " + portNum));
        logger.warning("Could not find an OMS Service on port " + portNum);
      }
    }
 
   return null;
  }
  
  @Override
  public void done()
  {
    // completion notification
    logger.info("Done Searching for OMS Services");
   }
  
}

