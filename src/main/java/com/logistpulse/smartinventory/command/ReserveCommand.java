package com.logistpulse.smartinventory.command;

import com.logistpulse.smartinventory.domain.InventoryService;

public final class ReserveCommand extends AbstractInventoryCommand {
    private final String warehouse, sku;
    private final int quantity;
    public ReserveCommand(InventoryService inventory, String key, String warehouse, String sku, int quantity) {
        super(inventory, key); this.warehouse = warehouse; this.sku = sku; this.quantity = quantity;
    }
    public void execute() { inventory.reserve(warehouse, sku, quantity); }
    public Command inverse() { return new ReleaseCommand(inventory, inverseKey(), warehouse, sku, quantity); }
    public String description() { return "RESERVE " + quantity + " " + sku + " @ " + warehouse; }
}
