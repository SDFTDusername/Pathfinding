package io.github.sdftdusername.pathfinding;

import finalforeach.cosmicreach.chat.commands.Command;
import io.github.sdftdusername.pathfinding.commands.*;

public class CommandsMod {
    public static void RegisterCommands() {
        PathfindingMod.LOGGER.info("Registering commands");
        Command.registerCommand(CommandStart::new, "start");
        Command.registerCommand(CommandStop::new, "stop");
        Command.registerCommand(CommandSpawn::new, "spawn");
        Command.registerCommand(CommandFollow::new, "follow");
        Command.registerCommand(CommandStopFollow::new, "stopfollow");
    }
}
