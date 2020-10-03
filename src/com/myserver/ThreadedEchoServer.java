package com.myserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ThreadedEchoServer {

    //handle single client
    class ThreadedEchoHandler implements Runnable {
        private Socket incoming;

        public ThreadedEchoHandler(Socket incomingSocket) {
            incoming = incomingSocket;
        }

        @Override
        public void run() {
            EchoService echoService = new EchoService();
            echoService.Echo(incoming);
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
