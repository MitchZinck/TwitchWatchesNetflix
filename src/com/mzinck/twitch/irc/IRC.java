package com.mzinck.twitch.irc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mzinck.twitch.Netflix;
import com.mzinck.twitch.State;

public class IRC implements Runnable {

    private final int                 TWITCH_PORT = 6667;
    private final String              SERVER      = "irc.twitch.tv";
    private final String              NICK        = "mitchzinck";
    private final String              OAUTH       = "uk0ysbaynvon911vfd0oxy6r6o6tl2";
    private final String[]            modList     = new String[] { "mitchzinck" };

    private String[]                  split;

    public static Map<String, String> map;

    private Socket                    socket;
    private InputStream               is;
    private OutputStream              os;
    private InputStreamReader         inputStreamReader;
    private OutputStreamWriter        outputStreamWriter;
    private BufferedReader            read;
    private BufferedWriter            writer;

    public IRC() {
  
    }

    public void writeLine(String msg) {
        try {
            writer.write(msg + "\r\n");
            writer.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void recieveComm() {
        while (true) {

        }
    }

    public String getText() {
        return "";
    }

    @Override
    public void run() {
        try {
            map = new ConcurrentHashMap<String, String>();

            System.out.print("Connecting to twitch servers.");
            socket = new Socket(SERVER, TWITCH_PORT);
            is = socket.getInputStream();
            os = socket.getOutputStream();

            inputStreamReader = new InputStreamReader(is, "UTF-8");
            outputStreamWriter = new OutputStreamWriter(os, "UTF-8");

            read = new BufferedReader(inputStreamReader);
            writer = new BufferedWriter(outputStreamWriter);

            writeLine("PASS oauth:" + OAUTH);
            writeLine("NICK " + NICK);
            writeLine("JOIN #mitchzinck");
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        boolean connected = false;
        while (true) {
            String line = null;
            try {
                Thread.sleep(100);
                line = read.readLine();
                if (line.contains("366")) {
                    connected = true;
                    System.out.println("");
                    continue;
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (line != null && connected == true && line.contains("!vote")) {
                line = line.replace("!vote ", "");
                split = line.split(":");
                map.put(split[1].split("!")[0], split[split.length - 1]);
            } else if(connected == true && line != null && (line.contains("!setTime") || line.contains("!newVote"))) {
                for(String s : modList) {
                    if(line.contains(s)) {
                        if(line.contains("!setTime")) {
                            Netflix.setTime(Integer.parseInt(line.substring(line.indexOf("{") + 1, line.indexOf("}"))));
                        } else {
                            Netflix.state = State.VOTING;
                            Netflix.countdown = 2;
                        }
                    }
                }
            } else {
                System.out.print(".");
            }
        }
    }

}
