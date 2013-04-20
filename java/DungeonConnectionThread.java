import java.net.*;
import java.io.*;

import dungeon.*;
import dungeon.exceptions.*;

public class DungeonConnectionThread extends Thread {
  private Socket client;

  public DungeonConnectionThread(Socket s) {
    this.client = s;
  }

  public void run() {
    PrintWriter out = null;
    BufferedReader in = null;
    try {
      out = new PrintWriter(this.client.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(
                              this.client.getInputStream()));

    } catch (IOException e) {
      System.err.print("DungeonServer: failed getting client streams\n");
      return;
    }

    String name = null;
    try {
      /* Expect the first data sent from the client to be the name */
      name = in.readLine();

      if (DungeonServer.universe == null)
        throw new NoUniverseException();

      System.out.printf("player '%s' connected (start of stream)\n", name);

      /* Try to access saved state in universe for this player */
      Player p = null;
      if (DungeonServer.universe.hasSavedState(name))
        p = DungeonServer.universe.restore(name, out);
      else
        p = DungeonServer.universe.register(name, out);

      out.println("Connected.");

      /* Handle communications from client to protocol */
      String toProtocol;
      String fromProtocol;

      /* Read lines from client and wait for response from protocol */
      while ((toProtocol = in.readLine()) != null) {
        fromProtocol = DungeonProtocol.process(p, toProtocol);

        /* Protocol indicates that the client wants to disconnect */
        if (fromProtocol == null)
          break;
        else if (fromProtocol.isEmpty())
          continue;
        else
          out.println(fromProtocol);
        
      }
      
      System.out.printf("player '%s' disconnected (quitting)\n", name);

      out.close();
      in.close();
      client.close();

    } catch (IOException e) {
      System.err.print("DungeonServer: failed reading or closing streams\n");
      System.out.printf("player '%s' disconnected (socket failure)\n", name);
      e.printStackTrace();
    } catch (NoUniverseException e) {
      System.err.print("DungeonServer: universe missing or not ready\n");
      System.out.printf("player '%s' disconnected (no universe)\n", name);
    }
    
  }
}
