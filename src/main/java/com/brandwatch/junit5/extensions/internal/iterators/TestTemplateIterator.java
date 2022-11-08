package com.brandwatch.junit5.extensions.internal.iterators;

import org.junit.jupiter.api.extension.TestTemplateInvocationContext;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link TestTemplateInvocationContext} iterator that can be consumed in via Streams.
 * It keeps stream active until end is reached.
 *
 * @param <T> type of {@link TestTemplateInvocationContext}
 */
public class TestTemplateIterator<T extends TestTemplateInvocationContext> implements Iterator<T>, AutoCloseable {

    final BlockingQueue<T> queue = new LinkedBlockingQueue<>();
    final ExecutorService executor = Executors.newSingleThreadExecutor();

    final AtomicBoolean closed = new AtomicBoolean(false);
    final AtomicInteger endCount = new AtomicInteger(0);

    private int endCountGoal = 1;

    private T next = null;

    private static final TestTemplateInvocationContext END = new TestTemplateInvocationContext() {};

    /**
     * Adds a new invocation context to the iterator.
     * This method is thread safe and can be used by multiple threads.
     *
     * @param value {@link TestTemplateInvocationContext} to add
     */
    public void add(T value) {
        if (closed.get()) {
            throw new UnsupportedOperationException("Iterator closed");
        }
        queue.add(value);
    }

    /**
     * Sets the end count goal for this iterator.
     * End goal marks how many times {@link #close()} needs to be called to close the iterator.
     *
     * @param endCountGoal expected number of close calls
     */
    public void setEndCountGoal(int endCountGoal) {
        this.endCountGoal = endCountGoal;
    }

    /**
     * A blocking next context availability check.
     * Returns true if previous (not consumed yet) or next context is set.
     * <p>
     *     It waits (blocking the thread) if next context is not available yet.
     * </p>
     *
     * @return true if previous or next element is set, false if iterator is closed
     */
    private boolean hasNextWait() {
        if (next != null) {
            return next != END;
        }
        try {
            next = queue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return next != END;
    }

    @Override
    public boolean hasNext() {
        if (executor.isShutdown() || executor.isTerminated()) {
            return false;
        }
        Future<Boolean> cf = CompletableFuture.supplyAsync(this::hasNextWait, executor);
        boolean result = false;
        try {
            result = cf.get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
        }
        if (!result) {
            executor.shutdownNow(); // At this point nothing should wait on pool
            queue.clear();
        }
        return result;
    }

    @Override
    public T next() {
        if (hasNext()) {
            T take = next;
            next = null;
            return take;
        }
        throw new NoSuchElementException();
    }

    /**
     * Mark iterator for closing if {@link #endCountGoal} is reached.
     * Existing contexts will be consumed before it's completely closed.
     */
    @SuppressWarnings("unchecked")
    public void tryClose() {
        if (endCount.incrementAndGet() == endCountGoal) {
            closed.set(true);
            queue.add((T) END);
        }
    }

    /**
     * Force iterator close/shutdown.
     *
     * {@inheritDoc}
     */
    @Override
    public void close() {
        closed.set(true);
        executor.shutdownNow(); // At this point nothing should wait on pool
        queue.clear();
    }
}
