package fds.hai811i.pathio;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;
import fds.hai811i.pathio.model.Itinerary;
import fds.hai811i.pathio.model.UserPreferences;
import fds.hai811i.pathio.utils.ItineraryGenerator;

public class ItineraryGeneratorTest {

    @Test
    public void testGenerationFiltersByCategory() {
        UserPreferences prefs = new UserPreferences();
        prefs.categories.add("culture");
        prefs.effortLevel = 3;
        prefs.durationHours = 4f;

        List<Itinerary> results = ItineraryGenerator.generate(prefs);

        assertNotNull(results);
        assertEquals(3, results.size()); // Should always return 3 types

        for (Itinerary itinerary : results) {
            assertFalse(itinerary.steps.isEmpty());
            for (var poi : itinerary.steps) {
                assertEquals("culture", poi.getCategory());
            }
        }
    }

    @Test
    public void testGenerationFiltersByEffort() {
        UserPreferences prefs = new UserPreferences();
        prefs.effortLevel = 1; // Low effort only
        prefs.durationHours = 8f;

        List<Itinerary> results = ItineraryGenerator.generate(prefs);

        for (Itinerary itinerary : results) {
            for (var poi : itinerary.steps) {
                assertTrue(poi.getEffortLevel() <= 1);
            }
        }
    }
}
