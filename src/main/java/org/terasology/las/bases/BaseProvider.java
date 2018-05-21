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
    int baseExtent = 2; //determines size of base (# of tiles from center)

    Vector3i centerRedBasePosition = new Vector3i(30, 10, 0);
    Region3i redBaseRegion = Region3i.createFromMinMax(new Vector3i(centerRedBasePosition.x() - baseExtent, centerRedBasePosition.y(), centerRedBasePosition.z() - baseExtent), new Vector3i(centerRedBasePosition.x() + baseExtent, centerRedBasePosition.y(), centerRedBasePosition.z() + baseExtent));
    Region3i redFlagRegion = Region3i.createFromMinMax(new Vector3i(centerRedBasePosition.x(), centerRedBasePosition.y() + 1, centerRedBasePosition.z()), new Vector3i(centerRedBasePosition.x(), centerRedBasePosition.y() + 1, centerRedBasePosition.z()));

    Vector3i centerBlackBasePosition = new Vector3i(-30, 10, 0);
    Region3i blackBaseRegion = Region3i.createFromMinMax(new Vector3i(centerBlackBasePosition.x() - baseExtent, centerBlackBasePosition.y(), centerBlackBasePosition.z() - baseExtent), new Vector3i(centerBlackBasePosition.x() + baseExtent, centerBlackBasePosition.y(), centerBlackBasePosition.z() + baseExtent));
    Region3i blackFlagRegion = Region3i.createFromMinMax(new Vector3i(centerBlackBasePosition.x(), centerBlackBasePosition.y() + 1, centerBlackBasePosition.z()), new Vector3i(centerBlackBasePosition.x(), centerBlackBasePosition.y() + 1, centerBlackBasePosition.z()));

    private Collection<Base> fixedBases = ImmutableSet.of(
            new Base(redBaseRegion, redFlagRegion),
            new Base(blackBaseRegion, blackFlagRegion));

    @Override
    public void setSeed(long seed) {
    }

    @Override
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(BaseFacet.class).extendBy(0, 8, 4);
        BaseFacet facet = new BaseFacet(region.getRegion(), border);

        for (Base base : fixedBases) {
                facet.add(base);
        }

        region.setRegionFacet(BaseFacet.class, facet);
    }
}
