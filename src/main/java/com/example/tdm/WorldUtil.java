package com.example.tdm;

import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WorldUtil {

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

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
     * First transfers to world, then after 1 second delay teleports to exact position
     */
    public static void teleportToTDM(PlayerRef playerRef, Vector3d spawnPosition) {
        // First: transfer to the world (without specific position)
        teleportToWorld(playerRef, TDM_WORLD);

        // After 1 second delay: teleport to exact team spawn position
        scheduler.schedule(() -> {
            teleportToPosition(playerRef, spawnPosition);
        }, 1, TimeUnit.SECONDS);
    }

    /**
     * Teleport player to a specific position within TDM world
     */
    public static void teleportToPosition(PlayerRef playerRef, Vector3d position) {
        Universe universe = Universe.get();
        World tdmWorld = universe.getWorld(TDM_WORLD);
        if (tdmWorld == null) return;

        Transform transform = new Transform(position, new Vector3f(0, 0, 0));
        universe.resetPlayer(playerRef, playerRef.getHolder(), tdmWorld, transform);
    }

    /**
     * Teleport player to lobby
     */
    public static void teleportToLobby(PlayerRef playerRef) {
        teleportToWorld(playerRef, LOBBY_WORLD);
    }
}
