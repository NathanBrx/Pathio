package fds.hai811i.pathio;

public class Itinerary {
    public String title;
    public String time;
    public String distance;
    public String price;
    public boolean isSelected;

    public Itinerary(String title, String time, String distance, String price) {
        this.title = title;
        this.time = time;
        this.distance = distance;
        this.price = price;
        this.isSelected = false;
    }
}
