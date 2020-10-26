package com.myserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
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
        ByteBuffer byteBuffer = ByteBuffer.allocate(10);

        //read data from channel
        HttpParser httpParser = new HttpParser();
        boolean isParseEnd = false;
        while (socketChannel.read(byteBuffer) > 0 && !isParseEnd){
            byteBuffer.flip();
            String request = "";
            request += Charset.forName("UTF-8").decode(byteBuffer);
            try{
                isParseEnd = httpParser.parse(request);
            }
            catch (Exception httpParseException){
                socketChannel.close();
                httpParseException.printStackTrace();
                return;
            }
            byteBuffer.clear();
        }

        String response = httpParser.protocol + " 200 OK\r\n" +
                "Content-Type: text/html\r\n\r\n" +
                "<html>\n" +
                "<body>\n" +
                "看到的都是缘分" +
                "<p>" + httpParser.body + "</p>\n" +
                "</body>\n" +
                "</html>";

        socketChannel.register(selector, SelectionKey.OP_WRITE);
        selectionKey.attach(ByteBuffer.wrap(response.getBytes()));
    }


    private void writeHandler(SelectionKey selectionKey, Selector selector) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        ByteBuffer byteBuffer = (ByteBuffer) selectionKey.attachment();
        if (!byteBuffer.hasRemaining()){
            byteBuffer.rewind();
        }
        socketChannel.write(byteBuffer);
        selectionKey.channel().close();

    }

    public static void main(String[] args) throws IOException {
        NioServer nioServer = new NioServer();
        nioServer.start();
    }
}
