package com.gaddiel.smartbuzzers;

public class LocationInfo {
    double lat;
    double lng;
    String locationName;
    int range;

    public LocationInfo(String locationName, double lat, double lng, int range) {
        this.locationName = locationName;
        this.lat = lat;
        this.lng = lng;
        this.range = range;
    }
}
