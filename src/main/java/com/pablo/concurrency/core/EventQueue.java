package com.pablo.concurrency.core;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class EventQueue {
    // Cola bloqueante: si está vacía, el consumidor espera.
    // Si está llena, el productor espera.
    private final BlockingQueue<PriceUpdate> queue;

    public EventQueue(int capacity) {
        this.queue = new LinkedBlockingQueue<>(capacity);
    }

    public void enqueue(PriceUpdate update) {
        try {
            // put() bloquea el hilo si la cola está llena (Backpressure)
            queue.put(update);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public PriceUpdate dequeue() throws InterruptedException {
        // take() bloquea el hilo hasta que haya un elemento disponible
        return queue.take();
    }

    public int size() {
        return queue.size();
    }
}