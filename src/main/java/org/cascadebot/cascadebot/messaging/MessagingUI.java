/*
 * Copyright (c) 2019 CascadeBot. All rights reserved.
 * Licensed under the MIT license.
 */

package org.cascadebot.cascadebot.messaging;

import org.cascadebot.cascadebot.commandmeta.CommandContext;
import org.cascadebot.cascadebot.commandmeta.CommandException;
import org.cascadebot.cascadebot.commandmeta.ICommandExecutable;
import org.cascadebot.cascadebot.permissions.CascadePermission;
import org.cascadebot.cascadebot.utils.buttons.ButtonGroup;
import org.cascadebot.cascadebot.utils.pagination.Page;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.requests.RequestFuture;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class MessagingUI {

    private CommandContext context;

    public MessagingUI(CommandContext context) {
        this.context = context;
    }

    /**
     * Sends an image to the channel in the context
     * When embeds are on, it embeds the image into the embed.
     * When embeds are off, it downloads the image and sends it via the {@link net.dv8tion.jda.core.entities.TextChannel#sendFile(File)} method
     *
     * @param url The url of the image.
     * @return A {@link RequestFuture<Message>} so you can interact with the message after it sends.
     */
    public RequestFuture<Message> replyImage(String url) {
        if (context.getSettings().useEmbedForMessages()) {
            EmbedBuilder embedBuilder = MessagingObjects.getClearThreadLocalEmbedBuilder();
            embedBuilder.setImage(url);
            return context.getChannel().sendMessage(embedBuilder.build()).submit();
        } else {
            String[] split = url.split("/");
            try {
                return context.getChannel().sendFile(new URL(url).openStream(), split[split.length - 1]).submit();
            } catch (IOException e) {
                return Messaging.sendExceptionMessage(context.getChannel(), "Error loading image", new CommandException(e, context.getGuild(), context.getTrigger()));
            }
        }
    }

    /**
     * Sends a message with reactions to function as "buttons".
     *
     * @see ButtonGroup
     *
     * @param message The String message to send.
     * @param group   The {@link ButtonGroup} to use for buttons.
     */
    public void sendButtonedMessage(String message, ButtonGroup group) {
        Messaging.sendButtonedMessage(context.getChannel(), message, group);
    }

    /**
     * Sends a message with reactions to function as "buttons".
     *
     * @see ButtonGroup
     *
     * @param embed The {@link MessageEmbed} to use as the message.
     * @param group The {@link ButtonGroup} to use for buttons.
     */
    public void sendButtonedMessage(MessageEmbed embed, ButtonGroup group) {
        Messaging.sendButtonedMessage(context.getChannel(), embed, group);
    }

    /**
     * Sends a message with reactions to function as "buttons".
     *
     * @see ButtonGroup
     *
     * @param message The message to send.
     * @param group   The {@link ButtonGroup} to use for buttons.
     */
    public void sendButtonedMessage(Message message, ButtonGroup group) {
        Messaging.sendButtonedMessage(context.getChannel(), message, group);
    }

    /**
     * Sends a pages message with buttons for page navigation.
     *
     * @see org.cascadebot.cascadebot.utils.pagination.PageObjects.EmbedPage
     * @see org.cascadebot.cascadebot.utils.pagination.PageObjects.StringPage
     * @see org.cascadebot.cascadebot.utils.pagination.PageObjects.TablePage
     *
     * @param pages The list of pages to use. see {@link org.cascadebot.cascadebot.utils.pagination.PageObjects.EmbedPage}, {@link org.cascadebot.cascadebot.utils.pagination.PageObjects.StringPage}, and {@link org.cascadebot.cascadebot.utils.pagination.PageObjects.TablePage}.
     */
    public void sendPagedMessage(List<Page> pages) {
        Messaging.sendPagedMessage(context.getChannel(), context.getMember(), pages);
    }

    /**
     * Sends a permission error.
     *
     * @param permission The Cascade Permission that they don't have
     */
    public void sendPermissionsError(CascadePermission permission) {
        context.getTypedMessaging().replyDanger("You don't have the permission `%s` to do this!", permission.getPermissionNode());
    }

    public void replyUsage(ICommandExecutable command) {
        replyUsage(command, null);
    }

    public void replyUsage(ICommandExecutable command, String parent) {
        context.getTypedMessaging().replyWarning("Incorrect usage. Proper usage:\n" + context.getUsage(command, parent));
    }
}
