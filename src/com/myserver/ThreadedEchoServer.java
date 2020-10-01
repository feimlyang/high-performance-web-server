package com.myserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ThreadedEchoServer {

    //handle single client
    class ThreadedEchoHandler implements Runnable {
        private Socket incoming;

        public ThreadedEchoHandler(Socket incomingSocket) {
            incoming = incomingSocket;
        }

        @Override
        public void run() {
            try (InputStream inStream = incoming.getInputStream();
                 OutputStream outStream = incoming.getOutputStream()) {
                //read from client
                Scanner in = new Scanner(inStream, "UTF-8");

                //send response
                PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(outStream, "UTF-8"),
                        true);
                out.println("HI, Enter BYE to exit");

                //echo the message
                boolean done = false;
                while (!done && in.hasNextLine()){
                    String line = in.nextLine();
                    out.println("Echo: " + line);
                    if (line.trim().equals("BYE")){
                        done = true;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    //make multi threads
    public void runThreadedEchoHandler() {
        try (ServerSocket s = new ServerSocket(10050)) {
            int i = 1;
            while (true) {
                Socket incoming = s.accept();
                //create new thread
                Runnable r = new ThreadedEchoHandler(incoming);
                Thread t = new Thread(r);
                t.start();
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
