// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.lightandshadow.world.yinyang;

import org.joml.Vector3i;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.engine.world.generation.Region;
import org.terasology.engine.world.generation.WorldRasterizerPlugin;
import org.terasology.engine.world.generator.plugin.RegisterPlugin;
import org.terasology.module.lightandshadow.LASUtils;

@RegisterPlugin
public class YinYangRasterizer implements WorldRasterizerPlugin {

    private static final int RADIUS = 5;

    private Block blackStone;
    private Block redStone;
    private Block air;

    @Override
    public void initialize() {
        blackStone = CoreRegistry.get(BlockManager.class).getBlock("LightAndShadowResources:blackfloorblock");
        redStone = CoreRegistry.get(BlockManager.class).getBlock("LightAndShadowResources:redfloorblock");
        air = CoreRegistry.get(BlockManager.class).getBlock("engine:air");
    }

    @Override
    public void generateChunk(Chunk chunk, Region chunkRegion) {
        YinYangFacet yinYangFacet = chunkRegion.getFacet(YinYangFacet.class);

        yinYangFacet.getWorldEntries().keySet().stream()
                .map(Vector3i::new)
                .forEach(yinYangPosition -> placeYinYang(chunk, yinYangPosition));
    }

    private void placeYinYang(Chunk chunk, Vector3i yinYangPosition) {
        Vector3i tempPos = new Vector3i();
        for (int i = -RADIUS; i <= RADIUS; i++) {
            for (int j = -2 * RADIUS; j <= 2 * RADIUS; j++) {
                String blockString = LASUtils.pixel(j, i, RADIUS);
                Vector3i chunkBlockPosition = new Vector3i(i, 0, j).add(yinYangPosition);
                if (chunk.getRegion().contains(chunkBlockPosition)) {
                    chunk.setBlock(Chunks.toRelative(chunkBlockPosition, tempPos), getBlock(blockString));
                }
            }
        }
    }

    private Block getBlock(String blockString) {
        if (blockString.equals("LightAndShadowResources:blackfloorblock")) {
            return blackStone;
        }
        if (blockString.equals("LightAndShadowResources:redfloorblock")) {
            return redStone;
        }
        return air;
    }

}
