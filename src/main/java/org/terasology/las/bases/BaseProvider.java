// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.las.bases;

import com.google.common.collect.ImmutableSet;
import org.terasology.engine.math.Region3i;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.Facet;
import org.terasology.engine.world.generation.FacetProvider;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generation.Requires;
import org.terasology.engine.world.generation.facets.SurfaceHeightFacet;
import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.math.geom.Vector3i;

import java.util.Collection;

@Produces(BaseFacet.class)
@Requires(@Facet(SurfaceHeightFacet.class))

public class BaseProvider implements FacetProvider {
    Region3i redBaseRegion = CreateBaseRegionFromVector(LASUtils.CENTER_RED_BASE_POSITION);
    Region3i blackBaseRegion = CreateBaseRegionFromVector(LASUtils.CENTER_BLACK_BASE_POSITION);

    Region3i redFlagRegion = CreateFlagRegionFromVector(LASUtils.CENTER_RED_BASE_POSITION);
    Region3i blackFlagRegion = CreateFlagRegionFromVector(LASUtils.CENTER_BLACK_BASE_POSITION);

    private final Collection<Base> fixedBases = ImmutableSet.of(
            new Base(redBaseRegion, redFlagRegion),
            new Base(blackBaseRegion, blackFlagRegion));

    @Override
    public void setSeed(long seed) {
    }

    @Override
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(BaseFacet.class);
        BaseFacet facet = new BaseFacet(region.getRegion(), border);

        for (Base base : fixedBases) {
            facet.add(base);
        }
        region.setRegionFacet(BaseFacet.class, facet);
    }

    private Region3i CreateBaseRegionFromVector(Vector3i centerBasePosition) {
        return Region3i.createFromMinMax(new Vector3i(centerBasePosition.x() - LASUtils.BASE_EXTENT,
                centerBasePosition.y(), centerBasePosition.z() - LASUtils.BASE_EXTENT),
                new Vector3i(centerBasePosition.x() + LASUtils.BASE_EXTENT, centerBasePosition.y(),
                        centerBasePosition.z() + LASUtils.BASE_EXTENT));
    }

    private Region3i CreateFlagRegionFromVector(Vector3i centerBasePosition) {
        return Region3i.createFromMinMax(new Vector3i(centerBasePosition.x(), centerBasePosition.y() + 1,
                centerBasePosition.z()), new Vector3i(centerBasePosition.x(), centerBasePosition.y() + 1,
                centerBasePosition.z()));
    }
}
