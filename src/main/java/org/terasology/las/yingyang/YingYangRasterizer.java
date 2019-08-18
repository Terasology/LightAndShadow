/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.las.yingyang;

import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.math.ChunkMath;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3i;
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
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Collections.singletonMap;

@RegisterPlugin
public class YingYangRasterizer implements WorldRasterizerPlugin {
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
        YingYangFacet yingYangFacet = chunkRegion.getFacet(YingYangFacet.class);
        if (yingYangFacet.getYingYangs().isEmpty()) {
            return;
        }
        Region3i reg = chunkRegion.getRegion();
        for (YingYang yingYang: yingYangFacet.getYingYangs()) {
            Vector3i yingYangPosition = yingYang.getCenterPosition();
            if (chunkRegion.getRegion().encompasses(yingYangPosition)) {
                int r = 5;
                for (int i = -r; i <= r; i++) {
                    for (int j = -2 * r; j <= 2 * r; j++) {
                        String blockString = pixel(j, i, r);
                        Vector3i chunkBlockPosition = new Vector3i(i, 0, j).add(yingYangPosition);
                        if (chunk.getRegion().encompasses(chunkBlockPosition)) {
                            chunk.setBlock(ChunkMath.calcBlockPos(chunkBlockPosition), getBlock(blockString));
                        }
                    }
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
