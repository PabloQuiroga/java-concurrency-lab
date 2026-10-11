package com.pablo.concurrency.core;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

public class PriceFeed implements Runnable {
    private final String asset;
    private final EventQueue eventQueue;
    private volatile boolean running = true;

    public PriceFeed(String asset, EventQueue eventQueue) {
        this.asset = asset;
        this.eventQueue = eventQueue;
    }

    public void stop() {
        this.running = false;
    }

    @Override
    public void run() {
        while (running) {
            double price = 100 + (1000 - 100) * ThreadLocalRandom.current().nextDouble();
            PriceUpdate update = new PriceUpdate(asset, price, Instant.now());

            // ENVIAR A LA COLA en lugar de llamar al store directamente
            eventQueue.enqueue(update);

            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(500, 2000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}