import java.util.*;
import java.util.concurrent.*;
import java.io.*;

import dungeon.*;

/**
 * The DungeonDispatcher class maintains an event queue that stores and
 * distributes events (strings intended as notifications, server notices, or
 * narration) to connected players. When the server or universe needs to send
 * strings to players, they are queued here before being sent over the network.
 */
public class DungeonDispatcher extends Thread {
  private static final String CHEVRONS = ">>> ";    // used for notifications
  private static final String ASTERISKS = "*** ";   // used for server notices
  private static final String BANGS = "!!! ";       // used for server errors

  private static final String SERVER_CLOSING_MESSAGE = "Server closing...";
  private static final String SERVER_RESTART_MESSAGE = "Server restarting...";

  /**
   * Base abstract class for all dispatcher events.
   */
  private abstract class Event {
    protected PrintWriter[] writers;
    protected String output;
    
    public Event(PrintWriter[] w, String s) {
      this.writers = w;
      this.output = s;
    }

    public String toString() {
      return this.output;
    }

    public PrintWriter[] getWriters() {
      return this.writers;
    }
  }
  
  /**
   * The player-specific narration event that sends a message to one or
   * several players. The message is narration and is not prefixed with any
   * strings.
   */
  private class NarrationEvent extends Event {
    public NarrationEvent(PrintWriter[] w, String s) {
      super(w, s);
    }
  }

  /**
   * Adds a narration event to the specified writers with the specified
   * message.
   * @param w The array of PrintWriters to which to send the message
   * @param s The event message
   */
  public void addNarrationEvent(PrintWriter[] w, String s) {
    this.addEvent(new NarrationEvent(w, s));
  }
  
  /**
   * Adds a narration event to just one writer with the specified message.
   * @param w The PrintWriter to which to send the message
   * @param s The event message
   */
  public void addNarrationEvent(PrintWriter w, String s) {
    PrintWriter[] temp = new PrintWriter[1];
    temp[0] = w;
    this.addNarrationEvent(temp, s);
  }

  /**
   * The player-specific notification event that sends a message to one
   * or several players. The message is prefixed by a string defined by
   * the constant DungeonDispatcher.CHEVRONS.
   * 
   * @see DungeonDispatcher.CHEVRONS
   */
  private class NotificationEvent extends Event {
    public NotificationEvent(PrintWriter[] w, String s) {
      super(w, s);
    }

    public String toString() {
      return CHEVRONS + this.output;
    }
  }

  /**
   * Adds a notification event to the specified writers with the specified
   * message.
   * @param w The array of PrintWriters to which to send the message
   * @param s The event message
   */
  public void addNotificationEvent(PrintWriter[] w, String s) {
    this.addEvent(new NotificationEvent(w, s));
  }

  /**
   * Adds a notification event to just one writer with the specified
   * message.
   * @param w The PrintWriter to which to send the message
   * @param s The event message
   */
  public void addNotificationEvent(PrintWriter w, String s) {
    PrintWriter[] temp = new PrintWriter[1];
    temp[0] = w;
    this.addNotificationEvent(temp, s);
  }
  
  /**
   * The server-wide notification event that sends a message to all
   * connected players. The message is prefixed by a string defined by
   * the constant DungeonDispatcher.ASTERISKS.
   * 
   * @see DungeonDispatcher.ASTERISKS
   */
  private class ServerNotificationEvent extends Event {
    public ServerNotificationEvent(String s) {
      super(DungeonServer.universe.getAllPlayerWriters(), s);
    }

    public String toString() {
      return ASTERISKS + this.output;
    }
  }

  /**
   * Adds a server notification event with the specified message.
   * @param s The event message
   */
  public void addServerNotificationEvent(String s) {
    this.addEvent(new ServerNotificationEvent(s));
  }

  /**
   * The server-wide error event that notifies all connected players, only
   * used when the server is starting, stopping, or must halt due to an
   * error. The message is prefixed by a string defined by the constant
   * DungeonDispatcher.BANGS.
   * 
   * @see DungeonDispatcher.BANGS
   */
  private class ServerErrorEvent extends ServerNotificationEvent {
    public ServerErrorEvent(String s) {
      super(s);
    }

    public String toString() {
      return BANGS + this.output;
    }
  }

  /**
   * Adds a server error event with the specified message.
   * @param s The event message
   */
  public void addServerErrorEvent(String s) {
    this.addEvent(new ServerErrorEvent(s));
  }

  /**
   * Automatically inform all connected players that the server is
   * immediately closing.
   */
  public void addServerClosingEvent() {
    this.addEvent(new ServerNotificationEvent(SERVER_CLOSING_MESSAGE));
  }

  /**
   * Automatically inform all connected players that the server will
   * restart.
   */
  public void addServerRestartEvent() {
    this.addEvent(new ServerNotificationEvent(SERVER_RESTART_MESSAGE));
  }

  private LinkedBlockingQueue<Event> eventQueue;
  
  public DungeonDispatcher() {
    this.eventQueue = new LinkedBlockingQueue<Event>();
  }

  private synchronized void addEvent(Event event) {
    try { 
      this.eventQueue.put(event);
    } catch (InterruptedException e) {
      return;
    }
  }
  
  public void run() {
    while (true) {
      try { 
        Event e = this.eventQueue.take();
        PrintWriter[] w = e.getWriters();
        for (int i = 0; i < w.length; i++) {
          w[i].println(e.toString());
        }

      } catch (InterruptedException e) {
        System.out.println("event queue got interrupt");
        return;
      }
    }
  }

}
