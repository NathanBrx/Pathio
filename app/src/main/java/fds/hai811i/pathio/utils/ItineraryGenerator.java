package fds.hai811i.pathio.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import fds.hai811i.pathio.model.Itinerary;
import fds.hai811i.pathio.model.POI;
import fds.hai811i.pathio.model.UserPreferences;

public class ItineraryGenerator {

    public static List<Itinerary> generate(UserPreferences prefs, List<POI> database) {
        List<Itinerary> results = new ArrayList<>();

        if (database == null || database.isEmpty()) return results;

        // Shuffle the entire database once at the start to ensure randomness
        List<POI> pool = new ArrayList<>(database);
        Collections.shuffle(pool);

        // Track used POIs to avoid exact duplicates if possible (though some overlap is okay)
        List<POI> usedInResults = new ArrayList<>();

        // Generate 3 types with distinct logic
        results.add(createItinerary("Économique", 0.2f, prefs, pool, usedInResults));
        results.add(createItinerary("Équilibré", 0.5f, prefs, pool, usedInResults));
        results.add(createItinerary("Confort", 1.0f, prefs, pool, usedInResults));

        return results;
    }

    private static Itinerary createItinerary(String type, float budgetFactor, UserPreferences prefs, List<POI> database, List<POI> globalUsed) {
        List<POI> filtered = new ArrayList<>();
        float maxAllowedPrice = (prefs.maxBudget > 0) ? (prefs.maxBudget * budgetFactor) : Float.MAX_VALUE;

        // For "Économique", we really want low prices. For "Confort", we can take anything but prefer high quality/indoor.
        for (POI poi : database) {
            // Basic Filters
            if (!prefs.categories.isEmpty() && !prefs.categories.contains(poi.getCategory())) {
                continue;
            }
            
            if (poi.getEffortLevel() > prefs.effortLevel) {
                android.util.Log.d("FilterPOI", "Excluded " + poi.getName() + " - Effort too high: " + poi.getEffortLevel() + " > " + prefs.effortLevel);
                continue;
            }
            
            if (prefs.adaptedForPMR && !poi.isAdaptedForPMR()) {
                android.util.Log.d("FilterPOI", "Excluded " + poi.getName() + " - Not PMR adapted");
                continue;
            }
            
            if (prefs.adaptedForChildren && !poi.isAdaptedForChildren()) {
                android.util.Log.d("FilterPOI", "Excluded " + poi.getName() + " - Not child adapted");
                continue;
            }

            // Budget specific filter
            // Note: OSM doesn't often provide exact prices, so we estimate based on tags
            if (type.equals("Économique") && poi.getPrice() > (prefs.maxBudget * 0.2)) {
                continue;
            }
            
            // Avoid using exactly the same POIs as the previous generated itinerary in this session if we have enough choice
            // This ensures the 3 cards look different
            if (database.size() > 10 && globalUsed.contains(poi) && Math.random() > 0.2) continue;

            filtered.add(poi);
        }

        if (filtered.isEmpty()) {
            // Fallback to anything if too restrictive
            filtered.addAll(database.subList(0, Math.min(database.size(), 5)));
        }

        // Shuffle filtered list
        Collections.shuffle(filtered);
        
        // Vary the number of steps slightly to make them look different
        int baseCount = Math.max(2, (int) (prefs.durationHours / 1.5) + 1);
        int count;
        if (type.equals("Économique")) count = baseCount;
        else if (type.equals("Équilibré")) count = baseCount + 1;
        else count = baseCount + 2;
        
        count = Math.min(filtered.size(), count);
        List<POI> selected = new ArrayList<>(filtered.subList(0, count));

        globalUsed.addAll(selected);
        optimizeRoute(selected);

        double totalPrice = 0;
        for (POI p : selected) totalPrice += p.getPrice();

        Itinerary itinerary = new Itinerary(
            type,
            String.format("%.1f HR", selected.size() * 0.9 + (type.equals("Confort") ? 1.0 : 0)),
            String.format("%.1f KM", selected.size() * 0.8),
            totalPrice == 0 ? "GRATUIT" : String.format("%.2f €", totalPrice)
        );
        itinerary.steps.addAll(selected);
        
        return itinerary;
    }

    public static String buildOverpassQuery(double lat, double lon, double radiusMeters, List<String> categories) {
        StringBuilder query = new StringBuilder("[out:json][timeout:30];(");
        
        String around = "(around:" + radiusMeters + "," + lat + "," + lon + ")";

        if (categories.isEmpty() || categories.contains("culture")) {
            query.append("nwr[\"amenity\"~\"arts_centre|library\"]").append(around).append(";");
            query.append("nwr[\"tourism\"~\"museum|gallery\"]").append(around).append(";");
            query.append("nwr[\"historic\"]").append(around).append(";");
        }
        if (categories.isEmpty() || categories.contains("food")) {
            query.append("nwr[\"amenity\"~\"restaurant|cafe|bar|pub\"]").append(around).append(";");
        }
        if (categories.isEmpty() || categories.contains("loisir")) {
            query.append("nwr[\"leisure\"~\"park|garden|playground|stadium\"]").append(around).append(";");
            query.append("nwr[\"tourism\"~\"theme_park|zoo|aquarium\"]").append(around).append(";");
        }
        if (categories.isEmpty() || categories.contains("discover")) {
            query.append("nwr[\"tourism\"~\"viewpoint|attraction\"]").append(around).append(";");
            query.append("nwr[\"amenity\"=\"fountain\"]").append(around).append(";");
            query.append("nwr[\"natural\"=\"peak\"]").append(around).append(";");
        }
        
        query.append("); out center;");
        return query.toString();
    }

    public static POI fromOverpass(fds.hai811i.pathio.model.OverpassResponse.Element element) {
        String name = element.tags != null && element.tags.containsKey("name") ? element.tags.get("name") : "Lieu intéressant";
        String category = "discover";
        
        double lat = element.lat;
        double lon = element.lon;
        
        // Handle center for ways/relations
        if (lat == 0 && lon == 0 && element.center != null) {
            lat = element.center.lat;
            lon = element.center.lon;
        }

        double estimatedPrice = 0;
        int estimatedEffort = 1;

        if (element.tags != null) {
            String amenity = element.tags.get("amenity");
            String tourism = element.tags.get("tourism");
            String leisure = element.tags.get("leisure");
            
            if ("restaurant".equals(amenity)) {
                category = "food";
                estimatedPrice = 25.0;
                estimatedEffort = 1;
            } else if ("cafe".equals(amenity) || "bar".equals(amenity) || "pub".equals(amenity)) {
                category = "food";
                estimatedPrice = 12.0;
                estimatedEffort = 1;
            } else if ("museum".equals(tourism) || "gallery".equals(tourism)) {
                category = "culture";
                estimatedPrice = 12.0;
                estimatedEffort = 1;
            } else if (element.tags.containsKey("historic") || "arts_centre".equals(amenity)) {
                category = "culture";
                estimatedPrice = 5.0;
                estimatedEffort = 1;
            } else if ("park".equals(leisure) || "garden".equals(leisure)) {
                category = "loisir";
                estimatedPrice = 0.0;
                estimatedEffort = 2; // Walking in a park
            } else if ("theme_park".equals(tourism) || "zoo".equals(tourism) || "aquarium".equals(tourism)) {
                category = "loisir";
                estimatedPrice = 35.0;
                estimatedEffort = 3; // Long day, lots of walking
            } else if ("viewpoint".equals(tourism) || "attraction".equals(tourism)) {
                category = "discover";
                estimatedPrice = 0.0;
                estimatedEffort = 2;
            }

            // Refine if OSM has explicit fee tag
            if ("yes".equals(element.tags.get("fee"))) {
                if (estimatedPrice == 0) estimatedPrice = 10.0;
            } else if ("no".equals(element.tags.get("fee"))) {
                estimatedPrice = 0;
            }
        }

        boolean pmr = element.tags != null && ("yes".equals(element.tags.get("wheelchair")) || "limited".equals(element.tags.get("wheelchair")));
        boolean children = true;
        boolean indoor = element.tags != null && ("yes".equals(element.tags.get("indoor")) || "yes".equals(element.tags.get("covered")));

        return new POI(name, lat, lon, category, estimatedPrice, estimatedEffort, "Trouvé via OpenStreetMap", pmr, children, indoor);
    }

    /**
     * Simple Nearest Neighbor algorithm to make the route more logical
     */
    private static void optimizeRoute(List<POI> pois) {
        if (pois.size() <= 2) return;

        List<POI> optimized = new ArrayList<>();
        POI current = pois.remove(0);
        optimized.add(current);

        while (!pois.isEmpty()) {
            POI nearest = null;
            double minDistance = Double.MAX_VALUE;
            int nearestIdx = -1;

            for (int i = 0; i < pois.size(); i++) {
                double dist = calculateDistance(current, pois.get(i));
                if (dist < minDistance) {
                    minDistance = dist;
                    nearest = pois.get(i);
                    nearestIdx = i;
                }
            }

            optimized.add(nearest);
            current = nearest;
            pois.remove(nearestIdx);
        }
        pois.addAll(optimized);
    }

    private static double calculateDistance(POI p1, POI p2) {
        double latDiff = p1.getLatitude() - p2.getLatitude();
        double lonDiff = p1.getLongitude() - p2.getLongitude();
        return Math.sqrt(latDiff * latDiff + lonDiff * lonDiff);
    }
}
