package com.logistpulse.smartinventory.domain;

import java.util.LinkedHashMap;
import java.util.Map;

/** Receiver: reglas de negocio. Ejemplo en memoria (sin BD). */
public final class InventoryService {
    private record Key(String warehouse, String sku) { }
    private static final class Stock {
        int physical;
        int reserved;
    }
    public record Snapshot(int physical, int reserved, int available) { }
    private final Map<Key, Stock> stocks = new LinkedHashMap<>();

    private static void valid(String warehouse, String sku) {
        if (warehouse == null || warehouse.isBlank() || sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("Bodega y SKU son obligatorios");
        }
    }
    private static void positive(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Cantidad debe ser positiva");
    }
    private Stock get(String warehouse, String sku) {
        valid(warehouse, sku);
        return stocks.computeIfAbsent(new Key(warehouse, sku), k -> new Stock());
    }
    public synchronized Snapshot snapshot(String warehouse, String sku) {
        Stock s = get(warehouse, sku);
        return new Snapshot(s.physical, s.reserved, s.physical - s.reserved);
    }
    public synchronized void reserve(String warehouse, String sku, int quantity) {
        positive(quantity);
        Stock s = get(warehouse, sku);
        if (s.physical - s.reserved < quantity) throw new IllegalStateException("Stock disponible insuficiente para reservar");
        s.reserved = Math.addExact(s.reserved, quantity);
    }
    public synchronized void release(String warehouse, String sku, int quantity) {
        positive(quantity);
        Stock s = get(warehouse, sku);
        if (s.reserved < quantity) throw new IllegalStateException("Cantidad reservada insuficiente para liberar");
        s.reserved -= quantity;
    }
    public synchronized void adjust(String warehouse, String sku, int delta) {
        if (delta == 0) throw new IllegalArgumentException("El ajuste no puede ser cero");
        Stock s = get(warehouse, sku);
        int next = Math.addExact(s.physical, delta);
        if (next < s.reserved) throw new IllegalStateException("El ajuste dejaría stock físico menor al reservado");
        s.physical = next;
    }
    public synchronized void transfer(String source, String destination, String sku, int quantity) {
        positive(quantity);
        valid(source, sku); valid(destination, sku);
        if (source.equals(destination)) throw new IllegalArgumentException("Origen y destino deben ser diferentes");
        Stock from = get(source, sku);
        if (from.physical - from.reserved < quantity) throw new IllegalStateException("Stock disponible insuficiente para transferir");
        Stock to = get(destination, sku);
        int destinationNew = Math.addExact(to.physical, quantity);
        // Validar primero para que la transferencia sea atómica en este Receiver.
        from.physical -= quantity;
        to.physical = destinationNew;
    }
}
