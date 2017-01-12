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
 *        file: OMS_UpdateListenerPanel.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.text;

import java.awt.BorderLayout;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Updater;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_ServiceChangeListener;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.SmtConstants;
import gov.llnl.lc.smt.data.SMT_AnalysisChangeListener;
import gov.llnl.lc.smt.event.SmtMessage;
import gov.llnl.lc.smt.event.SmtMessageType;
import gov.llnl.lc.smt.manager.MessageManager;
import gov.llnl.lc.smt.manager.SMT_AnalysisUpdater;
import gov.llnl.lc.time.TimeStamp;

public class OMS_UpdateListenerPanel extends JPanel implements OSM_ServiceChangeListener, SMT_AnalysisChangeListener, CommonLogger
{
  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = -3504282629992451944L;
  
  private JEditorPane editorPane;
  private boolean TopDown = false;
  private boolean HTML = false;
  private String header = "";
  private String footer = "";
  private String nl     = "\n";
  private boolean initialized = false;
  
  private static final int MAX_LEN = 10000;
  
  private int AnalysisCount = 0;

  private class OMS_UpdateListenerWorker extends SwingWorker<Void, Void>
  {
    SMT_AnalysisUpdater SMT_Updater = null;
    OMS_Updater Updater = null;

    public OMS_UpdateListenerWorker(SMT_AnalysisUpdater updater)
    {
      SMT_Updater = updater;
    }

    public OMS_UpdateListenerWorker(OMS_Updater updater)
    {
      Updater = updater;
      

    }

    @Override
    protected Void doInBackground() throws Exception
    {
      if(SMT_Updater != null)
      {
        // this is a SwingWorker thread from its pool, give it a recognizable name
        Thread.currentThread().setName("OMS_UpdateListenerWorker:S");
        putSMT_AnalysisText();
      }      
      else if(Updater != null)
      {
        // this is a SwingWorker thread from its pool, give it a recognizable name
        Thread.currentThread().setName("OMS_UpdateListenerWorker:O");
        putOMS_AnalysisText();
      }
    return null;
    }
    
    protected void putSMT_AnalysisText()
    {
      StringBuffer buff = new StringBuffer();
      String oldText    = getEditorPaneText();
      String body       = "";
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
      int maxLen = MAX_LEN;
      int endIndex = body.length() < maxLen ? body.length(): maxLen;
      body = body.substring(0, endIndex);

      buff.append(header);
    
    if(isHTML())
      buff.append(SmtConstants.MEDIUM_FONT);

    if(initialized && TopDown)
      buff.append(body);
    AnalysisCount++;
    buff.append("=====================================================================" + nl);
    buff.append("Analyzer      : " + SMT_Updater.getName() + nl);
    buff.append("Analysis Time : " + new TimeStamp().toString() + nl);
    buff.append("Delta Time    : " + SMT_Updater.getDeltaAnalysis().getDeltaTimeStamp().toString() + nl);
    buff.append("Analysis Count: " + AnalysisCount + nl);
    buff.append("# listeners   : " + SMT_Updater.getNumListeners() + nl);
    if(initialized && !TopDown)
      buff.append(body);

    buff.append(footer);
    
    // TODO seem to get runtime errors here, "must insert new content into body element" (html) using the setText()
    String s = buff.toString();
    if((editorPane != null) && (s != null) && (s.toString() != null) && (s.toString().length() > 0))
    {
      try
      {
        editorPane.setText(s);
      }
      catch(Exception e)
      {
        logger.severe(e.getMessage());
        System.err.println("Crap, can't setText in a swing component from this thread");
        System.exit(-1);
      }
    }
    initialized = true;
    return;
    }
    
    protected void putOMS_AnalysisText()
    {
      // if TopDown, then append new OMS to end of previous, if not
      // then prepend it.
      
      String oldText = getEditorPaneText();
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
      int maxLen = MAX_LEN;
      int endIndex = body.length() < maxLen ? body.length(): maxLen;
      body = body.substring(0, endIndex);
      
      StringBuffer buff = new StringBuffer();

      buff.append(header);
    
    if(isHTML())
      buff.append(SmtConstants.MEDIUM_FONT);

    if(initialized && TopDown)
      buff.append(body);
    buff.append("=====================================================================" + nl);
    buff.append("Fabric        : " + Updater.getOMS().getFabricName() + nl);
    buff.append("Update Time   : " + new TimeStamp().toString() + nl);
    buff.append("PerfMgr Time  : " + Updater.getOMS().getTimeStamp() + nl);
    buff.append("# listeners   : " + Updater.getNumListeners() + nl);
    if(initialized && !TopDown)
      buff.append(body);
    
    buff.append(footer);
    
    // TODO seem to get runtime errors here, "must insert new content into body element" (html) using the setText()
    if((editorPane != null) && (buff != null) && (buff.toString() != null) && (buff.toString().length() > 0))
    {
      try
      {
      editorPane.setText(buff.toString());
      }
      catch(Exception e)
      {
        logger.severe(e.getMessage());
      }
    }

    initialized = true;
    return;
    }
    
    @Override
    public void done()
    {
      // completion notification
      MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INFO, "Worker Done Listening for Updates"));
     }

  }

  
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
  
  private String getEditorPaneText()
  {
    // may need to buffer this myself, but right now, let the component do it
    String oldText = "oldText";
    if(editorPane != null)
    try
    {
      oldText = editorPane.getText();
    }
    catch (Exception e)
    {
      logger.severe("could not obtain old text from editor pane, consider maintaining my own buffer");
      logger.severe(e.getMessage());
    }
    else
      logger.severe("editorPane seems to be null");
    return oldText;
  }

  /**
   * Create the panel.
   */
  public OMS_UpdateListenerPanel()
  {
    setLayout(new BorderLayout(0, 0));
    
    editorPane = new JEditorPane();
    editorPane.setEditable(false);
    setHTML(true);
    setTopDown(false);
    add(editorPane);
  }

  @Override
  public void osmFabricUpdate(OSM_Fabric osmFabric) throws Exception
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void osmServiceUpdate(OMS_Updater updater, OpenSmMonitorService osmService) throws Exception
  {
    // if TopDown, then append new OMS to end of previous, if not
    // then prepend it.
    
    OMS_UpdateListenerWorker worker = new OMS_UpdateListenerWorker(updater);
    worker.execute();
  }

  @Override
  public void smtAnalysisUpdate(SMT_AnalysisUpdater updater) throws Exception
  {
    // if TopDown, then append analysis update to end of previous, if not
    // then prepend it.
    OMS_UpdateListenerWorker worker = new OMS_UpdateListenerWorker(updater);
    worker.execute();
  }
}
