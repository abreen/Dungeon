package com.abreen.dungeon.worker;

import java.util.*;
import java.io.*;
import com.abreen.dungeon.exceptions.*;
import com.abreen.dungeon.model.*;
import com.abreen.dungeon.DungeonServer;


public class DungeonProtocol {
  public static final double VERSION = 0.1;
  private static final String CHEVRONS = ">>> ";
  private static final String BANGS = "!!! ";

  private static enum Action {
    
    /**
     * The action a player issues to move from one room to another.
     */
    MOVE("m", "move", "go", "walk"),
    
    /**
     * The action a player issues to take an item from the room. The item
     * is then removed from the room and placed in the player's inventory.
     */
    TAKE("t", "take", "get"),
    
    /**
     * The action a player issues to drop an item from the player's inventory.
     * The item is then placed in the room.
     */
    DROP("d", "drop"),
    
    /**
     * The action a player issues to give an item to another player. The
     * two players must be in the same room. The item is removed from the
     * giving player's inventory and then placed in the receiving player's
     * inventory.
     */
    GIVE("g", "give"),
    
    /**
     * The action a player issues to be sent a description of the current
     * room or an item in the room or the player's inventory.
     */
    LOOK("l", "look", "describe"),
    
    /**
     * The action a player issues to be sent a list of items currently in
     * the player's inventory.
     */
    INVENTORY("i", "inventory"),
    
    /**
     * The action a player issues to be sent a list of exits out of the
     * current room.
     */
    EXITS("e", "exits"),
    
    /**
     * The action a player issues to speak. The message is then sent to
     * other players in the current room.
     */
    SAY("s", "say", "talk"),
    
    /**
     * The action a player issues to yell. The message is sent to players
     * in the current room and adjacent rooms.
     */
    YELL("y", "yell", "shout"),
    
    /**
     * The action a player issues to whisper to another player. The other
     * player must be in the same room. The message is only seen by the
     * receiving player.
     */
    WHISPER("w", "whisper"),
    
    /**
     * The action a player issues to use an item in the room or in the
     * player's inventory. The item must be a UseableItem.
     * 
     * @see UseableItem
     */
    USE("u", "use"),

    /**
     * The command a player issues to get a listing of acceptable commands.
     */
    HELP("help"),
    
    /**
     * The command a player issues to get a listing of currently connected
     * players.
     */
    WHO("who"),
    
    /**
     * The command a player issues to disconnect.
     */
    QUIT("quit");
    
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

  /**
   * Processes the supplied input from the supplied player's point of
   * view. This method calls mutating methods in the DungeonUniverse
   * class. It is the way connection threads attempt to modify or 
   * otherwise access the universe.
   * 
   * @param p The player object who sent the input string
   * @param input The input string received from the connected player
   * @throws PlayerIsQuittingException
   * @see DungeonUniverse
   */
  public static void process(Player p, String input)
    throws PlayerIsQuittingException {

    if (input.isEmpty()) return;

    String[] tokens = input.split("\\s");
    
    Action action = null;
    for (Action a : Action.values()) {
    	if (a.isAction(tokens[0])) {
    		action = a;
    		break;
    	}
    }
    
    PrintWriter playerWriter = p.getWriter();

    if (action == null) {
      String unsure = "Unsure what is meant by '" + tokens[0] + "'. Try " +
                      "'help' to get a list of valid actions.";
      DungeonServer.events.addNotificationEvent(playerWriter, unsure);
    }
    
    if (action == Action.QUIT)
      throw new PlayerIsQuittingException();

    DungeonUniverse   u = DungeonServer.universe;
    DungeonDispatcher d = DungeonServer.events;
    DungeonNarrator   n = DungeonServer.narrator;
    
    if (action == Action.MOVE) {
      try {
        Room here  = p.here();
        Room there = u.movePlayer(p, tokens[1]);

        /*
         * Do narration for players watching this player leave.
         * Because getPlayersInRoom would include the moving player if it
         * were called before the player moves, we send the narration here.
         */
        Iterator<Player> playersHere = u.getPlayersInRoom(here);
        int numPlayersHere = u.getNumberOfPlayersInRoom(here);

        String moveTo = n.narrateMoveToRoom(p.toString(), there.toString());
        d.addNarrationEvent(
                DungeonDispatcher.playerIteratorToWriterArray(playersHere,
                  numPlayersHere), moveTo);
          
      } catch (NoSuchDirectionException e) {
        String oops = "Unsure which direction is meant "
                + "by '" + tokens[1] + "'. The following directions "
                + "are recognized: " + Space.listValidDirections();
        d.addNotificationEvent(playerWriter, oops);
      } catch (NoSuchExitException e) {
        String oops = "That's not an exit. Try 'exits' for a list of ways "
                + "out.";
        d.addNotificationEvent(playerWriter, oops);
      } catch (LockedDoorException e) {
        String oops = "The door is locked, and you don't have the key.";
        d.addNotificationEvent(playerWriter, oops);
      } catch (ArrayIndexOutOfBoundsException e) {
        String oops = "Specify a direction in which to move.";
        d.addNotificationEvent(playerWriter, oops);
      } finally {
        return;
      }
    }
    
    /**
     * @todo Implement take action
     */
    if (action == Action.TAKE) { }
    
    /**
     * @todo Implement drop action
     */
    if (action == Action.DROP) { }
    
    /**
     * @todo Implement give action
     */
    if (action == Action.GIVE) { }
    
    if (action == Action.LOOK) {
      String tokensAfter = null;
      try {
        tokensAfter = getTokensAfterAction(tokens);

        if (tokensAfter == null)
          u.look(p, "here");
        else
          u.look(p, tokensAfter);

      } catch (NoSuchItemException e) {
        String oops = "There's no such item by the name '" + tokensAfter +
            "' in the room or your inventory.";
        d.addNotificationEvent(playerWriter, oops);
      } finally {
        return;
      }
    }
    
    /**
     * @todo Implement inventory action
     */
    if (action == Action.INVENTORY) { }
    
    /**
     * @todo Implement exits action
     */
    if (action == Action.EXITS) { }
    
    if (action == Action.SAY) {
      try {
        String tokensAfter = getTokensAfterAction(tokens);
        u.say(p, tokensAfter);
      } finally {
        return;
      }
    }
   
    if (action == Action.YELL) {
      try {
        String tokensAfter = getTokensAfterAction(tokens);
        
        if (tokensAfter == null) {
          String oops = "Supply something to yell.";
          d.addNotificationEvent(playerWriter, oops);
          return;
        }
        
        u.yell(p, tokensAfter);
      } finally {
        return;
      }
    }
    
    /**
     * @todo Implement whisper action
     */
    if (action == Action.WHISPER) { }
    
    /**
     * @todo Implement use action
     */
    if (action == Action.USE) { }
    
    /**
     * @todo Implement help action
     */
    if (action == Action.HELP) { }
    
    /**
     * @todo Implement who action
     */
    if (action == Action.WHO) { }
    
    
    // Action.QUIT handled above

  } // end of process

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
           "\n" + 
           "SERVER ACTION\n" + 
           "help\n" +
           "who\n" + 
           "quit";
  }
}
