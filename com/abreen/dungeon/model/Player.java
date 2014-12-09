package com.abreen.dungeon.model;

import java.util.*;
import java.io.*;

import com.abreen.dungeon.exceptions.*;
import com.abreen.dungeon.state.*;

public class Player extends Describable implements Serializable, Stateful {
    private static final long serialVersionUID = 1L;
    
    private Room here;
    private Hashtable<String, Item> inventory;
    private PrintWriter out;
    
    public final PlayerState state;

    /**
     * The Unix timestamp referring to the last time the player made an action.
     */
    private transient long lastActionTimestamp;

    public String getDescription() {
        return this.name;
    }

    /**
     * Returns the number of seconds since the player performed an action.
     * 
     * @return The quantity of seconds
     */
    public long getNumberOfSecondsIdle() {
        return System.currentTimeMillis() / 1000L - this.lastActionTimestamp;
    }

    /**
     * Sets the last action timestamp of the player to right now.
     */
    public void updateLastAction() {
        this.lastActionTimestamp = System.currentTimeMillis() / 1000L;
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException
    {
        in.defaultReadObject();
        this.updateLastAction();
    }

    public Player(String name, Room spawn) {
        this.name = name;
        this.here = spawn;
        this.inventory = new Hashtable<String, Item>();
        this.updateLastAction();
        
        this.state = new PlayerState();
        
        /*
         * Get your free watches here!
         */
        Watch w = new Watch("watch", "A pretty basic, battery-powered watch.");
        this.addToInventory(w);
    }

    public Player(String name, Room spawn, PrintWriter out) {
        this(name, spawn);
        this.setWriter(out);
    }
    
    public String toString() {
        return this.name + " (in " + this.here.getName() + ")";
    }

    public int getInventorySize() {
        return this.inventory.size();
    }

    public Iterator<Item> getInventoryIterator() {
        return this.inventory.values().iterator();
    }

    public Item dropFromInventory(Item i) throws NoSuchItemException {
        if (!this.inventory.contains(i))
            throw new NoSuchItemException();

        return this.inventory.remove(i.getName().toLowerCase());
    }

    public Item dropFromInventoryByName(String name) throws NoSuchItemException {
        Item i = this.inventory.remove(name.toLowerCase());

        if (i == null)
            throw new NoSuchItemException();

        return i;
    }

    public void addToInventory(Item i) {
        this.inventory.put(i.getName().toLowerCase(), i);
    }

    public Item getFromInventoryByName(String name) throws NoSuchItemException {
        Item i = this.inventory.get(name.toLowerCase());

        if (i == null)
            throw new NoSuchItemException();

        return i;
    }

    public Room here() {
        return this.here;
    }

    public void move(Room r) {
        if (r == null)
            throw new IllegalArgumentException("cannot move to null room");

        this.here = r;
    }

    public PrintWriter getWriter() {
        return this.out;
    }

    public void setWriter(PrintWriter out) {
        if (this.out != null)
            throw new RuntimeException("writer already set");

        if (out == null)
            throw new IllegalArgumentException("writer must be non-null");

        this.out = out;
    }

    /*
     * Removes the output stream writer for this player. This method does not
     * close the writer's stream!
     */
    public void unsetWriter() {
        if (this.out == null)
            throw new RuntimeException("writer already null");

        this.out = null;
    }
    
    public void tick() {
        state.fatigue++;
        state.hunger++;
        state.thirst++;
    }
}
