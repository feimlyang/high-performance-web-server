package com.myserver;

public class Main {

    public static void main(String[] args) {
        //test Threaded Echo Server
        ThreadedEchoServer server = new ThreadedEchoServer();
        server.runThreadedEchoHandler();
    }
}
