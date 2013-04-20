package dungeon;

import java.util.*;
import java.io.*;

import dungeon.exceptions.*;

/* TODO: Add description to player */

public class Player implements Describable, Serializable {
  private String name;
  private Room here;
  private Hashtable<String, Item> inventory;
  private PrintWriter out;

  public boolean wantsQuit;

  public String describe() {
    return this.name;
  }

  public Player(String name, Room spawn) {
    this.name = name;
    this.here = spawn;
    this.inventory = new Hashtable<String, Item>();
  }

  public Player(String name, Room spawn, PrintWriter out) {
    this(name, spawn);
    this.setWriter(out);
  }

  public Iterator<Item> getInventoryIterator() {
    return this.inventory.values().iterator();
  }

  public void dropFromInventory(Item i) throws NoSuchItemException {
    if (!this.inventory.contains(i))
      throw new NoSuchItemException();

    this.inventory.remove(i.getName());
  }

  public void addToInventory(Item i) {
    this.inventory.put(i.getName(), i);
  }

  public Room here() { return this.here; }

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
   * Removes the output stream writer for this player. This method
   * does not close the writer's stream!
   */
  public void unsetWriter() {
    if (this.out == null)
      throw new RuntimeException("writer already null");

    this.out = null;
  }
}
