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
 *        file: XY_PlotTableModel.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.infiniband.opensm.plugin.gui.chart;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

public class XY_PlotTableModel extends AbstractTableModel implements TableModel
{
  private static final int MAX_COLS = 4;
  private static final int MAX_ROWS   = 4;
  private int NumRows   = 4;
  
  private Object[][] data;

  /**
   * Creates a table model 4 columns by specified rows (1 + header)
   * 
   * @param rows
   *          the row count.
   */
  public XY_PlotTableModel(int rows)
  {
    // how many rows depends on how many series
    // the first (or zeroth) row is for labels
    NumRows = rows < MAX_ROWS ? rows: MAX_ROWS;
    this.data = new Object[NumRows][MAX_COLS];
  }

  /**
   * Returns the number of columns.
   * 
   * @return MAX_COLS.
   */
  public int getColumnCount()
  {
    return MAX_COLS;
  }

  /**
   * Returns the row count.
   * 
   * @return NumRows.
   */
  public int getRowCount()
  {
     return NumRows;
  }

  /**
   * Returns the value at the specified cell in the table.
   * 
   * @param row
   *          the row index.
   * @param column
   *          the column index.
   * 
   * @return The value.
   */
  public Object getValueAt(int row, int column)
  {
    return this.data[row][column];
  }

  /**
   * Sets the value at the specified cell.
   * 
   * @param value
   *          the value.
   * @param row
   *          the row index.
   * @param column
   *          the column index.
   */
  public void setValueAt(Object value, int row, int column)
  {
    this.data[row][column] = value;
    fireTableDataChanged();
  }

  /**
   * Returns the column name.
   * 
   * @param column
   *          the column index.
   * 
   * @return The column name.
   */
  public String getColumnName(int column)
  {
    switch (column)
    {
      case 0:
        return "name";
      case 3:
        return "units";
      case 1:
        return "time";
      case 2:
        return "value";
    }
    return null;
  }
}
