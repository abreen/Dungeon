package com.abreen.dungeon.state;

/**
 * An interface that game objects must implement if they should be affected
 * by the game tick. In particular, implementors must define their own
 * method tick() that responds to one game tick. The speed of this tick is
 * defined per-universe.
 * 
 * @author Alexander Breen <alexander.breen@gmail.com>
 */
public interface Stateful {
    
    /**
     * This method should change the internal state of a game object as
     * it would respond to the passage of time.
     */
    public void tick();
}
