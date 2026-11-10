package com.logistpulse.smartinventory.command;

import com.logistpulse.smartinventory.domain.InventoryService;
import java.util.Objects;
import java.util.UUID;

abstract class AbstractInventoryCommand implements Command {
    protected final InventoryService inventory;
    private final String key;
    protected AbstractInventoryCommand(InventoryService inventory, String key) {
        this.inventory = Objects.requireNonNull(inventory);
        if (key == null || key.isBlank()) throw new IllegalArgumentException("Idempotency key obligatoria");
        this.key = key;
    }
    public final String idempotencyKey() { return key; }
    protected final String inverseKey() { return "comp-" + UUID.randomUUID(); }
}
