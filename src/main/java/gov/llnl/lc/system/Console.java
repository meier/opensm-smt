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
 *        file: Console.java
 *
 *  Created on: Jul 31, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.system;

import gov.llnl.lc.parser.ParserUtils;

import java.awt.Dimension;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import jcurses.system.Toolkit;

/**********************************************************************
 * Describe purpose and responsibility of Console
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Jul 31, 2013 4:24:25 PM
 **********************************************************************/
public class Console
{
  public enum ConsoleColor
  {
    dark_grey(0,   "dark_grey"),
    red      (1,   "red"),
    green    (2,   "green"),
    yellow   (3,   "yellow"),
    blue     (4,   "blue"),
    magenta  (5,   "magenta"),
    cyan     (6,   "cyan"),
    white    (7,   "white"),
    navy     (19,  "navy"),
    n_blue   (21,  "neon blue"),
    forest   (22,  "forest green"),
    n_cyan   (45,  "neon cyan"),
    n_green  (46,  "neon green"),
    brown    (52,  "brown"),
    n_magenta(93,  "neon magenta"),
    tan      (130, "tan"),
    n_red    (196, "neon red"),
    pink     (200, "pink"),
    n_yellow (226, "neon yellow"),
    black    (232, "black"),
    grey     (248, "grey");
    
    public static final EnumSet<ConsoleColor> CC_ALL_COLORS = EnumSet.allOf(ConsoleColor.class);

    private static final Map<Integer,ConsoleColor> lookup = new HashMap<Integer,ConsoleColor>();

    static 
    {
      for(ConsoleColor s : CC_ALL_COLORS)
           lookup.put(s.getIndex(), s);
    }
    
  // the actual color value
    private int Index;
    
    // the name of the color
    private String Name;
    
    private ConsoleColor(int index, String name)
    {
      Index = index;
      Name = name;
    }
    
    public static ConsoleColor getByIndex(int index)
    {
      ConsoleColor t = null;
      for(ConsoleColor s : CC_ALL_COLORS)
      {
        if(s.getIndex() == index)
          return s;
      }
      return t;
    }

    public int getIndex()
    {
      return Index;
    }

    public String getName()
    {
      return Name;
    }
  }
  
   public enum ConsoleControl
  {
    clear_screen_entire(      0, "clear_screen_entire",     "\u001B[2J"),    
    clear_screen_to_end(      1, "clear_screen_to_end",     "\u001B[0J"),    
    clear_screen_from_start(  2, "clear_screen_from_start", "\u001B[1J"),    
    clear_line_entire(        3, "clear_line_entire",       "\u001B[2K"),    
    clear_line_to_end(        4, "clear_line_to_end",       "\u001B[0K"),    
    clear_line_from_start(    5, "clear_line_from_start",   "\u001B[1K"),    
    normal_text(              6, "normal_text",             "\u001B[0m"),    
    default_text_color(      20, "normal_text",             "\u001B[39m"),    
    default_background_color(21, "normal_text",             "\u001B[49m"),    
    bold_text(                7, "bold_text",               "\u001B[1m"),    
    inverse_text(             8, "inverse_text",            "\u001B[7m"),    
    blink_text(               9, "blink_text",              "\u001B[5m"),    
    under_line_text(         10, "under_line_text",         "\u001B[4m"),    
    report_cursor(           11, "report_cursor",           "\u001B[6n"),    
    save_cursor(             12, "save_cursor",             "\u001B7"),    
    restore_cursor(          13, "restore_cursor",          "\u001B8"),    
    beep(                    14, "beep",                    "\u0007"),    
    frgrnd_256_color(        15, "frgrnd 256 color",        "\u001B[38;5;%dm"),    
    bkgrnd_256_color(        16, "bkgrnd 256 color",        "\u001B[48;5;%dm"),    
    put_cursor(              17, "put_cursor",              "\u001B[%d;%dH");   
 
    public static final EnumSet<ConsoleControl> CC_ALL_CONTROLS = EnumSet.allOf(ConsoleControl.class);

    private static final Map<Integer,ConsoleControl> lookup = new HashMap<Integer,ConsoleControl>();

    static 
    {
      for(ConsoleControl s : CC_ALL_CONTROLS)
           lookup.put(s.getIndex(), s);
    }
    
    private int Index;
    
    // the name of the control sequence
    private String Name;
    
    // the control sequence
    private String ControlString;
    
    private ConsoleControl(int index, String name, String controlString)
    {
      Index = index;
      Name = name;
      ControlString = controlString;
    }
    
    public static ConsoleControl getByName(String name)
    {
      ConsoleControl t = null;
      for(ConsoleControl s : CC_ALL_CONTROLS)
      {
        if(s.getName().equals(name))
          return s;
      }
      return t;
    }

    public static ConsoleControl getByIndex(int index)
    {
      ConsoleControl t = null;
      for(ConsoleControl s : CC_ALL_CONTROLS)
      {
        if(s.getIndex() == index)
          return s;
      }
      return t;
    }

    public int getIndex()
    {
      return Index;
    }

    public String getName()
    {
      return Name;
    }


    public String getControlString()
    {
      return ControlString;
    }
  };
  
  public static void restoreCursor()
  {
    System.out.print(ConsoleControl.restore_cursor.getControlString());
  }
  
  public static void beep()
  {
    System.out.print(ConsoleControl.beep.getControlString());
  }
  
  public static void saveCursor()
  {
    System.out.print(ConsoleControl.save_cursor.getControlString());
  }

  public static void putCursor(int row, int column)
  {
    // needs to be one of the clear screen controls
    System.out.print(String.format(ConsoleControl.put_cursor.getControlString(), row, column));
  }

  public static void homeCursor()
  {
    // top left position
    putCursor(1,1);
  }

  public static String getTextModeString(ConsoleControl cc)
  {
    // needs to be one of the text mode controls
    return cc.getControlString();
  }

  public static void textMode(ConsoleControl cc)
  {
    // needs to be one of the text mode controls
    System.out.print(getTextModeString(cc));
  }

  public static void textColor(ConsoleColor color)
  {
    textColor(color.getIndex());
  }

  public static void textColor(int color)
  {
    System.out.print(String.format(ConsoleControl.frgrnd_256_color.getControlString(), color));
  }

  public static String getTextColorString(ConsoleColor color)
  {
    return String.format(ConsoleControl.frgrnd_256_color.getControlString(), color.getIndex());
  }

  public static String getBackgroundColorString(ConsoleColor color)
  {
    return String.format(ConsoleControl.bkgrnd_256_color.getControlString(), color.getIndex());
  }

  public static void backgroundColor(ConsoleColor color)
  {
    backgroundColor(color.getIndex());
  }

  public static void backgroundColor(int color)
  {
    System.out.print(String.format(ConsoleControl.bkgrnd_256_color.getControlString(), color));
  }

  public static String getResetString()
  {
    return getTextModeString(ConsoleControl.normal_text);
  }

  public static void reset()
  {
    textMode(ConsoleControl.normal_text);
  }

  public static void textNormal()
  {
    Console.reset();
    textMode(ConsoleControl.default_text_color);
  }

  public static String getTextNormalString()
  {
    // a control string to append to other strings
    // which will restore the text color and attributes to
    // normal
    StringBuffer buff = new StringBuffer();
    buff.append(Console.getResetString());
    buff.append(getTextModeString(ConsoleControl.default_text_color));
    return buff.toString();
  }

  public static void textRed()
  {
    textColor(Console.ConsoleColor.n_red);
  }
  
  public static void textBlue()
  {
    textColor(Console.ConsoleColor.n_blue);
  }
  
  public static void textGreen()
  {
    textColor(Console.ConsoleColor.n_green);
  }
  
  public static void textYellow()
  {
    textColor(Console.ConsoleColor.n_yellow);
  }
  
  public static void textPink()
  {
    textColor(Console.ConsoleColor.pink);
  }
  
  public static void textMagenta()
  {
    textColor(Console.ConsoleColor.n_magenta);
  }
  
  public static void textCyan()
  {
    textColor(Console.ConsoleColor.n_cyan);
  }
  
  public static void textTan()
  {
    textColor(Console.ConsoleColor.tan);
  }
  
  public static void textBlack()
  {
    textColor(Console.ConsoleColor.black);
  }
  
  public static void textGrey()
  {
    textColor(Console.ConsoleColor.dark_grey);
  }
  
  public static void backgroundNormal()
  {
    textMode(ConsoleControl.default_background_color);
  }

  public static void bold()
  {
    textMode(ConsoleControl.bold_text);
  }

  public static void clearScreen(ConsoleControl cc)
  {
    // needs to be one of the clear screen controls
    System.out.print(cc.getControlString());
  }

  public static void clearScreen()
  {
    clearScreen(ConsoleControl.clear_screen_entire);
  }

  public static void clearLine(ConsoleControl cc)
  {
    // needs to be one of the clear line controls
    System.out.print(cc.getControlString());
  }

  public static void clearLine()
  {
    clearScreen(ConsoleControl.clear_line_entire);
  }

  public static void print(int row, int col, ConsoleControl cc, String string)
  {
    putCursor(row, col);
    if(cc != null)
      textMode(cc);
    System.out.print(string);
  }
  
  public static void print(int row, int col, String string)
  {
    print(row, col, null, string);
  }
  
  public static Dimension getScreenDimension()
  {
    Toolkit.init();
    Dimension d = new Dimension(Toolkit.getScreenWidth(), Toolkit.getScreenHeight());
    Toolkit.shutdown();
    return d;
  }
  
  public static Dimension getCursorPostition() throws IOException
  {
    // FIXME:  this blocks, waits for a CR or LF, which never happens
    //         then it returns the correct thing.
    //         Need to be able to return immediately
    Dimension d = new Dimension(1, 1);
    java.io.Console console = System.console();
    char[] posData = new char[128];
    if(console != null) 
    {
//      PrintWriter wrt = console.writer();
//      Reader rdr      = console.reader();
//      Scanner scn     = new Scanner(rdr);
      
      // attempt to write then read
//      wrt.write(ConsoleControl.report_cursor.getControlString());
//      rdr.read(posData);
      posData = console.readPassword(ConsoleControl.report_cursor.getControlString() + "\n");
      // returned in the form esc[r;cR so parse this and get just the desired values
      int row = ParserUtils.getIntValue("[", String.valueOf(posData), ";");
      int col = ParserUtils.getIntValue(";", String.valueOf(posData), "R");
      d = new Dimension(row, col);
     }
    return d;
  }
  

  /************************************************************
   * Method Name:
   *  main
   **/
  /**
   * Describe the method here
   *
   * @see     describe related java objects
   *
   * @param args
   * @throws IOException 
   ***********************************************************/
  public static void main(String[] args) throws IOException
  {
    Console.clearScreen();
    Console.homeCursor();
    Console.textMode(ConsoleControl.bold_text);
    System.out.println("Cleared Screen, repositioned to top left, and in bold");
    Console.textNormal();
    Dimension d = Console.getScreenDimension();
    System.out.println("The screen dimension is: " + d.toString());
    Console.print((int)d.getHeight()/2, (int)d.getWidth()/2, ConsoleControl.normal_text, "Middle of Screen");
    Console.saveCursor();
    Console.beep();
    Console.print(4,  3, ConsoleControl.normal_text, "Normal Text");
    Console.restoreCursor();
    System.out.println("Restored position");
    Console.bold();
    System.out.println("This should be bold green");
    Console.reset();
    System.out.println("This should be back to normal");
    for(int color = 0; color < 256; color++)
    {
      Console.textNormal();
      Console.textColor(color);
      System.out.print("Color " + color + " normal, ");
      Console.bold();
      System.out.println("Color " + color + " bold");
    }
    for(int color = 0; color < 256; color++)
    {
      Console.textNormal();
      Console.backgroundColor(color);
      System.out.print("Color " + color + " normal, ");
      Console.bold();
      System.out.println("Color " + color + " bold");
    }
    Console.textNormal();
    Console.backgroundNormal();
    System.out.println("Everything should be back to normal - forground testing");
    for(ConsoleColor c : ConsoleColor.CC_ALL_COLORS)
    {
      Console.textNormal();
      Console.textColor(c);
      System.out.print("Color " + c.getName() + " normal, ");
      Console.bold();
      System.out.println("Color " + c.getName() + " bold");
      
    }
    Console.backgroundNormal();
    Console.textNormal();
    System.out.println("Everything should be back to normal - background testing");
    for(ConsoleColor c : ConsoleColor.CC_ALL_COLORS)
    {
      Console.textNormal();
      Console.backgroundNormal();
      Console.backgroundColor(c);
      System.out.print("Color " + c.getName() + " normal, ");
      Console.bold();
      System.out.println("Color " + c.getName() + " bold");
    }
    int row = 4;
    int col = 23;
    
    System.out.println("Getting the current cursor postion (should be " +row+", "+col+")");
    Console.putCursor(row, col);
    System.out.println("Position: " + Console.getCursorPostition().toString());
    Console.reset();
    System.out.println("Position: " + Console.getCursorPostition().toString());

  }

}
