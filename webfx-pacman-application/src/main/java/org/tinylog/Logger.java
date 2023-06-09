package org.tinylog;

/**
 * Mock class for missing TinyLog support.
 */
public class Logger {

    public static void info(String msg, Object... args) {};
    public static void trace(String msg, Object... args) {};
    public static void error(String msg, Object... args) {};
}
