package com.abreen.dungeon.random;

import java.util.Random;

public class Exponential extends Distribution {
    private double lambda;
    private Random rand;

    public Exponential(double l) {
        this.lambda = l;
        this.rand = new Random();
    }

    public double next() {
        double next = rand.nextDouble();
        return (-1 * Math.log(next)) / lambda;
    }
}
