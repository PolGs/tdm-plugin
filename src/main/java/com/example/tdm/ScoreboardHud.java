package com.example.tdm;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.entity.entities.player.hud.HudManager;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardHud extends CustomUIHud {

    private static final String SERVER_IP = "fctc.hytaleservers.space";

    // Track active HUDs for each player
    private static final Map<UUID, ScoreboardHud> activeHuds = new HashMap<>();

    public ScoreboardHud(PlayerRef playerRef) {
        super(playerRef);
    }

    @Override
    protected void build(UICommandBuilder uiCommandBuilder) {
        uiCommandBuilder.append("Pages/ScoreboardHud.ui");

        // Update the values
        uiCommandBuilder.set("#RedKills.Text", String.valueOf(TeamManager.getRedKills()));
        uiCommandBuilder.set("#BlueKills.Text", String.valueOf(TeamManager.getBlueKills()));
        uiCommandBuilder.set("#ServerIP.Text", SERVER_IP);
    }

    /**
     * Show the scoreboard HUD to a player
     */
    public static void showToPlayer(PlayerRef playerRef) {
        Ref<EntityStore> ref = playerRef.getReference();
        if (ref == null) return;

        Store<EntityStore> store = ref.getStore();
        Player player = (Player) store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        HudManager hudManager = player.getHudManager();
        ScoreboardHud hud = new ScoreboardHud(playerRef);
        hudManager.setCustomHud(playerRef, hud);
        hud.show();

        activeHuds.put(playerRef.getUuid(), hud);
    }

    /**
     * Hide the scoreboard HUD from a player
     */
    public static void hideFromPlayer(PlayerRef playerRef) {
        Ref<EntityStore> ref = playerRef.getReference();
        if (ref == null) return;

        Store<EntityStore> store = ref.getStore();
        Player player = (Player) store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        HudManager hudManager = player.getHudManager();
        hudManager.setCustomHud(playerRef, null);

        activeHuds.remove(playerRef.getUuid());
    }

    /**
     * Update the HUD for a specific player
     */
    public static void updatePlayer(PlayerRef playerRef) {
        ScoreboardHud hud = activeHuds.get(playerRef.getUuid());
        if (hud != null) {
            UICommandBuilder builder = new UICommandBuilder();
            builder.set("#RedKills.Text", String.valueOf(TeamManager.getRedKills()));
            builder.set("#BlueKills.Text", String.valueOf(TeamManager.getBlueKills()));
            hud.update(false, builder);
        }
    }

    /**
     * Update the HUD for all players in TDM
     */
    public static void updateAllPlayers() {
        for (PlayerRef playerRef : Universe.get().getPlayers()) {
            if (TeamManager.getTeam(playerRef.getUuid()) != null) {
                updatePlayer(playerRef);
            }
        }
    }
}
