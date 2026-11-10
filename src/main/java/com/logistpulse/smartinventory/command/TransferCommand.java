package com.logistpulse.smartinventory.command;

import com.logistpulse.smartinventory.domain.InventoryService;

public final class TransferCommand extends AbstractInventoryCommand {
    private final String source, destination, sku;
    private final int quantity;
    public TransferCommand(InventoryService inventory, String key, String source, String destination, String sku, int quantity) {
        super(inventory, key); this.source = source; this.destination = destination; this.sku = sku; this.quantity = quantity;
    }
    public void execute() { inventory.transfer(source, destination, sku, quantity); }
    public Command inverse() { return new TransferCommand(inventory, inverseKey(), destination, source, sku, quantity); }
    public String description() { return "TRANSFER " + quantity + " " + sku + " " + source + " -> " + destination; }
}
