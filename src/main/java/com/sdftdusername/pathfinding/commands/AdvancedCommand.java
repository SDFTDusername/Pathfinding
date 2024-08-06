package com.sdftdusername.pathfinding.commands;

import com.sdftdusername.pathfinding.mixins.CommandGetFields;
import finalforeach.cosmicreach.chat.Chat;
import finalforeach.cosmicreach.chat.commands.Command;
import finalforeach.cosmicreach.entities.Player;
import finalforeach.cosmicreach.world.World;

import java.util.*;

public abstract class AdvancedCommand extends Command {
    public World world;
    public Player player;
    public Chat chat;

    @Override
    public void run(Chat chat, String[] args) {
        world = getWorld();
        player = getPlayer();
        this.chat = chat;

        Argument[] arguments = getArguments();
        Map<String, String> values = new HashMap<>();

        List<String> unknownArguments = new ArrayList<>();

        for (int i = 1; i < args.length; ++i) {
            String arg = args[i];

            int equalsIndex = arg.indexOf('=');

            String name;
            String value;

            if (equalsIndex != -1) {
                name = arg.substring(0, equalsIndex);
                value = arg.substring(equalsIndex + 1);
            } else {
                name = arg;
                value = "";
            }

            boolean foundArgument = false;

            for (Argument argument : arguments) {
                if (name.equalsIgnoreCase(argument.fullName) || name.equalsIgnoreCase(argument.shortName)) {
                    if (values.containsKey(argument.fullName))
                        values.replace(argument.fullName, value);
                    else
                        values.put(argument.fullName, value);

                    foundArgument = true;
                    break;
                }
            }

            if (!foundArgument) {
                int index = unknownArguments.stream()
                        .map(String::toLowerCase)
                        .toList()
                        .indexOf(name.toLowerCase());

                if (index == -1)
                    unknownArguments.add(name);
                else
                    unknownArguments.set(index, name);
            }
        }

        List<Argument> missingArguments = new ArrayList<>();

        for (Argument argument : arguments) {
            if (!values.containsKey(argument.fullName)) {
                if (argument.optional)
                    values.put(argument.fullName, argument.defaultValue);
                else
                    missingArguments.add(argument);
            } else if (values.get(argument.fullName).isEmpty()) {
                missingArguments.add(argument);
            }
        }

        boolean error = false;

        if (!missingArguments.isEmpty()) {
            for (Argument argument : missingArguments)
                sendError("No value for " + argument.fullName + " (" + argument.shortName + ")");
            error = true;
        }

        if (!unknownArguments.isEmpty()) {
            for (String argument : unknownArguments)
                sendError("Unknown argument " + argument);
            error = true;
        }

        if (error)
            return;

        run(values);
    }

    public void run(Map<String, String> args) {
        for (String key : args.keySet()) {
            String value = args.get(key);
            sendMessage(key + ": " + value);
        }
    }

    public boolean valueToBoolean(String value) {
        return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("1");
    }

    public double valueToNumber(String value, double defaultValue) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public abstract Argument[] getArguments();

    public abstract String getCommandDescription();

    public abstract String getCommandName();

    public void sendMessage(String message) {
        chat.sendMessage(world, player, null, message);
    }

    public void sendError(String message) {
        chat.sendMessage(world, player, null, "ERROR: " + message);
    }

    private World getWorld() {
        return ((CommandGetFields)this).getWorld();
    }

    private Player getPlayer() {
        return ((CommandGetFields)this).getPlayer();
    }

    @Override
    public String getDescription() {
        StringBuilder description = new StringBuilder(getCommandDescription());
        Argument[] arguments = getArguments();

        if (arguments.length == 0)
            description.append(" No arguments.");
        else
            description.append(" Arguments: ");

        for (Argument argument : arguments)
            description.append(" ").append(argument.toString());

        return description.toString();
    }
}
