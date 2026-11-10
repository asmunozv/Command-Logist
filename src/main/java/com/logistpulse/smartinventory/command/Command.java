package com.logistpulse.smartinventory.command;

/** Una petición encapsulada. El invocador solo conoce este contrato. */
public interface Command {
    String idempotencyKey();
    String description();
    void execute();
    /** Nueva petición que revierte el efecto de esta operación. */
    Command inverse();
}
