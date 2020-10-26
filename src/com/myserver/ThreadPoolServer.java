package com.myserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolServer {

    class Handler implements Runnable {
        private Socket incoming;

        public Handler(Socket incomingSocket) {
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

    class NetworkService implements Runnable {
        private ServerSocket serverSocket;
        private ExecutorService pool;

        public NetworkService(int port, int poolSize) throws IOException {
            serverSocket = new ServerSocket(port, 10000);
            pool = Executors.newFixedThreadPool(poolSize);
        }

        @Override
        public void run() {
            try {
                int i = 0;
                while (true) {
                    pool.submit(new Handler(serverSocket.accept()));
                }
            } catch (IOException e) {
                pool.shutdown();
            }
        }
    }

    public void start() {
        try {
            Runnable networkService = new NetworkService(10050, Runtime.getRuntime().availableProcessors());
            Thread t = new Thread(networkService);
            t.start();
            System.out.println("--- Thread Pool Server is running---");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ThreadPoolServer threadPoolServer = new ThreadPoolServer();
        threadPoolServer.start();
    }
}
