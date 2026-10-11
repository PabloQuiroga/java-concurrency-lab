package com.pablo.concurrency;

import com.pablo.concurrency.core.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        // 1. Creamos la infraestructura base
        PriceStore store = new PriceStore();
        EventQueue queue = new EventQueue(100); // Cola con capacidad de 100
        TradingEngine engine = new TradingEngine(queue, store);

        // 2. Lanzamos el Motor de Decisiones en su propio hilo
        Thread engineThread = new Thread(engine);
        engineThread.start();

        // 3. Creamos un pool para los feeds de precios
        ExecutorService executor = Executors.newFixedThreadPool(3);

        // 4. Lanzamos los feeds pasándoles la COLA, no el store
        executor.execute(new PriceFeed("BTC/USD", queue));
        executor.execute(new PriceFeed("ETH/USD", queue));
        executor.execute(new PriceFeed("AAPL", queue));

        // Dejamos correr la simulación por 10 segundos
        try {
            // Esperamos para ver la magia ocurrir
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Stopping simulation...");
        // Apagado ordenado
        executor.shutdownNow();
        // Forzamos la parada del motor
        engine.stop();

        System.out.println("Simulation finished.");
    }
}