/*
 * Copyright 2015 MovingBlocks
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

package org.terasology.las.platform;

import org.terasology.cities.BlockTheme;
import org.terasology.cities.BlockTypes;
import org.terasology.cities.raster.AbstractPen;
import org.terasology.cities.raster.ChunkRasterTarget;
import org.terasology.cities.raster.Pen;
import org.terasology.cities.raster.RasterUtil;
import org.terasology.math.Region3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.WorldRasterizerPlugin;
import org.terasology.world.generator.plugin.RegisterPlugin;

/**
 *
 */
@RegisterPlugin
public class FloatingPlatformRasterizer implements WorldRasterizerPlugin {

    private BlockTheme theme;

    @Override
    public void initialize() {
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        theme = BlockTheme.builder(blockManager)
                .register(BlockTypes.ROOF_FLAT, "LightAndShadowResources:MagicPlank")
                .register(BlockTypes.FENCE, "LightAndShadowResources:MagicGlass")
                .register(BlockTypes.BUILDING_WALL, "LightAndShadowResources:MagicStone")
                .build();
    }

    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {

        FloatingPlatformFacet platformFacet = chunkRegion.getFacet(FloatingPlatformFacet.class);

        if (platformFacet.getPlatforms().isEmpty()) {
            return;
        }

        ChunkRasterTarget target = new ChunkRasterTarget(chunk, theme);

        Region3i reg = chunkRegion.getRegion();

        int wallHeight = 5;
        for (FloatingPlatform platform : platformFacet.getPlatforms()) {
            int base = platform.getBaseHeight();
            if (reg.minY() <= base && reg.maxY() >= base) {
                Pen floorPen = new AbstractPen(target.getAffectedArea()) {

                    @Override
                    public void draw(int x, int z) {
                        int bx = (x / 4) % 3;
                        int bz = (z / 3) % 3;
                        BlockTypes type = (bx == 0 || bz == 0) ? BlockTypes.ROOF_FLAT : BlockTypes.FENCE;
                        target.setBlock(x, base, z, type);
                    }
                };
                RasterUtil.fillRect(floorPen, platform.getArea());
            }

            if (reg.minY() <= base + wallHeight && reg.maxY() >= base) {

                Pen wallPen = new AbstractPen(target.getAffectedArea()) {
                    int bot = Math.max(target.getMinHeight(), base + 1);
                    int top = Math.min(target.getMaxHeight(), base + wallHeight - 1);  // top layer is exclusive

                    @Override
                    public void draw(int x, int z) {
                        BlockTypes type = (x % 14 == 0) && (z % 14 == 0) ?
                                BlockTypes.BUILDING_WALL : BlockTypes.FENCE;

                        for (int y = bot; y <= top; y++) {
                            target.setBlock(x, y, z, type);
                        }
                    }
                };
                RasterUtil.drawRect(wallPen, platform.getArea());
            }
        }
    }

}
