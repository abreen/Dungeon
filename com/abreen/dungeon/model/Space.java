package com.abreen.dungeon.model;

import java.util.*;
import com.abreen.dungeon.exceptions.*;

public abstract class Space extends Describable {
    public static final int DEFAULT_EXITS_SIZE = 6;

    protected HashMap<Direction, Space> exits;

    public void addExit(Direction direction, Space sp) {
        if (sp == null)
            throw new IllegalArgumentException("space must be non-null");

        if (this.exits.containsKey(direction))
            throw new IllegalArgumentException(
                    "already an exit in this direction");

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
        this.exits = new HashMap<Direction, Space>(Space.DEFAULT_EXITS_SIZE);
    }

    public Space(String n, String d, int size) {
        this.name = n;
        this.description = d;
        this.exits = new HashMap<Direction, Space>(size);
    }

}
