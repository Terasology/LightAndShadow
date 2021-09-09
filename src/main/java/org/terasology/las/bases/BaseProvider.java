// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.las.bases;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.joml.Vector3ic;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.Facet;
import org.terasology.engine.world.generation.FacetProvider;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generation.Updates;
import org.terasology.engine.world.generation.facets.SurfacesFacet;
import org.terasology.ligthandshadow.componentsystem.LASUtils;

import java.util.Collection;

@Produces(BaseFacet.class)
@Updates(@Facet(value = SurfacesFacet.class))
public class BaseProvider implements FacetProvider {
    BlockRegion redBaseRegion = new BlockRegion(LASUtils.CENTER_RED_BASE_POSITION).expand(LASUtils.BASE_EXTENT, 0, LASUtils.BASE_EXTENT);
    BlockRegion blackBaseRegion = new BlockRegion(LASUtils.CENTER_BLACK_BASE_POSITION).expand(LASUtils.BASE_EXTENT, 0, LASUtils.BASE_EXTENT);

    BlockRegion redFlagRegion = new BlockRegion(LASUtils.CENTER_RED_BASE_POSITION).translate(0, 1, 0);
    BlockRegion blackFlagRegion = new BlockRegion(LASUtils.CENTER_BLACK_BASE_POSITION).translate(0, 1, 0);

    private final Collection<Base> fixedBases = ImmutableSet.of(
            new Base(redBaseRegion, redFlagRegion),
            new Base(blackBaseRegion, blackFlagRegion));

    @Override
    public void setSeed(long seed) {
    }

    @Override
    public void process(GeneratingRegion region) {
        SurfacesFacet surfacesFacet = region.getRegionFacet(SurfacesFacet.class);
        Border3D border = region.getBorderForFacet(BaseFacet.class);
        BaseFacet facet = new BaseFacet(region.getRegion(), border);

        for (Base base : fixedBases) {
            facet.add(base);
        }

        BlockRegion surfaceRegion = surfacesFacet.getWorldRegion();
        for (Vector3ic pos : Iterables.concat(redBaseRegion, blackBaseRegion)) {
            if (surfaceRegion.contains(pos)) {
                int y = surfacesFacet.getNextBelow(pos);
                surfacesFacet.setWorld(pos.x(), y, pos.z(), false);
            }
        }
        region.setRegionFacet(BaseFacet.class, facet);
    }
}
