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
 *        file: OMS_CollectionListenerPanel.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.text;

import java.awt.BorderLayout;

import javax.swing.JEditorPane;
import javax.swing.JPanel;

import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Collection;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_CollectionChangeListener;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Updater;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.smt.SmtConstants;
import gov.llnl.lc.time.TimeStamp;

public class OMS_CollectionListenerPanel extends JPanel implements OMS_CollectionChangeListener
{
  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = 7340170542788175064L;

  private JEditorPane editorPane;

  private OMS_Updater UpdateService;
  
  private boolean TopDown = false;
  private boolean HTML = false;
  private String header = "";
  private String footer = "";
  private String nl     = "\n";
  private boolean initialized = false;
  

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
  public OMS_CollectionListenerPanel()
  {
    setLayout(new BorderLayout(0, 0));
    
    editorPane = new JEditorPane();
    editorPane.setEditable(false);
    setHTML(true);
    setTopDown(false);
    add(editorPane, BorderLayout.CENTER);
  }

  @Override
  public void osmCollectionUpdate(OMS_Collection omsHistory, OpenSmMonitorService osmService, boolean recording) throws Exception
  {
    int collectionSize = omsHistory.getSize();
    String iTime       = omsHistory.getOldestOMS().getTimeStamp().toString();
    String fTime       = omsHistory.getCurrentOMS().getTimeStamp().toString();
    String oTime       = osmService.getTimeStamp().toString();
    
    // since oTime is always fTime, try to get the currently served time
    if(UpdateService != null)
    {
      OpenSmMonitorService cOMS = UpdateService.getOMS();
      oTime = cOMS.getTimeStamp().toString();
    }
    else
      System.err.println("The UpdateService was null!");
    
    String oldText = editorPane.getText();
    String body = "";
    if(isHTML())
    {
      int sBody = oldText.indexOf(header) + header.length();
      int eBody = oldText.indexOf(footer);
      if((sBody > 0) && (eBody > 0))
        body = oldText.substring(sBody, eBody);
    }
    else
      body = oldText;
    
    // Truncate the body, to guarantee a manageable size
    int maxLen = 10000;
    int endIndex = body.length() < maxLen ? body.length(): maxLen;
    body = body.substring(0, endIndex);
    
    StringBuffer buff = new StringBuffer();

    buff.append(header);
    
    String sRecording = Boolean.toString(recording);
  
  if(isHTML())
  {
    buff.append(SmtConstants.MEDIUM_FONT);
    if(recording)
      sRecording = SmtConstants.GREEN_FONT + sRecording + SmtConstants.END_FONT;
    else
      sRecording = SmtConstants.RED_FONT + sRecording + SmtConstants.END_FONT;
  }

  if(initialized && TopDown)
    buff.append(body);
  buff.append("=====================================================================" + nl);
  buff.append("Fabric      : " + osmService.getFabricName() + nl);
  buff.append("Current Time: " + new TimeStamp().toString() + nl);
  buff.append("History Size: " + collectionSize + nl);
  buff.append("Initial Time: " + iTime + nl);
  buff.append("Final Time  : " + fTime + nl);
  buff.append("OMS Time    : " + oTime + nl);
  buff.append("# listeners : " + omsHistory.getNumListeners() + nl);
  buff.append("Recording?  : " + sRecording + nl);
  if(initialized && !TopDown)
    buff.append(body);

  buff.append(footer);
  editorPane.setText(buff.toString());
  initialized = true;
  }

  public void setUpdateService(OMS_Updater updateService)
  {
    UpdateService = updateService;
  }
  
}
