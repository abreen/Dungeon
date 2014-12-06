package com.abreen.dungeon.util;

/**
 * A plain-old data structure representing a product type of three values
 * (e.g., a triple).
 * 
 * @author Alexander Breen <alexander.breen@gmail.com>
 *
 * @param <X> The type of the first value
 * @param <Y> The type of the second value
 * @param <Z> The type of the third value
 */
public class Triple<X, Y, Z> {
    public X first;
    public Y second;
    public Z third;

    public Triple(X x, Y y, Z z) {
        this.first = x;
        this.second = y;
        this.third = z;
    }
}