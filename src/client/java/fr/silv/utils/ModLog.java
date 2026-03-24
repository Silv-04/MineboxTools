package fr.silv.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Logging helpers with once/throttled safeguards.
 */
public final class ModLog {
    private static final Map<String, Long> THROTTLED_LOGS = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> ONCE_LOGS = new ConcurrentHashMap<>();

    private ModLog() {
    }

    /**
     * Returns the logger.
     * @param type value for type
     * @return the logger
     */
    public static Logger getLogger(Class<?> type) {
        return LoggerFactory.getLogger(type);
    }

    /**
     * Executes the warn once operation.
     * @param logger value for logger
     * @param key value for key
     * @param message value for message
     * @param args value for args
     */
    public static void warnOnce(Logger logger, String key, String message, Object... args) {
        if (ONCE_LOGS.putIfAbsent(key, Boolean.TRUE) == null) {
            logger.warn(message, args);
        }
    }

    /**
     * Executes the error once operation.
     * @param logger value for logger
     * @param key value for key
     * @param message value for message
     * @param args value for args
     */
    public static void errorOnce(Logger logger, String key, String message, Object... args) {
        if (ONCE_LOGS.putIfAbsent(key, Boolean.TRUE) == null) {
            logger.error(message, args);
        }
    }

    /**
     * Executes the warn throttled operation.
     * @param logger value for logger
     * @param key value for key
     * @param throttleMs value for throttleMs
     * @param message value for message
     * @param args value for args
     */
    public static void warnThrottled(Logger logger, String key, long throttleMs, String message, Object... args) {
        logThrottled(logger, key, throttleMs, LogLevel.WARN, message, args);
    }

    /**
     * Executes the error throttled operation.
     * @param logger value for logger
     * @param key value for key
     * @param throttleMs value for throttleMs
     * @param message value for message
     * @param args value for args
     */
    public static void errorThrottled(Logger logger, String key, long throttleMs, String message, Object... args) {
        logThrottled(logger, key, throttleMs, LogLevel.ERROR, message, args);
    }

    private static void logThrottled(Logger logger, String key, long throttleMs, LogLevel level, String message, Object... args) {
        long now = System.currentTimeMillis();
        Long previous = THROTTLED_LOGS.get(key);
        if (previous != null && now - previous < throttleMs) {
            return;
        }

        THROTTLED_LOGS.put(key, now);
        if (level == LogLevel.WARN) {
            logger.warn(message, args);
        } else {
            logger.error(message, args);
        }
    }

    private enum LogLevel {
        WARN,
        ERROR
    }
}
