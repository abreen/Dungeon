package dungeon;

import java.util.Hashtable;
import dungeon.exceptions.*;

/* TODO: exits should use Direction enum for direction, not strings */

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

  protected Hashtable<String, Space> exits;

  public String describe() {
    return this.name + "\n\n" + this.description;
  }

  public String getName() { return this.name; }

  public void addExit(String direction, Space sp) {
    if (direction == null || direction.isEmpty())
      throw new IllegalArgumentException("exit must have direction");

    if (sp == null)
      throw new IllegalArgumentException("space must be non-null");

    if (this.exits.containsKey(direction))
      throw new IllegalArgumentException("already an exit in this direction");

    this.exits.put(direction, sp);
  }

  public Space to(String direction) throws NoSuchDirectionException {
    Space newDirection = this.exits.get(direction);

    if (newDirection == null)
      throw new NoSuchDirectionException();

    return newDirection;
  }

  public Space(String n, String d) {
    this.name = n;
    this.description = d;
    this.exits = new Hashtable<String, Space>(Space.DEFAULT_EXITS_SIZE);
  }

  public Space(String n, String d, int size) {
    this.name = n;
    this.description = d;
    this.exits = new Hashtable<String, Space>(size);
  }

}
