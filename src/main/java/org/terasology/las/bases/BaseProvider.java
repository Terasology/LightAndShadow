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
import org.joml.Vector3i;
import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;

import java.util.Collection;

@Produces(BaseFacet.class)
public class BaseProvider implements FacetProvider {
    BlockRegion redBaseRegion = CreateBaseRegionFromVector(LASUtils.CENTER_RED_BASE_POSITION);
    BlockRegion blackBaseRegion = CreateBaseRegionFromVector(LASUtils.CENTER_BLACK_BASE_POSITION);

    BlockRegion redFlagRegion = CreateFlagRegionFromVector(LASUtils.CENTER_RED_BASE_POSITION);
    BlockRegion blackFlagRegion = CreateFlagRegionFromVector(LASUtils.CENTER_BLACK_BASE_POSITION);

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

    private BlockRegion CreateBaseRegionFromVector(Vector3i centerBasePosition) {
        return new BlockRegion(centerBasePosition.x() - LASUtils.BASE_EXTENT, centerBasePosition.y(),
                centerBasePosition.z() - LASUtils.BASE_EXTENT, centerBasePosition.x() + LASUtils.BASE_EXTENT,
                centerBasePosition.y(), centerBasePosition.z() + LASUtils.BASE_EXTENT);
    }

    private BlockRegion CreateFlagRegionFromVector(Vector3i centerBasePosition) {
        return new BlockRegion(centerBasePosition.x(), centerBasePosition.y() + 1,
                centerBasePosition.z(), centerBasePosition.x(), centerBasePosition.y() + 1,
                centerBasePosition.z());
    }
}
