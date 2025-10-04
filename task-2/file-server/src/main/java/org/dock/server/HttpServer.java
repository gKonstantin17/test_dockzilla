package org.dock.server;

import com.sun.net.httpserver.HttpContext;
import org.dock.handler.*;
import org.dock.service.CleanupService;
import org.dock.service.FileService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class HttpServer {
    private final int port;
    private com.sun.net.httpserver.HttpServer server;
    private final FileService fileService;
    private final CleanupService cleanupService;

    public HttpServer(int port) {
        this.port = port;
        this.fileService = new FileService();
        this.cleanupService = new CleanupService(fileService);
    }

    public void start() {
        try {
            server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(port), 0);

            // API endpoints
            server.createContext("/api/upload", new UploadHandler(fileService));
            server.createContext("/api/download", new DownloadHandler(fileService));
            server.createContext("/api/stats", new StatsHandler(fileService));

            // Static files
            server.createContext("/", new StaticFileHandler());

            server.setExecutor(Executors.newFixedThreadPool(10));
            server.start();

            // Start cleanup service
            cleanupService.start();

            System.out.println("Server started on http://localhost:" + port);
        } catch (IOException e) {
            throw new RuntimeException("Failed to start server", e);
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            cleanupService.stop();
        }
    }
}
