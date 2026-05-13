package fds.hai811i.pathio.model;

import java.io.Serializable;

public class POI implements Serializable {
    private String name;
    private double latitude;
    private double longitude;
    private String category; // culture, loisir, food, discover
    private double price;
    private int effortLevel; // 1: Low, 2: Moderate, 3: High
    private String description;
    private boolean adaptedForPMR;
    private boolean adaptedForChildren;
    private boolean indoor;

    public POI(String name, double latitude, double longitude, String category, double price, int effortLevel, String description) {
        this(name, latitude, longitude, category, price, effortLevel, description, true, true, false);
    }

    public POI(String name, double latitude, double longitude, String category, double price, int effortLevel, String description, boolean adaptedForPMR, boolean adaptedForChildren, boolean indoor) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category = category;
        this.price = price;
        this.effortLevel = effortLevel;
        this.description = description;
        this.adaptedForPMR = adaptedForPMR;
        this.adaptedForChildren = adaptedForChildren;
        this.indoor = indoor;
    }

    public String getName() { return name; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public int getEffortLevel() { return effortLevel; }
    public String getDescription() { return description; }
    public boolean isAdaptedForPMR() { return adaptedForPMR; }
    public boolean isAdaptedForChildren() { return adaptedForChildren; }
    public boolean isIndoor() { return indoor; }
}
