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

public class LoadoutManager {

    // =====================================================
    // SPAWN LOCATIONS - Customize these coordinates!
    // =====================================================
    private static final Vector3d WARRIOR_SPAWN = new Vector3d(100, 64, 100);
    private static final Vector3d ARCHER_SPAWN = new Vector3d(150, 64, 100);
    private static final Vector3d MAGE_SPAWN = new Vector3d(200, 64, 100);

    /**
     * Applies a loadout to the player - clears inventory, gives gear, and teleports
     */
    public static void applyLoadout(PlayerRef playerRef, Store<EntityStore> store, Ref<EntityStore> ref, String loadoutType) {
        Player player = (Player) store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        // Clear current inventory
        clearInventory(player);

        // Give gear based on loadout type
        switch (loadoutType.toLowerCase()) {
            case "warrior":
                giveWarriorGear(player);
                teleportPlayer(player, WARRIOR_SPAWN, ref);
                playerRef.sendMessage(Message.raw("You are now a Warrior! Fight with honor!"));
                break;

            case "archer":
                giveArcherGear(player);
                teleportPlayer(player, ARCHER_SPAWN, ref);
                playerRef.sendMessage(Message.raw("You are now an Archer! Strike from afar!"));
                break;

            case "mage":
                giveMageGear(player);
                teleportPlayer(player, MAGE_SPAWN, ref);
                playerRef.sendMessage(Message.raw("You are now a Mage! Wield the arcane!"));
                break;

            default:
                playerRef.sendMessage(Message.raw("Unknown loadout type: " + loadoutType));
        }
    }

    /**
     * Clears the player's inventory
     */
    private static void clearInventory(Player player) {
        Inventory inventory = player.getInventory();
        inventory.clear();
    }

    // =====================================================
    // LOADOUT DEFINITIONS - Customize item names here!
    // =====================================================

    private static void giveWarriorGear(Player player) {
        ItemContainer hotbar = player.getInventory().getHotbar();

        // Weapons
        hotbar.addItemStack(new ItemStack("IronSword", 1));
        hotbar.addItemStack(new ItemStack("WoodenShield", 1));

        // Consumables
        hotbar.addItemStack(new ItemStack("HealthPotion", 3));
    }

    private static void giveArcherGear(Player player) {
        ItemContainer hotbar = player.getInventory().getHotbar();

        // Weapons
        hotbar.addItemStack(new ItemStack("Bow", 1));
        hotbar.addItemStack(new ItemStack("Arrow", 64));
        hotbar.addItemStack(new ItemStack("StoneDagger", 1));

        // Consumables
        hotbar.addItemStack(new ItemStack("SpeedPotion", 2));
        hotbar.addItemStack(new ItemStack("HealthPotion", 2));
    }

    private static void giveMageGear(Player player) {
        ItemContainer hotbar = player.getInventory().getHotbar();

        // Weapons
        hotbar.addItemStack(new ItemStack("MagicStaff", 1));
        hotbar.addItemStack(new ItemStack("Wand", 1));

        // Consumables
        hotbar.addItemStack(new ItemStack("ManaPotion", 5));
        hotbar.addItemStack(new ItemStack("HealthPotion", 2));
    }

    /**
     * Teleports a player to the specified position
     */
    public static void teleportPlayer(Player player, Vector3d position, Ref<EntityStore> ref) {
        World world = player.getWorld();
        if (world == null) return;

        world.execute(() -> {
            if (ref == null) return;

            Store<EntityStore> store = ref.getStore();
            Teleport teleport = new Teleport(position, new Vector3f(0, 0, 0));
            store.addComponent(ref, Teleport.getComponentType(), teleport);
        });
    }
}
