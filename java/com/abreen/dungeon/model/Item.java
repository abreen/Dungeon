package com.abreen.dungeon.model;

public class Item implements Describable {
  public static final char[] VOWELS = {'a', 'e', 'i', 'o', 'u'};
  public static final boolean DEFAULT_CARRYABILITY = false;

  protected String name;
  protected String description;
  protected boolean carryable;
  private boolean nameStartsWithVowel;

  /**
   * @deprecated Use toString() instead
   */
  public String getName() { return this.name; }
  
  public String toString() { return this.name; }
  
  public String describe() { return this.description; }

  public String getArticle() {
    if (this.nameStartsWithVowel)
      return "an";
    else
      return "a";
  }

  public boolean isCarryable() { return this.carryable; }

  public Item(String n, String d) {
    if (n == null || n.isEmpty())
      throw new IllegalArgumentException("item must have a name");

    if (d == null || d.isEmpty())
      throw new IllegalArgumentException("item must have a description");

    this.name = n;

    char first = n.charAt(0);
    for (int i = 0; i < Item.VOWELS.length; i++) {
      if (Item.VOWELS[i] == first) {
        this.nameStartsWithVowel = true;
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
