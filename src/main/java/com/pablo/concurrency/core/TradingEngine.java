package com.pablo.concurrency.core;

import java.util.concurrent.atomic.AtomicLong;

public class TradingEngine implements Runnable {
    private final EventQueue eventQueue;
    private final PriceStore priceStore;
    private final OrderExecutor orderExecutor;
    private volatile boolean running = true;
    private final AtomicLong ordersExecuted = new AtomicLong(0);

    public TradingEngine(EventQueue queue, PriceStore store, OrderExecutor executor) {
        this.eventQueue = queue;
        this.priceStore = store;
        this.orderExecutor = executor;
    }

    public void stop() {
        this.running = false;
    }

    @Override
    public void run() {
        System.out.println("Trading Engine started. Monitoring prices...");
        while (true) {
            try {
                PriceUpdate update = eventQueue.dequeue();

                if (update == PriceUpdate.SHUTDOWN_SIGNAL) {
                    break;
                }

                priceStore.updatePrice(update);
                if (update.price() > 500.0) {
                    executeOrder(update);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println("Trading Engine stopped.");
    }

    private void executeOrder(PriceUpdate update) {
        ordersExecuted.incrementAndGet();
//        System.out.printf("[ORDER] Executed BUY for %s at %.2f\n",
//                update.asset(), update.price());
        orderExecutor.execute(update);
    }

    public long getOrdersExecuted() {
        return ordersExecuted.get();
    }
}