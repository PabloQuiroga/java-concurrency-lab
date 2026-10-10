package com.pablo.concurrency;

import com.pablo.concurrency.core.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        PriceStore store = new PriceStore();

        // Creamos un pool de hilos para manejar los feeds
        ExecutorService executor = Executors.newFixedThreadPool(3);

        // Definimos la acción que ocurre cuando llega un precio
        java.util.function.Consumer<PriceUpdate> consumer = update -> {
            store.updatePrice(update);
            System.out.printf("Update: %s -> %.2f\n", update.asset(), update.price());
        };

        // Lanzamos 3 feeds diferentes
        executor.execute(new PriceFeed("BTC/USD", consumer));
        executor.execute(new PriceFeed("ETH/USD", consumer));
        executor.execute(new PriceFeed("AAPL", consumer));

        // Dejamos correr el simulador por 5 segundos
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        executor.shutdownNow();
        System.out.println("Simulation finished.");
    }
}