package com.myserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

/*NIO Server*/
public class NioServer {

    public void start() throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress("0.0.0.0", 10050));
        //set channel to non blocking
        serverSocketChannel.configureBlocking(false);

        //register channel to the selector and start listening
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("NIO server starts");

        //selector checks status if any channel is ready
        while (true){
            int numOfReadyChannels = selector.select();
            if (numOfReadyChannels == 0) continue;

            //get ready channels
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator iterator = selectionKeys.iterator();
            while (iterator.hasNext()){
                SelectionKey selectionKey = (SelectionKey) iterator.next();
                //remove the selectionKey from ready to process
                iterator.remove();
                //accept event
                if (selectionKey.isAcceptable()){
                    acceptHandler(serverSocketChannel, selector);
                }
                //read event
                if (selectionKey.isReadable()){
                    readHandler(selectionKey, selector);
                }
                //write event
                if (selectionKey.isWritable()){
                    writeHandler(selectionKey, selector);
                }
            }
        }
    }

    private void acceptHandler(ServerSocketChannel serverSocketChannel, Selector selector) throws IOException {
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
    }

    private void readHandler(SelectionKey selectionKey, Selector selector) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        String request = "";
        //read data from channel
        while (socketChannel.read(byteBuffer) > 0){
            byteBuffer.flip();
            request += Charset.forName("UTF-8").decode(byteBuffer);

//            if (request.length() > 0){
//                System.out.println("request: " + request);
//            }
        }

        socketChannel.register(selector, SelectionKey.OP_WRITE);
        selectionKey.attach(ByteBuffer.wrap(request.getBytes(StandardCharsets.UTF_8)));

    }

    private void writeHandler(SelectionKey selectionKey, Selector selector) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        ByteBuffer byteBuffer = (ByteBuffer) selectionKey.attachment(); //request from browser
        if (!byteBuffer.hasRemaining()){
            byteBuffer.rewind();
        }
        //http response
        socketChannel.write(Charset.forName("UTF-8").encode("HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n{\"status\":\"ok\"}"));
        selectionKey.channel().close();
    }

    public static void main(String[] args) throws IOException {
        NioServer nioServer = new NioServer();
        nioServer.start();
    }
}
