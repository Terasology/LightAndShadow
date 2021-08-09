// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.las.yinyang;

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.Facet;
import org.terasology.engine.world.generation.FacetProviderPlugin;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generation.Updates;
import org.terasology.engine.world.generation.facets.SurfacesFacet;
import org.terasology.engine.world.generator.plugin.RegisterPlugin;
import org.terasology.ligthandshadow.componentsystem.LASUtils;

import java.util.Collections;
import java.util.List;

@RegisterPlugin
@Produces(YinYangFacet.class)
@Updates(@Facet(value = SurfacesFacet.class))
public class YinYangProvider implements FacetProviderPlugin {

    private static final int RADIUS = 5;
    private final List<Vector3i> yinYangPositions = Collections.singletonList(new Vector3i(0, 10, 0));

    @Override
    public void process(GeneratingRegion region) {
        Border3D border =
                region.getBorderForFacet(YinYangFacet.class)
                        .extendBy(0, 0, 10);

        SurfacesFacet surfacesFacet = region.getRegionFacet(SurfacesFacet.class);

        YinYangFacet yinYangFacet = new YinYangFacet(region.getRegion(), border);

        BlockRegion worldRect = yinYangFacet.getWorldRegion();
        final Vector3i tempPos = new Vector3i();
        yinYangPositions.stream()
            .filter(worldRect::contains)
            .forEach(pos -> {
                yinYangFacet.setWorld(pos, new YinYang());
                int y = surfacesFacet.getNextBelow(pos);
                for (int i = -RADIUS; i <= RADIUS; i++) {
                    for (int j = -2 * RADIUS; j <= 2 * RADIUS; j++) {
                        if (!LASUtils.pixel(j, i, RADIUS).equals("engine:air")) {
                            pos.add(i, 0, j, tempPos);
                            tempPos.y = y;
                            surfacesFacet.setWorld(tempPos, false);
                        }
                    }
                }
            });

        region.setRegionFacet(YinYangFacet.class, yinYangFacet);
    }

}
