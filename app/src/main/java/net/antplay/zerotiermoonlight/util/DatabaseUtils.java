package net.antplay.zerotiermoonlight.util;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import lombok.var;

/**
 * database access tools
 */
public class DatabaseUtils {
    public static final Lock readLock;
    public static final ReadWriteLock readWriteLock;
    public static final Lock writeLock;

    static {
        ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
        readWriteLock = reentrantReadWriteLock;
        writeLock = reentrantReadWriteLock.writeLock();
        readLock = reentrantReadWriteLock.readLock();
    }
}
