package com.logistpulse.smartinventory.command;

import com.logistpulse.smartinventory.domain.InventoryService;

public final class AdjustCommand extends AbstractInventoryCommand {
    private final String warehouse, sku;
    private final int delta;
    public AdjustCommand(InventoryService inventory, String key, String warehouse, String sku, int delta) {
        super(inventory, key); this.warehouse = warehouse; this.sku = sku; this.delta = delta;
    }
    public void execute() { inventory.adjust(warehouse, sku, delta); }
    public Command inverse() { return new AdjustCommand(inventory, inverseKey(), warehouse, sku, Math.negateExact(delta)); }
    public String description() { return "ADJUST " + (delta > 0 ? "+" : "") + delta + " " + sku + " @ " + warehouse; }
}
