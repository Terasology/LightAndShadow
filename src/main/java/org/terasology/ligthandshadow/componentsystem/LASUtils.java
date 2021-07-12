/*
 * Copyright 2019 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.ligthandshadow.componentsystem;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.util.Collection;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Collections.singletonMap;

public final class LASUtils {
    public static final String BLACK_FLAG_URI = "lightAndShadowResources:blackFlag";
    public static final String RED_FLAG_URI = "lightAndShadowResources:redFlag";
    public static final String RED_TEAM = "red";
    public static final String BLACK_TEAM = "black";
    public static final String WHITE_TEAM = "white";
    /**
     * Floating Platform values
     */
    public static final Vector3ic FLOATING_PLATFORM_POSITION = new Vector3i(0, 60, 252);
    public static final int FLOATING_PLATFORM_WIDTH = 28;
    public static final int FLOATING_PLATFORM_LENGTH = 56;
    public static final int FLOATING_PLATFORM_HEIGHT_LEVEL = 60;
    public static final int TELEPORTER_OFFSET = 4;
    public static final int NPC_OFFSET = 24;
    /**
     * Play Area values
     */
    public static final int PLAY_AREA_RADIUS = 50;
    /**
     * Position of Red base
     */
    public static final Vector3ic CENTER_RED_BASE_POSITION = new Vector3i(30, 10, 0);
    /**
     * Position of Black base
     */
    public static final Vector3ic CENTER_BLACK_BASE_POSITION = new Vector3i(-30, 10, 0);
    public static final String BLACK_BASE_STONE = "lightAndShadowResources:blackBaseStone";
    public static final String RED_BASE_STONE = "lightAndShadowResources:redBaseStone";
    /**
     * Determines size of base Base is a square of side 2 * BASE_EXTENT + 1 with the flag at the center
     */
    public static final int BASE_EXTENT = 2;
    public static final int GOAL_SCORE = 5;
    public static final String SPADES_PARTICLE = "lightAndShadowResources:blackFlagParticleEffect";
    public static final String HEARTS_PARTICLE = "lightAndShadowResources:redFlagParticleEffect";
    public static final String BLACK_PAWN_SKIN = "lightAndShadowResources:blackPawnPlayerSkin";
    public static final String RED_PAWN_SKIN = "lightAndShadowResources:redPawnPlayerSkin";
    public static final String WHITE_PAWN_SKIN = "lightAndShadowResources:whitePawnPlayerSkin";
    public static final String WHITE_HEALTH_ICON = "lightAndShadowResources:icons#circle";
    public static final String RED_HEALTH_ICON = "engine:icons#redHeart";
    public static final String BLACK_HEALTH_ICON = "lightAndShadowResources:icons#spades";
    public static final String WHITE_HEALTH_SKIN = "lightAndShadowResources:healthWhite";
    public static final String RED_HEALTH_SKIN = "lightAndShadowResources:healthRed";
    public static final String BLACK_HEALTH_SKIN = "lightAndShadowResources:healthBlack";
    // The position near the team's base that player will be teleported to on choosing a team
    public static final Vector3fc RED_TELEPORT_DESTINATION = new Vector3f(29, 12, 0);
    public static final Vector3fc BLACK_TELEPORT_DESTINATION = new Vector3f(-29, 12, 0);
    public static final String MAGIC_STAFF_URI = "LightAndShadowResources:magicStaff";

    public static final String DEATH_SCREEN = "engine:DeathScreen";
    public static final String ONLINE_PLAYERS_OVERLAY = "engine:onlinePlayersOverlay";

    public static final String DROPPED_FLAG = "dropped_flag_indicator";
    public static final long FLAG_TELEPORT_DELAY = 20000;

    private LASUtils() {
    }

    public static Vector3i getFlagLocation(String flagTeam) {
        if (flagTeam.equals(RED_TEAM)) {
            return (new Vector3i(CENTER_RED_BASE_POSITION.x(), CENTER_RED_BASE_POSITION.y() + 1,
                    CENTER_RED_BASE_POSITION.z()));
        }
        if (flagTeam.equals(BLACK_TEAM)) {
            return (new Vector3i(CENTER_BLACK_BASE_POSITION.x(), CENTER_BLACK_BASE_POSITION.y() + 1,
                    CENTER_BLACK_BASE_POSITION.z()));
        }
        return null;
    }

    public static String getFlagURI(String flagTeam) {
        if (flagTeam.equals(RED_TEAM)) {
            return RED_FLAG_URI;
        }
        if (flagTeam.equals(BLACK_TEAM)) {
            return BLACK_FLAG_URI;
        }
        return null;
    }

    public static String getFlagParticle(String flagTeam) {
        if (flagTeam.equals(RED_TEAM)) {
            return HEARTS_PARTICLE;
        }
        if (flagTeam.equals(BLACK_TEAM)) {
            return SPADES_PARTICLE;
        }
        return null;
    }

    public static Vector3fc getTeleportDestination(String team) {
        if (team.equals(RED_TEAM)) {
            return RED_TELEPORT_DESTINATION;
        }
        if (team.equals(BLACK_TEAM)) {
            return BLACK_TELEPORT_DESTINATION;
        }
        return null;
    }

    public static String getHealthIcon(String team) {
        if (team.equals(RED_TEAM)) {
            return RED_HEALTH_ICON;
        }
        if (team.equals(BLACK_TEAM)) {
            return BLACK_HEALTH_ICON;
        }
        if (team.equals(WHITE_TEAM)) {
            return WHITE_HEALTH_ICON;
        }
        return null;
    }

    public static String getHealthSkin(String team) {
        if (team.equals(RED_TEAM)) {
            return RED_HEALTH_SKIN;
        }
        if (team.equals(BLACK_TEAM)) {
            return BLACK_HEALTH_SKIN;
        }
        if (team.equals(WHITE_TEAM)) {
            return WHITE_HEALTH_SKIN;
        }
        return null;
    }

    public static String getPlayerSkin(String team) {
        if (team.equals(RED_TEAM)) {
            return RED_PAWN_SKIN;
        }
        if (team.equals(BLACK_TEAM)) {
            return BLACK_PAWN_SKIN;
        }
        if (team.equals(WHITE_TEAM)) {
            return WHITE_PAWN_SKIN;
        }
        return null;
    }

    public static String getOppositionTeam(String team) {
        if (team.equals(RED_TEAM)) {
            return BLACK_TEAM;
        }
        if (team.equals(BLACK_TEAM)) {
            return RED_TEAM;
        }
        return null;
    }

    public static boolean circle(
            int x,
            int y,
            int c,
            int r
    ) {
        return
                (r * r) >= ((x / 2) * x) + ((y - c) * y);
    }

    public static String pixel(int x, int y, int r) {
        return Stream.<Map<BooleanSupplier, Supplier<String>>>of(
                singletonMap(
                        () -> circle(x, y, -r / 2, r / 6),
                        () -> "LightAndShadowResources:blackfloorblock"
                ),
                singletonMap(
                        () -> circle(x, y, r / 2, r / 6),
                        () -> "LightAndShadowResources:redfloorblock"
                ),
                singletonMap(
                        () -> circle(x, y, -r / 2, r / 2),
                        () -> "LightAndShadowResources:redfloorblock"
                ),
                singletonMap(
                        () -> circle(x, y, r / 2, r / 2),
                        () -> "LightAndShadowResources:blackfloorblock"
                ),
                singletonMap(
                        () -> circle(x, y, 0, r),
                        () -> x < 0 ? "LightAndShadowResources:redfloorblock" : "LightAndShadowResources:blackfloorblock"
                )
        )
                .sequential()
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .filter(e -> e.getKey().getAsBoolean())
                .map(Map.Entry::getValue)
                .map(Supplier::get)
                .findAny()
                .orElse("engine:air");
    }
}
