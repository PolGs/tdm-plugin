package com.example.tdm.pages;

import com.example.tdm.TeamManager;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class LoadoutPage extends InteractiveCustomUIPage<LoadoutPage.TeamSelectEventData> {

    public static class TeamSelectEventData {
        public String action;

        public static final BuilderCodec<TeamSelectEventData> CODEC = ((BuilderCodec.Builder<TeamSelectEventData>)
            BuilderCodec.builder(TeamSelectEventData.class, TeamSelectEventData::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (TeamSelectEventData o, String v) -> o.action = v, (TeamSelectEventData o) -> o.action)
                .add())
            .build();
    }

    public LoadoutPage(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, TeamSelectEventData.CODEC);
    }

    @Override
    public void build(
        @Nonnull Ref<EntityStore> ref,
        @Nonnull UICommandBuilder commandBuilder,
        @Nonnull UIEventBuilder eventBuilder,
        @Nonnull Store<EntityStore> store
    ) {
        commandBuilder.append("Pages/LoadoutMenu.ui");

        // Update team counts
        String counts = "Red: " + TeamManager.getRedCount() + " | Blue: " + TeamManager.getBlueCount();
        commandBuilder.set("#TeamCounts.Text", counts);

        // Bind click events
        eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#RedButton",
            new EventData().append("Action", "Red")
        );

        eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#BlueButton",
            new EventData().append("Action", "Blue")
        );

        eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#AutoButton",
            new EventData().append("Action", "Auto")
        );

        eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#CloseButton",
            new EventData().append("Action", "Close")
        );
    }

    @Override
    public void handleDataEvent(
        @Nonnull Ref<EntityStore> ref,
        @Nonnull Store<EntityStore> store,
        @Nonnull TeamSelectEventData data
    ) {
        Player player = (Player) store.getComponent(ref, Player.getComponentType());

        switch (data.action) {
            case "Red":
                TeamManager.joinTeam(playerRef, store, ref, TeamManager.Team.RED);
                player.getPageManager().setPage(ref, store, Page.None);
                break;

            case "Blue":
                TeamManager.joinTeam(playerRef, store, ref, TeamManager.Team.BLUE);
                player.getPageManager().setPage(ref, store, Page.None);
                break;

            case "Auto":
                TeamManager.autoJoinTeam(playerRef, store, ref);
                player.getPageManager().setPage(ref, store, Page.None);
                break;

            case "Close":
                player.getPageManager().setPage(ref, store, Page.None);
                break;

            default:
                break;
        }
    }
}
