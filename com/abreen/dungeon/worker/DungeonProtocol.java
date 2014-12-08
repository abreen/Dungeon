package com.abreen.dungeon.worker;

import java.util.*;

import com.abreen.dungeon.exceptions.*;
import com.abreen.dungeon.model.*;
import com.abreen.dungeon.util.*;
import com.abreen.dungeon.DungeonServer;

public class DungeonProtocol {
    private static final int DEFAULT_BUFFER_SIZE = 2048;
    
    public static enum Action {

        /**
         * The action a player issues to move from one room to another.
         */
        MOVE("<direction>", "move", "m", "go", "walk"),

        /**
         * The action a player issues to take an item from the room. The item is
         * then removed from the room and placed in the player's inventory.
         */
        TAKE("<item name>", "take", "t"),

        /**
         * The action a player issues to drop an item from the player's
         * inventory. The item is then placed in the room.
         */
        DROP("<item name>", "drop"),

        /**
         * The action a player issues to give an item to another player. The two
         * players must be in the same room. The item is removed from the giving
         * player's inventory and then placed in the receiving player's
         * inventory.
         */
        GIVE("<item name> to <player name>", "give"),

        /**
         * The action a player issues to be sent a description of the current
         * room or an item in the room or the player's inventory.
         */
        LOOK("[<space, item, or player name>]", "look", "l"),

        /**
         * The action a player issues to be sent a list of items currently in
         * the player's inventory.
         */
        INVENTORY("", "inventory", "i"),

        /**
         * The action a player issues to be sent a list of exits out of the
         * current room.
         */
        EXITS("", "exits", "e"),

        /**
         * The action a player issues to speak. The message is then sent to
         * other players in the current room.
         */
        SAY("[<message>]", "say", "s"),

        /**
         * The action a player issues to yell. The message is sent to players in
         * the current room and adjacent rooms.
         */
        YELL("<message>", "yell", "y", "shout"),

        /**
         * The action a player issues to whisper to another player. The other
         * player must be in the same room. The message is only seen by the
         * receiving player.
         */
        WHISPER("<message> to <player name>", "whisper", "w"),

        /**
         * The action a player issues to use an item in the room or in the
         * player's inventory. The item must be a UseableItem.
         * 
         * @see UseableItem
         */
        USE("<item name>", "use", "u"),

        /**
         * The command a player issues to get a listing of acceptable commands.
         */
        HELP("", "help"),

        /**
         * The command a player issues to get a listing of currently connected
         * players.
         */
        WHO("", "who"),

        /**
         * The command a player issues to disconnect.
         */
        QUIT("", "quit");

        private String usage;           // usage printed in help message
        private String name;            // e.g., "move"
        private String[] otherNames;    // other synonyms or abbreviations

        Action(String usage, String name, String... otherNames) {
            this.usage = usage;
            this.name = name;
            this.otherNames = otherNames;
        }

        public boolean isThisAction(String str) {
            if (str.equalsIgnoreCase(name))
                return true;
            
            for (String other : otherNames)
                if (str.equalsIgnoreCase(other))
                    return true;
            
            return false;
        }
    }

    private static DungeonUniverse u = DungeonServer.universe;
    private static DungeonDispatcher d = DungeonServer.events;
    private static DungeonNarrator n = DungeonServer.narrator;
    
    public static boolean isValidActionOrDirection(String s) {
        for (Action a : Action.values())
            if (a.isThisAction(s))
                return true;
        
        for (Direction d : Direction.values())
            if (d.isThisDirection(s))
                return true;
        
        return false;
    }
    
    /**
     * Breaks an input string into tokens. Similar to using the string's
     * split() method with a whitespace regular expression, but is
     * actually faster, since no regular expressions are used.
     * 
     * @param s The string to tokenize
     * @return An array of string tokens
     */
    private static ArrayList<String> tokenize(String s) {
        ArrayList<String> tokens = new ArrayList<String>();
        
        if (s.indexOf(' ') < 0) {
            tokens.add(s);
            return tokens;
        }
        
        int start = 0, end;
        while ((end = s.indexOf(' ', start)) >= 0) {
            if (start != end)
                tokens.add(s.substring(start, end));
            start = end + 1;
        }
        end = s.length();
        
        if (end - start > 0)
            tokens.add(s.substring(start, end));
        
        return tokens;
    }

    /**
     * Processes the supplied input from the supplied player's point of view.
     * This method calls mutating methods in the DungeonUniverse class. It is
     * the way connection threads attempt to modify or otherwise access the
     * universe.
     * 
     * @param p
     *            The player object who sent the input string
     * @param input
     *            The input string received from the connected player
     * @throws PlayerIsQuittingException
     * @see DungeonUniverse
     */
    public static void process(Player p, String input)
            throws PlayerIsQuittingException
    {
        if (input.isEmpty())
            return;
        
        System.out.printf("%s → %s\n", p.toString(), input);

        p.updateLastAction();

        ArrayList<String> tokens = tokenize(input);
        
        if (tokens.size() == 0)
            return;
        
        String first = tokens.get(0);

        Action action = null;
        for (Action a : Action.values()) {
            if (a.isThisAction(first)) {
                action = a;
                break;
            }
        }

        if (action == null) {
            // try to interpret it as a direction for a move command
            try {
                Direction.fromString(first);
            } catch (NoSuchDirectionException e) {
                String unsure = "Unsure what is meant by \"" + first + "\". " +
                        "Try \"help\" to get a list of valid actions.";
                StringBuilder buf = new StringBuilder(unsure);
                d.addNotificationEvent(p.getWriter(), buf);
                return;
            }
            
            // add in a "move" action before the direction before processing
            tokens.add(0, "move");
            processMove(p, tokens);
            return;
        }

        switch (action) {
        case QUIT:
            throw new PlayerIsQuittingException();
        case MOVE:
            processMove(p, tokens);
            return;
        case TAKE:
            processTake(p, tokens);
            return;
        case DROP:
            processDrop(p, tokens);
            return;
        case GIVE:
            processGive(p, tokens);
            return;
        case LOOK:
            processLook(p, tokens);
            return;
        case INVENTORY:
            processInventory(p, tokens);
            return;
        case EXITS:
            processExits(p, tokens);
            return;
        case SAY:
            processSay(p, tokens);
            return;
        case YELL:
            processYell(p, tokens);
            return;
        case WHISPER:
            processWhisper(p, tokens);
            return;
        case USE:
            processUse(p, tokens);
            return;
        case WHO:
            processWho(p, tokens);
            return;
        case HELP:
        default:
            processHelp(p);
            return;
        }
    }

    private static void processDrop(Player p, ArrayList<String> tokens) {
        String s = getTokensAfterAction(tokens);

        try {
            Item i = u.drop(p, s);

            String narr = n.narrateDrop(DungeonNarrator.toString(p),
                    DungeonNarrator.toString(i,
                            DungeonNarrator.StringType.WITH_ARTICLE));

            Iterator<Player> players = p.here().getPlayers();
            int size = p.here().getNumberOfPlayers();

            d.addNarrationEvent(DungeonDispatcher.playerIteratorToWriterArray(
                    players, size), narr);

        } catch (NoSuchItemException e) {
            String oops = "You do not have an item known as \"" + s + "\".";
            d.addNotificationEvent(p.getWriter(), oops);
        }
    }

    private static void processExits(Player p, ArrayList<String> tokens) {
        String desc = DungeonNarrator.describeExits(p.here());
        d.addNotificationEvent(p.getWriter(), desc);
    }

    private static void processGive(Player p, ArrayList<String> tokens) {
        String s = getTokensAfterAction(tokens);

        if (s == null) {
            String oops = "Specify an item from your inventory to give, followed "
                    + "by \"to\" and the name of the recipient.";
            d.addNotificationEvent(p.getWriter(), oops);
            return;
        }

        int indirectIndex = s.lastIndexOf(" to ");

        if (indirectIndex == -1) {
            String oops = "You must specify a recipient.";
            d.addNotificationEvent(p.getWriter(), oops);
            return;
        }

        String object = s.substring(0, indirectIndex);
        String indirectObject = s.substring(indirectIndex + 4).trim();

        try {
            u.give(p, object, indirectObject);

            String narr = n.narrateGive(DungeonNarrator.toString(p), object,
                    indirectObject);

            Iterator<Player> ps = u.getPlayersInRoom(p.here());
            int size = u.getNumberOfPlayersInRoom(p.here());

            d.addNarrationEvent(
                    DungeonDispatcher.playerIteratorToWriterArray(ps, size),
                    narr);

        } catch (NoSuchItemException e) {
            String oops = "You do not have an item known as \""
                    + object + "\".";
            d.addNotificationEvent(p.getWriter(), oops);
        } catch (NoSuchPlayerException e) {
            String oops = "There is no such player \"" + indirectObject
                    + "\" in this room.";
            d.addNotificationEvent(p.getWriter(), oops);
        }
    }

    private static void processInventory(Player p, ArrayList<String> tokens) {
        String desc = DungeonNarrator.describeInventory(p);
        d.addNotificationEvent(p.getWriter(), desc);
    }

    private static void processLook(Player p, ArrayList<String> tokens) {
        String tokensAfter = null;
        
        if (tokens != null)
            tokensAfter = getTokensAfterAction(tokens);

        try {
            if (tokensAfter == null)
                u.look(p, "here");
            else
                u.look(p, tokensAfter);

        } catch (NoSuchItemException e) {
            String oops = "There's no such item by the name \"" + tokensAfter
                    + "\" in the room or your inventory.";
            d.addNotificationEvent(p.getWriter(), oops);
        }
    }

    private static void processMove(Player p, ArrayList<String> tokens) {
        try {
            Room here = p.here();
            Room there = u.movePlayer(p, tokens.get(1));

            /*
             * Do narration for players watching this player leave. Because
             * getPlayersInRoom would include the moving player if it were
             * called before the player moves, we send the narration here.
             */
            Iterator<Player> playersHere = u.getPlayersInRoom(here);
            int numPlayersHere = u.getNumberOfPlayersInRoom(here);

            String playerString = DungeonNarrator.toString(p);
            String roomString = DungeonNarrator.toString(there,
                    DungeonNarrator.StringType.WITH_ARTICLE);
            String moveTo = n.narrateMoveToRoom(playerString, roomString);
            d.addNarrationEvent(DungeonDispatcher.playerIteratorToWriterArray(
                    playersHere, numPlayersHere), moveTo);

            /*
             * Finally, give the player a description of the new room.
             */
            processLook(p, null);

        } catch (NoSuchDirectionException e) {
            String validDirs =
                    DungeonNarrator.toNaturalList(Direction.values(), false);
            
            String oops = "Unsure which direction is meant " + "by \""
                    + tokens.get(1) + "\". The following directions "
                    + "are recognized: " + validDirs;
            d.addNotificationEvent(p.getWriter(), oops);
        } catch (NoSuchExitException e) {
            String oops = "That's not an exit. Try \"exits\" for a list of " +
                    "ways out.";
            d.addNotificationEvent(p.getWriter(), oops);
        } catch (LockedDoorException e) {
            String oops = "The door is locked, and you don't have the key.";
            d.addNotificationEvent(p.getWriter(), oops);
        } catch (IndexOutOfBoundsException e) {
            String oops = "Specify a direction in which to move.";
            d.addNotificationEvent(p.getWriter(), oops);
        }
    }

    private static void processSay(Player p, ArrayList<String> tokens) {
        String tokensAfter = getTokensAfterAction(tokens, false);
        if (tokensAfter != null && tokensAfter.length() > 0 &&
                (tokensAfter.charAt(0) == '"' || tokensAfter.charAt(0) == '\''))
            tokensAfter = stripQuotationMarks(tokensAfter);
        
        u.say(p, tokensAfter);
    }

    private static void processTake(Player p, ArrayList<String> tokens) {
        String s = getTokensAfterAction(tokens);

        try {
            Item i = u.take(p, s);

            Iterator<Player> ps = u.getPlayersInRoom(p.here());
            int size = u.getNumberOfPlayersInRoom(p.here());
            String narr = n.narrateTake(DungeonNarrator.toString(p),
                    DungeonNarrator.toString(i,
                            DungeonNarrator.StringType.WITH_ARTICLE));
            d.addNarrationEvent(
                    DungeonDispatcher.playerIteratorToWriterArray(ps, size),
                    narr);

        } catch (NoSuchItemException e) {
            String oops = "There is no item \"" + s + "\" in the room.";
            d.addNotificationEvent(p.getWriter(), oops);
        }
    }

    private static void processUse(Player p, ArrayList<String> tokens) {
        String oops = "That cannot be used.";
        d.addNotificationEvent(p.getWriter(), oops);
    }

    private static void processWhisper(Player p, ArrayList<String> tokens) {
        String s = getTokensAfterAction(tokens, false);

        if (s == null) {
            String oops = "Write a secret message, followed by \"to\" and the "
                    + "name of the recipient.";
            d.addNotificationEvent(p.getWriter(), oops);
            return;
        }

        int indirectIndex = s.lastIndexOf(" to ");

        if (indirectIndex == -1) {
            String oops = "You didn't specify a recipient, so you whispered to "
                    + "yourself.";
            d.addNotificationEvent(p.getWriter(), oops);
            return;
        }

        String message = s.substring(0, indirectIndex);
        String recipient = s.substring(indirectIndex + 4).trim();

        if (p.getName().equals(recipient)) {
            String oops = "OK, you murmur something completely inaudible.";
            d.addNarrationEvent(p.getWriter(), oops);
            return;
        }

        try {
            u.whisper(p, message, recipient);
        } catch (NoSuchPlayerException e) {
            String oops = "There is no such player \"" + recipient
                    + "\" in this room.";
            d.addNotificationEvent(p.getWriter(), oops);
        }
    }

    private static void processWho(Player p, ArrayList<String> tokens) {
        Iterator<Player> ps = u.getPlayers();
        int numPlayers = u.getNumberOfPlayers();

        String[] lines = new String[numPlayers + 1];
        
        String fmt = "%-32s%s";

        lines[0] = String.format(fmt, "PLAYER", "LAST HEARD FROM");

        int i = 1;
        while (ps.hasNext()) {
            Player thisPlayer = ps.next();

            String name;
            if (thisPlayer == p)
                name = DungeonNarrator.toString(thisPlayer) + " (you)";
            else
                name = DungeonNarrator.toString(thisPlayer);

            lines[i++] = String.format(fmt, name,
                    DungeonNarrator.timeSinceLastAction(thisPlayer));
        }

        for (String line : lines)
            d.addNotificationEvent(p.getWriter(), line);

    }

    private static void processYell(Player p, ArrayList<String> tokens) {
        String tokensAfter = getTokensAfterAction(tokens, false);

        if (tokensAfter == null) {
            String oops = "Supply something to yell.";
            d.addNotificationEvent(p.getWriter(), oops);
            return;
        }

        u.yell(p, tokensAfter);
    }
    
    private static void processHelp(Player p) {
        StringBuilder buf = new StringBuilder(1024);
        String fmt = "%-16s%-36s%s\n";
        int numChevrons = DungeonDispatcher.CHEVRONS.length();
        String fmt2 = Strings.repeat(" ", numChevrons) + fmt;
        
        buf.append(String.format(fmt, "COMMAND", "USAGE", "SYNONYMS"));
        
        for (Action a : Action.values()) {
            String synonyms = Strings.join(a.otherNames, ", ");
            buf.append(String.format(fmt2, a.name, a.usage, synonyms));
        }
        
        d.addNotificationEvent(p.getWriter(), buf);
    }
    
    private static String getTokensAfterAction(ArrayList<String> tokens) {
        return getTokensAfterAction(tokens, true);
    }

    /*
     * Returns all the tokens after the action as a space-separated string.
     * Returns null if there are no tokens after the action.
     */
    private static String getTokensAfterAction(ArrayList<String> tokens,
            boolean avoidThe)
    {
        int size = tokens.size();
        if (size < 2)
            return null;

        int i;
        if (avoidThe && tokens.get(1).equalsIgnoreCase("the"))
            i = 2;
        else
            i = 1;

        StringBuilder rest = new StringBuilder();
        
        for (; i < size; i++) {
            rest.append(tokens.get(i));

            if (i != size - 1)
                rest.append(" ");
        }

        return rest.toString();
    }
    
    private static String stripQuotationMarks(String s) {
        StringBuilder buf = new StringBuilder(s);
        int length = s.length();
        int bufLength = length;
        
        if (s.charAt(0) == '"' || s.charAt(0) == '\'') {
            buf.deleteCharAt(0);
            bufLength--;
        }
        
        if (s.charAt(length - 1) == '"' || s.charAt(length - 1) == '\'')
            buf.deleteCharAt(bufLength - 1);
        
        return buf.toString();
    }
}
