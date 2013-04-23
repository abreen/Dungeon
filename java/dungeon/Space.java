package dungeon;

import java.util.*;
import dungeon.exceptions.*;

public abstract class Space implements Describable {
  public static enum Direction {
    NORTH("north", "n"),
    NORTHEAST("northeast", "ne"),
    EAST("east", "e"),
    SOUTHEAST("southeast", "se"),
    SOUTH("south", "s"),
    SOUTHWEST("southwest", "sw"),
    WEST("west", "w"),
    NORTHWEST("northwest", "nw"),
    UP("up"),
    DOWN("down");
    
    private String fullName;
    private String[] names;
    
    Direction(String fullName, String... abbreviations) {
    	this.fullName = fullName;
    	this.names = abbreviations;
    }
    
    public boolean isThisDirection(String nm) {
    	if(nm.equalsIgnoreCase(fullName))
    		return true;
    	for(String name : names) {
    		if(name.equalsIgnoreCase(nm)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public String getName() {
    	return fullName;
    }
  }

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

    Direction dir = null;
    for (Direction direction : Direction.values()) {
    	if(direction.isThisDirection(s)) {
    		dir = direction;
    		break;
    	}
    }

    if (dir == null)
      throw new NoSuchDirectionException();

    return dir;
  }

  /**
   * @deprecated Use {@code d.getName()} instead
   * @param d the direction
   * @return the name of the direction
   */
  public static String getStringFromDirection(Direction d) {
    return d.getName();
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
