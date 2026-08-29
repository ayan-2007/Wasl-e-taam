package com.waslataam.domain;

public enum AdminPermission {
    VIEW_DASHBOARD("View Dashboard"),
    MANAGE_STRATEGIES("Manage Matching Strategies"),
    VIEW_AUDIT_LOG("View Audit Log"),
    UNDO_ACTIONS("Undo Actions"),
    MANAGE_USERS("Manage Users"),
    VIEW_ALL_DONATIONS("View All Donations"),
    EXPORT_REPORTS("Export Reports");

    private final String displayName;

    AdminPermission(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}