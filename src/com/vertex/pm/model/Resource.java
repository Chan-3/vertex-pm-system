package com.vertex.pm.model;

/**
 * Represents a resource assigned to project work.
 */
public class Resource {
    private final int id;
    private String name;
    private String role;
    private boolean available;

    /**
     * Creates a resource.
     */
    public Resource(int id, String name, String role, boolean available) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.available = available;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /**
     * Updates the resource name.
     */
    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    /**
     * Updates the resource role.
     */
    public void setRole(String role) {
        this.role = role;
    }

    public boolean isAvailable() {
        return available;
    }

    /**
     * Updates the resource availability.
     */
    public void setAvailable(boolean available) {
        this.available = available;
    }
}
