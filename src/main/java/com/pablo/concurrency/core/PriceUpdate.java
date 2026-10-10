package com.pablo.concurrency.core;

import java.time.Instant;

public record PriceUpdate(String asset, double price, Instant timestamp) {
}
