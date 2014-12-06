package com.abreen.dungeon;

import java.io.*;
import java.net.*;
import java.util.*;

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
    public static int BUFFER_SIZE = 1 << 16;

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
            System.exit(5);
        } catch (IOException e) {
            System.err.printf("DungeonClient: could not connect to host " +
                    "'%s'\n", host);
            System.exit(6);
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
            System.exit(4);
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
    private static int MESSAGE_LIMIT = 1000;    // how many server lines to keep
    private static int HISTORY_LIMIT = 100;     // size of local command history

    private PrintWriter toServer;
    
    private Screen screen;
    
    private int rows, columns;
    
    private char[] localBuffer;         // buffer for composing a message
    private int bufferIndex;                      // index into localBuffer
    
    private LinkedList<String> lines;   // history of server messages, by line
    
    private LinkedList<String> localLines;  // history of local commands
    
    /*
     * When > 1, the user pressed Up/Down arrow one or more times and is
     * browsing the local history of commands. Upon pressing Enter, that
     * command is added to the local history and this field is reset to -1.
     */
    private int historyIndex;
    
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
            System.exit(2);
        }
        
        this.localBuffer = new char[BUFFER_SIZE];
        this.bufferIndex= 0;
        
        this.lines = new LinkedList<String>();
        
        this.localLines = new LinkedList<String>();
        this.historyIndex = -1;
    }

    /*
     * Called when the main thread gets a new message from the server.
     */
    public synchronized void update(char[] buf, int len) throws IOException {
        String s = String.valueOf(buf, 0, len);
        String[] lines = s.split("\n");
        
        // whether this line starts with DungeonDispatcher.CHEVRONS
        boolean isSpecial = false;
        
        for (String line : lines) {
            isSpecial = line.indexOf(DungeonDispatcher.CHEVRONS) == 0;
            String[] tokens = line.split("\\s");
            int token = 0;
            
            int prefixLength = 0;
            if (isSpecial)
                prefixLength = DungeonDispatcher.CHEVRONS.length();
            
            String newLine = "";
            
            while (token < tokens.length) {
                do {
                    if (newLine.length() + tokens[token].length() + 1 > columns)
                        break;
                    
                    newLine += tokens[token++] + " ";
                } while (newLine.length() < columns && token < tokens.length);
                
                this.lines.addFirst(newLine);
                newLine = repeat(" ", prefixLength);
            }
        }
        
        drawMessages();
        refresh();
        
        if (this.lines.size() > MESSAGE_LIMIT) {
            for (int i = 0; i < this.lines.size() - MESSAGE_LIMIT; i++)
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
                pruneHistory();
                
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
                    
                    String cmd = String.valueOf(localBuffer, 0, bufferIndex);
                    localLines.addFirst(cmd);
                    
                    // this effectively clears the prompt
                    bufferIndex = 0;
                    
                    drawPrompt();
                    refresh();
                    break;
                    
                case Backspace:
                    if (bufferIndex > 0) {
                        bufferIndex--;
                        drawPrompt();
                        refresh();
                    }
                    break;
                
                case ArrowUp:
                    // replace local buffer with previous line in history
                    historyIndex++;
                    
                    if (historyIndex == localLines.size()) {
                        historyIndex = -1;
                        
                        // reset back to blank line
                        bufferIndex = 0;
                    }
                    
                    loadLineFromHistory();

                    drawPrompt();
                    refresh();
                    
                    break;
                
                case ArrowDown:
                    // replace local buffer with next line in history
                    historyIndex--;
                    
                    if (historyIndex == -2) {
                        historyIndex = localLines.size() - 1;
                    } else if (historyIndex == -1) {
                        // reset back to blank line
                        bufferIndex = 0;
                    }
                    
                    loadLineFromHistory();
                    
                    drawPrompt();
                    refresh();
                    
                    break;
                    
                default:
                    Character ch = k.getCharacter();
                    
                    if (ch == null) {
                        // character was special key type that we should ignore
                        continue;
                    }
                    
                    localBuffer[bufferIndex++] = ch.charValue();
                    drawPrompt();
                    refresh();
                }
            }
            
        } catch (IOException e) {
            System.err.println("DungeonClient: terminal failure");
            System.exit(3);
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
        
        String s = String.valueOf(localBuffer, 0, bufferIndex);
        putString(0, rows - 1, "> ", TextColor.ANSI.BLUE);
        
        // color the first token red if it is not actually a valid action
        TextColor.ANSI c = TextColor.ANSI.DEFAULT;
        
        String[] tokens = s.split(" ");
        if (tokens.length == 0)
            return;
        
        String firstToken = tokens[0];
        
        if (!DungeonProtocol.Action.isValidKey(firstToken)) {
            c = TextColor.ANSI.RED;
        }
        
        putString(2, rows - 1, firstToken, c);
        putString(2 + firstToken.length() + 1, rows - 1, join(tokens, 1, " "));
    }
    
    
    private void clearLastLine() {
        putString(0, rows - 1, repeat(" ", columns));
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
        for (int i = rows - 2; i >= 0; i--) {
            if (!it.hasNext())
                break;
            
            String str = it.next();
         
            String[] specials = {
                    DungeonDispatcher.CHEVRONS,
                    DungeonDispatcher.ASTERISKS,
                    DungeonDispatcher.BANGS
            };

            if (str.indexOf(specials[0]) == 0) {
                putString(0, i, specials[0], TextColor.ANSI.MAGENTA);
                putString(specials[0].length(), i,
                        str.substring(specials[0].length()));
                
            } else if (str.indexOf(specials[1]) == 0) {
                putString(0, i, specials[1], TextColor.ANSI.YELLOW);
                putString(specials[1].length(), i,
                        str.substring(specials[1].length()));
                
            } else if (str.indexOf(specials[2]) == 0) {
                putString(0, i, specials[2], TextColor.ANSI.RED);
                putString(specials[2].length(), i,
                        str.substring(specials[2].length()));
                
            } else {
                putString(0, i, str);
            }
        }
    }
    
    
    private void clearMessageArea() {
        String z = repeat(" ", columns);
        
        for (int i = rows - 2; i >= 0; i--)
            putString(0, i, z);
    }

    
    /*
     * Does the same thing as screen.refresh(), but it moves the cursor to
     * the right edge of the prompt text after refreshing.
     */
    private void refresh() {
        TerminalPosition pos = new TerminalPosition(bufferIndex + 2, rows - 1);
        screen.setCursorPosition(pos);
        
        try {
            screen.refresh();
        } catch (IOException e) {
            System.err.println("failed refreshing terminal");
            System.exit(4);
        }
    }
    
    
    private void sendLocalBuffer() {
        // don't send empty strings to server
        if (bufferIndex == 0)
            return;
        
        String s = String.valueOf(localBuffer, 0, bufferIndex);
        toServer.println(s.trim());
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
    
    
    /*
     * Use the current value of historyIndex to retrieve the specified
     * line from the command history into the local buffer.
     */
    private void loadLineFromHistory() {
        if (historyIndex < 0)
            return;
        
        String hist = localLines.get(historyIndex);
        for (bufferIndex = 0; bufferIndex < hist.length(); bufferIndex++)
            localBuffer[bufferIndex] = hist.charAt(bufferIndex);
    }
    
    
    private void pruneHistory() {
        if (this.localLines.size() > HISTORY_LIMIT) {
            for (int i = 0; i < this.localLines.size() - HISTORY_LIMIT; i++)
                this.localLines.removeLast();
        }
    }
    
    
    private String repeat(String s, int times) {
        String z = "";
        for (int i = 0; i < times; i++)
            z += s;
        return z;
    }
    
    
    private String join(String[] tokens, int start, String with) {
        if (tokens.length == 0)
            return "";
        if (start >= tokens.length)
            return "";
        
        String s = "";
        int i;
        for (i = start; i < tokens.length - 1; i++)
            s += tokens[i] + with;
        
        s += tokens[i];
        return s;
    }
}
