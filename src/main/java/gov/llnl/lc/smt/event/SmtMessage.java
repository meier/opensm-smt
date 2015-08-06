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
 *        file: SmtMessage.java
 *
 *  Created on: Sep 30, 2013
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.event;

import gov.llnl.lc.smt.SmtConstants;
import gov.llnl.lc.time.TimeStamp;

/**********************************************************************
 * Describe purpose and responsibility of SmtMessage
 * <p>
 * @see  related classes and interfaces
 *
 * @author meier3
 * 
 * @version Sep 30, 2013 11:34:23 AM
 **********************************************************************/
public class SmtMessage extends SmtEventObject
{
  private String MessageTime;
    

  public SmtMessage(String message)
  {
    this(SmtMessageType.SMT_MSG_INFO, message, null);
  }

  public SmtMessage(SmtMessageType type, String message)
  {
    this(type, message, null);
  }

  public SmtMessage(SmtMessageType type, String message, Object source)
  {
    super(SmtEvent.SMT_EVENT_MESSAGE, source, type, message);
    MessageTime = new TimeStamp().toString();
  }

  /************************************************************
   * Method Name:
   *  getMessage
   **/
  /**
   * Returns the value of message
   *
   * @return the message
   *
   ***********************************************************/
  
  public String getMessage()
  {
    return (String)(this.getUserObject());
  }

  /************************************************************
   * Method Name:
   *  getMessageTime
   **/
  /**
   * Returns the value of messageTime
   *
   * @return the messageTime
   *
   ***********************************************************/
  
  public String getMessageTime()
  {
    return MessageTime;
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
  
  public SmtMessageType getType()
  {
    return (SmtMessageType)(this.getContext());
  }

  public static String getContent(java.util.LinkedList<SmtMessage> messageList,
      boolean timeStamped, boolean includeType, boolean recentFirst)
  {
    // craft a pretty html version of the message
    StringBuffer buff = new StringBuffer();

    buff.append(SmtConstants.NO_FORMAT_START);

    java.util.Iterator<SmtMessage> it = messageList.iterator();
    SmtMessage msg = null;

    // the normal natural order is most recent is appended at the end
    // so "recentFirst" means reverse the order
    if (recentFirst)
      it = messageList.descendingIterator();

    while (it.hasNext())
    {
      msg = it.next();
      buff.append(SmtConstants.MEDIUM_FONT);
      buff.append(msg.getContentInternal(timeStamped, includeType) + SmtConstants.NEW_LINE);
    }
    buff.append(SmtConstants.NO_FORMAT_END);

    return buff.toString();
  }

  public static String toStringList(java.util.LinkedList <SmtMessage> messageList, boolean timeStamped, boolean includeType, boolean recentFirst)
  {
    // craft a pretty html version of the message
    StringBuffer buff = new StringBuffer();
    
  java.util.Iterator <SmtMessage> it = messageList.iterator();
  SmtMessage msg = null;
   
  // the normal natural order is most recent is appended at the end
  // so "recentFirst" means reverse the order
  if(recentFirst)
    it = messageList.descendingIterator();
  
  while ( it.hasNext() )
  {
    msg = it.next();
    buff.append(msg.toString(timeStamped, includeType) + SmtConstants.NEW_LINE);
  }
     return buff.toString();  
  }

  private String getContentInternal(boolean timeStamped, boolean includeType)
  {
    // craft a pretty html version of the message ([time] type: message
    StringBuffer buff = new StringBuffer();

    String msgStr = getMessage();

    if (timeStamped)
      buff.append("[" + getMessageTime().toString() + "] ");

    if (includeType)
      buff.append(getType().getMessageName() + ": ");

    // if severe message, make red
    if (getType() == SmtMessageType.SMT_MSG_SEVERE)
      msgStr = SmtConstants.RED_FONT + msgStr + SmtConstants.END_FONT;

    // if severe message, make red
    if (getType() == SmtMessageType.SMT_MSG_WARNING)
      msgStr = SmtConstants.ORANGE_FONT + msgStr + SmtConstants.END_FONT;

    // if severe message, make red
    if (getType() == SmtMessageType.SMT_MSG_DEBUG)
      msgStr = SmtConstants.GREEN_FONT + msgStr + SmtConstants.END_FONT;

    buff.append(msgStr);

    return buff.toString();
  }

  public String getContent(boolean timeStamped, boolean includeType)
  {
    // craft a pretty html version of the message ([time] type: message
    StringBuffer buff = new StringBuffer();
    
    buff.append(SmtConstants.NO_FORMAT_START);
    buff.append(SmtConstants.MEDIUM_FONT);
    buff.append(getContentInternal(timeStamped, includeType));
    buff.append(SmtConstants.NO_FORMAT_END);

    return buff.toString();  
  }

  public String getContent()
  {
    return getContent(false, false);  
  }

  public String toString(boolean timeStamped, boolean includeType)
  {
    StringBuffer buff = new StringBuffer();

    String msgStr = getMessage();

    if (timeStamped)
      buff.append("[" + getMessageTime().toString() + "] ");

    if (includeType)
      buff.append(getType().getMessageName() + ": ");

    buff.append(msgStr);

    return buff.toString();
  }

  public String toString()
  {
    return toString(false, false);  
  }

}
