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
 *        file: SMT_AnalysisPanel.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.text;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Collection;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_FabricDeltaAnalyzer;
import gov.llnl.lc.infiniband.opensm.plugin.data.RT_Path;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Depth;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Edge;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionEvent;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;
import gov.llnl.lc.infiniband.opensm.plugin.gui.chart.PortHeatMapPlotPanel;
import gov.llnl.lc.infiniband.opensm.plugin.gui.chart.PortUtilizationPanel;
import gov.llnl.lc.infiniband.opensm.plugin.gui.tree.SystemTreeModel;
import gov.llnl.lc.infiniband.opensm.plugin.gui.tree.SystemTreePopupMenu;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.SmtConstants;
import gov.llnl.lc.smt.data.SMT_AnalysisChangeListener;
import gov.llnl.lc.smt.data.SMT_UpdateService;
import gov.llnl.lc.smt.manager.GraphSelectionManager;
import gov.llnl.lc.smt.manager.SMT_AnalysisType;
import gov.llnl.lc.smt.manager.SMT_AnalysisUpdater;

public class SMT_AnalysisPanel extends JPanel implements SMT_AnalysisChangeListener,  CommonLogger
{
  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = 5355735591148429815L;
  /**
   * Create the panel.
   */
  
  private SMT_AnalysisUpdater AnalysisUpdater = null;
  private boolean HM_init = false;
  
  // this panel supports one of many types of analysis
  private SMT_AnalysisType Type;
  private IB_Guid Guid;
  private int PortNum;
  
  private IB_Edge Edge = null;
  private RT_Path Path = null;
  
  private EnumSet<IB_Depth> IncludedDepths = null;
  private ArrayList<IB_Vertex> IncludedNodes    = null;

  
  private JEditorPane ePane;
  private int Count = 0;
  private boolean HTML = false;
 
  public IB_Vertex getIB_Vertex()
  {
    //
    if(Guid != null)
    {
       return getIB_Vertex(Guid);
    }
    return null;
  }

  public IB_Vertex getIB_Vertex(IB_Guid guid)
  {
    //
    if(AnalysisUpdater != null)
    {
      OSM_FabricDeltaAnalyzer ofda = AnalysisUpdater.getDeltaAnalysis();
      return ofda.getIB_Vertex(guid);
    }
    return null;
  }

  public IB_Edge getIB_Edge()
  {
    if(Edge != null)
    {
      return Edge;
    }
    else if(Guid != null)
    {
      return getIB_Edge(Guid, PortNum);
    }
    return null;
  }

  public IB_Edge getIB_Edge(IB_Guid guid, int portNum)
  {
    if(AnalysisUpdater != null)
    {
      OSM_FabricDeltaAnalyzer ofda = AnalysisUpdater.getDeltaAnalysis();
      return ofda.getIB_Edge(guid, portNum);
    }
    return null;
  }
  
  /************************************************************
   * Method Name:
   *  getType
   **/
  /**
   * Returns the value of type
   *
   * @return the type
   *
   ***********************************************************/
  
  public SMT_AnalysisType getType()
  {
    return Type;
  }

  /************************************************************
   * Method Name:
   *  setType
   **/
  /**
   * Sets the value of type
   *
   * @param type the type to set
   *
   ***********************************************************/
  public void setType(SMT_AnalysisType type)
  {
    Type = type;
    
    // change the title
    setBorder(new TitledBorder(null, Type.getAnalysisName(), TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
    
  }

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

  public SMT_AnalysisPanel()
  {
    // by default, do fabric utilization
    this(SMT_AnalysisType.SMT_FABRIC_UTILIZATION);
  }
  
  public SMT_AnalysisPanel(SMT_AnalysisType type)
  {
    this(type, null);
  }
  
  public SMT_AnalysisPanel(SMT_AnalysisType type, Object obj)
  {
    this(type, obj, -1);
  }
  
  public SMT_AnalysisPanel(SMT_AnalysisType type, Object obj, int portNum)
  {
    setType(type);
    setLayout(new BorderLayout(0, 0));
    
    // the object is based on the type, but is normally a guid
    //
    // it could also be an IB_Edge, or an RT_Path
    // it could also be an EnumSet<IB_Depth>  (HeatMap)
    
    // assume its a guid, unless one of the special types
    if(type.equals(SMT_AnalysisType.SMT_LINK_UTILIZATION) && (obj instanceof IB_Edge))
      Edge = (IB_Edge)obj;
    else if(type.equals(SMT_AnalysisType.SMT_ROUTE_UTILIZATION) && (obj instanceof RT_Path))
      Path = (RT_Path)obj;
    else if(SMT_AnalysisType.SMT_HMAP_TYPES.contains(type) && (obj instanceof EnumSet<?>))
    {
      // TODO - this is not an editor pane, so should probably be implemented different, just
      //        wedge the HeatMap Panel in here
      //
      
      // The HeatMap is static, don't try to update it with new stuff
      SMT_UpdateService updateService = SMT_UpdateService.getInstance();
      OMS_Collection history = updateService.getCollection();

      IncludedDepths = (EnumSet<IB_Depth>) obj;
      add(new PortHeatMapPlotPanel(history, IncludedDepths));
      return;
    }
    else if(type.equals(SMT_AnalysisType.SMT_HMAP_SYS_PORTS) && (obj instanceof SystemTreeModel))
    {
      // TODO - this is not an editor pane, so should probably be implemented different, just
      //        wedge the HeatMap Panel in here
      //
      
      // The HeatMap is static, don't try to update it with new stuff
      SMT_UpdateService updateService = SMT_UpdateService.getInstance();
      OMS_Collection history = updateService.getCollection();
      SystemTreeModel model = (SystemTreeModel)obj;

//      String sysName = model.getSystemNameString();
//      System.err.println("The system name is2: (" + sysName + ")");
      IncludedNodes = model.getVertexList();
      add(new PortHeatMapPlotPanel(history, IncludedNodes));
      return;
    }
    else if(SMT_AnalysisType.SMT_UTIL_PLOT_TYPES.contains(type))
    {
      // TODO - this is not an editor pane, so should probably be implemented different, just
      //        wedge the Utilization Panel in here
      //
       // The Utilization Graph is static, don't try to update it with new stuff
      SMT_UpdateService updateService = SMT_UpdateService.getInstance();
      OMS_Collection history = updateService.getCollection();

      add(new PortUtilizationPanel(history, type));
      return;
    }
    else if(type.equals(SMT_AnalysisType.SMT_UTILIZATION))
    {
      // TODO - this is not an editor pane, so should probably be implemented different, just
      //        wedge the Utilization Panel in here
      //
       // The Utilization Graph is static, don't try to update it with new stuff
      SMT_UpdateService updateService = SMT_UpdateService.getInstance();
      OMS_Collection history = updateService.getCollection();

      add(new PortUtilizationPanel(history));
      return;
    }
    else if (obj != null)
      Guid = (IB_Guid)obj;
    
    PortNum = portNum;
    ePane = new JEditorPane();
    ePane.setEditable(false);
    
    if(type.equals(SMT_AnalysisType.SMT_FABRIC_UTILIZATION))
      ePane.setComponentPopupMenu(new SystemTreePopupMenu());
    else
      ePane.setComponentPopupMenu(new PrintPopupMenu(this));

    setHTML(true); // do in a per analysis specific way
    
    // if the text has hyperlinks, make them selectable
    ePane.addHyperlinkListener(new HyperlinkListener()
    {
      // this happens with anything, even if I just hover over the thing
      public void hyperlinkUpdate(HyperlinkEvent e)
      {
        // this happens only when the link is selected
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
        {
          JEditorPane pane = (JEditorPane) e.getSource();
          String selectedHREF = e.getDescription();

          // given this HREF, generate a selection event
          GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(this, Type, selectedHREF));
        }
      }
    });
    
    add(ePane);
    init(null);
   }
  
  
  /************************************************************
   * Method Name:
   *  getSingleNumberString
  **/
  /**
   * Draws a horizontal line, and then puts up a single overall
   * string in big blue leters, meant to be the overall average
   * analysis value.
   *
   * @see     describe related java objects
   *
   * @param val
   * @return
   ***********************************************************/
  private String getSingleNumberString(String val)
  {
    StringBuffer buff = new StringBuffer();
    
    // assumes we are resetting from a pre block to a normal html block
    buff.append(footer);
    buff.append("<br>");
   
    // put a dividing line and then a big number
    buff.append(SmtConstants.H_LINE);
    buff.append("<br>");
          
    // put single value here
    // try to center this
    buff.append(SmtConstants.CENTER_START);
    buff.append(SmtConstants.BOLD_START);

    buff.append(SmtConstants.XX_LARGE_FONT);
    buff.append(SmtConstants.BLUE_FONT + val + SmtConstants.END_FONT );

    // now reset back to a pre block to finish up
    buff.append(SmtConstants.BOLD_END);
    buff.append(SmtConstants.CENTER_END);
    buff.append(header);
    return buff.toString();    
  }
  
  /************************************************************
   * Method Name:
   *  toAnalysisString
  **/
  /**
   * Gets (builds) the analysis string from the AnalysisUpdater
   * based on the desired type of analysis.
   * 
   * Normally it consists of a header, followed by values, followed
   * by a single number (seperated by a horizontal line)
   *
   * @see     describe related java objects
   *
   * @return
   ***********************************************************/
  public String toAnalysisString()
  {
    // just overwrite the new time on top of the old
    StringBuffer buff = new StringBuffer();
    int maxTop = 20;
    
    // if one of the top table types...
    if((AnalysisUpdater != null) && (SMT_AnalysisType.SMT_TOP_TYPES.contains(Type) || (Type == SMT_AnalysisType.SMT_ROUTE_UTILIZATION)))
    {
      OSM_FabricDeltaAnalyzer ofda = AnalysisUpdater.getDeltaAnalysis();
      
      ePane.setContentType("text/html");
      // put other header crap here
      buff.append(this.TableStyle);
      
      if(Type == SMT_AnalysisType.SMT_FABRIC_ERROR_PORTS)
        buff.append(ofda.getPortErrorTableString(maxTop) + "\n\n");
      else if(Type == SMT_AnalysisType.SMT_FABRIC_ERROR_LINKS)
        buff.append(ofda.getLinkErrorTableString(maxTop) + "\n\n");
      else if(Type == SMT_AnalysisType.SMT_FABRIC_ERROR_NODES)
        buff.append(ofda.getNodeErrorTableString(maxTop) + "\n\n");
      else if(Type == SMT_AnalysisType.SMT_FABRIC_TOP_PORTS)
        buff.append(ofda.getTopPortTableString(maxTop) + "\n\n");
      else if(Type == SMT_AnalysisType.SMT_FABRIC_TOP_LINKS)
        buff.append(ofda.getTopLinkTableString(maxTop) + "\n\n");
      else if(Type == SMT_AnalysisType.SMT_FABRIC_TOP_NODES)
        buff.append(ofda.getTopNodeTableString(maxTop) + "\n\n");
      else if(Type == SMT_AnalysisType.SMT_ROUTE_UTILIZATION)
      {
        buff.append(ofda.getPathUtilizationTablesString(Path) + "\n\n");
        buff.append(getSingleNumberString(ofda.getPathUtilizationString(Path)));
      }
       
      return buff.toString();
    }
    
    buff.append(header);
  
  if(isHTML())
    buff.append(SmtConstants.MEDIUM_FONT);
  
  if(AnalysisUpdater != null)
  {
    OSM_FabricDeltaAnalyzer ofda = AnalysisUpdater.getDeltaAnalysis();
    
    // get the "type" of string from the updater
    if(Type == SMT_AnalysisType.SMT_FABRIC_UTILIZATION)
    {
      buff.append(ofda.getFabricRateUtilizationSummary());
      buff.append(getSingleNumberString(ofda.getFabrictUtilizationString()));
    }
    
    if(Type == SMT_AnalysisType.SMT_NODE_UTILIZATION)
    {
      buff.append(ofda.getNodeRateUtilizationSummary(Guid));
      buff.append(getSingleNumberString(ofda.getNodeUtilizationString(Guid)));
    }
    
    if(Type == SMT_AnalysisType.SMT_PORT_UTILIZATION)
    {
      buff.append(ofda.getPortRateUtilizationSummary(Guid, PortNum));
      buff.append(getSingleNumberString(ofda.getPortUtilizationString(Guid, PortNum)));
   }
    
    if(Type == SMT_AnalysisType.SMT_LINK_UTILIZATION)
    {
      buff.append(ofda.getLinkRateUtilizationSummary(Edge));
      buff.append(getSingleNumberString(ofda.getLinkUtilizationString(Edge)));
    }
    
    if(Type == SMT_AnalysisType.SMT_ROUTE_UTILIZATION)
    {
      // old version, dead code, will never get invoked
      // see table version in above section
      // TODO
      // convert all other forms to tables, for better visualization and active
      // links
      buff.append(ofda.getPathRateUtilizationSummary(Path, true) + "\n\n");
      buff.append(ofda.getPathRateUtilizationSummary(Path.getReturnPath(), false));
      buff.append(getSingleNumberString(ofda.getPathUtilizationString(Path)));
    }
    
    if(Type == SMT_AnalysisType.SMT_FABRIC_ERROR_PORTS)
    {
      buff.append(ofda.getTopPortTableString(20) + "\n\n");
    }
  }
  
  buff.append(footer);
  return buff.toString();
    
  }
  
  public void init(SMT_AnalysisUpdater updater)
  {
    AnalysisUpdater = updater;
    
    // given this information, I can create an HTML string
    if(ePane != null)
      ePane.setText(toAnalysisString());
    return;
  }
  
  public String toString()
  {
    StringBuffer buff = new StringBuffer();
    buff.append("SMT_AnalysisType: ");
    buff.append(this.getType().getName());
    return buff.toString();
  }

  @Override
  public void smtAnalysisUpdate(SMT_AnalysisUpdater updater) throws Exception
  {
    init(updater);
  }

}
