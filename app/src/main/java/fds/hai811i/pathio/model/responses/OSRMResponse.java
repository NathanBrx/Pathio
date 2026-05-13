package fds.hai811i.pathio.model.responses;

import java.util.List;

public class OSRMResponse {
    public List<Route> routes;

    public static class Route {
        public Geometry geometry;
        public double distance;
        public double duration;
    }

    public static class Geometry {
        public List<List<Double>> coordinates; // [longitude, latitude]
        public String type;
    }
}
