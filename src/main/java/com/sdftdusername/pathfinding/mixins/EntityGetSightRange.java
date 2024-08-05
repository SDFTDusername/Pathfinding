package com.sdftdusername.pathfinding.mixins;

import finalforeach.cosmicreach.entities.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityGetSightRange {
    @Accessor(value = "sightRange")
    float getSightRange();
}
