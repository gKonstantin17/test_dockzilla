package org.dock.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadata {
    private Long id;
    private String uniqueId;
    private String originalName;
    private String storedName;
    private Long size;
    private String contentType;
    private Long userId;
    private Long uploadedAt;
    private Long lastDownloadedAt;
    private Integer downloadCount;
}
