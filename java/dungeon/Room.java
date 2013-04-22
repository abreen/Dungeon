package dungeon;

import java.util.*;

import dungeon.exceptions.*;

public class Room extends Space {
  public static final int DEFAULT_ITEMS_SIZE = 11;

  private Hashtable<String, Item> items;

  public void addItem(Item i) {
    this.items.put(i.getName(), i);
  }

  public Item removeItemByName(String name) throws NoSuchItemException {
    Item i = this.items.remove(name);
    
    if (i == null)
      throw new NoSuchItemException();

    return i;
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

  public Room(String n, String d) {
    super(n, d);
    this.items = new Hashtable<String, Item>(Room.DEFAULT_ITEMS_SIZE);
  }
}
