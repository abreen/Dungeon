package com.abreen.dungeon.worker;

import java.util.*;
import java.io.*;

import com.abreen.dungeon.DungeonServer;
import com.abreen.dungeon.exceptions.*;
import com.abreen.dungeon.model.*;
import com.abreen.dungeon.state.Stateful;
import com.abreen.dungeon.state.TimeOfDay;

public class DungeonUniverse implements Serializable, Stateful {
    private static final long serialVersionUID = 1L;

    private Collection<Room> rooms;
    private Hashtable<String, Player> players;
    private Room spawnPoint;
    private boolean doWeather;
    private int timescale;
    
    public final TimeOfDay tod;

    /*
     * Loads a boring universe.
     */
    public DungeonUniverse() {
        this.rooms = new ArrayList<Room>();
        this.players = new Hashtable<String, Player>();
        this.tod = new TimeOfDay(12, 0, 0);
    }

    public DungeonUniverse(Room spawn, boolean weather, int timescale,
            Collection<Room> sps)
    {
        this();
        this.spawnPoint = spawn;
        this.doWeather = weather;
        this.timescale = timescale;
        this.rooms = sps;
    }

    public boolean doWeather() {
        return this.doWeather;
    }

    public boolean hasSavedState(String name) {
        return false;
    }
    
    public int getTimescale() {
        return this.timescale;
    }
    
    public void tick() {
        this.tod.addSeconds(1);
    }

    /*
     * If the universe kept the saved state of a player, this method will
     * deserialize the object and assign it a fresh output stream writer.
     */
    public synchronized Player restore(String name, PrintWriter w) {
        return null;
    }

    /*
     * If this player is new, a new Player object will be created and an output
     * stream writer will be assigned to it.
     */
    public synchronized Player register(String name, PrintWriter w) {
        Player p = new Player(name, this.spawnPoint, w);
        this.players.put(name, p);
        this.spawnPoint.addPlayer(p);
        return p;
    }

    /*
     * Removes the player from the universe and serializes the player object.
     */
    public synchronized void retire(Player p) {
        // serialize the Player object and save to disk
        p.here().removePlayer(p);
        this.players.remove(p.getName());
    }

    public Room getSpawn() {
        return this.spawnPoint;
    }

    public synchronized Iterator<Player> getPlayers() {
        return this.players.values().iterator();
    }

    public synchronized int getNumberOfPlayers() {
        return this.players.size();
    }

    /**
     * Responds to a player movement action.
     * 
     * @param p
     *            The player who wants to move
     * @param dest
     *            The player's direction input
     * @return The room the player is moved to
     * @throws NoSuchDirectionException
     *             If the direction input is invalid
     * @throws NoSuchExitException
     *             If there is no exit in the specified direction
     * @throws LockedDoorException
     *             When a player does not have the correct key
     */
    public synchronized Room movePlayer(Player p, String dest)
            throws NoSuchDirectionException, NoSuchExitException,
            LockedDoorException
    {

        Direction direction = Direction.fromString(dest);
        Space destination = p.here().to(direction);

        if (destination instanceof Room) {

            /*
             * Do narration for players watching this player enter. We do this
             * before the player actually moves so that the call to
             * getPlayersInRoom does not include the moving player.
             */
            Iterator<Player> ps = getPlayersInRoom((Room) destination);
            int n = getNumberOfPlayersInRoom((Room) destination);

            String playerString = DungeonNarrator.toString(p);
            String moveHere = DungeonServer.narrator
                    .narrateMoveHere(playerString);
            DungeonServer.events.addNarrationEvent(
                    DungeonDispatcher.playerIteratorToWriterArray(ps, n),
                    moveHere);

            unconditionallyMovePlayer(p, (Room) destination);
            return (Room) destination;
        } else if (destination instanceof Door) {
            Door d = (Door) destination;

            if (d.isLocked())
                if (hasKeyTo(p, d)) {
                    Room otherSide = (Room) d.to(direction);

                    String unlock = "Your key unlocks the door. You lock it behind you.";
                    DungeonServer.events.addNotificationEvent(p.getWriter(),
                            unlock);

                    /*
                     * As before, do narration for players watching this player
                     * enter. We do this before the player actually moves so
                     * that the call to getPlayersInRoom does not include the
                     * moving player.
                     */
                    Iterator<Player> ps = getPlayersInRoom(otherSide);
                    int n = getNumberOfPlayersInRoom(otherSide);

                    String playerString = DungeonNarrator.toString(p);
                    String moveHere = DungeonServer.narrator
                            .narrateMoveHere(playerString);
                    DungeonServer.events.addNarrationEvent(DungeonDispatcher
                            .playerIteratorToWriterArray(ps, n), moveHere);

                    unconditionallyMovePlayer(p, otherSide);
                    return otherSide;
                } else
                    throw new LockedDoorException();

        }

        return null; // should be unreachable

    }

    /**
     * Simply moves a player to another room.
     * 
     * @param p
     *            The player to move
     * @param dest
     *            The room into which the player is moved
     */
    private void unconditionallyMovePlayer(Player p, Room dest) {
        Room r = p.here();
        r.removePlayer(p);
        p.move(dest);
        dest.addPlayer(p);
    }

    /**
     * Scans a player's inventory for keys that fit the specified door. If the
     * player has a matching key, this method returns true.
     * 
     * @param p
     *            The player whose inventory is searched
     * @param d
     *            The door to which to find a key
     * @return True if the player has a matching key to the door
     */
    private boolean hasKeyTo(Player p, Door d) {
        Iterator<Item> iter = p.getInventoryIterator();

        boolean found = false;
        while (iter.hasNext()) {
            Item i = iter.next();

            if (!(i instanceof Key))
                continue;

            if (d.keyFits((Key) i))
                return true;
        }

        if (!found)
            return false;
        else
            return true;
    }

    /**
     * Gets iterator over players in the specified room.
     * 
     * @param r
     *            The room in which to look for players
     * @return An iterator over players in the specified room
     */
    public synchronized Iterator<Player> getPlayersInRoom(Room r) {
        return r.getPlayers();
    }

    /**
     * Finds the number of players currently in the specified room.
     * 
     * @param r
     *            The room in which to look for players
     * @return The number of players in the room
     */
    public synchronized int getNumberOfPlayersInRoom(Room r) {
        return r.getNumberOfPlayers();
    }

    /**
     * Responds to a player's intent to get a description of the room or an item
     * in the room or the player's inventory.
     * 
     * @param p
     *            The player
     * @param s
     *            The name of the item, or "here" for the current room
     * @throws NoSuchItemException
     *             If the specified item is nowhere to be found
     */
    public void look(Player p, String s) throws NoSuchItemException {
        if (s == null)
            throw new IllegalArgumentException();

        if (s.equals("here")) {
            String name = DungeonNarrator.toString(p.here()).toUpperCase();
            String desc = DungeonNarrator.describe(p.here());
            DungeonServer.events.addNotificationEvent(p.getWriter(), name);
            DungeonServer.events.addNotificationEvent(p.getWriter(), desc);

            desc = DungeonNarrator.describePlayers(p, p.here());
            if (desc != null)
                DungeonServer.events.addNotificationEvent(p.getWriter(), desc);

            desc = DungeonNarrator.describeItems(p.here());
            if (desc != null)
                DungeonServer.events.addNotificationEvent(p.getWriter(), desc);

        } else {
            /*
             * The player specified an object in the room or in the player's own
             * inventory
             */
            try {
                Item item = p.here().getItemByName(s);
                DungeonServer.events.addNotificationEvent(p.getWriter(),
                        DungeonNarrator.describe(item));
            
            } catch (NoSuchItemException e) {
                /*
                 * Try looking in the player's inventory for the item
                 */
                Item item = p.getFromInventoryByName(s);
                String desc = "(from your inventory) "
                        + DungeonNarrator.describe(item);
                DungeonServer.events.addNotificationEvent(p.getWriter(), desc);
            }
        }
    }

    public synchronized void say(Player p, String s) {
        String narr;
        String playerString = DungeonNarrator.toString(p);
        if (s == null)
            narr = DungeonServer.narrator.narrateSay(playerString, "");
        else
            narr = DungeonServer.narrator.narrateSay(playerString, s);

        Iterator<Player> ps = getPlayersInRoom(p.here());
        int n = getNumberOfPlayersInRoom(p.here());
        DungeonServer.events.addNarrationEvent(
                DungeonDispatcher.playerIteratorToWriterArray(ps, n), narr);

    }

    public synchronized void whisper(Player p, String message, String recipient)
            throws NoSuchPlayerException
    {
        Iterator<Player> ps = p.here().getPlayers();
        ArrayList<Player> observers = new ArrayList<Player>();

        Player otherPlayer = null;
        while (ps.hasNext()) {
            Player thisPlayer = ps.next();

            if (thisPlayer.getName().equals(recipient))
                otherPlayer = thisPlayer;
            else if (thisPlayer != p)
                observers.add(thisPlayer);
        }

        if (otherPlayer == null)
            throw new NoSuchPlayerException();

        String secretNarr = DungeonServer.narrator.narrateWhisper(
                DungeonNarrator.toString(p), message);
        String publicNarr = DungeonServer.narrator.narrateUnheardWhisper(
                DungeonNarrator.toString(p),
                DungeonNarrator.toString(otherPlayer));

        ArrayList<Player> secrets = new ArrayList<Player>();
        secrets.add(p);
        secrets.add(otherPlayer);

        DungeonServer.events.addNarrationEvent(
                DungeonDispatcher.playerIteratorToWriterArray(
                        secrets.iterator(), secrets.size()), secretNarr);

        DungeonServer.events.addNarrationEvent(
                DungeonDispatcher.playerIteratorToWriterArray(
                        observers.iterator(), observers.size()), publicNarr);

    }

    public synchronized void yell(Player p, String s) {
        String playerString = DungeonNarrator.toString(p);
        String narr1 = DungeonServer.narrator.narrateYell(playerString, s);
        String narr2 = DungeonServer.narrator.narrateDistantYell(s);

        Iterator<Player> ps = getPlayersInRoom(p.here());
        int n = getNumberOfPlayersInRoom(p.here());

        /* Get players in adjacent rooms */
        Iterator<Room> adjacentRooms = p.here().getAdjacentRooms();
        ArrayList<Player> farPlayers = new ArrayList<Player>();

        while (adjacentRooms.hasNext()) {
            Room r = adjacentRooms.next();
            Iterator<Player> playersHere = r.getPlayers();

            while (playersHere.hasNext())
                farPlayers.add(playersHere.next());
        }

        DungeonServer.events.addNarrationEvent(
                DungeonDispatcher.playerIteratorToWriterArray(ps, n), narr1);
        DungeonServer.events.addNarrationEvent(
                DungeonDispatcher.playerIteratorToWriterArray(
                        farPlayers.iterator(), farPlayers.size()), narr2);

    }

    public synchronized Item take(Player p, String s)
            throws NoSuchItemException
    {
        Item i = p.here().removeItemByName(s);
        p.addToInventory(i);
        return i;
    }

    public synchronized Item drop(Player p, String s)
            throws NoSuchItemException
    {
        Item i = p.dropFromInventoryByName(s);
        p.here().addItem(i);
        return i;
    }

    public synchronized Item give(Player p, String object, String whom)
            throws NoSuchItemException, NoSuchPlayerException
    {

        Item i = p.dropFromInventoryByName(object);
        Iterator<Player> ps = p.here().getPlayers();

        Player otherPlayer = null;
        while (ps.hasNext()) {
            Player thisPlayer = ps.next();
            if (thisPlayer.getName().equals(whom)) {
                otherPlayer = thisPlayer;
                break;
            }
        }

        if (otherPlayer == null)
            throw new NoSuchPlayerException();

        otherPlayer.addToInventory(i);

        return i;
    }
}
