package com.abreen.dungeon;

import java.io.*;
import java.util.*;
import java.net.*;

import com.abreen.dungeon.util.*;
import com.abreen.dungeon.worker.*;
import com.abreen.dungeon.model.*;

import org.yaml.snakeyaml.*;

public class DungeonServer {
    public static final String DEFAULT_UNIVERSE_FILE = "default.universe.yml";
    public static final String CONFIGURATION_FILE = "config.yml";
    public static final String YAML_PATH = "yaml" + File.separator;

    private static Yaml yamlInstance;

    public static DungeonUniverse universe;
    public static DungeonDispatcher events;
    public static DungeonNarrator narrator;

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws IOException {
        boolean useArguments = false;
        Map config = null;

        try {
            yamlInstance = new Yaml();

            /*
             * Try to load configuration file
             */
            FileReader configFile = new FileReader(YAML_PATH
                    + CONFIGURATION_FILE);
            config = (Map) yamlInstance.load(configFile);

        } catch (FileNotFoundException e) {
            System.out.println("Could not load configuration file. Using program "
                            + "arguments instead.");
            useArguments = true;
        }

        /* Scan for program arguments */
        int port = -1;
        try {

            if (useArguments)
                port = Integer.parseInt(args[0]);
            else
                // take the port number from the config file
                port = (Integer) config.get("port");

        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("DungeonServer: specify a port to which to bind");
            System.out.println(usage());
            System.exit(1);
        }

        /* Attempt to bind to port */
        ServerSocket server = null;
        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            System.err.printf("DungeonServer: could not listen on port %d\n",
                    port);
            System.exit(2);
        }

        System.out.printf("bound to port %d\n", port);

        System.out.println("loading universe:");

        /* Load universe */
        try {
            System.out.println("\tfinding universe file");

            FileReader universeFile = null;
            if (useArguments)
                universeFile = new FileReader(YAML_PATH + DEFAULT_UNIVERSE_FILE);
            else
                universeFile = new FileReader(YAML_PATH + config.get("world")
                        + ".universe.yml");

            Object[] docs = new Object[3];
            Map<String, Object> preamble = null;
            Map<String, Map<String, Object>> rooms = null, items = null;

            try {
                System.out.println("\tchecking universe file");

                int i = 0;
                for (Object o : yamlInstance.loadAll(universeFile))
                    docs[i++] = o;

                preamble = (Map<String, Object>) docs[0];
                rooms = (Map<String, Map<String, Object>>) docs[1];
                items = (Map<String, Map<String, Object>>) docs[2];

                if (preamble == null || rooms == null || items == null)
                    throw new NullPointerException();

            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("DungeonServer: error parsing universe " +
                        "file: too many documents in universe file");
                System.exit(3);
            } catch (NullPointerException e) {
                System.err.println("DungeonServer: error parsing universe " +
                        "file: too few documents in universe file");
                System.exit(3);
            }

            /*
             * Used after parsing rooms to set universe parameters
             */
            boolean doWeather = false;
            String spawnRoomID = null;
            try {
                System.out.println("\treading preamble");

                /*
                 * Load universe parameters from the preamble
                 */
                doWeather = (Boolean) validateAndGet(preamble, "weather",
                        Boolean.class);

                spawnRoomID = (String) validateAndGet(preamble, "spawn",
                        String.class);

            } catch (Exception e) {
                System.err.println("DungeonServer: failed parsing preamble ("
                        + e.getMessage() + ")");
                System.exit(4);
            }

            /*
             * Loop through room definitions in universe file
             */

            /**
             * This hash map is used to resolve references from one room to
             * another. Each time a room is parsed, it is added to this map, and
             * henceforth back references to the newly added room will be
             * resolved by checking this map.
             */
            HashMap<String, Room> knownRooms = new HashMap<String, Room>();

            /**
             * This list is maintained to easily check at the end of parsing if
             * there are still references to unseen rooms.
             */
            ArrayList<String> unseenRooms = new ArrayList<String>();

            /**
             * This is a list of triples (A, B, C) such that A is a room
             * waiting for a reference to another room, C, through a direction
             * B. For A and B, the string ID of the rooms are used (the same
             * key used in the knownRooms hash map). This list is used whenever
             * a room's exit references cannot actually be resolved because the
             * destination room has not yet been parsed. At the end of parsing,
             * as long as the unseenRooms list is empty, this list is traversed
             * to resolve the remaining references.
             */
            ArrayList<Triple<String, Space.Direction, String>> unresolved;
            unresolved =
                    new ArrayList<Triple<String, Space.Direction, String>>();

            String thisRoomID = null;
            try {
                System.out.println("\tparsing rooms");

                for (Map.Entry<String, Map<String, Object>> m :
                        rooms.entrySet())
                {
                    thisRoomID = m.getKey();
                    Map<String, Object> thisMap = m.getValue();

                    String roomName = (String) validateAndGet(thisMap, "name",
                            String.class);

                    String description = (String) validateAndGet(thisMap,
                            "description", String.class);

                    boolean isOutside = (Boolean) validateAndGet(thisMap,
                            "isOutside", Boolean.class);

                    Room r = new Room(roomName, description, isOutside);

                    if (thisMap.containsKey("neverUseArticle")) {
                        boolean neverUseArticle = (Boolean) validateAndGet(
                                thisMap, "neverUseArticle", Boolean.class);

                        r.setNeverUseArticle(neverUseArticle);
                    }

                    if (unseenRooms.contains(thisRoomID))
                        unseenRooms.remove(thisRoomID);

                    knownRooms.put(thisRoomID, r);

                    /*
                     * Process exits out of this room
                     */
                    Map<String, String> exits = (Map) validateAndGet(thisMap,
                            "exits", Map.class);

                    for (Map.Entry<String, String> exit : exits.entrySet()) {
                        String thisDirection = exit.getKey();
                        String toRoomID = exit.getValue();

                        /*
                         * Verify the direction from the file
                         */
                        Space.Direction dir;
                        dir = Space.getDirectionFromString(thisDirection);
                        if (dir == null)
                            throw new InvalidDirectionException(thisDirection);

                        /*
                         * Look up the destination room in the hash map
                         */
                        if (knownRooms.containsKey(toRoomID))
                            r.addExit(dir, knownRooms.get(toRoomID));
                        else {
                            if (!unseenRooms.contains(toRoomID))
                                unseenRooms.add(toRoomID);

                            Triple<String, Space.Direction, String> t;
                            t = new Triple<String, Space.Direction,
                                    String>(thisRoomID, dir, toRoomID);
                            unresolved.add(t);
                        }

                    }

                }
            } catch (Exception e) {
                System.err.println("DungeonServer: failed parsing room '"
                        + thisRoomID + "' (" + e.getMessage() + ")");
                System.exit(4);
            }

            if (!unseenRooms.isEmpty())
                throw new UnresolvedReferenceException(unseenRooms);

            /*
             * Invariant: There were no references to undefined rooms in the
             * file. Invariant: All the rooms in the file have been
             * instantiated.
             * 
             * All rooms in the universe file have been parsed, but there may
             * still be exits waiting to be added because their destination was
             * not yet parsed at the time. Now loop through the unresolved list
             * to set them up.
             */
            for (Triple<String, Space.Direction, String> t : unresolved) {
                Room fromRoom = knownRooms.get(t.first);
                Room toRoom = knownRooms.get(t.third);
                Space.Direction dir = t.second;

                fromRoom.addExit(dir, toRoom);
            }

            /*
             * Invariant: All exits in the file have been set up among the
             * rooms.
             */

            Room spawnRoom = knownRooms.get(spawnRoomID);
            universe = new DungeonUniverse(spawnRoom, doWeather,
                    knownRooms.values());

            universeFile.close();

        } catch (Exception e) {
            System.err.println("DungeonServer: failed loading universe " + "("
                    + e.getMessage() + ")");
            System.exit(2);
        }

        System.out.println("loaded universe");

        /* Start narrator */
        try {
            narrator = new DungeonNarrator();
        } catch (Exception e) {
            System.err.println("DungeonServer: failed starting narrator");
            System.exit(3);
        }

        System.out.println("started narrator");

        /* Start accepting events */
        try {
            events = new DungeonDispatcher();
            events.start();
        } catch (Exception e) {
            System.err.println("DungeonServer: failed starting event queue");
            System.exit(3);
        }

        System.out.println("started event queue");

        /* Listen for clients */
        try {
            System.out.println("listening for clients");

            while (true)
                new DungeonConnectionThread(server.accept()).start();
        } catch (IOException e) {
            System.err.printf("DungeonServer: failed accepting client on port %d\n",
                            port);
            System.exit(2);
        } finally {
            server.close();
        }

    }

    private static String usage() {
        return "usage: java DungeonServer <port>";
    }

    /**
     * Exception thrown at the end of parsing if there are still rooms to which
     * references have been made but whose definitions are not in the universe
     * file.
     */
    private static class UnresolvedReferenceException extends Exception {
        private static final long serialVersionUID = 1L;

        /**
         * Constructs a new UnresolvedReferenceException that takes a list of
         * strings corresponding to the unresolved room IDs left over.
         */
        public UnresolvedReferenceException(ArrayList<String> list) {
            super(list.size() > 1 ?
                    "rooms referenced but never actually defined: " + list
                    : "room referenced but never actually defined: " +
                        list.get(0));
        }
    }

    /**
     * Given a map between a string and an object of unknown type, attempt to
     * retrieve what value is mapped to from the specified string and validate
     * that the actual type matches the specified class.
     * 
     * @return Null if the types do not match, or the value
     */
    private static Object validateAndGet(Map<String, Object> m, String s,
            Class<?> c)
        throws MissingMappingException, UnexpectedTypeException
    {
        Object o = m.get(s);
        if (o == null)
            throw new MissingMappingException(s);

        if (!c.isAssignableFrom(o.getClass()))
            throw new UnexpectedTypeException(s, c.toString(), o.getClass()
                    .toString());
        else
            return o;
    }

    /**
     * Thrown while parsing a universe file and a room or item is missing a
     * required mapping (e.g., a room is missing a name).
     */
    private static class MissingMappingException extends Exception {
        private static final long serialVersionUID = 1L;
        public MissingMappingException(String s) {
            super("expected key '" + s + "', but it is missing");
        }
    }

    /**
     * Thrown while parsing a universe file and somewhere in the YAML we get a
     * type not expected for a certain mapping (e.g., got a number for isOutside
     * and not boolean).
     */
    private static class UnexpectedTypeException extends Exception {
        private static final long serialVersionUID = 1L;
        public UnexpectedTypeException(String keyName, String expected,
                String got) {
            super("for '" + keyName + "', expected type '" + expected
                    + "', but " + "got '" + got + "' instead");
        }
    }

    /**
     * Thrown when a universe file tries to mention an exit in a direction that
     * is misspelled or otherwise unacceptable.
     */
    private static class InvalidDirectionException extends Exception {
        private static final long serialVersionUID = 1L;
        public InvalidDirectionException(String invalidDirection) {
            super("'" + invalidDirection + "' is not a valid direction");
        }
    }

}
