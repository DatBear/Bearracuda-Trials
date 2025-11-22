package com.datbear.data;

import net.runelite.api.coords.WorldPoint;

public class BoostLocation {
    public int GameObjectId;
    public WorldPoint Location;

    public BoostLocation(int gameObjectId, WorldPoint location) {
        GameObjectId = gameObjectId;
        Location = location;
    }
}
