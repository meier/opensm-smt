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
 *        file: MessageManager.java
 *
 *  Created on: Aug 6, 2015
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.smt.manager;

import gov.llnl.lc.logging.CommonLogger;
import gov.llnl.lc.smt.event.SmtMessage;
import gov.llnl.lc.smt.event.SmtMessageListener;
import gov.llnl.lc.smt.event.SmtMessageType;

/**********************************************************************
 * The MessageManager is a one to many message broadcaster. Almost anything can
 * post a message to this manager, and it will be broadcast to all of the
 * registered listeners.
 * 
 * This is a singleton, which means it is a globally shared object, easily
 * obtainable through the getInstance() method.
 * <p>
 * 
 * @see related classes and interfaces
 * 
 * @author meier3
 * 
 * @version Aug 21, 2013 12:03:33 PM
 **********************************************************************/
public class MessageManager implements SmtMessageUpdater, CommonLogger
{

  /** the one and only <code>MessageManager</code> Singleton **/
  private volatile static MessageManager                 gMessageMgr       = null;

  /** the synchronization object **/
  private static Boolean                                 semaphore         = new Boolean(true);

  /** a list of Listeners, interested in knowing when Message Events happened **/
  private static java.util.ArrayList<SmtMessageListener> Message_Listeners = new java.util.ArrayList<SmtMessageListener>();

  /** logger for the class **/
  private final java.util.logging.Logger                 classLogger       = java.util.logging.Logger.getLogger(getClass().getName());

  /** A collection of Message Objects **/
  protected static java.util.LinkedList <SmtMessage> MessageList;

  private static SmtMessage currentMsg;

  /** an ever increasing number (reveal duplicates?) **/
  private static int MsgNumber = 0;

  /** the maximum number of messages to save in the queue **/
  private static int MaxNumMessages = 500;
  
  private static boolean TopToBottom       = true;
  private static boolean IncludeTimeStamps = true;
  private static boolean IncludeMsgType    = true;
  
  private static SmtMessageType MsgLevel   = SmtMessageType.SMT_MSG_INIT;
  

  /************************************************************
   * Method Name: GraphSelectionManager
   **/
  /**
   * Describe the constructor here
   * 
   * @see describe related java objects
   * 
   ***********************************************************/
  private MessageManager()
  {
    super();
    initManager();
  }

  /**************************************************************************
   *** Method Name: getInstance
   **/
  /**
   *** Get the singleton MessageManager. This can be used if the application wants
   * to share one manager across the whole JVM. Currently I am not sure how this
   * ought to be used.
   *** <p>
   *** 
   *** @return the GLOBAL (or shared) MessageManager
   **************************************************************************/

  public static MessageManager getInstance()
  {
    synchronized (MessageManager.semaphore)
    {
      if (gMessageMgr == null)
      {
        gMessageMgr = new MessageManager();
      }
      return gMessageMgr;
    }
  }

  /*-----------------------------------------------------------------------*/

  public Object clone() throws CloneNotSupportedException
  {
    throw new CloneNotSupportedException();
  }

  protected boolean initManager()
  {
    MessageList = new java.util.LinkedList<SmtMessage>();
    return true;
  }

  public String getName()
  {
    return this.getClass().getSimpleName();
  }
  
  public synchronized SmtMessage getMessage()
  {
    return currentMsg; 
  }

  public synchronized java.util.LinkedList <SmtMessage> getMessages()
  {
    // return a list of messages
    return MessageList;
  }
  
  public synchronized void flushMessages()
  {
    // clear the list of messages
    MessageList.clear();  // not able to add new ones?
  }


  public synchronized int getNumListeners()
  {
    return Message_Listeners.size();
  }
   
  @Override
  public synchronized boolean addMessageListener(SmtMessageListener listener)
  {
    classLogger.info("adding message listener");
    if (listener != null)
    {
      Message_Listeners.add(listener);
    }
    return true;
  }

  @Override
  public synchronized boolean removeMessageListener(SmtMessageListener listener)
  {
    classLogger.info("removing message listener");
    if (Message_Listeners.remove(listener))
    {
    }
    return true;
  }

  @Override
  public synchronized void updateAllListeners(SmtMessage message)
  {
    if(Message_Listeners.size() < 1)
      logger.severe("No one listening ("+message.toString(true, true)+")");
    for (int i = 0; i < Message_Listeners.size(); i++)
    {
      SmtMessageListener listener = (SmtMessageListener) Message_Listeners.get(i);
      if(listener != null)
        listener.messageUpdate(this, message);
    }
  }
  
  public synchronized void postMessage(SmtMessage message)
  {
    // add this puppy to the list of messages
    MessageList.add(message);
    
    // remove the oldest, which should be index 0, if the list is full
    if(MessageList.size() > MaxNumMessages)
      MessageList.remove(0);
    currentMsg = message;
    MsgNumber++;

    updateAllListeners(message);
  }

  public void setRecentMsgOnTop(boolean selected)
  {
    TopToBottom = selected;
  }

  public boolean isRecentMsgOnTop()
  {
    return TopToBottom;
  }

  public void includeTimeStamps(boolean selected)
  {
    IncludeTimeStamps = selected;
  }

  public boolean isTimeStampsIncluded()
  {
    return IncludeTimeStamps;
  }

  public void includeMsgType(boolean selected)
  {
    IncludeMsgType = selected;
  }

  public boolean isMsgTypeIncluded()
  {
    return IncludeMsgType;
  }
  
  public SmtMessageType getMsgLevel()
  {
    return MsgLevel;
  }

  public void setMsgLevel(SmtMessageType level)
  {
    MsgLevel = level;
  }


}
