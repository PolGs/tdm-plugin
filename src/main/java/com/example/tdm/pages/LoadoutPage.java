package com.example.tdm.pages;

import com.example.tdm.LoadoutManager;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class LoadoutPage extends InteractiveCustomUIPage<LoadoutPage.LoadoutEventData> {

    public static class LoadoutEventData {
        public String action;

        public static final BuilderCodec<LoadoutEventData> CODEC = ((BuilderCodec.Builder<LoadoutEventData>)
            BuilderCodec.builder(LoadoutEventData.class, LoadoutEventData::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (LoadoutEventData o, String v) -> o.action = v, (LoadoutEventData o) -> o.action)
                .add())
            .build();
    }

    public LoadoutPage(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, LoadoutEventData.CODEC);
    }

    @Override
    public void build(
        @Nonnull Ref<EntityStore> ref,
        @Nonnull UICommandBuilder commandBuilder,
        @Nonnull UIEventBuilder eventBuilder,
        @Nonnull Store<EntityStore> store
    ) {
        commandBuilder.append("Pages/LoadoutMenu.ui");

        // Bind click events for each loadout button
        eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#WarriorButton",
            new EventData().append("Action", "Warrior")
        );

        eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#ArcherButton",
            new EventData().append("Action", "Archer")
        );

        eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#MageButton",
            new EventData().append("Action", "Mage")
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
        @Nonnull LoadoutEventData data
    ) {
        Player player = (Player) store.getComponent(ref, Player.getComponentType());

        switch (data.action) {
            case "Warrior":
                LoadoutManager.applyLoadout(playerRef, store, ref, "warrior");
                player.getPageManager().setPage(ref, store, Page.None);
                break;

            case "Archer":
                LoadoutManager.applyLoadout(playerRef, store, ref, "archer");
                player.getPageManager().setPage(ref, store, Page.None);
                break;

            case "Mage":
                LoadoutManager.applyLoadout(playerRef, store, ref, "mage");
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
