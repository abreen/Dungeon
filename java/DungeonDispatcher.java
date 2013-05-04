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

  /* Base abstract class for all events. */
  public abstract class Event {
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
  public class NarrationEvent extends Event {
    public NarrationEvent(PrintWriter[] w, String s) {
      super(w, s);
    }
  }

  /* Notification event for one or more players (prefixed by the server
   * chevrons). */
  public class NotificationEvent extends Event {
    public NotificationEvent(PrintWriter[] w, String s) {
      super(w, s);
    }

    public String toString() {
      return CHEVRONS + this.output;
    }
  }

  /* Server-wide notification event for all connected players (prefixed
   * by server asterisks). */
  public class ServerNotificationEvent extends Event {
    public ServerNotificationEvent(String s) {
      super(DungeonServer.universe.getAllPlayerWriters(), s);
    }

    public String toString() {
      return ASTERISKS + this.output;
    }
  }

  /* Server-wide error event for all connected players (prefixed by
   * exclamation points), only used when the server is starting, stopping,
   * or must halt due to an error. */
  public class ServerErrorEvent extends ServerNotificationEvent {
    public ServerErrorEvent(String s) {
      super(s);
    }

    public String toString() {
      return BANGS + this.output;
    }
  }

  private LinkedBlockingQueue<Event> eventQueue;
  
  public DungeonDispatcher() {
    this.eventQueue = new LinkedBlockingQueue<Event>();
  }

  public synchronized void addEvent(Event event) {
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
