package com.waslataam.domain;

import java.util.Objects;

public class Location {
    private final double latitude;
    private final double longitude;
    private final String address;
    private final String proximityZone;

    public Location(double latitude, double longitude, String address, String proximityZone) {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.proximityZone = proximityZone;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getAddress() {
        return address;
    }

    public String getProximityZone() {
        return proximityZone;
    }

    public double distanceTo(Location other) {
        if (other == null) {
            return Double.MAX_VALUE;
        }
        final int R = 6371; // Earth radius in km
        double latDistance = Math.toRadians(other.latitude - this.latitude);
        double lonDistance = Math.toRadians(other.longitude - this.longitude);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(other.latitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Double.compare(location.latitude, latitude) == 0 &&
                Double.compare(location.longitude, longitude) == 0 &&
                Objects.equals(address, location.address) &&
                Objects.equals(proximityZone, location.proximityZone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude, address, proximityZone);
    }

    @Override
    public String toString() {
        return "Location{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", address='" + address + '\'' +
                ", proximityZone='" + proximityZone + '\'' +
                '}';
    }
}