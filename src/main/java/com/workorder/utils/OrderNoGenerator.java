package com.workorder.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public class OrderNoGenerator {
    private static final AtomicInteger counter = new AtomicInteger(1);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static String generate() {
        String date = LocalDateTime.now().format(formatter);
        int seq = counter.getAndUpdate(i -> i >= 9999 ? 1 : i + 1);
        return String.format("WO%s-%04d", date, seq);
    }
}