package org.dock.entity;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileMetadata {
    private String id;
    private String originalFilename;
    private String storedFilename;
    private long size;
    private String contentType;
    private LocalDateTime uploadTime;
    private LocalDateTime lastAccessTime;
    private int downloadCount;
}
