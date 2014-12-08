package com.abreen.dungeon.model;

public class Item extends Describable {
    public static final boolean DEFAULT_CARRYABILITY = false;

    protected boolean carryable;

    public boolean isCarryable() {
        return this.carryable;
    }

    public Item(String n, String d) {
        if (n == null || n.isEmpty())
            throw new IllegalArgumentException("item must have a name");

        if (d == null || d.isEmpty())
            throw new IllegalArgumentException("item must have a description");

        this.name = n;

        char first = n.charAt(0);
        for (char element : Describable.VOWELS) {
            if (element == first) {
                this.startsWithVowel = true;
                break;
            }
        }

        this.description = d;
        this.carryable = DEFAULT_CARRYABILITY;
    }

    public Item(String n, String d, boolean c) {
        this(n, d);
        this.carryable = c;
    }
}
