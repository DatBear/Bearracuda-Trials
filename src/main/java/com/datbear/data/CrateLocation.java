package com.datbear.data;

import net.runelite.api.coords.WorldPoint;

public class CrateLocation {
    public int GameObjectId;
    public WorldPoint Location;
    public TrialLocations TrialLocation;
    public TrialRanks MinimumRank;

    public CrateLocation(int gameObjectId, WorldPoint location, TrialLocations trialLocation, TrialRanks minimumRank) {
        GameObjectId = gameObjectId;
        Location = location;
        TrialLocation = trialLocation;
        MinimumRank = minimumRank;
    }
}
