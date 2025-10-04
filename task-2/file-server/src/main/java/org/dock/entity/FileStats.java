package org.dock.entity;

import lombok.Data;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
public class FileStats {
    private int totalFiles;
    private long totalSize;
    private List<FileMetadata> files;
}
