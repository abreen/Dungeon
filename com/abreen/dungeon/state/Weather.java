package com.abreen.dungeon.state;

public enum Weather {
    CLEAR("clear"),
    RAIN("rain"),
    SNOW("snow"),
    FOG("fog");
    
    private String key;
    
    Weather(String s) {
        this.key = s;
    }
    
    public static Weather fromString(String s) {
        if (s == null)
            throw new IllegalArgumentException("weather key must be non-null");

        Weather weather = null;
        for (Weather w : Weather.values()) {
            if (w.key.equals(s)) {
                weather = w;
                break;
            }
        }
        
        return weather;
    }
}