// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.lightandshadow.worldgeneration.bases;

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.engine.world.generation.Region;
import org.terasology.engine.world.generation.WorldRasterizer;
import org.terasology.lightandshadow.gamestate.LASUtils;

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
    public void generateChunk(Chunk chunk, Region chunkRegion) {
        BaseFacet baseFacet = chunkRegion.getFacet(BaseFacet.class);

        Vector3i tempPos = new Vector3i();
        for (Base base : baseFacet.getBases()) {
            BlockRegion baseRegion = base.getArea();
            BlockRegion flagRegion = base.getFlagArea();

            //place blocks for each of the bases and flags
            for (Vector3ic baseBlockPosition : baseRegion) {
                if (chunkRegion.getRegion().contains(baseBlockPosition) && baseBlockPosition.x() > 0) {
                    chunk.setBlock(Chunks.toRelative(baseBlockPosition, tempPos), redBaseStone);
                } else if (chunkRegion.getRegion().contains(baseBlockPosition)) {
                    chunk.setBlock(Chunks.toRelative(baseBlockPosition, tempPos), blackBaseStone);
                }
            }

            for (Vector3ic flagPosition : flagRegion) {
                //flag type depends on the x position of the flag to determine which base it's at
                if (chunkRegion.getRegion().contains(flagPosition) && flagPosition.x() > 0) {
                    chunk.setBlock(Chunks.toRelative(flagPosition, tempPos), redFlag);
                } else if (chunkRegion.getRegion().contains(flagPosition)) {
                    chunk.setBlock(Chunks.toRelative(flagPosition, tempPos), blackFlag);
                }
            }
        }
    }
}
