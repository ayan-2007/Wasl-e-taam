package com.waslataam.viewmodel;

import javafx.beans.property.*;

public class DashboardViewModel {
    private static final DashboardViewModel INSTANCE = new DashboardViewModel();
    private final DoubleProperty kgSaved = new SimpleDoubleProperty(0);
    private final IntegerProperty mealsServed = new SimpleIntegerProperty(0);
    private final DoubleProperty co2Avoided = new SimpleDoubleProperty(0);
    private final IntegerProperty activeDonations = new SimpleIntegerProperty(0);
    private final IntegerProperty totalDonors = new SimpleIntegerProperty(0);
    private final IntegerProperty totalRecipients = new SimpleIntegerProperty(0);
    private final IntegerProperty totalVolunteers = new SimpleIntegerProperty(0);

    public DoubleProperty kgSavedProperty() {
        return kgSaved;
    }

    public double getKgSaved() {
        return kgSaved.get();
    }

    public void setKgSaved(double value) {
        kgSaved.set(value);
    }

    public void addKgSaved(double value) {
        kgSaved.set(kgSaved.get() + value);
    }

    public IntegerProperty mealsServedProperty() {
        return mealsServed;
    }

    public int getMealsServed() {
        return mealsServed.get();
    }

    public void setMealsServed(int value) {
        mealsServed.set(value);
    }

    public void addMealsServed(int value) {
        mealsServed.set(mealsServed.get() + value);
    }

    public DoubleProperty co2AvoidedProperty() {
        return co2Avoided;
    }

    public double getCo2Avoided() {
        return co2Avoided.get();
    }

    public void setCo2Avoided(double value) {
        co2Avoided.set(value);
    }

    public void addCo2Avoided(double value) {
        co2Avoided.set(co2Avoided.get() + value);
    }

    public IntegerProperty activeDonationsProperty() {
        return activeDonations;
    }

    public int getActiveDonations() {
        return activeDonations.get();
    }

    public void setActiveDonations(int value) {
        activeDonations.set(value);
    }

    public IntegerProperty totalDonorsProperty() {
        return totalDonors;
    }

    public int getTotalDonors() {
        return totalDonors.get();
    }

    public void setTotalDonors(int value) {
        totalDonors.set(value);
    }

    public IntegerProperty totalRecipientsProperty() {
        return totalRecipients;
    }

    public int getTotalRecipients() {
        return totalRecipients.get();
    }

    public void setTotalRecipients(int value) {
        totalRecipients.set(value);
    }

    public IntegerProperty totalVolunteersProperty() {
        return totalVolunteers;
    }

    public int getTotalVolunteers() {
        return totalVolunteers.get();
    }

    public void setTotalVolunteers(int value) {
        totalVolunteers.set(value);
    }

    public void updateFromDelivery(double quantityKg, int servings) {
        addKgSaved(quantityKg);
        addMealsServed(servings > 0 ? servings : (int) (quantityKg * 2)); // rough estimate: 0.5kg per meal
        addCo2Avoided(quantityKg * 2.5); // ~2.5 kg CO2 per kg food saved
    }

    public static DashboardViewModel getInstance() {
        return INSTANCE;
    }
}