// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.lightandshadow.world;

import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.FacetProvider;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.lightandshadow.LASUtils;

@Produces(PlayAreaFacet.class)
public class PlayAreaProvider implements FacetProvider {
    @Override
    public void process(GeneratingRegion region) {
        // Create our surface height facet (we will get into borders later)
        Border3D border = region.getBorderForFacet(PlayAreaFacet.class);
        PlayAreaFacet facet = new PlayAreaFacet(region.getRegion(), border);
        int playAreaRadiusSquared = LASUtils.PLAY_AREA_RADIUS * LASUtils.PLAY_AREA_RADIUS;

        // Loop through every position in our 2d array
        for (Vector2ic position : facet.getWorldArea()) {
            if (position.distanceSquared(new Vector2i(0, 0)) <= playAreaRadiusSquared) {
                facet.setWorld(position, true);
            } else {
                facet.setWorld(position, false);
            }
        }

        // Pass our newly created and populated facet to the region
        region.setRegionFacet(PlayAreaFacet.class, facet);
    }
}
