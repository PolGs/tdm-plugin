package com.example.tdm.commands;

import com.example.tdm.TeamManager;
import com.example.tdm.WorldUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class BackCommand extends AbstractPlayerCommand {

    public BackCommand() {
        super("back", "Return to the lobby");
    }

    @Override
    protected void execute(
        @Nonnull CommandContext context,
        @Nonnull Store<EntityStore> store,
        @Nonnull Ref<EntityStore> ref,
        @Nonnull PlayerRef playerRef,
        @Nonnull World world
    ) {
        // Remove from team
        TeamManager.removePlayer(playerRef.getUuid());

        // Teleport to lobby world
        WorldUtil.teleportToWorld(playerRef, "lobby");
        playerRef.sendMessage(Message.raw("Returning to lobby..."));
    }
}
