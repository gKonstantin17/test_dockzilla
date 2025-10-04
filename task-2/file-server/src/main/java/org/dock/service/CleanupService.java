package org.dock.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CleanupService {
    private static final int DAYS_INACTIVE = 30;
    private static final int CHECK_INTERVAL_HOURS = 24;

    private final FileService fileService;
    private final ScheduledExecutorService scheduler;

    public CleanupService(FileService fileService) {
        this.fileService = fileService;
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public void start() {
        scheduler.scheduleAtFixedRate(
                () -> {
                    try {
                        System.out.println("Running cleanup task...");
                        fileService.deleteOldFiles(DAYS_INACTIVE);
                    } catch (Exception e) {
                        System.err.println("Cleanup task failed: " + e.getMessage());
                    }
                },
                0,
                CHECK_INTERVAL_HOURS,
                TimeUnit.HOURS
        );
    }

    public void stop() {
        scheduler.shutdown();
    }
}
