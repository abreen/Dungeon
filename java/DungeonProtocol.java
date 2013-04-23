import java.util.*;
import java.io.*;

import dungeon.*;
import dungeon.exceptions.*;


public class DungeonProtocol {
  public static final double VERSION = 0.1;
  public static final String CHEVRONS = ">>> ";
  public static final String BANGS = "!!! ";

  public static enum Action {
    MOVE("m", "move", "go", "walk"), 
    TAKE("t", "take", "get"),
    DROP("d", "drop"),
    GIVE("g", "give"),
    LOOK("l", "look", "describe"),
    INVENTORY("i", "inventory"),
    EXITS("e", "exits"),
    SAY("s", "say", "talk"), 
    YELL("y", "yell", "shout"),
    WHISPER("w", "whisper"),
    USE("u", "use"),
    HELP("h", "help"),
    QUIT("q", "quit");
    
    private String[] keys;
    
    Action(String... keys) {
    	this.keys = keys;
    }
    
    public boolean isAction(String str) {
    	for(String key : keys) {
    		if(key.equalsIgnoreCase(str)) {
    			return true;
    		}
    	}
    	return false;
    }
  }

  /*
   * This method is called by the server when it recieves a string
   * from the client over the network and attempts to verify it against
   * the Dungeon protocol. If there are any errors, or if the player
   * is not authorized to take the specified action, the universe is
   * not altered and an error string is sent back.
   */
  public static String process(Player p, String input) {
    if (input == null)
      throw new IllegalArgumentException("recieved null");

    /* Ignore blank input */
    if (input.isEmpty()) return "";

    /* Split into tokens */
    String[] tokens = input.split("\\s");
    
    Action action = null;
    /* Parse the action or return an error */
    for(Action act : Action.values()) {
    	if(act.isAction(tokens[0])) {
    		action = act;
    		break;
    	}
    }

    if (action == null)
      return CHEVRONS + "Unsure what is meant by '" + tokens[0] + "'.\n" +
             CHEVRONS + "Try 'help' to get a list of actions.";

    if (p.wantsQuit && action != Action.QUIT)
      p.wantsQuit = false;

    /* Start string for output */
    String str = "";

    switch (action) {
      case MOVE:

        try {
          Space.Direction dir = Space.getDirectionFromString(tokens[1]);
          Space dest = p.here().to(dir);
          
          if (dest instanceof Room) {
            p.move((Room)dest);
          }

          if (dest instanceof Door) {
            Door d = (Door)dest;

            if (d.isLocked()) {
              /* Does the player have the correct key? */
              Iterator<Item> iter = p.getInventoryIterator();

              boolean found = false;
              while (iter.hasNext()) {
                Item i = iter.next();

                if (!(i instanceof Key))
                  continue;

                if (d.keyFits((Key)i)) {
                  found = true;
                  str = "You use your key to unlock the door and lock it " +
                        "behind you.\n";
                  p.move((Room)d.to(dir));
                  break;
                }
              }

              if (!found)
                return "The door is locked, and you don't have the key.";
              
            } else {
              str = "You close the door behind you.\n";
              p.move((Room)d.to(dir));
            }
          }

          return str + p.here().describe();

        } catch (NoSuchDirectionException e) {
          return CHEVRONS + "'" + tokens[1] + "' is not a direction.";
        } catch (NoSuchExitException e) {
          return CHEVRONS + "That's not a way out of here.";
        } catch (ArrayIndexOutOfBoundsException e) {
          return CHEVRONS + "Specify a direction in which to move.";
        }
        
      case TAKE:
        
        String toTake = getTokensAfterAction(tokens);

        if (toTake == null)
          return CHEVRONS + "Specify an object to take.";

        /* Search room for this item */
        Item item = null;
        try {
          item = p.here().removeItemByName(toTake);
        } catch (NoSuchItemException e) {
          return CHEVRONS + "No such item by the name '" + toTake + "' here.";
        }

        /* Add item to player's inventory */
        p.addToInventory(item);

        return CHEVRONS + "Taken.";
        
      case DROP:

        String toDrop = getTokensAfterAction(tokens);
        
        if (toDrop == null)
          return CHEVRONS + "Specify an object to drop.";

        try {
          Item i = p.dropFromInventoryByName(toDrop);
          p.here().addItem(i);

        } catch (NoSuchItemException e) {
          return CHEVRONS + "No such item by the name '" + toDrop +
                 "' in your inventory.";
        }
        
        return CHEVRONS + "Dropped.";
        
      case GIVE:
        return "got give";
      case LOOK:
        
        String toLook = getTokensAfterAction(tokens);

        if (toLook == null || toLook.equalsIgnoreCase("here"))
          return p.here().describe();

        try {
          str = p.here().getItemByName(toLook).describe();
        } catch (NoSuchItemException e) {
          try {
            str = "You rummage around in your bag.\n";
            str += p.getFromInventoryByName(toLook).describe();
          } catch (NoSuchItemException f) {
            str = CHEVRONS + "No such item '" + toLook + "' in this room " +
                  "or your inventory.";
          }
        }

        return str;

      case INVENTORY:
        int size = p.getInventorySize();

        if (size == 0)
          return CHEVRONS + "You aren't carrying anything.";

        Iterator <Item> iter = p.getInventoryIterator();

        str = CHEVRONS;
        
        if (size > 1) {
          str += size + " items: ";
        } else {
          Item i = iter.next();
          str += "Only " + i.getArticle() + " " + i.getName() + ".";
          return str;
        }
        
        int count = 1;
        while (iter.hasNext()) {
          Item i = iter.next();

          str += i.getArticle() + " " + i.getName();

          if (count == size - 1) {
            if (size == 2) {
              str += " and ";
            } else {
              str += ", and ";
            }
          } else if (count != size) {
            str += ", ";
          }


          count++;
        }

        str += ".";

        return str;

      case EXITS:
        Room h = p.here();
        int numberOfExits = h.getNumberOfExits();

        if (numberOfExits == 0)
          return CHEVRONS + "There is no way out.";

        str = CHEVRONS;

        if (numberOfExits > 1)
          str += numberOfExits + " exits: ";
        else
          str += "Only ";

        Iterator<Map.Entry<Space.Direction, Space>> i = h.getExitsIterator();

        while (i.hasNext()) {
          Map.Entry<Space.Direction, Space> e = i.next();

          String dir = Space.getStringFromDirection(e.getKey());
          str += dir + " to ";

          Space s = e.getValue();

          if (s instanceof Room)
            str += "the " + s.getName();

          if (s instanceof Door) {
            Door d = (Door)s;

            if (d.isLocked())
              str += "a locked " + d.getName();
            else
              str += "an unlocked " + d.getName();
          }

          if (i.hasNext())
            str += ", ";
          else
            str += ".";
        }

        return str;

      case SAY:
        return "got say";
      case YELL:
        return "got yell";
      case WHISPER:
        return "got whisper";
      case USE:
        return "got use";
      case HELP:
        return usage();
      case QUIT:
        if (p.wantsQuit) {
          p.wantsQuit = false;
          DungeonServer.universe.retire(p);
          
          /* Indicate to the connection thread that we want to disconnect */
          return null;
        } else {
          p.wantsQuit = true;
          return CHEVRONS + "Are you sure you want to quit?\n" +
                 CHEVRONS + "Type 'q' or 'quit' again to quit.";
        }

    }

    return BANGS + "Unexpected server error.";
  }

  /*
   * Returns all the tokens after the action as a space-separated
   * string. Returns null if there are no tokens after the action.
   */
  private static String getTokensAfterAction(String[] tokens) {
    if (tokens.length < 2)
      return null;

    int i;
    if (tokens[1].equalsIgnoreCase("the"))
      i = 2;
    else
      i = 1;
    
    String obj = "";
    for ( ; i < tokens.length; i++) {
      obj += tokens[i];
      
      if (i != (tokens.length - 1))
        obj += " ";
    }
    
    return obj;
  }

  private static String usage() {
    return "ACTION               OBJECT        INDIRECT OBJECT\n" +
           "[{m,move,go,walk}]   <direction>\n" +
           "{t,take,get}         <object>\n" +
           "{d,drop}\n" +
           "{g,give}             <object>      to <player>\n" +
           "{l,look,describe}    [<object>]\n" +
           "{i,inventory}\n" +
           "{e,exits}\n" +
           "{s,say,talk}         [<string>]\n" +
           "{y,yell,shout}       <string>\n" +
           "{w,whisper}          <string>      to <player>\n" +
           "{u,use}              <object in inventory>\n" +
           "{h,help}\n" +
           "{q,quit}";
  }
}
