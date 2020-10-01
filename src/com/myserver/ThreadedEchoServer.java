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
            try (InputStream inStream = incoming.getInputStream();
                 OutputStream outStream = incoming.getOutputStream()) {
                //read from client
                byte[] data = new byte[1024];
                int bytesRead = inStream.read(data);
                System.out.println(new String(data));

                //send response
                PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(outStream, "UTF-8"),
                        true);
                //echo the message
                out.println("http test");
                out.println("Echo: " + bytesRead + " bytes\n content: " + new String(data));

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
