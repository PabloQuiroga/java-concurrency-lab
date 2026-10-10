package com.pablo.concurrency.core;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;

public class PriceStore {
    // Almacenamiento concurrente: Activo -> Precio actual
    private final ConcurrentHashMap<String, Double> prices = new ConcurrentHashMap<>();

    public void updatePrice(PriceUpdate update) {
        prices.put(update.asset(), update.price());
    }

    public Optional<Double> getPrice(String asset) {
        return Optional.ofNullable(prices.get(asset));
    }
}