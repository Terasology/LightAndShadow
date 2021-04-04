/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.las;

import com.google.common.base.Predicate;
import org.joml.Vector3i;
import org.terasology.engine.world.generation.facets.SurfacesFacet;
import org.terasology.ligthandshadow.componentsystem.LASUtils;


/**
 * A collection of filters that restrict the placement of objects, specific to Light and Shadow.
 *
 */
public final class LaSExtraPositionFilters {

    private LaSExtraPositionFilters() {
        // no instances
    }

    /**
     * Filters based on surface flatness
     *
     * @param surfaceFacet the surface facet that contains all tested coords.
     * @return a predicate that returns true only if the facet would not overlap the red or black base.
     */
    public static Predicate<Vector3i> notOnBase(final SurfacesFacet surfaceFacet) {

        return new Predicate<Vector3i>() {

            @Override
            public boolean apply(Vector3i input) {
                int myx = input.x();
                int myz = input.z();
                int blackx = LASUtils.CENTER_BLACK_BASE_POSITION.x();
                int blackz = LASUtils.CENTER_BLACK_BASE_POSITION.z();
                int redx = LASUtils.CENTER_RED_BASE_POSITION.x();
                int redz = LASUtils.CENTER_RED_BASE_POSITION.z();

                for (int xOffset = -1*(LASUtils.BASE_EXTENT); xOffset <= LASUtils.BASE_EXTENT; xOffset++) {
                    for (int zOffset = -1*(LASUtils.BASE_EXTENT); zOffset <= LASUtils.BASE_EXTENT; zOffset++) {
                        if(     (myx == blackx + xOffset && myz == blackz + zOffset)||
                                (myx == redx + xOffset && myz == redz + zOffset)||
                                (myx-1 == blackx + xOffset && myz == blackz + zOffset)||
                                (myx-1 == redx + xOffset && myz == redz + zOffset)||
                                (myx+1 == blackx + xOffset && myz == blackz + zOffset)||
                                (myx+1 == redx + xOffset && myz == redz + zOffset)||
                                (myx == blackx + xOffset && myz-1 == blackz + zOffset)||
                                (myx == redx + xOffset && myz-1 == redz + zOffset)||
                                (myx == blackx + xOffset && myz+1 == blackz + zOffset)||
                                (myx == redx + xOffset && myz+1 == redz + zOffset))
                            return false;
                    }
                }
                return true;
            }
        };
    }

}
