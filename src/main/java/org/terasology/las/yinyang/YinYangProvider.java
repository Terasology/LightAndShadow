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
package org.terasology.las.yinyang;

import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetBorder;
import org.terasology.world.generation.FacetProviderPlugin;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.facets.SurfaceHeightFacet;
import org.terasology.world.generator.plugin.RegisterPlugin;

import java.util.Collections;
import java.util.List;

@RegisterPlugin
@Produces(YinYangFacet.class)
@Requires(@Facet(value = SurfaceHeightFacet.class, border = @FacetBorder(sides = 10)))
public class YinYangProvider implements FacetProviderPlugin {

    private List<Vector3i> yinYangPositions = Collections.singletonList(new Vector3i(0, 10, 0));

    @Override
    public void process(GeneratingRegion region) {
        Border3D border =
                region.getBorderForFacet(YinYangFacet.class)
                        .extendBy(0, 0, 10);

        YinYangFacet yinYangFacet = new YinYangFacet(region.getRegion(), border);

        Region3i worldRect = yinYangFacet.getWorldRegion();

        yinYangPositions.stream()
                .filter(worldRect::encompasses)
                .forEach(pos -> yinYangFacet.setWorld(pos, new YinYang()));

        region.setRegionFacet(YinYangFacet.class, yinYangFacet);
    }

}
