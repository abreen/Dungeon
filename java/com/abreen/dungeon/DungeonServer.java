package com.abreen.dungeon;

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
      universe = new DungeonUniverse();
    } catch (Exception e) {
      System.err.println("DungeonServer: failed loading universe");
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
}
