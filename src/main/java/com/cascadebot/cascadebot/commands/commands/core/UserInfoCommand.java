package com.cascadebot.cascadebot.commands.commands.core;

import com.cascadebot.cascadebot.commands.ICommand;
import com.cascadebot.cascadebot.commands.CommandContext;
import com.cascadebot.cascadebot.commands.CommandType;
import net.dv8tion.jda.core.entities.Member;

public class UserInfoCommand implements ICommand {
    @Override
    public void onCommand(Member sender, CommandContext context) {

    }

    @Override
    public String defaultCommand() {
        return "userinfo";
    }

    @Override
    public CommandType getType() {
        return null;
    }
}
