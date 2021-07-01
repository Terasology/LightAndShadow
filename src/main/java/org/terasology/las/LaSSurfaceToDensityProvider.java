// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.las;

import org.joml.Vector2ic;
import org.terasology.engine.world.block.BlockArea;
import org.terasology.engine.world.block.BlockAreac;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.Facet;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generation.Requires;
import org.terasology.engine.world.generation.ScalableFacetProvider;
import org.terasology.engine.world.generation.facets.DensityFacet;
import org.terasology.engine.world.generation.facets.ElevationFacet;
import org.terasology.engine.world.generation.facets.SurfacesFacet;
import org.terasology.ligthandshadow.componentsystem.LASUtils;

/**
 * Sets density based on its distance from the surface.
 * Also sets the BlockHeightsFacet at the same time, because it should be kept synchronised with the DensityFacet.
 */
@Requires(@Facet(ElevationFacet.class))
@Produces({DensityFacet.class, SurfacesFacet.class})
public class LaSSurfaceToDensityProvider implements ScalableFacetProvider {

    private static final BlockAreac FLOATING_PLATFORM_REGION =
            new BlockArea(LASUtils.FLOATING_PLATFORM_POSITION.x() - LASUtils.FLOATING_PLATFORM_WIDTH / 2,
                    LASUtils.FLOATING_PLATFORM_POSITION.z() - LASUtils.FLOATING_PLATFORM_LENGTH / 2,
                    LASUtils.FLOATING_PLATFORM_POSITION.x() + LASUtils.FLOATING_PLATFORM_WIDTH / 2,
                    LASUtils.FLOATING_PLATFORM_POSITION.z() + LASUtils.FLOATING_PLATFORM_LENGTH / 2);
    @Override
    public void setSeed(long seed) {

    }

    @Override
    public void process(GeneratingRegion region, float scale) {
        ElevationFacet elevation = region.getRegionFacet(ElevationFacet.class);
        DensityFacet densityFacet = new DensityFacet(region.getRegion(), region.getBorderForFacet(DensityFacet.class));
        SurfacesFacet surfacesFacet = new SurfacesFacet(region.getRegion(), region.getBorderForFacet(SurfacesFacet.class));

        BlockRegion area = region.getRegion();
        BlockArea densityRect = new BlockArea(densityFacet.getRelativeRegion().minX(), densityFacet.getRelativeRegion().minZ(),
                densityFacet.getRelativeRegion().maxX(), densityFacet.getRelativeRegion().maxZ());
        for (Vector2ic pos : densityRect) {
            float height = elevation.get(pos);
            for (int y = densityFacet.getRelativeRegion().minY(); y <= densityFacet.getRelativeRegion().maxY(); ++y) {
                densityFacet.set(pos.x(), y, pos.y(), height - (area.minY() + y) * scale);
            }
        }
        region.setRegionFacet(DensityFacet.class, densityFacet);

        BlockArea surfaceRect = new BlockArea(surfacesFacet.getWorldRegion().minX(), surfacesFacet.getWorldRegion().minZ(),
                surfacesFacet.getWorldRegion().maxX(), surfacesFacet.getWorldRegion().maxZ());
        for (Vector2ic pos : surfaceRect) {
            if (!FLOATING_PLATFORM_REGION.contains(pos.x(), pos.y())) {
                // Round in this odd way because if the elevation is precisely an integer, the block at that level has density 0, so it's air.
                int height = (int) Math.ceil(elevation.getWorld(pos) / scale) - 1;
                if (height >= surfacesFacet.getWorldRegion().minY() && height <= surfacesFacet.getWorldRegion().maxY()) {
                    surfacesFacet.setWorld(pos.x(), height, pos.y(), true);
                }
            }
        }
        region.setRegionFacet(SurfacesFacet.class, surfacesFacet);
    }
}
