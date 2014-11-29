package com.abreen.dungeon;

import java.io.*;
import java.net.*;

public class DungeonClient {
    public static void main(String[] args) throws IOException {

        /* Scan for program arguments */
        String name = null;
        String host = null;
        int port = -1;
        try {
            name = args[0];
            host = args[1];
            port = Integer.parseInt(args[2]);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("DungeonClient: specify a name, server, and port");
            System.out.println(usage());
            System.exit(1);
        }

        /* Attempt to connect and set up streams */
        Socket server = null;
        PrintWriter out = null;
        BufferedReader in = null;
        try {
            server = new Socket(host, port);
            out = new PrintWriter(server.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(
                    server.getInputStream()));

        } catch (UnknownHostException e) {
            System.err.printf("DungeonClient: could not find host '%s'\n", host);
            System.exit(2);
        } catch (IOException e) {
            System.err.println("DungeonClient: could not get I/O for connection");
            System.exit(2);
        }

        /* Send username */
        out.println(name);

        /* Set up console reader */
        BufferedReader cons = new BufferedReader(new InputStreamReader(
                System.in));
        String toServer;
        String fromServer;

        while ((fromServer = in.readLine()) != null) {
            System.out.println(fromServer);

            toServer = cons.readLine();
            out.println(toServer);
        }

        out.close();
        in.close();
        cons.close();
        server.close();

    }

    private static String usage() {
        return "usage: java DungeonClient <name> <server> <port>";
    }
}
