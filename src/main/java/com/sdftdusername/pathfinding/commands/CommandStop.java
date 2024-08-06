package com.sdftdusername.pathfinding.commands;

import com.sdftdusername.pathfinding.advanced_command.AdvancedCommand;
import com.sdftdusername.pathfinding.advanced_command.Argument;

import java.util.Map;

public class CommandStop extends AdvancedCommand {
    public static boolean stop = false;

    @Override
    public void run(Map<String, String> args) {
        super.run(args);
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