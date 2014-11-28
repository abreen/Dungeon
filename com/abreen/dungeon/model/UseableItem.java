package com.abreen.dungeon.model;

public class UseableItem extends Item implements Useable {
    private Useable useMethodObject;

    public void setUseMethodObject(Useable u) {
        if (u == null)
            throw new IllegalArgumentException("method object must be non-null");

        this.useMethodObject = u;
    }

    public void use() {
        this.useMethodObject.use();
    }

    public UseableItem(String n, String d) {
        super(n, d);
    }

    public UseableItem(String n, String d, boolean c) {
        super(n, d, c);
    }
}
