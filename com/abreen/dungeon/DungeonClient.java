package com.abreen.dungeon;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import com.abreen.dungeon.worker.DungeonDispatcher;
import com.abreen.dungeon.worker.DungeonProtocol;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.ansi.UnixTerminal;

public class DungeonClient {
    /*
     * Size of the read buffer used to store individual messages from
     * the server.
     */
    public static int BUFFER_SIZE = 262144;

    /*
     * Character buffer used to store individual messages from the server.
     * The main client thread reads characters into this buffer and notifies
     * the display thread when the buffer is ready to be read.
     */
    public static char[] buf = new char[BUFFER_SIZE];
    
    private static Socket server = null;
    private static PrintWriter out = null;
    private static BufferedReader in = null;

    public static void main(String[] args) {
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

        // attempt to connect and set up streams
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

        // send user name
        out.println(name);

        // start display thread
        DungeonDisplayThread disp = new DungeonDisplayThread(out);
        disp.start();

        try {
            int len = 0;
            while ((len = in.read(buf, 0, BUFFER_SIZE)) != -1)
                disp.update(buf, len);
           
            quit();
            
        } catch (IOException e) {
            System.err.println("failed in communication loop");
            System.exit(5);
        }
    }

    private static String usage() {
        return "usage: java DungeonClient <name> <server> <port>";
    }
    
    public static void quit() {
        if (out != null)
            out.close();
        
        if (server != null) {
            try {
                server.close();
            } catch (IOException e) {
                
            }
        }
        
        System.out.println("\nDisconnected.");
        System.exit(0);
    }
}


/*
 * When run by the main thread, this thread takes control of the terminal
 * display and allows the user to compose a message to the server. Whenever
 * the main thread receives a message from the server, this thread is
 * notified and the terminal screen is updated with the new message.
 */
class DungeonDisplayThread extends Thread {
    private static int BUFFER_SIZE = 4096;
    private static int HISTORY_LIMIT = 1000;    // how many server lines to keep

    private PrintWriter toServer;
    
    private Screen screen;
    
    private int rows, columns;
    
    private char[] localBuffer;         // buffer for composing a message
    private int i;                      // index into localBuffer
    
    private LinkedList<String> lines;   // history of server messages, by line
    
    public DungeonDisplayThread(PrintWriter out) {
        this.toServer = out;
        
        Terminal t = null;
        try {
            t = new UnixTerminal();
            this.screen = new TerminalScreen(t);
            
            TerminalSize size = t.getTerminalSize();
            
            this.rows = size.getRows();
            this.columns = size.getColumns();
            
        } catch (IOException e) {
            System.err.println("DungeonClient: failed to get control of " +
                    "terminal");
            System.exit(1);
        }
        
        this.localBuffer = new char[BUFFER_SIZE];
        this.i = 0;
        
        this.lines = new LinkedList<String>();
    }

    /*
     * Called when the main thread gets a new message from the server.
     */
    public synchronized void update(char[] buf, int len) throws IOException {
        String s = String.valueOf(buf, 0, len);
        String[] lines = s.split("\n");
        
        for (String line : lines) {
            String[] tokens = line.split("\\s+");
            String newLine = "";
            int token = 0;
            
            while (token < tokens.length) {
                do {
                    if (newLine.length() + tokens[token].length() + 1 > columns)
                        break;
                    
                    newLine += tokens[token++] + " ";
                } while (newLine.length() < columns && token < tokens.length);
                
                this.lines.addFirst(newLine);
                newLine = "";
            }
        }
        
        drawMessages();
        refresh();
        
        if (this.lines.size() > HISTORY_LIMIT) {
            for (int i = 0; i < this.lines.size() - HISTORY_LIMIT; i++)
                this.lines.removeLast();
        }
    }


    public void run() {
        try {
            screen.startScreen();
            
            drawMessages();
            drawPrompt();
            refresh();
            
            while (true) {
                KeyStroke k = screen.readInput();
                
                if (k == null)
                    continue;
                
                KeyType type = k.getKeyType();
                
                switch (type) {
                
                case F10:
                    screen.stopScreen();
                    DungeonClient.quit();
                    break;
                    
                case Enter:
                    sendLocalBuffer();
                    
                    // this effectively clears the prompt
                    i = 0;
                    
                    drawPrompt();
                    refresh();
                    break;
                    
                case Backspace:
                    if (i > 0) {
                        i--;
                        drawPrompt();
                        refresh();
                    }
                    break;
                    
                default:
                    char ch = k.getCharacter();
                    localBuffer[i++] = ch;
                    drawPrompt();
                    refresh();
                }
            }
            
        } catch (IOException e) {
            System.err.println("DungeonClient: terminal failure");
            System.exit(2);
        }
    }
    
    
    /*
     * Writes the contents of localBuffer into the bottom row of the screen
     * after the prompt ">" symbol.
     * 
     * This method doesn't refresh the screen.
     */
    private void drawPrompt() {
        clearLastLine();
        
        String s = String.valueOf(localBuffer, 0, i);
        putString(0, rows - 1, "> ", TextColor.ANSI.BLUE);
        putString(2, rows - 1, s);
    }
    
    
    private void clearLastLine() {
        String z = "";
        for (int j = 0; j < columns; j++)
            z += " ";
        
        putString(0, rows - 1, z);
    }
    
    
    /*
     * Writes the contents of the server message history to the screen ---
     * as many lines as possible. The bottom row is skipped, since the prompt
     * occupies that space.
     * 
     * This method doesn't refresh the screen.
     */
    private void drawMessages() {
        clearMessageArea();
        
        Iterator<String> it = lines.iterator();
        for (int j = rows - 2; j >= 0; j--) {
            if (!it.hasNext())
                break;
            
            String str = it.next();
         
            String[] specials = {
                    DungeonDispatcher.CHEVRONS,
                    DungeonDispatcher.ASTERISKS,
                    DungeonDispatcher.BANGS
            };

            if (str.indexOf(specials[0]) == 0) {
                putString(0, j, specials[0], TextColor.ANSI.MAGENTA);
                putString(specials[0].length(), j,
                        str.substring(specials[0].length()));
                
            } else if (str.indexOf(specials[1]) == 0) {
                putString(0, j, specials[1], TextColor.ANSI.YELLOW);
                putString(specials[1].length(), j,
                        str.substring(specials[1].length()));
                
            } else if (str.indexOf(specials[2]) == 0) {
                putString(0, j, specials[2], TextColor.ANSI.RED);
                putString(specials[2].length(), j,
                        str.substring(specials[2].length()));
                
            } else {
                putString(0, j, str);
            }
        }
    }
    
    
    private void clearMessageArea() {
        String z = "";
        for (int j = 0; j < columns; j++)
            z += " ";
        
        for (int j = rows - 2; j >= 0; j--)
            putString(0, j, z);
    }

    
    /*
     * Does the same thing as screen.refresh(), but it moves the cursor to
     * the right edge of the prompt text after refreshing.
     */
    private void refresh() {
        TerminalPosition pos = new TerminalPosition(i + 2, rows - 1);
        screen.setCursorPosition(pos);
        
        try {
            screen.refresh();
        } catch (IOException e) {
            System.err.println("failed refreshing terminal");
            System.exit(3);
        }
    }
    
    
    private void sendLocalBuffer() {
        // don't send empty strings to server
        if (i == 0)
            return;
        
        String s = String.valueOf(localBuffer, 0, i);
        toServer.println(s);
    }
    
    
    private void putString(int col, int row, String str) {
        putString(col, row, str, TextColor.ANSI.DEFAULT);
    }
    
    
    private void putString(int col, int row, String str, TextColor.ANSI c) {
        for (int i = 0; i < str.length(); i++) {
            TextCharacter ch = new TextCharacter(str.charAt(i), c,
                    TextColor.ANSI.DEFAULT);
            
            screen.setCharacter(col + i, row, ch);
        }
    }
}
