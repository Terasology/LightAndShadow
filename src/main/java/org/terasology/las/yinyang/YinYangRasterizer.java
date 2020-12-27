// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.las.yinyang;

import org.joml.Vector3i;
import org.terasology.math.ChunkMath;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.WorldRasterizerPlugin;
import org.terasology.world.generator.plugin.RegisterPlugin;

import java.util.Collection;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Collections.singletonMap;

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
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        YinYangFacet yinYangFacet = chunkRegion.getFacet(YinYangFacet.class);

        yinYangFacet.getWorldEntries().keySet().stream()
                .map(Vector3i::new)
                .forEach(yinYangPosition -> placeYinYang(chunk, yinYangPosition));
    }

    private void placeYinYang(CoreChunk chunk, Vector3i yinYangPosition) {
        Vector3i tempPos = new Vector3i();
        for (int i = -RADIUS; i <= RADIUS; i++) {
            for (int j = -2 * RADIUS; j <= 2 * RADIUS; j++) {
                String blockString = pixel(j, i, RADIUS);
                Vector3i chunkBlockPosition = new Vector3i(i, 0, j).add(yinYangPosition);
                if (chunk.getRegion().contains(chunkBlockPosition)) {
                    chunk.setBlock(ChunkMath.calcRelativeBlockPos(chunkBlockPosition, tempPos), getBlock(blockString));
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

    public static boolean circle(
            int x,
            int y,
            int c,
            int r
    ) {
        return
                (r * r) >= ((x / 2) * x) + ((y - c) * y);
    }

    public static String pixel(int x, int y, int r) {
        return Stream.<Map<BooleanSupplier, Supplier<String>>>of(
                singletonMap(
                        () -> circle(x, y, -r / 2, r / 6),
                        () -> "LightAndShadowResources:blackfloorblock"
                ),
                singletonMap(
                        () -> circle(x, y, r / 2, r / 6),
                        () -> "LightAndShadowResources:redfloorblock"
                ),
                singletonMap(
                        () -> circle(x, y, -r / 2, r / 2),
                        () -> "LightAndShadowResources:redfloorblock"
                ),
                singletonMap(
                        () -> circle(x, y, r / 2, r / 2),
                        () -> "LightAndShadowResources:blackfloorblock"
                ),
                singletonMap(
                        () -> circle(x, y, 0, r),
                        () -> x < 0 ? "LightAndShadowResources:redfloorblock" : "LightAndShadowResources:blackfloorblock"
                )
        )
                .sequential()
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .filter(e -> e.getKey().getAsBoolean())
                .map(Map.Entry::getValue)
                .map(Supplier::get)
                .findAny()
                .orElse("engine:air");
    }
}
