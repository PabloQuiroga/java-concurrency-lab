package com.pablo.concurrency.core;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrderExecutor {
    // Pool de hilos dedicado exclusivamente a la ejecución de órdenes
    // para no interferir con el motor de decisiones
    private final ExecutorService executionPool = Executors.newFixedThreadPool(4);

    public void execute(PriceUpdate update) {
        // Lanzamos la ejecución de forma asíncrona
        CompletableFuture.runAsync(() -> {
            // Simulamos que la ejecución de una orden es lenta (ej: llamada a API)
            try {
                // Simulamos latencia de red de 100ms a 500ms
                Thread.sleep((long) (Math.random() * 400 + 100));
                System.out.printf("[ORDER EXECUTED] Asset: %s | Price: %.2f | Thread: %s\n",
                        update.asset(), update.price(), Thread.currentThread().getName());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, executionPool).exceptionally(ex -> {
            System.err.println("Critical error executing order: " + ex.getMessage());
            return null;
        });
    }

    public void shutdown() {
        executionPool.shutdown();
    }
}