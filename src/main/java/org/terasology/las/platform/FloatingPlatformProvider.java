// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.las.platform;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.math.Region3i;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.ConfigurableFacetProvider;
import org.terasology.engine.world.generation.FacetProviderPlugin;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generator.plugin.RegisterPlugin;
import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector3i;

import java.util.Collection;
import java.util.Collections;

/**
 * Provides {@link FloatingPlatformFacet} instances.
 */
@RegisterPlugin
@Produces(FloatingPlatformFacet.class)
public class FloatingPlatformProvider implements ConfigurableFacetProvider, FacetProviderPlugin {
    private static final Rect2i FLOATING_PLATFORM_REGION =
            Rect2i.createFromMinAndMax(LASUtils.FLOATING_PLATFORM_POSITION.getX() - LASUtils.FLOATING_PLATFORM_WIDTH / 2,
            LASUtils.FLOATING_PLATFORM_POSITION.getZ() - LASUtils.FLOATING_PLATFORM_LENGTH / 2,
            LASUtils.FLOATING_PLATFORM_POSITION.getX() + LASUtils.FLOATING_PLATFORM_WIDTH / 2,
            LASUtils.FLOATING_PLATFORM_POSITION.getZ() + LASUtils.FLOATING_PLATFORM_LENGTH / 2);

    private static final Region3i RED_TELEPORTER_REGION =
            Region3i.createFromMinMax(new Vector3i(LASUtils.FLOATING_PLATFORM_POSITION.getX() + LASUtils.TELEPORTER_OFFSET,
                    LASUtils.FLOATING_PLATFORM_POSITION.getY() + 1,
                    LASUtils.FLOATING_PLATFORM_POSITION.getZ() + LASUtils.NPC_OFFSET),
            new Vector3i(LASUtils.FLOATING_PLATFORM_POSITION.getX() + LASUtils.TELEPORTER_OFFSET,
                    LASUtils.FLOATING_PLATFORM_POSITION.getY() + 1,
                    LASUtils.FLOATING_PLATFORM_POSITION.getZ() + LASUtils.NPC_OFFSET));
    private static final Region3i BLACK_TELEPORTER_REGION =
            Region3i.createFromMinMax(new Vector3i(LASUtils.FLOATING_PLATFORM_POSITION.getX() - LASUtils.TELEPORTER_OFFSET,
                    LASUtils.FLOATING_PLATFORM_POSITION.getY() + 1,
                    LASUtils.FLOATING_PLATFORM_POSITION.getZ() + LASUtils.NPC_OFFSET),
            new Vector3i(LASUtils.FLOATING_PLATFORM_POSITION.getX() - LASUtils.TELEPORTER_OFFSET,
                    LASUtils.FLOATING_PLATFORM_POSITION.getY() + 1,
                    LASUtils.FLOATING_PLATFORM_POSITION.getZ() + LASUtils.NPC_OFFSET));
    private final Collection<FloatingPlatform> fixedPlatforms = Collections.singleton(
            new FloatingPlatform(FLOATING_PLATFORM_REGION, LASUtils.FLOATING_PLATFORM_POSITION.getY(),
                    RED_TELEPORTER_REGION, BLACK_TELEPORTER_REGION));
    private Config configuration = new Config();

    @Override
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(FloatingPlatformFacet.class);
        FloatingPlatformFacet platformFacet = new FloatingPlatformFacet(region.getRegion(), border);
        Rect2i worldRect = platformFacet.getWorldRegion();

        for (FloatingPlatform platform : fixedPlatforms) {
            if (platform.getArea().overlaps(worldRect)) {
                // TODO: consider checking height as well
                platformFacet.add(platform);
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

    private static class Config implements Component {
//        @Range(min = 10, max = 1000, increment = 1, precision = 0, label = "Platform Height")
//        public int height = 100;
    }
}
