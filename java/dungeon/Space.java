package dungeon;

import java.util.Hashtable;
import dungeon.exceptions.*;

public abstract class Space implements Describable {
  public static final int DEFAULT_HASHTABLE_SIZE = 6;

  protected String name;
  protected String description;

  private Hashtable<String, Space> exits;

  public String describe() {
    return this.name + "\n\n" + this.description;
  }

  public String getName() { return this.name; }

  public void addExit(String direction, Space sp) {
    if (direction == null || direction.isEmpty())
      throw new IllegalArgumentException("exit must have direction");

    if (sp == null)
      throw new IllegalArgumentException("space must be non-null");

    if (this.exits.containsKey(direction))
      throw new IllegalArgumentException("already an exit in this direction");

    this.exits.put(direction, sp);
  }

  public Space to(String direction) throws NoSuchDirectionException {
    Space newDirection = this.exits.get(direction);

    if (newDirection == null)
      throw new NoSuchDirectionException();

    return newDirection;
  }

  public Space(String n, String d) {
    this.name = n;
    this.description = d;
    this.exits = new Hashtable<String, Space>(Space.DEFAULT_HASHTABLE_SIZE);
  }

  public Space(String n, String d, int size) {
    this.name = n;
    this.description = d;
    this.exits = new Hashtable<String, Space>(size);
  }

}
