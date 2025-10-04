package org.dock.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.dock.entity.FileMetadata;
import org.dock.service.FileService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class DownloadHandler implements HttpHandler {
    private final FileService fileService;

    public DownloadHandler(FileService fileService) {
        this.fileService = fileService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Method not allowed");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String fileId = path.substring("/api/download/".length());

        if (fileId.isEmpty()) {
            sendError(exchange, 400, "File ID required");
            return;
        }

        FileMetadata metadata = fileService.getFileMetadata(fileId);
        if (metadata == null) {
            sendError(exchange, 404, "File not found");
            return;
        }

        byte[] fileContent = fileService.getFileContent(fileId);
        if (fileContent == null) {
            sendError(exchange, 404, "File not found");
            return;
        }

        exchange.getResponseHeaders().set("Content-Type", metadata.getContentType());
        exchange.getResponseHeaders().set(
                "Content-Disposition",
                "attachment; filename=\"" + encodeFilename(metadata.getOriginalFilename()) + "\""
        );

        exchange.sendResponseHeaders(200, fileContent.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(fileContent);
        }
    }

    private String encodeFilename(String filename) {
        try {
            return URLEncoder.encode(filename, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");
        } catch (Exception e) {
            return filename;
        }
    }

    private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
