package com.myserver;

public class Main {

    public static void main(String[] args) {
        //run basic multi threaded Server
//        ThreadedEchoServer threadedServer = new ThreadedEchoServer();
//        System.out.println("--- Basic Threaded Server is running---");
//        threadedServer.runThreadedEchoHandler();

        //run thread pool server
        ThreadPoolEchoServer threadPoolServer = new ThreadPoolEchoServer();
        System.out.println("--- Thread Pool Server is running---");
        threadPoolServer.runThreadPoolServer();
    }
}
