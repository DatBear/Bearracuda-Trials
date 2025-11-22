package com.datbear.data;

import java.util.List;

import net.runelite.api.coords.*;

public class TrialRoute {
    public TrialLocations Location;
    public TrialRanks Rank;
    public List<WorldPoint> Points;

    public TrialRoute(TrialLocations location, TrialRanks rank, List<WorldPoint> points) {
        Location = location;
        Rank = rank;
        Points = points;
    }
}
