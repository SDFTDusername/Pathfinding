package com.sdftdusername.pathfinding.mixins;

import finalforeach.cosmicreach.rendering.entities.EntityModelInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.HashMap;

@Mixin(EntityModelInstance.class)
public interface EntityModelInstanceGetBoneMap {
    @Accessor(value = "boneMap")
    HashMap getBoneMap();
}
