/*
 * Copyright 2014 MovingBlocks
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

package org.terasology.las.platform;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.world.block.BlockArea;
import org.terasology.engine.world.block.BlockAreac;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.block.BlockRegionc;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.ConfigurableFacetProvider;
import org.terasology.engine.world.generation.FacetProviderPlugin;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generator.plugin.RegisterPlugin;
import org.terasology.ligthandshadow.componentsystem.LASUtils;

import java.util.Collection;
import java.util.Collections;

/**
 * Provides {@link FloatingPlatformFacet} instances.
 */
@RegisterPlugin
@Produces(FloatingPlatformFacet.class)
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
        Border3D border = region.getBorderForFacet(FloatingPlatformFacet.class);
        FloatingPlatformFacet platformFacet = new FloatingPlatformFacet(region.getRegion(), border);
        BlockAreac worldRect = platformFacet.getWorldArea();

        for (FloatingPlatform platform : fixedPlatforms) {
            if (platform.getArea().intersectsBlockArea(worldRect)) {
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
