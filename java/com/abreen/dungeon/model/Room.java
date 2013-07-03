package com.abreen.dungeon.model;

import java.util.*;
import com.abreen.dungeon.exceptions.*;

public class Room extends Space {
  public static final int DEFAULT_ITEMS_SIZE = 11;
  public static final int DEFAULT_PLAYERS_SIZE = 36;

  private Hashtable<String, Item> items;
  private Hashtable<String, Player> players;

  public void addItem(Item i) {
    this.items.put(i.getName().toLowerCase(), i);
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
    this.players.put(p.toString(), p);
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

  public String describe() {
    String str = this.description;

    if (!items.isEmpty()) {
      str += " There is ";

      Iterator<Item> iter = this.items.values().iterator();
      int i = 1, size = this.items.size();

      while (iter.hasNext()) {
        Item j = iter.next();
        str += j.getArticle() + " " + j.getName();

        if (i == size - 1) {
          if (size == 2) {
            str += " and ";
          } else {
            str += ", and ";
          }
        } else if (i != size) {
          str += ", ";
        }

        i++;
      }

      str += " here.";
    }
    
    return str;
  }
  
  public String describePlayers(Player perspective) {
    String str = "";
    
    int size;
    if ((size = this.getNumberOfPlayers()) > 0) {
      
      if (size == 1)
        str += "Player ";
      else
        str += "Players ";
      
      Iterator<Player> ps = this.getPlayers();
      
      int i = 1;
      while (ps.hasNext()) {
        Player p = ps.next();
        
        if (p == perspective)
          str += p.toString() + " (you)";
        else
          str += p.toString();
        
        if (i == size - 1) {
          if (size == 2) {
            str += " and ";
          } else {
            str += ", and ";
          }
        } else if (i != size) {
          str += ", ";
        }
        
        i++;
      }
      
      if (size == 1)
        str += " is here.";
      else
        str += " are here.";
    } else {
      return null;  // if there are no players here
    }

    return str;
  }

  public Room(String n, String d) {
    super(n, d);
    this.players = new Hashtable<String, Player>(Room.DEFAULT_PLAYERS_SIZE);
    this.items = new Hashtable<String, Item>(Room.DEFAULT_ITEMS_SIZE);
  }
}