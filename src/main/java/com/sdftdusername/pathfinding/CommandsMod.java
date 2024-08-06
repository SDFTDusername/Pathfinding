package com.sdftdusername.pathfinding;

import com.sdftdusername.pathfinding.commands.*;
import finalforeach.cosmicreach.chat.commands.Command;

public class CommandsMod {
    public static void RegisterCommands() {
        PathfindingMod.LOGGER.info("Registering commands");
        Command.registerCommand("start", CommandStart::new);
        Command.registerCommand("stop", CommandStop::new);
        Command.registerCommand("spawn", CommandSpawn::new);
        Command.registerCommand("follow", CommandFollow::new);
        Command.registerCommand("stopfollow", CommandStopFollow::new);
    }
}
