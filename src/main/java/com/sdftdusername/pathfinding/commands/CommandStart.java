package com.sdftdusername.pathfinding.commands;

import com.badlogic.gdx.math.Vector3;
import com.sdftdusername.pathfinding.advanced_command.AdvancedCommand;
import com.sdftdusername.pathfinding.advanced_command.Argument;
import com.sdftdusername.pathfinding.advanced_command.ArgumentDataType;
import com.sdftdusername.pathfinding.mixins.CommandGetFields;

import java.util.Map;

public class CommandStart extends AdvancedCommand {
    public static Vector3 queuePosition = Vector3.Zero;
    public static boolean positionInQueue = false;
    public static boolean busy = false;

    public static boolean spawnWaypointItems = false;
    public static boolean moveToWaypoints = false;

    @Override
    public void run(Map<String, String> args) {
        super.run(args);

        if (CommandFollow.follow || CommandStopFollow.stop) {
            sendError("The pathfinder is already following");
            return;
        }

        if (busy) {
            sendError("Pathfinding already started");
            return;
        }

        spawnWaypointItems = valueToBoolean(args.get("spawnWaypointItems"));
        moveToWaypoints = valueToBoolean(args.get("moveToWaypoints"));

        queuePosition = ((CommandGetFields)this).getPlayer().getEntity().position;
        positionInQueue = true;
    }

    @Override
    public Argument[] getArguments() {
        return new Argument[] {
            new Argument("spawnWaypointItems", "swi", ArgumentDataType.BOOLEAN, "0"),
            new Argument("moveToWaypoints", "mtw", ArgumentDataType.BOOLEAN, "1")
        };
    }

    @Override
    public String getCommandDescription() {
        return "Starts pathfinding";
    }
}