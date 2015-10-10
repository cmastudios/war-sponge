package com.tommytony.war;

import com.tommytony.war.struct.WarLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Stores state for players.
 */
public class WarPlayerState {
    private static List<WarPlayerState> states = new ArrayList<>();
    private UUID playerId;
    private ZoneCreationState zoneCreationState;

    public WarPlayerState(UUID playerId) {
        this.playerId = playerId;
    }

    public boolean isCreatingZone() {
        return this.getZoneCreationState() != null;
    }

    public ZoneCreationState getZoneCreationState() {
        return zoneCreationState;
    }

    public void setZoneCreationState(ZoneCreationState zoneCreationState) {
        this.zoneCreationState = zoneCreationState;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public static WarPlayerState getState(UUID playerId) {
        for (WarPlayerState state : states) {
            if (state.getPlayerId().equals(playerId)) {
                return state;
            }
        }
        WarPlayerState state = new WarPlayerState(playerId);
        states.add(state);
        return state;
    }

    public static class ZoneCreationState {
        private final String zoneName;
        private WarLocation position1;

        public ZoneCreationState(String zoneName) {
            this.zoneName = zoneName;
        }

        public String getZoneName() {
            return zoneName;
        }

        public boolean isPosition1Set() {
            return this.getPosition1() != null;
        }

        public WarLocation getPosition1() {
            return position1;
        }

        public void setPosition1(WarLocation position1) {
            this.position1 = position1;
        }
    }
}
