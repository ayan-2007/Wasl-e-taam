package com.waslataam.security;

import com.waslataam.domain.Admin;
import com.waslataam.domain.AdminPermission;
import com.waslataam.domain.User;

/**
 * Central place for administrator permission checks.
 *
 * <p>Navigation and individual admin screens both call this class, so a screen
 * can never be opened just because the window still has a stale button.</p>
 */
public final class AdminGuardrails {

    private AdminGuardrails() {
    }

    public static boolean isAdmin(User user) {
        return user instanceof Admin;
    }

    public static boolean hasPermission(User user, AdminPermission permission) {
        return user instanceof Admin admin && admin.hasPermission(permission);
    }

    /**
     * @return the permission required by an FXML view, or {@code null} when the
     *         view is not an admin-only screen.
     */
    public static AdminPermission requiredPermission(String fxmlPath) {
        if (fxmlPath == null) {
            return null;
        }
        String normalized = fxmlPath.replace('\\', '/');
        if (normalized.endsWith("AdminDashboard.fxml")) {
            return AdminPermission.VIEW_DASHBOARD;
        }
        if (normalized.endsWith("StrategySwitcherView.fxml")) {
            return AdminPermission.MANAGE_STRATEGIES;
        }
        if (normalized.endsWith("AuditReportView.fxml")) {
            return AdminPermission.VIEW_AUDIT_LOG;
        }
        if (normalized.endsWith("ImpactDashboardView.fxml")) {
            return AdminPermission.VIEW_DASHBOARD;
        }
        return null;
    }

    public static boolean canOpen(User user, String fxmlPath) {
        AdminPermission required = requiredPermission(fxmlPath);
        if (required == null) {
            return true;
        }
        return hasPermission(user, required);
    }

    public static String describeScope(User user) {
        if (!(user instanceof Admin admin)) {
            return "Not an administrator";
        }
        int granted = admin.getPermissions().size();
        int total = AdminPermission.values().length;
        if (granted == total) {
            return "Full access · " + granted + "/" + total + " permissions";
        }
        return "Limited access · " + granted + "/" + total + " permissions";
    }
}
