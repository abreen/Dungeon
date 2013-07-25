package com.abreen.dungeon;

import com.abreen.dungeon.model.Room;
import com.abreen.dungeon.model.Space;
import com.abreen.dungeon.model.Space.Direction;
import java.io.*;
import java.util.*;
import java.net.*;
import com.abreen.dungeon.worker.DungeonUniverse;
import com.abreen.dungeon.worker.DungeonDispatcher;
import com.abreen.dungeon.worker.DungeonNarrator;
import com.abreen.dungeon.worker.DungeonConnectionThread;

import org.yaml.snakeyaml.*;

public class DungeonServer {
  public static final String DEFAULT_UNIVERSE_FILE = "default.universe.yml";
  public static final String CONFIGURATION_FILE = "config.yml";
  public static final String YAML_PATH = "yaml" + File.separator;
  
  private static Yaml yamlInstance;
  
  public static DungeonUniverse universe;     // reference to universe instance
  public static DungeonDispatcher events;     // reference to event queue
  public static DungeonNarrator narrator;     // reference to narrator

  public static void main(String[] args) throws IOException {
    boolean useArguments = false;
    Map config = null;
    
    try {
      yamlInstance = new Yaml();
      
      /*
       * Try to load configuration file
       */
      FileReader configFile = new FileReader(YAML_PATH + CONFIGURATION_FILE);
      config = (Map) yamlInstance.load(configFile);
      
    } catch (FileNotFoundException e) {
      System.out.println("Could not load configuration file. Using program " +
                         "arguments instead.");
      useArguments = true;
    }
    
    /* Scan for program arguments */
    int port = -1;
    try {
      
      if (useArguments)
        port = Integer.parseInt(args[0]);
      else  // take the port number from the config file
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
      System.err.printf("DungeonServer: could not listen on port %d\n", port);
      System.exit(2);
    }

    System.out.printf("bound to port %d\n", port);

    /* Load universe */
    try {
      FileReader universeFile = null;
      if (useArguments)
        universeFile = new FileReader(YAML_PATH + DEFAULT_UNIVERSE_FILE);
      else
        universeFile = new FileReader(YAML_PATH + config.get("world") + 
                                      ".universe.yml");
      
      universe = new DungeonUniverse();
      Object[] docs = new Object[3];
      Map<String, Object> preamble = null;
      Map<String, Map<String, Object>> rooms = null, items = null;
      
      try {
        int i = 0;
        for (Object o : yamlInstance.loadAll(universeFile)) {
          docs[i++] = o;
        }
        
        preamble = (Map<String, Object>) docs[0];
        rooms = (Map<String, Map<String, Object>>) docs[1];
        items = (Map<String, Map<String, Object>>) docs[2];
        
        if (preamble == null || rooms == null || items == null)
          throw new NullPointerException();
        
      } catch (ArrayIndexOutOfBoundsException e) {
        System.err.println("DungeonServer: error parsing universe file: " +
                           "too many documents in universe file");
        System.exit(3);
      } catch (NullPointerException e) {
        System.err.println("DungeonServer: error parsing universe file: " +
                           "too few documents in universe file");
        System.exit(3);
      }
      
      String spawnRoomID = null;
      try {
        /*
         * Load variables from the preamble
         */
        boolean doWeather =
                (Boolean) validateAndGet(preamble, "weather", Boolean.class);
        
        universe.setWeather(doWeather);
        
        spawnRoomID =
                (String) validateAndGet(preamble, "spawn", String.class);
        
      } catch (Exception e) {
        System.err.println("DungeonServer: failed parsing preamble (" + 
                           e.getMessage() + ")");
        System.exit(4);
      }
      
      /*
       * Loop through room definitions in universe file
       */
      
      /**
       * This hash map is used to resolve references in exits from one room
       * to another. Each time a room is parsed, it is added to this map,
       * and then henceforth back references to the newly added room will
       * be resolved by checking this hash map.
       * 
       * If a room makes a reference to another room that has not yet been
       * seen, the not-yet-seen room is also added to a list that can be
       * checked at the end of parsing for invalid references.
       */
      HashMap<String, Room> knownRooms = new HashMap<>();
      ArrayList<String> unresolvedReferences = new ArrayList<>();
      
      String thisRoomID = null;
      try {
        for (Map.Entry<String, Map<String, Object>> m : rooms.entrySet()) {
          thisRoomID = m.getKey();
          Map<String, Object> thisMap = m.getValue();
          
          String roomName =
                  (String) validateAndGet(thisMap, "name", String.class);
          
          String description =
                  (String) validateAndGet(thisMap, "description", String.class);
          
          boolean isOutside = 
                  (Boolean) validateAndGet(thisMap, "isOutside", Boolean.class);
          
          Room r = new Room(roomName, description, isOutside);
          
          if (unresolvedReferences.contains(thisRoomID)) {
            knownRooms.remove(thisRoomID);
            knownRooms.put(thisRoomID, r);
            unresolvedReferences.remove(thisRoomID);
          } else {
            knownRooms.put(thisRoomID, r);
          }
          
          /*
           * Process exits away from this room, adding unseen rooms to the
           * list/hash map when appropriate.
           */
          Map<String, String> exits =
                  (Map) validateAndGet(thisMap, "exits", Map.class);
          
          for (Map.Entry<String, String> exit : exits.entrySet()) {
            String thisDirection = exit.getKey();
            String toRoom = exit.getValue();
            
            /*
             * Verify the direction from the file
             */
            Space.Direction dir = Space.Direction.fromString(thisDirection);
            if (dir == null)
              throw new InvalidDirectionException(thisDirection);
            
            /*
             * Look up the destination room in the hash map
             */
            if (knownRooms.containsKey(toRoom)) {
              r.addExit(dir, knownRooms.get(toRoom));
            } else {
              unresolvedReferences.add(toRoom);
              UnresolvedReferenceRoom u = new UnresolvedReferenceRoom();
              knownRooms.put(toRoom, u);
              r.addExit(dir, u);
            }
            
          }

        } // end of for loop adding rooms
      } catch (Exception e) {
        System.err.println("DungeonServer: failed parsing room '" +
                            thisRoomID + "' (" + e.getMessage() + ")");
        System.exit(4);
      } // end of try/catch adding rooms
      
      /*
       * Invariant: All rooms in universe file are in the hash map. The
       * rooms to which references were found but had no actual definition
       * are in the unresolvedReferences list.
       */
       if (!unresolvedReferences.isEmpty())
         throw new UnresolvedReferenceException(unresolvedReferences);
      
    } catch (Exception e) {
      System.err.println("DungeonServer: failed loading universe " +
                         "(" + e.getMessage() + ")");
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

      while (true) {
        new DungeonConnectionThread(server.accept()).start();
      }
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
   * Exception thrown at the end of parsing if there are still rooms to
   * which references have been made but whose definitions are not in the
   * universe file.
   */
  private static class UnresolvedReferenceException extends Exception {

    /**
     * Constructs a new UnresolvedReferenceException that takes a list
     * of strings corresponding to the unresolved room IDs left over.
     */
    public UnresolvedReferenceException(ArrayList<String> list) {
      super(list.size() > 1 ? "rooms referenced but never actually defined: " +
      list : "room referenced but never actually defined: " + list.get(0));
    }
  }
  
  /**
   * Class used when parsing the universe file. If a room references another
   * room not yet parsed, an instance of this class is used in the hash map.
   * Later, all instances of this class must be replaced by the appropriate
   * instance of the superclass. If not, the universe file is invalid.
   */
  private static class UnresolvedReferenceRoom extends Room {
    public UnresolvedReferenceRoom() {
      super("(unresolved reference)", "");
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
          Class<?> c) throws MissingMappingException,
                                   UnexpectedTypeException {
    
    Object o = m.get(s);
    if (o == null)
      throw new MissingMappingException(s);
    
    if (!c.isAssignableFrom(o.getClass()))
      throw new UnexpectedTypeException(s, c.toString(), o.getClass().toString());
    else
      return o;
  }
  
  /**
   * Thrown while parsing a universe file and a room or item is missing a
   * required mapping (e.g., a room is missing a name).
   */
  private static class MissingMappingException extends Exception {
    public MissingMappingException(String s) {
      super("expected key '" + s + "', but it is missing");
    }
  }
  
  /**
   * Thrown while parsing a universe file and somewhere in the YAML we get
   * a type not expected for a certain mapping (e.g., got a number for
   * isOutside and not boolean).
   */
  private static class UnexpectedTypeException extends Exception {
    public UnexpectedTypeException(String keyName, String expected, String got) {
      super("for '" + keyName + "', expected type '" + expected + "', but " +
            "got '" + got + "' instead");
    }
  }
  
  /**
   * Thrown when a universe file tries to mention an exit in a direction
   * that is misspelled or otherwise unacceptable.
   */
  private static class InvalidDirectionException extends Exception {
    public InvalidDirectionException(String invalidDirection) {
      super("'" + invalidDirection + "' is not a valid direction");
    }
  }

}
