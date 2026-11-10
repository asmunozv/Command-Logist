package com.logistpulse.smartinventory.invoker;

/** Solo estos errores se reintentan. Los errores de negocio no. */
public final class TransientInventoryException extends RuntimeException {
    public TransientInventoryException(String message) { super(message); }
}
