package com.sdftdusername.pathfinding.mixins;

import finalforeach.cosmicreach.chat.commands.Command;
import finalforeach.cosmicreach.entities.Player;
import finalforeach.cosmicreach.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Command.class)
public interface CommandGetFields {
    @Accessor(value = "world")
    World getWorld();

    @Accessor(value = "player")
    Player getPlayer();
}
