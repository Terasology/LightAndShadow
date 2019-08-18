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
package org.terasology.las.yingyang;

import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.generation.*;
import org.terasology.world.generation.facets.SurfaceHeightFacet;
import org.terasology.world.generator.plugin.RegisterPlugin;

import java.util.Collection;
import java.util.Collections;

@RegisterPlugin
@Produces(YingYangFacet.class)
@Requires(@Facet(value = SurfaceHeightFacet.class, border = @FacetBorder(sides = 5)))
public class YingYangProvider implements FacetProviderPlugin {
    private Collection<YingYang> yingYangs = Collections.singleton((
            new YingYang(new Vector3i(0, 10, 0))));
    @Override
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(YingYangFacet.class).extendBy(0, 0, 5);
        YingYangFacet yingYangFacet = new YingYangFacet(region.getRegion(), border);
        Region3i worldRect = yingYangFacet.getWorldRegion();

        for (YingYang yingYang : yingYangs) {
            if (worldRect.encompasses(yingYang.getCenterPosition())) {
                yingYangFacet.add(yingYang);
            }
        }

        region.setRegionFacet(YingYangFacet.class, yingYangFacet);
    }

}
