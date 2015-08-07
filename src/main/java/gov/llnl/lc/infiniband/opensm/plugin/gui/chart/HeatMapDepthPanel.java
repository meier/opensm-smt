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
 *        file: HeatMapDepthPanel.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.chart;

import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_Depth;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.EnumSet;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class HeatMapDepthPanel extends JPanel
{

  /**  describe serialVersionUID here **/
  private static final long serialVersionUID = -4649218449467882730L;
  
  private PortHeatMapDataSet HeatMap = null;
  private Color backgroundColor = Color.WHITE;

  public HeatMapDepthPanel(PortHeatMapDataSet heatMap)
  {
    super();
    HeatMap = heatMap;
    initialize();
  }
  
  /************************************************************
   * Method Name:
   *  initialize
    *
   * @see     describe related java objects
   *
  */
  private void initialize()
  {
    setLayout(new FlowLayout(FlowLayout.CENTER, 5, 1));
    setBackground(backgroundColor);
    
    int tPorts = 1024;
    int sPorts = 666;
    
    if(HeatMap == null)
    {
      // just the default, placeholder panel, used for designing composite guis
    JCheckBox level0CB = new JCheckBox("Level 0: 345 ports");
    JCheckBox level1CB = new JCheckBox("Level 1: 678 ports");
    JCheckBox level2CB = new JCheckBox("Level 2: 123 ports");
    JCheckBox level3CB = new JCheckBox("Level 3: 952 ports");
    JCheckBox level4CB = new JCheckBox("Level 4: 158 ports");
    
    level0CB.setBackground(backgroundColor);
    level1CB.setBackground(backgroundColor);
    level2CB.setBackground(backgroundColor);
    level3CB.setBackground(backgroundColor);
    level4CB.setBackground(backgroundColor);
    
    add(level0CB);
    add(level1CB);
    add(level2CB);
    add(level3CB);
    add(level4CB);
    
    level0CB.setEnabled(false);
    level1CB.setEnabled(false);
    level2CB.setEnabled(false);
    level3CB.setEnabled(false);
    level4CB.setEnabled(false);
    
    level0CB.setSelected(true);
    level1CB.setSelected(true);
    level2CB.setSelected(true);
    level3CB.setSelected(true);
    level4CB.setSelected(true);

    }
    else
    {
      int numLevels = 0;
      for(int i = 0; i < HeatMap.LevelSize.length; i++)
      {
        if(HeatMap.LevelSize[i] > 0)
          numLevels++;
      }
      
      tPorts = 0;
      sPorts = 0;
      
      // add the checkboxes
      for(int j = 0; j < numLevels; j++)
      {
        String label = "Level " + j + ": " + HeatMap.LevelSize[j] + " ports";
        JCheckBox levelCB = new JCheckBox(label);
        levelCB.setBackground(backgroundColor);
        levelCB.setEnabled(false);  // just for display, never selectable
        
        tPorts += HeatMap.LevelSize[j];
        
        EnumSet<IB_Depth> includedDepths = HeatMap.IncludedDepths;
        if(includedDepths != null)
        {
          IB_Depth d = IB_Depth.get(j);
          if(includedDepths.contains(d))
          {
            levelCB.setSelected(true);
            sPorts += HeatMap.LevelSize[j];
          }
         }
        else
          System.err.println("Cant determine inclusive");
        
        add(levelCB);
        }
    }
    /* now add the totals */
    JLabel totalLabel = new JLabel("Total Ports: " + tPorts);
    JLabel selectedLabel = new JLabel("Selected Ports: " + sPorts);
    add(selectedLabel);
    add(totalLabel);

    validate();
  }

  /**  
   * @wbp.parser.entryPoint
 */
   public static void main(String[] args)
   {
     JFrame frame = new JFrame("HetMapDepthPanel Example");
     frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     HeatMapDepthPanel tsp = new HeatMapDepthPanel(null);
     tsp.setPreferredSize(new Dimension(200, 300));
     
     frame.setContentPane(tsp);
     frame.pack();
     frame.setVisible(true);
   }


}
