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
 *        file: JClosableTabbedPane.java
 *
 *  Created on: Oct 1, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.Icon;
import javax.swing.JTabbedPane;

import gov.llnl.lc.infiniband.opensm.plugin.graph.IB_GraphSelectionEvent;
import gov.llnl.lc.smt.manager.GraphSelectionManager;

/**********************************************************************
 * Describe purpose and responsibility of JClosableTabbedPane
 * <p>
 * 
 * @see related classes and interfaces
 * 
 * @author meier3
 * 
 * @version Oct 1, 2013 2:00:50 PM
 **********************************************************************/

public class JClosableTabbedPane extends JTabbedPane
{
  private static final long   serialVersionUID = -5923476382802695732L;
  private TabCloseUI          closeUI          = new TabCloseUI();
  private JClosableTabbedPane thisPane;

  public JClosableTabbedPane(int top)
  {
    super(top);
    thisPane = this;
  }

  @Override
  public void paint(Graphics g)
  {
    super.paint(g);
    closeUI.paint(g);
  }

  public void insertClosableTab(String title, Icon icon, Component component, String tip, int index)
  {
    super.insertTab(title + "   ", icon, component, tip, index);
  }

  public void addUnclosableTab(String title, Component component)
  {
    super.addTab(title.trim(), component);
  }

  public void addUnclosableTab(String title, Icon icon, Component component)
  {
    super.addTab(title.trim(), icon, component);
  }

  public void addUnclosableTab(String title, Icon icon, Component component, String tip)
  {
    super.addTab(title.trim(), icon, component, tip);
  }

  private class TabCloseUI implements MouseListener, MouseMotionListener
  {
    private int       closeX        = 0, closeY = 0, mouseX = 0, mouseY = 0;
    private int       selectedTab;
    private final int width         = 7, height = 7;
    private final int thickness     = 4;
    private final int offset        = 6;
    private Rectangle rectangle     = new Rectangle(0, 0, width, height);
    private boolean   lastOverClose = false;

    public TabCloseUI()
    {
      addMouseMotionListener(this);
      addMouseListener(this);
    }

    public void mouseEntered(MouseEvent me)
    {
      int meX = me.getX();
      int meY = me.getY();
      if (mouseOverTab(meX, meY))
      {
        controlCursor();
        repaint();
      }
    }

    public void mouseExited(MouseEvent me)
    {
      int meX = me.getX();
      int meY = me.getY();
      if (mouseOverTab(meX, meY))
      {
        controlCursor();
        repaint();
      }
    }

    public void mousePressed(MouseEvent me)
    {
    }

    public void mouseClicked(MouseEvent me)
    {
    }

    public void mouseDragged(MouseEvent me)
    {
    }

    public void mouseReleased(MouseEvent me)
    {
      if (closeUnderMouse(me.getX(), me.getY()))
      {
        boolean isToCloseTab = tabAboutToClose(selectedTab);
        if (isToCloseTab && selectedTab > -1)
        {
           int origTabCount = thisPane.getTabCount();
          // tell everyone the tab is closing at this index
          GraphSelectionManager.getInstance().updateAllListeners(new IB_GraphSelectionEvent(thisPane, getComponentAt(getSelectedIndex()), getSelectedIndex()));

          if (getSelectedIndex() > 0)
            setSelectedIndex(getSelectedIndex() - 1);

          // make sure still valid, broadcast event may remove before I can
          if(thisPane.getTabCount() == origTabCount)
           removeTabAt(selectedTab);
        }
        selectedTab = getSelectedIndex();
      }
      int meX = me.getX();
      int meY = me.getY();
      if (mouseOverTabUnclosable(meX, meY))
      {
        controlCursor();
        repaint();
      }
    }

    public void mouseMoved(MouseEvent me)
    {
      mouseX = me.getX();
      mouseY = me.getY();
      boolean mouseOverTab = mouseOverTabUnclosable(mouseX, mouseY);
      if (mouseOverTab || lastOverClose)
      {
        controlCursor();
        repaint();
        lastOverClose = mouseOverTab;
      }
    }

    private void controlCursor()
    {
      if (getTabCount() > 0)
        if (closeUnderMouse(mouseX, mouseY))
        {
          setCursor(new Cursor(Cursor.HAND_CURSOR));
          if (selectedTab > -1)
          {
            try
            {
              setToolTipTextAt(selectedTab, "Close " + getTitleAt(selectedTab));
            }
            catch(Exception e)
            {
              // probably some sort of array out of bounds exception
              System.err.println("controlCursor exception1: " + e.getMessage());
            }
          }
        }
        else
        {
          setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
          if (selectedTab > -1)
          {
            try
            {
              setToolTipTextAt(selectedTab, "");
            }
            catch(Exception e)
            {
              // probably some sort of array out of bounds exception
              System.err.println("controlCursor exception2: " + e.getMessage());
            }
          }
         }
    }

    private boolean closeUnderMouse(int x, int y)
    {
      rectangle.x = closeX;
      rectangle.y = closeY;
      return rectangle.contains(x, y);
    }

    public void paint(Graphics g)
    {
      int tabCount = getTabCount();
      for (int j = 0; j < tabCount; j++)
        if (getSelectedIndex() == j && getTitleAt(j).endsWith("   "))
        {
          Rectangle bounds = getBoundsAt(j);
          int x = bounds.x + bounds.width - width - offset;
          int y = bounds.y + offset;
          drawClose(g, x, y);
          break;
        }
      if (mouseOverTab(mouseX, mouseY))
      {
        drawClose(g, closeX, closeY);
      }
    }

    private void drawClose(Graphics g, int x, int y)
    {
      if (getTabCount() > 0)
      {
        Graphics2D g2 = (Graphics2D) g;
        drawColored(g2, isUnderMouse(x, y) ? Color.RED : Color.WHITE, x, y);
      }
    }

    private void drawColored(Graphics2D g2, Color color, int x, int y)
    {
      g2.setStroke(new BasicStroke(thickness, BasicStroke.JOIN_ROUND, BasicStroke.CAP_ROUND));
      g2.setColor(Color.BLACK);
      g2.drawLine(x, y, x + width, y + height);
      g2.drawLine(x + width, y, x, y + height);
      g2.setColor(color);
      g2.setStroke(new BasicStroke(thickness - 2, BasicStroke.JOIN_ROUND, BasicStroke.CAP_ROUND));
      g2.drawLine(x, y, x + width, y + height);
      g2.drawLine(x + width, y, x, y + height);

    }

    private boolean isUnderMouse(int x, int y)
    {
      if (Math.abs(x - mouseX) < width && Math.abs(y - mouseY) < height)
        return true;
      return false;
    }

    private boolean mouseOverTab(int x, int y)
    {
      int tabCount = getTabCount();
      for (int j = 0; j < tabCount; j++)
        if (getBoundsAt(j).contains(mouseX, mouseY))
        {
          selectedTab = j;
          closeX = getBoundsAt(j).x + getBoundsAt(j).width - width - offset;
          closeY = getBoundsAt(j).y + offset;
          return getTitleAt(j).endsWith("   ");
        }
      return false;
    }

    private boolean mouseOverTabUnclosable(int x, int y)
    {
      int tabCount = getTabCount();
      for (int j = 0; j < tabCount; j++)
        if (getBoundsAt(j).contains(mouseX, mouseY))
        {
          selectedTab = j;
          closeX = getBoundsAt(j).x + getBoundsAt(j).width - width - offset;
          closeY = getBoundsAt(j).y + offset;
          return true;
        }
      return false;
    }
  }

  public boolean tabAboutToClose(int tabIndex)
  {
    if (!getTitleAt(tabIndex).endsWith("   "))
      return false;
    Component c = getComponentAt(tabIndex);
    c.firePropertyChange("TAB_REMOVE", 0, 0);
    return true;
  }
}