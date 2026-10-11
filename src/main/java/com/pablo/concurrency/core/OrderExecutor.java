package com.pablo.concurrency.core;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class OrderExecutor {
    private final ExecutorService executionPool = Executors.newFixedThreadPool(4);
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private static final int FAILURE_THRESHOLD = 5; // Límite de errores antes del Circuit Breaker
    private volatile boolean circuitOpen = false;

    public void execute(PriceUpdate update) {
        if (circuitOpen) {
            System.err.println("[CIRCUIT BREAKER] Order blocked for " + update.asset() + " - System in recovery mode.");
            return;
        }

        // Capturamos el momento exacto del envío para medir la latencia total
        Instant startTime = Instant.now();

        CompletableFuture.runAsync(() -> {
            try {
                // Simulamos la latencia de ejecución (red/API)
                Thread.sleep((long) (Math.random() * 400 + 100));

                // Simulamos un fallo aleatorio el 10% de las veces para probar la resiliencia
                if (Math.random() < 0.1) {
                    throw new RuntimeException("Network Timeout");
                }

                // CÁLCULO DE LATENCIA: Tiempo actual menos tiempo de creación del evento
                // Esto mide el tiempo total desde que el precio nació hasta que la orden se ejecutó
                // (Incluyendo el tiempo que pasó en la cola)
                // Nota: Para simplificar, calculamos desde el inicio de la ejecución de la orden
                long latency = Duration.between(startTime, Instant.now()).toMillis();

                System.out.printf("[ORDER EXECUTED] Asset: %s | Price: %.2f | Latency: %dms | Thread: %s\n",
                        update.asset(), update.price(), latency, Thread.currentThread().getName());

                // Si la orden tuvo éxito, reseteamos el contador de fallos
                failureCount.set(0);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, executionPool).exceptionally(ex -> {
            // Manejo de errores y activación del Circuit Breaker
            int currentFailures = failureCount.incrementAndGet();
            System.err.println("[ORDER ERROR] " + ex.getMessage() + " | Total Failures: " + currentFailures);

            if (currentFailures >= FAILURE_THRESHOLD) {
                System.err.println("!!! CIRCUIT BREAKER OPENED: System entering recovery mode !!!");
                circuitOpen = true;
                // Programamos la recuperación automática después de 10 segundos
                new Thread(() -> {
                    try {
                        Thread.sleep(10000);
                        // println para dejar rastro en la consola
                        System.out.println(">>> CIRCUIT BREAKER CLOSED: System resuming operations...");
                        circuitOpen = false;
                        // resetear el contador al cerrar el circuito
                        // failureCount.set(0); // Opcional: resetear o mantener historial
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            }
            return null;
        });
    }

    public void shutdown() {
        executionPool.shutdown();
    }
}