package com.abreen.dungeon.model;

import com.abreen.dungeon.exceptions.NoSuchDirectionException;

public enum Direction {
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
    
    /**
     * An array of alternate names that may be used for this space
     * (e.g., abbreviations or alternate spellings of the name).
     */
    private String[] names;

    Direction(String fullName, String... abbreviations) {
        this.fullName = fullName;
        this.names = abbreviations;
    }

    public boolean isThisDirection(String nm) {
        if (nm.equalsIgnoreCase(fullName))
            return true;

        for (String name : names)
            if (name.equalsIgnoreCase(nm))
                return true;
        return false;
    }
    
    public static Direction fromString(String s)
            throws NoSuchDirectionException
    {

        if (s == null)
            throw new IllegalArgumentException("direction must be non-null");

        Direction dir = null;
        for (Direction direction : Direction.values())
            if (direction.isThisDirection(s)) {
                dir = direction;
                break;
            }

        if (dir == null)
            throw new NoSuchDirectionException();

        return dir;
    }

    public String toString() {
        return fullName;
    }

}