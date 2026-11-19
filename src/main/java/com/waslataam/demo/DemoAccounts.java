package com.waslataam.demo;

import com.waslataam.domain.AdminPermission;
import com.waslataam.domain.UserRole;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Single source of truth for the demo accounts shown on the sign-in screen and
 * created by {@link DemoDataSeeder}.
 */
public record DemoAccounts(
        UserRole role,
        String name,
        String email,
        String organization,
        String blurb,
        Set<AdminPermission> permissions
) {
    /** Every demo account uses this password, shown on the sign-in screen. */
    public static final String PASSWORD = "demo1234";

    private static final List<DemoAccounts> ACCOUNTS = List.of(
            new DemoAccounts(UserRole.DONOR, "Amina Siddiqui", "donor@wasl.demo",
                    "Amina's Kitchen",
                    "Posts across the lifecycle, pending to delivered", EnumSet.noneOf(AdminPermission.class)),
            new DemoAccounts(UserRole.DONOR, "Rehan Qureshi", "bakery@wasl.demo",
                    "Karachi Bakery House",
                    "Dry food, perishables and a rejected listing", EnumSet.noneOf(AdminPermission.class)),
            new DemoAccounts(UserRole.RECIPIENT, "Sadaf Welfare Trust", "recipient@wasl.demo",
                    "Sadaf Welfare Trust",
                    "Claims, deliveries and impact history", EnumSet.noneOf(AdminPermission.class)),
            new DemoAccounts(UserRole.RECIPIENT, "Al-Noor Shelter Home", "shelter@wasl.demo",
                    "Al-Noor Shelter Home",
                    "An in-transit delivery and a completed delivery", EnumSet.noneOf(AdminPermission.class)),
            new DemoAccounts(UserRole.VOLUNTEER, "Bilal Ahmed", "volunteer@wasl.demo",
                    "Bilal Ahmed",
                    "Van driver with pickup and delivery work", EnumSet.noneOf(AdminPermission.class)),
            new DemoAccounts(UserRole.VOLUNTEER, "Zara Khan", "courier@wasl.demo",
                    "Zara Khan",
                    "Motorcycle courier with completed deliveries", EnumSet.noneOf(AdminPermission.class)),
            new DemoAccounts(UserRole.ADMIN, "Ayesha Khan", "admin@wasl.demo",
                    "Platform owner",
                    "Full administrator: every permission",
                    EnumSet.allOf(AdminPermission.class)),
            new DemoAccounts(UserRole.ADMIN, "Omar Farooq", "ops@wasl.demo",
                    "Operations team",
                    "Limited admin: dashboards and audit, no strategy or undo",
                    EnumSet.of(AdminPermission.VIEW_DASHBOARD,
                            AdminPermission.VIEW_ALL_DONATIONS,
                            AdminPermission.VIEW_AUDIT_LOG))
    );

    public static List<DemoAccounts> all() {
        return ACCOUNTS;
    }

    public static Optional<DemoAccounts> byEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return ACCOUNTS.stream()
                .filter(account -> account.email().equalsIgnoreCase(email.trim()))
                .findFirst();
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    @Override
    public String toString() {
        String suffix = "";
        if (role == UserRole.ADMIN) {
            suffix = permissions.size() == AdminPermission.values().length
                    ? " · full access"
                    : " · limited access";
        }
        return name + " · " + role.getDisplayName() + " · " + email + suffix;
    }
}
