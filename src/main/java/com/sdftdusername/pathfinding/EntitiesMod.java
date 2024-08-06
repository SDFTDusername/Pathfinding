package com.sdftdusername.pathfinding;

import com.sdftdusername.pathfinding.entities.PathfinderEntity;
import finalforeach.cosmicreach.entities.EntityCreator;

public class EntitiesMod {
    public static void RegisterEntities() {
        PathfindingMod.LOGGER.info("Registering entities");
        EntityCreator.registerEntityCreator(PathfinderEntity.ENTITY_TYPE_ID, PathfinderEntity::new);
    }
}
