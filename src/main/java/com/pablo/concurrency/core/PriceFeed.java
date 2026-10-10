package com.pablo.concurrency.core;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class PriceFeed implements Runnable {
    private final String asset;
    private final Consumer<PriceUpdate> onUpdate;
    private volatile boolean running = true;

    public PriceFeed(String asset, Consumer<PriceUpdate> onUpdate) {
        this.asset = asset;
        this.onUpdate = onUpdate;
    }

    public void stop() {
        this.running = false;
    }

    @Override
    public void run() {
        while (running) {
            // Simulamos un precio aleatorio entre 100 y 1000
            double price = 100 + (1000 - 100) * ThreadLocalRandom.current().nextDouble();
            PriceUpdate update = new PriceUpdate(asset, price, Instant.now());

            onUpdate.accept(update);

            try {
                // Simulamos que la actualización llega cada 500ms - 2s
                Thread.sleep(ThreadLocalRandom.current().nextInt(500, 2000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}