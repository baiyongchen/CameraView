package com.otaliastudios.cameraview;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class that can log traces and info.
 */
public final class CameraLogger {

    public final static int LEVEL_VERBOSE = 0;
    public final static int LEVEL_INFO = 1;
    public final static int LEVEL_WARNING = 2;
    public final static int LEVEL_ERROR = 3;

    /**
     * Interface of integers representing log levels.
     * @see #LEVEL_VERBOSE
     * @see #LEVEL_INFO
     * @see #LEVEL_WARNING
     * @see #LEVEL_ERROR
     */
    @IntDef({LEVEL_VERBOSE, LEVEL_INFO, LEVEL_WARNING, LEVEL_ERROR})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LogLevel {}

    /**
     * A Logger can listen to internal log events
     * and log them to different providers.
     * The default logger will simply post to logcat.
     */
    public interface Logger {

        /**
         * Notifies that an internal log event was just triggered.
         *
         * @param level the log level
         * @param tag the log tag
         * @param message the log message
         * @param throwable an optional throwable
         */
        void log(@LogLevel int level, @NonNull String tag, @NonNull String message, @Nullable Throwable throwable);
    }

    static String lastMessage;
    static String lastTag;

    private static int sLevel;
    private static List<Logger> sLoggers;

    static {
        setLogLevel(LEVEL_ERROR);
        sLoggers = new ArrayList<>();
        sLoggers.add(new Logger() {
            @Override
            public void log(int level, @NonNull String tag, @NonNull String message, @Nullable Throwable throwable) {
                switch (level) {
                    case LEVEL_VERBOSE: Log.v(tag, message, throwable); break;
                    case LEVEL_INFO: Log.i(tag, message, throwable); break;
                    case LEVEL_WARNING: Log.w(tag, message, throwable); break;
                    case LEVEL_ERROR: Log.e(tag, message, throwable); break;
                }
            }
        });
    }

    static CameraLogger create(@NonNull String tag) {
        return new CameraLogger(tag);
    }

    /**
     * Sets the log sLevel for logcat events.
     *
     * @see #LEVEL_VERBOSE
     * @see #LEVEL_INFO
     * @see #LEVEL_WARNING
     * @see #LEVEL_ERROR
     * @param logLevel the desired log sLevel
     */
    public static void setLogLevel(@LogLevel int logLevel) {
        sLevel = logLevel;
    }

    /**
     * Registers an external {@link Logger} for log events.
     * Make sure to unregister using {@link #unregisterLogger(Logger)}.
     *
     * @param logger logger to add
     */
    @SuppressWarnings("WeakerAccess")
    public static void registerLogger(@NonNull Logger logger) {
        sLoggers.add(logger);
    }

    /**
     * Unregisters a previously registered {@link Logger} for log events.
     * This is needed in order to avoid leaks.
     *
     * @param logger logger to remove
     */
    @SuppressWarnings("WeakerAccess")
    public static void unregisterLogger(@NonNull Logger logger) {
        sLoggers.remove(logger);
    }

    @NonNull
    private String mTag;

    private CameraLogger(@NonNull String tag) {
        mTag = tag;
    }

    private boolean should(int messageLevel) {
        return sLevel <= messageLevel && sLoggers.size() > 0;
    }

    void v(@NonNull Object... data) {
        log(LEVEL_VERBOSE, data);
    }

    void i(@NonNull Object... data) {
        log(LEVEL_INFO, data);
    }

    void w(@NonNull Object... data) {
        log(LEVEL_WARNING, data);
    }

    void e(@NonNull Object... data) {
        log(LEVEL_ERROR, data);
    }

    private void log(@LogLevel int level, @NonNull Object... data) {
        if (!should(level)) return;

        StringBuilder message = new StringBuilder();
        Throwable throwable = null;
        for (Object object : data) {
            if (object instanceof Throwable) {
                throwable = (Throwable) object;
            }
            message.append(String.valueOf(object));
            message.append(" ");
        }
        for (Logger logger : sLoggers) {
            logger.log(level, mTag, message.toString().trim(), throwable);
        }

        lastMessage = message.toString();
        lastTag = mTag;
    }
}

