package com.myserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPoolEchoServer {

    class Handler implements Runnable {
        private Socket incoming;

        public Handler(Socket incomingSocket) {
            incoming = incomingSocket;
        }

        @Override
        public void run() {
            EchoService echoService = new EchoService();
            echoService.Echo(incoming);
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

    public void runThreadPoolServer() {
        try {
            Runnable networkService = new NetworkService(10050, Runtime.getRuntime().availableProcessors());
            Thread t = new Thread(networkService);
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
