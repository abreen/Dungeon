package com.abreen.dungeon.model;

import java.util.*;
import com.abreen.dungeon.exceptions.*;

public abstract class Space extends Describable {
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
    	if (nm.equalsIgnoreCase(fullName))
    		return true;
      
    	for (String name : names) {
    		if (name.equalsIgnoreCase(nm)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    /**
     * @deprecated Use toString() instead
     */
    public String getName() {
    	return fullName;
    }
    
    public String toString() {
      return fullName;
    }
    
  }

  public static String listValidDirections() {
    String str = "";
    
    Direction[] dirs = Direction.values();
    
    for (int i = 0; i < dirs.length; i++) {
      str += dirs[i].toString();
      
      if ((i + 1) < dirs.length)
        str += ", ";
      else
        str += ".";
    }
    
    return str;
  }
  
  public static final int DEFAULT_EXITS_SIZE = 6;

  protected Hashtable<Direction, Space> exits;

  @Override
  public String getDescription() {
    return this.name + "\n\n" + this.description;
  }

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
