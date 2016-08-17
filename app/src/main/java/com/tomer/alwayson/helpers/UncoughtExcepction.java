package com.tomer.alwayson.helpers;

public class UncoughtExcepction implements Thread.UncaughtExceptionHandler {
    public void uncaughtException(Thread t, Throwable e) {
        System.err.println("Uncaught exception by " + t + " caught:");
        e.printStackTrace();
    }
}
