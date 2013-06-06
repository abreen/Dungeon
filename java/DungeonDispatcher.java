import java.util.*;
import java.util.concurrent.*;
import java.io.*;

import dungeon.*;

/*
 * The DungeonDispatcher class maintains an event queue that
 * stores and distributes events to connected players. Notifications
 * and narration triggered by the server or other players come here
 * to be sent to their destinations.
 *
 * Methods that mutate the event queue are synchronized.
 */
public class DungeonDispatcher extends Thread {
  private static final String CHEVRONS = ">>> ";    // used for notifications
  private static final String ASTERISKS = "*** ";   // used for server notices
  private static final String BANGS = "!!! ";       // used for server errors

  private static final String SERVER_CLOSING_MESSAGE = "Server closing...";
  private static final String SERVER_RESTART_MESSAGE = "Server restarting...";

  /* Base abstract class for all events. */
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
  
  /* Narration event for one or more players. */
  private class NarrationEvent extends Event {
    public NarrationEvent(PrintWriter[] w, String s) {
      super(w, s);
    }
  }

  public void addNarrationEvent(PrintWriter[] w, String s) {
    this.addEvent(new NarrationEvent(w, s));
  }

  /* Notification event for one or more players (prefixed by the server
   * chevrons). */
  private class NotificationEvent extends Event {
    public NotificationEvent(PrintWriter[] w, String s) {
      super(w, s);
    }

    public String toString() {
      return CHEVRONS + this.output;
    }
  }

  public void addNotificationEvent(PrintWriter[] w, String s) {
    this.addEvent(new NotificationEvent(w, s));
  }

  /* Server-wide notification event for all connected players (prefixed
   * by server asterisks). */
  private class ServerNotificationEvent extends Event {
    public ServerNotificationEvent(String s) {
      super(DungeonServer.universe.getAllPlayerWriters(), s);
    }

    public String toString() {
      return ASTERISKS + this.output;
    }
  }

  public void addServerNotificationEvent(String s) {
    this.addEvent(new ServerNotificationEvent(s));
  }

  /* Server-wide error event for all connected players (prefixed by
   * exclamation points), only used when the server is starting, stopping,
   * or must halt due to an error. */
  private class ServerErrorEvent extends ServerNotificationEvent {
    public ServerErrorEvent(String s) {
      super(s);
    }

    public String toString() {
      return BANGS + this.output;
    }
  }

  public void addServerErrorEvent(String s) {
    this.addEvent(new ServerErrorEvent(s));
  }

  public void addServerClosingEvent() {
    this.addEvent(new ServerNotificationEvent(SERVER_CLOSING_MESSAGE));
  }

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

  /*
   * Waits for incoming events and dispatches them in order.
   */
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
