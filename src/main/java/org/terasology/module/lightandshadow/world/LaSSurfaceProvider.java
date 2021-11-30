// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.lightandshadow.world;

import org.joml.Vector2ic;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.FacetProvider;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generation.facets.ElevationFacet;

@Produces(ElevationFacet.class)
public class LaSSurfaceProvider implements FacetProvider {

    @Override
    public void setSeed(long seed) {
    }

    @Override
    public void process(GeneratingRegion region) {
        // Create our surface height facet (we will get into borders later)
        Border3D border = region.getBorderForFacet(ElevationFacet.class);
        ElevationFacet facet = new ElevationFacet(region.getRegion(), border);

        // Loop through every position in our 2d array
        for (Vector2ic position : facet.getWorldArea()) {
            facet.setWorld(position, 9.5f);
        }

        // Pass our newly created and populated facet to the region
        region.setRegionFacet(ElevationFacet.class, facet);
    }


}
