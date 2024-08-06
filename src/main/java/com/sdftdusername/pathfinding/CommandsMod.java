package com.sdftdusername.pathfinding;

import com.sdftdusername.pathfinding.commands.CommandSpawn;
import com.sdftdusername.pathfinding.commands.CommandStart;
import com.sdftdusername.pathfinding.commands.CommandStop;
import finalforeach.cosmicreach.chat.commands.Command;

public class CommandsMod {
    public static void RegisterCommands() {
        PathfindingMod.LOGGER.info("Registering commands");
        Command.registerCommand("start", CommandStart::new);
        Command.registerCommand("stop", CommandStop::new);
        Command.registerCommand("spawn", CommandSpawn::new);
    }
}
