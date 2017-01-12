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
 *        file: SMT_AboutPanel.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.text;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.SmtConstants;
import gov.llnl.lc.smt.command.about.SmtAbout;
import gov.llnl.lc.smt.command.about.SmtAboutRecord;

public class SMT_AboutPanel extends JPanel implements CommonLogger
{
  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = -1494467535623890423L;
  /**
   * Create the panel.
   */
  
  private JEditorPane editorPane;
  private boolean HTML = false;
  private String header = "";
  private String footer = "";
  private String nl     = "\n";

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

  
  public SMT_AboutPanel()
  {
    setBorder(new TitledBorder(null, "About SMT Components", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
    setLayout(new BorderLayout(0, 0));
    editorPane = new JEditorPane();
    editorPane.setEditable(false);
    setHTML(true);
    add(editorPane);
    init();
   }
  
  public void init()
  {
     // given this information, I can create an HTML string
     StringBuffer buff = new StringBuffer();
          
       this.setName("About SMT");
       ArrayList<SmtAboutRecord> records = SmtAbout.getRecordsFromManifest(this);
       
       buff.append(SmtConstants.MEDIUM_FONT);
       buff.append(SmtConstants.INDENT_START);

       buff.append("The following " + SmtConstants.SPACE + "<b>" + records.size() + "</b> records describe JAR files, libraries, or packages, used by SMT");        
       buff.append(SmtConstants.INDENT_END);
      buff.append(SmtConstants.H_LINE);
       
       for(SmtAboutRecord record: records)
       {
         buff.append(header);
         buff.append(SmtConstants.MEDIUM_FONT);
         buff.append(record.toContent());
         buff.append(footer);

         buff.append(SmtConstants.H_LINE);
      }
       editorPane.setText(buff.toString());
    return;
  }
  
}
