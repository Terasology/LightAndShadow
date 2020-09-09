// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.las.yinyang;

import org.terasology.engine.math.Region3i;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.Facet;
import org.terasology.engine.world.generation.FacetBorder;
import org.terasology.engine.world.generation.FacetProviderPlugin;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generation.Requires;
import org.terasology.engine.world.generation.facets.SurfaceHeightFacet;
import org.terasology.engine.world.generator.plugin.RegisterPlugin;
import org.terasology.math.geom.Vector3i;

import java.util.Collections;
import java.util.List;

@RegisterPlugin
@Produces(YinYangFacet.class)
@Requires(@Facet(value = SurfaceHeightFacet.class, border = @FacetBorder(sides = 10)))
public class YinYangProvider implements FacetProviderPlugin {

    private final List<Vector3i> yinYangPositions = Collections.singletonList(new Vector3i(0, 10, 0));

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
