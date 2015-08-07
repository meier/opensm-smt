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
 *        file: SearchIdentificationPanel.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.text;

import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.logging.CommonLogger;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

public class SearchIdentificationPanel extends JPanel implements CommonLogger
{
  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = -6832292727026819931L;
  
  OpenSmMonitorService OMS = null;  // the current snapshot

  /**
   * Create the panel.
   */
  public SearchIdentificationPanel()
  {
    this(null, null, null);
  }
  
  public SearchIdentificationPanel(OpenSmMonitorService oms)
  {
    this(null, null, oms);
  }
  
  public SearchIdentificationPanel(String hostName, String portNum)
  {
    this(hostName, portNum, null);
  }
  
  private SearchIdentificationPanel(String hostName, String portNum, OpenSmMonitorService oms)
  {
    updateBorderTitle();
    setLayout(new GridLayout(0, 1, 0, 0));
    try
    {
      init(hostName, portNum, oms);
    }
    catch (IOException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  protected void updateBorderTitle()
  {
    // put the number of "discovered" fabrics in the border title.  Each component represents a table with results.
     setBorder(new TitledBorder(null, "Find...", TitledBorder.LEADING, TitledBorder.TOP, null, null));
   }

  protected void init(String hostName, String portNum, OpenSmMonitorService oms) throws IOException
  {
    if(oms != null)
      OMS = oms;
    else
      OMS = getOMS(hostName, portNum);
    
    SMT_SearchPanel ssp = new SMT_SearchPanel(OMS);
    JScrollPane sp = new JScrollPane(ssp);

    if (getComponentCount() > 0)
      removeAll();

    add(sp);
  }
  
  private OpenSmMonitorService getOMS(String hostName, String portNum)
  {
    // return an OMS or NULL
    try
    {
      return OpenSmMonitorService.getOpenSmMonitorService(hostName, portNum);
    }
    catch (IOException e)
    {
      // remain silent, I don't care
    }
    return null;
  }

  /**
   * @param args
   * @throws InterruptedException
   */
  public static void main(String[] args) throws Exception
  {
    SearchIdentificationPanel vttp = new SearchIdentificationPanel();

    JFrame f = new JFrame("Search IdentificationPanel Panel");
    f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    f.setMinimumSize(new Dimension(700, 400));
    f.setLocationRelativeTo(null);
    
    f.setContentPane(vttp);
    f.pack();
    f.setVisible(true);
    
    // set up environ vars, so keystore stuff will work

    TimeUnit.SECONDS.sleep(3);
    vttp.init("localhost", "10011", null);
    f.setVisible(true);

    TimeUnit.SECONDS.sleep(100);
    vttp.init("localhost", "10011", null);
    f.setVisible(true);
  }



}
