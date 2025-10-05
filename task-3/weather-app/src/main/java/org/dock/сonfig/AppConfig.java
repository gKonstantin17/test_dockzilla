package org.dock.сonfig;

import lombok.Data;

@Data
public class AppConfig {
    private static final int SERVER_PORT = 8080;
    private static final int CACHE_TTL_MINUTES = 15;

    public static int getServerPort() {
        return SERVER_PORT;
    }

    public static int getCacheTtlMinutes() {
        return CACHE_TTL_MINUTES;
    }
}
