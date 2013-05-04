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

  /*
   * Moves a player to the specified room.
   */
  public synchronized void movePlayer(Player p, Room dest) {
    Room r = p.here();
    r.removePlayer(p);
    p.move(dest);
    dest.addPlayer(p);
  }

  /*
   * Returns true if the player has a key to the specified door.
   */
  public synchronized boolean hasKeyTo(Player p, Door d) {
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
