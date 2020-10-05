package com.myserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ThreadedServer {

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
                byte[] data = new byte[1024];
                int bytesRead = inStream.read(data);
                //send response
                PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(outStream, "UTF-8"),
                        true);
                //echo the HTTP response:
                //HEADER
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: text/plain");
                out.println();
                //BODY
                out.println("echo back " + bytesRead + " bytes\n content: " + new String(data));
                inStream.close();
                outStream.close();
                incoming.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //make multi threads
    public void start() {
        try (ServerSocket s = new ServerSocket(10050)) {
            System.out.println("--- Basic Threaded Server is running---");
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

    public static void main(String[] args) {
        ThreadedServer threadedServer = new ThreadedServer();
        threadedServer.start();
    }
}
