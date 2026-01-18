package com.example.tdm;

import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;

public class WorldUtil {

    // TDM world name
    public static final String TDM_WORLD = "tdm2";
    public static final String LOBBY_WORLD = "lobby";

    /**
     * Teleport a player to a different world
     */
    public static void teleportToWorld(PlayerRef playerRef, String worldName) {
        teleportToWorld(playerRef, worldName, null);
    }

    /**
     * Teleport a player to a different world at a specific position
     */
    public static void teleportToWorld(PlayerRef playerRef, String worldName, Vector3d position) {
        Universe universe = Universe.get();
        World targetWorld = universe.getWorld(worldName);

        if (targetWorld == null) {
            playerRef.sendMessage(Message.raw("World '" + worldName + "' not found!"));
            return;
        }

        // Create transform for position (use world spawn if position is null)
        Transform transform = null;
        if (position != null) {
            transform = new Transform(position, new Vector3f(0, 0, 0));
        }

        // Reset player to the new world
        if (transform != null) {
            universe.resetPlayer(playerRef, playerRef.getHolder(), targetWorld, transform);
        } else {
            universe.resetPlayer(playerRef, playerRef.getHolder(), targetWorld, null);
        }
    }

    /**
     * Teleport player to TDM world at team spawn
     */
    public static void teleportToTDM(PlayerRef playerRef, Vector3d spawnPosition) {
        teleportToWorld(playerRef, TDM_WORLD, spawnPosition);
    }

    /**
     * Teleport player to lobby
     */
    public static void teleportToLobby(PlayerRef playerRef) {
        teleportToWorld(playerRef, LOBBY_WORLD);
    }
}
