package com.myserver;

import java.io.*;
import java.net.Socket;

public class EchoService {

    public void Echo(Socket incoming){
        try (InputStream inStream = incoming.getInputStream();
             OutputStream outStream = incoming.getOutputStream()) {
            //read from client
            byte[] data = new byte[1024];
            int bytesRead = inStream.read(data);
//            System.out.println(new String(data));

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
