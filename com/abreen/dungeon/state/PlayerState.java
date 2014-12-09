package com.abreen.dungeon.state;

/**
 * A class representing all the player status variables (e.g., fatigue).
 * Each Player object holds a reference to exactly one PlayerState object
 * and updates this object inside its tick() method.
 * 
 * @author Alexander Breen <alexander.breen@gmail.com>
 */
public class PlayerState {
    
    /**
     * Player's level of fatigue. A value of 0 corresponds to well-rested.
     * A value of MAX_FATIGUE causes death.
     */
    public long fatigue = MAX_FATIGUE / 2;
    public static final long MAX_FATIGUE = 0x10000;
    
    /**
     * Player's body temperature, in degrees Celsius.
     */
    public double temperature = 37.0;
    public static final double MIN_TEMP = 28.0;     // hypothermia
    public static final double MAX_TEMP = 41.5;     // hyperpyrexia
    
    /**
     * Player's hunger level. A value of 0 corresponds to sated.
     * A value of MAX_HUNGER initiates starvation.
     */
    public long hunger = MAX_HUNGER / 2;
    public static final long MAX_HUNGER = 0x10000;
    
    /**
     * Player's thirst level. A value of 0 corresponds to sated.
     * A value of MAX_THIRST initiates dehydration.
     */
    public long thirst = MAX_THIRST / 2;
    public static final long MAX_THIRST = 0x1000;
    
    /**
     * Returns a fatigue "level" --- a number from 0 to the supplied scale,
     * expressing more imprecisely the player's level of fatigue.
     */
    public int fatigueLevel(int scale) {
        double ratio = (double)fatigue / MAX_FATIGUE;
        return (int)(ratio * scale);
    }
    
    /**
     * Returns a hunger "level" --- a number from 0 to the supplied scale,
     * expressing more imprecisely the player's level of hunger.
     */
    public int hungerLevel(int scale) {
        double ratio = (double)hunger / MAX_HUNGER;
        return (int)(ratio * scale);
    }
    
    /**
     * Returns a thirst "level" --- a number from 0 to the supplied scale,
     * expressing more imprecisely the player's level of thirst.
     */
    public int thirstLevel(int scale) {
        double ratio = (double)thirst / MAX_THIRST;
        return (int)(ratio * scale);
    }
    
    /*
     * Note: used merely for convenience/debugging --- the narrator should
     * handle actually conveying player state.
     */
    public String toString() {
        return String.format("F: %d, Tm: %f, H: %d, Th: %d",
                fatigue, temperature, hunger, thirst);
    }
}
