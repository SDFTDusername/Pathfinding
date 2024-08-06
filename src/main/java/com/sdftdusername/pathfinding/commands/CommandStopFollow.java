package com.sdftdusername.pathfinding.commands;

import com.sdftdusername.pathfinding.advanced_command.AdvancedCommand;
import com.sdftdusername.pathfinding.advanced_command.Argument;

import java.util.Map;

public class CommandStopFollow extends AdvancedCommand {
    public static boolean stop = false;

    @Override
    public void run(Map<String, String> args) {
        super.run(args);

        if (CommandStart.busy) {
            sendError("Pathfinding is busy");
            return;
        }

        if (stop) {
            sendMessage("The pathfinder is already stopping");
            return;
        }

        if (!CommandFollow.follow) {
            sendError("The pathfinder is not following yet");
            return;
        }

        stop = true;
    }

    @Override
    public Argument[] getArguments() {
        return new Argument[0];
    }

    @Override
    public String getCommandDescription() {
        return "Stops the pathfinder from following";
    }
}
