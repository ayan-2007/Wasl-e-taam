module com.waslataam {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires org.controlsfx.controls;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires org.slf4j;

    opens com.waslataam to javafx.fxml;
    opens com.waslataam.ui.controllers to javafx.fxml;
    opens com.waslataam.domain to com.fasterxml.jackson.databind;
    opens com.waslataam.factory to com.fasterxml.jackson.databind;
    opens com.waslataam.strategy to com.fasterxml.jackson.databind;
    opens com.waslataam.state to com.fasterxml.jackson.databind;
    opens com.waslataam.command to com.fasterxml.jackson.databind;
    opens com.waslataam.chain to com.fasterxml.jackson.databind;
    opens com.waslataam.observer to com.fasterxml.jackson.databind;
    opens com.waslataam.coordination to com.fasterxml.jackson.databind;
    opens com.waslataam.repository to com.fasterxml.jackson.databind;
    opens com.waslataam.viewmodel to com.fasterxml.jackson.databind;

    exports com.waslataam;
    exports com.waslataam.domain;
    exports com.waslataam.factory;
    exports com.waslataam.strategy;
    exports com.waslataam.state;
    exports com.waslataam.command;
    exports com.waslataam.chain;
    exports com.waslataam.observer;
    exports com.waslataam.coordination;
    exports com.waslataam.repository;
    exports com.waslataam.viewmodel;
}