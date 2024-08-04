package com.sdftdusername.saturn.commands;

import com.badlogic.gdx.math.Vector3;
import com.sdftdusername.saturn.mixins.CommandGetFields;
import finalforeach.cosmicreach.chat.Chat;
import finalforeach.cosmicreach.chat.commands.Command;

public class CommandStart extends Command {
    public static Vector3 queuePosition = Vector3.Zero;
    public static boolean positionInQueue = false;
    public static boolean busy = false;

    @Override
    public void run(Chat chat, String[] args) {
        super.run(chat, args);

        if (busy) {
            commandError("Pathfinding already started");
            return;
        }

        queuePosition = ((CommandGetFields)this).getPlayer().getEntity().position;
        positionInQueue = true;
    }

    @Override
    public String getDescription() {
        return "Start the pathfinding";
    }
}