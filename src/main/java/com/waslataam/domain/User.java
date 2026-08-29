package com.waslataam.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public abstract class User {
    private final UUID id;
    private final String name;
    private final String email;
    private final Location location;
    private final UserRole role;
    private final LocalDateTime registeredAt;
    private boolean active;

    protected User(UUID id, String name, String email, Location location, UserRole role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.location = location;
        this.role = role;
        this.registeredAt = LocalDateTime.now();
        this.active = true;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Location getLocation() {
        return location;
    }

    public UserRole getRole() {
        return role;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                '}';
    }
}