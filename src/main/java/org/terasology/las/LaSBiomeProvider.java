// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.las;

import org.terasology.coreworlds.CoreBiome;
import org.terasology.coreworlds.generator.facets.BiomeFacet;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.FacetProvider;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.math.geom.BaseVector2i;

@Produces(BiomeFacet.class)
public class LaSBiomeProvider implements FacetProvider {
    @Override
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(BiomeFacet.class);
        BiomeFacet biomeFacet = new BiomeFacet(region.getRegion(), border);

        for (BaseVector2i pos : biomeFacet.getRelativeRegion().contents()) {
            biomeFacet.set(pos, CoreBiome.PLAINS);
        }
        region.setRegionFacet(BiomeFacet.class, biomeFacet);
    }
}
