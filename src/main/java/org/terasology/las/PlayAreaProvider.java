// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.las;

import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.FacetProvider;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;

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
            facet.setWorld((Vector2i) position, position.distanceSquared(new Vector2i(0, 0)) <= playAreaRadiusSquared);
        }

        // Pass our newly created and populated facet to the region
        region.setRegionFacet(PlayAreaFacet.class, facet);
    }
}
