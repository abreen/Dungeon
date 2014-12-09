package com.abreen.dungeon.util;

/**
 * A plain-old data structure representing a product type of two values
 * (e.g., a pair).
 * 
 * @author Alexander Breen <alexander.breen@gmail.com>
 *
 * @param <X> The type of the first value
 * @param <Y> The type of the second value
 */
public class Pair<X, Y> {
    public X first;
    public Y second;

    public Pair(X x, Y y) {
        this.first = x;
        this.second = y;
    }
    
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
    
    public boolean equals(Pair<X, Y> other) {
        return first.equals(other.first) && second.equals(other.second);
    }
    
    public int hashCode() {
        return (first == null ? 0 : first.hashCode()) ^
               (second == null ? 0 : second.hashCode());
    }
}