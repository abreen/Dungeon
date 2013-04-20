import java.util.*;
import java.io.*;
import java.net.*;

import dungeon.*;

public class DungeonServer {
  public static DungeonUniverse universe;

  public static void main(String[] args) throws IOException {

    /* Scan for program arguments */
    int port = -1;
    try {
      port = Integer.parseInt(args[0]);
    } catch (ArrayIndexOutOfBoundsException e) {
      System.err.println("DungeonServer: specify a port to which to bind");
      System.out.println(usage());
      System.exit(1);
    }

    /* Load universe */
    try {
      universe = new DungeonUniverse();
    } catch (Exception e) {
      System.err.println("DungeonServer: failed loading universe");
      System.exit(2);
    }

    System.out.println("loaded universe");

    /* Attempt to bind to port */
    ServerSocket server = null;
    try {
      server = new ServerSocket(port);
    } catch (IOException e) {
      System.err.printf("DungeonServer: could not listen on port %d\n", port);
      System.exit(2);
    }

    System.out.printf("bound to port %d\n", port);

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
