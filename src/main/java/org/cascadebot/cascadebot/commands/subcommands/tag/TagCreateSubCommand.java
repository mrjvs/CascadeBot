/*

  * Copyright (c) 2019 CascadeBot. All rights reserved.
  * Licensed under the MIT license.

 */

package org.cascadebot.cascadebot.commands.subcommands.tag;

import net.dv8tion.jda.core.entities.Member;
import org.cascadebot.cascadebot.commandmeta.CommandContext;
import org.cascadebot.cascadebot.commandmeta.ICommandExecutable;
import org.cascadebot.cascadebot.data.objects.GuildData;
import org.cascadebot.cascadebot.data.objects.Tag;
import org.cascadebot.cascadebot.permissions.CascadePermission;

public class TagCreateSubCommand implements ICommandExecutable {

    @Override
    public void onCommand(Member sender, CommandContext context) {
        context.getData();
    }

    @Override
    public String command() {
        return null;
    }

    @Override
    public CascadePermission getPermission() {
        return null;
    }

    @Override
    public String description() {
        return null;
    }

}
