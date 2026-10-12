package com.sms.model;

/**
 * Lookup entity for the {@code departments} table.
 */
public record Department(int id, String code, String name) {

    @Override
    public String toString() {
        return String.format("[%d] %-6s %s", id, code, name);
    }
}
