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
            
            /*
             * Update players.
             */
            Iterator<Player> players = DungeonServer.universe.getPlayers();
            while (players.hasNext()) {
                Player p = players.next();
                
                // update the player themselves
                p.tick();
                
                // update all items in player's inventory
                Iterator<Item> items = p.getInventoryIterator();
                while (items.hasNext()) {
                    Item i = items.next();
                    if (i instanceof Stateful)
                        ((Stateful) i).tick();
                }
            }
            
            /*
             * Update items in all rooms.
             */
            Iterator<Room> rooms = DungeonServer.universe.getRooms();
            while (rooms.hasNext()) {
                Room r = rooms.next();
                
                Iterator<Item> items = r.getItems().iterator();
                while (items.hasNext()) {
                    Item i = items.next();
                    if (i instanceof Stateful)
                        ((Stateful) i).tick();
                }
            }
        }
    }
}
