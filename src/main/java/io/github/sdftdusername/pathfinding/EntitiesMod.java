package io.github.sdftdusername.pathfinding;

import finalforeach.cosmicreach.entities.EntityCreator;
import io.github.sdftdusername.pathfinding.entities.PathfinderEntity;

public class EntitiesMod {
    public static void RegisterEntities() {
        PathfindingMod.LOGGER.info("Registering entities");
        EntityCreator.registerEntityCreator(PathfinderEntity.ENTITY_TYPE_ID, PathfinderEntity::new);
    }
}
