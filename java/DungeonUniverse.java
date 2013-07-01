import java.util.*;
import java.io.*;

import dungeon.*;
import dungeon.exceptions.*;

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

  public Iterator<Player> getPlayers() {
    return this.players.values().iterator();
  }

  public int getNumberOfPlayers() { return this.players.size(); }

  /*
   * Returns an array of writers for every player connected.
   */
  public synchronized PrintWriter[] getAllPlayerWriters() {
    PrintWriter[] ws = new PrintWriter[this.players.size()];
    int i = 0;
    for (Player p : this.players.values()) {
      ws[i] = p.getWriter();
      i++;
    }

    return ws;
  }

  /**
   * @todo Finish implementing
   * @param p
   * @param dest 
   */
  public synchronized Room movePlayer(Player p, String dest)
    throws NoSuchDirectionException, NoSuchExitException, 
           LockedDoorException {
    
    Space.Direction direction = Space.getDirectionFromString(dest);
    Space destination = p.here().to(direction);
    
    if (destination instanceof Room) {
      unconditionallyMovePlayer(p, (Room)destination);
      return (Room)destination;
    }
    
    if (destination instanceof Door) {
      Door d = (Door)destination;
      
      if (d.isLocked()) {
        
        if (hasKeyTo(p, d)) {
          Room otherSide = (Room)d.to(direction);
          
          String unlock = "Your key unlocks the door. You lock it behind you.";
          DungeonServer.events.addNotificationEvent(p.getWriter(), unlock);
          unconditionallyMovePlayer(p, otherSide);
          return otherSide;
        } else {
          throw new LockedDoorException();
        }

      }
      
    } // end if (destination instanceof Door)
    
    return null; // this isn't a good idea
    
  } // end movePlayer()
  
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

  /*
   * Returns an array of players currently in the specified room.
   */
  public synchronized Player[] getPlayersInRoom(Room r) {
    return r.getPlayers();
  }

}
