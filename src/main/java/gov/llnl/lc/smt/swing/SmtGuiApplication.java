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
 *        file: SmtGuiApplication.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.swing;

import gov.llnl.lc.infiniband.core.IB_Guid;
import gov.llnl.lc.infiniband.core.IB_Link;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Collection;
import gov.llnl.lc.infiniband.opensm.plugin.data.OMS_Updater;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Fabric;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Node;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.OSM_ServiceChangeListener;
import gov.llnl.lc.infiniband.opensm.plugin.data.OpenSmMonitorService;
import gov.llnl.lc.infiniband.opensm.plugin.data.PFM_Port.PortCounterName;
import gov.llnl.lc.infiniband.opensm.plugin.data.RT_Node;
import gov.llnl.lc.infiniband.opensm.plugin.data.RT_Path;
import gov.llnl.lc.infiniband.opensm.plugin.data.RT_Port;
import gov.llnl.lc.infiniband.opensm.plugin.data.RT_Table;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Depth;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Edge;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionEvent;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionListener;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionUpdater;
import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Vertex;
import gov.llnl.lc.infiniband.opensm.plugin.graph.SystemErrGraphListener;
import gov.llnl.lc.infiniband.opensm.plugin.gui.graph.SimpleGraphControlPanel;
import gov.llnl.lc.infiniband.opensm.plugin.gui.text.FabricRootNodePopupMenu;
import gov.llnl.lc.infiniband.opensm.plugin.gui.text.FabricSummaryPanel;
import gov.llnl.lc.infiniband.opensm.plugin.gui.text.GraphSelectionPanel;
import gov.llnl.lc.infiniband.opensm.plugin.gui.text.MessageListenerPanel;
import gov.llnl.lc.infiniband.opensm.plugin.gui.text.OMS_CollectionListenerPanel;
import gov.llnl.lc.infiniband.opensm.plugin.gui.text.OMS_UpdateListenerPanel;
import gov.llnl.lc.infiniband.opensm.plugin.gui.text.SMT_AboutPanel;
import gov.llnl.lc.infiniband.opensm.plugin.gui.text.SMT_AnalysisPanel;
import gov.llnl.lc.infiniband.opensm.plugin.gui.text.SMT_SearchPanel;
import gov.llnl.lc.infiniband.opensm.plugin.gui.text.SearchIdentificationPanel;
import gov.llnl.lc.infiniband.opensm.plugin.gui.text.TimeListenerPanel;
import gov.llnl.lc.infiniband.opensm.plugin.gui.tree.FabricDiscoveryPanel;
import gov.llnl.lc.infiniband.opensm.plugin.gui.tree.FabricTreeModel;
import gov.llnl.lc.infiniband.opensm.plugin.gui.tree.FabricTreePanel;
import gov.llnl.lc.infiniband.opensm.plugin.gui.tree.LinkTreeModel;
import gov.llnl.lc.infiniband.opensm.plugin.gui.tree.LinkTreePanel;
import gov.llnl.lc.infiniband.opensm.plugin.gui.tree.NameValueNode;
import gov.llnl.lc.infiniband.opensm.plugin.gui.tree.OptionMapTreeModel;
import gov.llnl.lc.infiniband.opensm.plugin.gui.tree.OptionMapTreePanel;
import gov.llnl.lc.infiniband.opensm.plugin.gui.tree.PortTreeModel;
import gov.llnl.lc.infiniband.opensm.plugin.gui.tree.PortTreePanel;
import gov.llnl.lc.infiniband.opensm.plugin.gui.tree.RT_NodeTreeModel;
import gov.llnl.lc.infiniband.opensm.plugin.gui.tree.RT_NodeTreePanel;
import gov.llnl.lc.infiniband.opensm.plugin.gui.tree.RT_PathTreeModel;
import gov.llnl.lc.infiniband.opensm.plugin.gui.tree.RT_PathTreePanel;
import gov.llnl.lc.infiniband.opensm.plugin.gui.tree.RT_PortTreeModel;
import gov.llnl.lc.infiniband.opensm.plugin.gui.tree.RT_PortTreePanel;
import gov.llnl.lc.infiniband.opensm.plugin.gui.tree.RT_TableTreeModel;
import gov.llnl.lc.infiniband.opensm.plugin.gui.tree.RT_TableTreePanel;
import gov.llnl.lc.infiniband.opensm.plugin.gui.tree.SimplePortTreeModel;
import gov.llnl.lc.infiniband.opensm.plugin.gui.tree.SubnetTreeModel;
import gov.llnl.lc.infiniband.opensm.plugin.gui.tree.SubnetTreePanel;
import gov.llnl.lc.infiniband.opensm.plugin.gui.tree.SystemTreeModel;
import gov.llnl.lc.infiniband.opensm.plugin.gui.tree.SystemTreePanel;
import gov.llnl.lc.infiniband.opensm.plugin.gui.tree.UserObjectTreeNode;
import gov.llnl.lc.infiniband.opensm.plugin.gui.tree.VertexTreeModel;
import gov.llnl.lc.infiniband.opensm.plugin.gui.tree.VertexTreePanel;
import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.command.SmtCommand;
import gov.llnl.lc.smt.command.SmtCommandType;
import gov.llnl.lc.smt.command.file.SmtFile;
import gov.llnl.lc.smt.command.gui.SmtGuiRunner;
import gov.llnl.lc.smt.command.search.SMT_SearchResult;
import gov.llnl.lc.smt.command.search.SMT_SearchResultType;
import gov.llnl.lc.smt.data.SMT_UpdateService;
import gov.llnl.lc.smt.event.SmtMessage;
import gov.llnl.lc.smt.event.SmtMessageType;
import gov.llnl.lc.smt.manager.GraphSelectionManager;
import gov.llnl.lc.smt.manager.HeartBeatManager;
import gov.llnl.lc.smt.manager.MessageManager;
import gov.llnl.lc.smt.manager.SMT_AnalysisManager;
import gov.llnl.lc.smt.manager.SMT_AnalysisType;
import gov.llnl.lc.smt.manager.SMT_AnalysisUpdater;
import gov.llnl.lc.smt.manager.SMT_GraphManager;
import gov.llnl.lc.smt.manager.SMT_RouteManager;
import gov.llnl.lc.smt.manager.SmtHeartBeatUpdater;
import gov.llnl.lc.smt.manager.SmtMessageUpdater;
import gov.llnl.lc.smt.prefs.SmtGuiPreferences;
import gov.llnl.lc.smt.props.SmtProperties;
import gov.llnl.lc.smt.props.SmtProperty;
import gov.llnl.lc.time.TimeListener;
import gov.llnl.lc.time.TimeService;
import gov.llnl.lc.time.TimeSliderPanel;
import gov.llnl.lc.time.TimeStamp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.KeyStroke;

public class SmtGuiApplication implements IB_GraphSelectionListener, CommonLogger, OSM_ServiceChangeListener, TimeListener, ActionListener
{
  private static OMS_Updater UpdateService = null;

  private static OpenSmMonitorService OMS = null;
  
  private static final String GControlName = "G:Controls";
  private static final String HelpSetName = "main_en_US.hs";
  
  private static final boolean debug = false;
  
  // the various managers (don't need to keep a copy, just use getInstance()?? )
  //   better yet, use the interface so new mgrs can be used
  private volatile static TimeService Tserv = null;
  private static SmtHeartBeatUpdater HeartBeat_Mgr = null;
  private static SmtMessageUpdater   Message_Mgr = null;
  private static SMT_AnalysisUpdater Analysis_Mgr = null;
  private static IB_GraphSelectionUpdater Selection_Mgr = null;
  private static SMT_RouteManager Route_Mgr = null;
  private static SMT_GraphManager Graph_Mgr = null;
  
  // these need to be visible locally
  private static JFrame applicationFrame;
  private static JClosableTabbedPane textTabbedPane;
  private static JClosableTabbedPane westTabbedPane;
  private static JClosableTabbedPane centerTabbedPane;
  private static JPanel southPanel;
  
  private FabricRootNodePopupMenu rootPopup = new FabricRootNodePopupMenu();  // menu for the Fabric operations (graph,route, status, etc.)
  
  private JCheckBoxMenuItem OverviewCBMI;
  private JCheckBoxMenuItem GraphCBMI;
  private JCheckBoxMenuItem UtilizationCBMI;
  private JCheckBoxMenuItem RoutingCBMI;

  private JCheckBoxMenuItem DetailsCBMI;
  
  private static JMenuItem CloseTabsMenuItem;
  private JCheckBoxMenuItem HeartBeatCBMI;
  private JCheckBoxMenuItem MessageCBMI;
  private JCheckBoxMenuItem OMSUpdateCBMI;
  private JCheckBoxMenuItem OMSRecordCBMI;
  private JCheckBoxMenuItem SelectionCBMI;
  private JMenuItem ExitMenuItem;
  
  // Top Menu Items
  private JCheckBoxMenuItem TNodesCBMI;
  private JCheckBoxMenuItem TLinksCBMI;
  private JCheckBoxMenuItem TPortsCBMI;
  private JCheckBoxMenuItem ENodesCBMI;
  private JCheckBoxMenuItem ELinksCBMI;
  private JCheckBoxMenuItem EPortsCBMI;
  
  // HeatMap Menu Items
  private JCheckBoxMenuItem HmpCaPortsCBMI;
  private JCheckBoxMenuItem HmpSwPortsCBMI;
  private JCheckBoxMenuItem HmpAllPortsCBMI;
  
  // Main HelpSet & Broker
  private HelpSet mainHS = null;
  private HelpBroker mainHB;
  private JMenuItem AboutMenuItem;
  
  // Historical OMS sessions
  private JMenuItem mntmHistory1;
  private JMenuItem mntmHistory2;
  private JMenuItem mntmHistory3;
  private JMenuItem mntmHistory4;
  private JMenuItem mntmHistory5;

  /**
   * Launch the application.
    */
  public static void main(String[] args)
  {
    go(null, null, null);
  }
  
  public FabricRootNodePopupMenu getRootNodePopupMenu()
  {
    return rootPopup;
  }
  
  public static void go(OMS_Updater updater, OpenSmMonitorService initialOMS, SmtCommand cmd)
  {
    // running via the swing (awt) eventqueue is the correct way, but I already
    // have a splashpanel running in this thread.  I want to talk to it via messages, not
    // events.
    
//    EventQueue.invokeLater(new SmtGuiRunner(updater, initialOMS, cmd));
    
    // FIXME - hardcode the run for now, later convert messages to events for the splashpanel info
    (new SmtGuiRunner(updater, initialOMS, cmd)).run();
   }
   
  public static Dimension getAveAvailableScreenDimension()
  {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] gs = ge.getScreenDevices(); // Get size of each screen
    int screenWidth = 0;
    int screenHeight = 0;
    int i = 0;
    for (i=0; i<gs.length; i++)
    {
      DisplayMode dm = gs[i].getDisplayMode();
      screenWidth += dm.getWidth();  // the sum of the screens
      screenHeight += dm.getHeight();  // the sum of the screens
      } 
    return new Dimension(screenWidth/i, screenHeight/i);
  }

  /**
   * Create the application (do minimum necessary for gui builder, don't init services and such)
   */
  public SmtGuiApplication()
  {
    initialize();
    
    // complete initialization starts with above, followed by;
//    initializeFramework();
//    initializeUpdateService();
    // in that order
  }
  
  private boolean addToCenter(JScrollPane scrollPane, boolean exclusive, boolean selected)
  {
    int index = findNamedInCenter(scrollPane.getName());
    // if exclusive is true, only add if the name of the scrollPane is unique
    if(exclusive && (index > -1))
    {
      // already exists, so don't create another one, but bring it into focus
      setSelectedInCenter(index);
      return false;
    }
    
    // add this JScrollPane to the center TabbedPanel, it should have its name set
    // because it will be used for the tab
    Dimension msgDim = new Dimension(300, 200);
    scrollPane.setPreferredSize(msgDim);  // the size of this panel

    centerTabbedPane.addTab(scrollPane.getName(), null, scrollPane, null);
    int ndex = centerTabbedPane.getComponentCount() -1;
    centerTabbedPane.insertClosableTab(scrollPane.getName(), null, scrollPane, null, ndex);
    
    // should this pane be brought to the front, or be selected?
    if(selected)
      centerTabbedPane.setSelectedComponent(scrollPane);
    return true;
  }

  private void addToCenter(JScrollPane scrollPane)
  {
    // by default, don't allow duplicates, and they can be selectable
    addToCenter(scrollPane, true, true);
  }

  private void addToWest(JScrollPane scrollPane)
  {
    // add this JScrollPane to the south TabbedPanel, it should have its name set
    // because it will be used for the tab
    if(westTabbedPane == null)
    {
      System.err.println("The WestTabbedPane (main area) is null, suspect initialization()");
      System.exit(-1);
    }
      
    westTabbedPane.addTab(scrollPane.getName(), null, scrollPane, null);
    int ndex = westTabbedPane.getComponentCount() -1;
    westTabbedPane.insertClosableTab(scrollPane.getName(), null, scrollPane, null, ndex);
  }

  private int findNamedInCenter(String name)
  {
    int ndex = centerTabbedPane.getComponentCount();
    if(( ndex > 0) && (name != null))
    {
      for(int j = 0; j < ndex; j++)
      {
        // there are only two type of objects in the center pane
        // JScrollPanes and SMT_FabricGraphPanel
        Object obj = centerTabbedPane.getComponentAt(j);
        if(obj instanceof JScrollPane)
        {
          JScrollPane scrollPane = (JScrollPane) obj;
          if(name.equals(scrollPane.getName()))
            return j;
        }
        else if (obj instanceof SMT_FabricGraphPanel)
        {
          // should only be one, no need to check name
          if(name.equals("G:" + OMS.getFabricName()))
           return j;
        }
       }
    }
    return -1;
  }
    
  private void setSelectedInCenter(int index)
  {
    centerTabbedPane.setSelectedIndex(index);
  }
  
  private boolean addFabricGraph()
  {
    String tabName = "G:" + OMS.getFabricName();
    boolean exclusive = true;
    
    // if exclusive is true, only add if the name of the scrollPane is unique
    if(exclusive && (this.findNamedInCenter(tabName) > -1))
      return false;
    
    SMT_FabricGraphPanel fgp = new SMT_FabricGraphPanel(OMS.getFabric(), centerTabbedPane.getSize());
    Graph_Mgr.setGraphPanel(fgp);
    
    UpdateService.addListener(fgp);
    Analysis_Mgr.addSMT_AnalysisChangeListener(fgp);

    // add this GraphPanel to the center TabbedPanel, it should have its name set
    // because it will be used for the tab

    centerTabbedPane.addTab(tabName, null, fgp, null);
    int ndex = centerTabbedPane.getTabCount() -1;
    centerTabbedPane.insertClosableTab(tabName, null, fgp, null, ndex);
    centerTabbedPane.setSelectedComponent(fgp);
    
    // now also add the control panel down in the message area
    SimpleGraphControlPanel gcp = new SimpleGraphControlPanel(fgp.getFabricGraph());
    Graph_Mgr.setGraphControlPanel(gcp);

    JScrollPane scrollPaneControls = new JScrollPane(gcp);
    scrollPaneControls.setName(GControlName);
    addToSouth(scrollPaneControls, true);
     
    return true;
  }
  
  private void removeFabricGraph()
  {
    logger.info("Fabric Graph being Removed");

    if(!removeNamedFromCenter("G:" + OMS.getFabricName()))
       logger.severe("Could NOT remove FabricGraph from center");
     
    // attempt to remove controls
    removeFromSouth(new SimpleGraphControlPanel(false));
  }

  private boolean removeNamedFromCenter(String name)
  {
    int ndex = findNamedInCenter(name);
    if( ndex > -1)
      removeFromCenter(ndex);
    return ndex > -1 ? true: false;
  }

  private void removeFromCenter(int index)
  {
    int ndex = centerTabbedPane.getTabCount();
    logger.info("Removing index: " + index + ", from center which has: " + ndex + " elements");
    if(index < ndex)
    {
      // attempt to remove it from listener lists
      Object obj = centerTabbedPane.getComponentAt(index);
      
      if(obj instanceof JScrollPane)
      {
        JScrollPane scrollPane = (JScrollPane) obj;
        closeScrollPane(scrollPane);
      }
      else if(obj instanceof SMT_FabricGraphPanel)
      {
        SMT_FabricGraphPanel fgp = (SMT_FabricGraphPanel)obj;
        
        // remove this from both listener lists
        UpdateService.removeListener(fgp);
        Analysis_Mgr.removeSMT_AnalysisChangeListener(fgp);
        Graph_Mgr.removeGraphPanel(fgp);
       }
      else
      {
        // if something other than a scroll pane, handle it here
        logger.info("Removing something other than a scrollpane or fabricgraph from the center");
        logger.info(obj.getClass().getCanonicalName());
      }
      centerTabbedPane.remove(index);
    }
  }

  private void removeAllFromCenter()
  {
    int ndex = centerTabbedPane.getTabCount();
    
    if(ndex > 0)
    {
      logger.warning("There are " + ndex + " elements in the tabbed pane");
      JScrollPane sp = null;
      int index = 0;
      
      // can't iterate through collection, it changes after each removal, always remove element 0
      for (int j = 0; j < ndex; j++)
      {
        // these should all be scroll panes
        if(centerTabbedPane.getComponentAt(index) instanceof JScrollPane)
        {
          sp = (JScrollPane)centerTabbedPane.getComponentAt(index);
          JViewport vp = sp.getViewport();
          logger.warning("I have a JViewport, and it contains: " + vp.getComponentCount() + " objects");
          for (int i = 0; i < vp.getComponentCount(); i++)
          {
            // normally only a single component
            logger.warning("The Closing Component is at index " + index);
            logger.warning("The Closing Component is in scroll panel: " + sp.getName());

            GraphSelectionManager.getInstance().updateAllListeners( new IB_GraphSelectionEvent(centerTabbedPane, sp, index));
            removeFromCenter(index);
           }
         }
        else if(centerTabbedPane.getComponentAt(index) instanceof SMT_FabricGraphPanel)
        {
          removeFabricGraph();
        }
      }
   }
  }

  private void removeFromSouth(int index)
  {
    int ndex = textTabbedPane.getComponentCount();
    if(index < ndex)
    {
      textTabbedPane.remove(index);
    }
  }

  private boolean removeFromSouth(Component obj)
  {
    // there should only ever be one of each type of panel in the south
    // find the index of this component, and if it exists, close it
    int ndex = textTabbedPane.getComponentCount();
    boolean removed = false;
    
    if((obj != null) && (ndex > 0))
    {
      logger.info("There are " + ndex + " elements in the tabbed pane");
      JScrollPane sp = null;
      int index = 0;
      for (int j = 0; j < ndex; j++)
      {
        // these should all be scroll panes
        if(textTabbedPane.getComponentAt(j) instanceof JScrollPane)
        {
          sp = (JScrollPane)textTabbedPane.getComponentAt(j);
          JViewport vp = sp.getViewport();
           for (int i = 0; i < vp.getComponentCount(); i++)
          {
            // normally only a single component but loop through and break ASAP
            if ((vp.getComponent(i) instanceof TimeListenerPanel) && (obj instanceof TimeListenerPanel))
              removed = true;
            else if ((vp.getComponent(i) instanceof GraphSelectionPanel) && (obj instanceof GraphSelectionPanel))
              removed = true;
            else if ((vp.getComponent(i) instanceof OMS_CollectionListenerPanel) && (obj instanceof OMS_CollectionListenerPanel))
              removed = true;
            else if ((vp.getComponent(i) instanceof OMS_UpdateListenerPanel) && (obj instanceof OMS_UpdateListenerPanel))
              removed = true;
            else if ((vp.getComponent(i) instanceof MessageListenerPanel) && (obj instanceof MessageListenerPanel))
              removed = true;
            else if ((vp.getComponent(i) instanceof SimpleGraphControlPanel) && (obj instanceof SimpleGraphControlPanel))
              removed = true;
            // found it, all done
            if (removed)
            {
              index = j;
              break;
            }
          }
          // found it, all done
          if (removed)
            break;
        }
      }
      // now do the actual remove
      if(removed)
      {
        logger.warning("The South Removed Component is at index " + index);
        logger.warning("The South Removed Component is in scroll panel: " + sp.getName());
        // send the message, which will be handled by doing a "closeScrollPane()"
        GraphSelectionManager.getInstance().updateAllListeners( new IB_GraphSelectionEvent(textTabbedPane, sp,index));
        textTabbedPane.removeTabAt(index);
     }
   }
    return removed;
  }
  
  private void setApplicationTitle(String FabricName, String Mode, String Other)
  {
    // create a useful title bar, providing prioritized information based on the argument order above
    String fabricName = FabricName;
    String toolName   = SmtCommandType.SMT_GUI_CMD.getToolName();
    String fileName   = null;
    String portNum    = "10011";
    String hostNam    = "localhost";
    
    // how big is the title bar??
    Rectangle bounds = applicationFrame.getBounds();
    int approxTitleBarLen = (int)(bounds.getWidth()/10.0);
    
    // roughly calculate the padding between title elements (assume cmd + fabric = 20 chars) (assume mode + other = 50)
    int npad = (approxTitleBarLen - 70)/2;
    
    // am I reading from a file, or not
    if(UpdateService instanceof SMT_UpdateService)
    {
      SMT_UpdateService sus = (SMT_UpdateService)UpdateService;
      toolName = sus.getToolName();
      if(OMS != null)
         fabricName = OMS.getFabricName(true);
      fileName = sus.getFile();
      portNum = sus.getPort();
      hostNam = sus.getHost();
    }
    
    String modeStr = null;
    String optsStr = null;
    
    // Mode will typically be On-Line or Off-Line
    if((fileName != null) && !(OMS_Collection.OMS_NO_FILE.equals(fileName)))
    {
      modeStr = "Off-Line";
      optsStr = fileName;
    }
    else
    {
      modeStr = "On-Line";
      optsStr = "-h " + hostNam + " -pn " + portNum;
    }
    
    StringBuffer buff = new StringBuffer();
    String TitleFormat = "%s: %s %" + npad + "s [%s] %" + npad + "s (%s)";
    buff.append(String.format(TitleFormat, toolName, fabricName, " ", modeStr, " ", optsStr));
    applicationFrame.setTitle(buff.toString());
  }

  private void removeFromWest(int index)
  {
    int ndex = westTabbedPane.getComponentCount();
    if(index < ndex)
    {
      westTabbedPane.remove(index);
    }
  }

  protected void addToSouth(JScrollPane scrollPane, boolean selected)
  {
    // add this JScrollPane to the south TabbedPanel, it should have its name set
    // because it will be used for the tab
    textTabbedPane.addTab(scrollPane.getName(), null, scrollPane, null);
    int ndex = textTabbedPane.getComponentCount() -1;
    textTabbedPane.insertClosableTab(scrollPane.getName(), null, scrollPane, null, ndex);
    
    // should this pane be brought to the front, or be selected?
    if(selected)
      textTabbedPane.setSelectedComponent(scrollPane);
  }

  protected void addToSouth(JScrollPane scrollPane)
  {
    addToSouth(scrollPane, false);
  }

  protected void addTimeSlider(TimeSliderPanel timeSliderPanel)
  {
    southPanel.add(timeSliderPanel, BorderLayout.SOUTH);
    timeSliderPanel.setVisible(true);
  }
  
  
  private void newConnectionDialog()
  {
    JButton OK = new JButton("OK");
    JButton CANCEL = new JButton("Cancel");
    JPanel north = new JPanel();
    north.add(OK);
    north.add(CANCEL);
    final JLabel label1 = new JLabel("Host Name"), label2 = new JLabel("Port Number");
    final JTextField field1 = new JTextField(12), field2 = new JTextField(12);
    final JDialog dialog = new JDialog(applicationFrame, true);
    dialog.setTitle("New OMS Connection");
    OK.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        // get the hostname and port number
        String cmd = "-h " + field1.getText() + " -pn " + field2.getText();
        Message_Mgr.postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INFO, "New Connection ["
            + cmd + "]"));
        restart(cmd);
        dialog.dispose();
      }
    });
    CANCEL.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        dialog.dispose();
      }
    });
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridwidth = gbc.REMAINDER + 10;
    gbc.insets = new Insets(5, 5, 5, 5);
    panel.add(label1, gbc);
    panel.add(field1, gbc);
    panel.add(label2, gbc);
    panel.add(field2, gbc);
    dialog.getContentPane().add(north, "South");
    dialog.getContentPane().add(panel);
    dialog.pack();
    field1.setText("localhost");
    field2.setText("10011");
    dialog.setLocation(525, 200);
    dialog.setVisible(true);
  }  
  

  /**
   * Initialize the contents of the frame.
   * 
   * Using a border layout, the frame consists of;
   * 
   *   North:  Menu and "play" area
   *   West:   Tabbed Pane, for various trees strucutres for (navigation and selection)
   *   South:  Tabbed Pane, Text Input and Output area (messages, logs, etc)
   *   East:   Not Used
   *   Center: Tabbed Pane, main body.  Graphs, charts, analysis, etc
   *   
   *   At startup;
   *     North is simple menu
   *     West is fabric tree
   *     South is message area
   *     Center is graph
   * @wbp.parser.entryPoint
   */
  private void initialize()
  {
    applicationFrame = new JFrame();
    
    Dimension maxD = getAveAvailableScreenDimension();
    
    // load persistent data here, this should come with good defaults if no prior value
    Rectangle bounds = SmtGuiPreferences.getBounds();
    applicationFrame.setBounds(bounds);
    setApplicationTitle(null, null, null);

    applicationFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    applicationFrame.addWindowListener( new WindowAdapter()
    {
      public void windowClosing(WindowEvent e)
      {
        logger.severe("Closing Gracefully");
        destroy("Done!");
      }
    });
        
    applicationFrame.getContentPane().setLayout(new BorderLayout(0, 0));
    
    JMenuBar menuBar = new JMenuBar();
    menuBar.setForeground(Color.WHITE);
    menuBar.setBackground(Color.GRAY);
    applicationFrame.getContentPane().add(menuBar, BorderLayout.NORTH);
    
    JMenu mnNewMenu = new JMenu("Fabric");
    mnNewMenu.setForeground(Color.WHITE);
    menuBar.add(mnNewMenu);
    
    JMenuItem mntmNewMenuItem = new JMenuItem("New");
    mntmNewMenuItem.addActionListener(new ActionListener() 
    {
      public void actionPerformed(ActionEvent e)
      {
        newConnectionDialog();
      }
    });
    mnNewMenu.add(mntmNewMenuItem);
    
    JMenuItem mntmNewMenuItem_1 = new JMenuItem("Open File ...");
    mntmNewMenuItem_1.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        Message_Mgr.postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INFO, "Open a File, so bring up the file chooser"));
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select an OMS History File to read");   

        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION)
        {
          File selectedFile = fileChooser.getSelectedFile();
          Message_Mgr.postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INFO, "The selected file is: (" + selectedFile.getAbsolutePath() + ")"));
          
          // test this selected file, to see if its an rH file
          if(SmtFile.isFileType(selectedFile.getAbsolutePath(), SmtProperty.SMT_OMS_COLLECTION_FILE))
            restart(selectedFile.getAbsolutePath());
          else
            Message_Mgr.postMessage(new SmtMessage(SmtMessageType.SMT_MSG_SEVERE, "The selected file: (" + selectedFile.getAbsolutePath() + ") is not an OMS History file."));
        }
      }
    });
    mnNewMenu.add(mntmNewMenuItem_1);
    
    JSeparator separator = new JSeparator();
    mnNewMenu.add(separator);
    
    JMenuItem mntmNewMenuItem_5 = new JMenuItem("Save As ...");
    mntmNewMenuItem_5.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        // skip, if this is not an On-Line session or the right kind of update
        // service
        if (UpdateService instanceof SMT_UpdateService)
        {
          SMT_UpdateService sus = (SMT_UpdateService) UpdateService;
          String fileName = sus.getFile();

          if ((fileName == null) || (OMS_Collection.OMS_NO_FILE.equals(fileName)))
          {
            // we are not in file mode, so okay to continue

            Message_Mgr.postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INFO,
                "Save the on-line session to a file"));
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save the On-Line session to a file");

            int userSelection = fileChooser.showSaveDialog(null);

            if (userSelection == JFileChooser.APPROVE_OPTION)
            {
              File fileToSave = fileChooser.getSelectedFile();
              try
              {
                Message_Mgr.postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INFO,
                    "Save to file: " + fileToSave.getAbsolutePath()));
               OMS_Collection.writeOMS_Collection(fileToSave.getAbsolutePath(),
                    sus.getCollection());
              }
              catch (IOException e1)
              {
                Message_Mgr.postMessage(new SmtMessage(SmtMessageType.SMT_MSG_SEVERE,
                    "Error Saving to file: " + fileToSave.getAbsolutePath()));
              }

            }
          }
        }
      }
    });
    mnNewMenu.add(mntmNewMenuItem_5);
    
    JSeparator separator_1 = new JSeparator();
    mnNewMenu.add(separator_1);
    
    JMenuItem mntmNewMenuItem_6 = new JMenuItem("Properties");
    mntmNewMenuItem_6.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        Message_Mgr.postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INFO, "Show the Properties of this Application & Session"));
        //       Selection_Mgr.updateAllListeners(new IB_GraphSelectionEvent(CloseTabsMenuItem, "Closing all tabs", e));
        new SmtProperties().printProperties();
      }
    });
    mnNewMenu.add(mntmNewMenuItem_6);
    
    JSeparator separator_2 = new JSeparator();
    mnNewMenu.add(separator_2);
    
    mntmHistory1 = new JMenuItem("1.");
    mntmHistory1.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) 
      {
        String cmd = mntmHistory1.getActionCommand();
        // the action command is the history information, and the text is the numbered version of same
        Message_Mgr.postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INFO, "1 Switch to this type of OMS, if possible [" + cmd + "]"));
        restart(cmd);
      }
    });
    mnNewMenu.add(mntmHistory1);
    
    mntmHistory2 = new JMenuItem("2.");
    mntmHistory2.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) 
      {
        String cmd = mntmHistory2.getActionCommand();
        // the action command is the history information, and the text is the numbered version of same
        Message_Mgr.postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INFO, "2 Switch to this type of OMS, if possible [" + cmd + "]"));
        restart(cmd);
      }
    });
    mnNewMenu.add(mntmHistory2);
    
    mntmHistory3 = new JMenuItem("3.");
    mntmHistory3.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) 
      {
        String cmd = mntmHistory3.getActionCommand();
        // the action command is the history information, and the text is the numbered version of same
        Message_Mgr.postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INFO, "3 Switch to this type of OMS, if possible [" + cmd + "]"));
        restart(cmd);
      }
    });
    mnNewMenu.add(mntmHistory3);
    
    mntmHistory4 = new JMenuItem("4.");
    mntmHistory4.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) 
      {
        String cmd = mntmHistory4.getActionCommand();
        // the action command is the history information, and the text is the numbered version of same
        Message_Mgr.postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INFO, "4 Switch to this type of OMS, if possible [" + cmd + "]"));
        restart(cmd);
      }
    });
    mnNewMenu.add(mntmHistory4);
    
    mntmHistory5 = new JMenuItem("5.");
    mntmHistory5.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) 
      {
        String cmd = mntmHistory5.getActionCommand();
        // the action command is the history information, and the text is the numbered version of same
        Message_Mgr.postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INFO, "5 Switch to this type of OMS, if possible [" + cmd + "]"));
        restart(cmd);
      }
    });
    mnNewMenu.add(mntmHistory5);
    
    JSeparator separator_3 = new JSeparator();
    mnNewMenu.add(separator_3);
    
    ExitMenuItem = new JMenuItem("Exit");
    ExitMenuItem.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        logger.severe("Closing Gracefully");
        destroy("Done!");
      }
    });
    mnNewMenu.add(ExitMenuItem);
    
    JMenu mnNewMenu_1 = new JMenu("Search");
    mnNewMenu_1.setForeground(Color.WHITE);
    menuBar.add(mnNewMenu_1);
    
    JMenuItem mntmFindAvailableOms = new JMenuItem("Find Available OMS");
    mntmFindAvailableOms.addActionListener(new ActionListener() 
    {
      public void actionPerformed(ActionEvent e)
      {
        // by default look for 7
        discoverOMS(10011, 7);
     }
    });
    mnNewMenu_1.add(mntmFindAvailableOms);
    
    JSeparator separator_5 = new JSeparator();
    mnNewMenu_1.add(separator_5);
    
    
    JMenuItem FindMenuItem = new JMenuItem("Find...");
    FindMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK));
    FindMenuItem.addActionListener(new ActionListener() 
    {
      public void actionPerformed(ActionEvent e) 
      {
        // open the search panel
        searchIdentification();
      }
    });
    mnNewMenu_1.add(FindMenuItem);
    
    JMenu mnFilter = new JMenu("Filter");
    mnFilter.setForeground(Color.WHITE);
    menuBar.add(mnFilter);
    
    JCheckBoxMenuItem chckbxmntmWhiteList = new JCheckBoxMenuItem("White LIst");
    mnFilter.add(chckbxmntmWhiteList);
    
    JCheckBoxMenuItem chckbxmntmBlackList = new JCheckBoxMenuItem("Black List");
    mnFilter.add(chckbxmntmBlackList);
    
    JMenu mnAnalyze = new JMenu("Analyze");
    mnAnalyze.setForeground(Color.WHITE);
    menuBar.add(mnAnalyze);
    
    JMenu mnTop = new JMenu("Top");
    mnAnalyze.add(mnTop);
    
    JMenuItem setMaxTopMenuItem = new JMenuItem("# top: 20");
    setMaxTopMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e)
      {
        System.err.println("Set the Max Top Here");
      }
    });
    mnTop.add(setMaxTopMenuItem);
    
    JMenu mnTraffic = new JMenu("Traffic");
    mnTop.add(mnTraffic);
    
    TNodesCBMI = new JCheckBoxMenuItem("Nodes");
    mnTraffic.add(TNodesCBMI);
    
    TLinksCBMI = new JCheckBoxMenuItem("Links");
    mnTraffic.add(TLinksCBMI);
    
    TPortsCBMI = new JCheckBoxMenuItem("Ports");
    TPortsCBMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK));
    mnTraffic.add(TPortsCBMI);
    
    JMenu mnErrors = new JMenu("Errors");
    mnTop.add(mnErrors);
    
    ENodesCBMI = new JCheckBoxMenuItem("Nodes");
    mnErrors.add(ENodesCBMI);
    
    ELinksCBMI = new JCheckBoxMenuItem("Links");
    mnErrors.add(ELinksCBMI);
    
    EPortsCBMI = new JCheckBoxMenuItem("Ports");
    EPortsCBMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK));
    mnErrors.add(EPortsCBMI);
    
    JMenu mnHeatmap = new JMenu("HeatMap");
    mnAnalyze.add(mnHeatmap);
    
    HmpCaPortsCBMI = new JCheckBoxMenuItem("CA Ports Only");
    HmpCaPortsCBMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.ALT_MASK));
    mnHeatmap.add(HmpCaPortsCBMI);
    
    HmpSwPortsCBMI = new JCheckBoxMenuItem("SW Ports Only");
    mnHeatmap.add(HmpSwPortsCBMI);
    
    HmpAllPortsCBMI = new JCheckBoxMenuItem("All Ports");
    HmpAllPortsCBMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_MASK));
    mnHeatmap.add(HmpAllPortsCBMI);
    
    JMenu mnNewMenu_2 = new JMenu("Window");
    mnNewMenu_2.setForeground(Color.WHITE);
    menuBar.add(mnNewMenu_2);
    
    CloseTabsMenuItem = new JMenuItem("Close All Tabs");
    CloseTabsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
    CloseTabsMenuItem.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        Message_Mgr.postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INFO, "Closing all tabs"));
        Selection_Mgr.updateAllListeners(new IB_GraphSelectionEvent(CloseTabsMenuItem, "Closing all tabs", e));
        removeAllFromCenter();
      }
    });
    
    mnNewMenu_2.add(CloseTabsMenuItem);
    
    JMenu mnShowFabric = new JMenu("Show Fabric");
    mnNewMenu_2.add(mnShowFabric);
    
    // share the checkbox menu items with the global popup menu
    OverviewCBMI = new JCheckBoxMenuItem("Overview");
    OverviewCBMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
    mnShowFabric.add(OverviewCBMI);
    
    GraphCBMI = new JCheckBoxMenuItem("Graph");
    GraphCBMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK));
    mnShowFabric.add(GraphCBMI);
    
    UtilizationCBMI = new JCheckBoxMenuItem("Utilization");
    UtilizationCBMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_MASK));
    mnShowFabric.add(UtilizationCBMI);
    
    RoutingCBMI = new JCheckBoxMenuItem("Routing");
    RoutingCBMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK));
    mnShowFabric.add(RoutingCBMI);
    
    DetailsCBMI = new JCheckBoxMenuItem("Details");
    DetailsCBMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK));
    mnShowFabric.add(DetailsCBMI);
    
    JMenu mnNewMenu_3 = new JMenu("Show Messages");
    mnNewMenu_2.add(mnNewMenu_3);
    
    SelectionCBMI = new JCheckBoxMenuItem("Selection Events");
    mnNewMenu_3.add(SelectionCBMI);
    
    OMSRecordCBMI = new JCheckBoxMenuItem("OMS Recording");
    mnNewMenu_3.add(OMSRecordCBMI);
    
    OMSUpdateCBMI = new JCheckBoxMenuItem("OMS Updates");
    mnNewMenu_3.add(OMSUpdateCBMI);
    
    MessageCBMI = new JCheckBoxMenuItem("Messages");
    mnNewMenu_3.add(MessageCBMI);
    
    HeartBeatCBMI = new JCheckBoxMenuItem("Heartbeat");
    mnNewMenu_3.add(HeartBeatCBMI);
    
    JSeparator separator_4 = new JSeparator();
    mnNewMenu_2.add(separator_4);
    
    JMenuItem mntmNewMenuItem_15 = new JMenuItem("Preferences");
    mnNewMenu_2.add(mntmNewMenuItem_15);
    
    
    // try to initialize the help system
    try
    {
      ClassLoader cl = SmtGuiApplication.class.getClassLoader();
      URL url = HelpSet.findHelpSet(cl, HelpSetName);
      mainHS = new HelpSet(cl, url);
      mainHB = mainHS.createHelpBroker();
      
      // set the initial bounds (size & location)
      Rectangle hBounds = SmtGuiPreferences.getHelpBounds();
      mainHB.setLocation(hBounds.getLocation());
      mainHB.setSize(hBounds.getSize());
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
    
    JMenu mnNewMenu_4 = new JMenu("Help");
    mnNewMenu_4.setForeground(Color.WHITE);
    menuBar.add(mnNewMenu_4);
    
    JMenuItem mntmSmtHelpContents = new JMenuItem("SMT Help Contents");
    mntmSmtHelpContents.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
    mntmSmtHelpContents.addActionListener(new CSH.DisplayHelpFromSource(mainHB));

    mnNewMenu_4.add(mntmSmtHelpContents);
    
    AboutMenuItem = new JMenuItem("About SMT");
    AboutMenuItem.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) 
      {
        Message_Mgr.postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INFO, "About the JAR files that SMT utilizes"));
        Selection_Mgr.updateAllListeners(new IB_GraphSelectionEvent(AboutMenuItem, AboutMenuItem.getText(), e));
      }
    });
    mnNewMenu_4.add(AboutMenuItem);
    
    
    /*****************************************************************/
    
    textTabbedPane = new JClosableTabbedPane(JTabbedPane.TOP);
        
    Dimension msgDim = new Dimension(maxD.width - (maxD.width/25), (maxD.height)/5);
    textTabbedPane.setPreferredSize(msgDim);  // the size of this panel
    
    JScrollPane scrollPaneM2 = new JScrollPane(new JEditorPane());
    scrollPaneM2.setName("message");
    
    addToSouth(scrollPaneM2);
    southPanel = new JPanel();
    applicationFrame.getContentPane().add(southPanel, BorderLayout.SOUTH);
    southPanel.setLayout(new BorderLayout(0, 0));
    southPanel.setPreferredSize(msgDim);  // the size of this panel

    southPanel.add(textTabbedPane, BorderLayout.CENTER);
    textTabbedPane.setVisible(true);
    
    // The south panel may be used for a variety of things, so hiding one or
    // more components may be desired.  This is one way to do it (false).
    textTabbedPane.setVisible(true);


    /********************************************************************/
    
    // GENERAL STRATEGY
    //
    // create the component
    // set its preferred size (normally dictated by where it will be placed?)
    // create a scrollpane, and add the component to it
    // add the scrollpane to the desired tabbedpane, as a "new tab"
    //
    // the tabbed panes are the main components in the frame, and should be placed
    // in their border locations with fixed line borders, or titled borders

    
    
    
    Dimension fabDim = new Dimension((maxD.width)/8, maxD.height - (maxD.height)/8);
    westTabbedPane = new JClosableTabbedPane(JTabbedPane.TOP);
    westTabbedPane.setPreferredSize(fabDim);  // the size of this panel
    applicationFrame.getContentPane().add(westTabbedPane, BorderLayout.WEST);

    JTree tree = new JTree();
    JScrollPane scrollPaneF = new JScrollPane(tree);
    scrollPaneF.setName("fabric tab");
    addToWest(scrollPaneF);

    centerTabbedPane = new JClosableTabbedPane(JTabbedPane.TOP);
    applicationFrame.getContentPane().add(centerTabbedPane, BorderLayout.CENTER);
    
    JTextArea textArea = new JTextArea();
    textArea.setEditable(false);
    JScrollPane scrollPaneT = new JScrollPane(textArea);
    scrollPaneT.setName("text area tab");
    addToCenter(scrollPaneT);
    
    logger.severe("Done initializing");
     
    }
  
  /************************************************************
   * Method Name:
   *  initializeFramework
  **/
  /**
   * This is essentially part two of the initialization of the application.
   * The original initialize() method simply constructed the gui, and populated
   * it with default placeholders.  It is useful for the gui builder tools, so
   * a gui can be built without connecting to the OMS.  The final initialization
   * occurs after the singleton "managers" are up and running and application
   * specific information can be obtained from them.  This is my way of binding
   * data to the gui, since I don't like the traditional data binding stuff.
   * 
   * Create initial scroll panels, and remove the default ones.  Each
   * scroll panel is responsible for "listening" for events, and publishing
   * their events.  
   *
   * @see     describe related java objects
   *
   ***********************************************************/
  public void initializeFramework(OpenSmMonitorService initialOMS)
  {
    logger.severe("Initializing the framework, hooking up managers, listeners, and gui components");
    
    if(initialOMS != null)
      setApplicationTitle(initialOMS.getFabricName(true), null, null);

    
    Analysis_Mgr  = SMT_AnalysisManager.getInstance();
    Message_Mgr   = MessageManager.getInstance();
    Tserv         = TimeService.getInstance();
    HeartBeat_Mgr = HeartBeatManager.getInstance();
    Selection_Mgr = GraphSelectionManager.getInstance();
    Route_Mgr     = SMT_RouteManager.getInstance();
    Graph_Mgr     = SMT_GraphManager.getInstance();

    Message_Mgr.postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "Initializing the gui framework, hooking up everything"));
    OMS = initialOMS;
    if(OMS == null)
    {
      logger.severe("The initial OMS is null");
      Message_Mgr.postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "ERROR: The initial OMS is null"));
    }
    else
      Route_Mgr.setOMS(OMS);
    
    // create the fabric tree panel, and put it in the west panel
    Message_Mgr.postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "Building the VertexMap for fabric: " + OMS.getFabricName()));
    LinkedHashMap<String, IB_Vertex> vertexMap = IB_Vertex.createVertexMap(OMS.getFabric());
    if (vertexMap == null)
    {
      logger.severe("The VetexMap for fabric " + OMS.getFabricName() + " could not be built");
      System.exit(-1);
    }
    Message_Mgr.postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "Building the FabricTreeModel from the VertexMap "));
    FabricTreeModel treeModel = new FabricTreeModel(vertexMap, OMS.getFabricName());

    Message_Mgr.postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "Initializing the FabricTreePanel"));
    UserObjectTreeNode root = (UserObjectTreeNode) treeModel.getRoot();
    FabricTreePanel vtp = new FabricTreePanel();
    vtp.setTreeRootNode(root);
    vtp.setRootNodePopup(getRootNodePopupMenu());
    
    // during development, use a system.err.println listener, for selection events
    // TODO remove once confident not necessary
    if(debug)
    {
    SystemErrGraphListener listener = new SystemErrGraphListener();
    Selection_Mgr.addIB_GraphSelectionListener(listener);
    }
    else
    {
      String msg = "Debugging turned off, no SystemErrGraphListener()";
      logger.warning(msg);
      Message_Mgr.postMessage(new SmtMessage(SmtMessageType.SMT_MSG_WARNING, msg));
    }

    Selection_Mgr.addIB_GraphSelectionListener(this);
    
    JScrollPane scroller = new JScrollPane(vtp);
    scroller.setName(OMS.getFabricName());
    // now add this to the west tabbed pane and remove the original placeholder
    addToWest(scroller);
    removeFromWest(0);
    
    // remove the placeholder panel
    removeFromSouth(0);
     
     return;
  }
  
  public void initUpdateService(OMS_Updater updater)
  {
    // a valid OMS_Updater is required, or we terminate
    
    UpdateService = updater;
    if((UpdateService == null))
    {
      destroy("service failed");
      return;
    }
    
    // TODO:  find & replace all getInstance() methods
    
    if(UpdateService instanceof SMT_UpdateService)
    {
      SMT_UpdateService sus = (SMT_UpdateService)UpdateService;
      String msg = "The file is (" + sus.getFile() + ")";
      MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, msg));
      msg = "The tool using this updater is (" + sus.getToolName() + ")";
      MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, msg));
            
      // get the time player, and add it to the south panel
      MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "Initializing the TimeSliderPanel"));
      TimeSliderPanel timeSliderPanel = sus.getTimePlayer();
      if(timeSliderPanel != null)
        addTimeSlider(timeSliderPanel);
      else
        logger.severe("Could not get the TimeSliderPanel from the SMT_UpdateService");
      
      // connect the analyser to the service
      sus.addListener(Analysis_Mgr);
      sus.refreshService();
     }
    
    if(OMS != null)
      setApplicationTitle(OMS.getFabricName(true), null, null);
    
  // do these last, when everything is ready to receive data
   UpdateService.addListener(this);
   
    MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "Initializing the HeartBeatManager and TimeListenerPanel"));
    Tserv = TimeService.getInstance();
    HeartBeat_Mgr = HeartBeatManager.getInstance();
    Tserv.addListener(HeartBeat_Mgr);
    UpdateService.addListener(HeartBeat_Mgr);
    UpdateService.addListener(Route_Mgr);
    
    // some of the checkbox item states are persistent, set up the listerner first, then load the states, so
    // the listeners will catch the values and perform the initilization, if any
     
    // fabric level checkbox items, see PortTreePopupMenu()
    OverviewCBMI.addItemListener(new ItemListener() 
    {
      public void itemStateChanged(ItemEvent e)
      {
      JCheckBoxMenuItem item = (JCheckBoxMenuItem)e.getSource();
      SmtGuiPreferences.setCheckBox(OverviewCBMI);
      // generate a selection event to mimic the popupmenu behavior (see handleOverviewSelected())
      GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(this, SMT_AnalysisType.SMT_FABRIC_OVERVIEW, new Boolean(item.isSelected())));
      }
    });
   
    GraphCBMI.addItemListener(new ItemListener() 
    {
      public void itemStateChanged(ItemEvent e)
      {
      JCheckBoxMenuItem item = (JCheckBoxMenuItem)e.getSource();
      SmtGuiPreferences.setCheckBox(GraphCBMI);
      // generate a selection event to mimic the popupmenu behavior (see handleGraphSelected())
      GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(this, SMT_AnalysisType.SMT_FABRIC_GRAPH, new Boolean(item.isSelected())));
      }
    });
   
    UtilizationCBMI.addItemListener(new ItemListener() 
    {
      public void itemStateChanged(ItemEvent e)
      {
      JCheckBoxMenuItem item = (JCheckBoxMenuItem)e.getSource();
      SmtGuiPreferences.setCheckBox(UtilizationCBMI);
      // generate a selection event to mimic the popupmenu behavior (see handleUtilizationSelected())
      GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(this, SMT_AnalysisType.SMT_FABRIC_UTILIZATION, new Boolean(item.isSelected())));
      }
    });
    
    RoutingCBMI.addItemListener(new ItemListener() 
    {
      public void itemStateChanged(ItemEvent e)
      {
      JCheckBoxMenuItem item = (JCheckBoxMenuItem)e.getSource();
      SmtGuiPreferences.setCheckBox(RoutingCBMI);
      // generate a selection event to mimic the popupmenu behavior (see handleRoutingSelected())
      GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(this, SMT_AnalysisType.SMT_FABRIC_ROUTING, new Boolean(item.isSelected())));
      }
    });
    
    DetailsCBMI.addItemListener(new ItemListener() 
    {
      public void itemStateChanged(ItemEvent e)
      {
      JCheckBoxMenuItem item = (JCheckBoxMenuItem)e.getSource();
      SmtGuiPreferences.setCheckBox(DetailsCBMI);
      // generate a selection event to mimic the popupmenu behavior (see handleDetailsSelected())
      GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(this, SMT_AnalysisType.SMT_FABRIC_DETAILS, new Boolean(item.isSelected())));
      }
    });
    
    // create the checkbox listeners for the text/message area panels
    // and then apply the persistent values to create (or not) them
    
    // these are the guys creating and destroying panels in the message area
    HeartBeatCBMI.addItemListener(new ItemListener() 
    {
      public void itemStateChanged(ItemEvent e)
      {
      JCheckBoxMenuItem item = (JCheckBoxMenuItem)e.getSource();
      SmtGuiPreferences.setCheckBox(HeartBeatCBMI);
      if(!item.isSelected())
      {
        // it only needs a type
        removeFromSouth(new TimeListenerPanel());
      }
      else
      {
        // only add if not already there
        TimeListenerPanel HeartBeatPanel = new TimeListenerPanel();
        HeartBeat_Mgr.addHeartBeatListener(HeartBeatPanel);
        JScrollPane scrollPaneTime = new JScrollPane(HeartBeatPanel);
        scrollPaneTime.setName("Heartbeat Watchdog");
        addToSouth(scrollPaneTime);
      }
      }
    });
    SelectionCBMI.addItemListener(new ItemListener() 
    {
      public void itemStateChanged(ItemEvent e)
      {
      JCheckBoxMenuItem item = (JCheckBoxMenuItem)e.getSource();
      SmtGuiPreferences.setCheckBox(SelectionCBMI);
       if(!item.isSelected())
      {
        // it only needs a type
        removeFromSouth(new GraphSelectionPanel());
      }
      else
      {
        // only add if not already there
        // by default, the GraphSelectionPanel is a listener, no need to add
        Message_Mgr.postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "Initializing the GraphSelectionPanel"));
        GraphSelectionPanel gsp = new GraphSelectionPanel();
        JScrollPane scrollPaneGS = new JScrollPane(gsp);
        scrollPaneGS.setName("Selection Events");
        // now add this to the bottom area and remove the original placeholder
        addToSouth(scrollPaneGS);
      }
      }
    });
    MessageCBMI.addItemListener(new ItemListener() 
    {
      public void itemStateChanged(ItemEvent e)
      {
      JCheckBoxMenuItem item = (JCheckBoxMenuItem)e.getSource();
      SmtGuiPreferences.setCheckBox(MessageCBMI);
      if(!item.isSelected())
      {
        // it only needs a type
        removeFromSouth(new MessageListenerPanel());
      }
      else
      {
        // only add if not already there
        MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "Initializing the MessageListenerPanel"));
        Message_Mgr = MessageManager.getInstance();
        MessageListenerPanel mlp = new MessageListenerPanel(true, false);
        JScrollPane scrollPaneMessage = new JScrollPane(mlp);
        scrollPaneMessage.setName("Messages");
        addToSouth(scrollPaneMessage);        
      }
      }
    });
    OMSUpdateCBMI.addItemListener(new ItemListener() 
    {
      public void itemStateChanged(ItemEvent e)
      {
      JCheckBoxMenuItem item = (JCheckBoxMenuItem)e.getSource();
      SmtGuiPreferences.setCheckBox(OMSUpdateCBMI);
      if(!item.isSelected())
      {
        // it only needs a type
        removeFromSouth(new OMS_UpdateListenerPanel());
      }
      else
      {
        // only add if not already there
        MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "Initializing the OMS_UpdateListenerPanel"));
        OMS_UpdateListenerPanel olp = new OMS_UpdateListenerPanel();
        UpdateService.addListener(olp);
        Analysis_Mgr.addSMT_AnalysisChangeListener(olp);
        JScrollPane scrollPaneOMS = new JScrollPane(olp);
        scrollPaneOMS.setName("OMS Updates");
        addToSouth(scrollPaneOMS);
        
      }
      }
    });
    OMSRecordCBMI.addItemListener(new ItemListener() 
    {
      public void itemStateChanged(ItemEvent e)
      {
      JCheckBoxMenuItem item = (JCheckBoxMenuItem)e.getSource();
      SmtGuiPreferences.setCheckBox(OMSRecordCBMI);
      if(!item.isSelected())
      {
        // it only needs a type
        removeFromSouth(new OMS_CollectionListenerPanel());
      }
      else
      {
        // only add if not already there
        MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "Initializing the CollectionListenerPanel"));
        OMS_CollectionListenerPanel oclp = new OMS_CollectionListenerPanel();
        JScrollPane scrollPaneCL = new JScrollPane(oclp);
        scrollPaneCL.setName("OMS Recording");
        addToSouth(scrollPaneCL);
        
        // give the Panel the update service so it knows its parent
        oclp.setUpdateService(UpdateService);
        
        // tell the collection that the panel wants to listen for its growth
        if(UpdateService instanceof SMT_UpdateService)
          ((SMT_UpdateService)UpdateService).getCollection().addOMS_CollectionChangeListener(oclp);
      }
      }
    });
    
    // the TOP checkbox event handlers
    TNodesCBMI.addItemListener(new ItemListener() 
    {
      public void itemStateChanged(ItemEvent e)
      {
        JCheckBoxMenuItem item = (JCheckBoxMenuItem)e.getSource();
        SmtGuiPreferences.setCheckBox(TNodesCBMI);
        // generate a selection event to handle this here (see handleTopSelected())
        GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(this, SMT_AnalysisType.SMT_FABRIC_TOP_NODES, new Boolean(item.isSelected())));
      }
    });
    TLinksCBMI.addItemListener(new ItemListener() 
    {
      public void itemStateChanged(ItemEvent e)
      {
        JCheckBoxMenuItem item = (JCheckBoxMenuItem)e.getSource();
        SmtGuiPreferences.setCheckBox(TLinksCBMI);
        // generate a selection event to handle this here (see handleTopSelected())
        GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(this, SMT_AnalysisType.SMT_FABRIC_TOP_LINKS, new Boolean(item.isSelected())));
      }
    });
    TPortsCBMI.addItemListener(new ItemListener() 
    {
      public void itemStateChanged(ItemEvent e)
      {
        JCheckBoxMenuItem item = (JCheckBoxMenuItem)e.getSource();
        SmtGuiPreferences.setCheckBox(TPortsCBMI);
        // generate a selection event to handle this here (see handleTopSelected())
        GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(this, SMT_AnalysisType.SMT_FABRIC_TOP_PORTS, new Boolean(item.isSelected())));
      }
    });
    ENodesCBMI.addItemListener(new ItemListener() 
    {
      public void itemStateChanged(ItemEvent e)
      {
        JCheckBoxMenuItem item = (JCheckBoxMenuItem)e.getSource();
        SmtGuiPreferences.setCheckBox(ENodesCBMI);
        // generate a selection event to handle this here (see handleTopSelected())
        GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(this, SMT_AnalysisType.SMT_FABRIC_ERROR_NODES, new Boolean(item.isSelected())));
      }
    });
    ELinksCBMI.addItemListener(new ItemListener() 
    {
      public void itemStateChanged(ItemEvent e)
      {
        JCheckBoxMenuItem item = (JCheckBoxMenuItem)e.getSource();
        SmtGuiPreferences.setCheckBox(ELinksCBMI);
        // generate a selection event to handle this here (see handleTopSelected())
        GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(this, SMT_AnalysisType.SMT_FABRIC_ERROR_LINKS, new Boolean(item.isSelected())));
      }
    });
    EPortsCBMI.addItemListener(new ItemListener() 
    {
      public void itemStateChanged(ItemEvent e)
      {
        JCheckBoxMenuItem item = (JCheckBoxMenuItem)e.getSource();
        SmtGuiPreferences.setCheckBox(EPortsCBMI);
        // generate a selection event to handle this here (see handleTopSelected())
        GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(this, SMT_AnalysisType.SMT_FABRIC_ERROR_PORTS, new Boolean(item.isSelected())));
      }
    });
    
    // the HEATMAP checkbox event handlers
    HmpCaPortsCBMI.addItemListener(new ItemListener() 
    {
      public void itemStateChanged(ItemEvent e)
      {
        JCheckBoxMenuItem item = (JCheckBoxMenuItem)e.getSource();
        SmtGuiPreferences.setCheckBox(HmpCaPortsCBMI);
        // generate a selection event to handle this here (see handleHeatMapSelected())
        GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(this, SMT_AnalysisType.SMT_HMAP_CA_PORTS, new Boolean(item.isSelected())));
      }
    });
    HmpSwPortsCBMI.addItemListener(new ItemListener() 
    {
      public void itemStateChanged(ItemEvent e)
      {
        JCheckBoxMenuItem item = (JCheckBoxMenuItem)e.getSource();
        SmtGuiPreferences.setCheckBox(HmpSwPortsCBMI);
        // generate a selection event to handle this here (see handleHeatMapSelected())
        GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(this, SMT_AnalysisType.SMT_HMAP_SW_PORTS, new Boolean(item.isSelected())));
      }
    });
    HmpAllPortsCBMI.addItemListener(new ItemListener() 
    {
      public void itemStateChanged(ItemEvent e)
      {
        JCheckBoxMenuItem item = (JCheckBoxMenuItem)e.getSource();
        SmtGuiPreferences.setCheckBox(HmpAllPortsCBMI);
        // generate a selection event to handle this here (see handleHeatMapSelected())
        GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(this, SMT_AnalysisType.SMT_HMAP_ALL_PORTS, new Boolean(item.isSelected())));
      }
    });

    
    // apply persistence "after" the ItemListener, so it will do the work of
    // adding or removing the panel
    loadPersistentData();
    
    // replace the center fabric pane, with one hooked up to the update service
    MessageManager.getInstance().postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "Initializing the FabricSummaryPanel"));
    GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(this, SMT_AnalysisType.SMT_FABRIC_OVERVIEW, new Boolean(true)));
    removeFromCenter(0);  // remove the dummy placeholder panel, 
  }

  
  /************************************************************
   * Method Name:
   *  restart
  **/
  /**
   * Will attempt to start a new SmtGui with the given command
   * options, and destroy the current or original SmtGui.  This
   * command is intended to be used with a list of historical or
   * previous SmtGui instances, so when one is selected, it will
   * open.
   *
   * @see     describe related java objects
   *
   * @param cmd the OMS on-line (connection) or off-line (file based)
   *            information necessary for providing the SmtGui with
   *            OMS data.
   ***********************************************************/
  public void restart(String aCmd)
  {
    logger.info("Terminating the SmtGui");
    savePersistentData();
    
    String cmd = aCmd.startsWith("-h") ? aCmd: "-rH " + aCmd;

    logger.info(cmd);
    
    final ArrayList<String> comd = new ArrayList<String>();
    // use the default script to invoke the command
    comd.add("/usr/share/java/SubnetMonitorTool/bin/smt-gui");
    
    // try to use personal/custom/current settings for remainder
    // ** only args that can be used with the above script! **
    comd.add("-rC /home/meier3/.smt/config.file");
    comd.add("-lf /home/meier3/.smt/smt-gui%u.log");
    comd.add(cmd);
    final ProcessBuilder pb = new ProcessBuilder(comd);
    try
    {
      pb.start();
    }
    catch (IOException e)
    {
      logger.severe("Couldn't start historical SmtGui with (" + cmd + ")");
      logger.severe(pb.command().toString());
      logger.severe(e.getMessage());
    }
    
    logger.severe("Ending Previous SmtGui now");
    System.exit(0);
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
    logger.info("Terminating the SmtGui");
    savePersistentData();
    logger.info(msg);
    
    // normally, I would call the destroy() methods for all the other
    // managers, but since I am exiting, I will let the JVM clean up.
    
    logger.severe("Ending now");
    System.err.println(msg);
    System.exit(0);
  }

 
  private void loadPersistentData()
  {
    logger.info("loading persistent data"); 
    // the objects must exist
    
    Rectangle r = SmtGuiPreferences.getBounds();
     
     HeartBeatCBMI = SmtGuiPreferences.getCheckBox(HeartBeatCBMI);
     SelectionCBMI = SmtGuiPreferences.getCheckBox(SelectionCBMI);
     MessageCBMI   = SmtGuiPreferences.getCheckBox(MessageCBMI);
     OMSUpdateCBMI = SmtGuiPreferences.getCheckBox(OMSUpdateCBMI);
     OMSRecordCBMI = SmtGuiPreferences.getCheckBox(OMSRecordCBMI);
     
     TNodesCBMI = SmtGuiPreferences.getCheckBox(TNodesCBMI);
     TLinksCBMI = SmtGuiPreferences.getCheckBox(TLinksCBMI);
     TPortsCBMI = SmtGuiPreferences.getCheckBox(TPortsCBMI);
     ENodesCBMI = SmtGuiPreferences.getCheckBox(ENodesCBMI);
     ELinksCBMI = SmtGuiPreferences.getCheckBox(ELinksCBMI);
     EPortsCBMI = SmtGuiPreferences.getCheckBox(EPortsCBMI);
     
     // the historical OMS Sessions
     mntmHistory1.setText("1. " + SmtGuiPreferences.getHist_1());
     mntmHistory1.setActionCommand(SmtGuiPreferences.getHist_1());
     mntmHistory2.setText("2. " + SmtGuiPreferences.getHist_2());
     mntmHistory2.setActionCommand(SmtGuiPreferences.getHist_2());
     mntmHistory3.setText("3. " + SmtGuiPreferences.getHist_3());
     mntmHistory3.setActionCommand(SmtGuiPreferences.getHist_3());
     mntmHistory4.setText("4. " + SmtGuiPreferences.getHist_4());
     mntmHistory4.setActionCommand(SmtGuiPreferences.getHist_4());
     mntmHistory5.setText("5. " + SmtGuiPreferences.getHist_5());
     mntmHistory5.setActionCommand(SmtGuiPreferences.getHist_5());
   }

  private void savePersistentData()
  {
    // save the size and location of the application
    logger.info("saving persistent data");    

    Rectangle r = applicationFrame.getBounds();
    SmtGuiPreferences.setBounds(r);
    SmtGuiPreferences.setCheckBox(HeartBeatCBMI);
    SmtGuiPreferences.setCheckBox(MessageCBMI);
    SmtGuiPreferences.setCheckBox(SelectionCBMI);
    SmtGuiPreferences.setCheckBox(OMSUpdateCBMI);
    SmtGuiPreferences.setCheckBox(OMSRecordCBMI);
    
    SmtGuiPreferences.setCheckBox(TNodesCBMI);
    SmtGuiPreferences.setCheckBox(TLinksCBMI);
    SmtGuiPreferences.setCheckBox(TPortsCBMI);
    SmtGuiPreferences.setCheckBox(ENodesCBMI);
    SmtGuiPreferences.setCheckBox(ELinksCBMI);
    SmtGuiPreferences.setCheckBox(EPortsCBMI);
    
    // set the final bounds (size & location)
    Rectangle hBounds = new Rectangle();
    hBounds.setLocation(mainHB.getLocation());
    hBounds.setSize(mainHB.getSize());
    SmtGuiPreferences.setHelpBounds(hBounds);
    
    // the historical OMS Sessions (file, or connection?)
    if(UpdateService instanceof SMT_UpdateService)
    {
      SMT_UpdateService sus = (SMT_UpdateService)UpdateService;
      String fileName = sus.getFile();
      String portNum = sus.getPort();
      String hostNam = sus.getHost();
      
      if((fileName != null) && !(OMS_Collection.OMS_NO_FILE.equals(fileName)))
      {
        SmtGuiPreferences.setHist_1(fileName);
      }
      else
      {
        SmtGuiPreferences.setHist_1("-h " + hostNam + " -pn " + portNum);
      }
    }
  }

  public void setVisible(boolean b)
  {
    applicationFrame.setVisible(b);
  }

  @Override
  public void valueChanged(IB_GraphSelectionUpdater source, IB_GraphSelectionEvent event)
  {
    // the source will always be the global graph selection manager
    // the event.source will be the parent object where the event came from
    // the event.getSelectedObject() is the object that was selected/chosen/clicked
    // the event.graphEvent is the object that was selected

    logger.severe("Selection event detected");
    boolean handled = false;
    
    if((event == null) || (event.getSelectedObject() == null) || (event.getSource() == null))
    {
      logger.severe("Selection event has NULL components, can't handle");
      return;
    }

    if ((event.getSource() instanceof JMenuItem) && !handled)
    {
      // currently, the only JMenuItem that triggers an event is the about
      logger.severe("Got a JMenuItem selection event");
      handled = handleAboutSelected(source, event);
    }

    if ((event.getContextObject() instanceof SMT_AnalysisType) && !handled)
    {
      // utilization, overview, or...
      SMT_AnalysisType sType = (SMT_AnalysisType) event.getContextObject();
      
      // create one of the various utilization panels
      if(sType.equals(SMT_AnalysisType.SMT_FABRIC_OVERVIEW))
      {
        handled = handleOverviewSelected(source, event);
        logger.severe("Done handling overview");
      }
      else if(SMT_AnalysisType.SMT_UTILIZATION_TYPES.contains(sType))
      {
      handled = handleUtilizationSelected(source, event);
      logger.severe("Done handling utilization");
      }
      else if(sType.equals(SMT_AnalysisType.SMT_ROUTE_PATH))
     {
       handled = handleRoutePathSelected(source, event);
       logger.severe("Done handling route path");
     }
      else if(sType.equals(SMT_AnalysisType.SMT_FABRIC_ROUTING))
     {
       handled = handleRoutingSelected(source, event);
       logger.severe("Done handling routing");
     }
      else if(sType.equals(SMT_AnalysisType.SMT_FABRIC_DETAILS))
     {
       handled = handleDetailsSelected(source, event);
       logger.severe("Done handling details");
     }
      else if(sType.equals(SMT_AnalysisType.SMT_FABRIC_GRAPH))
      {
      handled = handleGraphSelected(source, event);
      logger.severe("Done handling graph");
      }
      else if(SMT_AnalysisType.SMT_TOP_TYPES.contains(sType))
      {
      handled = handleTopSelected(source, event);
      logger.severe("Done handling top");
      }
      else if(SMT_AnalysisType.SMT_HMAP_TYPES.contains(sType))
      {
      handled = handleHeatMapSelected(source, event);
      logger.severe("Done handling heat map");
      }
      
      if(!handled)
        logger.severe("Unhandled SMT_Analysis Type: " + sType.getAnalysisName());

        
    }
    
    if ((event.getSelectedObject() instanceof IB_Vertex) && !handled)
    {
      handled = handleVertexSelected(source, event);
      logger.severe("Done handling vertex selected");
    }
    
    if ((event.getSelectedObject() instanceof IB_Edge) && !handled)
    {
      handled = handleLinkSelected(source, event);
      logger.severe("Done handling edge selected");
    }
    
    if ((event.getSelectedObject() instanceof OSM_Port) && !handled)
    {
      handled = handlePortSelected(source, event);
      logger.severe("Done handling port selected");
    }
    
    if ((event.getSelectedObject() instanceof UserObjectTreeNode) && !handled)
    {
      handled = handleTreeNodeSelected(source, event);
    }
    
    if ((event.getSource() instanceof JClosableTabbedPane) && !handled)
    {
      // only do this is valid
      JClosableTabbedPane tp = (JClosableTabbedPane) event.getSource();
      Integer index = (Integer) event.getSelectedObject();
      if(index < tp.getComponentCount())
        handled = handleTabClosure(source, event);
     }
    
    if ((event.getSource() instanceof SMT_SearchPanel) && !handled)
    {
      handled = handleSearchSelected(source, event);
    }
    
    // the selection manager itself can generate events, mostly happen as strings
    if ((event.getSource() instanceof GraphSelectionManager) && (event.getSelectedObject() instanceof String) && !handled)
    {
      // consume the only known string, intended for the GraphSelectionPanel (which is where the objects are stored)
      if("FLUSH".equalsIgnoreCase((String)event.getSelectedObject()))
        handled = true;
      if("REFRESH".equalsIgnoreCase((String)event.getSelectedObject()))
        handled = true;
    }
    
    if(!handled)
    {
      handled = handleUnHandledSelection(source, event);
    }
  }  
    
  private boolean handleSearchSelected(IB_GraphSelectionUpdater source, IB_GraphSelectionEvent event)
  {
    boolean handled = false;
    if(event.getSource() instanceof SMT_SearchPanel)
    {
      // the selected object should be of search result type, which contains the actual object
      SMT_SearchPanel sp = (SMT_SearchPanel)event.getSource();
      
    if(event.getSelectedObject() instanceof String)
    {
      String val = (String)event.getSelectedObject();
      int ndex = Integer.parseInt(val) -1;
//      System.err.println("The selected search object is a String (" + val + ")");
//      System.err.println("The selected search object is at index   (" + ndex + ")");
//      System.err.println("Search history size: " + SMT_SearchManager.getInstance().getSearchHistory().size());
      
      // get the selected search result from the panel
      SMT_SearchResult result = sp.getSearchResult(ndex);
      if(result != null)
      {
        SMT_SearchResultType type = result.getType();
        
        if (type == SMT_SearchResultType.SEARCH_PORT)
        {
          // generate a new PORT event, which in turn will be "handled" by the port handler
          OSM_Port p = (OSM_Port)result.getResultObject();
          GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(this, this, p));
          handled = true;
        }

        if (type == SMT_SearchResultType.SEARCH_NODE)
        {
          // generate a new Vertex event, which in turn will be "handled" by the vertex handler
          OSM_Node n = (OSM_Node)result.getResultObject();
          IB_Vertex v = null;
          
          // if the updater is an SMT_Updater, skip analysis, already done
          if(UpdateService instanceof SMT_UpdateService)
          {
            LinkedHashMap<String, IB_Vertex> vmap = ((SMT_UpdateService)UpdateService).getVertexMap();
            if((vmap != null) && (n != null))
              v = vmap.get(IB_Vertex.getVertexKey(n.getNodeGuid()));
           }
          
          if(v != null)
          {
            GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(this, this, v));
            handled = true;
          }
        }

        if (type == SMT_SearchResultType.SEARCH_RTNODE)
        {
          // generate a new RT_Node event, which in turn will be "handled" by the route tree handler
          RT_Node n = (RT_Node)result.getResultObject();
          if(n != null)
          {
            RT_Table t = n.getParentTable();
            IB_GraphSelectionEvent ev = (new IB_GraphSelectionEvent(this, t, n));
            handled = handleRouteTreeSelected(source, ev);
          }
         }

        if (type == SMT_SearchResultType.SEARCH_RTPORT)
        {
          // generate a new RT_Port event, which in turn will be "handled" by the route tree handler
          RT_Port p = (RT_Port)result.getResultObject();
          if(p != null)
          {
            RT_Node n = p.getParentNode();
            IB_GraphSelectionEvent ev = (new IB_GraphSelectionEvent(this, n, p));
            handled = handleRouteTreeSelected(source, ev);
          }
         }

        if (type == SMT_SearchResultType.SEARCH_LINK)
        {
          // generate a new Edge event, which in turn will be "handled" by the port handler
          IB_Link l = (IB_Link)result.getResultObject();
          IB_Edge e = null;
          // if the updater is an SMT_Updater, skip analysis, already done
          if(UpdateService instanceof SMT_UpdateService)
          {
            LinkedHashMap<String, IB_Edge> emap = ((SMT_UpdateService)UpdateService).getEdgeMap();
            if((emap != null) && (l != null))
              e = emap.get(IB_Edge.getEdgeKey(l));
          }
          
          if(e != null)
          {
            GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(this, this, e));
            handled = true;
          }
       }

        if (type == SMT_SearchResultType.SEARCH_CONFIG)
        {
          // pull up the configuration panel, no need to zero in on selected item
            logger.info("The Configuration (OptionMap) was selected from SEARCH");
            handled = handleOptionMapSelected(source, event);
        }
      }
     }
    }

    return handled;
  }

  private boolean discoverOMS(int startPort, int numPorts)
  {
    // attempt to discover the services by connecting, then add a panel to
    // the center area
    
    String scrollerName = "Available OMS";
    
    // only do this, if the scroll pane doesn't already exist
    if(findNamedInCenter(scrollerName) >= 0)
       return false;
    
    FabricDiscoveryPanel fd = new FabricDiscoveryPanel();
    
    JScrollPane scroller = new JScrollPane(fd);
    scroller.setName(scrollerName);
    addToCenter(scroller);
     
    // This takes time, so do it in a background worker thread
//    gov.llnl.lc.smt.manager.OMS_DiscoverWorker[] workerArray = new gov.llnl.lc.smt.manager.OMS_DiscoverWorker[numPorts];
    
    for(int n = 0; n < numPorts; n++)
    {
      int pNum = startPort + n;
      new gov.llnl.lc.smt.manager.OMS_DiscoverWorker(pNum, 1, fd).execute();
//      workerArray[n].execute();
    }
     return true;
  }
  
  private boolean searchIdentification()
  {
    // add the generic search panel, if it doesn't already exist
    String scrollerName = "Find...";
    
    // only do this, if the scroll pane doesn't already exist
    int index = findNamedInCenter(scrollerName);
    if(index >= 0)
    {
      // already exists, so get it, and bring it to focus
       setSelectedInCenter(index);
       return false;
    }
    
    SearchIdentificationPanel sip = new SearchIdentificationPanel(OMS);
    
    JScrollPane scroller = new JScrollPane(sip);
    scroller.setName(scrollerName);
    addToCenter(scroller);
     
     return true;
  }
  
  public boolean handleVertexSelected(IB_GraphSelectionUpdater source, IB_GraphSelectionEvent event)
  {
    // the source will always be the global graph selection manager
    // the event.source will be the parent object where the event came from
    // the event.graphEvent is the object that was selected

    boolean handled = false;  // return true if this method handled (consumed) this event

    if (event.getSelectedObject() instanceof IB_Vertex)
    {
      IB_Vertex myVertex = (IB_Vertex) event.getSelectedObject();

      if (myVertex == null)
      {
        // not found, oops
        logger.severe("Could not find that vertex");
      }
      // this is either a root (fake fabric vertext) vertex, or a real one
      //     check to see if it has a guid?

      // where did this vertex come from?
      if (event.getSource() instanceof FabricTreePanel)
      {
        logger.info("From the FabricTreePanel");
      }

      if (event.getSource() instanceof VertexTreePanel)
      {
        logger.info("From the VertexTreePanel");
      }

      if (myVertex.isRoot() && (myVertex.getGuid().compareTo(new IB_Guid(0)) == 0) && (OMS != null))
      {
        // this must be the fabrics root, or fake node
        
        // 3 options here, FabricSummaryPanel, FabricUtilization (SMT_AnalysisPanel), or FabricGraph
        FabricSummaryPanel fp = new FabricSummaryPanel();
        fp.init(OMS);
        fp.setRootNodePopup(getRootNodePopupMenu());
        JScrollPane scroller = new JScrollPane(fp);
        scroller.setName(OMS.getFabricName());
        if (UpdateService != null)
          UpdateService.addListener(fp);
        // now add this to the center tabbed pane
        addToCenter(scroller);
        handled = true;
       }
      else
      {
        // a normal vertex (not root), so create a VertexTreePanel, and add this
        // to center
        // also, make it a listener
        LinkedHashMap<String, OSM_Port> portMap = OMS.getFabric().getOSM_Ports();
        ArrayList<OSM_Port> pList = myVertex.getNode().getOSM_Ports(portMap);

        VertexTreeModel treeModel = new VertexTreeModel(myVertex, pList);

        UserObjectTreeNode root = (UserObjectTreeNode) treeModel.getRoot();

        if (!(event.getSource() instanceof VertexTreePanel))
        {
          VertexTreePanel vtp = new VertexTreePanel();
           vtp.setTreeModel(treeModel);
 
          JScrollPane scroller = new JScrollPane(vtp);
           scroller.setName(myVertex.getName());
          if (UpdateService != null)
            UpdateService.addListener(vtp);
          else
            logger.severe("The UpdateService appears to be null, can't add the VertexTreePanel as a listener");
          // now add this to the west tabbed pane
          addToCenter(scroller);
          handled = true;
        }

      }
    }
    return handled;
  }

  public boolean handleSystemImageGuidSelected(IB_GraphSelectionUpdater source, IB_GraphSelectionEvent event)
  {
    // the source will always be the global graph selection manager
    // the event.source will be the parent object where the event came from
    // the event.graphEvent is the object that was selected

    boolean handled = false;  // return true if this method handled (consumed) this event
    
    
    Object context  = event.getContextObject();    // this should be the IB_Vertex, or the switch node
    Object selected = event.getSelectedObject();   // this should be the UserObjectTreeNode, "sys_guid" object
    
    IB_Guid swGuid  = null;
    IB_Guid sysGuid = null;
    
    IB_Vertex sw = null;
    RT_Node  rNode = null;
    RT_Port  rPort = null;
    
    // process the context object first
    if(context != null)
    {
      if(context instanceof IB_Vertex)
      {
        // typically when an RT_Node is selected
        sw = (IB_Vertex)context;
        swGuid = sw.getGuid();
      }
      
      if(selected instanceof UserObjectTreeNode)
      {
        UserObjectTreeNode tn = (UserObjectTreeNode) selected;
//        System.err.println("A tree was selected! [" + tn.toString() + "]\n");
//        System.err.println("ChildCount [" + tn.getChildCount() + "]\n");
        
        NameValueNode vmn = (NameValueNode) tn.getUserObject();
 //       System.err.println("The name of the object is: " + vmn.getMemberName() + "\n");
        Object obj = vmn.getMemberObject();
        if(obj instanceof String)
        {
        	sysGuid = new IB_Guid((String)obj);
            // TODO FIXME - make the TreeModel and panel dynamically updatable, so that
            // I don't have to go through the Manager
            String tabName = "SYS: " + sysGuid.toColonString();
            
            // how many nodes are associated with this system image guid?
            ArrayList<IB_Guid> guidList = OMS.getFabric().getNodeGuidsForSystemGuid(sysGuid);
            
            if(guidList != null)
              logger.fine("There are " + OMS.getFabric().getNodeGuidsForSystemGuid(sysGuid).size() + " nodes for this system guid");
            else
              logger.fine("Could not find guids for system guid: " + sysGuid.toColonString());
            
            // Build the Tree Model for System Image Guid, put it in a Tree Panel, put that in a Scroll Pane (in center) and hook it up as a listener
            
            LinkedHashMap<String, IB_Vertex> vertexMap = IB_Vertex.createVertexMap(OMS.getFabric(), guidList);
            if (vertexMap == null)
            {
              logger.severe("The VetexMap for System " + sysGuid.toColonString() + " could not be built");
              System.exit(-1);
            }
            Message_Mgr.postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "Building the System Tree from the VertexMap "));
//            SystemTreeModel treeModel = new SystemTreeModel(vertexMap, sysGuid);
            SystemTreeModel treeModel = new SystemTreeModel(OMS.getFabric(), sysGuid);

            Message_Mgr.postMessage(new SmtMessage(SmtMessageType.SMT_MSG_INIT, "Initializing the SystemTreePanel"));
            SystemTreePanel vtp = new SystemTreePanel();
            vtp.setTreeModel(treeModel);
             
            JScrollPane scroller = new JScrollPane(vtp);
            scroller.setName(tabName);
            if (UpdateService != null)
              UpdateService.addListener(vtp);
            // now add this to the center tabbed pane
            addToCenter(scroller, true, true);
        }
      }

    }
    return handled;
  }

  public boolean handleAboutSelected(IB_GraphSelectionUpdater source, IB_GraphSelectionEvent event)
  {
    boolean handled = false;  // return true if this method handled (consumed) this event
    
    if (event.getSource() instanceof JMenuItem)
    {
      JMenuItem mi = (JMenuItem)event.getSource();
      if(mi == AboutMenuItem)
      {
        handled = true;
        SMT_AboutPanel about = new SMT_AboutPanel();
        
        JScrollPane scroller = new JScrollPane(about);
        scroller.setName(about.getName());
        addToCenter(scroller, true, true);
      }
    }
    return handled;
  }
  
  public boolean handleGraphSelected(IB_GraphSelectionUpdater source, IB_GraphSelectionEvent event)
  {
    boolean handled = false;  // return true if this method handled (consumed) this event

    if (event.getContextObject() instanceof SMT_AnalysisType)
    {
      // typically these events happen within a PopupMenu
      SMT_AnalysisType sType = (SMT_AnalysisType) event.getContextObject();
      
      // create the summary panel
      if(sType.equals(SMT_AnalysisType.SMT_FABRIC_GRAPH))
      {
        Object o = event.getSelectedObject();
        if((o != null) && (o instanceof Boolean))
        {
          Boolean b = (Boolean)o;  // boolean indicating to create a new one, or destroy old one
          
          this.getRootNodePopupMenu().setGraphSelected(b);
          ItemListener[] listarray = GraphCBMI.getItemListeners();
          if(listarray.length > 0)
          {
            GraphCBMI.removeItemListener(listarray[0]);
            GraphCBMI.setSelected(b);
            GraphCBMI.addItemListener(listarray[0]);
          }

          if(b)
          {
            // optimization:  see if tab by this name already exists, and don't create a new panel if not
            addFabricGraph();
           }
          else
          {
            // this handles all checkbox items see handleTabClosure()
           removeFabricGraph();
          }
           handled = true;
        }
     }
    }
    return handled;
  }

  public boolean handleUnHandledSelection(IB_GraphSelectionUpdater source, IB_GraphSelectionEvent event)
  {
    Message_Mgr.postMessage(new SmtMessage(SmtMessageType.SMT_MSG_WARNING, "Unhandled Graph Selection Event"));
    logger.warning("Unhandled Graph Selection Event");
    logger.warning("Selected Object: " + event.getSelectedObject().getClass().getName());
    logger.warning("Context Object: " + event.getContextObject().getClass().getName());
    logger.warning("Source: " + event.getSource().getClass().getName());
    return true;
  }

  public boolean handleOptionMapSelected(IB_GraphSelectionUpdater source,
      IB_GraphSelectionEvent event)
  {
    boolean handled = false; // return true if this method handled (consumed)
                             // this event

    // from the details panel, or the search panel

    if ((event.getSource() instanceof SubnetTreePanel)
        || (event.getSource() instanceof SMT_SearchPanel))
    {
      // create the option panel
      String tabName = "C: " + OMS.getFabricName();

      OptionMapTreeModel model = OptionMapTreePanel.getTreeModel(OMS);
      OptionMapTreePanel omtp = new OptionMapTreePanel();
      omtp.setTreeModel(model);
      omtp.setRootNodePopup(getRootNodePopupMenu());

      JScrollPane scroller = new JScrollPane(omtp);
      scroller.setName(tabName);
      if (UpdateService != null)
        UpdateService.addListener(omtp);

      // now add this to the center tabbed pane
      addToCenter(scroller, true, true);
      handled = true;
    }
    return handled;
  }

  public boolean handleDetailsSelected(IB_GraphSelectionUpdater source, IB_GraphSelectionEvent event)
  {
    boolean handled = false;  // return true if this method handled (consumed) this event

    if (event.getContextObject() instanceof SMT_AnalysisType)
    {
      // typically these events happen within a PopupMenu
      SMT_AnalysisType sType = (SMT_AnalysisType) event.getContextObject();
      
      // create the summary panel
      if(sType.equals(SMT_AnalysisType.SMT_FABRIC_DETAILS))
      {
        String tabName = "D: " + OMS.getFabricName();

        Object o = event.getSelectedObject();
        if((o != null) && (o instanceof Boolean))
        {
          Boolean b = (Boolean)o;  // boolean indicating to create a new one, or destroy old one
          
          this.getRootNodePopupMenu().setDetailsSelected(b);
          ItemListener[] listarray = DetailsCBMI.getItemListeners();
          if(listarray.length > 0)
          {
            DetailsCBMI.removeItemListener(listarray[0]);
            DetailsCBMI.setSelected(b);
            DetailsCBMI.addItemListener(listarray[0]);
          }

          if(b)
          {
            // optimization:  see if tab by this name already exists, and don't create a new panel if not
            SubnetTreeModel model = SubnetTreePanel.getTreeModel(OMS);
            SubnetTreePanel stp = new SubnetTreePanel();
            stp.setTreeModel(model);
            stp.setRootNodePopup(getRootNodePopupMenu());
            
            JScrollPane scroller = new JScrollPane(stp);
            scroller.setName(tabName);
            if (UpdateService != null)
              UpdateService.addListener(stp);
            // now add this to the center tabbed pane
            addToCenter(scroller, true, true);
          }
          else
          {
            // this handles all checkbox items see handleTabClosure()
            this.removeNamedFromCenter(tabName);
          }
           handled = true;
        }
     }
    }
    return handled;
  }

  public boolean handleRoutingSelected(IB_GraphSelectionUpdater source, IB_GraphSelectionEvent event)
  {
    boolean handled = false;  // return true if this method handled (consumed) this event

    if (event.getContextObject() instanceof SMT_AnalysisType)
    {
      // typically these events happen within a PopupMenu
      SMT_AnalysisType sType = (SMT_AnalysisType) event.getContextObject();
      
      // create the summary panel
      if(sType.equals(SMT_AnalysisType.SMT_FABRIC_ROUTING))
      {
        String tabName = "R: " + OMS.getFabricName();

        Object o = event.getSelectedObject();
        if((o != null) && (o instanceof Boolean))
        {
          Boolean b = (Boolean)o;  // boolean indicating to create a new one, or destroy old one
          
          this.getRootNodePopupMenu().setRoutingSelected(b);
          ItemListener[] listarray = RoutingCBMI.getItemListeners();
          if(listarray.length > 0)
          {
            RoutingCBMI.removeItemListener(listarray[0]);
            RoutingCBMI.setSelected(b);
            RoutingCBMI.addItemListener(listarray[0]);
          }

          if(b)
          {
            // if the updater is an SMT_Updater, there may be routing information
            if(UpdateService instanceof SMT_UpdateService)
            {
              // the updater may or may not have the table yet
              SMT_UpdateService sus = ((SMT_UpdateService)UpdateService);
              Route_Mgr.getRouteTable();
              if(!Route_Mgr.isEnabled())
              {
                // hmmmm
              logger.severe("Sorry, routing table not available");
              Message_Mgr.postMessage(new SmtMessage(SmtMessageType.SMT_MSG_WARNING, "Sorry, the routing table is not available (it seems to be null for: " + OMS.getFabricName() + ")"));

              }
              else
              {
                // TODO FIXME - make the TreeModel and panel dynamically updatable, so that
                // I don't have to go through the Manager
                RT_Table rtable = Route_Mgr.getRouteTable();
                RT_TableTreeModel model = new RT_TableTreeModel(rtable, OMS.getFabric());
                RT_TableTreePanel stp = new RT_TableTreePanel();
                stp.setTreeModel(model);
                
                stp.setRootNodePopup(getRootNodePopupMenu());
                
                JScrollPane scroller = new JScrollPane(stp);
                scroller.setName(tabName);
                if (UpdateService != null)
                  UpdateService.addListener(stp);
                // now add this to the center tabbed pane
                addToCenter(scroller, true, true);
              }
            }
            else
              logger.severe("Router Tables unavailable in this type of UpdateService");
          }
          else
          {
            // this handles all checkbox items see handleTabClosure()
            this.removeNamedFromCenter(tabName);
          }
           handled = true;
        }
     }
    }
    return handled;
  }

  public boolean handleRoutePathSelected(IB_GraphSelectionUpdater source, IB_GraphSelectionEvent event)
  {
    // the source will always be the global graph selection manager
    // the event.source will be the parent object where the event came from
    // the event.graphEvent is the object that was selected

    boolean handled = false;  // return true if this method handled (consumed) this event

    if (event.getSelectedObject() instanceof RT_Path)
    {
      RT_Path myPath = (RT_Path) event.getSelectedObject();

      if (myPath == null)
      {
        // not found, oops
        logger.severe("Could not find that path");
      }
      // this is either a root (fake fabric vertext) vertex, or a real one
      //     check to see if it has a guid?

      // where did this vertex come from?
      if (event.getSource() instanceof FabricTreePanel)
      {
        logger.info("From the FabricTreePanel");
      }

      if (event.getSource() instanceof VertexTreePanel)
      {
        logger.info("From the VertexTreePanel");
      }

        // a normal path, so create an RT_PathTreePanel, and add this
        // to center
        // also, make it a listener
      
      RT_PathTreeModel model = new RT_PathTreeModel(myPath, true);
      RT_PathTreePanel vtp = new RT_PathTreePanel();
      GraphSelectionManager.getInstance().addIB_GraphSelectionListener(vtp);
      vtp.setTreeModel(model);
      JScrollPane scroller = new JScrollPane(vtp);
      scroller.setName(myPath.getPathIdString());
      
//     if (UpdateService != null)
//       UpdateService.addListener(vtp);
//     else
//       logger.severe("The UpdateService appears to be null, can't add the VertexTreePanel as a listener");
     
     addToCenter(scroller);
     handled = true;
    }
    return handled;
  }

  public boolean handleOverviewSelected(IB_GraphSelectionUpdater source, IB_GraphSelectionEvent event)
  {
    boolean handled = false;  // return true if this method handled (consumed) this event

    if (event.getContextObject() instanceof SMT_AnalysisType)
    {
      // typically these events happen within a PopupMenu
      SMT_AnalysisType sType = (SMT_AnalysisType) event.getContextObject();
      
      // create the summary panel
      if(sType.equals(SMT_AnalysisType.SMT_FABRIC_OVERVIEW))
      {
        String tabName = OMS.getFabricName();

        Object o = event.getSelectedObject();
        if((o != null) && (o instanceof Boolean))
        {
          Boolean b = (Boolean)o;  // boolean indicating to create a new one, or destroy old one
          
          this.getRootNodePopupMenu().setOverviewSelected(b);
          ItemListener[] listarray = OverviewCBMI.getItemListeners();
          if(listarray.length > 0)
          {
            OverviewCBMI.removeItemListener(listarray[0]);
            OverviewCBMI.setSelected(b);
            OverviewCBMI.addItemListener(listarray[0]);
          }

          if(b)
          {
            // optimization:  see if tab by this name already exists, and don't create a new panel if not
            FabricSummaryPanel fp = new FabricSummaryPanel();
            fp.init(OMS);
            fp.setRootNodePopup(getRootNodePopupMenu());
            JScrollPane scroller = new JScrollPane(fp);
            scroller.setName(OMS.getFabricName());
            if (UpdateService != null)
              UpdateService.addListener(fp);
            // now add this to the center tabbed pane
            addToCenter(scroller, true, true);
          }
          else
          {
            // this handles all checkbox items see handleTabClosure()
            this.removeNamedFromCenter(tabName);
          }
           handled = true;
        }
     }
    }
    return handled;
  }

  public boolean handleUtilizationSelected(IB_GraphSelectionUpdater source, IB_GraphSelectionEvent event)
  {
    boolean handled = false;  // return true if this method handled (consumed) this event

    if (event.getContextObject() instanceof SMT_AnalysisType)
    {
      // typically these events happen within a PopupMenu
      SMT_AnalysisType sType = (SMT_AnalysisType) event.getContextObject();
      
      // create one of the various utilization panels
      if(sType.equals(SMT_AnalysisType.SMT_FABRIC_UTILIZATION))
      {
        Object o = event.getSelectedObject();
        if((o != null) && (o instanceof Boolean))
        {
          Boolean b = (Boolean)o;  // boolean indicating to create a new one, or destroy old one
          String tabName = "U: " + OMS.getFabricName();
          
          // if true, open a utilization tab ONLY IF one does not already exist.
          // if false, close ALL utiliztion tabs (should only have one, but check for more)
          
          // don't know the true source of this event, make sure the popupmenu is set accordingly
          this.getRootNodePopupMenu().setUtilizationSelected(b);

          if(b)
          {
            // create the panel
            SMT_AnalysisPanel sap = new SMT_AnalysisPanel(SMT_AnalysisType.SMT_FABRIC_UTILIZATION);
            Analysis_Mgr.addSMT_AnalysisChangeListener(sap);
            JScrollPane scrollPaneAnal = new JScrollPane(sap);
            // this should be the name of the fabric
            scrollPaneAnal.setName(tabName);
            // only need one, don't duplicate
            this.addToCenter(scrollPaneAnal, true, true);
            
            // finally, make sure the menu checkbox is set true (without invoking events)
            ItemListener[] listarray = UtilizationCBMI.getItemListeners();
            if(listarray.length > 0)
            {
              UtilizationCBMI.removeItemListener(listarray[0]);
              UtilizationCBMI.setSelected(true);
              UtilizationCBMI.addItemListener(listarray[0]);
            }
          }
          else
          {
            // this handles all checkbox items see handleTabClosure()
            this.removeNamedFromCenter(tabName);
          }
          handled = true;
        }
     }
      
      // create one of the various utilization panels
      else if(sType.equals(SMT_AnalysisType.SMT_NODE_UTILIZATION))
      {
        // attempt to create a guid for the analysis panel
        String guidString = (String)event.getSelectedObject();
        IB_Guid guid = new IB_Guid(guidString);
        SMT_AnalysisPanel sap = new SMT_AnalysisPanel(SMT_AnalysisType.SMT_NODE_UTILIZATION, guid);
        Analysis_Mgr.addSMT_AnalysisChangeListener(sap);
        JScrollPane scrollPaneAnal = new JScrollPane(sap);
        
        String tabName = "U: " + guidString;
        // attempt to find the vertex for this node/guid
        IB_Vertex myVertex = sap.getIB_Vertex();
        if(myVertex != null)
          tabName = "U: " + myVertex.getName();
        
       // this should be the name of the node
        scrollPaneAnal.setName(tabName);
        this.addToCenter(scrollPaneAnal);
        handled = true;
     }
      
      // create one of the various utilization panels
      else if(sType.equals(SMT_AnalysisType.SMT_PORT_UTILIZATION))
      {
        String portAddress = (String)event.getSelectedObject();
        // the first part is the guid, and the final part is the port number
        
        int ndex = portAddress.lastIndexOf(":");
        String guidString = portAddress.substring(0, ndex);
//        System.err.println("The port number is: " + portAddress.substring(ndex+1));
        int portNum = Integer.parseInt(portAddress.substring(ndex+1));
        IB_Guid guid = new IB_Guid(guidString);
        SMT_AnalysisPanel sap = new SMT_AnalysisPanel(SMT_AnalysisType.SMT_PORT_UTILIZATION, guid, portNum);
        Analysis_Mgr.addSMT_AnalysisChangeListener(sap);
        JScrollPane scrollPaneAnal = new JScrollPane(sap);
        
        String tabName = "U: " + portAddress;
        // attempt to find the vertex for this node/guid
        IB_Vertex myVertex = sap.getIB_Vertex();
        if(myVertex != null)
          tabName = "U: " + myVertex.getName() + ":" + portNum;
        
       // this should be the name of the node plus port number
        scrollPaneAnal.setName(tabName);
        this.addToCenter(scrollPaneAnal);
        handled = true;
     }
      
      else if(sType.equals(SMT_AnalysisType.SMT_ROUTE_UTILIZATION))
      {
        // this can occur in a couple of different spots, and the type
        // of the selected object determines what to do
        if(event.getSelectedObject() instanceof RT_Path)
        {
        RT_Path path = (RT_Path)event.getSelectedObject();
//        System.err.println("Put Route Utilization Panel here: " + path.getPathIdString());
        
        SMT_AnalysisPanel sap = new SMT_AnalysisPanel(SMT_AnalysisType.SMT_ROUTE_UTILIZATION, path);
        Analysis_Mgr.addSMT_AnalysisChangeListener(sap);
        JScrollPane scrollPaneAnal = new JScrollPane(sap);
        
        String tabName = "U: " + path.getPathIdString();
       scrollPaneAnal.setName(tabName);
       this.addToCenter(scrollPaneAnal);
        handled = true;
        }
        else if(event.getSelectedObject() instanceof String)
        {
          // TODO - this should be a guid:pn from the hop table
          //        go to link or port
          String portIdString = (String)event.getSelectedObject();
          System.err.println("Recieved String (" + portIdString + ") Route Utilization");
          OSM_Port p = OMS.getFabric().getOSM_PortByNodeString(portIdString);
          
          if(p != null)
          {
            int portNum = p.getPortNumber();
            IB_Guid g = p.getNodeGuid();
            IB_Vertex myVertex = Analysis_Mgr.getDeltaAnalysis().getIB_Vertex(g);
            
            
            if((myVertex != null) && (portNum > -1))
            {
                // create the model, then the panel, then put it in the scroll pane
                PortTreeModel treeModel = new PortTreeModel(myVertex, portNum);
                if(treeModel.isLink())
                {
                  if (!(event.getSource() instanceof PortTreePanel))
                  {
                    PortTreePanel ptp = new PortTreePanel();
                    ptp.setTreeModel(treeModel);

                    JScrollPane scroller = new JScrollPane(ptp);
                    scroller.setName(myVertex.getName() + ":" + portNum);
                    if (UpdateService != null)
                      UpdateService.addListener(ptp);
                    // now add this to the west tabbed pane
                    addToCenter(scroller);
                    handled = true;
                  }
                }
                else
                {
                  logger.warning("Cannot expand this ports tree model");
                  handled = true;
                }
            }
          }
          
        }
        else
          logger.warning("Unknown event from Route Utilization");
     }
      
      // create one of the various utilization panels
      else if(sType.equals(SMT_AnalysisType.SMT_LINK_UTILIZATION))
      {
        IB_Edge edge = (IB_Edge)event.getSelectedObject();
 //        System.err.println("The link is: " + edge.getKey());
 
        SMT_AnalysisPanel sap = new SMT_AnalysisPanel(SMT_AnalysisType.SMT_LINK_UTILIZATION, edge);
        Analysis_Mgr.addSMT_AnalysisChangeListener(sap);
        JScrollPane scrollPaneAnal = new JScrollPane(sap);
        
        String tabName = "U: " + edge.toEdgeIdString(32);
       scrollPaneAnal.setName(tabName);
       this.addToCenter(scrollPaneAnal);
        handled = true;
     }
      
      handled = true;
    }
    return handled;
  }

  public boolean handleTopSelected(IB_GraphSelectionUpdater source, IB_GraphSelectionEvent event)
  {
    boolean handled = false;  // return true if this method handled (consumed) this event

    if (event.getContextObject() instanceof SMT_AnalysisType)
    {
      // typically these events happen within a top table
      SMT_AnalysisType sType = (SMT_AnalysisType) event.getContextObject();
      String selectionDescription = "";  // length needs to be empty
      Boolean addTopPanel = false;
      
      // a node, port, or link has been selected from one of the top panels
      // create or find the object, then trigger a generic selection event
      // so the corresponding objects tree can be displayed, for examination
      
      Object o = event.getSelectedObject();
      if((o != null) && (o instanceof Boolean))
        addTopPanel = (Boolean)o;  // boolean indicating to create a new one, or destroy old one
      
      if((o != null) && (o instanceof String))
        selectionDescription = (String)o;  // a string, which represents a link, node, or port
      
//      logger.severe("The selection description is: (" + selectionDescription + ")");
//      logger.severe("The analysis type is: (" + sType.getAnalysisName() + ")");

       // create one of the various top panels
      if(SMT_AnalysisType.SMT_TOP_TYPES.contains(sType))
       {
        String tabName = sType.getName();  // this needs to be unique among all tabs in the center pane
        
        if(addTopPanel)
        {
          SMT_AnalysisPanel sap = new SMT_AnalysisPanel(sType);
          Analysis_Mgr.addSMT_AnalysisChangeListener(sap);
          JScrollPane scrollPaneAnal = new JScrollPane(sap);
           
         // this should be the name of the node
          scrollPaneAnal.setName(tabName);
          Boolean b = this.addToCenter(scrollPaneAnal, true, true);
          handled = true;
        }
        else if(selectionDescription.length() > 2)
        {
          // attempt to get port num and guid
          int portNdex = selectionDescription.indexOf(':',19);
          
          if(portNdex > 1)
          {
            // found a colon past 19, so must be a link or port
            String gString = selectionDescription.substring(0, portNdex);
            String pString = selectionDescription.substring(portNdex+1);
            int pn = Integer.parseInt(pString);

            IB_Guid g = new IB_Guid(gString);
            IB_Vertex v = Analysis_Mgr.getDeltaAnalysis().getIB_Vertex(g);
            IB_Edge   e = Analysis_Mgr.getDeltaAnalysis().getIB_Edge(g, pn);
            OSM_Port  p = null;
            if(v != null)
              p = v.getOSM_Port(pn);
            logger.severe("A link or a port");
            
            // generate another selection event, which should trigger a link or port tree panel
            if((sType.equals(SMT_AnalysisType.SMT_FABRIC_TOP_PORTS)) || (sType.equals(SMT_AnalysisType.SMT_FABRIC_ERROR_PORTS)))
            {
              // if the port is down, then trigger the node to be displayed, so you can see its down
              // otherwise, do the normal thing, and trigger the port event
              if(p!= null)
               GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(this, this, p));
              else
              {
                String msg = "The selected port was null (down??), so showing its parent node instead";
                logger.warning(msg);
                Message_Mgr.postMessage(new SmtMessage(SmtMessageType.SMT_MSG_WARNING, msg));
                GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(this, this, v));
              }
            }
            else if((sType.equals(SMT_AnalysisType.SMT_FABRIC_TOP_LINKS)) || (sType.equals(SMT_AnalysisType.SMT_FABRIC_ERROR_LINKS)))
              GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(this, this, e));
            else
              logger.severe("Could not handle port or link for type: " + sType.getAnalysisName());
          }
          else
          {
            // no extra colon, so must be a guid
            IB_Guid g = new IB_Guid(selectionDescription);
            IB_Vertex v = Analysis_Mgr.getDeltaAnalysis().getIB_Vertex(g);
            
            logger.severe("Just a node, only a guid");
            
           // generate another selection event, which should trigger the vertex panel
           if(v != null)
             GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(this, this, v));
          }
          
          }
        else
        {
          // attempt to remove
          Boolean b = removeNamedFromCenter(tabName);
          handled = true;
         }
     }
      else
        logger.severe("This TOP Type not handled yet");
       
      handled = true;
    }
    
    
    return handled;
  }

  public boolean handleHeatMapSelected(IB_GraphSelectionUpdater source, IB_GraphSelectionEvent event)
  {
    boolean handled = false;  // return true if this method handled (consumed) this event

    if (event.getContextObject() instanceof SMT_AnalysisType)
    {
      // typically these events happen within the heatmap menu
      SMT_AnalysisType sType = (SMT_AnalysisType) event.getContextObject();
      String selectionDescription = "";  // length needs to be empty
      Boolean addHeatMapPanel = false;
      
      // the selected event should be a boolean (add or remove the panel)
       
      Object o = event.getSelectedObject();
      if((o != null) && (o instanceof Boolean))
        addHeatMapPanel = (Boolean)o;  // boolean indicating to create a new one, or destroy old one
      
       // create one of the various heat map panels
      if(SMT_AnalysisType.SMT_HMAP_TYPES.contains(sType))
       {
        String tabName = sType.getName();  // this needs to be unique among all tabs in the center pane
        
        if(addHeatMapPanel)
        {
          SMT_AnalysisPanel sap = new SMT_AnalysisPanel(sType);
          if(SMT_AnalysisType.SMT_HMAP_ALL_PORTS.equals(sType))
          {
            sap = new SMT_AnalysisPanel(sType, IB_Depth.IBD_ALL_LEVELS);
          }
          else if(SMT_AnalysisType.SMT_HMAP_CA_PORTS.equals(sType))
          {
            sap = new SMT_AnalysisPanel(sType, IB_Depth.IBD_COMPUTE_NODES);
          }
          else if(SMT_AnalysisType.SMT_HMAP_SW_PORTS.equals(sType))
          {
            sap = new SMT_AnalysisPanel(sType, IB_Depth.IBD_SWITCH_NODES);
          }
          Analysis_Mgr.addSMT_AnalysisChangeListener(sap);
          JScrollPane scrollPaneAnal = new JScrollPane(sap);
           
         // this should be the name of the heat map
          scrollPaneAnal.setName(tabName);
          Boolean b = this.addToCenter(scrollPaneAnal, true, true);
          handled = true;
        }
        else
        {
          // attempt to remove
          Boolean b = removeNamedFromCenter(tabName);
          handled = true;
         }
     }
      else
        logger.severe("This HeatMap Type not handled yet");
       
      handled = true;
    }
    
    
    return handled;
  }

  public boolean handleTreeNodeSelected(IB_GraphSelectionUpdater source, IB_GraphSelectionEvent event)
  {
    // the source will always be the global graph selection manager
    // the event.source will be the parent object where the event came from
    // the event.graphEvent is the object that was selected

    // from within the VertexTree, a port can be selected, and if so
    // it should display the PortTreePanel
    
    boolean handled = false;  // return true if this method handled (consumed) this event

    if (event.getSelectedObject() instanceof UserObjectTreeNode)
    {
      // typically these events happen within the VertexTreePanel
      UserObjectTreeNode tn = (UserObjectTreeNode) event.getSelectedObject();
      NameValueNode vmn = (NameValueNode) tn.getUserObject();
      PortCounterName pcn = PortCounterName.getByName(vmn.getMemberName());

      if (vmn.getMemberName().equals("port #"))
      {
        handled = handlePortSelected(source, event);
      }
      else if (vmn.getMemberName().startsWith("sw table"))
      {
        logger.info("a switch table was selected");
        handled = handleRouteTreeSelected(source, event);
      }
      else if (vmn.getMemberName().startsWith("routes for port #"))
      {
        logger.info("a port route table was selected");
        handled = handleRouteTreeSelected(source, event);
      }
      else if (vmn.getMemberName().startsWith("endport"))
      {
        logger.info("An endport was selected");
        handled = handlePortSelected(source, event);
      }
      else if (vmn.getMemberName().equals("sys_guid"))
      {
        handled = handleSystemImageGuidSelected(source, event);
      }
//      else if (vmn.getMemberName().equals("link >"))
//      {
//        logger.info("A link > was selected");
//        handled = handleLinkSelected(source, event);
//      }
      else if (vmn.getMemberName().equals("link >"))
      {
        logger.info("A link > was selected");
        handled = handleLinkSelected(source, event);
      }
      else if (vmn.getMemberName().equals("link <"))
      {
        logger.info("A link < was selected");
        handled = handleLinkSelected(source, event);
     }
      else if (vmn.getMemberName().equals("Configuration"))
      {
        logger.info("The Configuration (OptionMap) was selected");
        handled = handleOptionMapSelected(source, event);
     }
      else if ((pcn != null) && PortCounterName.PFM_ALL_COUNTERS.contains(pcn))
      {
        logger.info("A counter was selected, and handled elsewhere");
        handled = true;
      }
      else
        logger.warning("Member (" + vmn.getMemberName() + ") was selected, but not handled yet");
          
      // TODO, what kind of object is this, and what to do with it?
    }
    return handled;
  }

  private boolean handleRouteTreeSelected(IB_GraphSelectionUpdater source, IB_GraphSelectionEvent event)
  {
    boolean handled = false;  // return true if this method handled (consumed) this event
    
    event.getSource();           // this should be the RT_TableTreePanel
    event.getContextObject();    // this should be the RT_Table, RT_Node, or RT_Port
    event.getSelectedObject();   // this should be the UserObjectTreeNode, or the actual object
    
    IB_Guid swGuid = null;
    
    RT_Table table = null;
    RT_Node  rNode = null;
    RT_Port  rPort = null;
    
    // process the context object first
    if(event.getContextObject() != null)
    {
      if(event.getContextObject() instanceof RT_Table)
      {
        // typically when an RT_Node is selected
        table = (RT_Table)event.getContextObject();
      }
      else if(event.getContextObject() instanceof RT_Node)
      {
        // typically when an RT_Path is selected
        rNode = (RT_Node)event.getContextObject();
        if(rNode != null)
          table = rNode.getParentTable();

      }
      else if(event.getContextObject() instanceof RT_Port)
      {
        System.err.println("I don't think anything ever reaches this DEAD CODE??");
        rPort = (RT_Port)event.getContextObject();
        if(rPort != null)
        {
          rNode = rPort.getParentNode();
          if(rNode != null)
            table = rNode.getParentTable();
        }
      }
    }
      
    if ((event.getSelectedObject() instanceof UserObjectTreeNode) || (event.getSelectedObject() instanceof RT_Node) || (event.getSelectedObject() instanceof RT_Port))
    {
      if (event.getSelectedObject() instanceof UserObjectTreeNode)
      {
        // if here, a node or port route tree was selected from a gui tree type panel

        UserObjectTreeNode tn = (UserObjectTreeNode) event.getSelectedObject();
        NameValueNode vmn = (NameValueNode) tn.getUserObject();
        if (vmn.getMemberName().equals("sw table"))
        {
           RT_Node rn = (RT_Node)vmn.getMemberObject();
          if(rn != null)
          {
            swGuid = rn.getGuid();
            rn.getAveRoutes();
          }
          else
          {
            System.err.println("Could not convert to an RT_Node");
            System.err.println(vmn.getMemberObject().getClass().getName());
          }
        }
        else if (vmn.getMemberName().startsWith("routes for port #"))
        {
           rPort = (RT_Port)vmn.getMemberObject();
          if(rPort != null)
          {
//            System.out.println("Converted!!");
          }
          else
          {
            System.err.println("Could not convert to an RT_Port");
            System.err.println(vmn.getMemberObject().getClass().getName());
          }
        }
        else
        {
          System.err.println("NOT a sw_table");
          System.err.println(vmn.getMemberName());
          System.err.println(event.getContextObject().getClass().getName());

        }
        
      }
      else if (event.getSelectedObject() instanceof RT_Node)
      {
        // if here, an rt_node was directly selected (probably search panel)
        rNode = (RT_Node)event.getSelectedObject();
        swGuid = rNode.getGuid();
       }
      else if (event.getSelectedObject() instanceof RT_Port)
      {
        // if here, an rt_port was directly selected (probably search panel)
        rPort = (RT_Port)event.getSelectedObject();
       }
      else
      {
        System.err.println("This is UNKNOWN");
        logger.severe("unknown selection while handlingRouteTreeSelection");
        return true;
      }
      
      // Should have enough information now to TAKE ACTION
      if(rPort != null)
      {
        // TODO FIXME - make the TreeModel and panel dynamically updatable, so that
       // I don't have to go through the Manager
       RT_PortTreeModel rptm = new RT_PortTreeModel(rPort, table, OMS.getFabric());
       
       // create a panel for the model
       if(rptm != null)
       {
         RT_PortTreePanel ptp = new RT_PortTreePanel();
         ptp.setTreeModel(rptm);
 
         JScrollPane scroller = new JScrollPane(ptp);
         String tabName = "PR: " + rptm.getParentNode().getName(OMS.getFabric()) + ":" + rPort.getPortNumber();

         scroller.setName(tabName);
 //              if (UpdateService != null)
 //                UpdateService.addListener(ptp);
         // now add this to the center tabbed pane
         addToCenter(scroller);
         handled = true;
       }
      }
      else if((swGuid != null) && (table != null))
      {
         // TODO FIXME - make the TreeModel and panel dynamically updatable, so that
        // I don't have to go through the Manager
        RT_NodeTreeModel rptm = new RT_NodeTreeModel(swGuid, table, OMS.getFabric());
        
        // create a panel for the model
        if(rptm != null)
        {
          RT_NodeTreePanel ptp = new RT_NodeTreePanel();
          ptp.setTreeModel(rptm);
  
          JScrollPane scroller = new JScrollPane(ptp);
          String tabName = "SR: " + rptm.getRootRtNode().getName(OMS.getFabric());

          scroller.setName(tabName);
  //              if (UpdateService != null)
  //                UpdateService.addListener(ptp);
          // now add this to the center tabbed pane
          addToCenter(scroller);
          handled = true;
        }
      }
      else
        System.err.println("I have a bad guid: ");

     }
    return handled;
  }

  public boolean handleLinkSelected(IB_GraphSelectionUpdater source, IB_GraphSelectionEvent event)
  {
    // the source will always be the global graph selection manager
    // the event.source will be the parent object where the event came from
    // the event.graphEvent is the object that was selected

    // from within the VertexTree, a port can be selected, and then a link, and if so
    // it should display the LinkTreePanel
    
    // from within the PortTree, a link can be selected, and if so
    // it should display the LinkTreePanel
    
    // from within the top tables, a link can be selected, and if so
    // the associated LinkTreePanel should be displayed
    
    boolean handled = false;  // return true if this method handled (consumed) this event

    if ((event.getSelectedObject() instanceof UserObjectTreeNode) || (event.getSelectedObject() instanceof IB_Edge))
    {
      // need a vertex and an edgekey
      IB_Vertex myVertex = null;
      int portNum = -1;
      
      if (event.getSelectedObject() instanceof UserObjectTreeNode)
      {
        UserObjectTreeNode tn = (UserObjectTreeNode) event.getSelectedObject();
        NameValueNode vmn = (NameValueNode) tn.getUserObject();
        if (vmn.getMemberName().startsWith("link"))
        {
          if(tn.getParent() instanceof UserObjectTreeNode)
          {
            UserObjectTreeNode tpn = (UserObjectTreeNode) tn.getParent();
            // this ought to be a port
            NameValueNode pmn = (NameValueNode) tpn.getUserObject();
            if (pmn.getMemberName().equals("port #"))
            {
              OSM_Port p = (OSM_Port)pmn.getMemberObject();
              if(p != null)
                portNum = p.getPortNumber();
            }
          
          }
        }
        if (event.getContextObject() instanceof IB_Vertex)
        {
          // from either the VertexTreeModel or PortTreeModel
          myVertex = (IB_Vertex) event.getContextObject();
        }
      }
      else if (event.getSelectedObject() instanceof IB_Edge)
      {
        logger.severe("Got an edge from somewhere");
        // is this from the graph or tree?
        IB_Edge e = (IB_Edge)event.getSelectedObject();
        if((event.getSource() instanceof VertexTreePanel) || (event.getSource() instanceof PortTreePanel) ||(event.getSource() instanceof LinkTreePanel))
        {
          myVertex = e.getEndpoint1();
          portNum  = e.getEndPort1().getPortNumber();
          logger.severe("One of the Trees");
         }
        else if (event.getSource() instanceof SmtGuiApplication)
        {
          // mostly here due to Top Traffic or Error analysis panel
//          logger.severe("Source is: " + event.getSource().getClass().getCanonicalName());
//          logger.severe("Context is: " + event.getContextObject().getClass().getCanonicalName());
//          logger.severe("Edge is: " + e.toEdgeIdStringVerbose(60));
          myVertex = e.getEndpoint1();
          portNum  = e.getEndPort1().getPortNumber();
        }
        else
          logger.severe("Must be a SimpleCollapsableGraph");  // do nothing for now TODO
        
       }
      else
      {
        logger.severe("unknown selection while handlingLinkSelection");
        return true;
      }
      

      if((myVertex != null) && (portNum > -1))
      {
          // create the model, then the panel, then put it in the scroll pane
          LinkTreeModel treeModel = new LinkTreeModel(myVertex, portNum);
          if(treeModel.isLink())
          {
            if (!(event.getSource() instanceof LinkTreePanel))
            {
              LinkTreePanel ptp = new LinkTreePanel();
              ptp.setTreeModel(treeModel);
              
              IB_Edge e = treeModel.getRootEdge();
              
              JScrollPane scroller = new JScrollPane(ptp);
 //             scroller.setName(e.getKey());
              scroller.setName(e.toEdgeIdString(16));
              if (UpdateService != null)
                UpdateService.addListener(ptp);
              // now add this to the west tabbed pane
              addToCenter(scroller);
              handled = true;
            }
          }
          else
          {
            logger.warning("Cannot expand this link tree model");
            handled = true;
          }
      }
     }
    return handled;
  }

  public boolean handlePortSelected(IB_GraphSelectionUpdater source, IB_GraphSelectionEvent event)
  {
    // the source will always be the global graph selection manager
    // the event.source will be the parent object where the event came from
    // the event.graphEvent is the object that was selected

    // from within the VertexTree, a port can be selected, and if so
    // it should display the PortTreePanel
    
    // from within the top tables, a port or link can be selected, and if so
    // the associated PortTreePanel should be displayed
    
    boolean handled = false;  // return true if this method handled (consumed) this event
    OSM_Port p = null;
    

    if ((event.getSelectedObject() instanceof UserObjectTreeNode) || (event.getSelectedObject() instanceof OSM_Port) || (event.getSelectedObject() instanceof IB_Edge))
    {
      // need a vertex and a portnumber
      IB_Vertex myVertex = null;
      int portNum = -1;
      
      if (event.getSelectedObject() instanceof UserObjectTreeNode)
      {
        UserObjectTreeNode tn = (UserObjectTreeNode) event.getSelectedObject();
        NameValueNode vmn = (NameValueNode) tn.getUserObject();
        if (vmn.getMemberName().equals("port #"))
        {
          p = (OSM_Port)vmn.getMemberObject();
          if(p != null)
            portNum = p.getPortNumber();
        }
        if (vmn.getMemberName().equals("endport1") && (event.getContextObject() instanceof IB_Edge))
        {
          IB_Edge e = (IB_Edge)event.getContextObject();
          myVertex = e.getEndpoint1();
          portNum = e.getEndPort1().getPortNumber();
         }
        if (vmn.getMemberName().equals("endport2") && (event.getContextObject() instanceof IB_Edge))
        {
          IB_Edge e = (IB_Edge)event.getContextObject();
          myVertex = e.getEndpoint2();
          portNum = e.getEndPort2().getPortNumber();
        }
         
        if (event.getContextObject() instanceof IB_Vertex)
        {
          myVertex = (IB_Vertex) event.getContextObject();
        }
      }
      else if (event.getSelectedObject() instanceof OSM_Port)
      {
        p = (OSM_Port)event.getSelectedObject();
        if(p != null)
        {
          portNum = p.getPortNumber();
          IB_Guid g = p.getNodeGuid();
          if(UpdateService instanceof SMT_UpdateService)
          {
            LinkedHashMap<String, IB_Vertex> vmap = ((SMT_UpdateService)UpdateService).getVertexMap();
            if((vmap != null) && (g != null))
              myVertex = vmap.get(IB_Vertex.getVertexKey(g));
            else
              logger.severe("The Node Guid or Vertex Map is null: " + (g==null) + ", " + (vmap==null));
           }
        }
        else
        {
          logger.severe("Couldn't create an OSM_Port from the selected object");
        }
       }
      else if (event.getSelectedObject() instanceof IB_Edge)
      {
        IB_Edge e = (IB_Edge)event.getSelectedObject();
        myVertex = e.getEndpoint1();
        p = e.getEndPort1();
        portNum = e.getEndPort1().getPortNumber();
       }
      else
      {
        logger.severe("unknown selection while handlingPortSelection");
        return true;
      }
      

     // if((myVertex != null) && (portNum > -1))
      if((myVertex != null) && (p != null ))
      {
          // create the model, then the panel, then put it in the scroll pane
          SimplePortTreeModel sTreeModel = new SimplePortTreeModel(myVertex, p);
          PortTreeModel treeModel = new PortTreeModel(myVertex, portNum);
          if(treeModel.isLink())
          {
            if (!(event.getSource() instanceof PortTreePanel))
            {
              PortTreePanel ptp = new PortTreePanel();
              ptp.setTreeModel(treeModel);

              JScrollPane scroller = new JScrollPane(ptp);
              scroller.setName(myVertex.getName() + ":" + portNum);
              if (UpdateService != null)
                UpdateService.addListener(ptp);
              // now add this to the west tabbed pane
              addToCenter(scroller);
              handled = true;
            }
          }
          else
          {
            PortTreePanel ptp = new PortTreePanel();
            ptp.setTreeModel(sTreeModel);

            JScrollPane scroller = new JScrollPane(ptp);
            scroller.setName(myVertex.getName() + ":" + portNum);
            if (UpdateService != null)
              UpdateService.addListener(ptp);
            // now add this to the west tabbed pane
            addToCenter(scroller);
            handled = true;

            logger.warning("Cannot expand this ports tree model");
            handled = true;
          }
      }
     }
    return handled;
  }

  public boolean handleTabClosure(IB_GraphSelectionUpdater source, IB_GraphSelectionEvent event)
  {
    // the source will always be the global graph selection manager
    // the event.source will be the parent object where the event came from
    // the event.graphEvent is the object that was selected

    // a tab is closing, find out what it is, and release its resources
    // specifically, remove it from listener lists.

    // There are many types of tabbed panes, see which one and attempt to handle
    // it
    //
    // FabricTreePanel
    // FabricSummaryPanel
    // VertexTreePanel
    // PortTreePanel
    // OMS_UpdateListenerPanel
    // GraphSelectionPanel
    // MessageListenerPanel
    // TimeListenerPanel
    //
    // The TopPanels (six of them)

    boolean handled = false;  // return true if this method handled (consumed) this event

    logger.severe("Tab Close event detected");

    if (event.getSource() instanceof JClosableTabbedPane)
    {
      // the only reason this happens is when a closable tab is closed
      // and I may want to see what was in the tab, and clean it up
      // (like remove it from listener lists) before removing the tab

      JClosableTabbedPane tp = (JClosableTabbedPane) event.getSource();
      Integer index = (Integer) event.getSelectedObject();
      String tabTitle = (tp.getTitleAt(index)).trim();
      if (event.getContextObject() instanceof JScrollPane)
      {
        JScrollPane sp = (JScrollPane) event.getContextObject();
        closeScrollPane(sp);
        handled = true;
     }
      else if (event.getContextObject() instanceof SMT_FabricGraphPanel)
      {
        handled = true;
        // attempt to remove controls, which in turn will remove the graph
        removeFromSouth(new SimpleGraphControlPanel(false));
      }
      else
        logger.info("The context object is: " + event.getContextObject().getClass().getName());
      
      logger.info("Closing tab: (" + tabTitle + ") at index: " + index + "\n");
    }
    return handled;

  }

  private boolean closeScrollPane(JScrollPane sp)
  {
    // the source will always be the global graph selection manager
    // the event.source will be the parent object where the event came from
    // the event.graphEvent is the object that was selected

    // a tab is closing, find out what it is, and release its resources
    // specifically, remove it from listener lists.

    // There are many types of tabbed panes, see which one and attempt to handle
    // it
    //
    // FabricTreePanel
    // FabricSummaryPanel
    // VertexTreePanel
    // PortTreePanel
    // OMS_UpdateListenerPanel
    // GraphSelectionPanel
    // MessageListenerPanel
    // TimeListenerPanel
    //
    // The TopPanels (six of them)

    boolean handled = false;  // return true if this method handled (consumed) this event

        JViewport vp = sp.getViewport();
        logger.info("I have a JViewport, and it contains: " + vp.getComponentCount() + " objects");
        
        // normally the viewport only contains a single object, the one in the scrollpane
        for (int j = 0; j < vp.getComponentCount(); j++)
        {
          logger.info("The object at index " + j + " is: "
              + vp.getComponent(j).getClass().getName());

          // iterate through the objects, and if it is one of the known
          // listeners, remove it
          if (vp.getComponent(j) instanceof FabricTreePanel)
          {
            FabricTreePanel panel = (FabricTreePanel) vp.getComponent(j);
            UpdateService.removeListener(panel);
            handled = true;
          }
          else if (vp.getComponent(j) instanceof FabricSummaryPanel)
          {
            // mark all checkboxmenuitems appropriately
            FabricSummaryPanel panel = (FabricSummaryPanel) vp.getComponent(j);
            UpdateService.removeListener(panel);
            handled = true;
            
                 // notify the global jpopupmenu and the jmenuitems
              getRootNodePopupMenu().setOverviewSelected(false);
              ItemListener[] listarray = OverviewCBMI.getItemListeners();
              if(listarray.length > 0)
              {
                OverviewCBMI.removeItemListener(listarray[0]);
                OverviewCBMI.setSelected(false);
                OverviewCBMI.addItemListener(listarray[0]);
              }
          }
          else if (vp.getComponent(j) instanceof RT_TableTreePanel)
          {
            // mark all checkboxmenuitems appropriately
            RT_TableTreePanel panel = (RT_TableTreePanel) vp.getComponent(j);
            UpdateService.removeListener(panel);
            handled = true;
            
                 // notify the global jpopupmenu and the jmenuitems
              getRootNodePopupMenu().setRoutingSelected(false);
              ItemListener[] listarray = RoutingCBMI.getItemListeners();
              if(listarray.length > 0)
              {
                RoutingCBMI.removeItemListener(listarray[0]);
                RoutingCBMI.setSelected(false);
                RoutingCBMI.addItemListener(listarray[0]);
              }
          }
          else if (vp.getComponent(j) instanceof SubnetTreePanel)
          {
            // mark all checkboxmenuitems appropriately
            SubnetTreePanel panel = (SubnetTreePanel) vp.getComponent(j);
            UpdateService.removeListener(panel);
            handled = true;
            
                 // notify the global jpopupmenu and the jmenuitems
              getRootNodePopupMenu().setDetailsSelected(false);
              ItemListener[] listarray = DetailsCBMI.getItemListeners();
              if(listarray.length > 0)
              {
                DetailsCBMI.removeItemListener(listarray[0]);
                DetailsCBMI.setSelected(false);
                DetailsCBMI.addItemListener(listarray[0]);
              }
          }
          else if (vp.getComponent(j) instanceof VertexTreePanel)
          {
            VertexTreePanel panel = (VertexTreePanel) vp.getComponent(j);
            logger.severe("Closing a VertexTreePanel");
            UpdateService.removeListener(panel);
            handled = true;
         }
          else if (vp.getComponent(j) instanceof PortTreePanel)
          {
            PortTreePanel panel = (PortTreePanel) vp.getComponent(j);
            logger.severe("Closing a PortTreePanel");
            UpdateService.removeListener(panel);
            handled = true;
          }
          else if (vp.getComponent(j) instanceof OMS_CollectionListenerPanel)
          {
            OMS_CollectionListenerPanel panel = (OMS_CollectionListenerPanel) vp.getComponent(j);
            logger.severe("Closing a OMS_CollectionListenerPanel");
            if(UpdateService instanceof SMT_UpdateService)
            {
              SMT_UpdateService sus = (SMT_UpdateService)UpdateService;
              // tell the collection that this panel can stop listening
              sus.getCollection().removeOMS_CollectionChangeListener(panel);
            }
            // I want to set the checkbox value without triggering an event, which will lead back here
            ItemListener[] listarray = OMSRecordCBMI.getItemListeners();
            if(listarray.length > 0)
            {
              OMSRecordCBMI.removeItemListener(listarray[0]);
              OMSRecordCBMI.setSelected(false);
              OMSRecordCBMI.addItemListener(listarray[0]);
            }
            handled = true;
          }
          else if (vp.getComponent(j) instanceof OMS_UpdateListenerPanel)
          {
            OMS_UpdateListenerPanel panel = (OMS_UpdateListenerPanel) vp.getComponent(j);
            logger.severe("Closing a OMS_UpdateListenerPanel");
            UpdateService.removeListener(panel);
            Analysis_Mgr.removeSMT_AnalysisChangeListener(panel);
            // I want to set the checkbox value without triggering an event, which will lead back here
            ItemListener[] listarray = OMSUpdateCBMI.getItemListeners();
            if(listarray.length > 0)
            {
              OMSUpdateCBMI.removeItemListener(listarray[0]);
              OMSUpdateCBMI.setSelected(false);
              OMSUpdateCBMI.addItemListener(listarray[0]);
            }
            handled = true;
          }
          else if (vp.getComponent(j) instanceof GraphSelectionPanel)
          {
            GraphSelectionPanel panel = (GraphSelectionPanel) vp.getComponent(j);
            logger.severe("Closing a GraphSelectionPanel");
           // UpdateService.removeListener(panel);
            // I want to set the checkbox value without triggering an event, which will lead back here
            ItemListener[] listarray = SelectionCBMI.getItemListeners();
            if(listarray.length > 0)
            {
              SelectionCBMI.removeItemListener(listarray[0]);
              SelectionCBMI.setSelected(false);
              SelectionCBMI.addItemListener(listarray[0]);
            }
            handled = true;
          }
          else if (vp.getComponent(j) instanceof MessageListenerPanel)
          {
            MessageListenerPanel panel = (MessageListenerPanel) vp.getComponent(j);
            logger.severe("Closing a MessageListenerPanel");
            // UpdateService.removeListener(panel);
            // I want to set the checkbox value without triggering an event, which will lead back here
            ItemListener[] listarray = MessageCBMI.getItemListeners();
            if(listarray.length > 0)
            {
              MessageCBMI.removeItemListener(listarray[0]);
              MessageCBMI.setSelected(false);
              MessageCBMI.addItemListener(listarray[0]);
            }
            handled = true;
          }
          else if (vp.getComponent(j) instanceof TimeListenerPanel)
          {
            TimeListenerPanel panel = (TimeListenerPanel) vp.getComponent(j);
            logger.severe("Closing a TimeListenerPanel");
            HeartBeat_Mgr.removeHeartBeatListener(panel);
            // I want to set the checkbox value without triggering an event, which will lead back here
            ItemListener[] listarray = HeartBeatCBMI.getItemListeners();
            if(listarray.length > 0)
            {
              HeartBeatCBMI.removeItemListener(listarray[0]);
              HeartBeatCBMI.setSelected(false);
              HeartBeatCBMI.addItemListener(listarray[0]);
            }
            handled = true;
          }
          else if (vp.getComponent(j) instanceof SMT_AnalysisPanel)
          {
            // ALL of the AnalysisListenerPanels need to go here
            
             SMT_AnalysisPanel panel = (SMT_AnalysisPanel) vp.getComponent(j);
            logger.severe("Closing a SMT_AnalysisPanel");
            // UpdateService.removeListener(panel);
            Analysis_Mgr.removeSMT_AnalysisChangeListener(panel);
            handled = true;
            
            // if this is the fabric root analysis panel, clear the various checkbox items 
            String tabName = "U: " + OMS.getFabricName();
            if(panel.getType().equals(SMT_AnalysisType.SMT_FABRIC_UTILIZATION))
            {              
              // notify the global jpopupmenu and the jmenuitems
              getRootNodePopupMenu().setUtilizationSelected(false);
              ItemListener[] listarray = UtilizationCBMI.getItemListeners();
              if(listarray.length > 0)
              {
                UtilizationCBMI.removeItemListener(listarray[0]);
                UtilizationCBMI.setSelected(false);
                UtilizationCBMI.addItemListener(listarray[0]);
              }
              // force the controls to close too
            }
            else if(SMT_AnalysisType.SMT_TOP_TYPES.contains(panel.getType()))
            {
              for(SMT_AnalysisType s : SMT_AnalysisType.SMT_TOP_TYPES)
              {
                if(panel.getType().equals(s))
                {
                  tabName = s.getName();

                  JCheckBoxMenuItem cbmi = EPortsCBMI;
                  if(s.equals(SMT_AnalysisType.SMT_FABRIC_ERROR_PORTS))
                    cbmi = EPortsCBMI;
                  else if(s.equals(SMT_AnalysisType.SMT_FABRIC_ERROR_NODES))
                    cbmi = ENodesCBMI;
                  else if(s.equals(SMT_AnalysisType.SMT_FABRIC_ERROR_LINKS))
                    cbmi = ELinksCBMI;
                  else if(s.equals(SMT_AnalysisType.SMT_FABRIC_TOP_NODES))
                    cbmi = TNodesCBMI;
                  else if(s.equals(SMT_AnalysisType.SMT_FABRIC_TOP_LINKS))
                    cbmi = TLinksCBMI;
                  else if(s.equals(SMT_AnalysisType.SMT_FABRIC_TOP_PORTS))
                    cbmi = TPortsCBMI;
                  
                  // I want to set the checkbox value without triggering an event, which will lead back here
                  ItemListener[] listarray = cbmi.getItemListeners();
                  if(listarray.length > 0)
                  {
                    cbmi.removeItemListener(listarray[0]);
                    cbmi.setSelected(false);
                    cbmi.addItemListener(listarray[0]);
                  }
                  break;
                }
               }
            }
            else if(SMT_AnalysisType.SMT_HMAP_TYPES.contains(panel.getType()))
            {
              for(SMT_AnalysisType s : SMT_AnalysisType.SMT_HMAP_TYPES)
              {
                if(panel.getType().equals(s))
                {
                  tabName = s.getName();
                  
                   JCheckBoxMenuItem cbmi = HmpCaPortsCBMI;
                  if(s.equals(SMT_AnalysisType.SMT_HMAP_CA_PORTS))
                    cbmi = HmpCaPortsCBMI;
                  else if(s.equals(SMT_AnalysisType.SMT_HMAP_SW_PORTS))
                    cbmi = HmpSwPortsCBMI;
                  else if(s.equals(SMT_AnalysisType.SMT_HMAP_ALL_PORTS))
                    cbmi = HmpAllPortsCBMI;
                  
                  // I want to set the checkbox value without triggering an event, which will lead back here
                  ItemListener[] listarray = cbmi.getItemListeners();
                  if(listarray.length > 0)
                  {
                    cbmi.removeItemListener(listarray[0]);
                    cbmi.setSelected(false);
                    cbmi.addItemListener(listarray[0]);
                  }
                  break;
                }
               }
            }
            else
            {
              // unhandled Analysis Panel event, should never get here
              System.err.println("This is an SMT_AnalysisPanel of type: " + panel.getType().getAnalysisName());
            }
          }
          else if (vp.getComponent(j) instanceof SimpleGraphControlPanel)
          {
            SimpleGraphControlPanel panel = (SimpleGraphControlPanel) vp.getComponent(j);
            logger.severe("Closing a SimpleGraphControlPanel");
            handled = true;
            
            // if this is the fabric root analysis panel, clear the various checkbox items 
            String tabName = GControlName;
              
              // notify the global jpopupmenu and the jmenuitems
              getRootNodePopupMenu().setGraphSelected(false);
              ItemListener[] listarray = GraphCBMI.getItemListeners();
              if(listarray.length > 0)
              {
                GraphCBMI.removeItemListener(listarray[0]);
                GraphCBMI.setSelected(false);
                GraphCBMI.addItemListener(listarray[0]);
              }
              // DO NOT call removeFabricGraph, as this will result in an endless loop
              removeNamedFromCenter("G:" + OMS.getFabricName());
           }
          else
          {
            // what is this
            logger.info("The Component about to close is: (" + vp.getComponent(j).getClass().getName() + ")");
          }
      }
    return handled;

  }

  @Override
  public void osmFabricUpdate(OSM_Fabric osmFabric) throws Exception
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void timeUpdate(TimeStamp time)
  {
    // The application can be a proxy for this event, or simply hook the updater
    // (in this case Tserv) to all the listeners
    //
    // TODO add all the listener components up to Tserv
//    System.err.println("New Time: " + time.toString());
    
  }

  @Override
  public void osmServiceUpdate(OMS_Updater updater, OpenSmMonitorService osmService) throws Exception
  {
    // The application can be a proxy for this event, or simply hook the updater
    // (in this case UpdateService) to all the listeners
    //
    // TODO add all the listener components up to the Updater
//    System.err.println("New OMS: " + osmService.getTimeStamp().toString());
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    // this event should only be coming from a SplashFrame
    // hide this and expose the other
    if((e.getSource() instanceof SmtSplashFrame))
    {
      SmtSplashFrame sf = (SmtSplashFrame)e.getSource();
    logger.severe("received an event from the splash frame, probably a close?");
    destroy("closed by splash exit");
    
    }
    else if ((e.getSource() instanceof JCheckBoxMenuItem))
    {
      logger.severe("received an event from one of the checkbox menus");
      JCheckBoxMenuItem cbmi = (JCheckBoxMenuItem)e.getSource();
      logger.severe("Name is:" + cbmi.getText() + ", and value is: " + Boolean.toString(cbmi.isSelected()));
    }
    else
      logger.severe("Recieved a spurrious action event, not from the SplashFrame");
  }
}
