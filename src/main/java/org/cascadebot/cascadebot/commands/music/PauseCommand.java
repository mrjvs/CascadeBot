/*
 *
 *  * Copyright (c) 2019 CascadeBot. All rights reserved.
 *  * Licensed under the MIT license.
 *
 */

package org.cascadebot.cascadebot.commands.music;

import net.dv8tion.jda.core.entities.Member;
import org.cascadebot.cascadebot.commandmeta.CommandContext;
import org.cascadebot.cascadebot.commandmeta.ICommandMain;
import org.cascadebot.cascadebot.commandmeta.Module;
import org.cascadebot.cascadebot.permissions.CascadePermission;

public class PauseCommand implements ICommandMain {

    @Override
    public void onCommand(Member sender, CommandContext context) {
        if (context.getMusicPlayer().getPlayer().isPaused()) {
            context.getTypedMessaging().replyDanger("Music has already been paused! Use **" + context.getSettings().getPrefix() + "resume** to resume.");
        } else {
            context.getMusicPlayer().getPlayer().setPaused(true);
            context.getTypedMessaging().replySuccess("Paused! Use **" + context.getSettings().getPrefix() + "resume** to resume.");
        }
    }

    @Override
    public Module getModule() {
        return Module.MUSIC;
    }

    @Override
    public String command() {
        return "pause";
    }

    @Override
    public CascadePermission getPermission() {
        return CascadePermission.of("Pause command", "pause", true);
    }

    @Override
    public String description() {
        return "Pauses the currently playing track";
    }

}
