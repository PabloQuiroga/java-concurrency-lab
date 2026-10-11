package com.pablo.concurrency.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

class PriceStoreTest {
    private PriceStore store;
    private OrderExecutor executor;

    @BeforeEach
    void setUp() {
        store = new PriceStore();
        executor = new OrderExecutor();
    }

    @Test
    void testUpdateAndGetPrice() {
        PriceUpdate update = new PriceUpdate("BTC/USD", 50000.0, Instant.now());
        store.updatePrice(update);

        var price = store.getPrice("BTC/USD");

        assertTrue(price.isPresent(), "El precio debería estar presente");
        assertEquals(50000.0, price.get(), "El precio debe coincidir con el valor insertado");
    }

    @Test
    void testGetNonExistentPrice() {
        var price = store.getPrice("NON_EXISTENT");
        assertTrue(price.isEmpty(), "El precio debería estar vacío para un activo inexistente");
    }

    @Test
    void testPriceFeedNotifiesConsumer() throws InterruptedException {
        // 1. Necesitamos una cola y un motor para verificar que el precio llega al store
        EventQueue queue = new EventQueue(10);
        PriceStore store = new PriceStore();
        TradingEngine engine = new TradingEngine(queue, store, executor);

        // Lanzamos el motor en un hilo separado para que procese la cola
        Thread engineThread = new Thread(engine);
        engineThread.start();

        // 2. Creamos el feed pasándole la COLA (no el callback)
        PriceFeed feed = new PriceFeed("BTC/USD", queue);

        // Lanzamos el feed en un hilo separado
        Thread feedThread = new Thread(feed);
        feedThread.start();

        // 3. Esperamos a que el precio llegue al store
        // En lugar de un latch, consultamos el store hasta que el precio no sea nulo
        boolean success = false;
        for (int i = 0; i < 20; i++) { // Intentamos durante 2 segundos
            if (store.getPrice("BTC/USD").isPresent()) {
                success = true;
                break;
            }
            Thread.sleep(100);
        }

        queue.enqueue(PriceUpdate.SHUTDOWN_SIGNAL);

        // Limpieza
        feed.stop();
        //engine.stop();
        feedThread.join();
        engineThread.join();

        assertTrue(success, "El PriceFeed debería haber enviado la actualización a través de la cola al PriceStore");
    }
}