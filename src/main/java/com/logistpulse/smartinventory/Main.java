package com.logistpulse.smartinventory;

import com.logistpulse.smartinventory.command.*;
import com.logistpulse.smartinventory.domain.InventoryService;
import com.logistpulse.smartinventory.invoker.InventoryInvoker;
import com.logistpulse.smartinventory.ledger.CommandLedger;

/** Client: construye y configura comandos, Receiver e Invoker. */
public final class Main {
    public static void main(String[] args) {
        InventoryService inventory = new InventoryService();
        CommandLedger ledger = new CommandLedger();
        InventoryInvoker invoker = new InventoryInvoker(ledger, 3);
        String sku = "SKU-001", a = "BODEGA-A", b = "BODEGA-B";
        invoker.submit(new AdjustCommand(inventory, "pedido-001", a, sku, 100));
        invoker.submit(new ReserveCommand(inventory, "pedido-002", a, sku, 20));
        invoker.submit(new ReleaseCommand(inventory, "pedido-003", a, sku, 5));
        invoker.submit(new TransferCommand(inventory, "pedido-004", a, b, sku, 30));
        invoker.processPending();
        System.out.println("ANTES COMPENSACION A=" + inventory.snapshot(a,sku) + " B=" + inventory.snapshot(b,sku));
        invoker.executeCommand(new TransferCommand(inventory, "pedido-004", a, b, sku, 30)); // idempotente
        invoker.undoLast(); // comando inverso para compensar transferencia
        System.out.println("DESPUES COMPENSACION A=" + inventory.snapshot(a,sku) + " B=" + inventory.snapshot(b,sku));
        System.out.println("--- LEDGER ---");
        ledger.entries().forEach(System.out::println);
    }
}
