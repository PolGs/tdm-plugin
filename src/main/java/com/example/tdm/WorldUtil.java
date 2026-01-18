package com.example.tdm;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.logging.Logger;

public class WorldUtil {

    private static final Logger LOGGER = Logger.getLogger("TDM-WorldUtil");

    // TDM world name
    public static final String TDM_WORLD = "tdm2";
    public static final String LOBBY_WORLD = "lobby";

    /**
     * Teleport a player to a different world at a specific position (for page context with store/ref)
     */
    public static void teleportToWorld(PlayerRef playerRef, Store<EntityStore> store, Ref<EntityStore> ref, String worldName, Vector3d position) {
        LOGGER.info("[DEBUG] teleportToWorld called - player: " + playerRef.getUsername() + ", world: " + worldName + ", position: " + position);

        Universe universe = Universe.get();
        World targetWorld = universe.getWorld(worldName);

        if (targetWorld == null) {
            LOGGER.warning("[DEBUG] World '" + worldName + "' not found!");
            playerRef.sendMessage(Message.raw("World '" + worldName + "' not found!"));
            return;
        }

        // Get current rotation to preserve it
        HeadRotation headRotation = (HeadRotation) store.getComponent(ref, HeadRotation.getComponentType());
        Vector3f rotation = (headRotation != null) ? headRotation.getRotation().clone() : new Vector3f(0, 0, 0);

        LOGGER.info("[DEBUG] Creating Teleport component - world: " + targetWorld.getName() + ", pos: " + position + ", rot: " + rotation);

        // Create and add teleport component
        Teleport teleport = new Teleport(targetWorld, position, rotation);
        store.addComponent(ref, Teleport.getComponentType(), teleport);

        LOGGER.info("[DEBUG] Teleport component added successfully");
    }

    /**
     * Teleport player to TDM world at team spawn (for page context)
     */
    public static void teleportToTDM(PlayerRef playerRef, Store<EntityStore> store, Ref<EntityStore> ref, Vector3d spawnPosition) {
        LOGGER.info("[DEBUG] teleportToTDM called - player: " + playerRef.getUsername() + ", spawn: " + spawnPosition);
        teleportToWorld(playerRef, store, ref, TDM_WORLD, spawnPosition);
    }

    /**
     * Teleport player to TDM world (for contexts without store/ref like respawn)
     */
    public static void teleportToTDM(PlayerRef playerRef, Vector3d spawnPosition) {
        LOGGER.info("[DEBUG] teleportToTDM (no store/ref) called - player: " + playerRef.getUsername() + ", spawn: " + spawnPosition);

        Ref<EntityStore> ref = playerRef.getReference();
        if (ref == null || !ref.isValid()) {
            LOGGER.warning("[DEBUG] Player reference is invalid!");
            playerRef.sendMessage(Message.raw("Error: Cannot teleport - not in a world"));
            return;
        }

        // Get current world and execute on its thread
        World currentWorld = ((EntityStore) ref.getStore().getExternalData()).getWorld();
        Universe universe = Universe.get();
        World targetWorld = universe.getWorld(TDM_WORLD);

        if (targetWorld == null) {
            LOGGER.warning("[DEBUG] TDM world not found!");
            playerRef.sendMessage(Message.raw("World '" + TDM_WORLD + "' not found!"));
            return;
        }

        targetWorld.execute(() -> {
            Store<EntityStore> store = ref.getStore();

            // Get current rotation
            HeadRotation headRotation = (HeadRotation) store.getComponent(ref, HeadRotation.getComponentType());
            Vector3f rotation = (headRotation != null) ? headRotation.getRotation().clone() : new Vector3f(0, 0, 0);

            LOGGER.info("[DEBUG] Creating Teleport component on world thread");
            Teleport teleport = new Teleport(targetWorld, spawnPosition, rotation);
            store.addComponent(ref, Teleport.getComponentType(), teleport);
            LOGGER.info("[DEBUG] Teleport component added");
        });
    }

    /**
     * Teleport a player to a different world (legacy for commands, uses world spawn)
     */
    public static void teleportToWorld(PlayerRef playerRef, String worldName) {
        LOGGER.info("[DEBUG] teleportToWorld (legacy) called - player: " + playerRef.getUsername() + ", world: " + worldName);

        Ref<EntityStore> ref = playerRef.getReference();
        if (ref == null || !ref.isValid()) {
            LOGGER.warning("[DEBUG] Player reference is invalid!");
            playerRef.sendMessage(Message.raw("Error: Cannot teleport - not in a world"));
            return;
        }

        Universe universe = Universe.get();
        World targetWorld = universe.getWorld(worldName);

        if (targetWorld == null) {
            LOGGER.warning("[DEBUG] World '" + worldName + "' not found!");
            playerRef.sendMessage(Message.raw("World '" + worldName + "' not found!"));
            return;
        }

        targetWorld.execute(() -> {
            Store<EntityStore> store = ref.getStore();

            // Get spawn point from world config
            com.hypixel.hytale.math.vector.Transform spawnPoint = targetWorld.getWorldConfig().getSpawnProvider().getSpawnPoint(ref, store);
            Vector3d position = (spawnPoint != null) ? spawnPoint.getPosition() : new Vector3d(0, 64, 0);
            Vector3f rotation = (spawnPoint != null) ? spawnPoint.getRotation() : new Vector3f(0, 0, 0);

            LOGGER.info("[DEBUG] Teleporting to world spawn: " + position);
            Teleport teleport = new Teleport(targetWorld, position, rotation);
            store.addComponent(ref, Teleport.getComponentType(), teleport);
            LOGGER.info("[DEBUG] Teleport component added");
        });
    }

    /**
     * Teleport player to lobby
     */
    public static void teleportToLobby(PlayerRef playerRef) {
        teleportToWorld(playerRef, LOBBY_WORLD);
    }
}
