package org.dock;

import org.dock.server.HttpServer;

public class Main {
    public static void main(String[] args) {
        int port = 8080;
        HttpServer server = new HttpServer(port);

        System.out.println("Starting File Sharing Service on port " + port);
        server.start();
    }
}
