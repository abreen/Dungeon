package com.abreen.dungeon.exceptions;

/*
 * Thrown when a player attempts to connect when the server is not
 * finished loading or has not yet attempted to load a universe.
 */
public class NoUniverseException extends RuntimeException {
    private static final long serialVersionUID = 1L;
}
