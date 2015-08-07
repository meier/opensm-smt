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
 *        file: TimeListenerPanel.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.text;

import gov.llnl.lc.smt.SmtConstants;
import gov.llnl.lc.smt.event.SmtHeartBeat;
import gov.llnl.lc.smt.event.SmtHeartBeatListener;
import gov.llnl.lc.smt.manager.SmtHeartBeatUpdater;
import gov.llnl.lc.time.TimeListener;
import gov.llnl.lc.time.TimeStamp;

import java.awt.BorderLayout;

import javax.swing.JEditorPane;
import javax.swing.JPanel;

public class TimeListenerPanel extends JPanel implements TimeListener, SmtHeartBeatListener
{
  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = -1406797786795286594L;
  /**  describe serialVersionUID here **/
  
  private JEditorPane editorPane;
  private boolean HTML = false;
  private String header = "";
  private String footer = "";
  private String nl     = "\n";
  
  private TimeStamp HBTime = new TimeStamp();
  private int NumUpdates = 0;

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
      header = "<pre>";
      footer = "</pre>";
      nl     = "<br>";
    }
    else
    {
      editorPane.setContentType("text/plain");
      header = "";
      footer = "";
      nl     = "\n";
    }
  }

  /**
   * Create the panel.
   */
  public TimeListenerPanel()
  {
    setLayout(new BorderLayout(0, 0));
    
    editorPane = new JEditorPane();
    editorPane.setEditable(false);
    setHTML(true);
    add(editorPane);
  }

  public String toTimeString(TimeStamp time)
  {
    // just overwrite the new time on top of the old
    StringBuffer buff = new StringBuffer();

    buff.append(header);
  
  if(isHTML())
    buff.append(SmtConstants.MEDIUM_FONT);

  buff.append("Current Time : " + time.toString() + nl);
  buff.append(footer);
  return buff.toString();
  }

  public String toHeartBeatString(SmtHeartBeatUpdater hBeatMgr, SmtHeartBeat heartBeat)
  {
    // just overwrite the new time on top of the old
    StringBuffer buff = new StringBuffer();

    buff.append(header);
  
  if(isHTML())
    buff.append(SmtConstants.MEDIUM_FONT);
  
  int nO = heartBeat.getNumSecsToOMS();
  int nP = heartBeat.getNumSecsToPerfMgr();
  
  String nOstr = Integer.toString(nO);
  String nPstr = Integer.toString(nP);
  
  if((nO < 0) && (isHTML()))
    nOstr = SmtConstants.RED_FONT + nOstr + SmtConstants.END_FONT ;
  
  if((nP < 0) && (isHTML()))
    nPstr = SmtConstants.RED_FONT + nPstr + SmtConstants.END_FONT ;
  
  
  buff.append("Current Time       : " + heartBeat.getTime().toString() + nl);
  buff.append("Heartbeat up since : " + HBTime.getTime().toString() + nl);
  buff.append("# heartbeats       : " + NumUpdates + nl);
  buff.append("OMS update in      : " + nOstr + " secs" + nl);
  buff.append("Perf Mgr update in : " + nPstr + " secs" + nl);
  buff.append("# listeners        : " + hBeatMgr.getNumListeners() + nl);
  buff.append(footer);
  return buff.toString();
  }

 @Override
  public void timeUpdate(TimeStamp time)
  {
    // just overwrite the new time on top of the old
  editorPane.setText(toTimeString(time));
  }

  @Override
  public void heartBeatUpdate(SmtHeartBeatUpdater hBeatMgr, SmtHeartBeat heartBeat)
  {
    // just overwrite the new time on top of the old
    NumUpdates++;
    editorPane.setText(toHeartBeatString(hBeatMgr, heartBeat));
  }

}
