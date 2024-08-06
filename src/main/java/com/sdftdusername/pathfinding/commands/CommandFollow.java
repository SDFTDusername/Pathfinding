package com.sdftdusername.pathfinding.commands;

import com.sdftdusername.pathfinding.advanced_command.AdvancedCommand;
import com.sdftdusername.pathfinding.advanced_command.Argument;
import finalforeach.cosmicreach.entities.Entity;

import java.util.Map;

public class CommandFollow extends AdvancedCommand {
    public static boolean follow = false;
    public static Entity target;

    @Override
    public void run(Map<String, String> args) {
        super.run(args);

        if (CommandStart.busy) {
            sendError("Pathfinding is busy");
            return;
        }

        if (CommandStopFollow.stop) {
            sendError("The pathfinder is stopping");
            return;
        }

        if (follow) {
            sendError("The pathfinder is already following");
            return;
        }

        target = player.getEntity();
        follow = true;
    }

    @Override
    public Argument[] getArguments() {
        return new Argument[0];
    }

    @Override
    public String getCommandDescription() {
        return "Makes the pathfinder follow the player";
    }
}
