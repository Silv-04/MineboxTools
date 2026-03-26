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
     * Returns the logger instance associated with the provided class.
     *
     * @param type class used as logger category
     * @return SLF4J logger for the class
     */
    public static Logger getLogger(Class<?> type) {
        return LoggerFactory.getLogger(type);
    }

    /**
     * Logs a warning only once for a unique key.
     *
     * @param logger target logger
     * @param key deduplication key
     * @param message warning message template
     * @param args message arguments
     */
    public static void warnOnce(Logger logger, String key, String message, Object... args) {
        if (ONCE_LOGS.putIfAbsent(key, Boolean.TRUE) == null) {
            logger.warn(message, args);
        }
    }

    /**
        * Logs an error only once for a unique key.
        *
        * @param logger target logger
        * @param key deduplication key
        * @param message error message template
        * @param args message arguments
     */
    public static void errorOnce(Logger logger, String key, String message, Object... args) {
        if (ONCE_LOGS.putIfAbsent(key, Boolean.TRUE) == null) {
            logger.error(message, args);
        }
    }

    /**
        * Logs a warning with time-based throttling.
        *
        * @param logger target logger
        * @param key throttle bucket key
        * @param throttleMs minimum interval between logs for the key
        * @param message warning message template
        * @param args message arguments
     */
    public static void warnThrottled(Logger logger, String key, long throttleMs, String message, Object... args) {
        logThrottled(logger, key, throttleMs, LogLevel.WARN, message, args);
    }

    /**
        * Logs an error with time-based throttling.
        *
        * @param logger target logger
        * @param key throttle bucket key
        * @param throttleMs minimum interval between logs for the key
        * @param message error message template
        * @param args message arguments
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
