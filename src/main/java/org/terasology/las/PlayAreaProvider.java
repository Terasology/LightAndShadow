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
package org.terasology.las;

import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;

@Produces(PlayAreaFacet.class)
public class PlayAreaProvider implements FacetProvider {
    @Override
    public void process(GeneratingRegion region) {
        // Create our surface height facet (we will get into borders later)
        Border3D border = region.getBorderForFacet(PlayAreaFacet.class);
        PlayAreaFacet facet = new PlayAreaFacet(region.getRegion(), border);
        int playAreaRadiusSquared = LASUtils.PLAY_AREA_RADIUS * LASUtils.PLAY_AREA_RADIUS;

        // Loop through every position in our 2d array
        Rect2i processRegion = facet.getWorldRegion();
        for (BaseVector2i position : processRegion.contents()) {
            if (position.distanceSquared(new Vector2i(0, 0)) <= playAreaRadiusSquared) {
                facet.setWorld((Vector2i) position, true);
            } else {
                facet.setWorld((Vector2i) position, false);
            }
        }

        // Pass our newly created and populated facet to the region
        region.setRegionFacet(PlayAreaFacet.class, facet);
    }
}
