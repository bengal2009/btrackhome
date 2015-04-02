package com.blin.btrack.GPS;

/**
 * Created by Lin on 2015/3/29.
 */
public class LocationInfo {
    private String LocationTime;
    private int ErrorCode;
    private double Latitude;
    private double Longitude;
    private int LocationType;
    private float Radius;

    public int getErrorCode() {
        return ErrorCode;
    }

    public void setErrorCode(int errorCode) {
        ErrorCode = errorCode;
    }

    public String getLocationTime() {
        return LocationTime;
    }

    public void setLocationTime(String locationTime) {
        LocationTime = locationTime;
    }

    public double getLatitude() {
        return Latitude;
    }

    public void setLatitude(double latitude) {
        Latitude = latitude;
    }

    public double getLongitude() {
        return Longitude;
    }

    public void setLongitude(double longitude) {
        Longitude = longitude;
    }

    public int getLocationType() {
        return LocationType;
    }

    public void setLocationType(int locationType) {
        LocationType = locationType;
    }

    public float getRadius() {
        return Radius;
    }

    public void setRadius(float radius) {
        Radius = radius;
    }
}
