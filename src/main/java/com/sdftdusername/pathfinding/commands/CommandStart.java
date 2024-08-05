package com.sdftdusername.pathfinding.commands;

import com.badlogic.gdx.math.Vector3;
import com.sdftdusername.pathfinding.mixins.CommandGetFields;
import finalforeach.cosmicreach.chat.Chat;
import finalforeach.cosmicreach.chat.commands.Command;

public class CommandStart extends Command {
    public static Vector3 queuePosition = Vector3.Zero;
    public static boolean positionInQueue = false;
    public static boolean busy = false;

    public static boolean spawnWaypointItems = false;
    public static boolean moveToWaypoints = false;

    @Override
    public void run(Chat chat, String[] args) {
        super.run(chat, args);

        if (busy) {
            commandError("Pathfinding already started");
            return;
        }

        if (args.length >= 2)
            spawnWaypointItems = args[1].equalsIgnoreCase("true");
        else
            spawnWaypointItems = false;

        if (args.length >= 3)
            moveToWaypoints = args[2].equalsIgnoreCase("true");
        else
            moveToWaypoints = true;

        queuePosition = ((CommandGetFields)this).getPlayer().getEntity().position;
        positionInQueue = true;
    }

    @Override
    public String getDescription() {
        return "Start the pathfinding";
    }
}