/*
 * Copyright (c) 2019 CascadeBot. All rights reserved.
 * Licensed under the MIT license.
 */

package org.cascadebot.cascadebot.data.objects;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.utils.Checks;
import org.cascadebot.cascadebot.CascadeBot;
import org.cascadebot.cascadebot.Environment;
import org.cascadebot.cascadebot.permissions.CascadePermission;
import org.cascadebot.cascadebot.permissions.Security;
import org.cascadebot.cascadebot.permissions.objects.Group;
import org.cascadebot.cascadebot.permissions.objects.PermissionAction;
import org.cascadebot.cascadebot.permissions.objects.PermissionMode;
import org.cascadebot.cascadebot.permissions.objects.User;
import org.cascadebot.shared.SecurityLevel;
import spark.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GuildPermissions {

    private PermissionMode mode = PermissionMode.HIERARCHICAL;

    private List<Group> groups = Collections.synchronizedList(new ArrayList<>());
    private Map<Long, User> users = new ConcurrentHashMap<>();

    public boolean hasPermission(Member member, CascadePermission permission, GuildSettings settings) {
        return hasPermission(member, null, permission, settings);
    }

    public boolean hasPermission(Member member, Channel channel, CascadePermission permission, GuildSettings settings) {

        Checks.notNull(member, "member");
        Checks.notNull(permission, "permission");

        // This allows developers and owners to go into guilds and fix problems
        if (Security.isAuthorised(member.getUser().getIdLong(), SecurityLevel.DEVELOPER)) return true;
        if (Security.isAuthorised(member.getUser().getIdLong(), SecurityLevel.CONTRIBUTOR) && Environment.isDevelopment()) return true;
        // If the user is owner then they have all perms, obsv..
        if (member.isOwner()) return true;
        // By default all members with the administrator perm have access to all perms; this can be turned off
        if (member.hasPermission(Permission.ADMINISTRATOR) && settings.doAdminsHaveAllPerms()) return true;

        User user = users.computeIfAbsent(member.getUser().getIdLong(), id -> new User());
        // Get all user groups that are directly assigned and the groups assigned through roles
        List<Group> userGroups = getUserGroups(member);

        PermissionAction action = getDefaultAction(permission);
        PermissionAction evaluatedAction = PermissionAction.NEUTRAL;

        if (mode == PermissionMode.MOST_RESTRICTIVE) {
            evaluatedAction = evaluateMostRestrictiveMode(user, userGroups, permission);
        } else if (mode == PermissionMode.HIERARCHICAL) {
            evaluatedAction = evaluateHierarchicalMode(user, userGroups, permission);
        }

        if (evaluatedAction != PermissionAction.NEUTRAL) {
            action = evaluatedAction;
        }

        // Discord permissions will only allow a permission if is not already allowed or denied.
        // It will not override Cascade permissions!
        if (action == PermissionAction.NEUTRAL && hasDiscordPermissions(member, channel, permission.getDiscordPerm())) {
            action = PermissionAction.ALLOW;
        }

        return action == PermissionAction.ALLOW;
    }

    private boolean hasDiscordPermissions(Member member, Channel channel, Set<Permission> permissions) {
        if (CollectionUtils.isEmpty(permissions)) return false;
        if (channel != null) {
            return member.hasPermission(channel, permissions);
        } else {
            return member.hasPermission(permissions);
        }
    }

    private PermissionAction evaluateMostRestrictiveMode(User user, List<Group> userGroups, CascadePermission permission) {
        PermissionAction action = user.getPermissionAction(permission);
        if (action == PermissionAction.DENY) return action;

        for (Group group : userGroups) {
            PermissionAction groupAction = group.getPermissionAction(permission);
            // If the action is neutral, it has no effect on the existing action.
            if (groupAction == PermissionAction.NEUTRAL) continue;
            // This is most restrictive mode so if any group permissions is DENY, the evaluated action is DENY.
            if (groupAction == PermissionAction.DENY) return PermissionAction.DENY;
            action = groupAction;
        }
        return action;
    }

    private PermissionAction evaluateHierarchicalMode(User user, List<Group> userGroups, CascadePermission permission) {
        PermissionAction action = PermissionAction.NEUTRAL;
        // Loop through the groups backwards to preserve hierarchy; groups higher up override lower groups.
        for (int i = userGroups.size() - 1; i >= 0; i--) {
            PermissionAction groupAction = userGroups.get(i).getPermissionAction(permission);
            if (groupAction == PermissionAction.NEUTRAL) continue;
            // This overrides any previous action with no regard to what it was.
            action = groupAction;
        }

        PermissionAction userAction = user.getPermissionAction(permission);
        // User permissions take ultimate precedence over group permissions
        if (userAction != PermissionAction.NEUTRAL) {
            action = userAction;
        }

        return action;
    }

    private PermissionAction getDefaultAction(CascadePermission permission) {
        // A default permission will never explicitly deny a permission.
        return permission.isDefaultPerm() ? PermissionAction.ALLOW : PermissionAction.NEUTRAL;
    }

    public Group createGroup(String name) {
        Set<String> ids = groups.stream().map(Group::getId).collect(Collectors.toSet());
        Group group;
        int iterations = 0;
        do {
            group = new Group(name);
            if (++iterations == 7) {
                // If this happens then... run?
                CascadeBot.LOGGER.error("Somehow we couldn't manage to create a unique guild ID :(");
                throw new IllegalStateException("Could not create a group with a unique ID!");
            }
        } while (ids.contains(group.getId()));
        groups.add(group);
        return group;
    }

    public boolean deleteGroup(String id) {
        return groups.removeIf(group -> group.getId().equals(id));
    }

    public List<Group> getUserGroups(Member member) {
        User user = users.computeIfAbsent(member.getUser().getIdLong(), id -> new User());
        List<Group> userGroups = groups.stream().filter(group -> user.getGroupIds().contains(group.getId())).collect(Collectors.toList());

        // Now I know this is a mess... If you can figure out a better method hit me up 👀
        // This adds all the groups which have a id representing a role the member has.
        groups.stream()
                .filter(group -> group.getRoleIds().stream().anyMatch(id -> member.getRoles().contains(member.getGuild().getRoleById(id))))
                .forEach(userGroups::add);
        return userGroups;
    }

    public List<Group> getGroups() {
        return List.copyOf(groups);
    }

}
