package org.dock.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.dock.service.FileService;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class UploadHandler implements HttpHandler {
    private final FileService fileService;
    private final Gson gson;

    public UploadHandler(FileService fileService) {
        this.fileService = fileService;
        this.gson = new Gson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "Method not allowed");
            return;
        }

        try {
            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");

            if (contentType != null && contentType.startsWith("multipart/form-data")) {
                handleMultipartUpload(exchange, contentType);
            } else {
                sendResponse(exchange, 400, "Invalid content type");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Upload failed: " + e.getMessage());
        }
    }

    private void handleMultipartUpload(HttpExchange exchange, String contentType) throws IOException {
        String boundary = extractBoundary(contentType);
        if (boundary == null) {
            sendResponse(exchange, 400, "Invalid multipart request");
            return;
        }

        InputStream inputStream = exchange.getRequestBody();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int bytesRead;
        while ((bytesRead = inputStream.read(data)) != -1) {
            buffer.write(data, 0, bytesRead);
        }

        byte[] body = buffer.toByteArray();
        MultipartData multipartData = parseMultipart(body, boundary);

        if (multipartData == null || multipartData.fileContent == null) {
            sendResponse(exchange, 400, "No file uploaded");
            return;
        }

        String fileId = fileService.saveFile(
                multipartData.filename,
                multipartData.fileContent,
                multipartData.contentType
        );

        Map<String, String> response = new HashMap<>();
        response.put("fileId", fileId);
        response.put("downloadUrl", "/api/download/" + fileId);

        String jsonResponse = gson.toJson(response);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        sendResponse(exchange, 200, jsonResponse);
    }

    private String extractBoundary(String contentType) {
        String[] parts = contentType.split(";");
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith("boundary=")) {
                return part.substring("boundary=".length());
            }
        }
        return null;
    }

    private MultipartData parseMultipart(byte[] body, String boundary) {
        String boundaryString = "--" + boundary;
        String bodyString = new String(body, StandardCharsets.UTF_8);

        String[] parts = bodyString.split(boundaryString);

        for (String part : parts) {
            if (part.contains("Content-Disposition: form-data") && part.contains("filename=")) {
                return parseFilePart(part.getBytes(StandardCharsets.UTF_8));
            }
        }
        return null;
    }

    private MultipartData parseFilePart(byte[] partBytes) {
        try {
            String partString = new String(partBytes, StandardCharsets.UTF_8);
            String[] lines = partString.split("\r\n");

            String filename = null;
            String contentType = "application/octet-stream";
            int contentStart = 0;

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];

                if (line.contains("filename=")) {
                    int start = line.indexOf("filename=\"") + 10;
                    int end = line.indexOf("\"", start);
                    if (end > start) {
                        filename = line.substring(start, end);
                    }
                }

                if (line.startsWith("Content-Type:")) {
                    contentType = line.substring("Content-Type:".length()).trim();
                }

                if (line.isEmpty() && i > 0) {
                    contentStart = i + 1;
                    break;
                }
            }

            if (filename == null) {
                return null;
            }

            // Find content bytes
            String contentMarker = "\r\n\r\n";
            String partStr = new String(partBytes, StandardCharsets.ISO_8859_1);
            int contentIndex = partStr.indexOf(contentMarker);
            if (contentIndex == -1) {
                return null;
            }

            contentIndex += contentMarker.length();
            int endIndex = partStr.lastIndexOf("\r\n");
            if (endIndex == -1) {
                endIndex = partBytes.length;
            }

            byte[] fileContent = new byte[endIndex - contentIndex];
            System.arraycopy(partBytes, contentIndex, fileContent, 0, fileContent.length);

            MultipartData data = new MultipartData();
            data.filename = filename;
            data.contentType = contentType;
            data.fileContent = fileContent;

            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static class MultipartData {
        String filename;
        String contentType;
        byte[] fileContent;
    }
}
