import java.util.*;
import java.io.*;
import java.net.*;

import dungeon.*;

public class DungeonClient {
 
public static void main(String[] args) {
    
    /* Scan for program arguments */
    String name = null;
    String host = null;
    int port = -1;
    try {
      name = args[0];
      host = args[1];
      port = Integer.parseInt(args[2]);
    } catch (ArrayIndexOutOfBoundsException e) {
      System.err.print("DungeonClient: specify a name, server, and port\n");
      System.out.println(usage());
      System.exit(1);
    }

    DungeonClient client = new DungeonClient(name, host, port);
    client.begin();
  }

  private static String usage() {
    return "usage: java DungeonClient <name> <server> <port>";
  }

  private String name;
  private String host;
  private int port;
  
  public DungeonClient(String name, String host, int port) {
	  this.name = name;
	  this.host = host;
	  this.port = port;
  }

  private void begin() {
    System.out.println("Connecting");
    long startTime = System.currentTimeMillis();
    // Verify the address
    InetAddress address = null;
    try {
      address = InetAddress.getByName(host); 
    } catch (UnknownHostException e) {
      System.err.println("The specified address was invalid. (" + e.getMessage() + ")");
      return;
    }
    System.out.println("...");
    // Attempt to connect
    Socket socket = null;
    try {
      socket = new Socket(address, port);
    } catch (IOException e) {
      System.err.println("An error occurred connecting to the server");
      e.printStackTrace();
      return;
    }
    long timeTakenMS = System.currentTimeMillis() - startTime;
    System.out.println("Successfully connected as " + name + " (" + timeTakenMS + "ms)");
    playGame(socket);
  }

  private void playGame(Socket socket) {
    PrintWriter out = null;
    BufferedReader in = null;
    try {
      out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    } catch (IOException e) {
      System.err.println("An error occurred preparing the streams (" + e.getMessage() + ")");
      e.printStackTrace();
      return;
    }
    
    BufferedReader cons = new BufferedReader(new InputStreamReader(System.in));
    
    try {
      String toServer;
      String fromServer;
      /* Send username */
      out.println(name);
      out.flush();
      while ((fromServer = in.readLine()) != null) {
        while(in.ready()) {
          fromServer += "\n" + in.readLine();
        }
        if(!fromServer.isEmpty())
          System.out.println(fromServer);
        toServer = cons.readLine();
        out.println(toServer);
        out.flush();
      }
      System.out.println("Quitting");
      out.close();
      in.close();
      cons.close();
      socket.close();
    }catch(IOException ex) {
      System.err.println("Connection shutdown unexpectedly: " + ex.getMessage());
      ex.printStackTrace();
      return;
    }
  }
}
