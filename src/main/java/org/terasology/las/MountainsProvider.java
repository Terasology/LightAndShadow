/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.las;

import org.terasology.math.TeraMath;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector2i;
import org.terasology.utilities.procedural.*;
import org.terasology.world.generation.*;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

@Requires(@Facet(PlayAreaFacet.class))
@Updates(@Facet(SurfaceHeightFacet.class))
public class MountainsProvider implements FacetProvider {
    private Noise mountainNoise;
//    private Noise hillNoise;

    @Override
    public void setSeed(long seed) {
        mountainNoise = new SubSampledNoise(new BrownianNoise(new SimplexNoise(seed + 3)), new Vector2f(0.0002f, 0.0002f), 4);
//        hillNoise = new SubSampledNoise(new BrownianNoise(new SimplexNoise(seed + 4)), new Vector2f(0.0008f, 0.0008f), 4);
    }

    @Override
    public void process(GeneratingRegion region) {
        SurfaceHeightFacet facet = region.getRegionFacet(SurfaceHeightFacet.class);
        PlayAreaFacet playAreaFacet = region.getRegionFacet(PlayAreaFacet.class);
        float mountainHeight = 40;
        float hillHeight = 10;
        // loop through every position on our 2d array
        Rect2i processRegion = facet.getWorldRegion();
        for (BaseVector2i position : processRegion.contents()) {
            // scale our max mountain height to noise (between -1 and 1)
            float additiveMountainHeight = mountainNoise.noise(position.x(), position.y()) * mountainHeight;
//            float additiveHillHeight = hillNoise.noise(position.x(), position.y()) * hillHeight;
            // don't bother subtracting mountain height, that will allow unaffected regions
            additiveMountainHeight = TeraMath.clamp(additiveMountainHeight, 0, mountainHeight);
            if (!playAreaFacet.getWorld((Vector2i) position)) {
                facet.setWorld(position, facet.getWorld(position) + additiveMountainHeight + 10);
            }
//            else {
//                facet.setWorld(position, facet.getWorld(position) + additiveHillHeight);
//            }
        }
    }
}
