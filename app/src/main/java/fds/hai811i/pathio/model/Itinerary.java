package fds.hai811i.pathio.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Itinerary implements Serializable {
    public String title;
    public String time;
    public String distance;
    public String price;
    public boolean isSelected;
    public List<POI> steps;

    public Itinerary(String title, String time, String distance, String price) {
        this.title = title;
        this.time = time;
        this.distance = distance;
        this.price = price;
        this.isSelected = false;
        this.steps = new ArrayList<>();
    }

    public void addStep(POI poi) {
        this.steps.add(poi);
    }
}
