package com.abreen.dungeon.model;

import com.abreen.dungeon.exceptions.*;

public class Door extends Space {
  public static final String DEFAULT_NAME = "door";
  public static final String DEFAULT_DESCRIPTION = "A normal-looking door.";

  private Key key;
  private boolean locked;

  public String getDescription() {
    return this.description;
  }

  public Key getKey() { return this.key; }

  public boolean keyFits(Key k) {
    return k == this.key;
  }

  public boolean isLocked() { return this.locked; }

  public void lock(Key k) throws AlreadyLockedException,
                                 WrongKeyException {
    if (k == null)
      throw new IllegalArgumentException("key must be non-null");

    if (this.locked)
      throw new AlreadyLockedException();

    if (k != this.key)
      throw new WrongKeyException();

    this.locked = true;
  }

  public void unlock(Key k) throws NotLockedException,
                                   WrongKeyException {
    if (k == null)
      throw new IllegalArgumentException("key must be non-null");

    if (!this.locked)
      throw new NotLockedException();

    if (k != this.key)
      throw new WrongKeyException();

    this.locked = false;
  }

  /*
   * Constructs a new door with a lock mechanism that fits the specified
   * key. Locks the door by default.
   */
  public Door(Key k) {
    /* 
     * Let the Space constructor make a space whose hashtable size is
     * exactly 2 --- one for each side of the door.
     */
    super(Door.DEFAULT_NAME, Door.DEFAULT_DESCRIPTION, 2);
    if (k == null)
      throw new IllegalArgumentException("key must be non-null");

    this.key = k;
    this.locked = true;
  }
}
