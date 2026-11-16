package com.waslataam.ui.util;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Small, shared UI helpers so every screen speaks the same visual language. */
public final class UiUtils {
    private static final DateTimeFormatter TIME_TODAY = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("d MMM, HH:mm");
    private static final DateTimeFormatter DATE_ONLY = DateTimeFormatter.ofPattern("d MMM yyyy");

    private UiUtils() {
    }

    public static VBox emptyState(String title, String message) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("empty-title");

        Label bodyLabel = new Label(message);
        bodyLabel.getStyleClass().add("empty-body");
        bodyLabel.setWrapText(true);
        bodyLabel.setMaxWidth(380);
        bodyLabel.setTextAlignment(TextAlignment.CENTER);

        VBox box = new VBox(4, titleLabel, bodyLabel);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new javafx.geometry.Insets(28));
        return box;
    }

    public static Label statusChip(String status) {
        Label chip = new Label(status == null ? "Unknown" : status);
        chip.getStyleClass().addAll("status-chip", statusStyleClass(status));
        return chip;
    }

    public static <S> Callback<TableColumn<S, String>, TableCell<S, String>> statusCellFactory() {
        return column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(null);
                    setGraphic(statusChip(item));
                }
            }
        };
    }

    public static void setError(Control control, boolean error) {
        if (control == null) {
            return;
        }
        if (error) {
            if (!control.getStyleClass().contains("error")) {
                control.getStyleClass().add("error");
            }
        } else {
            control.getStyleClass().remove("error");
        }
    }

    public static void setFieldError(Control control, Label label, String message) {
        boolean hasError = message != null && !message.isBlank();
        setError(control, hasError);
        if (label != null) {
            label.setText(hasError ? message : "");
            label.setVisible(hasError);
            label.setManaged(hasError);
        }
    }

    public static boolean confirm(Node owner, String title, String header, String message,
                                  String confirmText, String cancelText, boolean destructive) {
        ButtonType confirm = new ButtonType(confirmText, ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType(cancelText, ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, confirm, cancel);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.initOwner(owner == null || owner.getScene() == null
                ? null : owner.getScene().getWindow());
        styleDialog(alert);
        if (destructive) {
            Node confirmButton = alert.getDialogPane().lookupButton(confirm);
            confirmButton.getStyleClass().add("danger-button");
        }
        return alert.showAndWait().filter(confirm::equals).isPresent();
    }

    public static void showError(Node owner, String title, String message) {
        ButtonType dismiss = new ButtonType("Dismiss", ButtonBar.ButtonData.OK_DONE);
        Alert alert = new Alert(Alert.AlertType.ERROR, message, dismiss);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.initOwner(owner == null || owner.getScene() == null
                ? null : owner.getScene().getWindow());
        styleDialog(alert);
        alert.showAndWait();
    }

    private static void styleDialog(Alert alert) {
        alert.getDialogPane().getStylesheets().add(ThemeManager.stylesheetUrl());
        alert.getDialogPane().getStyleClass().add("app-dialog");
        if (ThemeManager.isDark()) {
            alert.getDialogPane().getStyleClass().add("dark");
        }
    }

    public static String formatDateTime(LocalDateTime value) {
        if (value == null) {
            return "—";
        }
        LocalDate date = value.toLocalDate();
        LocalDate today = LocalDate.now();
        if (date.equals(today)) {
            return "Today, " + value.format(TIME_TODAY);
        }
        if (date.equals(today.plusDays(1))) {
            return "Tomorrow, " + value.format(TIME_TODAY);
        }
        return value.format(DATE_TIME);
    }

    public static String formatDate(LocalDate value) {
        return value == null ? "—" : value.format(DATE_ONLY);
    }

    public static String humanizeAction(String action) {
        if (action == null) {
            return "Unknown action";
        }
        return switch (action) {
            case "POST_DONATION" -> "Posted donation";
            case "CLAIM_DONATION" -> "Claimed donation";
            case "CANCEL_CLAIM" -> "Cancelled claim";
            case "PICKUP_DONATION" -> "Confirmed pickup";
            case "DELIVER_DONATION" -> "Confirmed delivery";
            case "EXPIRE_DONATION" -> "Expired donation";
            case "REJECT_DONATION" -> "Rejected donation";
            default -> action.replace('_', ' ').toLowerCase(Locale.ROOT);
        };
    }

    public static String quantityText(double quantity, String unit) {
        String amount = Math.abs(quantity - Math.rint(quantity)) < 0.0001
                ? String.valueOf((long) quantity)
                : String.format(Locale.US, "%.1f", quantity);
        return unit == null || unit.isBlank() ? amount : amount + " " + unit;
    }

    private static String statusStyleClass(String status) {
        if (status == null) {
            return "status-neutral";
        }
        return switch (status.toLowerCase(Locale.ROOT)) {
            case "available" -> "status-available";
            case "claimed" -> "status-claimed";
            case "in transit" -> "status-transit";
            case "delivered" -> "status-delivered";
            case "expired" -> "status-expired";
            case "rejected" -> "status-rejected";
            case "pending review" -> "status-pending";
            default -> "status-neutral";
        };
    }
}
