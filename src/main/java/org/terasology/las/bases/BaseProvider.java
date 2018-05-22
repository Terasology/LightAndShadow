/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.las.bases;

import com.google.common.collect.ImmutableSet;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.generation.*;
import org.terasology.world.generation.facets.SurfaceHeightFacet;
import java.util.Collection;

@Produces(BaseFacet.class)
@Requires(@Facet(SurfaceHeightFacet.class))

public class BaseProvider implements FacetProvider {
    /** Determines size of base
     * Base is a square of side 2 * BASE_EXTENT + 1 with the flag at the center
     */
    static final int BASE_EXTENT = 2;
    /** Position of Red base */
    static final Vector3i CENTER_RED_BASE_POSITION = new Vector3i(30, 10, 0);
    /** Position of Black base */
    static final Vector3i CENTER_BLACK_BASE_POSITION = new Vector3i(-30, 10, 0);

    Region3i redBaseRegion = CreateBaseRegionFromVector(CENTER_RED_BASE_POSITION);
    Region3i blackBaseRegion = CreateBaseRegionFromVector(CENTER_BLACK_BASE_POSITION);

    Region3i redFlagRegion = CreateFlagRegionFromVector(CENTER_RED_BASE_POSITION);
    Region3i blackFlagRegion = CreateFlagRegionFromVector(CENTER_BLACK_BASE_POSITION);

    private Collection<Base> fixedBases = ImmutableSet.of(
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
        return Region3i.createFromMinMax(new Vector3i(centerBasePosition.x() - BASE_EXTENT, centerBasePosition.y(), centerBasePosition.z() - BASE_EXTENT), new Vector3i(centerBasePosition.x() + BASE_EXTENT, centerBasePosition.y(), centerBasePosition.z() + BASE_EXTENT));
    }
    private Region3i CreateFlagRegionFromVector(Vector3i centerBasePosition) {
        return Region3i.createFromMinMax(new Vector3i(centerBasePosition.x(), centerBasePosition.y() + 1, centerBasePosition.z()), new Vector3i(centerBasePosition.x(), centerBasePosition.y() + 1, centerBasePosition.z()));
    }
}
