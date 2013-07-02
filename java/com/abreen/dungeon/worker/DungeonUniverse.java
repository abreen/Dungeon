package com.abreen.dungeon.worker;

import java.util.*;
import java.io.*;
import com.abreen.dungeon.DungeonServer;
import com.abreen.dungeon.exceptions.*;
import com.abreen.dungeon.model.*;

public class DungeonUniverse implements Serializable {
  private ArrayList<Space> spaces;
  private Hashtable<String, Player> players;
  private Room spawnPoint;

  /*
   * Loads a boring universe.
   */
  public DungeonUniverse() {
    this.spaces = new ArrayList<Space>();
    this.players = new Hashtable<String, Player>();

    Room a = new Room("train platform", "An empty train platform.");
    Room b = new Room("lobby", "An empty train station lobby.");
    a.addExit(Space.Direction.EAST, b);
    b.addExit(Space.Direction.WEST, a);

    Item x = new Item("flashlight", "A heavy flashlight.");
    Item y = new Item("aerosol can", "Shaving cream. Seems empty.");
    Item z = new Item("ocelot", "Slender and purring.");
    b.addItem(x);
    b.addItem(y);
    b.addItem(z);

    Room c = new Room("broom closet", "A dark broom closet.");
    Key k = new Key("skeleton key", "A skeleton key.");
    a.addItem(k);

    Door d = a.addDoor(Space.Direction.NORTH, c, Space.Direction.SOUTH, k);

    Item p = new Item("broom", "Standard-looking broom.");
    c.addItem(p);

    this.spaces.add(a);
    this.spaces.add(b);
    this.spaces.add(c);
    this.spawnPoint = a;
  }

  public boolean hasSavedState(String name) { return false; }

  /*
   * If the universe kept the saved state of a player, this method
   * will deserialize the object and assign it a fresh output stream
   * writer.
   */
  public synchronized Player restore(String name, PrintWriter w) {
    return null;
  }

  /*
   * If this player is new, a new Player object will be created
   * and an output stream writer will be assigned to it.
   */
  public synchronized Player register(String name, PrintWriter w) {
    Player p = new Player(name, this.spawnPoint, w);
    this.players.put(name, p);
    this.spawnPoint.addPlayer(p);
    return p;
  }

  /*
   * Removes the player from the universe and serializes
   * the player object.
   */
  public synchronized void retire(Player p) {
    // serialize the Player object and save to disk
    this.players.remove(p.getName());
  }

  public Room getSpawn() { return this.spawnPoint; }

  public synchronized Iterator<Player> getPlayers() {
    return this.players.values().iterator();
  }

  public synchronized int getNumberOfPlayers() { return this.players.size(); }

  /**
   * Responds to a player movement action.
   * @param p The player who wants to move
   * @param dest The player's direction input
   * @return The room the player is moved to
   * @throws NoSuchDirectionException If the direction input is invalid
   * @throws NoSuchExitException If there is no exit in the specified direction
   * @throws LockedDoorException When a player does not have the correct key
   */
  public synchronized Room movePlayer(Player p, String dest)
    throws NoSuchDirectionException, NoSuchExitException, 
           LockedDoorException {
    
    Space.Direction direction = Space.getDirectionFromString(dest);
    Space destination = p.here().to(direction);
    
    if (destination instanceof Room) {
      
      /* Do narration for players watching this player enter */
      Iterator<Player> ps = getPlayersInRoom((Room)destination);
      int n = getNumberOfPlayersInRoom((Room)destination);
      
      String moveHere = DungeonServer.narrator.narrateMoveHere(p.toString());
      DungeonServer.events.addNarrationEvent(
              DungeonDispatcher.playerIteratorToWriterArray(ps,
                n), moveHere);
      
      unconditionallyMovePlayer(p, (Room)destination);
      return (Room)destination;
    } else if (destination instanceof Door) {
      Door d = (Door)destination;
      
      if (d.isLocked()) {
        if (hasKeyTo(p, d)) {
          Room otherSide = (Room)d.to(direction);
          
          String unlock = "Your key unlocks the door. You lock it behind you.";
          DungeonServer.events.addNotificationEvent(p.getWriter(), unlock);
          
          /* Do narration for players watching this player enter */
          Iterator<Player> ps = getPlayersInRoom((Room)otherSide);
          int n = getNumberOfPlayersInRoom((Room)otherSide);

          String moveHere = DungeonServer.narrator.narrateMoveHere(p.toString());
          DungeonServer.events.addNarrationEvent(
                  DungeonDispatcher.playerIteratorToWriterArray(ps,
                  n), moveHere);
          
          unconditionallyMovePlayer(p, otherSide);
          return otherSide;
        } else {
          throw new LockedDoorException();
        }
      }
      
    }
    
    return null;  // should be unreachable
    
  }
  
  /**
   * Simply moves a player to another room.
   * @param p The player to move
   * @param dest The room into which the player is moved
   */
  private void unconditionallyMovePlayer(Player p, Room dest) {
    Room r = p.here();
    r.removePlayer(p);
    p.move(dest);
    dest.addPlayer(p);
  }
  

  /**
   * Scans a player's inventory for keys that fit the specified door. If
   * the player has a matching key, this method returns true.
   * 
   * @param p The player whose inventory is searched
   * @param d The door to which to find a key
   * @return True if the player has a matching key to the door
   */
  private boolean hasKeyTo(Player p, Door d) {
    Iterator<Item> iter = p.getInventoryIterator();

    boolean found = false;
    while (iter.hasNext()) {
      Item i = iter.next();

      if (!(i instanceof Key))
        continue;

      if (d.keyFits((Key)i)) {
        return true;
      }
    }

    if (!found)
      return false;
    else
      return true;
  }

  /**
   * Gets iterator over players in the specified room.
   * @param r The room in which to look for players
   * @return An iterator over players in the specified room
   */
  public synchronized Iterator<Player> getPlayersInRoom(Room r) {
    return r.getPlayers();
  }
  
  /**
   * Finds the number of players currently in the specified room.
   * @param r The room in which to look for players
   * @return The number of players in the room
   */
  public synchronized int getNumberOfPlayersInRoom(Room r) {
    return r.getNumberOfPlayers();
  }

}
