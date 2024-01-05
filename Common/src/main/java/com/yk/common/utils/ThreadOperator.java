package com.yk.common.utils;

import androidx.annotation.NonNull;

import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.SneakyThrows;

public class ThreadOperator {

    private final Queue<Runnable> persistenceOperationQueue = new LinkedBlockingQueue<>();
    private final ExecutorService executor;
    private boolean isRunning = true;
    private Thread threadWithRunningTask = null;

    @NonNull
    public static ThreadOperator getInstance(boolean isConcurrent) {
        return new ThreadOperator(isConcurrent);
    }

    public void addToQueue(Runnable runnable) {
        persistenceOperationQueue.add(runnable);
        synchronized (this) {
            this.notifyAll();
        }
    }

    public void stop() {
        this.isRunning = false;
        if (threadWithRunningTask != null && threadWithRunningTask.isAlive()) {
            threadWithRunningTask.interrupt();
        }
    }

    public <T, Y, E extends Exception> T executeSingle(Function<Y, T> operator, Y parameter, Thrower<E> thrower) throws E {
        return perform(() -> operator.apply(parameter), thrower);
    }

    public <T, E extends Exception> T executeSingle(Supplier<T> operator, Thrower<E> thrower) throws E {
        return perform(operator, thrower);
    }

    private ThreadOperator(boolean isConcurrent) {
        if (isConcurrent) {
            executor = null;
        } else {
            executor = Executors.newFixedThreadPool(20);
        }
        Thread operator = new Thread(() -> {
            while (isRunning) {
                Runnable taskToExecute = persistenceOperationQueue.poll();
                if (taskToExecute == null)
                    waitForThread();
                else {
                    if (executor != null) {
                        executor.submit(taskToExecute);
                    } else {
                        taskToExecute.run();
                    }
                }
            }
        });
        operator.setDaemon(true);
        operator.start();
    }

    private <T, E extends Exception> T perform(Supplier<T> singleRunOperator, Thrower<E> thrower) throws E {
        AtomicReference<T> objectAtomicReference = new AtomicReference<>();

        threadWithRunningTask = new Thread(() -> objectAtomicReference.set(singleRunOperator.get()));
        threadWithRunningTask.start();

        try {
            joinThread(threadWithRunningTask);
        } catch (InterruptedException interruptedException) {
            thrower.throwException();
        }
        return objectAtomicReference.get();
    }

    @SneakyThrows
    private void waitForThread() {
        synchronized (this) {
            this.wait();
        }
    }

    private static void joinThread(@NonNull Thread threadToJoin) throws InterruptedException {
        threadToJoin.join();
    }

    @FunctionalInterface
    public interface Thrower<E extends Exception> {
        void throwException() throws E;
    }

}
