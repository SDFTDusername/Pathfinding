package io.github.sdftdusername.pathfinding.commands;

import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.EntityCreator;
import io.github.sdftdusername.pathfinding.advancedcommand.AdvancedCommand;
import io.github.sdftdusername.pathfinding.advancedcommand.Argument;
import io.github.sdftdusername.pathfinding.entities.PathfinderEntity;

import java.util.Map;

public class CommandSpawn extends AdvancedCommand {
    @Override
    public void run(Map<String, String> args) {
        super.run(args);

        for (int i = 0; i < zone.allEntities.size; ++i) {
            Entity entity = zone.allEntities.get(i);
            if (entity.entityTypeId.equals(PathfinderEntity.ENTITY_TYPE_ID)) {
                entity.setPosition(player.getPosition());
                sendMessage("Teleported " + entity.entityTypeId);
                return;
            }
        }

        Entity pathfinder = EntityCreator.get(PathfinderEntity.ENTITY_TYPE_ID);
        pathfinder.setPosition(player.getPosition());
        zone.addEntity(pathfinder);

        sendMessage("Summoned " + PathfinderEntity.ENTITY_TYPE_ID);
    }

    @Override
    public Argument[] getArguments() {
        return new Argument[0];
    }

    @Override
    public String getCommandDescription() {
        return "Summons the pathfinder";
    }
}
