package com.abreen.dungeon.state;

import java.util.*;

import com.abreen.dungeon.DungeonServer;
import com.abreen.dungeon.model.*;

public class DungeonGameTick extends Thread {
    public boolean running = true;
    
    public void run() {
        int scale = DungeonServer.universe.getTimescale();
        
        while (running) {
            try {
                Thread.sleep(1000 / scale);
            } catch (InterruptedException e) {
                return;
            }
            
            /*
             * Skips all object updates if there are no players connected.
             */
            if (DungeonServer.universe.getNumberOfPlayers() == 0)
                continue;

            /*
             * Update all game objects, starting with the universe.
             */
            DungeonServer.universe.tick();
            
            Iterator<Player> it = DungeonServer.universe.getPlayers();
            while (it.hasNext())
                it.next().tick();
            
        }
    }
}
