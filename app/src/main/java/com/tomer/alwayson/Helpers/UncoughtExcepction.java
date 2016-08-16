package com.tomer.alwayson.Helpers;

public class UncoughtExcepction implements Thread.UncaughtExceptionHandler {
    public void uncaughtException(Thread t, Throwable e) {
        System.err.println("Uncaught exception by " + t + " caught:");
        e.printStackTrace();
    }
}
