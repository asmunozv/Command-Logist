package com.logistpulse.smartinventory.ledger;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Ledger en memoria. Un registro único por clave de idempotencia. */
public final class CommandLedger {
    public enum Status { PENDING, RUNNING, SUCCEEDED, FAILED }
    public static final class Entry {
        private final String key, description;
        private Status status = Status.PENDING;
        private int attempts;
        private String error = "";
        Entry(String key, String description) { this.key = key; this.description = description; }
        public String key() { return key; }
        public String description() { return description; }
        public Status status() { return status; }
        public int attempts() { return attempts; }
        public String error() { return error; }
        public String toString() { return key + " | " + description + " | " + status + " | intentos=" + attempts + (error.isEmpty()? "" : " | " + error); }
    }
    private final Map<String, Entry> entries = new LinkedHashMap<>();
    public synchronized Entry register(String key, String description) {
        Entry entry = entries.get(key);
        if (entry != null && !entry.description.equals(description))
            throw new IllegalArgumentException("Clave de idempotencia reutilizada con otra petición: " + key);
        if (entry == null) { entry = new Entry(key, description); entries.put(key, entry); }
        return entry;
    }
    public synchronized void running(Entry e) { e.status = Status.RUNNING; e.attempts++; e.error = ""; }
    public synchronized void succeeded(Entry e) { e.status = Status.SUCCEEDED; e.error = ""; }
    public synchronized void failed(Entry e, Exception ex) { e.status = Status.FAILED; e.error = ex.getMessage(); }
    public synchronized List<Entry> entries() { return List.copyOf(entries.values()); }
}
