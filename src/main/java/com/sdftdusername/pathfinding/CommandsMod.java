package com.sdftdusername.pathfinding;

import com.sdftdusername.pathfinding.commands.CommandStart;
import finalforeach.cosmicreach.chat.commands.Command;

public class CommandsMod {
    public static void RegisterCommands() {
        PathfindingMod.LOGGER.info("Registering commands");
        Command.registerCommand("start", CommandStart::new);
    }
}
