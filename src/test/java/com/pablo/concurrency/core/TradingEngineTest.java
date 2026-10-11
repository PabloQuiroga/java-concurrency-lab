package com.pablo.concurrency.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

class TradingEngineTest {
    private PriceStore store;
    private EventQueue queue;
    private TradingEngine engine;

    @BeforeEach
    void setUp() {
        store = new PriceStore();
        queue = new EventQueue(100);
        engine = new TradingEngine(queue, store);
    }

    @Test
    void testOrderExecutionLogic() throws InterruptedException {
        // 1. Iniciamos el motor en un hilo separado
        Thread engineThread = new Thread(engine);
        engineThread.start();

        // 2. Enviamos un precio que DEBÍA disparar una orden (> 500)
        // Usamos un PriceUpdate manual
        queue.enqueue(new PriceUpdate("BTC/USD", 600.0, Instant.now()));

        // 3. Enviamos un precio que NO debe disparar una orden (< 500)
        queue.enqueue(new PriceUpdate("ETH/USD", 100.0, Instant.now()));

        // Esperamos un momento a que el worker procese la cola
        Thread.sleep(200);

        // 4. Verificamos que solo se haya ejecutado UNA orden
        assertEquals(1, engine.getOrdersExecuted(), "Debería haberse ejecutado exactamente una orden");

        queue.enqueue(PriceUpdate.SHUTDOWN_SIGNAL);

        engine.stop();
        engineThread.join();
    }

    @Test
    void testConcurrentPriceUpdates() throws InterruptedException {
        // Este test verifica que el motor no crashee con múltiples productores
        int numThreads = 10;
        int updatesPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);

        // Lanzamos 10 hilos enviando precios simultáneamente
        for (int i = 0; i < numThreads; i++) {
            executor.execute(() -> {
                for (int j = 0; j < updatesPerThread; j++) {
                    queue.enqueue(new PriceUpdate("TEST", 600.0, Instant.now()));
                }
                latch.countDown();
            });
        }

        // Iniciamos el motor
        Thread engineThread = new Thread(engine);
        engineThread.start();

        // Esperamos a que todos los productores terminen
        latch.await(5, TimeUnit.SECONDS);

        // Esperamos un poco más para que el motor procese todo
        Thread.sleep(500);

        // Verificamos que el total de órdenes coincida con el total de actualizaciones
        assertEquals(numThreads * updatesPerThread, engine.getOrdersExecuted(),
                "El motor debería haber procesado todas las actualizaciones concurrentes");

        queue.enqueue(PriceUpdate.SHUTDOWN_SIGNAL);

        engine.stop();
        engineThread.join();
        executor.shutdown();
    }
}
