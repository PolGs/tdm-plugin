package com.example.tdm;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

public class TeamManager {

    private static final Logger LOGGER = Logger.getLogger("TDM-TeamManager");

    public enum Team {
        RED, BLUE
    }

    // Team spawn locations in tdm2 world
    private static final Vector3d RED_SPAWN = new Vector3d(55, 56, 23);
    private static final Vector3d BLUE_SPAWN = new Vector3d(55, 56, 74);

    // Win condition
    private static final int KILLS_TO_WIN = 50;

    // Track players on each team
    private static final Set<UUID> redTeam = new HashSet<>();
    private static final Set<UUID> blueTeam = new HashSet<>();

    // Kill counters
    private static int redKills = 0;
    private static int blueKills = 0;

    /**
     * Join a specific team
     */
    public static void joinTeam(PlayerRef playerRef, Store<EntityStore> store, Ref<EntityStore> ref, Team team) {
        UUID playerId = playerRef.getUuid();

        LOGGER.info("[DEBUG] joinTeam called - player: " + playerRef.getUsername() + ", team: " + team);

        // Remove from any existing team
        redTeam.remove(playerId);
        blueTeam.remove(playerId);

        // Add to selected team
        if (team == Team.RED) {
            redTeam.add(playerId);
        } else {
            blueTeam.add(playerId);
        }

        // Clear inventory and give team gear
        Player player = (Player) store.getComponent(ref, Player.getComponentType());
        if (player != null) {
            clearAndEquip(player, team);
            LOGGER.info("[DEBUG] Cleared inventory and equipped gear for " + playerRef.getUsername());
        } else {
            LOGGER.warning("[DEBUG] Could not get Player component for " + playerRef.getUsername());
        }

        // Notify player
        String teamName = (team == Team.RED) ? "RED" : "BLUE";
        playerRef.sendMessage(Message.raw("You joined the " + teamName + " team!"));

        // Announce current score
        announceScore(playerRef);

        // Teleport to TDM world at team spawn
        Vector3d spawn = (team == Team.RED) ? RED_SPAWN : BLUE_SPAWN;
        LOGGER.info("[DEBUG] Teleporting to spawn: " + spawn);
        WorldUtil.teleportToTDM(playerRef, store, ref, spawn);
    }

    /**
     * Auto-assign to team with fewer players
     */
    public static void autoJoinTeam(PlayerRef playerRef, Store<EntityStore> store, Ref<EntityStore> ref) {
        Team team = (redTeam.size() <= blueTeam.size()) ? Team.RED : Team.BLUE;
        joinTeam(playerRef, store, ref, team);
    }

    /**
     * Get a player's current team
     */
    public static Team getTeam(UUID playerId) {
        if (redTeam.contains(playerId)) return Team.RED;
        if (blueTeam.contains(playerId)) return Team.BLUE;
        return null;
    }

    /**
     * Remove player from their team (on disconnect or /back)
     */
    public static void removePlayer(UUID playerId) {
        redTeam.remove(playerId);
        blueTeam.remove(playerId);
    }

    /**
     * Process a kill event
     */
    public static void processKill(UUID killerUuid, UUID victimUuid) {
        Team killerTeam = getTeam(killerUuid);
        Team victimTeam = getTeam(victimUuid);

        if (killerTeam == null || victimTeam == null) return;

        if (killerTeam != victimTeam) {
            // Enemy kill = +1
            if (killerTeam == Team.RED) {
                redKills++;
            } else {
                blueKills++;
            }
        } else {
            // Friendly fire = -1
            if (killerTeam == Team.RED) {
                redKills = Math.max(0, redKills - 1);
            } else {
                blueKills = Math.max(0, blueKills - 1);
            }
        }

        // Announce to all players
        announceScoreToAll();

        // Check win condition
        checkWinCondition();
    }

    /**
     * Announce score to a single player
     */
    private static void announceScore(PlayerRef playerRef) {
        playerRef.sendMessage(Message.raw("[TDM] Score - RED: " + redKills + " | BLUE: " + blueKills + " (First to " + KILLS_TO_WIN + " wins)"));
    }

    /**
     * Announce score to all TDM players
     */
    private static void announceScoreToAll() {
        String scoreMsg = "[TDM] Score - RED: " + redKills + " | BLUE: " + blueKills;
        for (PlayerRef playerRef : Universe.get().getPlayers()) {
            if (getTeam(playerRef.getUuid()) != null) {
                playerRef.sendMessage(Message.raw(scoreMsg));
            }
        }
    }

    /**
     * Check if a team has won
     */
    private static void checkWinCondition() {
        Team winner = null;

        if (redKills >= KILLS_TO_WIN) {
            winner = Team.RED;
        } else if (blueKills >= KILLS_TO_WIN) {
            winner = Team.BLUE;
        }

        if (winner != null) {
            announceWinner(winner);
            endMatch();
        }
    }

    /**
     * Announce the winner to all players
     */
    private static void announceWinner(Team winner) {
        String winnerName = (winner == Team.RED) ? "RED" : "BLUE";
        String message = "[TDM] " + winnerName + " TEAM WINS! Final Score: Red " + redKills + " - Blue " + blueKills;

        for (PlayerRef playerRef : Universe.get().getPlayers()) {
            if (getTeam(playerRef.getUuid()) != null) {
                playerRef.sendMessage(Message.raw(message));
            }
        }
    }

    /**
     * End the match - teleport everyone to lobby and reset
     */
    private static void endMatch() {
        // Teleport all TDM players to lobby
        for (PlayerRef playerRef : Universe.get().getPlayers()) {
            UUID playerId = playerRef.getUuid();
            if (getTeam(playerId) != null) {
                WorldUtil.teleportToLobby(playerRef);
            }
        }

        // Clear teams and reset kills
        redTeam.clear();
        blueTeam.clear();
        redKills = 0;
        blueKills = 0;
    }

    /**
     * Get spawn position for a team
     */
    public static Vector3d getSpawn(Team team) {
        return (team == Team.RED) ? RED_SPAWN : BLUE_SPAWN;
    }

    /**
     * Respawn a player on their current team (after death)
     */
    public static void respawnPlayer(PlayerRef playerRef) {
        Team team = getTeam(playerRef.getUuid());
        if (team != null) {
            Vector3d spawn = getSpawn(team);
            WorldUtil.teleportToTDM(playerRef, spawn);
        }
    }

    public static int getRedCount() {
        return redTeam.size();
    }

    public static int getBlueCount() {
        return blueTeam.size();
    }

    public static int getRedKills() {
        return redKills;
    }

    public static int getBlueKills() {
        return blueKills;
    }

    public static void resetKills() {
        redKills = 0;
        blueKills = 0;
    }

    /**
     * Clear inventory and equip team gear
     */
    private static void clearAndEquip(Player player, Team team) {
        Inventory inventory = player.getInventory();
        inventory.clear();

        ItemContainer hotbar = inventory.getHotbar();
        ItemContainer armor = inventory.getArmor();

        // === ARMOR ===
        // Head - team specific
        if (team == Team.RED) {
            armor.addItemStack(new ItemStack("Armor_Cloth_Cotton_Head", 1));
        } else {
            armor.addItemStack(new ItemStack("Armor_Wool_Head", 1));
        }
        // Body armor - same for all
        armor.addItemStack(new ItemStack("Armor_Steel_Chest", 1));
        armor.addItemStack(new ItemStack("Armor_Steel_Hands", 1));
        armor.addItemStack(new ItemStack("Armor_Steel_Legs", 1));

        // === WEAPONS ===
        hotbar.addItemStack(new ItemStack("Weapon_Longsword_Iron", 1));
        hotbar.addItemStack(new ItemStack("Weapon_Shortbow_Iron", 1));

        // Shield - team specific
        if (team == Team.RED) {
            hotbar.addItemStack(new ItemStack("Weapon_Shield_Copper", 1));
        } else {
            hotbar.addItemStack(new ItemStack("Weapon_Shield_Doomed", 1));
        }

        // === CONSUMABLES ===
        hotbar.addItemStack(new ItemStack("Potion_Health_Large", 10));
        hotbar.addItemStack(new ItemStack("Weapon_Arrow_Crude", 100));
        hotbar.addItemStack(new ItemStack("Food_Pie_Apple", 25));

        LOGGER.info("[DEBUG] Equipped " + team + " team gear");
    }
}
