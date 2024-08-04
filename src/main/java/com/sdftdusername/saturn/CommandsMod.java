package com.sdftdusername.saturn;

import com.sdftdusername.saturn.commands.CommandLoad;
import com.sdftdusername.saturn.commands.CommandSave;
import com.sdftdusername.saturn.commands.CommandStart;
import finalforeach.cosmicreach.chat.commands.Command;

public class CommandsMod {
    public static void RegisterCommands() {
        SaturnMod.LOGGER.info("Registering commands");
        Command.registerCommand("save", CommandSave::new);
        Command.registerCommand("load", CommandLoad::new);
        Command.registerCommand("start", CommandStart::new);
    }
}
