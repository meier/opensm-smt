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
 *        file: FabricSummaryPanel.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.text;

import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Updater;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_ServiceChangeListener;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_SysInfo;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.infiniband.opensm.plugin.net.OsmServerStatus;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.SmtConstants;
import gov.llnl.lc.smt.command.about.SmtAbout;
import gov.llnl.lc.smt.command.about.SmtAboutRecord;
import gov.llnl.lc.smt.command.fabric.SmtFabricStructure;
import gov.llnl.lc.smt.manager.MessageManager;
import gov.llnl.lc.time.TimeStamp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

public class FabricSummaryPanel extends JPanel implements OSM_ServiceChangeListener,  CommonLogger
{
  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = -5876531586380256969L;
  /**
   * Create the panel.
   */
  
  private FabricRootNodePopupMenu rootPopup = new FabricRootNodePopupMenu();
  
  private JEditorPane ePane;
  
  public FabricSummaryPanel()
  {
    setBorder(new TitledBorder(null, "Fabric Overview", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
    setLayout(new BorderLayout(0, 0));
    ePane = new JEditorPane();
    ePane.setEditable(false);
    
     ePane.setContentType("text/html");
    add(ePane);
    init(null);
   }
  
  public void init(OpenSmMonitorService OMS)
  {
     // given this information, I can create an HTML string
     String fabString = null;
    
    if(OMS != null)
    {
       ePane.setName(OMS.getFabricName());

      // if the timestamp is stale, make it red
       fabString = FabricSummaryPanel.getContent(OMS);
    }
    
    ePane.setText(fabString);
    return;
  }
  
  public static String getContent(OpenSmMonitorService OMS)
  {
     // given this information, I can create an HTML string
     StringBuffer buff = new StringBuffer();
    
    if(OMS != null)
    {
      SmtFabricStructure fabStruct = new SmtFabricStructure(OMS);
      OsmServerStatus ServerStatus = OMS.getRemoteServerStatus();
      OSM_SysInfo SysInfo          = OMS.getFabric().getOsmSysInfo();
      
      // if the timestamp is stale, make it red
      long secDiff = (new TimeStamp()).getTimeInSeconds() - OMS.getTimeStamp().getTimeInSeconds();
      String tsString = OMS.getTimeStamp().toString();
      if(secDiff > 360)
        tsString = SmtConstants.RED_FONT + tsString + SmtConstants.END_FONT ;

      // craft a pretty description of the fabric using the OMS
      buff.append(SmtConstants.MEDIUM_FONT);
      buff.append("<p align=right>time stamp: " + SmtConstants.SPACE + "<b>" + tsString + "</b>");
      if(ServerStatus != null)
        buff.append("<br>up since: " + SmtConstants.SPACE + "<b>" + ServerStatus.Server.getStartTime().toString() + "</b>");
      buff.append(fabStruct.toContent());
      buff.append("<br>");
      buff.append(SmtConstants.H_LINE);
      
      // put software version stuff here
      buff.append(SmtConstants.MEDIUM_FONT);
      if(SysInfo != null)
      {
        // get the versions installed on the remote host
        buff.append("OpenSM version: " + SmtConstants.SPACE + "<b>" + SysInfo.OpenSM_Version + "</b><br>");
        buff.append("OMS Plugin version: " + SmtConstants.SPACE + "<b>" + SysInfo.OsmJpi_Version + "</b><br>");

        String name   = "OsmClientServer";
        buff.append("OMS version (server side): " + SmtConstants.SPACE + "<b>" + name + SmtConstants.SPACE + ServerStatus.Version + SmtConstants.SPACE + "(" + ServerStatus.BuildDate + ")</b><br>");
        
        // get the versions installed on the client (they may be the same)
        String version   = "version";
        String buildDate = "buildDate";
        ArrayList<SmtAboutRecord> records = SmtAbout.getRecordsFromManifest(MessageManager.getInstance());
        SmtAboutRecord record = SmtAbout.getAboutRecord(records, name);
        if(record != null)
        {
          version = record.getVersion();
          buildDate = record.getDate();
        }
        buff.append("OMS version (client side): " + SmtConstants.SPACE + "<b>" + name + SmtConstants.SPACE + version + SmtConstants.SPACE + "(" + buildDate + ")</b><br>");
        
        name = "SubnetMonitorTool";
        record = SmtAbout.getAboutRecord(records, name);
        if(record != null)
        {
          version = record.getVersion();
          buildDate = record.getDate();
        }
        buff.append("SMT version: " + SmtConstants.SPACE + "<b>" + name + SmtConstants.SPACE + version + SmtConstants.SPACE + "(" + buildDate + ")</b>");
       }
    }
    else
    {
      // should I report an error, or just ignore?
      buff.append("<html><b><u>T</u>wo</b><br>lines</html>");
    }
    
    return buff.toString();
  }
  
  public void setRootNodePopup(FabricRootNodePopupMenu rootNodePopupMenu)
  {
    rootPopup = rootNodePopupMenu;
    ePane.setComponentPopupMenu(rootPopup);
  }


  @Override
  public void osmFabricUpdate(OSM_Fabric osmFabric) throws Exception
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void osmServiceUpdate(OMS_Updater updater, OpenSmMonitorService osmService)
      throws Exception
  {
    if(osmService != null)
      init(osmService);
    
  }

}
