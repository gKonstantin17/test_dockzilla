package org.dock.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.dock.entity.FileMetadata;
import org.dock.entity.FileStats;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FileService {
    private static final String UPLOAD_DIR = "files-upload";
    private static final String METADATA_FILE = "files-upload/metadata.json";
    private final Map<String, FileMetadata> fileMetadataMap;
    private final Gson gson;

    public FileService() {
        this.fileMetadataMap = new ConcurrentHashMap<>();
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .setPrettyPrinting()
                .create();

        createUploadDirectory();
        loadMetadata();
    }

    private void createUploadDirectory() {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create upload directory", e);
        }
    }

    public String saveFile(String filename, byte[] content, String contentType) {
        String fileId = UUID.randomUUID().toString();
        String storedFilename = fileId + "_" + filename;
        Path filePath = Paths.get(UPLOAD_DIR, storedFilename);

        try {
            Files.write(filePath, content);

            FileMetadata metadata = new FileMetadata(
                    fileId,
                    filename,
                    storedFilename,
                    content.length,
                    contentType,
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    0
            );

            fileMetadataMap.put(fileId, metadata);
            saveMetadata();

            return fileId;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file", e);
        }
    }

    public FileMetadata getFileMetadata(String fileId) {
        return fileMetadataMap.get(fileId);
    }

    public byte[] getFileContent(String fileId) {
        FileMetadata metadata = fileMetadataMap.get(fileId);
        if (metadata == null) {
            return null;
        }

        Path filePath = Paths.get(UPLOAD_DIR, metadata.getStoredFilename());
        try {
            // Update access time and download count
            metadata.setLastAccessTime(LocalDateTime.now());
            metadata.setDownloadCount(metadata.getDownloadCount() + 1);
            saveMetadata();

            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file", e);
        }
    }

    public FileStats getStats() {
        List<FileMetadata> files = new ArrayList<>(fileMetadataMap.values());
        files.sort((f1, f2) -> f2.getUploadTime().compareTo(f1.getUploadTime()));

        long totalSize = files.stream().mapToLong(FileMetadata::getSize).sum();

        return new FileStats(files.size(), totalSize, files);
    }

    public void deleteOldFiles(int daysInactive) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(daysInactive);
        List<String> toDelete = new ArrayList<>();

        for (Map.Entry<String, FileMetadata> entry : fileMetadataMap.entrySet()) {
            if (entry.getValue().getLastAccessTime().isBefore(threshold)) {
                toDelete.add(entry.getKey());
            }
        }

        for (String fileId : toDelete) {
            deleteFile(fileId);
        }

        if (!toDelete.isEmpty()) {
            System.out.println("Deleted " + toDelete.size() + " inactive files");
        }
    }

    private void deleteFile(String fileId) {
        FileMetadata metadata = fileMetadataMap.get(fileId);
        if (metadata != null) {
            Path filePath = Paths.get(UPLOAD_DIR, metadata.getStoredFilename());
            try {
                Files.deleteIfExists(filePath);
                fileMetadataMap.remove(fileId);
                saveMetadata();
            } catch (IOException e) {
                System.err.println("Failed to delete file: " + fileId);
            }
        }
    }

    private void saveMetadata() {
        try (Writer writer = new FileWriter(METADATA_FILE)) {
            gson.toJson(fileMetadataMap, writer);
        } catch (IOException e) {
            System.err.println("Failed to save metadata: " + e.getMessage());
        }
    }

    private void loadMetadata() {
        File metadataFile = new File(METADATA_FILE);
        if (!metadataFile.exists()) {
            return;
        }

        try (Reader reader = new FileReader(metadataFile)) {
            Type type = new TypeToken<ConcurrentHashMap<String, FileMetadata>>(){}.getType();
            Map<String, FileMetadata> loaded = gson.fromJson(reader, type);
            if (loaded != null) {
                fileMetadataMap.putAll(loaded);
            }
        } catch (IOException e) {
            System.err.println("Failed to load metadata: " + e.getMessage());
        }
    }
}
