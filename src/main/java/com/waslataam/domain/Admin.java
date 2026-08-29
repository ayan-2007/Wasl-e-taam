package com.waslataam.domain;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

public class Admin extends User {
    private final Set<AdminPermission> permissions;

    public Admin(UUID id, String name, String email, Location location, Set<AdminPermission> permissions) {
        super(id, name, email, location, UserRole.ADMIN);
        this.permissions = permissions != null ? EnumSet.copyOf(permissions) : EnumSet.noneOf(AdminPermission.class);
    }

    public Set<AdminPermission> getPermissions() {
        return permissions;
    }

    public boolean hasPermission(AdminPermission permission) {
        return permissions.contains(permission);
    }

    @Override
    public String toString() {
        return "Admin{" +
                "permissions=" + permissions +
                "} " + super.toString();
    }
}