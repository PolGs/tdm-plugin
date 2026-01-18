package com.example.tdm;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TeamManager {

    public enum Team {
        RED, BLUE
    }

    // Team spawn locations in tdm2 world
    private static final Vector3d RED_SPAWN = new Vector3d(56, 56, 23);
    private static final Vector3d BLUE_SPAWN = new Vector3d(56, 56, 73);

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

        // Remove from any existing team
        redTeam.remove(playerId);
        blueTeam.remove(playerId);

        // Add to selected team
        if (team == Team.RED) {
            redTeam.add(playerId);
        } else {
            blueTeam.add(playerId);
        }

        // Notify player
        String teamName = (team == Team.RED) ? "RED" : "BLUE";
        playerRef.sendMessage(Message.raw("You joined the " + teamName + " team!"));

        // Show scoreboard HUD
        ScoreboardHud.showToPlayer(playerRef);

        // Teleport to TDM world at team spawn
        Vector3d spawn = (team == Team.RED) ? RED_SPAWN : BLUE_SPAWN;
        WorldUtil.teleportToTDM(playerRef, spawn);
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
     * @param killerUuid The player who got the kill
     * @param victimUuid The player who died
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

        // Update HUD for all players
        ScoreboardHud.updateAllPlayers();

        // Check win condition
        checkWinCondition();
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
        String message = winnerName + " TEAM WINS! Final Score: Red " + redKills + " - Blue " + blueKills;

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
                ScoreboardHud.hideFromPlayer(playerRef);
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

    /**
     * Reset kills (for new match)
     */
    public static void resetKills() {
        redKills = 0;
        blueKills = 0;
    }
}
