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
 *        file: SMT_SearchPanel.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.text;

import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.core.IB_Link;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Node;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.infiniband.opensm.plugin.data.RT_Node;
import gov.llnl.lc.infiniband.opensm.plugin.data.RT_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.RT_Table;
import gov.llnl.lc.infiniband.opensm.plugin.data.SBN_MulticastGroup;
import gov.llnl.lc.infiniband.opensm.plugin.data.SBN_PartitionKey;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionEvent;
import gov.llnl.lc.infiniband.opensm.plugin.gui.data.SmtIconType;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.SmtConstants;
import gov.llnl.lc.smt.command.search.SMT_SearchResult;
import gov.llnl.lc.smt.command.search.SmtIdentification;
import gov.llnl.lc.smt.data.SMT_UpdateService;
import gov.llnl.lc.smt.event.SmtMessage;
import gov.llnl.lc.smt.event.SmtMessageType;
import gov.llnl.lc.smt.manager.GraphSelectionManager;
import gov.llnl.lc.smt.manager.MessageManager;
import gov.llnl.lc.smt.manager.SMT_RouteManager;
import gov.llnl.lc.smt.manager.SMT_SearchManager;
import gov.llnl.lc.time.TimeStamp;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.AbstractMap;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class SMT_SearchPanel extends JPanel implements  CommonLogger
{
  private SMT_SearchPanel thisPanel;
  private OpenSmMonitorService OMS;
  private java.util.ArrayList<SMT_SearchResult> SearchResults;
  
  private JEditorPane ePane;
  private boolean HTML = false;

  private String header = "";
  private String footer = "";
  private String nl     = "\n";
  
  private String  TableStyle = "<style type=\"text/css\">" +
      ".tftable {font-size:9px;color:#333333;width:100%;border-width: 1px;border-color: #a9a9a9;border-collapse: collapse;}" +
      ".tftable th {font-size:9px;background-color:#b8b8b8;border-width: 1px;padding: 6px;border-style: solid;border-color: #a9a9a9;text-align:center;}" +
      ".tftable tr {background-color:#ffffff;}" +
      ".tftable td {font-size:9px;border-width: 1px;padding: 6px;border-style: solid;border-color: #a9a9a9;}" +
      ".tftable tr:hover {background-color:#ffff99;}" +
      "</style> ";
  private JTextField searchText;


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
      ePane.setContentType("text/html");
      header = "<pre>";
      footer = "</pre>";
      nl     = "<br>";
    }
    else
    {
      ePane.setContentType("text/plain");
      header = "";
      footer = "";
      nl     = "\n";
    }
  }

  /**
   * Create the panel.
   */
  public SMT_SearchPanel()
  {
    this(null);
  }

  public SMT_SearchPanel(OpenSmMonitorService oms)
  {
    super();
    initialize(oms);
  }

  /**
   * Create the panel.
   */
  public void initialize(OpenSmMonitorService oms)
  {
    thisPanel = this;
    ePane = new JEditorPane();
    ePane.setEditable(false);
    // ePane.setComponentPopupMenu(new PrintPopupMenu(this));

    setHTML(true); // using an HTML table for results

    // if the text has hyperlinks, make them selectable
    ePane.addHyperlinkListener(new HyperlinkListener()
    {
      // this happens with anything, even if I just hover over the thing
      public void hyperlinkUpdate(HyperlinkEvent e)
      {
        // this happens only when the link is selected
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
        {
//          JEditorPane pane = (JEditorPane) e.getSource();
          String selectedHREF = e.getDescription();

          // given this HREF, generate a selection event
          GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(thisPanel, "SearchSelection", selectedHREF));
        }
      }
    });
    setLayout(new BorderLayout(0, 0));

    add(ePane);

    JPanel panel = new JPanel();
    add(panel, BorderLayout.NORTH);

    searchText = new JTextField();
    panel.add(searchText);
    searchText.setToolTipText("enter text to search");
    searchText.setEnabled(true);
    searchText.setEditable(true);
    searchText.setText("search string");
    searchText.setColumns(24);

    JButton btnNewButton = new JButton("");
    btnNewButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        if(isValidSearchText())
        {
          SMT_UpdateService updater = SMT_UpdateService.getInstance();
          OpenSmMonitorService oms = updater.getOMS();
          if (oms != null)
          {
            IB_Guid g = SMT_SearchManager.getNodeGuid(searchText.getText(), false, oms);
            OSM_Fabric fabric = null;
            OSM_Node n = null;
            if ((oms != null) && (g != null))
            {
              fabric = oms.getFabric();
              n = fabric.getOSM_Node(g);
            }
            refreshSearch(oms);
          }
          else
            System.err.println("OMS is null, can't search");
        }
        else
        {
          String msg = "Invalid search text (" + searchText.getText() + "), please try again";
          logger.warning( msg);
          MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_WARNING, msg));
        }
      }
    });
    btnNewButton.setIcon(SmtIconType.SMT_SEARCH_ICON.getIcon());

    panel.add(btnNewButton);
    refreshSearch(null);
  }

  protected boolean isValidSearchText()
  {
    // validate the search text
    //
    // must be at least 1 character long, like a number
    // can't contain illegal characters (TBD)
    int len = searchText.getText().length();
    if((len > 0) && (len < 80))
      return true;
    return false;
  }

  protected void refreshSearch(OpenSmMonitorService oms)
  {
    if(oms != null)
      OMS = oms;
    
    // given this information, I can create an HTML string
    if(ePane != null)
      ePane.setText(toSearchResultsString());
    return;
  }
  
  public static String getIntAsHexString(int val)
  {
    return "0x" + Integer.toHexString(val) + " (" + val + ")";
  }
  
  
  /************************************************************
   * Method Name:
   *  getSearchResults
   **/
  /**
   * Returns the value of searchResults
   *
   * @return the searchResults
   *
   ***********************************************************/
  
  public java.util.ArrayList<SMT_SearchResult> getSearchResults()
  {
    return SearchResults;
  }

  /************************************************************
   * Method Name:
   *  getSearchResults
   **/
  /**
   * Returns the value of searchResults
   *
   * @return the searchResults
   *
   ***********************************************************/
  
  public SMT_SearchResult getSearchResult(int index)
  {
    // do some basic checks first
    if((SearchResults != null) && (SearchResults.size() > index))
      return SearchResults.get(index);
    return null;
  }

  /************************************************************
   * Method Name:
   *  toSearchResultsString
  **/
  /**
   * Gets (builds) the search results string from the AnalysisUpdater
   * based on the desired type of analysis.
   * 
   * Normally it consists of a header, followed by search result records,
   * followed by a footer
    *
   * @see     describe related java objects
   *
   * @return
   ***********************************************************/
  protected String toSearchResultsString()
  {
    // just overwrite the new time on top of the old
    StringBuffer buff = new StringBuffer();
    int maxResults = 20;
    
    
   // use an HTML Table to show results (change this if prefer something else)
    ePane.setContentType("text/html");
    // put other header crap here
    buff.append(this.TableStyle);
    
    buff.append(getSearchResultsTableString());
  
  buff.append(footer);
  return buff.toString();
    
  }
  
  protected String getSearchResultsTableString()
  {
    // return a string that will construct an HTML table
    // populated with the search results records
    
    // INPUT:  the validated search text
    //         an OMS instance
    //
    // Supported OUTPUT types:  NODES
    //                          PORTS
    //                          LINKS
    //                          MLIDS  (multicast groups)
    //                          PKEYS  (partitions)
    //                          SWITCH Tables (form of node)
    //
    StringBuffer sbuff = new StringBuffer();
    String search = searchText.getText();
    String curString = new TimeStamp().toString();
    String tsString = curString;
    if(OMS != null)
      tsString  = OMS.getTimeStamp().toString();
    
    // using the OMS and search string, find stuff, and put it in the table
    SearchResults = SMT_SearchManager.getSearchResults(search, OMS);

    sbuff.append(SmtConstants.H_LINE);

    sbuff.append(SmtConstants.MEDIUM_FONT);
    sbuff.append("<p align=right>search time: " + SmtConstants.SPACE + "<b>" + curString + "</b>");
    sbuff.append("<br>time stamp: " + SmtConstants.SPACE + "<b>" + tsString + "</b>");
    sbuff.append("<h4>Results: " + SearchResults.size() + "</h4>");
     
    sbuff.append("<blockquote>");
    sbuff.append(SmtConstants.MEDIUM_FONT);
    sbuff.append("search id: " + SmtConstants.SPACE + "[<b>" + search + "</b>]");
    sbuff.append("</blockquote>");

    sbuff.append("<table class=\"tftable\" border=\"1\">");
    
    // the header, or title
    sbuff.append("<tr><th>num</th><th>type</th><th>identification</th><th>attribute</th><th>more</th><th>other</th></tr>");
    
    
    int n = 1;
    if((SearchResults != null) && (SearchResults.size() > 0))
    {
      for(SMT_SearchResult sr: SearchResults)
      {
        sbuff.append(getSearchResultsTableLine(n++, sr));
      }
    }

    sbuff.append("</table>");
    return sbuff.toString();
  }
  
  private Object getSearchResultsTableLine(int i, SMT_SearchResult sr)
  {
    StringBuffer sbuff = new StringBuffer();
    if(sr != null)
    {
      // put this in table form
      String ID     = "<a href=\"" + i  + "\">" + i + "</a>";
      String name   = "name";
      String attrib = "attrib";
      String more   = "more";
      String other  = "other";
      Object obj    = sr.getResultObject();
      OSM_Fabric Fabric = sr.getOMS().getFabric();
      RT_Table Table    = SMT_RouteManager.getInstance().getRouteTable();
      
      switch (sr.getType())
      {
        case SEARCH_NODE:
          // fix the fields for this type
          more = "";
          other = "";
          if(obj != null)
          {
            // I know its a Node
            OSM_Node n = (OSM_Node)obj;
            name = n.sbnNode.description;
            attrib = n.getNodeGuid().toColonString();
            int lid = Fabric.getOsmPorts().getLidFromPortGuid(n.getNodeGuid());
            // if this returns < 0, add one to the node guid, and try again
            if(lid < 0)
            {
              IB_Guid g = new IB_Guid(n.getNodeGuid().getGuid() + 1);
              lid = Fabric.getOsmPorts().getLidFromPortGuid(g);
            }
            more = getIntAsHexString(lid);
            other = (n.isSwitch()) ? "Switch": "Channel Adapter";
          }
          break;
          
        case SEARCH_PORT:
          // fix the fields for this type
          more = "";
          other = "";
           if(obj != null)
           {
             // I know its a Port
             OSM_Port p = (OSM_Port)obj;
             name = Fabric.getNameFromGuid(p.getNodeGuid()) + " port: " + p.getPortNumber();
             attrib = p.getNodeGuid().toColonString()+":"+p.getPortNumber();
             more = p.getRateString();
             other = p.getStateString();
            }
          break;
          
        case SEARCH_LINK:
          // fix the fields for this type
          more = "";
          other = "";
          if(obj != null)
          {
            // I know its a Link
            IB_Link l = (IB_Link)obj;
            OSM_Port p1 = l.getEndpoint1();
            OSM_Port p2 = l.getEndpoint2();
            name   = Fabric.getNameFromGuid(p1.getNodeGuid()) + " port: " + p1.getPortNumber() + " -> ";
            attrib =  Fabric.getNameFromGuid(p2.getNodeGuid()) + " port: " + p2.getPortNumber();
            more   = l.getRateString();
            other  = l.getStateString();
           }
          break;
          
        case SEARCH_RTNODE:
          // fix the fields for this type
          more = "";
          other = "";
          if(obj != null)
          {
            // I know its a node
            RT_Node n = (RT_Node)obj;
            name   = n.getName(Fabric);
            attrib = n.getGuid().toColonString();
            if(Table != null)
            {
              more  = "CA Routes: " + n.getNumCaRoutes(Table);
              other = "Ports with Routes: " + n.getPortRouteMap().size();
            }
           }
          break;
          
        case SEARCH_RTPORT:
          // fix the fields for this type
          more = "";
          other = "";
          if(obj != null)
          {
            // I know its a port
            RT_Port p = (RT_Port)obj;
            RT_Node n = p.getParentNode();
            name = n.getName(Fabric) + " port: " + p.getPortNumber();
            attrib = n.getGuid().toColonString() +":"+p.getPortNumber();
            if(Table != null)
            {
              more  = "CA Routes: " + p.getNumCaRoutes(Table);
              other = "Total Routes: " + p.getNumRoutes();
            }
           }
          break;
          
        case SEARCH_CONFIG:
          // fix the fields for this type
          more = "";
          other = "";
         if(obj != null)
          {
            // I know its a name/value pair
            AbstractMap.SimpleEntry<String, String> pair = (AbstractMap.SimpleEntry<String, String>)obj;
            name = pair.getKey();
            attrib = pair.getValue();
          }
           break;
          
        case SEARCH_PARTITION:
          // fix the fields for this type
          more = "";
          other = "";
          if(obj != null)
          {
            // I know its a name/value pair
            SBN_PartitionKey key = (SBN_PartitionKey)obj;
            name = key.Name;
            attrib = "pkey: " + getIntAsHexString(key.pkey);
            more = "mlid: " + key.mlid;
            other = "full members: " + key.full_member_guids.length;
          }
           break;
          
        case SEARCH_MCAST:
          // fix the fields for this type
          more = "";
          other = "";
          if(obj != null)
          {
            // I know its a name/value pair
            SBN_MulticastGroup mg = (SBN_MulticastGroup)obj;
            name = "";
            more = "well known?: " + mg.well_known;
            attrib = "mlid: " + getIntAsHexString(mg.mlid);
            other = "members: " + mg.port_guids.length;
          }
          break;
          
        case SEARCH_SA_KEY:
        case SEARCH_SUBNET_KEY:
        case SEARCH_SUBNET_PREFIX:
          // fix the fields for this type
          more = "";
          other = "";
          if(obj != null)
          {
            // I know its a name/value pair
            IB_Guid g = (IB_Guid)obj;
            name      = g.toColonString();
            attrib    = Long.toString(g.getGuid());
          }
          break;
          
            // fix the fields for this type
         default:
           name   = "unknown";
           more = "";
           other = "";
           break;
       }
      
        sbuff.append("<tr>");
        sbuff.append("<td align=\"center\">" + ID + "</td>");
        sbuff.append("<td align=\"left\">"   + sr.getType().getName() + "</td>");
        sbuff.append("<td align=\"left\">"   + name + "</td>");
        sbuff.append("<td align=\"right\">"  + attrib + "</td>");
        sbuff.append("<td align=\"right\">"  + more + "</td>");
        sbuff.append("<td align=\"right\">"  + other + "</td>");
      
    }
    
  return sbuff.toString();
  }
}
