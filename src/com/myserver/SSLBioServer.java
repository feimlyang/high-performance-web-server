package com.myserver;

import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.security.KeyStoreException;

public class SSLBioServer {
    private static String KEYSTORE = "/Users/manlin/GitHub/high-performance-server/mykey.keystore.new";
    private static String KEYSTORE_PASSWORD = "mypassword";
    private SSLContext sslContext;

    public SSLBioServer() throws Exception {
        System.setProperty("javax.net.ssl.trustStore", KEYSTORE);
        sslContext = SSLContext.getInstance("TLSv1.2");
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(KEYSTORE), KEYSTORE_PASSWORD.toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, KEYSTORE_PASSWORD.toCharArray());
        sslContext.init(kmf.getKeyManagers(), null, null);
    }

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
                HttpParser httpParser = new HttpParser();
                System.out.println(bytesRead);
                byte[] request = new byte[bytesRead];
                for (int i = 0; i < bytesRead; ++i) {
                    request[i] = data[i];
                }

                httpParser.parse(new String(request));

                //send response
                PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(outStream, "UTF-8"),
                        true);
                //echo the HTTP response:
                out.println(
                        httpParser.protocol + " 200 OK\r\n" +
                                "Content-Type: text/html\r\n\r\n" +
                                "<html>\n" +
                                "<body>\n" +
                                "<p> 看到的都是缘分 </p>" +
                                "<p>" + httpParser.body + "</p>\n" +
                                "</body>\n" +
                                "</html>"
                );
                inStream.close();
                outStream.close();
                incoming.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //make multi threads
    public void start() throws IOException {

        ServerSocketFactory factory = sslContext.getServerSocketFactory();
        ServerSocket serverSocket = factory.createServerSocket(10050);
        ((SSLServerSocket) serverSocket).setNeedClientAuth(false);
        System.out.println("--- Basic Threaded Server is running---");
        int i = 1;
        while (true) {
            Socket incoming = serverSocket.accept();
            //create new thread
            Runnable r = new SSLBioServer.ThreadedEchoHandler(incoming);
            Thread t = new Thread(r);
            t.start();
            i++;
        }
    }

    public static void main(String[] args) {
        try{
            SSLBioServer sslBioServer = new SSLBioServer();
            sslBioServer.start();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
}
