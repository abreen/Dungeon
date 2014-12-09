package com.abreen.dungeon.random;

import java.util.Random;

public class Uniform extends Distribution {
    private double left;
    private double right;
    private Random rand;

    /**
     * Construct a new random distribution with left and right bounds.
     * The right bound is exclusive.
     * 
     * @param l The left bound
     * @param r The right bound
     */
    public Uniform(double l, double r) {
        this.left = l;
        this.right = r;
        this.rand = new Random();
    }

    public double next() {
        double next = this.rand.nextDouble();
        double diff = right - left;

        return left + (next * diff);
    }
}
