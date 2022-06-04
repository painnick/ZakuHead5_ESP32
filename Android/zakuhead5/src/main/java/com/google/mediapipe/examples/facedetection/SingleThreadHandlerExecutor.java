package com.google.mediapipe.examples.facedetection;

import android.os.Handler;
import android.os.HandlerThread;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

public class SingleThreadHandlerExecutor implements Executor {
    private final HandlerThread handlerThread;
    private final Handler handler;

    SingleThreadHandlerExecutor(String threadName, int priority) {
        handlerThread = new HandlerThread(threadName, priority);
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    @Override
    public void execute(Runnable command) {
        if (!handler.post(command)) {
            throw new RejectedExecutionException(handlerThread.getName() + " is shutting down.");
        }
    }

    boolean shutdown() {
        return handlerThread.quitSafely();
    }
}
