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

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.math.ChunkMath;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.WorldRasterizer;

public class BaseRasterizer implements WorldRasterizer {
    private Block blackBaseStone;
    private Block redBaseStone;
    private Block redFlag;
    private Block blackFlag;


    @Override
    public void initialize() {
        blackBaseStone = CoreRegistry.get(BlockManager.class).getBlock(LASUtils.BLACK_BASE_STONE);
        redBaseStone = CoreRegistry.get(BlockManager.class).getBlock(LASUtils.RED_BASE_STONE);
        redFlag = CoreRegistry.get(BlockManager.class).getBlock(LASUtils.RED_FLAG_URI);
        blackFlag = CoreRegistry.get(BlockManager.class).getBlock(LASUtils.BLACK_FLAG_URI);
    }

    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        BaseFacet baseFacet = chunkRegion.getFacet(BaseFacet.class);

        for (Base base : baseFacet.getBases()) {
            BlockRegion baseRegion = base.getArea();
            BlockRegion flagRegion = base.getFlagArea();

            //place blocks for each of the bases and flags
            for (Vector3ic baseBlockPosition : baseRegion) {
                if (chunkRegion.getRegion().contains(baseBlockPosition) && baseBlockPosition.x() > 0) {
                    chunk.setBlock(ChunkMath.calcRelativeBlockPos(baseBlockPosition, new Vector3i()), redBaseStone);
                } else if (chunkRegion.getRegion().contains(baseBlockPosition)) {
                    chunk.setBlock(ChunkMath.calcRelativeBlockPos(baseBlockPosition, new Vector3i()), blackBaseStone);
                }
            }

            for (Vector3ic flagPosition : flagRegion) {
                //flag type depends on the x position of the flag to determine which base it's at
                if (chunkRegion.getRegion().contains(flagPosition) && flagPosition.x() > 0) {
                    chunk.setBlock(ChunkMath.calcRelativeBlockPos(flagPosition, new Vector3i()), redFlag);
                } else if (chunkRegion.getRegion().contains(flagPosition)) {
                    chunk.setBlock(ChunkMath.calcRelativeBlockPos(flagPosition, new Vector3i()), blackFlag);
                }
            }
        }
    }
}
