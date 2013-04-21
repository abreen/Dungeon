package dungeon;

import java.util.*;

import dungeon.exceptions.*;

public class Room extends Space {
  public static final int DEFAULT_ITEMS_SIZE = 11;

  private Hashtable<String, Item> items;

  public void addItem(Item i) {
    this.items.put(i.getName(), i);
  }

  public Item getItemByName(String name) throws NoSuchItemException {
    Item i = this.items.get(name);

    if (i == null)
      throw new NoSuchItemException();

    return i;
  }
  
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
      str += "\n\nThere is ";

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

  /*
   * Returns a list of exits from this room and their directions.
   */
  public String describeExits() {
    if (this.exits.isEmpty())
      return ">>> There is no way out.";

    String str = ">>> ";
    int size = this.exits.size();

    if (size > 1)
      str += this.exits.size() + " exits: ";
    else
      str += "Only ";

    Iterator<Map.Entry<Direction, Space>> i = this.exits.entrySet().iterator();

    while (i.hasNext()) {
      Map.Entry<Direction, Space> e = i.next();

      String dir = Space.getStringFromDirection(e.getKey());
      str += dir + " to ";

      Space s = e.getValue();
      if (s instanceof Room)
        str += "the " + s.getName();
      
      if (s instanceof Door) {
        Door d = (Door)s;
        if (d.isLocked())
          str += "a locked " + d.getName();
        else
          str += "an unlocked " + d.getName();
      }

      if (i.hasNext())
        str += ", ";
      else
        str += ".";
    }
    
    return str;
  }

  public Room(String n, String d) {
    super(n, d);
    this.items = new Hashtable<String, Item>(Room.DEFAULT_ITEMS_SIZE);
  }
}
