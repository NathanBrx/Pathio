package fds.hai811i.pathio.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserPreferences implements Serializable {
    public List<String> categories = new ArrayList<>();
    public float minBudget;
    public float maxBudget;
    public float durationHours;
    public int effortLevel; // 1, 2, 3
    public boolean adaptedForOld;
    public boolean adaptedForChildren;
    public boolean adaptedForPMR;
    public boolean sensitiveToCold;
    public boolean sensitiveToHeat;
    public boolean sensitiveToHumidity;
    public String specificRequests;
}
