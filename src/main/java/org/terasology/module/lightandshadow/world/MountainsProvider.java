// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.lightandshadow.world;

import org.joml.Vector2f;
import org.joml.Vector2ic;
import org.terasology.engine.utilities.procedural.BrownianNoise;
import org.terasology.engine.utilities.procedural.Noise;
import org.terasology.engine.utilities.procedural.SimplexNoise;
import org.terasology.engine.utilities.procedural.SubSampledNoise;
import org.terasology.engine.world.generation.Facet;
import org.terasology.engine.world.generation.FacetProvider;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Requires;
import org.terasology.engine.world.generation.Updates;
import org.terasology.engine.world.generation.facets.ElevationFacet;
import org.terasology.math.TeraMath;

@Requires(@Facet(PlayAreaFacet.class))
@Updates(@Facet(ElevationFacet.class))
public class MountainsProvider implements FacetProvider {
    private Noise mountainNoise;

    @Override
    public void setSeed(long seed) {
        mountainNoise = new SubSampledNoise(new BrownianNoise(new SimplexNoise(seed + 3)), new Vector2f(0.0002f,
                0.0002f), 4);
    }

    @Override
    public void process(GeneratingRegion region) {
        ElevationFacet facet = region.getRegionFacet(ElevationFacet.class);
        PlayAreaFacet playAreaFacet = region.getRegionFacet(PlayAreaFacet.class);
        float mountainHeight = 40;

        for (Vector2ic position : facet.getWorldArea()) {
            // scale our max mountain height to noise (between -1 and 1)
            float additiveMountainHeight = mountainNoise.noise(position.x(), position.y()) * mountainHeight;
            // don't bother subtracting mountain height, that will allow unaffected regions
            additiveMountainHeight = TeraMath.clamp(additiveMountainHeight, 0, mountainHeight);
            if (!playAreaFacet.getWorld(position)) {
                facet.setWorld(position, facet.getWorld(position) + additiveMountainHeight + 10);
            }
        }
    }
}
