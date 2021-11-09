// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.lightandshadow.world.platform;

import org.joml.Vector3i;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.engine.world.block.BlockArea;
import org.terasology.engine.world.block.BlockAreac;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.block.BlockRegionc;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.ConfigurableFacetProvider;
import org.terasology.engine.world.generation.Facet;
import org.terasology.engine.world.generation.FacetProviderPlugin;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generation.Updates;
import org.terasology.engine.world.generation.facets.SurfacesFacet;
import org.terasology.engine.world.generator.plugin.RegisterPlugin;
import org.terasology.lightandshadow.LASUtils;

import java.util.Collection;
import java.util.Collections;

/**
 * Provides {@link FloatingPlatformFacet} instances.
 */
@RegisterPlugin
@Produces(FloatingPlatformFacet.class)
@Updates(@Facet(value = SurfacesFacet.class))
public class FloatingPlatformProvider implements ConfigurableFacetProvider, FacetProviderPlugin {
    private static final BlockAreac FLOATING_PLATFORM_REGION =
            new BlockArea(LASUtils.FLOATING_PLATFORM_POSITION.x() - LASUtils.FLOATING_PLATFORM_WIDTH / 2,
                    LASUtils.FLOATING_PLATFORM_POSITION.z() - LASUtils.FLOATING_PLATFORM_LENGTH / 2,
                    LASUtils.FLOATING_PLATFORM_POSITION.x() + LASUtils.FLOATING_PLATFORM_WIDTH / 2,
                    LASUtils.FLOATING_PLATFORM_POSITION.z() + LASUtils.FLOATING_PLATFORM_LENGTH / 2);

    private static final BlockRegionc RED_TELEPORTER_REGION =
            new BlockRegion(LASUtils.FLOATING_PLATFORM_POSITION.x() + LASUtils.TELEPORTER_OFFSET,
                    LASUtils.FLOATING_PLATFORM_POSITION.y() + 1,
                    LASUtils.FLOATING_PLATFORM_POSITION.z() + LASUtils.NPC_OFFSET,
                    LASUtils.FLOATING_PLATFORM_POSITION.x() + LASUtils.TELEPORTER_OFFSET,
                    LASUtils.FLOATING_PLATFORM_POSITION.y() + 1,
                    LASUtils.FLOATING_PLATFORM_POSITION.z() + LASUtils.NPC_OFFSET);
    private static final BlockRegionc BLACK_TELEPORTER_REGION =
            new BlockRegion(LASUtils.FLOATING_PLATFORM_POSITION.x() - LASUtils.TELEPORTER_OFFSET,
                    LASUtils.FLOATING_PLATFORM_POSITION.y() + 1,
                    LASUtils.FLOATING_PLATFORM_POSITION.z() + LASUtils.NPC_OFFSET,
                    LASUtils.FLOATING_PLATFORM_POSITION.x() - LASUtils.TELEPORTER_OFFSET,
                    LASUtils.FLOATING_PLATFORM_POSITION.y() + 1,
                    LASUtils.FLOATING_PLATFORM_POSITION.z() + LASUtils.NPC_OFFSET);

    private Config configuration = new Config();

    private final Collection<FloatingPlatform> fixedPlatforms = Collections.singleton(
            new FloatingPlatform(FLOATING_PLATFORM_REGION, LASUtils.FLOATING_PLATFORM_POSITION.y(),
                    RED_TELEPORTER_REGION, BLACK_TELEPORTER_REGION));

    @Override
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(SurfacesFacet.class);
        FloatingPlatformFacet platformFacet = new FloatingPlatformFacet(region.getRegion(), border);
        SurfacesFacet surfacesFacet = region.getRegionFacet(SurfacesFacet.class);
        BlockAreac worldRect = platformFacet.getWorldArea();

        for (FloatingPlatform platform : fixedPlatforms) {
            if (platform.getArea().intersectsBlockArea(worldRect)) {
                // TODO: consider checking height as well
                platformFacet.add(platform);
            }
        }
        Vector3i pos = new Vector3i();
        BlockRegion worldRegion = surfacesFacet.getWorldRegion();
        for (int x = worldRegion.minX(); x <= worldRegion.maxX(); x++) {
            for (int z = worldRegion.minZ(); z <= worldRegion.maxZ(); z++) {
                int y = surfacesFacet.getNextBelow(pos.set(x, LASUtils.FLOATING_PLATFORM_HEIGHT_LEVEL, z));
                if (worldRegion.contains(x, y, z) && FLOATING_PLATFORM_REGION.contains(x, z)) {
                    surfacesFacet.setWorld(x, y, z, false);
                }
            }
        }
        region.setRegionFacet(FloatingPlatformFacet.class, platformFacet);
    }

    @Override
    public String getConfigurationName() {
        return "Floating Platforms";
    }

    @Override
    public Component getConfiguration() {
        return configuration;
    }

    @Override
    public void setConfiguration(Component configuration) {
        this.configuration = (Config) configuration;
    }

    private static class Config implements Component<Config> {
//        @Range(min = 10, max = 1000, increment = 1, precision = 0, label = "Platform Height")
//        public int height = 100;

        @Override
        public void copyFrom(Config other) {
            // no-op, but needs to copy 'height' if commented in again
        }
    }
}
