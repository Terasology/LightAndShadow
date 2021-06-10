// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.lightandshadow.worldgeneration.yinyang;

import org.joml.Vector3i;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.FacetProviderPlugin;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generator.plugin.RegisterPlugin;

import java.util.Collections;
import java.util.List;

@RegisterPlugin
@Produces(YinYangFacet.class)
public class YinYangProvider implements FacetProviderPlugin {

    private List<Vector3i> yinYangPositions = Collections.singletonList(new Vector3i(0, 10, 0));

    @Override
    public void process(GeneratingRegion region) {
        Border3D border =
                region.getBorderForFacet(YinYangFacet.class)
                        .extendBy(0, 0, 10);

        YinYangFacet yinYangFacet = new YinYangFacet(region.getRegion(), border);

        BlockRegion worldRect = yinYangFacet.getWorldRegion();

        yinYangPositions.stream()
                .filter(worldRect::contains)
                .forEach(pos -> yinYangFacet.setWorld(pos, new YinYang()));

        region.setRegionFacet(YinYangFacet.class, yinYangFacet);
    }

}
