package com.pablo.concurrency.core;

import java.time.Instant;

public record PriceUpdate(String asset, double price, Instant timestamp) {
    public static final PriceUpdate SHUTDOWN_SIGNAL = new PriceUpdate("SHUTDOWN", -1.0, Instant.EPOCH);
}
