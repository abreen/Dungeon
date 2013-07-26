package com.abreen.dungeon.model;

/**
 * This abstract class contains fields and methods that aim to specify how to
 * getDescription an ingame object to a player. The class contains fields for an
 * object's name and description.
 * 
 * Put simply, the name of an object is used when mentioning it in a list or
 * referring to it by name. For rooms, the name is used when describing the
 * room, but also when the game informs the player to where another player is
 * moving, or when describing exits from the current room.
 * 
 * For items, the name of the item is used when the game lists what items are
 * sitting in the current room or when listing the contents of a player's
 * inventory.
 * 
 * For players, the name of the player is her login name, or perhaps something
 * more exotic.
 * 
 * Descriptions are more specific; descriptions of rooms are printed when the
 * player specifies 'look here'. The descriptions of room *do not* contain a
 * list of which items or players are currently in the room. (Such lists are
 * generated by the server, as a notification.) The same applies for items
 * ('look pencil') and players ('look Jason').
 * 
 * @author Alexander Breen <alexander.breen@gmail.com>
 */
public abstract class Describable {
  
  /**
   * Specification of possible genders used when formulating string
   * representations of objects with articles. Intended for non-English
   * languages. Currently not used.
   */
  public static enum Gender {
    MASCULINE, FEMININE, NEUTER
  }
  
  /**
   * The ingame object's name.
   */
  protected String name;
  
  /**
   * Whether the ingame object's name starts with a vowel. Used
   * when generating a string representation of the object.
   */
  protected boolean startsWithVowel;
  
  /**
   * The gender of the object being described.
   */
  protected Gender gender;
  
  /**
   * The full text of an ingame object's description.
   */
  protected String description;
  
  /**
   * When this field is set to true, the game will never use an
   * article to describe this object, even when otherwise specified.
   */
  protected boolean neverUseArticle;
  
  public void setNeverUseArticle(boolean b) {
    this.neverUseArticle = b;
  }
  
  public String getDescription() {
    return this.description;
  }
  
  public String getName() {
    return this.name;
  }
  
  public boolean startsWithVowel() {
    return this.startsWithVowel;
  }
  
  public boolean neverUseArticle() {
    return this.neverUseArticle;
  }
  
}
