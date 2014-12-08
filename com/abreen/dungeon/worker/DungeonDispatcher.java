package com.abreen.dungeon.worker;

import java.util.*;
import java.util.concurrent.*;
import java.io.*;

import com.abreen.dungeon.DungeonServer;
import com.abreen.dungeon.model.Player;

/**
 * The DungeonDispatcher class maintains an event queue that stores and
 * distributes events (strings intended as notifications, server notices, or
 * narration) to connected players. When the server or universe needs to send
 * strings to players, they are queued here before being sent over the network.
 */
public class DungeonDispatcher extends Thread {
    public static final String CHEVRONS = ">>> "; // used for notifications
    public static final String ASTERISKS = "*** "; // used for server notices
    public static final String BANGS = "!!! "; // used for server errors

    private static final String SERVER_CLOSING_MESSAGE = "Server closing...";
    private static final String SERVER_RESTART_MESSAGE = "Server restarting...";

    /**
     * Converts a player iterator (usually produced by methods from
     * DungeonUniverse) to an array containing the player's writers (an ideal
     * form for the inner classes of DungeonDispatcher).
     * 
     * @param it The player iterator with which to select players
     * @param size The number of players over which to iterate
     * @return An array containing all the players' writers
     */
    public static PrintWriter[] playerIteratorToWriterArray(
            Iterator<Player> it, int size)
    {
        PrintWriter[] arr = new PrintWriter[size];

        int i = 0;
        while (it.hasNext())
            arr[i++] = it.next().getWriter();

        return arr;
    }

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
     * 
     * @param w The array of PrintWriters to which to send the message
     * @param s The event message (in a StringBuilder)
     */
    public void addNarrationEvent(PrintWriter[] w, StringBuilder buf) {
        this.addEvent(new NarrationEvent(w, buf.toString()));
    }
    
    /**
     * Adds a narration event to the specified writers with the specified
     * message.
     * 
     * @param w The array of PrintWriters to which to send the message
     * @param s The event message (as a string)
     */
    public void addNarrationEvent(PrintWriter[] w, String s) {
        this.addEvent(new NarrationEvent(w, s));
    }

    /**
     * Adds a narration event to just one writer with the specified message.
     * 
     * @param w The PrintWriter to which to send the message
     * @param s The event message (in a StringBuilder)
     */
    public void addNarrationEvent(PrintWriter w, StringBuilder buf) {
        PrintWriter[] tempWriter = { w };
        this.addNarrationEvent(tempWriter, buf.toString());
    }
    
    /**
     * Adds a narration event to just one writer with the specified message.
     * 
     * @param w The PrintWriter to which to send the message
     * @param s The event message (as a String)
     */
    public void addNarrationEvent(PrintWriter w, String s) {
        PrintWriter[] tempWriter = { w };
        this.addNarrationEvent(tempWriter, s);
    }

    /**
     * The player-specific notification event that sends a message to one or
     * several players. The message is prefixed by a string defined by the
     * constant DungeonDispatcher.CHEVRONS.
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
     * 
     * @param w The array of PrintWriters to which to send the message
     * @param s The event message (in a StringBuilder)
     */
    public void addNotificationEvent(PrintWriter[] w, StringBuilder buf) {
        this.addEvent(new NotificationEvent(w, buf.toString()));
    }
    
    /**
     * Adds a notification event to the specified writers with the specified
     * message.
     * 
     * @param w The array of PrintWriters to which to send the message
     * @param s The event message (as a String)
     */
    public void addNotificationEvent(PrintWriter[] w, String s) {
        this.addEvent(new NotificationEvent(w, s));
    }

    /**
     * Adds a notification event to just one writer with the specified message.
     * 
     * @param w The PrintWriter to which to send the message
     * @param s The event message (in a StringBuilder)
     */
    public void addNotificationEvent(PrintWriter w, StringBuilder buf) {
        PrintWriter[] temp = { w };
        this.addNotificationEvent(temp, buf);
    }
    
    /**
     * Adds a notification event to just one writer with the specified message.
     * 
     * @param w The PrintWriter to which to send the message
     * @param s The event message (as a String)
     */
    public void addNotificationEvent(PrintWriter w, String s) {
        PrintWriter[] temp = { w };
        this.addNotificationEvent(temp, s);
    }

    /**
     * The server-wide notification event that sends a message to all connected
     * players. The message is prefixed by a string defined by the constant
     * DungeonDispatcher.ASTERISKS.
     * 
     * @see DungeonDispatcher.ASTERISKS
     */
    private class ServerNotificationEvent extends Event {
        public ServerNotificationEvent(String s) {
            super(playerIteratorToWriterArray(
                    DungeonServer.universe.getPlayers(),
                    DungeonServer.universe.getNumberOfPlayers()), s);
        }

        public String toString() {
            return ASTERISKS + this.output;
        }
    }

    /**
     * Adds a server notification event with the specified message.
     * 
     * @param s The event message (in a StringBuilder)
     */
    public void addServerNotificationEvent(StringBuilder buf) {
        this.addEvent(new ServerNotificationEvent(buf.toString()));
    }
    
    /**
     * Adds a server notification event with the specified message.
     * 
     * @param s The event message (as a String)
     */
    public void addServerNotificationEvent(String s) {
        this.addEvent(new ServerNotificationEvent(s));
    }

    /**
     * The server-wide error event that notifies all connected players, only
     * used when the server is starting, stopping, or must halt due to an error.
     * The message is prefixed by a string defined by the constant
     * DungeonDispatcher.BANGS.
     * 
     * @see DungeonDispatcher.BANGS
     */
    private class ServerErrorEvent extends ServerNotificationEvent {
        public ServerErrorEvent(String s) {
            super(s);
        }

        public String toString() {
            return BANGS + super.toString();
        }
    }
    
    /**
     * Adds a server error event with the specified message.
     * 
     * @param s The event message (in a StringBuilder)
     */
    public void addServerErrorEvent(StringBuilder buf) {
        this.addEvent(new ServerErrorEvent(buf.toString()));
    }

    /**
     * Adds a server error event with the specified message.
     * 
     * @param s The event message (as a String)
     */
    public void addServerErrorEvent(String s) {
        this.addEvent(new ServerErrorEvent(s));
    }

    /**
     * Automatically inform all connected players that the server is immediately
     * closing.
     */
    public void addServerClosingEvent() {
        this.addEvent(new ServerNotificationEvent(SERVER_CLOSING_MESSAGE));
    }

    /**
     * Automatically inform all connected players that the server will restart.
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
        while (true)
            try {
                Event event = this.eventQueue.take();
                logEvent(event);
                
                PrintWriter[] writers = event.getWriters();
                for (PrintWriter writer : writers) {
                    // println() should automatically flush
                    String s = DungeonServer.narrator.prettify(event.toString());
                    writer.println(s);
                }

            } catch (InterruptedException e) {
                System.out.println("event queue got interrupt");
                return;
            }
    }
    
    private static void logEvent(Event e) {
        String cls = e.getClass().getSimpleName();
        String s = e.toString();
        
        if (!(e instanceof NarrationEvent))
            s = s.substring(0, Math.min(60, s.length()));
        
        System.out.printf("← %s (%s)\n", s, cls);
    }

}
