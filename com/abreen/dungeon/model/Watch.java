package com.abreen.dungeon.model;

import com.abreen.dungeon.DungeonServer;
import com.abreen.dungeon.state.Stateful;
import com.abreen.dungeon.state.TimeOfDay;

public class Watch extends Item implements Stateful {
    private static final long FULL_BATTERY = 0x1000000;
    
    private final TimeOfDay tod;
    private long battery;
    
    public Watch(String n, String d) {
        super(n, d, true);
        this.tod = new TimeOfDay(DungeonServer.universe.tod);
        this.battery = FULL_BATTERY;
    }
    
    public String getDescription() {
        StringBuilder buf = new StringBuilder(super.getDescription());
        buf.append(" It currently reads ");
        buf.append(tod);
        
        if (battery == 0) {
            buf.append(", but it seems like its battery is dead.");
        } else {
            buf.append(".");
        }
        
        return buf.toString();
    }
    
    public void tick() {
        if (battery <= 0)
            return;
        
        battery--;
        this.tod.addSeconds(1);
    }
}
