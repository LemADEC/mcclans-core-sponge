/*
 * Copyright (c) 2016 riebie, Kippers <https://bitbucket.org/Kippers/mcclans-core-sponge>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package nl.riebie.mcclans.commands.implementations;

import nl.riebie.mcclans.api.enums.Permission;
import nl.riebie.mcclans.api.enums.PermissionModifyResponse;
import nl.riebie.mcclans.clan.ClanImpl;
import nl.riebie.mcclans.clan.RankImpl;
import nl.riebie.mcclans.commands.annotations.Command;
import nl.riebie.mcclans.commands.annotations.Multiline;
import nl.riebie.mcclans.commands.annotations.PageParameter;
import nl.riebie.mcclans.commands.annotations.Parameter;
import nl.riebie.mcclans.messages.Messages;
import nl.riebie.mcclans.player.ClanPlayerImpl;
import nl.riebie.mcclans.table.HorizontalTable;
import nl.riebie.mcclans.table.TableAdapter;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

import java.util.List;

/**
 * Created by riebie on 27/02/2016.
 */
public class ClanRankPermissionCommands {

    @Command(name = "add", description = "Adds the given permissions to a rank", isPlayerOnly = true, isClanOnly = true, clanPermission = Permission.rank, spongePermission = "mcclans.user.rank.permission.add")
    public void clanRankPermissionAddCommand(CommandSource sender, ClanPlayerImpl clanPlayer, @Parameter(name = "rankName") String rankName,
                                             @Multiline(listType = Permission.class) @Parameter(name = "permissions") List<Permission> permissions) {

        ClanImpl clan = clanPlayer.getClan();
        if (clan == null) {
            Messages.sendWarningMessage(sender, Messages.YOU_ARE_NOT_IN_A_CLAN);
            return;
        }
        RankImpl rank = clan.getRank(rankName);
        if (rank == null) {
            Messages.sendWarningMessage(sender, Messages.RANK_DOES_NOT_EXIST);
            return;
        }
        if (!rank.isChangeable()) {
            Messages.sendWarningMessage(sender, Messages.RANK_IS_NOT_CHANGEABLE);
            return;
        }

        Messages.sendRankSuccessfullyModified(sender, rankName);
        for (Permission permission : permissions) {
            PermissionModifyResponse response = rank.addPermission(permission);

            switch (response) {
                case ALREADY_CONTAINS_PERMISSION:
                    Messages.sendAddingPermissionFailedRankAlreadyHasThisPermission(sender, permission.name());
                    break;
                case SUCCESSFULLY_MODIFIED:
                    Messages.sendSuccessfullyAddedThisPermission(sender, permission.name());
                    break;
                default:
                    break;
            }
        }
    }

    @Command(name = "set", description = "Sets the given permissions to a rank", isPlayerOnly = true, isClanOnly = true, clanPermission = Permission.rank, spongePermission = "mcclans.user.rank.permission.set")
    public void clanRankPermissionSetCommand(CommandSource sender, ClanPlayerImpl clanPlayer, @Parameter(name = "rankName") String rankName,
                                             @Multiline(listType = Permission.class) @Parameter(name = "permissions") List<Permission> permissions) {
        ClanImpl clan = clanPlayer.getClan();
        if (clan == null) {
            Messages.sendWarningMessage(sender, Messages.YOU_ARE_NOT_IN_A_CLAN);
            return;
        }
        RankImpl rank = clan.getRank(rankName);
        if (rank == null) {
            Messages.sendWarningMessage(sender, Messages.RANK_DOES_NOT_EXIST);
            return;
        }
        if (!rank.isChangeable()) {
            Messages.sendWarningMessage(sender, Messages.RANK_IS_NOT_CHANGEABLE);
            return;
        }

        Messages.sendRankSuccessfullyModified(sender, rankName);

        List<Permission> oldPermissions = rank.getPermissions();

        for (Permission permission : oldPermissions) {
            rank.removePermission(permission.name());
        }


        for (Permission permission : permissions) {
            rank.addPermission(permission);
            Messages.sendSuccessfullySetThisPermission(sender, permission.name());
        }
    }

    @Command(name = "remove", description = "Removes the given permissions from a rank", isPlayerOnly = true, isClanOnly = true, clanPermission = Permission.rank, spongePermission = "mcclans.user.rank.permission.remove")
    public void canPermissionRemoveCommand(CommandSource sender, ClanPlayerImpl clanPlayer, @Parameter(name = "rankName") String rankName,
                                           @Multiline(listType = Permission.class) @Parameter(name = "permissions") List<Permission> permissions) {
        ClanImpl clan = clanPlayer.getClan();
        if (clan == null) {
            Messages.sendWarningMessage(sender, Messages.YOU_ARE_NOT_IN_A_CLAN);
            return;
        }
        RankImpl rank = clan.getRank(rankName);
        if (rank == null) {
            Messages.sendWarningMessage(sender, Messages.RANK_DOES_NOT_EXIST);
            return;
        }
        if (!rank.isChangeable()) {
            Messages.sendWarningMessage(sender, Messages.RANK_IS_NOT_CHANGEABLE);
            return;
        }

        Messages.sendRankSuccessfullyModified(sender, rankName);

        for (Permission permission : permissions) {
            PermissionModifyResponse response = rank.removePermission(permission);

            switch (response) {
                case DOES_NOT_CONTAIN_PERMISSION:
                    Messages.sendRemovingPermissionFailedRankDoesNotHaveThisPermission(sender, permission.name());
                    break;
                case SUCCESSFULLY_MODIFIED:
                    Messages.sendSuccessfullyRemovedThisPermission(sender, permission.name());
                    break;
                default:
                    break;
            }
        }
    }

    @Command(name = "view", description = "View all available permissions", spongePermission = "mcclans.user.rank.permission.view")
    public void clanPermissonViewCommand(CommandSource sender, @PageParameter int page) {
        HorizontalTable<Permission> table = new HorizontalTable<>("Permissions", 10,
                (TableAdapter<Permission>) (row, permission, index) -> {
                    row.setValue("Permission", Text.of(permission.name()));
                    row.setValue("Description", Text.of(permission.getDescription()));
                });
        table.defineColumn("Permission", 20);
        table.defineColumn("Description", 20);

        List<Permission> permissions = Permission.getUsablePermissions();

        table.draw(permissions, page, sender);
    }
}
