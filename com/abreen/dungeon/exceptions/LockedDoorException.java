package com.abreen.dungeon.exceptions;

/*
 * Thrown when a player attempts to move through a door for which the
 * player has no key.
 */
public class LockedDoorException extends DungeonException {
    private static final long serialVersionUID = 1L;
}
