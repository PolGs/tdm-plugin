package com.example.tdm;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TeamManager {

    public enum Team {
        RED, BLUE
    }

    // Team spawn locations
    private static final Vector3d RED_SPAWN = new Vector3d(844, 116, 577);
    private static final Vector3d BLUE_SPAWN = new Vector3d(779, 116, 578);

    // Track players on each team
    private static final Set<UUID> redTeam = new HashSet<>();
    private static final Set<UUID> blueTeam = new HashSet<>();

    /**
     * Join a specific team
     */
    public static void joinTeam(PlayerRef playerRef, Store<EntityStore> store, Ref<EntityStore> ref, Team team) {
        UUID playerId = playerRef.getUuid();

        // Remove from any existing team
        redTeam.remove(playerId);
        blueTeam.remove(playerId);

        // Add to selected team
        if (team == Team.RED) {
            redTeam.add(playerId);
        } else {
            blueTeam.add(playerId);
        }

        // Spawn the player
        spawnPlayer(playerRef, store, ref, team);
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
     * Remove player from their team (on disconnect)
     */
    public static void removePlayer(UUID playerId) {
        redTeam.remove(playerId);
        blueTeam.remove(playerId);
    }

    /**
     * Respawn a player on their current team
     */
    public static void respawnPlayer(PlayerRef playerRef, Store<EntityStore> store, Ref<EntityStore> ref) {
        Team team = getTeam(playerRef.getUuid());
        if (team != null) {
            spawnPlayer(playerRef, store, ref, team);
        }
    }

    /**
     * Spawn a player with their team's gear
     */
    private static void spawnPlayer(PlayerRef playerRef, Store<EntityStore> store, Ref<EntityStore> ref, Team team) {
        Player player = (Player) store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        // Clear inventory and give gear
        clearAndEquip(player, team);

        // Teleport to team spawn
        Vector3d spawn = (team == Team.RED) ? RED_SPAWN : BLUE_SPAWN;
        teleportPlayer(player, spawn, ref);

        // Notify player
        String teamName = (team == Team.RED) ? "RED" : "BLUE";
        playerRef.sendMessage(Message.raw("You joined the " + teamName + " team!"));
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
    }

    /**
     * Teleport player to position
     */
    private static void teleportPlayer(Player player, Vector3d position, Ref<EntityStore> ref) {
        World world = player.getWorld();
        if (world == null) return;

        world.execute(() -> {
            if (ref == null) return;
            Store<EntityStore> store = ref.getStore();
            Teleport teleport = new Teleport(position, new Vector3f(0, 0, 0));
            store.addComponent(ref, Teleport.getComponentType(), teleport);
        });
    }

    public static int getRedCount() {
        return redTeam.size();
    }

    public static int getBlueCount() {
        return blueTeam.size();
    }
}
