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

    @BeforeEach
    void setUp() {
        store = new PriceStore();
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
        // Usamos un CountDownLatch para sincronizar el hilo del test con el hilo del PriceFeed
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean notified = new AtomicBoolean(false);

        // Creamos un feed que, al recibir un precio, marca la variable 'notified' y libera el latch
        PriceFeed feed = new PriceFeed("BTC/USD", update -> {
            notified.set(true);
            latch.countDown();
        });

        // Lanzamos el feed en un hilo separado
        Thread thread = new Thread(feed);
        thread.start();

        // Esperamos hasta 2 segundos a que el feed envíe la primera actualización
        boolean success = latch.await(2, TimeUnit.SECONDS);

        feed.stop();
        thread.join();

        assertTrue(success, "El PriceFeed debería haber notificado al consumidor");
        assertTrue(notified.get(), "La variable notified debería ser true");
    }
}