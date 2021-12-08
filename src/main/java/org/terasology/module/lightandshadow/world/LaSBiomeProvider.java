// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.lightandshadow.world;

import org.joml.Vector2ic;
import org.terasology.core.world.CoreBiome;
import org.terasology.core.world.generator.facets.BiomeFacet;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.FacetProvider;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;

@Produces(BiomeFacet.class)
public class LaSBiomeProvider implements FacetProvider {
    @Override
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(BiomeFacet.class);
        BiomeFacet biomeFacet = new BiomeFacet(region.getRegion(), border);

        for (Vector2ic pos : biomeFacet.getRelativeArea()) {
            biomeFacet.set(pos, CoreBiome.PLAINS);
        }
        region.setRegionFacet(BiomeFacet.class, biomeFacet);
    }
}
