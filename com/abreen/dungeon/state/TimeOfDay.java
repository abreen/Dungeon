package com.abreen.dungeon.state;

import java.io.Serializable;

/**
 * A light-weight and general-purpose class representing time of day.
 * Instances of this class are designed to be mutable, so that times of day
 * can be set, reset, and advanced.
 * 
 * @author Alexander Breen <alexander.breen@gmail.com>
 */
public class TimeOfDay implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private static final int HOURS_IN_DAY = 24;
    private static final int MINUTES_IN_HOUR = 60;
    private static final int SECONDS_IN_MINUTE = 60;
    
    public int hour;
    public int minute;
    public int second;
    
    public TimeOfDay(int h, int m, int s) {
        if (h < 0 || h > HOURS_IN_DAY)
            throw new IllegalArgumentException("invalid initial hour");
        
        if (m < 0 || m > MINUTES_IN_HOUR)
            throw new IllegalArgumentException("invalid initial minute");
        
        if (s < 0 || s > SECONDS_IN_MINUTE)
            throw new IllegalArgumentException("invalid initial second");
        
        this.hour = h;
        this.minute = m;
        this.second = s;
    }
    
    public TimeOfDay(TimeOfDay t) {
        this.hour = t.hour;
        this.minute = t.minute;
        this.second = t.second;
    }
    
    public boolean equals(TimeOfDay t) {
        return hour == t.hour && minute == t.minute && second == t.second;
    }
    
    public void addSecond() {
        second++;
        
        if (second >= SECONDS_IN_MINUTE) {
            second = 0;
            minute++;
        }
        
        if (minute >= MINUTES_IN_HOUR) {
            minute = 0;
            hour++;
        }
        
        if (hour >= HOURS_IN_DAY) {
            hour = 0;
        }
    }
    
    public void addSeconds(int s) {
        while (s-- > 0) addSecond();
    }
    
    public DayPart getDayPart() {
        if (hour > 5 && hour < 18)
            return DayPart.DAY;
        else
            return DayPart.NIGHT;
    }
    
    public String to12hString() {
        int h = hour % 12;
        if (h == 0) h = 12;
        return String.format("%02d:%02d:%02d", h, minute, second);
    }
    
    public String to24hString() {
        return String.format("%02d:%02d:%02d", hour, minute, second);
    }
    
    public String toString() {
        return to24hString();
    }
}
