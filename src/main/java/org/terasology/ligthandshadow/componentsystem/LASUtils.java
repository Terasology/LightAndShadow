/*
 * Copyright 2017 MovingBlocks
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

import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.world.block.BlockManager;

public final class LASUtils {
    public static final String BLACK_FLAG_URI = "LightAndShadowResources:blackFlag";
    public static final String RED_FLAG_URI = "LightAndShadowResources:redFlag";
    public static final String RED_TEAM = "red";
    public static final String BLACK_TEAM = "black";
    /** Position of Red base */
    public static final Vector3i CENTER_RED_BASE_POSITION = new Vector3i(30, 10, 0);
    /** Position of Black base */
    public static final Vector3i CENTER_BLACK_BASE_POSITION = new Vector3i(-30, 10, 0);
    public static final String BLACK_BASE_STONE = "LightAndShadowResources:blackBaseStone";
    public static final String RED_BASE_STONE = "LightAndShadowResources:redBaseStone";
    /**
     * Determines size of base
     * Base is a square of side 2 * BASE_EXTENT + 1 with the flag at the center
     */
    public static final int BASE_EXTENT = 2;
    public static final int GOAL_SCORE = 5;
    public static final String SPADES_PARTICLE = "LightAndShadowResources:blackFlagParticleEffect";
    public static final String  HEARTS_PARTICLE = "LightAndShadowResources:redFlagParticleEffect";
    public static final String BLACK_PAWN = "LightAndShadowResources:blackPawnPlayerSkin";
    public static final String  RED_PAWN = "LightAndShadowResources:redPawnPlayerSkin";

    @In
    private BlockManager blockManager;

    public static Vector3i getFlagLocation(String flagTeam) {
        if (flagTeam.equals(RED_TEAM)) {
            return (new Vector3i(CENTER_RED_BASE_POSITION.x, CENTER_RED_BASE_POSITION.y + 1, CENTER_RED_BASE_POSITION.z));
        }
        if (flagTeam.equals(BLACK_TEAM)) {
            return (new Vector3i(CENTER_BLACK_BASE_POSITION.x, CENTER_BLACK_BASE_POSITION.y + 1, CENTER_BLACK_BASE_POSITION.z));
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
}
