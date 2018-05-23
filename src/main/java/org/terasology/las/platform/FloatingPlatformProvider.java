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

import org.terasology.entitySystem.Component;
import org.terasology.math.geom.Rect2i;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.ConfigurableFacetProvider;
import org.terasology.world.generation.FacetProviderPlugin;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generator.plugin.RegisterPlugin;

import java.util.Collection;
import java.util.Collections;

/**
 * Provides {@link FloatingPlatformFacet} instances.
 */
@RegisterPlugin
@Produces(FloatingPlatformFacet.class)
public class FloatingPlatformProvider implements ConfigurableFacetProvider, FacetProviderPlugin {

    private Config configuration = new Config();

    private Collection<FloatingPlatform> fixedPlatforms = Collections.singleton(
            new FloatingPlatform(Rect2i.createFromMinAndMax(-14, -28, 14, 28), 60));

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
