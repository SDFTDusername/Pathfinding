package com.sdftdusername.pathfinding.commands;

import finalforeach.cosmicreach.chat.Chat;
import finalforeach.cosmicreach.chat.commands.Command;

public class CommandStop extends Command {
    public static boolean stop = false;

    @Override
    public void run(Chat chat, String[] args) {
        super.run(chat, args);

        stop = true;
    }

    @Override
    public String getDescription() {
        return "Stop the pathfinding";
    }
}