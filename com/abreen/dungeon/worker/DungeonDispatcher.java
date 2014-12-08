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

    private static final StringBuffer SERVER_CLOSING_MESSAGE =
            new StringBuffer("Server closing...");
    
    private static final StringBuffer SERVER_RESTART_MESSAGE =
            new StringBuffer("Server restarting...");

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
        protected StringBuffer output;

        public Event(PrintWriter[] w, StringBuffer buf) {
            this.writers = w;
            this.output = buf;
        }

        public String toString() {
            return this.output.toString();
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
        public NarrationEvent(PrintWriter[] w, StringBuffer buf) {
            super(w, buf);
        }
    }

    /**
     * Adds a narration event to the specified writers with the specified
     * message.
     * 
     * @param w The array of PrintWriters to which to send the message
     * @param s The event message (in a StringBuffer)
     */
    public void addNarrationEvent(PrintWriter[] w, StringBuffer buf) {
        this.addEvent(new NarrationEvent(w, buf));
    }
    
    /**
     * Adds a narration event to the specified writers with the specified
     * message.
     * 
     * @deprecated Use the StringBuffer version instead
     * 
     * @param w The array of PrintWriters to which to send the message
     * @param s The event message (as a string)
     */
    public void addNarrationEvent(PrintWriter[] w, String s) {
        StringBuffer tempBuf = new StringBuffer(s);
        this.addEvent(new NarrationEvent(w, tempBuf));
    }

    /**
     * Adds a narration event to just one writer with the specified message.
     * 
     * @param w The PrintWriter to which to send the message
     * @param s The event message (in a StringBuffer)
     */
    public void addNarrationEvent(PrintWriter w, StringBuffer buf) {
        PrintWriter[] tempWriter = { w };
        this.addNarrationEvent(tempWriter, buf);
    }
    
    /**
     * Adds a narration event to just one writer with the specified message.
     * 
     * @deprecated Use the StringBuffer version instead
     * 
     * @param w The PrintWriter to which to send the message
     * @param s The event message (as a String)
     */
    public void addNarrationEvent(PrintWriter w, String s) {
        PrintWriter[] tempWriter = { w };
        StringBuffer tempBuf = new StringBuffer(s);
        this.addNarrationEvent(tempWriter, tempBuf);
    }

    /**
     * The player-specific notification event that sends a message to one or
     * several players. The message is prefixed by a string defined by the
     * constant DungeonDispatcher.CHEVRONS.
     * 
     * @see DungeonDispatcher.CHEVRONS
     */
    private class NotificationEvent extends Event {
        public NotificationEvent(PrintWriter[] w, StringBuffer buf) {
            super(w, buf);
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
     * @param s The event message (in a StringBuffer)
     */
    public void addNotificationEvent(PrintWriter[] w, StringBuffer buf) {
        this.addEvent(new NotificationEvent(w, buf));
    }
    
    /**
     * Adds a notification event to the specified writers with the specified
     * message.
     * 
     * @deprecated Use the StringBuffer version instead
     * 
     * @param w The array of PrintWriters to which to send the message
     * @param s The event message (as a String)
     */
    public void addNotificationEvent(PrintWriter[] w, String s) {
        StringBuffer tempBuf = new StringBuffer(s);
        this.addEvent(new NotificationEvent(w, tempBuf));
    }

    /**
     * Adds a notification event to just one writer with the specified message.
     * 
     * @param w The PrintWriter to which to send the message
     * @param s The event message (in a StringBuffer)
     */
    public void addNotificationEvent(PrintWriter w, StringBuffer buf) {
        PrintWriter[] temp = { w };
        this.addNotificationEvent(temp, buf);
    }
    
    /**
     * Adds a notification event to just one writer with the specified message.
     * 
     * @deprecated Use the StringBuffer version instead
     * 
     * @param w The PrintWriter to which to send the message
     * @param s The event message (as a String)
     */
    public void addNotificationEvent(PrintWriter w, String s) {
        PrintWriter[] temp = { w };
        StringBuffer tempBuf = new StringBuffer(s);
        this.addNotificationEvent(temp, tempBuf);
    }

    /**
     * The server-wide notification event that sends a message to all connected
     * players. The message is prefixed by a string defined by the constant
     * DungeonDispatcher.ASTERISKS.
     * 
     * @see DungeonDispatcher.ASTERISKS
     */
    private class ServerNotificationEvent extends Event {
        public ServerNotificationEvent(StringBuffer buf) {
            super(playerIteratorToWriterArray(
                    DungeonServer.universe.getPlayers(),
                    DungeonServer.universe.getNumberOfPlayers()), buf);
        }

        public String toString() {
            return ASTERISKS + this.output;
        }
    }

    /**
     * Adds a server notification event with the specified message.
     * 
     * @param s The event message (in a StringBuffer)
     */
    public void addServerNotificationEvent(StringBuffer buf) {
        this.addEvent(new ServerNotificationEvent(buf));
    }
    
    /**
     * Adds a server notification event with the specified message.
     * 
     * @deprecated Use the StringBuffer version instead
     * 
     * @param s The event message (as a String)
     */
    public void addServerNotificationEvent(String s) {
        StringBuffer tempBuf = new StringBuffer(s);
        this.addEvent(new ServerNotificationEvent(tempBuf));
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
        public ServerErrorEvent(StringBuffer buf) {
            super(buf);
        }

        public String toString() {
            return BANGS + super.toString();
        }
    }
    
    /**
     * Adds a server error event with the specified message.
     * 
     * @param s The event message (in a StringBuffer)
     */
    public void addServerErrorEvent(StringBuffer buf) {
        this.addEvent(new ServerErrorEvent(buf));
    }

    /**
     * Adds a server error event with the specified message.
     * 
     * @deprecated Use the StringBuffer version instead
     * 
     * @param s The event message (as a String)
     */
    public void addServerErrorEvent(String s) {
        StringBuffer tempBuf = new StringBuffer(s);
        this.addEvent(new ServerErrorEvent(tempBuf));
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

}
