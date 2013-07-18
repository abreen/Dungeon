package com.abreen.dungeon.model;

import com.abreen.dungeon.DungeonServer;
import java.util.*;
import com.abreen.dungeon.exceptions.*;
import com.abreen.dungeon.worker.DungeonNarrator;

public class Room extends Space {
  public static final int DEFAULT_ITEMS_SIZE = 11;
  public static final int DEFAULT_PLAYERS_SIZE = 36;

  private Hashtable<String, Item> items;
  private Hashtable<String, Player> players;

  public void addItem(Item i) {
    this.items.put(i.getName().toLowerCase(), i);
  }
  
  public int getNumberOfItems() {
    return this.items.size();
  }
  
  public boolean hasNoItems() {
    return this.items.isEmpty();
  }
  
  public Collection<Item> getItems() {
    return this.items.values();
  }

  public Item removeItemByName(String name) throws NoSuchItemException {
    Item i = this.items.remove(name.toLowerCase());
    
    if (i == null)
      throw new NoSuchItemException();

    return i;
  }

  public Item getItemByName(String name) throws NoSuchItemException {
    Item i = this.items.get(name.toLowerCase());

    if (i == null)
      throw new NoSuchItemException();

    return i;
  }

  public void addPlayer(Player p) {
    this.players.put(p.getName(), p);
  }

  public void removePlayer(Player p) {
    this.players.remove(p.getName());
  }

  public Iterator<Player> getPlayers() {
    return this.players.values().iterator();
  }
  
  public int getNumberOfPlayers() { return this.players.size(); }

  /*
   * Adds a door between this room and 'dest'. Uses 'forward' as
   * exit direction from this room to the next room. Uses 'backward'
   * as exit direction from 'dest' to this room. Locks the door using 'k'.
   */
  public Door addDoor(Direction forward, Room dest, Direction back, Key k) {
    Door d = this.addDoor(forward, dest, k);
    
    /* Add exit from door to this room */
    d.addExit(back, this);

    /* Add exit from destination room to door */
    dest.addExit(back, d);

    return d;
  }

  /*
   * Adds a one-way door from this room to 'dest'. Uses 'forward' as
   * exit direction from this room to the next room. Locks door with 'k'.
   */
  public Door addDoor(Direction forward, Room dest, Key k) {
    Door d = new Door(k);
    d.addExit(forward, dest);

    this.addExit(forward, d);

    return d;
  }
  
  /**
   * Returns rooms connected to this room through an exit not blocked by
   * a door.
   * @return An iterator over this room's adjacent rooms
   */
  public Iterator<Room> getAdjacentRooms() {
    ArrayList<Room> list = new ArrayList<Room>(DEFAULT_EXITS_SIZE);
    
    Iterator<Map.Entry<Direction, Space>> it = this.getExitsIterator();
    
    while (it.hasNext()) {
      Map.Entry<Direction, Space> i = it.next();
      Space val = i.getValue();
      
      if (val instanceof Room) {
        list.add((Room)val);
      }
    }
    
    return list.iterator();
    
  }

  public Room(String n, String d) {
    super(n, d);
    this.players = new Hashtable<String, Player>(Room.DEFAULT_PLAYERS_SIZE);
    this.items = new Hashtable<String, Item>(Room.DEFAULT_ITEMS_SIZE);
  }
}
