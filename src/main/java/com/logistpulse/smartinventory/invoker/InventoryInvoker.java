package com.logistpulse.smartinventory.invoker;

import com.logistpulse.smartinventory.command.Command;
import com.logistpulse.smartinventory.ledger.CommandLedger;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/** Invoker: encola, ejecuta, reintenta errores transitorios y compensa. */
public final class InventoryInvoker {
    private final CommandLedger ledger;
    private final int maxAttempts;
    private final Deque<Command> pending = new ArrayDeque<>();
    private final Deque<Command> history = new ArrayDeque<>();
    public InventoryInvoker(CommandLedger ledger, int maxAttempts) {
        this.ledger = Objects.requireNonNull(ledger);
        if (maxAttempts < 1) throw new IllegalArgumentException("maxAttempts >= 1");
        this.maxAttempts = maxAttempts;
    }
    /** Encola solo peticiones nuevas; evita duplicar peticiones ya registradas. */
    public void submit(Command command) {
        Objects.requireNonNull(command);
        CommandLedger.Entry e = ledger.register(command.idempotencyKey(), command.description());
        if (e.status() == CommandLedger.Status.PENDING && pending.stream().noneMatch(c -> c.idempotencyKey().equals(command.idempotencyKey()))) {
            pending.addLast(command);
        }
    }
    public void processPending() {
        while (!pending.isEmpty()) executeInternal(pending.removeFirst(), true);
    }
    /** Ejecuta ahora, sin pasar por cola. */
    public void executeCommand(Command command) { executeInternal(command, true); }
    private void executeInternal(Command command, boolean recordHistory) {
        Objects.requireNonNull(command);
        CommandLedger.Entry e = ledger.register(command.idempotencyKey(), command.description());
        if (e.status() == CommandLedger.Status.SUCCEEDED) {
            System.out.println("DUPLICADO OMITIDO: " + e.key());
            return;
        }
        if (e.status() == CommandLedger.Status.RUNNING) throw new IllegalStateException("Comando en ejecución: " + e.key());
        for (int i = 0; i < maxAttempts; i++) {
            ledger.running(e);
            try {
                command.execute();
                ledger.succeeded(e);
                if (recordHistory) history.push(command);
                System.out.println("OK: " + command.description());
                return;
            } catch (TransientInventoryException ex) {
                ledger.failed(e, ex);
                if (i == maxAttempts - 1) throw ex;
                System.out.println("Reintento " + (i + 1) + " por error transitorio: " + ex.getMessage());
            } catch (RuntimeException ex) {
                ledger.failed(e, ex);
                throw ex;
            }
        }
    }
    /** Compensación: ejecuta otro comando; no borra el registro original. */
    public void undoLast() {
        if (history.isEmpty()) throw new IllegalStateException("No hay operación para compensar");
        Command original = history.peek();
        executeInternal(original.inverse(), false);
        history.pop();
    }
}
