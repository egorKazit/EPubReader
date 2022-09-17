package com.yk.common.utils;

import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final public class ThreadOperator {

    private final static Queue<Runnable> PERSISTENCE_OPERATION_QUEUE = new LinkedBlockingQueue<>();
    private final static ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();
    private final static Thread PROCESSOR;

    static {
        PROCESSOR = new Thread(() -> {
            while (true) {
                Runnable taskToExecute = PERSISTENCE_OPERATION_QUEUE.poll();
                if (taskToExecute == null)
                    waitForThread();
                else
                    EXECUTOR_SERVICE.submit(taskToExecute);
            }
        });
        PROCESSOR.start();
    }

    public static void addToQueue(Runnable runnable) {
        PERSISTENCE_OPERATION_QUEUE.add(runnable);
        synchronized (ThreadOperator.class) {
            ThreadOperator.class.notify();
        }
    }

    public static <T, Y, E extends Exception> Object executeSingle(Function<Y, T> operator, Y parameter, Thrower<E> thrower) throws E {
        return perform(() -> operator.apply(parameter), thrower);
    }

    public static <T, E extends Exception> Object executeSingle(Supplier<T> operator, Thrower<E> thrower) throws E {
        return perform(operator, thrower);
    }

    private static <T, E extends Exception> T perform(Supplier<T> singleRunOperator, Thrower<E> thrower) throws E {
        AtomicReference<T> bookAtomicReference = new AtomicReference<>();

        Thread threadWithRunningTask = new Thread(() -> bookAtomicReference.set(singleRunOperator.get()));
        threadWithRunningTask.start();

        try {
            joinThread(threadWithRunningTask);
        } catch (InterruptedException interruptedException) {
            thrower.throwException();
        }
        return bookAtomicReference.get();
    }

    @SneakyThrows
    private static void waitForThread() {
        synchronized (ThreadOperator.class) {
            ThreadOperator.class.wait();
        }
    }

    private static void joinThread(Thread threadToJoin) throws InterruptedException {
        threadToJoin.join();
    }

    @FunctionalInterface
    public interface Thrower<E extends Exception> {
        void throwException() throws E;
    }

}
