package dungeon;

import java.util.*;
import dungeon.exceptions.*;

public abstract class Space implements Describable {
  public static enum Direction {
    NORTH, NORTHEAST, EAST, SOUTHEAST, SOUTH, SOUTHWEST, WEST, NORTHWEST,
    UP, DOWN
  }

  public static final String[] DIRECTION_STRINGS = {
    "n", "ne", "e", "se", "s", "sw", "w", "nw", "north", "northeast",
    "east", "southeast", "south", "southwest", "west", "northwest",
    "up", "down"
  };

  public static final Direction[] DIRECTION_ENUMS = {
    Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST,
    Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST,
    Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST,
    Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST,
    Direction.UP, Direction.DOWN
  };

  public static final int DEFAULT_EXITS_SIZE = 6;

  protected String name;
  protected String description;

  protected Hashtable<Direction, Space> exits;

  public String describe() {
    return this.name + "\n\n" + this.description;
  }

  public String getName() { return this.name; }

  public static Direction getDirectionFromString(String s)
    throws NoSuchDirectionException {

    if (s == null)
      throw new IllegalArgumentException("direction must be non-null");

    int k = -1;
    for (int i = 0; i < DIRECTION_STRINGS.length; i++) {
      if (s.equals(DIRECTION_STRINGS[i])) {
        k = i;
        break;
      }
    }

    if (k == -1)
      throw new NoSuchDirectionException();

    return DIRECTION_ENUMS[k];
  }

  public static String getStringFromDirection(Direction d) {
    switch (d) {
      case NORTH:     return "north";
      case NORTHEAST: return "northeast";
      case EAST:      return "east";
      case SOUTHEAST: return "southeast";
      case SOUTH:     return "south";
      case SOUTHWEST: return "southwest";
      case WEST:      return "west";
      case NORTHWEST: return "northwest";
      case UP:        return "up";
      case DOWN:      return "down";
    }

    return "nowhere";
  }

  public void addExit(Direction direction, Space sp) {
    if (sp == null)
      throw new IllegalArgumentException("space must be non-null");

    if (this.exits.containsKey(direction))
      throw new IllegalArgumentException("already an exit in this direction");

    this.exits.put(direction, sp);
  }

  public Iterator<Map.Entry<Direction, Space>> getExitsIterator() {
    return this.exits.entrySet().iterator();
  }

  public int getNumberOfExits() {
    return this.exits.size();
  }

  public Space to(Direction direction) throws NoSuchExitException {
    Space newDirection = this.exits.get(direction);

    if (newDirection == null)
      throw new NoSuchExitException();

    return newDirection;
  }

  public Space(String n, String d) {
    this.name = n;
    this.description = d;
    this.exits = new Hashtable<Direction, Space>(Space.DEFAULT_EXITS_SIZE);
  }

  public Space(String n, String d, int size) {
    this.name = n;
    this.description = d;
    this.exits = new Hashtable<Direction, Space>(size);
  }

}
