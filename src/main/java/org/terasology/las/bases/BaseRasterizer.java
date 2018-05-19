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

import java.util.Map.Entry;

import org.terasology.math.ChunkMath;
import org.terasology.math.Region3i;
import org.terasology.math.geom.BaseVector3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.WorldRasterizer;

public class BaseRasterizer implements WorldRasterizer {
    private Block stone;

    @Override
    public void initialize() {
        stone = CoreRegistry.get(BlockManager.class).getBlock("Core:Stone");
    }

    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        BaseFacet baseFacet = chunkRegion.getFacet(BaseFacet.class);

        for (Entry<BaseVector3i, Base> entry : baseFacet.getWorldEntries().entrySet()) {

            Vector3i centerBasePosition = new Vector3i(entry.getKey());
            int extent = entry.getValue().getExtent();
            centerBasePosition.add(0, extent, 0);
            //Region3i walls = Region3i.createFromCenterExtents(centerBasePosition, extent);
            Vector3i min = new Vector3i(centerBasePosition.x() - extent + 2, centerBasePosition.y() - extent, centerBasePosition.z() - extent);
            Vector3i max = new Vector3i(centerBasePosition.x() + extent - 2, centerBasePosition.y() - extent, centerBasePosition.z() + extent);
            Region3i walls = Region3i.createFromMinMax(min, max);

            // loop through each of the positions in the base
            for (Vector3i newBlockPosition : walls) {
                if (chunkRegion.getRegion().encompasses(newBlockPosition)) {
                    chunk.setBlock(ChunkMath.calcBlockPos(newBlockPosition), stone);
                }
            }
        }
    }
}