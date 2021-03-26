// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.las.platform;

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.cities.BlockTheme;
import org.terasology.cities.BlockType;
import org.terasology.cities.DefaultBlockType;
import org.terasology.cities.raster.AbstractPen;
import org.terasology.cities.raster.ChunkRasterTarget;
import org.terasology.cities.raster.Pen;
import org.terasology.cities.raster.RasterUtil;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.block.BlockRegionc;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.engine.world.generation.Region;
import org.terasology.engine.world.generation.WorldRasterizerPlugin;
import org.terasology.engine.world.generator.plugin.RegisterPlugin;

/**
 *
 */
@RegisterPlugin
public class FloatingPlatformRasterizer implements WorldRasterizerPlugin {

    private BlockTheme theme;
    private static final Block RED_DICE = CoreRegistry.get(BlockManager.class).getBlock("LightAndShadowResources:redDice");
    private static final Block BLACK_DICE = CoreRegistry.get(BlockManager.class).getBlock("LightAndShadowResources:blackDice");

    @Override
    public void initialize() {
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        theme = BlockTheme.builder(blockManager)
                .register(DefaultBlockType.ROOF_FLAT, "LightAndShadowResources:MagicPlank")
                .register(DefaultBlockType.FENCE, "LightAndShadowResources:MagicGlass")
                .register(DefaultBlockType.BUILDING_WALL, "LightAndShadowResources:MagicStone")
                .build();
    }

    @Override
    public void generateChunk(Chunk chunk, Region chunkRegion) {

        FloatingPlatformFacet platformFacet = chunkRegion.getFacet(FloatingPlatformFacet.class);

        if (platformFacet.getPlatforms().isEmpty()) {
            return;
        }

        ChunkRasterTarget target = new ChunkRasterTarget(chunk, theme);

        BlockRegion reg = chunkRegion.getRegion();

        int wallHeight = 5;
        for (FloatingPlatform platform : platformFacet.getPlatforms()) {
            int base = platform.getBaseHeight();
            if (reg.minY() <= base && reg.maxY() >= base) {
                Pen floorPen = new AbstractPen(target.getAffectedArea()) {

                    @Override
                    public void draw(int x, int z) {
                        int bx = (x / 4) % 3;
                        int bz = (z / 3) % 3;
                        BlockType type = (bx == 0 || bz == 0) ? DefaultBlockType.ROOF_FLAT : DefaultBlockType.FENCE;
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
                        BlockType type = (x % 14 == 0) && (z % 14 == 0) ?
                                DefaultBlockType.BUILDING_WALL : DefaultBlockType.FENCE;

                        for (int y = bot; y <= top; y++) {
                            target.setBlock(x, y, z, type);
                        }
                    }
                };
                RasterUtil.drawRect(wallPen, platform.getArea());
            }
            BlockRegionc blackTeleporterRegion = platform.getBlackTeleporterRegion();
            BlockRegionc redTeleporterRegion = platform.getRedTeleporterRegion();

            Vector3i tempPos = new Vector3i();
            for (Vector3ic blackTeleporterPosition : blackTeleporterRegion) {
                //set down the teleporter at every square in the designated region
                if (chunkRegion.getRegion().contains(blackTeleporterPosition)) {
                    chunk.setBlock(Chunks.toRelative(blackTeleporterPosition, tempPos), BLACK_DICE);
                }
            }

            for (Vector3ic redTeleporterPosition : redTeleporterRegion) {
                //set down the teleporter at every square in the designated region
                if (chunkRegion.getRegion().contains(redTeleporterPosition)) {
                    chunk.setBlock(Chunks.toRelative(redTeleporterPosition, tempPos), RED_DICE);
                }
            }
        }
    }
}
