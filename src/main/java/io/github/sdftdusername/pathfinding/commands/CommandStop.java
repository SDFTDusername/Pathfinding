package io.github.sdftdusername.pathfinding.commands;

import io.github.sdftdusername.pathfinding.advancedcommand.AdvancedCommand;
import io.github.sdftdusername.pathfinding.advancedcommand.Argument;

import java.util.Map;

public class CommandStop extends AdvancedCommand {
    public static boolean stop = false;

    @Override
    public void run(Map<String, String> args) {
        super.run(args);

        if (CommandFollow.follow || CommandStopFollow.stop) {
            sendError("The pathfinder is following");
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
        return "Stops pathfinding";
    }
}