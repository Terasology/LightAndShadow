// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.las;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.terasology.coreworlds.generator.facets.FloraFacet;
import org.terasology.coreworlds.generator.rasterizers.FloraRasterizer;
import org.terasology.coreworlds.generator.rasterizers.FloraType;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.utilities.procedural.WhiteNoise;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.chunks.CoreChunk;
import org.terasology.engine.world.generation.Region;
import org.terasology.math.geom.BaseVector3i;

import java.util.List;
import java.util.Map;

public class LaSFloraRasterizer extends FloraRasterizer {
    private final Map<FloraType, List<Block>> flora = Maps.newEnumMap(FloraType.class);
    private Block air;

    @Override
    public void initialize() {
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        air = blockManager.getBlock(BlockManager.AIR_ID);

        flora.put(FloraType.GRASS, ImmutableList.of(
                blockManager.getBlock("core:TallGrass1"),
                blockManager.getBlock("core:TallGrass2"),
                blockManager.getBlock("core:TallGrass3")));

        flora.put(FloraType.FLOWER, ImmutableList.of(
                blockManager.getBlock("lightAndShadowResources:spadesCropSapling"),
                blockManager.getBlock("lightAndShadowResources:heartsCropSapling"),
                blockManager.getBlock("lightAndShadowResources:diamondsCropSapling"),
                blockManager.getBlock("lightAndShadowResources:clubsCropSapling"),
                blockManager.getBlock("lightAndShadowResources:unclaimedCropSapling")));


        flora.put(FloraType.MUSHROOM, ImmutableList.of(
                blockManager.getBlock("core:BigBrownShroom"),
                blockManager.getBlock("core:BrownShroom"),
                blockManager.getBlock("core:RedShroom")));
    }


    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        super.generateChunk(chunk, chunkRegion);

        FloraFacet facet = chunkRegion.getFacet(FloraFacet.class);

        WhiteNoise noise = new WhiteNoise(chunk.getPosition().hashCode());

        Map<BaseVector3i, FloraType> entries = facet.getRelativeEntries();
        // check if some other rasterizer has already placed something here
        entries.keySet().stream().filter(pos -> chunk.getBlock(pos).equals(air)).forEach(pos -> {

            FloraType type = entries.get(pos);
            List<Block> list = flora.get(type);
            int blockIdx = Math.abs(noise.intNoise(pos.x(), pos.y(), pos.z())) % list.size();
            Block block = list.get(blockIdx);
            chunk.setBlock(pos, block);
        });
    }
}
